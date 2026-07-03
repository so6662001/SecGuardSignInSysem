package cn.faccess.steel.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class VehicleVisitReq {
    @NotBlank(message = "车牌号不能为空")
    private String plateNo;
    /** INBOUND送货入厂 / OUTBOUND提货出厂 */
    @NotBlank(message = "方向不能为空")
    private String direction;
    private String orderNo;
    private Long carrierId;
    private Long driverId;
    private String goodsName;
    private String goodsSpec;
    private BigDecimal planWeight;
    private String warehouse;
    private BigDecimal deductRate;
    private Long gateId;
}
