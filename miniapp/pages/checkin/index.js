const checkinApi = require('../../api/checkin')
const childApi = require('../../api/child')

const taskIconMap = {
  '阅读': { emoji: '📚', bg: 'linear-gradient(135deg, #81D4FA 0%, #29B6F6 100%)' },
  '读书': { emoji: '📚', bg: 'linear-gradient(135deg, #81D4FA 0%, #29B6F6 100%)' },
  '写作业': { emoji: '✏️', bg: 'linear-gradient(135deg, #A5D6A7 0%, #66BB6A 100%)' },
  '作业': { emoji: '✏️', bg: 'linear-gradient(135deg, #A5D6A7 0%, #66BB6A 100%)' },
  '口算': { emoji: '🔢', bg: 'linear-gradient(135deg, #FFCC80 0%, #FFA726 100%)' },
  '练字': { emoji: '🖊️', bg: 'linear-gradient(135deg, #CE93D8 0%, #AB47BC 100%)' },
  '背诵': { emoji: '📖', bg: 'linear-gradient(135deg, #F48FB1 0%, #EC407A 100%)' },
  '英语': { emoji: '🔤', bg: 'linear-gradient(135deg, #80DEEA 0%, #26C6DA 100%)' },
  '语文': { emoji: '📝', bg: 'linear-gradient(135deg, #FFAB91 0%, #FF7043 100%)' },
  '数学': { emoji: '🧮', bg: 'linear-gradient(135deg, #B39DDB 0%, #7E57C2 100%)' },
  '钢琴': { emoji: '🎹', bg: 'linear-gradient(135deg, #90CAF9 0%, #42A5F5 100%)' },
  '练琴': { emoji: '🎹', bg: 'linear-gradient(135deg, #90CAF9 0%, #42A5F5 100%)' },
  '画画': { emoji: '🎨', bg: 'linear-gradient(135deg, #F8BBD9 0%, #F06292 100%)' },
  '绘画': { emoji: '🎨', bg: 'linear-gradient(135deg, #F8BBD9 0%, #F06292 100%)' },
  '运动': { emoji: '⚽', bg: 'linear-gradient(135deg, #C5E1A5 0%, #9CCC65 100%)' },
  '跑步': { emoji: '🏃', bg: 'linear-gradient(135deg, #FFE082 0%, #FFCA28 100%)' },
  '跳绳': { emoji: '🪢', bg: 'linear-gradient(135deg, #80CBC4 0%, #26A69A 100%)' },
  '游泳': { emoji: '🏊', bg: 'linear-gradient(135deg, #81D4FA 0%, #29B6F6 100%)' },
  '家务': { emoji: '🧹', bg: 'linear-gradient(135deg, #DCE775 0%, #C0CA33 100%)' },
  '整理': { emoji: '📦', bg: 'linear-gradient(135deg, #BCAAA4 0%, #8D6E63 100%)' }
}

const defaultIcons = [
  { emoji: '🎯', bg: 'linear-gradient(135deg, #FFCC80 0%, #FFA726 100%)' },
  { emoji: '⭐', bg: 'linear-gradient(135deg, #81D4FA 0%, #29B6F6 100%)' },
  { emoji: '🌟', bg: 'linear-gradient(135deg, #F48FB1 0%, #EC407A 100%)' },
  { emoji: '💫', bg: 'linear-gradient(135deg, #A5D6A7 0%, #66BB6A 100%)' },
  { emoji: '🎪', bg: 'linear-gradient(135deg, #B39DDB 0%, #7E57C2 100%)' }
]

function getTaskIcon(taskName, taskId) {
  for (const key in taskIconMap) {
    if (taskName.includes(key)) {
      return taskIconMap[key]
    }
  }
  const idx = parseInt(taskId || 0) % defaultIcons.length
  return defaultIcons[idx]
}

function getGreeting() {
  const hour = new Date().getHours()
  if (hour < 6) return '夜深了'
  if (hour < 9) return '早上好'
  if (hour < 12) return '上午好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  if (hour < 22) return '晚上好'
  return '夜深了'
}

function getWeekday() {
  const days = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  return days[new Date().getDay()]
}

Page({
  data: {
    dayNum: '',
    monthText: '',
    weekdayText: '',
    taskList: [],
    childId: null,
    childName: '小朋友',
    streakDays: 0,
    greetingText: '你好',
    completedCount: 0,
    progressPercent: 0
  },

  onLoad() {
    this.initDate()
    this.checkAndLoad()
  },

  onShow() {
    this.checkAndLoad()
  },

  initDate() {
    const today = new Date()
    this.setData({
      dayNum: today.getDate(),
      monthText: `${today.getMonth() + 1}月`,
      weekdayText: getWeekday(),
      greetingText: getGreeting()
    })
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

    this.setData({ childId })

    Promise.all([
      childApi.detail(childId),
      checkinApi.getTodayTasks(childId),
      checkinApi.getTodayStatus(childId),
      checkinApi.getStreak(childId)
    ]).then(([childRes, tasksRes, statusRes, streakRes]) => {
      const child = childRes.data
      const tasks = tasksRes.data || []
      const todayStatus = statusRes.data || []
      const streak = streakRes.data || 0
      const completedTaskIds = todayStatus.map(s => s.taskId)

      const taskList = tasks.map(task => {
        const icon = getTaskIcon(task.name, task.id)
        return {
          ...task,
          completed: completedTaskIds.includes(task.id),
          iconEmoji: icon.emoji,
          iconBg: icon.bg
        }
      })

      const completedCount = taskList.filter(t => t.completed).length
      const progressPercent = taskList.length > 0
        ? Math.round((completedCount / taskList.length) * 100)
        : 0

      this.setData({
        taskList,
        childName: child.name || '小朋友',
        streakDays: streak,
        completedCount,
        progressPercent
      })
    }).catch(err => {
      console.error('loadData error', err)
    })
  },

  onCheckin(e) {
    const task = e.currentTarget.dataset.task
    const childId = this.data.childId

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
      wx.showToast({ title: err.message || '打卡失败', icon: 'none' })
    })
  }
})