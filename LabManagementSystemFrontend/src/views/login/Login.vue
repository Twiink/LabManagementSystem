<template>
  <div class="login-container">
    <div class="glass-card login-box">
      <div class="header">
        <div class="logo-circle">
          <el-icon><ElementPlus /></el-icon>
        </div>
        <h2>欢迎回来</h2>
        <p>智能实验室预约与设备管理系统</p>
      </div>
      
      <el-form :model="form" ref="formRef" :rules="rules" size="large" class="login-form">
        <el-form-item prop="username">
          <el-input 
            v-model="form.username" 
            placeholder="用户名 / 邮箱" 
            prefix-icon="User"
            class="glass-input"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input 
            v-model="form.password" 
            type="password" 
            placeholder="密码" 
            prefix-icon="Lock"
            show-password
            class="glass-input"
          />
        </el-form-item>
        
        <!-- Role Selection for Demo -->
        <el-form-item>
          <el-radio-group v-model="selectedRole" size="small" style="width: 100%; justify-content: center;">
            <el-radio-button label="ADMIN">管理员</el-radio-button>
            <el-radio-button label="TEACHER">教师</el-radio-button>
            <el-radio-button label="STUDENT">学生</el-radio-button>
          </el-radio-group>
        </el-form-item>

        <div class="actions">
          <el-checkbox v-model="rememberMe" label="记住我" style="color: var(--text-main)" />
          <el-link type="primary" :underline="false">忘记密码？</el-link>
        </div>

        <el-form-item>
          <el-button type="primary" :loading="loading" class="submit-btn" @click="handleLogin" round>
            立即登录
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="footer">
        <span>还没有账号？</span>
        <el-link type="primary" @click="router.push('/register')">去注册</el-link>
      </div>
      
      <div class="tips">
        <p>演示模式：选择角色后点击登录即可（任意密码）</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../store/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const rememberMe = ref(false)
const selectedRole = ref('ADMIN')

const form = reactive({
  username: 'admin',
  password: '123'
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const formRef = ref()

const handleLogin = async () => {
  if (!formRef.value) return
  await formRef.value.validate((valid: boolean) => {
    if (valid) {
      loading.value = true
      // Mock Login
      setTimeout(() => {
        loading.value = false
        // Determine user based on role selection
        let user = { id: 0, name: '', role: '' }
        if (selectedRole.value === 'ADMIN') {
          user = { id: 1, name: 'Admin', role: 'ADMIN' }
        } else if (selectedRole.value === 'TEACHER') {
          user = { id: 2, name: '张老师', role: 'TEACHER' }
        } else {
          user = { id: 3, name: '李同学', role: 'STUDENT' }
        }

        userStore.setToken('mock-token-' + user.role)
        userStore.setUserInfo(user)
        ElMessage.success(`登录成功，欢迎 ${user.name} (${user.role})`)
        router.push('/dashboard')
      }, 800)
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
    color: var(--text-main);
    font-weight: 700;
  }

  p {
    margin: 0;
    color: var(--text-regular);
    font-size: 14px;
  }
}

.login-form {
  width: 100%;
}

.actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
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
  color: var(--text-regular);
  display: flex;
  gap: 8px;
  align-items: center;
}

.tips {
  margin-top: 30px;
  padding: 10px;
  background: rgba(64, 158, 255, 0.1);
  border-radius: 8px;
  width: 100%;
  text-align: center;
  
  p {
    margin: 0;
    font-size: 12px;
    color: var(--primary-color);
  }
}
</style>
