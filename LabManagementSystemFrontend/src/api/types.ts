/**
 * 全局类型定义
 *
 * 包含所有业务实体的 TypeScript 类型定义
 * 对应后端 domain 层的实体类
 */

// ==================== 枚举类型定义 ====================

/**
 * 用户角色枚举
 */
export type UserRole = 'ADMIN' | 'TEACHER' | 'STUDENT'

/**
 * 用户状态枚举
 */
export type UserStatus = 'ACTIVE' | 'DISABLED'

/**
 * 实验室状态枚举
 */
export type LabStatus = 'IDLE' | 'RESERVED' | 'IN_USE' | 'MAINTENANCE' | 'DISABLED'

/**
 * 设备状态枚举
 */
export type DeviceStatus = 'IDLE' | 'RESERVED' | 'IN_USE' | 'MAINTENANCE' | 'RETIRED'

/**
 * 预约状态枚举
 */
export type ReservationStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'CANCELLED' | 'EXPIRED' | 'IN_USE' | 'COMPLETED'

/**
 * 预约类型枚举
 */
export type ReservationType = 'SINGLE' | 'RECURRING' | 'COURSE'

/**
 * 通知状态枚举
 */
export type NotificationStatus = 'UNREAD' | 'READ'

/**
 * 审计日志目标类型枚举
 */
export type AuditTargetType = 'LAB' | 'DEVICE' | 'RESERVATION' | 'USER' | 'COURSE' | 'RULE_CONFIG'

// ==================== 用户相关类型 ====================

/**
 * 用户信息
 */
export interface User {
  id: number
  username: string
  name: string
  email?: string
  phone?: string
  role: UserRole
  status: UserStatus
  createdAt?: string
  updatedAt?: string
}

/**
 * 用户简要信息（用于列表显示等场景）
 */
export interface UserBrief {
  id: number
  name: string
  role: UserRole
}

// ==================== 实验室相关类型 ====================

/**
 * 实验室信息
 */
export interface Lab {
  id: number
  name: string           // 实验室名称
  location: string       // 位置
  capacity: number       // 容纳人数
  description?: string   // 描述
  status: LabStatus      // 状态
  createdAt?: string
  updatedAt?: string
}

/**
 * 创建实验室请求参数
 */
export interface CreateLabParams {
  name: string
  location: string
  capacity: number
  description?: string
}

/**
 * 更新实验室请求参数
 */
export interface UpdateLabParams {
  name?: string
  location?: string
  capacity?: number
  description?: string
}

// ==================== 设备相关类型 ====================

/**
 * 设备信息
 */
export interface Device {
  id: number
  labId: number          // 所属实验室 ID
  name: string           // 设备名称
  model?: string         // 型号
  description?: string   // 描述
  status: DeviceStatus   // 状态
  lab?: Lab              // 关联的实验室信息
  createdAt?: string
  updatedAt?: string
}

/**
 * 创建设备请求参数
 */
export interface CreateDeviceParams {
  labId: number
  name: string
  model?: string
  description?: string
}

/**
 * 更新设备请求参数
 */
export interface UpdateDeviceParams {
  name?: string
  model?: string
  description?: string
  labId?: number
}

// ==================== 课程相关类型 ====================

/**
 * 课程信息
 */
export interface Course {
  id: number
  name: string           // 课程名称
  className: string      // 班级名称
  studentCount: number   // 学生人数
  term: string           // 学期，如 "2024-2025-2"
  createdBy: number      // 创建人 ID
  creator?: UserBrief    // 创建人信息
  createdAt?: string
  updatedAt?: string
}

/**
 * 创建课程请求参数
 */
export interface CreateCourseParams {
  name: string
  className: string
  studentCount: number
  term: string
}

/**
 * 更新课程请求参数
 */
export interface UpdateCourseParams {
  name: string
  className: string
  studentCount: number
  term: string
}

// ==================== 预约相关类型 ====================

/**
 * 预约信息
 */
export interface Reservation {
  id: number
  labId: number                   // 实验室 ID
  deviceId?: number               // 设备 ID（可选，设备级预约时必填）
  requesterId: number             // 预约人 ID
  courseId?: number               // 课程 ID（课程预约时必填）
  title: string                   // 预约标题
  startTime: string               // 开始时间 (ISO 8601)
  endTime: string                 // 结束时间 (ISO 8601)
  status: ReservationStatus       // 预约状态
  type: ReservationType           // 预约类型
  remark?: string                 // 备注
  rejectReason?: string           // 拒绝原因
  checkinTime?: string            // 签到时间
  checkoutTime?: string           // 签出时间
  approvedBy?: number             // 审批人 ID
  approvedAt?: string             // 审批时间
  lab?: Lab                       // 关联的实验室信息
  device?: Device                 // 关联的设备信息
  requester?: UserBrief           // 预约人信息
  course?: Course                 // 关联的课程信息
  createdAt?: string
  updatedAt?: string
}

/**
 * 创建单次预约请求参数
 */
export interface CreateReservationParams {
  labId: number
  deviceId?: number
  title: string
  startTime: string
  endTime: string
  remark?: string
}

/**
 * 批量预约规则类型
 */
export type SeriesRuleType = 'DAILY' | 'WEEKLY' | 'CUSTOM'

/**
 * 批量预约规则
 */
export interface SeriesRule {
  type: SeriesRuleType
  value: {
    count?: number           // DAILY/WEEKLY: 重复次数
    interval?: number        // DAILY: 间隔天数
    daysOfWeek?: number[]    // WEEKLY: 周几 (1-7, 1=周一)
    dates?: string[]         // CUSTOM: 自定义日期数组
  }
  mode: 'STRICT' | 'LENIENT'  // STRICT: 有冲突则全部失败; LENIENT: 跳过冲突继续创建
}

/**
 * 创建系列预约请求参数
 */
export interface CreateSeriesReservationParams {
  labId: number
  deviceId?: number
  rule: SeriesRule
  time: {
    startTime: string
    endTime: string
  }
}

/**
 * 创建课程预约请求参数
 */
export interface CreateCourseReservationParams {
  labId: number
  courseId: number
  title: string
  slots: Array<{
    startTime: string
    endTime: string
  }>
  remark?: string
}

/**
 * 更新预约请求参数
 */
export interface UpdateReservationParams {
  title?: string
  startTime?: string
  endTime?: string
  remark?: string
}

/**
 * 拒绝预约请求参数
 */
export interface RejectReservationParams {
  reason: string
}

/**
 * 覆盖预约请求参数
 */
export interface OverrideReservationParams {
  reason: string
}

// ==================== 日历相关类型 ====================

/**
 * 日历事件
 */
export interface CalendarEvent {
  id: number
  title: string
  startTime: string
  endTime: string
  status: ReservationStatus
  type: ReservationType
  labId: number
  labName?: string
  deviceId?: number
  deviceName?: string
  requesterName?: string
}

// ==================== 通知相关类型 ====================

/**
 * 通知信息
 */
export interface Notification {
  id: number
  userId: number              // 接收者 ID
  title: string               // 通知标题
  content: string             // 通知内容
  status: NotificationStatus  // 通知状态
  createdAt: string
  readAt?: string
}

// ==================== 审计日志相关类型 ====================

/**
 * 审计日志
 */
export interface AuditLog {
  id: number
  actorId: number             // 操作人 ID
  actorName?: string          // 操作人姓名
  action: string              // 操作类型
  targetType: AuditTargetType // 目标类型
  targetId: number            // 目标 ID
  detail?: string             // 详细信息 (JSON 字符串)
  createdAt: string
}

// ==================== 规则配置相关类型 ====================

/**
 * 规则配置
 */
export interface RuleConfig {
  key: string                 // 配置键
  value: any                  // 配置值 (JSON 对象)
  description: string         // 配置描述
  updatedAt?: string
}

/**
 * 更新规则配置请求参数
 */
export interface UpdateRuleConfigParams {
  value: any
  description?: string
}

// ==================== 统计相关类型 ====================

/**
 * 仪表板统计数据
 */
export interface DashboardStats {
  totalLabs: number
  totalDevices: number
  activeReservations: number
  pendingApprovals: number
}
