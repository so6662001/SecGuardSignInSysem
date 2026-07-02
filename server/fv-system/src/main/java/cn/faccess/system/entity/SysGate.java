package cn.faccess.system.entity;

import cn.faccess.common.mp.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_gate")
public class SysGate extends BaseEntity {
    private Long siteId;
    private String name;
    private String gateCode;
    /** 1人行 2车行 3人车混合 */
    private Integer type;
    private String location;
    private String guardIds;
    private String qrcodeUrl;
    private Integer status;
}
