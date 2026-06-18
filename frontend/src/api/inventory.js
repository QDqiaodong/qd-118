import request from '@/utils/request'

export function getInventoryPage(params) {
  return request({
    url: '/inventory/page',
    method: 'get',
    params
  })
}

export function createInventory(data) {
  return request({
    url: '/inventory',
    method: 'post',
    data
  })
}

export function getInventoryDetail(id) {
  return request({
    url: `/inventory/${id}`,
    method: 'get'
  })
}
