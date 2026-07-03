package cn.faccess.ops.controller;

import cn.faccess.common.web.PageResult;
import cn.faccess.common.web.R;
import cn.faccess.ops.entity.TenantApplication;
import cn.faccess.ops.service.ApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "ops-application", description = "开通申请审核")
@RestController
@RequestMapping("/api/ops/applications")
@PreAuthorize("hasRole('PLATFORM_ADMIN') or hasRole('PLATFORM_OPS')")
public class OpsApplicationController {

    private final ApplicationService applicationService;

    public OpsApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @Operation(summary = "开通申请列表")
    @GetMapping
    public R<PageResult<TenantApplication>> list(@RequestParam(required = false) Integer status,
                                                 @RequestParam(required = false) String keyword,
                                                 @RequestParam(defaultValue = "1") long page,
                                                 @RequestParam(defaultValue = "20") long size) {
        return R.ok(applicationService.page(status, keyword, page, size));
    }

    @Operation(summary = "开通申请详情")
    @GetMapping("/{id}")
    public R<TenantApplication> detail(@PathVariable Long id) {
        return R.ok(applicationService.detail(id));
    }

    @Operation(summary = "审核通过并开通")
    @PostMapping("/{id}/approve")
    public R<Map<String, Object>> approve(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        String domain = body == null ? null : body.get("domain");
        String remark = body == null ? null : body.get("remark");
        return R.ok(applicationService.approve(id, domain, remark));
    }

    @Operation(summary = "驳回申请")
    @PostMapping("/{id}/reject")
    public R<Void> reject(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        applicationService.reject(id, body == null ? null : body.get("remark"));
        return R.ok();
    }

    @Operation(summary = "要求补充材料")
    @PostMapping("/{id}/request-materials")
    public R<Void> requestMaterials(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        applicationService.requestMaterials(id, body == null ? null : body.get("remark"));
        return R.ok();
    }
}
