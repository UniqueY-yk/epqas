<template>
  <div class="role-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>角色管理</span>
          <el-button type="primary" :icon="Plus" @click="handleAdd">新增角色</el-button>
        </div>
      </template>

      <el-table :data="tableData" style="width: 100%" v-loading="loading" stripe border>
        <template #empty>
          <el-empty description="暂无数据" />
        </template>
        <el-table-column prop="roleId" label="ID" width="80" />
        <el-table-column prop="roleName" label="角色名称" width="200" />
        <el-table-column prop="description" label="描述" />
        <el-table-column label="操作" width="180">
          <template #default="scope">
            <el-button size="small" :icon="Edit" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button 
              size="small" 
              type="danger" 
              :icon="Delete" 
              @click="handleDelete(scope.row)" 
              :disabled="scope.row.roleId <= 4"
              :title="scope.row.roleId <= 4 ? '系统内置角色不可删除' : ''"
            >删除</el-button>
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
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName" placeholder="如：管理员" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input type="textarea" v-model="form.description" placeholder="请简要描述角色" />
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
import { getRolesPage, addRole, updateRole, deleteRole } from '@/api/role'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

const dialogVisible = ref(false)
const dialogTitle = ref('新增角色')

const form = reactive({
  roleId: null as number | null,
  roleName: '',
  description: ''
})

const roleFormRef = ref<FormInstance>()
const rules = reactive({
  roleName: [
    { required: true, message: '请输入角色名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度为2到50', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入描述', trigger: 'blur' }
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
  dialogTitle.value = '新增角色'
  Object.assign(form, {
    roleId: null,
    roleName: '',
    description: ''
  })
  dialogVisible.value = true
  nextTick(() => { roleFormRef.value?.clearValidate() })
}

const handleEdit = (row: any) => {
  dialogTitle.value = '编辑角色'
  Object.assign(form, row)
  dialogVisible.value = true
  nextTick(() => { roleFormRef.value?.clearValidate() })
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm('确定删除该角色吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteRole(row.roleId)
      ElMessage.success('删除成功')
      fetchRoles()
    } catch(e) {
      ElMessage.error('删除失败（可能有关联数据）')
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
        ElMessage.success('操作成功')
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
