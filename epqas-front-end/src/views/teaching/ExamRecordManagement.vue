<template>
  <div class="page-container">
    <div class="page-header">
      <h2>考试记录管理</h2>
      <p class="subtitle">管理并追踪班级的特定考试实例与参考人数。</p>
    </div>

    <el-card shadow="never" class="toolbar-card">
      <div class="toolbar-content">
        <el-input
          v-model="searchQuery.examId"
          placeholder="搜索考试ID"
          clearable
          class="search-input"
          :prefixIcon="Search"
          @clear="fetchData"
          @keyup.enter="fetchData"
        />
        <el-button type="primary" @click="fetchData" :icon="Search">搜索</el-button>
        <div class="toolbar-spacer"></div>
        <el-button type="primary" @click="handleAdd" :icon="Plus" class="add-btn">
          新建考试记录
        </el-button>
      </div>
    </el-card>

    <el-card shadow="never" class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%" class="custom-table">
        <el-table-column prop="examId" label="考试ID" width="100" />
        <el-table-column label="试卷名称">
          <template #default="scope">
            {{ getPaperName(scope.row.paperId) }}
          </template>
        </el-table-column>
        <el-table-column label="参考班级">
          <template #default="scope">
            {{ getClassName(scope.row.classId) }}
          </template>
        </el-table-column>
        <el-table-column prop="examDate" label="考试日期" width="180">
          <template #default="{ row }">
            {{ formatDate(row.examDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="totalCandidates" label="应考人数" width="120" />
        <el-table-column prop="actualExaminees" label="实考人数" width="120" />
        
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="scope">
            <el-button size="small" type="success" link @click="goToGrades(scope.row.examId)">录入成绩</el-button>
            <el-button size="small" type="primary" link @click="handleEdit(scope.row)">编辑</el-button>
            <el-popconfirm title="确定要删除此考试记录吗？" @confirm="handleDelete(scope.row.examId)">
              <template #reference>
                <el-button size="small" type="danger" link>删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :background="true"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- Premium Examination Record Dialog -->
    <el-dialog
      v-model="dialogVisible"
      width="540px"
      append-to-body
      class="premium-dialog"
      :show-close="false"
      align-center
      destroy-on-close
    >
      <div class="premium-header">
        <div class="header-left">
          <div class="icon-box">
            <el-icon><Monitor /></el-icon>
          </div>
          <div class="header-text">
            <h3>{{ dialogType === 'add' ? '新建考试记录' : '编辑考试记录' }}</h3>
            <p>完善基础信息，以便准确关联学生成绩</p>
          </div>
        </div>
        <el-button class="circle-close-btn" circle @click="dialogVisible = false">
          <el-icon><Close /></el-icon>
        </el-button>
      </div>

      <div class="premium-body">
        <el-form :model="formData" :rules="rules" ref="formRef" label-position="top" class="premium-form">
          <div class="form-grid">
            <div class="grid-item full-width">
              <el-form-item label="试卷模板 :" prop="paperId">
                <el-select v-model="formData.paperId" placeholder="搜索并选择关联的试卷模板..." class="w-100 enhanced-input" filterable>
                  <template #prefix><el-icon><Document /></el-icon></template>
                  <el-option
                    v-for="item in paperList"
                    :key="item.paperId"
                    :label="item.title"
                    :value="item.paperId"
                  />
                </el-select>
              </el-form-item>
            </div>
            
            <div class="grid-item full-width">
              <el-form-item label="参考班级 :" prop="classId">
                <el-select v-model="formData.classId" placeholder="选择指定的参考班级..." class="w-100 enhanced-input" filterable>
                  <template #prefix><el-icon><User /></el-icon></template>
                  <el-option
                    v-for="item in classList"
                    :key="item.classId"
                    :label="item.className"
                    :value="item.classId"
                  />
                </el-select>
              </el-form-item>
            </div>

            <div class="grid-item full-width">
              <el-form-item label="考试日期安排 :" prop="examDate">
                <el-date-picker
                  v-model="formData.examDate"
                  type="datetime"
                  placeholder="选择具体的考试日期与时间"
                  format="YYYY-MM-DD HH:mm:ss"
                  value-format="YYYY-MM-DDTHH:mm:ss"
                  class="w-100 enhanced-input"
                />
              </el-form-item>
            </div>

            <div class="grid-item half-width">
              <el-form-item label="应考人数 :" prop="totalCandidates">
                <el-input-number v-model="formData.totalCandidates" :min="1" :max="1000" class="w-100 enhanced-input" controls-position="right" />
              </el-form-item>
            </div>
            
            <div class="grid-item half-width">
              <el-form-item label="实考人数 :" prop="actualExaminees">
                 <el-input-number v-model="formData.actualExaminees" :min="0" :max="formData.totalCandidates || 1000" class="w-100 enhanced-input" controls-position="right" />
              </el-form-item>
            </div>
          </div>
        </el-form>
      </div>

      <div class="premium-footer">
        <el-button class="btn-cancel" @click="dialogVisible = false">取 消</el-button>
        <el-button class="btn-submit" type="primary" @click="submitForm" :loading="submitLoading">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Plus, Monitor, Close, Document, User } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import dayjs from 'dayjs'
import { getExaminationsPage, createExamination, updateExamination, deleteExamination, getPapers, ExaminationDTO } from '@/api/exam'
import { getClasses } from '@/api/academic'

const router = useRouter()
const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref<ExaminationDTO[]>([])
const classList = ref<any[]>([])
const paperList = ref<any[]>([])

const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

const searchQuery = reactive({
  examId: ''
})

const dialogVisible = ref(false)
const dialogType = ref<'add' | 'edit'>('add')
const formRef = ref()

const formData = reactive<ExaminationDTO>({
  paperId: null,
  classId: null,
  examDate: '',
  totalCandidates: 30,
  actualExaminees: 30
})

const rules = {
  paperId: [{ required: true, message: '请选择试卷模板', trigger: 'change' }],
  classId: [{ required: true, message: '请选择参考班级', trigger: 'change' }],
  examDate: [{ required: true, message: '请选择考试日期', trigger: 'change' }],
  totalCandidates: [{ required: true, message: '请输入应考人数', trigger: 'blur' }],
  actualExaminees: [{ required: true, message: '请输入实考人数', trigger: 'blur' }]
}

onMounted(async () => {
  await fetchDictionaries()
  fetchData()
})

const fetchDictionaries = async () => {
  try {
    const [classRes, paperRes] = await Promise.all([
      getClasses({ current: 1, size: 500 }),
      getPapers({ current: 1, size: 500 })
    ])
    classList.value = classRes.data.records || []
    paperList.value = paperRes.data.records || []
  } catch (error) {
    console.error('Failed to load dictionaries', error)
    ElMessage.error('加载参考数据失败')
  }
}

const getClassName = (id: number) => {
  const c = classList.value.find(item => item.classId === id)
  return c ? c.className : `班级ID: ${id}`
}

const getPaperName = (id: number) => {
  const p = paperList.value.find(item => item.paperId === id)
  return p ? p.title : `试卷ID: ${id}`
}

const formatDate = (date: string) => {
  if (!date) return '-'
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

const fetchData = async () => {
  loading.value = true
  try {
    const params: any = {
      current: currentPage.value,
      size: pageSize.value
    }
    const res = await getExaminationsPage(params)
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
    
    // Simple local filter for examId if provided (since backend might not support it yet on /page)
    if (searchQuery.examId) {
        tableData.value = tableData.value.filter(item => item.examId && item.examId.toString() === searchQuery.examId)
    }

  } catch (error) {
    console.error('Fetch error:', error)
    ElMessage.error('获取考务数据失败')
  } finally {
    loading.value = false
  }
}

const handleSizeChange = (val: number) => {
  pageSize.value = val
  fetchData()
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
  fetchData()
}

const goToGrades = (examId: number) => {
  router.push(`/teaching/exams/${examId}/grades`)
}

const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  Object.assign(formData, {
    examId: undefined,
    paperId: null,
    classId: null,
    examDate: '',
    totalCandidates: 30,
    actualExaminees: 30
  })
}

const handleAdd = () => {
  dialogType.value = 'add'
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row: ExaminationDTO) => {
  dialogType.value = 'edit'
  resetForm()
  Object.assign(formData, { ...row })
  dialogVisible.value = true
}

const handleDelete = async (id: number) => {
  try {
    await deleteExamination(id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (error) {
    console.error(error)
  }
}

const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid: boolean) => {
    if (valid) {
      submitLoading.value = true
      try {
        if (dialogType.value === 'add') {
          await createExamination(formData)
          ElMessage.success('创建成功')
        } else {
          await updateExamination(formData)
          ElMessage.success('更新成功')
        }
        dialogVisible.value = false
        fetchData()
      } catch (error) {
        console.error(error)
      } finally {
        submitLoading.value = false
      }
    }
  })
}
</script>

<style scoped>
.page-container {
  padding: 24px;
}

.page-header {
  margin-bottom: 24px;
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

.toolbar-card {
  margin-bottom: 20px;
  border-radius: 8px;
  border: 1px solid #ebeef5;
}

.toolbar-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.search-input {
  width: 280px;
}

.toolbar-spacer {
  flex: 1;
}

.table-card {
  border-radius: 8px;
  border: 1px solid #ebeef5;
}

.custom-table {
  border-radius: 8px;
  overflow: hidden;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

/* Premium Dialog Styling */
:deep(.premium-dialog) {
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 25px 50px rgba(0, 0, 0, 0.15);
  background: white;
}

:deep(.premium-dialog .el-dialog__header) {
  display: none; /* Hide default header */
}

:deep(.premium-dialog .el-dialog__body) {
  padding: 0;
}

.premium-header {
  position: relative;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 30px 40px;
  background: linear-gradient(135deg, #f0f7ff 0%, #ffffff 100%);
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 18px;
}

.icon-box {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 52px;
  height: 52px;
  background: linear-gradient(135deg, #409EFF, #61b1ff);
  border-radius: 14px;
  box-shadow: 0 8px 16px rgba(64, 158, 255, 0.25);
  color: white;
  font-size: 24px;
  flex-shrink: 0;
}

.header-text h3 {
  margin: 0 0 6px 0;
  font-size: 20px;
  font-weight: 700;
  color: #1a1a1a;
  letter-spacing: 0.5px;
}

.header-text p {
  margin: 0;
  font-size: 13px;
  color: #8c8c8c;
}

.circle-close-btn {
  border: none;
  background: #f0f2f5;
  color: #606266;
  width: 32px;
  height: 32px;
  min-height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}
.circle-close-btn:hover {
  background: #ff4d4f;
  color: white;
  transform: rotate(90deg);
}

.premium-body {
  padding: 30px 40px;
}

.form-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 16px 20px;
}

.grid-item {
  box-sizing: border-box;
}

.grid-item.full-width {
  width: 100%;
}

.grid-item.half-width {
  width: calc(50% - 10px);
}

:deep(.premium-form .el-form-item__label) {
  font-weight: 600;
  color: #333;
  padding-bottom: 6px;
  font-size: 14px;
}

:deep(.enhanced-input .el-input__wrapper),
:deep(.enhanced-input .el-input-number) {
  border-radius: 8px;
  box-shadow: none;
  background-color: #f7f9fb;
  border: 1px solid #e4e7ed;
  transition: all 0.3s ease;
  height: 40px;
}

:deep(.enhanced-input .el-input__wrapper:hover),
:deep(.enhanced-input .el-input__wrapper.is-focus) {
  background-color: #ffffff;
  border-color: #409EFF;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.1);
}

.premium-footer {
  padding: 20px 40px 30px;
  background: #fafafa;
  display: flex;
  justify-content: flex-end;
  gap: 14px;
  border-top: 1px solid rgba(0,0,0,0.03);
}

.btn-cancel {
  border-radius: 8px;
  padding: 10px 24px;
  font-weight: 500;
  border: 1px solid #dcdfe6;
}

.btn-submit {
  border-radius: 8px;
  padding: 10px 28px;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
  background: linear-gradient(to right, #409eff, #53a8ff);
  border: none;
  transition: all 0.3s;
}
.btn-submit:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(64, 158, 255, 0.4);
}
.w-100 {
  width: 100%;
}
</style>
