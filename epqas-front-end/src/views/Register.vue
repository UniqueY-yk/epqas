<template>
  <div class="register-container">
    <el-card class="register-card">
      <template #header>
        <div class="card-header">
          <span>EPQAS Registration</span>
        </div>
      </template>
      <el-form :model="registerForm" :rules="rules" ref="registerFormRef" label-width="100px">
        <el-form-item label="Username" prop="username">
          <el-input v-model="registerForm.username" placeholder="Used for login"></el-input>
        </el-form-item>
        <el-form-item label="Password" prop="password">
          <el-input v-model="registerForm.password" type="password" show-password></el-input>
        </el-form-item>
        <el-form-item label="Full Name" prop="realName">
          <el-input v-model="registerForm.realName" placeholder="Your actual name"></el-input>
        </el-form-item>
        <el-form-item label="Email" prop="email">
          <el-input v-model="registerForm.email"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleRegister" :loading="loading">Register</el-button>
          <el-button @click="$router.push('/login')">Back to Login</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance } from 'element-plus'
import { register } from '../api/auth'

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
  username: [{ required: true, message: 'Required', trigger: 'blur' }],
  password: [{ required: true, message: 'Required', trigger: 'blur' }],
  realName: [{ required: true, message: 'Required', trigger: 'blur' }]
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
        ElMessage.success('Registration successful, please login')
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
  background-color: #f0f2f5;
}
.register-card {
  width: 500px;
}
.card-header {
  text-align: center;
  font-weight: bold;
}
</style>
