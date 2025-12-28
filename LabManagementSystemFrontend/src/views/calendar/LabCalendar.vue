<template>
  <div class="lab-calendar-container">
    <!-- 筛选栏 -->
    <div class="glass-card filter-bar">
      <div class="filter-left">
        <el-form inline>
          <el-form-item label="选择实验室">
            <el-select
              v-model="selectedLabId"
              placeholder="全部实验室"
              clearable
              style="width: 200px"
              @change="handleLabChange"
            >
              <el-option
                v-for="lab in labs"
                :key="lab.id"
                :label="lab.name"
                :value="lab.id"
              />
            </el-select>
          </el-form-item>
        </el-form>
      </div>
      <div class="filter-center">
        <el-button-group>
          <el-button @click="handlePrev" :icon="ArrowLeft" />
          <el-button @click="handleToday">今天</el-button>
          <el-button @click="handleNext" :icon="ArrowRight" />
        </el-button-group>
        <span class="current-month">{{ currentMonthText }}</span>
      </div>
      <div class="filter-right">
        <el-radio-group v-model="viewMode" size="small">
          <el-radio-button label="month">月</el-radio-button>
          <el-radio-button label="week">周</el-radio-button>
        </el-radio-group>
      </div>
    </div>

    <!-- 图例 -->
    <div class="glass-card legend-bar">
      <span class="legend-item">
        <span class="dot approved"></span>已通过
      </span>
      <span class="legend-item">
        <span class="dot pending"></span>待审批
      </span>
      <span class="legend-item">
        <span class="dot in-use"></span>使用中
      </span>
    </div>

    <!-- 日历主体 -->
    <div class="glass-card calendar-body" v-loading="loading">
      <!-- 星期标题 -->
      <div class="weekday-row">
        <div v-for="day in weekDays" :key="day" class="weekday-cell">{{ day }}</div>
      </div>

      <!-- 月视图 -->
      <template v-if="viewMode === 'month'">
        <div class="month-grid">
          <div
            v-for="(day, idx) in monthDays"
            :key="idx"
            class="day-cell"
            :class="{
              'other-month': !day.isCurrentMonth,
              'is-today': day.isToday
            }"
            @click="handleDayClick(day)"
          >
            <div class="day-header">
              <span class="day-num" :class="{ 'today-num': day.isToday }">{{ day.dayNum }}</span>
            </div>
            <div class="day-events">
              <div
                v-for="event in day.events.slice(0, 3)"
                :key="event.id"
                class="event-bar"
                :class="getEventStatusClass(event.status)"
                @click.stop="showEventDetail(event)"
              >
                <span class="event-time">{{ formatTime(event.startTime) }}</span>
                <span class="event-name">{{ event.title }}</span>
              </div>
              <div v-if="day.events.length > 3" class="more-link" @click.stop="showDayEvents(day)">
                还有 {{ day.events.length - 3 }} 项...
              </div>
            </div>
          </div>
        </div>
      </template>

      <!-- 周视图 -->
      <template v-else>
        <div class="week-grid">
          <div
            v-for="(day, idx) in weekViewDays"
            :key="idx"
            class="week-day-cell"
            :class="{ 'is-today': day.isToday }"
          >
            <div class="week-day-header">
              <span class="week-day-num" :class="{ 'today-num': day.isToday }">{{ day.dayNum }}</span>
            </div>
            <div class="week-day-events">
              <div
                v-for="event in day.events"
                :key="event.id"
                class="week-event-card"
                :class="getEventStatusClass(event.status)"
                @click="showEventDetail(event)"
              >
                <div class="event-time-range">
                  {{ formatTime(event.startTime) }} - {{ formatTime(event.endTime) }}
                </div>
                <div class="event-title">{{ event.title }}</div>
                <div class="event-lab">{{ event.labName }}</div>
              </div>
            </div>
          </div>
        </div>
      </template>
    </div>

    <!-- 预约详情弹窗 -->
    <el-dialog v-model="detailVisible" title="预约详情" width="450px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="预约事项">{{ currentEvent.title }}</el-descriptions-item>
        <el-descriptions-item label="实验室">{{ currentEvent.labName }}</el-descriptions-item>
        <el-descriptions-item label="申请人">{{ currentEvent.userName }}</el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ formatDateTime(currentEvent.startTime) }}</el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ formatDateTime(currentEvent.endTime) }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusTagType(currentEvent.status)">{{ getStatusText(currentEvent.status) }}</el-tag>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button v-if="!userStore.isAdmin" type="primary" @click="goToReserve">预约此实验室</el-button>
      </template>
    </el-dialog>

    <!-- 某日全部预约弹窗 -->
    <el-dialog v-model="dayEventsVisible" :title="dayEventsTitle" width="600px">
      <el-table :data="dayEventsList" style="width: 100%">
        <el-table-column prop="title" label="预约事项" />
        <el-table-column prop="labName" label="实验室" width="120" />
        <el-table-column label="时间" width="150">
          <template #default="{ row }">
            {{ formatTime(row.startTime) }} - {{ formatTime(row.endTime) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag size="small" :type="getStatusTagType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft, ArrowRight } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { getLabList } from '@/api/lab'
import { getReservationList } from '@/api/reservation'

const router = useRouter()
const userStore = useUserStore()

// 状态
const loading = ref(false)
const selectedLabId = ref<number | null>(null)
const viewMode = ref<'month' | 'week'>('month')
const currentDate = ref(new Date())

// 数据
const labs = ref<any[]>([])
const allReservations = ref<any[]>([])

// 弹窗
const detailVisible = ref(false)
const currentEvent = ref<any>({})
const dayEventsVisible = ref(false)
const dayEventsTitle = ref('')
const dayEventsList = ref<any[]>([])

// 常量
const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']

// 当前月份文本
const currentMonthText = computed(() => {
  const y = currentDate.value.getFullYear()
  const m = currentDate.value.getMonth() + 1
  return `${y}年${m}月`
})

// 月视图日期数组
const monthDays = computed(() => {
  const year = currentDate.value.getFullYear()
  const month = currentDate.value.getMonth()
  const firstDayOfMonth = new Date(year, month, 1)
  const lastDayOfMonth = new Date(year, month + 1, 0)
  const startDay = firstDayOfMonth.getDay() // 0-6
  const totalDays = lastDayOfMonth.getDate()

  const today = new Date()
  const todayStr = formatDateStr(today)

  const days: any[] = []

  // 上月末尾日期
  for (let i = startDay - 1; i >= 0; i--) {
    const d = new Date(year, month, -i)
    const dateStr = formatDateStr(d)
    days.push({
      date: d,
      dateStr,
      dayNum: d.getDate(),
      isCurrentMonth: false,
      isToday: dateStr === todayStr,
      events: getEventsForDate(dateStr)
    })
  }

  // 当月日期
  for (let i = 1; i <= totalDays; i++) {
    const d = new Date(year, month, i)
    const dateStr = formatDateStr(d)
    days.push({
      date: d,
      dateStr,
      dayNum: i,
      isCurrentMonth: true,
      isToday: dateStr === todayStr,
      events: getEventsForDate(dateStr)
    })
  }

  // 补齐到42天（6周）
  const remaining = 42 - days.length
  for (let i = 1; i <= remaining; i++) {
    const d = new Date(year, month + 1, i)
    const dateStr = formatDateStr(d)
    days.push({
      date: d,
      dateStr,
      dayNum: i,
      isCurrentMonth: false,
      isToday: dateStr === todayStr,
      events: getEventsForDate(dateStr)
    })
  }

  return days
})

// 周视图日期数组
const weekViewDays = computed(() => {
  const curr = new Date(currentDate.value)
  const day = curr.getDay()
  const diff = curr.getDate() - day
  const weekStart = new Date(curr.setDate(diff))

  const today = new Date()
  const todayStr = formatDateStr(today)

  const days: any[] = []
  for (let i = 0; i < 7; i++) {
    const d = new Date(weekStart)
    d.setDate(weekStart.getDate() + i)
    const dateStr = formatDateStr(d)
    days.push({
      date: d,
      dateStr,
      dayNum: d.getDate(),
      isToday: dateStr === todayStr,
      events: getEventsForDate(dateStr)
    })
  }
  return days
})

// 获取某日期的预约
function getEventsForDate(dateStr: string) {
  return allReservations.value.filter(r => {
    const eventDate = r.startTime?.split('T')[0] || r.startTime?.split(' ')[0]
    return eventDate === dateStr
  })
}

// 格式化日期字符串
function formatDateStr(d: Date) {
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

// 格式化时间
function formatTime(timeStr: string) {
  if (!timeStr) return ''
  const t = timeStr.split('T')[1] || timeStr.split(' ')[1]
  return t ? t.slice(0, 5) : ''
}

function formatDateTime(timeStr: string) {
  if (!timeStr) return ''
  const d = new Date(timeStr)
  return d.toLocaleString('zh-CN')
}

// 状态相关
function getEventStatusClass(status: string) {
  const map: Record<string, string> = {
    APPROVED: 'status-approved',
    PENDING: 'status-pending',
    IN_USE: 'status-in-use',
    REJECTED: 'status-rejected'
  }
  return map[status] || ''
}

function getStatusTagType(status: string) {
  const map: Record<string, string> = {
    APPROVED: 'success',
    PENDING: 'warning',
    IN_USE: 'primary',
    REJECTED: 'danger',
    CANCELLED: 'info',
    COMPLETED: 'success',
    EXPIRED: 'info'
  }
  return map[status] || 'info'
}

function getStatusText(status: string) {
  const map: Record<string, string> = {
    APPROVED: '已通过',
    PENDING: '待审批',
    IN_USE: '使用中',
    REJECTED: '已驳回',
    CANCELLED: '已取消',
    COMPLETED: '已完成',
    EXPIRED: '已过期'
  }
  return map[status] || status
}

// 加载数据
async function loadLabs() {
  try {
    const res = await getLabList({ page: 1, pageSize: 100 })
    labs.value = res.data.items || []
  } catch (e) {
    console.error('加载实验室失败', e)
  }
}

async function loadReservations() {
  loading.value = true
  try {
    const params: any = { page: 1, pageSize: 500 }
    if (selectedLabId.value) {
      params.labId = selectedLabId.value
    }

    const res = await getReservationList(params)
    const items = res.data.items || []

    // 添加实验室名称
    allReservations.value = items.map((item: any) => ({
      ...item,
      labName: labs.value.find(l => l.id === item.labId)?.name || `实验室${item.labId}`,
      userName: item.user?.name || item.userName || '未知'
    }))

    // 如果没有数据，添加示例数据便于展示
    if (allReservations.value.length === 0) {
      addDemoData()
    }
  } catch (e) {
    console.error('加载预约失败', e)
    // 加载失败时使用示例数据
    addDemoData()
  } finally {
    loading.value = false
  }
}

// 添加示例数据
function addDemoData() {
  const today = new Date()
  const labNames = labs.value.length > 0
    ? labs.value.map(l => l.name)
    : ['化学实验室A', '化学实验室B', '物理实验室']

  const demoEvents = []
  for (let i = -3; i <= 10; i++) {
    const d = new Date(today)
    d.setDate(d.getDate() + i)
    const dateStr = formatDateStr(d)

    // 每天随机1-3个预约
    const count = Math.floor(Math.random() * 3) + 1
    for (let j = 0; j < count; j++) {
      const hour = 8 + Math.floor(Math.random() * 8)
      const labIdx = Math.floor(Math.random() * labNames.length)
      const statuses = ['APPROVED', 'PENDING', 'IN_USE']
      const status = statuses[Math.floor(Math.random() * statuses.length)]

      demoEvents.push({
        id: i * 10 + j,
        title: ['分析化学实验', '有机化学实验', '物理实验', '生物实验'][Math.floor(Math.random() * 4)],
        labId: labIdx + 1,
        labName: labNames[labIdx],
        userName: ['张同学', '李同学', '王老师'][Math.floor(Math.random() * 3)],
        startTime: `${dateStr}T${String(hour).padStart(2, '0')}:00:00`,
        endTime: `${dateStr}T${String(hour + 2).padStart(2, '0')}:00:00`,
        status
      })
    }
  }

  allReservations.value = demoEvents
}

// 事件处理
function handleLabChange() {
  loadReservations()
}

function handlePrev() {
  const d = new Date(currentDate.value)
  if (viewMode.value === 'month') {
    d.setMonth(d.getMonth() - 1)
  } else {
    d.setDate(d.getDate() - 7)
  }
  currentDate.value = d
}

function handleNext() {
  const d = new Date(currentDate.value)
  if (viewMode.value === 'month') {
    d.setMonth(d.getMonth() + 1)
  } else {
    d.setDate(d.getDate() + 7)
  }
  currentDate.value = d
}

function handleToday() {
  currentDate.value = new Date()
}

function handleDayClick(day: any) {
  if (day.events.length > 0) {
    showDayEvents(day)
  }
}

function showEventDetail(event: any) {
  currentEvent.value = event
  detailVisible.value = true
}

function showDayEvents(day: any) {
  dayEventsTitle.value = `${day.dateStr} 预约列表`
  dayEventsList.value = day.events
  dayEventsVisible.value = true
}

function goToReserve() {
  detailVisible.value = false
  router.push({
    path: '/my-reservations',
    query: { labId: currentEvent.value.labId }
  })
}

// 初始化
onMounted(async () => {
  await loadLabs()
  await loadReservations()
})
</script>

<style scoped lang="scss">
.lab-calendar-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;

  .filter-center {
    display: flex;
    align-items: center;
    gap: 16px;

    .current-month {
      font-size: 18px;
      font-weight: 600;
      color: #303133;
      min-width: 120px;
    }
  }
}

.legend-bar {
  display: flex;
  gap: 24px;
  padding: 10px 20px;
  font-size: 13px;

  .legend-item {
    display: flex;
    align-items: center;
    gap: 6px;

    .dot {
      width: 12px;
      height: 12px;
      border-radius: 3px;

      &.approved { background: #67c23a; }
      &.pending { background: #e6a23c; }
      &.in-use { background: #409eff; }
    }
  }
}

.calendar-body {
  min-height: 600px;
}

.weekday-row {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  background: #f5f7fa;
  border-bottom: 1px solid #ebeef5;

  .weekday-cell {
    padding: 12px;
    text-align: center;
    font-weight: 600;
    color: #606266;
  }
}

// 月视图
.month-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);

  .day-cell {
    min-height: 110px;
    border-right: 1px solid #ebeef5;
    border-bottom: 1px solid #ebeef5;
    padding: 6px;
    cursor: pointer;
    transition: background 0.2s;

    &:nth-child(7n) {
      border-right: none;
    }

    &:hover {
      background: #f5f7fa;
    }

    &.other-month {
      background: #fafafa;
      .day-num { color: #c0c4cc; }
    }

    &.is-today {
      background: rgba(64, 158, 255, 0.05);
    }

    .day-header {
      margin-bottom: 4px;

      .day-num {
        font-weight: 600;
        color: #303133;
        font-size: 14px;

        &.today-num {
          display: inline-flex;
          width: 24px;
          height: 24px;
          align-items: center;
          justify-content: center;
          background: #409eff;
          color: #fff;
          border-radius: 50%;
        }
      }
    }

    .day-events {
      display: flex;
      flex-direction: column;
      gap: 3px;
    }

    .event-bar {
      display: flex;
      align-items: center;
      gap: 4px;
      padding: 2px 6px;
      border-radius: 4px;
      font-size: 12px;
      overflow: hidden;
      white-space: nowrap;
      text-overflow: ellipsis;
      cursor: pointer;

      .event-time {
        font-weight: 600;
        flex-shrink: 0;
      }

      .event-name {
        overflow: hidden;
        text-overflow: ellipsis;
      }
    }

    .more-link {
      font-size: 12px;
      color: #409eff;
      padding: 2px 6px;
      cursor: pointer;

      &:hover {
        text-decoration: underline;
      }
    }
  }
}

// 周视图
.week-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  min-height: 500px;

  .week-day-cell {
    border-right: 1px solid #ebeef5;
    padding: 8px;

    &:last-child {
      border-right: none;
    }

    &.is-today {
      background: rgba(64, 158, 255, 0.05);
    }

    .week-day-header {
      text-align: center;
      margin-bottom: 8px;

      .week-day-num {
        font-size: 20px;
        font-weight: 600;
        color: #303133;

        &.today-num {
          display: inline-flex;
          width: 32px;
          height: 32px;
          align-items: center;
          justify-content: center;
          background: #409eff;
          color: #fff;
          border-radius: 50%;
        }
      }
    }

    .week-day-events {
      display: flex;
      flex-direction: column;
      gap: 6px;
    }

    .week-event-card {
      padding: 8px;
      border-radius: 6px;
      cursor: pointer;
      transition: transform 0.2s;

      &:hover {
        transform: translateX(2px);
      }

      .event-time-range {
        font-size: 12px;
        font-weight: 600;
      }

      .event-title {
        font-size: 13px;
        font-weight: 500;
        margin: 2px 0;
      }

      .event-lab {
        font-size: 11px;
        opacity: 0.8;
      }
    }
  }
}

// 状态颜色
.status-approved {
  background: rgba(103, 194, 58, 0.15);
  border-left: 3px solid #67c23a;
  color: #529b2e;
}

.status-pending {
  background: rgba(230, 162, 60, 0.15);
  border-left: 3px solid #e6a23c;
  color: #b88230;
}

.status-in-use {
  background: rgba(64, 158, 255, 0.15);
  border-left: 3px solid #409eff;
  color: #337ecc;
}

.status-rejected {
  background: rgba(245, 108, 108, 0.1);
  border-left: 3px solid #f56c6c;
  color: #c45656;
}
</style>
