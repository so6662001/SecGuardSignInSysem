# E2E（Playwright）· 租户端

覆盖登录页渲染、错误登录提示，以及「运营开通租户 → 租户登录 → 工作台 → 在场访客」端到端主线（通过 API 播种租户后走真实 UI）。

## 前置
- 后端在 `http://localhost:8080` 运行（`server/` 里 `java -jar` 或根 `docker compose up`），数据库/Redis 就绪。
- 首次执行安装浏览器：`pnpm --filter e2e install-browser`（或 `pnpm --filter e2e exec playwright install --with-deps chromium`）。

## 运行
```bash
pnpm --filter e2e test
```
`playwright.config.ts` 的 `webServer` 会自动拉起 `pc-tenant` dev(5173，`/api` 代理到 8080)；已在运行则复用。

## 用例
- `tests/login.spec.ts`：登录页品牌/表单渲染；错误账号密码提示失败。
- `tests/flow.spec.ts`：`beforeAll` 用 API 以运营身份开通一个随机租户，再走 UI 登录并断言工作台指标与「在场访客」跳转。
