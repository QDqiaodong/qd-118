import request from '@/utils/request'

export function getWarehouseZoneStats() {
  return request({
    url: '/warehouse/zone-stats',
    method: 'get'
  })
}
