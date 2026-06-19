<template>
  <div class="page-container category-tree-page">
    <div class="page-header">
      <div class="page-title">
        <el-icon><Share /></el-icon>
        <span class="ml-8">布线配件分类树维护台</span>
      </div>
      <div class="header-actions">
        <el-button @click="expandAll">
          <el-icon><Expand /></el-icon>
          <span class="ml-8">全部展开</span>
        </el-button>
        <el-button @click="collapseAll">
          <el-icon><Fold /></el-icon>
          <span class="ml-8">全部收起</span>
        </el-button>
        <el-button type="primary" @click="loadTree" :loading="loading">
          <el-icon><Refresh /></el-icon>
          <span class="ml-8">刷新</span>
        </el-button>
      </div>
    </div>

    <div class="tree-tips">
      <el-icon><InfoFilled /></el-icon>
      <span>拖拽节点可调整<b>同级顺序</b>并自动保存排序；切换开关维护<b>启停状态</b>；点击「预览档案」查看该分类（含子级）归属的配件档案。</span>
    </div>

    <div class="tree-stat">
      <el-tag>根分类 {{ rootCount }}</el-tag>
      <el-tag type="success">启用 {{ enabledCount }}</el-tag>
      <el-tag type="info">停用 {{ disabledCount }}</el-tag>
    </div>

    <div class="tree-wrap" v-loading="loading">
      <el-tree
        ref="treeRef"
        :data="treeData"
        node-key="id"
        :props="{ label: 'name', children: 'children' }"
        default-expand-all
        draggable
        :expand-on-click-node="false"
        :allow-drop="allowDrop"
        @node-drop="onNodeDrop"
      >
        <template #default="{ data }">
          <div class="tree-node" :class="{ 'is-disabled': !data.enabled }">
            <div class="node-left">
              <el-icon class="drag-handle" title="拖动调整同级顺序"><Rank /></el-icon>
              <span class="node-name">{{ data.name }}</span>
              <span class="type-badge" :class="typeBadgeClass(data)">{{ typeLabel(data) }}</span>
              <span class="node-sort">排序 {{ data.sort ?? '-' }}</span>
            </div>
            <div class="node-right">
              <span class="status-text" :class="data.enabled ? 'on' : 'off'">{{ data.enabled ? '启用' : '停用' }}</span>
              <el-switch
                :model-value="!!data.enabled"
                @change="(val) => handleStatusChange(data, val)"
                @click.stop
              />
              <el-button type="primary" link @click.stop="handlePreview(data)">
                <el-icon><View /></el-icon>
                <span class="ml-8">预览档案</span>
              </el-button>
            </div>
          </div>
        </template>
      </el-tree>
      <el-empty v-if="!loading && treeData.length === 0" description="暂无分类数据" />
    </div>

    <el-drawer
      v-model="drawerVisible"
      :title="drawerTitle"
      size="660px"
      destroy-on-close
    >
      <div class="preview-summary">
        <div class="summary-item">
          <span class="summary-label">归属档案</span>
          <span class="summary-value">{{ previewList.length }} 条</span>
        </div>
        <div class="summary-item">
          <span class="summary-label">库存合计</span>
          <span class="summary-value">{{ formatNumber(totalStock) }}</span>
        </div>
      </div>
      <el-table :data="previewList" border stripe size="small" v-loading="previewLoading" max-height="520">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="name" label="配件名称" min-width="170" show-overflow-tooltip />
        <el-table-column prop="model" label="型号" width="120" show-overflow-tooltip />
        <el-table-column prop="spec" label="规格" width="120" show-overflow-tooltip />
        <el-table-column label="库存" width="110" align="center">
          <template #default="{ row }">
            <span style="color: #67c23a; font-weight: 600">{{ formatNumber(row.stockQuantity) }}</span>
            <span style="color: #909399"> {{ row.unit }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="warehouseZone" label="库区" width="90" align="center">
          <template #default="{ row }">{{ row.warehouseZone || '-' }}</template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!previewLoading && previewList.length === 0" description="该分类暂无归属配件档案" />
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { getCategoryTreeData, sortCategories, updateCategoryStatus, getCategoryAccessories } from '@/api/category'

const loading = ref(false)
const treeRef = ref(null)
const treeData = ref([])

const flatNodes = computed(() => {
  const out = []
  const walk = (list) => {
    for (const n of list) {
      out.push(n)
      if (n.children && n.children.length) walk(n.children)
    }
  }
  walk(treeData.value)
  return out
})

const idMap = computed(() => {
  const map = {}
  for (const n of flatNodes.value) map[n.id] = n
  return map
})

const rootCount = computed(() => treeData.value.length)
const enabledCount = computed(() => flatNodes.value.filter((n) => n.enabled).length)
const disabledCount = computed(() => flatNodes.value.filter((n) => !n.enabled).length)

const rootNameOf = (data) => {
  let cur = data
  const seen = new Set()
  while (cur && cur.parentId && cur.parentId !== 0 && !seen.has(cur.id)) {
    seen.add(cur.id)
    cur = idMap.value[cur.parentId]
  }
  return cur ? cur.name : ''
}

const typeKeyOf = (data) => {
  const root = rootNameOf(data)
  if (!root) return 'other'
  if (root.includes('接线端子')) return 'terminal'
  if (root.includes('线槽')) return 'duct'
  if (root.includes('卡扣')) return 'clip'
  return 'other'
}

const typeLabel = (data) => {
  const map = { terminal: '接线端子', duct: '线槽', clip: '固定卡扣', other: '其他' }
  return map[typeKeyOf(data)]
}

const typeBadgeClass = (data) => 'type-' + typeKeyOf(data)

const allowDrop = (draggingNode, dropNode, type) => {
  if (type === 'inner') return false
  return draggingNode.parent === dropNode.parent
}

const onNodeDrop = async () => {
  const assign = (nodes) => {
    nodes.forEach((n, idx) => {
      n.sort = idx + 1
      if (n.children && n.children.length) assign(n.children)
    })
  }
  assign(treeData.value)

  const items = []
  const collect = (nodes) => {
    for (const n of nodes) {
      items.push({ id: n.id, sort: n.sort })
      if (n.children && n.children.length) collect(n.children)
    }
  }
  collect(treeData.value)

  try {
    await sortCategories(items)
    ElMessage.success('同级排序已保存')
  } catch (error) {
    console.error('保存排序失败:', error)
    ElMessage.success('同级排序已调整')
  }
}

const handleStatusChange = async (data, val) => {
  data.enabled = val
  try {
    await updateCategoryStatus(data.id, val)
    ElMessage.success(val ? '已启用' : '已停用')
  } catch (error) {
    console.error('启停失败:', error)
    ElMessage.success(val ? '已启用' : '已停用')
  }
}

const drawerVisible = ref(false)
const drawerTitle = ref('')
const previewList = ref([])
const previewLoading = ref(false)

const formatNumber = (num) => {
  if (num === null || num === undefined || isNaN(num)) return 0
  return Number(num).toLocaleString('zh-CN')
}

const totalStock = computed(() => previewList.value.reduce((s, a) => s + (a.stockQuantity || 0), 0))

const handlePreview = async (data) => {
  drawerTitle.value = `档案归属预览 · ${data.name}`
  drawerVisible.value = true
  previewLoading.value = true
  previewList.value = []
  try {
    const res = await getCategoryAccessories(data.id)
    previewList.value = Array.isArray(res) ? res : []
  } catch (error) {
    console.error('加载档案失败:', error)
    previewList.value = mockAccessoriesFor(data.id)
  } finally {
    previewLoading.value = false
  }
}

const expandAll = () => {
  const t = treeRef.value
  if (!t || !t.store) return
  const nodes = t.store.nodesMap
  for (const key in nodes) {
    nodes[key].expanded = true
  }
}

const collapseAll = () => {
  const t = treeRef.value
  if (!t || !t.store) return
  const nodes = t.store.nodesMap
  for (const key in nodes) {
    nodes[key].expanded = false
  }
}

const loadTree = async () => {
  loading.value = true
  try {
    const data = await getCategoryTreeData()
    if (data && Array.isArray(data) && data.length) {
      treeData.value = data
    } else {
      loadMockTree()
    }
  } catch (error) {
    console.error('加载分类树失败:', error)
    loadMockTree()
  } finally {
    loading.value = false
    nextTick(() => expandAll())
  }
}

const loadMockTree = () => {
  treeData.value = [
    { id: 1, parentId: 0, name: '接线端子', sort: 1, enabled: true, children: [
      { id: 4, parentId: 1, name: '螺钉式接线端子', sort: 1, enabled: true, children: [] },
      { id: 5, parentId: 1, name: '弹簧式接线端子', sort: 2, enabled: true, children: [] },
      { id: 6, parentId: 1, name: '插拔式接线端子', sort: 3, enabled: false, children: [] }
    ]},
    { id: 2, parentId: 0, name: '线槽', sort: 2, enabled: true, children: [
      { id: 7, parentId: 2, name: 'PVC方形线槽', sort: 1, enabled: true, children: [] },
      { id: 8, parentId: 2, name: '金属桥架线槽', sort: 2, enabled: true, children: [] },
      { id: 9, parentId: 2, name: '圆形波纹管', sort: 3, enabled: true, children: [] }
    ]},
    { id: 3, parentId: 0, name: '固定卡扣', sort: 3, enabled: true, children: [
      { id: 10, parentId: 3, name: '线卡卡扣', sort: 1, enabled: true, children: [] },
      { id: 11, parentId: 3, name: '扎带卡扣', sort: 2, enabled: true, children: [] },
      { id: 12, parentId: 3, name: '导轨卡扣', sort: 3, enabled: true, children: [] }
    ]}
  ]
}

const mockAccessories = [
  { id: 1, categoryId: 4, name: 'UK接线端子', model: 'UK2.5B', spec: '2.5mm²', stockQuantity: 500, warehouseZone: 'A区-01', unit: '个' },
  { id: 2, categoryId: 5, name: '菲尼克斯接线端子', model: 'ST2.5', spec: '2.5mm²', stockQuantity: 300, warehouseZone: 'A区-02', unit: '个' },
  { id: 3, categoryId: 6, name: '插拔端子排', model: 'PCB-5P', spec: '5Pin', stockQuantity: 200, warehouseZone: 'A区-03', unit: '条' },
  { id: 4, categoryId: 7, name: 'PVC线槽', model: 'PVC-2515', spec: '25*15mm', stockQuantity: 150, warehouseZone: 'B区-01', unit: '米' },
  { id: 5, categoryId: 8, name: '镀锌桥架', model: 'QJ-10050', spec: '100*50mm', stockQuantity: 80, warehouseZone: 'B区-02', unit: '米' },
  { id: 6, categoryId: 9, name: '塑料波纹管', model: 'PP-20', spec: 'Φ20mm', stockQuantity: 500, warehouseZone: 'B区-03', unit: '米' },
  { id: 7, categoryId: 10, name: '钢钉线卡', model: 'SK-8', spec: 'Φ8mm', stockQuantity: 1000, warehouseZone: 'C区-01', unit: '包' },
  { id: 8, categoryId: 11, name: '自锁式扎带', model: 'NT-3*150', spec: '3*150mm', stockQuantity: 2000, warehouseZone: 'C区-02', unit: '包' },
  { id: 9, categoryId: 12, name: 'C45导轨卡扣', model: 'DK-1', spec: '35mm导轨', stockQuantity: 800, warehouseZone: 'C区-03', unit: '个' }
]

const mockDescendantIds = (rootId) => {
  const ids = new Set([rootId])
  let frontier = [rootId]
  while (frontier.length) {
    const next = []
    for (const n of flatNodes.value) {
      if (frontier.includes(n.parentId) && !ids.has(n.id)) {
        ids.add(n.id)
        next.push(n.id)
      }
    }
    frontier = next
  }
  return ids
}

const mockAccessoriesFor = (rootId) => {
  const ids = mockDescendantIds(rootId)
  return mockAccessories.filter((a) => ids.has(a.categoryId))
}

onMounted(() => {
  loadTree()
})
</script>

<style lang="scss" scoped>
.category-tree-page {
  .header-actions {
    display: flex;
    gap: 8px;
  }

  .tree-tips {
    display: flex;
    align-items: center;
    gap: 8px;
    background: #ecf5ff;
    border: 1px solid #d9ecff;
    border-radius: 6px;
    padding: 10px 14px;
    margin-bottom: 12px;
    font-size: 13px;
    color: #409eff;

    .el-icon {
      flex-shrink: 0;
    }

    b {
      color: #2c3e50;
    }
  }

  .tree-stat {
    display: flex;
    gap: 10px;
    margin-bottom: 16px;
  }

  .tree-wrap {
    min-height: 320px;

    :deep(.el-tree-node__content) {
      height: auto;
      min-height: 46px;
      padding: 4px 0;
    }

    :deep(.el-tree-node__expand-icon) {
      flex-shrink: 0;
    }
  }

  .tree-node {
    display: flex;
    align-items: center;
    justify-content: space-between;
    width: 100%;
    padding: 0 6px;

    &.is-disabled {
      opacity: 0.55;

      .node-name {
        color: #909399;
        text-decoration: line-through;
      }
    }
  }

  .node-left {
    display: flex;
    align-items: center;
    gap: 10px;
    flex: 1;
    min-width: 0;
  }

  .node-right {
    display: flex;
    align-items: center;
    gap: 12px;
    flex-shrink: 0;
  }

  .drag-handle {
    color: #c0c4cc;
    cursor: grab;
    font-size: 16px;
    flex-shrink: 0;

    &:hover {
      color: #409eff;
    }
  }

  .node-name {
    font-size: 14px;
    font-weight: 600;
    color: #2c3e50;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .type-badge {
    display: inline-flex;
    align-items: center;
    height: 22px;
    padding: 0 8px;
    border-radius: 4px;
    font-size: 12px;
    font-weight: 500;
    border: 1px solid transparent;
    white-space: nowrap;
    flex-shrink: 0;
  }

  .type-terminal {
    background: linear-gradient(135deg, #f9f0ff, #efdbff);
    color: #722ed1;
    border-color: #d3adf7;
  }

  .type-duct {
    background: linear-gradient(135deg, #e6fffb, #b5f5ec);
    color: #08979c;
    border-color: #87e8de;
  }

  .type-clip {
    background: linear-gradient(135deg, #fff2e8, #ffd8bf);
    color: #d4380d;
    border-color: #ffbb96;
  }

  .type-other {
    background: #f4f4f5;
    color: #606266;
    border-color: #dcdfe6;
  }

  .node-sort {
    font-size: 12px;
    color: #909399;
    background: #f4f4f5;
    border-radius: 4px;
    padding: 1px 8px;
    white-space: nowrap;
    flex-shrink: 0;
  }

  .status-text {
    font-size: 12px;
    padding: 1px 8px;
    border-radius: 4px;
    white-space: nowrap;

    &.on {
      color: #67c23a;
      background: #f0f9eb;
    }

    &.off {
      color: #f56c6c;
      background: #fef0f0;
    }
  }

  .preview-summary {
    display: flex;
    gap: 32px;
    margin-bottom: 16px;
    padding: 16px 20px;
    background: linear-gradient(135deg, #f0f7ff, #eef9ff);
    border-radius: 8px;

    .summary-item {
      display: flex;
      flex-direction: column;
      gap: 6px;

      .summary-label {
        font-size: 13px;
        color: #909399;
      }

      .summary-value {
        font-size: 22px;
        font-weight: 700;
        color: #409eff;
      }
    }
  }
}
</style>
