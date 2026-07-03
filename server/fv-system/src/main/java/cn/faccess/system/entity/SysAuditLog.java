package cn.faccess.system.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_audit_log")
public class SysAuditLog {
    @TableId
    private Long id;
    private Long tenantId;
    private Long userId;
    private String userName;
    private String module;
    private String action;
    private String target;
    private String detail;
    private String ip;
    private LocalDateTime createTime;
}
