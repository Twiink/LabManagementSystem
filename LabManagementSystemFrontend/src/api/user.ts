/**
 * 用户管理模块 API
 *
 * 接口路径: /api/users
 * 功能: 用户信息管理、用户列表查询
 * 权限: 部分接口需要 ADMIN 权限
 */

import { get } from './request'
import type { PageParams, PageData } from './request'
import type { User, UserRole, UserStatus } from './types'

// ==================== 类型定义 ====================

/**
 * 用户列表查询参数
 */
export interface UserListParams extends PageParams {
  role?: UserRole      // 按角色筛选
  status?: UserStatus  // 按状态筛选
}

// ==================== API 方法 ====================

/**
 * 获取当前登录用户信息
 *
 * @description 获取当前已登录用户的详细信息
 * @returns 当前用户信息
 *
 * @example
 * ```ts
 * const res = await getCurrentUser()
 * console.log(res.data.name, res.data.role)
 * ```
 */
export function getCurrentUser() {
  return get<User>('/users/me')
}

/**
 * 获取用户列表
 *
 * @description 分页获取用户列表，支持按角色和状态筛选（仅管理员可用）
 * @param params - 查询参数
 * @returns 分页用户列表
 *
 * @example
 * ```ts
 * // 获取所有教师
 * const res = await getUserList({ role: 'TEACHER', page: 1, pageSize: 20 })
 * console.log(res.data.items)
 * ```
 */
export function getUserList(params?: UserListParams) {
  return get<PageData<User>>('/users', params)
}
