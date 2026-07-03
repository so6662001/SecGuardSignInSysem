package cn.faccess.system.service;

import cn.faccess.common.exception.BizException;
import cn.faccess.common.security.JwtUtil;
import cn.faccess.common.security.LoginUser;
import cn.faccess.system.dto.LoginReq;
import cn.faccess.system.entity.SysMenu;
import cn.faccess.system.entity.SysTenant;
import cn.faccess.system.entity.SysUser;
import cn.faccess.system.mapper.SysMenuMapper;
import cn.faccess.system.mapper.SysTenantMapper;
import cn.faccess.system.mapper.SysUserMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 登录鉴权（运营端 / 租户端）、当前用户信息、动态菜单。
 */
@Service
public class AuthService {

    private final SysUserMapper userMapper;
    private final SysTenantMapper tenantMapper;
    private final SysMenuMapper menuMapper;
    private final PermissionService permissionService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(SysUserMapper userMapper, SysTenantMapper tenantMapper, SysMenuMapper menuMapper,
                       PermissionService permissionService, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.tenantMapper = tenantMapper;
        this.menuMapper = menuMapper;
        this.permissionService = permissionService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    /** 运营端登录：平台用户 tenant_id=0。 */
    public Map<String, Object> loginPlatform(LoginReq req) {
        SysUser user = userMapper.selectOne(Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getTenantId, 0L)
                .eq(SysUser::getUsername, req.getUsername()));
        return doLogin(user, req.getPassword());
    }

    /** 租户端登录：按专属域名定位租户，再按账号在该租户内查找。 */
    public Map<String, Object> loginTenant(LoginReq req) {
        if (!StringUtils.hasText(req.getDomain())) {
            throw new BizException(400, "请提供企业专属域名");
        }
        SysTenant tenant = tenantMapper.selectOne(Wrappers.<SysTenant>lambdaQuery()
                .eq(SysTenant::getDomain, req.getDomain()));
        if (tenant == null) {
            throw new BizException(404, "企业空间不存在");
        }
        if (tenant.getStatus() != null && tenant.getStatus() == 5) {
            throw new BizException(403, "企业空间已停用");
        }
        SysUser user = userMapper.selectOne(Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getTenantId, tenant.getId())
                .eq(SysUser::getUsername, req.getUsername()));
        return doLogin(user, req.getPassword());
    }

    private Map<String, Object> doLogin(SysUser user, String rawPassword) {
        if (user == null || !passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new BizException(401, "账号或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BizException(403, "账号已停用");
        }
        List<String> roles = permissionService.roleCodesOf(user.getId());
        LoginUser lu = new LoginUser(user.getId(), user.getTenantId(), user.getUsername(), roles);

        user.setLastLogin(LocalDateTime.now());
        userMapper.updateById(user);

        Map<String, Object> data = new HashMap<>();
        data.put("accessToken", jwtUtil.generateAccessToken(lu));
        data.put("refreshToken", jwtUtil.generateRefreshToken(lu));
        data.put("user", profile(user));
        return data;
    }

    public Map<String, Object> profile(SysUser user) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", user.getId());
        m.put("tenantId", user.getTenantId());
        m.put("username", user.getUsername());
        m.put("realName", user.getRealName());
        m.put("roles", permissionService.roleCodesOf(user.getId()));
        m.put("perms", permissionService.permCodesOf(user.getId()));
        return m;
    }

    public SysUser getUser(Long userId) {
        return userMapper.selectById(userId);
    }

    /** 动态菜单：按 app 与用户权限过滤（permCode 为空的公共菜单直接展示）。 */
    public List<SysMenu> menus(Long userId, String app) {
        List<String> perms = permissionService.permCodesOf(userId);
        return menuMapper.selectList(Wrappers.<SysMenu>lambdaQuery()
                        .eq(SysMenu::getApp, app)
                        .eq(SysMenu::getVisible, 1)
                        .orderByAsc(SysMenu::getSort))
                .stream()
                .filter(menu -> !StringUtils.hasText(menu.getPermCode()) || perms.contains(menu.getPermCode()))
                .collect(Collectors.toList());
    }
}
