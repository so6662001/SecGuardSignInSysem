package cn.faccess.steel.controller;

import cn.faccess.common.web.R;
import cn.faccess.steel.dto.WeighReq;
import cn.faccess.steel.entity.WeighRecord;
import cn.faccess.steel.service.WeighService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "steel-weigh", description = "磅房过磅")
@RestController
@RequestMapping("/api/tenant/steel/weigh")
@PreAuthorize("hasAuthority('steel:weigh') or hasRole('TENANT_ADMIN')")
public class WeighController {

    private final WeighService weighService;

    public WeighController(WeighService weighService) {
        this.weighService = weighService;
    }

    @Operation(summary = "记录过磅（皮重/毛重）")
    @PostMapping
    public R<WeighRecord> weigh(@Valid @RequestBody WeighReq req) {
        return R.ok(weighService.weigh(req));
    }

    @Operation(summary = "磅单流水")
    @GetMapping("/records")
    public R<List<WeighRecord>> records() {
        return R.ok(weighService.records());
    }
}
