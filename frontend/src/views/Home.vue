<template>
  <div>
    <el-card shadow="never" class="welcome-card">
      <div class="welcome">
        <div>
          <h2>{{ greeting }}，{{ user.realName || user.username }}！</h2>
          <p class="text-muted">{{ description }}</p>
        </div>
        <div class="role-badge">{{ roleName }}</div>
      </div>
    </el-card>
    <el-row :gutter="16" class="link-row">
      <el-col v-for="item in links" :key="item.path" :xs="24" :sm="12" :md="8" :lg="6">
        <el-card shadow="hover" class="link-card" @click.native="$router.push(item.path)">
          <i :class="item.icon"></i>
          <h3>{{ item.title }}</h3>
          <p>{{ item.desc }}</p>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { getUser } from '../utils/auth'

const LINKS = {
  1: [
    { path: '/admin/users', title: '用户管理', desc: '管理教师与学生账号', icon: 'el-icon-user' },
    { path: '/admin/courses', title: '课程管理', desc: '维护课程基础信息', icon: 'el-icon-reading' }
  ],
  2: [
    { path: '/teacher/questions', title: '题库管理', desc: '维护题目与批量导入', icon: 'el-icon-tickets' },
    { path: '/teacher/paper-add', title: '新建试卷', desc: '手动或随机组卷', icon: 'el-icon-circle-plus-outline' },
    { path: '/teacher/papers', title: '试卷管理', desc: '发布试卷与查看详情', icon: 'el-icon-document' },
    { path: '/teacher/records', title: '成绩管理', desc: '统计成绩并导出成绩表', icon: 'el-icon-data-line' }
  ],
  3: [
    { path: '/student/papers', title: '考试列表', desc: '参加进行中的考试', icon: 'el-icon-edit-outline' },
    { path: '/student/records', title: '我的成绩', desc: '查看历史成绩与答卷', icon: 'el-icon-trophy' }
  ]
}

export default {
  name: 'Home',
  data() {
    return {
      user: getUser() || {}
    }
  },
  computed: {
    roleName() {
      return { 1: '管理员', 2: '教师', 3: '学生' }[this.user.role] || '用户'
    },
    greeting() {
      const hour = new Date().getHours()
      if (hour < 12) return '早上好'
      if (hour < 18) return '下午好'
      return '晚上好'
    },
    description() {
      if (this.user.role === 2) return '在这里维护题库、生成试卷并跟踪学生成绩。'
      if (this.user.role === 3) return '查看可参加的考试，按时完成答题并查询成绩。'
      return '在这里管理系统用户与课程基础数据。'
    },
    links() {
      return LINKS[this.user.role] || []
    }
  }
}
</script>

<style scoped>
.welcome {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.welcome h2 {
  margin: 0 0 8px;
  font-size: 22px;
}

.welcome p {
  margin: 0;
}

.role-badge {
  padding: 8px 16px;
  border-radius: 20px;
  background: #ecf5ff;
  color: #409eff;
  font-size: 14px;
}

.link-row {
  margin-top: 16px;
}

.link-card {
  margin-bottom: 16px;
  cursor: pointer;
  text-align: center;
  padding: 10px 0;
}

.link-card i {
  font-size: 34px;
  color: #409eff;
}

.link-card h3 {
  margin: 10px 0 6px;
  font-size: 16px;
}

.link-card p {
  margin: 0;
  color: #909399;
  font-size: 13px;
}
</style>
