import request from '@/utils/request'

export function previewAgingBatch(params) {
  return request({
    url: '/aging-batches/preview',
    method: 'post',
    data: params
  })
}

export function createAgingBatch(data) {
  return request({
    url: '/aging-batches',
    method: 'post',
    data
  })
}

export function updateAgingBatch(data) {
  return request({
    url: '/aging-batches',
    method: 'put',
    data
  })
}

export function deleteAgingBatch(id) {
  return request({
    url: `/aging-batches/${id}`,
    method: 'delete'
  })
}

export function getAgingBatchById(id) {
  return request({
    url: `/aging-batches/${id}`,
    method: 'get'
  })
}

export function getAgingBatchList(params) {
  return request({
    url: '/aging-batches',
    method: 'get',
    params
  })
}

export function getAgingBatchItems(id) {
  return request({
    url: `/aging-batches/${id}/items`,
    method: 'get'
  })
}
