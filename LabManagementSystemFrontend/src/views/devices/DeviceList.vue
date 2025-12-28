<template>
  <div class="devices-container">
    <div class="glass-card action-bar">
      <el-form inline>
        <el-form-item>
          <el-input v-model="searchQuery" placeholder="搜索设备名称/型号" prefix-icon="Search" clearable @clear="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-select v-model="statusFilter" placeholder="状态" clearable style="width: 120px" @change="handleSearch">
            <el-option label="空闲" value="IDLE" />
            <el-option label="使用中" value="IN_USE" />
            <el-option label="维护中" value="MAINTENANCE" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <!-- 只有管理员可以新增设备 -->
          <el-button v-if="userStore.isAdmin" type="success" @click="handleAdd" icon="Plus">新增设备</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="glass-card">
      <el-table :data="tableData" style="width: 100%" v-loading="loading">
        <el-table-column prop="name" label="设备名称" />
        <el-table-column prop="model" label="型号" />
        <el-table-column prop="labName" label="所属实验室" />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)" effect="dark" round>{{ getStatusLabel(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <!-- 学生和教师只能预约空闲设备 -->
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
    <el-dialog v-model="dialogVisible" :title="dialogType === 'add' ? '新增设备' : '编辑设备'" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="设备名称">
          <el-input v-model="form.name" placeholder="请输入设备名称" />
        </el-form-item>
        <el-form-item label="型号">
          <el-input v-model="form.model" placeholder="请输入设备型号" />
        </el-form-item>
        <el-form-item label="所属实验室">
          <el-select v-model="form.labId" placeholder="请选择实验室" style="width: 100%">
            <el-option v-for="lab in labs" :key="lab.id" :label="lab.name" :value="lab.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" placeholder="请输入描述信息" />
        </el-form-item>
        <el-form-item label="状态" v-if="dialogType === 'edit'">
          <el-select v-model="form.status">
            <el-option label="空闲" value="IDLE" />
            <el-option label="使用中" value="IN_USE" />
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
    <el-dialog v-model="reserveDialogVisible" title="设备预约" width="500px">
      <el-form :model="reserveForm" label-width="100px">
        <el-form-item label="设备">
          <el-input :value="reserveForm.deviceName" disabled />
        </el-form-item>
        <el-form-item label="预约事项">
          <el-input v-model="reserveForm.title" placeholder="请输入预约事项" />
        </el-form-item>
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
import { getDeviceList, createDevice, updateDevice, updateDeviceStatus } from '@/api/device'
import { getLabList } from '@/api/lab'
import { createReservation } from '@/api/reservation'
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

// 实验室列表
const labs = ref<any[]>([])

const dialogVisible = ref(false)
const dialogType = ref<'add' | 'edit'>('add')
const form = reactive({
  id: 0,
  labId: null as number | null,
  name: '',
  model: '',
  description: '',
  status: 'IDLE'
})

// 预约表单
const reserveDialogVisible = ref(false)
const reserveForm = reactive({
  deviceId: 0,
  labId: 0,
  deviceName: '',
  title: '',
  timeRange: [] as Date[]
})

// 加载实验室列表
const loadLabs = async () => {
  try {
    const res = await getLabList({ page: 1, pageSize: 100 })
    labs.value = res.data.items || []
  } catch (error) {
    console.error('加载实验室列表失败:', error)
  }
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const res = await getDeviceList({
      page: currentPage.value,
      pageSize: pageSize.value,
      status: (statusFilter.value || undefined) as any,
      keyword: searchQuery.value || undefined
    })
    // 将实验室ID转换为实验室名称
    const items = res.data.items || []
    tableData.value = items.map((item: any) => ({
      ...item,
      labName: labs.value.find(lab => lab.id === item.labId)?.name || `实验室 ${item.labId}`
    }))
    total.value = res.data.total
  } catch (error) {
    console.error('加载设备列表失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await loadLabs()
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
  form.labId = null
  form.name = ''
  form.model = ''
  form.description = ''
  form.status = 'IDLE'
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  dialogType.value = 'edit'
  form.id = row.id
  form.labId = row.labId
  form.name = row.name
  form.model = row.model
  form.description = row.description || ''
  form.status = row.status
  dialogVisible.value = true
}

const handleDelete = (id: number) => {
  ElMessageBox.confirm('确定要删除该设备吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await updateDeviceStatus(id, { status: 'RETIRED' })
      ElMessage.success('删除成功')
      loadData()
    } catch (error) {
      console.error('删除失败:', error)
    }
  })
}

const handleSubmit = async () => {
  if (!form.name || !form.model || !form.labId) {
    ElMessage.warning('请填写完整信息')
    return
  }

  submitting.value = true
  try {
    if (dialogType.value === 'add') {
      await createDevice({
        labId: form.labId,
        name: form.name,
        model: form.model,
        description: form.description
      })
      ElMessage.success('新增成功')
    } else {
      await updateDevice(form.id, {
        labId: form.labId,
        name: form.name,
        model: form.model,
        description: form.description
      })
      const originalItem = tableData.value.find(item => item.id === form.id)
      if (originalItem && originalItem.status !== form.status) {
        await updateDeviceStatus(form.id, { status: form.status as any })
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

// 预约设备
const handleReserve = (row: any) => {
  reserveForm.deviceId = row.id
  reserveForm.labId = row.labId || 0
  reserveForm.deviceName = row.name
  reserveForm.title = ''
  reserveForm.timeRange = []
  reserveDialogVisible.value = true
}

const handleReserveSubmit = async () => {
  if (!reserveForm.title || !reserveForm.timeRange || reserveForm.timeRange.length !== 2) {
    ElMessage.warning('请填写完整信息')
    return
  }

  submitting.value = true
  try {
    await createReservation({
      labId: reserveForm.labId,
      deviceId: reserveForm.deviceId,
      title: reserveForm.title,
      startTime: reserveForm.timeRange[0]!.toISOString(),
      endTime: reserveForm.timeRange[1]!.toISOString()
    })
    ElMessage.success('预约申请已提交')
    reserveDialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('预约失败:', error)
  } finally {
    submitting.value = false
  }
}

const getStatusType = (status: string) => {
  const map: Record<string, string> = {
    IDLE: 'success',
    IN_USE: 'warning',
    MAINTENANCE: 'danger',
    RESERVED: 'primary'
  }
  return map[status] || 'info'
}

const getStatusLabel = (status: string) => {
  const map: Record<string, string> = {
    IDLE: '空闲',
    IN_USE: '使用中',
    MAINTENANCE: '维护中',
    RESERVED: '预约中'
  }
  return map[status] || status
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
