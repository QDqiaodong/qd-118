import request from '@/utils/request'

const mapAccessoryFields = (item) => {
  const mapped = {
    ...item,
    quantity: item.stockQuantity ?? item.quantity,
    zone: item.warehouseZone ?? item.zone
  }
  if (typeof mapped.categoryPath === 'string' && mapped.categoryPath) {
    mapped.categoryPath = mapped.categoryPath.split('/')
  } else if (!mapped.categoryPath) {
    mapped.categoryPath = []
  }
  return mapped
}

const mapAccessoryList = (data) => {
  if (!data) return data
  if (Array.isArray(data)) {
    return data.map(mapAccessoryFields)
  }
  if (data.records && Array.isArray(data.records)) {
    return { ...data, records: data.records.map(mapAccessoryFields) }
  }
  if (data.list && Array.isArray(data.list)) {
    return { ...data, list: data.list.map(mapAccessoryFields) }
  }
  return data
}

export function getAccessoryPage(params) {
  return request({
    url: '/accessories',
    method: 'get',
    params
  }).then((data) => mapAccessoryList(data))
}

export function getAccessoryList(params) {
  return request({
    url: '/accessories',
    method: 'get',
    params
  }).then((data) => mapAccessoryList(data))
}

export function getAccessoryById(id) {
  return request({
    url: `/accessories/${id}`,
    method: 'get'
  }).then((data) => data ? mapAccessoryFields(data) : data)
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
      stockQuantity: data.quantity ?? data.stockQuantity,
      warehouseZone: data.zone ?? data.warehouseZone,
      unit: data.unit
    }
  }).then((res) => res ? mapAccessoryFields(res) : res)
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
      stockQuantity: data.quantity ?? data.stockQuantity,
      warehouseZone: data.zone ?? data.warehouseZone,
      unit: data.unit
    }
  }).then((res) => res ? mapAccessoryFields(res) : res)
}

export function deleteAccessory(id) {
  return request({
    url: `/accessories/${id}`,
    method: 'delete'
  })
}
