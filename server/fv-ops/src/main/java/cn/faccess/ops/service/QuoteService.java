package cn.faccess.ops.service;

import cn.faccess.common.exception.BizException;
import cn.faccess.ops.entity.BizAddon;
import cn.faccess.ops.entity.BizHardware;
import cn.faccess.ops.entity.BizPlan;
import cn.faccess.ops.entity.BizQuote;
import cn.faccess.ops.mapper.BizAddonMapper;
import cn.faccess.ops.mapper.BizHardwareMapper;
import cn.faccess.ops.mapper.BizPlanMapper;
import cn.faccess.ops.mapper.BizQuoteMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 配置报价：按 版本×厂区 + 增值模块 + 硬件(购买一次性/租赁月费) 计算，生成报价单。
 */
@Service
public class QuoteService {

    private static final Logger log = LoggerFactory.getLogger(QuoteService.class);
    private static final AtomicInteger SEQ = new AtomicInteger(0);

    private final BizPlanMapper planMapper;
    private final BizAddonMapper addonMapper;
    private final BizHardwareMapper hardwareMapper;
    private final BizQuoteMapper quoteMapper;
    private final ObjectMapper objectMapper;

    public QuoteService(BizPlanMapper planMapper, BizAddonMapper addonMapper, BizHardwareMapper hardwareMapper,
                        BizQuoteMapper quoteMapper, ObjectMapper objectMapper) {
        this.planMapper = planMapper;
        this.addonMapper = addonMapper;
        this.hardwareMapper = hardwareMapper;
        this.quoteMapper = quoteMapper;
        this.objectMapper = objectMapper;
    }

    @SuppressWarnings("unchecked")
    public BizQuote create(Map<String, Object> req) {
        String planCode = (String) req.get("planCode");
        int siteCount = req.get("siteCount") == null ? 1 : ((Number) req.get("siteCount")).intValue();
        List<String> addons = (List<String>) req.getOrDefault("addons", List.of());
        List<Map<String, Object>> hardware = (List<Map<String, Object>>) req.getOrDefault("hardware", List.of());

        BizPlan plan = planMapper.selectOne(Wrappers.<BizPlan>lambdaQuery().eq(BizPlan::getCode, planCode));
        if (plan == null) throw new BizException(404, "套餐不存在");
        boolean custom = plan.getIsCustom() != null && plan.getIsCustom() == 1;

        List<Map<String, Object>> items = new ArrayList<>();
        BigDecimal monthly = BigDecimal.ZERO;
        BigDecimal onetime = BigDecimal.ZERO;

        if (!custom) {
            BigDecimal planFee = plan.getMonthlyPrice().multiply(BigDecimal.valueOf(siteCount));
            monthly = monthly.add(planFee);
            items.add(item(plan.getName(), "平台订阅", siteCount, plan.getMonthlyPrice(), "订阅/月·厂区", planFee + "/月"));
        } else {
            items.add(item(plan.getName(), "定制方案", siteCount, null, "面议", "面议"));
        }

        for (String code : addons) {
            BizAddon a = addonMapper.selectOne(Wrappers.<BizAddon>lambdaQuery().eq(BizAddon::getCode, code));
            if (a == null) continue;
            if ("MONTHLY".equals(a.getBillType())) {
                monthly = monthly.add(a.getPrice());
                items.add(item(a.getName(), "增值模块", 1, a.getPrice(), "订阅/月", a.getPrice() + "/月"));
            } else {
                items.add(item(a.getName(), "按" + (a.getUnit() == null ? "量" : a.getUnit()) + "计费", 1, a.getPrice(), "/" + a.getUnit(), "按量"));
            }
        }

        for (Map<String, Object> hw : hardware) {
            String code = (String) hw.get("code");
            String mode = (String) hw.getOrDefault("mode", "buy");
            int qty = hw.get("qty") == null ? 1 : ((Number) hw.get("qty")).intValue();
            if (qty <= 0) continue;
            BizHardware h = hardwareMapper.selectOne(Wrappers.<BizHardware>lambdaQuery().eq(BizHardware::getCode, code));
            if (h == null) continue;
            if ("rent".equals(mode) && h.getRentPrice() != null) {
                BigDecimal fee = h.getRentPrice().multiply(BigDecimal.valueOf(qty));
                monthly = monthly.add(fee);
                items.add(item(h.getName(), "租赁(含维保)", qty, h.getRentPrice(), "租赁/月", fee + "/月"));
            } else {
                BigDecimal fee = h.getBuyPrice().multiply(BigDecimal.valueOf(qty));
                onetime = onetime.add(fee);
                items.add(item(h.getName(), "购买(含质保)", qty, h.getBuyPrice(), "一次性", fee.toString()));
            }
        }

        BigDecimal firstYear = custom ? BigDecimal.ZERO : monthly.multiply(BigDecimal.valueOf(12)).add(onetime);

        BizQuote q = new BizQuote();
        q.setQuoteNo(genNo());
        q.setCustomerName((String) req.get("customerName"));
        q.setContactName((String) req.get("contactName"));
        q.setContactPhone((String) req.get("contactPhone"));
        q.setSalesName((String) req.get("salesName"));
        q.setMonthlyTotal(monthly);
        q.setOnetimeTotal(onetime);
        q.setFirstYear(firstYear);
        q.setIsCustom(custom ? 1 : 0);
        q.setValidDays(30);
        q.setStatus(1);
        try { q.setItems(objectMapper.writeValueAsString(items)); } catch (Exception ignored) {}
        quoteMapper.insert(q);
        return q;
    }

    public BizQuote get(Long id) {
        BizQuote q = quoteMapper.selectById(id);
        if (q == null) throw new BizException(404, "报价单不存在");
        return q;
    }

    public void send(Long id) {
        BizQuote q = get(id);
        q.setStatus(2);
        quoteMapper.updateById(q);
        log.info("[报价单] {} 已发送给客户 {}", q.getQuoteNo(), q.getCustomerName());
    }

    private Map<String, Object> item(String name, String spec, int qty, BigDecimal unit, String mode, String sub) {
        Map<String, Object> m = new java.util.HashMap<>();
        m.put("name", name);
        m.put("spec", spec);
        m.put("qty", qty);
        m.put("unit", unit);
        m.put("mode", mode);
        m.put("sub", sub);
        return m;
    }

    private String genNo() {
        // 结合毫秒时间戳后缀，避免应用重启后序号从 0 开始导致的重复
        long ts = System.currentTimeMillis() % 100000;
        return "Q" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)
                + "-" + String.format("%04d", SEQ.incrementAndGet()) + String.format("%05d", ts);
    }
}
