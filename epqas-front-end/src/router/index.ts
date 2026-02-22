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
                    name: 'Dashboard',
                    component: () => import('../views/HomeView.vue')
                },
                {
                    path: 'admin/users',
                    name: 'UserManagement',
                    component: () => import('../views/admin/UserManagement.vue'),
                    meta: { requiresAdmin: true }
                },
                {
                    path: 'academic/classes',
                    name: 'ClassManagement',
                    component: () => import('../views/academic/ClassManagement.vue'),
                    meta: { requiresAdmin: true }
                },
                {
                    path: 'academic/courses',
                    name: 'CourseManagement',
                    component: () => import('../views/academic/CourseManagement.vue'),
                    meta: { requiresAdmin: true }
                }
            ]
        },
        {
            path: '/login',
            name: 'login',
            component: () => import('../views/Login.vue')
        },
        {
            path: '/register',
            name: 'register',
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
