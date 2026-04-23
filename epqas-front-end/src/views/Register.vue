<template>
  <div class="register-container">
    <div class="register-box">
      <div class="register-header">
        <div class="logo-area">
          <el-icon :size="40" color="#409EFF"><Platform /></el-icon>
          <span class="title">创建账号</span>
        </div>
        <p class="subtitle">加入题析</p>
      </div>
      
      <el-form :model="registerForm" :rules="rules" ref="registerFormRef" size="large" class="register-form" label-width="0">
        <el-form-item prop="username">
          <el-input 
            v-model="registerForm.username" 
            placeholder="工号/学号" 
            :prefix-icon="User"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input 
            v-model="registerForm.password" 
            type="password" 
            show-password 
            placeholder="密码"
            :prefix-icon="Lock"
          />
        </el-form-item>
        <el-form-item prop="realName">
          <el-input 
            v-model="registerForm.realName" 
            placeholder="真实姓名"
            :prefix-icon="Avatar"
          />
        </el-form-item>
        <el-form-item prop="email">
          <el-input 
            v-model="registerForm.email" 
            placeholder="邮箱"
            :prefix-icon="Message"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleRegister" :loading="loading" class="register-button">
            创建账号
          </el-button>
        </el-form-item>
        
        <div class="form-footer">
          <span class="text">已有账号？</span>
          <el-button link type="primary" @click="$router.push('/login')">去登录</el-button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance } from 'element-plus'
import { register } from '../api/auth'
import { User, Lock, Platform, Avatar, Message } from '@element-plus/icons-vue'

const router = useRouter()
const registerFormRef = ref<FormInstance>()
const loading = ref(false)

const registerForm = reactive({
  username: '',
  password: '',
  realName: '',
  email: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  email: [
      { required: true, message: '请输入邮箱', trigger: 'blur' },
      { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ]
}

const handleRegister = async () => {
  if (!registerFormRef.value) return
  
  await registerFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await register({
          username: registerForm.username,
          passwordHash: registerForm.password, // Mapping to partial User entity
          realName: registerForm.realName,
          email: registerForm.email
        })
        ElMessage.success('注册成功，请登录')
        router.push('/login')
      } catch (e) {
        // Handled by request interceptor
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  background-image: url('https://images.unsplash.com/photo-1497294815431-9365093b7331?ixlib=rb-1.2.1&auto=format&fit=crop&w=1950&q=80');
  background-size: cover;
  background-position: center;
  position: relative;
}

.register-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.85); /* White overlay/tint */
  backdrop-filter: blur(5px);
}

.register-box {
  width: 450px;
  padding: 40px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  position: relative;
  z-index: 1;
  animation: slideUp 0.6s ease-out;
}

.register-header {
  text-align: center;
  margin-bottom: 30px;
}

.logo-area {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  margin-bottom: 5px;
}

.title {
  font-size: 24px;
  font-weight: 700;
  color: #2c3e50;
}

.subtitle {
  color: #606266;
  font-size: 14px;
  margin: 0;
}

.register-form {
  margin-top: 20px;
}

.register-button {
  width: 100%;
  font-weight: 600;
  letter-spacing: 0.5px;
  height: 40px;
  font-size: 16px;
  background: linear-gradient(90deg, #409EFF, #3a8ee6);
  border: none;
  transition: transform 0.2s, box-shadow 0.2s;
}

.register-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
}

.form-footer {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 20px;
  gap: 5px;
}

.text {
  color: #909399;
  font-size: 14px;
}

/* Animations */
@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

:deep(.el-input__wrapper) {
  box-shadow: 0 0 0 1px #dcdfe6 inset;
  transition: box-shadow 0.3s, transform 0.3s;
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #409EFF inset;
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px #409EFF inset !important;
  transform: scale(1.01);
}
</style>
