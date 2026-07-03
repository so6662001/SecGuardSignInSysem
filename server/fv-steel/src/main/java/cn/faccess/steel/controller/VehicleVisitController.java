package cn.faccess.steel.controller;

import cn.faccess.common.web.PageResult;
import cn.faccess.common.web.R;
import cn.faccess.steel.adapter.TradePlatformAdapter;
import cn.faccess.steel.dto.VehicleVisitReq;
import cn.faccess.steel.entity.VehicleVisit;
import cn.faccess.steel.service.VehicleVisitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "steel-vehicle", description = "车辆到厂（钢铁专属）")
@RestController
@RequestMapping("/api/tenant/steel")
@PreAuthorize("hasAuthority('steel:vehicle') or hasRole('TENANT_ADMIN')")
public class VehicleVisitController {

    private final VehicleVisitService service;

    public VehicleVisitController(VehicleVisitService service) {
        this.service = service;
    }

    @Operation(summary = "核验并带出交易平台单据")
    @GetMapping("/trade/order/{no}")
    public R<TradePlatformAdapter.TradeOrderView> verifyOrder(@PathVariable("no") String orderNo,
                                                              @RequestParam(defaultValue = "SALES") String type) {
        return R.ok(service.verifyOrder(orderNo, type));
    }

    @Operation(summary = "车辆到厂登记（送货/提货）")
    @PostMapping("/vehicle-visits")
    public R<VehicleVisit> checkin(@Valid @RequestBody VehicleVisitReq req) {
        return R.ok(service.checkin(req));
    }

    @Operation(summary = "在厂车辆排队列表")
    @GetMapping("/vehicle-visits")
    public R<PageResult<VehicleVisit>> queue(@RequestParam(required = false) String direction,
                                             @RequestParam(defaultValue = "1") long page,
                                             @RequestParam(defaultValue = "50") long size) {
        return R.ok(service.queue(direction, page, size));
    }

    @Operation(summary = "叫号")
    @PostMapping("/vehicle-visits/{id}/call")
    public R<Void> call(@PathVariable Long id) {
        service.call(id);
        return R.ok();
    }

    @Operation(summary = "放行（净重回写交易平台）")
    @PostMapping("/vehicle-visits/{id}/release")
    public R<VehicleVisit> release(@PathVariable Long id) {
        return R.ok(service.release(id));
    }
}
