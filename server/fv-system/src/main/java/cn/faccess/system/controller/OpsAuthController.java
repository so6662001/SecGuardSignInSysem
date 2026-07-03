package cn.faccess.system.controller;

import cn.faccess.common.tenant.TenantContext;
import cn.faccess.common.web.R;
import cn.faccess.system.dto.LoginReq;
import cn.faccess.system.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "ops-auth", description = "运营登录")
@RestController
@RequestMapping("/api/ops/auth")
public class OpsAuthController {

    private final AuthService authService;

    public OpsAuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "运营登录")
    @PostMapping("/login")
    public R<Map<String, Object>> login(@Valid @RequestBody LoginReq req) {
        return R.ok(authService.loginPlatform(req));
    }

    @Operation(summary = "当前运营用户信息")
    @GetMapping("/profile")
    public R<Map<String, Object>> profile() {
        return R.ok(authService.profile(authService.getUser(TenantContext.getUserId())));
    }
}
