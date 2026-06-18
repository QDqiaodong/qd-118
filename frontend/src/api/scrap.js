import request from '@/utils/request'

export function getScrapPage(params) {
  return request({
    url: '/scrap/page',
    method: 'get',
    params
  })
}

export function getScrapList(params) {
  return request({
    url: '/scrap/list',
    method: 'get',
    params
  })
}

export function createScrap(data) {
  return request({
    url: '/scrap',
    method: 'post',
    data
  })
}

export function deleteScrap(id) {
  return request({
    url: `/scrap/${id}`,
    method: 'delete'
  })
}
