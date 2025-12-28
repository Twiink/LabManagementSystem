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
    <el-row :gutter="20" class="charts-row">
      <el-col :span="12">
        <div class="glass-card chart-card">
          <h3>设备状态分布</h3>
          <div ref="deviceChartRef" class="chart-container"></div>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="glass-card chart-card">
          <h3>实验室本周使用频次</h3>
          <div ref="labChartRef" class="chart-container"></div>
        </div>
      </el-col>
    </el-row>

    <!-- Recent Reservations -->
    <div class="glass-card" style="margin-top: 20px;">
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
            {{ formatDateTime(scope.row.startTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="endTime" label="结束时间">
          <template #default="scope">
            {{ formatDateTime(scope.row.endTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)" effect="dark" round size="small">{{ getStatusLabel(scope.row.status) }}</el-tag>
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
  { label: '实验室总数', value: 0, icon: 'OfficeBuilding', bgClass: 'bg-blue', route: '', roles: ['ADMIN', 'TEACHER', 'STUDENT'] },
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
      userName: item.requester?.name || item.userName || '未知用户'
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
          itemStyle: { borderRadius: [5, 5, 0, 0], color: '#409eff' },
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
      legend: { bottom: '0%', left: 'center' },
      color: ['#67c23a', '#e6a23c', '#409eff', '#f56c6c'],
      series: [
        {
          name: '设备状态',
          type: 'pie',
          radius: ['40%', '70%'],
          avoidLabelOverlap: false,
          itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
          label: { show: false, position: 'center' },
          emphasis: { label: { show: true, fontSize: 20, fontWeight: 'bold' } },
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

const formatDateTime = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}
</script>

<style scoped lang="scss">
.stat-card {
  display: flex;
  align-items: center;
  padding: 25px;
  cursor: pointer;
  transition: all 0.3s;
  background: rgba(255, 255, 255, 0.85);

  &:hover {
    transform: translateY(-5px);
    box-shadow: 0 10px 25px rgba(64, 158, 255, 0.2);
  }

  .icon-wrapper {
    width: 60px;
    height: 60px;
    border-radius: 50%;
    display: flex;
    justify-content: center;
    align-items: center;
    font-size: 28px;
    color: white;
    margin-right: 20px;

    &.bg-blue { background: linear-gradient(135deg, #409eff, #8cc5ff); box-shadow: 0 4px 10px rgba(64, 158, 255, 0.4); }
    &.bg-green { background: linear-gradient(135deg, #67c23a, #95d475); box-shadow: 0 4px 10px rgba(103, 194, 58, 0.4); }
    &.bg-orange { background: linear-gradient(135deg, #e6a23c, #f3d19e); box-shadow: 0 4px 10px rgba(230, 162, 60, 0.4); }
    &.bg-red { background: linear-gradient(135deg, #f56c6c, #fab6b6); box-shadow: 0 4px 10px rgba(245, 108, 108, 0.4); }
  }

  .content {
    .value {
      font-size: 30px;
      font-weight: 800;
      color: #2c3e50;
      line-height: 1.2;
    }
    .label {
      font-size: 14px;
      color: #909399;
      font-weight: 500;
    }
  }
}

.charts-row {
  margin-top: 20px;
}

.chart-card {
  height: 400px;
  background: rgba(255, 255, 255, 0.85);
  display: flex;
  flex-direction: column;

  h3 {
    margin: 0 0 20px;
    color: #2c3e50;
    font-size: 18px;
    font-weight: 600;
  }
}

.chart-container {
  flex: 1;
  width: 100%;
  height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;

  h3 {
    margin: 0;
    font-size: 18px;
    color: #2c3e50;
  }
}
</style>
