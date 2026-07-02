# Cursor 开发提示词 · 后端（Java / Spring Boot 3）

> 使用方法：按顺序把每个「提示词」整段粘贴给 Cursor（Agent 模式）。每个提示词开头都要求先读 `.cursor/rules/project.mdc`、`docs/01-需求与架构规格.md` 与相关 `prototype/*.html`。完成一个再进行下一个，前一步产物是后一步的基础。
> 通用验收：编译通过、Flyway 迁移可执行、OpenAPI 可见、含单元/集成测试、含 Mock 使无第三方也能跑通、遵守多租户与统一响应规范。

---

## B0 · 后端脚手架与公共基座

```
阅读 .cursor/rules/project.mdc 与 docs/01-需求与架构规格.md。
创建后端工程 `server/`，Maven 多模块（parent + 子模块：fv-common、fv-system、fv-visitor、fv-steel、fv-notify、fv-device、fv-billing、fv-ops、fv-tenant-api、fv-admin-api）。Java 17、Spring Boot 3.x。
在 fv-common 实现公共基座：
1. 统一响应 R<T>、PageResult<T>、业务异常 BizException、全局异常处理器（含参数校验错误聚合）。
2. 多租户：MyBatis-Plus 配置 + TenantLineInnerInterceptor（忽略平台域表清单可配置）；TenantContextHolder(ThreadLocal)；请求拦截器从 JWT 解析 tenantId/userId/roles 写入上下文，响应结束清理。
3. 安全：Spring Security + JWT（accessToken + refreshToken）、密码 BCrypt、鉴权入口按 /api/ops、/api/tenant、/api/mobile、/api/public、/api/device 分链路配置；设备端用设备密钥+签名过滤器；public 端加限流(Redis)。
4. 基础设施：Redis（RedisTemplate + 分布式锁 + 验证码/排队号工具）、RabbitMQ 配置（含延时队列/插件）、MinIO 客户端与文件上传下载封装、Knife4j/OpenAPI3、Flyway、MapStruct、雪花 ID、审计日志切面(@AuditLog)、操作日志表 sys_audit_log。
5. 公共实体基类 BaseEntity（含 tenant_id、公共字段、逻辑删）。
6. Docker Compose（mysql/redis/rabbitmq/minio）与 application-{dev,prod}.yml；本地一键启动脚本。
交付：可启动的空壳应用 + 健康检查 + Swagger 首页 + 首个 Flyway 基线脚本。写 README 说明启动方式。
```

## B1 · 系统域：用户 / 角色 / 权限 / 菜单 / 门岗 / 厂区 / 审计

```
在 fv-system 实现系统域（多租户，参考 docs 第 3.2、5 节 与 prototype/settings.html、gate-config.html、multi-site.html）。
1. 表与迁移：sys_user、sys_role、sys_permission、sys_user_role、sys_role_permission、sys_menu、sys_gate、sys_device(先建表)、sys_site、sys_audit_log。内置权限点与默认角色（企业超管/管理员/审核员/门卫/司磅员/被访人）与平台角色。
2. RBAC：登录鉴权、动态菜单/权限点接口 GET /api/tenant/me/menus；@PreAuthorize 或自定义权限校验注解到按钮级。
3. 成员与角色管理 CRUD、成员启停、重置密码、门岗/厂区归属分配（对应 settings.html 人员与权限）。
4. 门岗管理 CRUD：类型(人行/车行/人车混合)、值守门卫、启用停用、生成并返回门岗自助登记二维码(含 gateCode，存 MinIO)（对应 gate-config.html）。
5. 厂区/多厂区：sys_site CRUD、用户与门岗归属厂区、集团汇总接口 GET /api/tenant/sites/overview（对应 multi-site.html）。
6. 数据权限：按厂区/门岗过滤（门卫只看本岗）。
交付：完整接口 + 测试 + OpenAPI。
```

## B2 · 平台运营域：申请开通 / 租户 / 套餐订阅 / 账单 / 报价 / 引流转化

```
在 fv-ops 与 fv-billing 实现平台运营域（参考 docs 第 3.1、4.D、5 与 prototype: signup.html、signup-done.html、platform.html、application-detail.html、tenant-detail.html、conversion.html、pricing.html、quote.html）。
1. 表与迁移：sys_tenant、tenant_application、tenant_subscription、biz_plan、biz_addon、biz_hardware、biz_invoice、biz_quote、ops_lead、ops_conversion。
2. 企业在线申请开通：POST /api/public/apply（企业信息/管理员账号/套餐/厂区数/证照上传）+ 短信验证码；工商核验用适配器(Mock)；生成 tenant_application(待审)。
3. 开通审核：GET/POST /api/ops/applications...（列表/详情/通过并开通/驳回/要求补充材料）。通过时：创建 sys_tenant + 分配专属域名 + 初始化租户超管账号与默认角色/模板/规则 + 生成 subscription（14 天试用）+ 发送开通通知（账号与域名）。对应 application-detail.html 的处理进度。
4. 租户管理：列表(状态筛选)、详情(概览/订阅账单/用量/操作日志 tab)、手动续费、调整套餐、重置密码、停用、impersonate 进入租户空间（生成受限令牌）。对应 tenant-detail.html。
5. 运营概览：GET /api/ops/overview（租户数/试用/MRR/待审）、收入趋势、到期预警、一键催续（触发通知）。对应 platform.html。
6. 套餐/增值/硬件 CRUD；订阅与账单生成（定时任务：到期续费、试用转正、生成 invoice、续费提醒）。
7. 引流转化：ops_conversion 记录事件流水，GET /api/ops/funnel（漏斗）、/destination（转化去向）、/leads（高潜线索+意向分算法：依据行业、到访/送货频次、是否用专属版）、POST /leads/{id}/assign 派发。对应 conversion.html。
8. 报价单：POST /api/ops/quotes（明细/月度合计/一次性/首年预估计算）、GET /quotes/{id}、生成报价单号、send 发送。对应 quote.html 的算价逻辑（版本×厂区 + 增值模块 + 硬件购买一次性/租赁月费；集团版=面议）。
交付：完整接口 + 计费/续费定时任务 + 测试。
```

## B3 · 访客通用域：登记模板 / 字段 / 访客登记 / 审批 / 在场离场 / 记录台账 / 预约邀请 / 黑名单 / 访客牌 / 看板

```
在 fv-visitor 实现访客通用域（参考 docs 第 3.3、4.A、5 与 prototype: field-config.html、checkin.html、checkin-secure.html、onsite.html、records.html、rules.html、invite.html、dashboard.html、approval.html）。
1. 表与迁移：register_template、register_field、visitor、visit_record、visit_approval、invitation、blacklist、visitor_badge、tenant_rules（登记规则）。
2. 登记项配置/模板设计器：模板 CRUD、字段 CRUD（9 种字段类型、模块开关：基础/证件/携带设备/危险品/承诺书/健康/车辆/自定义）、排序、发布；提供「访客端渲染 schema」接口供移动端动态渲染。对应 field-config.html。
3. 登记规则：GET/PUT /api/tenant/rules（必填项、被访人确认后入场、门卫代批、滞留时限、当日自动离场时间、通行时段、访客牌自动分配/归还/号段、黑名单拦截、数据留存/脱敏/授权）。对应 rules.html。
4. 入场登记：POST /api/tenant/visits（校验必填/黑名单/规则；分配访客牌；写扩展字段值 JSON；携带设备明细；触发审批与通知）。支持登记方式：门卫手动/扫码自助/证件识别(OCR 适配器)。对应 checkin.html / checkin-secure.html（携带设备逐项、危险品、承诺书电子签调用电子签适配器）。
5. 审批：visit_approval 状态机（待响应/同意/拒绝/超时升级/门卫代批）；被访人确认后方可入场（按规则）；调用通知引擎(见 fv-notify)。对应 approval.html 流转。
6. 在场/离场：GET /visits/onsite（在场名单+在场时长实时计算）、POST /visits/{id}/checkout（回收访客牌）、滞留提醒定时任务、当日自动离场定时任务。对应 onsite.html。
7. 记录台账：GET /visits（多条件筛选/分页）、GET /visits/{id}、POST /visits/{id}/audit（通过/驳回）、导出 Excel（异步）、打印数据接口。对应 records.html。
8. 预约邀请：invitation CRUD、生成邀请码+二维码、发送（短信/微信/链接，调通知引擎）、到场核验免审（扫码/报码命中则跳过审批）、状态流转、重发/取消。对应 invite.html。
9. 黑名单：CRUD + 登记时命中拦截告警。
10. 数据看板：到访趋势、事由分布、高峰时段、来访单位 TOP、实时进出动态(WebSocket 推送)、日/周/月。对应 dashboard.html。
交付：完整接口 + 状态机 + 定时任务 + WebSocket 实时 + 测试。
```

## B4 · 通知 / 提醒引擎（多渠道 + 超时自动升级）

```
在 fv-notify 实现通知与提醒引擎（参考 docs 第 3.5、7 与 prototype: approval.html、approve.html、mobile.html）。
1. 表与迁移：notify_template、notify_task、notify_log、notify_channel_config。
2. 渠道适配器接口 NotifyChannel（send/回执），实现：SystemPush(WebSocket/小程序订阅)、Sms、Voice、Wechat/Wecom/Dingtalk。均含 Mock 实现，配置切换真实厂商。
3. 通知编排服务：按租户 notify_channel_config 的渠道优先级依次触达；模板渲染（占位变量）。
4. 超时自动升级引擎（关键）：用 RabbitMQ 延时队列实现——首次通知设响应时限 → 超时二次提醒(换渠道) → 升级备用审批人/部门负责人 → 门卫代批兜底；每步写 notify_log 与 visit_approval 流转轨迹；被访人一旦响应则取消后续延时任务。
5. 免登录审批短链：生成一次性、带时效、防篡改 token；GET /api/public/approve/{token} 返回访客信息，POST 决策；对应 approve.html 场景①。
6. 语音外呼：AI 播报「XX 到访，同意请按 1，拒绝请按 2」，接收按键回调更新审批（Mock 可模拟回调）。
7. 通知场景接入：到访待确认、审批结果、入/离场、滞留、自动离场、邀请、试用到期/续费、账单、设备离线、开通申请、磅单异常。
交付：编排+升级引擎+短链+全部场景接入+测试（含超时升级的时间线测试）。
```

## B5 · 钢铁厂专属域：承运商 / 车辆 / 车辆到厂 / 磅单 / 交易平台对接

```
在 fv-steel 实现钢铁专属域（参考 docs 第 3.4、4.B、5 与 prototype: steel-checkin.html、steel-inbound.html、weighbridge.html、carrier.html）。仅当租户订阅含钢铁模块时启用（能力开关）。
1. 表与迁移：carrier、vehicle、driver、vehicle_visit、weigh_record、trade_order_ref。
2. 承运商/车辆/司机 CRUD、白名单免审、受限/黑名单；识别高频未入驻承运商并 POST /carriers/{id}/invite-trade 邀请入驻交易平台运力池。对应 carrier.html。
3. 车辆到厂登记：POST /api/tenant/steel/vehicle-visits，方向=送货入厂/提货出厂；车牌识别带出车辆/承运商/司机；关联单据号调用交易平台适配器核验并带出（提货：客户/货物/应提/已提/剩余可提/结算状态；送货：供应商/物料/采购量/已收/剩余/质检要求）。写货物、料场/仓库/垛位、安全合规项、扣杂率。对应 steel-checkin.html / steel-inbound.html。
4. 排队叫号：生成排队号、状态机（预约/到厂/已登记/过磅/装卸中/复磅/放行），叫号与放行接口，实时看板 WebSocket 推送。
5. 磅单闭环：POST /weigh 记录皮重/毛重（提货：皮重进→装货→毛重复磅；送货：毛重进→卸货→皮重复磅）；净重=毛-皮，结算净重=净重×(1-扣杂率)；防作弊校验（车牌一致、读数稳定、与历史皮重偏差）；抓拍存证（摄像头适配器→MinIO）；异常（超载/复磅差异）标记；放行时净重与磅单回写交易平台单据（trade_order_ref 更新 + 交易平台适配器回写）。对应 weighbridge.html。
6. 磅房工作站接口：待过磅队列、地磅实时读数（订阅地磅设备事件）、今日磅单流水。
交付：完整接口 + 状态机 + 交易平台适配器(含 Mock 单据) + 磅单回写 + 测试。
```

## B6 · 设备接入域（车牌道闸 / 人脸 / 地磅 / 摄像头 / OCR / 电子签 / 平板）

```
在 fv-device 实现设备接入（参考 docs 第 3.2 设备表、第 6 节 与 prototype: gate-config.html、weighbridge.html、steel-*.html、checkin*.html）。全部「适配器接口 + Mock 实现 + 厂商实现骨架」，配置切换，无硬件用 Mock 跑通。
1. sys_device 设备档案：注册、绑定门岗、设备密钥、心跳、在线状态；心跳巡检定时任务 + 离线告警(接通知引擎)。
2. 设备端鉴权：/api/device/** 用设备密钥+签名。
3. 车牌识别道闸 LprAdapter：POST /api/device/lpr/event 接收识别事件→匹配 vehicle/待到厂单据→触发登记/放行；POST /api/device/gate/{id}/open 下发抬杆；黑名单车辆拦截。
4. 人脸一体机 FaceAdapter：下发访客/员工人脸库；POST /api/device/face/event 识别回调→核验放行；测温字段。
5. 地磅 WeighbridgeAdapter：POST /api/device/weighbridge/reading 接收实时读数（经串口服务器/边缘网关）；稳定值判定；供磅房工作站订阅（WebSocket）。
6. 抓拍摄像头 CameraAdapter：过磅/入场触发抓图存 MinIO，返回图片 URL 供存证。
7. OCR OcrAdapter：身份证识别返回结构化信息（checkin 证件识别用）。
8. 电子签 ESignAdapter：创建承诺书/保密协议签署、回调、存证下载（checkin-secure 用）。
9. 登记平板：说明其调用租户端/移动端 API，无需独立设备接口（或提供设备绑定态）。
交付：适配器接口 + Mock + 厂商实现骨架(海康/大华/阿里/腾讯/e签宝占位) + 设备管理接口 + 测试。
```

## B7 · 集成与自测：端到端主流程打通

```
串联并自测端到端主流程，补齐遗漏：
- 通用版：企业申请开通→运营审核开通→租户超管登录→配置门岗/模板/规则/成员→门卫入场登记→通知被访人→（多渠道+超时升级）审批→在场→离场→台账→看板。
- 预约邀请：员工发起邀请→访客收码→到场扫码免审入场。
- 钢铁版：车辆到厂(关联交易平台单据)→过磅→装卸→复磅→净重结算→放行回写交易平台；承运商邀请入驻运力池。
- 计费：试用→到期提醒→续费→账单；运营看板 MRR/转化漏斗数据产生。
用 Mock 适配器保证无硬件可跑通。补充 Postman/HTTP 用例集合与集成测试；修复所有断链逻辑。输出「功能对照清单」逐页核对 prototype/ 是否全部覆盖。
```
