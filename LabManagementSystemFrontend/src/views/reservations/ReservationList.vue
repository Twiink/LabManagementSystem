<template>
  <div class="reservations-container">
    <div class="glass-card action-bar">
      <el-form inline>
        <el-form-item>
          <el-input v-model="searchQuery" placeholder="搜索预约事项" prefix-icon="Search" clearable @clear="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-select v-model="statusFilter" placeholder="状态" clearable style="width: 120px" @change="handleSearch">
            <el-option label="待审批" value="PENDING" />
            <el-option label="已通过" value="APPROVED" />
            <el-option label="已驳回" value="REJECTED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <!-- 我的预约页面可以发起预约 -->
          <el-button v-if="viewMode === 'my'" type="primary" @click="handleApply" icon="Plus">发起预约</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="glass-card">
      <!-- 日历/列表切换（仅管理员预约管理可用） -->
      <div v-if="viewMode === 'manage'" class="view-switch">
        <el-radio-group v-model="displayMode">
          <el-radio-button label="list">列表视图</el-radio-button>
          <el-radio-button label="calendar">日历视图</el-radio-button>
        </el-radio-group>
      </div>

      <div v-if="displayMode === 'list'">
        <el-table :data="tableData" style="width: 100%" v-loading="loading">
          <el-table-column prop="title" label="预约事项" />
          <!-- 审批和管理模式显示申请人 -->
          <el-table-column v-if="viewMode !== 'my'" prop="userName" label="申请人" width="100" />
          <el-table-column prop="resourceName" label="资源" width="150" />
          <el-table-column prop="type" label="类型" width="80">
            <template #default="scope">
              <el-tag effect="plain" round size="small">{{ getTypeLabel(scope.row.type) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="startTime" label="开始时间" width="150">
            <template #default="scope">
              {{ formatDateTime(scope.row.startTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="endTime" label="结束时间" width="150">
            <template #default="scope">
              {{ formatDateTime(scope.row.endTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)" effect="dark" round size="small">{{ getStatusLabel(scope.row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200">
            <template #default="scope">
              <!-- 我的预约：可以取消待审批的预约 -->
              <template v-if="viewMode === 'my'">
                <el-button
                  v-if="scope.row.status === 'PENDING'"
                  link type="warning" size="small"
                  @click="handleCancel(scope.row)"
                >取消</el-button>
                <el-button link type="primary" size="small" @click="handleDetail(scope.row)">详情</el-button>
              </template>

              <!-- 审批模式：教师审批学生预约 -->
              <template v-else-if="viewMode === 'approval'">
                <el-button
                  v-if="scope.row.status === 'PENDING'"
                  link type="success" size="small"
                  @click="handleApprove(scope.row)"
                >通过</el-button>
                <el-button
                  v-if="scope.row.status === 'PENDING'"
                  link type="danger" size="small"
                  @click="handleReject(scope.row)"
                >驳回</el-button>
                <el-button link type="primary" size="small" @click="handleDetail(scope.row)">详情</el-button>
              </template>

              <!-- 管理模式：管理员管理所有预约 -->
              <template v-else-if="viewMode === 'manage'">
                <el-button
                  v-if="scope.row.status === 'PENDING'"
                  link type="success" size="small"
                  @click="handleApprove(scope.row)"
                >通过</el-button>
                <el-button
                  v-if="scope.row.status === 'PENDING'"
                  link type="danger" size="small"
                  @click="handleReject(scope.row)"
                >驳回</el-button>
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
            <div class="calendar-cell">
              <div class="calendar-date">{{ data.day.split('-').slice(2).join('') }}</div>
              <div class="calendar-events">
                <div v-for="item in reservationsByDate[data.day]" :key="item.id" class="calendar-event" :class="getEventClass(item.status)">
                  <span class="event-title">{{ item.title }}</span>
                  <span class="event-time">{{ formatTime(item.startTime) }}</span>
                </div>
              </div>
            </div>
          </template>
        </el-calendar>
      </div>
    </div>

    <!-- Apply Reservation Dialog -->
    <el-dialog v-model="dialogVisible" title="发起预约" width="650px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="预约事项">
          <el-input v-model="form.title" placeholder="例如：分析化学实验" />
        </el-form-item>
        <el-form-item label="预约模式">
          <el-radio-group v-model="form.mode">
            <el-radio label="SINGLE">单次预约</el-radio>
            <el-radio label="BATCH">批量预约</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="资源类型">
           <el-radio-group v-model="resourceType">
            <el-radio label="LAB">实验室</el-radio>
            <el-radio label="DEVICE">设备</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="选择资源">
           <el-select v-model="form.resourceId" placeholder="请选择" style="width: 100%">
             <el-option v-for="item in resourceOptions" :key="item.id" :label="item.name" :value="item.id" />
           </el-select>
        </el-form-item>

        <!-- 单次预约：选择时间段 -->
        <template v-if="form.mode === 'SINGLE'">
          <el-form-item label="时间段">
            <el-date-picker
              v-model="timeRange"
              type="datetimerange"
              range-separator="至"
              start-placeholder="开始时间"
              end-placeholder="结束时间"
              style="width: 100%"
            />
          </el-form-item>
        </template>

        <!-- 批量预约：选择重复规则 -->
        <template v-else>
          <el-form-item label="重复方式">
            <el-radio-group v-model="batchForm.ruleType">
              <el-radio label="DAILY">每天</el-radio>
              <el-radio label="WEEKLY">每周</el-radio>
              <el-radio label="CUSTOM">自定义日期</el-radio>
            </el-radio-group>
          </el-form-item>

          <!-- 每天重复 -->
          <template v-if="batchForm.ruleType === 'DAILY'">
            <el-form-item label="起始日期">
              <el-date-picker v-model="batchForm.startDate" type="date" placeholder="选择起始日期" style="width: 100%" />
            </el-form-item>
            <el-form-item label="时间段">
              <el-time-picker v-model="batchForm.timeSlot" is-range range-separator="至" start-placeholder="开始" end-placeholder="结束" format="HH:mm" style="width: 100%" />
            </el-form-item>
            <el-form-item label="连续天数">
              <el-input-number v-model="batchForm.count" :min="1" :max="30" />
            </el-form-item>
            <el-form-item label="间隔天数">
              <el-input-number v-model="batchForm.interval" :min="1" :max="7" />
              <span style="margin-left: 10px; color: #909399">（每隔几天预约一次）</span>
            </el-form-item>
          </template>

          <!-- 每周重复 -->
          <template v-if="batchForm.ruleType === 'WEEKLY'">
            <el-form-item label="起始日期">
              <el-date-picker v-model="batchForm.startDate" type="date" placeholder="选择起始日期" style="width: 100%" />
            </el-form-item>
            <el-form-item label="时间段">
              <el-time-picker v-model="batchForm.timeSlot" is-range range-separator="至" start-placeholder="开始" end-placeholder="结束" format="HH:mm" style="width: 100%" />
            </el-form-item>
            <el-form-item label="选择周几">
              <el-checkbox-group v-model="batchForm.daysOfWeek">
                <el-checkbox :label="1">周一</el-checkbox>
                <el-checkbox :label="2">周二</el-checkbox>
                <el-checkbox :label="3">周三</el-checkbox>
                <el-checkbox :label="4">周四</el-checkbox>
                <el-checkbox :label="5">周五</el-checkbox>
                <el-checkbox :label="6">周六</el-checkbox>
                <el-checkbox :label="7">周日</el-checkbox>
              </el-checkbox-group>
            </el-form-item>
            <el-form-item label="重复周数">
              <el-input-number v-model="batchForm.weekCount" :min="1" :max="18" />
              <span style="margin-left: 10px; color: #909399">（持续几周）</span>
            </el-form-item>
          </template>

          <!-- 自定义日期 -->
          <template v-if="batchForm.ruleType === 'CUSTOM'">
            <el-form-item label="时间段">
              <el-time-picker v-model="batchForm.timeSlot" is-range range-separator="至" start-placeholder="开始" end-placeholder="结束" format="HH:mm" style="width: 100%" />
            </el-form-item>
            <el-form-item label="选择日期">
              <el-date-picker v-model="batchForm.customDates" type="dates" placeholder="可多选日期" style="width: 100%" />
            </el-form-item>
          </template>

          <el-form-item label="冲突处理">
            <el-radio-group v-model="batchForm.conflictMode">
              <el-radio label="LENIENT">跳过冲突继续创建</el-radio>
              <el-radio label="STRICT">有冲突则全部取消</el-radio>
            </el-radio-group>
          </el-form-item>
        </template>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">提交申请</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailDialogVisible" title="预约详情" width="500px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="预约事项">{{ detailData.title }}</el-descriptions-item>
        <el-descriptions-item label="申请人">{{ detailData.userName }}</el-descriptions-item>
        <el-descriptions-item label="资源">{{ detailData.resourceName }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ getTypeLabel(detailData.type) }}</el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ formatDateTime(detailData.startTime) }}</el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ formatDateTime(detailData.endTime) }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(detailData.status)" effect="dark">{{ getStatusLabel(detailData.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item v-if="detailData.rejectReason" label="驳回原因">{{ detailData.rejectReason }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
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

const route = useRoute()
const userStore = useUserStore()

// 根据路由判断视图模式
// my-reservations: 我的预约（学生/教师）
// approval: 预约审批（教师）
// reservations: 预约管理（管理员）
const viewMode = computed(() => {
  if (route.path === '/my-reservations') return 'my'
  if (route.path === '/approval') return 'approval'
  return 'manage'
})

const searchQuery = ref('')
const statusFilter = ref('')
const tableData = ref<any[]>([])
const loading = ref(false)
const submitting = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)

const dialogVisible = ref(false)
const displayMode = ref<'list' | 'calendar'>('list')
const calendarDate = ref(new Date())
const resourceType = ref('LAB')
const timeRange = ref<Date[]>([])

const form = reactive({
  title: '',
  mode: 'SINGLE' as 'SINGLE' | 'BATCH',
  resourceId: null as number | null
})

// 批量预约表单
const batchForm = reactive({
  ruleType: 'WEEKLY' as 'DAILY' | 'WEEKLY' | 'CUSTOM',
  startDate: null as Date | null,
  timeSlot: null as [Date, Date] | null,
  count: 5,
  interval: 1,
  daysOfWeek: [] as number[],
  weekCount: 4,
  customDates: [] as Date[],
  conflictMode: 'LENIENT' as 'STRICT' | 'LENIENT'
})

// 详情弹窗
const detailDialogVisible = ref(false)
const detailData = reactive({
  title: '',
  userName: '',
  resourceName: '',
  type: '',
  startTime: '',
  endTime: '',
  status: '',
  rejectReason: ''
})

// 资源列表
const labs = ref<any[]>([])
const devices = ref<any[]>([])

const resourceOptions = computed(() => {
  return resourceType.value === 'LAB' ? labs.value : devices.value
})

const reservationsByDate = computed(() => {
  const map: Record<string, any[]> = {}
  tableData.value.forEach(item => {
    const dateKey = item.startTime?.split('T')[0] || item.startTime?.split(' ')[0]
    if (!dateKey) return
    if (!map[dateKey]) map[dateKey] = []
    map[dateKey].push(item)
  })
  return map
})

// 加载资源列表
const loadResources = async () => {
  try {
    const [labRes, deviceRes] = await Promise.all([
      getLabList({ page: 1, pageSize: 100 }),
      getDeviceList({ page: 1, pageSize: 100 })
    ])
    labs.value = labRes.data.items || []
    devices.value = deviceRes.data.items || []
  } catch (error) {
    console.error('加载资源列表失败:', error)
  }
}

// 加载预约数据
const loadData = async () => {
  loading.value = true
  try {
    const params: any = {
      page: currentPage.value,
      pageSize: pageSize.value,
      status: statusFilter.value || undefined
    }

    // 我的预约模式：只查看自己的预约
    if (viewMode.value === 'my') {
      params.requesterId = userStore.userInfo?.id
    }

    // 审批模式：只查看待审批的预约（学生提交的）
    if (viewMode.value === 'approval') {
      params.status = statusFilter.value || 'PENDING'
    }

    const res = await getReservationList(params)
    const items = res.data.items || []

    // 添加资源名称
    tableData.value = items.map((item: any) => ({
      ...item,
      resourceName: item.labId
        ? labs.value.find(l => l.id === item.labId)?.name || `实验室 ${item.labId}`
        : devices.value.find(d => d.id === item.deviceId)?.name || `设备 ${item.deviceId}`,
      userName: item.requester?.name || item.userName || '未知用户'
    }))
    total.value = res.data.total
  } catch (error) {
    console.error('加载预约列表失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await loadResources()
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

const handleApply = () => {
  form.title = ''
  form.mode = 'SINGLE'
  form.resourceId = null
  timeRange.value = []
  // 重置批量表单
  batchForm.ruleType = 'WEEKLY'
  batchForm.startDate = null
  batchForm.timeSlot = null
  batchForm.count = 5
  batchForm.interval = 1
  batchForm.daysOfWeek = []
  batchForm.weekCount = 4
  batchForm.customDates = []
  batchForm.conflictMode = 'LENIENT'
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!form.title || !form.resourceId) {
    ElMessage.warning('请填写预约事项和选择资源')
    return
  }

  submitting.value = true
  try {
    if (form.mode === 'SINGLE') {
      // 单次预约
      if (!timeRange.value || timeRange.value.length !== 2) {
        ElMessage.warning('请选择时间段')
        submitting.value = false
        return
      }
      await createReservation({
        labId: resourceType.value === 'LAB' ? form.resourceId! : 0,
        deviceId: resourceType.value === 'DEVICE' ? form.resourceId! : undefined,
        title: form.title,
        startTime: timeRange.value[0]!.toISOString(),
        endTime: timeRange.value[1]!.toISOString()
      })
      ElMessage.success('预约申请已提交，等待审批')
    } else {
      // 批量预约
      if (!batchForm.timeSlot || batchForm.timeSlot.length !== 2) {
        ElMessage.warning('请选择时间段')
        submitting.value = false
        return
      }

      // 构建起始时间
      let startDateTime: Date
      if (batchForm.ruleType === 'CUSTOM') {
        if (!batchForm.customDates || batchForm.customDates.length === 0) {
          ElMessage.warning('请选择日期')
          submitting.value = false
          return
        }
        // 使用第一个自定义日期作为起始
        startDateTime = new Date(batchForm.customDates[0]!)
      } else {
        if (!batchForm.startDate) {
          ElMessage.warning('请选择起始日期')
          submitting.value = false
          return
        }
        startDateTime = new Date(batchForm.startDate)
      }

      // 设置时间
      const startHour = batchForm.timeSlot[0].getHours()
      const startMin = batchForm.timeSlot[0].getMinutes()
      const endHour = batchForm.timeSlot[1].getHours()
      const endMin = batchForm.timeSlot[1].getMinutes()

      startDateTime.setHours(startHour, startMin, 0, 0)
      const endDateTime = new Date(startDateTime)
      endDateTime.setHours(endHour, endMin, 0, 0)

      // 构建规则
      let ruleValue: any = {}
      if (batchForm.ruleType === 'DAILY') {
        ruleValue = { count: batchForm.count, interval: batchForm.interval }
      } else if (batchForm.ruleType === 'WEEKLY') {
        if (batchForm.daysOfWeek.length === 0) {
          ElMessage.warning('请选择周几')
          submitting.value = false
          return
        }
        ruleValue = {
          daysOfWeek: batchForm.daysOfWeek,
          count: batchForm.daysOfWeek.length * batchForm.weekCount
        }
      } else if (batchForm.ruleType === 'CUSTOM') {
        ruleValue = {
          dates: batchForm.customDates.map(d => {
            const date = new Date(d)
            return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
          })
        }
      }

      await createSeriesReservation({
        labId: resourceType.value === 'LAB' ? form.resourceId! : 0,
        deviceId: resourceType.value === 'DEVICE' ? form.resourceId! : undefined,
        rule: {
          type: batchForm.ruleType,
          value: ruleValue,
          mode: batchForm.conflictMode
        },
        time: {
          startTime: startDateTime.toISOString(),
          endTime: endDateTime.toISOString()
        }
      })
      ElMessage.success('批量预约申请已提交，等待审批')
    }

    dialogVisible.value = false
    loadData()
  } catch (error: any) {
    console.error('提交预约失败:', error)
    ElMessage.error(error.message || '提交失败')
  } finally {
    submitting.value = false
  }
}

const handleApprove = (row: any) => {
  ElMessageBox.confirm('确定通过该预约申请吗？', '审批', {
    confirmButtonText: '通过',
    cancelButtonText: '取消',
    type: 'success'
  }).then(async () => {
    try {
      await approveReservation(row.id)
      ElMessage.success('已批准')
      loadData()
    } catch (error) {
      console.error('审批失败:', error)
    }
  })
}

const handleReject = (row: any) => {
  ElMessageBox.prompt('请输入驳回原因', '审批驳回', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
  }).then(async ({ value }) => {
    try {
      await rejectReservation(row.id, { reason: value })
      ElMessage.info('已驳回')
      loadData()
    } catch (error) {
      console.error('驳回失败:', error)
    }
  })
}

const handleCancel = (row: any) => {
  ElMessageBox.confirm('确定取消该预约吗？', '取消预约', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await cancelReservation(row.id)
      ElMessage.success('已取消')
      loadData()
    } catch (error) {
      console.error('取消失败:', error)
    }
  })
}

const handleDetail = (row: any) => {
  detailData.title = row.title
  detailData.userName = row.userName
  detailData.resourceName = row.resourceName
  detailData.type = row.type
  detailData.startTime = row.startTime
  detailData.endTime = row.endTime
  detailData.status = row.status
  detailData.rejectReason = row.rejectReason || ''
  detailDialogVisible.value = true
}

const getStatusType = (status: string) => {
  const map: Record<string, string> = {
    APPROVED: 'success',
    PENDING: 'warning',
    REJECTED: 'danger',
    CANCELLED: 'info',
    IN_USE: 'primary',
    COMPLETED: 'success',
    EXPIRED: 'info'
  }
  return map[status] || 'info'
}

const getStatusLabel = (status: string) => {
  const map: Record<string, string> = {
    APPROVED: '已通过',
    PENDING: '待审批',
    REJECTED: '已驳回',
    CANCELLED: '已取消',
    IN_USE: '使用中',
    COMPLETED: '已完成',
    EXPIRED: '已过期'
  }
  return map[status] || status
}

const getTypeLabel = (type: string) => {
  const map: Record<string, string> = {
    SINGLE: '单次',
    COURSE: '课程',
    SERIES: '系列'
  }
  return map[type] || type
}

const getEventClass = (status: string) => {
  const map: Record<string, string> = {
    APPROVED: 'event-approved',
    PENDING: 'event-pending',
    REJECTED: 'event-rejected'
  }
  return map[status] || ''
}

const formatDateTime = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const formatTime = (value: string) => {
  if (!value) return ''
  if (value.includes('T')) {
    return value.split('T')[1]?.replace('Z', '').slice(0, 5)
  }
  return value.split(' ')[1]?.slice(0, 5) || ''
}
</script>

<style scoped lang="scss">
.action-bar {
  display: flex;
  align-items: center;
  padding: 15px 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.view-switch {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 16px;
}

.calendar-wrapper {
  padding: 10px 0;
}

.calendar-cell {
  min-height: 90px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.calendar-date {
  font-weight: 600;
  color: #1f2d3d;
}

.calendar-events {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.calendar-event {
  font-size: 12px;
  background: rgba(48, 79, 254, 0.08);
  border-radius: 6px;
  padding: 2px 6px;
  display: flex;
  justify-content: space-between;
  gap: 6px;

  &.event-approved {
    background: rgba(103, 194, 58, 0.15);
    border-left: 3px solid #67c23a;
  }

  &.event-pending {
    background: rgba(230, 162, 60, 0.15);
    border-left: 3px solid #e6a23c;
  }

  &.event-rejected {
    background: rgba(245, 108, 108, 0.15);
    border-left: 3px solid #f56c6c;
  }
}

.event-title {
  color: #1f2d3d;
  font-weight: 500;
}

.event-time {
  color: #3f51b5;
}
</style>
