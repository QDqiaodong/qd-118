import request from '@/utils/request'

export function getScrapPage(params) {
  return request({
    url: '/scrap-records',
    method: 'get',
    params
  })
}

export function getScrapList(params) {
  return request({
    url: '/scrap-records',
    method: 'get',
    params
  })
}

export function createScrap(data) {
  return request({
    url: '/scrap-records',
    method: 'post',
    data
  })
}

export function createScrapBatch(data) {
  return request({
    url: '/scrap-records/batch',
    method: 'post',
    data
  })
}

export function deleteScrap(id) {
  return request({
    url: `/scrap-records/${id}`,
    method: 'delete'
  })
}
