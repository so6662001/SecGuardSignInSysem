package cn.faccess.visitor.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("visit_record")
public class VisitRecord {
    @TableId
    private Long id;
    private Long tenantId;
    private Long siteId;
    private Long gateId;
    private Long templateId;
    private Long visitorId;
    private String visitorName;
    private String visitorMobile;
    private String company;
    private String reason;
    private String hostName;
    private Long hostUserId;
    private String hostDept;
    private Integer companions;
    private String plateNo;
    private String badgeNo;
    /** GUARD/SELF/ID_OCR */
    private String registerType;
    private Long registerBy;
    private LocalDateTime inTime;
    private LocalDateTime outTime;
    /** PENDING/ONSITE/LEFT/REJECTED/OVERSTAY */
    private String status;
    private String extFields;
    private String devices;
    private String pledgeUrl;
    private Long invitationId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
