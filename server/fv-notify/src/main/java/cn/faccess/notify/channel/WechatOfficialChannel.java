package cn.faccess.notify.channel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.Map;

/**
 * 微信公众号模板消息渠道（真实 HTTP 落地）。配置 fv.channel.wechat=official 时启用。
 * target 为用户 openid；access_token 缓存于 Redis（约 7200s）。
 */
@Component
@ConditionalOnProperty(name = "fv.channel.wechat", havingValue = "official")
public class WechatOfficialChannel implements NotifyChannel {

    private static final Logger log = LoggerFactory.getLogger(WechatOfficialChannel.class);
    private static final String TOKEN_KEY = "wx:access_token";

    private final WechatProperties props;
    private final StringRedisTemplate redis;
    private final RestClient client;

    public WechatOfficialChannel(WechatProperties props, StringRedisTemplate redis) {
        this.props = props;
        this.redis = redis;
        this.client = RestClient.builder().baseUrl(props.getApiBase()).build();
    }

    @Override
    public String code() {
        return "WECHAT";
    }

    @Override
    @SuppressWarnings("unchecked")
    public String send(String target, String title, String content) {
        try {
            String token = accessToken();
            Map<String, Object> body = Map.of(
                    "touser", target,
                    "template_id", props.getTemplateId(),
                    "data", Map.of(
                            "first", Map.of("value", title),
                            "keyword1", Map.of("value", content),
                            "remark", Map.of("value", "厂智访客")
                    ));
            Map<String, Object> resp = client.post()
                    .uri("/cgi-bin/message/template/send?access_token={t}", token)
                    .body(body).retrieve().body(Map.class);
            Object errcode = resp == null ? null : resp.get("errcode");
            if (errcode != null && !"0".equals(String.valueOf(errcode))) {
                log.warn("[微信模板消息] 失败 {}", resp);
                return "FAIL:" + errcode;
            }
            return resp == null ? "OK" : String.valueOf(resp.get("msgid"));
        } catch (Exception e) {
            log.error("[微信模板消息] 异常", e);
            return "FAIL:" + e.getMessage();
        }
    }

    @SuppressWarnings("unchecked")
    private String accessToken() {
        String cached = redis.opsForValue().get(TOKEN_KEY);
        if (cached != null) return cached;
        Map<String, Object> resp = client.get()
                .uri("/cgi-bin/token?grant_type=client_credential&appid={a}&secret={s}",
                        props.getAppId(), props.getAppSecret())
                .retrieve().body(Map.class);
        String token = resp == null ? null : (String) resp.get("access_token");
        if (token != null) {
            redis.opsForValue().set(TOKEN_KEY, token, Duration.ofSeconds(7000));
        }
        return token;
    }
}
