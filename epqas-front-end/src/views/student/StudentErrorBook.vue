<template>
  <div class="error-book">
    <div class="page-header">
      <h2>我的错题本</h2>
      <p class="subtitle">记录着您在历次考试中答错或未拿满分的题目，助您精准复习，查漏补缺</p>
    </div>

    <!-- Toolbar -->
    <el-card class="toolbar-card" shadow="never">
      <div class="toolbar">
        <div class="search-area">
          <el-select v-model="filters.courseId" placeholder="选择课程" style="width: 180px;" clearable @change="handleFilterChange">
            <el-option v-for="c in courseList" :key="c.courseId" :label="c.courseName" :value="c.courseId" />
          </el-select>
          <el-select v-model="filters.examId" placeholder="选择考试" style="width: 280px;" clearable filterable @change="handleFilterChange">
            <el-option v-for="e in examList" :key="e.examId" :label="getExamName(e.examId)" :value="e.examId" />
          </el-select>
          <el-select v-model="filters.questionType" placeholder="题目类型" style="width: 150px;" clearable @change="handleFilterChange">
            <el-option label="单选题" value="SingleChoice" />
            <el-option label="多选题" value="MultipleChoice" />
            <el-option label="判断题" value="TrueFalse" />
            <el-option label="填空题" value="FillBlank" />
            <el-option label="简答题" value="ShortAnswer" />
          </el-select>
        </div>
        <div class="stats-area">
          <el-tag type="danger" effect="dark" size="large">累计错题: {{ totalErrors }} 道</el-tag>
        </div>
      </div>
    </el-card>

    <!-- Error List with Infinite Scrolling -->
    <div 
      class="error-list-container" 
      v-infinite-scroll="loadMore" 
      :infinite-scroll-disabled="loading || noMore"
      :infinite-scroll-distance="100"
    >
      <div class="error-list">
        <el-empty 
          v-if="!loading && errorList.length === 0" 
          description="太棒了！符合条件的错题本空空如也"
          image-size="200"
        />

        <div 
          v-for="(err, index) in errorList" 
          :key="`${err.questionId}-${index}`" 
          class="question-card status-wrong"
        >
          <div class="q-header">
            <div class="q-title-area">
              <span class="q-index">#{{ index + 1 }}</span>
              <el-tag size="small" type="info" class="q-type-tag">{{ err.questionType }}</el-tag>
              <span class="q-exam-context text-muted">
                <el-icon><Calendar /></el-icon>
                {{ getExamName(err.examId) }}
                ({{ err.examDate ? err.examDate.substring(0,10) : '未知日期' }})
              </span>
            </div>
            <span class="q-score-info">得分: <strong>{{ err.scoreObtained }}</strong> / {{ err.maxScore }}</span>
          </div>
          
          <div class="q-body">
            <div class="q-content" v-html="err.questionContent"></div>
            
            <div class="q-options" v-if="getOptions(err.optionsJson).length > 0">
              <div 
                v-for="opt in getOptions(err.optionsJson)" 
                :key="opt.key" 
                class="q-option-item"
                :class="{ 
                  'is-correct-opt': parseAndFormat(err.correctAnswer) === opt.key || parseAndFormat(err.correctAnswer)?.includes(opt.key),
                  'is-my-opt': parseAndFormat(err.studentChoice) === opt.key || parseAndFormat(err.studentChoice)?.includes(opt.key)
                }"
              >
                <span class="opt-key">{{ opt.key }}.</span>
                <span class="opt-val">{{ opt.value }}</span>
                <el-tag size="small" type="success" effect="dark" v-if="parseAndFormat(err.correctAnswer) === opt.key || parseAndFormat(err.correctAnswer)?.includes(opt.key)" style="margin-left: 10px;">标准答案</el-tag>
                <el-tag size="small" type="danger" v-if="parseAndFormat(err.studentChoice) === opt.key || parseAndFormat(err.studentChoice)?.includes(opt.key)" style="margin-left: 10px;">我的错误选择</el-tag>
              </div>
            </div>
          </div>

          <div class="q-footer" :class="{ 'q-footer-vertical': ['FillBlank', 'ShortAnswer', 'Other'].includes(err.questionType) }">
            <div class="answer-compare" :class="{ 'is-vertical': ['FillBlank', 'ShortAnswer', 'Other'].includes(err.questionType) }">
              <div class="answer-box my-answer">
                <span class="ans-label">我的解答:</span>
                <span class="ans-value text-danger">
                  {{ parseAndFormat(err.studentChoice) || '未作答' }}
                </span>
              </div>
              <div class="answer-box std-answer">
                <span class="ans-label">正确答案:</span>
                <span class="ans-value text-success">{{ parseAndFormat(err.correctAnswer) || '略' }}</span>
              </div>
            </div>
          </div>

          <!-- Knowledge Point Footer -->
          <div class="kp-footer" v-if="err.knowledgePoints && err.knowledgePoints.length > 0">
            <span class="kp-label"><el-icon><WarningFilled /></el-icon> 关联薄弱点：</span>
            <el-tag 
              v-for="kp in err.knowledgePoints" 
              :key="kp" 
              type="warning" 
              size="small" 
              effect="light" 
              class="kp-tag"
            >
              {{ kp }}
            </el-tag>
          </div>
        </div>
      </div>

      <!-- Loading and End states for Infinite Scroll -->
      <div v-if="loading" class="scroll-status-block">
        <el-icon class="is-loading"><Loading /></el-icon> 加载中...
      </div>
      <div v-if="noMore && errorList.length > 0" class="scroll-status-block text-muted">
        --- 到底啦，没有更多错题了 ---
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, reactive } from 'vue'
import { getStudentErrorQuestions, getExaminationsPage, getPapers, getStudentScores } from '@/api/exam'
import { getClasses, getCourses } from '@/api/academic'
import { ElMessage } from 'element-plus'
import { Calendar, WarningFilled, Loading } from '@element-plus/icons-vue'

const userId = Number(localStorage.getItem('userId') || '0')
const loading = ref(false)
const errorList = ref<any[]>([])
const totalErrors = ref(0) // Mapped directly from backend Page.total

const filters = reactive({
  courseId: null,
  examId: null,
  questionType: ''
})

const pagination = reactive({
  current: 1,
  size: 5
})

const noMore = computed(() => {
  return errorList.value.length >= totalErrors.value && totalErrors.value > 0
})

// Lookups for exam context filters
const examList = ref<any[]>([])
const paperList = ref<any[]>([])
const classList = ref<any[]>([])
const courseList = ref<any[]>([])

const handleFilterChange = () => {
  errorList.value = []
  pagination.current = 1
  totalErrors.value = 0
  fetchErrorData()
}

const loadMore = () => {
  if (loading.value || noMore.value) return;
  pagination.current += 1
  fetchErrorData()
}

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

const getOptions = (optionsJson: string | null) => {
  let options: any[] = []
  if (optionsJson) {
    try { 
      const parsed = JSON.parse(optionsJson) 
      if (Array.isArray(parsed)) {
        options = parsed.map((item, i) => ({ key: String.fromCharCode(65 + i), value: item }))
      } else if (typeof parsed === 'object') {
        options = Object.keys(parsed).map(k => ({ key: k, value: parsed[k] }))
      }
    } catch (e) {}
  }
  return options
}

const getExamName = (examId: number) => {
  if (!examId) return '未知测验'
  const exam = examList.value.find(e => e.examId === examId)
  if (!exam) return `考试ID: ${examId}`
  const paper = paperList.value.find(p => p.paperId === exam.paperId)
  const cls = classList.value.find(c => c.classId === exam.classId)
  const paperName = paper ? paper.title : `试卷${exam.paperId}`
  const className = cls ? cls.className : ''
  return className ? `${paperName} (${className})` : paperName
}

const fetchInitialContext = async () => {
  try {
    const [examsRes, papersRes, classesRes, coursesRes, scoresRes] = await Promise.all([
      getExaminationsPage({ current: 1, size: 500 }),
      getPapers({ current: 1, size: 500 }),
      getClasses({ page: 1, size: 200 }),
      getCourses({ page: 1, size: 200 }),
      getStudentScores(userId)
    ])
    
    const allExams = examsRes.data?.records || examsRes.data || []
    const allPapers = papersRes.data?.records || papersRes.data || []
    classList.value = classesRes.data?.records || classesRes.data || []
    const allCourses = coursesRes.data?.records || coursesRes.data || []
    const userScores = scoresRes.data || []
    
    // 1. Filter exams to ONLY those taken by the student
    const takenExamIds = Array.from(new Set(userScores.map((s: any) => s.examId)))
    examList.value = allExams.filter((e: any) => takenExamIds.includes(e.examId))
    
    // 2. Map taken exams to papers to find courses taken
    const validPaperIds = examList.value.map((e: any) => e.paperId)
    const validPapers = allPapers.filter((p: any) => validPaperIds.includes(p.paperId))
    const takenCourseIds = Array.from(new Set(validPapers.map((p: any) => p.courseId)))
    
    // 3. Filter course dropdown correctly
    courseList.value = allCourses.filter((c: any) => takenCourseIds.includes(c.courseId))
    
    paperList.value = allPapers // Retain full map for rendering test names successfully
    
    // Once context is loaded, fetch first page of errors!
    fetchErrorData()
  } catch (e) {
    console.error(e)
    ElMessage.error('加载筛选数据失败')
  }
}

const fetchErrorData = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.current,
      size: pagination.size,
      courseId: filters.courseId || undefined,
      examId: filters.examId || undefined,
      questionType: filters.questionType || undefined
    }
    
    const res = await getStudentErrorQuestions(userId, params)
    
    // Page payload contains .records and .total
    const newRecords = res.data?.records || []
    totalErrors.value = res.data?.total || 0
    
    if (pagination.current === 1) {
      errorList.value = newRecords
    } else {
      errorList.value = [...errorList.value, ...newRecords]
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('加载错题本数据失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchInitialContext()
})
</script>

<style scoped>
.error-book {
  padding: 24px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.page-header { margin-bottom: 24px; }
.page-header h2 { margin: 0; font-size: 26px; color: #303133; font-weight: 700; }
.subtitle { margin: 8px 0 0; color: #909399; font-size: 15px; }

.toolbar-card { border-radius: 8px; margin-bottom: 24px; }
.toolbar { display: flex; justify-content: space-between; align-items: center; }

.error-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.question-card {
  border: 1px solid #fde2e2;
  border-radius: 12px;
  padding: 24px;
  background-color: #fffaf9;
  position: relative;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(245, 108, 108, 0.05);
  transition: transform 0.2s;
}

.question-card:hover { transform: translateY(-2px); }

.question-card::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 5px;
  background-color: #f56c6c;
}

.q-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.q-title-area { display: flex; align-items: center; gap: 12px; }
.q-index { font-size: 20px; font-weight: 800; color: #f56c6c; font-style: italic; }
.q-exam-context { font-size: 14px; display: flex; align-items: center; gap: 4px; }
.text-muted { color: #909399; }

.q-score-info { font-size: 15px; color: #606266; }
.q-score-info strong { color: #f56c6c; font-size: 20px; font-weight: 800; }

.q-body { margin-bottom: 24px; }
.q-content { font-size: 16px; line-height: 1.6; color: #303133; margin-bottom: 16px; }

.q-options { display: flex; flex-direction: column; gap: 12px; }
.q-option-item {
  padding: 12px 16px;
  border-radius: 8px;
  background-color: #ffffff;
  border: 1px solid #ebeef5;
}
.q-option-item.is-correct-opt { border-color: #67c23a; background-color: #f0f9eb; }
.q-option-item.is-my-opt { border-color: #f56c6c; background-color: #fef0f0; border-style: dashed; }

.opt-key { font-weight: bold; margin-right: 8px; }

.q-footer {
  padding-top: 16px;
  border-top: 1px dashed #e4e7ed;
  margin-bottom: 16px;
}

.q-footer-vertical { align-items: flex-start; }

.answer-compare { display: flex; gap: 40px; }
.answer-compare.is-vertical { flex-direction: column; gap: 16px; max-width: 90%; }

.answer-box { display: flex; align-items: center; gap: 10px; }
.answer-compare.is-vertical .answer-box { flex-direction: column; align-items: flex-start; gap: 6px; }

.ans-label { color: #606266; font-size: 14px; font-weight: 600; }
.ans-value { font-weight: 700; font-size: 16px; }
.answer-compare.is-vertical .ans-value { white-space: pre-wrap; line-height: 1.6; font-weight: normal; }

.text-success { color: #67c23a; }
.text-danger { color: #f56c6c; }

.kp-footer {
  padding: 12px 16px;
  background: #fff8f8;
  border-radius: 8px;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.kp-label { font-size: 14px; color: #e6a23c; font-weight: 600; display: flex; align-items: center; gap: 4px; }
.kp-tag { font-weight: 600; }

.scroll-status-block {
  text-align: center;
  padding: 16px;
  margin-top: 10px;
  font-size: 14px;
}
.scroll-status-block .is-loading {
  animation: loading-rotate 2s linear infinite;
}
@keyframes loading-rotate {
  100% {
    transform: rotate(360deg);
  }
}

</style>
