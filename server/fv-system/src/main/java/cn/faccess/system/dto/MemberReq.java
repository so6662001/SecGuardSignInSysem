package cn.faccess.system.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class MemberReq {
    @NotBlank(message = "账号不能为空")
    private String username;
    @NotBlank(message = "姓名不能为空")
    private String realName;
    private String password;
    private String mobile;
    private String email;
    private String jobTitle;
    private Long siteId;
    private Long gateId;
    private Integer status;
    private List<Long> roleIds;
}
