<template>
  <div class="reward-management">
    <el-row :gutter="15">
      <el-col :span="5">
        <el-card class="category-card" shadow="never">
          <div class="card-header">
            <span>分类</span>
          </div>
          <div
            v-for="cat in categoryList"
            :key="cat.id"
            class="category-item"
            :class="{ active: currentCategoryId === cat.id }"
            @click="selectCategory(cat)">
            <span class="cat-icon">{{ cat.icon || '📦' }}</span>
            <span class="cat-name">{{ cat.name }}</span>
          </div>
          <div v-if="categoryList.length === 0" class="empty-tip">
            <el-empty description="暂无分类" :image-size="60"></el-empty>
          </div>
        </el-card>
      </el-col>

      <el-col :span="19">
        <el-card class="reward-list-card" shadow="never">
          <div class="card-header">
            <div class="header-left">
              <span>奖励列表</span>
              <el-input
                v-model="searchKeyword"
                placeholder="搜索奖励名称"
                clearable
                style="width: 200px; margin-left: 15px;"
                @keyup.enter="handleSearch">
              </el-input>
            </div>
            <el-button type="primary" @click="handleAdd">
              <el-icon><Plus /></el-icon>
              添加奖励
            </el-button>
          </div>

          <el-table :data="tableData" v-loading="loading" style="width: 100%">
            <el-table-column prop="name" label="奖励名称" min-width="150">
              <template #default="scope">
                <div style="display: flex; align-items: center; gap: 8px;">
                  <span style="font-size: 20px;">{{ scope.row.icon || '🎁' }}</span>
                  {{ scope.row.name }}
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="points" label="所需积分" width="100" align="center">
              <template #default="scope">
                <span style="color: #ff9800; font-weight: bold;">{{ scope.row.points }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="stock" label="库存" width="100" align="center">
              <template #default="scope">
                <span v-if="scope.row.stock === -1" style="color: #67c23a;">无限</span>
                <span v-else :style="{ color: scope.row.stock > 10 ? '#67c23a' : '#f56c6c' }">
                  {{ scope.row.stock }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="description" label="描述" min-width="150">
              <template #default="scope">
                {{ scope.row.description || '-' }}
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
                  {{ scope.row.status === 1 ? '上架' : '下架' }}
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
    </el-row>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="550px"
      :close-on-click-modal="false">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="90px">
        <el-form-item label="所属分类" prop="categoryId">
          <el-select v-model="formData.categoryId" placeholder="请选择分类" style="width: 100%;">
            <el-option
              v-for="cat in categoryList"
              :key="cat.id"
              :label="cat.name"
              :value="cat.id">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="奖励名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入奖励名称"></el-input>
        </el-form-item>
        <el-form-item label="图标" prop="icon">
          <el-input v-model="formData.icon" placeholder="输入emoji，如 🎁">
            <template #prepend>表情</template>
          </el-input>
        </el-form-item>
        <el-form-item label="所需积分" prop="points">
          <el-input-number v-model="formData.points" :min="1" :max="99999"></el-input-number>
          <span style="margin-left: 10px; color: #999;">分</span>
        </el-form-item>
        <el-form-item label="库存" prop="stock">
          <el-input-number v-model="formData.stock" :min="-1" :max="99999"></el-input-number>
          <span style="margin-left: 10px; color: #999;">-1 表示无限库存</span>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入奖励描述"
            maxlength="200"
            show-word-limit>
          </el-input>
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="formData.sort" :min="0" :max="999"></el-input-number>
          <span style="margin-left: 10px; color: #999;">数字越小越靠前</span>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :label="1">上架</el-radio>
            <el-radio :label="0">下架</el-radio>
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
import {
  getRewardCategories,
  getRewardList,
  addReward,
  updateReward,
  deleteReward
} from '@/api/reward'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)
const isEdit = ref(false)
const searchKeyword = ref('')

const categoryList = ref([])
const currentCategoryId = ref(null)
const tableData = ref([])

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const formData = reactive({
  id: null,
  categoryId: null,
  name: '',
  icon: '',
  points: 100,
  stock: -1,
  description: '',
  sort: 0,
  status: 1
})

const formRules = {
  categoryId: [
    { required: true, message: '请选择分类', trigger: 'change' }
  ],
  name: [
    { required: true, message: '请输入奖励名称', trigger: 'blur' }
  ],
  points: [
    { required: true, message: '请输入所需积分', trigger: 'blur' }
  ]
}

const loadCategories = () => {
  getRewardCategories().then(res => {
    categoryList.value = res.data
    if (categoryList.value.length > 0) {
      selectCategory(categoryList.value[0])
    }
  })
}

const selectCategory = (cat) => {
  currentCategoryId.value = cat.id
  pagination.pageNum = 1
  loadRewards()
}

const loadRewards = () => {
  loading.value = true
  const params = {
    pageNum: pagination.pageNum,
    pageSize: pagination.pageSize,
    categoryId: currentCategoryId.value,
    keyword: searchKeyword.value
  }
  getRewardList(params).then(res => {
    tableData.value = res.data.list
    pagination.total = res.data.total
  }).finally(() => {
    loading.value = false
  })
}

const handleSearch = () => {
  pagination.pageNum = 1
  loadRewards()
}

const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.pageNum = 1
  loadRewards()
}

const handlePageChange = (page) => {
  pagination.pageNum = page
  loadRewards()
}

const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '添加奖励'
  formData.id = null
  formData.categoryId = currentCategoryId.value
  formData.name = ''
  formData.icon = ''
  formData.points = 100
  formData.stock = -1
  formData.description = ''
  formData.sort = 0
  formData.status = 1
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑奖励'
  formData.id = row.id
  formData.categoryId = row.categoryId
  formData.name = row.name
  formData.icon = row.icon || ''
  formData.points = row.points
  formData.stock = row.stock
  formData.description = row.description || ''
  formData.sort = row.sort
  formData.status = row.status
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除奖励「${row.name}」吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    deleteReward(row.id).then(() => {
      ElMessage.success('删除成功')
      loadRewards()
    })
  }).catch(() => {})
}

const handleSubmit = () => {
  formRef.value.validate((valid) => {
    if (valid) {
      submitLoading.value = true
      const request = isEdit.value
        ? updateReward(formData.id, formData)
        : addReward(formData)
      request.then(() => {
        ElMessage.success(isEdit.value ? '修改成功' : '添加成功')
        dialogVisible.value = false
        loadRewards()
      }).finally(() => {
        submitLoading.value = false
      })
    }
  })
}

onMounted(() => {
  loadCategories()
})
</script>

<style lang="scss" scoped>
.reward-management {
  .category-card {
    height: calc(100vh - 140px);
    overflow-y: auto;
  }

  .reward-list-card {
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

    .header-left {
      display: flex;
      align-items: center;
    }
  }

  .category-item {
    display: flex;
    align-items: center;
    gap: 10px;
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

    .cat-icon {
      font-size: 18px;
    }

    .cat-name {
      font-size: 14px;
      color: #333;
    }
  }

  .pagination-wrapper {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }

  .empty-tip {
    padding: 40px 0;
  }
}
</style>
