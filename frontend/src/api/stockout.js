import request from '@/utils/request'

export function getStockOutPage(params) {
  return request({
    url: '/stockout/page',
    method: 'get',
    params
  })
}

export function getStockOutList(params) {
  return request({
    url: '/stockout/list',
    method: 'get',
    params
  })
}

export function createStockOut(data) {
  return request({
    url: '/stockout',
    method: 'post',
    data
  })
}

export function deleteStockOut(id) {
  return request({
    url: `/stockout/${id}`,
    method: 'delete'
  })
}
