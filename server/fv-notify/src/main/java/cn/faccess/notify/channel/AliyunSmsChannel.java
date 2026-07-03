package cn.faccess.notify.channel;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.teaopenapi.models.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 阿里云短信渠道（真实 SDK 落地）。配置 fv.channel.sms=aliyun 时启用。
 * 说明：阿里云短信按「签名 + 模板 + 模板参数」发送，content 作为模板参数注入。
 */
@Component
@ConditionalOnProperty(name = "fv.channel.sms", havingValue = "aliyun")
public class AliyunSmsChannel implements NotifyChannel {

    private static final Logger log = LoggerFactory.getLogger(AliyunSmsChannel.class);

    private final SmsProperties props;
    private volatile Client client;

    public AliyunSmsChannel(SmsProperties props) {
        this.props = props;
    }

    @Override
    public String code() {
        return "SMS";
    }

    @Override
    public String send(String target, String title, String content) {
        try {
            SendSmsRequest req = new SendSmsRequest()
                    .setPhoneNumbers(target)
                    .setSignName(props.getSignName())
                    .setTemplateCode(props.getTemplateCode())
                    .setTemplateParam("{\"content\":\"" + escape(content) + "\"}");
            SendSmsResponse resp = client().sendSms(req);
            String code = resp.getBody() == null ? null : resp.getBody().getCode();
            String bizId = resp.getBody() == null ? null : resp.getBody().getBizId();
            if (!"OK".equals(code)) {
                log.warn("[阿里云短信] 发送失败 code={} msg={}", code, resp.getBody() == null ? "" : resp.getBody().getMessage());
                return "FAIL:" + code;
            }
            return bizId;
        } catch (Exception e) {
            log.error("[阿里云短信] 异常", e);
            return "FAIL:" + e.getMessage();
        }
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

    private String escape(String s) {
        return s == null ? "" : s.replace("\"", "\\\"");
    }
}
