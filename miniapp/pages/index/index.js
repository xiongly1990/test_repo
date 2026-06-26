const checkinApi = require('../../api/checkin')
const childApi = require('../../api/child')

const iconEmojiMap = {
  'book': '📚',
  'reading': '📖',
  'pencil': '✏️',
  'write': '📝',
  'math': '🔢',
  'english': '🔤',
  'run': '🏃',
  'sport': '⚽',
  'exercise': '💪',
  'music': '🎵',
  'piano': '🎹',
  'painting': '🎨',
  'brush': '🖌️',
  'coding': '💻',
  'sleep': '😴',
  'housework': '🧹',
  'default': '📋'
}

function getIconEmoji(iconName) {
  if (!iconName) return iconEmojiMap.default
  return iconEmojiMap[iconName] || iconEmojiMap.default
}

function getGreetingText() {
  const hour = new Date().getHours()
  if (hour < 6) return '夜深了'
  if (hour < 9) return '早上好'
  if (hour < 12) return '上午好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  if (hour < 22) return '晚上好'
  return '夜深了'
}

function checkLogin(callback) {
  const childId = wx.getStorageSync('childId')
  if (!childId) {
    wx.showModal({
      title: '提示',
      content: '请先登录',
      showCancel: false,
      success: () => {
        wx.navigateTo({ url: '/pages/login/index' })
      }
    })
    return false
  }
  return true
}

Page({
  data: {
    childInfo: {},
    points: 0,
    streakDays: 0,
    totalDays: 0,
    completedCount: 0,
    totalCount: 0,
    taskList: [],
    dateStr: '',
    greetingText: ''
  },

  onLoad() {
    this.setData({ greetingText: getGreetingText() })
  },

  onShow() {
    const childId = wx.getStorageSync('childId')
    if (!childId) {
      wx.navigateTo({ url: '/pages/login/index' })
      return
    }

    this.setData({ greetingText: getGreetingText() })
    this.loadData()
  },

  loadData() {
    const childId = wx.getStorageSync('childId')
    if (!childId) return

    const today = new Date()
    const dateStr = `${today.getMonth() + 1}月${today.getDate()}日`
    this.setData({ dateStr })

    Promise.all([
      childApi.detail(childId),
      checkinApi.getTodayTasks(childId),
      checkinApi.getStreak(childId),
      checkinApi.getTodayStatus(childId)
    ]).then(([childRes, tasksRes, streakRes, statusRes]) => {
      const child = childRes.data
      const tasks = tasksRes.data || []
      const streak = streakRes.data
      const todayStatus = statusRes.data || []

      const completedTaskIds = todayStatus.map(s => s.taskId)

      const taskList = tasks.map(task => ({
        ...task,
        completed: completedTaskIds.includes(task.id),
        iconEmoji: getIconEmoji(task.icon)
      }))

      this.setData({
        childInfo: child,
        points: child.points || 0,
        streakDays: streak || 0,
        totalDays: child.totalDays || 0,
        completedCount: completedTaskIds.length,
        totalCount: tasks.length,
        taskList
      })
    }).catch(err => {
      console.error('loadData error', err)
    })
  },

  onCheckin(e) {
    const task = e.currentTarget.dataset.task
    const childId = wx.getStorageSync('childId')

    if (task.needPhoto === 1) {
      wx.chooseMedia({
        count: 1,
        mediaType: ['image'],
        sourceType: ['camera'],
        success: (res) => {
          const tempFilePath = res.tempFiles[0].tempFilePath
          this.uploadAndCheckin(childId, task, tempFilePath)
        }
      })
    } else {
      this.doCheckin(childId, task, null)
    }
  },

  uploadAndCheckin(childId, task, filePath) {
    wx.showLoading({ title: '上传中...' })
    wx.uploadFile({
      url: 'http://192.168.3.33:8080/api/upload/image',
      filePath: filePath,
      name: 'file',
      header: {
        'Authorization': wx.getStorageSync('token')
      },
      success: (res) => {
        wx.hideLoading()
        const data = JSON.parse(res.data)
        if (data.code === 200) {
          this.doCheckin(childId, task, data.data.url)
        } else {
          wx.showToast({ title: data.message || '上传失败', icon: 'none' })
        }
      },
      fail: () => {
        wx.hideLoading()
        wx.showToast({ title: '上传失败', icon: 'none' })
      }
    })
  },

  doCheckin(childId, task, photoUrl) {
    wx.showLoading({ title: '打卡中...' })
    checkinApi.submitCheckin({
      childId,
      taskId: task.id,
      photoUrl
    }).then(res => {
      wx.hideLoading()
      wx.showToast({ title: '打卡成功', icon: 'success' })
      setTimeout(() => {
        this.loadData()
      }, 1500)
    }).catch(err => {
      wx.hideLoading()
      if (err.message === '今日已打卡') {
        wx.showToast({ title: '今日已打卡', icon: 'none' })
        this.loadData()
      }
    })
  }
})