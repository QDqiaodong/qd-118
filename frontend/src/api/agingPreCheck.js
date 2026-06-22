import request from '@/utils/request'

export function getAgingPreCheckList(params) {
  return request({
    url: '/aging-pre-checks',
    method: 'get',
    params
  })
}

export function getAgingPreCheckById(id) {
  return request({
    url: `/aging-pre-checks/${id}`,
    method: 'get'
  })
}

export function getAgingPreCheckByAccessoryId(accessoryId) {
  return request({
    url: `/aging-pre-checks/accessory/${accessoryId}`,
    method: 'get'
  })
}

export function getPendingScrapList() {
  return request({
    url: '/aging-pre-checks/pending-scrap',
    method: 'get'
  })
}

export function createAgingPreCheck(data) {
  return request({
    url: '/aging-pre-checks',
    method: 'post',
    data
  })
}

export function createAgingPreCheckBatch(data) {
  return request({
    url: '/aging-pre-checks/batch',
    method: 'post',
    data
  })
}

export function updateAgingPreCheck(data) {
  return request({
    url: '/aging-pre-checks',
    method: 'put',
    data
  })
}

export function deleteAgingPreCheck(id) {
  return request({
    url: `/aging-pre-checks/${id}`,
    method: 'delete'
  })
}

export function calculateOverallLevel(data) {
  return request({
    url: '/aging-pre-checks/calculate',
    method: 'post',
    data
  })
}
