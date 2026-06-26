import request from '@/utils/request'

export function getChildList(params) {
  return request({
    url: '/children',
    method: 'get',
    params
  })
}

export function getChildDetail(id) {
  return request({
    url: '/children/' + id,
    method: 'get'
  })
}

export function addChild(data) {
  return request({
    url: '/children',
    method: 'post',
    data
  })
}

export function updateChild(id, data) {
  return request({
    url: '/children/' + id,
    method: 'put',
    data
  })
}

export function deleteChild(id) {
  return request({
    url: '/children/' + id,
    method: 'delete'
  })
}

export function uploadImage(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/upload/image',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
