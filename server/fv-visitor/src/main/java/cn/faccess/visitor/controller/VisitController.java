package cn.faccess.visitor.controller;

import cn.faccess.common.web.PageResult;
import cn.faccess.common.web.R;
import cn.faccess.visitor.dto.CheckinReq;
import cn.faccess.visitor.entity.VisitRecord;
import cn.faccess.visitor.service.VisitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "tenant-visit", description = "访客进出")
@RestController
@RequestMapping("/api/tenant/visits")
public class VisitController {

    private final VisitService visitService;

    public VisitController(VisitService visitService) {
        this.visitService = visitService;
    }

    @Operation(summary = "入场登记")
    @PreAuthorize("hasAuthority('visit:checkin') or hasRole('TENANT_ADMIN')")
    @PostMapping
    public R<VisitRecord> checkin(@Valid @RequestBody CheckinReq req) {
        return R.ok(visitService.checkin(req));
    }

    @Operation(summary = "进出记录列表")
    @GetMapping
    public R<PageResult<VisitRecord>> list(@RequestParam(required = false) String status,
                                           @RequestParam(required = false) String reason,
                                           @RequestParam(required = false) String keyword,
                                           @RequestParam(defaultValue = "1") long page,
                                           @RequestParam(defaultValue = "20") long size) {
        return R.ok(visitService.page(status, reason, keyword, page, size));
    }

    @Operation(summary = "在场访客")
    @GetMapping("/onsite")
    public R<PageResult<VisitRecord>> onsite(@RequestParam(defaultValue = "1") long page,
                                             @RequestParam(defaultValue = "50") long size) {
        return R.ok(visitService.onsite(page, size));
    }

    @Operation(summary = "记录详情")
    @GetMapping("/{id}")
    public R<Map<String, Object>> detail(@PathVariable Long id) {
        return R.ok(visitService.detail(id));
    }

    @Operation(summary = "离场登记")
    @PostMapping("/{id}/checkout")
    public R<Void> checkout(@PathVariable Long id) {
        visitService.checkout(id);
        return R.ok();
    }

    @Operation(summary = "审核（通过/驳回）")
    @PostMapping("/{id}/audit")
    public R<Void> audit(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> body) {
        boolean pass = body != null && Boolean.TRUE.equals(body.get("pass"));
        String remark = body == null ? null : (String) body.get("remark");
        visitService.audit(id, pass, remark);
        return R.ok();
    }
}
