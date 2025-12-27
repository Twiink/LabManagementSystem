/**
 * 用户状态管理
 *
 * 管理用户登录状态、Token、用户信息等
 */

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, type LoginParams } from '@/api/auth'
import type { User, UserRole } from '@/api/types'

/**
 * 用户 Store
 */
export const useUserStore = defineStore('user', () => {
  // ==================== 状态 ====================

  /** 访问令牌 */
  const token = ref<string | null>(localStorage.getItem('token'))

  /** 用户信息 */
  const userInfo = ref<User | null>(
    JSON.parse(localStorage.getItem('userInfo') || 'null')
  )

  // ==================== 计算属性 ====================

  /** 是否已登录 */
  const isLoggedIn = computed(() => !!token.value)

  /** 用户角色 */
  const role = computed<UserRole | null>(() => userInfo.value?.role ?? null)

  /** 是否是管理员 */
  const isAdmin = computed(() => role.value === 'ADMIN')

  /** 是否是教师 */
  const isTeacher = computed(() => role.value === 'TEACHER')

  /** 是否是学生 */
  const isStudent = computed(() => role.value === 'STUDENT')

  // ==================== 方法 ====================

  /**
   * 设置 Token
   * @param newToken - 新的访问令牌
   */
  function setToken(newToken: string) {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  /**
   * 设置用户信息
   * @param info - 用户信息
   */
  function setUserInfo(info: User) {
    userInfo.value = info
    localStorage.setItem('userInfo', JSON.stringify(info))
  }

  /**
   * 用户登录
   * @param params - 登录参数
   * @returns 登录结果
   */
  async function login(params: LoginParams) {
    const res = await loginApi(params)
    if (res.code === 0) {
      setToken(res.data.accessToken)
      setUserInfo(res.data.user)
    }
    return res
  }

  /**
   * 用户登出
   * 清除本地存储的 Token 和用户信息
   */
  function logout() {
    token.value = null
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  return {
    // 状态
    token,
    userInfo,
    // 计算属性
    isLoggedIn,
    role,
    isAdmin,
    isTeacher,
    isStudent,
    // 方法
    setToken,
    setUserInfo,
    login,
    logout
  }
})
