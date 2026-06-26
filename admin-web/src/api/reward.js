import request from '@/utils/request'

export function getRewardCategories() {
  return request({
    url: '/rewards/categories',
    method: 'get'
  })
}

export function getRewardList(params) {
  return request({
    url: '/rewards',
    method: 'get',
    params
  })
}

export function getRewardDetail(id) {
  return request({
    url: '/rewards/' + id,
    method: 'get'
  })
}

export function addReward(data) {
  return request({
    url: '/rewards',
    method: 'post',
    data
  })
}

export function updateReward(id, data) {
  return request({
    url: '/rewards/' + id,
    method: 'put',
    data
  })
}

export function deleteReward(id) {
  return request({
    url: '/rewards/' + id,
    method: 'delete'
  })
}

export function getExchangeRecords(params) {
  return request({
    url: '/exchange/records',
    method: 'get',
    params
  })
}

export function exchangeReward(data) {
  return request({
    url: '/exchange',
    method: 'post',
    data
  })
}

export function deliverExchange(id) {
  return request({
    url: '/exchange/' + id + '/deliver',
    method: 'post'
  })
}

export function cancelExchange(id) {
  return request({
    url: '/exchange/' + id + '/cancel',
    method: 'post'
  })
}

export function getAchievementTree(childId) {
  return request({
    url: '/achievements/tree',
    method: 'get',
    params: { childId }
  })
}
