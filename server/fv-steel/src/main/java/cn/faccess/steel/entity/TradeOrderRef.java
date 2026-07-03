package cn.faccess.steel.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("trade_order_ref")
public class TradeOrderRef {
    @TableId
    private Long id;
    private Long tenantId;
    private Long vehicleVisitId;
    private String extOrderNo;
    /** SALES销售 / PURCHASE采购 */
    private String orderType;
    private String partnerName;
    private String goodsName;
    private BigDecimal planQty;
    private BigDecimal doneQty;
    private BigDecimal remainQty;
    private String settleStatus;
    private LocalDateTime syncTime;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
