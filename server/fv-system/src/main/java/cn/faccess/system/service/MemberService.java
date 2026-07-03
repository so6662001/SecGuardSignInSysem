package cn.faccess.system.service;

import cn.faccess.common.exception.BizException;
import cn.faccess.common.web.PageResult;
import cn.faccess.system.dto.MemberReq;
import cn.faccess.system.entity.SysUser;
import cn.faccess.system.entity.SysUserRole;
import cn.faccess.system.mapper.SysUserMapper;
import cn.faccess.system.mapper.SysUserRoleMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 租户成员管理（租户上下文由拦截器自动隔离）。
 */
@Service
public class MemberService {

    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final PermissionService permissionService;
    private final PasswordEncoder passwordEncoder;

    public MemberService(SysUserMapper userMapper, SysUserRoleMapper userRoleMapper,
                         PermissionService permissionService, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.userRoleMapper = userRoleMapper;
        this.permissionService = permissionService;
        this.passwordEncoder = passwordEncoder;
    }

    public PageResult<SysUser> page(String keyword, long page, long size) {
        IPage<SysUser> p = userMapper.selectPage(new Page<>(page, size),
                Wrappers.<SysUser>lambdaQuery()
                        .and(StringUtils.hasText(keyword), w -> w
                                .like(SysUser::getRealName, keyword).or().like(SysUser::getUsername, keyword))
                        .orderByDesc(SysUser::getId));
        p.getRecords().forEach(u -> u.setPassword(null));
        return new PageResult<>(p.getRecords(), p.getTotal(), page, size);
    }

    @Transactional(rollbackFor = Exception.class)
    public Long create(MemberReq req) {
        Long exist = userMapper.selectCount(Wrappers.<SysUser>lambdaQuery().eq(SysUser::getUsername, req.getUsername()));
        if (exist != null && exist > 0) {
            throw new BizException("账号已存在");
        }
        SysUser u = new SysUser();
        u.setUsername(req.getUsername());
        u.setRealName(req.getRealName());
        u.setMobile(req.getMobile());
        u.setEmail(req.getEmail());
        u.setJobTitle(req.getJobTitle());
        u.setSiteId(req.getSiteId());
        u.setGateId(req.getGateId());
        u.setUserType(2);
        u.setStatus(req.getStatus() == null ? 1 : req.getStatus());
        u.setPassword(passwordEncoder.encode(StringUtils.hasText(req.getPassword()) ? req.getPassword() : "123456"));
        userMapper.insert(u);
        bindRoles(u.getId(), req.getRoleIds());
        return u.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, MemberReq req) {
        SysUser u = userMapper.selectById(id);
        if (u == null) throw new BizException(404, "成员不存在");
        u.setRealName(req.getRealName());
        u.setMobile(req.getMobile());
        u.setEmail(req.getEmail());
        u.setJobTitle(req.getJobTitle());
        u.setSiteId(req.getSiteId());
        u.setGateId(req.getGateId());
        if (req.getStatus() != null) u.setStatus(req.getStatus());
        userMapper.updateById(u);
        if (req.getRoleIds() != null) {
            userRoleMapper.delete(Wrappers.<SysUserRole>lambdaQuery().eq(SysUserRole::getUserId, id));
            bindRoles(id, req.getRoleIds());
        }
    }

    public void delete(Long id) {
        userMapper.deleteById(id);
        userRoleMapper.delete(Wrappers.<SysUserRole>lambdaQuery().eq(SysUserRole::getUserId, id));
    }

    public void resetPassword(Long id, String newPassword) {
        SysUser u = userMapper.selectById(id);
        if (u == null) throw new BizException(404, "成员不存在");
        u.setPassword(passwordEncoder.encode(StringUtils.hasText(newPassword) ? newPassword : "123456"));
        userMapper.updateById(u);
    }

    private void bindRoles(Long userId, List<Long> roleIds) {
        if (roleIds == null) return;
        for (Long rid : roleIds) {
            SysUserRole ur = new SysUserRole();
            ur.setUserId(userId);
            ur.setRoleId(rid);
            userRoleMapper.insert(ur);
        }
    }
}
