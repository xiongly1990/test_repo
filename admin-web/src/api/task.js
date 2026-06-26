import request from '@/utils/request'

export function getTaskList(childId) {
  return request({
    url: '/tasks',
    method: 'get',
    params: { childId }
  })
}

export function getTodayTasks(childId) {
  return request({
    url: '/tasks/today',
    method: 'get',
    params: { childId }
  })
}

export function getTaskDetail(id) {
  return request({
    url: '/tasks/' + id,
    method: 'get'
  })
}

export function addTask(data) {
  return request({
    url: '/tasks',
    method: 'post',
    data
  })
}

export function updateTask(id, data) {
  return request({
    url: '/tasks/' + id,
    method: 'put',
    data
  })
}

export function deleteTask(id) {
  return request({
    url: '/tasks/' + id,
    method: 'delete'
  })
}
