package cn.faccess.visitor.controller;

import cn.faccess.common.web.R;
import cn.faccess.visitor.service.TemplateService;
import cn.faccess.visitor.service.VisitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "public", description = "公开接口（免登录）")
@RestController
@RequestMapping("/api/public")
public class PublicVisitController {

    private final VisitService visitService;
    private final TemplateService templateService;

    public PublicVisitController(VisitService visitService, TemplateService templateService) {
        this.visitService = visitService;
        this.templateService = templateService;
    }

    @Operation(summary = "扫码取门岗登记模板")
    @GetMapping("/gate/{gateCode}/template")
    public R<Map<String, Object>> gateTemplate(@PathVariable String gateCode) {
        return R.ok(templateService.schemaByGate(gateCode));
    }

    @Operation(summary = "免登录审批详情")
    @GetMapping("/approve/{token}")
    public R<Map<String, Object>> approveView(@PathVariable String token) {
        return R.ok(visitService.viewByToken(token));
    }

    @Operation(summary = "免登录审批决策")
    @PostMapping("/approve/{token}/decision")
    public R<Void> approveDecision(@PathVariable String token, @RequestBody Map<String, Object> body) {
        boolean approve = Boolean.TRUE.equals(body.get("approve"));
        String validScope = (String) body.getOrDefault("validScope", "ONCE");
        visitService.decideByToken(token, approve, validScope);
        return R.ok();
    }
}
