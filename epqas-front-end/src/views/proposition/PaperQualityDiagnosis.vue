<template>
  <div class="quality-diagnosis">
    <div class="page-header">
      <h2>试卷质量诊断</h2>
      <p class="subtitle">查看您命题的试卷在考试后的质量分析报告</p>
    </div>

    <!-- Table -->
    <el-card shadow="hover" class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="paperTitle" label="试卷名称" min-width="200" show-overflow-tooltip />
        <el-table-column prop="courseName" label="所属课程" width="120" />
        <el-table-column prop="examDate" label="考试日期" width="160">
            <template #default="scope">
                {{ formatDate(scope.row.examDate) }}
            </template>
        </el-table-column>
        <el-table-column label="基础得分分析" align="center">
            <el-table-column prop="averageScore" label="平均分" width="80" align="center" />
            <el-table-column prop="highestScore" label="最高分" width="80" align="center" />
            <el-table-column prop="lowestScore" label="最低分" width="80" align="center" />
        </el-table-column>
        
        <el-table-column label="质量诊断指标" align="center">
            <el-table-column prop="overallDifficulty" label="难度系数" width="100" align="center">
                <template #default="scope">
                    <span :class="getValClass(scope.row.overallDifficulty, 0.4, 0.7)">
                        {{ scope.row.overallDifficulty?.toFixed(2) }}
                    </span>
                </template>
            </el-table-column>
            <el-table-column prop="overallDiscrimination" label="区分度" width="100" align="center">
                <template #default="scope">
                     <span :class="getDiscriminationClass(scope.row.overallDiscrimination)">
                        {{ scope.row.overallDiscrimination?.toFixed(2) }}
                    </span>
                </template>
            </el-table-column>
            <el-table-column prop="reliabilityCoefficient" label="信度(α)" width="100" align="center">
                <template #default="scope">
                    {{ scope.row.reliabilityCoefficient?.toFixed(2) }}
                </template>
            </el-table-column>
            <el-table-column prop="validityCoefficient" label="效度" width="100" align="center">
                <template #default="scope">
                    {{ scope.row.validityCoefficient?.toFixed(2) }}
                </template>
            </el-table-column>
        </el-table-column>
        
        <el-table-column prop="isAbnormal" label="状态" width="100" align="center" fixed="right">
          <template #default="scope">
            <el-tag :type="scope.row.isAbnormal ? 'danger' : 'success'">
              {{ scope.row.isAbnormal ? '质量异常' : '质量达标' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right" align="center">
            <template #default="scope">
                <el-button 
                    link 
                    type="primary" 
                    size="small" 
                    @click="handleAnalysisDetails(scope.row)"
                >
                    试题异动诊断
                </el-button>
                <el-button 
                    link 
                    type="warning" 
                    size="small" 
                    @click="handleCalculate(scope.row)"
                    :loading="calcLoading === scope.row.examId"
                >
                    执行重新计算
                </el-button>
            </template>
        </el-table-column>
      </el-table>

      <!-- Pagination -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="searchQuery.current"
          v-model:page-size="searchQuery.size"
          :page-sizes="[10, 20, 50, 100]"
          :background="true"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- Question Analysis Scatter Plot Dialog -->
    <PaperQuestionDiagnosis ref="questionDiagnosisRef" />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMyPaperAnalyses, calculateExamIndicators, type PaperAnalysisVO } from '../../api/analysis'
import dayjs from 'dayjs'
import PaperQuestionDiagnosis from './PaperQuestionDiagnosis.vue'

// --- State ---
const loading = ref(false)
const calcLoading = ref<number | null>(null)
const tableData = ref<PaperAnalysisVO[]>([])
const total = ref(0)
// For mock purposes or setter role logic, read from storage.
const setterId = Number(localStorage.getItem('userId')) || 3

const searchQuery = reactive({
  current: 1,
  size: 10,
  setterId: setterId
})

// --- Methods ---
onMounted(async () => {
    loadData()
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await getMyPaperAnalyses(searchQuery)
    tableData.value = res.data.records
    total.value = res.data.total
  } catch (error: any) {
    ElMessage.error(error.message || '获取试卷分析列表失败')
  } finally {
    loading.value = false
  }
}

const handleSizeChange = (val: number) => {
  searchQuery.size = val
  loadData()
}

const handleCurrentChange = (val: number) => {
  searchQuery.current = val
  loadData()
}

const formatDate = (date: string) => {
    if(!date) return '-'
    return dayjs(date).format('YYYY-MM-DD HH:mm')
}

// Indicator colors
const getValClass = (val: number, min: number, max: number) => {
    if (val === undefined || val === null) return ''
    if (val < min) return 'text-success' // Hard
    if (val > max) return 'text-danger'  // Easy
    return 'text-normal'
}

const getDiscriminationClass = (val: number) => {
    if (val === undefined || val === null) return ''
    if (val < 0.2) return 'text-danger' // Poor discrimination
    if (val >= 0.4) return 'text-success' // Excellent
    return 'text-normal'
}

const handleCalculate = async (row: PaperAnalysisVO) => {
    if (!row.examId) {
        ElMessage.warning('Exam ID is missing.')
        return
    }
    calcLoading.value = row.examId
    try {
        await calculateExamIndicators(row.examId)
        ElMessage.success('计算完成，指标已更新！')
        loadData()
    } catch (error: any) {
        ElMessage.error(error.message || '计算失败，请检查是否已关联考生成绩数据。')
    } finally {
        calcLoading.value = null
    }
}

const questionDiagnosisRef = ref<InstanceType<typeof PaperQuestionDiagnosis> | null>(null)

const handleAnalysisDetails = (row: PaperAnalysisVO) => {
    if (!row.examId) {
        ElMessage.warning('正在后台计算指标中，请稍后再试。')
        return
    }
    questionDiagnosisRef.value?.openDialog(row.examId, row.paperTitle)
}

</script>

<style scoped>
.quality-diagnosis {
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

.table-card {
  border-radius: 8px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.text-success {
    color: #67C23A;
    font-weight: bold;
}
.text-danger {
    color: #F56C6C;
    font-weight: bold;
}
.text-normal {
    color: #606266;
}
</style>
