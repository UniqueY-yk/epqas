import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        {
            path: '/',
            component: () => import('../layout/MainLayout.vue'),
            meta: { requiresAuth: true },
            children: [
                {
                    path: '',
                    name: '首页',
                    component: () => import('../views/HomeView.vue')
                },
                {
                    path: 'admin/users',
                    name: '用户管理',
                    component: () => import('../views/admin/UserManagement.vue'),
                    meta: { roles: [1] }
                },
                {
                    path: 'admin/roles',
                    name: '角色管理',
                    component: () => import('../views/admin/RoleManagement.vue'),
                    meta: { roles: [1] }
                },
                {
                    path: 'academic/classes',
                    name: '班级管理',
                    component: () => import('../views/academic/ClassManagement.vue'),
                    meta: { roles: [1] }
                },
                {
                    path: 'academic/courses',
                    name: '课程管理',
                    component: () => import('../views/academic/CourseManagement.vue'),
                    meta: { roles: [1] }
                },
                {
                    path: 'academic/students',
                    name: '学生管理',
                    component: () => import('../views/academic/StudentManagement.vue'),
                    meta: { roles: [1] }
                },
                {
                    path: 'academic/knowledge-points',
                    name: '知识点管理',
                    component: () => import('../views/academic/KnowledgePointManagement.vue'),
                    meta: { roles: [1] }
                },
                {
                    path: 'question/bank',
                    name: '题库管理',
                    component: () => import('../views/question/QuestionManagement.vue'),
                    meta: { roles: [1, 2] }
                },
                {
                    path: 'proposition/diagnosis',
                    name: '试卷质量诊断',
                    component: () => import('../views/proposition/PaperQualityDiagnosis.vue'),
                    meta: { roles: [1, 2] }
                },
                {
                    path: 'proposition/papers',
                    name: '试卷模板管理',
                    component: () => import('../views/proposition/ExaminationPaperManagement.vue'),
                    meta: { roles: [1, 2] }
                }
            ]
        },
        {
            path: '/login',
            name: '登录',
            component: () => import('../views/Login.vue')
        },
        {
            path: '/register',
            name: '注册',
            component: () => import('../views/Register.vue')
        }
    ]
})

router.beforeEach((to, _from, next) => {
    const token = localStorage.getItem('token')
    const roleId = Number(localStorage.getItem('roleId'))

    if (to.meta.requiresAuth && !token) {
        next('/login')
    } else if (to.meta.roles && !(to.meta.roles as number[]).includes(roleId)) {
        ElMessage.error('Access Denied. Insufficient privileges.')
        next('/')
    } else if (to.path === '/login' && token) {
        next('/')
    } else {
        next()
    }
})

export default router
