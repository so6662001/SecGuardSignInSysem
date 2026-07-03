package cn.faccess.device.controller;

import cn.faccess.common.web.R;
import cn.faccess.device.adapter.DeviceAdapters;
import cn.faccess.system.entity.SysDevice;
import cn.faccess.system.service.DeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;

/**
 * 设备回调入口（/api/device/**，设备编号+密钥鉴权，安全白名单放行）。
 */
@Tag(name = "device", description = "设备回调")
@RestController
@RequestMapping("/api/device")
public class DeviceEventController {

    private static final Logger log = LoggerFactory.getLogger(DeviceEventController.class);
    private static final String READING_KEY = "weigh:reading:";

    private final DeviceService deviceService;
    private final DeviceAdapters.LprGateAdapter lprGate;
    private final StringRedisTemplate redis;

    public DeviceEventController(DeviceService deviceService, DeviceAdapters.LprGateAdapter lprGate,
                                 StringRedisTemplate redis) {
        this.deviceService = deviceService;
        this.lprGate = lprGate;
        this.redis = redis;
    }

    @Operation(summary = "设备心跳")
    @PostMapping("/heartbeat")
    public R<Void> heartbeat(@RequestHeader("X-Device-No") String no, @RequestHeader("X-Device-Secret") String secret) {
        SysDevice d = deviceService.authenticate(no, secret);
        deviceService.heartbeat(d.getId());
        return R.ok();
    }

    @Operation(summary = "车牌识别上报")
    @PostMapping("/lpr/event")
    public R<Map<String, Object>> lpr(@RequestHeader("X-Device-No") String no,
                                      @RequestHeader("X-Device-Secret") String secret,
                                      @RequestBody Map<String, Object> body) {
        SysDevice d = deviceService.authenticate(no, secret);
        deviceService.heartbeat(d.getId());
        String plate = String.valueOf(body.get("plate"));
        log.info("[车牌上报] 设备 {} 门岗 {} 识别车牌 {}", no, d.getGateId(), plate);
        // 示意：识别后下发抬杆（真实场景先匹配车辆/待到厂单据与黑白名单）
        lprGate.openGate(d.getGateId(), plate);
        return R.ok(Map.of("plate", plate, "action", "OPEN_GATE"));
    }

    @Operation(summary = "地磅读数上报")
    @PostMapping("/weighbridge/reading")
    public R<Void> weighbridge(@RequestHeader("X-Device-No") String no,
                               @RequestHeader("X-Device-Secret") String secret,
                               @RequestBody Map<String, Object> body) {
        SysDevice d = deviceService.authenticate(no, secret);
        deviceService.heartbeat(d.getId());
        Object weight = body.get("weight");
        boolean stable = Boolean.TRUE.equals(body.get("stable"));
        // 实时读数写入 Redis，供磅房工作站订阅/轮询
        redis.opsForValue().set(READING_KEY + d.getId(), weight + "|" + stable, Duration.ofMinutes(5));
        log.info("[地磅读数] 设备 {} 读数 {} 稳定 {}", no, weight, stable);
        return R.ok();
    }

    @Operation(summary = "人脸识别上报")
    @PostMapping("/face/event")
    public R<Void> face(@RequestHeader("X-Device-No") String no,
                        @RequestHeader("X-Device-Secret") String secret,
                        @RequestBody Map<String, Object> body) {
        SysDevice d = deviceService.authenticate(no, secret);
        deviceService.heartbeat(d.getId());
        log.info("[人脸上报] 设备 {} 识别 {}", no, body.get("personId"));
        return R.ok();
    }

    @Operation(summary = "读取地磅最新读数（磅房工作站轮询）")
    @GetMapping("/weighbridge/{deviceId}/reading")
    public R<Map<String, Object>> latestReading(@PathVariable Long deviceId) {
        String v = redis.opsForValue().get(READING_KEY + deviceId);
        if (v == null) return R.ok(Map.of("weight", 0, "stable", false));
        String[] p = v.split("\\|");
        return R.ok(Map.of("weight", p[0], "stable", Boolean.parseBoolean(p[1])));
    }
}
