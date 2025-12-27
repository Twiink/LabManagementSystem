/**
 * 审计日志模块 API
 *
 * 接口路径: /api/audit-logs
 * 功能: 查询系统操作审计日志
 * 权限: 仅 ADMIN 可访问（只读）
 */

import { get } from './request'
import type { PageParams, PageData } from './request'
import type { AuditLog, AuditTargetType } from './types'

// ==================== 类型定义 ====================

/**
 * 审计日志查询参数
 */
export interface AuditLogListParams extends PageParams {
  actorId?: number            // 按操作人筛选
  targetType?: AuditTargetType // 按目标类型筛选
  targetId?: number           // 按目标 ID 筛选
  from?: string               // 开始时间范围 (ISO 8601)
  to?: string                 // 结束时间范围 (ISO 8601)
}

// ==================== API 方法 ====================

/**
 * 获取审计日志列表
 *
 * @description 分页获取系统审计日志，支持多条件筛选（仅管理员可用）
 * @param params - 查询参数
 * @returns 分页审计日志列表
 *
 * @example
 * ```ts
 * // 获取所有实验室相关的操作日志
 * const res = await getAuditLogList({
 *   targetType: 'LAB',
 *   page: 1,
 *   pageSize: 50
 * })
 * console.log(res.data.items)
 * ```
 *
 * @example
 * ```ts
 * // 获取指定用户在某时间段内的操作日志
 * const res = await getAuditLogList({
 *   actorId: 1,
 *   from: '2025-01-01T00:00:00Z',
 *   to: '2025-01-31T23:59:59Z'
 * })
 * ```
 */
export function getAuditLogList(params?: AuditLogListParams) {
  return get<PageData<AuditLog>>('/audit-logs', params)
}
