<template>
  <div class="dashboard-container">
    <!-- Stat Cards -->
    <el-row :gutter="20">
      <el-col :span="6" v-for="(stat, index) in filteredStatItems" :key="index">
        <div class="glass-card stat-card" @click="stat.route ? router.push(stat.route) : null">
          <div class="icon-wrapper" :class="stat.bgClass">
            <el-icon><component :is="stat.icon" /></el-icon>
          </div>
          <div class="content">
            <div class="value">{{ stat.value }}</div>
            <div class="label">{{ stat.label }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- Charts Section -->
    <el-row :gutter="24" class="charts-row">
      <el-col :span="12">
        <div class="glass-card chart-card">
          <div class="chart-header">
            <h3>设备状态分布</h3>
          </div>
          <div ref="deviceChartRef" class="chart-container"></div>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="glass-card chart-card">
          <div class="chart-header">
            <h3>实验室本周使用频次</h3>
          </div>
          <div ref="labChartRef" class="chart-container"></div>
        </div>
      </el-col>
    </el-row>

    <!-- Recent Reservations -->
    <div class="glass-card table-card">
      <div class="card-header">
        <h3>{{ userStore.isAdmin ? '近期预约' : '我的预约' }}</h3>
        <el-button link type="primary" @click="router.push(reservationRoute)">查看全部</el-button>
      </div>
      <el-table :data="recentReservations" style="width: 100%" v-loading="loadingReservations">
        <el-table-column prop="title" label="预约事项" />
        <el-table-column v-if="userStore.isAdmin" prop="userName" label="申请人" width="100" />
        <el-table-column prop="resourceName" label="资源" width="150" />
        <el-table-column prop="startTime" label="开始时间">
          <template #default="scope">
            {{ formatDateTimeShort(scope.row.startTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="endTime" label="结束时间">
          <template #default="scope">
            {{ formatDateTimeShort(scope.row.endTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <!-- Updated Tag Style: Flat, Sharp, Custom Colors -->
            <el-tag 
              :type="getStatusType(scope.row.status)" 
              effect="plain" 
              class="status-tag"
              size="small"
            >
              {{ getStatusLabel(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { getLabList } from '@/api/lab'
import { getDeviceList } from '@/api/device'
import { getReservationList } from '@/api/reservation'
import { formatDateTime } from '@/utils/time'
import * as echarts from 'echarts'

const router = useRouter()
const userStore = useUserStore()

// 根据角色确定预约页面路由
const reservationRoute = computed(() => {
  if (userStore.isAdmin) return '/reservations'
  return '/my-reservations'
})

// 统计数据
const stats = reactive({
  totalLabs: 0,
  totalDevices: 0,
  activeReservations: 0,
  pendingApprovals: 0,
  myReservations: 0
})

// 根据角色显示不同的统计项
const allStatItems = ref([
  { label: '实验室总数', value: 0, icon: 'OfficeBuilding', bgClass: 'bg-primary', route: '', roles: ['ADMIN', 'TEACHER', 'STUDENT'] },
  { label: '设备总数', value: 0, icon: 'Monitor', bgClass: 'bg-green', route: '', roles: ['ADMIN', 'TEACHER', 'STUDENT'] },
  { label: '我的预约', value: 0, icon: 'Calendar', bgClass: 'bg-orange', route: '/my-reservations', roles: ['TEACHER', 'STUDENT'] },
  { label: '当前预约', value: 0, icon: 'Calendar', bgClass: 'bg-orange', route: '/reservations', roles: ['ADMIN'] },
  { label: '待审批', value: 0, icon: 'Bell', bgClass: 'bg-red', route: '/approval', roles: ['TEACHER'] },
  { label: '待审批', value: 0, icon: 'Bell', bgClass: 'bg-red', route: '/reservations', roles: ['ADMIN'] },
])

// 根据用户角色过滤统计项
const filteredStatItems = computed(() => {
  const role = userStore.role || 'STUDENT'
  const items = allStatItems.value.filter(item => item.roles.includes(role))

  // 设置路由
  items.forEach(item => {
    if (item.label === '实验室总数') {
      item.route = userStore.isAdmin ? '/lab-manage' : '/labs'
    } else if (item.label === '设备总数') {
      item.route = userStore.isAdmin ? '/device-manage' : '/devices'
    }
  })

  return items
})

const recentReservations = ref<any[]>([])
const loadingReservations = ref(false)

// 设备状态统计
const deviceStats = reactive({
  idle: 0,
  inUse: 0,
  reserved: 0,
  maintenance: 0
})

// 资源列表（用于显示名称）
const labs = ref<any[]>([])
const devices = ref<any[]>([])

// Charts
const deviceChartRef = ref<HTMLElement | null>(null)
const labChartRef = ref<HTMLElement | null>(null)
let deviceChart: echarts.ECharts | null = null
let labChart: echarts.ECharts | null = null

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

// 加载统计数据
const loadStats = async () => {
  try {
    // 加载实验室数据
    const labRes = await getLabList({ page: 1, pageSize: 1 })
    stats.totalLabs = labRes.data.total
    const labItem = allStatItems.value.find(i => i.label === '实验室总数')
    if (labItem) labItem.value = stats.totalLabs

    // 加载设备数据
    const deviceRes = await getDeviceList({ page: 1, pageSize: 200 })
    stats.totalDevices = deviceRes.data.total
    const deviceItem = allStatItems.value.find(i => i.label === '设备总数')
    if (deviceItem) deviceItem.value = stats.totalDevices

    // 统计设备状态
    const devicesList = deviceRes.data.items || []
    deviceStats.idle = devicesList.filter((d: any) => d.status === 'IDLE').length
    deviceStats.inUse = devicesList.filter((d: any) => d.status === 'IN_USE').length
    deviceStats.reserved = devicesList.filter((d: any) => d.status === 'RESERVED').length
    deviceStats.maintenance = devicesList.filter((d: any) => d.status === 'MAINTENANCE').length

    // 加载预约数据
    loadingReservations.value = true
    const reservationParams: any = { page: 1, pageSize: 10 }

    // 非管理员只加载自己的预约
    if (!userStore.isAdmin) {
      reservationParams.requesterId = userStore.userInfo?.id
    }

    const reservationRes = await getReservationList(reservationParams)
    const items = reservationRes.data.items || []

    // 添加资源名称
    recentReservations.value = items.map((item: any) => ({
      ...item,
      resourceName: item.labId
        ? labs.value.find(l => l.id === item.labId)?.name || `实验室 ${item.labId}`
        : devices.value.find(d => d.id === item.deviceId)?.name || `设备 ${item.deviceId}`,
      userName: item.requesterName || item.userName || '未知用户'
    }))

    // 统计当前预约和待审批
    const allReservationsRes = await getReservationList({ page: 1, pageSize: 200 })
    const allReservations = allReservationsRes.data.items || []

    if (userStore.isAdmin) {
      stats.activeReservations = allReservations.filter((r: any) => ['APPROVED', 'IN_USE'].includes(r.status)).length
      stats.pendingApprovals = allReservations.filter((r: any) => r.status === 'PENDING').length

      const activeItem = allStatItems.value.find(i => i.label === '当前预约' && i.roles.includes('ADMIN'))
      if (activeItem) activeItem.value = stats.activeReservations

      const pendingItem = allStatItems.value.find(i => i.label === '待审批' && i.roles.includes('ADMIN'))
      if (pendingItem) pendingItem.value = stats.pendingApprovals
    } else {
      // 学生/教师显示自己的预约数
      const myReservations = allReservations.filter((r: any) => r.requesterId === userStore.userInfo?.id)
      stats.myReservations = myReservations.length

      const myItem = allStatItems.value.find(i => i.label === '我的预约')
      if (myItem) myItem.value = stats.myReservations

      // 教师显示待审批数
      if (userStore.isTeacher) {
        stats.pendingApprovals = allReservations.filter((r: any) => r.status === 'PENDING').length
        const pendingItem = allStatItems.value.find(i => i.label === '待审批' && i.roles.includes('TEACHER'))
        if (pendingItem) pendingItem.value = stats.pendingApprovals
      }
    }

    loadingReservations.value = false

    // 更新图表
    updateCharts()
  } catch (error) {
    console.error('加载统计数据失败:', error)
    loadingReservations.value = false
  }
}

const initCharts = () => {
  if (deviceChartRef.value) {
    deviceChart = echarts.init(deviceChartRef.value)
  }

  if (labChartRef.value) {
    labChart = echarts.init(labChartRef.value)
    labChart.setOption({
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: [
        {
          type: 'category',
          data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
          axisTick: { alignWithLabel: true }
        }
      ],
      yAxis: [{ type: 'value' }],
      series: [
        {
          name: '预约次数',
          type: 'bar',
          barWidth: '60%',
          itemStyle: { borderRadius: [0, 0, 0, 0], color: '#FFC085' },
          data: [12, 8, 15, 10, 14, 3, 2]
        }
      ]
    })
  }
}

const updateCharts = () => {
  if (deviceChart) {
    deviceChart.setOption({
      tooltip: { trigger: 'item' },
      legend: { bottom: '0%', left: 'center', textStyle: { color: '#4A403A' } },
      color: ['#FFC085', '#E68A2E', '#9D8D85', '#D4A373'],
      series: [
        {
          name: '设备状态',
          type: 'pie',
          radius: ['40%', '70%'],
          avoidLabelOverlap: false,
          itemStyle: { borderRadius: 0, borderColor: '#fff', borderWidth: 2 },
          label: { show: false, position: 'center' },
          emphasis: { label: { show: true, fontSize: 18, fontWeight: 'bold' } },
          data: [
            { value: deviceStats.idle, name: '空闲' },
            { value: deviceStats.inUse, name: '使用中' },
            { value: deviceStats.reserved, name: '预约中' },
            { value: deviceStats.maintenance, name: '维护中' }
          ]
        }
      ]
    })
  }
}

const handleResize = () => {
  deviceChart?.resize()
  labChart?.resize()
}

onMounted(async () => {
  await loadResources()
  nextTick(() => {
    initCharts()
    loadStats()
    window.addEventListener('resize', handleResize)
  })
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  deviceChart?.dispose()
  labChart?.dispose()
})

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
  return map[status] || ''
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

const formatDateTimeShort = (dateStr: string) => formatDateTime(dateStr, {
  month: '2-digit',
  day: '2-digit',
  hour: '2-digit',
  minute: '2-digit'
})
</script>

<style scoped lang="scss">
.dashboard-container {
  animation: fade-in 0.5s ease-out;
  padding-bottom: 40px;
}

@keyframes fade-in {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 24px;
  cursor: pointer;
  height: 100%;
  background: #fff;
  border: 1px solid var(--accent-border);
  box-shadow: var(--glass-shadow);
  transition: all 0.2s cubic-bezier(0.16, 1, 0.3, 1);
  position: relative;
  overflow: hidden;

  &:hover {
    transform: translate(-4px, -4px);
    box-shadow: var(--glass-shadow-hover);
    
    .icon-wrapper {
      transform: scale(1.1);
    }
  }

  .icon-wrapper {
    width: 56px;
    height: 56px;
    border-radius: 0px;
    display: flex;
    justify-content: center;
    align-items: center;
    font-size: 24px;
    color: white;
    margin-right: 20px;
    transition: transform 0.3s ease;
    flex-shrink: 0;
    border: 1px solid var(--accent-border);

    &.bg-primary { 
      background: var(--primary-color);
      box-shadow: 2px 2px 0px var(--accent-border); 
      color: var(--text-main);
    }
    &.bg-green { 
      background: #10b981; 
      box-shadow: 2px 2px 0px var(--accent-border); 
    }
    &.bg-orange { 
      background: #f59e0b; 
      box-shadow: 2px 2px 0px var(--accent-border); 
    }
    &.bg-red { 
      background: #ef4444; 
      box-shadow: 2px 2px 0px var(--accent-border); 
    }
  }

  .content {
    .value {
      font-size: 32px;
      font-weight: 800;
      color: var(--text-main);
      line-height: 1;
      margin-bottom: 4px;
      font-family: 'JetBrains Mono', monospace;
    }
    .label {
      font-size: 13px;
      color: var(--text-light);
      font-weight: 700;
      text-transform: uppercase;
      letter-spacing: 1px;
    }
  }
}

.charts-row {
  margin-top: 70px;
}

.chart-card {
  height: 420px;
  padding: 24px;
  display: flex;
  flex-direction: column;
  background: #fff;
  border: 1px solid var(--accent-border);
  box-shadow: var(--glass-shadow);
  
  .chart-header h3 {
    margin: 0 0 24px;
    color: var(--text-main);
    font-size: 18px;
    font-weight: 800;
    display: flex;
    align-items: center;
    text-transform: uppercase;
    
    &::before {
      content: '';
      display: block;
      width: 8px;
      height: 18px;
      background: var(--primary-color);
      margin-right: 12px;
      border: 1px solid var(--accent-border);
    }
  }
}

.chart-container {
  flex: 1;
  width: 100%;
  height: 100%;
}

.table-card {
  margin-top: 40px;
  padding: 24px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;

  h3 {
    margin: 0;
    font-size: 18px;
    color: var(--text-main);
    font-weight: 800;
    text-transform: uppercase;
  }
}

/* Updated Status Tag Style */
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
  
  &.el-tag--primary {
    background-color: rgba(255, 192, 133, 0.1) !important;
    border-color: var(--accent-border) !important;
    color: var(--text-main) !important;
  }
  
  &.el-tag--info {
    background-color: #f4f4f5 !important;
    border-color: #909399 !important;
    color: #909399 !important;
  }
}

/* Stagger Animation */
.el-col {
  opacity: 0;
  animation: slide-up-fade 0.5s cubic-bezier(0.4, 0, 0.2, 1) forwards;
}

@for $i from 1 through 6 {
  .el-col:nth-child(#{$i}) {
    animation-delay: #{$i * 0.1}s;
  }
}

@keyframes slide-up-fade {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
