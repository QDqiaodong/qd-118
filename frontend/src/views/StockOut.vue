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

        <el-form-item label="库区" v-if="selectedAccessory">
          <el-tag type="primary" effect="plain" size="large">
            {{ selectedAccessory.zone || '-' }}
          </el-tag>
        </el-form-item>

        <el-form-item label="当前库存" v-if="selectedAccessory">
          <el-tag type="success" effect="plain" size="large">
            {{ formatNumber(selectedAccessory.quantity) }} {{ selectedAccessory.unit }}
          </el-tag>
        </el-form-item>

        <el-form-item label="单位" v-if="selectedAccessory">
          <el-tag type="info" effect="plain" size="large">
            {{ selectedAccessory.unit || '-' }}
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

        <el-form-item label="领用数量" prop="quantity" style="width: 220px" :error="quantityError">
          <el-input-number
            ref="quantityInputRef"
            v-model="outForm.quantity"
            :min="1"
            :max="maxQuantity"
            controls-position="right"
            style="width: 100%"
            :class="{ 'is-error': isQuantityOverStock }"
            @change="handleQuantityChange"
          />
        </el-form-item>

        <el-form-item label="出库后余量" v-if="selectedAccessory">
          <el-tag :type="remainingStock >= 0 ? 'success' : 'danger'" effect="plain" size="large">
            {{ formatNumber(remainingStock) }} {{ selectedAccessory.unit }}
          </el-tag>
          <span v-if="isQuantityOverStock" style="color: #f56c6c; margin-left: 8px; font-size: 12px">
            库存不足
          </span>
        </el-form-item>

        <el-form-item label="领用人" prop="operator" style="width: 200px">
          <el-input v-model="outForm.operator" placeholder="请输入领用人姓名" maxlength="20" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="submitting" :disabled="isQuantityOverStock" @click="handleOutSubmit">
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
        :total="filteredHistoryList.length"
        :page-sizes="[10, 20, 50, 100]"
        v-model:current-page="historyPage"
        v-model:page-size="historyPageSize"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAccessoryList } from '@/api/accessory'
import { getStockOutPage, createStockOut, deleteStockOut } from '@/api/stockout'

const historyLoading = ref(false)
const submitting = ref(false)
const outFormRef = ref(null)
const quantityInputRef = ref(null)

const accessoryList = ref([])
const historyList = ref([])
const filterWorkshop = ref('')
const historyPage = ref(1)
const historyPageSize = ref(10)
const quantityError = ref('')

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

const mapAccessoryFields = (item) => ({
  ...item,
  quantity: item.stockQuantity ?? item.quantity,
  zone: item.warehouseZone ?? item.zone
})

const selectedAccessory = computed(() => {
  if (!outForm.accessoryId) return null
  return accessoryList.value.find((a) => a.id === outForm.accessoryId) || null
})

const maxQuantity = computed(() => {
  if (!selectedAccessory.value) return 99999
  return Math.max(selectedAccessory.value.quantity, 1)
})

const remainingStock = computed(() => {
  if (!selectedAccessory.value) return 0
  return selectedAccessory.value.quantity - outForm.quantity
})

const isQuantityOverStock = computed(() => {
  if (!selectedAccessory.value) return false
  return outForm.quantity > selectedAccessory.value.quantity
})

const filteredHistoryList = computed(() => {
  let list = historyList.value
  if (filterWorkshop.value) {
    list = list.filter((h) => h.workshop === filterWorkshop.value)
  }
  return list
})

const filteredHistory = computed(() => {
  const list = filteredHistoryList.value
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

const focusQuantityInput = async () => {
  await nextTick()
  if (quantityInputRef.value) {
    const input = quantityInputRef.value.$el.querySelector('input')
    if (input) {
      input.focus()
      input.select()
    }
  }
}

const validateQuantity = () => {
  if (selectedAccessory.value && outForm.quantity > selectedAccessory.value.quantity) {
    quantityError.value = `领用数量不能超过当前库存(${selectedAccessory.value.quantity})`
    return false
  }
  quantityError.value = ''
  return true
}

const handleQuantityChange = () => {
  validateQuantity()
}

const handleAccessoryChange = () => {
  if (outForm.quantity > maxQuantity.value) {
    outForm.quantity = maxQuantity.value
  }
  if (outForm.quantity < 1) {
    outForm.quantity = 1
  }
  validateQuantity()
  if (isQuantityOverStock.value) {
    focusQuantityInput()
  }
}

const resetOutForm = () => {
  outForm.accessoryId = null
  outForm.workshop = ''
  outForm.quantity = 1
  outForm.operator = ''
  quantityError.value = ''
  outFormRef.value?.resetFields()
}

const loadAccessoryList = async () => {
  try {
    const data = await getAccessoryList({ pageNum: 1, pageSize: 9999 })
    if (data) {
      const list = data.records || data.list || data || []
      accessoryList.value = list.map(mapAccessoryFields)
    }
  } catch (error) {
    console.error('加载配件列表失败:', error)
    ElMessage.error('加载配件列表失败，请稍后重试')
  }
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
    ElMessage.error('加载领用记录失败，请稍后重试')
  } finally {
    historyLoading.value = false
  }
}

const handleOutSubmit = async () => {
  if (!outFormRef.value) return
  
  if (!validateQuantity()) {
    ElMessage.error(`领用数量不能超过当前库存(${selectedAccessory.value?.quantity || 0})`)
    focusQuantityInput()
    return
  }
  
  await outFormRef.value.validate(async (valid) => {
    if (!valid) return
    if (!selectedAccessory.value) {
      ElMessage.warning('请先选择配件')
      return
    }
    if (isQuantityOverStock.value) {
      ElMessage.error(`领用数量不能超过当前库存(${selectedAccessory.value.quantity})`)
      focusQuantityInput()
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
      loadAccessoryList()
      loadHistory()
      resetOutForm()
    } catch (error) {
      console.error('领用出库失败:', error)
      ElMessage.error('领用出库失败，请稍后重试')
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
    loadAccessoryList()
    loadHistory()
  } catch (error) {
    console.error('删除失败:', error)
    ElMessage.error('删除失败，请稍后重试')
  }
}

onMounted(() => {
  loadAccessoryList()
  loadHistory()
})
</script>
