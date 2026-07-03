package cn.faccess.visitor.service;

import cn.faccess.common.exception.BizException;
import cn.faccess.common.tenant.TenantContext;
import cn.faccess.common.web.PageResult;
import cn.faccess.notify.service.NotifyService;
import cn.faccess.system.entity.SysUser;
import cn.faccess.system.mapper.SysUserMapper;
import cn.faccess.visitor.dto.CheckinReq;
import cn.faccess.visitor.entity.VisitApproval;
import cn.faccess.visitor.entity.VisitRecord;
import cn.faccess.visitor.mapper.VisitApprovalMapper;
import cn.faccess.visitor.mapper.VisitRecordMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 访客进出核心：入场登记（黑名单拦截+访客牌+审批+通知）、审批、在场/离场、台账。
 */
@Service
public class VisitService {

    private static final String TOKEN_PREFIX = "approve:token:";

    private final VisitRecordMapper recordMapper;
    private final VisitApprovalMapper approvalMapper;
    private final BadgeService badgeService;
    private final BlacklistService blacklistService;
    private final NotifyService notifyService;
    private final SysUserMapper userMapper;
    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;

    @Value("${fv.public-base-url:http://localhost:8080}")
    private String baseUrl;

    public VisitService(VisitRecordMapper recordMapper, VisitApprovalMapper approvalMapper, BadgeService badgeService,
                        BlacklistService blacklistService, NotifyService notifyService, SysUserMapper userMapper,
                        StringRedisTemplate redis, ObjectMapper objectMapper) {
        this.recordMapper = recordMapper;
        this.approvalMapper = approvalMapper;
        this.badgeService = badgeService;
        this.blacklistService = blacklistService;
        this.notifyService = notifyService;
        this.userMapper = userMapper;
        this.redis = redis;
        this.objectMapper = objectMapper;
    }

    @Transactional(rollbackFor = Exception.class)
    public VisitRecord checkin(CheckinReq req) {
        String hit = blacklistService.hit(req.getVisitorMobile(), req.getCompany());
        if ("BLACK".equals(hit)) {
            throw new BizException("该访客/单位在黑名单中，禁止登记");
        }

        VisitRecord r = new VisitRecord();
        r.setGateId(req.getGateId());
        r.setTemplateId(req.getTemplateId());
        r.setVisitorName(req.getVisitorName());
        r.setVisitorMobile(req.getVisitorMobile());
        r.setCompany(req.getCompany());
        r.setReason(req.getReason());
        r.setHostName(req.getHostName());
        r.setHostUserId(req.getHostUserId());
        r.setHostDept(req.getHostDept());
        r.setCompanions(req.getCompanions() == null ? 1 : req.getCompanions());
        r.setPlateNo(req.getPlateNo());
        r.setRegisterType(req.getRegisterType() == null ? "GUARD" : req.getRegisterType());
        r.setRegisterBy(TenantContext.getUserId());
        r.setStatus("PENDING");
        r.setExtFields(toJson(req.getExtFields()));
        r.setDevices(toJson(req.getDevices()));
        recordMapper.insert(r);

        r.setBadgeNo(badgeService.allocate(r.getId()));
        recordMapper.updateById(r);

        // 创建审批 + 通知被访人
        VisitApproval approval = new VisitApproval();
        approval.setRecordId(r.getId());
        approval.setHostUserId(req.getHostUserId());
        approval.setStatus("WAITING");
        approval.setTrace("[\"" + LocalDateTime.now() + " 提交到访申请\"]");
        approvalMapper.insert(approval);

        notifyHost(r, approval);
        return r;
    }

    private void notifyHost(VisitRecord r, VisitApproval approval) {
        String hostMobile = null;
        if (r.getHostUserId() != null) {
            SysUser host = userMapper.selectById(r.getHostUserId());
            if (host != null) hostMobile = host.getMobile();
        }
        Long tenantId = TenantContext.getTenantId();
        // 生成免登录审批短链 token（10 分钟有效）
        String token = UUID.randomUUID().toString().replace("-", "");
        redis.opsForValue().set(TOKEN_PREFIX + token,
                tenantId + ":" + approval.getId(), Duration.ofMinutes(10));

        Map<String, Object> vars = new HashMap<>();
        vars.put("visitor", r.getVisitorName());
        vars.put("unit", r.getCompany());
        vars.put("company", "厂智访客");
        vars.put("reason", r.getReason());
        vars.put("link", baseUrl + "/api/public/approve/" + token);
        notifyService.notify("visit_approve", tenantId, approval.getId(),
                r.getHostUserId(), hostMobile, vars, 180);
    }

    /** 被访人/审核端决策。 */
    @Transactional(rollbackFor = Exception.class)
    public void decide(Long approvalId, boolean pass, String validScope, String channel, Long approverId) {
        VisitApproval approval = approvalMapper.selectById(approvalId);
        if (approval == null) throw new BizException(404, "审批不存在");
        VisitRecord r = recordMapper.selectById(approval.getRecordId());
        if (r == null) throw new BizException(404, "记录不存在");
        if (!"WAITING".equals(approval.getStatus()) && !"ESCALATED".equals(approval.getStatus())) {
            throw new BizException("该申请已处理");
        }
        applyDecision(approval, r, pass, validScope, channel, approverId);
    }

    /** 免登录 token 决策。 */
    @Transactional(rollbackFor = Exception.class)
    public void decideByToken(String token, boolean pass, String validScope) {
        String val = redis.opsForValue().get(TOKEN_PREFIX + token);
        if (val == null) throw new BizException(400, "审批链接已失效");
        String[] parts = val.split(":");
        Long tenantId = Long.valueOf(parts[0]);
        Long approvalId = Long.valueOf(parts[1]);
        // 免登录场景无租户上下文，手动设置以便拦截器与写库正确
        TenantContext.set(tenantId, null, "public-approve", java.util.List.of());
        try {
            decide(approvalId, pass, validScope, "SMS_LINK", null);
            redis.delete(TOKEN_PREFIX + token);
        } finally {
            TenantContext.clear();
        }
    }

    private void applyDecision(VisitApproval approval, VisitRecord r, boolean pass,
                               String validScope, String channel, Long approverId) {
        approval.setStatus(pass ? "APPROVED" : "REJECTED");
        approval.setApproverId(approverId);
        approval.setChannel(channel);
        approval.setValidScope(validScope);
        if (approval.getCreateTime() != null) {
            approval.setCostSeconds((int) ChronoUnit.SECONDS.between(approval.getCreateTime(), LocalDateTime.now()));
        }
        approval.setTrace(appendTrace(approval.getTrace(), (pass ? "同意接待" : "拒绝") + " · 渠道 " + channel));
        approvalMapper.updateById(approval);

        if (pass) {
            r.setStatus("ONSITE");
            if (r.getInTime() == null) r.setInTime(LocalDateTime.now());
            recordMapper.updateById(r);
            // 通知访客已放行
            Map<String, Object> vars = new HashMap<>();
            vars.put("visitor", r.getVisitorName());
            notifyService.notify("visit_result", TenantContext.getTenantId(), r.getId(), null, r.getVisitorMobile(), vars, null);
        } else {
            r.setStatus("REJECTED");
            recordMapper.updateById(r);
            badgeService.release(r.getId());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void checkout(Long recordId) {
        VisitRecord r = recordMapper.selectById(recordId);
        if (r == null) throw new BizException(404, "记录不存在");
        if (!"ONSITE".equals(r.getStatus()) && !"OVERSTAY".equals(r.getStatus())) {
            throw new BizException("该访客不在场");
        }
        r.setStatus("LEFT");
        r.setOutTime(LocalDateTime.now());
        recordMapper.updateById(r);
        badgeService.release(recordId);
    }

    /** 审核端通过/驳回（等价于对该记录审批做决策）。 */
    @Transactional(rollbackFor = Exception.class)
    public void audit(Long recordId, boolean pass, String remark) {
        VisitApproval approval = approvalMapper.selectOne(Wrappers.<VisitApproval>lambdaQuery()
                .eq(VisitApproval::getRecordId, recordId).last("limit 1"));
        if (approval == null) throw new BizException(404, "审批不存在");
        decide(approval.getId(), pass, "ONCE", "AUDIT", TenantContext.getUserId());
    }

    public PageResult<VisitRecord> page(String status, String reason, String keyword, long page, long size) {
        IPage<VisitRecord> p = recordMapper.selectPage(new Page<>(page, size),
                Wrappers.<VisitRecord>lambdaQuery()
                        .eq(StringUtils.hasText(status), VisitRecord::getStatus, status)
                        .eq(StringUtils.hasText(reason), VisitRecord::getReason, reason)
                        .and(StringUtils.hasText(keyword), w -> w
                                .like(VisitRecord::getVisitorName, keyword).or().like(VisitRecord::getVisitorMobile, keyword)
                                .or().like(VisitRecord::getCompany, keyword))
                        .orderByDesc(VisitRecord::getId));
        return new PageResult<>(p.getRecords(), p.getTotal(), page, size);
    }

    public PageResult<VisitRecord> onsite(long page, long size) {
        IPage<VisitRecord> p = recordMapper.selectPage(new Page<>(page, size),
                Wrappers.<VisitRecord>lambdaQuery()
                        .in(VisitRecord::getStatus, "ONSITE", "OVERSTAY")
                        .orderByDesc(VisitRecord::getInTime));
        return new PageResult<>(p.getRecords(), p.getTotal(), page, size);
    }

    public Map<String, Object> detail(Long id) {
        VisitRecord r = recordMapper.selectById(id);
        if (r == null) throw new BizException(404, "记录不存在");
        VisitApproval approval = approvalMapper.selectOne(Wrappers.<VisitApproval>lambdaQuery()
                .eq(VisitApproval::getRecordId, id).last("limit 1"));
        Map<String, Object> m = new HashMap<>();
        m.put("record", r);
        m.put("approval", approval);
        return m;
    }

    /** 免登录审批页：按 token 返回访客与到访信息。 */
    public Map<String, Object> viewByToken(String token) {
        String val = redis.opsForValue().get(TOKEN_PREFIX + token);
        if (val == null) throw new BizException(400, "审批链接已失效");
        String[] parts = val.split(":");
        Long tenantId = Long.valueOf(parts[0]);
        Long approvalId = Long.valueOf(parts[1]);
        TenantContext.set(tenantId, null, "public-approve", java.util.List.of());
        try {
            VisitApproval approval = approvalMapper.selectById(approvalId);
            if (approval == null) throw new BizException(400, "审批不存在");
            VisitRecord r = recordMapper.selectById(approval.getRecordId());
            Map<String, Object> m = new HashMap<>();
            m.put("visitorName", r.getVisitorName());
            m.put("company", r.getCompany());
            m.put("reason", r.getReason());
            m.put("hostName", r.getHostName());
            m.put("companions", r.getCompanions());
            m.put("applyTime", r.getCreateTime());
            m.put("status", approval.getStatus());
            m.put("expired", !"WAITING".equals(approval.getStatus()) && !"ESCALATED".equals(approval.getStatus()));
            return m;
        } finally {
            TenantContext.clear();
        }
    }

    private String toJson(Object o) {
        if (o == null) return null;
        try {
            return objectMapper.writeValueAsString(o);
        } catch (Exception e) {
            return null;
        }
    }

    private String appendTrace(String trace, String step) {
        String entry = LocalDateTime.now() + " " + step;
        if (trace == null || trace.isBlank()) return "[\"" + entry + "\"]";
        return trace.substring(0, trace.length() - 1) + ",\"" + entry + "\"]";
    }
}
