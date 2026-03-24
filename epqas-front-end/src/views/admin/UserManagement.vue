<template>
    <div class="user-management examination-paper-management">
        <div class="page-header">
            <h2>用户管理</h2>
            <p class="subtitle">管理系统中的所有用户及其角色权限信息</p>
        </div>

        <el-card class="toolbar-card" shadow="hover">
            <div class="toolbar">
                <div class="search-area">
                    <el-input
                        v-model="searchForm.username"
                        placeholder="按用户名搜索..."
                        clearable
                        class="search-input"
                        @keyup.enter="fetchUsers"
                    >
                        <template #prefix><el-icon><Search /></el-icon></template>
                    </el-input>
                    <el-button type="primary" @click="fetchUsers">
                        <el-icon><Search /></el-icon> 搜索
                    </el-button>
                </div>
                <div class="action-area">
                    <el-button type="success" @click="handleAdd">
                        <el-icon><Plus /></el-icon> 新增用户
                    </el-button>
                </div>
            </div>
        </el-card>

        <el-card shadow="hover" class="table-card">

            <el-table
                :data="tableData"
                style="width: 100%"
                v-loading="loading"
                stripe
                border
            >
                <template #empty>
                    <el-empty description="暂无数据" />
                </template>
                <el-table-column prop="userId" label="ID" width="80" align="center" />
                <el-table-column prop="username" label="用户名" width="180" />
                <el-table-column prop="realName" label="真实姓名" width="180" />
                <el-table-column prop="roleId" label="角色" width="180" align="center">
                    <template #default="scope">
                        <el-tag :type="getRoleTagType(scope.row.roleId)">{{
                            getRoleName(scope.row.roleId)
                        }}</el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="email" label="邮箱" />
                <el-table-column prop="isActive" label="启用" width="100">
                    <template #default="scope">
                        <el-tag
                            :type="scope.row.isActive ? 'success' : 'danger'"
                            >{{ scope.row.isActive ? "是" : "否" }}</el-tag
                        >
                    </template>
                </el-table-column>
                <el-table-column label="操作" width="180" fixed="right">
                    <template #default="scope">
                        <el-button-group>
                            <el-button
                                size="small"
                                type="primary"
                                plain
                                :icon="Edit"
                                @click="handleEdit(scope.row)"
                            />
                            <el-popconfirm title="确定删除该用户吗？" @confirm="handleDelete(scope.row)">
                                <template #reference>
                                    <el-button size="small" type="danger" plain :icon="Delete" />
                                </template>
                            </el-popconfirm>
                        </el-button-group>
                    </template>
                </el-table-column>
            </el-table>

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
            <el-form
                :model="form"
                label-width="100px"
                :rules="rules"
                ref="userFormRef"
                class="paper-form"
            >
                <div class="form-header-section">
                    <el-form-item label="用户名" prop="username">
                        <el-input
                            v-model="form.username"
                            :disabled="!!form.userId"
                        />
                    </el-form-item>
                    <el-form-item
                        label="密码"
                        prop="passwordHash"
                        v-if="!form.userId"
                    >
                        <el-input v-model="form.passwordHash" type="password" />
                    </el-form-item>
                    <el-form-item label="真实姓名" prop="realName">
                        <el-input v-model="form.realName" />
                    </el-form-item>
                    <el-form-item label="邮箱" prop="email">
                        <el-input v-model="form.email" />
                    </el-form-item>
                    <el-form-item label="角色" prop="roleId">
                        <el-select v-model="form.roleId" placeholder="请选择角色" style="width: 100%">
                            <el-option
                                v-for="role in roles"
                                :key="role.roleId"
                                :label="role.roleName"
                                :value="role.roleId"
                            />
                        </el-select>
                    </el-form-item>
                    <el-form-item label="启用">
                        <el-switch v-model="form.isActive" />
                    </el-form-item>
                </div>
            </el-form>
            <template #footer>
                <span class="dialog-footer">
                    <el-button @click="dialogVisible = false">取消</el-button>
                    <el-button type="primary" @click="submitForm"
                        >确认</el-button
                    >
                </span>
            </template>
        </el-dialog>
    </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from "vue";
import { getUsers, addUser, updateUser, deleteUser } from "@/api/user";
import { getAllRoles } from "@/api/role";
import { ElMessage } from "element-plus";
import { Search, Plus, Edit, Delete } from "@element-plus/icons-vue";

const loading = ref(false);
const tableData = ref([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);
const searchForm = reactive({ username: "" });

const roles = ref<any[]>([]);
const dialogVisible = ref(false);
const dialogTitle = ref("新增用户");
const form = reactive({
    userId: null,
    username: "",
    passwordHash: "",
    realName: "",
    email: "",
    roleId: 4,
    isActive: true,
});

const userFormRef = ref();
const rules = reactive({
    username: [
        { required: true, message: "请输入用户名", trigger: "blur" },
        { min: 3, max: 20, message: "长度为3到20", trigger: "blur" },
    ],
    passwordHash: [
        { required: true, message: "请输入密码", trigger: "blur" },
        { min: 6, message: "密码至少6位", trigger: "blur" },
    ],
    email: [
        {
            type: "email",
            message: "邮箱格式不正确",
            trigger: ["blur", "change"],
        },
    ],
    roleId: [{ required: true, message: "请选择角色", trigger: "change" }],
});

const getRoleTagType = (roleId: number) => {
    switch (roleId) {
        case 1:
            return "danger";
        case 2:
            return "warning";
        case 3:
            return "success";
        case 4:
            return "info";
        default:
            return "info";
    }
};

const getRoleName = (roleId: number) => {
    const role = roles.value.find(r => r.roleId === roleId);
    return role ? role.roleName : "未知";
};

const fetchUsers = async () => {
    loading.value = true;
    try {
        const res = await getUsers({
            page: currentPage.value,
            size: pageSize.value,
            username: searchForm.username,
        });
        tableData.value = res.data.records;
        total.value = res.data.total;
    } finally {
        loading.value = false;
    }
};

const handleCurrentChange = (val: number) => {
    currentPage.value = val;
    fetchUsers();
};

const handleSizeChange = (val: number) => {
    pageSize.value = val;
    currentPage.value = 1;
    fetchUsers();
};

const handleAdd = () => {
    dialogTitle.value = "新增用户";
    Object.assign(form, {
        userId: null,
        username: "",
        passwordHash: "",
        realName: "",
        email: "",
        roleId: 4,
        isActive: true,
    });
    dialogVisible.value = true;
    nextTick(() => {
        userFormRef.value?.clearValidate();
    });
};

const handleEdit = (row: any) => {
    dialogTitle.value = "编辑用户";
    Object.assign(form, row);
    dialogVisible.value = true;
    nextTick(() => {
        userFormRef.value?.clearValidate();
    });
};

const handleDelete = async (row: any) => {
    try {
        await deleteUser(row.userId);
        ElMessage.success("删除成功");
        fetchUsers();
    } catch(e) {
    }
};

const submitForm = async () => {
    if (!userFormRef.value) return;
    await userFormRef.value.validate(async (valid: boolean) => {
        if (valid) {
            try {
                if (form.userId) {
                    await updateUser(form);
                } else {
                    await addUser(form);
                }
                ElMessage.success("操作成功");
                dialogVisible.value = false;
                fetchUsers();
            } catch (e) {
                // handled
            }
        }
    });
};

const fetchAllRoles = async () => {
    try {
        const res = await getAllRoles();
        roles.value = res.data;
    } catch (e) {
        console.error("Failed to fetch roles", e);
    }
};

onMounted(() => {
    fetchAllRoles();
    fetchUsers();
});
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
.search-input { width: 240px; }
.table-card { border-radius: 8px; }
.pagination-container { margin-top: 20px; display: flex; justify-content: flex-end; }
.paper-dialog :deep(.el-dialog__body) { padding-top: 10px; }
.form-header-section { background: #f8f9fc; padding: 20px 20px 5px 20px; border-radius: 8px; margin-bottom: 20px; }
</style>
