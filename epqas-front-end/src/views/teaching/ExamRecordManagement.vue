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
        
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
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

    <el-dialog
      :title="dialogType === 'add' ? '新建考试记录' : '编辑考试记录'"
      v-model="dialogVisible"
      width="500px"
      append-to-body
      class="custom-dialog"
    >
      <div class="form-header-section">
        <el-icon class="form-icon"><Monitor /></el-icon>
        <div class="form-title-text">
          <h3>{{ dialogType === 'add' ? '新建记录' : '编辑记录' }}</h3>
          <p>请准确填写考试信息，确保学生成绩正确匹配。</p>
        </div>
      </div>
      <el-form :model="formData" :rules="rules" ref="formRef" label-width="100px" class="modern-form">
        <el-form-item label="试卷模板" prop="paperId">
          <el-select v-model="formData.paperId" placeholder="请选择试卷模板" class="w-100" filterable>
            <el-option
              v-for="item in paperList"
              :key="item.paperId"
              :label="item.title"
              :value="item.paperId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="参考班级" prop="classId">
          <el-select v-model="formData.classId" placeholder="请选择参考班级" class="w-100" filterable>
            <el-option
              v-for="item in classList"
              :key="item.classId"
              :label="item.className"
              :value="item.classId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="考试日期" prop="examDate">
          <el-date-picker
            v-model="formData.examDate"
            type="datetime"
            placeholder="选择考试日期和时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DDTHH:mm:ss"
            class="w-100"
          />
        </el-form-item>
        <el-form-item label="应考人数" prop="totalCandidates">
          <el-input-number v-model="formData.totalCandidates" :min="1" :max="1000" class="w-100" />
        </el-form-item>
        <el-form-item label="实考人数" prop="actualExaminees">
           <el-input-number v-model="formData.actualExaminees" :min="0" :max="formData.totalCandidates || 1000" class="w-100" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm" :loading="submitLoading">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Plus, Monitor } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import { getExaminationsPage, createExamination, updateExamination, deleteExamination, getPapers, ExaminationDTO } from '@/api/exam'
import { getClasses } from '@/api/academic'

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

.custom-dialog {
  border-radius: 12px;
  overflow: hidden;
}

.form-header-section {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 24px 32px;
  background: linear-gradient(135deg, #f5f7fa 0%, #ffffff 100%);
  border-bottom: 1px solid #ebeef5;
  margin: -20px -20px 24px -20px;
}

.form-icon {
  font-size: 32px;
  color: #409EFF;
  background: #ecf5ff;
  padding: 12px;
  border-radius: 12px;
}

.form-title-text h3 {
  margin: 0 0 4px 0;
  font-size: 18px;
  color: #303133;
}

.form-title-text p {
  margin: 0;
  font-size: 13px;
  color: #909399;
}

.modern-form {
  padding: 0 12px;
}

.w-100 {
  width: 100%;
}

.dialog-footer {
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
  margin: 0 -20px -20px -20px;
  padding: 16px 24px;
  background: #fafafa;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
