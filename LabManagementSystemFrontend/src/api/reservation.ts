/**
 * 预约管理模块 API
 *
 * 接口路径: /api/reservations
 * 功能: 预约的创建、查询、修改、取消、审批、签到/签出等
 * 权限: 根据操作类型和用户角色有不同限制
 *
 * 业务规则:
 * - 设备级预约（deviceId 不为空）自动进入 PENDING 状态，需要审批
 * - 实验室级普通预约（deviceId 为空且 type=SINGLE）自动进入 APPROVED 状态
 * - 课程预约自动进入 PENDING 状态
 * - 教师修改预约: 开始前 >= 12 小时
 * - 学生修改预约: 开始前 >= 24 小时
 * - 超时修改仅管理员可操作，需填写原因
 */

import { get, post, put, del } from './request'
import type { PageParams, PageData } from './request'
import type {
  Reservation,
  ReservationStatus,
  ReservationType,
  CreateReservationParams,
  CreateSeriesReservationParams,
  CreateCourseReservationParams,
  UpdateReservationParams,
  RejectReservationParams,
  OverrideReservationParams
} from './types'

// ==================== 类型定义 ====================

/**
 * 预约列表查询参数
 */
export interface ReservationListParams extends PageParams {
  labId?: number                // 按实验室筛选
  deviceId?: number             // 按设备筛选
  requesterId?: number          // 按预约人筛选
  status?: ReservationStatus    // 按状态筛选
  type?: ReservationType        // 按类型筛选
  from?: string                 // 开始时间范围 (ISO 8601)
  to?: string                   // 结束时间范围 (ISO 8601)
}

// ==================== API 方法 ====================

/**
 * 获取预约列表
 *
 * @description 分页获取预约列表，支持多条件筛选
 * @param params - 查询参数
 * @returns 分页预约列表
 *
 * @example
 * ```ts
 * // 获取实验室 ID 为 1 的所有待审批预约
 * const res = await getReservationList({
 *   labId: 1,
 *   status: 'PENDING',
 *   page: 1
 * })
 * ```
 */
export function getReservationList(params?: ReservationListParams) {
  return get<PageData<Reservation>>('/reservations', params)
}

/**
 * 创建单次预约
 *
 * @description 创建单次预约
 * - 设备级预约自动进入 PENDING 状态
 * - 实验室级预约自动进入 APPROVED 状态
 * @param params - 创建参数
 * @returns 创建的预约信息
 *
 * @example
 * ```ts
 * const res = await createReservation({
 *   labId: 1,
 *   title: '个人实验',
 *   startTime: '2025-01-15T08:30:00Z',
 *   endTime: '2025-01-15T10:30:00Z'
 * })
 * ```
 */
export function createReservation(params: CreateReservationParams) {
  return post<Reservation>('/reservations', params)
}

/**
 * 创建系列预约
 *
 * @description 创建多个时间段的系列预约（教师或管理员可用）
 * @param params - 创建参数，包含多个时间段
 * @returns 创建的预约列表
 *
 * @example
 * ```ts
 * const res = await createSeriesReservation({
 *   labId: 1,
 *   title: '周期实验',
 *   slots: [
 *     { startTime: '2025-01-15T08:30:00Z', endTime: '2025-01-15T10:30:00Z' },
 *     { startTime: '2025-01-22T08:30:00Z', endTime: '2025-01-22T10:30:00Z' }
 *   ]
 * })
 * ```
 */
export function createSeriesReservation(params: CreateSeriesReservationParams) {
  return post<Reservation[]>('/reservations/series', params)
}

/**
 * 创建课程预约
 *
 * @description 创建课程关联的预约（教师或管理员可用）
 * 课程预约自动进入 PENDING 状态
 * @param params - 创建参数，包含课程 ID 和时间段
 * @returns 创建的预约列表
 *
 * @example
 * ```ts
 * const res = await createCourseReservation({
 *   labId: 1,
 *   courseId: 10,
 *   title: '分析化学实验',
 *   slots: [
 *     { startTime: '2025-01-15T08:30:00Z', endTime: '2025-01-15T10:30:00Z' }
 *   ]
 * })
 * ```
 */
export function createCourseReservation(params: CreateCourseReservationParams) {
  return post<Reservation[]>('/reservations/course', params)
}

/**
 * 获取预约详情
 *
 * @description 获取指定预约的详细信息
 * @param id - 预约 ID
 * @returns 预约详细信息
 *
 * @example
 * ```ts
 * const res = await getReservationDetail(101)
 * console.log(res.data.title, res.data.status)
 * ```
 */
export function getReservationDetail(id: number) {
  return get<Reservation>(`/reservations/${id}`)
}

/**
 * 修改预约
 *
 * @description 修改预约信息（有时间限制）
 * - 教师: 开始前 >= 12 小时可修改
 * - 学生: 开始前 >= 24 小时可修改
 * - 超时仅管理员可修改
 * @param id - 预约 ID
 * @param params - 更新参数
 * @returns 更新后的预约信息
 *
 * @example
 * ```ts
 * const res = await updateReservation(101, {
 *   title: '更新后的实验名称'
 * })
 * ```
 */
export function updateReservation(id: number, params: UpdateReservationParams) {
  return put<Reservation>(`/reservations/${id}`, params)
}

/**
 * 取消预约
 *
 * @description 取消指定预约（有时间限制，规则同修改）
 * @param id - 预约 ID
 * @returns 操作结果
 *
 * @example
 * ```ts
 * await cancelReservation(101)
 * ```
 */
export function cancelReservation(id: number) {
  return del(`/reservations/${id}`)
}

/**
 * 预约签到
 *
 * @description 开始使用，将预约状态变更为 IN_USE
 * @param id - 预约 ID
 * @returns 更新后的预约信息
 *
 * @example
 * ```ts
 * const res = await checkinReservation(101)
 * console.log(res.data.checkinTime)
 * ```
 */
export function checkinReservation(id: number) {
  return post<Reservation>(`/reservations/${id}/checkin`)
}

/**
 * 预约签出
 *
 * @description 结束使用，将预约状态变更为 COMPLETED
 * @param id - 预约 ID
 * @returns 更新后的预约信息
 *
 * @example
 * ```ts
 * const res = await checkoutReservation(101)
 * console.log(res.data.checkoutTime)
 * ```
 */
export function checkoutReservation(id: number) {
  return post<Reservation>(`/reservations/${id}/checkout`)
}

/**
 * 审批通过预约
 *
 * @description 管理员审批通过预约（仅管理员可用）
 * @param id - 预约 ID
 * @returns 更新后的预约信息
 *
 * @example
 * ```ts
 * const res = await approveReservation(101)
 * console.log(res.data.status) // 'APPROVED'
 * ```
 */
export function approveReservation(id: number) {
  return post<Reservation>(`/reservations/${id}/approve`)
}

/**
 * 审批拒绝预约
 *
 * @description 管理员拒绝预约，需提供拒绝原因（仅管理员可用）
 * @param id - 预约 ID
 * @param params - 拒绝参数，包含原因
 * @returns 更新后的预约信息
 *
 * @example
 * ```ts
 * const res = await rejectReservation(101, {
 *   reason: '时间冲突，请选择其他时段'
 * })
 * console.log(res.data.status) // 'REJECTED'
 * ```
 */
export function rejectReservation(id: number, params: RejectReservationParams) {
  return post<Reservation>(`/reservations/${id}/reject`, params)
}

/**
 * 强制覆盖/调整预约
 *
 * @description 管理员强制覆盖或调整预约，需提供原因（仅管理员可用）
 * 可用于课程预约覆盖普通预约等场景
 * @param id - 预约 ID
 * @param params - 覆盖参数，包含原因
 * @returns 更新后的预约信息
 *
 * @example
 * ```ts
 * const res = await overrideReservation(101, {
 *   reason: '课程预约优先，已通知原预约用户'
 * })
 * ```
 */
export function overrideReservation(id: number, params: OverrideReservationParams) {
  return post<Reservation>(`/reservations/${id}/override`, params)
}
