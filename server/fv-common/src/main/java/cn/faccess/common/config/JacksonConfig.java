package cn.faccess.common.config;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 全局 Jackson 配置：将 Long / BigInteger 序列化为字符串。
 * 雪花算法生成的 19 位 ID 超过 JS Number.MAX_SAFE_INTEGER(2^53)，直接返回数字会在前端丢精度，
 * 导致「详情/审批」等携带 ID 回传时命中错误记录。统一转字符串可彻底规避。
 */
@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer longToStringCustomizer() {
        return builder -> {
            SimpleModule module = new SimpleModule();
            module.addSerializer(Long.class, ToStringSerializer.instance);
            module.addSerializer(Long.TYPE, ToStringSerializer.instance);
            module.addSerializer(java.math.BigInteger.class, ToStringSerializer.instance);
            // 使用 modulesToInstall 追加, 避免覆盖 Spring Boot 默认注册的 JavaTimeModule 等
            builder.modulesToInstall(module);
        };
    }
}
