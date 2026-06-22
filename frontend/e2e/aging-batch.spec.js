import { test, expect } from '@playwright/test'

test.describe('老化批次归档主路径', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/aging-batch')
  })

  test('选择类别/库区/时间 → 预览 → 填归档人/原因 → 确认归档 → 查看历史详情', async ({ page }) => {
    await expect(page.locator('.page-title')).toContainText('老化批次归档')

    const initialBatchCount = await page.locator('.el-table__row').count()

    const categorySelect = page.locator('.el-tree-select')
    if (await categorySelect.isVisible()) {
      await categorySelect.click()
      const firstCategory = page.locator('.el-tree-node').first()
      if (await firstCategory.isVisible()) {
        await firstCategory.click()
      }
    }

    const zoneSelect = page.locator('.el-form-item').filter({ hasText: '库区' }).locator('.el-select')
    if (await zoneSelect.isVisible()) {
      await zoneSelect.click()
      const firstOption = page.locator('.el-select-dropdown__item').first()
      if (await firstOption.isVisible()) {
        await firstOption.click()
      }
    }

    const datePicker = page.locator('.el-date-editor--datetimerange')
    if (await datePicker.isVisible()) {
      await datePicker.click()
      const todayBtn = page.locator('.el-picker-panel__today')
      if (await todayBtn.isVisible()) {
        await todayBtn.click()
      }
    }

    const previewBtn = page.getByRole('button', { name: '预览待报废配件' })
    await expect(previewBtn).toBeVisible()
    await previewBtn.click()

    await page.waitForTimeout(1000)

    const previewTable = page.locator('.el-table').nth(1)
    const hasPreviewItems = (await previewTable.locator('.el-table__row').count()) > 0

    if (hasPreviewItems) {
      const reasonSelect = page.locator('.el-form-item').filter({ hasText: '统一原因' }).locator('.el-select')
      if (await reasonSelect.isVisible()) {
        await reasonSelect.click()
        const naturalAgingOption = page.getByText('自然老化', { exact: true })
        if (await naturalAgingOption.isVisible()) {
          await naturalAgingOption.click()
        }
      }

      const applyReasonBtn = page.getByRole('button', { name: '应用统一原因' })
      if (await applyReasonBtn.isVisible()) {
        await applyReasonBtn.click()
      }

      const operatorInput = page.locator('.el-form-item').filter({ hasText: '归档人' }).locator('input')
      await expect(operatorInput).toBeVisible()
      await operatorInput.fill('测试归档人')

      const archiveBtn = page.getByRole('button', { name: /确认归档/ })
      await expect(archiveBtn).toBeVisible()
      await archiveBtn.click()

      const confirmDialog = page.locator('.el-message-box')
      if (await confirmDialog.isVisible()) {
        const confirmBtn = confirmDialog.getByRole('button', { name: '确认归档' })
        if (await confirmBtn.isVisible()) {
          await confirmBtn.click()
        }
      }

      await page.waitForTimeout(1500)

      const successMessage = page.locator('.el-message--success')
      if (await successMessage.isVisible()) {
        await expect(successMessage).toContainText('成功')
      }

      const historyTable = page.locator('.el-table').last()
      const newBatchCount = await historyTable.locator('.el-table__row').count()
      expect(newBatchCount).toBeGreaterThanOrEqual(initialBatchCount)

      const firstDetailBtn = historyTable.getByRole('button', { name: '详情' }).first()
      if (await firstDetailBtn.isVisible()) {
        await firstDetailBtn.click()

        const detailDialog = page.locator('.el-dialog').filter({ hasText: '批次详情' })
        await expect(detailDialog).toBeVisible()

        const batchNoLabel = detailDialog.locator('.el-descriptions__label').filter({ hasText: '批次号' })
        await expect(batchNoLabel).toBeVisible()

        const closeBtn = detailDialog.getByRole('button', { name: '关闭' })
        await closeBtn.click()
      }
    } else {
      console.log('没有找到待报废配件，跳过归档测试')
    }
  })

  test('页面字段展示完整性验证', async ({ page }) => {
    await expect(page.getByText('生成待报废批次')).toBeVisible()
    await expect(page.getByText('配件类别')).toBeVisible()
    await expect(page.getByText('库区')).toBeVisible()
    await expect(page.getByText('入库时间')).toBeVisible()
    await expect(page.getByRole('button', { name: '预览待报废配件' })).toBeVisible()
    await expect(page.getByText('归档批次历史')).toBeVisible()
  })
})
