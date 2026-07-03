import { test, expect } from '@playwright/test'

test.describe('登录页', () => {
  test('渲染品牌与登录表单', async ({ page }) => {
    await page.goto('/#/login')
    await expect(page.getByText('厂智访客').first()).toBeVisible()
    await expect(page.getByText('欢迎回来 👋')).toBeVisible()
    await expect(page.getByRole('button', { name: '登 录' })).toBeVisible()
  })

  test('错误账号密码提示失败', async ({ page }) => {
    await page.goto('/#/login')
    // 切到平台运营，避免租户登录需要域名
    await page.getByText('平台运营').click()
    await page.getByPlaceholder('账号 / 手机号').fill('admin')
    await page.getByPlaceholder('密码').fill('wrong-password')
    await page.getByRole('button', { name: '登 录' }).click()
    await expect(page.getByText('账号或密码错误')).toBeVisible()
  })
})
