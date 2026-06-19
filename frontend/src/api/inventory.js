import request from '@/utils/request'

export function getInventoryPage(params) {
  return request({
    url: '/inventory-checks',
    method: 'get',
    params
  })
}

export function createInventory(data) {
  return request({
    url: '/inventory-checks',
    method: 'post',
    data
  })
}

export function getInventoryDetail(id) {
  return request({
    url: `/inventory-checks/${id}`,
    method: 'get'
  })
}
