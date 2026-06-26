<template>
  <div class="checkin-review">
    <el-card class="table-card" shadow="never">
      <div class="card-header">
        <span>📋 待审核打卡</span>
        <el-tag type="warning" v-if="pendingCount > 0">{{ pendingCount }} 条待审核</el-tag>
      </div>

      <el-table :data="tableData" v-loading="loading" style="width: 100%">
        <el-table-column prop="taskName" label="任务" min-width="120">
          <template #default="scope">
            {{ scope.row.taskName }}
          </template>
        </el-table-column>
        <el-table-column label="孩子" width="120">
          <template #default="scope">
            <el-tag type="info">{{ getChildName(scope.row.childId) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="points" label="积分" width="80" align="center">
          <template #default="scope">
            <span style="color: #ff9800; font-weight: bold;">+{{ scope.row.points }}</span>
          </template>
        </el-table-column>
        <el-table-column label="照片" width="100" align="center">
          <template #default="scope">
            <div v-if="scope.row.photoUrl" class="photo-preview" @click="previewPhoto(scope.row.photoUrl)">
              <el-image
                :src="getPhotoUrl(scope.row.photoUrl)"
                :preview-src-list="[getPhotoUrl(scope.row.photoUrl)]"
                fit="cover"
                style="width: 60px; height: 60px; border-radius: 4px; cursor: pointer;">
              </el-image>
            </div>
            <span v-else style="color: #999;">无</span>
          </template>
        </el-table-column>
        <el-table-column prop="checkinDate" label="打卡日期" width="120">
          <template #default="scope">
            {{ scope.row.checkinDate }}
          </template>
        </el-table-column>
        <el-table-column prop="checkinTime" label="打卡时间" width="160">
          <template #default="scope">
            {{ formatTime(scope.row.checkinTime) }}
          </template>
        </el-table-column>
        <el-table-column label="是否补卡" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.isSupplement === 1 ? 'warning' : 'info'" size="small">
              {{ scope.row.isSupplement === 1 ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="scope">
            <el-button type="success" size="small" @click="handleApprove(scope.row)">通过</el-button>
            <el-button type="danger" size="small" @click="handleReject(scope.row)">不通过</el-button>
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

    <el-dialog v-model="previewVisible" title="打卡照片" width="500px">
      <el-image
        :src="previewUrl"
        fit="contain"
        style="width: 100%; max-height: 500px;">
      </el-image>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPendingList, approveCheckin, rejectCheckin } from '@/api/checkin'
import { getChildList } from '@/api/child'

const loading = ref(false)
const tableData = ref([])
const pendingCount = ref(0)
const previewVisible = ref(false)
const previewUrl = ref('')
const childMap = ref({})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const getChildName = (id) => {
  return childMap.value[id] || '未知'
}

const getPhotoUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return '/api' + url
}

const formatTime = (time) => {
  if (!time) return '-'
  return time
}

const previewPhoto = (url) => {
  previewUrl.value = getPhotoUrl(url)
  previewVisible.value = true
}

const loadChildren = () => {
  getChildList({ pageNum: 1, pageSize: 100 }).then(res => {
    const map = {}
    res.data.list.forEach(child => {
      map[child.id] = child.name
    })
    childMap.value = map
  })
}

const loadData = () => {
  loading.value = true
  getPendingList({
    pageNum: pagination.pageNum,
    pageSize: pagination.pageSize
  }).then(res => {
    tableData.value = res.data.list
    pagination.total = res.data.total
    pendingCount.value = res.data.total
  }).finally(() => {
    loading.value = false
  })
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

const handleApprove = (row) => {
  ElMessageBox.confirm(`确定通过「${row.taskName}」的打卡吗？`, '审核确认', {
    confirmButtonText: '通过',
    cancelButtonText: '取消',
    type: 'success'
  }).then(() => {
    approveCheckin(row.id).then(() => {
      ElMessage.success('审核通过')
      loadData()
    })
  }).catch(() => {})
}

const handleReject = (row) => {
  ElMessageBox.prompt('请输入不通过的原因', '审核不通过', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPattern: /.+/,
    inputErrorMessage: '请输入原因'
  }).then(({ value }) => {
    rejectCheckin(row.id, value).then(() => {
      ElMessage.success('已拒绝')
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
.checkin-review {
  .table-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 15px;
      font-size: 14px;
      font-weight: bold;
      color: #333;
    }

    .photo-preview {
      display: flex;
      justify-content: center;
    }

    .pagination-wrapper {
      margin-top: 20px;
      display: flex;
      justify-content: flex-end;
    }
  }
}
</style>
