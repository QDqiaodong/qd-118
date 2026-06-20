<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-title">
        <el-icon><Tickets /></el-icon>
        <span class="ml-8">半年度库房清点</span>
      </div>
      <div style="display: flex; gap: 12px">
        <el-input v-model="remarkInput" placeholder="清点备注（可选）" maxlength="100" show-word-limit style="width: 280px" />
        <el-button type="primary" :loading="submitting" @click="handleSubmitInventory" :disabled="!hasAnyInput">
          <el-icon><Check /></el-icon>
          <span class="ml-8">一键提交清点</span>
        </el-button>
        <el-button @click="handleFillAll">
          <el-icon><DocumentChecked /></el-icon>
          <span class="ml-8">全部按系统库存填充</span>
        </el-button>
        <el-button @click="handleClearAll">
          <el-icon><DeleteLocate /></el-icon>
          <span class="ml-8">清空实盘数量</span>
        </el-button>
      </div>
    </div>

    <div class="table-toolbar">
      <el-input v-model="searchKeyword" placeholder="搜索配件名称、型号" clearable style="width: 260px" :prefix-icon="Search" />
      <el-select v-model="filterZone" placeholder="库房分区筛选" clearable style="width: 160px">
        <el-option label="A区" value="A" />
        <el-option label="B区" value="B" />
        <el-option label="C区" value="C" />
        <el-option label="D区" value="D" />
      </el-select>
      <el-radio-group v-model="filterStatus">
        <el-radio-button label="all">全部</el-radio-button>
        <el-radio-button label="diff">有差异</el-radio-button>
        <el-radio-button label="match">账实相符</el-radio-button>
        <el-radio-button label="unfilled">未盘点</el-radio-button>
      </el-radio-group>
      <div class="spacer"></div>
      <div style="display: flex; gap: 24px; font-size: 14px">
        <span>总数量：<b style="color: #409eff">{{ formatNumber(totalSystem) }}</b></span>
        <span>实盘合计：<b style="color: #67c23a">{{ formatNumber(totalActual) }}</b></span>
        <span>差异合计：
          <b :style="{ color: totalDiff > 0 ? '#f56c6c' : totalDiff < 0 ? '#e6a23c' : '#909399' }">
            {{ totalDiff > 0 ? '+' : '' }}{{ formatNumber(totalDiff) }}
          </b>
        </span>
      </div>
    </div>

    <el-table :data="pagedList" border stripe style="width: 100%" v-loading="loading">
      <el-table-column type="index" label="序号" width="70" align="center" />
      <el-table-column prop="name" label="配件名称" min-width="200" show-overflow-tooltip />
      <el-table-column prop="model" label="型号" width="140" show-overflow-tooltip />
      <el-table-column label="库房分区" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="zoneTagType(row.zone)" size="small">{{ row.zone ? row.zone + '区' : '-' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="系统库存" width="120" align="center">
        <template #default="{ row }">
          <span style="font-weight: 600; color: #409eff">{{ formatNumber(row.quantity) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="实盘数量" width="180" align="center">
        <template #default="{ row }">
          <el-input-number
            v-model="row.actualQuantity"
            :min="0"
            :max="999999"
            controls-position="right"
            size="small"
            style="width: 100%"
            @change="onActualChange(row)"
          />
        </template>
      </el-table-column>
      <el-table-column label="差异数量" width="120" align="center">
        <template #default="{ row }">
          <span v-if="row.actualQuantity !== null && row.actualQuantity !== undefined">
            <el-tag :type="diffTagType(row)" size="small">
              {{ getDiff(row) > 0 ? '+' : '' }}{{ formatNumber(getDiff(row)) }}
            </el-tag>
          </span>
          <span v-else style="color: #c0c4cc">-</span>
        </template>
      </el-table-column>
      <el-table-column label="差异状态" width="120" align="center">
        <template #default="{ row }">
          <span v-if="row.actualQuantity === null || row.actualQuantity === undefined" style="color: #909399">
            <el-icon><Warning /></el-icon> 未盘点
          </span>
          <span v-else-if="getDiff(row) === 0" style="color: #67c23a">
            <el-icon><CircleCheck /></el-icon> 账实相符
          </span>
          <span v-else-if="getDiff(row) > 0" style="color: #f56c6c">
            <el-icon><Top /></el-icon> 盘盈
          </span>
          <span v-else style="color: #e6a23c">
            <el-icon><Bottom /></el-icon> 盘亏
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="unit" label="单位" width="80" align="center" />
    </el-table>

    <el-pagination
      class="mt-16"
      background
      layout="total, sizes, prev, pager, next, jumper"
      :total="filteredList.length"
      :page-sizes="[10, 20, 50, 100]"
      v-model:current-page="currentPage"
      v-model:page-size="pageSize"
    />

    <el-dialog v-model="submitDialogVisible" title="清点提交确认" width="500px">
      <el-descriptions :column="1" border size="small">
        <el-descriptions-item label="配件总数">{{ dataList.length }} 项</el-descriptions-item>
        <el-descriptions-item label="已盘点">{{ filledCount }} 项</el-descriptions-item>
        <el-descriptions-item label="账实相符">{{ matchCount }} 项</el-descriptions-item>
        <el-descriptions-item label="盘盈项数">{{ surplusCount }} 项</el-descriptions-item>
        <el-descriptions-item label="盘亏项数">{{ lossCount }} 项</el-descriptions-item>
        <el-descriptions-item label="系统库存合计">{{ formatNumber(totalSystem) }}</el-descriptions-item>
        <el-descriptions-item label="实盘数量合计">{{ formatNumber(totalActual) }}</el-descriptions-item>
        <el-descriptions-item label="差异总数">
          <span :style="{ color: totalDiff > 0 ? '#f56c6c' : totalDiff < 0 ? '#e6a23c' : '#67c23a' }">
            {{ totalDiff > 0 ? '+' : '' }}{{ formatNumber(totalDiff) }}
          </span>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <div class="form-dialog-footer">
          <el-button @click="submitDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="confirmSubmit">确认提交</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { getAccessoryList } from '@/api/accessory'
import { createInventory } from '@/api/inventory'

const loading = ref(false)
const submitting = ref(false)
const searchKeyword = ref('')
const filterZone = ref('')
const filterStatus = ref('all')
const currentPage = ref(1)
const pageSize = ref(10)
const submitDialogVisible = ref(false)
const remarkInput = ref('')

const dataList = ref([])

const formatNumber = (num) => {
  if (num === null || num === undefined || isNaN(num)) return 0
  return Number(num).toLocaleString('zh-CN')
}

const zoneTagType = (zone) => {
  const map = { A: 'primary', B: 'success', C: 'warning', D: 'danger' }
  return map[zone] || 'info'
}

const getDiff = (row) => {
  if (row.actualQuantity === null || row.actualQuantity === undefined) return null
  return row.actualQuantity - row.quantity
}

const diffTagType = (row) => {
  const diff = getDiff(row)
  if (diff === null) return 'info'
  if (diff === 0) return 'success'
  if (diff > 0) return 'danger'
  return 'warning'
}

const onActualChange = () => {}

const hasAnyInput = computed(() => {
  return dataList.value.some((r) => r.actualQuantity !== null && r.actualQuantity !== undefined)
})

const totalSystem = computed(() => {
  return dataList.value.reduce((sum, r) => sum + (r.quantity || 0), 0)
})

const totalActual = computed(() => {
  return dataList.value.reduce((sum, r) => sum + (r.actualQuantity ?? 0), 0)
})

const totalDiff = computed(() => {
  return dataList.value.reduce((sum, r) => {
    if (r.actualQuantity === null || r.actualQuantity === undefined) return sum
    return sum + (r.actualQuantity - r.quantity)
  }, 0)
})

const filledCount = computed(() => {
  return dataList.value.filter((r) => r.actualQuantity !== null && r.actualQuantity !== undefined).length
})

const matchCount = computed(() => {
  return dataList.value.filter((r) => r.actualQuantity !== null && r.actualQuantity !== undefined && getDiff(r) === 0).length
})

const surplusCount = computed(() => {
  return dataList.value.filter((r) => getDiff(r) !== null && getDiff(r) > 0).length
})

const lossCount = computed(() => {
  return dataList.value.filter((r) => getDiff(r) !== null && getDiff(r) < 0).length
})

const filteredList = computed(() => {
  return dataList.value.filter((item) => {
    let ok = true
    if (searchKeyword.value) {
      const kw = searchKeyword.value.toLowerCase()
      ok = ok && (
        item.name?.toLowerCase().includes(kw) ||
        item.model?.toLowerCase().includes(kw)
      )
    }
    if (filterZone.value) {
      ok = ok && item.zone === filterZone.value
    }
    if (filterStatus.value !== 'all') {
      const diff = getDiff(item)
      if (filterStatus.value === 'diff') {
        ok = ok && diff !== null && diff !== 0
      } else if (filterStatus.value === 'match') {
        ok = ok && diff === 0
      } else if (filterStatus.value === 'unfilled') {
        ok = ok && diff === null
      }
    }
    return ok
  })
})

const pagedList = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredList.value.slice(start, start + pageSize.value)
})

const mapAccessoryFields = (item) => ({
  ...item,
  quantity: item.stockQuantity ?? item.quantity,
  zone: item.warehouseZone ?? item.zone
})

const loadData = async () => {
  loading.value = true
  try {
    const data = await getAccessoryList({ pageNum: 1, pageSize: 9999 })
    if (data) {
      const list = data.records || data.list || data || []
      dataList.value = list.map((item) => ({
        ...mapAccessoryFields(item),
        actualQuantity: null
      }))
    }
  } catch (error) {
    console.error('加载配件列表失败:', error)
    ElMessage.error('加载配件列表失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const handleFillAll = () => {
  dataList.value.forEach((row) => {
    row.actualQuantity = row.quantity
  })
  ElMessage.success('已按系统库存填充全部实盘数量')
}

const handleClearAll = () => {
  dataList.value.forEach((row) => {
    row.actualQuantity = null
  })
  ElMessage.success('已清空实盘数量')
}

const handleSubmitInventory = () => {
  if (!hasAnyInput.value) {
    ElMessage.warning('请至少录入一项实盘数量')
    return
  }
  submitDialogVisible.value = true
}

const confirmSubmit = async () => {
  const details = dataList.value
    .filter((r) => r.actualQuantity !== null && r.actualQuantity !== undefined)
    .map((r) => ({
      accessoryId: r.id,
      systemQuantity: r.quantity,
      actualQuantity: r.actualQuantity,
      diff: r.actualQuantity - r.quantity
    }))

  submitting.value = true
  try {
    await createInventory({
      remark: remarkInput.value,
      details
    })
    ElMessage.success('清点记录提交成功，库存已更新')
    loadData()
    submitDialogVisible.value = false
    remarkInput.value = ''
  } catch (error) {
    console.error('提交清点失败:', error)
    ElMessage.error('提交清点失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>
