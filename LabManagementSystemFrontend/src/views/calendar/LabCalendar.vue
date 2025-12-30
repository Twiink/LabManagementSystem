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
              class="industrial-select"
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
        <div class="nav-group">
          <el-button @click="handlePrev" :icon="ArrowLeft" class="nav-btn" />
          <el-button @click="handleToday" class="nav-btn today-btn">今天</el-button>
          <el-button @click="handleNext" :icon="ArrowRight" class="nav-btn" />
        </div>
        <span class="current-month">{{ currentMonthText }}</span>
      </div>
      <div class="filter-right">
        <!-- Replaced radio-group with a custom industrial switcher -->
        <div class="mode-switcher">
          <div 
            class="mode-item" 
            :class="{ active: viewMode === 'month' }"
            @click="viewMode = 'month'"
          >月视图</div>
          <div 
            class="mode-item" 
            :class="{ active: viewMode === 'week' }"
            @click="viewMode = 'week'"
          >周视图</div>
        </div>
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
                + 还有 {{ day.events.length - 3 }} 项
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
    <el-dialog v-model="detailVisible" title="预约详情" width="450px" class="industrial-dialog" :show-close="false">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="预约事项">{{ currentEvent.title }}</el-descriptions-item>
        <el-descriptions-item label="实验室">{{ currentEvent.labName }}</el-descriptions-item>
        <el-descriptions-item label="申请人">{{ currentEvent.userName }}</el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ formatDateTime(currentEvent.startTime) }}</el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ formatDateTime(currentEvent.endTime) }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusTagType(currentEvent.status)" effect="plain" class="status-tag">{{ getStatusText(currentEvent.status) }}</el-tag>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button v-if="!userStore.isAdmin" type="primary" @click="goToReserve">预约此实验室</el-button>
      </template>
    </el-dialog>

    <!-- 某日全部预约弹窗 -->
    <el-dialog v-model="dayEventsVisible" :title="dayEventsTitle" width="600px" class="industrial-dialog" :show-close="false">
      <el-table :data="dayEventsList" style="width: 100%">
        <el-table-column prop="title" label="预约事项" />
        <el-table-column prop="labName" label="实验室" width="120" />
        <el-table-column label="时间" width="150">
          <template #default="{ row }">
            {{ formatTime(row.startTime) }} - {{ formatTime(row.endTime) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag size="small" :type="getStatusTagType(row.status)" effect="plain" class="status-tag">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft, ArrowRight } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { getLabList } from '@/api/lab'
import { getCalendarEvents } from '@/api/calendar'
import { formatDateKey, formatDateTime, formatTime, parseDateTime } from '@/utils/time'

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
  return `${y} / ${String(m).padStart(2, '0')}`
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
    const eventDate = parseDateTime(r.startTime)
    if (!eventDate) return false
    return formatDateKey(eventDate) === dateStr
  })
}

// 格式化日期字符串
function formatDateStr(d: Date) {
  return formatDateKey(d)
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
    const { from, to } = getViewRange()
    const params: any = { from, to }
    if (selectedLabId.value) {
      params.labId = selectedLabId.value
    }

    const res = await getCalendarEvents(params)
    const items = res.data || []

    allReservations.value = items.map((item: any) => ({
      ...item,
      id: item.id ?? item.reservationId,
      labName: labs.value.find(l => l.id === item.labId)?.name || `实验室${item.labId}`,
      userName: item.requesterName || item.userName || item.user?.name || '未知'
    }))

    if (allReservations.value.length === 0) {
      addDemoData()
    }
  } catch (e) {
    console.error('加载预约失败', e)
    addDemoData()
  } finally {
    loading.value = false
  }
}

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
    path: '/reservations',
    query: { labId: currentEvent.value.labId }
  })
}

function getViewRange() {
  const base = new Date(currentDate.value)
  if (viewMode.value === 'month') {
    const year = base.getFullYear()
    const month = base.getMonth()
    const firstDay = new Date(year, month, 1)
    const start = new Date(firstDay)
    start.setDate(firstDay.getDate() - firstDay.getDay())
    start.setHours(0, 0, 0, 0)
    const end = new Date(start)
    end.setDate(start.getDate() + 41)
    end.setHours(23, 59, 59, 999)
    return { from: start.toISOString(), to: end.toISOString() }
  }

  const day = base.getDay()
  const start = new Date(base)
  start.setDate(base.getDate() - day)
  start.setHours(0, 0, 0, 0)
  const end = new Date(start)
  end.setDate(start.getDate() + 6)
  end.setHours(23, 59, 59, 999)
  return { from: start.toISOString(), to: end.toISOString() }
}

onMounted(async () => {
  await loadLabs()
  await loadReservations()
})

watch([currentDate, viewMode], () => {
  loadReservations()
})
</script>

<style scoped lang="scss">
.lab-calendar-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
  animation: fade-in 0.5s ease-out;
}

@keyframes fade-in {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: #fff;

  .filter-center {
    display: flex;
    align-items: center;
    gap: 32px;

    .nav-group {
      display: flex;
      border: 1px solid var(--accent-border);
      
      .nav-btn {
        border: none !important;
        border-radius: 0 !important;
        margin: 0 !important;
        height: 36px;
        background: #fff;
        color: var(--text-main);
        
        &:hover { background: var(--bg-color); color: var(--primary-color); }
        
        &.today-btn {
          border-left: 1px solid var(--accent-border) !important;
          border-right: 1px solid var(--accent-border) !important;
          font-family: 'JetBrains Mono', monospace;
          font-weight: 800;
          font-size: 12px;
        }
      }
    }

    .current-month {
      font-size: 20px;
      font-weight: 800;
      color: var(--text-main);
      font-family: 'JetBrains Mono', monospace;
      letter-spacing: -1px;
    }
  }

  .filter-right {
    .mode-switcher {
      display: flex;
      border: 1px solid var(--accent-border);
      padding: 2px;
      
      .mode-item {
        padding: 6px 16px;
        font-size: 12px;
        font-weight: 800;
        font-family: 'JetBrains Mono', monospace;
        color: var(--text-light);
        cursor: pointer;
        transition: all 0.2s;
        
        &.active {
          background: var(--primary-color);
          color: var(--text-main);
          box-shadow: 2px 2px 0px rgba(0,0,0,0.1);
        }
        
        &:hover:not(.active) {
          color: var(--text-main);
          background: var(--bg-color);
        }
      }
    }
  }
}

.legend-bar {
  display: flex;
  gap: 24px;
  padding: 12px 24px;
  font-size: 12px;

  .legend-item {
    display: flex;
    align-items: center;
    gap: 8px;
    color: var(--text-main);
    font-weight: 700;
    text-transform: uppercase;

    .dot {
      width: 12px;
      height: 12px;
      border: 1px solid var(--accent-border);

      &.approved { background: var(--success-color); }
      &.pending { background: var(--warning-color); }
      &.in-use { background: var(--primary-color); }
    }
  }
}

.calendar-body {
  padding: 0;
  overflow: hidden;
}

.weekday-row {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  background: var(--bg-color);
  border-bottom: 1px solid var(--accent-border);

  .weekday-cell {
    padding: 12px;
    text-align: center;
    font-weight: 800;
    color: var(--text-light);
    font-family: 'JetBrains Mono', monospace;
    font-size: 12px;
  }
}

.month-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  background: #fff;

  .day-cell {
    min-height: 120px;
    border-right: 1px solid rgba(140, 107, 93, 0.1);
    border-bottom: 1px solid rgba(140, 107, 93, 0.1);
    padding: 10px;
    cursor: pointer;
    transition: all 0.2s;

    &:hover { background: var(--bg-color); }

    &.other-month {
      background: #fafafa;
      .day-num { color: #ccc; }
    }

    &.is-today {
      background: rgba(255, 192, 133, 0.05);
      .day-num { border-bottom: 3px solid var(--primary-color); }
    }

    .day-header {
      margin-bottom: 8px;
      .day-num {
        font-weight: 800;
        color: var(--text-main);
        font-size: 14px;
        font-family: 'JetBrains Mono', monospace;
      }
    }

    .day-events {
      display: flex;
      flex-direction: column;
      gap: 4px;
    }

    .event-bar {
      padding: 4px 6px;
      font-size: 11px;
      font-weight: 700;
      border-left: 3px solid transparent;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
      
      .event-time { margin-right: 4px; opacity: 0.7; }
    }

    .more-link {
      font-size: 10px;
      font-weight: 800;
      color: var(--text-light);
      margin-top: 4px;
      &:hover { color: var(--primary-color); }
    }
  }
}

.week-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  min-height: 500px;
  background: #fff;

  .week-day-cell {
    border-right: 1px solid rgba(140, 107, 93, 0.1);
    padding: 16px;

    &.is-today { background: rgba(255, 192, 133, 0.05); }

    .week-day-header {
      text-align: center;
      margin-bottom: 20px;
      .week-day-num {
        font-size: 28px;
        font-weight: 800;
        font-family: 'JetBrains Mono', monospace;
        color: var(--text-main);
        display: inline-block;
        padding-bottom: 4px;
        &.today-num { border-bottom: 4px solid var(--primary-color); }
      }
    }

    .week-event-card {
      padding: 10px;
      margin-bottom: 8px;
      border: 1px solid var(--accent-border);
      border-left-width: 4px;
      cursor: pointer;
      transition: transform 0.2s;
      
      &:hover { transform: scale(1.02); }
      
      .event-time-range { font-size: 11px; font-weight: 800; margin-bottom: 4px; opacity: 0.8; }
      .event-title { font-size: 13px; font-weight: 800; }
      .event-lab { font-size: 11px; margin-top: 4px; opacity: 0.6; }
    }
  }
}

/* Status Classes */
.status-approved { background: rgba(16, 185, 129, 0.05); border-left-color: #10b981; color: #10b981; }
.status-pending { background: rgba(245, 158, 11, 0.05); border-left-color: #f59e0b; color: #f59e0b; }
.status-in-use { background: rgba(255, 192, 133, 0.1); border-left-color: var(--primary-color); color: var(--text-main); }
.status-rejected { background: rgba(239, 68, 68, 0.05); border-left-color: #ef4444; color: #ef4444; }

.status-tag {
  border-radius: 0 !important;
  font-weight: 800;
  font-family: 'JetBrains Mono', monospace;
}

.industrial-dialog :deep(.el-dialog__header) {
  background: var(--accent-border);
  .el-dialog__title { color: #fff; font-weight: 800; }
}
</style>