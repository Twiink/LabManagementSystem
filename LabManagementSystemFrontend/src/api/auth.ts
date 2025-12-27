/**
 * 认证模块 API
 *
 * 接口路径: /api/auth
 * 功能: 用户登录认证、学生注册
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
  role: string      // 角色：ADMIN, TEACHER, STUDENT
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

/**
 * 注册请求参数
 */
export interface RegisterParams {
  username: string  // 用户名
  password: string  // 密码
  email: string     // 邮箱
  phone?: string    // 手机号（可选）
}

// ==================== API 方法 ====================

/**
 * 用户登录
 *
 * @description 使用用户名、密码和角色进行登录，成功后返回访问令牌和用户信息
 * @param params - 登录参数
 * @returns 登录结果，包含 token 和用户信息
 */
export function login(params: LoginParams) {
  return post<LoginResult>('/auth/login', params)
}

/**
 * 学生注册
 *
 * @description 注册新的学生账号（仅支持学生身份注册）
 * @param params - 注册参数
 * @returns 注册成功的用户信息
 */
export function register(params: RegisterParams) {
  return post<User>('/auth/register', params)
}
