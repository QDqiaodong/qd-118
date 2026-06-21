<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-title">
        <el-icon><SetUp /></el-icon>
        <span class="ml-8">车间领用用途管理</span>
      </div>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        <span class="ml-8">新增用途</span>
      </el-button>
    </div>

    <el-card shadow="never" style="border: 1px solid #ebeef5; border-radius: 8px">
      <el-table :data="tableData" border stripe style="width: 100%" v-loading="loading">
        <el-table-column type="index" label="序号" width="70" align="center" />
        <el-table-column prop="name" label="用途名称" min-width="160" show-overflow-tooltip />
        <el-table-column prop="code" label="用途编码" width="200" show-overflow-tooltip />
        <el-table-column prop="sortOrder" label="排序号" width="100" align="center" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'info'" effect="plain" size="small">
              {{ row.enabled ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="200" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="170" align="center" />
        <el-table-column label="操作" width="260" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">
              <el-icon><Edit /></el-icon>
              <span class="ml-8">编辑</span>
            </el-button>
            <el-button :type="row.enabled ? 'warning' : 'success'" link size="small" @click="handleToggleStatus(row)">
              <el-icon><Switch /></el-icon>
              <span class="ml-8">{{ row.enabled ? '停用' : '启用' }}</span>
            </el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">
              <el-icon><Delete /></el-icon>
              <span class="ml-8">删除</span>
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="90px"
      >
        <el-form-item label="用途名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入用途名称" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="用途编码" prop="code">
          <el-input v-model="formData.code" placeholder="请输入用途编码（如：NETWORK）" maxlength="32" />
        </el-form-item>
        <el-form-item label="排序号" prop="sortOrder">
          <el-input-number v-model="formData.sortOrder" :min="0" :max="9999" controls-position="right" style="width: 100%" />
        </el-form-item>
        <el-form-item label="是否启用">
          <el-switch v-model="formData.enabled" active-text="启用" inactive-text="停用" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="formData.remark" type="textarea" :rows="3" maxlength="500" show-word-limit placeholder="请输入备注信息（可选）" />
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getWorkshopUsageList,
  addWorkshopUsage,
  updateWorkshopUsage,
  deleteWorkshopUsage,
  updateWorkshopUsageStatus
} from '@/api/workshopUsage'

const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增用途')
const submitting = ref(false)
const formRef = ref(null)
const isEdit = ref(false)

const formData = reactive({
  id: null,
  name: '',
  code: '',
  sortOrder: 0,
  enabled: true,
  remark: ''
})

const formRules = {
  name: [
    { required: true, message: '请输入用途名称', trigger: 'blur' },
    { min: 1, max: 100, message: '用途名称长度在 1 到 100 个字符', trigger: 'blur' }
  ],
  code: [
    { max: 32, message: '用途编码长度不能超过 32 个字符', trigger: 'blur' },
    { pattern: /^[A-Za-z0-9_-]*$/, message: '编码只能包含字母、数字、下划线和中划线', trigger: 'blur' }
  ],
  sortOrder: [
    { required: true, message: '请输入排序号', trigger: 'blur' },
    { type: 'number', min: 0, max: 9999, message: '排序号范围 0-9999', trigger: 'blur' }
  ]
}

const resetForm = () => {
  formData.id = null
  formData.name = ''
  formData.code = ''
  formData.sortOrder = 0
  formData.enabled = true
  formData.remark = ''
  isEdit.value = false
}

const loadList = async () => {
  loading.value = true
  try {
    const data = await getWorkshopUsageList()
    if (data && Array.isArray(data)) {
      tableData.value = data
    }
  } catch (error) {
    console.error('加载领用用途列表失败:', error)
    ElMessage.error('加载失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  resetForm()
  dialogTitle.value = '新增用途'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  resetForm()
  isEdit.value = true
  dialogTitle.value = '编辑用途'
  formData.id = row.id
  formData.name = row.name
  formData.code = row.code || ''
  formData.sortOrder = row.sortOrder
  formData.enabled = row.enabled
  formData.remark = row.remark || ''
  dialogVisible.value = true
}

const handleToggleStatus = async (row) => {
  const newStatus = !row.enabled
  const action = newStatus ? '启用' : '停用'
  try {
    await ElMessageBox.confirm(`确定要${action}用途【${row.name}】吗？`, '操作确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }
  try {
    await updateWorkshopUsageStatus(row.id, newStatus)
    ElMessage.success(`${action}成功`)
    loadList()
  } catch (error) {
    console.error(`${action}失败:`, error)
    ElMessage.error(`${action}失败，请稍后重试`)
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除用途【${row.name}】吗？`, '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }
  try {
    await deleteWorkshopUsage(row.id)
    ElMessage.success('删除成功')
    loadList()
  } catch (error) {
    console.error('删除失败:', error)
    ElMessage.error('删除失败，请稍后重试')
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      const payload = {
        name: formData.name,
        code: formData.code,
        sortOrder: formData.sortOrder,
        enabled: formData.enabled,
        remark: formData.remark
      }
      if (isEdit.value) {
        payload.id = formData.id
        await updateWorkshopUsage(payload)
      } else {
        await addWorkshopUsage(payload)
      }
      ElMessage.success(isEdit.value ? '修改成功' : '新增成功')
      dialogVisible.value = false
      loadList()
    } catch (error) {
      console.error('提交失败:', error)
      ElMessage.error(isEdit.value ? '修改失败，请稍后重试' : '新增失败，请稍后重试')
    } finally {
      submitting.value = false
    }
  })
}

onMounted(() => {
  loadList()
})
</script>
