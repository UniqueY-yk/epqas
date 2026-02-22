<template>
  <div class="role-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>Role Management</span>
          <el-button type="primary" :icon="Plus" @click="handleAdd">Add Role</el-button>
        </div>
      </template>

      <el-table :data="tableData" style="width: 100%" v-loading="loading" stripe border>
        <template #empty>
          <el-empty description="No Data" />
        </template>
        <el-table-column prop="roleId" label="ID" width="80" />
        <el-table-column prop="roleName" label="Role Name" width="200" />
        <el-table-column prop="description" label="Description" />
        <el-table-column label="Operations" width="180">
          <template #default="scope">
            <el-button size="small" :icon="Edit" @click="handleEdit(scope.row)">Edit</el-button>
            <el-button 
              size="small" 
              type="danger" 
              :icon="Delete" 
              @click="handleDelete(scope.row)" 
              :disabled="scope.row.roleId <= 4"
              :title="scope.row.roleId <= 4 ? 'System default roles cannot be deleted' : ''"
            >Delete</el-button>
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
      <el-form :model="form" label-width="120px" :rules="rules" ref="roleFormRef">
        <el-form-item label="Role Name" prop="roleName">
          <el-input v-model="form.roleName" placeholder="e.g., Administrator" />
        </el-form-item>
        <el-form-item label="Description" prop="description">
          <el-input type="textarea" v-model="form.description" placeholder="Briefly describe the role" />
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
import { getRolesPage, addRole, updateRole, deleteRole } from '@/api/role'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

const dialogVisible = ref(false)
const dialogTitle = ref('Add Role')

const form = reactive({
  roleId: null as number | null,
  roleName: '',
  description: ''
})

const roleFormRef = ref<FormInstance>()
const rules = reactive({
  roleName: [
    { required: true, message: 'Please input role name', trigger: 'blur' },
    { min: 2, max: 50, message: 'Length should be 2 to 50', trigger: 'blur' }
  ],
  description: [
    { required: true, message: 'Please input description', trigger: 'blur' }
  ]
})

const fetchRoles = async () => {
  loading.value = true
  try {
    const res = await getRolesPage({
      current: currentPage.value,
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
  fetchRoles()
}

const handleAdd = () => {
  dialogTitle.value = 'Add Role'
  Object.assign(form, {
    roleId: null,
    roleName: '',
    description: ''
  })
  dialogVisible.value = true
  nextTick(() => { roleFormRef.value?.clearValidate() })
}

const handleEdit = (row: any) => {
  dialogTitle.value = 'Edit Role'
  Object.assign(form, row)
  dialogVisible.value = true
  nextTick(() => { roleFormRef.value?.clearValidate() })
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm('Are you sure to delete this role?', 'Warning', {
    confirmButtonText: 'Yes',
    cancelButtonText: 'No',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteRole(row.roleId)
      ElMessage.success('Deleted successfully')
      fetchRoles()
    } catch(e) {
      ElMessage.error('Deletion failed (foreign key constraint likely)')
    }
  })
}

const submitForm = async () => {
  if (!roleFormRef.value) return
  await roleFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      try {
        if (form.roleId) {
          await updateRole(form)
        } else {
          await addRole(form)
        }
        ElMessage.success('Operation successful')
        dialogVisible.value = false
        fetchRoles()
      } catch(e) {
        // Handled globally
      }
    }
  })
}

onMounted(() => {
  fetchRoles()
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
