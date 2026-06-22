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
        <div style="display: flex; align-items: center; justify-content: space-between">
          <div style="display: flex; align-items: center; font-weight: 600">
            <el-icon><WarningFilled /></el-icon>
            <span class="ml-8">报废登记</span>
          </div>
          <el-button type="primary" size="small" :icon="Plus" @click="addScrapItem">
            添加报废项
          </el-button>
        </div>
      </template>

      <el-form ref="scrapFormRef" :model="scrapForm" :rules="scrapFormRules" label-width="100px">
        <el-table :data="scrapForm.items" border style="width: 100%; margin-bottom: 16px">
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column label="选择配件" min-width="260">
            <template #default="{ row, $index }">
              <el-form-item
                :prop="`items.${$index}.accessoryId`"
                :rules="{ required: true, message: '请选择配件', trigger: 'change' }"
                style="margin-bottom: 0"
              >
                <el-select
                  v-model="row.accessoryId"
                  placeholder="请选择配件"
                  filterable
                  style="width: 100%"
                  @change="handleAccessoryChange(row)"
                >
                  <el-option
                    v-for="item in accessoryList"
                    :key="item.id"
                    :value="item.id"
                    :label="`${item.name} - ${item.model}`"
                  >
                    <span style="float: left">{{ item.name }} - {{ item.model }}</span>
                    <span style="float: right; color: #909399; font-size: 12px">
                      库存: {{ formatNumber(getAvailableStock(item.id)) }} {{ item.unit }}
                    </span>
                  </el-option>
                </el-select>
              </el-form-item>
            </template>
          </el-table-column>
          <el-table-column label="当前库存" width="120" align="center">
            <template #default="{ row }">
              <el-tag v-if="getRowAccessory(row)" type="danger" effect="plain" size="small">
                {{ formatNumber(getAvailableStock(row.accessoryId)) }} {{ getRowAccessory(row).unit }}
              </el-tag>
              <span v-else style="color: #c0c4cc">-</span>
            </template>
          </el-table-column>
          <el-table-column label="报废数量" width="140" align="center">
            <template #default="{ row, $index }">
              <el-form-item
                :prop="`items.${$index}.quantity`"
                :rules="[
                  { required: true, message: '请输入数量', trigger: 'blur' },
                  { type: 'number', min: 1, message: '至少为1', trigger: 'blur' }
                ]"
                style="margin-bottom: 0"
              >
                <el-input-number
                  v-model="row.quantity"
                  :min="1"
                  :max="getMaxQuantity(row)"
                  controls-position="right"
                  size="small"
                  style="width: 100%"
                />
              </el-form-item>
            </template>
          </el-table-column>
          <el-table-column label="报废原因" width="200" align="center">
            <template #default="{ row, $index }">
              <el-form-item
                :prop="`items.${$index}.reason`"
                :rules="{ required: true, message: '请选择原因', trigger: 'change' }"
                style="margin-bottom: 0"
              >
                <el-select
                  v-model="row.reason"
                  placeholder="选择原因"
                  style="width: 100%"
                  @change="handleReasonChange(row)"
                >
                  <el-option-group label="老化">
                    <el-option label="自然老化" value="自然老化" />
                    <el-option label="过期失效" value="过期失效" />
                  </el-option-group>
                  <el-option-group label="破损">
                    <el-option label="物理损坏" value="物理损坏" />
                    <el-option label="腐蚀损坏" value="腐蚀损坏" />
                  </el-option-group>
                  <el-option-group label="规格不符">
                    <el-option label="型号不符" value="型号不符" />
                    <el-option label="参数不符" value="参数不符" />
                  </el-option-group>
                  <el-option-group label="其他">
                    <el-option label="质量问题" value="质量问题" />
                    <el-option label="技术淘汰" value="技术淘汰" />
                    <el-option label="其他原因" value="其他原因" />
                  </el-option-group>
                </el-select>
              </el-form-item>
            </template>
          </el-table-column>
          <el-table-column label="原因标签" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="getReasonGroupType(row.reason)" size="small" v-if="row.reason">
                {{ getReasonGroup(row.reason) }}
              </el-tag>
              <span v-else style="color: #c0c4cc">-</span>
            </template>
          </el-table-column>
          <el-table-column label="老化附件" min-width="200" align="center">
            <template #default="{ row, $index }">
              <div>
                <el-button
                  type="primary"
                  link
                  size="small"
                  :icon="Plus"
                  @click="addAttachment(row)"
                >
                  添加附件
                </el-button>
                <div v-if="row.attachments && row.attachments.length > 0" style="margin-top: 4px">
                  <el-tag
                    v-for="(att, attIndex) in row.attachments"
                    :key="attIndex"
                    type="info"
                    size="small"
                    style="margin-right: 4px; margin-bottom: 4px"
                    closable
                    @close="removeAttachment(row, attIndex)"
                  >
                    {{ att.description || '附件' + (attIndex + 1) }}
                  </el-tag>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="70" align="center">
            <template #default="{ $index }">
              <el-button
                type="danger"
                link
                size="small"
                :icon="Delete"
                :disabled="scrapForm.items.length <= 1"
                @click="removeScrapItem($index)"
              />
            </template>
          </el-table-column>
        </el-table>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="经办人" prop="operator">
              <el-input v-model="scrapForm.operator" placeholder="请输入经办人" maxlength="20" />
            </el-form-item>
          </el-col>
          <el-col :span="16">
            <el-form-item label="详细说明" prop="remark">
              <el-input
                v-model="scrapForm.remark"
                type="textarea"
                :rows="2"
                maxlength="200"
                show-word-limit
                placeholder="请输入报废详细说明（可选）"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item style="margin-bottom: 0">
          <el-button type="danger" :loading="submitting" @click="handleScrapSubmit">
            <el-icon><Warning /></el-icon>
            <span class="ml-8">确认报废归档 ({{ scrapForm.items.length }} 项)</span>
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
              <el-option-group label="老化">
                <el-option label="自然老化" value="自然老化" />
                <el-option label="过期失效" value="过期失效" />
              </el-option-group>
              <el-option-group label="破损">
                <el-option label="物理损坏" value="物理损坏" />
                <el-option label="腐蚀损坏" value="腐蚀损坏" />
              </el-option-group>
              <el-option-group label="规格不符">
                <el-option label="型号不符" value="型号不符" />
                <el-option label="参数不符" value="参数不符" />
              </el-option-group>
              <el-option-group label="其他">
                <el-option label="质量问题" value="质量问题" />
                <el-option label="技术淘汰" value="技术淘汰" />
                <el-option label="其他原因" value="其他原因" />
              </el-option-group>
            </el-select>
            <el-select v-model="filterReasonGroup" placeholder="筛选分组" clearable style="width: 140px">
              <el-option label="老化" value="老化" />
              <el-option label="破损" value="破损" />
              <el-option label="规格不符" value="规格不符" />
              <el-option label="其他" value="其他" />
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
        <el-table-column label="原因分组" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getReasonGroupType(row.reason)" size="small">
              {{ getReasonGroup(row.reason) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="报废原因" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="reasonTagType(row.reason)" size="small">{{ row.reason }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operator" label="经办人" width="100" align="center" />
        <el-table-column prop="createTime" label="报废时间" width="170" align="center" />
        <el-table-column prop="remark" label="详细说明" min-width="160" show-overflow-tooltip />
        <el-table-column label="附件" width="100" align="center">
          <template #default="{ row }">
            <el-button
              type="primary"
              link
              size="small"
              :icon="Picture"
              @click="handleViewAttachments(row)"
            >
              查看
            </el-button>
          </template>
        </el-table-column>
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

    <el-dialog v-model="attachmentFormVisible" title="添加老化附件" width="500px" top="10vh">
      <el-form :model="currentAttachmentForm" label-width="80px">
        <el-form-item label="附件地址">
          <el-input
            v-model="currentAttachmentForm.fileUrl"
            placeholder="请输入附件地址（图片/文档URL）"
          />
        </el-form-item>
        <el-form-item label="文件名称">
          <el-input
            v-model="currentAttachmentForm.fileName"
            placeholder="请输入文件名称（可选）"
          />
        </el-form-item>
        <el-form-item label="文件类型">
          <el-select v-model="currentAttachmentForm.fileType" style="width: 100%">
            <el-option label="图片" value="image" />
            <el-option label="文档" value="document" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="附件说明">
          <el-input
            v-model="currentAttachmentForm.description"
            type="textarea"
            :rows="2"
            placeholder="请输入附件说明（可选）"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="attachmentFormVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmAddAttachment">确认添加</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="attachmentDialogVisible" title="报废附件查看" width="700px" top="5vh">
      <div v-if="currentScrapRecord" style="margin-bottom: 16px">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="配件名称">{{ currentScrapRecord.accessoryName }}</el-descriptions-item>
          <el-descriptions-item label="型号">{{ currentScrapRecord.accessoryModel }}</el-descriptions-item>
          <el-descriptions-item label="报废数量">{{ currentScrapRecord.quantity }} {{ currentScrapRecord.unit }}</el-descriptions-item>
          <el-descriptions-item label="报废原因">{{ currentScrapRecord.reason }}</el-descriptions-item>
          <el-descriptions-item label="经办人">{{ currentScrapRecord.operator }}</el-descriptions-item>
          <el-descriptions-item label="报废时间">{{ currentScrapRecord.createTime }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <div v-if="currentAttachments.length === 0" style="text-align: center; padding: 40px; color: #909399">
        暂无附件
      </div>

      <div v-else>
        <div
          v-for="(att, index) in currentAttachments"
          :key="index"
          style="margin-bottom: 16px; padding: 12px; border: 1px solid #ebeef5; border-radius: 8px"
        >
          <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px">
            <div style="display: flex; align-items: center; gap: 8px">
              <el-icon :size="20" color="#409eff"><Picture /></el-icon>
              <span style="font-weight: 600">{{ att.fileName || '附件' + (index + 1) }}</span>
              <el-tag size="small">{{ att.fileType || '其他' }}</el-tag>
            </div>
            <el-button
              type="primary"
              link
              size="small"
              :icon="View"
              @click="openAttachment(att)"
            >
              打开
            </el-button>
          </div>
          <div v-if="att.description" style="color: #606266; font-size: 14px; margin-bottom: 8px">
            说明：{{ att.description }}
          </div>
          <div style="color: #909399; font-size: 12px; word-break: break-all">
            地址：{{ att.fileUrl }}
          </div>
          <div v-if="att.fileType === 'image' || att.fileUrl && (att.fileUrl.endsWith('.jpg') || att.fileUrl.endsWith('.jpeg') || att.fileUrl.endsWith('.png') || att.fileUrl.endsWith('.gif'))" style="margin-top: 8px">
            <el-image
              :src="att.fileUrl"
              :preview-src-list="[att.fileUrl]"
              style="width: 100px; height: 100px; object-fit: cover"
              fit="cover"
            />
          </div>
        </div>
      </div>

      <template #footer>
        <el-button @click="attachmentDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Delete, View, Picture } from '@element-plus/icons-vue'
import { getAccessoryList } from '@/api/accessory'
import { getScrapPage, createScrapBatch, deleteScrap, getScrapAttachments } from '@/api/scrap'

const route = useRoute()

const historyLoading = ref(false)
const submitting = ref(false)
const scrapFormRef = ref(null)

const accessoryList = ref([])
const historyList = ref([])
const filterReason = ref('')
const filterReasonGroup = ref('')
const historyPage = ref(1)
const historyPageSize = ref(10)

const attachmentDialogVisible = ref(false)
const attachmentFormVisible = ref(false)
const currentAttachments = ref([])
const currentScrapRecord = ref(null)
const currentAttachmentRow = ref(null)

const currentAttachmentForm = reactive({
  fileUrl: '',
  fileName: '',
  fileType: 'image',
  description: ''
})

const scrapForm = reactive({
  items: [
    {
      accessoryId: null,
      quantity: 1,
      reason: '',
      remark: '',
      attachments: [],
      agingPreCheckId: null
    }
  ],
  operator: '',
  remark: ''
})

const scrapFormRules = {
  operator: [
    { required: true, message: '请输入经办人姓名', trigger: 'blur' },
    { min: 1, max: 20, message: '姓名长度 1-20 个字符', trigger: 'blur' }
  ]
}

const reasonGroupMap = {
  '自然老化': '老化',
  '过期失效': '老化',
  '物理损坏': '破损',
  '腐蚀损坏': '破损',
  '型号不符': '规格不符',
  '参数不符': '规格不符',
  '质量问题': '其他',
  '技术淘汰': '其他',
  '其他原因': '其他'
}

const reasonGroupTypeMap = {
  '老化': 'info',
  '破损': 'danger',
  '规格不符': 'warning',
  '其他': 'primary'
}

const getReasonGroup = (reason) => {
  return reasonGroupMap[reason] || '其他'
}

const getReasonGroupType = (reason) => {
  const group = getReasonGroup(reason)
  return reasonGroupTypeMap[group] || 'info'
}

const getRowAccessory = (row) => {
  if (!row.accessoryId) return null
  return accessoryList.value.find((a) => a.id === row.accessoryId) || null
}

const getMergedQuantities = () => {
  const merged = {}
  scrapForm.items.forEach((item) => {
    if (item.accessoryId && item.quantity > 0) {
      merged[item.accessoryId] = (merged[item.accessoryId] || 0) + item.quantity
    }
  })
  return merged
}

const getAvailableStock = (accessoryId) => {
  const accessory = accessoryList.value.find((a) => a.id === accessoryId)
  if (!accessory) return 0
  return accessory.quantity
}

const getMaxQuantity = (row) => {
  const accessory = getRowAccessory(row)
  if (!accessory) return 99999
  const merged = getMergedQuantities()
  const currentItemQty = row.quantity || 0
  const otherSameItemQty = (merged[row.accessoryId] || 0) - currentItemQty
  return Math.max(accessory.quantity - otherSameItemQty, 1)
}

const selectedAccessory = computed(() => {
  if (!scrapForm.items[0]?.accessoryId) return null
  return accessoryList.value.find((a) => a.id === scrapForm.items[0].accessoryId) || null
})

const filteredHistoryList = computed(() => {
  let list = historyList.value
  if (filterReason.value) {
    list = list.filter((h) => h.reason === filterReason.value)
  }
  if (filterReasonGroup.value) {
    list = list.filter((h) => getReasonGroup(h.reason) === filterReasonGroup.value)
  }
  return list
})

const filteredHistory = computed(() => {
  const list = filteredHistoryList.value
  const start = (historyPage.value - 1) * historyPageSize.value
  return list.slice(start, start + historyPageSize.value)
})

watch([filterReason, filterReasonGroup], () => {
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
    '腐蚀损坏': 'danger',
    '质量问题': 'warning',
    '过期失效': 'warning',
    '型号不符': 'warning',
    '参数不符': 'warning',
    '技术淘汰': 'primary',
    '其他原因': 'default'
  }
  return map[reason] || 'info'
}

const addScrapItem = (preCheckItem = null) => {
  const newItem = {
    accessoryId: null,
    quantity: 1,
    reason: '',
    remark: '',
    attachments: [],
    agingPreCheckId: null
  }

  if (preCheckItem) {
    newItem.accessoryId = preCheckItem.accessoryId
    newItem.reason = '自然老化'
    newItem.agingPreCheckId = preCheckItem.agingPreCheckId || null
    if (preCheckItem.yellowingLevel !== undefined) {
      newItem.remark = `发黄${preCheckItem.yellowingLevel}级/脆裂${preCheckItem.crackingLevel}级/氧化${preCheckItem.oxidationLevel}级，综合${preCheckItem.overallLevel}级`
    }
  }

  scrapForm.items.push(newItem)
}

const removeScrapItem = (index) => {
  if (scrapForm.items.length <= 1) {
    ElMessage.warning('至少保留一项报废记录')
    return
  }
  scrapForm.items.splice(index, 1)
}

const addAttachment = (row) => {
  currentAttachmentRow.value = row
  currentAttachmentForm.fileUrl = ''
  currentAttachmentForm.fileName = ''
  currentAttachmentForm.fileType = 'image'
  currentAttachmentForm.description = ''
  attachmentFormVisible.value = true
}

const removeAttachment = (row, index) => {
  if (!row.attachments) {
    row.attachments = []
  }
  row.attachments.splice(index, 1)
}

const confirmAddAttachment = () => {
  if (!currentAttachmentForm.fileUrl || !currentAttachmentForm.fileUrl.trim()) {
    ElMessage.warning('请输入附件地址')
    return
  }

  if (!currentAttachmentRow.value.attachments) {
    currentAttachmentRow.value.attachments = []
  }

  currentAttachmentRow.value.attachments.push({
    fileUrl: currentAttachmentForm.fileUrl,
    fileName: currentAttachmentForm.fileName,
    fileType: currentAttachmentForm.fileType,
    description: currentAttachmentForm.description
  })

  attachmentFormVisible.value = false
  ElMessage.success('附件添加成功')
}

const handleViewAttachments = async (row) => {
  currentScrapRecord.value = row
  currentAttachments.value = []
  attachmentDialogVisible.value = true

  try {
    const data = await getScrapAttachments(row.id)
    if (data) {
      currentAttachments.value = data || []
    }
  } catch (error) {
    console.error('加载附件失败:', error)
    ElMessage.error('加载附件失败')
  }
}

const openAttachment = (att) => {
  if (att.fileUrl) {
    window.open(att.fileUrl, '_blank')
  }
}

const handleAccessoryChange = (row) => {
  if (row.quantity > getMaxQuantity(row)) {
    row.quantity = getMaxQuantity(row)
  }
  if (row.quantity < 1) {
    row.quantity = 1
  }
}

const handleReasonChange = (row) => {
}

const resetScrapForm = () => {
  scrapForm.items = [
    {
      accessoryId: null,
      quantity: 1,
      reason: '',
      remark: '',
      attachments: [],
      agingPreCheckId: null
    }
  ]
  scrapForm.operator = ''
  scrapForm.remark = ''
  scrapFormRef.value?.resetFields()
}

const loadFromPreCheck = () => {
  if (route.query.fromPreCheck === 'true' && route.query.items) {
    try {
      const items = JSON.parse(route.query.items)
      if (Array.isArray(items) && items.length > 0) {
        scrapForm.items = items.map((item) => ({
          accessoryId: item.accessoryId,
          quantity: 1,
          reason: '自然老化',
          remark: item.yellowingLevel !== undefined
            ? `发黄${item.yellowingLevel}级/脆裂${item.crackingLevel}级/氧化${item.oxidationLevel}级，综合${item.overallLevel}级`
            : '',
          attachments: [],
          agingPreCheckId: item.agingPreCheckId || null
        }))
        ElMessage.info(`已从老化预检导入 ${items.length} 项待报废配件`)
      }
    } catch (error) {
      console.error('解析预检数据失败:', error)
    }
  }
}

const mapAccessoryFields = (item) => ({
  ...item,
  quantity: item.stockQuantity ?? item.quantity,
  zone: item.warehouseZone ?? item.zone,
  unit: item.unit || '个'
})

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
    const data = await getScrapPage({ pageNum: 1, pageSize: 9999 })
    if (data) {
      const list = data.records || data.list || data || []
      historyList.value = list
    }
  } catch (error) {
    console.error('加载历史记录失败:', error)
    ElMessage.error('加载报废记录失败，请稍后重试')
  } finally {
    historyLoading.value = false
  }
}

const validateMergedQuantities = () => {
  const merged = getMergedQuantities()
  const errors = []

  for (const [accessoryId, totalQty] of Object.entries(merged)) {
    const id = Number(accessoryId)
    const accessory = accessoryList.value.find((a) => a.id === id)
    if (!accessory) continue

    if (totalQty > accessory.quantity) {
      errors.push(
        `配件【${accessory.name} - ${accessory.model}】合并报废数量 ${totalQty} ${accessory.unit} 超过库存 ${accessory.quantity} ${accessory.unit}`
      )
    }
  }

  return errors
}

const handleScrapSubmit = async () => {
  if (!scrapFormRef.value) return
  await scrapFormRef.value.validate(async (valid) => {
    if (!valid) return

    const hasEmptyAccessory = scrapForm.items.some((item) => !item.accessoryId)
    if (hasEmptyAccessory) {
      ElMessage.warning('请选择所有行的配件')
      return
    }

    const hasEmptyReason = scrapForm.items.some((item) => !item.reason)
    if (hasEmptyReason) {
      ElMessage.warning('请选择所有行的报废原因')
      return
    }

    const mergeErrors = validateMergedQuantities()
    if (mergeErrors.length > 0) {
      ElMessage.error(mergeErrors.join('\n'))
      return
    }

    const merged = getMergedQuantities()
    const itemDetails = scrapForm.items
      .map((item) => {
        const acc = getRowAccessory(item)
        return {
          name: acc ? `${acc.name} - ${acc.model}` : '未知',
          qty: item.quantity,
          reason: item.reason,
          group: getReasonGroup(item.reason)
        }
      })
      .map((d) => `• ${d.name} × ${d.qty} [${d.group}/${d.reason}]`)
      .join('\n')

    const mergedDetails = Object.entries(merged)
      .map(([id, qty]) => {
        const acc = accessoryList.value.find((a) => a.id === Number(id))
        return `${acc ? acc.name : '未知'}: ${qty} / 库存 ${acc ? acc.quantity : 0}`
      })
      .join(', ')

    try {
      await ElMessageBox.confirm(
        `确认报废以下 ${scrapForm.items.length} 项配件？\n\n${itemDetails}\n\n合并数量: ${mergedDetails}\n\n经办人: ${scrapForm.operator}\n\n报废后库存将扣减且不可恢复。`,
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
      const items = scrapForm.items.map((item) => {
        const acc = getRowAccessory(item)
        const attachments = (item.attachments || [])
          .filter((att) => att.fileUrl && att.fileUrl.trim())
          .map((att) => ({
            fileUrl: att.fileUrl,
            fileName: att.fileName,
            fileType: att.fileType,
            description: att.description
          }))
        return {
          accessoryId: item.accessoryId,
          accessoryName: acc ? acc.name : '',
          accessoryModel: acc ? acc.model : '',
          quantity: item.quantity,
          reason: item.reason,
          reasonGroup: getReasonGroup(item.reason),
          remark: item.remark,
          agingPreCheckId: item.agingPreCheckId,
          attachments: attachments.length > 0 ? attachments : undefined
        }
      })

      await createScrapBatch({
        items,
        operator: scrapForm.operator,
        remark: scrapForm.remark
      })

      ElMessage.success(`成功报废 ${scrapForm.items.length} 项配件`)
      loadAccessoryList()
      loadHistory()
      resetScrapForm()
    } catch (error) {
      console.error('报废失败:', error)
      ElMessage.error(error?.response?.data?.message || '报废归档失败，请稍后重试')
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
  loadFromPreCheck()
})
</script>
