package cn.faccess.steel.adapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 交易平台 Mock 实现：内置示例订单/采购单，支持核验带出与净重回写（内存态），
 * 使钢铁主线在无真实交易平台时可端到端跑通。
 */
@Component
@ConditionalOnMissingBean(name = "realTradePlatformAdapter")
public class MockTradePlatformAdapter implements TradePlatformAdapter {

    private static final Logger log = LoggerFactory.getLogger(MockTradePlatformAdapter.class);

    /** 内存态的已提/已收量，key=orderNo。 */
    private final Map<String, BigDecimal> doneMap = new ConcurrentHashMap<>();

    @Override
    public TradeOrderView verifyOrder(String orderNo, String orderType) {
        boolean sales = !"PURCHASE".equalsIgnoreCase(orderType);
        BigDecimal plan = sales ? new BigDecimal("120.000") : new BigDecimal("500.000");
        BigDecimal doneDefault = sales ? new BigDecimal("78.500") : new BigDecimal("312.600");
        BigDecimal done = doneMap.getOrDefault(orderNo, doneDefault);
        return TradeOrderView.builder()
                .orderNo(orderNo)
                .orderType(sales ? "SALES" : "PURCHASE")
                .partnerName(sales ? "中建八局采购中心" : "中拓废旧金属回收")
                .goodsName(sales ? "螺纹钢 HRB400E" : "重型废钢")
                .goodsSpec(sales ? "Φ12" : "6-9mm")
                .planQty(plan)
                .doneQty(done)
                .remainQty(plan.subtract(done))
                .settleStatus(sales ? "已付款" : "待结算")
                .valid(true)
                .build();
    }

    @Override
    public void writeBack(String orderNo, String orderType, BigDecimal netWeight) {
        boolean sales = !"PURCHASE".equalsIgnoreCase(orderType);
        BigDecimal doneDefault = sales ? new BigDecimal("78.500") : new BigDecimal("312.600");
        BigDecimal done = doneMap.getOrDefault(orderNo, doneDefault).add(netWeight);
        doneMap.put(orderNo, done);
        log.info("[交易平台回写] 单据 {} 净重 {} 吨已回写，累计已{} {} 吨",
                orderNo, netWeight, sales ? "提" : "收", done);
    }
}
