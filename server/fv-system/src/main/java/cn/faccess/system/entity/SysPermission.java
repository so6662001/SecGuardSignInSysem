package cn.faccess.system.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_permission")
public class SysPermission {
    @TableId
    private Long id;
    private String code;
    private String name;
    private String module;
    /** 1菜单 2按钮/操作 */
    private Integer type;
    /** 1平台 2租户 3通用 */
    private Integer scope;
    private LocalDateTime createTime;
}
