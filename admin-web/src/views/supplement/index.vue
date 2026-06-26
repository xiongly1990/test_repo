<template>
  <div class="supplement-checkin">
    <el-row :gutter="15">
      <el-col :span="14">
        <el-card class="record-card" shadow="never">
          <div class="card-header">
            <span>📋 补卡记录</span>
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
            </el-form>
          </div>
          <el-table :data="tableData" v-loading="loading" style="width: 100%">
            <el-table-column label="孩子" width="100">
              <template #default="scope">
                <el-tag type="info">{{ getChildName(scope.row.childId) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="taskName" label="任务" min-width="120">
              <template #default="scope">
                {{ scope.row.taskName }}
              </template>
            </el-table-column>
            <el-table-column prop="checkinDate" label="补卡日期" width="120">
              <template #default="scope">
                {{ scope.row.checkinDate }}
              </template>
            </el-table-column>
            <el-table-column prop="supplementReason" label="补卡原因" min-width="150">
              <template #default="scope">
                {{ scope.row.supplementReason || '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="points" label="积分" width="80" align="center">
              <template #default="scope">
                <span style="color: #ff9800; font-weight: bold;">+{{ scope.row.points }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="补卡时间" width="180">
              <template #default="scope">
                {{ formatTime(scope.row.createTime) }}
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
      </el-col>

      <el-col :span="10">
        <el-card class="form-card" shadow="never">
          <div class="card-header">
            <span>➕ 新增补卡</span>
          </div>
          <el-form ref="formRef" :model="formData" :rules="formRules" label-width="80px">
            <el-form-item label="孩子" prop="childId">
              <el-select v-model="formData.childId" placeholder="请选择孩子" style="width: 100%;" @change="loadChildTasks">
                <el-option
                  v-for="child in childList"
                  :key="child.id"
                  :label="child.name"
                  :value="child.id">
                </el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="任务" prop="taskId">
              <el-select v-model="formData.taskId" placeholder="请选择任务" style="width: 100%;" :disabled="!formData.childId">
                <el-option
                  v-for="task in taskList"
                  :key="task.id"
                  :label="task.name + ' (+' + task.points + '分)'"
                  :value="task.id">
                </el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="补卡日期" prop="checkinDate">
              <el-date-picker
                v-model="formData.checkinDate"
                type="date"
                placeholder="请选择日期"
                style="width: 100%;"
                value-format="YYYY-MM-DD">
              </el-date-picker>
            </el-form-item>
            <el-form-item label="原因" prop="reason">
              <el-input
                v-model="formData.reason"
                type="textarea"
                :rows="4"
                placeholder="请输入补卡原因"
                maxlength="200"
                show-word-limit>
              </el-input>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="submitLoading" style="width: 100%;" @click="handleSubmit">
                提交补卡
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getSupplementRecords, supplementCheckin } from '@/api/checkin'
import { getChildList } from '@/api/child'
import { getTaskList } from '@/api/task'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const childList = ref([])
const taskList = ref([])
const childMap = ref({})
const formRef = ref(null)

const searchForm = reactive({
  childId: null
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const formData = reactive({
  childId: null,
  taskId: null,
  checkinDate: '',
  reason: ''
})

const formRules = {
  childId: [
    { required: true, message: '请选择孩子', trigger: 'change' }
  ],
  taskId: [
    { required: true, message: '请选择任务', trigger: 'change' }
  ],
  checkinDate: [
    { required: true, message: '请选择补卡日期', trigger: 'change' }
  ]
}

const getChildName = (id) => {
  return childMap.value[id] || '未知'
}

const formatTime = (time) => {
  if (!time) return '-'
  return time
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

const loadChildTasks = () => {
  if (!formData.childId) {
    taskList.value = []
    return
  }
  getTaskList(formData.childId).then(res => {
    taskList.value = res.data
  })
}

const loadData = () => {
  loading.value = true
  const params = {
    pageNum: pagination.pageNum,
    pageSize: pagination.pageSize
  }
  if (searchForm.childId) {
    params.childId = searchForm.childId
  }
  getSupplementRecords(params).then(res => {
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

const handleSubmit = () => {
  formRef.value.validate((valid) => {
    if (valid) {
      submitLoading.value = true
      supplementCheckin(formData).then(() => {
        ElMessage.success('补卡成功')
        formData.taskId = null
        formData.checkinDate = ''
        formData.reason = ''
        loadData()
      }).finally(() => {
        submitLoading.value = false
      })
    }
  })
}

onMounted(() => {
  loadChildren()
  loadData()
})
</script>

<style lang="scss" scoped>
.supplement-checkin {
  .record-card,
  .form-card {
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
