package cn.faccess.notify.channel;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 阿里云语音外呼配置。
 */
@Component
@ConfigurationProperties(prefix = "fv.channel.voice-config")
public class VoiceProperties {
    private String accessKeyId = "";
    private String accessKeySecret = "";
    /** TTS 模板编码。 */
    private String ttsCode = "";
    private String endpoint = "dyvmsapi.aliyuncs.com";

    public String getAccessKeyId() { return accessKeyId; }
    public void setAccessKeyId(String v) { this.accessKeyId = v; }
    public String getAccessKeySecret() { return accessKeySecret; }
    public void setAccessKeySecret(String v) { this.accessKeySecret = v; }
    public String getTtsCode() { return ttsCode; }
    public void setTtsCode(String v) { this.ttsCode = v; }
    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String v) { this.endpoint = v; }
}
