const checkinApi = require('../../api/checkin')
const childApi = require('../../api/child')

Page({
  data: {
    recordList: [],
    pageNum: 1,
    pageSize: 10,
    hasMore: false,
    loading: false,
    currentFilter: 'all',
    totalDays: 0,
    streakDays: 0,
    points: 0
  },

  onLoad() {
    this.checkAndLoad()
  },

  onShow() {
    this.checkAndLoad()
  },

  checkAndLoad() {
    const childId = wx.getStorageSync('childId')
    if (!childId) {
      wx.navigateTo({ url: '/pages/login/index' })
      return
    }
    this.setData({ pageNum: 1, recordList: [] })
    this.loadSummary()
    this.loadRecords()
  },

  loadSummary() {
    const childId = wx.getStorageSync('childId')
    if (!childId) return

    Promise.all([
      childApi.detail(childId),
      checkinApi.getStreak(childId)
    ]).then(([childRes, streakRes]) => {
      const child = childRes.data
      this.setData({
        totalDays: child.totalDays || 0,
        streakDays: streakRes.data || 0,
        points: child.points || 0
      })
    }).catch(err => {
      console.error('loadSummary error', err)
    })
  },

  onFilterChange(e) {
    const status = e.currentTarget.dataset.status
    this.setData({
      currentFilter: status,
      pageNum: 1,
      recordList: []
    })
    this.loadRecords()
  },

  loadRecords() {
    if (this.data.loading) return

    const childId = wx.getStorageSync('childId')
    if (!childId) return

    this.setData({ loading: true })

    const params = {
      childId,
      pageNum: this.data.pageNum,
      pageSize: this.data.pageSize
    }

    if (this.data.currentFilter !== 'all') {
      params.status = this.data.currentFilter
    }

    checkinApi.getRecords(params).then(res => {
      const list = res.data.list || []
      const recordList = this.data.pageNum === 1 ? list : [...this.data.recordList, ...list]

      const formattedList = recordList.map(item => {
        const date = new Date(item.createTime)
        const dayStr = date.getDate()
        const monthStr = `${date.getMonth() + 1}月`
        const timeStr = `${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`

        let statusText = ''
        let statusClass = ''

        switch (item.status) {
          case 0:
            statusText = '待审核'
            statusClass = 'pending'
            break
          case 1:
            statusText = '已通过'
            statusClass = 'approved'
            break
          case 2:
            statusText = '已拒绝'
            statusClass = 'rejected'
            break
        }

        return {
          ...item,
          dayStr,
          monthStr,
          timeStr,
          statusText,
          statusClass
        }
      })

      const total = res.data.total || 0
      this.setData({
        recordList: formattedList,
        hasMore: this.data.pageNum * this.data.pageSize < total,
        loading: false
      })
    }).catch(err => {
      console.error('loadRecords error', err)
      this.setData({ loading: false })
    })
  },

  loadMore() {
    if (!this.data.hasMore) return
    this.setData({ pageNum: this.data.pageNum + 1 })
    this.loadRecords()
  },

  previewPhoto(e) {
    const url = e.currentTarget.dataset.url
    wx.previewImage({
      urls: [url],
      current: url
    })
  }
})