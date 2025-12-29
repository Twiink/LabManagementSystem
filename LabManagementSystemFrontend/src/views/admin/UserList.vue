<template>
  <div class="users-container">
    <div class="glass-card action-bar">
      <el-form inline>
        <el-form-item>
          <el-input v-model="searchQuery" placeholder="搜索用户" prefix-icon="Search" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item>
          <el-select v-model="roleFilter" placeholder="角色" clearable style="width: 120px" @change="handleSearch">
            <el-option label="管理员" value="ADMIN" />
            <el-option label="教师" value="TEACHER" />
            <el-option label="学生" value="STUDENT" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-select v-model="statusFilter" placeholder="状态" clearable style="width: 120px" @change="handleSearch">
            <el-option label="激活" value="ACTIVE" />
            <el-option label="禁用" value="DISABLED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button type="success" icon="Plus" @click="handleAdd">新增用户</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="glass-card">
      <el-table :data="tableData" style="width: 100%" v-loading="loading" class="user-table">
        <el-table-column prop="name" label="姓名" min-width="100" />
        <el-table-column prop="email" label="邮箱" min-width="150" />
        <el-table-column prop="phone" label="电话" min-width="120" />
        <el-table-column prop="role" label="角色" width="100" align="center">
          <template #default="scope">
            <el-tag :type="getRoleType(scope.row.role)">{{ getRoleLabel(scope.row.role) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'ACTIVE' ? 'success' : 'info'">
              {{ scope.row.status === 'ACTIVE' ? '激活' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleEdit(scope.row)" icon="Edit">编辑</el-button>
            <el-button type="warning" size="small" @click="handleResetPassword(scope.row)" icon="Lock">重置</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-container">
        <el-pagination
          background
          layout="total, prev, pager, next"
          :total="total"
          :page-size="pageSize"
          :current-page="currentPage"
          @current-change="handlePageChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getUserList } from '@/api/user'
import { ElMessage } from 'element-plus'

const searchQuery = ref('')
const roleFilter = ref('')
const statusFilter = ref('')
const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)

const loadData = async () => {
  loading.value = true
  try {
    const params: any = {
      page: currentPage.value,
      pageSize: pageSize.value
    }

    if (roleFilter.value) {
      params.role = roleFilter.value
    }

    if (statusFilter.value) {
      params.status = statusFilter.value
    }

    const res = await getUserList(params)
    tableData.value = res.data.items || []
    total.value = res.data.total || 0

    // 如果有搜索关键词，在前端进行过滤
    if (searchQuery.value) {
      tableData.value = tableData.value.filter(u =>
        u.name.includes(searchQuery.value) ||
        (u.email && u.email.includes(searchQuery.value)) ||
        (u.phone && u.phone.includes(searchQuery.value))
      )
      total.value = tableData.value.length
    }
  } catch (error) {
    console.error('加载用户列表失败:', error)
    ElMessage.error('加载用户列表失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})

const handleSearch = () => {
  currentPage.value = 1
  loadData()
}

const handlePageChange = (page: number) => {
  currentPage.value = page
  loadData()
}

const handleAdd = () => {
  ElMessage.info('新增用户功能待实现')
}

const handleEdit = (row: any) => {
  ElMessage.info(`编辑用户: ${row.name}`)
}

const handleResetPassword = (row: any) => {
  ElMessage.info(`重置密码: ${row.name}`)
}

const getRoleType = (role: string) => {
  if (role === 'ADMIN') return 'danger'
  if (role === 'TEACHER') return 'warning'
  return 'success'
}

const getRoleLabel = (role: string) => {
  if (role === 'ADMIN') return '管理员'
  if (role === 'TEACHER') return '教师'
  return '学生'
}
</script>

<style scoped lang="scss">
.action-bar {
  padding: 15px 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.user-table {
  font-size: 14px;

  :deep(.el-table__header) {
    th {
      font-size: 15px;
      font-weight: 600;
      background-color: #f5f7fa;
    }
  }

  :deep(.el-table__body) {
    td {
      font-size: 14px;
      padding: 12px 0;
    }
  }

  :deep(.el-button) {
    margin: 0 2px;
    font-size: 13px;

    &.el-button--small {
      padding: 7px 12px;
    }
  }
}
</style>
