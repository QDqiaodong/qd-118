<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-title">
        <el-icon><Tickets /></el-icon>
        <span class="ml-8">半年度清点向导</span>
      </div>
      <div style="display: flex; gap: 12px">
        <el-input v-model="checkPerson" placeholder="请输入清点人" maxlength="20" style="width: 180px" />
        <el-input v-model="remarkInput" placeholder="清点备注（可选）" maxlength="100" show-word-limit style="width: 280px" />
      </div>
    </div>

    <div class="wizard-steps" v-loading="loadingZones">
      <div
        v-for="(zone, index) in zoneList"
        :key="zone"
        class="wizard-step"
        :class="{ active: currentZoneIndex === index, completed: isZoneCompleted(zone) }"
        @click="switchZone(index)"
      >
        <div class="step-circle">
          <el-icon v-if="isZoneCompleted(zone)"><CircleCheck /></el-icon>
          <span v-else>{{ index + 1 }}</span>
        </div>
        <div class="step-label">{{ zone }}区</div>
        <div v-if="index < zoneList.length - 1" class="step-line"></div>
      </div>
    </div>

    <div v-if="currentZoneStats" class="zone-content" v-loading="loadingZone">
      <div class="zone-stats-cards">
        <div class="stat-card terminal">
          <div class="stat-icon">
            <el-icon :size="24"><Connection /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">接线端子</div>
            <div class="stat-value">{{ currentZoneStats.terminalCount }} 项</div>
            <div class="stat-filled">已录入: {{ getFilledCountByType('terminal') }} / {{ currentZoneStats.terminalCount }}</div>
          </div>
        </div>
        <div class="stat-card trunk">
          <div class="stat-icon">
            <el-icon :size="24"><Grid /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">线槽</div>
            <div class="stat-value">{{ currentZoneStats.trunkCount }} 项</div>
            <div class="stat-filled">已录入: {{ getFilledCountByType('trunk') }} / {{ currentZoneStats.trunkCount }}</div>
          </div>
        </div>
        <div class="stat-card clip">
          <div class="stat-icon">
            <el-icon :size="24"><Paperclip /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">卡扣</div>
            <div class="stat-value">{{ currentZoneStats.clipCount }} 项</div>
            <div class="stat-filled">已录入: {{ getFilledCountByType('clip') }} / {{ currentZoneStats.clipCount }}</div>
          </div>
        </div>
        <div class="stat-card other">
          <div class="stat-icon">
            <el-icon :size="24"><MoreFilled /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">其他配件</div>
            <div class="stat-value">{{ currentZoneStats.otherCount }} 项</div>
            <div class="stat-filled">已录入: {{ getFilledCountByType('other') }} / {{ currentZoneStats.otherCount }}</div>
          </div>
        </div>
      </div>

      <div class="unfilled-alert" v-if="currentZoneStats.unfilledCount > 0">
        <el-alert
          :title="`当前还有 ${currentZoneStats.unfilledCount} 项未录入实盘数量，请完成所有项目后再提交`"
          type="warning"
          show-icon
          :closable="false"
        />
      </div>
      <div class="filled-alert" v-else>
        <el-alert
          title="本区所有配件已完成清点，可以提交"
          type="success"
          show-icon
          :closable="false"
        />
      </div>

      <div class="table-toolbar">
        <el-radio-group v-model="filterType">
          <el-radio-button label="all">全部</el-radio-button>
          <el-radio-button label="terminal">接线端子</el-radio-button>
          <el-radio-button label="trunk">线槽</el-radio-button>
          <el-radio-button label="clip">卡扣</el-radio-button>
          <el-radio-button label="unfilled">未录入</el-radio-button>
        </el-radio-group>
        <div class="spacer"></div>
        <el-button @click="handleFillZone">
          <el-icon><DocumentChecked /></el-icon>
          <span class="ml-8">按系统库存填充本区</span>
        </el-button>
        <el-button @click="handleClearZone">
          <el-icon><DeleteLocate /></el-icon>
          <span class="ml-8">清空本区实盘</span>
        </el-button>
        <el-button
          type="primary"
          :loading="submitting"
          @click="handleSubmitZone"
          :disabled="currentZoneStats.unfilledCount > 0 || !checkPerson"
        >
          <el-icon><Check /></el-icon>
          <span class="ml-8">提交本区清点</span>
        </el-button>
      </div>

      <el-table :data="filteredItems" border stripe style="width: 100%" v-loading="loadingZone">
        <el-table-column type="index" label="序号" width="70" align="center" />
        <el-table-column label="类别" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getCategoryTagType(row.categoryType)" size="small">{{ getCategoryLabel(row.categoryType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="配件名称" min-width="200" show-overflow-tooltip />
        <el-table-column prop="model" label="型号" width="140" show-overflow-tooltip />
        <el-table-column prop="spec" label="规格" width="140" show-overflow-tooltip />
        <el-table-column label="系统库存" width="120" align="center">
          <template #default="{ row }">
            <span style="font-weight: 600; color: #409eff">{{ formatNumber(row.systemQuantity) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="实盘数量" width="180" align="center">
          <template #default="{ row }">
            <el-input-number
              v-model="row.physicalQuantity"
              :min="0"
              :max="999999"
              controls-position="right"
              size="small"
              style="width: 100%"
              @change="onActualChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="差异" width="100" align="center">
          <template #default="{ row }">
            <span v-if="row.status !== 'UNFILLED' && row.difference !== null && row.difference !== undefined">
              <el-tag :type="getStatusTagType(row.status)" size="small">
                {{ row.difference > 0 ? '+' : '' }}{{ formatNumber(row.difference) }}
              </el-tag>
            </span>
            <span v-else style="color: #c0c4cc">-</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)" size="small">{{ getStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="unit" label="单位" width="80" align="center" />
      </el-table>

      <el-pagination
        class="mt-16"
        background
        layout="total, sizes, prev, pager, next, jumper"
        :total="filteredItems.length"
        :page-sizes="[10, 20, 50, 100]"
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
      />
    </div>

    <el-dialog v-model="submitDialogVisible" title="提交清点确认" width="500px">
      <el-descriptions :column="1" border size="small">
        <el-descriptions-item label="库区">{{ currentZone }}区</el-descriptions-item>
        <el-descriptions-item label="清点人">{{ checkPerson }}</el-descriptions-item>
        <el-descriptions-item label="配件总数">{{ currentZoneStats?.totalItemCount }} 项</el-descriptions-item>
        <el-descriptions-item label="已录入">{{ filledCount }} 项</el-descriptions-item>
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

    <el-dialog v-model="completeDialogVisible" title="清点完成" width="500px">
      <div style="text-align: center; padding: 20px 0">
        <el-icon :size="80" color="#67c23a"><CircleCheck /></el-icon>
        <h2 style="margin: 20px 0 10px">所有库区清点完成！</h2>
        <p style="color: #909399">半年度库房清点工作已全部完成</p>
      </div>
      <template #footer>
        <div class="form-dialog-footer">
          <el-button type="primary" @click="handleRestart">开始新的清点</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getAllZones, getZoneInventoryStats, createWizardBatchInventory } from '@/api/trunkSpec'

const loadingZones = ref(false)
const loadingZone = ref(false)
const submitting = ref(false)
const zoneList = ref([])
const currentZoneIndex = ref(0)
const currentZoneStats = ref(null)
const zoneStatsMap = ref({})
const completedZones = ref(new Set())
const filterType = ref('all')
const currentPage = ref(1)
const pageSize = ref(10)
const checkPerson = ref('')
const remarkInput = ref('')
const submitDialogVisible = ref(false)
const completeDialogVisible = ref(false)

const formatNumber = (num) => {
  if (num === null || num === undefined || isNaN(num)) return 0
  return Number(num).toLocaleString('zh-CN')
}

const currentZone = computed(() => zoneList.value[currentZoneIndex.value])

const isZoneCompleted = (zone) => completedZones.value.has(zone)

const getCategoryLabel = (type) => {
  const map = { terminal: '接线端子', trunk: '线槽', clip: '卡扣', other: '其他' }
  return map[type] || '其他'
}

const getCategoryTagType = (type) => {
  const map = { terminal: 'primary', trunk: 'success', clip: 'warning', other: 'info' }
  return map[type] || 'info'
}

const getStatusLabel = (status) => {
  const map = { UNFILLED: '未录入', MATCH: '已录入', SURPLUS: '盘盈', LOSS: '盘亏' }
  return map[status] || '未录入'
}

const getStatusTagType = (status) => {
  const map = { UNFILLED: 'info', MATCH: 'success', SURPLUS: 'danger', LOSS: 'warning' }
  return map[status] || 'info'
}

const recalcItemStatus = (row) => {
  if (row.physicalQuantity === null || row.physicalQuantity === undefined) {
    row.difference = null
    row.status = 'UNFILLED'
    row.filled = false
  } else {
    const diff = row.physicalQuantity - row.systemQuantity
    row.difference = diff
    if (diff === 0) {
      row.status = 'MATCH'
    } else if (diff > 0) {
      row.status = 'SURPLUS'
    } else {
      row.status = 'LOSS'
    }
    row.filled = true
  }
}

const onActualChange = (row) => {
  const wasFilled = row.filled
  recalcItemStatus(row)
  if (wasFilled !== row.filled) {
    if (row.filled) {
      currentZoneStats.value.unfilledCount--
    } else {
      currentZoneStats.value.unfilledCount++
    }
  }
}

const getFilledCountByType = (type) => {
  if (!currentZoneStats.value) return 0
  return currentZoneStats.value.items.filter(item => item.categoryType === type && item.status !== 'UNFILLED').length
}

const filteredItems = computed(() => {
  if (!currentZoneStats.value) return []

  let items = currentZoneStats.value.items

  if (filterType.value === 'unfilled') {
    items = items.filter(item => item.status === 'UNFILLED')
  } else if (filterType.value !== 'all') {
    items = items.filter(item => item.categoryType === filterType.value)
  }

  const start = (currentPage.value - 1) * pageSize.value
  return items.slice(start, start + pageSize.value)
})

const filledCount = computed(() => {
  if (!currentZoneStats.value) return 0
  return currentZoneStats.value.items.filter(r => r.status && r.status !== 'UNFILLED').length
})

const matchCount = computed(() => {
  if (!currentZoneStats.value) return 0
  return currentZoneStats.value.items.filter(r => r.status === 'MATCH').length
})

const surplusCount = computed(() => {
  if (!currentZoneStats.value) return 0
  return currentZoneStats.value.items.filter(r => r.status === 'SURPLUS').length
})

const lossCount = computed(() => {
  if (!currentZoneStats.value) return 0
  return currentZoneStats.value.items.filter(r => r.status === 'LOSS').length
})

const totalSystem = computed(() => {
  if (!currentZoneStats.value) return 0
  return currentZoneStats.value.items.reduce((sum, r) => sum + (r.systemQuantity || 0), 0)
})

const totalActual = computed(() => {
  if (!currentZoneStats.value) return 0
  return currentZoneStats.value.items.reduce((sum, r) => sum + (r.physicalQuantity ?? 0), 0)
})

const totalDiff = computed(() => {
  if (!currentZoneStats.value) return 0
  return currentZoneStats.value.items.reduce((sum, r) => {
    if (r.status === 'UNFILLED' || r.difference === null || r.difference === undefined) return sum
    return sum + r.difference
  }, 0)
})

const loadZones = async () => {
  loadingZones.value = true
  try {
    const data = await getAllZones()
    if (data && Array.isArray(data)) {
      zoneList.value = data
      if (data.length > 0) {
        currentZoneIndex.value = 0
        loadZoneStats(data[0])
      }
    }
  } catch (error) {
    console.error('加载库区列表失败:', error)
    ElMessage.error('加载库区列表失败，请稍后重试')
  } finally {
    loadingZones.value = false
  }
}

const loadZoneStats = async (zone) => {
  if (zoneStatsMap.value[zone]) {
    currentZoneStats.value = zoneStatsMap.value[zone]
    return
  }

  loadingZone.value = true
  try {
    const data = await getZoneInventoryStats(zone)
    if (data) {
      data.items = data.items.map(item => ({
        ...item,
        status: item.status || 'UNFILLED',
        filled: item.filled !== undefined ? item.filled : (item.status && item.status !== 'UNFILLED')
      }))
      zoneStatsMap.value[zone] = data
      currentZoneStats.value = data
    }
  } catch (error) {
    console.error('加载库区数据失败:', error)
    ElMessage.error('加载库区数据失败，请稍后重试')
  } finally {
    loadingZone.value = false
  }
}

const switchZone = (index) => {
  currentZoneIndex.value = index
  currentPage.value = 1
  loadZoneStats(zoneList.value[index])
}

const handleFillZone = () => {
  if (!currentZoneStats.value) return
  let filledAdded = 0
  currentZoneStats.value.items.forEach((row) => {
    const wasUnfilled = row.status === 'UNFILLED'
    row.physicalQuantity = row.systemQuantity
    row.difference = 0
    row.status = 'MATCH'
    row.filled = true
    if (wasUnfilled) filledAdded++
  })
  currentZoneStats.value.unfilledCount -= filledAdded
  ElMessage.success('已按系统库存填充本区所有实盘数量')
}

const handleClearZone = () => {
  if (!currentZoneStats.value) return
  let filledRemoved = 0
  currentZoneStats.value.items.forEach((row) => {
    const wasFilled = row.status !== 'UNFILLED'
    row.physicalQuantity = null
    row.difference = null
    row.status = 'UNFILLED'
    row.filled = false
    if (wasFilled) filledRemoved++
  })
  currentZoneStats.value.unfilledCount += filledRemoved
  ElMessage.success('已清空本区所有实盘数量')
}

const handleSubmitZone = () => {
  if (!checkPerson.value) {
    ElMessage.warning('请输入清点人')
    return
  }
  if (currentZoneStats.value.unfilledCount > 0) {
    ElMessage.warning(`还有 ${currentZoneStats.value.unfilledCount} 项未录入，请完成后再提交`)
    return
  }
  submitDialogVisible.value = true
}

const confirmSubmit = async () => {
  const items = currentZoneStats.value.items
    .filter(r => r.status && r.status !== 'UNFILLED')
    .map(r => ({
      accessoryId: r.id,
      accessoryName: r.name,
      categoryName: r.categoryName,
      systemQuantity: r.systemQuantity,
      physicalQuantity: r.physicalQuantity,
      difference: r.difference
    }))

  submitting.value = true
  try {
    await createWizardBatchInventory({
      warehouseZone: currentZone.value,
      checkPerson: checkPerson.value,
      remark: remarkInput.value,
      items
    })

    ElMessage.success(`${currentZone.value}区清点提交成功，库存已更新`)
    completedZones.value.add(currentZone.value)
    submitDialogVisible.value = false

    if (completedZones.value.size === zoneList.value.length) {
      completeDialogVisible.value = true
    } else if (currentZoneIndex.value < zoneList.value.length - 1) {
      switchZone(currentZoneIndex.value + 1)
    }
  } catch (error) {
    console.error('提交清点失败:', error)
    ElMessage.error('提交清点失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

const handleRestart = () => {
  completedZones.value.clear()
  zoneStatsMap.value = {}
  completeDialogVisible.value = false
  remarkInput.value = ''
  currentZoneIndex.value = 0
  if (zoneList.value.length > 0) {
    loadZoneStats(zoneList.value[0])
  }
}

onMounted(() => {
  loadZones()
})
</script>

<style lang="scss" scoped>
.wizard-steps {
  display: flex;
  justify-content: center;
  align-items: flex-start;
  padding: 24px;
  background: #fff;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.wizard-step {
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  cursor: pointer;

  .step-circle {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    background: #f0f2f5;
    border: 2px solid #dcdfe6;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #909399;
    font-weight: 600;
    transition: all 0.3s ease;
    z-index: 1;
  }

  .step-label {
    margin-top: 8px;
    font-size: 14px;
    color: #909399;
  }

  .step-line {
    position: absolute;
    top: 20px;
    left: calc(50% + 20px);
    width: 80px;
    height: 2px;
    background: #dcdfe6;
    z-index: 0;
  }

  &.active .step-circle {
    background: #409eff;
    border-color: #409eff;
    color: #fff;
    transform: scale(1.1);
  }

  &.active .step-label {
    color: #409eff;
    font-weight: 600;
  }

  &.completed .step-circle {
    background: #67c23a;
    border-color: #67c23a;
    color: #fff;
  }

  &.completed .step-label {
    color: #67c23a;
  }

  &.completed .step-line {
    background: #67c23a;
  }
}

.zone-content {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.zone-stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 16px;
  margin-bottom: 16px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px;
  border-radius: 8px;
  background: #f8f9fa;
  border-left: 4px solid #909399;

  .stat-icon {
    width: 48px;
    height: 48px;
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    flex-shrink: 0;
  }

  .stat-info {
    flex: 1;

    .stat-label {
      font-size: 13px;
      color: #606266;
      margin-bottom: 2px;
    }

    .stat-value {
      font-size: 20px;
      font-weight: 600;
      color: #2c3e50;
      margin-bottom: 2px;
    }

    .stat-filled {
      font-size: 12px;
      color: #909399;
    }
  }

  &.terminal {
    border-left-color: #409eff;
    .stat-icon { background: linear-gradient(135deg, #409eff, #66b1ff); }
  }

  &.trunk {
    border-left-color: #67c23a;
    .stat-icon { background: linear-gradient(135deg, #67c23a, #85ce61); }
  }

  &.clip {
    border-left-color: #e6a23c;
    .stat-icon { background: linear-gradient(135deg, #e6a23c, #ebb563); }
  }

  &.other {
    border-left-color: #909399;
    .stat-icon { background: linear-gradient(135deg, #909399, #a6a9ad); }
  }
}

.unfilled-alert,
.filled-alert {
  margin-bottom: 16px;
}

.table-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 8px;
}

.spacer {
  flex: 1;
}

.mt-16 {
  margin-top: 16px;
}

.ml-8 {
  margin-left: 8px;
}
</style>
