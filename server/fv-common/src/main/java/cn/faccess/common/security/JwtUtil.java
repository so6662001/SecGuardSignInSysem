package cn.faccess.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

/**
 * JWT 生成与解析（HS256）。
 */
@Component
public class JwtUtil {

    private final JwtProperties props;
    private final SecretKey key;

    public JwtUtil(JwtProperties props) {
        this.props = props;
        this.key = Keys.hmacShaKeyFor(props.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(LoginUser user) {
        long ttl = props.getAccessTtlMinutes() * 60_000L;
        return build(user, ttl, "access");
    }

    public String generateRefreshToken(LoginUser user) {
        long ttl = props.getRefreshTtlDays() * 24 * 60 * 60_000L;
        return build(user, ttl, "refresh");
    }

    private String build(LoginUser user, long ttlMillis, String type) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(user.getUserId()))
                .claim("tid", user.getTenantId())
                .claim("uname", user.getUsername())
                .claim("roles", user.getRoles())
                .claim("typ", type)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + ttlMillis))
                .signWith(key)
                .compact();
    }

    @SuppressWarnings("unchecked")
    public LoginUser parse(String token) {
        Claims c = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        Long userId = Long.valueOf(c.getSubject());
        Long tenantId = c.get("tid", Number.class) == null ? null : c.get("tid", Number.class).longValue();
        String username = c.get("uname", String.class);
        List<String> roles = (List<String>) c.get("roles", List.class);
        return new LoginUser(userId, tenantId, username, roles);
    }
}
