/**
 * 课程管理模块 API
 *
 * 接口路径: /api/courses
 * 功能: 课程的增删改查
 * 权限: 查询公开，创建/更新/删除需要 TEACHER 或 ADMIN 权限
 */

import { get, post, put, del } from './request'
import type { PageParams, PageData } from './request'
import type { Course, CreateCourseParams, UpdateCourseParams } from './types'

// ==================== 类型定义 ====================

/**
 * 课程列表查询参数
 */
export interface CourseListParams extends PageParams {
  createdBy?: number  // 按创建人筛选（教师查看自己创建的课程）
  studentId?: number  // 按学生ID筛选（学生查看自己选修的课程）
  term?: string       // 按学期筛选，如 "2024-2025-2"
}

// ==================== API 方法 ====================

/**
 * 获取课程列表
 *
 * @description 分页获取课程列表，支持按创建人和学期筛选
 * @param params - 查询参数
 * @returns 分页课程列表
 *
 * @example
 * ```ts
 * // 获取当前学期的所有课程
 * const res = await getCourseList({ term: '2024-2025-2', page: 1 })
 * console.log(res.data.items)
 * ```
 */
export function getCourseList(params?: CourseListParams) {
  return get<PageData<Course>>('/courses', params)
}

/**
 * 创建课程
 *
 * @description 创建新的课程（教师或管理员可用）
 * @param params - 创建参数
 * @returns 创建的课程信息
 *
 * @example
 * ```ts
 * const res = await createCourse({
 *   name: '分析化学',
 *   className: '化学 2023 级 1 班',
 *   studentCount: 45,
 *   term: '2024-2025-2'
 * })
 * ```
 */
export function createCourse(params: CreateCourseParams) {
  return post<Course>('/courses', params)
}

/**
 * 获取课程详情
 *
 * @description 获取指定课程的详细信息
 * @param id - 课程 ID
 * @returns 课程详细信息
 *
 * @example
 * ```ts
 * const res = await getCourseDetail(1)
 * console.log(res.data.name, res.data.studentCount)
 * ```
 */
export function getCourseDetail(id: number) {
  return get<Course>(`/courses/${id}`)
}

/**
 * 更新课程信息
 *
 * @description 更新指定课程的信息（课程创建者或管理员可用）
 * @param id - 课程 ID
 * @param params - 更新参数
 * @returns 更新后的课程信息
 *
 * @example
 * ```ts
 * const res = await updateCourse(1, {
 *   name: '高级分析化学',
 *   className: '化学 2023 级 1 班',
 *   studentCount: 50,
 *   term: '2024-2025-2'
 * })
 * ```
 */
export function updateCourse(id: number, params: UpdateCourseParams) {
  return put<Course>(`/courses/${id}`, params)
}

/**
 * 删除课程
 *
 * @description 删除指定课程（课程创建者或管理员可用）
 * @param id - 课程 ID
 * @returns 操作结果
 *
 * @example
 * ```ts
 * await deleteCourse(1)
 * ```
 */
export function deleteCourse(id: number) {
  return del(`/courses/${id}`)
}
