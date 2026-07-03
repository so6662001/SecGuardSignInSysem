package cn.faccess.notify.channel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 阿里云短信渠道实现骨架。配置 fv.channel.sms=aliyun 时启用（替代 Mock）。
 * 真实对接：引入 aliyun-java-sdk-dysmsapi，用 signName+templateCode+templateParam 发送。
 * 这里保留结构与配置读取，未引入 SDK 依赖时以日志代替真实调用。
 */
@Component
@ConditionalOnProperty(name = "fv.channel.sms", havingValue = "aliyun")
public class AliyunSmsChannel implements NotifyChannel {

    private static final Logger log = LoggerFactory.getLogger(AliyunSmsChannel.class);

    private final SmsProperties props;

    public AliyunSmsChannel(SmsProperties props) {
        this.props = props;
    }

    @Override
    public String code() {
        return "SMS";
    }

    @Override
    public String send(String target, String title, String content) {
        // TODO: 接入 com.aliyun.dysmsapi20170525 Client：
        //   SendSmsRequest req = new SendSmsRequest()
        //       .setPhoneNumbers(target).setSignName(props.getSignName())
        //       .setTemplateCode(props.getTemplateCode()).setTemplateParam(json);
        //   client.sendSms(req);
        log.info("[阿里云短信] accessKey={} sign={} -> {} : {}",
                mask(props.getAccessKeyId()), props.getSignName(), target, content);
        return "ALIYUN-SIM-" + System.currentTimeMillis();
    }

    private String mask(String s) {
        if (s == null || s.length() < 6) return "****";
        return s.substring(0, 3) + "****" + s.substring(s.length() - 3);
    }
}
