/**
 * 日历视图模块 API
 *
 * 接口路径: /api/calendar
 * 功能: 获取指定时间段内的预约日历视图
 * 权限: 公开访问
 */

import { get } from './request'
import type { CalendarEvent } from './types'

// ==================== 类型定义 ====================

/**
 * 日历查询参数
 */
export interface CalendarParams {
  from: string      // 开始时间 (ISO 8601)，必填
  to: string        // 结束时间 (ISO 8601)，必填
  labId?: number    // 按实验室筛选
  deviceId?: number // 按设备筛选
}

// ==================== API 方法 ====================

/**
 * 获取日历视图
 *
 * @description 获取指定时间范围内的预约事件，用于日历展示
 * @param params - 查询参数，from 和 to 为必填
 * @returns 日历事件列表
 *
 * @example
 * ```ts
 * // 获取本周的所有预约
 * const res = await getCalendarEvents({
 *   from: '2025-01-13T00:00:00Z',
 *   to: '2025-01-19T23:59:59Z'
 * })
 * console.log(res.data)
 * ```
 *
 * @example
 * ```ts
 * // 获取指定实验室的月度预约
 * const res = await getCalendarEvents({
 *   from: '2025-01-01T00:00:00Z',
 *   to: '2025-01-31T23:59:59Z',
 *   labId: 1
 * })
 * ```
 */
export function getCalendarEvents(params: CalendarParams) {
  return get<CalendarEvent[]>('/calendar', params)
}
