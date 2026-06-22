import request from '@/utils/request'

export function getConstructionProjectList() {
  return request({
    url: '/construction-projects',
    method: 'get'
  })
}

export function getConstructionProjectActive() {
  return request({
    url: '/construction-projects/active',
    method: 'get'
  })
}

export function getConstructionProjectById(id) {
  return request({
    url: `/construction-projects/${id}`,
    method: 'get'
  })
}

export function addConstructionProject(data) {
  return request({
    url: '/construction-projects',
    method: 'post',
    data
  })
}

export function updateConstructionProject(data) {
  return request({
    url: '/construction-projects',
    method: 'put',
    data
  })
}

export function deleteConstructionProject(id) {
  return request({
    url: `/construction-projects/${id}`,
    method: 'delete'
  })
}

export function updateConstructionProjectStatus(id, status) {
  return request({
    url: `/construction-projects/${id}/status`,
    method: 'patch',
    params: { status }
  })
}

export function getProjectConsumption(projectId) {
  return request({
    url: `/construction-projects/${projectId}/consumption`,
    method: 'get'
  })
}

export function getAllProjectConsumption() {
  return request({
    url: '/construction-projects/consumption',
    method: 'get'
  })
}
