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
        strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);
        strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);
        strictInsertFill(metaObject, "createBy", Long.class, TenantContext.getUserId());
        strictInsertFill(metaObject, "updateBy", Long.class, TenantContext.getUserId());
        strictInsertFill(metaObject, "deleted", Integer.class, 0);
        // tenant_id 由 TenantLineInnerInterceptor 在 SQL 层注入，不在此填充
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        strictUpdateFill(metaObject, "updateBy", Long.class, TenantContext.getUserId());
    }
}
