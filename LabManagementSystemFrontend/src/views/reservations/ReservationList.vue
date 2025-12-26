<template>
  <div class="reservations-container">
    <div class="glass-card action-bar">
      <el-form inline>
        <el-form-item>
          <el-input v-model="searchQuery" placeholder="搜索预约事项" prefix-icon="Search" />
        </el-form-item>
        <el-form-item>
          <el-select v-model="statusFilter" placeholder="状态" clearable style="width: 120px">
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
      <el-table :data="tableData" style="width: 100%">
        <el-table-column prop="title" label="事项" />
        <el-table-column prop="type" label="类型" width="100">
           <template #default="scope">
            <el-tag effect="plain" round>{{ scope.row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" />
        <el-table-column prop="endTime" label="结束时间" />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)" effect="dark" round>{{ scope.row.status }}</el-tag>
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
        <el-pagination background layout="prev, pager, next" :total="100" />
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
           <el-select v-model="form.resourceId" placeholder="请选择">
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
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">提交申请</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { mockReservations, mockLabs, mockDevices } from '../../api/mock'
import { ElMessage, ElMessageBox } from 'element-plus'

const searchQuery = ref('')
const statusFilter = ref('')
const tableData = ref([...mockReservations])

const dialogVisible = ref(false)
const resourceType = ref('LAB')
const timeRange = ref([])

const form = reactive({
  title: '',
  type: 'SINGLE',
  resourceId: null,
  startTime: '',
  endTime: ''
})

const resourceOptions = computed(() => {
  return resourceType.value === 'LAB' ? mockLabs : mockDevices
})

const handleSearch = () => {
  tableData.value = mockReservations.filter(item => 
    (!searchQuery.value || item.title.includes(searchQuery.value)) &&
    (!statusFilter.value || item.status === statusFilter.value)
  )
}

const handleApply = () => {
  form.title = ''
  form.type = 'SINGLE'
  form.resourceId = null
  timeRange.value = []
  dialogVisible.value = true
}

const handleSubmit = () => {
  if (!form.title || !form.resourceId || !timeRange.value || timeRange.value.length !== 2) {
    ElMessage.warning('请填写完整信息')
    return
  }

  const [start, end] = timeRange.value
  const newReservation = {
    id: Math.floor(Math.random() * 1000) + 1000,
    title: form.title,
    type: form.type,
    startTime: start || '',
    endTime: end || '',
    status: form.type === 'COURSE' ? 'PENDING' : 'APPROVED' // Simple logic: Course needs approval
  }
  
  tableData.value.unshift(newReservation)
  ElMessage.success('预约申请已提交')
  dialogVisible.value = false
}

const handleApprove = (row: any) => {
  ElMessageBox.confirm('确定通过该预约申请吗？', '审批', {
    confirmButtonText: '通过',
    cancelButtonText: '取消',
    type: 'success'
  }).then(() => {
    row.status = 'APPROVED'
    ElMessage.success('已批准')
  })
}

const handleReject = (row: any) => {
   ElMessageBox.prompt('请输入驳回原因', '审批驳回', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
  }).then(({ value }) => {
    row.status = 'REJECTED'
    ElMessage.info(`已驳回: ${value}`)
  })
}

const handleDetail = (row: any) => {
  ElMessage.info(`查看详情: ${row.title}`)
}

const getStatusType = (status: string) => {
  const map: Record<string, string> = {
    APPROVED: 'success',
    PENDING: 'warning',
    REJECTED: 'danger',
    CANCELLED: 'info'
  }
  return map[status] || 'info'
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
</style>