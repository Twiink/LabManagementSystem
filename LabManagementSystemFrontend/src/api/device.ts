/**
 * 设备管理模块 API
 *
 * 接口路径: /api/devices
 * 功能: 设备的增删改查、状态管理
 * 权限: 查询公开，创建/更新/状态变更需要 ADMIN 权限
 */

import { get, post, put, patch } from './request'
import type { PageParams, PageData } from './request'
import type { Device, DeviceStatus, CreateDeviceParams, UpdateDeviceParams } from './types'

// ==================== 类型定义 ====================

/**
 * 设备列表查询参数
 */
export interface DeviceListParams extends PageParams {
  labId?: number        // 按所属实验室筛选
  status?: DeviceStatus // 按状态筛选
  keyword?: string      // 关键词搜索（名称/型号）
}

/**
 * 更新设备状态参数
 */
export interface UpdateDeviceStatusParams {
  status: DeviceStatus  // 新状态
}

// ==================== API 方法 ====================

/**
 * 获取设备列表
 *
 * @description 分页获取设备列表，支持按实验室、状态筛选和关键词搜索
 * @param params - 查询参数
 * @returns 分页设备列表
 *
 * @example
 * ```ts
 * // 获取实验室 ID 为 1 的所有设备
 * const res = await getDeviceList({ labId: 1, page: 1 })
 * console.log(res.data.items)
 * ```
 */
export function getDeviceList(params?: DeviceListParams) {
  return get<PageData<Device>>('/devices', params)
}

/**
 * 创建设备
 *
 * @description 创建新的设备（仅管理员可用）
 * @param params - 创建参数
 * @returns 创建的设备信息
 *
 * @example
 * ```ts
 * const res = await createDevice({
 *   labId: 1,
 *   name: '光谱仪',
 *   model: 'SP-2025',
 *   description: '用于光谱分析'
 * })
 * ```
 */
export function createDevice(params: CreateDeviceParams) {
  return post<Device>('/devices', params)
}

/**
 * 更新设备信息
 *
 * @description 更新指定设备的信息（仅管理员可用）
 * @param id - 设备 ID
 * @param params - 更新参数
 * @returns 更新后的设备信息
 *
 * @example
 * ```ts
 * const res = await updateDevice(1, { name: '高级光谱仪' })
 * ```
 */
export function updateDevice(id: number, params: UpdateDeviceParams) {
  return put<Device>(`/devices/${id}`, params)
}

/**
 * 更新设备状态
 *
 * @description 更新指定设备的状态（仅管理员可用）
 * @param id - 设备 ID
 * @param params - 状态更新参数
 * @returns 更新后的设备信息
 *
 * @example
 * ```ts
 * // 将设备设为维护中
 * const res = await updateDeviceStatus(1, { status: 'MAINTENANCE' })
 * ```
 */
export function updateDeviceStatus(id: number, params: UpdateDeviceStatusParams) {
  return patch<Device>(`/devices/${id}/status`, params)
}
