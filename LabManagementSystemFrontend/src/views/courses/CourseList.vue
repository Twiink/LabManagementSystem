<template>
  <div class="courses-container">
    <div class="glass-card action-bar">
      <el-form inline>
        <el-form-item>
          <el-input v-model="searchQuery" placeholder="搜索课程名称" prefix-icon="Search" clearable @clear="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-select v-model="termFilter" placeholder="学期" clearable style="width: 150px" @change="handleSearch">
            <el-option label="2024-2025-1" value="2024-2025-1" />
            <el-option label="2024-2025-2" value="2024-2025-2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <!-- 只有教师和管理员可以新增课程 -->
          <el-button v-if="canManage" type="success" @click="handleAdd" icon="Plus">新增课程</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="glass-card">
      <!-- 学生视图：课程表卡片样式 -->
      <template v-if="isStudentView">
        <div class="schedule-grid">
          <div v-for="course in tableData" :key="course.id" class="schedule-card">
            <div class="course-header">
              <span class="course-name">{{ course.name }}</span>
              <el-tag size="small" effect="plain">{{ course.term }}</el-tag>
            </div>
            <div class="course-info">
              <div class="info-item">
                <el-icon><User /></el-icon>
                <span>{{ course.teacherName || '待定' }}</span>
              </div>
              <div class="info-item">
                <el-icon><OfficeBuilding /></el-icon>
                <span>{{ course.labName || '待安排' }}</span>
              </div>
              <div class="info-item">
                <el-icon><Calendar /></el-icon>
                <span>{{ course.scheduleTime || '待定' }}</span>
              </div>
              <div class="info-item">
                <el-icon><UserFilled /></el-icon>
                <span>{{ course.className }} ({{ course.studentCount }}人)</span>
              </div>
            </div>
          </div>
        </div>
        <el-empty v-if="tableData.length === 0" description="暂无课程安排" />
      </template>

      <!-- 教师/管理员视图：表格样式 -->
      <template v-else>
        <el-table :data="tableData" style="width: 100%" v-loading="loading">
          <el-table-column prop="name" label="课程名称" />
          <el-table-column prop="className" label="班级" />
          <el-table-column prop="studentCount" label="人数" width="80" />
          <el-table-column prop="teacherName" label="授课教师" width="120" />
          <el-table-column prop="labName" label="实验室" width="150" />
          <el-table-column prop="term" label="学期" width="120" />
          <el-table-column label="操作" width="250">
            <template #default="scope">
              <el-button link type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
              <el-button link type="danger" size="small" @click="handleDelete(scope.row.id)">删除</el-button>
              <el-button link type="success" size="small" @click="handleBindReservation(scope.row)">绑定预约</el-button>
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
      </template>
    </div>

    <!-- 新增/编辑课程弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogType === 'add' ? '新增课程' : '编辑课程'" width="550px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="课程名称">
          <el-input v-model="form.name" placeholder="请输入课程名称" />
        </el-form-item>
        <el-form-item label="班级">
          <el-input v-model="form.className" placeholder="请输入班级，如：化学2023-1班" />
        </el-form-item>
        <el-form-item label="学生人数">
          <el-input-number v-model="form.studentCount" :min="1" :max="200" />
        </el-form-item>
        <el-form-item label="授课教师" v-if="userStore.isAdmin">
          <el-input v-model="form.teacherName" placeholder="请输入授课教师" />
        </el-form-item>
        <el-form-item label="学期">
          <el-select v-model="form.term" placeholder="请选择学期" style="width: 100%">
            <el-option label="2024-2025-1" value="2024-2025-1" />
            <el-option label="2024-2025-2" value="2024-2025-2" />
          </el-select>
        </el-form-item>
        <el-form-item label="上课时间">
          <el-input v-model="form.scheduleTime" placeholder="如：周一 8:00-10:00" />
        </el-form-item>
        <el-form-item label="实验室">
          <el-select v-model="form.labId" placeholder="请选择实验室" style="width: 100%">
            <el-option v-for="lab in labs" :key="lab.id" :label="lab.name" :value="lab.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 绑定预约弹窗 -->
    <el-dialog v-model="bindDialogVisible" title="课程绑定预约" width="600px">
      <el-form :model="bindForm" label-width="100px">
        <el-form-item label="课程">
          <el-input :value="bindForm.courseName" disabled />
        </el-form-item>
        <el-form-item label="实验室">
          <el-select v-model="bindForm.labId" placeholder="请选择实验室" style="width: 100%">
            <el-option v-for="lab in labs" :key="lab.id" :label="lab.name" :value="lab.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="预约周次">
          <el-checkbox-group v-model="bindForm.weeks">
            <el-checkbox v-for="w in 18" :key="w" :label="w">第{{ w }}周</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="上课时间">
          <el-time-picker
            v-model="bindForm.timeRange"
            is-range
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            format="HH:mm"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="bindDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleBindSubmit">批量创建预约</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/store/user'
import { getLabList } from '@/api/lab'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const userStore = useUserStore()

// 判断是否是学生视图（课程表）
const isStudentView = computed(() => {
  return route.path === '/schedule' || userStore.isStudent
})

// 判断是否可以管理课程
const canManage = computed(() => {
  return userStore.isTeacher || userStore.isAdmin
})

const searchQuery = ref('')
const termFilter = ref('')
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
  name: '',
  className: '',
  studentCount: 30,
  teacherName: '',
  term: '2024-2025-2',
  scheduleTime: '',
  labId: null as number | null
})

// 绑定预约
const bindDialogVisible = ref(false)
const bindForm = reactive({
  courseId: 0,
  courseName: '',
  labId: null as number | null,
  weeks: [] as number[],
  timeRange: null as [Date, Date] | null
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

// 加载课程数据（模拟数据）
const loadData = async () => {
  loading.value = true
  try {
    // TODO: 替换为真实 API 调用
    await new Promise(resolve => setTimeout(resolve, 300))

    const mockData = [
      {
        id: 1,
        name: '分析化学实验',
        className: '化学2023-1班',
        studentCount: 45,
        teacherName: '张老师',
        labName: '化学实验室A',
        labId: 1,
        term: '2024-2025-2',
        scheduleTime: '周一 8:00-10:00'
      },
      {
        id: 2,
        name: '有机化学实验',
        className: '化学2023-2班',
        studentCount: 42,
        teacherName: '李老师',
        labName: '化学实验室B',
        labId: 2,
        term: '2024-2025-2',
        scheduleTime: '周三 14:00-16:00'
      },
      {
        id: 3,
        name: '物理化学实验',
        className: '化学2023-1班',
        studentCount: 45,
        teacherName: '王老师',
        labName: '物理化学实验室',
        labId: 3,
        term: '2024-2025-2',
        scheduleTime: '周五 10:00-12:00'
      }
    ]

    // 根据搜索条件过滤
    let filtered = mockData
    if (searchQuery.value) {
      filtered = filtered.filter(c => c.name.includes(searchQuery.value))
    }
    if (termFilter.value) {
      filtered = filtered.filter(c => c.term === termFilter.value)
    }

    tableData.value = filtered
    total.value = filtered.length
  } catch (error) {
    console.error('加载课程列表失败:', error)
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
  form.name = ''
  form.className = ''
  form.studentCount = 30
  form.teacherName = userStore.isTeacher ? (userStore.userInfo?.name || '') : ''
  form.term = '2024-2025-2'
  form.scheduleTime = ''
  form.labId = null
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  dialogType.value = 'edit'
  form.id = row.id
  form.name = row.name
  form.className = row.className
  form.studentCount = row.studentCount
  form.teacherName = row.teacherName
  form.term = row.term
  form.scheduleTime = row.scheduleTime
  form.labId = row.labId
  dialogVisible.value = true
}

const handleDelete = (_id: number) => {
  ElMessageBox.confirm('确定要删除该课程吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      // TODO: 调用删除 API
      ElMessage.success('删除成功')
      loadData()
    } catch (error) {
      console.error('删除失败:', error)
    }
  })
}

const handleSubmit = async () => {
  if (!form.name || !form.className) {
    ElMessage.warning('请填写完整信息')
    return
  }

  submitting.value = true
  try {
    // TODO: 调用新增/编辑 API
    await new Promise(resolve => setTimeout(resolve, 300))
    ElMessage.success(dialogType.value === 'add' ? '新增成功' : '更新成功')
    dialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('提交失败:', error)
  } finally {
    submitting.value = false
  }
}

const handleBindReservation = (row: any) => {
  bindForm.courseId = row.id
  bindForm.courseName = row.name
  bindForm.labId = row.labId
  bindForm.weeks = []
  bindForm.timeRange = null
  bindDialogVisible.value = true
}

const handleBindSubmit = async () => {
  if (!bindForm.labId || bindForm.weeks.length === 0 || !bindForm.timeRange) {
    ElMessage.warning('请填写完整信息')
    return
  }

  submitting.value = true
  try {
    // TODO: 批量创建预约
    await new Promise(resolve => setTimeout(resolve, 500))
    ElMessage.success(`已为 ${bindForm.weeks.length} 周创建预约`)
    bindDialogVisible.value = false
  } catch (error) {
    console.error('创建预约失败:', error)
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped lang="scss">
.action-bar {
  padding: 15px 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

// 学生课程表卡片样式
.schedule-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  padding: 10px 0;
}

.schedule-card {
  background: linear-gradient(135deg, #f5f7fa 0%, #fff 100%);
  border-radius: 12px;
  padding: 20px;
  border: 1px solid #ebeef5;
  transition: all 0.3s;

  &:hover {
    transform: translateY(-3px);
    box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
  }
}

.course-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
}

.course-name {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.course-info {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #606266;
  font-size: 14px;

  .el-icon {
    color: #409eff;
  }
}
</style>
