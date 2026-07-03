package cn.faccess.notify.channel;

import com.aliyun.dyvmsapi20170525.Client;
import com.aliyun.dyvmsapi20170525.models.SingleCallByTtsRequest;
import com.aliyun.dyvmsapi20170525.models.SingleCallByTtsResponse;
import com.aliyun.teaopenapi.models.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 阿里云语音通知渠道（真实 SDK 落地）。fv.channel.voice=aliyun 时启用。
 * 使用「文本转语音（TTS）模板」外呼；审批场景配合 IVR 可实现「按1同意/按2拒绝」。
 */
@Component
@ConditionalOnProperty(name = "fv.channel.voice", havingValue = "aliyun")
public class AliyunVoiceChannel implements NotifyChannel {

    private static final Logger log = LoggerFactory.getLogger(AliyunVoiceChannel.class);

    private final VoiceProperties props;
    private volatile Client client;

    public AliyunVoiceChannel(VoiceProperties props) {
        this.props = props;
    }

    @Override
    public String code() {
        return "VOICE";
    }

    @Override
    public String send(String target, String title, String content) {
        try {
            SingleCallByTtsRequest req = new SingleCallByTtsRequest()
                    .setCalledNumber(target)
                    .setTtsCode(props.getTtsCode())
                    .setTtsParam("{\"content\":\"" + escape(content) + "\"}");
            SingleCallByTtsResponse resp = client().singleCallByTts(req);
            String code = resp.getBody() == null ? null : resp.getBody().getCode();
            if (!"OK".equals(code)) {
                log.warn("[阿里云语音] 外呼失败 code={} msg={}", code, resp.getBody() == null ? "" : resp.getBody().getMessage());
                return "FAIL:" + code;
            }
            return resp.getBody().getCallId();
        } catch (Exception e) {
            log.error("[阿里云语音] 异常", e);
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
