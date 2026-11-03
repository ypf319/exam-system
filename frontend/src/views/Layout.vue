<template>
  <el-container class="layout">
    <el-aside width="220px" class="aside">
      <div class="logo">
        <span class="logo-mark">考</span>
        <span>在线考试系统</span>
      </div>
      <el-menu
        :default-active="$route.path"
        router
        background-color="#243447"
        text-color="#c0ccda"
        active-text-color="#409eff"
      >
        <el-menu-item v-for="item in menus" :key="item.path" :index="item.path">
          <i :class="item.icon"></i>
          <span slot="title">{{ item.title }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div class="page-title">{{ $route.meta.title || '在线考试与题库管理系统' }}</div>
        <div class="user-area">
          <el-tag size="small" effect="plain">{{ roleName }}</el-tag>
          <span class="user-name">{{ user.realName || user.username }}</span>
          <el-button type="text" @click="logout">退出登录</el-button>
        </div>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script>
import { authApi } from '../api'
import { getUser, removeUser } from '../utils/auth'

const MENUS = {
  1: [
    { path: '/admin/users', title: '用户管理', icon: 'el-icon-user' },
    { path: '/admin/courses', title: '课程管理', icon: 'el-icon-reading' }
  ],
  2: [
    { path: '/teacher/questions', title: '题库管理', icon: 'el-icon-tickets' },
    { path: '/teacher/papers', title: '试卷管理', icon: 'el-icon-document' },
    { path: '/teacher/paper-add', title: '新建试卷', icon: 'el-icon-circle-plus-outline' },
    { path: '/teacher/records', title: '成绩管理', icon: 'el-icon-data-line' }
  ],
  3: [
    { path: '/student/papers', title: '考试列表', icon: 'el-icon-edit-outline' },
    { path: '/student/records', title: '我的成绩', icon: 'el-icon-trophy' }
  ]
}

export default {
  name: 'Layout',
  data() {
    return {
      user: getUser() || {}
    }
  },
  computed: {
    menus() {
      return MENUS[this.user.role] || []
    },
    roleName() {
      return { 1: '管理员', 2: '教师', 3: '学生' }[this.user.role] || '用户'
    }
  },
  methods: {
    async logout() {
      try {
        await authApi.logout()
      } catch (e) {
        // ignore
      }
      removeUser()
      this.$router.push('/login')
    }
  }
}
</script>

<style scoped>
.layout {
  height: 100%;
}

.aside {
  background: #243447;
  overflow-x: hidden;
}

.aside >>> .el-menu {
  border-right: none;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #fff;
  font-size: 17px;
  font-weight: 600;
  letter-spacing: 1px;
}

.logo-mark {
  width: 28px;
  height: 28px;
  line-height: 28px;
  text-align: center;
  border-radius: 6px;
  background: #409eff;
  font-size: 15px;
}

.header {
  height: 60px;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
}

.page-title {
  font-size: 17px;
  font-weight: 600;
}

.user-area {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-name {
  color: #606266;
}

.main {
  background: #f0f2f5;
  padding: 20px;
}
</style>
