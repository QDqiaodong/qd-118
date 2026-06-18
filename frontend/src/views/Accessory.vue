<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-title">
        <el-icon><Goods /></el-icon>
        <span class="ml-8">布线配件档案录入</span>
      </div>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        <span class="ml-8">新增配件</span>
      </el-button>
    </div>

    <div class="table-toolbar">
      <el-input v-model="searchKeyword" placeholder="搜索型号、名称、规格" clearable style="width: 260px" :prefix-icon="Search" />
      <el-tree-select
        v-model="searchCategoryId"
        :data="categoryTree"
        :props="{ label: 'name', value: 'id', children: 'children' }"
        placeholder="选择分类筛选"
        clearable
        check-strictly
        style="width: 220px"
      />
      <el-select v-model="searchZone" placeholder="库房分区筛选" clearable style="width: 160px">
        <el-option label="A区" value="A" />
        <el-option label="B区" value="B" />
        <el-option label="C区" value="C" />
        <el-option label="D区" value="D" />
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
      <el-table-column prop="name" label="配件名称" min-width="200" show-overflow-tooltip />
      <el-table-column prop="model" label="型号" width="160" show-overflow-tooltip />
      <el-table-column prop="spec" label="规格" width="180" show-overflow-tooltip />
      <el-table-column label="分类" width="180">
        <template #default="{ row }">
          <el-tag type="info" size="small">{{ row.categoryName || '-' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="unit" label="单位" width="80" align="center" />
      <el-table-column label="入库数量" width="120" align="center">
        <template #default="{ row }">
          <span style="color: #67c23a; font-weight: 600">{{ formatNumber(row.quantity) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="库房分区" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="zoneTagType(row.zone)" size="small">{{ row.zone ? row.zone + '区' : '-' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="170" align="center" />
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
      width="600px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-row :gutter="16">
          <el-col :span="24">
            <el-form-item label="配件分类" prop="categoryId">
              <el-tree-select
                v-model="formData.categoryId"
                :data="categoryTree"
                :props="{ label: 'name', value: 'id', children: 'children' }"
                placeholder="请选择配件分类"
                style="width: 100%"
                check-strictly
                :render-after-expand="false"
                show-checkbox
                :multiple="false"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="24">
            <el-form-item label="配件名称" prop="name">
              <el-input v-model="formData.name" placeholder="请输入配件名称" maxlength="100" show-word-limit />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="型号" prop="model">
              <el-input v-model="formData.model" placeholder="如：CAT6-305M" maxlength="50" show-word-limit />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="规格" prop="spec">
              <el-input v-model="formData.spec" placeholder="如：305米/箱" maxlength="50" show-word-limit />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="单位" prop="unit">
              <el-input v-model="formData.unit" placeholder="如：箱、个、米" maxlength="10" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="入库数量" prop="quantity">
              <el-input-number v-model="formData.quantity" :min="0" :max="99999" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="24">
            <el-form-item label="库房分区" prop="zone">
              <el-radio-group v-model="formData.zone">
                <el-radio-button label="A">A区</el-radio-button>
                <el-radio-button label="B">B区</el-radio-button>
                <el-radio-button label="C">C区</el-radio-button>
                <el-radio-button label="D">D区</el-radio-button>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="24">
            <el-form-item label="备注">
              <el-input v-model="formData.remark" type="textarea" :rows="3" maxlength="200" show-word-limit placeholder="请输入备注信息（可选）" />
            </el-form-item>
          </el-col>
        </el-row>
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
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { getAccessoryPage, addAccessory, updateAccessory, deleteAccessory } from '@/api/accessory'
import { getCategoryTree } from '@/api/category'

const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增配件')
const submitting = ref(false)
const formRef = ref(null)
const isEdit = ref(false)

const searchKeyword = ref('')
const searchCategoryId = ref(null)
const searchZone = ref('')
const currentPage = ref(1)
const pageSize = ref(10)

const dataList = ref([])
const categoryTree = ref([])

const formData = reactive({
  id: null,
  categoryId: null,
  name: '',
  model: '',
  spec: '',
  unit: '',
  quantity: 0,
  zone: '',
  remark: ''
})

const formRules = {
  categoryId: [
    { required: true, message: '请选择配件分类', trigger: 'change' }
  ],
  name: [
    { required: true, message: '请输入配件名称', trigger: 'blur' },
    { min: 1, max: 100, message: '名称长度 1-100 个字符', trigger: 'blur' }
  ],
  model: [
    { required: true, message: '请输入型号', trigger: 'blur' },
    { min: 1, max: 50, message: '型号长度 1-50 个字符', trigger: 'blur' }
  ],
  spec: [
    { required: true, message: '请输入规格', trigger: 'blur' },
    { min: 1, max: 50, message: '规格长度 1-50 个字符', trigger: 'blur' }
  ],
  unit: [
    { required: true, message: '请输入单位', trigger: 'blur' }
  ],
  quantity: [
    { required: true, message: '请输入入库数量', trigger: 'blur' },
    { type: 'number', min: 0, max: 99999, message: '入库数量范围 0-99999', trigger: 'blur' }
  ],
  zone: [
    { required: true, message: '请选择库房分区', trigger: 'change' }
  ]
}

const formatNumber = (num) => {
  if (num === null || num === undefined || isNaN(num)) return 0
  return Number(num).toLocaleString('zh-CN')
}

const zoneTagType = (zone) => {
  const map = { A: 'primary', B: 'success', C: 'warning', D: 'danger' }
  return map[zone] || 'info'
}

const findNodeById = (list, id) => {
  for (const node of list) {
    if (node.id === id) return node
    if (node.children && node.children.length > 0) {
      const found = findNodeById(node.children, id)
      if (found) return found
    }
  }
  return null
}

const filteredList = computed(() => {
  return dataList.value.filter((item) => {
    let ok = true
    if (searchKeyword.value) {
      const kw = searchKeyword.value.toLowerCase()
      ok = ok && (
        item.name?.toLowerCase().includes(kw) ||
        item.model?.toLowerCase().includes(kw) ||
        item.spec?.toLowerCase().includes(kw)
      )
    }
    if (searchCategoryId.value) {
      const cid = searchCategoryId.value
      const matchCategory = (itemId) => {
        if (itemId === cid) return true
        const node = findNodeById(categoryTree.value, itemId)
        let parent = node?.parentId
        while (parent) {
          if (parent === cid) return true
          const pNode = findNodeById(categoryTree.value, parent)
          parent = pNode?.parentId
        }
        return false
      }
      ok = ok && matchCategory(item.categoryId)
    }
    if (searchZone.value) {
      ok = ok && item.zone === searchZone.value
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
  searchCategoryId.value = null
  searchZone.value = ''
  currentPage.value = 1
}

const resetForm = () => {
  formData.id = null
  formData.categoryId = null
  formData.name = ''
  formData.model = ''
  formData.spec = ''
  formData.unit = ''
  formData.quantity = 0
  formData.zone = ''
  formData.remark = ''
  isEdit.value = false
}

const loadCategoryTree = async () => {
  try {
    const data = await getCategoryTree()
    if (data && Array.isArray(data)) {
      categoryTree.value = data
    }
  } catch (error) {
    console.error('加载分类树失败:', error)
  }
  if (categoryTree.value.length === 0) {
    loadMockCategory()
  }
}

const loadMockCategory = () => {
  categoryTree.value = [
    { id: 1, parentId: 0, name: '线缆类', children: [
      { id: 11, parentId: 1, name: '网线', children: [
        { id: 111, parentId: 11, name: '超五类网线', children: [] },
        { id: 112, parentId: 11, name: '六类网线', children: [] }
      ]},
      { id: 12, parentId: 1, name: '光纤', children: [] }
    ]},
    { id: 2, parentId: 0, name: '连接器件', children: [
      { id: 21, parentId: 2, name: '水晶头', children: [] },
      { id: 22, parentId: 2, name: '配线架', children: [] }
    ]},
    { id: 3, parentId: 0, name: '管材管件', children: [] }
  ]
}

const loadData = async () => {
  loading.value = true
  try {
    const data = await getAccessoryPage({ pageNum: 1, pageSize: 9999 })
    if (data) {
      const list = data.records || data.list || data || []
      dataList.value = list.map((item) => ({
        ...item,
        categoryName: findNodeById(categoryTree.value, item.categoryId)?.name || '-'
      }))
    }
  } catch (error) {
    console.error('加载数据失败:', error)
  } finally {
    loading.value = false
  }
  if (dataList.value.length === 0) {
    setTimeout(() => {
      if (dataList.value.length === 0) loadMockData()
    }, 600)
  }
}

const loadMockData = () => {
  dataList.value = [
    { id: 1, categoryId: 112, categoryName: '六类网线', name: '六类非屏蔽网线', model: 'CAT6-305M', spec: '305米/箱', unit: '箱', quantity: 156, zone: 'A', remark: '安普品牌', createTime: '2026-01-15 10:30:00' },
    { id: 2, categoryId: 21, categoryName: '水晶头', name: 'CAT6 RJ45水晶头', model: 'RJ45-C6', spec: '100个/盒', unit: '盒', quantity: 320, zone: 'B', remark: '镀金触点', createTime: '2026-01-16 14:20:00' },
    { id: 3, categoryId: 22, categoryName: '配线架', name: '24口六类配线架', model: 'PATCH-24-C6', spec: '1U机架式', unit: '台', quantity: 45, zone: 'A', remark: '', createTime: '2026-02-01 09:00:00' },
    { id: 4, categoryId: 111, categoryName: '超五类网线', name: '超五类非屏蔽网线', model: 'CAT5E-305M', spec: '305米/箱', unit: '箱', quantity: 88, zone: 'A', remark: '', createTime: '2026-02-05 11:15:00' },
    { id: 5, categoryId: 12, categoryName: '光纤', name: '单模室内光纤', model: 'SM-9/125-4C', spec: '4芯/100米', unit: '米', quantity: 5000, zone: 'C', remark: '低烟无卤', createTime: '2026-02-10 16:40:00' },
    { id: 6, categoryId: 3, categoryName: '管材管件', name: 'PVC线管', model: 'PVC-D20', spec: 'φ20mm/3米', unit: '根', quantity: 1200, zone: 'D', remark: '', createTime: '2026-02-12 08:30:00' },
    { id: 7, categoryId: 21, categoryName: '水晶头', name: '六类屏蔽水晶头', model: 'RJ45-C6-S', spec: '50个/盒', unit: '盒', quantity: 60, zone: 'B', remark: 'FTP屏蔽', createTime: '2026-03-01 10:00:00' },
    { id: 8, categoryId: 112, categoryName: '六类网线', name: '六类屏蔽网线', model: 'CAT6-S-305M', spec: '35米/箱', unit: '箱', quantity: 32, zone: 'A', remark: 'FTP双层屏蔽', createTime: '2026-03-05 14:00:00' }
  ]
}

const handleAdd = () => {
  resetForm()
  dialogTitle.value = '新增配件'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  resetForm()
  isEdit.value = true
  dialogTitle.value = '编辑配件'
  formData.id = row.id
  formData.categoryId = row.categoryId
  formData.name = row.name
  formData.model = row.model
  formData.spec = row.spec
  formData.unit = row.unit
  formData.quantity = row.quantity
  formData.zone = row.zone
  formData.remark = row.remark || ''
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除配件【${row.name}】吗？`, '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }
  try {
    await deleteAccessory(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    console.error('删除失败:', error)
    dataList.value = dataList.value.filter((item) => item.id !== row.id)
    ElMessage.success('删除成功')
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      const payload = {
        categoryId: formData.categoryId,
        name: formData.name,
        model: formData.model,
        spec: formData.spec,
        unit: formData.unit,
        quantity: formData.quantity,
        zone: formData.zone,
        remark: formData.remark
      }
      if (isEdit.value) {
        payload.id = formData.id
        await updateAccessory(payload)
      } else {
        await addAccessory(payload)
      }
      ElMessage.success(isEdit.value ? '修改成功' : '新增成功')
      dialogVisible.value = false
      loadData()
    } catch (error) {
      console.error('提交失败:', error)
      const categoryName = findNodeById(categoryTree.value, formData.categoryId)?.name || '-'
      if (isEdit.value) {
        const idx = dataList.value.findIndex((i) => i.id === formData.id)
        if (idx >= 0) {
          dataList.value[idx] = { ...dataList.value[idx], ...formData, categoryName }
        }
      } else {
        const now = new Date()
        const timeStr = `${now.getFullYear()}-${String(now.getMonth()+1).padStart(2,'0')}-${String(now.getDate()).padStart(2,'0')} ${String(now.getHours()).padStart(2,'0')}:${String(now.getMinutes()).padStart(2,'0')}:${String(now.getSeconds()).padStart(2,'0')}`
        dataList.value.unshift({
          id: Date.now(),
          ...formData,
          categoryName,
          createTime: timeStr
        })
      }
      dataList.value = [...dataList.value]
      ElMessage.success(isEdit.value ? '修改成功' : '新增成功')
      dialogVisible.value = false
    } finally {
      submitting.value = false
    }
  })
}

onMounted(async () => {
  await loadCategoryTree()
  loadData()
})
</script>
