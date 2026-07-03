package cn.faccess.ops.service;

import cn.faccess.ops.entity.TenantSubscription;
import cn.faccess.ops.mapper.TenantApplicationMapper;
import cn.faccess.ops.mapper.TenantSubscriptionMapper;
import cn.faccess.system.mapper.SysTenantMapper;
import cn.faccess.system.entity.SysTenant;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 运营概览指标、收入趋势、到期预警。
 */
@Service
public class OpsDashboardService {

    private final SysTenantMapper tenantMapper;
    private final TenantSubscriptionMapper subMapper;
    private final TenantApplicationMapper appMapper;

    public OpsDashboardService(SysTenantMapper tenantMapper, TenantSubscriptionMapper subMapper, TenantApplicationMapper appMapper) {
        this.tenantMapper = tenantMapper;
        this.subMapper = subMapper;
        this.appMapper = appMapper;
    }

    public Map<String, Object> overview() {
        long tenantTotal = tenantMapper.selectCount(null);
        long trial = tenantMapper.selectCount(Wrappers.<SysTenant>lambdaQuery().eq(SysTenant::getStatus, 1));
        long pending = appMapper.selectCount(Wrappers.lambdaQuery(new cn.faccess.ops.entity.TenantApplication()).eq(cn.faccess.ops.entity.TenantApplication::getStatus, 1));
        BigDecimal mrr = subMapper.selectList(Wrappers.<TenantSubscription>lambdaQuery().in(TenantSubscription::getStatus, 1, 2))
                .stream().map(s -> s.getMonthlyFee() == null ? BigDecimal.ZERO : s.getMonthlyFee())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Map<String, Object> m = new HashMap<>();
        m.put("tenantTotal", tenantTotal);
        m.put("trial", trial);
        m.put("pending", pending);
        m.put("mrr", mrr);
        return m;
    }

    /** 近 6 月收入趋势（按当前 MRR 估算演示）。 */
    public List<Map<String, Object>> revenueTrend() {
        BigDecimal mrr = (BigDecimal) overview().get("mrr");
        List<Map<String, Object>> list = new ArrayList<>();
        double base = mrr.doubleValue();
        double[] factor = {0.55, 0.66, 0.74, 0.85, 0.93, 1.0};
        for (int i = 0; i < 6; i++) {
            LocalDate month = LocalDate.now().minusMonths(5 - i);
            Map<String, Object> m = new HashMap<>();
            m.put("month", month.getYear() + "-" + String.format("%02d", month.getMonthValue()));
            m.put("revenue", Math.round(base * factor[i]));
            list.add(m);
        }
        return list;
    }

    /** 7 天内到期订阅。 */
    public List<TenantSubscription> expiring() {
        LocalDate limit = LocalDate.now().plusDays(7);
        return subMapper.selectList(Wrappers.<TenantSubscription>lambdaQuery()
                .le(TenantSubscription::getNextRenew, limit)
                .orderByAsc(TenantSubscription::getNextRenew));
    }
}
