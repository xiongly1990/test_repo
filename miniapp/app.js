App({
  globalData: {
    userInfo: null,
    token: null,
    childId: null
  },

  onLaunch() {
    const token = wx.getStorageSync('token')
    const childId = wx.getStorageSync('childId')
    if (token) {
      this.globalData.token = token
      this.globalData.childId = childId
    }
  }
})
