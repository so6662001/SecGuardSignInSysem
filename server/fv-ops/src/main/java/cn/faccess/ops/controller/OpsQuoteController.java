package cn.faccess.ops.controller;

import cn.faccess.common.web.R;
import cn.faccess.ops.entity.BizQuote;
import cn.faccess.ops.service.QuoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "ops-quote", description = "配置报价")
@RestController
@RequestMapping("/api/ops/quotes")
@PreAuthorize("hasRole('PLATFORM_ADMIN') or hasRole('PLATFORM_OPS')")
public class OpsQuoteController {

    private final QuoteService quoteService;

    public OpsQuoteController(QuoteService quoteService) {
        this.quoteService = quoteService;
    }

    @Operation(summary = "生成报价单")
    @PostMapping
    public R<BizQuote> create(@RequestBody Map<String, Object> req) {
        return R.ok(quoteService.create(req));
    }

    @Operation(summary = "报价单详情")
    @GetMapping("/{id}")
    public R<BizQuote> get(@PathVariable Long id) {
        return R.ok(quoteService.get(id));
    }

    @Operation(summary = "发送报价单")
    @PostMapping("/{id}/send")
    public R<Void> send(@PathVariable Long id) {
        quoteService.send(id);
        return R.ok();
    }
}
