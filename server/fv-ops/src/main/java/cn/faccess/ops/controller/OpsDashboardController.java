package cn.faccess.ops.controller;

import cn.faccess.common.web.R;
import cn.faccess.ops.entity.TenantSubscription;
import cn.faccess.ops.service.OpsDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "ops-dashboard", description = "运营概览")
@RestController
@RequestMapping("/api/ops")
@PreAuthorize("hasRole('PLATFORM_ADMIN') or hasRole('PLATFORM_OPS')")
public class OpsDashboardController {

    private static final Logger log = LoggerFactory.getLogger(OpsDashboardController.class);
    private final OpsDashboardService service;

    public OpsDashboardController(OpsDashboardService service) {
        this.service = service;
    }

    @Operation(summary = "运营概览指标")
    @GetMapping("/overview")
    public R<Map<String, Object>> overview() {
        return R.ok(service.overview());
    }

    @Operation(summary = "近6月收入趋势")
    @GetMapping("/revenue-trend")
    public R<List<Map<String, Object>>> revenueTrend() {
        return R.ok(service.revenueTrend());
    }

    @Operation(summary = "到期预警")
    @GetMapping("/expiring")
    public R<List<TenantSubscription>> expiring() {
        return R.ok(service.expiring());
    }

    @Operation(summary = "一键催续")
    @PostMapping("/expiring/notify")
    public R<Void> notifyExpiring() {
        int n = service.expiring().size();
        log.info("[催续] 已向 {} 家即将到期租户发送续费提醒", n);
        return R.ok();
    }
}
