package cn.faccess.system.entity;

import cn.faccess.common.mp.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {
    private String username;
    private String password;
    private String realName;
    private String mobile;
    private String mobileMask;
    private String email;
    private String avatar;
    private String jobTitle;
    private Long siteId;
    private Long gateId;
    /** 1平台 2租户 */
    private Integer userType;
    private String wxOpenid;
    private Integer status;
    private LocalDateTime lastLogin;
}
