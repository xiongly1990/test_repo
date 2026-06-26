<template>
  <div class="task-settings">
    <el-row :gutter="15">
      <el-col :span="5">
        <el-card class="child-list-card" shadow="never">
          <div class="card-header">
            <span>孩子列表</span>
          </div>
          <div
            v-for="child in childList"
            :key="child.id"
            class="child-item"
            :class="{ active: currentChildId === child.id }"
            @click="selectChild(child)">
            <el-avatar :size="36" :src="getAvatarUrl(child.avatar)">
              {{ child.name.charAt(0) }}
            </el-avatar>
            <div class="child-info">
              <div class="child-name">{{ child.name }}</div>
              <div class="child-grade">{{ child.grade || '-' }}</div>
            </div>
          </div>
          <div v-if="childList.length === 0" class="empty-tip">
            <el-empty description="请先添加孩子" :image-size="80"></el-empty>
          </div>
        </el-card>
      </el-col>

      <el-col :span="19">
        <el-card class="task-list-card" shadow="never">
          <div class="card-header">
            <span>任务列表</span>
            <el-button type="primary" :disabled="!currentChildId" @click="handleAdd">
              <el-icon><Plus /></el-icon>
              添加任务
            </el-button>
          </div>

          <el-table :data="taskList" v-loading="taskLoading" style="width: 100%" v-if="currentChildId">
            <el-table-column prop="name" label="任务名称" min-width="150">
              <template #default="scope">
                <div style="display: flex; align-items: center; gap: 8px;">
                  <span style="font-size: 20px;">{{ scope.row.icon || '📝' }}</span>
                  {{ scope.row.name }}
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="cycleType" label="周期" width="120" align="center">
              <template #default="scope">
                <el-tag :type="cycleTypeTag(scope.row.cycleType).type" size="small">
                  {{ cycleTypeTag(scope.row.cycleType).label }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="时间段" width="150" align="center">
              <template #default="scope">
                {{ scope.row.startTime || '-' }} ~ {{ scope.row.endTime || '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="needPhoto" label="需拍照" width="80" align="center">
              <template #default="scope">
                <el-tag :type="scope.row.needPhoto === 1 ? 'success' : 'info'" size="small">
                  {{ scope.row.needPhoto === 1 ? '是' : '否' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="points" label="积分" width="80" align="center">
              <template #default="scope">
                <span style="color: #ff9800; font-weight: bold;">+{{ scope.row.points }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="sort" label="排序" width="80" align="center">
              <template #default="scope">
                {{ scope.row.sort }}
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="80" align="center">
              <template #default="scope">
                <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'" size="small">
                  {{ scope.row.status === 1 ? '启用' : '停用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="160" fixed="right" align="center">
              <template #default="scope">
                <el-button type="primary" link @click="handleEdit(scope.row)">编辑</el-button>
                <el-button type="danger" link @click="handleDelete(scope.row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>

          <div v-if="!currentChildId" class="empty-tip">
            <el-empty description="请先选择一个孩子" :image-size="100"></el-empty>
          </div>
          <div v-else-if="taskList.length === 0 && !taskLoading" class="empty-tip">
            <el-empty description="暂无任务，点击右上角添加" :image-size="100"></el-empty>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="550px"
      :close-on-click-modal="false">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="90px">
        <el-form-item label="任务名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入任务名称"></el-input>
        </el-form-item>
        <el-form-item label="图标" prop="icon">
          <el-input v-model="formData.icon" placeholder="输入emoji，如 📚">
            <template #prepend>表情</template>
          </el-input>
        </el-form-item>
        <el-form-item label="周期" prop="cycleType">
          <el-radio-group v-model="formData.cycleType">
            <el-radio :label="1">每日</el-radio>
            <el-radio :label="2">工作日</el-radio>
            <el-radio :label="3">周末</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="时间段">
          <el-time-picker
            v-model="formData.startTime"
            placeholder="开始时间"
            format="HH:mm"
            value-format="HH:mm:ss"
            style="width: 45%;">
          </el-time-picker>
          <span style="margin: 0 10px; color: #999;">~</span>
          <el-time-picker
            v-model="formData.endTime"
            placeholder="结束时间"
            format="HH:mm"
            value-format="HH:mm:ss"
            style="width: 45%;">
          </el-time-picker>
        </el-form-item>
        <el-form-item label="需拍照" prop="needPhoto">
          <el-radio-group v-model="formData.needPhoto">
            <el-radio :label="1">是</el-radio>
            <el-radio :label="0">否</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="积分奖励" prop="points">
          <el-input-number v-model="formData.points" :min="1" :max="100"></el-input-number>
          <span style="margin-left: 10px; color: #999;">分</span>
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="formData.sort" :min="0" :max="999"></el-input-number>
          <span style="margin-left: 10px; color: #999;">数字越小越靠前</span>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getChildList } from '@/api/child'
import { getTaskList, addTask, updateTask, deleteTask } from '@/api/task'

const childList = ref([])
const currentChildId = ref(null)
const taskList = ref([])
const taskLoading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)
const isEdit = ref(false)
const submitLoading = ref(false)

const formData = reactive({
  id: null,
  childId: null,
  name: '',
  icon: '',
  cycleType: 1,
  startTime: '',
  endTime: '',
  needPhoto: 1,
  points: 10,
  sort: 0,
  status: 1
})

const formRules = {
  name: [
    { required: true, message: '请输入任务名称', trigger: 'blur' }
  ]
}

const getAvatarUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return '/api' + url
}

const cycleTypeTag = (type) => {
  const map = {
    1: { label: '每日', type: 'success' },
    2: { label: '工作日', type: 'warning' },
    3: { label: '周末', type: 'info' }
  }
  return map[type] || { label: '未知', type: '' }
}

const loadChildren = () => {
  getChildList({ pageNum: 1, pageSize: 100 }).then(res => {
    childList.value = res.data.list
    if (childList.value.length > 0) {
      selectChild(childList.value[0])
    }
  })
}

const selectChild = (child) => {
  currentChildId.value = child.id
  loadTasks()
}

const loadTasks = () => {
  if (!currentChildId.value) return
  taskLoading.value = true
  getTaskList(currentChildId.value).then(res => {
    taskList.value = res.data
  }).finally(() => {
    taskLoading.value = false
  })
}

const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '添加任务'
  formData.id = null
  formData.childId = currentChildId.value
  formData.name = ''
  formData.icon = ''
  formData.cycleType = 1
  formData.startTime = ''
  formData.endTime = ''
  formData.needPhoto = 1
  formData.points = 10
  formData.sort = 0
  formData.status = 1
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑任务'
  formData.id = row.id
  formData.childId = row.childId
  formData.name = row.name
  formData.icon = row.icon || ''
  formData.cycleType = row.cycleType
  formData.startTime = row.startTime || ''
  formData.endTime = row.endTime || ''
  formData.needPhoto = row.needPhoto
  formData.points = row.points
  formData.sort = row.sort
  formData.status = row.status
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除任务「${row.name}」吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    deleteTask(row.id).then(() => {
      ElMessage.success('删除成功')
      loadTasks()
    })
  }).catch(() => {})
}

const handleSubmit = () => {
  formRef.value.validate((valid) => {
    if (valid) {
      submitLoading.value = true
      const request = isEdit.value
        ? updateTask(formData.id, formData)
        : addTask(formData)
      request.then(() => {
        ElMessage.success(isEdit.value ? '修改成功' : '添加成功')
        dialogVisible.value = false
        loadTasks()
      }).finally(() => {
        submitLoading.value = false
      })
    }
  })
}

onMounted(() => {
  loadChildren()
})
</script>

<style lang="scss" scoped>
.task-settings {
  .child-list-card {
    height: calc(100vh - 140px);
    overflow-y: auto;
  }

  .task-list-card {
    min-height: calc(100vh - 140px);
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 15px;
    font-size: 14px;
    font-weight: bold;
    color: #333;
  }

  .child-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.2s;
    margin-bottom: 8px;

    &:hover {
      background: #f5f7fa;
    }

    &.active {
      background: #ecf5ff;
    }
  }

  .child-info {
    flex: 1;
  }

  .child-name {
    font-size: 14px;
    font-weight: 500;
    color: #333;
    margin-bottom: 2px;
  }

  .child-grade {
    font-size: 12px;
    color: #999;
  }

  .empty-tip {
    padding: 40px 0;
  }
}
</style>
