package cn.faccess.ops.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ops_lead")
public class OpsLead {
    @TableId
    private Long id;
    private Long tenantId;
    private String companyName;
    /** 行业信号:高频物流/供应链活跃/运力密集 */
    private String signalTag;
    /** 推荐产品:交易平台/管理软件/MES */
    private String recommend;
    private Integer intentScore;
    /** 1待跟进 2跟进中 3成交 4观察 */
    private Integer status;
    private Long ownerId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
