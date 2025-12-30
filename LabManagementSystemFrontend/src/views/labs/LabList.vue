<template>
  <div class="labs-container">
    <div class="glass-card action-bar">
      <el-form inline>
        <el-form-item>
          <el-input v-model="searchQuery" placeholder="搜索实验室名称/位置" prefix-icon="Search" clearable @clear="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-select v-model="statusFilter" placeholder="状态" clearable style="width: 120px" @change="handleSearch">
            <el-option label="空闲" value="IDLE" />
            <el-option label="使用中" value="IN_USE" />
            <el-option label="预约中" value="RESERVED" />
            <el-option label="维护中" value="MAINTENANCE" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <!-- 只有管理员可以新增实验室 -->
          <el-button v-if="userStore.isAdmin" type="success" @click="handleAdd" icon="Plus">新增实验室</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="glass-card">
      <el-table :data="tableData" style="width: 100%" v-loading="loading">
        <el-table-column prop="name" label="名称" />
        <el-table-column prop="location" label="位置" />
        <el-table-column prop="capacity" label="容量" width="100" />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)" effect="dark" round>{{ getStatusLabel(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <!-- 学生和教师可以预约空闲实验室 -->
            <el-button
              v-if="!userStore.isAdmin && scope.row.status === 'IDLE'"
              link type="primary" size="small"
              @click="handleReserve(scope.row)"
            >预约</el-button>
            <!-- 管理员可以编辑和删除 -->
            <el-button v-if="userStore.isAdmin" link type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button v-if="userStore.isAdmin" link type="danger" size="small" @click="handleDelete(scope.row.id)">删除</el-button>
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

    <!-- Edit/Add Dialog (管理员专用) -->
    <el-dialog v-model="dialogVisible" :title="dialogType === 'add' ? '新增实验室' : '编辑实验室'" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="名称">
          <el-input v-model="form.name" placeholder="请输入实验室名称" />
        </el-form-item>
        <el-form-item label="位置">
          <el-input v-model="form.location" placeholder="请输入实验室位置" />
        </el-form-item>
        <el-form-item label="容量">
          <el-input-number v-model="form.capacity" :min="1" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" placeholder="请输入描述信息" />
        </el-form-item>
        <el-form-item label="状态" v-if="dialogType === 'edit'">
          <el-select v-model="form.status">
            <el-option label="空闲" value="IDLE" />
            <el-option label="使用中" value="IN_USE" />
            <el-option label="预约中" value="RESERVED" />
            <el-option label="维护中" value="MAINTENANCE" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 预约弹窗 (学生/教师使用) -->
    <el-dialog v-model="reserveDialogVisible" title="实验室预约" width="650px">
      <el-form :model="reserveForm" label-width="100px">
        <el-form-item label="实验室">
          <el-input :value="reserveForm.labName" disabled />
        </el-form-item>
        <el-form-item label="预约事项">
          <el-input v-model="reserveForm.title" placeholder="请输入预约事项，如：分析化学实验" />
        </el-form-item>
        <el-form-item label="预约模式">
          <el-radio-group v-model="reserveForm.mode">
            <el-radio label="SINGLE">单次预约</el-radio>
            <el-radio label="BATCH">批量预约</el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 单次预约 -->
        <template v-if="reserveForm.mode === 'SINGLE'">
          <el-form-item label="时间段">
            <el-date-picker
              v-model="reserveForm.timeRange"
              type="datetimerange"
              range-separator="至"
              start-placeholder="开始时间"
              end-placeholder="结束时间"
              style="width: 100%"
            />
          </el-form-item>
        </template>

        <!-- 批量预约 -->
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
        </template>
      </el-form>
      <template #footer>
        <el-button @click="reserveDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleReserveSubmit">提交预约</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '@/store/user'
import { getLabList, createLab, updateLab, updateLabStatus } from '@/api/lab'
import { createReservation, createSeriesReservation } from '@/api/reservation'
import { ElMessage, ElMessageBox } from 'element-plus'

const userStore = useUserStore()

const searchQuery = ref('')
const statusFilter = ref('')
const tableData = ref<any[]>([])
const loading = ref(false)
const submitting = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)

const dialogVisible = ref(false)
const dialogType = ref<'add' | 'edit'>('add')
const form = reactive({
  id: 0,
  name: '',
  location: '',
  capacity: 30,
  description: '',
  status: 'IDLE'
})

// 预约表单
const reserveDialogVisible = ref(false)
const reserveForm = reactive({
  labId: 0,
  labName: '',
  title: '',
  mode: 'SINGLE' as 'SINGLE' | 'BATCH',
  timeRange: [] as Date[]
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
  customDates: [] as Date[]
})

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const res = await getLabList({
      page: currentPage.value,
      pageSize: pageSize.value,
      status: (statusFilter.value || undefined) as any,
      keyword: searchQuery.value || undefined
    })
    tableData.value = res.data.items || []
    total.value = res.data.total
  } catch (error) {
    console.error('加载实验室列表失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
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

const handleAdd = () => {
  dialogType.value = 'add'
  form.id = 0
  form.name = ''
  form.location = ''
  form.capacity = 30
  form.description = ''
  form.status = 'IDLE'
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  dialogType.value = 'edit'
  form.id = row.id
  form.name = row.name
  form.location = row.location
  form.capacity = row.capacity
  form.description = row.description || ''
  form.status = row.status
  dialogVisible.value = true
}

const handleDelete = (id: number) => {
  ElMessageBox.confirm('确定要删除该实验室吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await updateLabStatus(id, { status: 'DISABLED' })
      ElMessage.success('删除成功')
      loadData()
    } catch (error) {
      console.error('删除失败:', error)
    }
  })
}

const handleSubmit = async () => {
  if (!form.name || !form.location) {
    ElMessage.warning('请填写完整信息')
    return
  }

  submitting.value = true
  try {
    if (dialogType.value === 'add') {
      await createLab({
        name: form.name,
        location: form.location,
        capacity: form.capacity,
        description: form.description
      })
      ElMessage.success('新增成功')
    } else {
      await updateLab(form.id, {
        name: form.name,
        location: form.location,
        capacity: form.capacity,
        description: form.description
      })
      const originalItem = tableData.value.find(item => item.id === form.id)
      if (originalItem && originalItem.status !== form.status) {
        await updateLabStatus(form.id, { status: form.status as any })
      }
      ElMessage.success('更新成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('提交失败:', error)
  } finally {
    submitting.value = false
  }
}

// 预约实验室
const handleReserve = (row: any) => {
  reserveForm.labId = row.id
  reserveForm.labName = row.name
  reserveForm.title = ''
  reserveForm.mode = 'SINGLE'
  reserveForm.timeRange = []
  // 重置批量表单
  batchForm.ruleType = 'WEEKLY'
  batchForm.startDate = null
  batchForm.timeSlot = null
  batchForm.count = 5
  batchForm.interval = 1
  batchForm.daysOfWeek = []
  batchForm.weekCount = 4
  batchForm.customDates = []
  reserveDialogVisible.value = true
}

const handleReserveSubmit = async () => {
  if (!reserveForm.title) {
    ElMessage.warning('请填写预约事项')
    return
  }

  submitting.value = true
  try {
    if (reserveForm.mode === 'SINGLE') {
      // 单次预约
      if (!reserveForm.timeRange || reserveForm.timeRange.length !== 2) {
        ElMessage.warning('请选择时间段')
        submitting.value = false
        return
      }
      await createReservation({
        labId: reserveForm.labId,
        title: reserveForm.title,
        startTime: reserveForm.timeRange[0]!.toISOString(),
        endTime: reserveForm.timeRange[1]!.toISOString()
      })
      ElMessage.success('预约申请已提交')
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

      const res = await createSeriesReservation({
        labId: reserveForm.labId,
        title: reserveForm.title,
        rule: {
          type: batchForm.ruleType,
          value: ruleValue,
          mode: 'LENIENT'
        },
        time: {
          startTime: startDateTime.toISOString(),
          endTime: endDateTime.toISOString()
        }
      })

      // 处理批量预约结果
      const data = res.data as any
      const createdCount = data.created?.length || 0
      const failedCount = data.failed?.length || 0

      if (failedCount > 0 && createdCount > 0) {
        // 部分成功
        const failedDates = data.failed.map((f: any) => {
          const d = new Date(f.startTime)
          return `${d.getMonth() + 1}/${d.getDate()}`
        }).join('、')
        ElMessageBox.alert(
          `成功创建 ${createdCount} 个预约，${failedCount} 个预约因冲突失败。\n\n失败日期：${failedDates}`,
          '批量预约结果',
          { type: 'warning' }
        )
      } else if (failedCount > 0 && createdCount === 0) {
        // 全部失败
        ElMessage.error('所有预约都因冲突失败')
      } else {
        // 全部成功
        ElMessage.success(`批量预约成功，共创建 ${createdCount} 个预约`)
      }
    }

    reserveDialogVisible.value = false
    loadData()
  } catch (error: any) {
    console.error('预约失败:', error)
    ElMessage.error(error.message || '预约失败')
  } finally {
    submitting.value = false
  }
}

const getStatusType = (status: string) => {
  const map: Record<string, string> = {
    IDLE: 'success',
    IN_USE: 'warning',
    RESERVED: 'primary',
    MAINTENANCE: 'danger'
  }
  return map[status] || 'info'
}

const getStatusLabel = (status: string) => {
  const map: Record<string, string> = {
    IDLE: '空闲',
    IN_USE: '使用中',
    RESERVED: '预约中',
    MAINTENANCE: '维护中'
  }
  return map[status] || status
}
</script>

<style scoped lang="scss">
.labs-container {
  animation: fade-in 0.5s ease-out;
}

@keyframes fade-in {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.action-bar {
  display: flex;
  align-items: center;
  padding: 24px;
  margin-bottom: 24px;
}

.pagination-container {
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
}
</style>
