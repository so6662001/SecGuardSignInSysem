package cn.faccess.system.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 租户企业（平台域表，无 tenant_id，故不继承 BaseEntity；在租户拦截器忽略清单内）。
 */
@Data
@TableName("sys_tenant")
public class SysTenant {
    @TableId
    private Long id;
    private String name;
    private String creditCode;
    private String industry;
    private String address;
    private String domain;
    /** 1试用 2正常 3即将到期 4欠费 5停用 */
    private Integer status;
    private String source;
    private LocalDate openDate;
    private Integer siteCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Long createBy;
    private Long updateBy;
    @TableLogic
    private Integer deleted;
}
