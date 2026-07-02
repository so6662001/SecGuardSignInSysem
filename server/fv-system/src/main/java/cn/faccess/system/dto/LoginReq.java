package cn.faccess.system.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginReq {
    @NotBlank(message = "账号不能为空")
    private String username;
    @NotBlank(message = "密码不能为空")
    private String password;
    /** 租户端登录需提供专属域名以定位租户；运营端可不填。 */
    private String domain;
}
