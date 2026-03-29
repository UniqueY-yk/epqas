<template>
  <div class="page-container">
    <div class="page-header">
      <el-button @click="router.back()" :icon="ArrowLeft" class="back-btn">返回</el-button>
      <h2>成绩录入 - {{ paperName || '加载中...' }} ({{ className || '加载中...' }})</h2>
      <p class="subtitle">录入学生总成绩或详细的试题作答情况。</p>
    </div>

    <el-card shadow="never" class="table-card">
      <el-table :data="studentData" v-loading="loading" stripe style="width: 100%" class="custom-table" border>
        <el-table-column prop="studentNumber" label="学号" min-width="120" />
        <el-table-column label="真实姓名" min-width="120">
          <template #default="{ row }">
            {{ getUserName(row.studentId) }}
          </template>
        </el-table-column>
        <el-table-column label="总成绩 (百分制)" min-width="160">
          <template #default="{ row }">
            <el-input-number v-model="row.totalScore" :min="0" :max="100" class="score-input-table" />
          </template>
        </el-table-column>
        <el-table-column label="缺考状态" min-width="120" align="center">
          <template #default="{ row }">
            <el-switch v-model="row.isAbsent" active-text="缺考" inactive-text="正常" />
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="saveSingleResult(row)" :loading="row.saving">
              保存
            </el-button>
            <el-button type="success" size="small" plain @click="openDetailedEntry(row)" :disabled="row.isAbsent">
              详细答题录入
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      :title="`详细答题录入 - ${currentStudentName}`"
      v-model="dialogVisible"
      width="90%"
      top="5vh"
      append-to-body
      class="custom-dialog-wide"
    >
      <div v-loading="paperLoading" class="paper-questions-form">
        <el-form label-position="top" label-width="120px" class="modern-form">
           <div v-for="(group, type) in groupedQuestions" :key="type" class="question-type-section">
              <h3 class="type-header">{{ formatQuestionType(type as string) }}</h3>
              <div v-for="q in group" :key="q.questionId" class="question-block">
                <div class="q-header">
                  <span class="q-number">第 {{ getGlobalIndex(q.questionId) }} 题</span>
                  <span class="q-score">({{ q.scoreValue }}分)</span>
                </div>
                <p class="q-content">{{ q.questionContent }}</p>
                
                <div class="reference-answer-block" v-if="q.correctAnswer">
                    <el-tag type="success" effect="plain" class="ref-tag">参考答案</el-tag>
                    <span class="ref-content" v-if="q.questionType !== 'MultipleChoice'">{{ q.correctAnswer }}</span>
                    <span class="ref-content" v-else>{{ parseOptions(q.optionsJson) && parseOptions(q.optionsJson)[q.correctAnswer] ? q.correctAnswer + '. ' + parseOptions(q.optionsJson)[q.correctAnswer] : q.correctAnswer }}</span>
                </div>

                <div class="answer-inputs" v-if="currentStudentAnswers[q.questionId]">
                  <el-form-item label="学生选项/答案" class="flex-grow">
                    
                    <template v-if="q.questionType === 'SingleChoice'">
                        <el-radio-group v-model="currentStudentAnswers[q.questionId].studentChoice" @change="autoScore(q)">
                            <el-radio v-for="(text, key) in parseOptions(q.optionsJson)" :key="key" :value="String(key)">
                                {{ key }}. {{ text }}
                            </el-radio>
                        </el-radio-group>
                    </template>

                    <template v-else-if="q.questionType === 'MultipleChoice'">
                        <el-checkbox-group v-model="currentStudentAnswers[q.questionId].studentChoiceArray" @change="autoScore(q)">
                            <el-checkbox v-for="(text, key) in parseOptions(q.optionsJson)" :key="key" :value="String(key)">
                                {{ key }}. {{ text }}
                            </el-checkbox>
                        </el-checkbox-group>
                    </template>

                    <template v-else-if="q.questionType === 'TrueFalse'">
                        <el-radio-group v-model="currentStudentAnswers[q.questionId].studentChoice" @change="autoScore(q)">
                            <el-radio value="True">正确 (T)</el-radio>
                            <el-radio value="False">错误 (F)</el-radio>
                        </el-radio-group>
                    </template>

                    <template v-else-if="q.questionType === 'FillBlank'">
                        <el-input 
                          v-model="currentStudentAnswers[q.questionId].studentChoice" 
                          placeholder="请输入学生实际作答内容"
                          type="textarea"
                          :rows="2"
                          @change="autoScore(q)"
                        />
                    </template>

                    <template v-else>
                        <el-input 
                          v-model="currentStudentAnswers[q.questionId].studentChoice" 
                          placeholder="请输入学生实际作答内容"
                          type="textarea"
                          :rows="2"
                        />
                    </template>

                  </el-form-item>
                  <el-form-item label="本次得分" class="score-input">
                     <div class="score-input-wrapper">
                         <el-input-number 
                           v-model="currentStudentAnswers[q.questionId].scoreObtained" 
                           :min="0" 
                           :max="Number(q.scoreValue)" 
                           :step="0.5"
                           class="score-number-width"
                         />
                         <el-button type="success" plain size="small" @click="setFullMarks(q.questionId, q)">满分</el-button>
                     </div>
                  </el-form-item>
                </div>
              </div>
           </div>
        </el-form>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <div class="footer-stats">
            <span>总得分: <span class="total-calc">{{ calculatedTotal }}</span></span>
          </div>
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitDetailedAnswers" :loading="submitLoading">保存答题与成绩</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { getStudentsByClassId, getClasses } from '@/api/academic'
import { 
  getExaminationsPage, 
  getPaperById, 
  batchSaveStudentResult, 
  getResultsByExamId, 
  getAnswersByResultId,
  getResultByExamAndStudent,
  ExaminationDTO
} from '@/api/exam'
import { getUsers } from '@/api/user'

const route = useRoute()
const router = useRouter()
const examId = Number(route.params.examId)

const loading = ref(false)
const studentData = ref<any[]>([])
const usersMap = ref<Record<number, string>>({})

const paperName = ref('')
const className = ref('')
const currentExamination = ref<ExaminationDTO | null>(null)

// Detailed Entry state
const dialogVisible = ref(false)
const paperLoading = ref(false)
const submitLoading = ref(false)
const paperQuestions = ref<any[]>([])
const currentStudent = ref<any>(null)
const currentStudentAnswers = ref<Record<number, any>>({})
const existingResultsMap = ref<Record<string, any>>({})

const currentStudentName = computed(() => {
  if (!currentStudent.value) return ''
  return getUserName(currentStudent.value.studentId)
})

const groupedQuestions = computed(() => {
  const groups: Record<string, any[]> = {}
  paperQuestions.value.forEach(q => {
    const type = q.questionType || 'Other'
    if (!groups[type]) groups[type] = []
    groups[type].push(q)
  })
  return groups
})

const calculatedTotal = computed(() => {
  let total = 0
  for (const qId in currentStudentAnswers.value) {
    total += currentStudentAnswers.value[qId].scoreObtained || 0
  }
  return total
})

const formatQuestionType = (type: string) => {
  const map: Record<string, string> = {
    'SingleChoice': '单选题',
    'MultipleChoice': '多选题',
    'TrueFalse': '判断题',
    'FillBlank': '填空题',
    'ShortAnswer': '简答题',
    'Other': '其他题型'
  }
  return map[type] || type
}

const getGlobalIndex = (qId: number) => {
  return paperQuestions.value.findIndex(q => q.questionId === qId) + 1
}

const parseOptions = (optionsJson: string) => {
  if (!optionsJson) return {}
  try {
    return JSON.parse(optionsJson)
  } catch(e) {
    return {}
  }
}

const setFullMarks = (qId: number, q: any) => {
  const ans = currentStudentAnswers.value[qId]
  if (ans) {
    ans.scoreObtained = Number(q.scoreValue)
    let correctStr = q.correctAnswer || ''
    
    if (q.questionType === 'MultipleChoice') {
      try {
        const parsed = JSON.parse(correctStr)
        ans.studentChoiceArray = Array.isArray(parsed) ? parsed.map(String) : []
      } catch (e) {
        // Fallback for non-JSON string
        ans.studentChoiceArray = correctStr.split(',').map(s => s.trim()).filter(s => s)
      }
      ans.studentChoice = correctStr
    } else {
      ans.studentChoice = correctStr
    }
  }
}

const autoScore = (q: any) => {
  const ans = currentStudentAnswers.value[q.questionId]
  if (!ans || !q.correctAnswer) return

  const type = q.questionType
  const correctStr = String(q.correctAnswer).trim()
  
  if (['SingleChoice', 'TrueFalse'].includes(type)) {
     if (String(ans.studentChoice).trim() === correctStr) {
         ans.scoreObtained = Number(q.scoreValue)
     } else {
         ans.scoreObtained = 0
     }
  } else if (type === 'MultipleChoice') {
     let correctArr: string[] = []
     try {
        const parsed = JSON.parse(correctStr)
        correctArr = Array.isArray(parsed) ? parsed.map(String) : []
     } catch (e) {
        correctArr = correctStr.split(',').map(s => s.trim()).filter(Boolean)
     }
     
     const studentArr = ans.studentChoiceArray || []
     
     const sortedCorrect = [...correctArr].sort().join(',')
     const sortedStudent = [...studentArr].sort().join(',')
     
     if (sortedCorrect === sortedStudent && sortedCorrect !== '') {
         ans.scoreObtained = Number(q.scoreValue)
     } else {
         ans.scoreObtained = 0
     }
  } else if (type === 'FillBlank') {
      if (String(ans.studentChoice).trim() === correctStr) {
         ans.scoreObtained = Number(q.scoreValue)
      } else {
         ans.scoreObtained = 0
      }
  }
}

onMounted(async () => {
  if (!examId) {
    ElMessage.error('无效的考试ID')
    return
  }
  await fetchInitialData()
})

const fetchInitialData = async () => {
  loading.value = true
  try {
    // 1. Get Examination Info
    const examRes = await getExaminationsPage({ current: 1, size: 500 })
    const records = examRes.data?.records || []
    const exam = records.find((e: any) => e.examId === examId)
    
    if (!exam) {
      ElMessage.error('找不到考试记录')
      return
    }
    currentExamination.value = exam

    // 2. Resolve Names
    const [papersRes, classesRes, usersRes, resultsRes] = await Promise.all([
      getPaperById(exam.paperId),
      getClasses({ current: 1, size: 500 }),
      getUsers({ current: 1, size: 500 }),
      getResultsByExamId(examId) // Fetch existing results
    ])
    
    paperName.value = papersRes.data?.title || `试卷 ${exam.paperId}`
    const classes = classesRes.data?.records || []
    const c = classes.find((c: any) => c.classId === exam.classId)
    className.value = c?.className || `班级 ${exam.classId}`

    const users = usersRes.data?.records || []
    users.forEach((u: any) => {
      usersMap.value[u.userId] = u.realName
    })

    const results = resultsRes.data || []
    results.forEach((r: any) => {
      const sId = String(r.studentId)
      existingResultsMap.value[sId] = r
    })

    // 3. Fetch Students for the Class
    const studentsRes = await getStudentsByClassId(exam.classId)
    const students = studentsRes.data || []
    
    studentData.value = students.map((s: any) => {
      const sId = String(s.studentId)
      const existing = existingResultsMap.value[sId]
      return {
        ...s,
        totalScore: existing ? existing.totalScore : 0,
        isAbsent: existing ? existing.isAbsent : false,
        resultId: existing ? existing.resultId : null,
        saving: false
      }
    })

  } catch (error) {
    console.error(error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const getUserName = (userId: number) => {
  return usersMap.value[userId] || `用户ID:${userId}`
}

const saveSingleResult = async (row: any) => {
  row.saving = true
  try {
    const payload = {
      result: {
        resultId: row.resultId,
        examId: examId,
        studentId: row.studentId,
        totalScore: row.totalScore,
        isAbsent: row.isAbsent
      },
      answers: [] // Ignore detailed answers on simple row save
    }
    await batchSaveStudentResult(payload)
    ElMessage.success(`${getUserName(row.studentId)} 的成绩保存成功`)
    await fetchInitialData()
  } catch (error) {
    console.error(error)
    ElMessage.error('保存失败')
  } finally {
    row.saving = false
  }
}

const openDetailedEntry = async (row: any) => {
  currentStudent.value = row
  dialogVisible.value = true
  paperLoading.value = true
  
  try {
    if (!currentExamination.value) return
    
    // 1. Fetch Paper with questions
    const paperRes = await getPaperById(currentExamination.value.paperId as number)
    paperQuestions.value = paperRes.data?.questions || []

    // 2. Fetch existing result and answers
    currentStudentAnswers.value = {}
    
    let resId = row.resultId
    
    // Robust fallback: if resultId is missing, check backend specifically for this student and exam
    if (!resId) {
      const resLookup = await getResultByExamAndStudent(Number(examId), row.studentId)
      if (resLookup.data) {
        resId = resLookup.data.resultId
        // Update the row object so we don't look up again
        row.resultId = resId
        row.totalScore = resLookup.data.totalScore
        row.isAbsent = resLookup.data.isAbsent
      }
    }

    if (resId) {
      const res = await getAnswersByResultId(resId)
      const existingAnswers = res.data || []
      
      paperQuestions.value.forEach(q => {
        const existing = existingAnswers.find((a: any) => a.questionId === q.questionId)
        let choice = existing ? existing.studentChoice : ''
        let choiceArray: string[] = []
        if (q.questionType === 'MultipleChoice' && choice) {
          try {
            choiceArray = JSON.parse(choice)
            if (!Array.isArray(choiceArray)) choiceArray = choice.split(',').map(s => s.trim()).filter(Boolean)
          } catch(e) {
             choiceArray = choice.split(',').map(s => s.trim()).filter(Boolean)
          }
        }
        
        currentStudentAnswers.value[q.questionId] = {
          questionId: q.questionId,
          studentChoice: choice,
          studentChoiceArray: choiceArray,
          scoreObtained: existing ? existing.scoreObtained : 0,
          answerId: existing ? existing.answerId : null
        }
      })
    } else {
      // Initialize with defaults
      paperQuestions.value.forEach(q => {
        currentStudentAnswers.value[q.questionId] = {
          questionId: q.questionId,
          studentChoice: '',
          studentChoiceArray: [],
          scoreObtained: 0,
          answerId: null
        }
      })
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('加载答题数据失败')
  } finally {
    paperLoading.value = false
  }
}

const submitDetailedAnswers = async () => {
  submitLoading.value = true
  try {
    const row = currentStudent.value
    
    // Auto calculate total
    let calcTotal = 0
    const answersList = []
    for (const qId in currentStudentAnswers.value) {
      const ans = currentStudentAnswers.value[qId]
      const q = paperQuestions.value.find(pq => pq.questionId === Number(qId))
      
      if (q && q.questionType === 'MultipleChoice') {
        if (ans.studentChoiceArray && Array.isArray(ans.studentChoiceArray)) {
          // Store multiple choices as JSON string
          ans.studentChoice = JSON.stringify(ans.studentChoiceArray.sort())
        }
      }
      
      calcTotal += ans.scoreObtained || 0
      answersList.push(ans)
    }

    const payload = {
      result: {
        resultId: row.resultId,
        examId: examId,
        studentId: row.studentId,
        totalScore: calcTotal,
        isAbsent: row.isAbsent
      },
      answers: answersList
    }
    
    await batchSaveStudentResult(payload)
    ElMessage.success('详细答题记录保存成功')
    dialogVisible.value = false
    await fetchInitialData()
  } catch (error) {
    console.error(error)
    ElMessage.error('保存失败')
  } finally {
    submitLoading.value = false
  }
}
</script>

<style scoped>
.page-container {
  padding: 24px;
}

.page-header {
  margin-bottom: 24px;
  position: relative;
}

.back-btn {
  margin-bottom: 12px;
}

.page-header h2 {
  font-size: 24px;
  color: #303133;
  margin: 0 0 8px 0;
  font-weight: 600;
}

.subtitle {
  color: #909399;
  font-size: 14px;
  margin: 0;
}

.table-card {
  border-radius: 8px;
  border: 1px solid #ebeef5;
}

.custom-table {
  border-radius: 8px;
  overflow: hidden;
}

.score-input-table {
  width: 100%;
  max-width: 140px;
}

.paper-questions-form {
  min-height: 200px;
  max-height: 70vh;
  overflow-y: auto;
  padding: 0 20px;
}

.question-type-section {
  margin-bottom: 30px;
}

.type-header {
  border-left: 4px solid #409eff;
  padding-left: 12px;
  margin-bottom: 20px;
  font-size: 18px;
  color: #303133;
  background: #f0f7ff;
  padding-top: 8px;
  padding-bottom: 8px;
}

.question-block {
  background: #ffffff;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  transition: box-shadow 0.3s;
}

.question-block:hover {
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
}

.q-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 10px;
  border-bottom: 1px dashed #ebeef5;
}

.q-number {
  font-weight: 600;
  color: #409eff;
  font-size: 16px;
}

.q-score {
  color: #909399;
  font-size: 14px;
}

.q-content {
  color: #606266;
  margin-bottom: 20px;
  white-space: pre-wrap;
  line-height: 1.6;
  font-size: 15px;
}

.answer-inputs {
  display: flex;
  gap: 24px;
  align-items: flex-end;
  background: #f9fafc;
  padding: 16px;
  border-radius: 6px;
}

.flex-grow {
  flex: 1;
}

.score-input {
  width: 180px;
}

.dialog-footer {
  padding: 16px 32px;
  border-top: 1px solid #ebeef5;
  background: #fafafa;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 16px;
}

.footer-stats {
  margin-right: auto;
  font-size: 16px;
  color: #606266;
}

.total-calc {
  color: #f56c6c;
  font-weight: 600;
  font-size: 20px;
}

.reference-answer-block {
  background-color: #f0f9eb;
  padding: 12px 16px;
  border-radius: 6px;
  margin-bottom: 16px;
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.ref-tag {
  font-weight: 600;
}

.ref-content {
  color: #67c23a;
  font-size: 15px;
  line-height: 1.6;
  white-space: pre-wrap;
}

.score-input-wrapper {
  display: flex; 
  align-items: center; 
  gap: 12px;
}

.score-number-width {
  width: 130px;
}
</style>
