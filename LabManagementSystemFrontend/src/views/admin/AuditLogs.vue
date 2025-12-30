<template>
  <div class="audit-container">
    <div class="glass-card">
      <div class="header-section">
        <h3>系统审计日志</h3>
      </div>

      <!-- 筛选条件 -->
      <div class="action-bar-mini">
        <el-form :inline="true" class="filter-form">
          <el-form-item label="对象类型">
            <el-select 
              v-model="queryParams.targetType" 
              placeholder="全部" 
              clearable 
              style="width: 160px"
              class="industrial-select"
            >
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
            <el-button type="primary" @click="handleSearch" class="action-btn">查询</el-button>
            <el-button @click="handleReset" class="action-btn outline">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 日志表格 - 均匀分布 -->
      <el-table
        v-loading="loading"
        :data="tableData"
        style="width: 100%"
      >
        <el-table-column prop="createdAt" label="时间" min-width="180">
          <template #default="{ row }">
            <span class="mono-text">{{ formatTime(row.createdAt) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="actorName" label="操作人" min-width="120">
          <template #default="{ row }">
            <span class="actor-text">{{ row.actorName || `UID#${row.actorId}` }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="action" label="操作" min-width="140">
          <template #default="{ row }">
            <el-tag 
              :type="getActionType(row.action)" 
              effect="plain" 
              class="status-tag"
              size="small"
            >
              {{ formatAction(row.action) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="targetType" label="对象类型" min-width="120">
          <template #default="{ row }">
            <span class="type-badge">{{ formatTargetType(row.targetType) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="targetId" label="对象ID" min-width="100">
          <template #default="{ row }">
            <span class="mono-text">#{{ row.targetId }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="detail" label="详情" min-width="250">
          <template #default="{ row }">
            <span class="detail-text">{{ row.detail || '-' }}</span>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next"
          background
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
    if (isNaN(date.getTime())) return '-'
    
    const pad = (n: number) => String(n).padStart(2, '0')
    const y = date.getFullYear()
    const m = pad(date.getMonth() + 1)
    const d = pad(date.getDate())
    const hh = pad(date.getHours())
    const mm = pad(date.getMinutes())
    const ss = pad(date.getSeconds())
    return `${y}/${m}/${d} ${hh}:${mm}:${ss}`
  } catch (error) {
    return '-'
  }
}

// 格式化操作类型
const formatAction = (action: string) => {
  const actionMap: Record<string, string> = {
    CREATE: '创建',
    UPDATE: '更新',
    DELETE: '删除',
    RESET_PASSWORD: '重置密码',
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
    COURSE_ADD_STUDENT: '添加学生',
    COURSE_REMOVE_STUDENT: '移除学生',
    STATUS_CHANGE: '状态变更',
    LOGIN: '登录认证',
    LOGOUT: '退出系统'
  }
  return actionMap[action] || action
}

// 获取操作类型的标签颜色
const getActionType = (action: string) => {
  if (action.includes('CREATE') || action.includes('APPROVE') || action === 'LOGIN') return 'success'
  if (action.includes('UPDATE') || action.includes('CHANGE') || action.includes('CANCEL')) return 'warning'
  if (action.includes('DELETE') || action.includes('REJECT')) return 'danger'
  return 'info'
}

// 格式化目标类型
const formatTargetType = (type: AuditTargetType) => {
  const typeMap: Record<AuditTargetType, string> = {
    LAB: '实验室',
    DEVICE: '设备',
    RESERVATION: '预约记录',
    RESERVATION_SERIES: '周期规则',
    USER: '用户账户',
    COURSE: '课程信息',
    RULE_CONFIG: '系统规则'
  }
  return typeMap[type] || type
}

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

.header-section {
  margin-bottom: 32px;
  h3 {
    margin: 0;
    color: var(--text-main);
    font-size: 20px;
    font-weight: 800;
    text-transform: uppercase;
    display: flex;
    align-items: center;
    
    &::before {
      content: '';
      display: block;
      width: 8px;
      height: 20px;
      background: var(--primary-color);
      margin-right: 12px;
      border: 1px solid var(--accent-border);
    }
  }
}

.action-bar-mini {
  margin-bottom: 24px;
  padding: 16px;
  background: var(--bg-color);
  border: 1px solid var(--accent-border);
}

.action-btn {
  font-weight: 800;
  letter-spacing: 1px;
  &.outline { background: #fff; }
}

.mono-text {
  font-family: 'JetBrains Mono', monospace;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-regular);
}

.actor-text {
  font-weight: 700;
  color: var(--text-main);
}

.detail-text {
  font-size: 13px;
  color: var(--text-regular);
}

.type-badge {
  font-size: 11px;
  font-weight: 700;
  color: var(--text-regular);
  padding: 2px 6px;
  border: 1px solid var(--accent-border);
  background: #fff;
  white-space: nowrap;
}

/* Status Tag Style */
.status-tag {
  border-radius: 0px !important;
  font-weight: 800;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  font-family: 'JetBrains Mono', monospace;
  font-size: 11px;
  border-width: 1px;
  padding: 0 8px;
  height: 22px;
  line-height: 20px;
  
  &.el-tag--success {
    background-color: rgba(16, 185, 129, 0.1) !important;
    border-color: #10b981 !important;
    color: #10b981 !important;
  }
  
  &.el-tag--warning {
    background-color: rgba(245, 158, 11, 0.1) !important;
    border-color: #f59e0b !important;
    color: #f59e0b !important;
  }
  
  &.el-tag--danger {
    background-color: rgba(239, 68, 68, 0.1) !important;
    border-color: #ef4444 !important;
    color: #ef4444 !important;
  }
  
  &.el-tag--info {
    background-color: #f4f4f5 !important;
    border-color: #909399 !important;
    color: #909399 !important;
  }
}

.pagination-container {
  margin-top: 32px;
  display: flex;
  justify-content: flex-end;
}
</style>