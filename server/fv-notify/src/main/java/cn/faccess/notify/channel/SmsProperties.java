package cn.faccess.notify.channel;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 短信厂商配置（阿里云等）。
 */
@Component
@ConfigurationProperties(prefix = "fv.channel.sms-config")
public class SmsProperties {
    private String accessKeyId = "";
    private String accessKeySecret = "";
    private String signName = "厂智访客";
    private String templateCode = "";
    private String endpoint = "dysmsapi.aliyuncs.com";

    public String getAccessKeyId() { return accessKeyId; }
    public void setAccessKeyId(String v) { this.accessKeyId = v; }
    public String getAccessKeySecret() { return accessKeySecret; }
    public void setAccessKeySecret(String v) { this.accessKeySecret = v; }
    public String getSignName() { return signName; }
    public void setSignName(String v) { this.signName = v; }
    public String getTemplateCode() { return templateCode; }
    public void setTemplateCode(String v) { this.templateCode = v; }
    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String v) { this.endpoint = v; }
}
