<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <div class="card-header">
          <span>EPQAS Login</span>
        </div>
      </template>
      <el-form :model="loginForm" :rules="rules" ref="loginFormRef" label-width="80px">
        <el-form-item label="Username" prop="username">
          <el-input v-model="loginForm.username" placeholder="Enter valid username"></el-input>
        </el-form-item>
        <el-form-item label="Password" prop="password">
          <el-input v-model="loginForm.password" type="password" placeholder="Enter password" show-password></el-input>
        </el-form-item>
        <el-form-item label="Role" prop="roleId">
          <el-select v-model="loginForm.roleId" placeholder="Select Identity">
            <el-option v-for="role in roleList" :key="role.roleId" :label="role.roleName" :value="role.roleId"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin" :loading="loading">Login</el-button>
          <el-button @click="$router.push('/register')">Register</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance } from 'element-plus'
import { login, getRoles } from '../api/auth'

const router = useRouter()
const loginFormRef = ref<FormInstance>()
const loading = ref(false)
const roleList = ref<any[]>([])

const loginForm = reactive({
  username: '',
  password: '',
  roleId: null
})

const rules = {
  username: [{ required: true, message: 'Please input username', trigger: 'blur' }],
  password: [{ required: true, message: 'Please input password', trigger: 'blur' }],
  roleId: [{ required: true, message: 'Please select role', trigger: 'change' }]
}

onMounted(async () => {
    try {
        const res = await getRoles()
        roleList.value = res.data
    } catch (e) {
        ElMessage.error('Failed to load roles')
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
        localStorage.setItem('token', res.data)
        ElMessage.success('Login successful')
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
  background-color: #f0f2f5;
}
.login-card {
  width: 400px;
}
.card-header {
  text-align: center;
  font-weight: bold;
}
</style>
