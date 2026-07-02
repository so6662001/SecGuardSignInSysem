package cn.faccess;

import cn.faccess.common.security.JwtProperties;
import cn.faccess.common.security.JwtUtil;
import cn.faccess.common.security.LoginUser;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * JWT 生成与解析单元测试（不依赖 Spring 上下文/数据库）。
 */
class JwtUtilTest {

    private JwtUtil newJwtUtil() {
        JwtProperties props = new JwtProperties();
        props.setSecret("faccess-unit-test-secret-key-0123456789-abcdef");
        return new JwtUtil(props);
    }

    @Test
    void generateAndParse() {
        JwtUtil jwt = newJwtUtil();
        LoginUser user = new LoginUser(1001L, 0L, "admin", List.of("PLATFORM_ADMIN"));
        String token = jwt.generateAccessToken(user);
        assertNotNull(token);

        LoginUser parsed = jwt.parse(token);
        assertEquals(1001L, parsed.getUserId());
        assertEquals(0L, parsed.getTenantId());
        assertEquals("admin", parsed.getUsername());
        assertEquals(List.of("PLATFORM_ADMIN"), parsed.getRoles());
    }
}
