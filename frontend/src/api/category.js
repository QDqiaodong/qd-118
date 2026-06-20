import request from '@/utils/request'

const mapCategoryFields = (item) => ({
  ...item,
  sortOrder: item.sort ?? item.sortOrder
})

const mapCategoryTreeFields = (list) => {
  if (!list || !Array.isArray(list)) return list
  return list.map((item) => {
    const mapped = mapCategoryFields(item)
    if (mapped.children && mapped.children.length > 0) {
      mapped.children = mapCategoryTreeFields(mapped.children)
    }
    return mapped
  })
}

export function getCategoryTree() {
  return request({
    url: '/categories/tree',
    method: 'get'
  }).then((data) => mapCategoryTreeFields(data))
}

export function getCategoryList() {
  return request({
    url: '/categories',
    method: 'get'
  }).then((data) => data && data.map ? data.map(mapCategoryFields) : data)
}

export function addCategory(data) {
  const payload = {
    ...data,
    sort: data.sortOrder ?? data.sort
  }
  return request({
    url: '/categories',
    method: 'post',
    data: payload
  }).then((res) => mapCategoryFields(res))
}

export function updateCategory(data) {
  const payload = {
    ...data,
    sort: data.sortOrder ?? data.sort
  }
  return request({
    url: '/categories',
    method: 'put',
    data: payload
  }).then((res) => mapCategoryFields(res))
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
  }).then((data) => mapCategoryTreeFields(data))
}

export function sortCategories(items) {
  const payload = items && items.map ? items.map((item) => ({
    ...item,
    sort: item.sortOrder ?? item.sort
  })) : items
  return request({
    url: '/categories/sort',
    method: 'put',
    data: payload
  })
}

export function updateCategoryStatus(id, enabled) {
  return request({
    url: `/categories/${id}/status`,
    method: 'patch',
    params: { enabled }
  }).then((res) => mapCategoryFields(res))
}

export function getCategoryAccessories(id) {
  return request({
    url: `/categories/${id}/accessories`,
    method: 'get'
  })
}

