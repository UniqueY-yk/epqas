<template>
  <div class="student-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>Student Management</span>
          <div class="header-actions">
            <!-- Upload Excel Button -->
            <el-upload
              class="upload-demo"
              action="#"
              :http-request="handleImport"
              :show-file-list="false"
              accept=".xlsx, .xls"
            >
              <el-button type="success" :icon="Upload" :loading="importing">Import Excel</el-button>
            </el-upload>
            <el-button type="primary" :icon="Plus" @click="handleAdd" style="margin-left: 10px;">Add Student</el-button>
          </div>
        </div>
      </template>

      <!-- Search Area (Mocked for now as per simple specs) -->
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="Student Name">
          <el-input v-model="searchForm.realName" placeholder="Search Name (Optional)" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="fetchData">Search</el-button>
        </el-form-item>
      </el-form>

      <!-- Data Table -->
      <el-table :data="tableData" style="width: 100%" v-loading="loading" stripe border>
        <template #empty>
          <el-empty description="No Data" />
        </template>
        <el-table-column prop="studentId" label="User ID" width="80" />
        <el-table-column prop="studentNumber" label="Student Number" />
        <el-table-column prop="classId" label="Class ID" />
        <el-table-column label="Operations" width="180">
          <template #default="scope">
            <el-button size="small" :icon="Edit" @click="handleEdit(scope.row)">Edit</el-button>
            <el-button size="small" type="danger" :icon="Delete" @click="handleDelete(scope.row)">Delete</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- Pagination -->
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

    <!-- Create/Edit Dialog -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle">
      <el-form :model="form" label-width="120px" :rules="rules" ref="studentFormRef">
        <!-- User account details -->
        <el-divider content-position="left">Account Details</el-divider>
        <el-form-item label="Username" prop="username">
          <el-input v-model="form.username" placeholder="Login handle" :disabled="form.userId !== null" />
        </el-form-item>
        <el-form-item label="Password" prop="password">
          <el-input v-model="form.password" type="password" placeholder="Leave empty for default 123456" show-password />
        </el-form-item>
        <el-form-item label="Real Name" prop="realName">
          <el-input v-model="form.realName" />
        </el-form-item>
        <el-form-item label="Email" prop="email">
          <el-input v-model="form.email" />
        </el-form-item>

        <!-- Academic details -->
        <el-divider content-position="left">Academic Details</el-divider>
        <el-form-item label="Student Number" prop="studentNumber">
          <el-input v-model="form.studentNumber" />
        </el-form-item>
        <el-form-item label="Class ID" prop="classId">
          <el-input v-model.number="form.classId" type="number" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">Cancel</el-button>
          <el-button type="primary" @click="submitForm" :loading="submitting">Confirm</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
import { getStudents, addStudent, updateStudent, deleteStudent, importStudents } from '@/api/student'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Edit, Delete, Upload } from '@element-plus/icons-vue'

const loading = ref(false)
const importing = ref(false)
const submitting = ref(false)
const tableData = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const searchForm = reactive({ realName: '' })
const dialogVisible = ref(false)
const dialogTitle = ref('Add Student')
const form = reactive({ 
  userId: null as number | null, 
  username: '', 
  password: '', 
  realName: '', 
  email: '', 
  studentNumber: '', 
  classId: null as number | null 
})

const studentFormRef = ref()
const rules = reactive({
  realName: [{ required: true, message: 'Please input real name', trigger: 'blur' }],
  studentNumber: [{ required: true, message: 'Please input student number', trigger: 'blur' }],
  classId: [{ required: true, message: 'Please input class ID', trigger: 'blur' }]
})

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getStudents({ 
      page: currentPage.value, 
      size: pageSize.value 
      // Add realName if backend adds support
    })
    tableData.value = res.data.records
    total.value = res.data.total
  } catch (e) {
      console.error(e)
  } finally {
    loading.value = false
  }
}

const handleCurrentChange = (val: number) => {
    currentPage.value = val
    fetchData()
}

// Handling Excel Import
const handleImport = async (options: any) => {
  const { file } = options
  importing.value = true
  try {
    const formData = new FormData()
    formData.append('file', file)
    await importStudents(formData)
    ElMessage.success('Import successful! Processing in background.')
    fetchData()
  } catch (e) {
    ElMessage.error('Failed to import students')
  } finally {
    importing.value = false
  }
}

const handleAdd = () => {
  dialogTitle.value = 'Add Student'
  Object.assign(form, { userId: null, username: '', password: '', realName: '', email: '', studentNumber: '', classId: null })
  dialogVisible.value = true
  nextTick(() => { studentFormRef.value?.clearValidate() })
}

const handleEdit = (row: any) => {
  dialogTitle.value = 'Edit Student'
  // When editing, since the backend gets Student object, map it back correctly.
  // Note: DTO is mainly for creation. For updates we might just send the student table properties.
  Object.assign(form, { ...row, userId: row.studentId, password: '' })
  dialogVisible.value = true
  nextTick(() => { studentFormRef.value?.clearValidate() })
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm('Delete this student?', 'Warning', { type: 'warning' })
    .then(async () => {
      await deleteStudent(row.studentId)
      ElMessage.success('Deleted')
      fetchData()
    })
}

const submitForm = async () => {
  if (!studentFormRef.value) return
  await studentFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      submitting.value = true
      try {
        if (form.userId) {
          // Edit existing student record
          await updateStudent({
              studentId: form.userId,
              studentNumber: form.studentNumber,
              classId: form.classId
          })
        } else {
          // Creates User + Student records
          await addStudent(form)
        }
        ElMessage.success('Success')
        dialogVisible.value = false
        fetchData()
      } catch (e) {
        ElMessage.error('Operation failed')
      } finally {
        submitting.value = false
      }
    }
  })
}

onMounted(() => fetchData())
</script>

<style scoped>
.card-header { 
    display: flex; justify-content: space-between; align-items: center; 
}
.header-actions {
    display: flex; align-items: center;
}
.pagination { 
    margin-top: 20px; justify-content: flex-end; 
}
</style>
