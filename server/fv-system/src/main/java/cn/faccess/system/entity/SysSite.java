package cn.faccess.system.entity;

import cn.faccess.common.mp.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_site")
public class SysSite extends BaseEntity {
    private String name;
    private String address;
    private String manager;
    private Integer status;
}
