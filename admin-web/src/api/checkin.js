import request from '@/utils/request'

export function getPendingList(params) {
  return request({
    url: '/checkin/pending',
    method: 'get',
    params
  })
}

export function getCheckinRecords(params) {
  return request({
    url: '/checkin/records',
    method: 'get',
    params
  })
}

export function approveCheckin(id, remark) {
  return request({
    url: '/checkin/' + id + '/approve',
    method: 'post',
    data: { remark }
  })
}

export function rejectCheckin(id, remark) {
  return request({
    url: '/checkin/' + id + '/reject',
    method: 'post',
    data: { remark }
  })
}

export function supplementCheckin(data) {
  return request({
    url: '/checkin/supplement',
    method: 'post',
    data
  })
}

export function getSupplementRecords(params) {
  return request({
    url: '/checkin/supplement-records',
    method: 'get',
    params
  })
}

export function getStreak(childId) {
  return request({
    url: '/checkin/streak/' + childId,
    method: 'get'
  })
}
