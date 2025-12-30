<template>
  <div class="reservations-container">
    <div class="glass-card action-bar">
      <el-form inline>
        <el-form-item label="搜索">
          <el-input 
            v-model="searchQuery" 
            placeholder="搜索预约事项" 
            prefix-icon="Search" 
            clearable 
            @clear="handleSearch" 
            class="industrial-input"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select 
            v-model="statusFilter" 
            placeholder="全部状态" 
            clearable 
            style="width: 140px" 
            @change="handleSearch"
            class="industrial-select"
          >
            <el-option label="待审批" value="PENDING" />
            <el-option label="已通过" value="APPROVED" />
            <el-option label="已驳回" value="REJECTED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch" class="action-btn">查询</el-button>
          <el-button 
            v-if="viewMode === 'my'" 
            class="action-btn add-btn" 
            @click="handleApply" 
            icon="Plus"
          >发起预约</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="glass-card">
      <div v-if="viewMode === 'manage'" class="view-switch-container">
        <div class="mode-switcher">
          <div 
            class="mode-item" 
            :class="{ active: displayMode === 'list' }"
            @click="displayMode = 'list'"
          >列表视图</div>
          <div 
            class="mode-item" 
            :class="{ active: displayMode === 'calendar' }"
            @click="displayMode = 'calendar'"
          >日历视图</div>
        </div>
      </div>

      <div v-if="displayMode === 'list'">
        <el-table :data="tableData" style="width: 100%" v-loading="loading">
          <el-table-column prop="title" label="预约事项" min-width="200" />
          <el-table-column v-if="viewMode !== 'my'" prop="userName" label="申请人" min-width="120" />
          <el-table-column prop="resourceName" label="资源" min-width="180" />
          <!-- 类型列：中文 + 颜色 -->
          <el-table-column prop="type" label="类型" min-width="100">
            <template #default="scope">
              <el-tag 
                :type="getTypeTagType(scope.row.type)" 
                effect="light" 
                class="type-tag"
                size="small"
              >
                {{ getTypeLabel(scope.row.type) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="startTime" label="开始时间" min-width="160">
            <template #default="scope">
              {{ formatDateTimeFull(scope.row.startTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="endTime" label="结束时间" min-width="160">
            <template #default="scope">
              {{ formatDateTimeFull(scope.row.endTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" min-width="120">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)" effect="plain" class="status-tag" size="small">
                {{ getStatusLabel(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" min-width="180" fixed="right">
            <template #default="scope">
              <template v-if="viewMode === 'my'">
                <el-button v-if="scope.row.status === 'PENDING'" link type="warning" size="small" @click="handleCancel(scope.row)">取消</el-button>
                <el-button link type="primary" size="small" @click="handleDetail(scope.row)">详情</el-button>
              </template>
              <template v-else>
                <el-button v-if="scope.row.status === 'PENDING'" link type="success" size="small" @click="handleApprove(scope.row)">通过</el-button>
                <el-button v-if="scope.row.status === 'PENDING'" link type="danger" size="small" @click="handleReject(scope.row)">驳回</el-button>
                <el-button link type="primary" size="small" @click="handleDetail(scope.row)">详情</el-button>
              </template>
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

      <div v-else class="calendar-wrapper">
        <el-calendar v-model="calendarDate">
          <template #date-cell="{ data }">
            <div class="calendar-cell" @click="handleDateClick(data.day)">
              <div class="calendar-date" :class="{ 'is-today': isToday(data.date) }">{{ data.day.split('-').slice(2).join('') }}</div>
              <div class="calendar-events">
                <div v-for="item in reservationsByDate[data.day]" :key="item.id" class="calendar-event" :class="getEventClass(item.status)" @click.stop="handleDetail(item)">
                  <span class="event-title">{{ item.title }}</span>
                  <span class="event-time">{{ formatTimeShort(item.startTime) }}</span>
                </div>
              </div>
            </div>
          </template>
        </el-calendar>
      </div>
    </div>

    <!-- Apply Dialog -->
    <el-dialog v-model="dialogVisible" title="发起预约" width="650px" class="industrial-dialog" :show-close="false">
      <el-form :model="form" label-width="100px">
        <el-form-item label="预约事项"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="预约模式">
          <el-radio-group v-model="form.mode"><el-radio label="SINGLE">单次预约</el-radio><el-radio label="BATCH">批量预约</el-radio></el-radio-group>
        </el-form-item>
        <el-form-item label="资源类型">
           <el-radio-group v-model="resourceType"><el-radio label="LAB">实验室</el-radio><el-radio label="DEVICE">设备</el-radio></el-radio-group>
        </el-form-item>
        <el-form-item label="选择资源">
           <el-select v-model="form.resourceId" style="width: 100%"><el-option v-for="item in resourceOptions" :key="item.id" :label="item.name" :value="item.id" /></el-select>
        </el-form-item>
        <template v-if="form.mode === 'SINGLE'">
          <el-form-item label="时间段"><el-date-picker v-model="timeRange" type="datetimerange" style="width: 100%" /></el-form-item>
        </template>
        <template v-else>
          <el-form-item label="重复方式"><el-radio-group v-model="batchForm.ruleType"><el-radio label="DAILY">每天</el-radio><el-radio label="WEEKLY">每周</el-radio></el-radio-group></el-form-item>
          <el-form-item label="起始日期"><el-date-picker v-model="batchForm.startDate" type="date" style="width: 100%" /></el-form-item>
          <el-form-item label="时间段"><el-time-picker v-model="batchForm.timeSlot" is-range format="HH:mm" style="width: 100%" /></el-form-item>
          <template v-if="batchForm.ruleType === 'DAILY'">
             <el-form-item label="连续天数"><el-input-number v-model="batchForm.count" :min="1" :max="30" /></el-form-item>
          </template>
          <template v-if="batchForm.ruleType === 'WEEKLY'">
             <el-form-item label="选择周几"><el-checkbox-group v-model="batchForm.daysOfWeek"><el-checkbox :label="1">周一</el-checkbox><el-checkbox :label="2">周二</el-checkbox><el-checkbox :label="3">周三</el-checkbox><el-checkbox :label="4">周四</el-checkbox><el-checkbox :label="5">周五</el-checkbox></el-checkbox-group></el-form-item>
             <el-form-item label="持续周数"><el-input-number v-model="batchForm.weekCount" :min="1" :max="18" /></el-form-item>
          </template>
        </template>
      </el-form>
      <template #footer><el-button @click="dialogVisible = false">取消</el-button><el-button type="primary" :loading="submitting" @click="handleSubmit">提交申请</el-button></template>
    </el-dialog>

    <!-- Detail Dialog -->
    <el-dialog v-model="detailDialogVisible" title="预约详情" width="500px" class="industrial-dialog" :show-close="false">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="预约事项">{{ detailData.title }}</el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ formatDateTimeFull(detailData.startTime) }}</el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ formatDateTimeFull(detailData.endTime) }}</el-descriptions-item>
        <el-descriptions-item label="状态"><el-tag :type="getStatusType(detailData.status)" effect="plain" class="status-tag" size="small">{{ getStatusLabel(detailData.status) }}</el-tag></el-descriptions-item>
        <el-descriptions-item label="类型"><el-tag :type="getTypeTagType(detailData.type)" size="small">{{ getTypeLabel(detailData.type) }}</el-tag></el-descriptions-item>
      </el-descriptions>
      <template #footer><el-button @click="detailDialogVisible = false">关闭</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/store/user'
import { getReservationList, createReservation, createSeriesReservation, approveReservation, rejectReservation, cancelReservation } from '@/api/reservation'
import { getLabList } from '@/api/lab'
import { getDeviceList } from '@/api/device'
import { ElMessage, ElMessageBox } from 'element-plus'
import { formatDateKey, formatDateTime, formatTime, parseDateTime } from '@/utils/time'

const route = useRoute()
const userStore = useUserStore()
const viewMode = computed(() => route.path === '/my-reservations' ? 'my' : (route.path === '/approval' ? 'approval' : 'manage'))
const searchQuery = ref(''); const statusFilter = ref(''); const tableData = ref<any[]>([]); const loading = ref(false); const submitting = ref(false); const total = ref(0); const currentPage = ref(1); const pageSize = ref(20)
const dialogVisible = ref(false); const displayMode = ref<'list' | 'calendar'>('list'); const calendarDate = ref(new Date()); const resourceType = ref('LAB'); const timeRange = ref<Date[]>([])
const form = reactive({ title: '', mode: 'SINGLE' as 'SINGLE' | 'BATCH', resourceId: null as number | null })
const batchForm = reactive({ ruleType: 'WEEKLY' as 'DAILY' | 'WEEKLY' | 'CUSTOM', startDate: null as Date | null, timeSlot: null as [Date, Date] | null, count: 5, interval: 1, daysOfWeek: [] as number[], weekCount: 4, customDates: [] as Date[] })
const detailDialogVisible = ref(false); const detailData = reactive({ title: '', startTime: '', endTime: '', status: '', type: '' })
const labs = ref<any[]>([]); const devices = ref<any[]>([])
const resourceOptions = computed(() => resourceType.value === 'LAB' ? labs.value : devices.value)
const toLocalDateKey = (dateStr: string) => { const date = parseDateTime(dateStr); return date ? formatDateKey(date) : '' }
const reservationsByDate = computed(() => { const map: Record<string, any[]> = {}; tableData.value.forEach(item => { const dateKey = toLocalDateKey(item.startTime); if (dateKey) { if (!map[dateKey]) map[dateKey] = []; map[dateKey].push(item) } }); return map })
const isToday = (date: Date) => formatDateKey(date) === formatDateKey(new Date())
const loadResources = async () => { try { const [labRes, deviceRes] = await Promise.all([getLabList({ page: 1, pageSize: 100 }), getDeviceList({ page: 1, pageSize: 100 })]); labs.value = labRes.data.items || []; devices.value = deviceRes.data.items || [] } catch (e) { console.error(e) } }
const loadData = async () => {
  loading.value = true
  try {
    const params: any = { page: currentPage.value, pageSize: pageSize.value, status: statusFilter.value || undefined }
    if (viewMode.value === 'my') params.requesterId = userStore.userInfo?.id
    if (viewMode.value === 'approval') params.status = statusFilter.value || 'PENDING'
    const res = await getReservationList(params); const items = res.data.items || []
    tableData.value = items.map((item: any) => ({ ...item, resourceName: item.labId ? labs.value.find(l => l.id === item.labId)?.name || `实验室 ${item.labId}` : devices.value.find(d => d.id === item.deviceId)?.name || `设备 ${item.deviceId}`, userName: item.requesterName || '未知用户' }))
    total.value = res.data.total
  } catch (e) { console.error(e) } finally { loading.value = false }
}
onMounted(async () => { await loadResources(); loadData() })
const handleSearch = () => { currentPage.value = 1; loadData() }
const handlePageChange = (page: number) => { currentPage.value = page; loadData() }
const handleApply = () => { form.title = ''; form.mode = 'SINGLE'; form.resourceId = null; timeRange.value = []; dialogVisible.value = true }
const handleSubmit = async () => { /* Submit logic */ }
const handleApprove = (row: any) => { ElMessageBox.confirm('确定通过？', '审批', { type: 'success' }).then(async () => { await approveReservation(row.id); ElMessage.success('已批准'); loadData() }) }
const handleReject = (row: any) => { ElMessageBox.prompt('原因', '驳回').then(async ({ value }) => { await rejectReservation(row.id, { reason: value }); ElMessage.info('已驳回'); loadData() }) }
const handleCancel = (row: any) => { ElMessageBox.confirm('取消预约？', '取消', { type: 'warning' }).then(async () => { await cancelReservation(row.id); ElMessage.success('已取消'); loadData() }) }
const handleDetail = (row: any) => { detailData.title = row.title; detailData.startTime = row.startTime; detailData.endTime = row.endTime; detailData.status = row.status; detailData.type = row.type; detailDialogVisible.value = true }
const handleDateClick = (day: string) => { /* Optional */ }
const getStatusType = (s: string) => ({ APPROVED: 'success', PENDING: 'warning', REJECTED: 'danger', CANCELLED: 'info' }[s] || 'info')
const getStatusLabel = (s: string) => ({ APPROVED: '已通过', PENDING: '待审批', REJECTED: '已驳回', CANCELLED: '已取消' }[s] || s)
// New Type Labels
const getTypeLabel = (t: string) => ({ 
  SINGLE: '单次预约', 
  SERIES: '批量预约', 
  RECURRING: '定期预约', 
  COURSE: '课程排课' 
}[t] || t)

// New Type Colors - Custom industrial palette
const getTypeTagType = (t: string) => {
  const map: Record<string, string> = {
    SINGLE: 'info',
    SERIES: 'warning',
    RECURRING: 'danger', // Using red/purple tone for recurring
    COURSE: 'success'
  }
  return map[t] || ''
}
const getEventClass = (s: string) => ({ APPROVED: 'event-approved', PENDING: 'event-pending', REJECTED: 'event-rejected' }[s] || '')
const formatDateTimeFull = (d: string) => formatDateTime(d, { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
const formatTimeShort = (v: string) => formatTime(v)
</script>

<style scoped lang="scss">
.reservations-container { animation: fade-in 0.5s ease-out; }
@keyframes fade-in { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }
.action-bar { display: flex; align-items: center; padding: 24px; margin-bottom: 24px; }
.action-btn { font-weight: 800; letter-spacing: 1px; }
.add-btn { border-style: dashed !important; border-width: 2px !important; background: #fff !important; color: var(--primary-color) !important; border-color: var(--primary-color) !important; &:hover { background: rgba(255, 192, 133, 0.1) !important; } }
.pagination-container { margin-top: 24px; display: flex; justify-content: flex-end; }
.view-switch-container { display: flex; justify-content: flex-end; margin-bottom: 16px;
  .mode-switcher { display: flex; border: 1px solid var(--accent-border); padding: 2px;
    .mode-item { padding: 6px 16px; font-size: 12px; font-weight: 800; color: var(--text-light); cursor: pointer; transition: all 0.2s; &.active { background: var(--primary-color); color: var(--text-main); box-shadow: 2px 2px 0px rgba(0,0,0,0.1); } &:hover:not(.active) { color: var(--text-main); background: var(--bg-color); } }
  }
}
.status-tag { border-radius: 0px !important; font-weight: 800; text-transform: uppercase; letter-spacing: 0.5px; font-family: 'JetBrains Mono', monospace; font-size: 11px; border-width: 1px; padding: 0 8px; height: 22px; line-height: 20px;
  &.el-tag--success { background-color: rgba(16, 185, 129, 0.1) !important; border-color: #10b981 !important; color: #10b981 !important; }
  &.el-tag--warning { background-color: rgba(245, 158, 11, 0.1) !important; border-color: #f59e0b !important; color: #f59e0b !important; }
  &.el-tag--danger { background-color: rgba(239, 68, 68, 0.1) !important; border-color: #ef4444 !important; color: #ef4444 !important; }
  &.el-tag--primary { background-color: rgba(255, 192, 133, 0.1) !important; border-color: var(--accent-border) !important; color: var(--text-main) !important; }
  &.el-tag--info { background-color: #f4f4f5 !important; border-color: #909399 !important; color: #909399 !important; }
}
.type-tag { 
  font-weight: 800; 
  border-radius: 0px !important; 
  font-family: 'JetBrains Mono', 'PingFang SC', sans-serif;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  
  &.el-tag--info {
    background-color: #f4f4f5 !important;
    border-color: #909399 !important;
    color: #909399 !important;
  }
  
  &.el-tag--warning {
    background-color: rgba(255, 192, 133, 0.1) !important;
    border-color: var(--accent-border) !important;
    color: var(--text-main) !important;
  }
  
  &.el-tag--danger {
    background-color: rgba(140, 107, 93, 0.1) !important;
    border-color: #8C6B5D !important;
    color: #5D4037 !important;
  }
  
  &.el-tag--success {
    background-color: rgba(16, 185, 129, 0.1) !important;
    border-color: #10b981 !important;
    color: #10b981 !important;
  }
}
.calendar-wrapper { padding: 10px 0; }
:deep(.el-calendar) { background: transparent; --el-calendar-cell-width: 100%; }
:deep(.el-calendar-table .el-calendar-day) { height: auto; min-height: 100px; padding: 8px; transition: all 0.2s; &:hover { background-color: rgba(255, 192, 133, 0.05); } }
.calendar-cell { height: 100%; display: flex; flex-direction: column; gap: 6px; }
.calendar-date { font-weight: 600; color: var(--text-main); margin-bottom: 4px; &.is-today { color: var(--primary-color); text-decoration: underline; text-underline-offset: 4px; text-decoration-thickness: 3px; } }
.calendar-events { display: flex; flex-direction: column; gap: 4px; }
.calendar-event { font-size: 11px; background: rgba(255, 192, 133, 0.1); border-radius: 0; border: 1px solid transparent; padding: 2px 6px; display: flex; justify-content: space-between; gap: 6px; transition: all 0.2s; cursor: pointer; &:hover { transform: translateX(2px); border-color: var(--accent-border); }
  &.event-approved { background: rgba(16, 185, 129, 0.1); border-left: 3px solid var(--success-color); }
  &.event-pending { background: rgba(245, 158, 11, 0.1); border-left: 3px solid var(--warning-color); }
  &.event-rejected { background: rgba(239, 68, 68, 0.1); border-left: 3px solid var(--danger-color); }
}
.event-title { color: var(--text-main); font-weight: 500; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; max-width: 80px; }
.event-time { color: var(--text-regular); font-size: 10px; }
.industrial-dialog :deep(.el-dialog__header) { background: var(--accent-border); .el-dialog__title { color: #fff; font-weight: 800; } }
</style>