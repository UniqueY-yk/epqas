<template>
  <div class="knowledge-point-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>知识点管理</span>
          <el-button type="primary" :icon="Plus" @click="handleAdd">新增知识点</el-button>
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
        <el-form-item label="知识点名称">
          <el-input v-model="searchForm.pointName" placeholder="按知识点名称搜索" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="fetchData">搜索</el-button>
        </el-form-item>
      </el-form>

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

    <el-dialog v-model="dialogVisible" :title="dialogTitle">
      <el-form :model="form" label-width="120px" :rules="rules" ref="kpFormRef">
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Edit, Delete } from '@element-plus/icons-vue'

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

const handleDelete = (row: any) => {
  ElMessageBox.confirm('确定删除该知识点吗？', '提示', { type: 'warning' })
    .then(async () => {
      await deleteKnowledgePoint(row.pointId)
      ElMessage.success('删除成功')
      fetchData()
    })
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
.card-header { display: flex; justify-content: space-between; align-items: center; }
.pagination { margin-top: 20px; justify-content: flex-end; }
</style>
