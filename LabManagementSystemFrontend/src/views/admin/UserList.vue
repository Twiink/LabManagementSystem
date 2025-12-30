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
            <el-button type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="warning" size="small" @click="handleResetPassword(scope.row)">重置</el-button>
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

    <!-- 编辑用户弹窗 -->
    <el-dialog v-model="dialogVisible" title="编辑用户" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="姓名">
          <el-input v-model="form.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="电话">
          <el-input v-model="form.phone" placeholder="请输入电话" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.role" placeholder="请选择角色" style="width: 100%">
            <el-option label="管理员" value="ADMIN" />
            <el-option label="教师" value="TEACHER" />
            <el-option label="学生" value="STUDENT" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" placeholder="请选择状态" style="width: 100%">
            <el-option label="激活" value="ACTIVE" />
            <el-option label="禁用" value="DISABLED" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getUserList, updateUser, resetPassword } from '@/api/user'
import { ElMessage, ElMessageBox } from 'element-plus'

const searchQuery = ref('')
const roleFilter = ref('')
const statusFilter = ref('')
const loading = ref(false)
const submitting = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)

const dialogVisible = ref(false)
const form = reactive({
  id: 0,
  name: '',
  email: '',
  phone: '',
  role: 'STUDENT',
  status: 'ACTIVE'
})

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

const handleEdit = (row: any) => {
  form.id = row.id
  form.name = row.name
  form.email = row.email || ''
  form.phone = row.phone || ''
  form.role = row.role
  form.status = row.status
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!form.name) {
    ElMessage.warning('请输入姓名')
    return
  }

  submitting.value = true
  try {
    await updateUser(form.id, {
      name: form.name,
      email: form.email || undefined,
      phone: form.phone || undefined,
      role: form.role as any,
      status: form.status as any
    })
    ElMessage.success('更新成功')
    dialogVisible.value = false
    loadData()
  } catch (error: any) {
    console.error('更新失败:', error)
    ElMessage.error(error.response?.data?.message || '更新失败')
  } finally {
    submitting.value = false
  }
}

const handleResetPassword = (row: any) => {
  ElMessageBox.confirm(
    `确定要重置用户 "${row.name}" 的密码吗？密码将被重置为：123456`,
    '重置密码',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await resetPassword(row.id)
      ElMessage.success('密码已重置为：123456')
    } catch (error: any) {
      console.error('重置密码失败:', error)
      ElMessage.error(error.response?.data?.message || '重置密码失败')
    }
  })
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
.users-container {
  animation: fade-in 0.5s ease-out;
}

@keyframes fade-in {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.action-bar {
  display: flex;
  align-items: center;
  padding: 24px;
  margin-bottom: 24px;
}

.pagination-container {
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
}

.user-table {
  :deep(.el-button) {
    margin: 0 4px;
  }
}
</style>
