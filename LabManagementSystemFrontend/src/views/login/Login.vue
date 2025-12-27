<template>
  <div class="login-container">
    <!-- 背景装饰 -->
    <div class="bg-decoration">
      <div class="circle circle-1"></div>
      <div class="circle circle-2"></div>
      <div class="circle circle-3"></div>
    </div>

    <div class="login-card">
      <!-- 左侧装饰区域 -->
      <div class="card-left">
        <div class="brand-content">
          <div class="logo-wrapper">
            <el-icon class="logo-icon"><Monitor /></el-icon>
          </div>
          <h1>智能实验室管理系统</h1>
          <p>Laboratory Management System</p>
          <div class="features">
            <div class="feature-item">
              <el-icon><Calendar /></el-icon>
              <span>智能预约</span>
            </div>
            <div class="feature-item">
              <el-icon><DataAnalysis /></el-icon>
              <span>设备管理</span>
            </div>
            <div class="feature-item">
              <el-icon><UserFilled /></el-icon>
              <span>权限控制</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧登录表单 -->
      <div class="card-right">
        <div class="form-header">
          <h2>欢迎回来</h2>
          <p>请选择身份并登录您的账户</p>
        </div>

        <!-- 身份选择选项卡 -->
        <div class="role-tabs">
          <div
            v-for="role in roles"
            :key="role.value"
            class="role-tab"
            :class="{ active: selectedRole === role.value }"
            @click="selectedRole = role.value"
          >
            <el-icon :size="24"><component :is="role.icon" /></el-icon>
            <span>{{ role.label }}</span>
          </div>
        </div>

        <el-form
          :model="form"
          ref="formRef"
          :rules="rules"
          size="large"
          class="login-form"
          :validate-on-rule-change="false"
          @keyup.enter="handleLogin"
        >
          <el-form-item prop="username">
            <el-input
              v-model="form.username"
              placeholder="请输入用户名"
              :prefix-icon="User"
              clearable
            />
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              :prefix-icon="Lock"
              show-password
            />
          </el-form-item>

          <div class="form-options">
            <el-checkbox v-model="rememberMe">记住我</el-checkbox>
            <el-link type="primary" :underline="false">忘记密码？</el-link>
          </div>

          <el-form-item>
            <el-button
              type="primary"
              :loading="loading"
              class="login-btn"
              @click="handleLogin"
            >
              {{ loading ? '登录中...' : '登 录' }}
            </el-button>
          </el-form-item>
        </el-form>

        <div class="form-footer">
          <span>还没有账号？</span>
          <el-link type="primary" @click="router.push('/register')">立即注册</el-link>
          <span class="hint">（仅支持学生注册）</span>
        </div>

        <div class="copyright">
          © 2024 智能实验室管理系统 版权所有
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { login } from '@/api/auth'
import { ElMessage } from 'element-plus'
import {
  User,
  Lock,
  Monitor,
  Calendar,
  DataAnalysis,
  UserFilled,
  Avatar,
  Reading,
  Setting
} from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const rememberMe = ref(false)
const formRef = ref()
const selectedRole = ref('STUDENT')

const roles = [
  { value: 'STUDENT', label: '学生', icon: Reading },
  { value: 'TEACHER', label: '教师', icon: Avatar },
  { value: 'ADMIN', label: '管理员', icon: Setting }
]

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 50, message: '用户名长度为 2-50 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 100, message: '密码长度不能少于 6 个字符', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid: boolean) => {
    if (!valid) return

    loading.value = true
    try {
      const res = await login({
        username: form.username,
        password: form.password,
        role: selectedRole.value
      })

      // 保存 token 和用户信息
      userStore.setToken(res.data.accessToken)
      userStore.setUserInfo({
        id: res.data.user.id,
        name: res.data.user.name,
        role: res.data.user.role
      })

      ElMessage.success(`登录成功，欢迎 ${res.data.user.name}`)

      // 跳转到首页或重定向页面
      const redirect = router.currentRoute.value.query.redirect as string
      router.push(redirect || '/dashboard')
    } catch (error: any) {
      // 错误已在 request.ts 拦截器中统一处理
      console.error('登录失败:', error)
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped lang="scss">
.login-container {
  min-height: 100vh;
  width: 100vw;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;
}

// 背景装饰圆
.bg-decoration {
  position: absolute;
  inset: 0;
  overflow: hidden;
  pointer-events: none;

  .circle {
    position: absolute;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.1);
    animation: float 20s infinite ease-in-out;
  }

  .circle-1 {
    width: 400px;
    height: 400px;
    top: -100px;
    left: -100px;
    animation-delay: 0s;
  }

  .circle-2 {
    width: 300px;
    height: 300px;
    bottom: -50px;
    right: -50px;
    animation-delay: -5s;
  }

  .circle-3 {
    width: 200px;
    height: 200px;
    top: 50%;
    right: 20%;
    animation-delay: -10s;
  }
}

@keyframes float {
  0%, 100% {
    transform: translate(0, 0) scale(1);
  }
  25% {
    transform: translate(30px, -30px) scale(1.05);
  }
  50% {
    transform: translate(-20px, 20px) scale(0.95);
  }
  75% {
    transform: translate(20px, 10px) scale(1.02);
  }
}

// 登录卡片
.login-card {
  display: flex;
  width: 800px;
  min-height: 550px;
  background: rgba(255, 255, 255, 0.45);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 24px;
  border: 1px solid rgba(255, 255, 255, 0.4);
  box-shadow:
    0 8px 32px rgba(0, 0, 0, 0.1),
    inset 0 1px 1px rgba(255, 255, 255, 0.3);
  overflow: hidden;
  position: relative;
  z-index: 10;
}

// 左侧装饰区域
.card-left {
  width: 40%;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.8) 0%, rgba(118, 75, 162, 0.8) 100%);
  padding: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  backdrop-filter: blur(10px);

  &::before {
    content: '';
    position: absolute;
    inset: 0;
    background: url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%23ffffff' fill-opacity='0.05'%3E%3Cpath d='M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E");
  }
}

.brand-content {
  text-align: center;
  color: white;
  position: relative;
  z-index: 1;

  .logo-wrapper {
    width: 80px;
    height: 80px;
    background: rgba(255, 255, 255, 0.2);
    backdrop-filter: blur(10px);
    border-radius: 20px;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto 24px;
    border: 1px solid rgba(255, 255, 255, 0.3);

    .logo-icon {
      font-size: 40px;
    }
  }

  h1 {
    font-size: 24px;
    font-weight: 600;
    margin: 0 0 8px;
    letter-spacing: 1px;
  }

  p {
    font-size: 13px;
    opacity: 0.8;
    margin: 0 0 40px;
    letter-spacing: 2px;
  }
}

.features {
  display: flex;
  flex-direction: column;
  gap: 16px;

  .feature-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px 20px;
    background: rgba(255, 255, 255, 0.15);
    backdrop-filter: blur(10px);
    border-radius: 12px;
    border: 1px solid rgba(255, 255, 255, 0.2);
    transition: all 0.3s ease;

    &:hover {
      background: rgba(255, 255, 255, 0.25);
      transform: translateX(5px);
    }

    .el-icon {
      font-size: 20px;
    }

    span {
      font-size: 14px;
      font-weight: 500;
    }
  }
}

// 右侧表单区域
.card-right {
  flex: 1;
  padding: 40px 50px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  background: rgba(255, 255, 255, 0.2);
}

.form-header {
  margin-bottom: 24px;

  h2 {
    font-size: 26px;
    font-weight: 700;
    color: #2d3748;
    margin: 0 0 8px;
  }

  p {
    font-size: 14px;
    color: #4a5568;
    margin: 0;
  }
}

// 身份选择选项卡
.role-tabs {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;

  .role-tab {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    padding: 12px;
    border-radius: 12px;
    border: 1px solid rgba(229, 231, 235, 0.5);
    background: rgba(255, 255, 255, 0.3);
    cursor: pointer;
    transition: all 0.3s ease;

    span {
      font-size: 13px;
      font-weight: 500;
      color: #4a5568;
    }

    .el-icon {
      color: #718096;
    }

    &:hover {
      border-color: #667eea;
      background: rgba(102, 126, 234, 0.1);
    }

    &.active {
      border-color: #667eea;
      background: rgba(102, 126, 234, 0.15);
      box-shadow: 0 4px 12px rgba(102, 126, 234, 0.1);

      span {
        color: #667eea;
        font-weight: 600;
      }

      .el-icon {
        color: #667eea;
      }
    }
  }
}

.login-form {
  :deep(.el-form-item) {
    margin-bottom: 20px;
  }

  :deep(.el-input__wrapper) {
    padding: 4px 15px;
    border-radius: 12px;
    background: rgba(255, 255, 255, 0.4);
    box-shadow: 0 0 0 1px rgba(229, 231, 235, 0.5) inset;
    transition: all 0.3s ease;

    &:hover {
      background: rgba(255, 255, 255, 0.6);
      box-shadow: 0 0 0 1px #667eea inset;
    }

    &.is-focus {
      background: rgba(255, 255, 255, 0.8);
      box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.3) inset;
    }
  }

  :deep(.el-input__inner) {
    height: 44px;
    font-size: 15px;
    color: #2d3748;
  }

  :deep(.el-input__prefix) {
    font-size: 18px;
    color: #718096;
  }
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;

  :deep(.el-checkbox__label) {
    color: #4a5568;
  }
}

.login-btn {
  width: 100%;
  height: 50px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  letter-spacing: 4px;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 20px rgba(102, 126, 234, 0.4);
  }

  &:active {
    transform: translateY(0);
  }
}

.form-footer {
  text-align: center;
  margin-top: 20px;
  font-size: 14px;
  color: #4a5568;

  .el-link {
    margin-left: 4px;
    font-weight: 500;
  }

  .hint {
    font-size: 12px;
    color: #718096;
    margin-left: 4px;
  }
}

// 底部版权
.copyright {
  text-align: center;
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid rgba(229, 231, 235, 0.5);
  font-size: 12px;
  color: #718096;
}

// 响应式适配
@media (max-width: 968px) {
  .login-card {
    width: 90%;
    max-width: 420px;
    flex-direction: column;
  }

  .card-left {
    display: none;
  }

  .card-right {
    padding: 40px 30px;
  }

  .role-tabs .role-tab {
    padding: 12px 8px;
  }
}
</style>
