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
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/dashboard/Dashboard.vue'),
        meta: { title: '首页', icon: 'House', roles: ['ADMIN', 'TEACHER', 'STUDENT'] }
      },
      {
        path: 'labs',
        name: 'Labs',
        component: () => import('../views/labs/LabList.vue'),
        meta: { title: '实验室管理', icon: 'OfficeBuilding', roles: ['ADMIN', 'TEACHER', 'STUDENT'] }
      },
      {
        path: 'devices',
        name: 'Devices',
        component: () => import('../views/devices/DeviceList.vue'),
        meta: { title: '设备管理', icon: 'Monitor', roles: ['ADMIN', 'TEACHER', 'STUDENT'] }
      },
      {
        path: 'reservations',
        name: 'Reservations',
        component: () => import('../views/reservations/ReservationList.vue'),
        meta: { title: '预约管理', icon: 'Calendar', roles: ['ADMIN', 'TEACHER', 'STUDENT'] }
      },
      {
        path: 'courses',
        name: 'Courses',
        component: () => import('../views/courses/CourseList.vue'),
        meta: { title: '课程管理', icon: 'Notebook', roles: ['ADMIN', 'TEACHER'] }
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
        meta: { title: '审计日志', icon: 'Document', roles: ['ADMIN'] }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router