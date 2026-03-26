import Vue from 'vue'
import VueRouter from 'vue-router'
import Home from '../views/Home.vue'
import Users from '../views/Users.vue'
import Records from '../views/Records.vue'
import UserDetail from '../views/UserDetail.vue'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import Counselors from '../views/Counselors.vue'
import Students from '../views/Students.vue'
import Appointments from '../views/Appointments.vue'
import Sessions from '../views/Sessions.vue'
import Feedback from '../views/Feedback.vue'
import store from '../store'

Vue.use(VueRouter)

const routes = [
  {
    path: '/',
    redirect: '/home'
  },
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/register',
    name: 'Register',
    component: Register
  },
  {
    path: '/home',
    name: 'Home',
    component: Home,
    meta: { requiresAuth: true }
  },
  {
    path: '/users',
    name: 'Users',
    component: Users,
    meta: { requiresAuth: true, roles: ['ADMIN'] }
  },
  {
    path: '/users/:id',
    name: 'UserDetail',
    component: UserDetail,
    props: true,
    meta: { requiresAuth: true }
  },
  {
    path: '/records',
    name: 'Records',
    component: Records,
    meta: { requiresAuth: true, roles: ['ADMIN', 'COUNSELOR', 'STUDENT'] }
  },
  {
    path: '/counselors',
    name: 'Counselors',
    component: Counselors,
    meta: { requiresAuth: true, roles: ['ADMIN'] }
  },
  {
    path: '/students',
    name: 'Students',
    component: Students,
    meta: { requiresAuth: true, roles: ['ADMIN', 'COUNSELOR', 'STUDENT'] }
  },
  {
    path: '/appointments',
    name: 'Appointments',
    component: Appointments,
    meta: { requiresAuth: true }
  },
  {
    path: '/sessions',
    name: 'Sessions',
    component: Sessions,
    meta: { requiresAuth: true, roles: ['ADMIN', 'COUNSELOR', 'STUDENT'] }
  },
  {
    path: '/feedback',
    name: 'Feedback',
    component: Feedback,
    meta: { requiresAuth: true }
  }
]

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 初始化认证状态
  store.dispatch('initAuth')
  
  const isAuthenticated = store.getters.isAuthenticated
  const currentUser = store.getters.currentUser
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth)
  const requiredRoles = to.meta.roles || []
  
  // 需要认证但未登录，跳转到登录页
  if (requiresAuth && !isAuthenticated) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
    return
  }
  
  // 需要特定角色但用户角色不符，跳转到首页
  if (requiresAuth && requiredRoles.length > 0 && currentUser && !requiredRoles.includes(currentUser.role)) {
    next({ name: 'Home' })
    return
  }
  
  // 已登录但访问登录/注册页，跳转到首页
  if (isAuthenticated && (to.name === 'Login' || to.name === 'Register')) {
    next({ name: 'Home' })
    return
  }
  
  next()
})

export default router