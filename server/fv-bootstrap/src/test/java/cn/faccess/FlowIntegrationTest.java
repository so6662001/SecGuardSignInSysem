package cn.faccess;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 端到端集成测试（真实 MySQL 测试库 + Redis）：
 * 运营登录 → 企业申请开通 → 审核开通 → 租户登录 → 入场登记 → 审批 → 在场 → 离场 → 台账，
 * 以及钢铁 车辆到厂 → 过磅 → 放行。
 * 通过 FV_TEST_DB_URL / FV_TEST_REDIS_HOST 等环境变量配置依赖（默认本机 faccess_test）。
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FlowIntegrationTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper om;

    static String opsToken;
    static String tenantToken;
    static long appId;
    static long approvalId;
    static long recordId;
    static long vehicleVisitId;
    static final String MOBILE = "138" + String.format("%08d", (int) (System.currentTimeMillis() % 100000000));
    static final String DOMAIN = "it" + (System.currentTimeMillis() % 100000);

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", () -> System.getenv().getOrDefault("FV_TEST_DB_URL",
                "jdbc:mysql://127.0.0.1:3306/faccess_test?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false"));
        r.add("spring.datasource.username", () -> System.getenv().getOrDefault("FV_TEST_DB_USER", "root"));
        r.add("spring.datasource.password", () -> System.getenv().getOrDefault("FV_TEST_DB_PASSWORD", "faccess123"));
        r.add("spring.data.redis.host", () -> System.getenv().getOrDefault("FV_TEST_REDIS_HOST", "127.0.0.1"));
        r.add("fv.approval.scan-interval-ms", () -> "600000");
    }

    private JsonNode callPost(String url, String body, String token) throws Exception {
        var req = post(url).contentType(MediaType.APPLICATION_JSON);
        if (body != null) req = req.content(body);
        if (token != null) req = req.header("Authorization", "Bearer " + token);
        MvcResult res = mvc.perform(req).andExpect(status().isOk()).andReturn();
        return om.readTree(res.getResponse().getContentAsString());
    }

    private JsonNode callGet(String url, String token) throws Exception {
        var req = get(url);
        if (token != null) req = req.header("Authorization", "Bearer " + token);
        MvcResult res = mvc.perform(req).andExpect(status().isOk()).andReturn();
        return om.readTree(res.getResponse().getContentAsString());
    }

    @Test @Order(1)
    void opsLogin() throws Exception {
        JsonNode d = callPost("/api/ops/auth/login", "{\"username\":\"admin\",\"password\":\"admin123\"}", null);
        assertEquals(0, d.get("code").asInt());
        opsToken = d.get("data").get("accessToken").asText();
        assertNotNull(opsToken);
    }

    @Test @Order(2)
    void apply() throws Exception {
        JsonNode d = callPost("/api/public/apply",
                "{\"companyName\":\"集成测试厂\",\"industry\":\"钢铁\",\"contactName\":\"测试\",\"contactMobile\":\"" + MOBILE
                        + "\",\"password\":\"it888888\",\"planCode\":\"STEEL\",\"smsCode\":\"1\"}", null);
        assertEquals(0, d.get("code").asInt());
        JsonNode list = callGet("/api/ops/applications?status=1", opsToken).get("data").get("list");
        appId = list.get(0).get("id").asLong();
        assertTrue(appId > 0);
    }

    @Test @Order(3)
    void approve() throws Exception {
        JsonNode d = callPost("/api/ops/applications/" + appId + "/approve", "{\"domain\":\"" + DOMAIN + "\"}", opsToken);
        assertEquals(0, d.get("code").asInt());
        assertEquals(MOBILE, d.get("data").get("adminUsername").asText());
    }

    @Test @Order(4)
    void tenantLogin() throws Exception {
        JsonNode d = callPost("/api/tenant/auth/login",
                "{\"domain\":\"" + DOMAIN + "\",\"username\":\"" + MOBILE + "\",\"password\":\"it888888\"}", null);
        assertEquals(0, d.get("code").asInt());
        tenantToken = d.get("data").get("accessToken").asText();
        assertTrue(d.get("data").get("user").get("roles").toString().contains("TENANT_ADMIN"));
    }

    @Test @Order(5)
    void checkin() throws Exception {
        JsonNode d = callPost("/api/tenant/visits",
                "{\"visitorName\":\"访客IT\",\"visitorMobile\":\"13700000001\",\"company\":\"顺丰\",\"reason\":\"洽谈业务\",\"hostName\":\"王工\"}", tenantToken);
        assertEquals("PENDING", d.get("data").get("status").asText());
        assertNotNull(d.get("data").get("badgeNo").asText());
        recordId = d.get("data").get("id").asLong();
        approvalId = callGet("/api/tenant/approvals/pending", tenantToken).get("data").get(0).get("id").asLong();
    }

    @Test @Order(6)
    void approveAndOnsite() throws Exception {
        callPost("/api/tenant/approvals/" + approvalId + "/decision", "{\"approve\":true}", tenantToken);
        JsonNode onsite = callGet("/api/tenant/visits/onsite", tenantToken).get("data").get("list");
        assertTrue(onsite.size() >= 1);
        assertEquals("ONSITE", onsite.get(0).get("status").asText());
    }

    @Test @Order(7)
    void checkoutAndLedger() throws Exception {
        callPost("/api/tenant/visits/" + recordId + "/checkout", null, tenantToken);
        JsonNode d = callGet("/api/tenant/visits?status=LEFT", tenantToken).get("data").get("list");
        assertTrue(d.size() >= 1);
        assertEquals("LEFT", d.get(0).get("status").asText());
    }

    @Test @Order(8)
    void steelFlow() throws Exception {
        JsonNode vv = callPost("/api/tenant/steel/vehicle-visits",
                "{\"plateNo\":\"粤B·IT001\",\"direction\":\"OUTBOUND\",\"orderNo\":\"SO-IT-1\",\"deductRate\":0}", tenantToken);
        vehicleVisitId = vv.get("data").get("id").asLong();
        callPost("/api/tenant/steel/weigh", "{\"vehicleVisitId\":" + vehicleVisitId + ",\"weight\":12.5,\"weighType\":\"TARE\"}", tenantToken);
        JsonNode w = callPost("/api/tenant/steel/weigh", "{\"vehicleVisitId\":" + vehicleVisitId + ",\"weight\":42.5,\"weighType\":\"GROSS\"}", tenantToken);
        assertEquals(30.0, w.get("data").get("netWeight").asDouble(), 0.001);
        JsonNode rel = callPost("/api/tenant/steel/vehicle-visits/" + vehicleVisitId + "/release", null, tenantToken);
        assertEquals("RELEASED", rel.get("data").get("status").asText());
    }

    @Test @Order(9)
    void opsFunnelAndQuote() throws Exception {
        JsonNode funnel = callGet("/api/ops/funnel", opsToken).get("data");
        assertTrue(funnel.has("REGISTER"));
        JsonNode q = callPost("/api/ops/quotes", "{\"planCode\":\"STANDARD\",\"siteCount\":2,\"addons\":[\"OCR\"],\"hardware\":[]}", opsToken);
        assertEquals(219.0, q.get("data").get("monthlyTotal").asDouble(), 0.001);
    }
}
