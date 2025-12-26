<template>
  <div class="devices-container">
    <div class="glass-card action-bar">
      <el-form inline>
        <el-form-item>
          <el-input v-model="searchQuery" placeholder="搜索设备名称/型号" prefix-icon="Search" />
        </el-form-item>
        <el-form-item>
          <el-select v-model="statusFilter" placeholder="状态" clearable style="width: 120px">
            <el-option label="空闲" value="IDLE" />
            <el-option label="使用中" value="IN_USE" />
            <el-option label="维护中" value="MAINTENANCE" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button type="success" @click="handleAdd" icon="Plus">新增设备</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="glass-card">
      <el-table :data="tableData" style="width: 100%">
        <el-table-column prop="name" label="设备名称" />
        <el-table-column prop="model" label="型号" />
        <el-table-column prop="labId" label="所属实验室ID" width="120" />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)" effect="dark" round>{{ scope.row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button link type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-container">
        <el-pagination background layout="prev, pager, next" :total="100" />
      </div>
    </div>

    <!-- Edit/Add Dialog -->
    <el-dialog v-model="dialogVisible" :title="dialogType === 'add' ? '新增设备' : '编辑设备'" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="设备名称">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="型号">
          <el-input v-model="form.model" />
        </el-form-item>
        <el-form-item label="实验室ID">
          <el-input-number v-model="form.labId" :min="1" />
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
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { mockDevices } from '../../api/mock'
import { ElMessage, ElMessageBox } from 'element-plus'

const searchQuery = ref('')
const statusFilter = ref('')
const tableData = ref([...mockDevices])

const dialogVisible = ref(false)
const dialogType = ref<'add' | 'edit'>('add')
const form = reactive({
  id: 0,
  labId: 1,
  name: '',
  model: '',
  status: 'IDLE'
})

const handleSearch = () => {
  tableData.value = mockDevices.filter(item => 
    (!searchQuery.value || item.name.includes(searchQuery.value) || item.model.includes(searchQuery.value)) &&
    (!statusFilter.value || item.status === statusFilter.value)
  )
}

const handleAdd = () => {
  dialogType.value = 'add'
  form.id = 0
  form.labId = 1
  form.name = ''
  form.model = ''
  form.status = 'IDLE'
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  dialogType.value = 'edit'
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleDelete = (id: number) => {
  ElMessageBox.confirm('确定要删除该设备吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    tableData.value = tableData.value.filter(item => item.id !== id)
    ElMessage.success('删除成功')
  })
}

const handleSubmit = () => {
  if (dialogType.value === 'add') {
    const newId = Math.max(...tableData.value.map(i => i.id)) + 1
    tableData.value.push({ ...form, id: newId })
    ElMessage.success('新增成功')
  } else {
    const index = tableData.value.findIndex(item => item.id === form.id)
    if (index !== -1) {
      tableData.value[index] = { ...form }
      ElMessage.success('更新成功')
    }
  }
  dialogVisible.value = false
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