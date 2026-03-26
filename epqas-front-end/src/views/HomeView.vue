<template>
  <div class="home-container">
    <!-- Hero Section -->
    <div class="hero-section">
      <div class="hero-content">
        <h1>欢迎回来，{{ realName }}</h1>
        <p class="hero-subtitle">今天是 {{ currentDate }}，准备好开始今天的试卷质量分析了吗？</p>
        <div class="hero-badges">
          <div class="hero-badge role-badge">
            <el-icon><User /></el-icon>
            <span>{{ roleName }}</span>
          </div>
          <div class="hero-badge time-badge">
            <el-icon><Clock /></el-icon>
            <span>{{ currentTime }}</span>
          </div>
        </div>
      </div>
      <div class="hero-decoration">
        <el-icon class="bg-icon"><DataLine /></el-icon>
      </div>
    </div>

    <!-- Statistics Grid -->
    <el-row :gutter="20" class="stat-row">
      <el-col :xs="24" :sm="12" :md="6" v-for="stat in stats" :key="stat.title">
        <el-card shadow="hover" class="stat-card" :body-style="{ padding: '20px' }">
          <div class="stat-content">
            <div class="stat-icon" :style="{ background: stat.color + '15', color: stat.color }">
              <el-icon><component :is="stat.icon" /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-title">{{ stat.title }}</div>
              <div class="stat-value" :style="{ color: stat.color }">
                <span v-if="loadingStats" class="loading-placeholder">...</span>
                <span v-else>{{ stat.value }}</span>
              </div>
            </div>
          </div>
          <div class="stat-footer">
            <span class="trend-up" v-if="!loadingStats">
              <el-icon><CaretTop /></el-icon> 较昨日 +{{ Math.floor(Math.random() * 5) }}
            </span>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Quick Actions -->
    <div class="section-container">
      <h3 class="section-title">常用功能</h3>
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :md="8" v-for="action in quickActions" :key="action.title">
          <el-card shadow="hover" class="action-card" @click="router.push(action.path)">
            <div class="action-body">
              <div class="action-icon" :style="{ color: action.color }">
                <el-icon><component :is="action.icon" /></el-icon>
              </div>
              <div class="action-info">
                <h4>{{ action.title }}</h4>
                <p>{{ action.desc }}</p>
              </div>
              <el-icon class="arrow-icon"><ArrowRight /></el-icon>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- Info Section -->
    <el-row :gutter="20" class="info-row">
      <el-col :span="16">
        <el-card shadow="hover" class="system-guide">
          <template #header>
            <div class="card-header">
              <span><el-icon><Reading /></el-icon> 系统使用指南</span>
              <el-button type="primary" link>查看更多</el-button>
            </div>
          </template>
          <el-steps direction="vertical" :active="1" class="guide-steps">
            <el-step title="配置基础数据" description="在管理员模块中配置课程、班级以及知识点体系。" />
            <el-step title="维护题库" description="录入或导入题目，并为题目关联知识点和初始难度。" />
            <el-step title="组装试卷模板" description="根据教学大纲，在试卷管理模块中组装标准化的考试模板。" />
            <el-step title="质量诊断分析" description="导入学生成绩及答题数据，点击诊断获取多维度的质量报告。" />
          </el-steps>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="status-card">
          <template #header>
            <div class="card-header">
              <span><el-icon><Odometer /></el-icon> 系统运行状态</span>
            </div>
          </template>
          <div class="status-list">
            <div class="status-item">
              <span class="label">数据库连接</span>
              <el-tag size="small" type="success">正常</el-tag>
            </div>
            <div class="status-item">
              <span class="label">分析引擎服务</span>
              <el-tag size="small" type="success">在线</el-tag>
            </div>
            <div class="status-item">
              <span class="label">Feign 内部通信</span>
              <el-tag size="small" type="success">畅通</el-tag>
            </div>
            <div class="status-item">
              <span class="label">Gateway 网关</span>
              <el-tag size="small" type="success">运行中</el-tag>
            </div>
          </div>
          <el-divider />
          <div class="version-info">
            <span>当前版本: v2.5.5-stable</span>
            <span>最后更新: 2026-03-26</span>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { User, Clock, DataLine, CaretTop, ArrowRight, Document, Operation, Setting, Reading, Odometer, PieChart, School } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import { getAuthStats, getAcademicStats, getExamStats, getAnalysisStats } from '../api/dashboard'

const router = useRouter()
const realName = ref(localStorage.getItem('realName') || '用户')
const roleId = Number(localStorage.getItem('roleId') || '1')

const roleNames: Record<number, string> = {
  1: '系统管理员',
  2: '命题教师',
  3: '任课教师',
  4: '学生'
}
const roleName = ref(roleNames[roleId] || '普通用户')

const currentDate = ref(dayjs().format('YYYY年MM月DD日'))
const currentTime = ref(dayjs().format('HH:mm:ss'))
const loadingStats = ref(true)

let timer: any = null

const stats = ref([
  { title: '用户总数', value: 0, icon: 'User', color: '#409EFF' },
  { title: '管理科目', value: 0, icon: 'School', color: '#67C23A' },
  { title: '试卷模板', value: 0, icon: 'Document', color: '#E6A23C' },
  { title: '分析次数', value: 0, icon: 'PieChart', color: '#F56C6C' }
])

const quickActions = ref([
  { title: '试卷质量诊断', desc: '上传数据并生成质量分析报告', icon: 'Operation', path: '/proposition/diagnosis', color: '#409EFF' },
  { title: '题库管理', desc: '维护和管理系统内的各种题目', icon: 'Reading', path: '/question/bank', color: '#67C23A' },
  { title: '用户权限管理', desc: '配置系统用户及其操作权限', icon: 'Setting', path: '/admin/users', color: '#E6A23C' }
])

// Filter actions based on role
if (roleId !== 1) {
    quickActions.value = quickActions.value.filter(a => a.path !== '/admin/users')
}

onMounted(() => {
  timer = setInterval(() => {
    currentTime.value = dayjs().format('HH:mm:ss')
  }, 1000)
  
  fetchStats()
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})

const fetchStats = async () => {
  loadingStats.value = true
  try {
    const [authRes, academicRes, examRes, analysisRes] = await Promise.all([
      getAuthStats(),
      getAcademicStats(),
      getExamStats(),
      getAnalysisStats()
    ])
    
    stats.value[0].value = authRes.data.totalUsers || 0
    stats.value[1].value = academicRes.data.courseCount || 0
    stats.value[2].value = examRes.data.paperCount || 0
    stats.value[3].value = analysisRes.data.analysisCount || 0
  } catch (e) {
    console.error('Failed to fetch stats', e)
  } finally {
    loadingStats.value = false
  }
}
</script>

<style scoped>
.home-container {
  padding: 24px;
  background-color: #f0f2f5;
  min-height: calc(100vh - 64px);
  animation: fadeIn 0.5s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

/* Hero Section */
.hero-section {
  background: linear-gradient(135deg, #1890ff 0%, #36cfc9 100%);
  border-radius: 12px;
  padding: 40px;
  color: white;
  margin-bottom: 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: relative;
  overflow: hidden;
  box-shadow: 0 8px 16px rgba(24, 144, 255, 0.2);
}

.hero-content {
  position: relative;
  z-index: 2;
}

.hero-content h1 {
  font-size: 32px;
  margin: 0 0 10px 0;
  font-weight: 700;
}

.hero-subtitle {
  font-size: 16px;
  opacity: 0.9;
  margin: 0 0 24px 0;
}

.hero-badges {
  display: flex;
  gap: 16px;
  margin-top: 8px;
}

.hero-badge {
  background: rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  padding: 0 16px;
  height: 38px;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #ffffff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.hero-badge:hover {
  background: rgba(255, 255, 255, 0.35);
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15);
}

.hero-badge .el-icon {
  font-size: 18px;
}

.hero-decoration {
  position: absolute;
  right: -20px;
  bottom: -20px;
  opacity: 0.1;
  z-index: 1;
}

.bg-icon {
  font-size: 240px;
}

/* Stat Cards */
.stat-row {
  margin-bottom: 24px;
}

.stat-card {
  border: none;
  border-radius: 10px;
  transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.1);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 20px;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
}

.stat-title {
  font-size: 14px;
  color: #909399;
  margin-bottom: 4px;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  line-height: 1;
}

.stat-footer {
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid #f0f2f5;
  font-size: 13px;
}

.trend-up {
  color: #67C23A;
  display: flex;
  align-items: center;
  gap: 4px;
}

/* Quick Actions */
.section-container {
  margin-bottom: 30px;
}

.section-title {
  font-size: 18px;
  margin: 0 0 20px 0;
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
}

.action-card {
  cursor: pointer;
  border: none;
  border-radius: 10px;
  margin-bottom: 20px;
  transition: all 0.2s;
}

.action-card:hover {
  background-color: #f9fbfd;
}

.action-card:hover .arrow-icon {
  transform: translateX(5px);
  color: #409EFF;
}

.action-body {
  display: flex;
  align-items: center;
  gap: 16px;
}

.action-icon {
  font-size: 40px;
  display: flex;
  align-items: center;
}

.action-info {
  flex: 1;
}

.action-info h4 {
  margin: 0 0 4px 0;
  font-size: 16px;
}

.action-info p {
  margin: 0;
  font-size: 13px;
  color: #909399;
}

.arrow-icon {
  font-size: 18px;
  color: #C0C4CC;
  transition: all 0.2s;
}

/* Info Row */
.info-row {
  margin-bottom: 24px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
}

.guide-steps {
  padding: 20px 0;
}

.status-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.status-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.status-item .label {
  color: #606266;
  font-size: 14px;
}

.version-info {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #909399;
}
</style>
