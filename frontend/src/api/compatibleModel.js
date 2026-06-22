import request from '@/utils/request'

export function getCompatibleModelGroupList() {
  return request({
    url: '/compatible-model-groups',
    method: 'get'
  })
}

export function getCompatibleModelGroupById(id) {
  return request({
    url: `/compatible-model-groups/${id}`,
    method: 'get'
  })
}

export function getCompatibleModelGroupsByGroup(groupName) {
  return request({
    url: `/compatible-model-groups/by-group/${groupName}`,
    method: 'get'
  })
}

export function getCompatibleModelGroupsByModel(model) {
  return request({
    url: '/compatible-model-groups/by-model',
    method: 'get',
    params: { model }
  })
}

export function getCompatibleModelGroupsByAccessory(accessoryId) {
  return request({
    url: `/compatible-model-groups/by-accessory/${accessoryId}`,
    method: 'get'
  })
}

export function getGroupNames() {
  return request({
    url: '/compatible-model-groups/group-names',
    method: 'get'
  })
}

export function validateCompatibleModelGroup(data) {
  return request({
    url: '/compatible-model-groups/validate',
    method: 'post',
    data
  })
}

export function createCompatibleModelGroup(data) {
  return request({
    url: '/compatible-model-groups',
    method: 'post',
    data
  })
}

export function updateCompatibleModelGroup(data) {
  return request({
    url: '/compatible-model-groups',
    method: 'put',
    data
  })
}

export function deleteCompatibleModelGroup(id) {
  return request({
    url: `/compatible-model-groups/${id}`,
    method: 'delete'
  })
}

export function getAccessoryCompatibleGroup(accessoryId) {
  return request({
    url: `/accessories/${accessoryId}/compatible-group`,
    method: 'get'
  })
}

export function getCompatibleByModel(model) {
  return request({
    url: '/accessories/by-model/compatible',
    method: 'get',
    params: { model }
  })
}
