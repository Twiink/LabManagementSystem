/**
 * 通知模块 API
 *
 * 接口路径: /api/notifications
 * 功能: 获取用户通知列表、标记已读
 * 权限: 需要认证，只能操作自己的通知
 */

import { get, post } from './request'
import type { PageParams, PageData } from './request'
import type { Notification, NotificationStatus } from './types'

// ==================== 类型定义 ====================

/**
 * 通知列表查询参数
 */
export interface NotificationListParams extends PageParams {
  status?: NotificationStatus  // 按状态筛选（UNREAD/READ）
}

// ==================== API 方法 ====================

/**
 * 获取当前用户的通知列表
 *
 * @description 分页获取当前登录用户的通知列表，支持按状态筛选
 * @param params - 查询参数
 * @returns 分页通知列表
 *
 * @example
 * ```ts
 * // 获取所有未读通知
 * const res = await getNotificationList({ status: 'UNREAD', page: 1 })
 * console.log(res.data.items)
 * ```
 */
export function getNotificationList(params?: NotificationListParams) {
  return get<PageData<Notification>>('/notifications', params)
}

/**
 * 标记通知为已读
 *
 * @description 将指定通知标记为已读状态
 * @param id - 通知 ID
 * @returns 更新后的通知信息
 *
 * @example
 * ```ts
 * const res = await markNotificationRead(1)
 * console.log(res.data.status) // 'READ'
 * ```
 */
export function markNotificationRead(id: number) {
  return post<Notification>(`/notifications/${id}/read`)
}

/**
 * 批量标记通知为已读
 *
 * @description 将多个通知标记为已读状态
 * @param ids - 通知 ID 数组
 * @returns 操作结果
 *
 * @example
 * ```ts
 * await markNotificationsRead([1, 2, 3])
 * ```
 */
export async function markNotificationsRead(ids: number[]) {
  // 并行处理多个标记请求
  const promises = ids.map(id => markNotificationRead(id))
  return Promise.all(promises)
}
