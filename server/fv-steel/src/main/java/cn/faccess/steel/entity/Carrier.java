package cn.faccess.steel.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("carrier")
public class Carrier {
    @TableId
    private Long id;
    private Long tenantId;
    private String name;
    private String cooperateSince;
    private String allowedGoods;
    private Integer tradeJoined;
    private Integer whitelist;
    /** NORMAL/RESTRICT/BLACK */
    private String status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
