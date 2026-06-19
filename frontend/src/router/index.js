import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Dashboard',
    component: () => import('@/views/Dashboard.vue'),
    meta: { title: '仪表盘' }
  },
  {
    path: '/category',
    name: 'Category',
    component: () => import('@/views/Category.vue'),
    meta: { title: '配件分类管理' }
  },
  {
    path: '/category-tree',
    name: 'CategoryTree',
    component: () => import('@/views/CategoryTree.vue'),
    meta: { title: '分类树维护台' }
  },
  {
    path: '/accessory',
    name: 'Accessory',
    component: () => import('@/views/Accessory.vue'),
    meta: { title: '布线配件档案录入' }
  },
  {
    path: '/stockout',
    name: 'StockOut',
    component: () => import('@/views/StockOut.vue'),
    meta: { title: '车间领用出库登记' }
  },
  {
    path: '/inventory',
    name: 'Inventory',
    component: () => import('@/views/Inventory.vue'),
    meta: { title: '半年度库房清点' }
  },
  {
    path: '/scrap',
    name: 'Scrap',
    component: () => import('@/views/Scrap.vue'),
    meta: { title: '老化配件报废归档' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
