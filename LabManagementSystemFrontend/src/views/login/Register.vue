<template>
  <div class="register-container">
    <!-- 背景装饰 -->
    <div class="bg-decoration">
      <div class="circle circle-1"></div>
      <div class="circle circle-2"></div>
      <div class="circle circle-3"></div>
    </div>

    <div class="register-card">
      <div class="card-header">
        <div class="logo-wrapper">
          <el-icon class="logo-icon"><UserFilled /></el-icon>
        </div>
        <h2>创建学生账号</h2>
        <p>加入智能实验室管理系统</p>
      </div>

      <el-form
        :model="form"
        ref="formRef"
        :rules="rules"
        size="large"
        class="register-form"
        :validate-on-rule-change="false"
        @keyup.enter="handleRegister"
      >
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入用户名"
            :prefix-icon="User"
            clearable
          />
        </el-form-item>

        <el-form-item prop="email">
          <el-input
            v-model="form.email"
            placeholder="请输入邮箱地址"
            :prefix-icon="Message"
            clearable
          />
        </el-form-item>

        <el-form-item prop="phone">
          <el-input
            v-model="form.phone"
            placeholder="请输入手机号（选填）"
            :prefix-icon="Iphone"
            clearable
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请设置密码"
            :prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <el-form-item prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="请确认密码"
            :prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            class="register-btn"
            @click="handleRegister"
          >
            {{ loading ? '注册中...' : '立即注册' }}
          </el-button>
        </el-form-item>
      </el-form>

      <div class="form-footer">
        <span>已有账号？</span>
        <el-link type="primary" @click="router.push('/login')">直接登录</el-link>
      </div>

      <div class="notice">
        <el-icon><InfoFilled /></el-icon>
        <span>注意：仅支持学生身份注册，教师账号请联系管理员创建</span>
      </div>

      <div class="copyright">
        © 2024 智能实验室管理系统 版权所有
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { register } from '@/api/auth'
import { ElMessage } from 'element-plus'
import { User, Lock, Message, Iphone, UserFilled, InfoFilled } from '@element-plus/icons-vue'

const router = useRouter()
const loading = ref(false)
const formRef = ref()

const form = reactive({
  username: '',
  email: '',
  phone: '',
  password: '',
  confirmPassword: ''
})

const validatePass2 = (_rule: any, value: any, callback: any) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== form.password) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 50, message: '用户名长度为 2-50 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 100, message: '密码长度不能少于 6 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validatePass2, trigger: 'blur' }
  ]
}

const handleRegister = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid: boolean) => {
    if (!valid) return

    loading.value = true
    try {
      await register({
        username: form.username,
        password: form.password,
        email: form.email,
        phone: form.phone || undefined
      })

      ElMessage.success('注册成功，请登录')
      router.push('/login')
    } catch (error: any) {
      // 错误已在 request.ts 拦截器中统一处理
      console.error('注册失败:', error)
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped lang="scss">
.register-container {
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
    right: -100px;
    animation-delay: 0s;
  }

  .circle-2 {
    width: 300px;
    height: 300px;
    bottom: -50px;
    left: -50px;
    animation-delay: -5s;
  }

  .circle-3 {
    width: 200px;
    height: 200px;
    top: 50%;
    left: 20%;
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

.register-card {
  width: 440px;
  padding: 40px 45px;
  background: rgba(255, 255, 255, 0.45);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 24px;
  border: 1px solid rgba(255, 255, 255, 0.4);
  box-shadow:
    0 8px 32px rgba(0, 0, 0, 0.1),
    inset 0 1px 1px rgba(255, 255, 255, 0.3);
  position: relative;
  z-index: 10;
}

.card-header {
  text-align: center;
  margin-bottom: 30px;

  .logo-wrapper {
    width: 70px;
    height: 70px;
    background: linear-gradient(135deg, rgba(102, 126, 234, 0.8) 0%, rgba(118, 75, 162, 0.8) 100%);
    border-radius: 50%;
    margin: 0 auto 20px;
    display: flex;
    justify-content: center;
    align-items: center;
    box-shadow: 0 8px 16px rgba(102, 126, 234, 0.2);

    .logo-icon {
      font-size: 32px;
      color: white;
    }
  }

  h2 {
    margin: 0 0 8px;
    font-size: 26px;
    color: #2d3748;
    font-weight: 700;
  }

  p {
    margin: 0;
    color: #4a5568;
    font-size: 14px;
  }
}

.register-form {
  :deep(.el-form-item) {
    margin-bottom: 18px;
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

.register-btn {
  width: 100%;
  height: 50px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  letter-spacing: 2px;
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
}

.notice {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  margin-top: 20px;
  padding: 12px;
  background: rgba(102, 126, 234, 0.08);
  border-radius: 8px;
  font-size: 12px;
  color: #667eea;

  .el-icon {
    font-size: 14px;
  }
}

.copyright {
  text-align: center;
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid rgba(229, 231, 235, 0.5);
  font-size: 12px;
  color: #718096;
}

@media (max-width: 480px) {
  .register-card {
    width: 90%;
    padding: 30px 25px;
  }
}
</style>
