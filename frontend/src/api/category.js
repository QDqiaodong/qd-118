import request from '@/utils/request'

export function getCategoryTree() {
  return request({
    url: '/category/tree',
    method: 'get'
  })
}

export function getCategoryList() {
  return request({
    url: '/category/list',
    method: 'get'
  })
}

export function addCategory(data) {
  return request({
    url: '/category',
    method: 'post',
    data
  })
}

export function updateCategory(data) {
  return request({
    url: '/category',
    method: 'put',
    data
  })
}

export function deleteCategory(id) {
  return request({
    url: `/category/${id}`,
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

