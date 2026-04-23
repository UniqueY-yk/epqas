<template>
  <div class="student-management examination-paper-management">
    <div class="page-header">
      <h2>学生管理</h2>
      <p class="subtitle">管理系统中的学生账号及其学籍信息</p>
    </div>

    <el-card class="toolbar-card" shadow="hover">
      <div class="toolbar">
        <div class="search-area">
          <el-input v-model="searchForm.realName" placeholder="按姓名搜索..." clearable class="search-input" @keyup.enter="fetchData" >
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
          <el-button type="primary" @click="fetchData">
            <el-icon><Search /></el-icon> 搜索
          </el-button>
        </div>
        <div class="action-area">
          <el-button type="info" :icon="Download" plain @click="handleDownloadTemplate">下载Excel模板</el-button>
          <el-upload
            class="upload-demo"
            action="#"
            :http-request="handleImport"
            :show-file-list="false"
            accept=".xlsx, .xls"
          >
            <el-button type="warning" :icon="Upload" :loading="importing" plain>导入Excel</el-button>
          </el-upload>
          <el-button type="success" @click="handleAdd">
            <el-icon><Plus /></el-icon> 新增学生
          </el-button>
        </div>
      </div>
    </el-card>

    <el-card shadow="hover" class="table-card">
      <el-table :data="tableData" style="width: 100%" v-loading="loading" stripe border>
        <template #empty>
          <el-empty description="暂无数据" />
        </template>
        <el-table-column prop="studentId" label="用户ID" width="80" />
        <el-table-column prop="username" label="学号" />
        <el-table-column prop="realName" label="姓名" />
        <el-table-column prop="classId" label="班级ID" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column label="操作" width="180">
          <template #default="scope">
            <el-button-group>
              <el-button size="small" type="primary" plain :icon="Edit" @click="handleEdit(scope.row)" />
              <el-popconfirm title="确定删除该学生吗？" @confirm="handleDelete(scope.row)">
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="650px" class="paper-dialog" destroy-on-close>
      <el-form :model="form" label-width="100px" :rules="rules" ref="studentFormRef" class="paper-form">
        <div class="form-header-section">
        <el-divider content-position="left" style="margin-top: 0">账号信息</el-divider>
        <el-form-item label="学号" prop="username">
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

        <el-form-item label="班级ID" prop="classId">
          <el-input v-model.number="form.classId" type="number" />
        </el-form-item>
        </div>
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
import { getStudents, addStudent, updateStudent, deleteStudent, importStudents, downloadStudentTemplate } from '@/api/student'
import { ElMessage } from 'element-plus'
import { Search, Plus, Edit, Delete, Upload, Download } from '@element-plus/icons-vue'

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
  classId: null as number | null 
})

const studentFormRef = ref()
const rules = reactive({
  username: [{ required: true, message: '请输入用户名/学号', trigger: 'blur' }],
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

const handleSizeChange = (val: number) => {
  pageSize.value = val
  currentPage.value = 1
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

const handleDownloadTemplate = async () => {
  try {
    const res: any = await downloadStudentTemplate()
    const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', '导入学生信息Excel模板.xlsx')
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
  } catch (error) {
    ElMessage.error('下载模板失败')
  }
}

const handleAdd = () => {
  dialogTitle.value = '新增学生'
  Object.assign(form, { userId: null, username: '', password: '', realName: '', email: '', classId: null })
  dialogVisible.value = true
  nextTick(() => { studentFormRef.value?.clearValidate() })
}

const handleEdit = (row: any) => {
  dialogTitle.value = '编辑学生'
  Object.assign(form, { ...row, userId: row.studentId, password: '' })
  dialogVisible.value = true
  nextTick(() => { studentFormRef.value?.clearValidate() })
}

const handleDelete = async (row: any) => {
  try {
    await deleteStudent(row.studentId)
    ElMessage.success('删除成功')
    fetchData()
  } catch (e) {}
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
.examination-paper-management { padding: 24px; background-color: #f5f7fa; min-height: calc(100vh - 60px); }
.page-header { margin-bottom: 24px; }
.page-header h2 { margin: 0; font-size: 24px; color: #303133; font-weight: 600; }
.subtitle { margin: 8px 0 0; color: #909399; font-size: 14px; }
.toolbar-card { margin-bottom: 16px; border-radius: 8px; }
.toolbar { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 16px; }
.search-area { display: flex; gap: 12px; align-items: center; }
.search-input { width: 250px; }
.action-area { display: flex; gap: 12px; align-items: center; }
.table-card { border-radius: 8px; }
.pagination-container { margin-top: 20px; display: flex; justify-content: flex-end; }
.paper-dialog :deep(.el-dialog__body) { padding-top: 10px; }
.form-header-section { background: #f8f9fc; padding: 20px 20px 5px 20px; border-radius: 8px; margin-bottom: 20px; }
</style>
