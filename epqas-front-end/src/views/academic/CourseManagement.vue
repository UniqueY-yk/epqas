<template>
  <div class="course-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>课程管理</span>
          <el-button type="primary" :icon="Plus" @click="handleAdd">新增课程</el-button>
        </div>
      </template>

      <el-form :inline="true" :model="searchForm">
        <el-form-item label="课程名称">
          <el-input v-model="searchForm.courseName" placeholder="按课程名称搜索" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="fetchData">搜索</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" style="width: 100%" v-loading="loading" stripe border>
        <template #empty>
          <el-empty description="暂无数据" />
        </template>
        <el-table-column prop="courseId" label="ID" width="80" />
        <el-table-column prop="courseName" label="课程名称" />
        <el-table-column prop="courseCode" label="课程代码" />
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
      <el-form :model="form" label-width="120px" :rules="rules" ref="courseFormRef">
        <el-form-item label="课程名称" prop="courseName">
          <el-input v-model="form.courseName" />
        </el-form-item>
        <el-form-item label="课程代码" prop="courseCode">
          <el-input v-model="form.courseCode" />
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
import { getCourses, addCourse, updateCourse, deleteCourse } from '@/api/academic'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Edit, Delete } from '@element-plus/icons-vue'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const searchForm = reactive({ courseName: '' })
const dialogVisible = ref(false)
const dialogTitle = ref('新增课程')
const form = reactive({ courseId: null, courseName: '', courseCode: '' })

const courseFormRef = ref()
const rules = reactive({
  courseName: [
    { required: true, message: '请输入课程名称', trigger: 'blur' },
    { min: 2, message: '课程名称至少2个字', trigger: 'blur' }
  ],
  courseCode: [
    { required: true, message: '请输入课程代码', trigger: 'blur' }
  ]
})

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getCourses({ 
      page: currentPage.value, 
      size: pageSize.value,
      courseName: searchForm.courseName
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
  dialogTitle.value = '新增课程'
  Object.assign(form, { courseId: null, courseName: '', courseCode: '' })
  dialogVisible.value = true
  nextTick(() => { courseFormRef.value?.clearValidate() })
}

const handleEdit = (row: any) => {
  dialogTitle.value = '编辑课程'
  Object.assign(form, row)
  dialogVisible.value = true
  nextTick(() => { courseFormRef.value?.clearValidate() })
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm('确定删除该课程吗？', '提示', { type: 'warning' })
    .then(async () => {
      await deleteCourse(row.courseId)
      ElMessage.success('删除成功')
      fetchData()
    })
}

const submitForm = async () => {
  if (!courseFormRef.value) return
  await courseFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      try {
        if (form.courseId) {
          await updateCourse(form)
        } else {
          await addCourse(form)
        }
        ElMessage.success('操作成功')
        dialogVisible.value = false
        fetchData()
      } catch (e) {}
    }
  })
}

onMounted(() => fetchData())
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.pagination { margin-top: 20px; justify-content: flex-end; }
</style>
