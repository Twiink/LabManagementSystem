# 智能实验室预约与设备管理系统 API 设计文档

> 面向前后端对接的详细 API 规范，基于已确认需求、后端设计与数据库设计。

## 1. 基本约定
- 协议：HTTPS
- Base URL：`/api`
- 数据格式：JSON
- 编码：UTF-8
- 时间：ISO 8601（UTC），精度到分钟，例如 `2025-01-15T08:30:00Z`
- 认证：Bearer Token（OAuth2 Resource Server）

### 1.1 通用请求头
- `Authorization: Bearer <token>`
- `Content-Type: application/json`
- `Accept: application/json`
- 可选：`Idempotency-Key`（预约创建类接口建议带）

### 1.2 通用响应结构
```json
{
  "code": 0,
  "message": "OK",
  "data": {},
  "requestId": "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6a"
}
```

### 1.3 通用错误码
- 4001 预约时间窗不满足
- 4002 资源冲突
- 4003 资源维护中/停用/报废
- 4004 超出预约上限
- 4005 权限不足
- 4006 参数校验失败
- 4007 预约状态不允许该操作
- 4008 审批流程要求未满足
- 4009 资源不存在
- 4010 登录失效/未认证

### 1.4 分页规范
- Query：`page`（从 1 开始），`pageSize`（默认 20，最大 200）
- 响应：
```json
{
  "code": 0,
  "message": "OK",
  "data": {
    "items": [],
    "page": 1,
    "pageSize": 20,
    "total": 120
  }
}
```

### 1.5 枚举值
- users.role: `ADMIN`/`TEACHER`/`STUDENT`
- users.status: `ACTIVE`/`DISABLED`
- labs.status: `IDLE`/`RESERVED`/`IN_USE`/`MAINTENANCE`/`DISABLED`
- devices.status: `IDLE`/`RESERVED`/`IN_USE`/`MAINTENANCE`/`RETIRED`
- reservations.status: `PENDING`/`APPROVED`/`REJECTED`/`CANCELLED`/`EXPIRED`/`IN_USE`/`COMPLETED`
- reservations.type: `SINGLE`/`RECURRING`/`COURSE`
- reservations.priority: `NORMAL`/`COURSE`
- approvals.action: `APPROVE`/`REJECT`/`OVERRIDE`
- notifications.status: `UNREAD`/`READ`
- reservation_series.rule_type: `DAILY`/`WEEKLY`/`CUSTOM`
- reservation_series.mode: `STRICT`/`PARTIAL`

## 2. 认证与用户

### 2.1 登录
POST `/api/auth/login`

请求
```json
{
  "username": "teacher01",
  "password": "******"
}
```

响应
```json
{
  "code": 0,
  "message": "OK",
  "data": {
    "accessToken": "...",
    "tokenType": "Bearer",
    "expiresIn": 3600,
    "user": {
      "id": 12,
      "name": "张老师",
      "role": "TEACHER"
    }
  }
}
```

### 2.2 获取当前用户
GET `/api/users/me`

响应
```json
{
  "code": 0,
  "message": "OK",
  "data": {
    "id": 12,
    "name": "张老师",
    "email": "t1@school.edu",
    "phone": "13800000000",
    "role": "TEACHER",
    "status": "ACTIVE"
  }
}
```

### 2.3 用户列表（管理员）
GET `/api/users`

Query
- `role` 可选
- `status` 可选
- `page` `pageSize`

响应：分页结构

## 3. 实验室管理

### 3.1 实验室列表
GET `/api/labs`

Query
- `status` 可选
- `keyword` 可选（name/location）
- `page` `pageSize`

响应
```json
{
  "code": 0,
  "message": "OK",
  "data": {
    "items": [
      {
        "id": 1,
        "name": "化学实验室 A",
        "location": "1号楼 203",
        "capacity": 40,
        "openTimeStart": "08:00:00",
        "openTimeEnd": "22:00:00",
        "status": "IDLE"
      }
    ],
    "page": 1,
    "pageSize": 20,
    "total": 1
  }
}
```

### 3.2 新增实验室（管理员）
POST `/api/labs`

请求
```json
{
  "name": "化学实验室 A",
  "location": "1号楼 203",
  "capacity": 40,
  "openTimeStart": "08:00:00",
  "openTimeEnd": "22:00:00"
}
```

### 3.3 更新实验室（管理员）
PUT `/api/labs/{id}`

请求（同新增）

### 3.4 更新实验室状态（管理员）
PATCH `/api/labs/{id}/status`

请求
```json
{ "status": "MAINTENANCE" }
```

## 4. 设备管理

### 4.1 设备列表
GET `/api/devices`

Query
- `labId` 可选
- `status` 可选
- `keyword` 可选（name/model）
- `page` `pageSize`

### 4.2 新增设备（管理员）
POST `/api/devices`

请求
```json
{
  "labId": 1,
  "name": "光谱仪",
  "model": "SP-2025"
}
```

### 4.3 更新设备（管理员）
PUT `/api/devices/{id}`

### 4.4 更新设备状态（管理员）
PATCH `/api/devices/{id}/status`

请求
```json
{ "status": "MAINTENANCE" }
```

## 5. 课程管理

### 5.1 课程列表
GET `/api/courses`

Query
- `createdBy` 可选
- `term` 可选
- `page` `pageSize`

### 5.2 新增课程（教师/管理员）
POST `/api/courses`

请求
```json
{
  "name": "分析化学",
  "className": "化学 2023 级 1 班",
  "studentCount": 45,
  "term": "2024-2025-2"
}
```

### 5.3 获取课程详情
GET `/api/courses/{id}`

## 6. 预约管理

### 6.1 预约列表
GET `/api/reservations`

Query
- `labId` 可选
- `deviceId` 可选
- `requesterId` 可选（管理员）
- `status` 可选（多值逗号分隔）
- `type` 可选
- `from` `to`（时间范围）
- `page` `pageSize`

### 6.2 单次预约创建
POST `/api/reservations`

权限
- 教师/学生/管理员

规则
- 设备级预约需要审批（status = PENDING）
- 实验室级普通预约自动通过（status = APPROVED）
- 冲突检测、维护/停用校验

请求
```json
{
  "labId": 1,
  "deviceId": null,
  "startTime": "2025-01-15T08:30:00Z",
  "endTime": "2025-01-15T10:30:00Z",
  "reason": "实验课程"
}
```

响应
```json
{
  "code": 0,
  "message": "OK",
  "data": {
    "reservationId": 101,
    "status": "APPROVED"
  }
}
```

### 6.3 多次预约创建
POST `/api/reservations/series`

权限
- 教师/管理员

请求
```json
{
  "labId": 1,
  "deviceId": 3,
  "rule": {
    "type": "WEEKLY",
    "value": { "daysOfWeek": [1,3,5], "count": 8 },
    "mode": "PARTIAL"
  },
  "time": {
    "startTime": "2025-03-01T08:30:00Z",
    "endTime": "2025-03-01T10:30:00Z"
  }
}
```

响应
```json
{
  "code": 0,
  "message": "OK",
  "data": {
    "seriesId": 12,
    "created": [101, 102, 103],
    "failed": [
      {
        "startTime": "2025-03-08T08:30:00Z",
        "endTime": "2025-03-08T10:30:00Z",
        "reason": "CONFLICT"
      }
    ]
  }
}
```

### 6.4 课程预约创建
POST `/api/reservations/course`

权限
- 教师/管理员

规则
- 课程预约进入待审（status = PENDING）
- 审批通过后可覆盖普通预约并通知

请求
```json
{
  "courseId": 5,
  "labId": 1,
  "deviceId": null,
  "startTime": "2025-03-01T08:30:00Z",
  "endTime": "2025-03-01T10:30:00Z"
}
```

### 6.5 预约详情
GET `/api/reservations/{id}`

### 6.6 修改预约
PUT `/api/reservations/{id}`

规则
- 教师：开始前 >= 12 小时
- 学生：开始前 >= 24 小时
- 超时仅管理员可修改并需填写原因
- 修改后重新冲突检测

请求
```json
{
  "startTime": "2025-01-15T09:00:00Z",
  "endTime": "2025-01-15T11:00:00Z",
  "reason": "时间调整"
}
```

### 6.7 取消预约
DELETE `/api/reservations/{id}`

规则
- 教师：开始前 >= 12 小时
- 学生：开始前 >= 24 小时
- 超时仅管理员可取消并需填写原因

Query
- `reason` 必填（超时管理员操作）

### 6.8 预约状态流转（系统/管理员）
POST `/api/reservations/{id}/checkin`
- 预约开始后用于签到，状态转为 IN_USE

POST `/api/reservations/{id}/checkout`
- 结束使用，状态转为 COMPLETED

## 7. 审批管理（管理员）

### 7.1 预约审批通过
POST `/api/reservations/{id}/approve`

请求
```json
{ "reason": "审核通过" }
```

### 7.2 预约审批拒绝
POST `/api/reservations/{id}/reject`

请求
```json
{ "reason": "设备维护中" }
```

### 7.3 强制调整/覆盖
POST `/api/reservations/{id}/override`

请求
```json
{
  "reason": "紧急教学安排",
  "action": "CANCEL_CONFLICTS"  
}
```

说明
- 课程预约审批通过后，可触发覆盖普通预约并通知。

## 8. 审计与通知

### 8.1 审计日志（管理员）
GET `/api/audit-logs`

Query
- `actorId` `targetType` `targetId` `from` `to` `page` `pageSize`

### 8.2 通知列表
GET `/api/notifications`

Query
- `status` 可选
- `page` `pageSize`

### 8.3 通知标记已读
POST `/api/notifications/{id}/read`

## 9. 规则配置（管理员）

### 9.1 查询规则
GET `/api/rule-configs`

### 9.2 更新规则
PUT `/api/rule-configs/{key}`

请求
```json
{
  "value": {
    "teacherCancelHours": 12,
    "studentCancelHours": 24,
    "maxDailyReservations": 2
  },
  "description": "预约与取消规则"
}
```

## 10. 日历视图支持

### 10.1 获取某资源的时间段预约
GET `/api/calendar`

Query
- `labId` 可选
- `deviceId` 可选
- `from` `to` 必填

响应
```json
{
  "code": 0,
  "message": "OK",
  "data": [
    {
      "reservationId": 101,
      "title": "分析化学",
      "startTime": "2025-01-15T08:30:00Z",
      "endTime": "2025-01-15T10:30:00Z",
      "status": "APPROVED",
      "type": "COURSE"
    }
  ]
}
```

## 11. 业务规则映射说明
- 设备级预约需审批：当 `deviceId` 不为空，默认进入 PENDING。
- 课程预约需审批：`/reservations/course` 默认 PENDING。
- 实验室级普通预约自动通过：`deviceId` 为空且 `type=SINGLE`。
- 课程预约覆盖普通预约：审批通过后执行覆盖并通知。
- 冲突判断：`startA < endB` 且 `endA > startB`。
- 维护/停用资源不可预约。

## 12. 示例错误响应
```json
{
  "code": 4002,
  "message": "资源冲突",
  "data": {
    "conflicts": [
      {
        "reservationId": 99,
        "startTime": "2025-01-15T08:00:00Z",
        "endTime": "2025-01-15T09:00:00Z"
      }
    ]
  }
}
```

