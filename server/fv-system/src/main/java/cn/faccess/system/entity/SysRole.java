package cn.faccess.system.entity;

import cn.faccess.common.mp.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role")
public class SysRole extends BaseEntity {
    private String name;
    private String code;
    /** 数据范围:1全部 2本厂区 3本门岗 4本人 */
    private Integer dataScope;
    private Integer builtin;
    private String remark;
    private Integer status;
}
