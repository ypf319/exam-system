<template>
  <div class="page-card">
    <div class="toolbar">
      <el-input
        v-model="query.keyword"
        placeholder="课程名称"
        clearable
        style="width: 220px"
        @keyup.enter.native="search"
      />
      <el-button type="primary" icon="el-icon-search" @click="search">查询</el-button>
      <el-button icon="el-icon-refresh" @click="reset">重置</el-button>
      <div class="spacer"></div>
      <el-button type="success" icon="el-icon-plus" @click="openAdd">新增课程</el-button>
    </div>

    <el-table v-loading="loading" :data="rows" border stripe>
      <el-table-column prop="id" label="ID" width="70" align="center" />
      <el-table-column prop="courseName" label="课程名称" min-width="180" />
      <el-table-column prop="description" label="课程描述" min-width="260" show-overflow-tooltip />
      <el-table-column prop="createTime" label="创建时间" width="170" />
      <el-table-column label="操作" width="150" align="center">
        <template slot-scope="{ row }">
          <el-button type="text" @click="openEdit(row)">编辑</el-button>
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

    <el-dialog :title="editMode ? '编辑课程' : '新增课程'" :visible.sync="dialogVisible" width="460px">
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="课程名称" prop="courseName">
          <el-input v-model="form.courseName" placeholder="请输入课程名称" />
        </el-form-item>
        <el-form-item label="课程描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入课程描述" />
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
import { courseApi } from '../../api'

export default {
  name: 'Course',
  data() {
    return {
      loading: false,
      saving: false,
      rows: [],
      total: 0,
      query: {
        keyword: '',
        pageNum: 1,
        pageSize: 10
      },
      dialogVisible: false,
      editMode: false,
      form: {
        id: null,
        courseName: '',
        description: ''
      },
      rules: {
        courseName: [{ required: true, message: '请输入课程名称', trigger: 'blur' }]
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
        const res = await courseApi.page(this.query)
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
      this.query = { keyword: '', pageNum: 1, pageSize: 10 }
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
      this.form = { id: null, courseName: '', description: '' }
      this.dialogVisible = true
      this.$nextTick(() => this.$refs.form && this.$refs.form.clearValidate())
    },
    openEdit(row) {
      this.editMode = true
      this.form = { id: row.id, courseName: row.courseName, description: row.description }
      this.dialogVisible = true
      this.$nextTick(() => this.$refs.form && this.$refs.form.clearValidate())
    },
    submit() {
      this.$refs.form.validate(async valid => {
        if (!valid) return
        this.saving = true
        try {
          if (this.editMode) {
            await courseApi.update(this.form.id, this.form)
          } else {
            await courseApi.add(this.form)
          }
          this.$message.success('保存成功')
          this.dialogVisible = false
          this.load()
        } finally {
          this.saving = false
        }
      })
    },
    remove(row) {
      this.$confirm(`确定删除课程「${row.courseName}」吗？`, '提示', { type: 'warning' })
        .then(async () => {
          await courseApi.remove(row.id)
          this.$message.success('删除成功')
          this.load()
        })
        .catch(() => {})
    }
  }
}
</script>

<style scoped>
.danger-text {
  color: #f56c6c;
}
</style>
