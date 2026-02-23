<template>
  <div class="student-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>学生管理</span>
          <div class="header-actions">
            <el-upload
              class="upload-demo"
              action="#"
              :http-request="handleImport"
              :show-file-list="false"
              accept=".xlsx, .xls"
            >
              <el-button type="success" :icon="Upload" :loading="importing">导入Excel</el-button>
            </el-upload>
            <el-button type="primary" :icon="Plus" @click="handleAdd" style="margin-left: 10px;">新增学生</el-button>
          </div>
        </div>
      </template>

      <el-form :inline="true" :model="searchForm">
        <el-form-item label="学生姓名">
          <el-input v-model="searchForm.realName" placeholder="按姓名搜索" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="fetchData">搜索</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" style="width: 100%" v-loading="loading" stripe border>
        <template #empty>
          <el-empty description="暂无数据" />
        </template>
        <el-table-column prop="studentId" label="用户ID" width="80" />
        <el-table-column prop="studentNumber" label="学号" />
        <el-table-column prop="classId" label="班级ID" />
        <el-table-column label="操作" width="180">
          <template #default="scope">
            <el-button size="small" :icon="Edit" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" :icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle">
      <el-form :model="form" label-width="120px" :rules="rules" ref="studentFormRef">
        <el-divider content-position="left">账号信息</el-divider>
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="登录账号" :disabled="form.userId !== null" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="留空则默认 123456" show-password />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" />
        </el-form-item>

        <el-divider content-position="left">学籍信息</el-divider>
        <el-form-item label="学号" prop="studentNumber">
          <el-input v-model="form.studentNumber" />
        </el-form-item>
        <el-form-item label="班级ID" prop="classId">
          <el-input v-model.number="form.classId" type="number" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm" :loading="submitting">确认</el-button>
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
const dialogTitle = ref('新增学生')
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
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  studentNumber: [{ required: true, message: '请输入学号', trigger: 'blur' }],
  classId: [{ required: true, message: '请输入班级ID', trigger: 'blur' }]
})

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getStudents({ 
      page: currentPage.value, 
      size: pageSize.value 
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

const handleImport = async (options: any) => {
  const { file } = options
  importing.value = true
  try {
    const formData = new FormData()
    formData.append('file', file)
    await importStudents(formData)
    ElMessage.success('导入成功，正在后台处理')
    fetchData()
  } catch (e) {
    ElMessage.error('导入学生失败')
  } finally {
    importing.value = false
  }
}

const handleAdd = () => {
  dialogTitle.value = '新增学生'
  Object.assign(form, { userId: null, username: '', password: '', realName: '', email: '', studentNumber: '', classId: null })
  dialogVisible.value = true
  nextTick(() => { studentFormRef.value?.clearValidate() })
}

const handleEdit = (row: any) => {
  dialogTitle.value = '编辑学生'
  Object.assign(form, { ...row, userId: row.studentId, password: '' })
  dialogVisible.value = true
  nextTick(() => { studentFormRef.value?.clearValidate() })
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm('确定删除该学生吗？', '提示', { type: 'warning' })
    .then(async () => {
      await deleteStudent(row.studentId)
      ElMessage.success('删除成功')
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
          await updateStudent({
              studentId: form.userId,
              studentNumber: form.studentNumber,
              classId: form.classId
          })
        } else {
          await addStudent(form)
        }
        ElMessage.success('操作成功')
        dialogVisible.value = false
        fetchData()
      } catch (e) {
        ElMessage.error('操作失败')
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
