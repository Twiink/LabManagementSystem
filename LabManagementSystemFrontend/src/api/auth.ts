/**
 * 认证模块 API
 *
 * 接口路径: /api/auth
 * 功能: 用户登录认证
 */

import { post } from './request'
import type { User } from './types'

// ==================== 类型定义 ====================

/**
 * 登录请求参数
 */
export interface LoginParams {
  username: string  // 用户名
  password: string  // 密码
}

/**
 * 登录响应数据
 */
export interface LoginResult {
  accessToken: string   // JWT 访问令牌
  tokenType: string     // 令牌类型，固定为 "Bearer"
  expiresIn: number     // 过期时间（秒）
  user: User            // 用户信息
}

// ==================== API 方法 ====================

/**
 * 用户登录
 *
 * @description 使用用户名和密码进行登录，成功后返回访问令牌和用户信息
 * @param params - 登录参数
 * @returns 登录结果，包含 token 和用户信息
 *
 * @example
 * ```ts
 * const res = await login({ username: 'admin', password: '123456' })
 * console.log(res.data.accessToken)
 * ```
 */
export function login(params: LoginParams) {
  return post<LoginResult>('/auth/login', params)
}
