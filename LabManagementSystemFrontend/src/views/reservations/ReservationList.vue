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
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button type="primary" @click="handleApply" icon="Plus">发起预约</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="glass-card">
      <div class="view-switch">
        <el-radio-group v-model="viewMode">
          <el-radio-button label="list">列表视图</el-radio-button>
          <el-radio-button label="calendar">日历视图</el-radio-button>
        </el-radio-group>
      </div>

      <div v-if="viewMode === 'list'">
        <el-table :data="tableData" style="width: 100%" v-loading="loading">
          <el-table-column prop="title" label="事项" />
          <el-table-column prop="type" label="类型" width="100">
            <template #default="scope">
              <el-tag effect="plain" round>{{ getTypeLabel(scope.row.type) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="startTime" label="开始时间">
            <template #default="scope">
              {{ formatDateTime(scope.row.startTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="endTime" label="结束时间">
            <template #default="scope">
              {{ formatDateTime(scope.row.endTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="120">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)" effect="dark" round>{{ getStatusLabel(scope.row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="250">
            <template #default="scope">
              <el-button v-if="scope.row.status === 'PENDING'" link type="success" size="small" @click="handleApprove(scope.row)">通过</el-button>
              <el-button v-if="scope.row.status === 'PENDING'" link type="danger" size="small" @click="handleReject(scope.row)">驳回</el-button>
              <el-button link type="primary" size="small" @click="handleDetail(scope.row)">详情</el-button>
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
                <div v-for="item in reservationsByDate[data.day]" :key="item.id" class="calendar-event">
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
    <el-dialog v-model="dialogVisible" title="发起预约" width="600px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="预约事项">
          <el-input v-model="form.title" placeholder="例如：分析化学实验" />
        </el-form-item>
        <el-form-item label="预约类型">
          <el-radio-group v-model="form.type">
            <el-radio label="SINGLE">单次预约</el-radio>
            <el-radio label="COURSE">课程绑定</el-radio>
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
        <el-form-item label="时间段">
          <el-date-picker
            v-model="timeRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DDTHH:mm:ss[Z]"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">提交申请</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { getReservationList, createReservation, approveReservation, rejectReservation } from '@/api/reservation'
import { getLabList } from '@/api/lab'
import { getDeviceList } from '@/api/device'
import { ElMessage, ElMessageBox } from 'element-plus'

const searchQuery = ref('')
const statusFilter = ref('')
const tableData = ref<any[]>([])
const loading = ref(false)
const submitting = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)

const dialogVisible = ref(false)
const viewMode = ref<'list' | 'calendar'>('list')
const calendarDate = ref(new Date())
const resourceType = ref('LAB')
const timeRange = ref<string[]>([])

const form = reactive({
  title: '',
  type: 'SINGLE',
  resourceId: null as number | null,
  startTime: '',
  endTime: ''
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
    if (!dateKey) {
      return
    }
    if (!map[dateKey]) {
      map[dateKey] = []
    }
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
    const res = await getReservationList({
      page: currentPage.value,
      pageSize: pageSize.value,
      status: statusFilter.value || undefined
    })
    tableData.value = res.data.items || []
    total.value = res.data.total
  } catch (error) {
    console.error('加载预约列表失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
  loadResources()
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
  form.type = 'SINGLE'
  form.resourceId = null
  timeRange.value = []
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!form.title || !form.resourceId || !timeRange.value || timeRange.value.length !== 2) {
    ElMessage.warning('请填写完整信息')
    return
  }

  submitting.value = true
  try {
    const [start, end] = timeRange.value
    await createReservation({
      labId: resourceType.value === 'LAB' ? form.resourceId : undefined,
      deviceId: resourceType.value === 'DEVICE' ? form.resourceId : undefined,
      title: form.title,
      startTime: start,
      endTime: end
    })

    ElMessage.success('预约申请已提交')
    dialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('提交预约失败:', error)
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
      ElMessage.info(`已驳回`)
      loadData()
    } catch (error) {
      console.error('驳回失败:', error)
    }
  })
}

const handleDetail = (row: any) => {
  ElMessage.info(`预约详情: ${row.title}`)
}

const getStatusType = (status: string) => {
  const map: Record<string, string> = {
    APPROVED: 'success',
    PENDING: 'warning',
    REJECTED: 'danger',
    CANCELLED: 'info',
    IN_USE: 'primary',
    COMPLETED: 'success'
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
    COMPLETED: '已完成'
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
  if (!value) {
    return ''
  }
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
}

.event-title {
  color: #1f2d3d;
  font-weight: 500;
}

.event-time {
  color: #3f51b5;
}
</style>
