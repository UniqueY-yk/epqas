<template>
  <div class="abnormal-detection">
    <div class="page-header">
      <h2>异常答题检测</h2>
      <p class="subtitle">智能识别班级考试中的异常作答模式，辅助发现潜在舞弊行为</p>
    </div>

    <el-card class="toolbar-card" shadow="never">
      <div class="toolbar">
        <div class="search-area">
          <el-select v-model="selectedClassId" placeholder="请选择班级" style="width: 200px;" @change="handleClassChange" clearable filterable>
            <el-option v-for="c in classList" :key="c.classId" :label="c.className" :value="c.classId" />
          </el-select>
          <el-select v-model="selectedExamId" placeholder="请选择考试" style="width: 360px;" @change="loadDetection" :disabled="!selectedClassId" filterable>
            <el-option v-for="e in filteredExamList" :key="e.examId" :label="`${getPaperName(e.paperId)} (${e.examDate?.substring(0, 10)})`" :value="e.examId" />
          </el-select>
        </div>
      </div>
    </el-card>

    <!-- Summary Cards -->
    <div class="summary-grid" v-if="detectionData.length > 0 || (selectedExamId && !loading)">
      <el-card shadow="never" class="summary-card">
        <div class="summary-icon" style="background: #ecf5ff; color: #409eff;">
          <el-icon :size="28"><User /></el-icon>
        </div>
        <div class="summary-info">
          <span class="summary-value">{{ totalStudents }}</span>
          <span class="summary-label">参考学生数</span>
        </div>
      </el-card>

      <el-card shadow="never" class="summary-card">
        <div class="summary-icon" style="background: #fef0f0; color: #f56c6c;">
          <el-icon :size="28"><Warning /></el-icon>
        </div>
        <div class="summary-info">
          <span class="summary-value" :class="{ 'danger-text': detectionData.length > 0 }">{{ detectionData.length }}</span>
          <span class="summary-label">可疑学生对</span>
        </div>
      </el-card>

      <el-card shadow="never" class="summary-card">
        <div class="summary-icon" style="background: #fdf6ec; color: #e6a23c;">
          <el-icon :size="28"><TrendCharts /></el-icon>
        </div>
        <div class="summary-info">
          <span class="summary-value">{{ highestSimilarity }}%</span>
          <span class="summary-label">最高相似度</span>
        </div>
      </el-card>

      <el-card shadow="never" class="summary-card">
        <div class="summary-icon" style="background: #f0f9eb; color: #67c23a;">
          <el-icon :size="28"><CircleCheck /></el-icon>
        </div>
        <div class="summary-info">
          <span class="summary-value">{{ detectionData.length === 0 ? '正常' : '需关注' }}</span>
          <span class="summary-label">检测结论</span>
        </div>
      </el-card>
    </div>

    <!-- Suspicious Pairs Table -->
    <el-card shadow="never" v-if="detectionData.length > 0" class="table-card" style="margin-top: 20px;">
      <template #header>
        <div class="card-header-row">
          <strong>⚠️ 可疑答题对列表</strong>
          <el-tag type="danger" effect="dark" size="small">{{ detectionData.length }} 对</el-tag>
        </div>
      </template>
      <el-table :data="detectionData" stripe border style="width: 100%;" row-key="pairKey" :expand-row-keys="expandedRows" @expand-change="handleExpand">
        <el-table-column type="expand">
          <template #default="{ row }">
            <div class="expand-section">
              <h4 style="margin: 0 0 12px 0; color: #303133;">相同错误答案明细</h4>
              <el-table :data="row.identicalWrongDetails" border size="small" class="inner-table">
                <el-table-column label="题序" prop="questionOrder" width="70" align="center" />
                <el-table-column label="题目内容" prop="questionContent" min-width="250" show-overflow-tooltip />
                <el-table-column label="相同错误选择" prop="studentChoice" width="140" align="center">
                  <template #default="{ row: detail }">
                    <el-tag type="danger" effect="plain">{{ detail.studentChoice }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="正确答案" prop="correctAnswer" width="140" align="center">
                  <template #default="{ row: detail }">
                    <el-tag type="success" effect="plain">{{ detail.correctAnswer }}</el-tag>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="学生A" min-width="120" align="center">
          <template #default="{ row }">
            <span class="student-name">{{ row.studentNameA }}</span>
          </template>
        </el-table-column>
        <el-table-column label="学生B" min-width="120" align="center">
          <template #default="{ row }">
            <span class="student-name">{{ row.studentNameB }}</span>
          </template>
        </el-table-column>
        <el-table-column label="总题数" prop="totalQuestions" width="90" align="center" />
        <el-table-column label="相同错答数" prop="identicalWrongCount" width="110" align="center">
          <template #default="{ row }">
            <el-tag type="danger" effect="light" size="default">{{ row.identicalWrongCount }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="相似度" width="180" align="center">
          <template #default="{ row }">
            <el-progress
              :percentage="Math.round(row.similarityRate * 100)"
              :color="getSimilarityColor(row.similarityRate)"
              :stroke-width="12"
              :text-inside="true"
            />
          </template>
        </el-table-column>
        <el-table-column label="风险等级" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getRiskType(row.similarityRate)" effect="dark" size="small">{{ getRiskLabel(row.similarityRate) }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Relationship Graph -->
    <el-card shadow="never" v-if="detectionData.length > 0" class="chart-card" style="margin-top: 20px;">
      <template #header><strong>🕸️ 学生答题相似度关系图</strong></template>
      <div ref="relationChartRef" style="height: 900px;"></div>
    </el-card>

    <!-- Empty states -->
    <el-card shadow="never" v-if="!loading && selectedExamId && detectionData.length === 0" style="margin-top: 20px;">
      <el-result icon="success" title="未检测到异常" sub-title="该场考试未发现可疑的相同错误答题模式，考试整体正常。">
        <template #extra>
          <el-tag type="success" effect="dark">检测通过</el-tag>
        </template>
      </el-result>
    </el-card>
    <el-empty v-if="!selectedExamId && !loading" description="请先选择班级和考试" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, computed } from 'vue'
import { getExaminationsPage, getAbnormalDetection, getPapers } from '@/api/exam'
import { getClasses } from '@/api/academic'
import { ElMessage } from 'element-plus'
import { User, Warning, TrendCharts, CircleCheck } from '@element-plus/icons-vue'
import * as echarts from 'echarts'

const selectedClassId = ref<number | null>(null)
const classList = ref<any[]>([])

const selectedExamId = ref<number | null>(null)
const examList = ref<any[]>([])
const paperList = ref<any[]>([])

const detectionData = ref<any[]>([])
const loading = ref(false)
const totalStudents = ref(0)
const expandedRows = ref<string[]>([])

const relationChartRef = ref<HTMLElement | null>(null)
let relationChart: echarts.ECharts | null = null

const getPaperName = (id: number) => {
  const p = paperList.value.find(item => item.paperId === id)
  return p ? p.title : `试卷ID: ${id}`
}

const filteredExamList = computed(() => {
  if (!selectedClassId.value) return []
  return examList.value.filter(e => e.classId === selectedClassId.value)
})

const highestSimilarity = computed(() => {
  if (detectionData.value.length === 0) return 0
  return Math.round(Math.max(...detectionData.value.map(d => d.similarityRate)) * 100)
})

const getSimilarityColor = (rate: number) => {
  if (rate >= 0.7) return '#f56c6c'
  if (rate >= 0.5) return '#e6a23c'
  return '#909399'
}

const getRiskType = (rate: number): string => {
  if (rate >= 0.7) return 'danger'
  if (rate >= 0.5) return 'warning'
  return 'info'
}

const getRiskLabel = (rate: number) => {
  if (rate >= 0.7) return '高风险'
  if (rate >= 0.5) return '中风险'
  return '低风险'
}

const handleExpand = (_row: any, expanded: any[]) => {
  expandedRows.value = expanded.map((r: any) => r.pairKey)
}

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
  detectionData.value = []
  totalStudents.value = 0
  if (relationChart) relationChart.dispose()
}

const loadDetection = async () => {
  if (!selectedExamId.value) return
  loading.value = true
  try {
    const res = await getAbnormalDetection(selectedExamId.value)
    const rawData = res.data || []
    // Add pairKey for row-key
    detectionData.value = rawData.map((d: any, i: number) => ({
      ...d,
      pairKey: `pair-${i}`
    }))
    // Calculate total students from selected exam
    const selectedExam = examList.value.find(e => e.examId === selectedExamId.value)
    totalStudents.value = selectedExam?.actualExaminees || selectedExam?.totalCandidates || 0
    
    await nextTick()
    renderRelationChart()
  } catch (e) {
    ElMessage.error('加载检测数据失败')
  } finally {
    loading.value = false
  }
}

const renderRelationChart = () => {
  if (!relationChartRef.value || detectionData.value.length === 0) return

  relationChart?.dispose()
  relationChart = echarts.init(relationChartRef.value)

  // Collect unique student names for nodes
  const nameSet = new Set<string>()
  detectionData.value.forEach((d: any) => {
    nameSet.add(d.studentNameA)
    nameSet.add(d.studentNameB)
  })
  const names = Array.from(nameSet).sort()

  const palettes = ['#5470c6', '#91cc75', '#fac858', '#ea7ccc', '#73c0de', '#3ba272', '#fc8452', '#9a60b4']

  const nodes = names.map((name, index) => {
    // Determine maximum similarity for this student
    const maxSim = Math.max(
      ...detectionData.value
        .filter((d: any) => d.studentNameA === name || d.studentNameB === name)
        .map((d: any) => d.similarityRate)
    )
    return {
      name: name,
      symbolSize: maxSim >= 0.7 ? 55 : maxSim >= 0.5 ? 45 : 35,
      itemStyle: { 
        color: palettes[index % palettes.length],
        borderColor: '#ffffff',
        borderWidth: 2,
        shadowBlur: 10,
        shadowColor: 'rgba(0, 0, 0, 0.15)'
      },
      label: {
        color: '#303133',
        fontWeight: 'bold',
        textBorderColor: '#ffffff',
        textBorderWidth: 2
      }
    }
  })

  const links = detectionData.value.map((d: any) => ({
    source: d.studentNameA,
    target: d.studentNameB,
    value: Math.round(d.similarityRate * 100),
    lineStyle: {
      width: Math.max(2, d.similarityRate * 6),
      color: getSimilarityColor(d.similarityRate)
    }
  }))

  relationChart.setOption({
    tooltip: {
      formatter: (params: any) => {
        if (params.dataType === 'node') {
          return `学生: ${params.data.name}`
        } else if (params.dataType === 'edge') {
          return `${params.data.source} ↔ ${params.data.target}<br/>错答相似度: ${params.data.value}%`
        }
      }
    },
    animationDurationUpdate: 1500,
    animationEasingUpdate: 'quinticInOut',
    series: [
      {
        type: 'graph',
        layout: 'force',
        force: {
          repulsion: 600,
          edgeLength: 180,
          friction: 0.1
        },
        data: nodes,
        links: links,
        roam: true,
        draggable: true,
        label: {
          show: true,
          position: 'right',
          formatter: '{b}'
        },
        edgeLabel: {
          show: true,
          formatter: (params: any) => `${params.data.value}%`,
          fontSize: 12
        },
        lineStyle: {
          opacity: 0.8,
          curveness: 0.1
        }
      }
    ]
  })
}

onMounted(fetchInitialData)
</script>

<style scoped>
.abnormal-detection {
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

.toolbar-card {
  margin-bottom: 16px;
  border-radius: 8px;
}

.toolbar {
  display: flex;
  align-items: center;
}

.search-area {
  display: flex;
  gap: 12px;
  align-items: center;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-top: 20px;
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

.danger-text {
  color: #f56c6c;
}

.table-card {
  border-radius: 8px;
}

.card-header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.student-name {
  font-weight: 600;
  color: #303133;
}

.expand-section {
  padding: 16px 24px;
  background: #fafbfc;
}

.inner-table {
  border-radius: 6px;
  overflow: hidden;
}

.chart-card {
  border-radius: 8px;
}

@media screen and (max-width: 768px) {
  .summary-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
