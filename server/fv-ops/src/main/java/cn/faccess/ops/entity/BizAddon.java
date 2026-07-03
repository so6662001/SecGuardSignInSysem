package cn.faccess.ops.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("biz_addon")
public class BizAddon {
    @TableId
    private Long id;
    private String code;
    private String name;
    /** MONTHLY/USAGE */
    private String billType;
    private BigDecimal price;
    private String unit;
    /** ALL/STEEL */
    private String scope;
    private Integer status;
    private LocalDateTime createTime;
}
