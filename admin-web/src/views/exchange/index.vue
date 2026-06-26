<template>
  <div class="exchange-records">
    <el-card class="table-card" shadow="never">
      <div class="card-header">
        <span>🎁 兑换记录</span>
      </div>
      <div class="filter-bar">
        <el-form :inline="true" :model="searchForm">
          <el-form-item label="孩子">
            <el-select v-model="searchForm.childId" placeholder="全部" clearable style="width: 150px;" @change="handleSearch">
              <el-option
                v-for="child in childList"
                :key="child.id"
                :label="child.name"
                :value="child.id">
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 150px;" @change="handleSearch">
              <el-option label="待发放" :value="0"></el-option>
              <el-option label="已发放" :value="1"></el-option>
              <el-option label="已取消" :value="2"></el-option>
            </el-select>
          </el-form-item>
        </el-form>
      </div>
      <el-table :data="tableData" v-loading="loading" style="width: 100%">
        <el-table-column label="孩子" width="100">
          <template #default="scope">
            <el-tag type="info">{{ getChildName(scope.row.childId) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="rewardName" label="奖励" min-width="150">
          <template #default="scope">
            {{ scope.row.rewardName }}
          </template>
        </el-table-column>
        <el-table-column prop="points" label="消耗积分" width="100" align="center">
          <template #default="scope">
            <span style="color: #f56c6c; font-weight: bold;">-{{ scope.row.points }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="statusType(scope.row.status)" size="small">
              {{ statusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="兑换时间" width="180">
          <template #default="scope">
            {{ formatTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="deliverTime" label="发放时间" width="180">
          <template #default="scope">
            {{ scope.row.deliverTime ? formatTime(scope.row.deliverTime) : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="scope">
            <el-button
              v-if="scope.row.status === 0"
              type="success"
              size="small"
              @click="handleDeliver(scope.row)">
              标记发放
            </el-button>
            <el-button
              v-if="scope.row.status === 0"
              type="danger"
              size="small"
              link
              @click="handleCancel(scope.row)">
              取消
            </el-button>
            <span v-if="scope.row.status !== 0" style="color: #999;">-</span>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange">
        </el-pagination>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getExchangeRecords, deliverExchange, cancelExchange } from '@/api/reward'
import { getChildList } from '@/api/child'

const loading = ref(false)
const tableData = ref([])
const childList = ref([])
const childMap = ref({})

const searchForm = reactive({
  childId: null,
  status: null
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const getChildName = (id) => {
  return childMap.value[id] || '未知'
}

const formatTime = (time) => {
  if (!time) return '-'
  return time
}

const statusType = (status) => {
  const map = { 0: 'warning', 1: 'success', 2: 'info' }
  return map[status] || 'info'
}

const statusText = (status) => {
  const map = { 0: '待发放', 1: '已发放', 2: '已取消' }
  return map[status] || '未知'
}

const loadChildren = () => {
  getChildList({ pageNum: 1, pageSize: 100 }).then(res => {
    childList.value = res.data.list
    const map = {}
    res.data.list.forEach(child => {
      map[child.id] = child.name
    })
    childMap.value = map
  })
}

const loadData = () => {
  loading.value = true
  const params = {
    pageNum: pagination.pageNum,
    pageSize: pagination.pageSize
  }
  if (searchForm.childId) params.childId = searchForm.childId
  if (searchForm.status !== null && searchForm.status !== undefined && searchForm.status !== '') {
    params.status = searchForm.status
  }
  getExchangeRecords(params).then(res => {
    tableData.value = res.data.list
    pagination.total = res.data.total
  }).finally(() => {
    loading.value = false
  })
}

const handleSearch = () => {
  pagination.pageNum = 1
  loadData()
}

const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.pageNum = 1
  loadData()
}

const handlePageChange = (page) => {
  pagination.pageNum = page
  loadData()
}

const handleDeliver = (row) => {
  ElMessageBox.confirm(`确定将「${row.rewardName}」标记为已发放吗？`, '确认发放', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'success'
  }).then(() => {
    deliverExchange(row.id).then(() => {
      ElMessage.success('已标记为已发放')
      loadData()
    })
  }).catch(() => {})
}

const handleCancel = (row) => {
  ElMessageBox.confirm(
    `确定取消「${row.rewardName}」的兑换吗？取消后积分将退回。`,
    '确认取消',
    {
      confirmButtonText: '确定取消',
      cancelButtonText: '再想想',
      type: 'warning'
    }
  ).then(() => {
    cancelExchange(row.id).then(() => {
      ElMessage.success('已取消，积分已退回')
      loadData()
    })
  }).catch(() => {})
}

onMounted(() => {
  loadChildren()
  loadData()
})
</script>

<style lang="scss" scoped>
.exchange-records {
  .table-card {
    min-height: calc(100vh - 140px);
  }

  .card-header {
    margin-bottom: 15px;
    font-size: 14px;
    font-weight: bold;
    color: #333;
  }

  .filter-bar {
    margin-bottom: 15px;
  }

  .pagination-wrapper {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
}
</style>
