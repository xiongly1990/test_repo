const childApi = require('../../api/child')
const checkinApi = require('../../api/checkin')
const rewardApi = require('../../api/reward')

const avatarEmojis = ['🧒', '👦', '👧', '🧑', '🐱', '🐶', '🐰', '🦊', '🐼', '🐨']

function getAvatarEmoji(childId) {
  if (!childId) return '🧒'
  const index = parseInt(childId) % avatarEmojis.length
  return avatarEmojis[index]
}

function calcLevel(totalDays) {
  if (totalDays < 7) return 1
  if (totalDays < 30) return 2
  if (totalDays < 90) return 3
  if (totalDays < 180) return 4
  if (totalDays < 365) return 5
  return 6
}

Page({
  data: {
    childInfo: {},
    streakDays: 0,
    totalDays: 0,
    points: 0,
    joinDate: '',
    avatarEmoji: '🧒',
    currentLevel: 1,
    unlockedCount: 0
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
      childApi.detail(childId),
      checkinApi.getStreak(childId),
      rewardApi.getAchievementTree(childId)
    ]).then(([childRes, streakRes, treeRes]) => {
      const child = childRes.data
      const joinDate = child.createTime ? child.createTime.substring(0, 10) : ''
      const totalDays = child.totalDays || 0

      this.setData({
        childInfo: child,
        streakDays: streakRes.data || 0,
        totalDays,
        points: child.points || 0,
        joinDate,
        avatarEmoji: getAvatarEmoji(childId),
        currentLevel: calcLevel(totalDays),
        unlockedCount: treeRes.data?.unlockedCount || 0
      })
    }).catch(err => {
      console.error('loadData error', err)
    })
  },

  goToCheckinRecords() {
    wx.navigateTo({ url: '/pages/record/index' })
  },

  goToExchangeRecords() {
    wx.navigateTo({ url: '/pages/reward/index' })
  },

  goToAchievement() {
    wx.switchTab({ url: '/pages/task-tree/index' })
  },

  onAbout() {
    wx.showModal({
      title: '关于亲子学习打卡',
      content: '版本：v1.0.0\n\n让学习更有动力，让成长更有乐趣！',
      showCancel: false,
      confirmText: '知道了'
    })
  },

  onLogout() {
    wx.showModal({
      title: '确认退出',
      content: '确定要退出登录吗？',
      success: (res) => {
        if (res.confirm) {
          wx.clearStorageSync()
          wx.reLaunch({ url: '/pages/login/index' })
        }
      }
    })
  }
})