<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <div class="logo-area">
          <el-icon :size="40" color="#409EFF"><Platform /></el-icon>
          <span class="title">题析登录</span>
        </div>
        <p class="subtitle">题析</p>
      </div>
      
      <el-form :model="loginForm" :rules="rules" ref="loginFormRef" size="large" class="login-form">
        <el-form-item prop="username">
          <el-input 
            v-model="loginForm.username" 
            placeholder="工号/学号" 
            :prefix-icon="User"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input 
            v-model="loginForm.password" 
            type="password" 
            placeholder="密码" 
            show-password 
            :prefix-icon="Lock"
          />
        </el-form-item>
        <el-form-item prop="roleId">
          <el-select v-model="loginForm.roleId" placeholder="选择身份" style="width: 100%">
            <template #prefix>
              <el-icon><UserFilled /></el-icon>
            </template>
            <el-option v-for="role in roleList" :key="role.roleId" :label="role.roleName" :value="role.roleId"></el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleLogin" :loading="loading" class="login-button">
            登录
          </el-button>
        </el-form-item>
        
        <div class="form-footer">
          <span class="text">没有账号？</span>
          <el-button link type="primary" @click="$router.push('/register')">立即注册</el-button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance } from 'element-plus'
import { login, getRoles } from '../api/auth'
import { User, Lock, Platform, UserFilled } from '@element-plus/icons-vue'

const router = useRouter()
const loginFormRef = ref<FormInstance>()
const loading = ref(false)
const roleList = ref<any[]>([])

const loginForm = reactive({
  username: '',
  password: '',
  roleId: null as number | null
})

const rules = {
  username: [{ required: true, message: '请输入工号/学号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  roleId: [{ required: true, message: '请选择身份', trigger: 'change' }]
}

onMounted(async () => {
    try {
        const res = await getRoles()
        roleList.value = res.data
    } catch (e) {
        ElMessage.error('角色加载失败')
    }
})

const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res = await login({
           username: loginForm.username,
           passwordHash: loginForm.password,
           roleId: loginForm.roleId
        })
        // res.data is now { token, userId, realName }
        const loginData = res.data
        localStorage.setItem('token', loginData.token || loginData)
        if (loginData.userId) {
            localStorage.setItem('userId', loginData.userId.toString())
        }
        if (loginData.realName) {
            localStorage.setItem('realName', loginData.realName)
        }
        if (loginForm.roleId) {
            localStorage.setItem('roleId', loginForm.roleId.toString())
        }
        ElMessage.success('登录成功')
        router.push('/')
      } catch (e) {
        // Error handled in interceptor
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.login-container {
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

.login-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.85); /* White overlay/tint */
  backdrop-filter: blur(5px);
}

.login-box {
  width: 400px;
  padding: 40px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  position: relative;
  z-index: 1;
  animation: slideUp 0.6s ease-out;
}

.login-header {
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

.login-form {
  margin-top: 20px;
}

.login-button {
  width: 100%;
  font-weight: 600;
  letter-spacing: 0.5px;
  height: 40px;
  font-size: 16px;
  background: linear-gradient(90deg, #409EFF, #3a8ee6);
  border: none;
  transition: transform 0.2s, box-shadow 0.2s;
}

.login-button:hover {
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
