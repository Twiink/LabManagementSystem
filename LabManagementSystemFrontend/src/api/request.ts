/**
 * Axios 二次封装
 *
 * 功能说明：
 * 1. 统一的请求/响应拦截器
 * 2. 自动携带 Token 认证
 * 3. 统一的错误处理
 * 4. 请求超时配置
 * 5. 响应数据解构
 */

import axios from 'axios'
import type { AxiosInstance, AxiosRequestConfig, AxiosResponse, InternalAxiosRequestConfig } from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'

// ==================== 类型定义 ====================

/**
 * 后端统一响应结构
 */
export interface ApiResponse<T = any> {
  code: number        // 业务状态码，0 表示成功
  message: string     // 响应消息
  data: T             // 响应数据
  requestId?: string  // 请求追踪 ID
}

/**
 * 分页响应数据结构
 */
export interface PageData<T> {
  items: T[]       // 数据列表
  page: number     // 当前页码
  pageSize: number // 每页条数
  total: number    // 总记录数
}

/**
 * 分页请求参数
 */
export interface PageParams {
  page?: number     // 页码，从 1 开始
  pageSize?: number // 每页条数，默认 20，最大 200
}

// ==================== 错误码定义 ====================

/**
 * 业务错误码映射
 * 用于将后端错误码转换为友好的中文提示
 */
const ERROR_CODE_MAP: Record<number, string> = {
  4001: '预约时间窗不满足要求',
  4002: '资源时间冲突，请选择其他时间段',
  4003: '资源当前不可用（维护中/已停用/已报废）',
  4004: '已超出预约上限',
  4005: '权限不足，无法执行此操作',
  4006: '参数校验失败，请检查输入',
  4007: '当前预约状态不允许此操作',
  4008: '审批流程要求未满足',
  4009: '请求的资源不存在',
  4010: '登录已失效，请重新登录'
}

// ==================== Axios 实例创建 ====================

/**
 * 创建 Axios 实例
 * 配置基础 URL、超时时间等
 */
const service: AxiosInstance = axios.create({
  baseURL: '/api',        // API 基础路径，配合 Vite 代理使用
  timeout: 15000,         // 请求超时时间：15秒
  headers: {
    'Content-Type': 'application/json;charset=UTF-8'
  }
})

// ==================== 请求拦截器 ====================

/**
 * 请求拦截器
 * 1. 自动添加 Bearer Token 到请求头
 * 2. 可在此处添加请求日志、请求加密等逻辑
 */
service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    // 从 Pinia Store 获取 Token
    const userStore = useUserStore()
    const token = userStore.token

    // 如果存在 Token，添加到请求头
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }

    return config
  },
  (error) => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// ==================== 响应拦截器 ====================

/**
 * 响应拦截器
 * 1. 统一处理响应数据
 * 2. 统一处理错误状态码
 * 3. 处理登录过期等特殊情况
 */
service.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    const res = response.data

    // 业务状态码为 0 表示成功
    if (res.code === 0) {
      return response
    }

    // 处理登录失效（错误码 4010）
    if (res.code === 4010) {
      handleUnauthorized()
      return Promise.reject(new Error(res.message || '登录已失效'))
    }

    // 处理其他业务错误
    const errorMessage = ERROR_CODE_MAP[res.code] || res.message || '请求失败'
    ElMessage.error(errorMessage)
    return Promise.reject(new Error(errorMessage))
  },
  (error) => {
    // 处理 HTTP 错误状态码
    if (error.response) {
      const status = error.response.status
      switch (status) {
        case 401:
          handleUnauthorized()
          break
        case 403:
          ElMessage.error('权限不足，无法访问')
          break
        case 404:
          ElMessage.error('请求的资源不存在')
          break
        case 500:
          ElMessage.error('服务器内部错误，请稍后重试')
          break
        default:
          ElMessage.error(error.response.data?.message || `请求失败 (${status})`)
      }
    } else if (error.code === 'ECONNABORTED') {
      ElMessage.error('请求超时，请检查网络连接')
    } else if (!navigator.onLine) {
      ElMessage.error('网络连接已断开，请检查网络')
    } else {
      ElMessage.error('网络异常，请稍后重试')
    }

    return Promise.reject(error)
  }
)

// ==================== 辅助函数 ====================

/**
 * 处理未授权/登录失效的情况
 * 清除用户信息并跳转到登录页
 */
function handleUnauthorized(): void {
  ElMessageBox.confirm(
    '登录状态已过期，请重新登录',
    '提示',
    {
      confirmButtonText: '重新登录',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    const userStore = useUserStore()
    userStore.logout()
    // 跳转到登录页，保留当前路径用于登录后跳回
    window.location.href = `/login?redirect=${encodeURIComponent(window.location.pathname)}`
  }).catch(() => {
    // 用户取消，不做处理
  })
}

// ==================== 请求方法封装 ====================

/**
 * GET 请求
 * @param url - 请求地址
 * @param params - 查询参数
 * @param config - 额外配置
 */
export function get<T>(url: string, params?: object, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
  return service.get(url, { params, ...config }).then(res => res.data)
}

/**
 * POST 请求
 * @param url - 请求地址
 * @param data - 请求体数据
 * @param config - 额外配置
 */
export function post<T>(url: string, data?: object, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
  return service.post(url, data, config).then(res => res.data)
}

/**
 * PUT 请求
 * @param url - 请求地址
 * @param data - 请求体数据
 * @param config - 额外配置
 */
export function put<T>(url: string, data?: object, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
  return service.put(url, data, config).then(res => res.data)
}

/**
 * PATCH 请求
 * @param url - 请求地址
 * @param data - 请求体数据
 * @param config - 额外配置
 */
export function patch<T>(url: string, data?: object, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
  return service.patch(url, data, config).then(res => res.data)
}

/**
 * DELETE 请求
 * @param url - 请求地址
 * @param params - 查询参数
 * @param config - 额外配置
 */
export function del<T>(url: string, params?: object, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
  return service.delete(url, { params, ...config }).then(res => res.data)
}

// 导出 axios 实例，供特殊情况使用
export default service
