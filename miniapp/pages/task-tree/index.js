const rewardApi = require('../../api/reward')
const checkinApi = require('../../api/checkin')

Page({
  data: {
    tree: [],
    streakDays: 0,
    currentLevel: 0,
    unlockedCount: 0,
    totalAchievements: 0
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
    this.loadData()
  },

  loadData() {
    const childId = wx.getStorageSync('childId')
    if (!childId) return

    Promise.all([
      rewardApi.getAchievementTree(childId),
      checkinApi.getStreak(childId)
    ]).then(([treeRes, streakRes]) => {
      const treeData = treeRes.data
      this.setData({
        tree: treeData.tree || [],
        streakDays: treeData.streakDays || 0,
        currentLevel: treeData.currentLevel || 0,
        unlockedCount: treeData.unlockedCount || 0,
        totalAchievements: treeData.totalAchievements || 0
      })
    }).catch(err => {
      console.error('loadData error', err)
    })
  }
})