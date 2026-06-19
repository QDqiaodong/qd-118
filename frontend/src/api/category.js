import request from '@/utils/request'

export function getCategoryTree() {
  return request({
    url: '/categories/tree',
    method: 'get'
  })
}

export function getCategoryList() {
  return request({
    url: '/categories',
    method: 'get'
  })
}

export function addCategory(data) {
  return request({
    url: '/categories',
    method: 'post',
    data
  })
}

export function updateCategory(data) {
  return request({
    url: '/categories',
    method: 'put',
    data
  })
}

export function deleteCategory(id) {
  return request({
    url: `/categories/${id}`,
    method: 'delete'
  })
}

export function getCategoryTreeData() {
  return request({
    url: '/categories/tree',
    method: 'get'
  })
}

export function sortCategories(items) {
  return request({
    url: '/categories/sort',
    method: 'put',
    data: items
  })
}

export function updateCategoryStatus(id, enabled) {
  return request({
    url: `/categories/${id}/status`,
    method: 'patch',
    params: { enabled }
  })
}

export function getCategoryAccessories(id) {
  return request({
    url: `/categories/${id}/accessories`,
    method: 'get'
  })
}

