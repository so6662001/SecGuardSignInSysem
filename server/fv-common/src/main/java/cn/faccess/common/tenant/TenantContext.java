package cn.faccess.common.tenant;

import java.util.List;

/**
 * 租户/登录上下文（ThreadLocal）。请求进入时由 JWT 过滤器写入，请求结束清理。
 * 平台账号 tenantId = 0。
 */
public final class TenantContext {

    private static final ThreadLocal<Ctx> HOLDER = new ThreadLocal<>();

    private TenantContext() {
    }

    public static void set(Long tenantId, Long userId, String username, List<String> roles) {
        HOLDER.set(new Ctx(tenantId, userId, username, roles));
    }

    public static Long getTenantId() {
        Ctx c = HOLDER.get();
        return c == null ? null : c.tenantId;
    }

    public static Long getUserId() {
        Ctx c = HOLDER.get();
        return c == null ? null : c.userId;
    }

    public static String getUsername() {
        Ctx c = HOLDER.get();
        return c == null ? null : c.username;
    }

    public static List<String> getRoles() {
        Ctx c = HOLDER.get();
        return c == null ? List.of() : c.roles;
    }

    public static boolean isPlatform() {
        Long t = getTenantId();
        return t != null && t == 0L;
    }

    public static void clear() {
        HOLDER.remove();
    }

    private record Ctx(Long tenantId, Long userId, String username, List<String> roles) {
    }
}
