import request from '@/utils/request'

export function getDashboardStats() {
  return request({
    url: '/dashboard/stats',
    method: 'get'
  })
}

export function getTop5Accessories() {
  return request({
    url: '/dashboard/top5',
    method: 'get'
  })
}
