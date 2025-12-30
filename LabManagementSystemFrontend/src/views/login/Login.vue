<template>
  <div class="login-container">
    <div class="login-box">
      <!-- Left: Visual Identity - RESTORED TO 45% -->
      <div class="visual-side">
        <div class="visual-content">
          <div class="top-section">
            <div class="logo-group">
              <div class="logo-square">
                <el-icon><Monitor /></el-icon>
              </div>
              <span class="app-name">LAB·OS</span>
            </div>
            
            <div class="hero-text">
              <div class="tag-line">PRECISION · CONTROL</div>
              <h2>实验室综合管理系统</h2>
              <p>Professional Laboratory Management Platform</p>
            </div>
          </div>

          <!-- System Dashboard - 3 Progress Bars -->
          <div class="system-dashboard">
            <div class="dashboard-header">SYSTEM_METRICS</div>
            <div class="monitor-row">
              <span class="key">ACTIVE_LABS</span>
              <div class="progress-track"><div class="progress-fill" style="width: 75%"></div></div>
              <span class="val">12/16</span>
            </div>
            <div class="monitor-row">
              <span class="key">DEVICE_LOAD</span>
              <div class="progress-track"><div class="progress-fill" style="width: 42%"></div></div>
              <span class="val">42%</span>
            </div>
            <div class="monitor-row">
              <span class="key">GATEWAY</span>
              <div class="progress-track"><div class="progress-fill" style="width: 100%"></div></div>
              <span class="val">SECURE</span>
            </div>
          </div>

          <!-- Terminal Log with Cursor -->
          <div class="terminal-log">
            <div class="log-line">> INITIALIZING_CORE_MODULES...</div>
            <div class="log-line">> CONNECTING_TO_DATABASE... [OK]</div>
            <div class="log-line">> CHECKING_PERMISSIONS... [OK]</div>
            <div class="log-line">> SYSTEM_READY_FOR_AUTH_</div>
            <div class="cursor-blink"></div>
          </div>

          <div class="tech-specs">
            <div class="spec-row">
              <span class="label">VERSION</span>
              <span class="value">2.4.0 (Stable)</span>
            </div>
            <div class="spec-row">
              <span class="label">STATUS</span>
              <span class="value status-ok">● SYSTEM ONLINE</span>
            </div>
          </div>
        </div>
        
        <div class="geo-pattern">
          <div class="line line-1"></div>
          <div class="line line-2"></div>
          <div class="circle-outline"></div>
        </div>
      </div>

      <!-- Right: Auth Form -->
      <div class="auth-side">
        <div class="side-scroll-area">
          <div class="auth-header">
            <h2>用户登录</h2>
          </div>

          <!-- Role Slider -->
          <div class="role-selector-frame">
            <div class="role-slider-track">
              <div 
                class="slider-block"
                :style="{ left: `${activeRoleIndex * 33.33}%` }"
              ></div>
              <div 
                v-for="(role, index) in roles" 
                :key="role.value"
                class="role-option"
                :class="{ active: selectedRole === role.value }"
                @click="switchRole(role.value, index)"
              >
                {{ role.label }}
              </div>
            </div>
          </div>

          <el-form
            :model="form"
            ref="formRef"
            :rules="rules"
            size="large"
            class="auth-form"
            @keyup.enter="handleLogin"
          >
            <el-form-item prop="username">
              <div class="input-wrapper">
                <span class="input-label">用户名 / USERNAME</span>
                <el-input
                  v-model="form.username"
                  placeholder="请输入学号/工号/账号"
                  class="minimal-input"
                />
              </div>
            </el-form-item>

            <el-form-item prop="password">
              <div class="input-wrapper">
                <span class="input-label">密 码 / PASSWORD</span>
                <el-input
                  v-model="form.password"
                  type="password"
                  placeholder="请输入安全密码"
                  show-password
                  class="minimal-input"
                />
              </div>
            </el-form-item>

            <div class="auth-actions">
              <el-checkbox v-model="rememberMe" class="mono-checkbox">保持登录状态</el-checkbox>
              <el-link :underline="true" class="mono-link">忘记密码?</el-link>
            </div>

            <button
              type="button"
              class="mono-btn"
              :class="{ 'is-loading': loading }"
              @click="handleLogin"
            >
              <span v-if="!loading">确 认 登 录</span>
              <span v-else>正在建立安全连接...</span>
              <el-icon v-if="!loading" class="arrow"><Right /></el-icon>
            </button>
          </el-form>

          <div class="auth-footer">
            <span class="reg-link" @click="router.push('/register')">注册学生账号 -></span>
          </div>
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
import { Monitor, Right } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const rememberMe = ref(false)
const formRef = ref()
const selectedRole = ref('STUDENT')
const activeRoleIndex = ref(0)

const roles = [
  { value: 'STUDENT', label: '学生' },
  { value: 'TEACHER', label: '教师' },
  { value: 'ADMIN', label: '管理员' }
]

const switchRole = (value: string, index: number) => {
  selectedRole.value = value
  activeRoleIndex.value = index
}

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  console.log('Login button clicked')
  if (!formRef.value) return

  await formRef.value.validate(async (valid: boolean) => {
    console.log('Form validation result:', valid)
    if (!valid) return

    loading.value = true
    try {
      console.log('Sending login request...', {
        username: form.username,
        role: selectedRole.value
      })
      const res = await login({
        username: form.username,
        password: form.password,
        role: selectedRole.value
      })
      console.log('Login successful:', res)

      userStore.setToken(res.data.accessToken)
      userStore.setUserInfo({
        id: res.data.user.id,
        username: res.data.user.username || form.username,
        name: res.data.user.name,
        role: res.data.user.role,
        status: res.data.user.status || 'ACTIVE'
      })

      ElMessage.success({
        message: `欢迎回来，${res.data.user.name}`,
        plain: true,
      })
      const redirect = router.currentRoute.value.query.redirect as string
      router.push(redirect || '/dashboard')
    } catch (error: any) {
      console.error('Login failed:', error)
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped lang="scss">
@import url('https://fonts.googleapis.com/css2?family=JetBrains+Mono:wght@400;500;700&family=Inter:wght@400;600;800&display=swap');

$bg-color: #FFF7F0;
$text-main: #4A403A;
$text-light: #9D8D85;
$accent-color: #FFC085;
$accent-dark: #8C6B5D;

.login-container {
  min-height: 100vh;
  width: 100vw;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f8f8f8;
  background-image: radial-gradient(#e0e0e0 1px, transparent 1px);
  background-size: 20px 20px;
  font-family: 'Inter', sans-serif;
  padding: 40px 20px;
  box-sizing: border-box;
}

.login-box {
  display: flex;
  width: 1100px;
  height: 760px;
  background: #ffffff;
  border: 1px solid $accent-dark;
  box-shadow: 12px 12px 0px $accent-dark;
  overflow: hidden;
}

/* RESTORED TO 45% */
.visual-side {
  width: 45%;
  min-width: 400px;
  background: $bg-color;
  color: $text-main;
  padding: 60px;
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  overflow: hidden;
  border-right: 1px solid rgba($accent-dark, 0.2);
}

.visual-content {
  position: relative;
  z-index: 2;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.top-section { margin-bottom: auto; }

.logo-group {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 60px;

  .logo-square {
    width: 48px;
    height: 48px;
    background: $accent-color;
    color: #fff; 
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 24px;
    font-weight: bold;
    border: 1px solid $accent-dark;
  }

  .app-name {
    font-family: 'JetBrains Mono', monospace;
    font-size: 20px;
    font-weight: 700;
    color: $text-main;
  }
}

.hero-text {
  h1 {
    font-size: 56px;
    font-weight: 800;
    line-height: 1.1;
    margin: 0 0 24px;
    letter-spacing: -2px;
    color: #2c2420;
  }
  
  p {
    font-family: 'JetBrains Mono', monospace;
    font-size: 16px;
    color: $text-main;
    opacity: 0.8;
    line-height: 1.6;
    margin: 0;
    max-width: 400px;
  }
}

.system-dashboard {
  margin-top: auto;
  margin-bottom: 40px;
  border-left: 2px solid rgba($accent-dark, 0.2);
  padding-left: 20px;
  
  .dashboard-header {
    font-family: 'JetBrains Mono', monospace;
    font-size: 12px;
    color: $text-light;
    margin-bottom: 16px;
  }
  
  .monitor-row {
    display: flex;
    align-items: center;
    margin-bottom: 12px;
    font-family: 'JetBrains Mono', monospace;
    font-size: 12px;
    color: $text-main;
    .key { width: 100px; color: $text-light; }
    .progress-track { flex: 1; height: 6px; background: rgba($accent-dark, 0.1); margin: 0 16px; }
    .progress-fill { height: 100%; background: $accent-color; }
    .val { width: 50px; text-align: right; font-weight: 600; }
  }
}

.terminal-log {
  font-family: 'JetBrains Mono', monospace;
  font-size: 11px;
  color: $text-light;
  margin-bottom: 40px;
  .cursor-blink {
    display: inline-block;
    width: 8px; height: 14px;
    background: $text-main;
    animation: blink 1s infinite;
    vertical-align: middle;
  }
}

@keyframes blink { 0%, 100% { opacity: 1; } 50% { opacity: 0; } }

.tech-specs {
  font-family: 'JetBrains Mono', monospace;
  font-size: 12px;
  border-top: 1px solid rgba($accent-dark, 0.1);
  padding-top: 20px;
  .spec-row { display: flex; justify-content: space-between; margin-bottom: 8px; color: $text-main; }
  .label { opacity: 0.6; }
}

.geo-pattern {
  position: absolute; top: 0; left: 0; right: 0; bottom: 0; opacity: 0.08; pointer-events: none;
  .line { position: absolute; background: $accent-dark; }
  .line-1 { width: 1px; height: 100%; left: 30%; }
  .line-2 { width: 100%; height: 1px; top: 40%; }
}

.auth-side {
  flex: 1;
  background: #fff;
  position: relative;
  display: flex;
  flex-direction: column;
}

.side-scroll-area {
  padding: 60px 50px; /* Kept narrow padding for wide inputs */
  overflow-y: auto;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  &::-webkit-scrollbar { display: none; }
  -ms-overflow-style: none;
  scrollbar-width: none;
}

.auth-header { margin-bottom: 48px; h2 { font-size: 32px; font-weight: 800; color: #000; } }

.role-selector-frame { margin-bottom: 48px; border: 1px solid $accent-dark; padding: 4px; }
.role-slider-track { display: flex; position: relative; background: #fafafa; height: 60px; }
.role-option { flex: 1; display: flex; align-items: center; justify-content: center; z-index: 2; cursor: pointer; font-size: 16px; font-weight: 600; color: #666; &.active { color: $text-main; font-weight: 800; } }
.slider-block { position: absolute; top: 0; bottom: 0; width: 33.33%; background: $accent-color; border: 1px solid $accent-dark; box-sizing: border-box; transition: left 0.3s cubic-bezier(0.2, 0.8, 0.2, 1); z-index: 1; }

.input-wrapper {
  width: 100%;
  border-bottom: 2px solid #e0e0e0;
  padding-bottom: 6px;
  margin-bottom: 16px;
  transition: border-color 0.3s;
  &:focus-within { border-color: $accent-color; }
  .input-label { display: block; font-family: 'JetBrains Mono', monospace; font-size: 12px; color: #888; margin-bottom: 8px; font-weight: 700; }
}

.minimal-input :deep(.el-input__inner) { font-size: 17px; color: #000; height: 45px; }

.auth-actions { display: flex; justify-content: space-between; align-items: center; margin: 24px 0 40px; }
.mono-checkbox { :deep(.el-checkbox__input.is-checked .el-checkbox__inner) { background-color: $accent-color; border-color: $accent-dark; } }

.mono-btn {
  width: 100%;
  max-width: 420px;
  height: 64px; 
  background: $accent-color; 
  color: $text-main; 
  border: 1px solid $accent-dark; 
  font-size: 16px; font-weight: 800;
  display: flex; justify-content: space-between; align-items: center; padding: 0 32px;
  cursor: pointer;
  transition: all 0.2s;
  &:hover { background: #ffb066; padding-right: 24px; box-shadow: 6px 6px 0px rgba($accent-dark, 0.2); }
}

.auth-footer { margin-top: 48px; text-align: center; .reg-link { font-size: 14px; color: #666; cursor: pointer; font-weight: 600; &:hover { color: $accent-color; } } }

@media (max-width: 1024px) { .login-box { width: 95vw; flex-direction: column; } .visual-side { display: none; } .mono-btn { max-width: 100%; } }
</style>