package cn.faccess.common.mp;

import cn.faccess.common.tenant.TenantContext;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 公共字段自动填充：create_time/update_time/create_by/update_by/tenant_id/deleted。
 */
@Component
public class MetaFieldHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        fillIfNull(metaObject, "createTime", now);
        fillIfNull(metaObject, "updateTime", now);
        fillIfNull(metaObject, "createBy", TenantContext.getUserId());
        fillIfNull(metaObject, "updateBy", TenantContext.getUserId());
        fillIfNull(metaObject, "deleted", 0);
        // tenant_id 由 TenantLineInnerInterceptor 在 SQL 层注入，不在此填充
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        // 仅当实体存在该字段时才填充，兼容各表列差异
        if (metaObject.hasSetter("updateTime")) {
            setFieldValByName("updateTime", LocalDateTime.now(), metaObject);
        }
        if (metaObject.hasSetter("updateBy")) {
            setFieldValByName("updateBy", TenantContext.getUserId(), metaObject);
        }
    }

    /** 仅当实体拥有该字段且当前为空时填充（兼容不同表的列差异）。 */
    private void fillIfNull(MetaObject metaObject, String field, Object value) {
        if (metaObject.hasSetter(field) && getFieldValByName(field, metaObject) == null && value != null) {
            setFieldValByName(field, value, metaObject);
        }
    }
}
