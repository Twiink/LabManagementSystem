import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import Layout from '../layout/Layout.vue'

const routes: Array<RouteRecordRaw> = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/login/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/login/Register.vue')
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      // ===== 通用功能 =====
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/dashboard/Dashboard.vue'),
        meta: { title: '首页', icon: 'House', roles: ['ADMIN', 'TEACHER', 'STUDENT'] }
      },
      {
        path: 'calendar',
        name: 'Calendar',
        component: () => import('../views/calendar/LabCalendar.vue'),
        meta: { title: '预约日历', icon: 'Calendar', roles: ['ADMIN', 'TEACHER', 'STUDENT'] }
      },

      // ===== 资源浏览（学生/教师视角） =====
      {
        path: 'labs',
        name: 'Labs',
        component: () => import('../views/labs/LabList.vue'),
        meta: { title: '实验室浏览', icon: 'OfficeBuilding', roles: ['STUDENT', 'TEACHER'] }
      },
      {
        path: 'devices',
        name: 'Devices',
        component: () => import('../views/devices/DeviceList.vue'),
        meta: { title: '设备浏览', icon: 'Monitor', roles: ['STUDENT', 'TEACHER'] }
      },
      {
        path: 'my-reservations',
        name: 'MyReservations',
        component: () => import('../views/reservations/ReservationList.vue'),
        meta: { title: '我的预约', icon: 'List', roles: ['STUDENT', 'TEACHER'] }
      },
      {
        path: 'schedule',
        name: 'Schedule',
        component: () => import('../views/courses/CourseList.vue'),
        meta: { title: '课程表', icon: 'Notebook', roles: ['STUDENT'] }
      },

      // ===== 教师功能 =====
      {
        path: 'course-manage',
        name: 'CourseManage',
        component: () => import('../views/courses/CourseList.vue'),
        meta: { title: '课程管理', icon: 'Notebook', roles: ['TEACHER'] }
      },
      {
        path: 'approval',
        name: 'Approval',
        component: () => import('../views/reservations/ReservationList.vue'),
        meta: { title: '预约审批', icon: 'Checked', roles: ['TEACHER'] }
      },

      // ===== 管理员功能 =====
      {
        path: 'lab-manage',
        name: 'LabManage',
        component: () => import('../views/labs/LabList.vue'),
        meta: { title: '实验室管理', icon: 'OfficeBuilding', roles: ['ADMIN'] }
      },
      {
        path: 'device-manage',
        name: 'DeviceManage',
        component: () => import('../views/devices/DeviceList.vue'),
        meta: { title: '设备管理', icon: 'Monitor', roles: ['ADMIN'] }
      },
      {
        path: 'reservations',
        name: 'Reservations',
        component: () => import('../views/reservations/ReservationList.vue'),
        meta: { title: '预约管理', icon: 'Calendar', roles: ['ADMIN'] }
      },
      {
        path: 'courses',
        name: 'Courses',
        component: () => import('../views/courses/CourseList.vue'),
        meta: { title: '课程管理', icon: 'Notebook', roles: ['ADMIN'] }
      },
      {
        path: 'users',
        name: 'Users',
        component: () => import('../views/admin/UserList.vue'),
        meta: { title: '用户管理', icon: 'User', roles: ['ADMIN'] }
      },
      {
        path: 'audit',
        name: 'Audit',
        component: () => import('../views/admin/AuditLogs.vue'),
        meta: { title: '系统日志', icon: 'Document', roles: ['ADMIN'] }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
