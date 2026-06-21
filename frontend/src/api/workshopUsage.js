import request from '@/utils/request'

const mapFields = (item) => ({
  ...item,
  sortOrder: item.sort ?? item.sortOrder
})

const mapListFields = (list) => {
  if (!list || !Array.isArray(list)) return list
  return list.map(mapFields)
}

export function getWorkshopUsageList() {
  return request({
    url: '/workshop-usages',
    method: 'get'
  }).then((data) => mapListFields(data))
}

export function getWorkshopUsageEnabled() {
  return request({
    url: '/workshop-usages/enabled',
    method: 'get'
  }).then((data) => mapListFields(data))
}

export function getWorkshopUsageById(id) {
  return request({
    url: `/workshop-usages/${id}`,
    method: 'get'
  }).then((data) => mapFields(data))
}

export function addWorkshopUsage(data) {
  const payload = {
    ...data,
    sort: data.sortOrder ?? data.sort
  }
  return request({
    url: '/workshop-usages',
    method: 'post',
    data: payload
  }).then((res) => mapFields(res))
}

export function updateWorkshopUsage(data) {
  const payload = {
    ...data,
    sort: data.sortOrder ?? data.sort
  }
  return request({
    url: '/workshop-usages',
    method: 'put',
    data: payload
  }).then((res) => mapFields(res))
}

export function deleteWorkshopUsage(id) {
  return request({
    url: `/workshop-usages/${id}`,
    method: 'delete'
  })
}

export function updateWorkshopUsageStatus(id, enabled) {
  return request({
    url: `/workshop-usages/${id}/status`,
    method: 'patch',
    params: { enabled }
  }).then((res) => mapFields(res))
}
