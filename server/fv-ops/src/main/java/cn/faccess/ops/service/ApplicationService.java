package cn.faccess.ops.service;

import cn.faccess.common.exception.BizException;
import cn.faccess.common.tenant.TenantContext;
import cn.faccess.common.web.PageResult;
import cn.faccess.ops.dto.ApplyReq;
import cn.faccess.ops.entity.BizPlan;
import cn.faccess.ops.entity.TenantApplication;
import cn.faccess.ops.entity.TenantSubscription;
import cn.faccess.ops.mapper.BizPlanMapper;
import cn.faccess.ops.mapper.TenantApplicationMapper;
import cn.faccess.ops.mapper.TenantSubscriptionMapper;
import cn.faccess.system.service.TenantProvisionService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 企业开通申请：公开申请 + 运营审核开通（创建租户/超管/订阅）。
 */
@Service
public class ApplicationService {

    private static final AtomicInteger SEQ = new AtomicInteger(0);

    private final TenantApplicationMapper applicationMapper;
    private final TenantSubscriptionMapper subscriptionMapper;
    private final BizPlanMapper planMapper;
    private final TenantProvisionService provisionService;
    private final PasswordEncoder passwordEncoder;

    public ApplicationService(TenantApplicationMapper applicationMapper, TenantSubscriptionMapper subscriptionMapper,
                              BizPlanMapper planMapper, TenantProvisionService provisionService,
                              PasswordEncoder passwordEncoder) {
        this.applicationMapper = applicationMapper;
        this.subscriptionMapper = subscriptionMapper;
        this.planMapper = planMapper;
        this.provisionService = provisionService;
        this.passwordEncoder = passwordEncoder;
    }

    /** 公开：企业提交开通申请。 */
    public String apply(ApplyReq req) {
        Long dup = applicationMapper.selectCount(Wrappers.<TenantApplication>lambdaQuery()
                .eq(TenantApplication::getContactMobile, req.getContactMobile())
                .eq(TenantApplication::getStatus, 1));
        if (dup != null && dup > 0) {
            throw new BizException("您有一条待审核的申请，请勿重复提交");
        }
        TenantApplication app = new TenantApplication();
        app.setAppNo(genAppNo());
        app.setCompanyName(req.getCompanyName());
        app.setCreditCode(req.getCreditCode());
        app.setIndustry(req.getIndustry());
        app.setAddress(req.getAddress());
        app.setSiteCount(req.getSiteCount() == null ? 1 : req.getSiteCount());
        app.setContactName(req.getContactName());
        app.setContactMobile(req.getContactMobile());
        app.setContactTitle(req.getContactTitle());
        app.setEmail(req.getEmail());
        app.setPlanCode(req.getPlanCode());
        app.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        app.setStatus(1);
        applicationMapper.insert(app);
        return app.getAppNo();
    }

    public PageResult<TenantApplication> page(Integer status, String keyword, long page, long size) {
        IPage<TenantApplication> p = applicationMapper.selectPage(new Page<>(page, size),
                Wrappers.<TenantApplication>lambdaQuery()
                        .eq(status != null, TenantApplication::getStatus, status)
                        .like(StringUtils.hasText(keyword), TenantApplication::getCompanyName, keyword)
                        .orderByDesc(TenantApplication::getId));
        p.getRecords().forEach(a -> a.setPasswordHash(null));
        return new PageResult<>(p.getRecords(), p.getTotal(), page, size);
    }

    public TenantApplication detail(Long id) {
        TenantApplication app = applicationMapper.selectById(id);
        if (app == null) throw new BizException(404, "申请不存在");
        app.setPasswordHash(null);
        return app;
    }

    /** 运营审核通过并开通租户。 */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> approve(Long id, String domain, String remark) {
        TenantApplication app = applicationMapper.selectById(id);
        if (app == null) throw new BizException(404, "申请不存在");
        if (app.getStatus() != null && app.getStatus() == 2) throw new BizException("该申请已开通");

        String finalDomain = StringUtils.hasText(domain) ? domain : ("t" + app.getId());

        Long tenantId = provisionService.provision(TenantProvisionService.ProvisionCmd.builder()
                .companyName(app.getCompanyName())
                .creditCode(app.getCreditCode())
                .industry(app.getIndustry())
                .address(app.getAddress())
                .siteCount(app.getSiteCount())
                .domain(finalDomain)
                .adminUsername(app.getContactMobile())
                .adminPasswordHash(app.getPasswordHash())
                .adminRealName(app.getContactName())
                .adminMobile(app.getContactMobile())
                .build());

        // 创建订阅（14 天试用）
        BizPlan plan = planMapper.selectOne(Wrappers.<BizPlan>lambdaQuery().eq(BizPlan::getCode, app.getPlanCode()));
        TenantSubscription sub = new TenantSubscription();
        sub.setTenantId(tenantId);
        sub.setPlanCode(app.getPlanCode());
        sub.setSiteCount(app.getSiteCount());
        boolean custom = plan != null && plan.getIsCustom() != null && plan.getIsCustom() == 1;
        BigDecimal monthly = (plan == null || custom) ? BigDecimal.ZERO
                : plan.getMonthlyPrice().multiply(BigDecimal.valueOf(app.getSiteCount()));
        sub.setMonthlyFee(monthly);
        sub.setPeriod("MONTH");
        sub.setTrialEnd(LocalDate.now().plusDays(14));
        sub.setNextRenew(LocalDate.now().plusDays(14));
        sub.setStatus(1);
        subscriptionMapper.insert(sub);

        // 更新申请
        app.setStatus(2);
        app.setTenantId(tenantId);
        app.setDomain(finalDomain);
        app.setAuditBy(TenantContext.getUserId());
        app.setAuditRemark(remark);
        applicationMapper.updateById(app);

        Map<String, Object> res = new HashMap<>();
        res.put("tenantId", tenantId);
        res.put("domain", finalDomain);
        res.put("adminUsername", app.getContactMobile());
        res.put("trialEnd", sub.getTrialEnd());
        return res;
    }

    public void reject(Long id, String remark) {
        updateStatus(id, 3, remark);
    }

    public void requestMaterials(Long id, String remark) {
        updateStatus(id, 4, remark);
    }

    private void updateStatus(Long id, int status, String remark) {
        TenantApplication app = applicationMapper.selectById(id);
        if (app == null) throw new BizException(404, "申请不存在");
        app.setStatus(status);
        app.setAuditRemark(remark);
        app.setAuditBy(TenantContext.getUserId());
        applicationMapper.updateById(app);
    }

    private String genAppNo() {
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        return "AP-" + date + "-" + String.format("%04d", SEQ.incrementAndGet());
    }
}
