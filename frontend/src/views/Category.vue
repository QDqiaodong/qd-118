<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-title">
        <el-icon><Menu /></el-icon>
        <span class="ml-8">配件分类管理</span>
      </div>
      <el-button type="primary" @click="handleAddRoot">
        <el-icon><Plus /></el-icon>
        <span class="ml-8">新增根分类</span>
      </el-button>
    </div>

    <el-table :data="treeData" row-key="id" border default-expand-all style="width: 100%" :tree-props="{ children: 'children' }">
      <el-table-column prop="name" label="分类名称" min-width="200" />
      <el-table-column prop="code" label="分类编码" width="180" />
      <el-table-column prop="sortOrder" label="排序号" width="120" align="center" />
      <el-table-column label="操作" width="280" align="center" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="handleAddChild(row)">
            <el-icon><Plus /></el-icon>
            <span class="ml-8">新增子级</span>
          </el-button>
          <el-button type="warning" link @click="handleEdit(row)">
            <el-icon><Edit /></el-icon>
            <span class="ml-8">编辑</span>
          </el-button>
          <el-button type="danger" link @click="handleDelete(row)">
            <el-icon><Delete /></el-icon>
            <span class="ml-8">删除</span>
          </el-button>
        </template>
      </el-table-column>
    </el-table>

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
        <el-form-item label="父级分类" v-if="formData.parentId">
          <el-input :value="parentName" disabled />
        </el-form-item>
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入分类名称" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="分类编码" prop="code">
          <el-input v-model="formData.code" placeholder="请输入分类编码（如：CAT6）" maxlength="32" />
        </el-form-item>
        <el-form-item label="排序号" prop="sortOrder">
          <el-input-number v-model="formData.sortOrder" :min="0" :max="9999" controls-position="right" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="formData.remark" type="textarea" :rows="3" maxlength="200" show-word-limit placeholder="请输入备注信息（可选）" />
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
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCategoryTree, addCategory, updateCategory, deleteCategory } from '@/api/category'

const treeData = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增分类')
const submitting = ref(false)
const formRef = ref(null)
const parentName = ref('')
const isEdit = ref(false)

const formData = reactive({
  id: null,
  parentId: null,
  name: '',
  code: '',
  sortOrder: 0,
  remark: ''
})

const formRules = {
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { min: 1, max: 50, message: '分类名称长度在 1 到 50 个字符', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入分类编码', trigger: 'blur' },
    { min: 1, max: 32, message: '分类编码长度在 1 到 32 个字符', trigger: 'blur' },
    { pattern: /^[A-Za-z0-9_-]+$/, message: '编码只能包含字母、数字、下划线和中划线', trigger: 'blur' }
  ],
  sortOrder: [
    { required: true, message: '请输入排序号', trigger: 'blur' },
    { type: 'number', min: 0, max: 9999, message: '排序号范围 0-9999', trigger: 'blur' }
  ]
}

const resetForm = () => {
  formData.id = null
  formData.parentId = null
  formData.name = ''
  formData.code = ''
  formData.sortOrder = 0
  formData.remark = ''
  parentName.value = ''
  isEdit.value = false
}

const findNodeById = (list, id, path = []) => {
  for (const node of list) {
    const newPath = [...path, node]
    if (node.id === id) return { node, path: newPath }
    if (node.children && node.children.length > 0) {
      const result = findNodeById(node.children, id, newPath)
      if (result) return result
    }
  }
  return null
}

const loadTree = async () => {
  try {
    const data = await getCategoryTree()
    if (data && Array.isArray(data)) {
      treeData.value = data
    }
  } catch (error) {
    console.error('加载分类树失败:', error)
    ElMessage.error('加载分类失败，请稍后重试')
  }
}

const handleAddRoot = () => {
  resetForm()
  dialogTitle.value = '新增根分类'
  formData.parentId = 0
  parentName.value = '根分类'
  dialogVisible.value = true
}

const handleAddChild = (row) => {
  resetForm()
  dialogTitle.value = `新增【${row.name}】子分类`
  formData.parentId = row.id
  parentName.value = row.name
  dialogVisible.value = true
}

const handleEdit = (row) => {
  resetForm()
  isEdit.value = true
  dialogTitle.value = '编辑分类'
  formData.id = row.id
  formData.parentId = row.parentId
  formData.name = row.name
  formData.code = row.code
  formData.sortOrder = row.sortOrder
  formData.remark = row.remark || ''
  if (row.parentId && row.parentId !== 0) {
    const result = findNodeById(treeData.value, row.parentId)
    parentName.value = result ? result.node.name : '根分类'
  } else {
    parentName.value = '根分类'
  }
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除分类【${row.name}】吗？\n\n注意：存在子分类或绑定配件的分类无法删除。`,
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
    await deleteCategory(row.id)
    ElMessage.success('删除成功')
    loadTree()
  } catch (error) {
    console.error('删除失败:', error)
    const errorMsg = error?.response?.data?.message || '删除失败，请稍后重试'
    ElMessage.error(errorMsg)
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      const payload = {
        parentId: formData.parentId,
        name: formData.name,
        code: formData.code,
        sortOrder: formData.sortOrder,
        remark: formData.remark
      }
      if (isEdit.value) {
        payload.id = formData.id
        await updateCategory(payload)
      } else {
        await addCategory(payload)
      }
      ElMessage.success(isEdit.value ? '修改成功' : '新增成功')
      dialogVisible.value = false
      loadTree()
    } catch (error) {
      console.error('提交失败:', error)
      ElMessage.error(isEdit.value ? '修改失败，请稍后重试' : '新增失败，请稍后重试')
    } finally {
      submitting.value = false
    }
  })
}

onMounted(() => {
  loadTree()
})
</script>
