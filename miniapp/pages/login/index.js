const childApi = require('../../api/child')

const avatarOptions = ['🧒', '👦', '👧', '🧑', '🐱', '🐶', '🐰', '🦊', '🐼', '🐨', '🐸', '🦁', '🐯', '🐵', '🐷', '🐻']

const avatarEmojis = avatarOptions

function getAvatarEmoji(childId) {
  if (!childId) return avatarEmojis[0]
  const index = parseInt(childId) % avatarEmojis.length
  return avatarEmojis[index]
}

Page({
  data: {
    children: [],
    selectedChildId: null,
    loading: true,
    showRegister: false,
    registerName: '',
    registerAvatar: '🧒',
    avatarOptions: avatarOptions
  },

  onLoad() {
    this.loadChildren()
  },

  onShow() {
    if (!this.data.showRegister) {
      this.loadChildren()
    }
  },

  loadChildren() {
    this.setData({ loading: true })
    
    childApi.list({ pageNum: 1, pageSize: 50 }).then(res => {
      const list = res.data.list || []
      const children = list.map(child => ({
        ...child,
        avatarEmoji: getAvatarEmoji(child.id)
      }))

      let selectedChildId = this.data.selectedChildId
      if (children.length > 0 && !selectedChildId) {
        const savedChildId = wx.getStorageSync('childId')
        if (savedChildId && children.some(c => c.id === savedChildId)) {
          selectedChildId = savedChildId
        }
      }

      this.setData({
        children,
        selectedChildId,
        loading: false
      })
    }).catch(err => {
      console.error('loadChildren error', err)
      this.setData({ loading: false })
    })
  },

  onSelectChild(e) {
    const child = e.currentTarget.dataset.child
    this.setData({ selectedChildId: child.id })
  },

  onLogin() {
    if (!this.data.selectedChildId) return

    const child = this.data.children.find(c => c.id === this.data.selectedChildId)
    if (!child) return

    wx.setStorageSync('childId', child.id)
    wx.showToast({ title: `欢迎 ${child.name}！`, icon: 'success' })
    
    setTimeout(() => {
      wx.reLaunch({ url: '/pages/index/index' })
    }, 1500)
  },

  onShowRegister() {
    this.setData({
      showRegister: true,
      registerName: '',
      registerAvatar: avatarOptions[0]
    })
  },

  onCancelRegister() {
    this.setData({ showRegister: false })
  },

  onNameInput(e) {
    this.setData({ registerName: e.detail.value })
  },

  onSelectAvatar(e) {
    const avatar = e.currentTarget.dataset.avatar
    this.setData({ registerAvatar: avatar })
  },

  onConfirmRegister() {
    const name = this.data.registerName.trim()
    if (!name) {
      wx.showToast({ title: '请输入名字', icon: 'none' })
      return
    }

    if (name.length < 2) {
      wx.showToast({ title: '名字至少2个字符', icon: 'none' })
      return
    }

    wx.showLoading({ title: '创建中...' })
    
    childApi.add({
      name: name,
      avatar: this.data.registerAvatar
    }).then(res => {
      wx.hideLoading()
      if (res.code === 200 || res.code === 0) {
        wx.showToast({ title: '创建成功！', icon: 'success' })
        
        const newChild = res.data
        newChild.avatarEmoji = this.data.registerAvatar
        
        this.setData({
          showRegister: false,
          children: [...this.data.children, newChild],
          selectedChildId: newChild.id
        })
      } else {
        wx.showToast({ title: res.message || '创建失败', icon: 'none' })
      }
    }).catch(err => {
      wx.hideLoading()
      console.error('onConfirmRegister error', err)
      wx.showToast({ title: '创建失败', icon: 'none' })
    })
  }
})