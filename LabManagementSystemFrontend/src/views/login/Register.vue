<template>
  <div class="login-container">
    <div class="login-box">
      <!-- Left: Visual Identity - RESTORED TO 45% -->
      <div class="visual-side">
        <div class="visual-content">
          <div class="top-section">
            <div class="logo-group">
              <div class="logo-square">
                <el-icon><UserFilled /></el-icon>
              </div>
              <span class="app-name">LAB·OS</span>
            </div>
            
            <div class="hero-text">
              <h1>加入 · 协作</h1>
              <p>创建学生账号以开启您的实验之旅<br>Join the Professional Laboratory Network</p>
            </div>
          </div>

          <!-- System Dashboard - ADDED 3rd Progress Bar -->
          <div class="system-dashboard">
            <div class="dashboard-header">REGISTRATION_INFO</div>
            <div class="monitor-row">
              <span class="key">ACCESS_LEVEL</span>
              <div class="progress-track"><div class="progress-fill" style="width: 30%"></div></div>
              <span class="val">STUDENT</span>
            </div>
            <div class="monitor-row">
              <span class="key">SECURITY</span>
              <div class="progress-track"><div class="progress-fill" style="width: 100%"></div></div>
              <span class="val">ENCRYPTED</span>
            </div>
            <div class="monitor-row">
              <span class="key">GATEWAY</span>
              <div class="progress-track"><div class="progress-fill" style="width: 100%"></div></div>
              <span class="val">READY</span>
            </div>
          </div>

          <div class="terminal-log">
            <div class="log-line">> WAITING_FOR_USER_INPUT...</div>
            <div class="log-line">> NOTE: ONLY_STUDENT_REGISTRATION_ALLOWED</div>
            <div class="cursor-blink"></div>
          </div>

          <div class="tech-specs">
            <div class="spec-row">
              <span class="label">MODULE</span>
              <span class="value">Account_Creation</span>
            </div>
            <div class="spec-row">
              <span class="label">STATUS</span>
              <span class="value">● STANDBY</span>
            </div>
          </div>
        </div>
        
        <div class="geo-pattern">
          <div class="line line-1"></div>
          <div class="line line-2"></div>
        </div>
      </div>

      <!-- Right: Register Form -->
      <div class="auth-side">
        <div class="side-scroll-area">
          <div class="auth-header">
            <div class="header-pre-title">STUDENT ENROLLMENT</div>
            <h2>创建账号</h2>
          </div>

          <el-form
            :model="form"
            ref="formRef"
            :rules="rules"
            size="large"
            class="auth-form"
            @keyup.enter="handleRegister"
          >
            <el-form-item prop="username">
              <div class="input-wrapper">
                <span class="input-label">用户名 / USERNAME</span>
                <el-input v-model="form.username" placeholder="建议使用真实姓名或学号" class="minimal-input" />
              </div>
            </el-form-item>

            <el-form-item prop="email">
              <div class="input-wrapper">
                <span class="input-label">电子邮箱 / EMAIL</span>
                <el-input v-model="form.email" placeholder="用于接收通知和找回密码" class="minimal-input" />
              </div>
            </el-form-item>

            <el-form-item prop="password">
              <div class="input-wrapper">
                <span class="input-label">安全密码 / PASSWORD</span>
                <el-input v-model="form.password" type="password" placeholder="不少于6位字符" show-password class="minimal-input" />
              </div>
            </el-form-item>

            <el-form-item prop="confirmPassword">
              <div class="input-wrapper">
                <span class="input-label">确认密码 / CONFIRM</span>
                <el-input v-model="form.confirmPassword" type="password" placeholder="请再次输入密码" show-password class="minimal-input" />
              </div>
            </el-form-item>

            <button
              type="button"
              class="mono-btn"
              :class="{ 'is-loading': loading }"
              @click="handleRegister"
            >
              <span v-if="!loading">立 即 注 册</span>
              <span v-else>正在写入数据库...</span>
              <el-icon v-if="!loading" class="arrow"><Right /></el-icon>
            </button>
          </el-form>

          <div class="auth-footer">
            <span class="reg-link" @click="router.push('/login')">已有账号？返回登录 -></span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { register } from '@/api/auth'
import { ElMessage } from 'element-plus'
import { UserFilled, Right } from '@element-plus/icons-vue'

const router = useRouter()
const loading = ref(false)
const formRef = ref()

const form = reactive({
  username: '',
  email: '',
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
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }, { min: 6, message: '至少6位', trigger: 'blur' }],
  confirmPassword: [{ required: true, message: '请确认密码', trigger: 'blur' }, { validator: validatePass2, trigger: 'blur' }]
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
        email: form.email
      })
      ElMessage.success({ message: '注册成功，欢迎加入', plain: true })
      router.push('/login')
    } catch (error: any) {
      console.error('Register failed:', error)
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

.visual-content { z-index: 2; height: 100%; display: flex; flex-direction: column; }
.logo-group { display: flex; align-items: center; gap: 16px; margin-bottom: 40px; }
.logo-square { width: 56px; height: 56px; background: $accent-color; color: #fff; display: flex; align-items: center; justify-content: center; font-size: 28px; font-weight: bold; border: 1px solid $accent-dark; }
.app-name { font-family: 'JetBrains Mono', monospace; font-size: 24px; font-weight: 700; color: $text-main; }

.hero-text h1 { font-size: 48px; font-weight: 800; line-height: 1.2; margin: 0 0 16px; color: #2c2420; }
.hero-text p { font-family: 'JetBrains Mono', monospace; font-size: 14px; color: $text-main; opacity: 0.8; line-height: 1.6; }

/* System Dashboard & 3 Progress Bars */
.system-dashboard {
  margin-top: auto;
  margin-bottom: 40px;
  border-left: 2px solid rgba($accent-dark, 0.2);
  padding-left: 20px;
  
  .dashboard-header { font-family: 'JetBrains Mono', monospace; font-size: 12px; color: $text-light; margin-bottom: 16px; }
  .monitor-row { display: flex; align-items: center; margin-bottom: 12px; font-family: 'JetBrains Mono', monospace; font-size: 12px; color: $text-main; }
  .key { width: 100px; color: $text-light; }
  .progress-track { flex: 1; height: 6px; background: rgba($accent-dark, 0.1); margin: 0 16px; position: relative; }
  .progress-fill { height: 100%; background: $accent-color; }
  .val { width: 80px; text-align: right; color: $text-main; font-weight: 600; }
}

/* Terminal Log & Cursor */
.terminal-log {
  font-family: 'JetBrains Mono', monospace;
  font-size: 11px; 
  color: $text-light; 
  margin-bottom: 40px;
  .cursor-blink { display: inline-block; width: 8px; height: 14px; background: $text-main; animation: blink 1s infinite; vertical-align: middle; }
}

@keyframes blink { 0%, 100% { opacity: 1; } 50% { opacity: 0; } }

.tech-specs { font-size: 12px; border-top: 1px solid rgba($accent-dark, 0.1); padding-top: 20px; .spec-row { display: flex; justify-content: space-between; margin-bottom: 8px; color: $text-main; } .label { opacity: 0.6; } }

.geo-pattern { opacity: 0.08; .line { position: absolute; background: $accent-dark; } .line-1 { width: 1px; height: 100%; left: 30%; } .line-2 { width: 100%; height: 1px; top: 40%; } }

.auth-side { flex: 1; background: #fff; position: relative; display: flex; flex-direction: column; }

.side-scroll-area { 
  padding: 60px 50px;
  overflow-y: auto; 
  height: 100%; 
  display: flex; 
  flex-direction: column; 
  justify-content: center;
  &::-webkit-scrollbar { display: none; }
  -ms-overflow-style: none;
  scrollbar-width: none;
}

.auth-header { margin-bottom: 30px; h2 { font-size: 32px; font-weight: 800; color: #000; } }

.input-wrapper {
  width: 100%;
  border-bottom: 2px solid #e0e0e0; 
  padding-bottom: 8px; 
  margin-bottom: 12px;
  transition: border-color 0.3s;
  &:focus-within { border-color: $accent-color; }
  .input-label { display: block; font-family: 'JetBrains Mono', monospace; font-size: 12px; color: #888; margin-bottom: 8px; font-weight: 700; }
}

.minimal-input :deep(.el-input__inner) { font-size: 17px; color: #000; height: 45px; }

.mono-btn {
  width: 100%;
  max-width: 420px;
  height: 64px; 
  background: $accent-color; 
  color: $text-main; 
  border: 1px solid $accent-dark; 
  font-size: 16px; font-weight: 800;
  display: flex; justify-content: space-between; align-items: center; padding: 0 32px; cursor: pointer; transition: all 0.2s;
  margin-top: 20px;
  &:hover { background: #ffb066; padding-right: 24px; box-shadow: 6px 6px 0px rgba($accent-dark, 0.2); }
}

.auth-footer { margin-top: 30px; text-align: center; .reg-link { font-size: 14px; color: #666; cursor: pointer; font-weight: 600; &:hover { color: $accent-color; } } }

@media (max-width: 1024px) { .login-box { width: 95vw; flex-direction: column; } .visual-side { display: none; } .mono-btn { max-width: 100%; } }
</style>