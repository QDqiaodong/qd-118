<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-title">
        <el-icon><Connection /></el-icon>
        <span class="ml-8">兼容型号库</span>
      </div>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        <span class="ml-8">新增兼容型号</span>
      </el-button>
    </div>

    <div class="table-toolbar">
      <el-input v-model="searchKeyword" placeholder="搜索型号、品牌、组名" clearable style="width: 260px" :prefix-icon="Search" />
      <el-select v-model="filterGroupName" placeholder="筛选兼容组" clearable style="width: 200px">
        <el-option v-for="name in groupNames" :key="name" :label="name" :value="name" />
      </el-select>
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

    <el-table :data="pagedList" border stripe style="width: 100%" v-loading="loading">
      <el-table-column type="index" label="序号" width="70" align="center" />
      <el-table-column prop="groupName" label="兼容组名称" min-width="160" show-overflow-tooltip>
        <template #default="{ row }">
          <el-tag type="primary" effect="plain" size="small">{{ row.groupName }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="brand" label="品牌" width="140" show-overflow-tooltip />
      <el-table-column prop="model" label="型号" width="160" show-overflow-tooltip />
      <el-table-column prop="spec" label="规格参数" width="180" show-overflow-tooltip />
      <el-table-column label="关联配件" min-width="200" show-overflow-tooltip>
        <template #default="{ row }">
          <span v-if="row.accessoryId">{{ getAccessoryLabel(row.accessoryId) }}</span>
          <span v-else style="color: #c0c4cc">未关联</span>
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" min-width="180" show-overflow-tooltip />
      <el-table-column label="操作" width="180" align="center" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="handleEdit(row)">
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
      :title="dialogTitle"
      width="560px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item label="兼容组名称" prop="groupName">
          <el-autocomplete
            v-model="formData.groupName"
            :fetch-suggestions="queryGroupNames"
            placeholder="输入或选择已有组名"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="品牌" prop="brand">
          <el-input v-model="formData.brand" placeholder="如：菲尼克斯、魏德米勒" maxlength="100" />
        </el-form-item>
        <el-form-item label="型号" prop="model">
          <el-input v-model="formData.model" placeholder="如：ST2.5" maxlength="100" />
        </el-form-item>
        <el-form-item label="规格参数" prop="spec">
          <el-input v-model="formData.spec" placeholder="如：2.5mm²" maxlength="100" />
        </el-form-item>
        <el-form-item label="关联配件" prop="accessoryId">
          <el-select
            v-model="formData.accessoryId"
            placeholder="选择关联配件（可选）"
            filterable
            clearable
            style="width: 100%"
          >
            <el-option
              v-for="item in accessoryList"
              :key="item.id"
              :value="item.id"
              :label="`${item.name} - ${item.model || '-'}`"
            />
          </el-select>
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
      v-model="groupDetailVisible"
      :title="`兼容组 - ${selectedGroupName}`"
      width="700px"
      destroy-on-close
    >
      <el-table :data="groupDetailList" border stripe size="small">
        <el-table-column prop="brand" label="品牌" width="140" />
        <el-table-column prop="model" label="型号" width="160" />
        <el-table-column prop="spec" label="规格参数" width="160" />
        <el-table-column prop="remark" label="备注" min-width="160" show-overflow-tooltip />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import {
  getCompatibleModelGroupList,
  createCompatibleModelGroup,
  updateCompatibleModelGroup,
  deleteCompatibleModelGroup,
  getGroupNames,
  getCompatibleModelGroupsByGroup,
  validateCompatibleModelGroup
} from '@/api/compatibleModel'
import { getAccessoryList } from '@/api/accessory'

const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增兼容型号')
const submitting = ref(false)
const validating = ref(false)
const formRef = ref(null)
const isEdit = ref(false)

const searchKeyword = ref('')
const filterGroupName = ref('')
const currentPage = ref(1)
const pageSize = ref(10)

const dataList = ref([])
const groupNames = ref([])
const accessoryList = ref([])

const groupDetailVisible = ref(false)
const selectedGroupName = ref('')
const groupDetailList = ref([])

const formData = reactive({
  id: null,
  groupName: '',
  brand: '',
  model: '',
  spec: '',
  accessoryId: null,
  remark: ''
})

const validateDuplicate = async (rule, value, callback) => {
  if (!formData.groupName || !formData.model) {
    callback()
    return
  }
  validating.value = true
  try {
    const result = await validateCompatibleModelGroup({
      id: isEdit.value ? formData.id : null,
      groupName: formData.groupName,
      brand: formData.brand,
      model: formData.model
    })
    if (result && !result.valid) {
      callback(new Error(result.message || '校验失败'))
    } else {
      callback()
    }
  } catch (error) {
    console.error('校验失败:', error)
    callback(new Error('校验失败，请稍后重试'))
  } finally {
    validating.value = false
  }
}

let debounceTimer = null
const debouncedValidate = (rule, value, callback) => {
  if (debounceTimer) {
    clearTimeout(debounceTimer)
  }
  debounceTimer = setTimeout(() => {
    validateDuplicate(rule, value, callback)
  }, 300)
}

const formRules = {
  groupName: [
    { required: true, message: '请输入兼容组名称', trigger: 'blur' },
    { validator: debouncedValidate, trigger: 'blur' }
  ],
  model: [
    { required: true, message: '请输入型号', trigger: 'blur' },
    { validator: debouncedValidate, trigger: 'blur' }
  ],
  brand: [
    { validator: debouncedValidate, trigger: 'blur' }
  ]
}

const getAccessoryLabel = (accessoryId) => {
  const acc = accessoryList.value.find(a => a.id === accessoryId)
  return acc ? `${acc.name} - ${acc.model || '-'}` : `ID:${accessoryId}`
}

const queryGroupNames = (queryString, cb) => {
  const results = groupNames.value
    .filter(name => name.toLowerCase().includes(queryString.toLowerCase()))
    .map(name => ({ value: name }))
  cb(results)
}

const filteredList = computed(() => {
  return dataList.value.filter((item) => {
    let ok = true
    if (searchKeyword.value) {
      const kw = searchKeyword.value.toLowerCase()
      ok = ok && (
        item.model?.toLowerCase().includes(kw) ||
        item.brand?.toLowerCase().includes(kw) ||
        item.groupName?.toLowerCase().includes(kw)
      )
    }
    if (filterGroupName.value) {
      ok = ok && item.groupName === filterGroupName.value
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
  filterGroupName.value = ''
  currentPage.value = 1
}

const resetForm = () => {
  formData.id = null
  formData.groupName = ''
  formData.brand = ''
  formData.model = ''
  formData.spec = ''
  formData.accessoryId = null
  formData.remark = ''
  isEdit.value = false
}

const loadData = async () => {
  loading.value = true
  try {
    const data = await getCompatibleModelGroupList()
    if (data && Array.isArray(data)) {
      dataList.value = data
    }
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载兼容型号数据失败')
  } finally {
    loading.value = false
  }
}

const loadGroupNames = async () => {
  try {
    const data = await getGroupNames()
    if (data && Array.isArray(data)) {
      groupNames.value = data
    }
  } catch (error) {
    console.error('加载组名失败:', error)
  }
}

const loadAccessoryList = async () => {
  try {
    const data = await getAccessoryList({ pageNum: 1, pageSize: 9999 })
    if (data) {
      const list = data.records || data.list || data || []
      accessoryList.value = list
    }
  } catch (error) {
    console.error('加载配件列表失败:', error)
  }
}

const handleAdd = () => {
  resetForm()
  dialogTitle.value = '新增兼容型号'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  resetForm()
  isEdit.value = true
  dialogTitle.value = '编辑兼容型号'
  formData.id = row.id
  formData.groupName = row.groupName
  formData.brand = row.brand
  formData.model = row.model
  formData.spec = row.spec
  formData.accessoryId = row.accessoryId
  formData.remark = row.remark || ''
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除型号【${row.model}】吗？`, '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }
  try {
    await deleteCompatibleModelGroup(row.id)
    ElMessage.success('删除成功')
    loadData()
    loadGroupNames()
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
        groupName: formData.groupName,
        brand: formData.brand,
        model: formData.model,
        spec: formData.spec,
        accessoryId: formData.accessoryId,
        remark: formData.remark
      }
      if (isEdit.value) {
        payload.id = formData.id
        await updateCompatibleModelGroup(payload)
      } else {
        await createCompatibleModelGroup(payload)
      }
      ElMessage.success(isEdit.value ? '修改成功' : '新增成功')
      dialogVisible.value = false
      loadData()
      loadGroupNames()
    } catch (error) {
      console.error('提交失败:', error)
      if (error && error.code === 1011) {
        ElMessage.error(error.message || '同兼容组内已存在相同品牌和型号的记录')
      } else {
        ElMessage.error(isEdit.value ? '修改失败，请稍后重试' : '新增失败，请稍后重试')
      }
    } finally {
      submitting.value = false
    }
  })
}

onMounted(() => {
  loadData()
  loadGroupNames()
  loadAccessoryList()
})
</script>
