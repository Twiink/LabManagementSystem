<template>
  <div class="audit-container">
    <div class="glass-card">
      <h3>系统审计日志</h3>

      <!-- 筛选条件 -->
      <el-form :inline="true" class="filter-form">
        <el-form-item label="目标类型">
          <el-select v-model="queryParams.targetType" placeholder="全部" clearable style="width: 150px">
            <el-option label="实验室" value="LAB" />
            <el-option label="设备" value="DEVICE" />
            <el-option label="预约" value="RESERVATION" />
            <el-option label="周期预约" value="RESERVATION_SERIES" />
            <el-option label="用户" value="USER" />
            <el-option label="课程" value="COURSE" />
            <el-option label="规则配置" value="RULE_CONFIG" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 日志表格 -->
      <el-table
        v-loading="loading"
        :data="tableData"
        style="width: 100%"
      >
        <el-table-column prop="createdAt" label="时间">
          <template #default="{ row }">
            {{ formatTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column prop="actorName" label="操作人">
          <template #default="{ row }">
            {{ row.actorName || `用户#${row.actorId}` }}
          </template>
        </el-table-column>
        <el-table-column prop="action" label="操作">
          <template #default="{ row }">
            <el-tag :type="getActionType(row.action)" size="small">
              {{ formatAction(row.action) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="targetType" label="对象类型">
          <template #default="{ row }">
            {{ formatTargetType(row.targetType) }}
          </template>
        </el-table-column>
        <el-table-column prop="targetId" label="对象ID" />
        <el-table-column prop="detail" label="详情">
          <template #default="{ row }">
            {{ row.detail || '-' }}
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div style="margin-top: 20px; display: flex; justify-content: flex-end;">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadLogs"
          @current-change="loadLogs"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getAuditLogList } from '../../api/auditLog'
import type { AuditLog, AuditTargetType } from '../../api/types'
import { ElMessage } from 'element-plus'

// 查询参数
const queryParams = ref({
  page: 1,
  pageSize: 20,
  targetType: undefined as AuditTargetType | undefined
})

// 数据
const tableData = ref<AuditLog[]>([])
const total = ref(0)
const loading = ref(false)

// 加载日志数据
const loadLogs = async () => {
  loading.value = true
  try {
    const res = await getAuditLogList(queryParams.value)
    if (res.code === 0 && res.data) {
      tableData.value = res.data.items
      total.value = res.data.total
    } else {
      ElMessage.error(res.message || '加载日志失败')
    }
  } catch (error) {
    console.error('加载日志失败:', error)
    ElMessage.error('加载日志失败')
  } finally {
    loading.value = false
  }
}

// 查询
const handleSearch = () => {
  queryParams.value.page = 1
  loadLogs()
}

// 重置
const handleReset = () => {
  queryParams.value = {
    page: 1,
    pageSize: 20,
    targetType: undefined
  }
  loadLogs()
}

// 格式化时间
const formatTime = (time: string) => {
  if (!time) return '-'
  try {
    const date = new Date(time)
    // 检查日期是否有效
    if (isNaN(date.getTime())) {
      return '-'
    }
    return date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
      hour12: false
    })
  } catch (error) {
    console.error('时间格式化错误:', error)
    return '-'
  }
}

// 格式化操作类型
const formatAction = (action: string) => {
  const actionMap: Record<string, string> = {
    // 通用操作
    CREATE: '创建',
    UPDATE: '更新',
    DELETE: '删除',

    // 用户操作
    RESET_PASSWORD: '重置密码',

    // 预约操作
    RESERVATION_CREATE: '创建预约',
    RESERVATION_UPDATE: '更新预约',
    RESERVATION_CANCEL: '取消预约',
    RESERVATION_APPROVE: '批准预约',
    RESERVATION_REJECT: '拒绝预约',
    RESERVATION_CHECKIN: '签到',
    RESERVATION_CHECKOUT: '签退',
    RESERVATION_OVERRIDE: '覆盖预约',
    RESERVATION_SERIES_CREATE: '创建周期预约',
    RESERVATION_COURSE_CREATE: '创建课程预约',

    // 课程操作
    COURSE_ADD_STUDENT: '添加学生',
    COURSE_REMOVE_STUDENT: '移除学生',

    // 其他
    STATUS_CHANGE: '状态变更',
    LOGIN: '登录',
    LOGOUT: '退出登录'
  }
  return actionMap[action] || action
}

// 获取操作类型的标签颜色
const getActionType = (action: string) => {
  const typeMap: Record<string, 'success' | 'warning' | 'danger' | 'info' | ''> = {
    // 创建类操作 - 绿色
    CREATE: 'success',
    RESERVATION_CREATE: 'success',
    RESERVATION_SERIES_CREATE: 'success',
    RESERVATION_COURSE_CREATE: 'success',
    COURSE_ADD_STUDENT: 'success',

    // 批准类操作 - 绿色
    RESERVATION_APPROVE: 'success',
    RESERVATION_CHECKIN: 'success',

    // 更新/修改类操作 - 橙色
    UPDATE: 'warning',
    RESERVATION_UPDATE: 'warning',
    RESERVATION_CANCEL: 'warning',
    RESERVATION_OVERRIDE: 'warning',
    COURSE_REMOVE_STUDENT: 'warning',
    STATUS_CHANGE: 'warning',
    RESERVATION_CHECKOUT: 'warning',

    // 删除/拒绝类操作 - 红色
    DELETE: 'danger',
    RESERVATION_REJECT: 'danger',

    // 信息类操作 - 蓝色
    RESET_PASSWORD: 'info',

    // 普通操作 - 灰色
    LOGIN: '',
    LOGOUT: ''
  }
  return typeMap[action] || 'info'
}

// 格式化目标类型
const formatTargetType = (type: AuditTargetType) => {
  const typeMap: Record<AuditTargetType, string> = {
    LAB: '实验室',
    DEVICE: '设备',
    RESERVATION: '预约',
    RESERVATION_SERIES: '周期预约',
    USER: '用户',
    COURSE: '课程',
    RULE_CONFIG: '规则配置'
  }
  return typeMap[type] || type
}

// 页面加载时获取数据
onMounted(() => {
  loadLogs()
})
</script>

<style scoped lang="scss">
.audit-container {
  padding: 0;
  animation: fade-in 0.5s ease-out;
}

@keyframes fade-in {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.glass-card {
  padding: 24px;
}

h3 {
  margin: 0 0 24px;
  color: var(--text-main);
  font-size: 20px;
  font-weight: 600;
  display: flex;
  align-items: center;
  
  &::before {
    content: '';
    display: block;
    width: 4px;
    height: 18px;
    background: var(--primary-color);
    margin-right: 12px;
    border-radius: 2px;
  }
}

.filter-form {
  margin-bottom: 24px;
  padding: 16px;
  background: rgba(255, 255, 255, 0.4);
  border-radius: 8px;
}
</style>
