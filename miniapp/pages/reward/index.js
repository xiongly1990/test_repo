const rewardApi = require('../../api/reward')
const childApi = require('../../api/child')

const iconEmojiMap = {
  'ice_cream': '🍦',
  'hamburger': '🍔',
  'pizza': '🍕',
  'cake': '🍰',
  'chocolate': '🍫',
  'cookie': '🍪',
  'food': '🍱',
  'snack': '🍿',
  'game_pad': '🎮',
  'video_game': '🎯',
  'tv': '📺',
  'phone': '📱',
  'entertainment': '🎮',
  'book': '📚',
  'pen': '✏️',
  'notebook': '📒',
  'study': '📖',
  'toy': '🧸',
  'ball': '⚽',
  'car': '🚗',
  'toy_brick': '🧱',
  'ticket': '🎫',
  'gift_card': '🎁',
  'coupon': '🎟️',
  'travel': '✈️',
  'default': '🎁'
}

function getIconEmoji(iconName) {
  if (!iconName) return iconEmojiMap.default
  return iconEmojiMap[iconName] || iconEmojiMap.default
}

Page({
  data: {
    points: 0,
    categories: [],
    currentCategoryId: null,
    rewardList: [],
    childId: null
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
    this.loadCategories()
    this.loadChildInfo()
  },

  loadChildInfo() {
    const childId = wx.getStorageSync('childId')
    if (!childId) return

    childApi.detail(childId).then(res => {
      this.setData({
        points: res.data.points || 0,
        childId
      })
    }).catch(err => {
      console.error('loadChildInfo error', err)
    })
  },

  loadCategories() {
    rewardApi.getCategories().then(res => {
      const categories = (res.data || []).map(cat => ({
        ...cat,
        iconEmoji: getIconEmoji(cat.icon)
      }))
      this.setData({ categories })

      if (categories.length > 0) {
        const firstCategoryId = categories[0].id
        this.setData({ currentCategoryId: firstCategoryId })
        this.loadRewards(firstCategoryId)
      }
    }).catch(err => {
      console.error('loadCategories error', err)
    })
  },

  onTabChange(e) {
    const categoryId = e.currentTarget.dataset.id
    this.setData({ currentCategoryId: categoryId })
    this.loadRewards(categoryId)
  },

  loadRewards(categoryId) {
    rewardApi.getRewards({ categoryId, pageNum: 1, pageSize: 100 }).then(res => {
      const rewardList = (res.data.list || []).map(r => ({
        ...r,
        iconEmoji: getIconEmoji(r.icon),
        stock: r.stock !== undefined ? r.stock : 999
      }))
      this.setData({ rewardList })
    }).catch(err => {
      console.error('loadRewards error', err)
    })
  },

  onRewardTap(e) {
    const reward = e.currentTarget.dataset.reward
    console.log('tap reward', reward)
  },

  onExchange(e) {
    const reward = e.currentTarget.dataset.reward
    const childId = this.data.childId

    if (!childId) {
      wx.showToast({ title: '请先登录', icon: 'none' })
      return
    }

    if (this.data.points < reward.points) {
      wx.showToast({ title: '积分不足', icon: 'none' })
      return
    }

    if (reward.stock === 0) {
      wx.showToast({ title: '已兑完', icon: 'none' })
      return
    }

    wx.showModal({
      title: '确认兑换',
      content: `确定要兑换「${reward.name}」吗？\n需要 ${reward.points} 积分`,
      success: (res) => {
        if (res.confirm) {
          this.doExchange(reward, childId)
        }
      }
    })
  },

  doExchange(reward, childId) {
    wx.showLoading({ title: '兑换中...' })

    rewardApi.exchange({
      childId,
      rewardId: reward.id
    }).then(res => {
      wx.hideLoading()
      wx.showToast({ title: '兑换成功', icon: 'success' })

      this.setData({
        points: this.data.points - reward.points
      })

      if (this.data.currentCategoryId) {
        this.loadRewards(this.data.currentCategoryId)
      }
    }).catch(err => {
      wx.hideLoading()
      wx.showToast({ title: err.message || '兑换失败', icon: 'none' })
    })
  },

  goToRecords() {
    wx.navigateTo({ url: '/pages/record/index' })
  }
})