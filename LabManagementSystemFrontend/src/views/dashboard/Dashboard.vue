<template>
  <div class="dashboard-container">
    <!-- Stat Cards -->
    <el-row :gutter="20">
      <el-col :span="6" v-for="(stat, index) in statItems" :key="index">
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
        <h3>近期预约</h3>
        <el-button link type="primary" @click="router.push('/reservations')">查看全部</el-button>
      </div>
      <el-table :data="recentReservations" style="width: 100%">
        <el-table-column prop="title" label="预约事项" />
        <el-table-column prop="startTime" label="开始时间" />
        <el-table-column prop="endTime" label="结束时间" />
        <el-table-column prop="status" label="状态">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)" effect="dark" round>{{ scope.row.status }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { mockStats, mockReservations } from '../../api/mock'
import * as echarts from 'echarts'

const router = useRouter()

// Stats Data
const statItems = [
  { label: '实验室总数', value: mockStats.totalLabs, icon: 'OfficeBuilding', bgClass: 'bg-blue', route: '/labs' },
  { label: '设备总数', value: mockStats.totalDevices, icon: 'Monitor', bgClass: 'bg-green', route: '/devices' },
  { label: '当前预约', value: mockStats.activeReservations, icon: 'Calendar', bgClass: 'bg-orange', route: '/reservations' },
  { label: '待审批', value: mockStats.pendingApprovals, icon: 'Bell', bgClass: 'bg-red', route: '/reservations' },
]

const recentReservations = ref(mockReservations)

// Charts
const deviceChartRef = ref<HTMLElement | null>(null)
const labChartRef = ref<HTMLElement | null>(null)
let deviceChart: echarts.ECharts | null = null
let labChart: echarts.ECharts | null = null

const initCharts = () => {
  if (deviceChartRef.value) {
    deviceChart = echarts.init(deviceChartRef.value)
    deviceChart.setOption({
      tooltip: { trigger: 'item' },
      legend: { bottom: '0%', left: 'center' },
      color: ['#409eff', '#67c23a', '#e6a23c', '#f56c6c'],
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
            { value: 30, name: '空闲' },
            { value: 10, name: '使用中' },
            { value: 5, name: '预约中' },
            { value: 3, name: '维护中' }
          ]
        }
      ]
    })
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
          data: [10, 52, 200, 334, 390, 330, 220]
        }
      ]
    })
  }
}

const handleResize = () => {
  deviceChart?.resize()
  labChart?.resize()
}

onMounted(() => {
  nextTick(() => {
    initCharts()
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
    CANCELLED: 'info'
  }
  return map[status] || ''
}
</script>

<style scoped lang="scss">
.stat-card {
  display: flex;
  align-items: center;
  padding: 25px;
  cursor: pointer;
  transition: all 0.3s;
  background: rgba(255, 255, 255, 0.85); /* Clearer card */
  
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
  background: rgba(255, 255, 255, 0.85); /* Clearer card */
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
