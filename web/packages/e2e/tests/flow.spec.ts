import { test, expect, request } from '@playwright/test'

/**
 * 端到端：通过 API 播种一个租户（运营开通），再走 UI 租户登录 → 工作台 → 入场登记 → 在场访客。
 */
const API = 'http://localhost:8080'
const mobile = '138' + String(Date.now()).slice(-8)
const domain = 'e2e' + String(Date.now()).slice(-5)
const password = 'e2e88888'

test.beforeAll(async () => {
  const ctx = await request.newContext({ baseURL: API })
  const login = await (await ctx.post('/api/ops/auth/login', { data: { username: 'admin', password: 'admin123' } })).json()
  const token = login.data.accessToken
  const auth = { Authorization: `Bearer ${token}` }
  await ctx.post('/api/public/apply', {
    data: { companyName: 'E2E测试厂', industry: '钢铁', contactName: '测试', contactMobile: mobile, password, planCode: 'STEEL', smsCode: '1' }
  })
  const apps = await (await ctx.get('/api/ops/applications?status=1', { headers: auth })).json()
  const mine = (apps.data.list || []).find((a: any) => a.contactMobile === mobile)
  expect(mine, '找不到刚提交的入驻申请').toBeTruthy()
  const approve = await (await ctx.post(`/api/ops/applications/${mine.id}/approve`, { headers: auth, data: { domain } })).json()
  expect(approve.code, `开通失败: ${approve.message}`).toBe(0)
  await ctx.dispose()
})

test('租户登录并进入工作台与在场访客', async ({ page }) => {
  await page.goto('/#/login')
  // 默认即“企业租户”模式
  await page.getByPlaceholder('企业专属域名，如 desheng').fill(domain)
  await page.getByPlaceholder('账号 / 手机号').fill(mobile)
  await page.getByPlaceholder('密码').fill(password)
  await page.getByRole('button', { name: '登 录' }).click()

  await expect(page.getByText('今日到访')).toBeVisible()
  await expect(page.getByText('当前在场')).toBeVisible()

  // 工作台快捷入口进入“在场访客”
  await page.getByRole('button', { name: '在场访客' }).click()
  await expect(page).toHaveURL(/onsite/)
})
