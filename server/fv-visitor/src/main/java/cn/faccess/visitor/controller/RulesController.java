package cn.faccess.visitor.controller;

import cn.faccess.common.tenant.TenantContext;
import cn.faccess.common.web.R;
import cn.faccess.visitor.entity.TenantRules;
import cn.faccess.visitor.mapper.TenantRulesMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "tenant-rules", description = "登记规则")
@RestController
@RequestMapping("/api/tenant/rules")
public class RulesController {

    private final TenantRulesMapper rulesMapper;

    public RulesController(TenantRulesMapper rulesMapper) {
        this.rulesMapper = rulesMapper;
    }

    @Operation(summary = "获取登记规则")
    @GetMapping
    public R<TenantRules> get() {
        TenantRules rules = rulesMapper.selectOne(Wrappers.<TenantRules>lambdaQuery()
                .eq(TenantRules::getTenantId, TenantContext.getTenantId()).last("limit 1"));
        if (rules == null) {
            rules = new TenantRules();
            rulesMapper.insert(rules);
        }
        return R.ok(rules);
    }

    @Operation(summary = "保存登记规则")
    @PutMapping
    public R<Void> save(@RequestBody TenantRules body) {
        TenantRules rules = rulesMapper.selectOne(Wrappers.<TenantRules>lambdaQuery()
                .eq(TenantRules::getTenantId, TenantContext.getTenantId()).last("limit 1"));
        if (rules == null) {
            rulesMapper.insert(body);
        } else {
            body.setId(rules.getId());
            rulesMapper.updateById(body);
        }
        return R.ok();
    }
}
