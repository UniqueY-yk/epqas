<template>
  <div class="class-management examination-paper-management">
    <div class="page-header">
      <h2>班级管理</h2>
      <p class="subtitle">管理系统中的班级及其所属学院信息</p>
    </div>

    <el-card class="toolbar-card" shadow="hover">
      <div class="toolbar">
        <div class="search-area">
          <el-input v-model="searchForm.className" placeholder="按班级名称搜索..." clearable class="search-input" @keyup.enter="fetchData" >
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
          <el-button type="primary" @click="fetchData">
            <el-icon><Search /></el-icon> 搜索
          </el-button>
        </div>
        <div class="action-area">
          <el-button type="success" @click="handleAdd">
            <el-icon><Plus /></el-icon> 新增班级
          </el-button>
        </div>
      </div>
    </el-card>

    <el-card shadow="hover" class="table-card">
      <el-table :data="tableData" style="width: 100%" v-loading="loading" stripe border>
        <template #empty>
          <el-empty description="暂无数据" />
        </template>
        <el-table-column prop="classId" label="ID" width="80" />
        <el-table-column prop="className" label="班级名称" />
        <el-table-column prop="department" label="学院" />
        <el-table-column label="操作" width="180">
          <template #default="scope">
            <el-button-group>
              <el-button size="small" type="primary" plain :icon="Edit" @click="handleEdit(scope.row)" />
              <el-popconfirm title="确定删除该班级吗？" @confirm="handleDelete(scope.row)">
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
      <el-form :model="form" label-width="100px" :rules="rules" ref="classFormRef" class="paper-form">
        <div class="form-header-section">
        <el-form-item label="班级名称" prop="className">
          <el-input v-model="form.className" />
        </el-form-item>
        <el-form-item label="学院" prop="department">
          <el-input v-model="form.department" />
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
import { getClasses, addClass, updateClass, deleteClass } from '@/api/academic'
import { ElMessage } from 'element-plus'
import { Search, Plus, Edit, Delete } from '@element-plus/icons-vue'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const searchForm = reactive({ className: '' })
const dialogVisible = ref(false)
const dialogTitle = ref('新增班级')
const form = reactive({ classId: null, className: '', department: '' })

const classFormRef = ref()
const rules = reactive({
  className: [
    { required: true, message: '请输入班级名称', trigger: 'blur' },
    { min: 2, message: '班级名称至少2个字', trigger: 'blur' }
  ],
  department: [
    { required: true, message: '请输入学院', trigger: 'blur' }
  ]
})

const fetchData = async () => {
  loading.value = true
  try {
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

const handleSizeChange = (val: number) => {
  pageSize.value = val
  currentPage.value = 1
  fetchData()
}

const handleAdd = () => {
  dialogTitle.value = '新增班级'
  Object.assign(form, { classId: null, className: '', department: '' })
  dialogVisible.value = true
  nextTick(() => { classFormRef.value?.clearValidate() })
}

const handleEdit = (row: any) => {
  dialogTitle.value = '编辑班级'
  Object.assign(form, row)
  dialogVisible.value = true
  nextTick(() => { classFormRef.value?.clearValidate() })
}

const handleDelete = async (row: any) => {
  try {
    await deleteClass(row.classId)
    ElMessage.success('删除成功')
    fetchData()
  } catch (e) {}
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
.examination-paper-management {
  padding: 24px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}
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
