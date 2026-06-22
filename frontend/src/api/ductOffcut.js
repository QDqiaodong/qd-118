import request from '@/utils/request'

export function getDuctOffcutList() {
  return request({
    url: '/duct-offcuts',
    method: 'get'
  })
}

export function getDuctOffcutById(id) {
  return request({
    url: `/duct-offcuts/${id}`,
    method: 'get'
  })
}

export function getDuctOffcutsByAccessory(accessoryId) {
  return request({
    url: `/duct-offcuts/by-accessory/${accessoryId}`,
    method: 'get'
  })
}

export function getDuctOffcutsByStatus(status) {
  return request({
    url: `/duct-offcuts/by-status/${status}`,
    method: 'get'
  })
}

export function createDuctOffcut(data) {
  return request({
    url: '/duct-offcuts',
    method: 'post',
    data
  })
}

export function returnDuctOffcut(data) {
  return request({
    url: '/duct-offcuts/return',
    method: 'post',
    data
  })
}

export function deleteDuctOffcut(id) {
  return request({
    url: `/duct-offcuts/${id}`,
    method: 'delete'
  })
}
