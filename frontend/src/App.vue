<template>
  <el-container class="layout-container">
    <el-aside :width="isCollapse ? '64px' : '220px'" class="layout-aside">
      <div class="logo">
        <el-icon :size="28"><Tools /></el-icon>
        <span v-show="!isCollapse" class="logo-text">弱电库房管理</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :collapse-transition="false"
        router
        background-color="#001529"
        text-color="#b7bdc6"
        active-text-color="#409eff"
        class="layout-menu"
      >
        <el-menu-item index="/">
          <el-icon><DataAnalysis /></el-icon>
          <template #title>仪表盘</template>
        </el-menu-item>
        <el-menu-item index="/category">
          <el-icon><Menu /></el-icon>
          <template #title>配件分类管理</template>
        </el-menu-item>
        <el-menu-item index="/workshop-usage">
          <el-icon><SetUp /></el-icon>
          <template #title>领用用途字典</template>
        </el-menu-item>
        <el-menu-item index="/category-tree">
          <el-icon><Share /></el-icon>
          <template #title>分类树维护台</template>
        </el-menu-item>
        <el-menu-item index="/accessory">
          <el-icon><Goods /></el-icon>
          <template #title>配件档案录入</template>
        </el-menu-item>
        <el-menu-item index="/stockout">
          <el-icon><Sell /></el-icon>
          <template #title>车间领用出库</template>
        </el-menu-item>
        <el-menu-item index="/inventory">
          <el-icon><Tickets /></el-icon>
          <template #title>库房清点</template>
        </el-menu-item>
        <el-menu-item index="/inventory-wizard">
          <el-icon><Guide /></el-icon>
          <template #title>清点向导</template>
        </el-menu-item>
        <el-menu-item index="/trunk-spec">
          <el-icon><Collection /></el-icon>
          <template #title>线槽规格对照</template>
        </el-menu-item>
        <el-menu-item index="/scrap">
          <el-icon><Delete /></el-icon>
          <template #title>报废归档</template>
        </el-menu-item>
        <el-menu-item index="/warehouse-map">
          <el-icon><Grid /></el-icon>
          <template #title>货位图</template>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="layout-header">
        <div class="header-left">
          <el-icon class="collapse-btn" :size="20" @click="toggleCollapse">
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
          <span class="header-title">{{ currentTitle }}</span>
        </div>
        <div class="header-right">
          <el-avatar :size="32" class="avatar">A</el-avatar>
          <span class="username">管理员</span>
        </div>
      </el-header>

      <el-main class="layout-main">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const isCollapse = ref(false)

const activeMenu = computed(() => route.path)
const currentTitle = computed(() => route.meta.title || '弱电布线配件库房管理系统')

const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}
</script>

<style lang="scss" scoped>
.layout-container {
  height: 100vh;
}

.layout-aside {
  background-color: #001529;
  transition: width 0.3s;
  overflow: hidden;

  .logo {
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 10px;
    color: #fff;
    border-bottom: 1px solid #1f3a5c;

    .logo-text {
      font-size: 16px;
      font-weight: 600;
      white-space: nowrap;
    }
  }

  .layout-menu {
    border-right: none;
    height: calc(100vh - 60px);
  }
}

.layout-header {
  background-color: #fff;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);

  .header-left {
    display: flex;
    align-items: center;
    gap: 16px;

    .collapse-btn {
      cursor: pointer;
      color: #5a6072;
      transition: color 0.3s;

      &:hover {
        color: #409eff;
      }
    }

    .header-title {
      font-size: 18px;
      font-weight: 600;
      color: #2c3e50;
    }
  }

  .header-right {
    display: flex;
    align-items: center;
    gap: 10px;

    .avatar {
      background: linear-gradient(135deg, #409eff, #67c23a);
    }

    .username {
      color: #5a6072;
      font-size: 14px;
    }
  }
}

.layout-main {
  background-color: #f0f2f5;
  padding: 20px;
  overflow-y: auto;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
