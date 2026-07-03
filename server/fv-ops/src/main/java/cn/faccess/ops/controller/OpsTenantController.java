package cn.faccess.ops.controller;

import cn.faccess.common.web.PageResult;
import cn.faccess.common.web.R;
import cn.faccess.ops.service.TenantQueryService;
import cn.faccess.system.entity.SysTenant;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "ops-tenant", description = "租户管理")
@RestController
@RequestMapping("/api/ops/tenants")
@PreAuthorize("hasRole('PLATFORM_ADMIN') or hasRole('PLATFORM_OPS')")
public class OpsTenantController {

    private final TenantQueryService tenantQueryService;

    public OpsTenantController(TenantQueryService tenantQueryService) {
        this.tenantQueryService = tenantQueryService;
    }

    @Operation(summary = "租户列表")
    @GetMapping
    public R<PageResult<SysTenant>> list(@RequestParam(required = false) Integer status,
                                         @RequestParam(required = false) String keyword,
                                         @RequestParam(defaultValue = "1") long page,
                                         @RequestParam(defaultValue = "20") long size) {
        return R.ok(tenantQueryService.page(status, keyword, page, size));
    }

    @Operation(summary = "租户详情")
    @GetMapping("/{id}")
    public R<Map<String, Object>> detail(@PathVariable Long id) {
        return R.ok(tenantQueryService.detail(id));
    }
}
