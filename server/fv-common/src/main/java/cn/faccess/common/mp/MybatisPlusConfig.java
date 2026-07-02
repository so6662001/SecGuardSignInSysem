package cn.faccess.common.mp;

import cn.faccess.common.tenant.TenantContext;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

/**
 * MyBatis-Plus 配置：多租户行级隔离 + 分页。
 * 平台域表不参与租户过滤（见 IGNORE_TABLES）。
 */
@Configuration
public class MybatisPlusConfig {

    /** 不参与租户过滤的表（平台域 / 全局字典）。 */
    private static final Set<String> IGNORE_TABLES = Set.of(
            // 平台域表
            "sys_tenant", "tenant_application", "biz_plan", "biz_addon", "biz_hardware",
            "tenant_subscription", "biz_invoice", "biz_quote", "ops_lead", "ops_conversion",
            // 全局字典 / 无 tenant_id 列的关联表
            "sys_permission", "sys_menu", "sys_user_role", "sys_role_permission", "notify_template"
    );

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        TenantLineInnerInterceptor tenant = new TenantLineInnerInterceptor(new TenantLineHandler() {
            @Override
            public Expression getTenantId() {
                Long tid = TenantContext.getTenantId();
                return new LongValue(tid == null ? 0L : tid);
            }

            @Override
            public String getTenantIdColumn() {
                return "tenant_id";
            }

            @Override
            public boolean ignoreTable(String tableName) {
                // 平台域表、或未登录（无租户上下文，如启动/系统任务）时不加租户条件
                return IGNORE_TABLES.contains(tableName.toLowerCase()) || TenantContext.getTenantId() == null;
            }
        });
        interceptor.addInnerInterceptor(tenant);
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
