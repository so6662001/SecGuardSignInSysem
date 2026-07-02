package cn.faccess.auth;

import cn.faccess.common.exception.BizException;
import cn.faccess.common.security.JwtUtil;
import cn.faccess.common.security.LoginUser;
import cn.faccess.common.web.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * B0 登录示例（运营端）。真实的 DB 用户校验在 B1 系统域实现，此处用于打通安全链路与令牌发放。
 */
@Tag(name = "ops-auth", description = "运营登录")
@RestController
public class AuthController {

    private final JwtUtil jwtUtil;

    @Value("${fv.dev.admin-username:admin}")
    private String adminUsername;

    @Value("${fv.dev.admin-password:admin123}")
    private String adminPassword;

    public AuthController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Operation(summary = "运营登录（B0 示例）")
    @PostMapping("/api/ops/auth/login")
    public R<Map<String, Object>> login(@Valid @RequestBody LoginReq req) {
        if (!adminUsername.equals(req.getUsername()) || !adminPassword.equals(req.getPassword())) {
            throw new BizException(401, "账号或密码错误");
        }
        LoginUser user = new LoginUser(1L, 0L, adminUsername, List.of("PLATFORM_ADMIN"));
        return R.ok(Map.of(
                "accessToken", jwtUtil.generateAccessToken(user),
                "refreshToken", jwtUtil.generateRefreshToken(user),
                "user", Map.of("id", 1, "tenantId", 0, "username", adminUsername, "roles", user.getRoles())
        ));
    }

    public static class LoginReq {
        @NotBlank(message = "账号不能为空")
        private String username;
        @NotBlank(message = "密码不能为空")
        private String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
