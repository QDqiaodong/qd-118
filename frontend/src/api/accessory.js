import request from '@/utils/request'

export function getAccessoryPage(params) {
  return request({
    url: '/accessories',
    method: 'get',
    params
  })
}

export function getAccessoryList(params) {
  return request({
    url: '/accessories',
    method: 'get',
    params
  })
}

export function getAccessoryById(id) {
  return request({
    url: `/accessories/${id}`,
    method: 'get'
  })
}

export function addAccessory(data) {
  return request({
    url: '/accessories',
    method: 'post',
    data: {
      name: data.name,
      model: data.model,
      spec: data.spec,
      categoryId: data.categoryId,
      categoryName: data.categoryName,
      stockQuantity: data.quantity,
      warehouseZone: data.zone,
      unit: data.unit
    }
  })
}

export function updateAccessory(data) {
  return request({
    url: '/accessories',
    method: 'put',
    data: {
      id: data.id,
      name: data.name,
      model: data.model,
      spec: data.spec,
      categoryId: data.categoryId,
      categoryName: data.categoryName,
      stockQuantity: data.quantity,
      warehouseZone: data.zone,
      unit: data.unit
    }
  })
}

export function deleteAccessory(id) {
  return request({
    url: `/accessories/${id}`,
    method: 'delete'
  })
}
