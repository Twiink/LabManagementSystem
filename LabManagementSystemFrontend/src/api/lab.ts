/**
 * 实验室管理模块 API
 *
 * 接口路径: /api/labs
 * 功能: 实验室的增删改查、状态管理
 * 权限: 查询公开，创建/更新/状态变更需要 ADMIN 权限
 */

import { get, post, put, patch } from './request'
import type { PageParams, PageData } from './request'
import type { Lab, LabStatus, CreateLabParams, UpdateLabParams } from './types'

// ==================== 类型定义 ====================

/**
 * 实验室列表查询参数
 */
export interface LabListParams extends PageParams {
  status?: LabStatus  // 按状态筛选
  keyword?: string    // 关键词搜索（名称/位置）
}

/**
 * 更新实验室状态参数
 */
export interface UpdateLabStatusParams {
  status: LabStatus   // 新状态
}

// ==================== API 方法 ====================

/**
 * 获取实验室列表
 *
 * @description 分页获取实验室列表，支持状态筛选和关键词搜索
 * @param params - 查询参数
 * @returns 分页实验室列表
 *
 * @example
 * ```ts
 * // 获取所有空闲的实验室
 * const res = await getLabList({ status: 'IDLE', page: 1 })
 * console.log(res.data.items)
 * ```
 */
export function getLabList(params?: LabListParams) {
  return get<PageData<Lab>>('/labs', params)
}

/**
 * 创建实验室
 *
 * @description 创建新的实验室（仅管理员可用）
 * @param params - 创建参数
 * @returns 创建的实验室信息
 *
 * @example
 * ```ts
 * const res = await createLab({
 *   name: '化学实验室 A',
 *   location: '1号楼 203',
 *   capacity: 40,
 *   description: '基础化学实验用'
 * })
 * ```
 */
export function createLab(params: CreateLabParams) {
  return post<Lab>('/labs', params)
}

/**
 * 更新实验室信息
 *
 * @description 更新指定实验室的信息（仅管理员可用）
 * @param id - 实验室 ID
 * @param params - 更新参数
 * @returns 更新后的实验室信息
 *
 * @example
 * ```ts
 * const res = await updateLab(1, { capacity: 50 })
 * ```
 */
export function updateLab(id: number, params: UpdateLabParams) {
  return put<Lab>(`/labs/${id}`, params)
}

/**
 * 更新实验室状态
 *
 * @description 更新指定实验室的状态（仅管理员可用）
 * @param id - 实验室 ID
 * @param params - 状态更新参数
 * @returns 更新后的实验室信息
 *
 * @example
 * ```ts
 * // 将实验室设为维护中
 * const res = await updateLabStatus(1, { status: 'MAINTENANCE' })
 * ```
 */
export function updateLabStatus(id: number, params: UpdateLabStatusParams) {
  return patch<Lab>(`/labs/${id}/status`, params)
}
