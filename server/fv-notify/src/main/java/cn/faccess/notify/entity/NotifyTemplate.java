package cn.faccess.notify.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("notify_template")
public class NotifyTemplate {
    @TableId
    private Long id;
    private Long tenantId;
    private String code;
    /** PUSH/SMS/VOICE/WECHAT/WECOM/DINGTALK */
    private String channel;
    private String title;
    private String content;
    private String scene;
    private Integer status;
    private LocalDateTime createTime;
}
