<template>
  <div class="child-management">
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="孩子姓名">
          <el-input
            v-model="searchForm.keyword"
            placeholder="请输入姓名"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch">
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="success" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            添加孩子
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table :data="tableData" v-loading="loading" style="width: 100%">
        <el-table-column prop="name" label="姓名" width="120">
          <template #default="scope">
            <div style="display: flex; align-items: center; gap: 10px;">
              <el-avatar :size="36" :src="getAvatarUrl(scope.row.avatar)">
                {{ scope.row.name.charAt(0) }}
              </el-avatar>
              <span>{{ scope.row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="birthday" label="生日" width="130">
          <template #default="scope">
            {{ scope.row.birthday || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="grade" label="年级" width="130">
          <template #default="scope">
            {{ scope.row.grade || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="points" label="积分" width="100" align="center">
          <template #default="scope">
            <span style="color: #ff9800; font-weight: bold;">{{ scope.row.points || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="streakDays" label="连击天数" width="100" align="center">
          <template #default="scope">
            <span v-if="scope.row.streakDays > 0" style="color: #ff6b6b;">🔥 {{ scope.row.streakDays }}天</span>
            <span v-else style="color: #999;">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="totalDays" label="累计天数" width="100" align="center">
          <template #default="scope">
            {{ scope.row.totalDays || 0 }}天
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="scope">
            <el-button type="primary" link @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(scope.row)">删除</el-button>
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

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      :close-on-click-modal="false">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="80px">
        <el-form-item label="头像" prop="avatar">
          <el-upload
            class="avatar-uploader"
            :action="uploadUrl"
            :headers="uploadHeaders"
            :show-file-list="false"
            :on-success="handleAvatarSuccess"
            :before-upload="beforeAvatarUpload">
            <el-avatar v-if="formData.avatar" :size="80" :src="getAvatarUrl(formData.avatar)">
            </el-avatar>
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="formData.name" placeholder="请输入孩子姓名"></el-input>
        </el-form-item>
        <el-form-item label="生日" prop="birthday">
          <el-date-picker
            v-model="formData.birthday"
            type="date"
            placeholder="请选择生日"
            style="width: 100%"
            value-format="YYYY-MM-DD">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="年级" prop="grade">
          <el-input v-model="formData.grade" placeholder="如：小学三年级"></el-input>
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
import { getChildList, addChild, updateChild, deleteChild } from '@/api/child'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)
const isEdit = ref(false)

const uploadUrl = '/api/upload/image'
const uploadHeaders = {
  Authorization: localStorage.getItem('token') || ''
}

const searchForm = reactive({
  keyword: ''
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const tableData = ref([])

const formData = reactive({
  id: null,
  name: '',
  birthday: '',
  grade: '',
  avatar: ''
})

const formRules = {
  name: [
    { required: true, message: '请输入孩子姓名', trigger: 'blur' }
  ]
}

const getAvatarUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return '/api' + url
}

const formatDate = (date) => {
  if (!date) return '-'
  return date
}

const loadData = () => {
  loading.value = true
  getChildList({
    pageNum: pagination.pageNum,
    pageSize: pagination.pageSize,
    keyword: searchForm.keyword
  }).then(res => {
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

const handleReset = () => {
  searchForm.keyword = ''
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

const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '添加孩子'
  formData.id = null
  formData.name = ''
  formData.birthday = ''
  formData.grade = ''
  formData.avatar = ''
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑孩子'
  formData.id = row.id
  formData.name = row.name
  formData.birthday = row.birthday || ''
  formData.grade = row.grade || ''
  formData.avatar = row.avatar || ''
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除「${row.name}」吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    deleteChild(row.id).then(() => {
      ElMessage.success('删除成功')
      loadData()
    })
  }).catch(() => {})
}

const handleAvatarSuccess = (response) => {
  if (response.code === 200) {
    formData.avatar = response.data.url
    ElMessage.success('上传成功')
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

const beforeAvatarUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}

const handleSubmit = () => {
  formRef.value.validate((valid) => {
    if (valid) {
      submitLoading.value = true
      const request = isEdit.value
        ? updateChild(formData.id, formData)
        : addChild(formData)
      request.then(() => {
        ElMessage.success(isEdit.value ? '修改成功' : '添加成功')
        dialogVisible.value = false
        loadData()
      }).finally(() => {
        submitLoading.value = false
      })
    }
  })
}

onMounted(() => {
  loadData()
})
</script>

<style lang="scss" scoped>
.child-management {
  .search-card {
    margin-bottom: 15px;
  }

  .table-card {
    .pagination-wrapper {
      margin-top: 20px;
      display: flex;
      justify-content: flex-end;
    }
  }

  .avatar-uploader {
    :deep(.el-upload) {
      border: 1px dashed #d9d9d9;
      border-radius: 6px;
      cursor: pointer;
      position: relative;
      overflow: hidden;
      transition: border-color 0.3s;

      &:hover {
        border-color: #409eff;
      }
    }

    .avatar-uploader-icon {
      font-size: 28px;
      color: #8c939d;
      width: 80px;
      height: 80px;
      text-align: center;
      line-height: 80px;
    }
  }
}
</style>
