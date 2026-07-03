package cn.faccess.notify.channel;

/**
 * 通知渠道适配器。真实厂商实现（短信/语音/微信等）替换 Mock 即可，配置切换。
 */
public interface NotifyChannel {

    /** 渠道编码：PUSH/SMS/VOICE/WECHAT/WECOM/DINGTALK。 */
    String code();

    /**
     * 发送。
     * @return 回执（渠道返回的消息ID等）
     */
    String send(String target, String title, String content);
}
