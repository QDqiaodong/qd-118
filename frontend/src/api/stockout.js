import request from '@/utils/request'

export function getStockOutPage(params) {
  return request({
    url: '/stock-outs',
    method: 'get',
    params
  })
}

export function getStockOutList(params) {
  return request({
    url: '/stock-outs',
    method: 'get',
    params
  })
}

export function createStockOut(data) {
  return request({
    url: '/stock-outs',
    method: 'post',
    data
  })
}

export function deleteStockOut(id) {
  return request({
    url: `/stock-outs/${id}`,
    method: 'delete'
  })
}
