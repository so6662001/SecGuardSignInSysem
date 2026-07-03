package cn.faccess.notify.controller;

import cn.faccess.common.tenant.TenantContext;
import cn.faccess.common.web.R;
import cn.faccess.notify.entity.NotifyChannelConfig;
import cn.faccess.notify.mapper.NotifyChannelConfigMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "tenant-notify", description = "审批与通知配置")
@RestController
@RequestMapping("/api/tenant/notify-config")
public class NotifyConfigController {

    private final NotifyChannelConfigMapper configMapper;

    public NotifyConfigController(NotifyChannelConfigMapper configMapper) {
        this.configMapper = configMapper;
    }

    @Operation(summary = "获取通知渠道与升级配置")
    @GetMapping
    public R<NotifyChannelConfig> get() {
        NotifyChannelConfig cfg = current();
        if (cfg == null) {
            cfg = new NotifyChannelConfig();
            cfg.setChannelOrder("PUSH,SMS,VOICE");
            cfg.setFirstTimeout(180);
            cfg.setSecondRemind(1);
            cfg.setEscalateBackup(1);
            cfg.setGuardProxy(1);
            cfg.setInvitePass(1);
            cfg.setWhitelistPass(1);
            configMapper.insert(cfg);
        }
        return R.ok(cfg);
    }

    @Operation(summary = "保存通知配置")
    @PutMapping
    public R<Void> save(@RequestBody NotifyChannelConfig body) {
        NotifyChannelConfig cfg = current();
        if (cfg == null) {
            configMapper.insert(body);
        } else {
            body.setId(cfg.getId());
            configMapper.updateById(body);
        }
        return R.ok();
    }

    private NotifyChannelConfig current() {
        return configMapper.selectOne(Wrappers.<NotifyChannelConfig>lambdaQuery()
                .eq(NotifyChannelConfig::getTenantId, TenantContext.getTenantId()).last("limit 1"));
    }
}
