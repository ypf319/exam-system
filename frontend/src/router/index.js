import Vue from 'vue'
import Router from 'vue-router'
import { getUser, homePath } from '../utils/auth'

Vue.use(Router)

const router = new Router({
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('../views/Login.vue'),
      meta: { public: true }
    },
    {
      path: '/',
      component: () => import('../views/Layout.vue'),
      redirect: '/home',
      children: [
        {
          path: 'home',
          name: 'Home',
          component: () => import('../views/Home.vue'),
          meta: { title: '系统首页' }
        },
        {
          path: 'admin/users',
          component: () => import('../views/admin/UserList.vue'),
          meta: { title: '用户管理', roles: [1] }
        },
        {
          path: 'admin/courses',
          component: () => import('../views/admin/Course.vue'),
          meta: { title: '课程管理', roles: [1] }
        },
        {
          path: 'teacher/questions',
          component: () => import('../views/teacher/Question.vue'),
          meta: { title: '题库管理', roles: [2] }
        },
        {
          path: 'teacher/papers',
          component: () => import('../views/teacher/PaperList.vue'),
          meta: { title: '试卷管理', roles: [2] }
        },
        {
          path: 'teacher/paper-add',
          component: () => import('../views/teacher/PaperAdd.vue'),
          meta: { title: '新建试卷', roles: [2] }
        },
        {
          path: 'teacher/records',
          component: () => import('../views/teacher/Record.vue'),
          meta: { title: '成绩管理', roles: [2] }
        },
        {
          path: 'student/papers',
          component: () => import('../views/student/PaperList.vue'),
          meta: { title: '考试列表', roles: [3] }
        },
        {
          path: 'student/records',
          component: () => import('../views/student/MyRecord.vue'),
          meta: { title: '我的成绩', roles: [3] }
        }
      ]
    },
    {
      path: '/exam/:paperId',
      component: () => import('../views/student/Exam.vue'),
      meta: { title: '在线考试', roles: [3] }
    },
    {
      path: '*',
      redirect: '/home'
    }
  ]
})

router.beforeEach((to, from, next) => {
  const user = getUser()
  if (to.meta.public) {
    if (user && to.path === '/login') {
      next(homePath(user.role))
      return
    }
    next()
    return
  }
  if (!user) {
    next('/login')
    return
  }
  if (to.meta.roles && !to.meta.roles.includes(user.role)) {
    next(homePath(user.role))
    return
  }
  next()
})

export default router
