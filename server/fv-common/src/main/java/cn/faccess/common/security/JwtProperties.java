package cn.faccess.common.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT 配置。secret 至少 32 字节（HS256）。
 */
@ConfigurationProperties(prefix = "fv.jwt")
public class JwtProperties {

    /** 签名密钥（>=32 字节）。 */
    private String secret = "faccess-dev-secret-key-please-change-in-prod-0001";
    /** access token 有效期（分钟）。 */
    private long accessTtlMinutes = 120;
    /** refresh token 有效期（天）。 */
    private long refreshTtlDays = 7;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getAccessTtlMinutes() {
        return accessTtlMinutes;
    }

    public void setAccessTtlMinutes(long accessTtlMinutes) {
        this.accessTtlMinutes = accessTtlMinutes;
    }

    public long getRefreshTtlDays() {
        return refreshTtlDays;
    }

    public void setRefreshTtlDays(long refreshTtlDays) {
        this.refreshTtlDays = refreshTtlDays;
    }
}
