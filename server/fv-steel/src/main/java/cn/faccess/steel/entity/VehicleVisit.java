package cn.faccess.steel.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("vehicle_visit")
public class VehicleVisit {
    @TableId
    private Long id;
    private Long tenantId;
    private Long siteId;
    private Long gateId;
    private String plateNo;
    private Long carrierId;
    private Long driverId;
    /** INBOUND送货入厂 / OUTBOUND提货出厂 */
    private String direction;
    private String orderNo;
    private String goodsName;
    private String goodsSpec;
    private BigDecimal planWeight;
    private String warehouse;
    private BigDecimal deductRate;
    private String safetyCheck;
    private String queueNo;
    /** BOOKED/ARRIVED/REGISTERED/WEIGHING/LOADING/REWEIGH/RELEASED */
    private String status;
    private String timeline;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
