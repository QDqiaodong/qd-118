import request from '@/utils/request'

export function getTrunkSpecCompare() {
  return request({
    url: '/trunk-spec/compare',
    method: 'get'
  })
}

export function getZoneInventoryStats(zone) {
  return request({
    url: `/trunk-spec/zone-inventory/${zone}`,
    method: 'get'
  })
}

export function getAllZones() {
  return request({
    url: '/trunk-spec/zones',
    method: 'get'
  })
}

export function createWizardBatchInventory(data) {
  return request({
    url: '/inventory-checks/wizard-batch',
    method: 'post',
    data
  })
}
