package cn.faccess.system.controller;

import cn.faccess.common.tenant.TenantContext;
import cn.faccess.common.web.R;
import cn.faccess.system.dto.LoginReq;
import cn.faccess.system.entity.SysMenu;
import cn.faccess.system.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "tenant-auth", description = "租户登录与菜单")
@RestController
@RequestMapping("/api/tenant")
public class TenantAuthController {

    private final AuthService authService;

    public TenantAuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "租户登录（需专属域名）")
    @PostMapping("/auth/login")
    public R<Map<String, Object>> login(@Valid @RequestBody LoginReq req) {
        return R.ok(authService.loginTenant(req));
    }

    @Operation(summary = "当前用户信息")
    @GetMapping("/me/profile")
    public R<Map<String, Object>> profile() {
        return R.ok(authService.profile(authService.getUser(TenantContext.getUserId())));
    }

    @Operation(summary = "当前用户菜单与权限")
    @GetMapping("/me/menus")
    public R<List<SysMenu>> menus() {
        return R.ok(authService.menus(TenantContext.getUserId(), "tenant"));
    }
}
