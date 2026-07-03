package cn.faccess.visitor.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("invitation")
public class Invitation {
    @TableId
    private Long id;
    private Long tenantId;
    private String visitorName;
    private String visitorMobile;
    private String company;
    private String reason;
    private Long hostUserId;
    private String hostName;
    private Integer companions;
    private LocalDate visitDate;
    private String timeFrom;
    private String timeTo;
    private String inviteCode;
    private String qrcodeUrl;
    /** PENDING/ARRIVED/EXPIRED/CANCELLED */
    private String status;
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
