const API_BASE = 'http://192.168.3.33:8080/api'

let isRedirectingToLogin = false

class Request {
  constructor() {
    this.baseUrl = API_BASE
  }

  request(options) {
    return new Promise((resolve, reject) => {
      const token = wx.getStorageSync('token')
      const header = {
        'Content-Type': 'application/json',
        ...options.header
      }
      if (token) {
        header['Authorization'] = token
      }

      wx.showLoading({
        title: '加载中...',
        mask: true
      })

      wx.request({
        url: this.baseUrl + options.url,
        method: options.method || 'GET',
        data: options.data || {},
        header,
        success: (res) => {
          wx.hideLoading()
          if (res.data.code === 200) {
            resolve(res.data)
          } else if (res.data.code === 401) {
            if (isRedirectingToLogin) {
              reject(res.data)
              return
            }
            isRedirectingToLogin = true
            wx.removeStorageSync('token')
            wx.removeStorageSync('childId')
            wx.showToast({
              title: '请先登录',
              icon: 'none'
            })
            setTimeout(() => {
              isRedirectingToLogin = false
              wx.reLaunch({ url: '/pages/login/index' })
            }, 1500)
            reject(res.data)
          } else {
            wx.showToast({
              title: res.data.message || '请求失败',
              icon: 'none'
            })
            reject(res.data)
          }
        },
        fail: (err) => {
          wx.hideLoading()
          wx.showToast({
            title: '网络错误',
            icon: 'none'
          })
          reject(err)
        }
      })
    })
  }

  get(url, data) {
    return this.request({ url, method: 'GET', data })
  }

  post(url, data) {
    return this.request({ url, method: 'POST', data })
  }
}

const request = new Request()

module.exports = {
  API_BASE,
  request
}