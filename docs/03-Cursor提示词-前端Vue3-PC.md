# Cursor 开发提示词 · 前端 PC（Vue 3 + Vite + TS + Element Plus）

> 使用方法同前。每个提示词先读 `.cursor/rules/project.mdc`、`docs/01-需求与架构规格.md`、对应 `prototype/*.html`（像素级还原扁平化 UI 与交互）与后端 OpenAPI。
> 通用验收：与原型 UI/字段/交互一致；接口联调成功；含加载/错误/空态；按钮级权限控制；响应式；TypeScript 无 any 滥用。

---

## F0 · 前端 Monorepo 与公共基座

```
阅读 .cursor/rules/project.mdc、docs/01-需求与架构规格.md 与 prototype/assets/style.css（沿用其扁平化设计变量与视觉）。
创建 pnpm workspace 前端仓库 `web/`：apps/pc-tenant（租户端）、apps/pc-ops（运营平台）、apps/mobile（占位，见移动端文档）、packages/shared（通用）。
公共基座（packages/shared 与各 app 复用）：
1. Vite + Vue3 + TS + Pinia + Vue Router + Element Plus + Axios + ECharts + UnoCSS。
2. 设计令牌：把 prototype/assets/style.css 的配色/圆角/阴影/组件风格转为 UnoCSS 预设 + 全局 SCSS 变量，保证与原型一致的扁平化观感（主色 #2f6bed、成功 #22b07d、警告 #f5a623、危险 #ef5b5b、运营平台紫 #7c56e0）。
3. Axios 封装：baseURL、请求头带 accessToken 与租户标识、响应拦截统一解包 R<T>、401 刷新令牌/跳登录、错误 toast、下载封装。
4. Pinia：user（登录态/权限点/菜单）、app（主题/多厂区当前上下文）。
5. 路由：动态路由由后端菜单生成；权限指令 v-perm（按钮级）；布局组件 Layout（侧边栏+顶栏，还原 prototype 侧边栏/topbar）。
6. 通用组件：StatCard、DataTable(筛选/分页/排序/导出)、SearchBar、Badge/状态标签、Chip 选择块、Switch、Stepper、Toast、Modal、Empty/Loading/Error 态、PersonCell、Timeline、Charts(Bar/Ring/Line)、QRCode、PhonePreview（field-config 预览用）。
7. WebSocket/SSE 客户端封装（实时动态、排队叫号、审批推送、地磅读数）。
交付：两个 app 可启动、登录页跑通、布局与组件库就绪、与后端联调通。
```

## F1 · 租户端 · 登录与工作台框架

```
apps/pc-tenant。还原 prototype/login.html：门卫/管理员双身份切换、账号/密码/记住登录/找回密码、专属域名登录、其他登录方式入口。登录成功拉取 profile+菜单+权限，动态生成路由与侧边栏（还原 prototype 侧边栏分组：工作台/管理，含订阅信息卡）。顶栏含搜索、通知铃铛(未读红点)、头像、以及多租户/多厂区企业空间标识（tenant-switch）。接后端 /api/tenant/auth 与 me/menus。
```

## F2 · 租户端 · 门卫工作台与登记（核心）

```
apps/pc-tenant。还原并实现：
1. checkin.html 入场登记：顶部实时时钟+今日统计(今日到访/在场/待审)、登记表单（姓名/手机/单位/随行/来访事由 chip/被访人/车牌/证件类型 chip/访客牌自动分配）、手动/扫码/证件识别三 tab（证件识别调 OCR）、模板切换下拉（普通/承包商/钢铁厂车辆→跳 steel）、右侧「待被访人确认」列表与「最近登记」、预约邀请入口。提交调 POST /visits，成功 toast 并通知被访人。
2. checkin-secure.html 承包商/高安全模板：基础信息 + 携带设备登记表（可增行：名称/型号/序列号/数量/拍照存档/出场核对）+ 危险品申报 + 安全承诺书电子签署（调电子签）+ 右侧登记确认摘要。字段由后端模板 schema 动态渲染，保证与 field-config 配置联动。
按登记规则做前端校验（必填、黑名单命中提示）。
```

## F3 · 租户端 · 在场访客与进出记录台账

```
apps/pc-tenant。
1. onsite.html 在场访客：统计卡、超时滞留提醒条、在场名单表（访客/单位/事由/被访人/访客牌/入场时间/在场时长实时/状态/离场操作）、tab(全部/正常/超时滞留)、导出名单、分页。离场调 checkout 并回收访客牌。
2. records.html 进出记录/审核台账：筛选栏(日期/事由/状态/门岗)、汇总小卡、明细表、审核通过/驳回、详情、导出 Excel、打印台账、tab(全部/待审/已通过/异常)、分页。
实时：新登记/离场通过 WebSocket 刷新在场与统计。
```

## F4 · 租户端 · 配置类页面（登记项/规则/门岗/审批通知）

```
apps/pc-tenant。
1. field-config.html 登记项配置/模板设计器：模板 tab 与新建、登记模块开关（基础必备/证件/携带设备/危险品/承诺书/健康/车辆）、自定义字段面板（9 种类型点击添加、已添加字段列表可启停）、右侧 PhonePreview 实时预览访客端表单、保存并发布。与后端 templates/fields 接口联动。
2. rules.html 登记规则：锚点分区（信息填写/审批放行/在场与时长/访客牌/黑名单与限制/数据与隐私）；各项开关/下拉/输入；黑名单表增删；保存。接 /api/tenant/rules 与 blacklist。
3. gate-config.html 门岗管理：门岗卡片网格（类型/值守/今日流量/绑定设备 chips/自助登记二维码/下载打印/启用停用/编辑）、新增门岗卡；设备绑定选择 sys_device。
4. approval.html 审批与通知：通知渠道优先级列表(可拖拽排序+开关)、响应时限与超时升级层级开关、免审通道(邀请码→跳 invite/白名单)、审批流转可视化时间线、待审批实时列表。接 notify-config。
```

## F5 · 租户端 · 预约邀请、数据看板、多厂区、系统设置

```
apps/pc-tenant。
1. invite.html 预约到访邀请：发起邀请表单、生成邀请函（二维码+邀请码+分享：复制链接/短信/微信）、邀请记录表(tab/重发/取消/再次邀请)。接 invitations。
2. dashboard.html 数据看板：统计卡、到访趋势(Bar)、事由分布(Ring)、高峰时段(进度条)、来访单位 TOP、实时进出动态(LIVE 时间线，WebSocket)、日/周/月切换、集团总览入口。
3. multi-site.html 集团总览：厂区切换器、集团汇总指标、各厂区对比表(可下钻单厂区看板)、厂区到访占比 Ring、集团实时动态。仅多厂区租户显示。
4. settings.html 系统设置：左菜单(人员与权限/登记规则→rules/登记项配置→field-config/审批与通知→approval/门岗管理→gate-config/订阅与账单)；人员与权限(成员表增删/角色/门岗归属/启停/重置密码/新增成员)；登记规则快捷开关；订阅与账单卡(查看套餐→pricing/升级/续费/账单明细)。
```

## F6 · 租户端 · 钢铁厂专属版页面

```
apps/pc-tenant（能力开关：订阅含钢铁模块才显示这些菜单/路由，侧边栏品牌标注「钢铁厂专属版」）。
1. steel-checkin.html 提货出厂：排队叫号统计卡、登记表单（车牌识别→带出承运商/司机；关联销售订单号→POST 交易平台核验带出客户/货物/应提/剩余可提/结算；计划提货量/仓库垛位/安全合规 chip）、磅单闭环三格(皮/毛/净)、右侧在厂车辆排队叫号(WebSocket)与车辆闭环时间线、确认入厂排队、放行。
2. steel-inbound.html 送货入厂：与提货页 tab 互切；关联采购单带出供应商/物料/剩余可收/质检要求；质检与扣杂率；磅单四格(毛/皮/净/结算净重)；排队卸货。
3. weighbridge.html 磅房过磅工作站：地磅实时读数大屏(订阅地磅设备 WebSocket)、车头/车厢抓拍存证、过磅操作(读磅/记录皮重毛重)、防作弊校验条、待过磅队列、今日磅单流水(含复磅差异异常)。
4. carrier.html 承运商与车辆管理：统计卡、数据引流联动条(识别高频未入驻承运商→一键邀请入驻交易平台)、承运商档案表(tab/车辆司机/本月车次/准运货物/交易平台入驻状态/白名单/邀请入驻/车辆档案)、分页。
```

## F7 · 运营平台（apps/pc-ops，紫色系区分）

```
apps/pc-ops。还原并实现（运营侧全部功能）：
1. platform.html 运营概览：指标卡(付费租户/试用/MRR/待审申请)、待审核开通申请卡(通过并开通/拒绝→详情跳 application-detail)、近 6 月收入趋势、到期预警与一键催续、租户企业列表(状态 tab/续费/停用/详情→tenant-detail/进入租户空间)。
2. application-detail.html 开通申请详情：申请头与操作(补充材料/拒绝/审核通过并开通→成功后跳 tenant-detail)、企业信息(工商核验)、管理员/联系人、证照材料预览、申请套餐、审核操作(分配专属域名/审核备注)、处理进度时间线。
3. tenant-detail.html 租户详情：头部(状态/套餐/月费/进入租户空间/停用)、tab(概览:企业信息+管理员+用量卡；订阅与账单:当前订阅+手动续费/调整套餐+账单表；用量统计:登记量 Bar；操作日志表)。
4. conversion.html 引流转化看板：核心指标、转化漏斗、转化去向(交易平台/管理软件/MES 进度条)、钢铁专属版拉动对比、高潜转化线索表(意向分/派发跟进)。
5. 侧边栏含运营概览/开通申请(红点)/租户管理/引流转化/订阅与账单/平台设置。
接后端 /api/ops/**。
```

## F8 · 营销站点与商业化页面（可用 pc-ops 或独立站点）

```
还原 prototype 的对外页面（可作为官网/营销站，SSR 或纯静态皆可，但功能要真）：
1. index.html 产品落地页：Hero、功能特色、使用流程、原型/功能导航、定价入口、CTA→signup。
2. pricing.html 套餐与增值商城：四档套餐(免费/标准¥60/钢铁专属¥599/集团)、增值模块、硬件购买/租赁；CTA→quote 配置报价、→signup 申请开通。
3. quote.html 配置报价单：选版本/厂区数步进/增值模块勾选/硬件购买或租赁与数量→右侧实时算价(月度小计/一次性/首年预估，集团=面议)→生成正式报价单(报价单号/客户信息可编辑/明细表/合计/说明/公章占位)→打印(@media print)/下载 PDF/发送。算价与生成逻辑与后端 /api/ops/quotes 对齐或复用。
4. signup.html/signup-done.html 企业在线申请开通：四步(企业信息/管理员账号/套餐/完成)、手机验证码、协议勾选、提交调 /api/public/apply；成功页展示专属域名/数据隔离/下一步引导。
```
