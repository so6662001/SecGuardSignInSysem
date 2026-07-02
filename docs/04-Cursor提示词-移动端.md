# Cursor 开发提示词 · 移动端（uni-app / Vue3，编译到 H5 + 微信小程序 + App）

> 使用方法同前。先读 `.cursor/rules/project.mdc`、`docs/01-需求与架构规格.md`、`prototype/mobile.html`、`prototype/approve.html`、`prototype/invite.html`（移动侧交互与手机模型 UI）。
> 通用验收：还原原型手机页 UI；接 `/api/mobile` 与 `/api/public`；H5 免登录审批链路可用；小程序订阅消息/微信登录打通（Mock 可切换）；加载/错误/空态齐全。

---

## M0 · 移动端脚手架

```
在 web/apps/mobile 用 uni-app + Vue3 + TS + Pinia + wot-design-uni(或 uni-ui) 搭建，一套代码编译到 H5、微信小程序、App。
1. 请求封装(uni.request)：token、租户/门岗上下文、统一解包、错误提示、401 处理；区分需登录(/api/mobile)与免登录(/api/public)两类实例。
2. 登录：微信小程序 code2session / 公众号网页授权 / 手机号短信登录（适配器化，Mock 可跳过）。免登录页面（访客自助、短信审批）不需登录态。
3. 路由/分包、全局主题沿用 prototype 视觉（主色 #2f6bed，卡片扁平化）。
4. 订阅消息/模板消息授权封装（小程序 requestSubscribeMessage）。
交付：可运行的 H5 与小程序（开发者工具）骨架 + 登录跑通。
```

## M1 · 访客扫码自助登记（免登录 H5 / 小程序）

```
还原 prototype/mobile.html 左侧「访客端·扫码自助登记」：
- 扫门岗二维码进入（携带 gateCode）→ GET /api/public/gate/{gateCode}/template 拉取该门岗登记模板 schema，动态渲染表单（姓名/手机/单位/来访事由 chip/被访人/随行人数等；若模板启用携带设备/承诺书等模块则一并渲染）。
- 信息授权勾选《访客须知与信息授权》。
- 提交 POST /api/public/visit/self-register → 生成待审记录并触发通知被访人；成功页提示「请在门岗等候放行」，展示当前审批状态（可轮询/长连接更新为已通过）。
- 手机号验证码校验（按规则开关）。
```

## M2 · 被访人 / 员工移动端（需登录）

```
还原 prototype/mobile.html 右侧「管理端·实时通知与审核」：
- 我的待办：新的到访请求列表（访客信息卡：姓名/单位/事由/时间/电话/随行/门岗），同意接待/拒绝，可限定访客有效时段(仅本次/今日/本周)。调 POST /api/mobile/approvals/{id}/decision。
- 顶部统计：待我确认/今日到访我的/在场。
- 今日来访我的访客列表（在场/已离场状态）。
- 消息通知中心（到访、审批结果、滞留提醒等），小程序申请订阅消息授权。
- 员工发起预约邀请（invite 移动侧）：填写受邀访客与时段→生成邀请码/二维码→分享（微信/短信/复制）。调 POST /api/mobile/invitations。
```

## M3 · 被访人审批 · 多场景（关键：无论是否接入系统都能秒批）

```
还原 prototype/approve.html 三个场景：
① 未接入系统——短信免登录审批：短信内短链打开 H5 页 GET /api/public/approve/{token} 展示访客信息（姓名/单位/事由/拜访对象/随行/电话/门岗/申请时间），可选有效时段，点「同意接待/拒绝」→ POST /api/public/approve/{token}/decision。token 一次性、带时效、失效友好提示。无需登录/安装。
② 已接入系统——推送秒批：小程序订阅消息/公众号模板/企微卡片点击进入审批详情，一键同意/拒绝；通知栏卡片式操作。
③ 超时升级：展示审批已升级/由门卫代批的状态说明。
审批结果实时同步门卫端（后端 WebSocket），并给访客更新放行状态。
```

## M4 · 移动端联调与打包

```
联调全部移动端接口，处理 token 失效、网络异常、空态；小程序端处理订阅消息授权与合法域名配置；H5 端处理短链直达与微信内打开。输出 H5 构建产物与小程序上传说明。对照 prototype/mobile.html、approve.html 逐项核对无缺失。
```
