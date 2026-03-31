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
        <el-table-column label="操作" width="120" fixed="right" align="center">
          <template #default="{ row }">
            <el-button 
              type="primary" 
              link 
              size="small" 
              :disabled="row.isAbsent"
              @click="viewAnalysisReport(row)"
            >
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && scoreList.length === 0" description="您还没有参加过考试" />
    </el-card>

    <!-- Analysis Report Drawer -->
    <el-drawer
      v-model="drawerVisible"
      title="个人答题分析报告"
      direction="rtl"
      size="65%"
      class="analysis-drawer"
    >
      <div class="drawer-content" v-loading="drawerLoading">
        <template v-if="currentReportData">
          <!-- Report Header -->
          <div class="report-header">
            <h3>{{ currentReportData.examName }}</h3>
            <div class="report-meta">
              <span>得分: <strong class="highlight-score">{{ currentReportData.score }}</strong> / {{ currentReportData.totalScore }}</span>
              <el-tag :type="getGradeType(currentReportData.score)" effect="dark">{{ getGradeLabel(currentReportData.score) }}</el-tag>
            </div>
          </div>

          <!-- Report Summary Stats -->
          <div class="report-summary">
            <div class="summary-stat-box status-correct">
              <div class="stat-num">{{ currentReportData.correctCount }}</div>
              <div class="stat-label"><el-icon><Check /></el-icon> 正确</div>
            </div>
            <div class="summary-stat-box status-wrong">
              <div class="stat-num">{{ currentReportData.wrongCount }}</div>
              <div class="stat-label"><el-icon><Close /></el-icon> 错误</div>
            </div>
            <div class="summary-stat-box status-partial">
              <div class="stat-num">{{ currentReportData.partialCount }}</div>
              <div class="stat-label"><el-icon><WarningFilled /></el-icon> 半对</div>
            </div>
            <div class="summary-stat-box">
              <div class="stat-num">{{ currentReportData.unansweredCount }}</div>
              <div class="stat-label"><el-icon><Document /></el-icon> 未作答</div>
            </div>
          </div>

          <!-- Question Breakdown -->
          <div class="question-list">
            <h4>题目明细分析</h4>
            <div 
              v-for="(q, index) in currentReportData.questions" 
              :key="q.questionId" 
              class="question-card"
              :class="q.statusClass"
            >
              <div class="q-header">
                <span class="q-title">第 {{ Number(index) + 1 }} 题. [{{ q.questionType }}]</span>
                <span class="q-score-info">得分: <strong>{{ q.scoreObtained }}</strong> / {{ q.maxScore }}</span>
              </div>
              
              <div class="q-body">
                <div class="q-content" v-html="q.questionContent"></div>
                
                <div class="q-options" v-if="q.options && q.options.length > 0">
                  <div 
                    v-for="opt in q.options" 
                    :key="opt.key" 
                    class="q-option-item"
                    :class="{ 
                      'is-correct-opt': q.rawCorrectAnswer === opt.key || q.rawCorrectAnswer?.includes(opt.key),
                      'is-my-opt': q.rawStudentChoice === opt.key || q.rawStudentChoice?.includes(opt.key)
                    }"
                  >
                    <span class="opt-key">{{ opt.key }}.</span>
                    <span class="opt-val">{{ opt.value }}</span>
                    <el-tag size="small" type="success" effect="dark" v-if="q.rawCorrectAnswer === opt.key || q.rawCorrectAnswer?.includes(opt.key)" style="margin-left: 10px;">标准答案</el-tag>
                    <el-tag size="small" type="primary" v-if="q.rawStudentChoice === opt.key || q.rawStudentChoice?.includes(opt.key)" style="margin-left: 10px;">我的选择</el-tag>
                  </div>
                </div>
              </div>

              <div class="q-footer" :class="{ 'q-footer-vertical': ['FillBlank', 'ShortAnswer'].includes(q.questionType) }">
                <div class="answer-compare" :class="{ 'is-vertical': ['FillBlank', 'ShortAnswer'].includes(q.questionType) }">
                  <div class="answer-box my-answer">
                    <span class="ans-label">我的解答:</span>
                    <span class="ans-value" :class="q.isCorrect ? 'text-success' : 'text-danger'">
                      {{ q.studentChoice || '未作答' }}
                    </span>
                  </div>
                  <div class="answer-box std-answer">
                    <span class="ans-label">正确答案:</span>
                    <span class="ans-value text-success">{{ q.correctAnswer || '略' }}</span>
                  </div>
                </div>
                <!-- Visual Indicator -->
                <div class="status-icon" :class="q.statusClass">
                  <el-icon v-if="q.statusClass === 'status-correct'"><Check /></el-icon>
                  <el-icon v-else-if="q.statusClass === 'status-wrong'"><Close /></el-icon>
                  <el-icon v-else><WarningFilled /></el-icon>
                </div>
              </div>
            </div>
          </div>
        </template>
        <el-empty v-else description="暂无分析数据" />
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, nextTick } from 'vue'
import { getStudentScores, getExaminationsPage, getPapers, getAnswersByResultId, getPaperById } from '@/api/exam'
import { getClasses } from '@/api/academic'
import { ElMessage } from 'element-plus'
import { Document, TrendCharts, Trophy, Warning, Check, Close, WarningFilled } from '@element-plus/icons-vue'
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

// Drawer state
const drawerVisible = ref(false)
const drawerLoading = ref(false)
const currentReportData = ref<any>(null)

const viewAnalysisReport = async (row: any) => {
  drawerVisible.value = true
  drawerLoading.value = true
  try {
    const exam = examList.value.find(e => e.examId === row.examId)
    if (!exam || !exam.paperId) throw new Error('Exam or Paper not found')

    const [paperRes, answersRes] = await Promise.all([
      getPaperById(exam.paperId),
      getAnswersByResultId(row.resultId)
    ])

    const paper = paperRes.data
    const answers = answersRes.data || []

    const parseAndFormat = (val: string | null) => {
      if (!val) return val
      if (val.trim().startsWith('[') && val.trim().endsWith(']')) {
        try {
          const arr = JSON.parse(val)
          if (Array.isArray(arr)) return arr.join(', ')
        } catch(e) {}
      }
      return val
    }

    const mappedQuestions = paper.questions.map((pq: any) => {
      const stuAns = answers.find((a: any) => String(a.questionId) === String(pq.questionId))
      
      let options: any[] = []
      if (pq.optionsJson) {
        try { 
          const parsed = JSON.parse(pq.optionsJson) 
          if (Array.isArray(parsed)) {
            options = parsed.map((item, i) => ({ key: String.fromCharCode(65 + i), value: item }))
          } else if (typeof parsed === 'object') {
            options = Object.keys(parsed).map(k => ({ key: k, value: parsed[k] }))
          }
        } catch (e) {}
      }

      // Format answers for display (unwrap JSON arrays like ["A","B"] to "A, B")
      const rawCorrectAnswer = pq.correctAnswer
      const formattedCorrectAnswer = parseAndFormat(rawCorrectAnswer)
      
      const rawStudentChoice = stuAns ? stuAns.studentChoice : null
      const formattedStudentChoice = parseAndFormat(rawStudentChoice)

      // Determine status using score obtained vs max score
      let statusClass = 'status-wrong'
      let isCorrect = false
      if (stuAns) {
        const obtained = Number(stuAns.scoreObtained || 0)
        const maxScore = Number(pq.scoreValue || 0)
        
        if (obtained > 0 && obtained >= maxScore) {
          statusClass = 'status-correct'
          isCorrect = true
        } else if (obtained > 0) {
          statusClass = 'status-partial'
        }
      }

      return {
        questionId: pq.questionId,
        questionOrder: pq.questionOrder,
        questionContent: pq.questionContent,
        questionType: pq.questionType,
        options,
        correctAnswer: formattedCorrectAnswer,
        rawCorrectAnswer: rawCorrectAnswer,
        studentChoice: formattedStudentChoice,
        rawStudentChoice: rawStudentChoice,
        scoreObtained: stuAns ? stuAns.scoreObtained : 0,
        isCorrect,
        statusClass,
        hasAnswered: !!stuAns && rawStudentChoice !== null && rawStudentChoice !== ''
      }
    })

    // Sort by order
    mappedQuestions.sort((a: any, b: any) => a.questionOrder - b.questionOrder)

    // Calculate Summary Stats
    const correctCount = mappedQuestions.filter((q: any) => q.statusClass === 'status-correct').length
    const partialCount = mappedQuestions.filter((q: any) => q.statusClass === 'status-partial').length
    const wrongCount = mappedQuestions.filter((q: any) => q.statusClass === 'status-wrong' && q.scoreObtained !== null).length
    const unansweredCount = mappedQuestions.filter((q: any) => !q.hasAnswered).length

    currentReportData.value = {
      examName: getExamName(row.examId),
      score: row.totalScore,
      totalScore: paper.totalScore,
      questions: mappedQuestions,
      correctCount,
      partialCount,
      wrongCount,
      unansweredCount
    }

  } catch (e) {
    console.error(e)
    ElMessage.error('无法加载分析报告详情')
    drawerVisible.value = false
  } finally {
    drawerLoading.value = false
  }
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

/* Analysis Drawer Styles */
.analysis-drawer .drawer-content {
  padding: 0 24px 24px;
}

.report-header {
  border-bottom: 2px solid #ebeef5;
  padding-bottom: 16px;
  margin-bottom: 24px;
}

.report-header h3 {
  margin: 0 0 10px 0;
  font-size: 20px;
  color: #303133;
}

.report-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 15px;
  color: #606266;
}

.highlight-score {
  font-size: 24px;
  color: #f56c6c;
}

.report-summary {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 30px;
  flex-wrap: wrap;
}

.summary-stat-box {
  flex: 1;
  min-width: 100px;
  border-radius: 8px;
  padding: 16px;
  text-align: center;
  background-color: #f4f4f5;
  color: #909399;
}

.summary-stat-box.status-correct {
  background-color: #f0f9eb;
  color: #67c23a;
}
.summary-stat-box.status-wrong {
  background-color: #fef0f0;
  color: #f56c6c;
}
.summary-stat-box.status-partial {
  background-color: #fdf6ec;
  color: #e6a23c;
}

.stat-num {
  font-size: 24px;
  font-weight: 700;
  line-height: 1;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 13px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

.question-list h4 {
  font-size: 18px;
  margin-bottom: 16px;
  color: #303133;
}

.question-card {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  background-color: #ffffff;
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
}

/* Subtle Colored Backgrounds per user request */
.question-card.status-correct {
  background-color: #f8fdf6;
  border-color: #e1f3d8;
}

.question-card.status-wrong {
  background-color: #fffaf9; 
  border-color: #fde2e2;
}

.question-card.status-partial {
  background-color: #fffcf8;
  border-color: #faecd8;
}

/* Add a bold left border flag to emphasize state */
.question-card::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
}
.question-card.status-correct::before { background-color: #67c23a; }
.question-card.status-wrong::before { background-color: #f56c6c; }
.question-card.status-partial::before { background-color: #e6a23c; }

.q-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 16px;
  color: #606266;
  font-size: 15px;
}

.q-title { font-weight: 600; color: #303133; }
.q-score-info strong { color: #f56c6c; font-size: 18px; }

.q-body { margin-bottom: 20px; }

.q-content {
  font-size: 15px;
  line-height: 1.6;
  color: #303133;
  margin-bottom: 16px;
}

.q-options {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.q-option-item {
  padding: 10px 16px;
  border-radius: 6px;
  background-color: rgba(255, 255, 255, 0.6);
  border: 1px solid #dcdfe6;
}

.q-option-item.is-correct-opt {
  border-color: #67c23a;
  background-color: #f0f9eb;
}

.q-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
  border-top: 1px dashed #ebeef5;
}

.answer-compare {
  display: flex;
  gap: 32px;
}

.answer-compare.is-vertical {
  flex-direction: column;
  gap: 16px;
  max-width: 90%;
}

.answer-box {
  display: flex;
  align-items: center;
  gap: 8px;
}

.answer-compare.is-vertical .answer-box {
  flex-direction: column;
  align-items: flex-start;
  gap: 6px;
}

.answer-compare.is-vertical .ans-value {
  white-space: pre-wrap;
  line-height: 1.6;
  font-weight: normal;
}

.ans-label { color: #909399; font-size: 14px; font-weight: 600; }
.ans-value { font-weight: 600; font-size: 16px; }

.q-footer-vertical {
  align-items: flex-start;
}

.text-success { color: #67c23a; }
.text-danger { color: #f56c6c; }

.status-icon {
  font-size: 32px;
  opacity: 0.8;
}
.status-icon.status-correct { color: #67c23a; }
.status-icon.status-wrong { color: #f56c6c; }
.status-icon.status-partial { color: #e6a23c; }
</style>
