package cn.faccess.device.adapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * 法大大电子签（真实 HTTP OpenAPI 落地）。fv.device.esign=fdd 时启用。
 * 法大大无广泛使用的官方 Maven SDK，采用其 OpenAPI（HTTP）对接：创建签署流程并返回签署链接。
 * 说明：真实调用需按法大大规范做 HMAC 签名与鉴权头，这里给出结构与请求骨架。
 */
@Component
@ConditionalOnProperty(name = "fv.device.esign", havingValue = "fdd")
public class FddESignAdapter implements DeviceAdapters.ESignAdapter {

    private static final Logger log = LoggerFactory.getLogger(FddESignAdapter.class);

    private final Props props;
    private final RestClient client;

    public FddESignAdapter(Props props) {
        this.props = props;
        this.client = RestClient.builder().baseUrl(props.getBaseUrl()).build();
    }

    @Override
    @SuppressWarnings("unchecked")
    public String createSign(String subject, String signerName, String signerMobile) {
        try {
            Map<String, Object> body = Map.of(
                    "appId", props.getAppId(),
                    "docTitle", subject,
                    "signerName", signerName,
                    "signerMobile", signerMobile);
            Map<String, Object> resp = client.post()
                    .uri("/api/v5/signtask/create")
                    .header("X-App-Id", props.getAppId())
                    // 真实场景：.header("X-Timestamp", ts).header("X-Signature", hmac(...))
                    .body(body).retrieve().body(Map.class);
            Object url = resp == null ? null : resp.get("signUrl");
            log.info("[法大大] 为 {} 创建《{}》签署任务", signerName, subject);
            return url == null ? "FAIL" : url.toString();
        } catch (Exception e) {
            log.error("[法大大] 创建签署异常", e);
            return "FAIL:" + e.getMessage();
        }
    }

    @Component
    @ConfigurationProperties(prefix = "fv.device.esign-config")
    public static class Props {
        private String baseUrl = "https://api.fadada.com";
        private String appId = "";
        private String appSecret = "";
        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String v) { this.baseUrl = v; }
        public String getAppId() { return appId; }
        public void setAppId(String v) { this.appId = v; }
        public String getAppSecret() { return appSecret; }
        public void setAppSecret(String v) { this.appSecret = v; }
    }
}
