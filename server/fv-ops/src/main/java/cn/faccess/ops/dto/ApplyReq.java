package cn.faccess.ops.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class ApplyReq {
    @NotBlank(message = "企业名称不能为空")
    private String companyName;
    private String creditCode;
    private String industry;
    private String address;
    private Integer siteCount = 1;
    @NotBlank(message = "联系人不能为空")
    private String contactName;
    @NotBlank(message = "手机号不能为空")
    private String contactMobile;
    private String contactTitle;
    private String email;
    private String smsCode;
    @NotBlank(message = "登录密码不能为空")
    private String password;
    @NotBlank(message = "请选择套餐")
    private String planCode;
    private List<String> licenseFiles;
}
