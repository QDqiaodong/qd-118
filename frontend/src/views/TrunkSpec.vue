<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-title">
        <el-icon><Grid /></el-icon>
        <span class="ml-8">线槽规格对照面板</span>
      </div>
      <div style="display: flex; gap: 12px">
        <el-input v-model="searchKeyword" placeholder="搜索规格、型号" clearable style="width: 240px" :prefix-icon="Search" />
        <el-button type="primary" @click="loadData">
          <el-icon><Refresh /></el-icon>
          <span class="ml-8">刷新数据</span>
        </el-button>
      </div>
    </div>

    <div class="category-section" v-for="category in filteredCategories" :key="category.categoryType" v-loading="loading">
      <div class="category-header" :class="'category-' + category.categoryType">
        <el-icon :size="20"><Menu /></el-icon>
        <span class="category-title">{{ category.categoryName }}</span>
        <span class="category-count">共 {{ category.items.length }} 种规格</span>
      </div>

      <el-table :data="category.items" border stripe style="width: 100%" class="spec-table">
        <el-table-column type="index" label="序号" width="70" align="center" />
        <el-table-column prop="name" label="名称" min-width="160" show-overflow-tooltip />
        <el-table-column prop="model" label="型号" width="140" show-overflow-tooltip />
        <el-table-column prop="spec" label="完整规格" width="140" show-overflow-tooltip />
        <el-table-column v-if="category.categoryType !== 'corrugated'" prop="width" label="宽度" width="100" align="center">
          <template #default="{ row }">
            <el-tag type="primary" size="small">{{ row.width || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="category.categoryType !== 'corrugated'" prop="height" label="高度" width="100" align="center">
          <template #default="{ row }">
            <el-tag type="success" size="small">{{ row.height || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="category.categoryType === 'corrugated'" prop="diameter" label="直径" width="120" align="center">
          <template #default="{ row }">
            <el-tag type="warning" size="small">{{ row.diameter || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="unit" label="单位" width="80" align="center" />
        <el-table-column label="库存数量" width="120" align="center">
          <template #default="{ row }">
            <span :class="{ 'stock-low': row.stockQuantity < 50, 'stock-normal': row.stockQuantity >= 50 }">
              {{ formatNumber(row.stockQuantity) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="warehouseZone" label="库区" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="zoneTagType(row.warehouseZone)" size="small">{{ row.warehouseZone || '-' }}</el-tag>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="category.items.length === 0" description="暂无该分类数据" :image-size="80" />
    </div>

    <div class="summary-cards">
      <div class="summary-card">
        <div class="summary-icon" style="background: linear-gradient(135deg, #409eff, #66b1ff)">
          <el-icon :size="24"><Collection /></el-icon>
        </div>
        <div class="summary-info">
          <div class="summary-label">PVC方形线槽</div>
          <div class="summary-value">{{ pvcTotalCount }} 种</div>
          <div class="summary-sub">库存合计: {{ formatNumber(pvcTotalStock) }} 米</div>
        </div>
      </div>
      <div class="summary-card">
        <div class="summary-icon" style="background: linear-gradient(135deg, #67c23a, #85ce61)">
          <el-icon :size="24"><Box /></el-icon>
        </div>
        <div class="summary-info">
          <div class="summary-label">金属桥架</div>
          <div class="summary-value">{{ metalTotalCount }} 种</div>
          <div class="summary-sub">库存合计: {{ formatNumber(metalTotalStock) }} 米</div>
        </div>
      </div>
      <div class="summary-card">
        <div class="summary-icon" style="background: linear-gradient(135deg, #e6a23c, #ebb563)">
          <el-icon :size="24"><CircleCheck /></el-icon>
        </div>
        <div class="summary-info">
          <div class="summary-label">波纹管</div>
          <div class="summary-value">{{ corrugatedTotalCount }} 种</div>
          <div class="summary-sub">库存合计: {{ formatNumber(corrugatedTotalStock) }} 米</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getTrunkSpecCompare } from '@/api/trunkSpec'

const loading = ref(false)
const searchKeyword = ref('')
const specData = ref([])

const formatNumber = (num) => {
  if (num === null || num === undefined || isNaN(num)) return 0
  return Number(num).toLocaleString('zh-CN')
}

const zoneTagType = (zone) => {
  if (!zone) return 'info'
  const prefix = zone.charAt(0)
  const map = { A: 'primary', B: 'success', C: 'warning', D: 'danger' }
  return map[prefix] || 'info'
}

const filteredCategories = computed(() => {
  if (!searchKeyword.value) return specData.value

  const kw = searchKeyword.value.toLowerCase()
  return specData.value.map(category => ({
    ...category,
    items: category.items.filter(item =>
      item.name?.toLowerCase().includes(kw) ||
      item.model?.toLowerCase().includes(kw) ||
      item.spec?.toLowerCase().includes(kw)
    )
  })).filter(category => category.items.length > 0)
})

const pvcCategory = computed(() => specData.value.find(c => c.categoryType === 'pvc'))
const metalCategory = computed(() => specData.value.find(c => c.categoryType === 'metal'))
const corrugatedCategory = computed(() => specData.value.find(c => c.categoryType === 'corrugated'))

const pvcTotalCount = computed(() => pvcCategory.value?.items.length || 0)
const metalTotalCount = computed(() => metalCategory.value?.items.length || 0)
const corrugatedTotalCount = computed(() => corrugatedCategory.value?.items.length || 0)

const pvcTotalStock = computed(() => pvcCategory.value?.items.reduce((sum, item) => sum + (item.stockQuantity || 0), 0) || 0)
const metalTotalStock = computed(() => metalCategory.value?.items.reduce((sum, item) => sum + (item.stockQuantity || 0), 0) || 0)
const corrugatedTotalStock = computed(() => corrugatedCategory.value?.items.reduce((sum, item) => sum + (item.stockQuantity || 0), 0) || 0)

const loadData = async () => {
  loading.value = true
  try {
    const data = await getTrunkSpecCompare()
    if (data && Array.isArray(data)) {
      specData.value = data
    }
  } catch (error) {
    console.error('加载线槽规格数据失败:', error)
    ElMessage.error('加载线槽规格数据失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style lang="scss" scoped>
.category-section {
  margin-bottom: 24px;
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.category-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 16px 20px;
  color: #fff;

  .category-title {
    font-size: 18px;
    font-weight: 600;
  }

  .category-count {
    font-size: 13px;
    opacity: 0.85;
    margin-left: 8px;
  }

  &.category-pvc {
    background: linear-gradient(90deg, #409eff, #66b1ff);
  }

  &.category-metal {
    background: linear-gradient(90deg, #67c23a, #85ce61);
  }

  &.category-corrugated {
    background: linear-gradient(90deg, #e6a23c, #ebb563);
  }
}

.spec-table {
  border-radius: 0;
}

.stock-low {
  color: #f56c6c;
  font-weight: 600;
}

.stock-normal {
  color: #67c23a;
  font-weight: 600;
}

.summary-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 16px;
  margin-top: 8px;
}

.summary-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

  .summary-icon {
    width: 56px;
    height: 56px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    flex-shrink: 0;
  }

  .summary-info {
    flex: 1;

    .summary-label {
      font-size: 14px;
      color: #909399;
      margin-bottom: 4px;
    }

    .summary-value {
      font-size: 24px;
      font-weight: 600;
      color: #2c3e50;
      margin-bottom: 2px;
    }

    .summary-sub {
      font-size: 12px;
      color: #c0c4cc;
    }
  }
}
</style>
