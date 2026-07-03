package cn.faccess.notify.channel;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 微信公众号配置。
 */
@Component
@ConfigurationProperties(prefix = "fv.channel.wechat-config")
public class WechatProperties {
    private String appId = "";
    private String appSecret = "";
    /** 模板消息模板ID。 */
    private String templateId = "";
    private String apiBase = "https://api.weixin.qq.com";

    public String getAppId() { return appId; }
    public void setAppId(String v) { this.appId = v; }
    public String getAppSecret() { return appSecret; }
    public void setAppSecret(String v) { this.appSecret = v; }
    public String getTemplateId() { return templateId; }
    public void setTemplateId(String v) { this.templateId = v; }
    public String getApiBase() { return apiBase; }
    public void setApiBase(String v) { this.apiBase = v; }
}
