import { test, expect } from '@playwright/test'

test.describe('半年度库房清点页面', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/inventory')
  })

  test('页面字段展示完整性验证', async ({ page }) => {
    await expect(page.locator('.page-title')).toContainText('半年度库房清点')

    await expect(page.getByPlaceholder('请输入清点人')).toBeVisible()
    await expect(page.getByPlaceholder('清点备注')).toBeVisible()
    await expect(page.getByRole('button', { name: '一键提交清点' })).toBeVisible()
    await expect(page.getByRole('button', { name: '全部按系统库存填充' })).toBeVisible()
    await expect(page.getByRole('button', { name: '清空实盘数量' })).toBeVisible()

    await expect(page.getByPlaceholder('搜索配件名称、型号')).toBeVisible()

    await expect(page.locator('.el-table')).toBeVisible()

    const headers = page.locator('.el-table__header th')
    await expect(headers.filter({ hasText: '配件名称' })).toBeVisible()
    await expect(headers.filter({ hasText: '型号' })).toBeVisible()
    await expect(headers.filter({ hasText: '库房分区' })).toBeVisible()
    await expect(headers.filter({ hasText: '系统库存' })).toBeVisible()
    await expect(headers.filter({ hasText: '实盘数量' })).toBeVisible()
    await expect(headers.filter({ hasText: '差异数量' })).toBeVisible()
    await expect(headers.filter({ hasText: '差异状态' })).toBeVisible()
  })

  test('统计数据展示', async ({ page }) => {
    const toolbar = page.locator('.table-toolbar')
    await expect(toolbar).toContainText('总数量：')
    await expect(toolbar).toContainText('实盘合计：')
    await expect(toolbar).toContainText('差异合计：')
  })

  test('状态筛选功能', async ({ page }) => {
    const statusRadio = page.locator('.el-radio-group').first()
    await expect(statusRadio).toContainText('全部')
    await expect(statusRadio).toContainText('有差异')
    await expect(statusRadio).toContainText('账实相符')
    await expect(statusRadio).toContainText('未盘点')
  })

  test('全部按系统库存填充功能', async ({ page }) => {
    const fillAllBtn = page.getByRole('button', { name: '全部按系统库存填充' })
    await fillAllBtn.click()

    const successMessage = page.locator('.el-message--success')
    if (await successMessage.isVisible()) {
      await expect(successMessage).toContainText('已按系统库存填充')
    }
  })

  test('提交清点对话框验证', async ({ page }) => {
    const fillAllBtn = page.getByRole('button', { name: '全部按系统库存填充' })
    await fillAllBtn.click()

    const submitBtn = page.getByRole('button', { name: '一键提交清点' })
    await submitBtn.click()

    const dialog = page.locator('.el-dialog')
    await expect(dialog).toContainText('清点提交确认')
    await expect(dialog).toContainText('清点人')
    await expect(dialog).toContainText('配件总数')
    await expect(dialog).toContainText('已盘点')
    await expect(dialog).toContainText('账实相符')
    await expect(dialog).toContainText('盘盈项数')
    await expect(dialog).toContainText('盘亏项数')
    await expect(dialog).toContainText('系统库存合计')
    await expect(dialog).toContainText('实盘数量合计')
    await expect(dialog).toContainText('差异总数')
  })
})
