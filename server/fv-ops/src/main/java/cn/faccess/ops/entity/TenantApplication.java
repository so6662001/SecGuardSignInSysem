package cn.faccess.ops.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tenant_application")
public class TenantApplication {
    @TableId
    private Long id;
    private String appNo;
    private String companyName;
    private String creditCode;
    private String industry;
    private String address;
    private Integer siteCount;
    private String contactName;
    private String contactMobile;
    private String contactTitle;
    private String email;
    private String planCode;
    private String licenseFiles;
    private String gsVerify;
    /** 1待审 2通过开通 3驳回 4补充材料 */
    private Integer status;
    private Long auditBy;
    private String auditRemark;
    private String domain;
    private Long tenantId;
    /** 申请人登录密码（BCrypt 密文） */
    private String passwordHash;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
