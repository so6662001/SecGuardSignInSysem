package cn.faccess.ops.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("biz_plan")
public class BizPlan {
    @TableId
    private Long id;
    private String code;
    private String name;
    private BigDecimal monthlyPrice;
    private String billUnit;
    private String features;
    private Integer steelEnabled;
    private Integer isCustom;
    private Integer sort;
    private Integer status;
    private LocalDateTime createTime;
}
