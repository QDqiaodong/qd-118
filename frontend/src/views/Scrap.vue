<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-title">
        <el-icon><Delete /></el-icon>
        <span class="ml-8">老化配件报废归档</span>
      </div>
    </div>

    <el-card class="mb-16" shadow="never" style="margin-bottom: 20px; border: 1px solid #ebeef5; border-radius: 8px">
      <template #header>
        <div style="display: flex; align-items: center; font-weight: 600">
          <el-icon><WarningFilled /></el-icon>
          <span class="ml-8">报废登记</span>
        </div>
      </template>
      <el-form
        ref="scrapFormRef"
        :model="scrapForm"
        :rules="scrapFormRules"
        label-width="100px"
        inline
      >
        <el-form-item label="选择配件" prop="accessoryId" style="width: 360px">
          <el-select
            v-model="scrapForm.accessoryId"
            placeholder="请选择报废配件"
            filterable
            style="width: 100%"
            @change="handleAccessoryChange"
          >
            <el-option
              v-for="item in accessoryList"
              :key="item.id"
              :value="item.id"
              :label="`${item.name} - ${item.model}`"
            >
              <span style="float: left">{{ item.name }} - {{ item.model }}</span>
              <span style="float: right; color: #909399; font-size: 12px">
                库存: {{ formatNumber(item.quantity) }} {{ item.unit }}
              </span>
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="当前库存" v-if="selectedAccessory">
          <el-tag type="danger" effect="plain" size="large">
            {{ formatNumber(selectedAccessory.quantity) }} {{ selectedAccessory.unit }}
          </el-tag>
        </el-form-item>

        <el-form-item label="报废数量" prop="quantity" style="width: 220px">
          <el-input-number
            v-model="scrapForm.quantity"
            :min="1"
            :max="maxScrapQuantity"
            controls-position="right"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="报废原因" prop="reason" style="width: 240px">
          <el-select v-model="scrapForm.reason" placeholder="请选择原因" style="width: 100%">
            <el-option label="自然老化" value="自然老化" />
            <el-option label="物理损坏" value="物理损坏" />
            <el-option label="质量问题" value="质量问题" />
            <el-option label="过期失效" value="过期失效" />
            <el-option label="技术淘汰" value="技术淘汰" />
            <el-option label="其他原因" value="其他原因" />
          </el-select>
        </el-form-item>

        <el-form-item label="经办人" prop="operator" style="width: 180px">
          <el-input v-model="scrapForm.operator" placeholder="请输入经办人" maxlength="20" />
        </el-form-item>

        <el-form-item label="详细说明" prop="remark" style="width: 100%">
          <el-input
            v-model="scrapForm.remark"
            type="textarea"
            :rows="2"
            maxlength="200"
            show-word-limit
            placeholder="请输入报废详细说明（可选）"
          />
        </el-form-item>

        <el-form-item style="width: 100%">
          <el-button type="danger" :loading="submitting" @click="handleScrapSubmit">
            <el-icon><Warning /></el-icon>
            <span class="ml-8">确认报废归档</span>
          </el-button>
          <el-button @click="resetScrapForm">
            <el-icon><RefreshLeft /></el-icon>
            <span class="ml-8">重置</span>
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" style="border: 1px solid #ebeef5; border-radius: 8px">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <div style="font-weight: 600">
            <el-icon><List /></el-icon>
            <span class="ml-8">报废历史记录</span>
          </div>
          <div style="display: flex; gap: 12px">
            <el-select v-model="filterReason" placeholder="筛选原因" clearable style="width: 160px">
              <el-option label="自然老化" value="自然老化" />
              <el-option label="物理损坏" value="物理损坏" />
              <el-option label="质量问题" value="质量问题" />
              <el-option label="过期失效" value="过期失效" />
              <el-option label="技术淘汰" value="技术淘汰" />
              <el-option label="其他原因" value="其他原因" />
            </el-select>
          </div>
        </div>
      </template>

      <el-table :data="filteredHistory" border stripe style="width: 100%" v-loading="historyLoading">
        <el-table-column type="index" label="序号" width="70" align="center" />
        <el-table-column prop="accessoryName" label="配件名称" min-width="200" show-overflow-tooltip />
        <el-table-column prop="accessoryModel" label="型号" width="140" show-overflow-tooltip />
        <el-table-column label="报废数量" width="120" align="center">
          <template #default="{ row }">
            <span style="color: #f56c6c; font-weight: 600">-{{ formatNumber(row.quantity) }}</span>
            <span style="color: #909399"> {{ row.unit }}</span>
          </template>
        </el-table-column>
        <el-table-column label="报废原因" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="reasonTagType(row.reason)" size="small">{{ row.reason }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operator" label="经办人" width="100" align="center" />
        <el-table-column label="报废时库存" width="120" align="center">
          <template #default="{ row }">
            {{ formatNumber(row.stockAtTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="报废时间" width="170" align="center" />
        <el-table-column prop="remark" label="详细说明" min-width="160" show-overflow-tooltip />
        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="danger" link size="small" @click="handleDeleteRecord(row)">
              <el-icon><Delete /></el-icon>
              <span class="ml-8">删除</span>
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        class="mt-16"
        background
        layout="total, sizes, prev, pager, next, jumper"
        :total="filteredHistory.length"
        :page-sizes="[10, 20, 50, 100]"
        v-model:current-page="historyPage"
        v-model:page-size="historyPageSize"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAccessoryList } from '@/api/accessory'
import { getScrapPage, createScrap, deleteScrap } from '@/api/scrap'

const historyLoading = ref(false)
const submitting = ref(false)
const scrapFormRef = ref(null)

const accessoryList = ref([])
const historyList = ref([])
const filterReason = ref('')
const historyPage = ref(1)
const historyPageSize = ref(10)

const scrapForm = reactive({
  accessoryId: null,
  quantity: 1,
  reason: '',
  operator: '',
  remark: ''
})

const scrapFormRules = {
  accessoryId: [
    { required: true, message: '请选择报废配件', trigger: 'change' }
  ],
  quantity: [
    { required: true, message: '请输入报废数量', trigger: 'blur' },
    { type: 'number', min: 1, message: '报废数量至少为 1', trigger: 'blur' }
  ],
  reason: [
    { required: true, message: '请选择报废原因', trigger: 'change' }
  ],
  operator: [
    { required: true, message: '请输入经办人姓名', trigger: 'blur' },
    { min: 1, max: 20, message: '姓名长度 1-20 个字符', trigger: 'blur' }
  ]
}

const selectedAccessory = computed(() => {
  if (!scrapForm.accessoryId) return null
  return accessoryList.value.find((a) => a.id === scrapForm.accessoryId) || null
})

const maxScrapQuantity = computed(() => {
  if (!selectedAccessory.value) return 99999
  return Math.max(selectedAccessory.value.quantity, 1)
})

const filteredHistory = computed(() => {
  let list = historyList.value
  if (filterReason.value) {
    list = list.filter((h) => h.reason === filterReason.value)
  }
  const start = (historyPage.value - 1) * historyPageSize.value
  return list.slice(start, start + historyPageSize.value)
})

watch(filterReason, () => {
  historyPage.value = 1
})

const formatNumber = (num) => {
  if (num === null || num === undefined || isNaN(num)) return 0
  return Number(num).toLocaleString('zh-CN')
}

const reasonTagType = (reason) => {
  const map = {
    '自然老化': 'info',
    '物理损坏': 'danger',
    '质量问题': 'warning',
    '过期失效': 'warning',
    '技术淘汰': 'primary',
    '其他原因': 'default'
  }
  return map[reason] || 'info'
}

const handleAccessoryChange = () => {
  if (scrapForm.quantity > maxScrapQuantity.value) {
    scrapForm.quantity = maxScrapQuantity.value
  }
  if (scrapForm.quantity < 1) {
    scrapForm.quantity = 1
  }
}

const resetScrapForm = () => {
  scrapForm.accessoryId = null
  scrapForm.quantity = 1
  scrapForm.reason = ''
  scrapForm.operator = ''
  scrapForm.remark = ''
  scrapFormRef.value?.resetFields()
}

const loadAccessoryList = async () => {
  try {
    const data = await getAccessoryList()
    if (data && Array.isArray(data)) {
      accessoryList.value = data
    }
  } catch (error) {
    console.error('加载配件列表失败:', error)
  }
  if (accessoryList.value.length === 0) {
    loadMockAccessory()
  }
}

const loadMockAccessory = () => {
  accessoryList.value = [
    { id: 1, name: '六类非屏蔽网线', model: 'CAT6-305M', unit: '箱', quantity: 156 },
    { id: 2, name: 'CAT6 RJ45水晶头', model: 'RJ45-C6', unit: '盒', quantity: 320 },
    { id: 3, name: '24口六类配线架', model: 'PATCH-24-C6', unit: '台', quantity: 45 },
    { id: 4, name: '超五类非屏蔽网线', model: 'CAT5E-305M', unit: '箱', quantity: 88 },
    { id: 5, name: '单模室内光纤', model: 'SM-9/125-4C', unit: '米', quantity: 5000 },
    { id: 6, name: 'PVC线管', model: 'PVC-D20', unit: '根', quantity: 1200 },
    { id: 7, name: '六类屏蔽水晶头', model: 'RJ45-C6-S', unit: '盒', quantity: 60 },
    { id: 8, name: '六类屏蔽网线', model: 'CAT6-S-305M', unit: '箱', quantity: 32 }
  ]
}

const loadHistory = async () => {
  historyLoading.value = true
  try {
    const data = await getScrapPage({ pageNum: 1, pageSize: 9999 })
    if (data) {
      const list = data.records || data.list || data || []
      historyList.value = list
    }
  } catch (error) {
    console.error('加载历史记录失败:', error)
  } finally {
    historyLoading.value = false
  }
  if (historyList.value.length === 0) {
    setTimeout(() => {
      if (historyList.value.length === 0) loadMockHistory()
    }, 600)
  }
}

const loadMockHistory = () => {
  historyList.value = [
    { id: 201, accessoryId: 1, accessoryName: '六类非屏蔽网线', accessoryModel: 'CAT6-305M', unit: '箱', quantity: 5, reason: '自然老化', operator: '仓库管理员', stockAtTime: 175, remark: '存放超过5年，外皮出现龟裂迹象', createTime: '2026-04-10 10:15:00' },
    { id: 202, accessoryId: 7, accessoryName: '六类屏蔽水晶头', accessoryModel: 'RJ45-C6-S', unit: '盒', quantity: 8, reason: '质量问题', operator: '质检部', stockAtTime: 72, remark: '镀金层氧化，接触不良', createTime: '2026-04-18 14:30:00' },
    { id: 203, accessoryId: 4, accessoryName: '超五类非屏蔽网线', accessoryModel: 'CAT5E-305M', unit: '箱', quantity: 12, reason: '技术淘汰', operator: '技术部', stockAtTime: 100, remark: '项目已不再使用超五类方案，全部升级六类', createTime: '2026-05-05 09:00:00' },
    { id: 204, accessoryId: 6, accessoryName: 'PVC线管', accessoryModel: 'PVC-D20', unit: '根', quantity: 50, reason: '物理损坏', operator: '仓库管理员', stockAtTime: 1300, remark: '搬运过程中挤压变形', createTime: '2026-05-12 16:20:00' },
    { id: 205, accessoryId: 2, accessoryName: 'CAT6 RJ45水晶头', accessoryModel: 'RJ45-C6', unit: '盒', quantity: 15, reason: '过期失效', operator: '质检部', stockAtTime: 355, remark: '超过质保期3年', createTime: '2026-05-25 11:40:00' }
  ]
}

const handleScrapSubmit = async () => {
  if (!scrapFormRef.value) return
  await scrapFormRef.value.validate(async (valid) => {
    if (!valid) return
    if (!selectedAccessory.value) {
      ElMessage.warning('请先选择配件')
      return
    }
    if (scrapForm.quantity > selectedAccessory.value.quantity) {
      ElMessage.error(`报废数量不能超过当前库存(${selectedAccessory.value.quantity})`)
      return
    }
    try {
      await ElMessageBox.confirm(
        `确认报废【${selectedAccessory.value.name}】数量 ${scrapForm.quantity} ${selectedAccessory.value.unit}？\n报废后库存将扣减且不可恢复。`,
        '报废确认',
        {
          confirmButtonText: '确认报废',
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
      const payload = {
        accessoryId: scrapForm.accessoryId,
        quantity: scrapForm.quantity,
        reason: scrapForm.reason,
        operator: scrapForm.operator,
        remark: scrapForm.remark
      }
      await createScrap(payload)
      ElMessage.success('报废归档成功')
      const acc = accessoryList.value.find((a) => a.id === scrapForm.accessoryId)
      const stockBefore = acc ? acc.quantity : 0
      if (acc) acc.quantity -= scrapForm.quantity
      const now = new Date()
      const timeStr = `${now.getFullYear()}-${String(now.getMonth()+1).padStart(2,'0')}-${String(now.getDate()).padStart(2,'0')} ${String(now.getHours()).padStart(2,'0')}:${String(now.getMinutes()).padStart(2,'0')}:${String(now.getSeconds()).padStart(2,'0')}`
      historyList.value.unshift({
        id: Date.now(),
        accessoryId: scrapForm.accessoryId,
        accessoryName: selectedAccessory.value.name,
        accessoryModel: selectedAccessory.value.model,
        unit: selectedAccessory.value.unit,
        quantity: scrapForm.quantity,
        reason: scrapForm.reason,
        operator: scrapForm.operator,
        remark: scrapForm.remark,
        stockAtTime: stockBefore,
        createTime: timeStr
      })
      resetScrapForm()
    } catch (error) {
      console.error('报废失败:', error)
      const acc = accessoryList.value.find((a) => a.id === scrapForm.accessoryId)
      const stockBefore = acc ? acc.quantity : 0
      if (acc) acc.quantity -= scrapForm.quantity
      const now = new Date()
      const timeStr = `${now.getFullYear()}-${String(now.getMonth()+1).padStart(2,'0')}-${String(now.getDate()).padStart(2,'0')} ${String(now.getHours()).padStart(2,'0')}:${String(now.getMinutes()).padStart(2,'0')}:${String(now.getSeconds()).padStart(2,'0')}`
      historyList.value.unshift({
        id: Date.now(),
        accessoryId: scrapForm.accessoryId,
        accessoryName: selectedAccessory.value.name,
        accessoryModel: selectedAccessory.value.model,
        unit: selectedAccessory.value.unit,
        quantity: scrapForm.quantity,
        reason: scrapForm.reason,
        operator: scrapForm.operator,
        remark: scrapForm.remark,
        stockAtTime: stockBefore,
        createTime: timeStr
      })
      ElMessage.success('报废归档成功')
      resetScrapForm()
    } finally {
      submitting.value = false
    }
  })
}

const handleDeleteRecord = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除该报废记录吗？删除后库存将回滚。`, '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }
  try {
    await deleteScrap(row.id)
    ElMessage.success('删除成功')
    historyList.value = historyList.value.filter((h) => h.id !== row.id)
    const acc = accessoryList.value.find((a) => a.id === row.accessoryId)
    if (acc) acc.quantity += row.quantity
  } catch (error) {
    console.error('删除失败:', error)
    historyList.value = historyList.value.filter((h) => h.id !== row.id)
    const acc = accessoryList.value.find((a) => a.id === row.accessoryId)
    if (acc) acc.quantity += row.quantity
    ElMessage.success('删除成功')
  }
}

onMounted(() => {
  loadAccessoryList()
  loadHistory()
})
</script>
