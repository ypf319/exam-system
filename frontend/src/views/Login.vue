<template>
  <div class="login-page">
    <div class="login-box">
      <div class="login-header">
        <div class="logo-circle">考</div>
        <h2>在线考试与题库管理系统</h2>
        <p>题库 · 组卷 · 在线考试 · 成绩管理</p>
      </div>
      <el-form ref="form" :model="form" :rules="rules" label-position="top" @keyup.enter.native="submit">
        <el-form-item label="账号" prop="username">
          <el-input v-model="form.username" prefix-icon="el-icon-user" placeholder="请输入账号" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            prefix-icon="el-icon-lock"
            type="password"
            show-password
            placeholder="请输入密码"
          />
        </el-form-item>
        <el-button type="primary" class="login-btn" :loading="loading" @click="submit">登 录</el-button>
      </el-form>
      <div class="tips">
        <div>管理员：admin / 123456</div>
        <div>教师：teacher1 / 123456</div>
        <div>学生：student1 / 123456</div>
      </div>
    </div>
  </div>
</template>

<script>
import { authApi } from '../api'
import { setUser, homePath } from '../utils/auth'

export default {
  name: 'Login',
  data() {
    return {
      loading: false,
      form: {
        username: '',
        password: ''
      },
      rules: {
        username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
        password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
      }
    }
  },
  methods: {
    submit() {
      this.$refs.form.validate(async valid => {
        if (!valid) return
        this.loading = true
        try {
          const res = await authApi.login(this.form)
          setUser(res.data)
          this.$message.success('登录成功')
          this.$router.push(homePath(res.data.role))
        } finally {
          this.loading = false
        }
      })
    }
  }
}
</script>

<style scoped>
.login-page {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1f3c88 0%, #2f80ed 55%, #56ccf2 100%);
}

.login-box {
  width: 400px;
  padding: 36px 40px 28px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 18px 40px rgba(0, 0, 0, 0.18);
}

.login-header {
  text-align: center;
  margin-bottom: 20px;
}

.logo-circle {
  width: 56px;
  height: 56px;
  margin: 0 auto 12px;
  border-radius: 50%;
  background: #2f80ed;
  color: #fff;
  font-size: 26px;
  font-weight: 700;
  line-height: 56px;
}

.login-header h2 {
  margin: 0 0 8px;
  font-size: 20px;
  color: #1f2d3d;
}

.login-header p {
  margin: 0;
  color: #909399;
  font-size: 13px;
}

.login-btn {
  width: 100%;
  margin-top: 6px;
}

.tips {
  margin-top: 18px;
  padding-top: 14px;
  border-top: 1px dashed #ebeef5;
  color: #909399;
  font-size: 12px;
  line-height: 1.9;
  text-align: center;
}
</style>
