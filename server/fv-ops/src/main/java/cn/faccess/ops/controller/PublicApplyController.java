package cn.faccess.ops.controller;

import cn.faccess.common.web.R;
import cn.faccess.ops.dto.ApplyReq;
import cn.faccess.ops.service.ApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "public", description = "公开接口")
@RestController
@RequestMapping("/api/public")
public class PublicApplyController {

    private final ApplicationService applicationService;

    public PublicApplyController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @Operation(summary = "企业在线申请开通")
    @PostMapping("/apply")
    public R<Map<String, String>> apply(@Valid @RequestBody ApplyReq req) {
        String appNo = applicationService.apply(req);
        return R.ok(Map.of("appNo", appNo, "message", "申请已提交，请等待审核开通"));
    }

    @Operation(summary = "获取短信验证码（示例）")
    @PostMapping("/sms/code")
    public R<Void> smsCode(@RequestBody Map<String, String> body) {
        // 示例实现：真实场景对接短信适配器并写入 Redis 校验，此处直接返回成功
        return R.ok();
    }
}
