import { defineConfig, devices } from '@playwright/test'

/**
 * 厂智访客 · 租户端 E2E（Playwright）
 * 前置：后端在 8080 运行（docker compose 或本地）。webServer 自动拉起 pc-tenant dev(5173，代理 /api→8080)。
 */
export default defineConfig({
  testDir: './tests',
  timeout: 30_000,
  expect: { timeout: 8_000 },
  fullyParallel: false,
  workers: 1,
  reporter: [['list']],
  use: {
    baseURL: 'http://localhost:5173',
    trace: 'on-first-retry',
    headless: true
  },
  projects: [{ name: 'chromium', use: { ...devices['Desktop Chrome'] } }],
  webServer: {
    command: 'pnpm --filter pc-tenant dev',
    cwd: '../../',
    url: 'http://localhost:5173',
    reuseExistingServer: true,
    timeout: 120_000
  }
})
