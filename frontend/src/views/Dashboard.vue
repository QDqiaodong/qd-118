<template>
  <div>
    <div class="stat-cards">
      <div v-for="card in statCards" :key="card.key" class="stat-card">
        <div class="stat-icon" :style="{ background: card.gradient }">
          <el-icon :size="28"><component :is="card.icon" /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-label">
            <span>{{ card.label }}</span>
            <el-popover
              placement="bottom-start"
              :width="280"
              trigger="hover"
              popper-class="caliber-popover"
            >
              <template #reference>
                <el-icon class="caliber-icon" :size="14"><QuestionFilled /></el-icon>
              </template>
              <div class="caliber-panel">
                <div class="caliber-title">
                  <el-icon><InfoFilled /></el-icon>
                  <span>统计口径</span>
                </div>
                <div class="caliber-scope">{{ card.caliber.scope }}</div>
                <ul class="caliber-list">
                  <li v-for="(item, idx) in card.caliber.items" :key="idx">{{ item }}</li>
                </ul>
                <div v-if="card.caliber.note" class="caliber-note">{{ card.caliber.note }}</div>
              </div>
            </el-popover>
          </div>
          <div class="stat-value">{{ formatNumber(card.value) }}</div>
        </div>
      </div>
    </div>

    <div class="chart-container">
      <div class="chart-title">
        <el-icon><TrendCharts /></el-icon>
        <span class="ml-8">库存 Top 5 配件</span>
      </div>
      <div class="bar-chart">
        <div v-for="(item, index) in top5List" :key="index" class="bar-row">
          <div class="bar-label" :title="item.name">{{ item.name }}</div>
          <div class="bar-track">
            <div
              class="bar-fill"
              :style="{
                width: getBarWidth(item.stock) + '%',
                background: getBarGradient(index)
              }"
            >
              <span class="bar-text">{{ formatNumber(item.stock) }}</span>
            </div>
          </div>
        </div>
        <el-empty v-if="top5List.length === 0" description="暂无数据" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getDashboardStats, getTop5Accessories } from '@/api/dashboard'
import { ElMessage } from 'element-plus'

const stats = ref({
  accessoryCount: 0,
  totalStock: 0,
  monthStockOut: 0,
  monthScrap: 0
})

const statCards = computed(() => [
  {
    key: 'accessoryCount',
    label: '配件种类总数',
    value: stats.value.accessoryCount,
    icon: 'Goods',
    gradient: 'linear-gradient(135deg, #409eff, #66b1ff)',
    caliber: {
      scope: '统计口径：配件档案（SKU）记录数',
      items: [
        '系统内所有已录入的配件种类，按"条目"计数',
        '包含库存为 0 或已停用的配件',
        '不按库存数量累计，仅统计种类数'
      ],
      note: '与"库存总数量"不同：本指标只数种类，不数件数。'
    }
  },
  {
    key: 'totalStock',
    label: '库存总数量',
    value: stats.value.totalStock,
    icon: 'Box',
    gradient: 'linear-gradient(135deg, #67c23a, #85ce61)',
    caliber: {
      scope: '统计口径：各配件现存库存量之和',
      items: [
        '全部配件"库存数量"字段的累加值',
        '出库、报废时已扣减的库存不再计入',
        '含库存为 0 的配件'
      ],
      note: '单位随配件而定（个 / 箱 / 米等），此处为件数合计。'
    }
  },
  {
    key: 'monthStockOut',
    label: '本月领用数量',
    value: stats.value.monthStockOut,
    icon: 'Sell',
    gradient: 'linear-gradient(135deg, #e6a23c, #ebb563)',
    caliber: {
      scope: '统计口径：本月出库（领用）记录数量之和',
      items: [
        '统计周期：本月 1 日 00:00 至当前时刻',
        '归属字段：出库时间（outTime）',
        '按领用数量累计，不按金额计算'
      ],
      note: '按出库时间归属当月；新增 / 编辑 / 删除出库记录后实时刷新。'
    }
  },
  {
    key: 'monthScrap',
    label: '本月报废数量',
    value: stats.value.monthScrap,
    icon: 'Delete',
    gradient: 'linear-gradient(135deg, #f56c6c, #f78989)',
    caliber: {
      scope: '统计口径：本月报废记录数量之和',
      items: [
        '统计周期：本月 1 日 00:00 至当前时刻',
        '归属字段：报废时间（scrapTime）',
        '仅统计已生效的报废记录'
      ],
      note: '报废会同步扣减库存，因此也会影响"库存总数量"。'
    }
  }
])

const top5List = ref([])
const maxStock = ref(0)

const formatNumber = (num) => {
  if (num === null || num === undefined || isNaN(num)) return 0
  return Number(num).toLocaleString('zh-CN')
}

const getBarWidth = (stock) => {
  if (maxStock.value === 0) return 0
  const percent = (stock / maxStock.value) * 100
  return Math.max(percent, 5)
}

const gradients = [
  'linear-gradient(90deg, #409eff, #66b1ff)',
  'linear-gradient(90deg, #67c23a, #85ce61)',
  'linear-gradient(90deg, #e6a23c, #ebb563)',
  'linear-gradient(90deg, #f56c6c, #f78989)',
  'linear-gradient(90deg, #909399, #a6a9ad)'
]

const getBarGradient = (index) => gradients[index % gradients.length]

const loadStats = async () => {
  try {
    const data = await getDashboardStats()
    if (data) {
      stats.value = {
        accessoryCount: data.accessoryCount ?? 0,
        totalStock: data.totalStock ?? 0,
        monthStockOut: data.monthStockOut ?? 0,
        monthScrap: data.monthScrap ?? 0
      }
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

const loadTop5 = async () => {
  try {
    const data = await getTop5Accessories()
    if (data && Array.isArray(data)) {
      top5List.value = data
      if (data.length > 0) {
        maxStock.value = Math.max(...data.map((item) => item.stock || 0))
      }
    }
  } catch (error) {
    console.error('加载Top5数据失败:', error)
  }
}

const loadMockData = () => {
  stats.value = {
    accessoryCount: 128,
    totalStock: 58632,
    monthStockOut: 3245,
    monthScrap: 156
  }
  top5List.value = [
    { name: '六类非屏蔽网线 305米/箱', stock: 12500 },
    { name: 'CAT6 RJ45 水晶头 100个/盒', stock: 8620 },
    { name: '24口配线架 1U机架式', stock: 5280 },
    { name: 'PVC线管 φ20 3米/根', stock: 4120 },
    { name: '光纤跳线 LC-LC 双芯 3米', stock: 3050 }
  ]
  maxStock.value = 12500
}

onMounted(() => {
  loadStats()
  loadTop5()
  if (top5List.value.length === 0 && stats.value.accessoryCount === 0) {
    setTimeout(() => {
      if (top5List.value.length === 0 && stats.value.accessoryCount === 0) {
        loadMockData()
      }
    }, 800)
  }
})
</script>

<style scoped lang="scss">
.stat-label {
  display: flex;
  align-items: center;
  gap: 6px;
}

.caliber-icon {
  color: #c0c4cc;
  cursor: help;
  transition: color 0.2s ease;

  &:hover {
    color: #409eff;
  }
}
</style>
