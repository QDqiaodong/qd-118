<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-title">
        <el-icon><Scissor /></el-icon>
        <span class="ml-8">线槽裁切余料管理</span>
      </div>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        <span class="ml-8">登记余料</span>
      </el-button>
    </div>

    <div class="table-toolbar">
      <el-select v-model="filterStatus" placeholder="筛选状态" clearable style="width: 160px">
        <el-option label="待回库" :value="0" />
        <el-option label="已回库" :value="1" />
      </el-select>
      <el-input v-model="searchKeyword" placeholder="搜索线槽名称、型号" clearable style="width: 240px" :prefix-icon="Search" />
      <div class="spacer"></div>
      <el-button @click="handleSearch">
        <el-icon><Search /></el-icon>
        <span class="ml-8">查询</span>
      </el-button>
      <el-button @click="handleReset">
        <el-icon><Refresh /></el-icon>
        <span class="ml-8">重置</span>
      </el-button>
    </div>

    <div class="stat-cards" v-if="statCards.length">
      <div class="stat-card" v-for="card in statCards" :key="card.label">
        <div class="stat-icon" :style="{ background: card.color }">
          <el-icon :size="28"><component :is="card.icon" /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-label">{{ card.label }}</div>
          <div class="stat-value">{{ card.value }}</div>
        </div>
      </div>
    </div>

    <el-table :data="pagedList" border stripe style="width: 100%" v-loading="loading">
      <el-table-column type="index" label="序号" width="70" align="center" />
      <el-table-column prop="accessoryName" label="线槽名称" min-width="180" show-overflow-tooltip />
      <el-table-column prop="accessoryModel" label="型号" width="140" show-overflow-tooltip />
      <el-table-column label="领用长度" width="110" align="center">
        <template #default="{ row }">
          {{ row.originalLength }} 米
        </template>
      </el-table-column>
      <el-table-column label="已使用" width="100" align="center">
        <template #default="{ row }">
          {{ row.usedLength }} 米
        </template>
      </el-table-column>
      <el-table-column label="余料长度" width="110" align="center">
        <template #default="{ row }">
          <span style="color: #e6a23c; font-weight: 600">{{ row.offcutLength }} 米</span>
        </template>
      </el-table-column>
      <el-table-column prop="returnZone" label="回库货位" width="120" align="center">
        <template #default="{ row }">
          <el-tag v-if="row.returnZone" size="small" :type="zoneTagType(row.returnZone)">{{ row.returnZone }}</el-tag>
          <span v-else style="color: #c0c4cc">-</span>
        </template>
      </el-table-column>
      <el-table-column prop="operator" label="操作人" width="100" align="center" />
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'warning'" size="small">
            {{ row.status === 1 ? '已回库' : '待回库' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" min-width="160" show-overflow-tooltip />
      <el-table-column label="操作" width="200" align="center" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 0" type="success" link @click="handleReturn(row)">
            <el-icon><RefreshLeft /></el-icon>
            <span class="ml-8">回库</span>
          </el-button>
          <el-button type="danger" link @click="handleDelete(row)">
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
      :total="filteredList.length"
      :page-sizes="[10, 20, 50, 100]"
      v-model:current-page="currentPage"
      v-model:page-size="pageSize"
    />

    <el-dialog
      v-model="dialogVisible"
      title="登记余料"
      width="560px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item label="选择线槽" prop="accessoryId">
          <el-select
            v-model="formData.accessoryId"
            placeholder="请选择线槽配件"
            filterable
            style="width: 100%"
            @change="handleAccessoryChange"
          >
            <el-option
              v-for="item in ductAccessoryList"
              :key="item.id"
              :value="item.id"
              :label="`${item.name} - ${item.model || '-'}`"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="关联出库">
          <el-select
            v-model="formData.stockOutId"
            placeholder="选择出库记录（可选）"
            clearable
            style="width: 100%"
          >
            <el-option
              v-for="item in relatedStockOuts"
              :key="item.id"
              :value="item.id"
              :label="`${item.outTime || ''} - ${item.quantity}${item.unit || '米'} - ${item.operator || ''}`"
            />
          </el-select>
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="领用长度" prop="originalLength">
              <el-input-number v-model="formData.originalLength" :min="0" :precision="2" :step="0.5" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="已使用" prop="usedLength">
              <el-input-number v-model="formData.usedLength" :min="0" :precision="2" :step="0.5" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="余料长度" prop="offcutLength">
              <el-input-number v-model="formData.offcutLength" :min="0" :precision="2" :step="0.5" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="回库货位">
          <el-select v-model="formData.returnZone" placeholder="选择回库货位" clearable style="width: 100%">
            <el-option label="A区-01" value="A区-01" />
            <el-option label="A区-02" value="A区-02" />
            <el-option label="B区-01" value="B区-01" />
            <el-option label="B区-02" value="B区-02" />
            <el-option label="B区-03" value="B区-03" />
            <el-option label="C区-01" value="C区-01" />
            <el-option label="D区-01" value="D区-01" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作人" prop="operator">
          <el-input v-model="formData.operator" placeholder="请输入操作人姓名" maxlength="20" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="formData.remark" type="textarea" :rows="3" maxlength="500" show-word-limit placeholder="备注信息（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="form-dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog
      v-model="returnDialogVisible"
      title="余料回库确认"
      width="480px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-form ref="returnFormRef" :model="returnFormData" label-width="100px">
        <el-alert
          :title="`将回补 ${selectedOffcut?.offcutLength || 0} 米至 ${selectedOffcut?.accessoryName || ''} 库存`"
          type="success"
          :closable="false"
          show-icon
          style="margin-bottom: 16px"
        />
        <el-form-item label="回库货位">
          <el-select v-model="returnFormData.returnZone" placeholder="选择回库货位" clearable style="width: 100%">
            <el-option label="A区-01" value="A区-01" />
            <el-option label="A区-02" value="A区-02" />
            <el-option label="B区-01" value="B区-01" />
            <el-option label="B区-02" value="B区-02" />
            <el-option label="B区-03" value="B区-03" />
            <el-option label="C区-01" value="C区-01" />
            <el-option label="D区-01" value="D区-01" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作人">
          <el-input v-model="returnFormData.operator" placeholder="请输入操作人姓名" maxlength="20" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="returnFormData.remark" type="textarea" :rows="2" maxlength="500" placeholder="备注（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="form-dialog-footer">
          <el-button @click="returnDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="returnSubmitting" @click="handleReturnSubmit">确认回库</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import {
  getDuctOffcutList,
  createDuctOffcut,
  returnDuctOffcut,
  deleteDuctOffcut
} from '@/api/ductOffcut'
import { getAccessoryList } from '@/api/accessory'
import { getStockOutList } from '@/api/stockout'

const loading = ref(false)
const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref(null)

const returnDialogVisible = ref(false)
const returnSubmitting = ref(false)
const returnFormRef = ref(null)
const selectedOffcut = ref(null)

const searchKeyword = ref('')
const filterStatus = ref(null)
const currentPage = ref(1)
const pageSize = ref(10)

const dataList = ref([])
const ductAccessoryList = ref([])
const relatedStockOuts = ref([])

const formData = reactive({
  accessoryId: null,
  stockOutId: null,
  originalLength: 0,
  usedLength: 0,
  offcutLength: 0,
  returnZone: '',
  operator: '',
  remark: ''
})

const returnFormData = reactive({
  id: null,
  returnZone: '',
  operator: '',
  remark: ''
})

const formRules = {
  accessoryId: [
    { required: true, message: '请选择线槽配件', trigger: 'change' }
  ],
  originalLength: [
    { required: true, message: '请输入领用长度', trigger: 'blur' }
  ],
  usedLength: [
    { required: true, message: '请输入已使用长度', trigger: 'blur' }
  ],
  offcutLength: [
    { required: true, message: '请输入余料长度', trigger: 'blur' }
  ],
  operator: [
    { required: true, message: '请输入操作人', trigger: 'blur' }
  ]
}

const zoneTagType = (zone) => {
  if (!zone) return 'info'
  const map = { A: 'primary', B: 'success', C: 'warning', D: 'danger' }
  return map[zone.charAt(0)] || 'info'
}

const statCards = computed(() => {
  const total = dataList.value.length
  const pending = dataList.value.filter(d => d.status === 0).length
  const returned = dataList.value.filter(d => d.status === 1).length
  const totalOffcut = dataList.value
    .filter(d => d.status === 0)
    .reduce((sum, d) => sum + (parseFloat(d.offcutLength) || 0), 0)
  return [
    { label: '余料记录', value: total, color: 'linear-gradient(135deg, #409eff, #66b1ff)', icon: 'Document' },
    { label: '待回库', value: pending, color: 'linear-gradient(135deg, #e6a23c, #f0c78a)', icon: 'Clock' },
    { label: '已回库', value: returned, color: 'linear-gradient(135deg, #67c23a, #95d475)', icon: 'Check' },
    { label: '待回库米数', value: totalOffcut.toFixed(1) + ' 米', color: 'linear-gradient(135deg, #f56c6c, #fab6b6)', icon: 'DataLine' }
  ]
})

const filteredList = computed(() => {
  return dataList.value.filter((item) => {
    let ok = true
    if (filterStatus.value !== null && filterStatus.value !== '') {
      ok = ok && item.status === filterStatus.value
    }
    if (searchKeyword.value) {
      const kw = searchKeyword.value.toLowerCase()
      ok = ok && (
        item.accessoryName?.toLowerCase().includes(kw) ||
        item.accessoryModel?.toLowerCase().includes(kw)
      )
    }
    return ok
  })
})

const pagedList = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredList.value.slice(start, start + pageSize.value)
})

const handleSearch = () => {
  currentPage.value = 1
}

const handleReset = () => {
  searchKeyword.value = ''
  filterStatus.value = null
  currentPage.value = 1
}

const loadData = async () => {
  loading.value = true
  try {
    const data = await getDuctOffcutList()
    if (data && Array.isArray(data)) {
      dataList.value = data
    }
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载余料数据失败')
  } finally {
    loading.value = false
  }
}

const loadDuctAccessories = async () => {
  try {
    const data = await getAccessoryList({ pageNum: 1, pageSize: 9999 })
    if (data) {
      const list = data.records || data.list || data || []
      ductAccessoryList.value = list.filter(item => {
        const path = item.categoryPath
        if (Array.isArray(path)) return path.some(p => p.includes('线槽'))
        if (typeof path === 'string') return path.includes('线槽')
        return item.categoryName?.includes('线槽') || item.category?.includes('线槽')
      })
    }
  } catch (error) {
    console.error('加载线槽配件失败:', error)
  }
}

const handleAccessoryChange = async (accessoryId) => {
  relatedStockOuts.value = []
  if (!accessoryId) return
  try {
    const data = await getStockOutList({ pageNum: 1, pageSize: 100 })
    if (data) {
      const list = data.records || data.list || data || []
      relatedStockOuts.value = list.filter(s => s.accessoryId === accessoryId)
    }
  } catch (error) {
    console.error('加载出库记录失败:', error)
  }
}

const handleAdd = () => {
  formData.accessoryId = null
  formData.stockOutId = null
  formData.originalLength = 0
  formData.usedLength = 0
  formData.offcutLength = 0
  formData.returnZone = ''
  formData.operator = ''
  formData.remark = ''
  relatedStockOuts.value = []
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      const payload = {
        accessoryId: formData.accessoryId,
        stockOutId: formData.stockOutId,
        originalLength: formData.originalLength,
        usedLength: formData.usedLength,
        offcutLength: formData.offcutLength,
        returnZone: formData.returnZone,
        operator: formData.operator,
        remark: formData.remark
      }
      await createDuctOffcut(payload)
      ElMessage.success('余料登记成功')
      dialogVisible.value = false
      loadData()
    } catch (error) {
      console.error('登记失败:', error)
      ElMessage.error('余料登记失败，请稍后重试')
    } finally {
      submitting.value = false
    }
  })
}

const handleReturn = (row) => {
  selectedOffcut.value = row
  returnFormData.id = row.id
  returnFormData.returnZone = row.returnZone || ''
  returnFormData.operator = ''
  returnFormData.remark = ''
  returnDialogVisible.value = true
}

const handleReturnSubmit = async () => {
  returnSubmitting.value = true
  try {
    await returnDuctOffcut({
      id: returnFormData.id,
      returnZone: returnFormData.returnZone,
      operator: returnFormData.operator,
      remark: returnFormData.remark
    })
    ElMessage.success('余料回库成功，库存已回补')
    returnDialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('回库失败:', error)
    ElMessage.error('余料回库失败，请稍后重试')
  } finally {
    returnSubmitting.value = false
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除该余料记录吗？`, '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }
  try {
    await deleteDuctOffcut(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    console.error('删除失败:', error)
    ElMessage.error('删除失败，请稍后重试')
  }
}

onMounted(() => {
  loadData()
  loadDuctAccessories()
})
</script>
