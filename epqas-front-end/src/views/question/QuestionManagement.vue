<template>
  <div class="question-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>题目管理</span>
          <el-button type="primary" :icon="Plus" @click="handleAdd">新增题目</el-button>
        </div>
      </template>

      <el-form :inline="true" :model="searchForm">
        <el-form-item label="所属课程">
          <el-select v-model="searchForm.courseId" placeholder="请选择课程" clearable @change="fetchData" filterable>
            <el-option
              v-for="course in courses"
              :key="course.courseId"
              :label="course.courseName"
              :value="course.courseId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="题目类型">
          <el-select v-model="searchForm.type" placeholder="所有类型" clearable @change="fetchData">
            <el-option label="单选题" value="MCQ" />
            <el-option label="判断题" value="TrueFalse" />
            <el-option label="填空题" value="FillBlank" />
            <el-option label="主观题" value="Essay" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="搜索题目内容" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="fetchData">搜索</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" style="width: 100%" v-loading="loading" stripe border>
        <template #empty>
          <el-empty description="暂无数据" />
        </template>
        <el-table-column prop="questionId" label="ID" width="80" />
        <el-table-column prop="questionContent" label="题目内容" show-overflow-tooltip />
        <el-table-column prop="courseId" label="所属课程" width="150">
          <template #default="scope">
             {{ getCourseName(scope.row.courseId) }}
          </template>
        </el-table-column>
        <el-table-column prop="questionType" label="题型" width="100">
           <template #default="scope">
             <el-tag :type="getTypeTag(scope.row.questionType)">{{ getTypeName(scope.row.questionType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="initialDifficulty" label="初始难度" width="100" />
        <el-table-column label="操作" width="180">
          <template #default="scope">
            <el-button size="small" :icon="Edit" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" :icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <el-pagination
        v-if="total > 0"
        background
        layout="prev, pager, next"
        :total="total"
        v-model:current-page="currentPage"
        @current-change="handleCurrentChange"
        class="pagination"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="800px" destroy-on-close>
      <el-form :model="form" label-width="100px" :rules="rules" ref="qFormRef">
        
        <el-row :gutter="20">
            <el-col :span="12">
                <el-form-item label="所属课程" prop="courseId">
                  <el-select v-model="form.courseId" placeholder="请选择课程" @change="onCourseChange" filterable style="width: 100%;">
                    <el-option
                      v-for="course in courses"
                      :key="course.courseId"
                      :label="course.courseName"
                      :value="course.courseId"
                    />
                  </el-select>
                </el-form-item>
            </el-col>
            <el-col :span="12">
                 <el-form-item label="知识点标签" prop="pointIds">
                  <el-select v-model="form.pointIds" multiple placeholder="请选择知识点" :disabled="!form.courseId" style="width: 100%;">
                    <el-option
                      v-for="kp in knowledgePoints"
                      :key="kp.pointId"
                      :label="kp.pointName"
                      :value="kp.pointId"
                    />
                  </el-select>
                </el-form-item>
            </el-col>
        </el-row>

        <el-row :gutter="20">
            <el-col :span="12">
                <el-form-item label="题目类型" prop="questionType">
                  <el-select v-model="form.questionType" placeholder="请选择题型" style="width: 100%;">
                    <el-option label="单项选择题" value="MCQ" />
                    <el-option label="判断题" value="TrueFalse" />
                    <el-option label="填空题" value="FillBlank" />
                    <el-option label="主观问答题" value="Essay" />
                  </el-select>
                </el-form-item>
            </el-col>
            <el-col :span="12">
                 <el-form-item label="预估难度" prop="initialDifficulty">
                   <el-slider v-model="form.initialDifficulty" :step="0.1" :max="1" :marks="{0: '易', 1: '难'}" />
                </el-form-item>
            </el-col>
        </el-row>

        <el-form-item label="题目内容" prop="questionContent">
          <el-input v-model="form.questionContent" type="textarea" :rows="5" placeholder="请输入题干内容..." />
        </el-form-item>
        
        <template v-if="form.questionType === 'MCQ'">
            <el-form-item label="选项配置">
                <div v-for="(_, k) in mcqOptions" :key="k" class="option-item">
                    <span class="option-label">{{ k }}:</span>
                    <el-input v-model="mcqOptions[k]" placeholder="选项内容" style="width: 80%" />
                    <el-radio v-model="form.correctAnswer" :value="k" style="margin-left: 10px;">设为答案</el-radio>
                </div>
            </el-form-item>
        </template>
        
        <template v-if="form.questionType === 'TrueFalse'">
            <el-form-item label="正确答案">
                <el-radio-group v-model="form.correctAnswer">
                    <el-radio value="True">正确 (T)</el-radio>
                    <el-radio value="False">错误 (F)</el-radio>
                </el-radio-group>
            </el-form-item>
        </template>
        
         <template v-if="form.questionType === 'FillBlank' || form.questionType === 'Essay'">
            <el-form-item label="参考答案" prop="correctAnswer">
                <el-input v-model="form.correctAnswer" type="textarea" :rows="3" placeholder="请输入此题的标准/参考答案" />
            </el-form-item>
        </template>

      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm" :loading="submitting">保存题目</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
import { getQuestions, addQuestion, updateQuestion, deleteQuestion, getQuestionById } from '@/api/question'
import { getCourses } from '@/api/academic'
import { getKnowledgePoints } from '@/api/knowledgePoint'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Edit, Delete } from '@element-plus/icons-vue'

const loading = ref(false)
const submitting = ref(false)
const tableData = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

const searchForm = reactive({ courseId: null, type: '', keyword: '' })
const dialogVisible = ref(false)
const dialogTitle = ref('新增题目')

const form = reactive({ 
    questionId: null, 
    courseId: null, 
    questionContent: '', 
    questionType: 'MCQ', 
    correctAnswer: '', 
    initialDifficulty: 0.5,
    optionsJson: '',
    pointIds: [] as number[],
    creatorId: null
})

const mcqOptions = reactive({ A: '', B: '', C: '', D: '' })
const courses = ref<any[]>([])
const knowledgePoints = ref<any[]>([])
const qFormRef = ref()

const rules = reactive({
  courseId: [{ required: true, message: '请选择所属课程', trigger: 'change' }],
  questionContent: [{ required: true, message: '题目内容不能为空', trigger: 'blur' }],
  questionType: [{ required: true, message: '请选择题目类型', trigger: 'change' }]
})

const fetchCourses = async () => {
  try {
    const res = await getCourses({ page: 1, size: 1000 })
    courses.value = res.data.records
  } catch (error) {}
}

const getCourseName = (courseId: number) => {
    const course = courses.value.find(c => c.courseId === courseId);
    return course ? course.courseName : courseId;
}

const getTypeName = (type: string) => {
    const map: Record<string, string> = { 'MCQ': '单选题', 'TrueFalse': '判断题', 'FillBlank': '填空题', 'Essay': '主观题' }
    return map[type] || type
}

const getTypeTag = (type: string) => {
    const map: Record<string, string> = { 'MCQ': 'primary', 'TrueFalse': 'success', 'FillBlank': 'warning', 'Essay': 'info' }
    return map[type] || ''
}

const loadKnowledgePoints = async (courseId: number) => {
    if (!courseId) {
        knowledgePoints.value = []
        return
    }
    try {
        const res = await getKnowledgePoints({ courseId, page: 1, size: 1000 })
        knowledgePoints.value = res.data.records
    } catch(e) {}
}

const onCourseChange = (val: number) => {
    form.pointIds = []
    loadKnowledgePoints(val)
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getQuestions({ 
      current: currentPage.value, 
      size: pageSize.value,
      courseId: searchForm.courseId,
      type: searchForm.type,
      keyword: searchForm.keyword
    })
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const handleCurrentChange = (val: number) => {
    currentPage.value = val
    fetchData()
}

const handleAdd = () => {
  dialogTitle.value = '新增题目'
  Object.assign(form, { 
      questionId: null, 
      courseId: searchForm.courseId || null, 
      questionContent: '', 
      questionType: 'MCQ', 
      correctAnswer: '', 
      initialDifficulty: 0.5,
      optionsJson: '',
      pointIds: [],
      creatorId: parseInt(localStorage.getItem('userId') || '1') 
  })
  Object.assign(mcqOptions, { A: '', B: '', C: '', D: '' })
  
  if (form.courseId) loadKnowledgePoints(form.courseId);
  dialogVisible.value = true
  nextTick(() => qFormRef.value?.clearValidate())
}

const handleEdit = async (row: any) => {
  dialogTitle.value = '编辑题目'
  try {
      const res = await getQuestionById(row.questionId);
      const detailedQ = res.data;
      
      Object.assign(form, detailedQ)
      if (detailedQ.courseId) {
          await loadKnowledgePoints(detailedQ.courseId)
      }
      
      if (detailedQ.questionType === 'MCQ' && detailedQ.optionsJson) {
           try {
               const parsed = JSON.parse(detailedQ.optionsJson)
               Object.assign(mcqOptions, parsed)
           } catch(e) {}
      }
      
      dialogVisible.value = true
      nextTick(() => qFormRef.value?.clearValidate())
  } catch(e) {}
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm('确定删除该题目吗？此操作不可恢复。', '警告', { type: 'warning' })
    .then(async () => {
      await deleteQuestion(row.questionId)
      ElMessage.success('删除成功')
      fetchData()
    })
}

const submitForm = async () => {
  if (!qFormRef.value) return
  await qFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      submitting.value = true
      try {
        const payload = { ...form }
        // Pack MCQ options
        if (payload.questionType === 'MCQ') {
            payload.optionsJson = JSON.stringify(mcqOptions)
            if (!payload.correctAnswer) {
                ElMessage.warning('请选择单选题的正确答案')
                submitting.value = false
                return
            }
        } else {
             payload.optionsJson = ''
        }
        
        if (payload.questionId) {
          await updateQuestion(payload)
        } else {
          await addQuestion(payload)
        }
        ElMessage.success('保存成功')
        dialogVisible.value = false
        fetchData()
      } catch (e) {
          ElMessage.error('保存失败')
      } finally {
          submitting.value = false
      }
    }
  })
}

onMounted(async () => {
    await fetchCourses();
    fetchData();
})
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.pagination { margin-top: 20px; justify-content: flex-end; }
.option-item {
    display: flex;
    align-items: center;
    margin-bottom: 10px;
}
.option-label {
    width: 30px;
    font-weight: bold;
}
</style>
