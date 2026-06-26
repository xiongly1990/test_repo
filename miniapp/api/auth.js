const { request } = require('../utils/request')

module.exports = {
  login: (data) => request.post('/auth/login', data),
  logout: () => request.post('/auth/logout'),
  getInfo: () => request.get('/auth/info')
}
