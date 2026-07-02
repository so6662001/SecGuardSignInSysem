package cn.faccess.system.init;

import cn.faccess.system.entity.SysUser;
import cn.faccess.system.mapper.SysUserMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.ApplicationArguments;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 启动时确保平台超管账号存在且密码为配置值（种子 SQL 不便内置正确 BCrypt，故在此保证可登录）。
 */
@Component
public class PlatformAdminInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(PlatformAdminInitializer.class);

    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Value("${fv.dev.admin-username:admin}")
    private String adminUsername;
    @Value("${fv.dev.admin-password:admin123}")
    private String adminPassword;

    public PlatformAdminInitializer(SysUserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        SysUser admin = userMapper.selectOne(Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getTenantId, 0L)
                .eq(SysUser::getUsername, adminUsername));
        String encoded = passwordEncoder.encode(adminPassword);
        if (admin == null) {
            admin = new SysUser();
            admin.setUsername(adminUsername);
            admin.setRealName("平台管理员");
            admin.setUserType(1);
            admin.setStatus(1);
            admin.setPassword(encoded);
            userMapper.insert(admin);
            log.info("已创建平台超管账号: {}", adminUsername);
        } else {
            admin.setPassword(encoded);
            userMapper.updateById(admin);
            log.info("已重置平台超管密码: {}", adminUsername);
        }
    }
}
