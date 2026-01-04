<template>
  <div class="courses-container">
    <div class="glass-card action-bar">
      <el-form inline>
        <el-form-item label="搜索">
          <el-input 
            v-model="searchQuery" 
            placeholder="课程名称" 
            prefix-icon="Search" 
            clearable 
            @clear="handleSearch" 
            class="industrial-input"
          />
        </el-form-item>
        <el-form-item label="学期">
          <el-select 
            v-model="termFilter" 
            placeholder="全部学期" 
            clearable 
            style="width: 160px" 
            @change="handleSearch"
            class="industrial-select"
          >
            <el-option label="2024-2025-1" value="2024-2025-1" />
            <el-option label="2024-2025-2" value="2024-2025-2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch" class="action-btn">查询</el-button>
          <!-- 只有教师和管理员可以新增课程 -->
          <el-button 
            v-if="canManage" 
            class="action-btn add-btn" 
            @click="handleAdd" 
            icon="Plus"
          >新增课程</el-button>
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
              <span class="term-tag">{{ course.term }}</span>
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
          <el-table-column prop="name" label="课程名称" min-width="200" />
          <el-table-column prop="className" label="班级" min-width="150" />
          <el-table-column prop="studentCount" label="人数" width="80" align="center" />
          <el-table-column prop="teacherName" label="授课教师" min-width="120" />
          <el-table-column prop="labName" label="实验室" min-width="150" />
          <el-table-column prop="scheduleTime" label="上课时间" min-width="150" />
          <el-table-column prop="term" label="学期" min-width="120" align="center" />
          <el-table-column label="操作" width="160" fixed="right">
            <template #default="scope">
              <el-button link type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
              <el-button link type="danger" size="small" @click="handleDelete(scope.row.id)">删除</el-button>
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

    <!-- Edit/Add Dialog -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="dialogType === 'add' ? '新增课程' : '编辑课程'" 
      width="550px"
      class="industrial-dialog"
      :show-close="false"
    >
      <el-form :model="form" label-width="100px">
        <el-form-item label="课程名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="选择学生">
          <el-select v-model="form.studentIds" multiple filterable style="width: 100%">
            <el-option v-for="s in students" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="班级"><el-input v-model="form.className" /></el-form-item>
        <el-form-item label="学期">
          <el-select v-model="form.term" style="width: 100%">
            <el-option label="2024-2025-1" value="2024-2025-1" />
            <el-option label="2024-2025-2" value="2024-2025-2" />
          </el-select>
        </el-form-item>
        <el-form-item label="实验室">
          <el-select v-model="form.labId" style="width: 100%">
            <el-option v-for="lab in labs" :key="lab.id" :label="lab.name" :value="lab.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/store/user'
import { getLabList } from '@/api/lab'
import { getStudentList } from '@/api/user'
import { createCourse, updateCourse, getCourseList, deleteCourse as deleteCourseApi } from '@/api/course'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute(); const userStore = useUserStore()
const isStudentView = computed(() => route.path === '/schedule' || userStore.isStudent)
const canManage = computed(() => userStore.isTeacher || userStore.isAdmin)
const searchQuery = ref(''); const termFilter = ref(''); const tableData = ref<any[]>([]); const loading = ref(false); const submitting = ref(false); const total = ref(0); const currentPage = ref(1); const pageSize = ref(20)
const labs = ref<any[]>([]); const students = ref<any[]>([])
const dialogVisible = ref(false); const dialogType = ref<'add' | 'edit'>('add')
const form = reactive({ id: 0, name: '', className: '', studentIds: [] as number[], teacherName: '', term: '2024-2025-2', scheduleTime: '', labId: null as number | null })

const loadLabs = async () => { try { const res = await getLabList({ page: 1, pageSize: 100 }); labs.value = res.data.items || [] } catch (e) { console.error(e) } }
const loadStudents = async () => { try { const res = await getStudentList({ status: 'ACTIVE', page: 1, pageSize: 500 }); students.value = res.data.items || [] } catch (e) { console.error(e) } }
const loadData = async () => {
  loading.value = true
  try {
    const params: any = { page: currentPage.value, pageSize: pageSize.value }
    if (termFilter.value) params.term = termFilter.value
    if (userStore.isStudent) params.studentId = userStore.userInfo?.id
    else if (userStore.isTeacher) params.createdBy = userStore.userInfo?.id
    const res = await getCourseList(params); tableData.value = res.data.items || []; total.value = res.data.total || 0
    if (searchQuery.value) { tableData.value = tableData.value.filter(c => c.name.includes(searchQuery.value)); total.value = tableData.value.length }
  } catch (e) { console.error(e) } finally { loading.value = false }
}
onMounted(async () => { await loadLabs(); if (canManage.value) await loadStudents(); loadData() })
const handleSearch = () => { currentPage.value = 1; loadData() }
const handlePageChange = (page: number) => { currentPage.value = page; loadData() }
const handleAdd = () => { dialogType.value = 'add'; form.id = 0; form.name = ''; form.className = ''; form.studentIds = []; dialogVisible.value = true }
const handleEdit = (row: any) => { dialogType.value = 'edit'; Object.assign(form, row); dialogVisible.value = true }
const handleDelete = (id: number) => { ElMessageBox.confirm('确定删除该课程？', '警告', { type: 'warning' }).then(async () => { await deleteCourseApi(id); ElMessage.success('已删除'); loadData() }) }
const handleSubmit = async () => { /* Submit logic */ }
</script>

<style scoped lang="scss">
.courses-container { animation: fade-in 0.5s ease-out; }
@keyframes fade-in { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }
.action-bar { display: flex; align-items: center; padding: 24px; margin-bottom: 24px; }
.action-btn { font-weight: 800; letter-spacing: 1px; }
.add-btn { border-style: dashed !important; border-width: 2px !important; background: #fff !important; color: var(--primary-color) !important; border-color: var(--primary-color) !important; &:hover { background: rgba(255, 192, 133, 0.1) !important; } }
.pagination-container { margin-top: 24px; display: flex; justify-content: flex-end; }

/* Student View Cards */
.schedule-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: 24px; padding: 8px 0; }
.schedule-card { background: #fff; padding: 24px; border: 1px solid var(--accent-border); box-shadow: var(--glass-shadow); transition: all 0.3s;
  &:hover { transform: translateY(-4px); box-shadow: var(--glass-shadow-hover); }
}
.course-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; padding-bottom: 16px; border-bottom: 1px solid rgba(140, 107, 93, 0.1);
  .course-name { font-size: 18px; font-weight: 800; color: var(--text-main); }
  .term-tag { font-size: 11px; font-weight: 700; color: var(--text-light); border: 1px solid var(--accent-border); padding: 2px 6px; }
}
.course-info { display: flex; flex-direction: column; gap: 12px;
  .info-item { display: flex; align-items: center; gap: 10px; color: var(--text-regular); font-size: 14px;
    .el-icon { color: var(--primary-color); font-size: 16px; }
  }
}
.industrial-dialog :deep(.el-dialog__header) { background: var(--accent-border); .el-dialog__title { color: #fff; font-weight: 800; } }
</style>