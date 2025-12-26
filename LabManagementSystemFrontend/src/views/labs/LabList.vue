<template>
  <div class="labs-container">
    <div class="glass-card action-bar">
      <el-form inline>
        <el-form-item>
          <el-input v-model="searchQuery" placeholder="搜索实验室名称/位置" prefix-icon="Search" />
        </el-form-item>
        <el-form-item>
          <el-select v-model="statusFilter" placeholder="状态" clearable style="width: 120px">
            <el-option label="空闲" value="IDLE" />
            <el-option label="使用中" value="IN_USE" />
            <el-option label="预约中" value="RESERVED" />
            <el-option label="维护中" value="MAINTENANCE" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button type="success" @click="handleAdd" icon="Plus">新增实验室</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="glass-card">
      <el-table :data="tableData" style="width: 100%">
        <el-table-column prop="name" label="名称" />
        <el-table-column prop="location" label="位置" />
        <el-table-column prop="capacity" label="容量" width="100" />
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
    <el-dialog v-model="dialogVisible" :title="dialogType === 'add' ? '新增实验室' : '编辑实验室'" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="名称">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="位置">
          <el-input v-model="form.location" />
        </el-form-item>
        <el-form-item label="容量">
          <el-input-number v-model="form.capacity" :min="1" />
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
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { mockLabs } from '../../api/mock'
import { ElMessage, ElMessageBox } from 'element-plus'

const searchQuery = ref('')
const statusFilter = ref('')
const tableData = ref([...mockLabs]) // Clone to allow mutation

const dialogVisible = ref(false)
const dialogType = ref<'add' | 'edit'>('add')
const form = reactive({
  id: 0,
  name: '',
  location: '',
  capacity: 30,
  status: 'IDLE'
})

const handleSearch = () => {
  tableData.value = mockLabs.filter(item => 
    (!searchQuery.value || item.name.includes(searchQuery.value)) &&
    (!statusFilter.value || item.status === statusFilter.value)
  )
}

const handleAdd = () => {
  dialogType.value = 'add'
  form.id = 0
  form.name = ''
  form.location = ''
  form.capacity = 30
  form.status = 'IDLE'
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  dialogType.value = 'edit'
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleDelete = (id: number) => {
  ElMessageBox.confirm('确定要删除该实验室吗？', '警告', {
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
    RESERVED: 'primary',
    MAINTENANCE: 'danger'
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