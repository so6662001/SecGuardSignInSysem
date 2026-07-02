package cn.faccess.system.service;

import cn.faccess.system.entity.SysPermission;
import cn.faccess.system.entity.SysRole;
import cn.faccess.system.entity.SysRolePermission;
import cn.faccess.system.entity.SysUserRole;
import cn.faccess.system.mapper.SysPermissionMapper;
import cn.faccess.system.mapper.SysRoleMapper;
import cn.faccess.system.mapper.SysRolePermissionMapper;
import cn.faccess.system.mapper.SysUserRoleMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 加载用户的角色编码与权限编码。
 */
@Service
public class PermissionService {

    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMapper roleMapper;
    private final SysRolePermissionMapper rolePermMapper;
    private final SysPermissionMapper permMapper;

    public PermissionService(SysUserRoleMapper userRoleMapper, SysRoleMapper roleMapper,
                             SysRolePermissionMapper rolePermMapper, SysPermissionMapper permMapper) {
        this.userRoleMapper = userRoleMapper;
        this.roleMapper = roleMapper;
        this.rolePermMapper = rolePermMapper;
        this.permMapper = permMapper;
    }

    public List<Long> roleIdsOf(Long userId) {
        return userRoleMapper.selectList(Wrappers.<SysUserRole>lambdaQuery().eq(SysUserRole::getUserId, userId))
                .stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
    }

    public List<String> roleCodesOf(Long userId) {
        List<Long> roleIds = roleIdsOf(userId);
        if (roleIds.isEmpty()) return Collections.emptyList();
        return roleMapper.selectBatchIds(roleIds).stream().map(SysRole::getCode).collect(Collectors.toList());
    }

    public List<String> permCodesOf(Long userId) {
        List<Long> roleIds = roleIdsOf(userId);
        if (roleIds.isEmpty()) return Collections.emptyList();
        List<Long> permIds = rolePermMapper.selectList(
                        Wrappers.<SysRolePermission>lambdaQuery().in(SysRolePermission::getRoleId, roleIds))
                .stream().map(SysRolePermission::getPermissionId).distinct().collect(Collectors.toList());
        if (permIds.isEmpty()) return Collections.emptyList();
        return permMapper.selectBatchIds(permIds).stream().map(SysPermission::getCode).collect(Collectors.toList());
    }
}
