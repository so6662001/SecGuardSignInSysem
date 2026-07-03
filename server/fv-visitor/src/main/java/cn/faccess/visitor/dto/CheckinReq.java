package cn.faccess.visitor.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class CheckinReq {
    private Long gateId;
    private Long templateId;
    @NotBlank(message = "访客姓名不能为空")
    private String visitorName;
    private String visitorMobile;
    private String company;
    @NotBlank(message = "来访事由不能为空")
    private String reason;
    @NotBlank(message = "被访人不能为空")
    private String hostName;
    private Long hostUserId;
    private String hostDept;
    private Integer companions = 1;
    private String plateNo;
    private String registerType = "GUARD";
    private Map<String, Object> extFields;
    private List<Map<String, Object>> devices;
}
