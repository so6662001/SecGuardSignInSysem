# 厂智访客 · 前端镜像（构建三端并由 Nginx 提供，含 /api 反代后端）
FROM node:22-alpine AS build
WORKDIR /build
RUN npm i -g pnpm@10
COPY pnpm-workspace.yaml package.json ./
COPY apps/pc-tenant/package.json apps/pc-tenant/
COPY apps/pc-ops/package.json apps/pc-ops/
COPY apps/mobile/package.json apps/mobile/
RUN pnpm install --frozen-lockfile || pnpm install
COPY . .
RUN pnpm --filter pc-tenant build \
 && pnpm --filter pc-ops build \
 && pnpm --filter mobile build

FROM nginx:alpine
# 三端产物分别置于不同路径
COPY --from=build /build/apps/pc-tenant/dist /usr/share/nginx/html/tenant
COPY --from=build /build/apps/pc-ops/dist /usr/share/nginx/html/ops
COPY --from=build /build/apps/mobile/dist /usr/share/nginx/html/m
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
