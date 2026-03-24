<template>
  <div class="role-management examination-paper-management">
    <div class="page-header">
      <h2>角色管理</h2>
      <p class="subtitle">管理系统中的所有角色及其权限分配信息</p>
    </div>

    <!-- Toolbar -->
    <el-card class="toolbar-card" shadow="hover">
      <div class="toolbar" style="justify-content: flex-end;">
        <div class="action-area">
          <el-button type="success" @click="handleAdd">
            <el-icon><Plus /></el-icon> 新增角色
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- Table -->
    <el-card shadow="hover" class="table-card">

      <el-table :data="tableData" style="width: 100%" v-loading="loading" stripe border>
        <template #empty>
          <el-empty description="暂无数据" />
        </template>
        <el-table-column prop="roleId" label="ID" width="80" align="center" />
        <el-table-column prop="roleName" label="角色名称" width="200" />
        <el-table-column prop="description" label="描述" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="scope">
            <el-button-group>
              <el-button size="small" type="primary" plain :icon="Edit" @click="handleEdit(scope.row)" />
              <el-popconfirm title="确定删除该角色吗？系统内置角色不可删除" @confirm="handleDelete(scope.row)">
                <template #reference>
                  <el-button 
                    size="small" 
                    type="danger" 
                    plain
                    :icon="Delete" 
                    :disabled="scope.row.roleId <= 4"
                  />
                </template>
              </el-popconfirm>
            </el-button-group>
          </template>
        </el-table-column>
      </el-table>

      <!-- Pagination -->
      <div class="pagination-container" v-if="total > 0">
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
      <el-form :model="form" label-width="100px" :rules="rules" ref="roleFormRef" class="paper-form">
        <div class="form-header-section">
          <el-form-item label="角色名称" prop="roleName">
            <el-input v-model="form.roleName" placeholder="如：管理员" />
          </el-form-item>
          <el-form-item label="描述" prop="description">
            <el-input type="textarea" v-model="form.description" placeholder="请简要描述角色" />
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
import { getRolesPage, addRole, updateRole, deleteRole } from '@/api/role'
import { ElMessage, type FormInstance } from 'element-plus'
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

const handleSizeChange = (val: number) => {
  pageSize.value = val
  currentPage.value = 1
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

const handleDelete = async (row: any) => {
  try {
    await deleteRole(row.roleId)
    ElMessage.success('删除成功')
    fetchRoles()
  } catch(e) {
    ElMessage.error('删除失败（可能有关联数据）')
  }
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
.action-area { display: flex; gap: 12px; align-items: center; }
.table-card { border-radius: 8px; }
.pagination-container { margin-top: 20px; display: flex; justify-content: flex-end; }
.paper-dialog :deep(.el-dialog__body) { padding-top: 10px; }
.form-header-section { background: #f8f9fc; padding: 20px 20px 5px 20px; border-radius: 8px; margin-bottom: 20px; }
</style>
