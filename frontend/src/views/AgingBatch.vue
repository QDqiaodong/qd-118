<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-title">
        <el-icon><Box /></el-icon>
        <span class="ml-8">老化批次归档</span>
      </div>
    </div>

    <el-card class="mb-16" shadow="never" style="margin-bottom: 20px; border: 1px solid #ebeef5; border-radius: 8px">
      <template #header>
        <div style="display: flex; align-items: center; font-weight: 600">
          <el-icon><Search /></el-icon>
          <span class="ml-8">生成待报废批次</span>
        </div>
      </template>

      <el-form :model="queryForm" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-form-item label="配件类别">
              <el-tree-select
                v-model="queryForm.categoryId"
                :data="categoryTree"
                :props="{ label: 'name', value: 'id', children: 'children' }"
                check-strictly
                placeholder="请选择配件类别"
                clearable
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="库区">
              <el-select v-model="queryForm.warehouseZone" placeholder="请选择库区" clearable style="width: 100%">
                <el-option v-for="zone in warehouseZones" :key="zone" :label="zone" :value="zone" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="入库时间">
              <el-date-picker
                v-model="queryForm.inboundRange"
                type="datetimerange"
                range-separator="至"
                start-placeholder="开始时间"
                end-placeholder="结束时间"
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item style="margin-bottom: 0">
          <el-button type="primary" :icon="Search" :loading="previewLoading" @click="handleGeneratePreview">
            预览待报废配件
          </el-button>
          <el-button :icon="RefreshLeft" @click="resetQueryForm">
            重置
          </el-button>
          <span v-if="previewItems.length > 0" style="margin-left: 16px; color: #909399">
            共找到 <span style="color: #f56c6c; font-weight: 600">{{ previewItems.length }}</span> 项待报废配件，
            合计数量 <span style="color: #f56c6c; font-weight: 600">{{ totalPreviewQuantity }}</span>
          </span>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card v-if="previewItems.length > 0" class="mb-16" shadow="never" style="margin-bottom: 20px; border: 1px solid #ebeef5; border-radius: 8px">
      <template #header>
        <div style="display: flex; align-items: center; justify-content: space-between">
          <div style="display: flex; align-items: center; font-weight: 600">
            <el-icon><WarningFilled /></el-icon>
            <span class="ml-8">待报废配件清单</span>
          </div>
        </div>
      </template>

      <el-table :data="previewItems" border stripe style="width: 100%; margin-bottom: 16px">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="categoryName" label="分类" width="120" show-overflow-tooltip />
        <el-table-column prop="accessoryName" label="配件名称" min-width="160" show-overflow-tooltip />
        <el-table-column prop="accessoryModel" label="型号" width="120" show-overflow-tooltip />
        <el-table-column prop="warehouseZone" label="库区" width="100" align="center" />
        <el-table-column label="入库时间" width="170" align="center">
          <template #default="{ row }">
            {{ formatDateTime(row.inboundTime) }}
          </template>
        </el-table-column>
        <el-table-column label="当前库存" width="100" align="center">
          <template #default="{ row }">
            <el-tag type="info" size="small">{{ formatNumber(row.stockQuantity) }} {{ row.unit }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="报废数量" width="140" align="center">
          <template #default="{ row }">
            <el-input-number
              v-model="row.scrapQuantity"
              :min="1"
              :max="row.stockQuantity"
              controls-position="right"
              size="small"
              style="width: 100%"
            />
          </template>
        </el-table-column>
        <el-table-column label="报废原因" width="180" align="center">
          <template #default="{ row }">
            <el-select v-model="row.reason" placeholder="选择原因" style="width: 100%" size="small">
              <el-option-group label="老化">
                <el-option label="自然老化" value="自然老化" />
                <el-option label="过期失效" value="过期失效" />
              </el-option-group>
              <el-option-group label="破损">
                <el-option label="物理损坏" value="物理损坏" />
                <el-option label="腐蚀损坏" value="腐蚀损坏" />
              </el-option-group>
              <el-option-group label="其他">
                <el-option label="质量问题" value="质量问题" />
                <el-option label="技术淘汰" value="技术淘汰" />
              </el-option-group>
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="70" align="center">
          <template #default="{ $index }">
            <el-button type="danger" link size="small" :icon="Delete" @click="removePreviewItem($index)" />
          </template>
        </el-table-column>
      </el-table>

      <el-form :model="archiveForm" :rules="archiveFormRules" ref="archiveFormRef" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="统一原因" prop="reason">
              <el-select v-model="archiveForm.reason" placeholder="选择统一报废原因" style="width: 100%">
                <el-option-group label="老化">
                  <el-option label="自然老化" value="自然老化" />
                  <el-option label="过期失效" value="过期失效" />
                </el-option-group>
                <el-option-group label="破损">
                  <el-option label="物理损坏" value="物理损坏" />
                  <el-option label="腐蚀损坏" value="腐蚀损坏" />
                </el-option-group>
                <el-option-group label="其他">
                  <el-option label="质量问题" value="质量问题" />
                  <el-option label="技术淘汰" value="技术淘汰" />
                </el-option-group>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="归档人" prop="operator">
              <el-input v-model="archiveForm.operator" placeholder="请输入归档人姓名" maxlength="20" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="备注">
              <el-input v-model="archiveForm.remark" placeholder="请输入备注（可选）" maxlength="200" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item style="margin-bottom: 0">
          <el-button type="primary" :icon="Check" :loading="submitting" @click="handleApplyReason">
            应用统一原因
          </el-button>
          <el-button type="danger" :icon="Warning" :loading="submitting" @click="handleArchiveSubmit">
            确认归档报废 ({{ previewItems.length }} 项, {{ totalSelectedQuantity }} 件)
          </el-button>
          <el-button :icon="RefreshLeft" @click="clearPreview">
            清空清单
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" style="border: 1px solid #ebeef5; border-radius: 8px">
      <template #header>
        <div style="display: flex; align-items: center; font-weight: 600">
          <el-icon><List /></el-icon>
          <span class="ml-8">归档批次历史</span>
        </div>
      </template>

      <el-table :data="historyList" border stripe style="width: 100%" v-loading="historyLoading">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="batchNo" label="批次号" width="180" show-overflow-tooltip />
        <el-table-column prop="categoryName" label="配件类别" width="120" show-overflow-tooltip />
        <el-table-column prop="warehouseZone" label="库区" width="100" align="center" />
        <el-table-column label="入库时间范围" width="280" align="center">
          <template #default="{ row }">
            {{ formatDateTime(row.inboundStart) }} ~ {{ formatDateTime(row.inboundEnd) }}
          </template>
        </el-table-column>
        <el-table-column label="批次项数" width="90" align="center">
          <template #default="{ row }">
            {{ row.itemCount }}
          </template>
        </el-table-column>
        <el-table-column label="总数量" width="100" align="center">
          <template #default="{ row }">
            <span style="color: #f56c6c; font-weight: 600">{{ formatNumber(row.totalQuantity) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="报废原因" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small">{{ row.reason }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operator" label="归档人" width="90" align="center" />
        <el-table-column prop="archiveTime" label="归档时间" width="170" align="center">
          <template #default="{ row }">
            {{ formatDateTime(row.archiveTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleViewDetail(row)">
              <el-icon><View /></el-icon>
              <span class="ml-4">详情</span>
            </el-button>
            <el-button type="danger" link size="small" @click="handleDeleteBatch(row)">
              <el-icon><Delete /></el-icon>
              <span class="ml-4">删除</span>
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="detailDialogVisible" title="批次详情" width="80%" top="5vh">
      <div v-if="currentBatch" style="margin-bottom: 16px">
        <el-descriptions :column="3" border size="small">
          <el-descriptions-item label="批次号">{{ currentBatch.batchNo }}</el-descriptions-item>
          <el-descriptions-item label="配件类别">{{ currentBatch.categoryName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="库区">{{ currentBatch.warehouseZone || '-' }}</el-descriptions-item>
          <el-descriptions-item label="入库时间范围">
            {{ formatDateTime(currentBatch.inboundStart) }} ~ {{ formatDateTime(currentBatch.inboundEnd) }}
          </el-descriptions-item>
          <el-descriptions-item label="批次项数">{{ currentBatch.itemCount }}</el-descriptions-item>
          <el-descriptions-item label="总数量">{{ formatNumber(currentBatch.totalQuantity) }}</el-descriptions-item>
          <el-descriptions-item label="报废原因">{{ currentBatch.reason }}</el-descriptions-item>
          <el-descriptions-item label="归档人">{{ currentBatch.operator }}</el-descriptions-item>
          <el-descriptions-item label="归档时间">{{ formatDateTime(currentBatch.archiveTime) }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="3">{{ currentBatch.remark || '-' }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <el-table :data="currentBatchItems" border stripe style="width: 100%" v-loading="detailLoading">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="categoryName" label="分类" width="120" show-overflow-tooltip />
        <el-table-column prop="accessoryName" label="配件名称" min-width="160" show-overflow-tooltip />
        <el-table-column prop="accessoryModel" label="型号" width="120" show-overflow-tooltip />
        <el-table-column prop="warehouseZone" label="库区" width="100" align="center" />
        <el-table-column label="入库时间" width="170" align="center">
          <template #default="{ row }">
            {{ formatDateTime(row.inboundTime) }}
          </template>
        </el-table-column>
        <el-table-column label="归档时库存" width="110" align="center">
          <template #default="{ row }">
            {{ formatNumber(row.stockQuantity) }} {{ row.unit }}
          </template>
        </el-table-column>
        <el-table-column label="报废数量" width="110" align="center">
          <template #default="{ row }">
            <span style="color: #f56c6c; font-weight: 600">{{ formatNumber(row.scrapQuantity) }} {{ row.unit }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="原因" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small">{{ row.reason }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="120" show-overflow-tooltip />
      </el-table>

      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Box,
  Search,
  RefreshLeft,
  WarningFilled,
  Warning,
  Delete,
  Check,
  List,
  View
} from '@element-plus/icons-vue'
import { getCategoryTree } from '@/api/category'
import { getAccessoryList } from '@/api/accessory'
import {
  previewAgingBatch,
  createAgingBatch,
  deleteAgingBatch,
  getAgingBatchList,
  getAgingBatchItems
} from '@/api/agingBatch'

const previewLoading = ref(false)
const submitting = ref(false)
const historyLoading = ref(false)
const detailLoading = ref(false)
const archiveFormRef = ref(null)

const categoryTree = ref([])
const warehouseZones = ref([])
const previewItems = ref([])
const historyList = ref([])
const detailDialogVisible = ref(false)
const currentBatch = ref(null)
const currentBatchItems = ref([])

const queryForm = reactive({
  categoryId: null,
  warehouseZone: '',
  inboundRange: []
})

const archiveForm = reactive({
  reason: '',
  operator: '',
  remark: ''
})

const archiveFormRules = {
  operator: [
    { required: true, message: '请输入归档人姓名', trigger: 'blur' },
    { min: 1, max: 20, message: '姓名长度 1-20 个字符', trigger: 'blur' }
  ]
}

const totalPreviewQuantity = computed(() => {
  return previewItems.value.reduce((sum, item) => sum + (item.stockQuantity || 0), 0)
})

const totalSelectedQuantity = computed(() => {
  return previewItems.value.reduce((sum, item) => sum + (item.scrapQuantity || 0), 0)
})

const formatNumber = (num) => {
  if (num === null || num === undefined || isNaN(num)) return 0
  return Number(num).toLocaleString('zh-CN')
}

const formatDateTime = (dt) => {
  if (!dt) return '-'
  return dt
}

const loadCategoryTree = async () => {
  try {
    const data = await getCategoryTree()
    categoryTree.value = data || []
  } catch (error) {
    console.error('加载分类树失败:', error)
  }
}

const loadWarehouseZones = async () => {
  try {
    const data = await getAccessoryList({ pageNum: 1, pageSize: 9999 })
    const list = data.records || data.list || data || []
    const zones = [...new Set(list.map((a) => a.warehouseZone || a.zone).filter((z) => z))]
    warehouseZones.value = zones.sort()
  } catch (error) {
    console.error('加载库区列表失败:', error)
  }
}

const resetQueryForm = () => {
  queryForm.categoryId = null
  queryForm.warehouseZone = ''
  queryForm.inboundRange = []
}

const handleGeneratePreview = async () => {
  previewLoading.value = true
  try {
    const params = {
      categoryId: queryForm.categoryId || null,
      warehouseZone: queryForm.warehouseZone || null,
      inboundStart: queryForm.inboundRange?.[0] || null,
      inboundEnd: queryForm.inboundRange?.[1] || null
    }
    const data = await previewAgingBatch(params)
    previewItems.value = data || []
    if (previewItems.value.length === 0) {
      ElMessage.info('未找到符合条件的待报废配件')
    }
  } catch (error) {
    console.error('预览待报废配件失败:', error)
    ElMessage.error('预览待报废配件失败，请稍后重试')
  } finally {
    previewLoading.value = false
  }
}

const removePreviewItem = (index) => {
  previewItems.value.splice(index, 1)
}

const clearPreview = () => {
  previewItems.value = []
  archiveForm.reason = ''
  archiveForm.operator = ''
  archiveForm.remark = ''
}

const handleApplyReason = () => {
  if (!archiveForm.reason) {
    ElMessage.warning('请先选择统一报废原因')
    return
  }
  previewItems.value.forEach((item) => {
    item.reason = archiveForm.reason
  })
  ElMessage.success('已应用统一报废原因到所有项')
}

const handleArchiveSubmit = async () => {
  if (!archiveFormRef.value) return
  await archiveFormRef.value.validate(async (valid) => {
    if (!valid) return

    if (previewItems.value.length === 0) {
      ElMessage.warning('请先生成待报废配件清单')
      return
    }

    const hasEmptyReason = previewItems.value.some((item) => !item.reason)
    if (hasEmptyReason) {
      ElMessage.warning('请为所有配件选择报废原因，或使用"应用统一原因"')
      return
    }

    const hasInvalidQty = previewItems.value.some(
      (item) => !item.scrapQuantity || item.scrapQuantity < 1 || item.scrapQuantity > item.stockQuantity
    )
    if (hasInvalidQty) {
      ElMessage.warning('请检查所有配件的报废数量是否正确')
      return
    }

    const itemDetails = previewItems.value
      .map((item) => `• ${item.accessoryName} - ${item.accessoryModel} × ${item.scrapQuantity} [${item.reason}]`)
      .join('\n')

    try {
      await ElMessageBox.confirm(
        `确认归档以下 ${previewItems.value.length} 项配件报废？\n\n${itemDetails}\n\n归档人: ${archiveForm.operator}\n\n归档后库存将扣减且不可恢复。`,
        '归档确认',
        {
          confirmButtonText: '确认归档',
          cancelButtonText: '取消',
          type: 'warning',
          dangerouslyUseHTMLString: false
        }
      )
    } catch {
      return
    }

    submitting.value = true
    try {
      const items = previewItems.value.map((item) => ({
        accessoryId: item.accessoryId,
        accessoryName: item.accessoryName,
        accessoryModel: item.accessoryModel,
        unit: item.unit,
        warehouseZone: item.warehouseZone,
        categoryId: item.categoryId,
        categoryName: item.categoryName,
        stockQuantity: item.stockQuantity,
        scrapQuantity: item.scrapQuantity,
        inboundTime: item.inboundTime,
        reason: item.reason,
        remark: item.remark
      }))

      await createAgingBatch({
        categoryId: queryForm.categoryId || null,
        categoryName: getCategoryName(queryForm.categoryId),
        warehouseZone: queryForm.warehouseZone || null,
        inboundStart: queryForm.inboundRange?.[0] || null,
        inboundEnd: queryForm.inboundRange?.[1] || null,
        reason: archiveForm.reason || '自然老化',
        operator: archiveForm.operator,
        remark: archiveForm.remark,
        items
      })

      ElMessage.success(`成功归档 ${previewItems.value.length} 项配件`)
      clearPreview()
      loadHistory()
      loadWarehouseZones()
    } catch (error) {
      console.error('归档失败:', error)
      ElMessage.error(error?.response?.data?.message || '归档失败，请稍后重试')
    } finally {
      submitting.value = false
    }
  })
}

const getCategoryName = (id) => {
  if (!id) return ''
  const findInTree = (nodes) => {
    for (const node of nodes) {
      if (node.id === id) return node.name
      if (node.children) {
        const found = findInTree(node.children)
        if (found) return found
      }
    }
    return ''
  }
  return findInTree(categoryTree.value)
}

const loadHistory = async () => {
  historyLoading.value = true
  try {
    const data = await getAgingBatchList()
    historyList.value = data.records || data.list || data || []
  } catch (error) {
    console.error('加载归档历史失败:', error)
    ElMessage.error('加载归档历史失败，请稍后重试')
  } finally {
    historyLoading.value = false
  }
}

const handleViewDetail = async (row) => {
  currentBatch.value = row
  detailLoading.value = true
  detailDialogVisible.value = true
  try {
    const data = await getAgingBatchItems(row.id)
    currentBatchItems.value = data || []
  } catch (error) {
    console.error('加载批次详情失败:', error)
    ElMessage.error('加载批次详情失败')
  } finally {
    detailLoading.value = false
  }
}

const handleDeleteBatch = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除批次【${row.batchNo}】吗？删除后已扣减的库存将回滚。`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
  } catch {
    return
  }
  try {
    await deleteAgingBatch(row.id)
    ElMessage.success('删除成功')
    loadHistory()
    loadWarehouseZones()
  } catch (error) {
    console.error('删除失败:', error)
    ElMessage.error('删除失败，请稍后重试')
  }
}

onMounted(() => {
  loadCategoryTree()
  loadWarehouseZones()
  loadHistory()
})
</script>
