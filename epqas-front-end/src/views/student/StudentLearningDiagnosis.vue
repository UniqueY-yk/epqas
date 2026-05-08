<template>
  <div class="learning-diagnosis">
    <div class="page-header">
      <h2>学习薄弱点诊断</h2>
      <p class="subtitle">基于您参与过的所有考试记录，AI智能分析您的知识链条掌握情况</p>
    </div>

    <!-- Overview Statistics -->
    <div class="overview-cards" v-if="masteryData.length > 0">
      <el-card shadow="hover" class="stat-card">
        <div class="stat-icon excellent-icon"><el-icon><StarFilled /></el-icon></div>
        <div class="stat-content">
          <div class="stat-title">优势知识点</div>
          <div class="stat-value text-excellent">{{ excellentCount }} <span class="stat-unit">个</span></div>
        </div>
      </el-card>

      <el-card shadow="hover" class="stat-card">
        <div class="stat-icon warning-icon"><el-icon><WarningFilled /></el-icon></div>
        <div class="stat-content">
          <div class="stat-title">有待加强</div>
          <div class="stat-value text-warning">{{ goodCount }} <span class="stat-unit">个</span></div>
        </div>
      </el-card>

      <el-card shadow="hover" class="stat-card">
        <div class="stat-icon danger-icon"><el-icon><Histogram /></el-icon></div>
        <div class="stat-content">
          <div class="stat-title">薄弱知识点</div>
          <div class="stat-value text-danger">{{ weakCount }} <span class="stat-unit">个</span></div>
        </div>
      </el-card>

      <el-card shadow="hover" class="stat-card">
        <div class="stat-icon total-icon"><el-icon><Collection /></el-icon></div>
        <div class="stat-content">
          <div class="stat-title">评估范围</div>
          <div class="stat-value">{{ masteryData.length }} <span class="stat-unit">个考点</span></div>
        </div>
      </el-card>
    </div>

    <el-row :gutter="24" v-if="masteryData.length > 0" class="main-content-row">
      <!-- Left: Radar Chart -->
      <el-col :span="10">
        <el-card shadow="never" class="radar-card" v-loading="loading">
          <template #header><strong>🎯 知识画像雷达</strong></template>
          <div class="chart-container" ref="radarChartRef"></div>
        </el-card>
      </el-col>

      <!-- Right: Weakness Leaderboard -->
      <el-col :span="14">
        <el-card shadow="never" class="leaderboard-card" v-loading="loading">
          <template #header><strong>📉 知识薄弱点突破建议</strong></template>
          
          <div class="weakness-list">
            <div v-for="(item, index) in masteryData" :key="item.pointId" class="weakness-item" :class="item.level">
              <div class="item-rank" :class="'rank-' + (index + 1)">#{{ index + 1 }}</div>
              
              <div class="item-core">
                <div class="item-title">{{ item.pointName }}</div>
                <div class="item-meta">
                  <span>相关考题: <strong>{{ item.totalQuestions }}</strong> 道</span>
                  <el-divider direction="vertical" />
                  <span>答对: <strong>{{ item.correctCount }}</strong> 道</span>
                </div>
                
                <div class="item-ai-feedback">
                  <el-icon><ChatLineRound /></el-icon>
                  <span v-if="item.level === 'weak'">建议：该知识点失分严重。强烈建议重新复习教材相关章节，并进行专项强化训练。</span>
                  <span v-else-if="item.level === 'good'">建议：基本掌握，但熟练度不足。建议复习错题本，查漏补缺。</span>
                  <span v-else>建议：掌握非常扎实。继续保持！</span>
                </div>
              </div>

              <div class="item-score">
                <el-progress 
                  type="dashboard" 
                  :percentage="Math.round(item.masteryRate * 100)" 
                  :color="levelColor(item.level)"
                  :width="70"
                >
                  <template #default="{ percentage }">
                    <span class="percentage-value">{{ percentage }}%</span>
                  </template>
                </el-progress>
                <div class="level-label" :class="'text-' + item.level">{{ levelLabel(item.level) }}</div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Empty State -->
    <el-empty 
      v-if="!loading && masteryData.length === 0" 
      description="暂时没有足够的考试数据来生成诊断报告，请多参加考试积累数据" 
      image-size="200"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, nextTick } from 'vue'
import { getStudentKnowledgeMastery } from '@/api/exam'
import { ElMessage } from 'element-plus'
import { StarFilled, WarningFilled, Histogram, Collection, ChatLineRound } from '@element-plus/icons-vue'
import * as echarts from 'echarts'

const loading = ref(false)
const masteryData = ref<any[]>([])
const radarChartRef = ref<HTMLElement | null>(null)
let radarChart: echarts.ECharts | null = null

const userId = Number(localStorage.getItem('userId') || '0')

// Stats
const weakCount = computed(() => masteryData.value.filter(d => d.level === 'weak').length)
const goodCount = computed(() => masteryData.value.filter(d => d.level === 'good').length)
const excellentCount = computed(() => masteryData.value.filter(d => d.level === 'excellent').length)

const levelColor = (level: string) => {
  if (level === 'excellent') return '#67c23a'
  if (level === 'good') return '#e6a23c'
  return '#f56c6c'
}

const levelLabel = (level: string) => {
  if (level === 'excellent') return '优秀'
  if (level === 'good') return '一般'
  return '薄弱'
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getStudentKnowledgeMastery(userId)
    // The backend sorts by lowest mastery first
    masteryData.value = res.data || []
    await nextTick()
    renderRadarChart()
  } catch (e) {
    console.error(e)
    ElMessage.error('无法加载知识点诊断报告')
  } finally {
    loading.value = false
  }
}

const renderRadarChart = () => {
  if (!radarChartRef.value || masteryData.value.length === 0) return

  radarChart?.dispose()
  radarChart = echarts.init(radarChartRef.value)

  // Radar chart needs a minimum of 3 points usually to draw a polygon. If <3, Echarts handles it but looks weird.
  // We pick up to 10 points to avoid crowding the radar
  const radarPoints = [...masteryData.value].sort((a: any, b: any) => b.masteryRate - a.masteryRate).slice(0, 10)

  const indicators = radarPoints.map((d: any) => {
    let name = d.pointName
    if (name.length > 6) name = name.substring(0, 6) + '...'
    return { name, max: 100 }
  })
  const values = radarPoints.map((d: any) => Math.round(d.masteryRate * 100))

  radarChart.setOption({
    tooltip: { trigger: 'item' },
    radar: {
      indicator: indicators,
      shape: 'circle',
      splitNumber: 5,
      axisName: { color: '#303133', fontSize: 13, fontWeight: 'bold' },
      splitArea: {
        areaStyle: {
          color: [
            'rgba(245, 108, 108, 0.1)',
            'rgba(245, 108, 108, 0.05)',
            'rgba(230, 162, 60, 0.1)',
            'rgba(103, 194, 58, 0.1)',
            'rgba(103, 194, 58, 0.2)'
          ]
        }
      },
      axisLine: { lineStyle: { color: 'rgba(238, 238, 238, 0.6)' } },
      splitLine: { lineStyle: { color: 'rgba(238, 238, 238, 0.6)' } }
    },
    series: [{
      type: 'radar',
      name: '知识掌握率',
      data: [{
        value: values,
        name: '掌握率 (%)',
        symbol: 'circle',
        symbolSize: 8,
        lineStyle: { color: '#409eff', width: 3 },
        itemStyle: { color: '#409eff', borderColor: '#fff', borderWidth: 2 },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(64, 158, 255, 0.5)' },
            { offset: 1, color: 'rgba(64, 158, 255, 0.1)' }
          ])
        }
      }]
    }]
  })
}

// Handle resize resize
window.addEventListener('resize', () => {
  radarChart?.resize()
})

onMounted(fetchData)
</script>

<style scoped>
.learning-diagnosis {
  padding: 24px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.page-header {
  margin-bottom: 24px;
}

.page-header h2 {
  margin: 0;
  font-size: 26px;
  color: #303133;
  font-weight: 700;
}

.subtitle {
  margin: 8px 0 0;
  color: #909399;
  font-size: 15px;
}

/* Overview Cards */
.overview-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  border-radius: 12px;
  border: none;
}

.stat-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  padding: 24px;
  gap: 20px;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 28px;
}

.excellent-icon { background: #f0f9eb; color: #67c23a; }
.warning-icon { background: #fdf6ec; color: #e6a23c; }
.danger-icon { background: #fef0f0; color: #f56c6c; }
.total-icon { background: #ecf5ff; color: #409eff; }

.stat-content {
  flex: 1;
}

.stat-title {
  color: #909399;
  font-size: 14px;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: 800;
  line-height: 1;
}

.stat-unit {
  font-size: 14px;
  font-weight: normal;
  color: #606266;
}

.text-excellent { color: #303133; }
.text-warning { color: #303133; }
.text-danger { color: #f56c6c; }

/* Main Content */
.main-content-row {
  align-items: stretch;
}

.radar-card, .leaderboard-card {
  border-radius: 12px;
  height: 100%;
}

.chart-container {
  height: 500px;
  width: 100%;
}

/* Weakness Leaderboard */
.weakness-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  max-height: 500px;
  overflow-y: auto;
  padding-right: 8px;
}

.weakness-list::-webkit-scrollbar {
  width: 6px;
}

.weakness-list::-webkit-scrollbar-thumb {
  background-color: #dcdfe6;
  border-radius: 3px;
}

.weakness-item {
  display: flex;
  align-items: center;
  padding: 20px;
  border-radius: 12px;
  background: #fff;
  border: 1px solid #ebeef5;
  transition: all 0.3s;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.02);
}

.weakness-item:hover {
  transform: translateX(4px);
  box-shadow: 0 4px 16px 0 rgba(0,0,0,0.06);
}

/* Highlight specifically the weakest items */
.weakness-item.weak {
  border-left: 4px solid #f56c6c;
  background: linear-gradient(90deg, #fffaf9 0%, #ffffff 100%);
}

.weakness-item.good {
  border-left: 4px solid #e6a23c;
}

.weakness-item.excellent {
  border-left: 4px solid #67c23a;
}

.item-rank {
  font-size: 24px;
  font-weight: 800;
  color: #dcdfe6;
  min-width: 45px;
  font-style: italic;
}

.rank-1, .rank-2, .rank-3 {
  color: #f56c6c;
}

.item-core {
  flex: 1;
  padding: 0 20px;
}

.item-title {
  font-size: 18px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 8px;
}

.item-meta {
  font-size: 13px;
  color: #606266;
  margin-bottom: 12px;
}

.item-ai-feedback {
  background: #f4f4f5;
  padding: 10px 14px;
  border-radius: 6px;
  font-size: 13px;
  color: #606266;
  display: flex;
  align-items: flex-start;
  gap: 8px;
  line-height: 1.5;
}

.weakness-item.weak .item-ai-feedback {
  background: #fef0f0;
  color: #f56c6c;
}

.item-score {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-width: 90px;
}

.percentage-value {
  font-size: 16px;
  font-weight: 700;
  color: #303133;
}

.level-label {
  margin-top: -10px;
  font-size: 12px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 10px;
  background: #f4f4f5;
}

.level-label.text-excellent { color: #67c23a; background: #f0f9eb; }
.level-label.text-good { color: #e6a23c; background: #fdf6ec; }
.level-label.text-weak { color: #f56c6c; background: #fef0f0; }

</style>
