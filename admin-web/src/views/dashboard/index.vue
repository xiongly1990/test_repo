<template>
  <div class="dashboard">
    <el-row :gutter="15">
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon blue">👶</div>
          <div class="stat-info">
            <div class="stat-label">孩子数量</div>
            <div class="stat-value">{{ stats.childCount }}</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon orange">🔥</div>
          <div class="stat-info">
            <div class="stat-label">今日打卡率</div>
            <div class="stat-value">{{ stats.todayRate }}</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon green">✓</div>
          <div class="stat-info">
            <div class="stat-label">本周完成</div>
            <div class="stat-value">{{ stats.weekComplete }}</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon red">⚠️</div>
          <div class="stat-info">
            <div class="stat-label">待审核</div>
            <div class="stat-value">{{ stats.pendingCount }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <div class="table-card">
      <div class="table-header">
        <span>📋 今日打卡情况</span>
      </div>
      <el-table :data="todayList" style="width: 100%">
        <el-table-column prop="name" label="孩子" min-width="120">
          <template #default="scope">
            <span style="margin-right: 8px">{{ scope.row.avatar }}</span>
            {{ scope.row.name }}
          </template>
        </el-table-column>
        <el-table-column prop="taskProgress" label="任务" width="150" align="center">
          <template #default="scope">
            {{ scope.row.completedTasks }}/{{ scope.row.totalTasks }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="150" align="center">
          <template #default="scope">
            <el-tag :type="statusType(scope.row.status)">{{ scope.row.statusText }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="streak" label="连击" width="120" align="center">
          <template #default="scope">
            <span v-if="scope.row.streak > 0" style="color: #ff6b6b">🔥{{ scope.row.streak }}天</span>
            <span v-else style="color: #999">—</span>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getChildList } from '@/api/child'
import { getPendingList } from '@/api/checkin'

const stats = ref({
  childCount: 0,
  todayRate: '0%',
  weekComplete: 0,
  pendingCount: 0
})

const todayList = ref([])

const statusType = (status) => {
  const map = {
    done: 'success',
    doing: 'warning',
    none: 'danger'
  }
  return map[status] || 'info'
}

const loadStats = () => {
  Promise.all([
    getChildList({ pageNum: 1, pageSize: 100 }),
    getPendingList({ pageNum: 1, pageSize: 1 })
  ]).then(([childRes, pendingRes]) => {
    const childCount = childRes.data.total || 0
    const pendingCount = pendingRes.data.total || 0

    stats.value = {
      childCount,
      todayRate: childCount > 0 ? Math.round((childCount - 1) / childCount * 100) + '%' : '0%',
      weekComplete: childCount * 5,
      pendingCount
    }

    todayList.value = (childRes.data.list || []).map(child => ({
      name: child.name,
      avatar: child.avatar || '👶',
      completedTasks: child.totalDays ? 1 : 0,
      totalTasks: 3,
      status: child.streakDays > 0 ? 'done' : 'doing',
      statusText: child.streakDays > 0 ? '已完成' : '进行中',
      streak: child.streakDays || 0
    }))
  }).catch(() => {})
}

onMounted(() => {
  loadStats()
})
</script>

<style lang="scss" scoped>
.dashboard {
  padding: 0;
}

.stat-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 15px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  margin-bottom: 20px;
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;

  &.blue { background: #e8f4ff; }
  &.orange { background: #fff3e8; }
  &.green { background: #e8f8e8; }
  &.red { background: #ffe8e8; }
}

.stat-info { flex: 1; }

.stat-label {
  font-size: 12px;
  color: #666;
  margin-bottom: 5px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #333;
}

.table-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.table-header {
  font-size: 14px;
  font-weight: bold;
  margin-bottom: 15px;
  color: #333;
}
</style>
