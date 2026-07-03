package cn.faccess.visitor.schedule;

import cn.faccess.common.tenant.TenantContext;
import cn.faccess.notify.service.NotifyService;
import cn.faccess.system.entity.SysUser;
import cn.faccess.system.mapper.SysUserMapper;
import cn.faccess.visitor.entity.VisitApproval;
import cn.faccess.visitor.entity.VisitRecord;
import cn.faccess.visitor.mapper.VisitApprovalMapper;
import cn.faccess.visitor.mapper.VisitRecordMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 审批超时自动升级：定时扫描待响应审批，超时则二次提醒（换渠道）并标记升级。
 * 生产可用 RabbitMQ 延时队列替代；此处用调度扫描实现，无需额外中间件即可运行。
 */
@Component
public class EscalationScheduler {

    private static final Logger log = LoggerFactory.getLogger(EscalationScheduler.class);

    private final VisitApprovalMapper approvalMapper;
    private final VisitRecordMapper recordMapper;
    private final SysUserMapper userMapper;
    private final NotifyService notifyService;

    @Value("${fv.approval.timeout-seconds:180}")
    private int timeoutSeconds;

    public EscalationScheduler(VisitApprovalMapper approvalMapper, VisitRecordMapper recordMapper,
                               SysUserMapper userMapper, NotifyService notifyService) {
        this.approvalMapper = approvalMapper;
        this.recordMapper = recordMapper;
        this.userMapper = userMapper;
        this.notifyService = notifyService;
    }

    @Scheduled(fixedDelayString = "${fv.approval.scan-interval-ms:30000}")
    public void scan() {
        // 无租户上下文：拦截器跳过租户过滤，扫描全部租户的待响应审批
        List<VisitApproval> waiting = approvalMapper.selectList(
                Wrappers.<VisitApproval>lambdaQuery().eq(VisitApproval::getStatus, "WAITING"));
        LocalDateTime now = LocalDateTime.now();
        for (VisitApproval a : waiting) {
            if (a.getCreateTime() == null) continue;
            long age = ChronoUnit.SECONDS.between(a.getCreateTime(), now);
            if (age < timeoutSeconds) continue;
            escalate(a);
        }
    }

    private void escalate(VisitApproval a) {
        VisitRecord r = recordMapper.selectById(a.getRecordId());
        if (r == null) return;
        String hostMobile = null;
        if (r.getHostUserId() != null) {
            SysUser host = userMapper.selectById(r.getHostUserId());
            if (host != null) hostMobile = host.getMobile();
        }
        TenantContext.set(a.getTenantId(), null, "escalation", List.of());
        try {
            Map<String, Object> vars = new HashMap<>();
            vars.put("visitor", r.getVisitorName());
            vars.put("unit", r.getCompany());
            vars.put("company", "厂智访客");
            vars.put("reason", r.getReason());
            // 二次提醒改用语音渠道
            notifyService.sendVia(a.getTenantId(), a.getId(), "visit_approve", "VOICE", hostMobile, vars);

            a.setStatus("ESCALATED");
            String entry = LocalDateTime.now() + " 超时未响应，二次提醒并升级";
            a.setTrace(a.getTrace() == null ? "[\"" + entry + "\"]"
                    : a.getTrace().substring(0, a.getTrace().length() - 1) + ",\"" + entry + "\"]");
            approvalMapper.updateById(a);
            log.info("审批 {} 超时升级（访客 {}）", a.getId(), r.getVisitorName());
        } finally {
            TenantContext.clear();
        }
    }
}
