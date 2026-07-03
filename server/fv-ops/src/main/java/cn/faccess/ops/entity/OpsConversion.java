package cn.faccess.ops.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("ops_conversion")
public class OpsConversion {
    @TableId
    private Long id;
    private Long tenantId;
    /** REGISTER/ACTIVE/POTENTIAL/OPPORTUNITY/DEAL */
    private String stage;
    private String product;
    private BigDecimal amount;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
