package cn.faccess.common.security;

import java.util.List;

/**
 * 登录用户信息（存入 SecurityContext 与 TenantContext）。
 */
public class LoginUser {

    private Long userId;
    private Long tenantId;
    private String username;
    private List<String> roles;

    public LoginUser() {
    }

    public LoginUser(Long userId, Long tenantId, String username, List<String> roles) {
        this.userId = userId;
        this.tenantId = tenantId;
        this.username = username;
        this.roles = roles;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}
