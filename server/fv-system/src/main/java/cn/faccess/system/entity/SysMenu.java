package cn.faccess.system.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_menu")
public class SysMenu {
    @TableId
    private Long id;
    private Long parentId;
    private String name;
    private String path;
    private String component;
    private String icon;
    private String permCode;
    /** 归属端:tenant/ops */
    private String app;
    private Integer sort;
    private Integer visible;
    private LocalDateTime createTime;
}
