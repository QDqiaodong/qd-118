<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-title">
        <el-icon><Search /></el-icon>
        <span class="ml-8">老化预检登记</span>
      </div>
    </div>

    <el-card class="mb-16" shadow="never" style="margin-bottom: 20px; border: 1px solid #ebeef5; border-radius: 8px">
      <template #header>
        <div style="display: flex; align-items: center; justify-content: space-between">
          <div style="display: flex; align-items: center; font-weight: 600">
            <el-icon><WarningFilled /></el-icon>
            <span class="ml-8">预检登记</span>
          </div>
          <div style="display: flex; gap: 12px">
            <el-button type="primary" size="small" :icon="Plus" @click="addPreCheckItem">
              添加预检项
            </el-button>
            <el-button type="success" size="small" :icon="Guide" @click="loadPendingScrap">
              加载待报废 ({{ pendingScrapCount }})
            </el-button>
          </div>
        </div>
      </template>

      <el-form ref="preCheckFormRef" :model="preCheckForm" :rules="preCheckFormRules" label-width="100px">
        <el-table :data="preCheckForm.items" border style="width: 100%; margin-bottom: 16px">
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
          <el-table-column label="当前库存" width="100" align="center">
            <template #default="{ row }">
              <el-tag v-if="getRowAccessory(row)" type="info" effect="plain" size="small">
                {{ formatNumber(getAvailableStock(row.accessoryId)) }} {{ getRowAccessory(row).unit }}
              </el-tag>
              <span v-else style="color: #c0c4cc">-</span>
            </template>
          </el-table-column>
          <el-table-column label="发黄等级" width="140" align="center">
            <template #default="{ row, $index }">
              <el-form-item
                :prop="`items.${$index}.yellowingLevel`"
                :rules="{ required: true, message: '请选择等级', trigger: 'change' }"
                style="margin-bottom: 0"
              >
                <el-rate
                  v-model="row.yellowingLevel"
                  :max="5"
                  :colors="['#67c23a', '#67c23a', '#e6a23c', '#f56c6c', '#f56c6c']"
                  :texts="levelTexts"
                  show-text
                  size="large"
                  @change="handleLevelChange(row)"
                />
              </el-form-item>
            </template>
          </el-table-column>
          <el-table-column label="脆裂等级" width="140" align="center">
            <template #default="{ row, $index }">
              <el-form-item
                :prop="`items.${$index}.crackingLevel`"
                :rules="{ required: true, message: '请选择等级', trigger: 'change' }"
                style="margin-bottom: 0"
              >
                <el-rate
                  v-model="row.crackingLevel"
                  :max="5"
                  :colors="['#67c23a', '#67c23a', '#e6a23c', '#f56c6c', '#f56c6c']"
                  :texts="levelTexts"
                  show-text
                  size="large"
                  @change="handleLevelChange(row)"
                />
              </el-form-item>
            </template>
          </el-table-column>
          <el-table-column label="金属氧化等级" width="140" align="center">
            <template #default="{ row, $index }">
              <el-form-item
                :prop="`items.${$index}.oxidationLevel`"
                :rules="{ required: true, message: '请选择等级', trigger: 'change' }"
                style="margin-bottom: 0"
              >
                <el-rate
                  v-model="row.oxidationLevel"
                  :max="5"
                  :colors="['#67c23a', '#67c23a', '#e6a23c', '#f56c6c', '#f56c6c']"
                  :texts="levelTexts"
                  show-text
                  size="large"
                  @change="handleLevelChange(row)"
                />
              </el-form-item>
            </template>
          </el-table-column>
          <el-table-column label="综合等级" width="120" align="center">
            <template #default="{ row }">
              <el-tag
                v-if="row.overallLevel !== undefined && row.overallLevel !== null"
                :type="getLevelTagType(row.overallLevel)"
                size="large"
                effect="dark"
              >
                {{ row.overallLevel }} 级
                <span v-if="row.thresholdReached" style="margin-left: 4px">⚠</span>
              </el-tag>
              <span v-else style="color: #c0c4cc">-</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag
                v-if="row.thresholdReached"
                type="danger"
                effect="dark"
                size="small"
              >
                待报废
              </el-tag>
              <el-tag v-else type="success" size="small">
                正常
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="180" align="center" fixed="right">
            <template #default="{ row, $index }">
              <el-button
                v-if="row.thresholdReached && !row.scrapRecordId"
                type="danger"
                link
                size="small"
                :icon="Delete"
                @click="handleQuickScrap(row, $index)"
              >
                立即报废
              </el-button>
              <el-button
                v-if="row.scrapRecordId"
                type="success"
                link
                size="small"
                disabled
              >
                已报废
              </el-button>
              <el-button
                type="danger"
                link
                size="small"
                :icon="Close"
                :disabled="preCheckForm.items.length <= 1"
                @click="removePreCheckItem($index)"
              />
            </template>
          </el-table-column>
        </el-table>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="经办人" prop="operator">
              <el-input v-model="preCheckForm.operator" placeholder="请输入经办人" maxlength="20" />
            </el-form-item>
          </el-col>
          <el-col :span="16">
            <el-form-item label="详细说明" prop="remark">
              <el-input
                v-model="preCheckForm.remark"
                type="textarea"
                :rows="2"
                maxlength="200"
                show-word-limit
                placeholder="请输入预检详细说明（可选）"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item style="margin-bottom: 0">
          <el-button type="primary" :loading="submitting" @click="handlePreCheckSubmit">
            <el-icon><Check /></el-icon>
            <span class="ml-8">提交预检登记 ({{ preCheckForm.items.length }} 项)</span>
          </el-button>
          <el-button @click="resetPreCheckForm">
            <el-icon><RefreshLeft /></el-icon>
            <span class="ml-8">重置</span>
          </el-button>
          <el-button
            v-if="thresholdReachedCount > 0"
            type="danger"
            :icon="Warning"
            @click="handleBatchScrap"
          >
            批量报废 ({{ thresholdReachedCount }} 项)
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card v-if="pendingScrapList.length > 0" class="mb-16" shadow="never" style="margin-bottom: 20px; border: 1px solid #ebeef5; border-radius: 8px">
      <template #header>
        <div style="display: flex; align-items: center; justify-content: space-between">
          <div style="display: flex; align-items: center; font-weight: 600; color: #f56c6c">
            <el-icon><WarningFilled /></el-icon>
            <span class="ml-8">待报废配件提醒</span>
            <el-tag type="danger" size="small" style="margin-left: 12px">{{ pendingScrapList.length }} 项</el-tag>
          </div>
          <el-button type="primary" size="small" @click="handleBatchScrapFromPending">
            全部转入报废
          </el-button>
        </div>
      </template>

      <el-table :data="pendingScrapList" border stripe style="width: 100%">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="accessoryName" label="配件名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="accessoryModel" label="型号" width="120" show-overflow-tooltip />
        <el-table-column prop="categoryName" label="分类" width="120" show-overflow-tooltip />
        <el-table-column prop="warehouseZone" label="库区" width="100" align="center" />
        <el-table-column label="发黄等级" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getLevelTagType(row.yellowingLevel)" size="small">{{ row.yellowingLevel }} 级</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="脆裂等级" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getLevelTagType(row.crackingLevel)" size="small">{{ row.crackingLevel }} 级</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="氧化等级" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getLevelTagType(row.oxidationLevel)" size="small">{{ row.oxidationLevel }} 级</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="综合等级" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getLevelTagType(row.overallLevel)" effect="dark" size="small">{{ row.overallLevel }} 级</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="stockQuantity" label="库存" width="80" align="center" />
        <el-table-column prop="operator" label="预检人" width="100" align="center" />
        <el-table-column prop="checkTime" label="预检时间" width="170" align="center" />
        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="danger" link size="small" @click="handleQuickScrapFromPending(row)">
              报废
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card shadow="never" style="border: 1px solid #ebeef5; border-radius: 8px">
      <template #header>
        <div style="display: flex; align-items: center; font-weight: 600">
          <el-icon><List /></el-icon>
          <span class="ml-8">预检历史记录</span>
        </div>
      </template>

      <el-table :data="filteredHistory" border stripe style="width: 100%" v-loading="historyLoading">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="accessoryName" label="配件名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="accessoryModel" label="型号" width="120" show-overflow-tooltip />
        <el-table-column prop="categoryName" label="分类" width="120" show-overflow-tooltip />
        <el-table-column prop="warehouseZone" label="库区" width="100" align="center" />
        <el-table-column label="发黄等级" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getLevelTagType(row.yellowingLevel)" size="small">{{ row.yellowingLevel }} 级</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="脆裂等级" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getLevelTagType(row.crackingLevel)" size="small">{{ row.crackingLevel }} 级</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="氧化等级" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getLevelTagType(row.oxidationLevel)" size="small">{{ row.oxidationLevel }} 级</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="综合等级" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getLevelTagType(row.overallLevel)" effect="dark" size="small">{{ row.overallLevel }} 级</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.scrapRecordId" type="success" size="small">已报废</el-tag>
            <el-tag v-else-if="row.thresholdReached" type="danger" size="small">待报废</el-tag>
            <el-tag v-else type="info" size="small">正常</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operator" label="预检人" width="100" align="center" />
        <el-table-column prop="checkTime" label="预检时间" width="170" align="center" />
        <el-table-column prop="remark" label="备注" min-width="160" show-overflow-tooltip />
        <el-table-column label="操作" width="140" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.thresholdReached && !row.scrapRecordId"
              type="danger"
              link
              size="small"
              @click="handleQuickScrapFromHistory(row)"
            >
              报废
            </el-button>
            <el-button type="danger" link size="small" @click="handleDeleteRecord(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        class="mt-16"
        background
        layout="total, sizes, prev, pager, next, jumper"
        :total="historyList.length"
        :page-sizes="[10, 20, 50, 100]"
        v-model:current-page="historyPage"
        v-model:page-size="historyPageSize"
      />
    </el-card>

    <el-dialog v-model="scrapDialogVisible" title="报废确认" width="600px" top="10vh">
      <el-form :model="scrapForm" :rules="scrapFormRules" ref="scrapFormRef" label-width="100px">
        <el-alert
          v-if="currentPreCheckItem"
          :title="`配件【${currentPreCheckItem.accessoryName} - ${currentPreCheckItem.accessoryModel}】综合老化等级 ${currentPreCheckItem.overallLevel} 级，已达到报废阈值，请确认报废信息。`"
          type="warning"
          show-icon
          style="margin-bottom: 16px"
        />

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="报废数量">
              <el-input-number
                v-model="scrapForm.quantity"
                :min="1"
                :max="scrapForm.maxQuantity"
                controls-position="right"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="报废原因" prop="reason">
              <el-select v-model="scrapForm.reason" style="width: 100%">
                <el-option label="自然老化" value="自然老化" />
                <el-option label="过期失效" value="过期失效" />
                <el-option label="物理损坏" value="物理损坏" />
                <el-option label="腐蚀损坏" value="腐蚀损坏" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="老化附件">
          <div style="margin-bottom: 8px">
            <el-button type="primary" size="small" :icon="Plus" @click="addAttachment">
              添加附件
            </el-button>
          </div>
          <div v-for="(att, index) in scrapForm.attachments" :key="index" style="margin-bottom: 8px; display: flex; gap: 8px">
            <el-input
              v-model="att.fileUrl"
              placeholder="请输入附件地址（图片/文档URL）"
              style="flex: 2"
            />
            <el-input
              v-model="att.description"
              placeholder="附件说明（可选）"
              style="flex: 1"
            />
            <el-button type="danger" link :icon="Delete" @click="removeAttachment(index)" />
          </div>
        </el-form-item>

        <el-form-item label="备注">
          <el-input
            v-model="scrapForm.remark"
            type="textarea"
            :rows="2"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="scrapDialogVisible = false">取消</el-button>
        <el-button type="danger" :loading="scrapSubmitting" @click="confirmScrap">
          确认报废
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search,
  WarningFilled,
  Plus,
  Guide,
  Delete,
  Close,
  Check,
  RefreshLeft,
  Warning,
  List
} from '@element-plus/icons-vue'
import { getAccessoryList } from '@/api/accessory'
import {
  getAgingPreCheckList,
  createAgingPreCheckBatch,
  deleteAgingPreCheck,
  getPendingScrapList,
  calculateOverallLevel
} from '@/api/agingPreCheck'
import { createScrap } from '@/api/scrap'

const router = useRouter()

const historyLoading = ref(false)
const submitting = ref(false)
const scrapSubmitting = ref(false)
const preCheckFormRef = ref(null)
const scrapFormRef = ref(null)

const accessoryList = ref([])
const historyList = ref([])
const pendingScrapList = ref([])
const historyPage = ref(1)
const historyPageSize = ref(10)
const scrapDialogVisible = ref(false)
const currentPreCheckItem = ref(null)

const levelTexts = ['无', '轻微', '轻度', '中度', '重度', '严重']
const thresholdLevel = 3

const preCheckForm = reactive({
  items: [
    {
      accessoryId: null,
      yellowingLevel: 0,
      crackingLevel: 0,
      oxidationLevel: 0,
      overallLevel: 0,
      thresholdReached: false,
      scrapRecordId: null,
      remark: ''
    }
  ],
  operator: '',
  remark: ''
})

const preCheckFormRules = {
  operator: [
    { required: true, message: '请输入经办人姓名', trigger: 'blur' },
    { min: 1, max: 20, message: '姓名长度 1-20 个字符', trigger: 'blur' }
  ]
}

const scrapForm = reactive({
  quantity: 1,
  maxQuantity: 0,
  reason: '自然老化',
  attachments: [],
  remark: '',
  agingPreCheckId: null,
  accessoryId: null
})

const scrapFormRules = {
  reason: [
    { required: true, message: '请选择报废原因', trigger: 'change' }
  ]
}

const pendingScrapCount = computed(() => pendingScrapList.value.length)

const thresholdReachedCount = computed(() => {
  return preCheckForm.items.filter((item) => item.thresholdReached && !item.scrapRecordId).length
})

const filteredHistory = computed(() => {
  const start = (historyPage.value - 1) * historyPageSize.value
  return historyList.value.slice(start, start + historyPageSize.value)
})

const formatNumber = (num) => {
  if (num === null || num === undefined || isNaN(num)) return 0
  return Number(num).toLocaleString('zh-CN')
}

const getLevelTagType = (level) => {
  if (level >= thresholdLevel) return 'danger'
  if (level >= 2) return 'warning'
  return 'success'
}

const getRowAccessory = (row) => {
  if (!row.accessoryId) return null
  return accessoryList.value.find((a) => a.id === row.accessoryId) || null
}

const getAvailableStock = (accessoryId) => {
  const accessory = accessoryList.value.find((a) => a.id === accessoryId)
  if (!accessory) return 0
  return accessory.quantity
}

const handleAccessoryChange = (row) => {
  const acc = getRowAccessory(row)
  if (acc) {
    row.accessoryName = acc.name
  }
}

const handleLevelChange = async (row) => {
  try {
    const data = await calculateOverallLevel({
      yellowingLevel: row.yellowingLevel,
      crackingLevel: row.crackingLevel,
      oxidationLevel: row.oxidationLevel
    })
    if (data) {
      row.overallLevel = data.overallLevel
      row.thresholdReached = data.thresholdReached
    }
  } catch (error) {
    console.error('计算等级失败:', error)
  }
}

const addPreCheckItem = () => {
  preCheckForm.items.push({
    accessoryId: null,
    yellowingLevel: 0,
    crackingLevel: 0,
    oxidationLevel: 0,
    overallLevel: 0,
    thresholdReached: false,
    scrapRecordId: null,
    remark: ''
  })
}

const removePreCheckItem = (index) => {
  if (preCheckForm.items.length <= 1) {
    ElMessage.warning('至少保留一项预检记录')
    return
  }
  preCheckForm.items.splice(index, 1)
}

const resetPreCheckForm = () => {
  preCheckForm.items = [
    {
      accessoryId: null,
      yellowingLevel: 0,
      crackingLevel: 0,
      oxidationLevel: 0,
      overallLevel: 0,
      thresholdReached: false,
      scrapRecordId: null,
      remark: ''
    }
  ]
  preCheckForm.operator = ''
  preCheckForm.remark = ''
  preCheckFormRef.value?.resetFields()
}

const loadAccessoryList = async () => {
  try {
    const data = await getAccessoryList({ pageNum: 1, pageSize: 9999 })
    if (data) {
      const list = data.records || data.list || data || []
      accessoryList.value = list.map((item) => ({
        ...item,
        quantity: item.stockQuantity ?? item.quantity,
        zone: item.warehouseZone ?? item.zone,
        unit: item.unit || '个'
      }))
    }
  } catch (error) {
    console.error('加载配件列表失败:', error)
    ElMessage.error('加载配件列表失败，请稍后重试')
  }
}

const loadHistory = async () => {
  historyLoading.value = true
  try {
    const data = await getAgingPreCheckList({ pageNum: 1, pageSize: 9999 })
    if (data) {
      const list = data.records || data.list || data || []
      historyList.value = list.sort((a, b) => new Date(b.checkTime) - new Date(a.checkTime))
    }
  } catch (error) {
    console.error('加载历史记录失败:', error)
    ElMessage.error('加载预检记录失败，请稍后重试')
  } finally {
    historyLoading.value = false
  }
}

const loadPendingScrap = async () => {
  try {
    const data = await getPendingScrapList()
    if (data) {
      pendingScrapList.value = data || []
    }
  } catch (error) {
    console.error('加载待报废列表失败:', error)
  }
}

const handlePreCheckSubmit = async () => {
  if (!preCheckFormRef.value) return
  await preCheckFormRef.value.validate(async (valid) => {
    if (!valid) return

    const hasEmptyAccessory = preCheckForm.items.some((item) => !item.accessoryId)
    if (hasEmptyAccessory) {
      ElMessage.warning('请选择所有行的配件')
      return
    }

    submitting.value = true
    try {
      const items = preCheckForm.items.map((item) => ({
        accessoryId: item.accessoryId,
        accessoryName: item.accessoryName,
        yellowingLevel: item.yellowingLevel,
        crackingLevel: item.crackingLevel,
        oxidationLevel: item.oxidationLevel,
        overallLevel: item.overallLevel,
        remark: item.remark
      }))

      await createAgingPreCheckBatch({
        items,
        operator: preCheckForm.operator,
        remark: preCheckForm.remark
      })

      ElMessage.success(`成功提交 ${preCheckForm.items.length} 项预检记录`)

      const reachedCount = preCheckForm.items.filter((item) => item.thresholdReached).length
      if (reachedCount > 0) {
        try {
          await ElMessageBox.confirm(
            `有 ${reachedCount} 项配件已达到报废阈值，是否立即前往报废归档？`,
            '报废提醒',
            {
              confirmButtonText: '前往报废',
              cancelButtonText: '稍后处理',
              type: 'warning'
            }
          )
          router.push('/scrap')
        } catch {
          // 用户选择稍后处理
        }
      }

      loadAccessoryList()
      loadHistory()
      loadPendingScrap()
      resetPreCheckForm()
    } catch (error) {
      console.error('提交失败:', error)
      ElMessage.error(error?.response?.data?.message || '提交失败，请稍后重试')
    } finally {
      submitting.value = false
    }
  })
}

const handleQuickScrap = (row, index) => {
  const acc = getRowAccessory(row)
  if (!acc) {
    ElMessage.warning('请先选择配件')
    return
  }

  currentPreCheckItem.value = row
  scrapForm.accessoryId = row.accessoryId
  scrapForm.quantity = 1
  scrapForm.maxQuantity = acc.quantity
  scrapForm.reason = '自然老化'
  scrapForm.attachments = []
  scrapForm.remark = `发黄${row.yellowingLevel}级/脆裂${row.crackingLevel}级/氧化${row.oxidationLevel}级，综合${row.overallLevel}级`
  scrapForm.agingPreCheckId = null
  scrapDialogVisible.value = true
}

const handleQuickScrapFromPending = (row) => {
  currentPreCheckItem.value = row
  scrapForm.accessoryId = row.accessoryId
  scrapForm.quantity = 1
  scrapForm.maxQuantity = row.stockQuantity
  scrapForm.reason = '自然老化'
  scrapForm.attachments = []
  scrapForm.remark = `发黄${row.yellowingLevel}级/脆裂${row.crackingLevel}级/氧化${row.oxidationLevel}级，综合${row.overallLevel}级`
  scrapForm.agingPreCheckId = row.id
  scrapDialogVisible.value = true
}

const handleQuickScrapFromHistory = (row) => {
  currentPreCheckItem.value = row
  scrapForm.accessoryId = row.accessoryId
  scrapForm.quantity = 1
  scrapForm.maxQuantity = row.stockQuantity
  scrapForm.reason = '自然老化'
  scrapForm.attachments = []
  scrapForm.remark = `发黄${row.yellowingLevel}级/脆裂${row.crackingLevel}级/氧化${row.oxidationLevel}级，综合${row.overallLevel}级`
  scrapForm.agingPreCheckId = row.id
  scrapDialogVisible.value = true
}

const handleBatchScrap = () => {
  const reachedItems = preCheckForm.items.filter((item) => item.thresholdReached && !item.scrapRecordId)
  if (reachedItems.length === 0) {
    ElMessage.info('没有达到报废阈值的配件')
    return
  }

  router.push({
    path: '/scrap',
    query: {
      fromPreCheck: 'true',
      items: JSON.stringify(reachedItems.map((item) => ({
        accessoryId: item.accessoryId,
        accessoryName: item.accessoryName,
        yellowingLevel: item.yellowingLevel,
        crackingLevel: item.crackingLevel,
        oxidationLevel: item.oxidationLevel,
        overallLevel: item.overallLevel
      })))
    }
  })
}

const handleBatchScrapFromPending = () => {
  if (pendingScrapList.value.length === 0) {
    ElMessage.info('没有待报废的配件')
    return
  }

  router.push({
    path: '/scrap',
    query: {
      fromPreCheck: 'true',
      items: JSON.stringify(pendingScrapList.value.map((item) => ({
        accessoryId: item.accessoryId,
        accessoryName: item.accessoryName,
        yellowingLevel: item.yellowingLevel,
        crackingLevel: item.crackingLevel,
        oxidationLevel: item.oxidationLevel,
        overallLevel: item.overallLevel,
        agingPreCheckId: item.id
      })))
    }
  })
}

const addAttachment = () => {
  scrapForm.attachments.push({
    fileUrl: '',
    fileName: '',
    fileType: 'image',
    description: ''
  })
}

const removeAttachment = (index) => {
  scrapForm.attachments.splice(index, 1)
}

const confirmScrap = async () => {
  if (!scrapFormRef.value) return
  await scrapFormRef.value.validate(async (valid) => {
    if (!valid) return

    scrapSubmitting.value = true
    try {
      const attachments = scrapForm.attachments
        .filter((att) => att.fileUrl && att.fileUrl.trim())
        .map((att) => ({
          fileUrl: att.fileUrl,
          fileName: att.fileName,
          fileType: att.fileType,
          description: att.description
        }))

      await createScrap({
        accessoryId: scrapForm.accessoryId,
        quantity: scrapForm.quantity,
        reason: scrapForm.reason,
        operator: preCheckForm.operator || '管理员',
        remark: scrapForm.remark,
        agingPreCheckId: scrapForm.agingPreCheckId,
        attachments
      })

      ElMessage.success('报废成功')
      scrapDialogVisible.value = false
      loadAccessoryList()
      loadHistory()
      loadPendingScrap()
    } catch (error) {
      console.error('报废失败:', error)
      ElMessage.error(error?.response?.data?.message || '报废失败，请稍后重试')
    } finally {
      scrapSubmitting.value = false
    }
  })
}

const handleDeleteRecord = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除该预检记录吗？`, '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }
  try {
    await deleteAgingPreCheck(row.id)
    ElMessage.success('删除成功')
    loadHistory()
    loadPendingScrap()
  } catch (error) {
    console.error('删除失败:', error)
    ElMessage.error('删除失败，请稍后重试')
  }
}

onMounted(() => {
  loadAccessoryList()
  loadHistory()
  loadPendingScrap()
})
</script>
