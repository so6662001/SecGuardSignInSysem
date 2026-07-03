package cn.faccess.visitor.entity;

import cn.faccess.common.mp.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("register_template")
public class RegisterTemplate extends BaseEntity {
    private String name;
    /** NORMAL/CONTRACTOR/SUPPLIER/INTERVIEW/STEEL_VEHICLE */
    private String visitType;
    private String enabledModules;
    private Integer isDefault;
    private Integer sort;
    /** 0草稿 1已发布 */
    private Integer status;
}
