package cn.faccess.notify.service;

import cn.faccess.notify.channel.NotifyChannel;
import cn.faccess.notify.entity.NotifyChannelConfig;
import cn.faccess.notify.entity.NotifyLog;
import cn.faccess.notify.entity.NotifyTask;
import cn.faccess.notify.entity.NotifyTemplate;
import cn.faccess.notify.mapper.NotifyChannelConfigMapper;
import cn.faccess.notify.mapper.NotifyLogMapper;
import cn.faccess.notify.mapper.NotifyTaskMapper;
import cn.faccess.notify.mapper.NotifyTemplateMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 通知编排：按渠道优先级依次触达，模板渲染，写发送日志与任务。
 */
@Service
public class NotifyService {

    private static final Logger log = LoggerFactory.getLogger(NotifyService.class);
    private static final List<String> DEFAULT_ORDER = List.of("PUSH", "SMS");

    private final Map<String, NotifyChannel> channels;
    private final NotifyTemplateMapper templateMapper;
    private final NotifyTaskMapper taskMapper;
    private final NotifyLogMapper logMapper;
    private final NotifyChannelConfigMapper configMapper;

    public NotifyService(List<NotifyChannel> channelList, NotifyTemplateMapper templateMapper,
                         NotifyTaskMapper taskMapper, NotifyLogMapper logMapper,
                         NotifyChannelConfigMapper configMapper) {
        this.channels = channelList.stream().collect(Collectors.toMap(NotifyChannel::code, c -> c));
        this.templateMapper = templateMapper;
        this.taskMapper = taskMapper;
        this.logMapper = logMapper;
        this.configMapper = configMapper;
    }

    /**
     * 按场景发送通知（记录任务 + 逐渠道发送 + 写日志），返回创建的通知任务。
     */
    public NotifyTask notify(String scene, Long tenantId, Long bizId, Long targetUser, String targetMobile,
                             Map<String, Object> vars, Integer timeoutSec) {
        List<String> order = resolveChannelOrder(tenantId);

        NotifyTask task = new NotifyTask();
        task.setTenantId(tenantId);
        task.setScene(scene);
        task.setBizId(bizId);
        task.setTargetUser(targetUser);
        task.setTargetMobile(targetMobile);
        task.setChannels(toJsonArray(order));
        task.setTimeoutSec(timeoutSec);
        task.setStatus("SENT");
        taskMapper.insert(task);

        for (String ch : order) {
            dispatch(tenantId, task.getId(), scene, ch, targetMobile, vars);
        }
        return task;
    }

    /** 单渠道发送（用于二次提醒/升级追加语音等）。 */
    public void sendVia(Long tenantId, Long taskId, String scene, String channel, String target, Map<String, Object> vars) {
        dispatch(tenantId, taskId, scene, channel, target, vars);
    }

    private void dispatch(Long tenantId, Long taskId, String scene, String channelCode, String target, Map<String, Object> vars) {
        NotifyChannel channel = channels.get(channelCode);
        if (channel == null) {
            log.warn("未找到通知渠道实现: {}", channelCode);
            return;
        }
        NotifyTemplate tpl = resolveTemplate(scene, channelCode, tenantId);
        String title = tpl != null ? tpl.getTitle() : scene;
        String content = render(tpl != null ? tpl.getContent() : scene, vars);
        String receipt;
        String result = "SUCCESS";
        try {
            receipt = channel.send(target, title, content);
        } catch (Exception e) {
            result = "FAIL";
            receipt = e.getMessage();
        }
        NotifyLog nl = new NotifyLog();
        nl.setTenantId(tenantId);
        nl.setTaskId(taskId);
        nl.setChannel(channelCode);
        nl.setTarget(target);
        nl.setContent(content);
        nl.setResult(result);
        nl.setReceipt(receipt);
        logMapper.insert(nl);
    }

    private NotifyTemplate resolveTemplate(String scene, String channel, Long tenantId) {
        // 先租户自定义，后平台默认(tenant_id=0)
        return templateMapper.selectList(Wrappers.<NotifyTemplate>lambdaQuery()
                        .eq(NotifyTemplate::getScene, scene)
                        .eq(NotifyTemplate::getChannel, channel)
                        .in(NotifyTemplate::getTenantId, List.of(tenantId == null ? 0L : tenantId, 0L))
                        .orderByDesc(NotifyTemplate::getTenantId))
                .stream().findFirst().orElse(null);
    }

    private List<String> resolveChannelOrder(Long tenantId) {
        if (tenantId == null) return DEFAULT_ORDER;
        NotifyChannelConfig cfg = configMapper.selectOne(Wrappers.<NotifyChannelConfig>lambdaQuery()
                .eq(NotifyChannelConfig::getTenantId, tenantId).last("limit 1"));
        if (cfg != null && cfg.getChannelOrder() != null && !cfg.getChannelOrder().isBlank()) {
            return List.of(cfg.getChannelOrder().split(","));
        }
        return DEFAULT_ORDER;
    }

    private String toJsonArray(List<String> items) {
        return "[" + items.stream().map(s -> "\"" + s + "\"").collect(Collectors.joining(",")) + "]";
    }

    private String render(String template, Map<String, Object> vars) {
        if (template == null) return "";
        String out = template;
        if (vars != null) {
            for (Map.Entry<String, Object> e : vars.entrySet()) {
                out = out.replace("{" + e.getKey() + "}", String.valueOf(e.getValue() == null ? "" : e.getValue()));
            }
        }
        return out;
    }
}
