# 厂智访客 · 后端（Spring Boot 3 多模块）

对应开发提示词 `docs/02` 的 **B0 里程碑**：多模块脚手架 + 公共基座（统一响应、多租户、JWT 安全、异常处理、Flyway、OpenAPI）。

## 技术栈
Java 17（兼容 21）· Spring Boot 3.2 · Spring Security + JWT · MyBatis-Plus · MySQL 8 · Redis · Flyway · Knife4j/OpenAPI3 · Maven 多模块。

## 模块
| 模块 | 说明 |
| --- | --- |
| `fv-parent` | 父工程，统一依赖与版本管理 |
| `fv-common` | 公共基座：`R`/`PageResult`、`BizException`/全局异常、`TenantContext` 多租户上下文、MyBatis-Plus（租户行级隔离 + 分页 + 字段自动填充）、JWT 工具与安全配置、OpenAPI 配置 |
| `fv-bootstrap` | 可运行入口：启动类、健康检查、登录示例、`application.yml`、Flyway 迁移脚本（`resources/db/migration`） |

> 后续里程碑将新增 `fv-system / fv-visitor / fv-steel / fv-notify / fv-device / fv-billing / fv-ops` 等业务模块（见 `docs/02`）。

## 数据库迁移
Flyway 脚本位于 `fv-bootstrap/src/main/resources/db/migration/`（V1~V6，共 39 张表 + 种子数据），应用启动时自动执行。

## 本地运行
```bash
# 1) 启动依赖中间件
docker compose up -d

# 2) 构建
mvn clean package -DskipTests

# 3) 运行
java -jar fv-bootstrap/target/fv-server.jar
# 或：mvn -pl fv-bootstrap spring-boot:run
```

启动后：
- 健康检查：`GET http://localhost:8080/api/public/health`
- 接口文档（Knife4j）：`http://localhost:8080/doc.html`
- 登录示例：`POST http://localhost:8080/api/ops/auth/login`，body `{"username":"admin","password":"admin123"}`，返回 JWT。

## 环境变量（可选覆盖）
`FV_PORT` `FV_DB_URL` `FV_DB_USER` `FV_DB_PASSWORD` `FV_REDIS_HOST` `FV_REDIS_PORT` `FV_JWT_SECRET` `FV_ADMIN_USER` `FV_ADMIN_PWD`。

## 仅编译校验（无需数据库）
```bash
mvn clean package -DskipTests   # 编译 + 打包
mvn test                         # 运行单元测试（JwtUtilTest，不依赖 DB）
```
