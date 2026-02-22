<template>
  <div class="user-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>User Management</span>
          <el-button type="primary" :icon="Plus" @click="handleAdd">Add User</el-button>
        </div>
      </template>

      <el-form :inline="true" :model="searchForm" class="demo-form-inline">
        <el-form-item label="Username">
          <el-input v-model="searchForm.username" placeholder="Search by username" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="fetchUsers">Search</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" style="width: 100%" v-loading="loading" stripe border>
        <template #empty>
          <el-empty description="No Data" />
        </template>
        <el-table-column prop="userId" label="ID" width="80" />
        <el-table-column prop="username" label="Username" width="180" />
        <el-table-column prop="realName" label="Real Name" width="180" />
        <el-table-column prop="roleId" label="Role" width="180">
          <template #default="scope">
            <el-tag :type="getRoleTagType(scope.row.roleId)">{{ getRoleName(scope.row.roleId) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="email" label="Email" />
        <el-table-column prop="isActive" label="Active" width="100">
           <template #default="scope">
              <el-tag :type="scope.row.isActive ? 'success' : 'danger'">{{ scope.row.isActive ? 'Yes' : 'No' }}</el-tag>
           </template>
        </el-table-column>
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
      <el-form :model="form" label-width="120px" :rules="rules" ref="userFormRef">
        <el-form-item label="Username" prop="username">
          <el-input v-model="form.username" :disabled="!!form.userId" />
        </el-form-item>
        <el-form-item label="Password" prop="passwordHash" v-if="!form.userId">
          <el-input v-model="form.passwordHash" type="password" />
        </el-form-item>
        <el-form-item label="Real Name" prop="realName">
          <el-input v-model="form.realName" />
        </el-form-item>
        <el-form-item label="Email" prop="email">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="Role ID" prop="roleId">
            <el-input v-model.number="form.roleId" type="number" />
        </el-form-item>
         <el-form-item label="Active">
            <el-switch v-model="form.isActive" />
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
import { getUsers, addUser, updateUser, deleteUser } from '@/api/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Edit, Delete } from '@element-plus/icons-vue'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const searchForm = reactive({ username: '' })

const dialogVisible = ref(false)
const dialogTitle = ref('Add User')
const form = reactive({
  userId: null,
  username: '',
  passwordHash: '',
  realName: '',
  email: '',
  roleId: 4,
  isActive: true
})

const userFormRef = ref()
const rules = reactive({
  username: [
    { required: true, message: 'Please input username', trigger: 'blur' },
    { min: 3, max: 20, message: 'Length should be 3 to 20', trigger: 'blur' }
  ],
  passwordHash: [
    { required: true, message: 'Please input password', trigger: 'blur' },
    { min: 6, message: 'Password must be at least 6 characters', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: 'Please input correct email format', trigger: ['blur', 'change'] }
  ],
  roleId: [
    { required: true, message: 'Please input a role ID', trigger: 'blur' }
  ]
})

const getRoleTagType = (roleId: number) => {
  switch(roleId) {
    case 1: return 'danger'
    case 2: return 'warning'
    case 3: return 'success'
    case 4: return 'info'
    default: return 'info'
  }
}

const getRoleName = (roleId: number) => {
  switch(roleId) {
    case 1: return 'Administrator'
    case 2: return 'Question Setter'
    case 3: return 'Course Instructor'
    case 4: return 'Student'
    default: return 'Unknown'
  }
}

const fetchUsers = async () => {
  loading.value = true
  try {
    const res = await getUsers({
      page: currentPage.value,
      size: pageSize.value,
      username: searchForm.username
    })
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
  fetchUsers()
}

const handleAdd = () => {
  dialogTitle.value = 'Add User'
  Object.assign(form, {
    userId: null,
    username: '',
    passwordHash: '',
    realName: '',
    email: '',
    roleId: 4,
    isActive: true
  })
  dialogVisible.value = true
  nextTick(() => { userFormRef.value?.clearValidate() })
}

const handleEdit = (row: any) => {
  dialogTitle.value = 'Edit User'
  Object.assign(form, row)
  dialogVisible.value = true
  nextTick(() => { userFormRef.value?.clearValidate() })
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm('Are you sure to delete this user?', 'Warning', {
    confirmButtonText: 'Yes',
    cancelButtonText: 'No',
    type: 'warning'
  }).then(async () => {
    await deleteUser(row.userId)
    ElMessage.success('Deleted successfully')
    fetchUsers()
  })
}

const submitForm = async () => {
  if (!userFormRef.value) return
  await userFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      try {
        if (form.userId) {
          await updateUser(form)
        } else {
          await addUser(form)
        }
        ElMessage.success('Operation successful')
        dialogVisible.value = false
        fetchUsers()
      } catch(e) {
        // handled
      }
    }
  })
}

onMounted(() => {
  fetchUsers()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.pagination {
  margin-top: 20px;
  justify-content: flex-end;
}
</style>
