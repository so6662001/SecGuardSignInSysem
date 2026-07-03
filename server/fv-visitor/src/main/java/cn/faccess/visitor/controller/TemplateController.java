package cn.faccess.visitor.controller;

import cn.faccess.common.web.R;
import cn.faccess.visitor.entity.RegisterField;
import cn.faccess.visitor.entity.RegisterTemplate;
import cn.faccess.visitor.service.TemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "tenant-template", description = "登记模板与字段")
@RestController
@RequestMapping("/api/tenant/templates")
public class TemplateController {

    private final TemplateService templateService;

    public TemplateController(TemplateService templateService) {
        this.templateService = templateService;
    }

    @Operation(summary = "模板列表")
    @GetMapping
    public R<List<RegisterTemplate>> list() {
        return R.ok(templateService.list());
    }

    @Operation(summary = "模板详情（含字段）")
    @GetMapping("/{id}")
    public R<Map<String, Object>> get(@PathVariable Long id) {
        return R.ok(templateService.get(id));
    }

    @Operation(summary = "新建模板")
    @PostMapping
    public R<Long> create(@RequestBody RegisterTemplate t) {
        return R.ok(templateService.create(t));
    }

    @Operation(summary = "保存模板字段")
    @PutMapping("/{id}/fields")
    public R<Void> saveFields(@PathVariable Long id, @RequestBody List<RegisterField> fields) {
        templateService.saveFields(id, fields);
        return R.ok();
    }

    @Operation(summary = "发布模板")
    @PostMapping("/{id}/publish")
    public R<Void> publish(@PathVariable Long id) {
        templateService.publish(id);
        return R.ok();
    }
}
