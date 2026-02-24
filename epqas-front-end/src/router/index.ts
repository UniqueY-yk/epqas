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
                    meta: { requiresAdmin: true }
                },
                {
                    path: 'admin/roles',
                    name: '角色管理',
                    component: () => import('../views/admin/RoleManagement.vue'),
                    meta: { requiresAdmin: true }
                },
                {
                    path: 'academic/classes',
                    name: '班级管理',
                    component: () => import('../views/academic/ClassManagement.vue'),
                    meta: { requiresAdmin: true }
                },
                {
                    path: 'academic/courses',
                    name: '课程管理',
                    component: () => import('../views/academic/CourseManagement.vue'),
                    meta: { requiresAdmin: true }
                },
                {
                    path: 'academic/students',
                    name: '学生管理',
                    component: () => import('../views/academic/StudentManagement.vue'),
                    meta: { requiresAdmin: true }
                },
                {
                    path: 'academic/knowledge-points',
                    name: '知识点管理',
                    component: () => import('../views/academic/KnowledgePointManagement.vue'),
                    meta: { requiresAdmin: true }
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
    const roleId = localStorage.getItem('roleId')

    if (to.meta.requiresAuth && !token) {
        next('/login')
    } else if (to.meta.requiresAdmin && roleId !== '1') {
        ElMessage.error('Access Denied. Administrator privileges required.')
        next('/')
    } else if (to.path === '/login' && token) {
        next('/')
    } else {
        next()
    }
})

export default router
