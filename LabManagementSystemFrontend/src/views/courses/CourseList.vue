<template>
  <div class="courses-container">
    <div class="glass-card action-bar">
      <el-button type="primary" icon="Plus" @click="handleAdd">新增课程</el-button>
    </div>

    <div class="glass-card">
      <el-table :data="tableData" style="width: 100%">
        <el-table-column prop="name" label="课程名称" />
        <el-table-column prop="className" label="班级" />
        <el-table-column prop="studentCount" label="人数" width="100" />
        <el-table-column prop="term" label="学期" />
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button link type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button link type="danger" size="small">删除</el-button>
            <el-button link type="success" size="small">绑定预约</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const tableData = ref([
  { id: 1, name: '分析化学', className: '化学2023-1班', studentCount: 45, term: '2024-2025-2' },
  { id: 2, name: '有机化学实验', className: '化学2023-2班', studentCount: 42, term: '2024-2025-2' },
])

const handleAdd = () => {
  console.log('Add Course')
}

const handleEdit = (row: any) => {
  console.log('Edit Course', row)
}
</script>

<style scoped lang="scss">
.action-bar {
  padding: 15px 20px;
}
</style>
