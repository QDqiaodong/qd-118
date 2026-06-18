<template>
  <div>
    <div class="stat-cards">
      <div class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #409eff, #66b1ff)">
          <el-icon :size="28"><Goods /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-label">配件种类总数</div>
          <div class="stat-value">{{ formatNumber(stats.accessoryCount) }}</div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #67c23a, #85ce61)">
          <el-icon :size="28"><Box /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-label">库存总数量</div>
          <div class="stat-value">{{ formatNumber(stats.totalStock) }}</div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #e6a23c, #ebb563)">
          <el-icon :size="28"><Sell /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-label">本月领用数量</div>
          <div class="stat-value">{{ formatNumber(stats.monthStockOut) }}</div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #f56c6c, #f78989)">
          <el-icon :size="28"><Delete /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-label">本月报废数量</div>
          <div class="stat-value">{{ formatNumber(stats.monthScrap) }}</div>
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
import { ref, onMounted } from 'vue'
import { getDashboardStats, getTop5Accessories } from '@/api/dashboard'
import { ElMessage } from 'element-plus'

const stats = ref({
  accessoryCount: 0,
  totalStock: 0,
  monthStockOut: 0,
  monthScrap: 0
})

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
