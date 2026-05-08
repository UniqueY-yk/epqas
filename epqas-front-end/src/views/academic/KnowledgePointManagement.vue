<template>
  <div class="knowledge-point-management examination-paper-management">
    <div class="page-header">
      <h2>{{ isStudent ? '知识点查询' : '知识点管理' }}</h2>
      <p class="subtitle">{{ isStudent ? '查看系统中的所有知识点及其所属课程映射' : '管理系统中的所有知识点及其所属课程映射' }}</p>
    </div>

    <el-card class="toolbar-card" shadow="hover">
      <div class="toolbar">
        <div class="search-area">
          <el-select v-model="searchForm.courseId" placeholder="请选择课程..." clearable @change="fetchData" filterable class="search-input">
            <el-option
              v-for="course in courses"
              :key="course.courseId"
              :label="course.courseName"
              :value="course.courseId"
            />
          </el-select>
          <el-input v-model="searchForm.pointName" placeholder="按知识点名称搜索..." clearable class="search-input" @keyup.enter="fetchData" >
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
          <el-button type="primary" @click="fetchData">
            <el-icon><Search /></el-icon> 搜索
          </el-button>
        </div>
        <div class="action-area" v-if="!isStudent">
          <el-button type="success" @click="handleAdd">
            <el-icon><Plus /></el-icon> 新增知识点
          </el-button>
        </div>
      </div>
    </el-card>

    <el-card shadow="hover" class="table-card">
      <el-table :data="tableData" style="width: 100%" v-loading="loading" stripe border>
        <template #empty>
          <el-empty description="暂无数据" />
        </template>
        <el-table-column prop="pointId" label="ID" width="80" />
        <el-table-column prop="pointName" label="知识点名称" />
        <el-table-column prop="courseId" label="所属课程">
          <template #default="scope">
             {{ getCourseName(scope.row.courseId) }}
          </template>
        </el-table-column>
        <el-table-column prop="description" label="详细描述" show-overflow-tooltip />

        <el-table-column label="操作" width="180" v-if="!isStudent">
          <template #default="scope">
            <el-button-group>
              <el-button size="small" type="primary" plain :icon="Edit" @click="handleEdit(scope.row)" />
              <el-popconfirm title="确定删除该知识点吗？" @confirm="handleDelete(scope.row)">
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
          background
          layout="total, sizes, prev, pager, next, jumper"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" class="paper-dialog" destroy-on-close>
      <el-form :model="form" label-width="100px" :rules="rules" ref="kpFormRef" class="paper-form">
        <div class="form-header-section">
        <el-form-item label="所属课程" prop="courseId">
           <el-select v-model="form.courseId" placeholder="请选择课程" style="width: 100%;">
            <el-option
              v-for="course in courses"
              :key="course.courseId"
              :label="course.courseName"
              :value="course.courseId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="知识点名称" prop="pointName">
          <el-input v-model="form.pointName" />
        </el-form-item>
        <el-form-item label="详细描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确认</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
import { getKnowledgePoints, addKnowledgePoint, updateKnowledgePoint, deleteKnowledgePoint } from '@/api/knowledgePoint'
import { getCourses } from '@/api/academic'
import { ElMessage } from 'element-plus'
import { Search, Plus, Edit, Delete } from '@element-plus/icons-vue'

const roleId = Number(localStorage.getItem('roleId') || '0');
const isStudent = roleId === 4;

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const searchForm = reactive({ courseId: null, pointName: '' })
const dialogVisible = ref(false)
const dialogTitle = ref('新增知识点')
const form = reactive({ pointId: null, courseId: null, pointName: '', description: '' })
const courses = ref<any[]>([])

const kpFormRef = ref()
const rules = reactive({
  courseId: [
    { required: true, message: '请选择所属课程', trigger: 'change' }
  ],
  pointName: [
    { required: true, message: '请输入知识点名称', trigger: 'blur' }
  ]
})

const fetchCourses = async () => {
  try {
    const res = await getCourses({ page: 1, size: 1000 })
    courses.value = res.data.records
  } catch (error) {
     console.error("Failed to fetch courses", error);
  }
}

const getCourseName = (courseId: number) => {
    const course = courses.value.find(c => c.courseId === courseId);
    return course ? course.courseName : courseId;
}


const fetchData = async () => {
  loading.value = true
  try {
    const res = await getKnowledgePoints({ 
      page: currentPage.value, 
      size: pageSize.value,
      courseId: searchForm.courseId,
      pointName: searchForm.pointName
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

const handleSizeChange = (val: number) => {
  pageSize.value = val
  currentPage.value = 1
  fetchData()
}

const handleAdd = () => {
  dialogTitle.value = '新增知识点'
  Object.assign(form, { pointId: null, courseId: searchForm.courseId || null, pointName: '', description: '' })
  dialogVisible.value = true
  nextTick(() => { kpFormRef.value?.clearValidate() })
}

const handleEdit = (row: any) => {
  dialogTitle.value = '编辑知识点'
  Object.assign(form, row)
  dialogVisible.value = true
  nextTick(() => { kpFormRef.value?.clearValidate() })
}

const handleDelete = async (row: any) => {
  try {
    await deleteKnowledgePoint(row.pointId)
    ElMessage.success('删除成功')
    fetchData()
  } catch (e) {}
}

const submitForm = async () => {
  if (!kpFormRef.value) return
  await kpFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      try {
        if (form.pointId) {
          await updateKnowledgePoint(form)
        } else {
          await addKnowledgePoint(form)
        }
        ElMessage.success('操作成功')
        dialogVisible.value = false
        fetchData()
      } catch (e) {}
    }
  })
}

onMounted(async () => {
    await fetchCourses();
    fetchData();
})
</script>

<style scoped>
.examination-paper-management { padding: 24px; background-color: #f5f7fa; min-height: calc(100vh - 60px); }
.page-header { margin-bottom: 24px; }
.page-header h2 { margin: 0; font-size: 24px; color: #303133; font-weight: 600; }
.subtitle { margin: 8px 0 0; color: #909399; font-size: 14px; }
.toolbar-card { margin-bottom: 16px; border-radius: 8px; }
.toolbar { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 16px; }
.search-area { display: flex; gap: 12px; align-items: center; }
.search-input { width: 220px; }
.action-area { display: flex; gap: 12px; align-items: center; }
.table-card { border-radius: 8px; }
.pagination-container { margin-top: 20px; display: flex; justify-content: flex-end; }
.paper-dialog :deep(.el-dialog__body) { padding-top: 10px; }
.form-header-section { background: #f8f9fc; padding: 20px 20px 5px 20px; border-radius: 8px; margin-bottom: 20px; }
</style>
