package cn.faccess.notify.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("notify_log")
public class NotifyLog {
    @TableId
    private Long id;
    private Long tenantId;
    private Long taskId;
    private String channel;
    private String target;
    private String content;
    /** SENT/SUCCESS/FAIL */
    private String result;
    private String receipt;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
