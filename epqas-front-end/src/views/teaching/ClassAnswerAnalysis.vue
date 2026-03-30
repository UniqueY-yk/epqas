<template>
  <div class="class-analysis">
    <div class="page-header">
      <h2>班级答题分析</h2>
      <p class="subtitle">分析班级每道题的正确率和选项分布，定位薄弱环节</p>
    </div>

    <el-card class="toolbar-card" shadow="never">
      <div class="toolbar">
        <div class="search-area">
          <el-select v-model="selectedClassId" placeholder="请选择班级" style="width: 200px;" @change="handleClassChange" clearable filterable>
            <el-option v-for="c in classList" :key="c.classId" :label="c.className" :value="c.classId" />
          </el-select>
          <el-select v-model="selectedExamId" placeholder="请选择考试" style="width: 360px;" @change="loadAnalysis" :disabled="!selectedClassId" filterable>
            <el-option v-for="e in filteredExamList" :key="e.examId" :label="`${getPaperName(e.paperId)} (${e.examDate?.substring(0, 10)})`" :value="e.examId" />
          </el-select>
        </div>
      </div>
    </el-card>

    <el-row :gutter="20" v-if="questionStats.length > 0">
      <el-col :span="24">
        <el-card shadow="never" class="chart-card">
          <template #header><strong>📊 各题正确率</strong></template>
          <div ref="correctRateChartRef" style="height: 360px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <div class="chart-grid" v-if="scqData.length > 0 || mcqData.length > 0 || tfData.length > 0">
      <el-card shadow="never" class="chart-card" v-if="scqData.length > 0">
        <template #header><strong>🔵 单选题选项分布</strong></template>
        <div ref="scqChartRef" style="height: 380px;"></div>
      </el-card>

      <el-card shadow="never" class="chart-card" v-if="mcqData.length > 0">
        <template #header><strong>🔳 多选题选项分布</strong></template>
        <div ref="mcqChartRef" style="height: 380px;"></div>
      </el-card>

      <el-card shadow="never" class="chart-card" v-if="tfData.length > 0">
        <template #header><strong>⚖️ 判断题选项分布</strong></template>
        <div ref="tfChartRef" style="height: 380px;"></div>
      </el-card>
    </div>

    <el-card shadow="never" v-if="questionStats.length > 0" style="margin-top: 20px;">
      <template #header><strong>📋 题目明细</strong></template>
      <el-table :data="questionStats" stripe border style="width: 100%;">
        <el-table-column label="序号" prop="questionOrder" width="70" align="center" />
        <el-table-column label="题目内容" prop="questionContent" min-width="200" show-overflow-tooltip />
        <el-table-column label="题型" prop="questionType" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="typeTag(row.questionType)" size="small">{{ typeLabel(row.questionType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="正确答案" prop="correctAnswer" width="100" align="center" />
        <el-table-column label="作答人数" prop="totalAnswers" width="90" align="center" />
        <el-table-column label="正确率" width="120" align="center">
          <template #default="{ row }">
            <el-progress :percentage="Math.round(row.correctRate * 100)" :color="rateColor(row.correctRate)" :stroke-width="10" />
          </template>
        </el-table-column>
        <el-table-column label="关联知识点" min-width="180">
          <template #default="{ row }">
            <el-tag v-for="kp in row.knowledgePoints" :key="kp" size="small" style="margin: 2px;" type="info">{{ kp }}</el-tag>
            <span v-if="!row.knowledgePoints?.length" style="color: #999;">无</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-empty v-if="!loading && selectedExamId && questionStats.length === 0" description="暂无答题数据" />
    <el-empty v-if="!selectedExamId" description="请先选择一场考试" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, computed } from 'vue'
import { getExaminationsPage, getQuestionStats, getPapers } from '@/api/exam'
import { getClasses } from '@/api/academic'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'

const selectedClassId = ref<number | null>(null)
const classList = ref<any[]>([])

const selectedExamId = ref<number | null>(null)
const examList = ref<any[]>([])
const paperList = ref<any[]>([])

const getPaperName = (id: number) => {
  const p = paperList.value.find(item => item.paperId === id)
  return p ? p.title : `试卷ID: ${id}`
}

const filteredExamList = computed(() => {
  if (!selectedClassId.value) return []
  return examList.value.filter(e => e.classId === selectedClassId.value)
})

const questionStats = ref<any[]>([])
const loading = ref(false)

const scqData = computed(() => questionStats.value.filter(d => d.questionType === 'SingleChoice'))
const mcqData = computed(() => questionStats.value.filter(d => d.questionType === 'MultipleChoice'))
const tfData = computed(() => questionStats.value.filter(d => d.questionType === 'TrueFalse'))

const correctRateChartRef = ref<HTMLElement | null>(null)
const scqChartRef = ref<HTMLElement | null>(null)
const mcqChartRef = ref<HTMLElement | null>(null)
const tfChartRef = ref<HTMLElement | null>(null)

let correctRateChart: echarts.ECharts | null = null
let scqChart: echarts.ECharts | null = null
let mcqChart: echarts.ECharts | null = null
let tfChart: echarts.ECharts | null = null

const typeLabel = (t: string) => {
  const map: Record<string, string> = { SingleChoice: '单选', MultipleChoice: '多选', TrueFalse: '判断', FillBlank: '填空', ShortAnswer: '简答' }
  return map[t] || t
}
const typeTag = (t: string) => {
  const map: Record<string, string> = { SingleChoice: '', MultipleChoice: 'warning', TrueFalse: 'success', FillBlank: 'info', ShortAnswer: 'danger' }
  return map[t] || ''
}
const rateColor = (rate: number) => rate >= 0.8 ? '#67c23a' : rate >= 0.6 ? '#e6a23c' : '#f56c6c'

const fetchInitialData = async () => {
  try {
    const [classRes, examRes, paperRes] = await Promise.all([
        getClasses({ page: 1, size: 200 }),
        getExaminationsPage({ current: 1, size: 500 }),
        getPapers({ current: 1, size: 500 })
    ])
    classList.value = classRes.data?.records || classRes.data || []
    examList.value = examRes.data?.records || examRes.data || []
    paperList.value = paperRes.data?.records || paperRes.data || []
  } catch (e) {
    ElMessage.error('加载初始化数据失败')
  }
}

const handleClassChange = () => {
    selectedExamId.value = null
    questionStats.value = []
    if (correctRateChart) correctRateChart.dispose()
    if (scqChart) scqChart.dispose()
    if (mcqChart) mcqChart.dispose()
    if (tfChart) tfChart.dispose()
}

const loadAnalysis = async () => {
  if (!selectedExamId.value) return
  loading.value = true
  try {
    const res = await getQuestionStats(selectedExamId.value)
    questionStats.value = res.data || []
    await nextTick()
    renderCharts()
  } catch (e) {
    ElMessage.error('加载分析数据失败')
  } finally {
    loading.value = false
  }
}

const renderCharts = () => {
  const data = questionStats.value
  if (!data.length) return

  // Correct Rate Bar Chart
  if (correctRateChartRef.value) {
    correctRateChart?.dispose()
    correctRateChart = echarts.init(correctRateChartRef.value)
    const labels = data.map((d: any) => `第${d.questionOrder}题`)
    const rates = data.map((d: any) => Math.round(d.correctRate * 100))
    correctRateChart.setOption({
      tooltip: { trigger: 'axis', formatter: '{b}<br/>正确率: {c}%' },
      xAxis: { type: 'category', data: labels, axisLabel: { interval: 0, rotate: data.length > 15 ? 45 : 0 } },
      yAxis: { type: 'value', max: 100, name: '正确率 (%)' },
      series: [{
        type: 'bar',
        data: rates.map((r: number) => ({ value: r, itemStyle: { color: rateColor(r / 100), borderRadius: [4, 4, 0, 0], shadowColor: 'rgba(0, 0, 0, 0.1)', shadowBlur: 4 } })),
        barMaxWidth: 35,
        label: { show: true, position: 'top', formatter: '{c}%', fontSize: 11, color: '#666' }
      }],
      grid: { left: 50, right: 20, bottom: 50, top: 40 }
    })
  }

  // SCQ Chart
  if (scqChartRef.value && scqData.value.length > 0) {
    scqChart?.dispose()
    scqChart = echarts.init(scqChartRef.value)
    const labels = scqData.value.map((d: any) => `第${d.questionOrder}题`)
    const optionKeys = ['A', 'B', 'C', 'D']
    const colorPalette = ['#5470c6', '#91cc75', '#fac858', '#ee6666']
    const series = optionKeys.map((opt, i) => ({
      name: `选项 ${opt}`,
      type: 'bar' as const,
      barMaxWidth: 40,
      itemStyle: { color: colorPalette[i % colorPalette.length], borderRadius: [4, 4, 0, 0] },
      data: scqData.value.map((d: any) => d.optionDistribution?.[opt] || 0)
    }))
    scqChart.setOption({
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      legend: { data: optionKeys.map(o => `选项 ${o}`), top: 0 },
      xAxis: { type: 'category', data: labels, axisLabel: { interval: 0, rotate: labels.length > 8 ? 45 : 0 } },
      yAxis: { type: 'value', name: '选择人数' },
      series,
      grid: { left: 45, right: 20, bottom: 45, top: 40 }
    })
  }

  // MCQ Chart
  if (mcqChartRef.value && mcqData.value.length > 0) {
    mcqChart?.dispose()
    mcqChart = echarts.init(mcqChartRef.value)
    const labels = mcqData.value.map((d: any) => `第${d.questionOrder}题`)
    const optionKeys = ['A', 'B', 'C', 'D']
    const colorPalette = ['#91cc75', '#fac858', '#ee6666', '#5470c6']
    const series = optionKeys.map((opt, i) => ({
      name: `选项 ${opt}`,
      type: 'bar' as const,
      barMaxWidth: 40,
      itemStyle: { color: colorPalette[i % colorPalette.length], borderRadius: [4, 4, 0, 0] },
      data: mcqData.value.map((d: any) => d.optionDistribution?.[opt] || 0)
    }))
    mcqChart.setOption({
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      legend: { data: optionKeys.map(o => `选项 ${o}`), top: 0 },
      xAxis: { type: 'category', data: labels, axisLabel: { interval: 0, rotate: labels.length > 8 ? 45 : 0 } },
      yAxis: { type: 'value', name: '选择人次' },
      series,
      grid: { left: 45, right: 20, bottom: 45, top: 40 }
    })
  }

  // TF Chart
  if (tfChartRef.value && tfData.value.length > 0) {
    tfChart?.dispose()
    tfChart = echarts.init(tfChartRef.value)
    const labels = tfData.value.map((d: any) => `第${d.questionOrder}题`)
    const allOptions = new Set<string>()
    tfData.value.forEach((d: any) => {
      if (d.optionDistribution) Object.keys(d.optionDistribution).forEach(k => allOptions.add(k))
    })
    const optionKeys = Array.from(allOptions).sort()
    const colorPalette = ['#67c23a', '#f56c6c', '#909399'] 
    const series = optionKeys.map((opt, i) => ({
      name: opt,
      type: 'bar' as const,
      barMaxWidth: 40,
      itemStyle: { color: colorPalette[i % colorPalette.length], borderRadius: [4, 4, 0, 0] },
      data: tfData.value.map((d: any) => d.optionDistribution?.[opt] || 0)
    }))
    tfChart.setOption({
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      legend: { data: optionKeys, top: 0 },
      xAxis: { type: 'category', data: labels, axisLabel: { interval: 0, rotate: labels.length > 8 ? 45 : 0 } },
      yAxis: { type: 'value', name: '人数' },
      series,
      grid: { left: 45, right: 20, bottom: 45, top: 40 }
    })
  }
}

onMounted(fetchInitialData)
</script>

<style scoped>
.class-analysis { padding: 24px; background-color: #f5f7fa; min-height: calc(100vh - 60px); }
.page-header { margin-bottom: 24px; }
.page-header h2 { margin: 0; font-size: 24px; color: #303133; font-weight: 600; }
.subtitle { margin: 8px 0 0; color: #909399; font-size: 14px; }
.toolbar-card { margin-bottom: 16px; border-radius: 8px; }
.toolbar { display: flex; align-items: center; }
.search-area { display: flex; gap: 12px; align-items: center; }
.chart-card { border-radius: 8px; }
.chart-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(450px, 1fr));
  gap: 20px;
  margin-top: 20px;
}
@media screen and (max-width: 768px) {
  .chart-grid { grid-template-columns: 1fr; }
}
</style>
