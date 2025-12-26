<template>
  <div class="login-container">
    <div class="glass-card login-box">
      <div class="header">
        <div class="logo-circle">
          <el-icon><UserFilled /></el-icon>
        </div>
        <h2>创建账号</h2>
        <p>加入智能实验室管理系统</p>
      </div>
      
      <el-form :model="form" ref="formRef" :rules="rules" size="large" class="login-form">
        <el-form-item prop="username">
          <el-input 
            v-model="form.username" 
            placeholder="用户名" 
            prefix-icon="User"
            class="glass-input"
          />
        </el-form-item>
        <el-form-item prop="email">
          <el-input 
            v-model="form.email" 
            placeholder="邮箱地址" 
            prefix-icon="Message"
            class="glass-input"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input 
            v-model="form.password" 
            type="password" 
            placeholder="设置密码" 
            prefix-icon="Lock"
            show-password
            class="glass-input"
          />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input 
            v-model="form.confirmPassword" 
            type="password" 
            placeholder="确认密码" 
            prefix-icon="Lock"
            show-password
            class="glass-input"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" :loading="loading" class="submit-btn" @click="handleRegister" round>
            立即注册
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="footer">
        <span>已有账号？</span>
        <el-link type="primary" @click="router.push('/login')">直接登录</el-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()
const loading = ref(false)
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
    callback(new Error('两次输入密码不一致!'))
  } else {
    callback()
  }
}

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  confirmPassword: [{ validator: validatePass2, trigger: 'blur' }]
}

const formRef = ref()

const handleRegister = async () => {
  if (!formRef.value) return
  await formRef.value.validate((valid: boolean) => {
    if (valid) {
      loading.value = true
      // Mock Register
      setTimeout(() => {
        loading.value = false
        ElMessage.success('注册成功，请登录')
        router.push('/login')
      }, 1000)
    }
  })
}
</script>

<style scoped lang="scss">
.login-container {
  height: 100vh;
  width: 100vw;
  display: flex;
  justify-content: center;
  align-items: center;
  background: transparent;
}

.login-box {
  width: 420px;
  padding: 40px 50px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.6);
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  align-items: center;
}

.header {
  text-align: center;
  margin-bottom: 30px;
  
  .logo-circle {
    width: 60px;
    height: 60px;
    background: var(--primary-color);
    border-radius: 50%;
    margin: 0 auto 20px;
    display: flex;
    justify-content: center;
    align-items: center;
    color: white;
    font-size: 32px;
    box-shadow: 0 10px 20px rgba(64, 158, 255, 0.3);
  }

  h2 {
    margin: 0 0 10px;
    font-size: 28px;
    color: #2c3e50;
    font-weight: 700;
  }

  p {
    margin: 0;
    color: #606266;
    font-size: 14px;
  }
}

.login-form {
  width: 100%;
}

.submit-btn {
  width: 100%;
  font-size: 16px;
  padding: 22px 0;
  box-shadow: 0 10px 20px rgba(64, 158, 255, 0.3);
  transition: transform 0.2s;
  
  &:hover {
    transform: translateY(-2px);
  }
  
  &:active {
    transform: translateY(0);
  }
}

.footer {
  margin-top: 10px;
  font-size: 14px;
  color: #606266;
  display: flex;
  gap: 8px;
  align-items: center;
}
</style>
