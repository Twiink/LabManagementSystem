/**
 * API 模块统一导出
 *
 * 所有 API 模块的统一入口，便于在组件中引用
 *
 * @example
 * ```ts
 * // 方式一：按模块导入
 * import { authApi, labApi, reservationApi } from '@/api'
 * await authApi.login({ username: 'admin', password: '123456' })
 *
 * // 方式二：直接导入具体方法
 * import { login, getLabList, createReservation } from '@/api'
 * await login({ username: 'admin', password: '123456' })
 * ```
 */

// ==================== 导出请求工具 ====================

export {
  get,
  post,
  put,
  patch,
  del,
  type ApiResponse,
  type PageData,
  type PageParams
} from './request'

// ==================== 导出类型定义 ====================

export * from './types'

// ==================== 导出各模块 API ====================

// 认证模块
export * from './auth'
import * as authApi from './auth'
export { authApi }

// 用户模块
export * from './user'
import * as userApi from './user'
export { userApi }

// 实验室模块
export * from './lab'
import * as labApi from './lab'
export { labApi }

// 设备模块
export * from './device'
import * as deviceApi from './device'
export { deviceApi }

// 课程模块
export * from './course'
import * as courseApi from './course'
export { courseApi }

// 预约模块
export * from './reservation'
import * as reservationApi from './reservation'
export { reservationApi }

// 日历模块
export * from './calendar'
import * as calendarApi from './calendar'
export { calendarApi }

// 通知模块
export * from './notification'
import * as notificationApi from './notification'
export { notificationApi }

// 审计日志模块
export * from './auditLog'
import * as auditLogApi from './auditLog'
export { auditLogApi }

// 规则配置模块
export * from './ruleConfig'
import * as ruleConfigApi from './ruleConfig'
export { ruleConfigApi }
