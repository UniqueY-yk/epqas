<template>
    <el-container class="layout-container">
        <el-aside width="240px" class="aside">
            <div class="logo">
                <el-icon :size="24" color="#fff"><Platform /></el-icon>
                <span v-if="!isCollapse">题析</span>
            </div>
            <el-menu
                :default-active="activeMenu"
                class="el-menu-vertical"
                :collapse="isCollapse"
                background-color="#304156"
                text-color="#bfcbd9"
                active-text-color="#409EFF"
                router
            >
                <el-menu-item index="/">
                    <el-icon><Odometer /></el-icon>
                    <template #title>首页</template>
                </el-menu-item>

                <el-menu-item index="/admin/users" v-if="isAdmin">
                    <el-icon><User /></el-icon>
                    <template #title>用户管理</template>
                </el-menu-item>

                <el-menu-item index="/admin/roles" v-if="isAdmin">
                    <el-icon><Filter /></el-icon>
                    <template #title>角色管理</template>
                </el-menu-item>

                <el-sub-menu index="/academic" v-if="isAdmin">
                    <template #title>
                        <el-icon><School /></el-icon>
                        <span>教务管理</span>
                    </template>
                    <el-menu-item index="/academic/students"
                        >学生管理</el-menu-item
                    >
                    <el-menu-item index="/academic/classes"
                        >班级管理</el-menu-item
                    >
                    <el-menu-item index="/academic/courses"
                        >课程管理</el-menu-item
                    >
                    <el-menu-item index="/academic/knowledge-points"
                        >知识点管理</el-menu-item
                    >
                </el-sub-menu>

                <el-sub-menu index="/proposition" v-if="isAdmin || isSetter">
                    <template #title>
                        <el-icon><Document /></el-icon>
                        <span>命题管理</span>
                    </template>
                    <el-menu-item index="/question/bank"
                        >题库管理</el-menu-item
                    >
                    <el-menu-item index="/proposition/papers"
                        >试卷模板管理</el-menu-item
                    >
                    <el-menu-item index="/proposition/diagnosis"
                        >试卷质量诊断</el-menu-item
                    >
                    <el-menu-item index="/proposition/trend"
                        >历史命题趋势</el-menu-item
                    >
                </el-sub-menu>

                <el-sub-menu index="/teaching" v-if="isAdmin || isInstructor">
                    <template #title>
                        <el-icon><Monitor /></el-icon>
                        <span>教学管理</span>
                    </template>
                    <el-menu-item index="/teaching/exams"
                        >考试记录管理</el-menu-item
                    >
                    <el-menu-item index="/teaching/class-analysis"
                        >班级答题分析</el-menu-item
                    >
                    <el-menu-item index="/teaching/knowledge-mastery"
                        >知识点掌握诊断</el-menu-item
                    >
                </el-sub-menu>
            </el-menu>
        </el-aside>

        <el-container>
            <el-header class="header">
                <div class="header-left">
                    <el-icon class="collapse-btn" @click="toggleCollapse">
                        <Expand v-if="isCollapse" />
                        <Fold v-else />
                    </el-icon>
                    <el-breadcrumb separator="/">
                        <el-breadcrumb-item :to="{ path: '/' }"
                            >首页</el-breadcrumb-item
                        >
                        <el-breadcrumb-item>{{
                            currentRouteName
                        }}</el-breadcrumb-item>
                    </el-breadcrumb>
                </div>
                <div class="header-right">
                    <el-dropdown @command="handleCommand">
                        <span class="el-dropdown-link">
                            {{ roleName }}
                            <el-icon class="el-icon--right"
                                ><arrow-down
                            /></el-icon>
                        </span>
                        <template #dropdown>
                            <el-dropdown-menu>
                                <el-dropdown-item command="profile"
                                    >个人资料</el-dropdown-item
                                >
                                <el-dropdown-item divided command="logout"
                                    >退出登录</el-dropdown-item
                                >
                            </el-dropdown-menu>
                        </template>
                    </el-dropdown>
                </div>
            </el-header>

            <el-main class="main">
                <router-view v-slot="{ Component }">
                    <component :is="Component" />
                </router-view>
            </el-main>
        </el-container>
    </el-container>
</template>

<script setup lang="ts">
import { ref, computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import {
    Platform,
    Odometer,
    User,
    School,
    Expand,
    Fold,
    ArrowDown,
    Filter,
    Document,
    Monitor
} from "@element-plus/icons-vue";

const route = useRoute();
const router = useRouter();
const isCollapse = ref(false);

const roleId = Number(localStorage.getItem('roleId') || '0');
const isAdmin = computed(() => roleId === 1);
const isSetter = computed(() => roleId === 2);
const isInstructor = computed(() => roleId === 3);

const activeMenu = computed(() => route.path);
const currentRouteName = computed(() => route.name);

const roleName = computed(() => {
    switch (roleId) {
        case 1: return '系统管理员';
        case 2: return '命题教师';
        case 3: return '任课教师';
        case 4: return '学生';
        default: return '未知角色';
    }
});

const toggleCollapse = () => {
    isCollapse.value = !isCollapse.value;
};

const handleCommand = (command: string) => {
    if (command === "logout") {
        localStorage.removeItem("token");
        ElMessage.success("已退出登录");
        router.push("/login");
    }
};
</script>

<style scoped>
.layout-container {
    height: 100vh;
}

.aside {
    background-color: #304156;
    color: #fff;
    transition: width 0.3s;
}

.logo {
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 10px;
    background-color: #2b2f3a;
    color: #fff;
    font-weight: bold;
    font-size: 18px;
}

.el-menu-vertical {
    border-right: none;
}

.header {
    background-color: #fff;
    border-bottom: 1px solid #dcdfe6;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 20px;
    height: 60px;
}

.header-left {
    display: flex;
    align-items: center;
    gap: 20px;
}

.collapse-btn {
    font-size: 20px;
    cursor: pointer;
}

.header-right {
    cursor: pointer;
}

.main {
    background-color: #f0f2f5;
    padding: 20px;
}
</style>
