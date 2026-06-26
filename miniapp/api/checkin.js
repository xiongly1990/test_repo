const { request } = require('../utils/request')

module.exports = {
  getTodayTasks: (childId) => request.get('/tasks/today', { childId }),
  getByChildId: (childId) => request.get('/tasks', { childId }),
  getStreak: (childId) => request.get('/checkin/streak/' + childId),
  getTodayStatus: (childId) => request.get('/checkin/today-status', { childId }),
  submitCheckin: (data) => request.post('/checkin/submit', data),
  getRecords: (params) => request.get('/checkin/records', params)
}
