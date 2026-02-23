<template>
    <div class="user-management">
        <el-card>
            <template #header>
                <div class="card-header">
                    <span>用户管理</span>
                    <el-button type="primary" :icon="Plus" @click="handleAdd"
                        >新增用户</el-button
                    >
                </div>
            </template>

            <el-form
                :inline="true"
                :model="searchForm"
                class="demo-form-inline"
            >
                <el-form-item label="用户名">
                    <el-input
                        v-model="searchForm.username"
                        placeholder="按用户名搜索"
                    />
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" :icon="Search" @click="fetchUsers"
                        >搜索</el-button
                    >
                </el-form-item>
            </el-form>

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
                <el-table-column prop="userId" label="ID" width="80" />
                <el-table-column prop="username" label="用户名" width="180" />
                <el-table-column prop="realName" label="真实姓名" width="180" />
                <el-table-column prop="roleId" label="角色" width="180">
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
                <el-table-column label="操作" width="180">
                    <template #default="scope">
                        <el-button
                            size="small"
                            :icon="Edit"
                            @click="handleEdit(scope.row)"
                            >编辑</el-button
                        >
                        <el-button
                            size="small"
                            type="danger"
                            :icon="Delete"
                            @click="handleDelete(scope.row)"
                            >删除</el-button
                        >
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
            <el-form
                :model="form"
                label-width="120px"
                :rules="rules"
                ref="userFormRef"
            >
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
import { ElMessage, ElMessageBox } from "element-plus";
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

const handleDelete = (row: any) => {
    ElMessageBox.confirm("确定删除该用户吗？", "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
    }).then(async () => {
        await deleteUser(row.userId);
        ElMessage.success("删除成功");
        fetchUsers();
    });
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
