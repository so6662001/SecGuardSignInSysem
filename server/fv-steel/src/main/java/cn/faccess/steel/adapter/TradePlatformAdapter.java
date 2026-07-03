package cn.faccess.steel.adapter;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 交易平台/ERP 对接适配器。Mock 实现内置示例单据，生产替换为真实 REST/MQ 对接。
 */
public interface TradePlatformAdapter {

    /** 核验并带出单据信息。 */
    TradeOrderView verifyOrder(String orderNo, String orderType);

    /** 放行后回写本车净重，更新已提/已收与剩余。 */
    void writeBack(String orderNo, String orderType, BigDecimal netWeight);

    @Data
    @Builder
    class TradeOrderView {
        private String orderNo;
        private String orderType;
        private String partnerName;
        private String goodsName;
        private String goodsSpec;
        private BigDecimal planQty;
        private BigDecimal doneQty;
        private BigDecimal remainQty;
        private String settleStatus;
        private boolean valid;
    }
}
