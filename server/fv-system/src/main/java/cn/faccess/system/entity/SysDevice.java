package cn.faccess.system.entity;

import cn.faccess.common.mp.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_device")
public class SysDevice extends BaseEntity {
    private Long gateId;
    private Long siteId;
    private String name;
    private String deviceNo;
    /** TABLET/LPR_GATE/CAMERA/FACE/QR_STAND/WEIGHBRIDGE */
    private String type;
    private String vendor;
    private String model;
    private String connType;
    private String secret;
    /** 0离线 1在线 */
    private Integer online;
    private LocalDateTime lastHeartbeat;
    private Integer status;
}
