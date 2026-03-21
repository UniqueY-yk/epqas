<template>
  <div class="examination-paper-management">
    <div class="page-header">
      <h2>试卷模板管理</h2>
      <p class="subtitle">组装和管理各类考试的标准化模板</p>
    </div>

    <!-- Toolbar -->
    <el-card class="toolbar-card" shadow="hover">
      <div class="toolbar">
        <div class="search-area">
          <el-select v-model="searchQuery.courseId" placeholder="选择科目" clearable class="search-input">
            <el-option v-for="c in courses" :key="c.courseId" :label="c.courseName" :value="c.courseId" />
          </el-select>
          <el-input
            v-model="searchQuery.keyword"
            placeholder="搜索试卷标题..."
            clearable
            class="search-input"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon> 查询
          </el-button>
          <el-button @click="resetSearch">
            <el-icon><RefreshRight /></el-icon> 重置
          </el-button>
        </div>
        <div class="action-area">
          <el-button type="success" @click="openDialog()">
            <el-icon><Plus /></el-icon> 新建模板
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- Table -->
    <el-card shadow="hover" class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%" max-height="600px">
        <el-table-column prop="paperId" label="ID" width="80" align="center" />
        <el-table-column prop="title" label="试卷标题" min-width="250" show-overflow-tooltip />
        <el-table-column prop="courseId" label="所属科目" width="150">
          <template #default="scope">
            <el-tag :type="getCourseTagType(scope.row.courseId)" effect="plain">
              {{ getCourseName(scope.row.courseId) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="setterName" label="命题教师" width="120" align="center" />
        <el-table-column prop="totalScore" label="总分" width="100" align="center">
          <template #default="scope">
            <span class="score-text">{{ scope.row.totalScore }} 分</span>
          </template>
        </el-table-column>
        <el-table-column prop="targetDifficulty" label="目标难度" width="120" align="center">
          <template #default="scope">
             <el-rate
                :model-value="scope.row.targetDifficulty * 5"
                disabled
                show-score
                text-color="#ff9900"
                score-template="{value}"
              />
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'Published' ? 'success' : 'info'">
              {{ scope.row.status === 'Published' ? '已发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" align="center">
            <template #default="scope">
                {{ formatDate(scope.row.createdAt) }}
            </template>
        </el-table-column>
        
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="scope">
            <el-button-group>
              <el-button size="small" type="primary" plain @click="openDialog(scope.row)" :icon="Edit" />
              <el-popconfirm title="确定删除该试卷模板吗？" @confirm="handleDelete(scope.row)">
                <template #reference>
                  <el-button size="small" type="danger" plain :icon="Delete" />
                </template>
              </el-popconfirm>
            </el-button-group>
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

    <!-- Dialog -->
    <el-dialog
      :title="dialogMode === 'add' ? '新建试卷模板' : '编辑试卷模板'"
      v-model="dialogVisible"
      width="80%"
      class="paper-dialog"
      destroy-on-close
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px" class="paper-form">
        <div class="form-header-section">
            <el-row :gutter="20">
            <el-col :span="16">
                <el-form-item label="试卷标题" prop="title">
                <el-input v-model="form.title" placeholder="如：2024-2025学年第一学期数据结构期末试卷(A卷)" />
                </el-form-item>
            </el-col>
            <el-col :span="8">
                <el-form-item label="所属科目" prop="courseId">
                <el-select v-model="form.courseId" placeholder="选择科目" style="width: 100%">
                    <el-option v-for="c in courses" :key="c.courseId" :label="c.courseName" :value="c.courseId" />
                </el-select>
                </el-form-item>
            </el-col>
            </el-row>

            <el-row :gutter="20">
            <el-col :span="16">
                <el-form-item label="命题教师" prop="setterId">
                <el-select
                    v-model="form.setterId"
                    placeholder="选择命题教师"
                    style="width: 100%"
                    :disabled="currentRoleId !== 1"
                >
                    <el-option
                        v-for="s in setterOptions"
                        :key="s.userId"
                        :label="s.realName"
                        :value="s.userId"
                    />
                </el-select>
                </el-form-item>
            </el-col>
            </el-row>

            <el-row :gutter="20">
            <el-col :span="8">
                <el-form-item label="考试时长" prop="durationMinutes">
                <el-input-number v-model="form.durationMinutes" :min="30" :max="300" :step="10" style="width: 100%">
                    <template #suffix>分钟</template>
                </el-input-number>
                </el-form-item>
            </el-col>
            <el-col :span="8">
                <el-form-item label="目标难度" prop="targetDifficulty">
                    <el-slider v-model="form.targetDifficulty" :min="0" :max="1" :step="0.1" show-input />
                </el-form-item>
            </el-col>
            <el-col :span="8">
                <el-form-item label="状态" prop="status">
                <el-radio-group v-model="form.status">
                    <el-radio-button label="Draft">草稿</el-radio-button>
                    <el-radio-button label="Published">已发布</el-radio-button>
                </el-radio-group>
                </el-form-item>
            </el-col>
            </el-row>
        </div>

        <el-divider><el-icon><Document /></el-icon> 试题组装</el-divider>

        <div class="question-assembly">
          <div class="assembly-toolbar">
            <span class="stat-text">已选题目: {{ form.questions.length }}</span>
            <span class="stat-text score-high">总分: {{ computedTotalScore }} 分</span>
            <el-button type="primary" size="small" @click="openQuestionSelector" :disabled="!form.courseId">
              <el-icon><Plus /></el-icon> 从题库添加
            </el-button>
          </div>

          <el-table :data="form.questions" border style="width: 100%" class="nested-table">
            <el-table-column label="顺序" width="60" align="center">
                <template #default="scope">
                    <div class="drag-handle">
                        <el-icon><Grid /></el-icon>
                        {{ scope.$index + 1 }}
                    </div>
                </template>
            </el-table-column>
            <el-table-column label="题干摘要">
              <template #default="scope">
                 {{ getQuestionSummary(scope.row.questionId) }}
              </template>
            </el-table-column>
             <el-table-column label="题型" width="100" align="center">
                <template #default="scope">
                    <el-tag size="small" type="info">{{ getQuestionType(scope.row.questionId) }}</el-tag>
                </template>
            </el-table-column>
            <el-table-column label="分值分配" width="160" align="center">
              <template #default="scope">
                <el-input-number v-model="scope.row.scoreValue" :min="0.5" :step="0.5" size="small" />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="80" align="center">
              <template #default="scope">
                <el-button type="danger" link @click="removeQuestion(scope.$index)">移除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitLoading">保存模板</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- Question Selector Dialog -->
    <el-dialog title="选择题目" v-model="selectorVisible" width="70%" append-to-body>
        <div class="selector-content">
            <el-table 
                :data="availableQuestions" 
                v-loading="loadingQuestions"
                border 
                style="width: 100%" 
                max-height="400"
                @selection-change="handleSelectionChange"
                 row-key="questionId"
                ref="selectorTableRef"
            >
                <el-table-column type="selection" width="55" :reserve-selection="true" />
                <el-table-column prop="questionContent" label="题干" show-overflow-tooltip />
                <el-table-column prop="questionType" label="题型" width="120">
                    <template #default="scope">
                        <el-tag :type="getTypeTag(scope.row.questionType)">{{ translateType(scope.row.questionType) }}</el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="initialDifficulty" label="难度" width="80" align="center" />
            </el-table>
             <div class="pagination-container">
            <el-pagination
                v-model:current-page="qQuery.current"
                v-model:page-size="qQuery.size"
                layout="prev, pager, next"
                :total="qTotal"
                @current-change="loadAvailableQuestions"
            />
            </div>
        </div>
        <template #footer>
            <el-button @click="selectorVisible = false">取消</el-button>
            <el-button type="primary" @click="confirmQuestionSelection">确定导入 ({{ selectedQuestions.length }})</el-button>
        </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { Search, Plus, Edit, Delete, RefreshRight, Document, Grid } from '@element-plus/icons-vue'
import { getPapers, getPaperById, addPaper, updatePaper, deletePaper, getSetters, type ExaminationPaperDTO, type PaperQuestionDTO, type SetterInfo } from '../../api/exam'
import { getCourses } from '../../api/academic'
import { getQuestions } from '../../api/question'
import dayjs from 'dayjs'

// --- State ---
const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref<ExaminationPaperDTO[]>([])
const total = ref(0)
const courses = ref<any[]>([])
const setterOptions = ref<SetterInfo[]>([])
const currentRoleId = ref<number>(Number(localStorage.getItem('roleId') || '1'))
const currentUserId = ref<number>(Number(localStorage.getItem('userId') || '0'))
const currentRealName = ref<string>(localStorage.getItem('realName') || '')

const searchQuery = reactive({
  current: 1,
  size: 10,
  courseId: undefined as number | undefined,
  keyword: ''
})

// Dialog
const dialogVisible = ref(false)
const dialogMode = ref<'add' | 'edit'>('add')
const formRef = ref<FormInstance>()
const form = reactive<ExaminationPaperDTO>({
  title: '',
  courseId: undefined as any,
  setterId: undefined as any,
  totalScore: 0,
  durationMinutes: 120,
  targetDifficulty: 0.5,
  status: 'Draft',
  questions: []
})

const rules = {
  title: [{ required: true, message: '请输入试卷标题', trigger: 'blur' }],
  courseId: [{ required: true, message: '请选择所属科目', trigger: 'change' }],
  durationMinutes: [{ required: true, message: '请输入考试时长', trigger: 'blur' }],
  setterId: [{ required: true, message: '请选择试卷命题教师', trigger: 'change' }],
}

// Question Selector
const selectorVisible = ref(false)
const loadingQuestions = ref(false)
const availableQuestions = ref<any[]>([])
const questionDictionary = ref<Record<number, any>>({}) // cache questions for rendering summary
const qTotal = ref(0)
const qQuery = reactive({
    current: 1,
    size: 10,
    courseId: undefined as number | undefined
})
const selectedQuestions = ref<any[]>([])
const selectorTableRef = ref()

// --- Computed ---
const computedTotalScore = computed(() => {
    let sum = 0
    form.questions.forEach(q => {
        sum += (q.scoreValue || 0)
    })
    form.totalScore = sum
    return sum
})

// --- Methods ---
onMounted(async () => {
    await fetchCourses()
    await fetchSetters()
    loadData()
})

const fetchCourses = async () => {
  try {
    const res = await getCourses({ current: 1, size: 100 })
    courses.value = res.data.records
  } catch (e) {
    console.error(e)
  }
}

const fetchSetters = async () => {
  try {
    const res = await getSetters()
    setterOptions.value = res.data || []
    // If teacher is not in the list, add themselves
    if (currentRoleId.value === 2) {
      const exists = setterOptions.value.some(s => s.userId === currentUserId.value)
      if (!exists && currentUserId.value) {
        setterOptions.value.push({
          userId: currentUserId.value,
          realName: currentRealName.value || '当前教师',
          username: ''
        })
      }
    }
  } catch (e) {
    console.error('Failed to fetch setters', e)
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getPapers(searchQuery)
    tableData.value = res.data.records
    total.value = res.data.total
  } catch (error: any) {
    ElMessage.error(error.message || '获取试卷列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  searchQuery.current = 1
  loadData()
}

const resetSearch = () => {
  searchQuery.courseId = undefined
  searchQuery.keyword = ''
  handleSearch()
}

const handleSizeChange = (val: number) => {
  searchQuery.size = val
  loadData()
}

const handleCurrentChange = (val: number) => {
  searchQuery.current = val
  loadData()
}

// Dialog handling
const openDialog = async (row?: ExaminationPaperDTO) => {
  dialogVisible.value = true
  if (formRef.value) {
    formRef.value.resetFields()
  }
  
  if (row && row.paperId) {
    dialogMode.value = 'edit'
    try {
        const res = await getPaperById(row.paperId)
        Object.assign(form, res.data)
        // Ensure questions array exists
        if (!form.questions) form.questions = []
        
        // Fetch details of questions to display in the nested table
        for (const q of form.questions) {
            await cacheQuestionDetailsLocally(q.questionId)
        }
    } catch (e) {
        ElMessage.error('加载试卷详情失败')
    }
  } else {
    dialogMode.value = 'add'
    form.paperId = undefined
    form.title = ''
    form.courseId = undefined as any
    form.totalScore = 0
    form.durationMinutes = 120
    form.targetDifficulty = 0.5
    form.status = 'Draft'
    form.questions = []
    // Auto-assign setterId for non-admin roles
    if (currentRoleId.value !== 1) {
      form.setterId = currentUserId.value
    } else {
      form.setterId = undefined as any
    }
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      // Re-order questions sequentially
      form.questions.forEach((q, index) => {
          q.questionOrder = index + 1
      })
      form.totalScore = computedTotalScore.value

      submitLoading.value = true
      try {
        if (dialogMode.value === 'add') {
          await addPaper(form)
          ElMessage.success('新建试卷模板成功')
        } else {
          await updatePaper(form)
          ElMessage.success('编辑试卷模板成功')
        }
        dialogVisible.value = false
        loadData()
      } catch (error: any) {
         // interceptor handles
      } finally {
        submitLoading.value = false
      }
    }
  })
}

const handleDelete = async (row: ExaminationPaperDTO) => {
  try {
    await deletePaper(row.paperId!)
    ElMessage.success('删除成功')
    loadData()
  } catch (error: any) {}
}

// --- Question Assembly Logic ---

const openQuestionSelector = () => {
    if (!form.courseId) {
        ElMessage.warning('请先选择所属科目')
        return
    }
    selectorVisible.value = true
    qQuery.courseId = form.courseId
    qQuery.current = 1
    selectedQuestions.value = []
    
    // Slight delay to allow ref to mount
    setTimeout(() => {
        if(selectorTableRef.value) {
            selectorTableRef.value.clearSelection()
        }
    }, 100)
    
    loadAvailableQuestions()
}

const loadAvailableQuestions = async () => {
    loadingQuestions.value = true
    try {
        const res = await getQuestions(qQuery)
        availableQuestions.value = res.data.records
        qTotal.value = res.data.total
        
        // Cache these for rendering
        availableQuestions.value.forEach(q => {
            questionDictionary.value[q.questionId] = q
        })
    } catch (e) {
        ElMessage.error('获取题库失败')
    } finally {
        loadingQuestions.value = false
    }
}

const handleSelectionChange = (val: any[]) => {
    selectedQuestions.value = val
}

const confirmQuestionSelection = () => {
    // Avoid duplicates
    const currentQuestionIds = form.questions.map(q => q.questionId)
    
    selectedQuestions.value.forEach(q => {
        if (!currentQuestionIds.includes(q.questionId)) {
            let defaultScore = 5
            if(q.questionType === 'MCQ' || q.questionType === 'TrueFalse') defaultScore = 2
            
            form.questions.push({
                questionId: q.questionId,
                scoreValue: defaultScore,
                questionOrder: form.questions.length + 1
            })
            // ensure it's in dictionary
            questionDictionary.value[q.questionId] = q
        }
    })
    
    selectorVisible.value = false
}

const removeQuestion = (index: number) => {
    form.questions.splice(index, 1)
}

// Helper to fetch details if not in cache (used when opening EDIT mode)
const cacheQuestionDetailsLocally = async (qId: number) => {
    if (questionDictionary.value[qId]) return
    try {
        // Technically this should be a batched API or call a specific get endpoint. 
        // For simplicity, using getQuestions with a keyword hack or fetching it if available.
        // Assuming we need this data purely for display.
        const res = await getQuestions({ current: 1, size: 100, courseId: form.courseId })
         res.data.records.forEach((q: any) => {
            questionDictionary.value[q.questionId] = q
        })
    } catch (e) {}
}

const getQuestionSummary = (qId: number) => {
    const q = questionDictionary.value[qId]
    if (!q) return `题目ID: ${qId}`
    if (q.questionContent.length > 30) {
        return q.questionContent.substring(0, 30) + '...'
    }
    return q.questionContent
}

const getQuestionType = (qId: number) => {
    const q = questionDictionary.value[qId]
    if (!q) return '-'
    return translateType(q.questionType)
}


// --- Utils ---
const getCourseName = (id: number) => {
  const c = courses.value.find(c => c.courseId === id)
  return c ? c.courseName : id
}

const getCourseTagType = (id: number) => {
  const types = ['', 'success', 'info', 'warning', 'danger']
  return types[id % types.length] as any
}

const formatDate = (date: string) => {
    if(!date) return '-'
    return dayjs(date).format('YYYY-MM-DD HH:mm')
}

const translateType = (type: string) => {
  const map: Record<string, string> = {
    'MCQ': '单选题',
    'TrueFalse': '判断题',
    'FillBlank': '填空题',
    'Essay': '论述题'
  }
  return map[type] || type
}

const getTypeTag = (type: string) => {
  const map: Record<string, any> = {
    'MCQ': '',
    'TrueFalse': 'success',
    'FillBlank': 'warning',
    'Essay': 'danger'
  }
  return map[type] || 'info'
}

</script>

<style scoped>
.examination-paper-management {
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
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
}

.search-area {
  display: flex;
  gap: 12px;
  align-items: center;
}

.search-input {
  width: 240px;
}

.table-card {
  border-radius: 8px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.score-text {
    font-weight: bold;
    color: #409EFF;
}

.paper-dialog :deep(.el-dialog__body) {
    padding-top: 10px;
}

.form-header-section {
    background: #f8f9fc;
    padding: 20px 20px 5px 20px;
    border-radius: 8px;
    margin-bottom: 20px;
}

.question-assembly {
    border: 1px solid #ebeef5;
    border-radius: 8px;
    padding: 15px;
}

.assembly-toolbar {
    display: flex;
    align-items: center;
    gap: 15px;
    margin-bottom: 15px;
    background: #ecf5ff;
    padding: 10px 15px;
    border-radius: 4px;
}

.stat-text {
    font-size: 14px;
    color: #606266;
}
.score-high {
    font-weight: bold;
    color: #F56C6C;
    margin-right: auto;
}

.nested-table {
    margin-bottom: 10px;
}

.drag-handle {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 5px;
    color: #909399;
}
</style>
