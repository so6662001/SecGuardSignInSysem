package cn.faccess.ops.controller;

import cn.faccess.common.tenant.TenantContext;
import cn.faccess.common.web.R;
import cn.faccess.ops.entity.OpsLead;
import cn.faccess.ops.service.ConversionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "ops-conversion", description = "引流转化")
@RestController
@RequestMapping("/api/ops")
@PreAuthorize("hasRole('PLATFORM_ADMIN') or hasRole('PLATFORM_OPS')")
public class OpsConversionController {

    private final ConversionService service;

    public OpsConversionController(ConversionService service) {
        this.service = service;
    }

    @Operation(summary = "转化漏斗")
    @GetMapping("/funnel")
    public R<Map<String, Long>> funnel() {
        return R.ok(service.funnel());
    }

    @Operation(summary = "高潜线索")
    @GetMapping("/leads")
    public R<List<OpsLead>> leads(@RequestParam(required = false) Integer status) {
        return R.ok(service.leads(status));
    }

    @Operation(summary = "派发线索")
    @PostMapping("/leads/{id}/assign")
    public R<Void> assign(@PathVariable Long id) {
        service.assign(id, TenantContext.getUserId());
        return R.ok();
    }
}
