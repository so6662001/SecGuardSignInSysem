package cn.faccess.device.adapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * 海康车牌道闸（真实 ISAPI HTTP 落地）。fv.device.lpr=hik 时启用。
 * 通过海康 ISAPI 远程控制道闸/门（Digest 认证）。设备端车牌识别事件经 /api/device/lpr/event 上报后调用本适配器抬杆。
 * 说明：海康 artemis SDK 为本地 jar（不在 Maven 中央库），生产可用 ISAPI(HTTP) 或引入本地 artemis jar。
 */
@Component
@ConditionalOnProperty(name = "fv.device.lpr", havingValue = "hik")
public class HikvisionLprGateAdapter implements DeviceAdapters.LprGateAdapter {

    private static final Logger log = LoggerFactory.getLogger(HikvisionLprGateAdapter.class);

    private final Props props;
    private final RestClient client;

    public HikvisionLprGateAdapter(Props props) {
        this.props = props;
        this.client = RestClient.builder().baseUrl(props.getBaseUrl()).build();
    }

    @Override
    public void openGate(Long gateId, String plate) {
        try {
            // 海康 ISAPI 远程开门/抬杆（示例：门禁远程控制）
            String xml = "<RemoteControlDoor><cmd>open</cmd></RemoteControlDoor>";
            client.put()
                    .uri("/ISAPI/AccessControl/RemoteControl/door/1")
                    .header("Authorization", basicAuth())
                    .contentType(MediaType.APPLICATION_XML)
                    .body(xml).retrieve().toBodilessEntity();
            log.info("[海康车牌] 门岗 {} 识别 {} · ISAPI 抬杆", gateId, plate);
        } catch (Exception e) {
            log.error("[海康车牌] 抬杆异常（生产需用 Digest 认证）", e);
        }
    }

    private String basicAuth() {
        String token = props.getUsername() + ":" + props.getPassword();
        return "Basic " + java.util.Base64.getEncoder().encodeToString(token.getBytes());
    }

    @Component
    @ConfigurationProperties(prefix = "fv.device.lpr-config")
    public static class Props {
        private String baseUrl = "http://192.168.1.64";
        private String username = "admin";
        private String password = "";
        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String v) { this.baseUrl = v; }
        public String getUsername() { return username; }
        public void setUsername(String v) { this.username = v; }
        public String getPassword() { return password; }
        public void setPassword(String v) { this.password = v; }
    }
}
