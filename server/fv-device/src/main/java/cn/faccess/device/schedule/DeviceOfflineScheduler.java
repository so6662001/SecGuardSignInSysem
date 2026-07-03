package cn.faccess.device.schedule;

import cn.faccess.system.entity.SysDevice;
import cn.faccess.system.service.DeviceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 设备心跳巡检：超过阈值未心跳的设备置离线并告警。
 */
@Component
public class DeviceOfflineScheduler {

    private static final Logger log = LoggerFactory.getLogger(DeviceOfflineScheduler.class);

    private final DeviceService deviceService;

    @Value("${fv.device.offline-seconds:120}")
    private int offlineSeconds;

    public DeviceOfflineScheduler(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Scheduled(fixedDelayString = "${fv.device.scan-interval-ms:60000}")
    public void scan() {
        List<SysDevice> offline = deviceService.scanOffline(offlineSeconds);
        for (SysDevice d : offline) {
            log.warn("[设备离线告警] 设备 {}（{}）超过 {}s 未心跳，已置离线", d.getName(), d.getDeviceNo(), offlineSeconds);
        }
    }
}
