# 厂智访客 · 前端（Vue 3 Monorepo）

pnpm workspace。对应开发提示词 `docs/03` 的 F 系列。

## 结构
- `apps/pc-tenant`：**租户端 PC**（已落地：登录、动态菜单布局、工作台看板、入场登记、在场访客、进出记录/审核）
- `apps/pc-ops`：运营平台（规划中）
- `apps/mobile`：移动端（规划中）

## 技术栈
Vue 3 + Vite 5 + TypeScript + Pinia + Vue Router + Element Plus + Axios + ECharts。

## 运行
```bash
pnpm install
pnpm dev:tenant      # 启动租户端 http://localhost:5173 （已配置 /api 代理到后端 8080）
pnpm build:tenant    # 构建
```

## 已验证
- `pnpm build:tenant` 构建通过。
- Vite 代理 `/api` → 后端 `:8080`：健康检查、租户登录（返回 TENANT_ADMIN）均正常。
- 登录 → 拉取动态菜单 → 布局侧边栏渲染；工作台看板读取 `/tenant/dashboard/*`；入场登记/在场/离场/审核 对接后端接口。

## 与后端联调
后端见 `server/`（Spring Boot 多模块）。先 `docker compose up -d` 起中间件、启动后端，再 `pnpm dev:tenant`。
默认演示账号：运营 `admin/admin123`；租户 域名 `desheng` + 手机号 + 申请时设置的密码。
