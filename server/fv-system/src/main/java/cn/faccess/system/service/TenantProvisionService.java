package cn.faccess.system.service;

import cn.faccess.common.tenant.TenantContext;
import cn.faccess.system.entity.*;
import cn.faccess.system.mapper.*;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.Builder;
import lombok.Data;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 租户开通 provisioning：创建租户 + 企业超管角色（含全部租户级权限）+ 超管账号。
 * 由运营域审核通过时调用。
 */
@Service
public class TenantProvisionService {

    private final SysTenantMapper tenantMapper;
    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysPermissionMapper permissionMapper;
    private final SysRolePermissionMapper rolePermissionMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;

    public TenantProvisionService(SysTenantMapper tenantMapper, SysUserMapper userMapper, SysRoleMapper roleMapper,
                                  SysPermissionMapper permissionMapper, SysRolePermissionMapper rolePermissionMapper,
                                  SysUserRoleMapper userRoleMapper, PasswordEncoder passwordEncoder) {
        this.tenantMapper = tenantMapper;
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.permissionMapper = permissionMapper;
        this.rolePermissionMapper = rolePermissionMapper;
        this.userRoleMapper = userRoleMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Data
    @Builder
    public static class ProvisionCmd {
        private String companyName;
        private String creditCode;
        private String industry;
        private String address;
        private Integer siteCount;
        private String domain;
        private String adminUsername;
        /** 申请人已设置的登录密码（BCrypt 密文），直接作为超管密码。 */
        private String adminPasswordHash;
        private String adminRealName;
        private String adminMobile;
    }

    @Transactional(rollbackFor = Exception.class)
    public Long provision(ProvisionCmd cmd) {
        // 1) 创建租户（平台域表）
        SysTenant tenant = new SysTenant();
        tenant.setName(cmd.getCompanyName());
        tenant.setCreditCode(cmd.getCreditCode());
        tenant.setIndustry(cmd.getIndustry());
        tenant.setAddress(cmd.getAddress());
        tenant.setDomain(cmd.getDomain());
        tenant.setStatus(1); // 试用
        tenant.setSiteCount(cmd.getSiteCount() == null ? 1 : cmd.getSiteCount());
        tenant.setOpenDate(LocalDate.now());
        tenantMapper.insert(tenant);
        Long tenantId = tenant.getId();

        // 2) 在新租户上下文中创建角色与超管（拦截器据此注入 tenant_id）
        Long ctxTenant = TenantContext.getTenantId();
        Long ctxUser = TenantContext.getUserId();
        String ctxName = TenantContext.getUsername();
        List<String> ctxRoles = TenantContext.getRoles();
        try {
            TenantContext.set(tenantId, ctxUser, ctxName, ctxRoles);

            SysRole role = new SysRole();
            role.setName("企业超级管理员");
            role.setCode("TENANT_ADMIN");
            role.setDataScope(1);
            role.setBuiltin(1);
            role.setStatus(1);
            roleMapper.insert(role);

            // 绑定全部租户级/通用权限（scope 2/3）
            List<SysPermission> perms = permissionMapper.selectList(
                    Wrappers.<SysPermission>lambdaQuery().in(SysPermission::getScope, 2, 3));
            for (SysPermission p : perms) {
                SysRolePermission rp = new SysRolePermission();
                rp.setRoleId(role.getId());
                rp.setPermissionId(p.getId());
                rolePermissionMapper.insert(rp);
            }

            SysUser admin = new SysUser();
            admin.setUsername(cmd.getAdminUsername());
            admin.setPassword(cmd.getAdminPasswordHash() != null
                    ? cmd.getAdminPasswordHash()
                    : passwordEncoder.encode("123456"));
            admin.setRealName(cmd.getAdminRealName());
            admin.setMobile(cmd.getAdminMobile());
            admin.setMobileMask(mask(cmd.getAdminMobile()));
            admin.setUserType(2);
            admin.setStatus(1);
            userMapper.insert(admin);

            SysUserRole ur = new SysUserRole();
            ur.setUserId(admin.getId());
            ur.setRoleId(role.getId());
            userRoleMapper.insert(ur);
        } finally {
            TenantContext.set(ctxTenant, ctxUser, ctxName, ctxRoles);
        }
        return tenantId;
    }

    private String mask(String mobile) {
        if (mobile == null || mobile.length() < 7) return mobile;
        String digits = mobile.replaceAll("\\s", "");
        if (digits.length() != 11) return mobile;
        return digits.substring(0, 3) + "****" + digits.substring(7);
    }
}
