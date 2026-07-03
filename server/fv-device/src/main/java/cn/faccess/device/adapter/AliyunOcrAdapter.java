package cn.faccess.device.adapter;

import com.aliyun.ocr_api20210707.Client;
import com.aliyun.ocr_api20210707.models.RecognizeAllTextRequest;
import com.aliyun.ocr_api20210707.models.RecognizeAllTextResponse;
import com.aliyun.teaopenapi.models.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 阿里云 OCR 身份证识别（真实 SDK 落地）。fv.device.ocr=aliyun 时启用。
 * 使用 RecognizeAllText + Type=IdCard 识别，返回结构化姓名/证件号等。
 */
@Component
@ConditionalOnProperty(name = "fv.device.ocr", havingValue = "aliyun")
public class AliyunOcrAdapter implements DeviceAdapters.OcrAdapter {

    private static final Logger log = LoggerFactory.getLogger(AliyunOcrAdapter.class);

    private final Props props;
    private volatile Client client;

    public AliyunOcrAdapter(Props props) {
        this.props = props;
    }

    @Override
    public Map<String, String> recognizeIdCard(String imageUrl) {
        Map<String, String> result = new HashMap<>();
        try {
            RecognizeAllTextRequest req = new RecognizeAllTextRequest()
                    .setUrl(imageUrl)
                    .setType("IdCard");
            RecognizeAllTextResponse resp = client().recognizeAllText(req);
            if (resp.getBody() != null && resp.getBody().getData() != null) {
                // 具体字段结构随版本变化，这里返回原始内容供上层解析
                result.put("raw", com.aliyun.teautil.Common.toJSONString(resp.getBody().getData()));
            }
        } catch (Exception e) {
            log.error("[阿里云OCR] 识别异常", e);
            result.put("error", e.getMessage());
        }
        return result;
    }

    private Client client() throws Exception {
        if (client == null) {
            synchronized (this) {
                if (client == null) {
                    Config config = new Config()
                            .setAccessKeyId(props.getAccessKeyId())
                            .setAccessKeySecret(props.getAccessKeySecret())
                            .setEndpoint(props.getEndpoint());
                    client = new Client(config);
                }
            }
        }
        return client;
    }

    @Component
    @ConfigurationProperties(prefix = "fv.device.ocr-config")
    public static class Props {
        private String accessKeyId = "";
        private String accessKeySecret = "";
        private String endpoint = "ocr-api.cn-hangzhou.aliyuncs.com";
        public String getAccessKeyId() { return accessKeyId; }
        public void setAccessKeyId(String v) { this.accessKeyId = v; }
        public String getAccessKeySecret() { return accessKeySecret; }
        public void setAccessKeySecret(String v) { this.accessKeySecret = v; }
        public String getEndpoint() { return endpoint; }
        public void setEndpoint(String v) { this.endpoint = v; }
    }
}
