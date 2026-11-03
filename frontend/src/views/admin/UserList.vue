<template>
  <div class="page-card">
    <div class="toolbar">
      <el-input
        v-model="query.keyword"
        placeholder="账号 / 姓名"
        clearable
        style="width: 200px"
        @keyup.enter.native="search"
      />
      <el-select v-model="query.role" placeholder="全部角色" clearable style="width: 140px">
        <el-option label="教师" :value="2" />
        <el-option label="学生" :value="3" />
      </el-select>
      <el-button type="primary" icon="el-icon-search" @click="search">查询</el-button>
      <el-button icon="el-icon-refresh" @click="reset">重置</el-button>
      <div class="spacer"></div>
      <el-button type="success" icon="el-icon-plus" @click="openAdd">新增用户</el-button>
    </div>

    <el-table v-loading="loading" :data="rows" border stripe>
      <el-table-column prop="id" label="ID" width="70" align="center" />
      <el-table-column prop="username" label="账号" min-width="120" />
      <el-table-column prop="realName" label="姓名" min-width="100" />
      <el-table-column label="角色" width="90" align="center">
        <template slot-scope="{ row }">
          <el-tag :type="row.role === 2 ? 'warning' : 'success'" size="small">
            {{ row.role === 2 ? '教师' : '学生' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="90" align="center">
        <template slot-scope="{ row }">
          <el-switch
            :value="row.status === 1"
            active-color="#13ce66"
            inactive-color="#ff4949"
            @change="value => toggleStatus(row, value)"
          />
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="170" />
      <el-table-column label="操作" width="250" align="center">
        <template slot-scope="{ row }">
          <el-button type="text" @click="openEdit(row)">编辑</el-button>
          <el-button type="text" @click="resetPassword(row)">重置密码</el-button>
          <el-button type="text" class="danger-text" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination
        background
        :current-page="query.pageNum"
        :page-size="query.pageSize"
        :page-sizes="[10, 20, 50]"
        :total="total"
        layout="total, sizes, prev, pager, next"
        @current-change="handlePageChange"
        @size-change="handleSizeChange"
      />
    </div>

    <el-dialog :title="editMode ? '编辑用户' : '新增用户'" :visible.sync="dialogVisible" width="460px">
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="账号" prop="username">
          <el-input v-model="form.username" placeholder="登录账号" />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="真实姓名" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-radio-group v-model="form.role">
            <el-radio :label="2">教师</el-radio>
            <el-radio :label="3">学生</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="!editMode" label="密码" prop="password">
          <el-input v-model="form.password" placeholder="初始密码" />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { userApi } from '../../api'

export default {
  name: 'UserList',
  data() {
    return {
      loading: false,
      saving: false,
      rows: [],
      total: 0,
      query: {
        keyword: '',
        role: null,
        pageNum: 1,
        pageSize: 10
      },
      dialogVisible: false,
      editMode: false,
      form: {
        id: null,
        username: '',
        realName: '',
        role: 3,
        password: '123456'
      },
      rules: {
        username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
        realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
        role: [{ required: true, message: '请选择角色', trigger: 'change' }],
        password: [{ required: true, message: '请输入初始密码', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.load()
  },
  methods: {
    async load() {
      this.loading = true
      try {
        const res = await userApi.page(this.query)
        this.rows = res.data.list
        this.total = res.data.total
      } finally {
        this.loading = false
      }
    },
    search() {
      this.query.pageNum = 1
      this.load()
    },
    reset() {
      this.query = { keyword: '', role: null, pageNum: 1, pageSize: 10 }
      this.load()
    },
    handlePageChange(page) {
      this.query.pageNum = page
      this.load()
    },
    handleSizeChange(size) {
      this.query.pageSize = size
      this.query.pageNum = 1
      this.load()
    },
    openAdd() {
      this.editMode = false
      this.form = { id: null, username: '', realName: '', role: 3, password: '123456' }
      this.dialogVisible = true
      this.$nextTick(() => this.$refs.form && this.$refs.form.clearValidate())
    },
    openEdit(row) {
      this.editMode = true
      this.form = {
        id: row.id,
        username: row.username,
        realName: row.realName,
        role: row.role,
        password: ''
      }
      this.dialogVisible = true
      this.$nextTick(() => this.$refs.form && this.$refs.form.clearValidate())
    },
    submit() {
      this.$refs.form.validate(async valid => {
        if (!valid) return
        this.saving = true
        try {
          if (this.editMode) {
            await userApi.update(this.form.id, {
              username: this.form.username,
              realName: this.form.realName,
              role: this.form.role
            })
          } else {
            await userApi.add(this.form)
          }
          this.$message.success('保存成功')
          this.dialogVisible = false
          this.load()
        } finally {
          this.saving = false
        }
      })
    },
    async toggleStatus(row, value) {
      const status = value ? 1 : 0
      try {
        await userApi.updateStatus(row.id, status)
        row.status = status
        this.$message.success(value ? '已启用' : '已禁用')
      } catch (e) {
        row.status = value ? 0 : 1
      }
    },
    resetPassword(row) {
      this.$confirm(`确定将 ${row.realName || row.username} 的密码重置为 123456 吗？`, '提示', {
        type: 'warning'
      }).then(async () => {
        await userApi.resetPassword(row.id)
        this.$message.success('密码已重置为 123456')
      }).catch(() => {})
    },
    remove(row) {
      this.$confirm(`确定删除用户 ${row.realName || row.username} 吗？`, '提示', {
        type: 'warning'
      }).then(async () => {
        await userApi.remove(row.id)
        this.$message.success('删除成功')
        this.load()
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.danger-text {
  color: #f56c6c;
}
</style>
