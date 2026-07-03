package cn.faccess.steel.adapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 交易平台 REST 对接实现骨架。配置 fv.trade.provider=rest 时启用（替代 Mock）。
 * 通过 RestClient 调用「钢智汇」交易平台/ERP 的单据接口，读取 base-url 与鉴权。
 */
@Component("realTradePlatformAdapter")
@ConditionalOnProperty(name = "fv.trade.provider", havingValue = "rest")
public class RestTradePlatformAdapter implements TradePlatformAdapter {

    private static final Logger log = LoggerFactory.getLogger(RestTradePlatformAdapter.class);

    private final RestClient client;
    private final Props props;

    public RestTradePlatformAdapter(Props props) {
        this.props = props;
        this.client = RestClient.builder()
                .baseUrl(props.getBaseUrl())
                .defaultHeader("X-Api-Key", props.getApiKey())
                .build();
    }

    @Override
    @SuppressWarnings("unchecked")
    public TradeOrderView verifyOrder(String orderNo, String orderType) {
        Map<String, Object> body = client.get()
                .uri("/api/orders/{no}?type={t}", orderNo, orderType)
                .retrieve().body(Map.class);
        if (body == null) {
            return TradeOrderView.builder().orderNo(orderNo).orderType(orderType).valid(false).build();
        }
        return TradeOrderView.builder()
                .orderNo(orderNo).orderType(orderType)
                .partnerName((String) body.get("partnerName"))
                .goodsName((String) body.get("goodsName"))
                .planQty(num(body.get("planQty")))
                .doneQty(num(body.get("doneQty")))
                .remainQty(num(body.get("remainQty")))
                .settleStatus((String) body.get("settleStatus"))
                .valid(true)
                .build();
    }

    @Override
    public void writeBack(String orderNo, String orderType, BigDecimal netWeight) {
        client.post().uri("/api/orders/{no}/writeback", orderNo)
                .body(Map.of("orderType", orderType, "netWeight", netWeight))
                .retrieve().toBodilessEntity();
        log.info("[交易平台REST] 已回写 {} 净重 {}", orderNo, netWeight);
    }

    private BigDecimal num(Object o) {
        return o == null ? null : new BigDecimal(o.toString());
    }

    @Component
    @ConfigurationProperties(prefix = "fv.trade")
    public static class Props {
        private String provider = "mock";
        private String baseUrl = "http://localhost:9999";
        private String apiKey = "";
        public String getProvider() { return provider; }
        public void setProvider(String v) { this.provider = v; }
        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String v) { this.baseUrl = v; }
        public String getApiKey() { return apiKey; }
        public void setApiKey(String v) { this.apiKey = v; }
    }
}
