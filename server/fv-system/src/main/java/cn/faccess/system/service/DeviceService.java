package cn.faccess.system.service;

import cn.faccess.common.exception.BizException;
import cn.faccess.system.entity.SysDevice;
import cn.faccess.system.mapper.SysDeviceMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 设备档案：注册（生成设备编号+密钥）、绑定门岗、心跳、密钥校验。
 * 设备回调鉴权与在线状态由 fv-device 调用本服务。
 */
@Service
public class DeviceService {

    private final SysDeviceMapper deviceMapper;

    public DeviceService(SysDeviceMapper deviceMapper) {
        this.deviceMapper = deviceMapper;
    }

    public List<SysDevice> list(String type) {
        return deviceMapper.selectList(Wrappers.<SysDevice>lambdaQuery()
                .eq(type != null && !type.isBlank(), SysDevice::getType, type)
                .orderByDesc(SysDevice::getId));
    }

    public SysDevice create(SysDevice d) {
        d.setId(null);
        d.setDeviceNo("D" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase());
        d.setSecret(UUID.randomUUID().toString().replace("-", ""));
        d.setOnline(0);
        if (d.getStatus() == null) d.setStatus(1);
        deviceMapper.insert(d);
        return d;
    }

    public void update(Long id, SysDevice d) {
        SysDevice exist = deviceMapper.selectById(id);
        if (exist == null) throw new BizException(404, "设备不存在");
        d.setId(id);
        d.setDeviceNo(exist.getDeviceNo());
        d.setSecret(exist.getSecret());
        deviceMapper.updateById(d);
    }

    public void delete(Long id) {
        deviceMapper.deleteById(id);
    }

    /** 设备回调鉴权：校验 deviceNo + secret，返回设备（跨租户，供 /api/device 使用）。 */
    public SysDevice authenticate(String deviceNo, String secret) {
        SysDevice d = deviceMapper.selectOne(Wrappers.<SysDevice>lambdaQuery()
                .eq(SysDevice::getDeviceNo, deviceNo).last("limit 1"));
        if (d == null || d.getSecret() == null || !d.getSecret().equals(secret)) {
            throw new BizException(401, "设备鉴权失败");
        }
        return d;
    }

    /** 心跳：更新在线状态与最近心跳时间。 */
    public void heartbeat(Long deviceId) {
        SysDevice d = deviceMapper.selectById(deviceId);
        if (d == null) return;
        d.setOnline(1);
        d.setLastHeartbeat(LocalDateTime.now());
        deviceMapper.updateById(d);
    }

    /** 离线巡检：超过阈值未心跳则置离线，返回本次转为离线的设备。 */
    public List<SysDevice> scanOffline(int offlineSeconds) {
        LocalDateTime threshold = LocalDateTime.now().minusSeconds(offlineSeconds);
        List<SysDevice> stale = deviceMapper.selectList(Wrappers.<SysDevice>lambdaQuery()
                .eq(SysDevice::getOnline, 1)
                .and(w -> w.lt(SysDevice::getLastHeartbeat, threshold).or().isNull(SysDevice::getLastHeartbeat)));
        for (SysDevice d : stale) {
            d.setOnline(0);
            deviceMapper.updateById(d);
        }
        return stale;
    }
}
