const { request } = require('../utils/request')

module.exports = {
  list: (params) => request.get('/children', params),
  detail: (id) => request.get('/children/' + id),
  add: (data) => request.post('/children', data)
}
