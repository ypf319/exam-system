<template>
  <div class="page-card">
    <div class="toolbar">
      <span class="card-title" style="margin: 0">我的考试成绩</span>
      <div class="spacer"></div>
      <el-button icon="el-icon-refresh" @click="load">刷新</el-button>
    </div>

    <el-table v-loading="loading" :data="rows" border stripe>
      <el-table-column prop="courseName" label="课程" width="180" />
      <el-table-column prop="paperName" label="试卷" min-width="220" show-overflow-tooltip />
      <el-table-column label="得分" width="120" align="center">
        <template slot-scope="{ row }">
          <b class="score-text">{{ row.score }}</b> / {{ row.totalScore }}
        </template>
      </el-table-column>
      <el-table-column label="正确率" width="100" align="center">
        <template slot-scope="{ row }">
          {{ row.totalScore ? Math.round((row.score / row.totalScore) * 100) : 0 }}%
        </template>
      </el-table-column>
      <el-table-column prop="submitTime" label="提交时间" width="170" />
      <el-table-column label="操作" width="120" align="center">
        <template slot-scope="{ row }">
          <el-button type="text" @click="viewDetail(row)">查看答卷</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-empty v-if="!loading && rows.length === 0" description="暂无考试记录" />

    <div class="pagination">
      <el-pagination
        background
        :current-page="query.pageNum"
        :page-size="query.pageSize"
        :pager-count="5"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="handlePageChange"
      />
    </div>

    <el-dialog title="答卷详情" :visible.sync="detailVisible" width="860px">
      <template v-if="recordDetail">
        <el-descriptions :column="3" border size="small" style="margin-bottom: 16px">
          <el-descriptions-item label="试卷">{{ recordDetail.paperName }}</el-descriptions-item>
          <el-descriptions-item label="课程">{{ recordDetail.courseName }}</el-descriptions-item>
          <el-descriptions-item label="提交时间">{{ recordDetail.submitTime }}</el-descriptions-item>
          <el-descriptions-item label="得分">
            <b class="score-text">{{ recordDetail.score }}</b> / {{ recordDetail.totalScore }}
          </el-descriptions-item>
        </el-descriptions>
        <el-table :data="answerRows" border size="small" max-height="420">
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column prop="title" label="题干" min-width="260" show-overflow-tooltip />
          <el-table-column label="题型" width="80" align="center">
            <template slot-scope="{ row }">{{ typeName(row.questionType) }}</template>
          </el-table-column>
          <el-table-column label="我的答案" width="100" align="center">
            <template slot-scope="{ row }">{{ displayAnswer(row.studentAnswer, row.questionType) }}</template>
          </el-table-column>
          <el-table-column label="标准答案" width="100" align="center">
            <template slot-scope="{ row }">{{ displayAnswer(row.answer, row.questionType) }}</template>
          </el-table-column>
          <el-table-column label="结果" width="80" align="center">
            <template slot-scope="{ row }">
              <el-tag :type="row.correct ? 'success' : 'danger'" size="small">
                {{ row.correct ? '正确' : '错误' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="score" label="分值" width="70" align="center" />
        </el-table>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { recordApi } from '../../api'

export default {
  name: 'MyRecord',
  data() {
    return {
      loading: false,
      rows: [],
      total: 0,
      query: {
        pageNum: 1,
        pageSize: 10
      },
      detailVisible: false,
      recordDetail: null,
      answerRows: []
    }
  },
  created() {
    this.load()
  },
  methods: {
    async load() {
      this.loading = true
      try {
        const res = await recordApi.my(this.query)
        this.rows = res.data.list
        this.total = res.data.total
      } finally {
        this.loading = false
      }
    },
    handlePageChange(page) {
      this.query.pageNum = page
      this.load()
    },
    typeName(type) {
      return { 1: '单选', 2: '多选', 3: '判断' }[type]
    },
    displayAnswer(answer, type) {
      if (!answer) return '未作答'
      if (type === 3) return answer === 'true' ? '正确' : '错误'
      return answer
    },
    normalize(answer) {
      if (!answer) return ''
      let value = String(answer).trim().replace(/[\s,，;；、]/g, '').toUpperCase()
      if (value === 'TRUE' || value === '正确') return 'TRUE'
      if (value === 'FALSE' || value === '错误') return 'FALSE'
      return value.split('').sort().join('')
    },
    async viewDetail(row) {
      const [detailRes, questionRes] = await Promise.all([
        recordApi.detail(row.id),
        recordApi.questions(row.id)
      ])
      this.recordDetail = detailRes.data
      let answerMap = {}
      try {
        answerMap = JSON.parse(detailRes.data.studentAnswer || '{}')
      } catch (e) {
        answerMap = {}
      }
      this.answerRows = questionRes.data.map(item => {
        const studentAnswer = answerMap[item.questionId] || ''
        const standard = this.normalize(item.answer)
        const student = this.normalize(studentAnswer)
        return {
          ...item,
          studentAnswer,
          correct: student !== '' && student === standard
        }
      })
      this.detailVisible = true
    }
  }
}
</script>

<style scoped>
.score-text {
  color: #67c23a;
  font-size: 15px;
}
</style>
