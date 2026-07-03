package cn.faccess.notify.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("notify_task")
public class NotifyTask {
    @TableId
    private Long id;
    private Long tenantId;
    private String scene;
    private Long bizId;
    private Long targetUser;
    private String targetMobile;
    private String channels;
    private Integer timeoutSec;
    private String escalation;
    /** PENDING/SENT/RESPONDED/ESCALATED/DONE/CANCELLED */
    private String status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
