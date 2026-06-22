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
      <el-table-column label="分类" min-width="280" class-name="category-column">
        <template #default="{ row }">
          <div class="category-path" v-if="row.categoryPath && row.categoryPath.length">
            <template v-for="(seg, idx) in row.categoryPath" :key="idx">
              <span
                class="path-seg"
                :class="idx === row.categoryPath.length - 1 ? categoryLeafTagClass(seg) : 'seg-default'"
              >{{ seg }}</span>
              <span v-if="idx < row.categoryPath.length - 1" class="path-sep">/</span>
            </template>
          </div>
          <span v-else class="path-empty">-</span>
        </template>
      </el-table-column>
      <el-table-column prop="unit" label="单位" width="80" align="center" />
      <el-table-column label="库存数量" width="120" align="center">
        <template #default="{ row }">
          <span style="color: #67c23a; font-weight: 600">{{ formatNumber(row.quantity) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="库区" width="120" align="center">
        <template #default="{ row }">
          <el-tag :type="zoneTagType(row.zone)" size="small">{{ row.zone || '-' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" width="170" align="center">
        <template #default="{ row }">
          <span>{{ row.createTime || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="250" align="center" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="handleEdit(row)">
            <el-icon><Edit /></el-icon>
            <span class="ml-8">编辑</span>
          </el-button>
          <el-button type="success" link @click="handleViewCompatible(row)">
            <el-icon><Connection /></el-icon>
            <span class="ml-8">兼容组</span>
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
                :data="selectableCategoryTree"
                :props="{ label: 'name', value: 'id', children: 'children', disabled: (data) => data.enabled === false }"
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
        <el-row v-if="duplicateList && duplicateList.length" :gutter="16">
          <el-col :span="24">
            <el-alert
              title="同分类下已存在相同型号的配件，请确认是否重复建档"
              type="warning"
              :closable="false"
              show-icon
            >
              <template #default>
                <el-table :data="duplicateList" size="small" border style="margin-top: 8px">
                  <el-table-column prop="name" label="配件名称" min-width="160" />
                  <el-table-column prop="model" label="型号" width="140" />
                  <el-table-column prop="spec" label="规格" width="140" />
                  <el-table-column label="库区" width="100" align="center">
                    <template #default="{ row }">
                      <el-tag :type="zoneTagType(row.warehouseZone || row.zone)" size="small">
                        {{ row.warehouseZone || row.zone || '-' }}
                      </el-tag>
                    </template>
                  </el-table-column>
                </el-table>
              </template>
            </el-alert>
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

    <el-dialog
      v-model="compatibleDialogVisible"
      :title="`兼容型号 - ${compatibleAccessoryName}`"
      width="700px"
      destroy-on-close
    >
      <el-table :data="compatibleList" border stripe size="small" v-loading="compatibleLoading">
        <el-table-column prop="groupName" label="兼容组" width="160">
          <template #default="{ row }">
            <el-tag type="primary" effect="plain" size="small">{{ row.groupName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="brand" label="品牌" width="140" />
        <el-table-column prop="model" label="型号" width="160" />
        <el-table-column prop="spec" label="规格" width="140" />
        <el-table-column prop="remark" label="备注" min-width="160" show-overflow-tooltip />
      </el-table>
      <el-empty v-if="!compatibleLoading && compatibleList.length === 0" description="暂无兼容型号数据" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { getAccessoryPage, addAccessory, updateAccessory, deleteAccessory } from '@/api/accessory'
import { getCategoryTree } from '@/api/category'
import { getAccessoryCompatibleGroup } from '@/api/compatibleModel'

const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增配件')
const submitting = ref(false)
const formRef = ref(null)
const isEdit = ref(false)
const duplicateList = ref([])

const searchKeyword = ref('')
const searchCategoryId = ref(null)
const searchZone = ref('')
const currentPage = ref(1)
const pageSize = ref(10)

const dataList = ref([])
const categoryTree = ref([])
const selectableCategoryTree = ref([])

const compatibleDialogVisible = ref(false)
const compatibleAccessoryName = ref('')
const compatibleList = ref([])
const compatibleLoading = ref(false)

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
  const prefix = zone ? zone.charAt(0).toUpperCase() : ''
  return map[prefix] || 'info'
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

const filterEnabledCategories = (list, preserveIds = new Set()) => {
  if (!list || !Array.isArray(list)) return []
  return list
    .filter((node) => {
      if (preserveIds.has(node.id)) return true
      return node.enabled !== false
    })
    .map((node) => ({
      ...node,
      children: node.children ? filterEnabledCategories(node.children, preserveIds) : []
    }))
}

const collectDescendantIds = (list, id, ids = new Set()) => {
  for (const node of list) {
    if (node.id === id) {
      ids.add(node.id)
      if (node.children) {
        for (const child of node.children) {
          collectDescendantIds([child], child.id, ids)
        }
      }
    } else if (node.children) {
      collectDescendantIds(node.children, id, ids)
    }
  }
  return ids
}

const findNodePath = (list, id, path = []) => {
  for (const node of list) {
    const newPath = [...path, node]
    if (node.id === id) return newPath
    if (node.children && node.children.length > 0) {
      const found = findNodePath(node.children, id, newPath)
      if (found) return found
    }
  }
  return null
}

const buildCategoryInfo = (categoryId, fallbackName) => {
  const pathNodes = findNodePath(categoryTree.value, categoryId)
  if (pathNodes && pathNodes.length) {
    const path = pathNodes.map((n) => n.name)
    return { categoryName: path[path.length - 1], categoryPath: path }
  }
  if (fallbackName) {
    return { categoryName: fallbackName, categoryPath: [fallbackName] }
  }
  return { categoryName: '-', categoryPath: [] }
}

const categoryLeafTagClass = (leafName) => {
  if (!leafName) return 'seg-leaf-default'
  if (leafName.includes('接线端子')) return 'seg-terminal'
  if (leafName.includes('线槽')) return 'seg-duct'
  if (leafName.includes('固定卡扣')) return 'seg-clip'
  return 'seg-leaf-default'
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
      ok = ok && item.zone && item.zone.charAt(0).toUpperCase() === searchZone.value.charAt(0).toUpperCase()
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
  duplicateList.value = []
}

const loadCategoryTree = async () => {
  try {
    const data = await getCategoryTree()
    if (data && Array.isArray(data)) {
      categoryTree.value = data
      selectableCategoryTree.value = filterEnabledCategories(data)
    }
  } catch (error) {
    console.error('加载分类树失败:', error)
    ElMessage.error('加载分类失败，请稍后重试')
  }
}

const mapAccessoryFields = (item) => ({
  ...item,
  quantity: item.stockQuantity ?? item.quantity,
  zone: item.warehouseZone ?? item.zone
})

const loadData = async () => {
  loading.value = true
  try {
    const data = await getAccessoryPage({ pageNum: 1, pageSize: 9999 })
    if (data) {
      const list = data.records || data.list || data || []
      dataList.value = list.map((item) => ({
        ...mapAccessoryFields(item),
        ...buildCategoryInfo(item.categoryId, item.categoryName)
      }))
    }
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载配件列表失败，请点击重试')
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  resetForm()
  dialogTitle.value = '新增配件'
  selectableCategoryTree.value = filterEnabledCategories(categoryTree.value)
  dialogVisible.value = true
}

const handleEdit = (row) => {
  resetForm()
  isEdit.value = true
  dialogTitle.value = '编辑配件'
  const mapped = mapAccessoryFields(row)
  formData.id = mapped.id
  formData.categoryId = mapped.categoryId
  formData.name = mapped.name
  formData.model = mapped.model
  formData.spec = mapped.spec
  formData.unit = mapped.unit
  formData.quantity = mapped.quantity
  formData.zone = mapped.zone
  formData.remark = mapped.remark || ''
  const preserveIds = new Set()
  if (mapped.categoryId) {
    collectDescendantIds(categoryTree.value, mapped.categoryId, preserveIds)
  }
  selectableCategoryTree.value = filterEnabledCategories(categoryTree.value, preserveIds)
  dialogVisible.value = true
}

const handleViewCompatible = async (row) => {
  compatibleAccessoryName.value = row.name || ''
  compatibleList.value = []
  compatibleDialogVisible.value = true
  compatibleLoading.value = true
  try {
    const data = await getAccessoryCompatibleGroup(row.id)
    if (data && Array.isArray(data)) {
      compatibleList.value = data
    }
  } catch (error) {
    console.error('加载兼容组数据失败:', error)
  } finally {
    compatibleLoading.value = false
  }
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
    if (error && error.code === 1006 && error.data) {
      const { stockOutCount = 0, inventoryCheckCount = 0, scrapCount = 0 } = error.data
      const total = (stockOutCount || 0) + (inventoryCheckCount || 0) + (scrapCount || 0)
      const details = [
        stockOutCount ? `出库记录 ${stockOutCount} 条` : '',
        inventoryCheckCount ? `清点记录 ${inventoryCheckCount} 条` : '',
        scrapCount ? `报废记录 ${scrapCount} 条` : ''
      ].filter(Boolean).join('、')
      ElMessageBox.alert(
        `该配件存在 ${total} 条历史记录，无法物理删除。\n关联记录：${details}\n为避免弱电台账断链，请保留此档案。`,
        '删除失败：存在关联历史记录',
        {
          confirmButtonText: '我知道了',
          type: 'error'
        }
      ).catch(() => {})
    } else if (!error || error.code !== 1006) {
      ElMessage.error('删除失败，请稍后重试')
    }
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
      if (error && error.code === 1007 && error.data && Array.isArray(error.data)) {
        duplicateList.value = error.data.map((item) => ({
          ...item,
          zone: item.warehouseZone ?? item.zone
        }))
      } else if (!error || error.code !== 1007) {
        ElMessage.error(isEdit.value ? '修改失败，请稍后重试' : '新增失败，请稍后重试')
      }
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
