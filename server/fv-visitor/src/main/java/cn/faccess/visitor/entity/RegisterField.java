package cn.faccess.visitor.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("register_field")
public class RegisterField {
    @TableId
    private Long id;
    private Long tenantId;
    private Long templateId;
    /** BASIC/ID/DEVICE/DANGER/PLEDGE/HEALTH/VEHICLE/CUSTOM */
    private String module;
    private String fieldKey;
    private String label;
    /** TEXT/TEXTAREA/RADIO/CHECKBOX/NUMBER/DATE/PHOTO/SIGN/SWITCH */
    private String fieldType;
    private Integer required;
    private String options;
    private Integer sort;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
