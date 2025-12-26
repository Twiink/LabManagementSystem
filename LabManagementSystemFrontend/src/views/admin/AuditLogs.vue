<template>
  <div class="audit-container">
    <div class="glass-card">
      <h3>系统审计日志</h3>
      <el-table :data="tableData" style="width: 100%">
        <el-table-column prop="time" label="时间" width="180" />
        <el-table-column prop="actor" label="操作人" width="120" />
        <el-table-column prop="action" label="动作" width="150" />
        <el-table-column prop="target" label="对象" />
        <el-table-column prop="detail" label="详情" />
      </el-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const tableData = ref([
  { time: '2025-01-15 10:00:00', actor: 'admin', action: 'UPDATE_LAB', target: '化学实验室 A', detail: '状态变更为维护中' },
  { time: '2025-01-15 09:30:00', actor: 'teacher01', action: 'CREATE_RESERVATION', target: 'Reservation #102', detail: '申请课程预约' },
])
</script>
