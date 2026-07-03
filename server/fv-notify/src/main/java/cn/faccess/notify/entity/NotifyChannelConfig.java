package cn.faccess.notify.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("notify_channel_config")
public class NotifyChannelConfig {
    @TableId
    private Long id;
    private Long tenantId;
    private String channelOrder;
    private Integer firstTimeout;
    private Integer secondRemind;
    private Integer escalateBackup;
    private Integer guardProxy;
    private Integer invitePass;
    private Integer whitelistPass;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
