package cn.faccess.steel.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("weigh_record")
public class WeighRecord {
    @TableId
    private Long id;
    private Long tenantId;
    private Long vehicleVisitId;
    private Long deviceId;
    private BigDecimal tareWeight;
    private BigDecimal grossWeight;
    private BigDecimal netWeight;
    private BigDecimal deductRate;
    private BigDecimal settleWeight;
    private LocalDateTime tareTime;
    private LocalDateTime grossTime;
    private String snapshotUrls;
    /** OVERLOAD/REWEIGH_DIFF */
    private String abnormal;
    private Integer writtenBack;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
