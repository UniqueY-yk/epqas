<template>
  <div class="home">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>Total Users</span>
            </div>
          </template>
          <div class="card-value">1,234</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>Active Exams</span>
            </div>
          </template>
          <div class="card-value">56</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>Papers Analyzed</span>
            </div>
          </template>
          <div class="card-value">892</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
                <span>System Status</span>
            </div>
          </template>
          <div class="card-value status-ok">Normal</div>
        </el-card>
      </el-col>
    </el-row>

    <br/>

    <el-card class="welcome-card">
      <h3>Welcome to EPQAS</h3>
      <p>Use the sidebar to navigate to different management modules.</p>
    </el-card>
  </div>
</template>

<script setup lang="ts">
</script>

<style scoped>
.card-value {
  font-size: 24px;
  font-weight: bold;
  text-align: center;
  color: #409EFF;
}
.status-ok {
    color: #67C23A;
}
</style>
