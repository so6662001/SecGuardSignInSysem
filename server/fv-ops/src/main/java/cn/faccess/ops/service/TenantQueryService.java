package cn.faccess.ops.service;

import cn.faccess.common.exception.BizException;
import cn.faccess.common.web.PageResult;
import cn.faccess.ops.entity.TenantSubscription;
import cn.faccess.ops.mapper.TenantSubscriptionMapper;
import cn.faccess.system.entity.SysTenant;
import cn.faccess.system.mapper.SysTenantMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 运营端租户查询。
 */
@Service
public class TenantQueryService {

    private final SysTenantMapper tenantMapper;
    private final TenantSubscriptionMapper subscriptionMapper;

    public TenantQueryService(SysTenantMapper tenantMapper, TenantSubscriptionMapper subscriptionMapper) {
        this.tenantMapper = tenantMapper;
        this.subscriptionMapper = subscriptionMapper;
    }

    public PageResult<SysTenant> page(Integer status, String keyword, long page, long size) {
        IPage<SysTenant> p = tenantMapper.selectPage(new Page<>(page, size),
                Wrappers.<SysTenant>lambdaQuery()
                        .eq(status != null, SysTenant::getStatus, status)
                        .like(StringUtils.hasText(keyword), SysTenant::getName, keyword)
                        .orderByDesc(SysTenant::getId));
        return new PageResult<>(p.getRecords(), p.getTotal(), page, size);
    }

    public Map<String, Object> detail(Long id) {
        SysTenant tenant = tenantMapper.selectById(id);
        if (tenant == null) throw new BizException(404, "租户不存在");
        TenantSubscription sub = subscriptionMapper.selectOne(
                Wrappers.<TenantSubscription>lambdaQuery().eq(TenantSubscription::getTenantId, id).last("limit 1"));
        Map<String, Object> m = new HashMap<>();
        m.put("tenant", tenant);
        m.put("subscription", sub);
        return m;
    }
}
