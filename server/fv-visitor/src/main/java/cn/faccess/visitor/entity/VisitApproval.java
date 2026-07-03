package cn.faccess.visitor.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("visit_approval")
public class VisitApproval {
    @TableId
    private Long id;
    private Long tenantId;
    private Long recordId;
    private Long hostUserId;
    /** WAITING/APPROVED/REJECTED/ESCALATED/GUARD_PROXY */
    private String status;
    private Long approverId;
    private String channel;
    /** ONCE/TODAY/WEEK */
    private String validScope;
    private Integer costSeconds;
    private String trace;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
