<template>
  <div class="home">
    <el-container>
      <el-header>
        <div class="header-content">
          <h2>EPQAS Dashboard</h2>
          <el-button type="danger" @click="logout">Logout</el-button>
        </div>
      </el-header>
      <el-main>
        <el-card>
          <h3>Welcome to the Examination Paper Quality Analysis System</h3>
          <p>Select a module from the menu or use the navigation above.</p>
        </el-card>
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()

const logout = () => {
  localStorage.removeItem('token')
  ElMessage.success('Logged out successfully')
  router.push('/login')
}
</script>

<style scoped>
.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 60px;
  background-color: #409EFF;
  color: white;
  padding: 0 20px;
}
</style>
