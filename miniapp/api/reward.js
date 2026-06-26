const { request } = require('../utils/request')

module.exports = {
  getCategories: () => request.get('/rewards/categories'),
  getRewards: (params) => request.get('/rewards', params),
  exchange: (data) => request.post('/exchange', data),
  getExchangeRecords: (params) => request.get('/exchange/records', params),
  getAchievementTree: (childId) => request.get('/achievements/tree', { childId })
}
