import { createRouter, createWebHistory } from 'vue-router'

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
                    component: () => import('../views/admin/UserManagement.vue')
                },
                {
                    path: 'academic/classes',
                    name: 'ClassManagement',
                    component: () => import('../views/academic/ClassManagement.vue')
                },
                {
                    path: 'academic/courses',
                    name: 'CourseManagement',
                    component: () => import('../views/academic/CourseManagement.vue')
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
    if (to.meta.requiresAuth && !token) {
        next('/login')
    } else if (to.path === '/login' && token) {
        next('/')
    } else {
        next()
    }
})

export default router
