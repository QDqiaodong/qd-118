<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-title">
        <el-icon><Grid /></el-icon>
        <span class="ml-8">库房分区货位图</span>
      </div>
      <div style="display: flex; gap: 24px; font-size: 14px">
        <span>货位总数：<b style="color: #409eff">{{ totalZones }}</b> 个</span>
        <span>配件种类：<b style="color: #67c23a">{{ totalAccessoryCount }}</b> 种</span>
        <span>库存总量：<b style="color: #e6a23c">{{ formatNumber(totalStock) }}</b></span>
      </div>
    </div>

    <div class="zone-sections" v-loading="loading">
      <div v-for="zoneGroup in zoneGroups" :key="zoneGroup.zone" class="zone-section">
        <div class="zone-header" :class="'zone-' + zoneGroup.zone.toLowerCase()">
          <el-icon :size="20"><Location /></el-icon>
          <span class="zone-title">{{ zoneGroup.zone }}区</span>
          <span class="zone-subtitle">共 {{ zoneGroup.locations.length }} 个货位</span>
        </div>
        <div class="location-grid">
          <div
            v-for="location in zoneGroup.locations"
            :key="location.zone"
            class="location-card"
            :class="'location-' + zoneGroup.zone.toLowerCase()"
          >
            <div class="location-name">{{ location.zone }}</div>
            <div class="location-stats">
              <div class="stat-item">
                <div class="stat-icon-box" style="background: linear-gradient(135deg, #409eff, #66b1ff)">
                  <el-icon :size="16"><Goods /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-label">配件种类</div>
                  <div class="stat-value">{{ location.accessoryCount }} 种</div>
                </div>
              </div>
              <div class="stat-item">
                <div class="stat-icon-box" style="background: linear-gradient(135deg, #67c23a, #85ce61)">
                  <el-icon :size="16"><Box /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-label">库存总量</div>
                  <div class="stat-value">{{ formatNumber(location.totalStock) }}</div>
                </div>
              </div>
              <div class="stat-item">
                <div class="stat-icon-box" :style="getScrapGradient(location.scrapRatio)">
                  <el-icon :size="16"><Warning /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-label">老化报废占比</div>
                  <div class="stat-value" :style="{ color: getScrapColor(location.scrapRatio) }">
                    {{ location.scrapRatio }}%
                  </div>
                </div>
              </div>
            </div>
            <div class="scrap-bar">
              <div
                class="scrap-bar-fill"
                :style="{
                  width: Math.min(location.scrapRatio, 100) + '%',
                  background: getScrapGradient(location.scrapRatio)
                }"
              ></div>
            </div>
          </div>
          <div v-if="zoneGroup.locations.length === 0" class="empty-location">
            <el-empty description="暂无货位数据" :image-size="80" />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Grid, Location, Goods, Box, Warning } from '@element-plus/icons-vue'
import { getWarehouseZoneStats } from '@/api/warehouse'

const loading = ref(false)
const zoneStats = ref([])

const formatNumber = (num) => {
  if (num === null || num === undefined || isNaN(num)) return 0
  return Number(num).toLocaleString('zh-CN')
}

const getScrapColor = (ratio) => {
  if (ratio >= 10) return '#f56c6c'
  if (ratio >= 5) return '#e6a23c'
  return '#67c23a'
}

const getScrapGradient = (ratio) => {
  if (ratio >= 10) return 'linear-gradient(135deg, #f56c6c, #f78989)'
  if (ratio >= 5) return 'linear-gradient(135deg, #e6a23c, #ebb563)'
  return 'linear-gradient(135deg, #67c23a, #85ce61)'
}

const zoneGroups = computed(() => {
  const zones = ['A', 'B', 'C', 'D']
  return zones.map((zone) => {
    const locations = zoneStats.value
      .filter((item) => item.zone && item.zone.startsWith(zone))
      .sort((a, b) => {
        const numA = parseInt(a.zone.split('-')[1] || '0')
        const numB = parseInt(b.zone.split('-')[1] || '0')
        return numA - numB
      })
    return { zone, locations }
  })
})

const totalZones = computed(() => zoneStats.value.length)

const totalAccessoryCount = computed(() => {
  return zoneStats.value.reduce((sum, item) => sum + (item.accessoryCount || 0), 0)
})

const totalStock = computed(() => {
  return zoneStats.value.reduce((sum, item) => sum + (item.totalStock || 0), 0)
})

const loadData = async () => {
  loading.value = true
  try {
    const data = await getWarehouseZoneStats()
    if (data && Array.isArray(data)) {
      zoneStats.value = data
    }
  } catch (error) {
    console.error('加载货位统计失败:', error)
    ElMessage.error('加载货位统计失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style lang="scss" scoped>
.zone-sections {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.zone-section {
  background: #fafbfc;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #ebeef5;
}

.zone-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 16px 20px;
  color: #fff;

  .zone-title {
    font-size: 18px;
    font-weight: 600;
  }

  .zone-subtitle {
    font-size: 13px;
    opacity: 0.85;
    margin-left: 8px;
  }

  &.zone-a {
    background: linear-gradient(90deg, #409eff, #66b1ff);
  }

  &.zone-b {
    background: linear-gradient(90deg, #67c23a, #85ce61);
  }

  &.zone-c {
    background: linear-gradient(90deg, #e6a23c, #ebb563);
  }

  &.zone-d {
    background: linear-gradient(90deg, #f56c6c, #f78989);
  }
}

.location-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
  padding: 20px;
}

.location-card {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  border: 2px solid #ebeef5;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;

  &:hover {
    transform: translateY(-3px);
    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
  }

  &.location-a {
    border-top: 4px solid #409eff;
  }

  &.location-b {
    border-top: 4px solid #67c23a;
  }

  &.location-c {
    border-top: 4px solid #e6a23c;
  }

  &.location-d {
    border-top: 4px solid #f56c6c;
  }
}

.location-name {
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 14px;
  padding-bottom: 10px;
  border-bottom: 1px dashed #ebeef5;
}

.location-stats {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 14px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.stat-icon-box {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}

.stat-info {
  flex: 1;

  .stat-label {
    font-size: 12px;
    color: #909399;
    margin-bottom: 2px;
  }

  .stat-value {
    font-size: 15px;
    font-weight: 600;
    color: #2c3e50;
  }
}

.scrap-bar {
  height: 6px;
  background: #f4f4f5;
  border-radius: 3px;
  overflow: hidden;

  .scrap-bar-fill {
    height: 100%;
    border-radius: 3px;
    transition: width 0.6s ease;
  }
}

.empty-location {
  grid-column: 1 / -1;
  padding: 20px 0;
}
</style>
