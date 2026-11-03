<template>
  <div class="page-card">
    <div class="toolbar">
      <el-select v-model="query.courseId" placeholder="全部课程" clearable style="width: 180px" @change="search">
        <el-option v-for="item in courses" :key="item.id" :label="item.courseName" :value="item.id" />
      </el-select>
      <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 140px" @change="search">
        <el-option label="未发布" :value="0" />
        <el-option label="已启用" :value="1" />
        <el-option label="已关闭" :value="2" />
      </el-select>
      <el-input
        v-model="query.keyword"
        placeholder="试卷名称"
        clearable
        style="width: 200px"
        @keyup.enter.native="search"
      />
      <el-button type="primary" icon="el-icon-search" @click="search">查询</el-button>
      <el-button icon="el-icon-refresh" @click="reset">重置</el-button>
      <div class="spacer"></div>
      <el-button type="success" icon="el-icon-plus" @click="$router.push('/teacher/paper-add')">新建试卷</el-button>
    </div>

    <el-table v-loading="loading" :data="rows" border stripe>
      <el-table-column prop="id" label="ID" width="70" align="center" />
      <el-table-column prop="paperName" label="试卷名称" min-width="200" show-overflow-tooltip />
      <el-table-column prop="courseName" label="课程" width="150" />
      <el-table-column prop="totalScore" label="总分" width="70" align="center" />
      <el-table-column prop="questionCount" label="题数" width="70" align="center" />
      <el-table-column label="时长" width="80" align="center">
        <template slot-scope="{ row }">{{ row.duration }} 分钟</template>
      </el-table-column>
      <el-table-column label="考试时间" width="310">
        <template slot-scope="{ row }">
          <div class="text-muted">{{ row.startTime || '不限' }}</div>
          <div class="text-muted">{{ row.endTime || '不限' }}</div>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="90" align="center">
        <template slot-scope="{ row }">
          <el-tag :type="statusTag(row.status)" size="small">{{ statusName(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="260" align="center">
        <template slot-scope="{ row }">
          <el-button type="text" @click="viewDetail(row)">查看</el-button>
          <el-button type="text" @click="viewRecords(row)">成绩</el-button>
          <el-button v-if="row.status !== 1" type="text" @click="changeStatus(row, 1)">启用</el-button>
          <el-button v-else type="text" @click="changeStatus(row, 2)">关闭</el-button>
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

    <el-dialog :title="detail ? detail.paperName : '试卷详情'" :visible.sync="detailVisible" width="860px">
      <template v-if="detail">
        <el-descriptions :column="3" border size="small" style="margin-bottom: 16px">
          <el-descriptions-item label="课程">{{ detail.courseName }}</el-descriptions-item>
          <el-descriptions-item label="总分">{{ detail.totalScore }}</el-descriptions-item>
          <el-descriptions-item label="时长">{{ detail.duration }} 分钟</el-descriptions-item>
          <el-descriptions-item label="开始时间">{{ detail.startTime || '不限' }}</el-descriptions-item>
          <el-descriptions-item label="截止时间">{{ detail.endTime || '不限' }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ statusName(detail.status) }}</el-descriptions-item>
        </el-descriptions>
        <el-table :data="detail.questions" border size="small" max-height="420">
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column prop="title" label="题干" min-width="320" show-overflow-tooltip />
          <el-table-column label="题型" width="80" align="center">
            <template slot-scope="{ row }">{{ typeName(row.questionType) }}</template>
          </el-table-column>
          <el-table-column label="答案" width="80" align="center">
            <template slot-scope="{ row }">
              <span v-if="row.questionType === 3">{{ row.answer === 'true' ? '正确' : '错误' }}</span>
              <span v-else>{{ row.answer }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="score" label="分值" width="70" align="center" />
        </el-table>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { courseApi, paperApi } from '../../api'

export default {
  name: 'PaperList',
  data() {
    return {
      courses: [],
      rows: [],
      total: 0,
      loading: false,
      query: {
        courseId: null,
        status: null,
        keyword: '',
        pageNum: 1,
        pageSize: 10
      },
      detailVisible: false,
      detail: null
    }
  },
  created() {
    this.loadCourses()
    this.load()
  },
  methods: {
    async loadCourses() {
      const res = await courseApi.list()
      this.courses = res.data
    },
    async load() {
      this.loading = true
      try {
        const res = await paperApi.page(this.query)
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
      this.query = { courseId: null, status: null, keyword: '', pageNum: 1, pageSize: 10 }
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
    statusName(status) {
      return { 0: '未发布', 1: '已启用', 2: '已关闭' }[status]
    },
    statusTag(status) {
      return { 0: 'info', 1: 'success', 2: 'warning' }[status]
    },
    typeName(type) {
      return { 1: '单选', 2: '多选', 3: '判断' }[type]
    },
    async viewDetail(row) {
      const res = await paperApi.detail(row.id)
      this.detail = res.data
      this.detailVisible = true
    },
    viewRecords(row) {
      this.$router.push({ path: '/teacher/records', query: { paperId: row.id } })
    },
    changeStatus(row, status) {
      const action = status === 1 ? '启用' : '关闭'
      this.$confirm(`确定${action}试卷「${row.paperName}」吗？`, '提示', { type: 'warning' })
        .then(async () => {
          await paperApi.updateStatus(row.id, status)
          this.$message.success(`${action}成功`)
          this.load()
        })
        .catch(() => {})
    },
    remove(row) {
      this.$confirm(`确定删除试卷「${row.paperName}」及其成绩记录吗？`, '提示', { type: 'warning' })
        .then(async () => {
          await paperApi.remove(row.id)
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
