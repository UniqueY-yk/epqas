import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        {
            path: '/',
            name: 'home',
            component: () => import('../views/HomeView.vue'),
            meta: { requiresAuth: true }
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
        },
        {
            path: '/admin/users',
            name: 'UserManagement',
            component: () => import('../views/admin/UserManagement.vue'),
            meta: { requiresAuth: true }
        },
        {
            path: '/academic/classes',
            name: 'ClassManagement',
            component: () => import('../views/academic/ClassManagement.vue'),
            meta: { requiresAuth: true }
        },
        {
            path: '/academic/courses',
            name: 'CourseManagement',
            component: () => import('../views/academic/CourseManagement.vue'),
            meta: { requiresAuth: true }
        }
    ]
})

router.beforeEach((to, from, next) => {
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
