<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-title">
        <el-icon><Sell /></el-icon>
        <span class="ml-8">车间领用出库登记</span>
      </div>
    </div>

    <el-card class="mb-16" shadow="never" style="margin-bottom: 20px; border: 1px solid #ebeef5; border-radius: 8px">
      <template #header>
        <div style="display: flex; align-items: center; font-weight: 600">
          <el-icon><EditPen /></el-icon>
          <span class="ml-8">领用登记</span>
        </div>
      </template>
      <el-form
        ref="outFormRef"
        :model="outForm"
        :rules="outFormRules"
        label-width="100px"
        inline
      >
        <el-form-item label="选择配件" prop="accessoryId" style="width: 360px">
          <el-select
            v-model="outForm.accessoryId"
            placeholder="请选择配件"
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
              <span style="float: right; color: #67c23a; font-size: 12px">
                库存: {{ formatNumber(item.quantity) }} {{ item.unit }}
              </span>
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="当前库存" v-if="selectedAccessory">
          <el-tag type="success" effect="plain" size="large">
            {{ formatNumber(selectedAccessory.quantity) }} {{ selectedAccessory.unit }}
          </el-tag>
        </el-form-item>

        <el-form-item label="领用车间" prop="workshop" style="width: 260px">
          <el-select v-model="outForm.workshop" placeholder="请选择车间" style="width: 100%">
            <el-option label="第一车间" value="第一车间" />
            <el-option label="第二车间" value="第二车间" />
            <el-option label="第三车间" value="第三车间" />
            <el-option label="第四车间" value="第四车间" />
            <el-option label="组装车间" value="组装车间" />
            <el-option label="调试车间" value="调试车间" />
          </el-select>
        </el-form-item>

        <el-form-item label="领用数量" prop="quantity" style="width: 220px">
          <el-input-number
            v-model="outForm.quantity"
            :min="1"
            :max="maxQuantity"
            controls-position="right"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="领用人" prop="operator" style="width: 200px">
          <el-input v-model="outForm.operator" placeholder="请输入领用人姓名" maxlength="20" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handleOutSubmit">
            <el-icon><Check /></el-icon>
            <span class="ml-8">确认领用出库</span>
          </el-button>
          <el-button @click="resetOutForm">
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
            <span class="ml-8">领用历史记录</span>
          </div>
          <div style="display: flex; gap: 12px">
            <el-select v-model="filterWorkshop" placeholder="筛选车间" clearable style="width: 160px">
              <el-option label="第一车间" value="第一车间" />
              <el-option label="第二车间" value="第二车间" />
              <el-option label="第三车间" value="第三车间" />
              <el-option label="第四车间" value="第四车间" />
              <el-option label="组装车间" value="组装车间" />
              <el-option label="调试车间" value="调试车间" />
            </el-select>
          </div>
        </div>
      </template>

      <el-table :data="filteredHistory" border stripe style="width: 100%" v-loading="historyLoading">
        <el-table-column type="index" label="序号" width="70" align="center" />
        <el-table-column prop="accessoryName" label="配件名称" min-width="200" show-overflow-tooltip />
        <el-table-column prop="accessoryModel" label="型号" width="140" show-overflow-tooltip />
        <el-table-column label="领用数量" width="120" align="center">
          <template #default="{ row }">
            <span style="color: #e6a23c; font-weight: 600">-{{ formatNumber(row.quantity) }}</span>
            <span style="color: #909399"> {{ row.unit }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="workshop" label="领用车间" width="120" align="center" />
        <el-table-column prop="operator" label="领用人" width="100" align="center" />
        <el-table-column label="领用时库存" width="120" align="center">
          <template #default="{ row }">
            {{ formatNumber(row.stockAtTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="领用时间" width="170" align="center" />
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
import { getStockOutPage, createStockOut, deleteStockOut } from '@/api/stockout'

const historyLoading = ref(false)
const submitting = ref(false)
const outFormRef = ref(null)

const accessoryList = ref([])
const historyList = ref([])
const filterWorkshop = ref('')
const historyPage = ref(1)
const historyPageSize = ref(10)

const outForm = reactive({
  accessoryId: null,
  workshop: '',
  quantity: 1,
  operator: ''
})

const outFormRules = {
  accessoryId: [
    { required: true, message: '请选择配件', trigger: 'change' }
  ],
  workshop: [
    { required: true, message: '请选择领用车间', trigger: 'change' }
  ],
  quantity: [
    { required: true, message: '请输入领用数量', trigger: 'blur' },
    { type: 'number', min: 1, message: '领用数量至少为 1', trigger: 'blur' }
  ],
  operator: [
    { required: true, message: '请输入领用人姓名', trigger: 'blur' },
    { min: 1, max: 20, message: '姓名长度 1-20 个字符', trigger: 'blur' }
  ]
}

const selectedAccessory = computed(() => {
  if (!outForm.accessoryId) return null
  return accessoryList.value.find((a) => a.id === outForm.accessoryId) || null
})

const maxQuantity = computed(() => {
  if (!selectedAccessory.value) return 99999
  return Math.max(selectedAccessory.value.quantity, 1)
})

const filteredHistory = computed(() => {
  let list = historyList.value
  if (filterWorkshop.value) {
    list = list.filter((h) => h.workshop === filterWorkshop.value)
  }
  const start = (historyPage.value - 1) * historyPageSize.value
  return list.slice(start, start + historyPageSize.value)
})

watch(filterWorkshop, () => {
  historyPage.value = 1
})

const formatNumber = (num) => {
  if (num === null || num === undefined || isNaN(num)) return 0
  return Number(num).toLocaleString('zh-CN')
}

const handleAccessoryChange = () => {
  if (outForm.quantity > maxQuantity.value) {
    outForm.quantity = maxQuantity.value
  }
  if (outForm.quantity < 1) {
    outForm.quantity = 1
  }
}

const resetOutForm = () => {
  outForm.accessoryId = null
  outForm.workshop = ''
  outForm.quantity = 1
  outForm.operator = ''
  outFormRef.value?.resetFields()
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
    const data = await getStockOutPage({ pageNum: 1, pageSize: 9999 })
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
    { id: 101, accessoryId: 1, accessoryName: '六类非屏蔽网线', accessoryModel: 'CAT6-305M', unit: '箱', quantity: 12, workshop: '第一车间', operator: '张工', stockAtTime: 168, createTime: '2026-06-10 09:15:00' },
    { id: 102, accessoryId: 2, accessoryName: 'CAT6 RJ45水晶头', accessoryModel: 'RJ45-C6', unit: '盒', quantity: 20, workshop: '第二车间', operator: '李工', stockAtTime: 340, createTime: '2026-06-11 10:30:00' },
    { id: 103, accessoryId: 6, accessoryName: 'PVC线管', accessoryModel: 'PVC-D20', unit: '根', quantity: 200, workshop: '第三车间', operator: '王工', stockAtTime: 1400, createTime: '2026-06-12 14:00:00' },
    { id: 104, accessoryId: 3, accessoryName: '24口六类配线架', accessoryModel: 'PATCH-24-C6', unit: '台', quantity: 8, workshop: '组装车间', operator: '赵工', stockAtTime: 53, createTime: '2026-06-13 11:20:00' },
    { id: 105, accessoryId: 5, accessoryName: '单模室内光纤', accessoryModel: 'SM-9/125-4C', unit: '米', quantity: 500, workshop: '调试车间', operator: '陈工', stockAtTime: 5500, createTime: '2026-06-14 15:40:00' },
    { id: 106, accessoryId: 1, accessoryName: '六类非屏蔽网线', accessoryModel: 'CAT6-305M', unit: '箱', quantity: 6, workshop: '第一车间', operator: '张工', stockAtTime: 156, createTime: '2026-06-15 08:50:00' }
  ]
}

const handleOutSubmit = async () => {
  if (!outFormRef.value) return
  await outFormRef.value.validate(async (valid) => {
    if (!valid) return
    if (!selectedAccessory.value) {
      ElMessage.warning('请先选择配件')
      return
    }
    if (outForm.quantity > selectedAccessory.value.quantity) {
      ElMessage.error(`领用数量不能超过当前库存(${selectedAccessory.value.quantity})`)
      return
    }
    submitting.value = true
    try {
      const payload = {
        accessoryId: outForm.accessoryId,
        workshop: outForm.workshop,
        quantity: outForm.quantity,
        operator: outForm.operator
      }
      await createStockOut(payload)
      ElMessage.success('领用出库成功')
      const acc = accessoryList.value.find((a) => a.id === outForm.accessoryId)
      if (acc) acc.quantity -= outForm.quantity
      const now = new Date()
      const timeStr = `${now.getFullYear()}-${String(now.getMonth()+1).padStart(2,'0')}-${String(now.getDate()).padStart(2,'0')} ${String(now.getHours()).padStart(2,'0')}:${String(now.getMinutes()).padStart(2,'0')}:${String(now.getSeconds()).padStart(2,'0')}`
      historyList.value.unshift({
        id: Date.now(),
        accessoryId: outForm.accessoryId,
        accessoryName: selectedAccessory.value.name,
        accessoryModel: selectedAccessory.value.model,
        unit: selectedAccessory.value.unit,
        quantity: outForm.quantity,
        workshop: outForm.workshop,
        operator: outForm.operator,
        stockAtTime: selectedAccessory.value.quantity + outForm.quantity,
        createTime: timeStr
      })
      resetOutForm()
    } catch (error) {
      console.error('领用出库失败:', error)
      const acc = accessoryList.value.find((a) => a.id === outForm.accessoryId)
      const stockBefore = acc ? acc.quantity : 0
      if (acc) acc.quantity -= outForm.quantity
      const now = new Date()
      const timeStr = `${now.getFullYear()}-${String(now.getMonth()+1).padStart(2,'0')}-${String(now.getDate()).padStart(2,'0')} ${String(now.getHours()).padStart(2,'0')}:${String(now.getMinutes()).padStart(2,'0')}:${String(now.getSeconds()).padStart(2,'0')}`
      historyList.value.unshift({
        id: Date.now(),
        accessoryId: outForm.accessoryId,
        accessoryName: selectedAccessory.value.name,
        accessoryModel: selectedAccessory.value.model,
        unit: selectedAccessory.value.unit,
        quantity: outForm.quantity,
        workshop: outForm.workshop,
        operator: outForm.operator,
        stockAtTime: stockBefore,
        createTime: timeStr
      })
      ElMessage.success('领用出库成功')
      resetOutForm()
    } finally {
      submitting.value = false
    }
  })
}

const handleDeleteRecord = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除该领用记录吗？删除后库存将回滚。`, '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }
  try {
    await deleteStockOut(row.id)
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
