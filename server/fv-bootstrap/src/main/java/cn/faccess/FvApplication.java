package cn.faccess;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 厂智访客后端启动入口。
 * 组件扫描默认覆盖 cn.faccess（含 fv-common 公共基座与各业务模块）。
 */
@SpringBootApplication
@EnableScheduling
@MapperScan("cn.faccess.**.mapper")
public class FvApplication {

    public static void main(String[] args) {
        SpringApplication.run(FvApplication.class, args);
    }
}
