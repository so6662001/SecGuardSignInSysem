package cn.faccess.ops.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("tenant_subscription")
public class TenantSubscription {
    @TableId
    private Long id;
    private Long tenantId;
    private String planCode;
    private Integer siteCount;
    private String addons;
    private BigDecimal monthlyFee;
    private String period;
    private LocalDate trialEnd;
    private LocalDate nextRenew;
    /** 1试用 2正常 3欠费 4停用 */
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
