<template>
  <div class="users-container">
    <div class="glass-card action-bar">
      <el-input v-model="searchQuery" placeholder="搜索用户" prefix-icon="Search" style="width: 200px; margin-right: 10px;" />
      <el-button type="primary" icon="Plus">新增用户</el-button>
    </div>

    <div class="glass-card">
      <el-table :data="tableData" style="width: 100%">
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="name" label="姓名" />
        <el-table-column prop="role" label="角色">
           <template #default="scope">
            <el-tag :type="getRoleType(scope.row.role)">{{ scope.row.role }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" />
        <el-table-column label="操作">
          <template #default>
            <el-button link type="primary" size="small">编辑</el-button>
            <el-button link type="warning" size="small">重置密码</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const searchQuery = ref('')
const tableData = ref([
  { id: 1, username: 'admin', name: '系统管理员', role: 'ADMIN', status: 'ACTIVE' },
  { id: 2, username: 'teacher01', name: '张老师', role: 'TEACHER', status: 'ACTIVE' },
  { id: 3, username: 'student01', name: '李同学', role: 'STUDENT', status: 'ACTIVE' },
])

const getRoleType = (role: string) => {
  if (role === 'ADMIN') return 'danger'
  if (role === 'TEACHER') return 'warning'
  return 'success'
}
</script>

<style scoped lang="scss">
.action-bar {
  padding: 15px 20px;
  display: flex;
}
</style>
