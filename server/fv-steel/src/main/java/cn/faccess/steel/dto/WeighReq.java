package cn.faccess.steel.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class WeighReq {
    @NotNull(message = "车辆到厂ID不能为空")
    private Long vehicleVisitId;
    private Long deviceId;
    @NotNull(message = "称重读数不能为空")
    private BigDecimal weight;
    /** TARE皮重 / GROSS毛重 */
    @NotNull(message = "称重类型不能为空")
    private String weighType;
}
