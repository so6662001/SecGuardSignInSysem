package cn.faccess.ops.service;

import cn.faccess.common.exception.BizException;
import cn.faccess.ops.entity.OpsConversion;
import cn.faccess.ops.entity.OpsLead;
import cn.faccess.ops.mapper.OpsConversionMapper;
import cn.faccess.ops.mapper.OpsLeadMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 引流转化：漏斗、转化去向、高潜线索。
 */
@Service
public class ConversionService {

    private static final String[] STAGES = {"REGISTER", "ACTIVE", "POTENTIAL", "OPPORTUNITY", "DEAL"};

    private final OpsConversionMapper conversionMapper;
    private final OpsLeadMapper leadMapper;

    public ConversionService(OpsConversionMapper conversionMapper, OpsLeadMapper leadMapper) {
        this.conversionMapper = conversionMapper;
        this.leadMapper = leadMapper;
    }

    public Map<String, Long> funnel() {
        Map<String, Long> m = new LinkedHashMap<>();
        for (String s : STAGES) {
            m.put(s, conversionMapper.selectCount(Wrappers.<OpsConversion>lambdaQuery().eq(OpsConversion::getStage, s)));
        }
        return m;
    }

    public List<OpsLead> leads(Integer status) {
        return leadMapper.selectList(Wrappers.<OpsLead>lambdaQuery()
                .eq(status != null, OpsLead::getStatus, status)
                .orderByDesc(OpsLead::getIntentScore));
    }

    public void assign(Long id, Long ownerId) {
        OpsLead lead = leadMapper.selectById(id);
        if (lead == null) throw new BizException(404, "线索不存在");
        lead.setOwnerId(ownerId);
        lead.setStatus(2);
        leadMapper.updateById(lead);
    }
}
