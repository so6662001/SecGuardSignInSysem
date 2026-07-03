package cn.faccess.ops.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("biz_quote")
public class BizQuote {
    @TableId
    private Long id;
    private String quoteNo;
    private String customerName;
    private String contactName;
    private String contactPhone;
    private String salesName;
    private String items;
    private BigDecimal monthlyTotal;
    private BigDecimal onetimeTotal;
    private BigDecimal firstYear;
    private Integer isCustom;
    private Integer validDays;
    /** 1草稿 2已发送 3已成交 4已失效 */
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
