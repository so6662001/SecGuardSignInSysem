package cn.faccess.visitor.service;

import cn.faccess.common.exception.BizException;
import cn.faccess.common.tenant.TenantContext;
import cn.faccess.system.entity.SysGate;
import cn.faccess.system.mapper.SysGateMapper;
import cn.faccess.visitor.entity.RegisterField;
import cn.faccess.visitor.entity.RegisterTemplate;
import cn.faccess.visitor.mapper.RegisterFieldMapper;
import cn.faccess.visitor.mapper.RegisterTemplateMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 登记模板与字段。
 */
@Service
public class TemplateService {

    private final RegisterTemplateMapper templateMapper;
    private final RegisterFieldMapper fieldMapper;
    private final SysGateMapper gateMapper;

    public TemplateService(RegisterTemplateMapper templateMapper, RegisterFieldMapper fieldMapper, SysGateMapper gateMapper) {
        this.templateMapper = templateMapper;
        this.fieldMapper = fieldMapper;
        this.gateMapper = gateMapper;
    }

    public List<RegisterTemplate> list() {
        return templateMapper.selectList(Wrappers.<RegisterTemplate>lambdaQuery().orderByAsc(RegisterTemplate::getSort));
    }

    public Map<String, Object> get(Long id) {
        RegisterTemplate t = templateMapper.selectById(id);
        if (t == null) throw new BizException(404, "模板不存在");
        Map<String, Object> m = new HashMap<>();
        m.put("template", t);
        m.put("fields", fieldMapper.selectList(Wrappers.<RegisterField>lambdaQuery()
                .eq(RegisterField::getTemplateId, id).orderByAsc(RegisterField::getSort)));
        return m;
    }

    @Transactional(rollbackFor = Exception.class)
    public Long create(RegisterTemplate t) {
        t.setId(null);
        if (t.getStatus() == null) t.setStatus(0);
        templateMapper.insert(t);
        return t.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveFields(Long templateId, List<RegisterField> fields) {
        fieldMapper.delete(Wrappers.<RegisterField>lambdaQuery().eq(RegisterField::getTemplateId, templateId));
        if (fields != null) {
            int i = 0;
            for (RegisterField f : fields) {
                f.setId(null);
                f.setTemplateId(templateId);
                f.setSort(i++);
                if (f.getStatus() == null) f.setStatus(1);
                fieldMapper.insert(f);
            }
        }
    }

    public void publish(Long id) {
        RegisterTemplate t = templateMapper.selectById(id);
        if (t == null) throw new BizException(404, "模板不存在");
        t.setStatus(1);
        templateMapper.updateById(t);
    }

    /** 扫码取门岗登记模板 schema（免登录）。 */
    public Map<String, Object> schemaByGate(String gateCode) {
        SysGate gate = gateMapper.selectOne(Wrappers.<SysGate>lambdaQuery().eq(SysGate::getGateCode, gateCode).last("limit 1"));
        if (gate == null) throw new BizException(404, "门岗不存在");
        TenantContext.set(gate.getTenantId(), null, "public-gate", List.of());
        try {
            RegisterTemplate t = templateMapper.selectOne(Wrappers.<RegisterTemplate>lambdaQuery()
                    .eq(RegisterTemplate::getStatus, 1).orderByDesc(RegisterTemplate::getIsDefault)
                    .orderByAsc(RegisterTemplate::getSort).last("limit 1"));
            Map<String, Object> schema = new HashMap<>();
            schema.put("gateName", gate.getName());
            if (t == null) {
                schema.put("templateId", null);
                schema.put("visitType", "NORMAL");
                schema.put("fields", defaultFields());
            } else {
                schema.put("templateId", t.getId());
                schema.put("visitType", t.getVisitType());
                schema.put("fields", fieldMapper.selectList(Wrappers.<RegisterField>lambdaQuery()
                        .eq(RegisterField::getTemplateId, t.getId()).eq(RegisterField::getStatus, 1)
                        .orderByAsc(RegisterField::getSort)));
            }
            return schema;
        } finally {
            TenantContext.clear();
        }
    }

    private List<Map<String, Object>> defaultFields() {
        List<Map<String, Object>> fields = new ArrayList<>();
        fields.add(field("visitorName", "姓名", "TEXT", true));
        fields.add(field("visitorMobile", "手机号", "TEXT", true));
        fields.add(field("company", "来访单位", "TEXT", false));
        fields.add(field("reason", "来访事由", "RADIO", true));
        fields.add(field("hostName", "被访人", "TEXT", true));
        return fields;
    }

    private Map<String, Object> field(String key, String label, String type, boolean required) {
        Map<String, Object> m = new HashMap<>();
        m.put("fieldKey", key);
        m.put("label", label);
        m.put("fieldType", type);
        m.put("required", required);
        return m;
    }
}
