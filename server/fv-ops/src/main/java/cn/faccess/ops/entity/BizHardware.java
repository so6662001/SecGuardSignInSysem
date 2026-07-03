package cn.faccess.ops.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("biz_hardware")
public class BizHardware {
    @TableId
    private Long id;
    private String code;
    private String name;
    private BigDecimal buyPrice;
    private BigDecimal rentPrice;
    private String type;
    private Integer status;
    private LocalDateTime createTime;
}
