<template>
  <div class="knowledge-mastery">
    <div class="page-header">
      <h2>知识点掌握诊断</h2>
      <p class="subtitle">诊断班级知识点掌握情况，识别需要重点复习的薄弱环节</p>
    </div>

    <el-card class="toolbar-card" shadow="never">
      <div class="toolbar">
        <div class="search-area">
          <el-select v-model="selectedClassId" placeholder="请选择班级" style="width: 200px;" @change="handleClassChange" clearable filterable>
            <el-option v-for="c in classList" :key="c.classId" :label="c.className" :value="c.classId" />
          </el-select>
          <el-select v-model="selectedExamId" placeholder="请选择考试" style="width: 360px;" @change="loadMastery" :disabled="!selectedClassId" filterable>
            <el-option v-for="e in filteredExamList" :key="e.examId" :label="`${getPaperName(e.paperId)} (${e.examDate?.substring(0, 10)})`" :value="e.examId" />
          </el-select>
        </div>
      </div>
    </el-card>

    <el-row :gutter="20" v-if="masteryData.length > 0">
      <el-col :span="12">
        <el-card shadow="never" class="chart-card">
          <template #header><strong>🎯 知识点掌握雷达图</strong></template>
          <div ref="radarChartRef" style="height: 400px;"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never" class="chart-card">
          <template #header><strong>📊 知识点掌握率</strong></template>
          <div ref="barChartRef" style="height: 400px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" v-if="masteryData.length > 0" style="margin-top: 20px;">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <strong>📋 知识点明细</strong>
          <div>
            <el-tag type="danger" size="small" style="margin-right: 8px;">薄弱 (&lt;60%)</el-tag>
            <el-tag type="warning" size="small" style="margin-right: 8px;">一般 (60-80%)</el-tag>
            <el-tag type="success" size="small">优秀 (≥80%)</el-tag>
          </div>
        </div>
      </template>
      <el-table :data="masteryData" stripe border style="width: 100%;">
        <el-table-column label="知识点" prop="pointName" min-width="200" />
        <el-table-column label="涉及题数" prop="totalQuestions" width="100" align="center" />
        <el-table-column label="总作答次数" prop="totalAnswers" width="110" align="center" />
        <el-table-column label="正确次数" prop="correctCount" width="100" align="center" />
        <el-table-column label="掌握率" width="160" align="center">
          <template #default="{ row }">
            <el-progress :percentage="Math.round(row.masteryRate * 100)" :color="levelColor(row.level)" :stroke-width="12" />
          </template>
        </el-table-column>
        <el-table-column label="掌握等级" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="levelTag(row.level)" effect="dark">{{ levelLabel(row.level) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="建议" min-width="200">
          <template #default="{ row }">
            <span v-if="row.level === 'weak'" style="color: #f56c6c; font-weight: 600;">⚠️ 需要重点复习和重教</span>
            <span v-else-if="row.level === 'good'" style="color: #e6a23c;">📝 建议适当强化练习</span>
            <span v-else style="color: #67c23a;">✅ 掌握良好，可继续推进</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-empty v-if="!loading && selectedExamId && masteryData.length === 0" description="暂无知识点数据（可能该试卷题目未关联知识点）" />
    <el-empty v-if="!selectedExamId" description="请先选择一场考试" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, computed } from 'vue'
import { getExaminationsPage, getKnowledgeMastery, getPapers } from '@/api/exam'
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

const masteryData = ref<any[]>([])
const loading = ref(false)

const radarChartRef = ref<HTMLElement | null>(null)
const barChartRef = ref<HTMLElement | null>(null)
let radarChart: echarts.ECharts | null = null
let barChart: echarts.ECharts | null = null

const levelColor = (level: string) => level === 'excellent' ? '#67c23a' : level === 'good' ? '#e6a23c' : '#f56c6c'
const levelTag = (level: string) => level === 'excellent' ? 'success' : level === 'good' ? 'warning' : 'danger'
const levelLabel = (level: string) => level === 'excellent' ? '优秀' : level === 'good' ? '一般' : '薄弱'

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
    masteryData.value = []
    if (radarChart) radarChart.dispose()
    if (barChart) barChart.dispose()
}

const loadMastery = async () => {
  if (!selectedExamId.value) return
  loading.value = true
  try {
    const res = await getKnowledgeMastery(selectedExamId.value)
    masteryData.value = res.data || []
    await nextTick()
    renderCharts()
  } catch (e) {
    ElMessage.error('加载知识点数据失败')
  } finally {
    loading.value = false
  }
}

const renderCharts = () => {
  const data = masteryData.value
  if (!data.length) return

  // Radar Chart
  if (radarChartRef.value) {
    radarChart?.dispose()
    radarChart = echarts.init(radarChartRef.value)
    const indicators = data.map((d: any) => ({ name: d.pointName.length > 8 ? d.pointName.substring(0, 8) + '...' : d.pointName, max: 100 }))
    const values = data.map((d: any) => Math.round(d.masteryRate * 100))
    radarChart.setOption({
      tooltip: {},
      radar: {
        indicator: indicators,
        shape: 'polygon',
        splitNumber: 5,
        axisName: { color: '#606266', fontSize: 11 },
        splitArea: { areaStyle: { color: ['rgba(103,194,58,0.05)', 'rgba(103,194,58,0.1)', 'rgba(230,162,60,0.1)', 'rgba(245,108,108,0.1)', 'rgba(245,108,108,0.15)'].reverse() } }
      },
      series: [{
        type: 'radar',
        data: [{ value: values, name: '掌握率', areaStyle: { color: 'rgba(64,158,255,0.2)' }, lineStyle: { color: '#409eff' }, itemStyle: { color: '#409eff' } }]
      }]
    })
  }

  // Horizontal Bar Chart
  if (barChartRef.value) {
    barChart?.dispose()
    barChart = echarts.init(barChartRef.value)
    const sortedData = [...data].sort((a: any, b: any) => a.masteryRate - b.masteryRate)
    const names = sortedData.map((d: any) => d.pointName.length > 12 ? d.pointName.substring(0, 12) + '...' : d.pointName)
    const rates = sortedData.map((d: any) => Math.round(d.masteryRate * 100))
    barChart.setOption({
      tooltip: { trigger: 'axis', formatter: '{b}<br/>掌握率: {c}%' },
      xAxis: { type: 'value', max: 100, name: '掌握率 (%)' },
      yAxis: { type: 'category', data: names, axisLabel: { width: 100, overflow: 'truncate' } },
      series: [{
        type: 'bar',
        data: rates.map((r: number) => ({
          value: r,
          itemStyle: { color: r >= 80 ? '#67c23a' : r >= 60 ? '#e6a23c' : '#f56c6c', borderRadius: [0, 4, 4, 0] }
        })),
        barMaxWidth: 24,
        label: { show: true, position: 'right', formatter: '{c}%', fontSize: 11 }
      }],
      grid: { left: 120, right: 60, bottom: 20, top: 20 },
      visualMap: {
        show: false,
        pieces: [{ lt: 60, color: '#f56c6c' }, { gte: 60, lt: 80, color: '#e6a23c' }, { gte: 80, color: '#67c23a' }]
      }
    })
  }
}

onMounted(fetchInitialData)
</script>

<style scoped>
.knowledge-mastery { padding: 24px; background-color: #f5f7fa; min-height: calc(100vh - 60px); }
.page-header { margin-bottom: 24px; }
.page-header h2 { margin: 0; font-size: 24px; color: #303133; font-weight: 600; }
.subtitle { margin: 8px 0 0; color: #909399; font-size: 14px; }
.toolbar-card { margin-bottom: 16px; border-radius: 8px; }
.toolbar { display: flex; align-items: center; }
.search-area { display: flex; gap: 12px; align-items: center; }
.chart-card { border-radius: 8px; }
</style>
