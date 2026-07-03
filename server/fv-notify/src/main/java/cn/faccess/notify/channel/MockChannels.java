package cn.faccess.notify.channel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 各渠道 Mock 实现：仅记录日志并返回回执，便于无第三方时端到端跑通。
 * 生产环境用 @ConditionalOnProperty 切换为真实厂商实现（阿里云/腾讯云/微信等）。
 */
public class MockChannels {

    private static final Logger log = LoggerFactory.getLogger(MockChannels.class);

    private static String send(String channel, String target, String title, String content) {
        String receipt = "MOCK-" + UUID.randomUUID().toString().substring(0, 8);
        log.info("[通知-{}] -> {} | {} | {} | 回执 {}", channel, target, title, content, receipt);
        return receipt;
    }

    @Component
    public static class SystemPushChannel implements NotifyChannel {
        public String code() { return "PUSH"; }
        public String send(String target, String title, String content) { return MockChannels.send("PUSH", target, title, content); }
    }

    @Component
    public static class SmsChannel implements NotifyChannel {
        public String code() { return "SMS"; }
        public String send(String target, String title, String content) { return MockChannels.send("SMS", target, title, content); }
    }

    @Component
    public static class VoiceChannel implements NotifyChannel {
        public String code() { return "VOICE"; }
        public String send(String target, String title, String content) { return MockChannels.send("VOICE", target, title, content); }
    }

    @Component
    public static class WechatChannel implements NotifyChannel {
        public String code() { return "WECHAT"; }
        public String send(String target, String title, String content) { return MockChannels.send("WECHAT", target, title, content); }
    }
}
