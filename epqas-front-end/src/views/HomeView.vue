<template>
  <div class="home">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>用户总数</span>
            </div>
          </template>
          <div class="card-value">1,234</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>活跃考试数</span>
            </div>
          </template>
          <div class="card-value">56</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>已分析试卷数</span>
            </div>
          </template>
          <div class="card-value">892</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
                <span>系统状态</span>
            </div>
          </template>
          <div class="card-value status-ok">正常</div>
        </el-card>
      </el-col>
    </el-row>

    <br/>

    <el-card class="welcome-card">
      <h3>欢迎来到EPQAS</h3>
      <p>使用侧边栏导航到不同的管理模块。</p>
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
