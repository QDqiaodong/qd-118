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
    path: '/workshop-usage',
    name: 'WorkshopUsage',
    component: () => import('@/views/WorkshopUsage.vue'),
    meta: { title: '车间领用用途管理' }
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
    path: '/project-requisition',
    name: 'ProjectRequisition',
    component: () => import('@/views/ProjectRequisition.vue'),
    meta: { title: '施工项目领用' }
  },
  {
    path: '/inventory',
    name: 'Inventory',
    component: () => import('@/views/Inventory.vue'),
    meta: { title: '半年度库房清点' }
  },
  {
    path: '/inventory-wizard',
    name: 'InventoryWizard',
    component: () => import('@/views/InventoryWizard.vue'),
    meta: { title: '半年度清点向导' }
  },
  {
    path: '/trunk-spec',
    name: 'TrunkSpec',
    component: () => import('@/views/TrunkSpec.vue'),
    meta: { title: '线槽规格对照面板' }
  },
  {
    path: '/scrap',
    name: 'Scrap',
    component: () => import('@/views/Scrap.vue'),
    meta: { title: '老化配件报废归档' }
  },
  {
    path: '/aging-batch',
    name: 'AgingBatch',
    component: () => import('@/views/AgingBatch.vue'),
    meta: { title: '老化批次归档' }
  },
  {
    path: '/warehouse-map',
    name: 'WarehouseMap',
    component: () => import('@/views/WarehouseMap.vue'),
    meta: { title: '库房分区货位图' }
  },
  {
    path: '/compatible-model',
    name: 'CompatibleModel',
    component: () => import('@/views/CompatibleModel.vue'),
    meta: { title: '兼容型号库' }
  },
  {
    path: '/duct-offcut',
    name: 'DuctOffcut',
    component: () => import('@/views/DuctOffcut.vue'),
    meta: { title: '线槽裁切余料管理' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
