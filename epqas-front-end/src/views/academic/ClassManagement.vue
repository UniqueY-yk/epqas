<template>
  <div class="class-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>Class Management</span>
          <el-button type="primary" :icon="Plus" @click="handleAdd">Add Class</el-button>
        </div>
      </template>

      <el-form :inline="true" :model="searchForm">
        <el-form-item label="Class Name">
          <el-input v-model="searchForm.className" placeholder="Search Class" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="fetchData">Search</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" style="width: 100%" v-loading="loading" stripe border>
        <template #empty>
          <el-empty description="No Data" />
        </template>
        <el-table-column prop="classId" label="ID" width="80" />
        <el-table-column prop="className" label="Class Name" />
        <el-table-column prop="department" label="Department" />
        <el-table-column label="Operations" width="180">
          <template #default="scope">
            <el-button size="small" :icon="Edit" @click="handleEdit(scope.row)">Edit</el-button>
            <el-button size="small" type="danger" :icon="Delete" @click="handleDelete(scope.row)">Delete</el-button>
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
      <el-form :model="form" label-width="120px" :rules="rules" ref="classFormRef">
        <el-form-item label="Class Name" prop="className">
          <el-input v-model="form.className" />
        </el-form-item>
        <el-form-item label="Department" prop="department">
          <el-input v-model="form.department" />
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
import { ref, reactive, onMounted, nextTick } from 'vue'
import { getClasses, addClass, updateClass, deleteClass } from '@/api/academic'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Edit, Delete } from '@element-plus/icons-vue'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const searchForm = reactive({ className: '' })
const dialogVisible = ref(false)
const dialogTitle = ref('Add Class')
const form = reactive({ classId: null, className: '', department: '' })

const classFormRef = ref()
const rules = reactive({
  className: [
    { required: true, message: 'Please input class name', trigger: 'blur' },
    { min: 2, message: 'Class name must be at least 2 characters', trigger: 'blur' }
  ],
  department: [
    { required: true, message: 'Please input department', trigger: 'blur' }
  ]
})

const fetchData = async () => {
  loading.value = true
  try {
    // Calling the API with search param
    const res = await getClasses({ 
      page: currentPage.value, 
      size: pageSize.value,
      className: searchForm.className
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
  dialogTitle.value = 'Add Class'
  Object.assign(form, { classId: null, className: '', department: '' })
  dialogVisible.value = true
  nextTick(() => { classFormRef.value?.clearValidate() })
}

const handleEdit = (row: any) => {
  dialogTitle.value = 'Edit Class'
  Object.assign(form, row)
  dialogVisible.value = true
  nextTick(() => { classFormRef.value?.clearValidate() })
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm('Delete this class?', 'Warning', { type: 'warning' })
    .then(async () => {
      await deleteClass(row.classId)
      ElMessage.success('Deleted')
      fetchData()
    })
}

const submitForm = async () => {
  if (!classFormRef.value) return
  await classFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      try {
        if (form.classId) {
          await updateClass(form)
        } else {
          await addClass(form)
        }
        ElMessage.success('Success')
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
