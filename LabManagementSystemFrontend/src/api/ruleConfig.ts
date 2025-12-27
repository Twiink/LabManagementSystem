/**
 * 规则配置模块 API
 *
 * 接口路径: /api/rule-configs
 * 功能: 获取和更新系统规则配置
 * 权限: 仅 ADMIN 可访问
 */

import { get, put } from './request'
import type { RuleConfig, UpdateRuleConfigParams } from './types'

// ==================== API 方法 ====================

/**
 * 获取所有规则配置
 *
 * @description 获取系统中所有的规则配置项（仅管理员可用）
 * @returns 规则配置列表
 *
 * @example
 * ```ts
 * const res = await getRuleConfigList()
 * console.log(res.data)
 * // 输出示例:
 * // [
 * //   {
 * //     key: 'reservation_rules',
 * //     value: { teacherCancelHours: 12, studentCancelHours: 24, maxDailyReservations: 2 },
 * //     description: '预约与取消规则'
 * //   }
 * // ]
 * ```
 */
export function getRuleConfigList() {
  return get<RuleConfig[]>('/rule-configs')
}

/**
 * 更新规则配置
 *
 * @description 更新指定的规则配置项（仅管理员可用）
 * @param key - 配置键名
 * @param params - 更新参数
 * @returns 更新后的规则配置
 *
 * @example
 * ```ts
 * const res = await updateRuleConfig('reservation_rules', {
 *   value: {
 *     teacherCancelHours: 6,
 *     studentCancelHours: 12,
 *     maxDailyReservations: 3
 *   },
 *   description: '更新后的预约规则'
 * })
 * ```
 */
export function updateRuleConfig(key: string, params: UpdateRuleConfigParams) {
  return put<RuleConfig>(`/rule-configs/${key}`, params)
}
