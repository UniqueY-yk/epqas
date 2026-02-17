<template>
  <div class="course-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>Course Management</span>
          <el-button type="primary" @click="handleAdd">Add Course</el-button>
        </div>
      </template>

      <el-table :data="tableData" style="width: 100%" v-loading="loading">
        <el-table-column prop="courseId" label="ID" width="80" />
        <el-table-column prop="courseName" label="Course Name" />
        <el-table-column prop="courseCode" label="Code" />
        <el-table-column label="Operations" width="180">
          <template #default="scope">
            <el-button size="small" @click="handleEdit(scope.row)">Edit</el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row)">Delete</el-button>
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
      <el-form :model="form" label-width="120px">
        <el-form-item label="Course Name">
          <el-input v-model="form.courseName" />
        </el-form-item>
        <el-form-item label="Course Code">
          <el-input v-model="form.courseCode" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">Cancel</el-button>
          <el-button type="primary" @click="submitForm">Confirm</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getCourses, addCourse, updateCourse, deleteCourse } from '@/api/academic'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const dialogVisible = ref(false)
const dialogTitle = ref('Add Course')
const form = reactive({ courseId: null, courseName: '', courseCode: '' })

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getCourses({ page: currentPage.value, size: pageSize.value })
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
  dialogTitle.value = 'Add Course'
  Object.assign(form, { courseId: null, courseName: '', courseCode: '' })
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  dialogTitle.value = 'Edit Course'
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm('Delete this course?', 'Warning', { type: 'warning' })
    .then(async () => {
      await deleteCourse(row.courseId)
      ElMessage.success('Deleted')
      fetchData()
    })
}

const submitForm = async () => {
  try {
    if (form.courseId) {
      await updateCourse(form)
    } else {
      await addCourse(form)
    }
    ElMessage.success('Success')
    dialogVisible.value = false
    fetchData()
  } catch (e) {}
}

onMounted(() => fetchData())
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.pagination { margin-top: 20px; justify-content: flex-end; }
</style>
