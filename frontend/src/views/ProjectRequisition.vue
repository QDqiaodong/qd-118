<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-title">
        <el-icon><Connection /></el-icon>
        <span class="ml-8">施工项目领用</span>
      </div>
      <div style="display: flex; gap: 12px">
        <el-button type="primary" @click="handleAddProject">
          <el-icon><Plus /></el-icon>
          <span class="ml-8">新增项目</span>
        </el-button>
        <el-button @click="loadConsumption" :loading="consumptionLoading">
          <el-icon><Refresh /></el-icon>
          <span class="ml-8">刷新汇总</span>
        </el-button>
      </div>
    </div>

    <el-card class="mb-16" shadow="never" style="margin-bottom: 20px; border: 1px solid #ebeef5; border-radius: 8px">
      <template #header>
        <div style="display: flex; align-items: center; font-weight: 600">
          <el-icon><DataLine /></el-icon>
          <span class="ml-8">施工项目管理</span>
        </div>
      </template>
      <el-table :data="projectList" border stripe style="width: 100%" v-loading="projectLoading">
        <el-table-column type="index" label="序号" width="70" align="center" />
        <el-table-column prop="projectNo" label="项目编号" width="180" show-overflow-tooltip />
        <el-table-column prop="projectName" label="项目名称" min-width="200" show-overflow-tooltip />
        <el-table-column prop="factoryArea" label="厂区" width="140" align="center" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status ? 'success' : 'info'" effect="plain" size="small">
              {{ row.status ? '进行中' : '已结束' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="160" show-overflow-tooltip />
        <el-table-column label="操作" width="280" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleViewConsumption(row)">
              <el-icon><DataAnalysis /></el-icon>
              <span class="ml-8">用量</span>
            </el-button>
            <el-button type="primary" link size="small" @click="handleEditProject(row)">
              <el-icon><Edit /></el-icon>
              <span class="ml-8">编辑</span>
            </el-button>
            <el-button :type="row.status ? 'warning' : 'success'" link size="small" @click="handleToggleStatus(row)">
              <el-icon><Switch /></el-icon>
              <span class="ml-8">{{ row.status ? '结束' : '重启' }}</span>
            </el-button>
            <el-button type="danger" link size="small" @click="handleDeleteProject(row)">
              <el-icon><Delete /></el-icon>
              <span class="ml-8">删除</span>
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card shadow="never" style="border: 1px solid #ebeef5; border-radius: 8px">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <div style="font-weight: 600">
            <el-icon><TrendCharts /></el-icon>
            <span class="ml-8">项目用量汇总</span>
          </div>
          <el-select v-model="filterProjectId" placeholder="筛选项目" clearable style="width: 260px">
            <el-option
              v-for="item in projectList"
              :key="item.id"
              :value="item.id"
              :label="`${item.projectNo} - ${item.projectName}`"
            />
          </el-select>
        </div>
      </template>

      <el-table :data="filteredConsumption" border stripe style="width: 100%" v-loading="consumptionLoading">
        <el-table-column type="index" label="序号" width="70" align="center" />
        <el-table-column prop="projectNo" label="项目编号" width="160" show-overflow-tooltip />
        <el-table-column prop="projectName" label="项目名称" width="180" show-overflow-tooltip />
        <el-table-column prop="factoryArea" label="厂区" width="120" align="center" />
        <el-table-column prop="accessoryName" label="配件名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="workshop" label="领用车间" width="120" align="center" />
        <el-table-column prop="purpose" label="用途" min-width="160" show-overflow-tooltip />
        <el-table-column label="消耗总量" width="120" align="center">
          <template #default="{ row }">
            <span style="color: #e6a23c; font-weight: 600">{{ formatNumber(row.totalQuantity) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="recordCount" label="领用次数" width="110" align="center" />
      </el-table>

      <el-pagination
        class="mt-16"
        background
        layout="total, sizes, prev, pager, next, jumper"
        :total="filteredConsumptionList.length"
        :page-sizes="[10, 20, 50, 100]"
        v-model:current-page="page"
        v-model:page-size="pageSize"
      />
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="560px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="项目编号" prop="projectNo">
          <el-input v-model="formData.projectNo" placeholder="请输入施工项目编号" maxlength="64" />
        </el-form-item>
        <el-form-item label="项目名称" prop="projectName">
          <el-input v-model="formData.projectName" placeholder="请输入施工项目名称" maxlength="200" />
        </el-form-item>
        <el-form-item label="厂区" prop="factoryArea">
          <el-select v-model="formData.factoryArea" placeholder="请选择厂区" style="width: 100%">
            <el-option label="一厂区" value="一厂区" />
            <el-option label="二厂区" value="二厂区" />
            <el-option label="三厂区" value="三厂区" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="formData.status" active-text="进行中" inactive-text="已结束" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="formData.remark" type="textarea" :rows="3" maxlength="500" show-word-limit placeholder="请输入备注信息" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="form-dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getConstructionProjectList,
  addConstructionProject,
  updateConstructionProject,
  deleteConstructionProject,
  updateConstructionProjectStatus,
  getAllProjectConsumption,
  getProjectConsumption
} from '@/api/constructionProject'

const projectLoading = ref(false)
const consumptionLoading = ref(false)
const projectList = ref([])
const consumptionList = ref([])
const filterProjectId = ref('')
const page = ref(1)
const pageSize = ref(10)

const dialogVisible = ref(false)
const dialogTitle = ref('新增施工项目')
const submitting = ref(false)
const formRef = ref(null)
const isEdit = ref(false)

const formData = reactive({
  id: null,
  projectNo: '',
  projectName: '',
  factoryArea: '',
  status: true,
  remark: ''
})

const formRules = {
  projectNo: [
    { required: true, message: '请输入项目编号', trigger: 'blur' },
    { max: 64, message: '项目编号长度不能超过64个字符', trigger: 'blur' }
  ],
  projectName: [
    { required: true, message: '请输入项目名称', trigger: 'blur' },
    { max: 200, message: '项目名称长度不能超过200个字符', trigger: 'blur' }
  ]
}

const filteredConsumptionList = computed(() => {
  let list = consumptionList.value
  if (filterProjectId.value) {
    list = list.filter((c) => c.projectId === filterProjectId.value)
  }
  return list
})

const filteredConsumption = computed(() => {
  const list = filteredConsumptionList.value
  const start = (page.value - 1) * pageSize.value
  return list.slice(start, start + pageSize.value)
})

watch(filterProjectId, () => {
  page.value = 1
})

const formatNumber = (num) => {
  if (num === null || num === undefined || isNaN(num)) return 0
  return Number(num).toLocaleString('zh-CN')
}

const resetForm = () => {
  formData.id = null
  formData.projectNo = ''
  formData.projectName = ''
  formData.factoryArea = ''
  formData.status = true
  formData.remark = ''
  isEdit.value = false
}

const loadProjectList = async () => {
  projectLoading.value = true
  try {
    const data = await getConstructionProjectList()
    if (data && Array.isArray(data)) {
      projectList.value = data
    }
  } catch (error) {
    console.error('加载施工项目列表失败:', error)
    ElMessage.error('加载施工项目列表失败')
  } finally {
    projectLoading.value = false
  }
}

const loadConsumption = async () => {
  consumptionLoading.value = true
  try {
    const data = await getAllProjectConsumption()
    if (data && Array.isArray(data)) {
      consumptionList.value = data
    }
  } catch (error) {
    console.error('加载项目用量汇总失败:', error)
    ElMessage.error('加载项目用量汇总失败')
  } finally {
    consumptionLoading.value = false
  }
}

const handleAddProject = () => {
  resetForm()
  dialogTitle.value = '新增施工项目'
  dialogVisible.value = true
}

const handleEditProject = (row) => {
  resetForm()
  isEdit.value = true
  dialogTitle.value = '编辑施工项目'
  formData.id = row.id
  formData.projectNo = row.projectNo
  formData.projectName = row.projectName
  formData.factoryArea = row.factoryArea || ''
  formData.status = row.status
  formData.remark = row.remark || ''
  dialogVisible.value = true
}

const handleToggleStatus = async (row) => {
  const newStatus = !row.status
  const action = newStatus ? '重启' : '结束'
  try {
    await ElMessageBox.confirm(`确定要${action}项目【${row.projectName}】吗？`, '操作确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }
  try {
    await updateConstructionProjectStatus(row.id, newStatus)
    ElMessage.success(`${action}成功`)
    loadProjectList()
  } catch (error) {
    console.error(`${action}失败:`, error)
    ElMessage.error(`${action}失败，请稍后重试`)
  }
}

const handleDeleteProject = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除项目【${row.projectName}】吗？`, '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }
  try {
    await deleteConstructionProject(row.id)
    ElMessage.success('删除成功')
    loadProjectList()
    loadConsumption()
  } catch (error) {
    console.error('删除失败:', error)
    ElMessage.error('删除失败，请稍后重试')
  }
}

const handleViewConsumption = async (row) => {
  filterProjectId.value = row.id
  consumptionLoading.value = true
  try {
    const data = await getProjectConsumption(row.id)
    if (data && Array.isArray(data)) {
      const otherData = consumptionList.value.filter((c) => c.projectId !== row.id)
      consumptionList.value = [...otherData, ...data]
    }
  } catch (error) {
    console.error('加载项目用量失败:', error)
    ElMessage.error('加载项目用量失败')
  } finally {
    consumptionLoading.value = false
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      const payload = {
        projectNo: formData.projectNo,
        projectName: formData.projectName,
        factoryArea: formData.factoryArea,
        status: formData.status,
        remark: formData.remark
      }
      if (isEdit.value) {
        payload.id = formData.id
        await updateConstructionProject(payload)
      } else {
        await addConstructionProject(payload)
      }
      ElMessage.success(isEdit.value ? '修改成功' : '新增成功')
      dialogVisible.value = false
      loadProjectList()
      loadConsumption()
    } catch (error) {
      console.error('提交失败:', error)
      ElMessage.error(isEdit.value ? '修改失败，请稍后重试' : '新增失败，请稍后重试')
    } finally {
      submitting.value = false
    }
  })
}

onMounted(() => {
  loadProjectList()
  loadConsumption()
})
</script>
