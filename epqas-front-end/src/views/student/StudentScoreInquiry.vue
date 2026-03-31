<template>
  <div class="score-inquiry">
    <div class="page-header">
      <h2>我的成绩</h2>
      <p class="subtitle">查看您在各次考试中的成绩与考试状态</p>
    </div>

    <!-- Summary Cards -->
    <div class="summary-grid" v-if="scoreList.length > 0">
      <el-card shadow="never" class="summary-card">
        <div class="summary-icon" style="background: #ecf5ff; color: #409eff;">
          <el-icon :size="28"><Document /></el-icon>
        </div>
        <div class="summary-info">
          <span class="summary-value">{{ scoreList.length }}</span>
          <span class="summary-label">已参加考试</span>
        </div>
      </el-card>

      <el-card shadow="never" class="summary-card">
        <div class="summary-icon" style="background: #f0f9eb; color: #67c23a;">
          <el-icon :size="28"><TrendCharts /></el-icon>
        </div>
        <div class="summary-info">
          <span class="summary-value">{{ avgScore }}</span>
          <span class="summary-label">平均分</span>
        </div>
      </el-card>

      <el-card shadow="never" class="summary-card">
        <div class="summary-icon" style="background: #fdf6ec; color: #e6a23c;">
          <el-icon :size="28"><Trophy /></el-icon>
        </div>
        <div class="summary-info">
          <span class="summary-value">{{ highestScore }}</span>
          <span class="summary-label">最高分</span>
        </div>
      </el-card>

      <el-card shadow="never" class="summary-card">
        <div class="summary-icon" style="background: #fef0f0; color: #f56c6c;">
          <el-icon :size="28"><Warning /></el-icon>
        </div>
        <div class="summary-info">
          <span class="summary-value">{{ absentCount }}</span>
          <span class="summary-label">缺考次数</span>
        </div>
      </el-card>
    </div>

    <!-- Score Trend Chart -->
    <el-card shadow="never" class="chart-card" v-if="validScores.length >= 2" style="margin-top: 20px;">
      <template #header><strong>📈 成绩趋势</strong></template>
      <div ref="trendChartRef" style="height: 300px;"></div>
    </el-card>

    <!-- Score Table -->
    <el-card shadow="never" class="table-card" style="margin-top: 20px;" v-loading="loading">
      <template #header>
        <div class="card-header-row">
          <strong>📋 考试成绩明细</strong>
          <el-tag v-if="scoreList.length > 0" type="info" size="small">共 {{ scoreList.length }} 条记录</el-tag>
        </div>
      </template>

      <el-table :data="scoreList" stripe border style="width: 100%;" empty-text="暂无考试成绩记录">
        <el-table-column label="序号" type="index" width="60" align="center" />
        <el-table-column label="考试名称" min-width="200">
          <template #default="{ row }">
            <span class="exam-name">{{ getExamName(row.examId) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="考试日期" width="140" align="center">
          <template #default="{ row }">
            {{ getExamDate(row.examId) }}
          </template>
        </el-table-column>
        <el-table-column label="总成绩" width="120" align="center">
          <template #default="{ row }">
            <template v-if="row.isAbsent">
              <el-tag type="info" effect="dark" size="small">缺考</el-tag>
            </template>
            <template v-else>
              <span class="score-value" :class="getScoreClass(row.totalScore)">{{ row.totalScore }}</span>
            </template>
          </template>
        </el-table-column>
        <el-table-column label="等级" width="100" align="center">
          <template #default="{ row }">
            <template v-if="!row.isAbsent">
              <el-tag :type="getGradeType(row.totalScore)" effect="light">{{ getGradeLabel(row.totalScore) }}</el-tag>
            </template>
            <template v-else>
              <el-tag type="info" effect="plain">—</el-tag>
            </template>
          </template>
        </el-table-column>
        <el-table-column label="提交时间" width="180" align="center">
          <template #default="{ row }">
            {{ row.submittedAt ? row.submittedAt.substring(0, 19).replace('T', ' ') : '—' }}
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && scoreList.length === 0" description="您还没有参加过考试" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, nextTick } from 'vue'
import { getStudentScores, getExaminationsPage, getPapers } from '@/api/exam'
import { getClasses } from '@/api/academic'
import { ElMessage } from 'element-plus'
import { Document, TrendCharts, Trophy, Warning } from '@element-plus/icons-vue'
import * as echarts from 'echarts'

const loading = ref(false)
const scoreList = ref<any[]>([])
const examList = ref<any[]>([])
const paperList = ref<any[]>([])
const classList = ref<any[]>([])

const trendChartRef = ref<HTMLElement | null>(null)
let trendChart: echarts.ECharts | null = null

const userId = Number(localStorage.getItem('userId') || '0')

const validScores = computed(() => scoreList.value.filter(s => !s.isAbsent && s.totalScore != null))

const avgScore = computed(() => {
  if (validScores.value.length === 0) return '—'
  const sum = validScores.value.reduce((acc: number, s: any) => acc + Number(s.totalScore), 0)
  return (sum / validScores.value.length).toFixed(1)
})

const highestScore = computed(() => {
  if (validScores.value.length === 0) return '—'
  return Math.max(...validScores.value.map((s: any) => Number(s.totalScore)))
})

const absentCount = computed(() => scoreList.value.filter(s => s.isAbsent).length)

const getExamName = (examId: number) => {
  const exam = examList.value.find(e => e.examId === examId)
  if (!exam) return `考试ID: ${examId}`
  const paper = paperList.value.find(p => p.paperId === exam.paperId)
  const cls = classList.value.find(c => c.classId === exam.classId)
  const paperName = paper ? paper.title : `试卷${exam.paperId}`
  const className = cls ? cls.className : ''
  return className ? `${paperName} (${className})` : paperName
}

const getExamDate = (examId: number) => {
  const exam = examList.value.find(e => e.examId === examId)
  if (!exam || !exam.examDate) return '—'
  return exam.examDate.substring(0, 10)
}

const getScoreClass = (score: number) => {
  if (score >= 90) return 'score-excellent'
  if (score >= 70) return 'score-good'
  if (score >= 60) return 'score-pass'
  return 'score-fail'
}

const getGradeType = (score: number): string => {
  if (score >= 90) return 'success'
  if (score >= 70) return ''
  if (score >= 60) return 'warning'
  return 'danger'
}

const getGradeLabel = (score: number) => {
  if (score >= 90) return '优秀'
  if (score >= 80) return '良好'
  if (score >= 70) return '中等'
  if (score >= 60) return '及格'
  return '不及格'
}

const fetchData = async () => {
  loading.value = true
  try {
    const [scoresRes, examsRes, papersRes, classesRes] = await Promise.all([
      getStudentScores(userId),
      getExaminationsPage({ current: 1, size: 500 }),
      getPapers({ current: 1, size: 500 }),
      getClasses({ page: 1, size: 200 })
    ])
    scoreList.value = scoresRes.data || []
    examList.value = examsRes.data?.records || examsRes.data || []
    paperList.value = papersRes.data?.records || papersRes.data || []
    classList.value = classesRes.data?.records || classesRes.data || []

    await nextTick()
    renderTrendChart()
  } catch (e) {
    console.error(e)
    ElMessage.error('加载成绩数据失败')
  } finally {
    loading.value = false
  }
}

const renderTrendChart = () => {
  if (!trendChartRef.value || validScores.value.length < 2) return

  trendChart?.dispose()
  trendChart = echarts.init(trendChartRef.value)

  // Sort by exam date for chronological order
  const sorted = [...validScores.value].sort((a, b) => {
    const dateA = getExamDate(a.examId)
    const dateB = getExamDate(b.examId)
    return dateA.localeCompare(dateB)
  })

  const labels = sorted.map(s => getExamDate(s.examId))
  const scores = sorted.map(s => Number(s.totalScore))

  trendChart.setOption({
    tooltip: {
      trigger: 'axis',
      formatter: (params: any) => {
        const p = params[0]
        return `${p.axisValue}<br/>成绩: <strong>${p.value}</strong> 分`
      }
    },
    grid: { left: 50, right: 30, top: 30, bottom: 40 },
    xAxis: {
      type: 'category',
      data: labels,
      axisLabel: { rotate: labels.length > 6 ? 30 : 0 }
    },
    yAxis: {
      type: 'value',
      name: '分数',
      min: 0,
      max: 100
    },
    series: [{
      type: 'line',
      data: scores,
      smooth: true,
      symbol: 'circle',
      symbolSize: 10,
      lineStyle: { width: 3, color: '#409eff' },
      itemStyle: {
        color: '#409eff',
        borderColor: '#ffffff',
        borderWidth: 2
      },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
          { offset: 1, color: 'rgba(64, 158, 255, 0.02)' }
        ])
      },
      markLine: {
        data: [{ yAxis: 60, name: '及格线' }],
        lineStyle: { color: '#f56c6c', type: 'dashed' },
        label: { formatter: '及格线 60分' }
      }
    }]
  })
}

onMounted(fetchData)
</script>

<style scoped>
.score-inquiry {
  padding: 24px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.page-header {
  margin-bottom: 24px;
}

.page-header h2 {
  margin: 0;
  font-size: 24px;
  color: #303133;
  font-weight: 600;
}

.subtitle {
  margin: 8px 0 0;
  color: #909399;
  font-size: 14px;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.summary-card {
  border-radius: 12px;
  transition: transform 0.2s, box-shadow 0.2s;
}

.summary-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.08);
}

.summary-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
}

.summary-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.summary-info {
  display: flex;
  flex-direction: column;
}

.summary-value {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
  line-height: 1.2;
}

.summary-label {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}

.chart-card {
  border-radius: 8px;
}

.table-card {
  border-radius: 8px;
}

.card-header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.exam-name {
  font-weight: 600;
  color: #303133;
}

.score-value {
  font-size: 20px;
  font-weight: 700;
}

.score-excellent { color: #67c23a; }
.score-good { color: #409eff; }
.score-pass { color: #e6a23c; }
.score-fail { color: #f56c6c; }

@media screen and (max-width: 768px) {
  .summary-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
