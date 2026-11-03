<template>
  <div>
    <div class="page-card">
      <div class="toolbar">
        <el-select v-model="paperId" placeholder="请选择试卷" filterable style="width: 320px" @change="onPaperChange">
          <el-option v-for="item in papers" :key="item.id" :label="item.paperName" :value="item.id" />
        </el-select>
        <el-button type="primary" icon="el-icon-search" :disabled="!paperId" @click="refresh">查询</el-button>
        <div class="spacer"></div>
        <el-button type="success" icon="el-icon-download" :disabled="!paperId" @click="exportExcel">导出成绩</el-button>
      </div>

      <el-empty v-if="!paperId" description="请选择试卷查看成绩" />

      <template v-else>
        <el-row v-if="statistics" :gutter="12" class="stat-row">
          <el-col :xs="12" :sm="8" :md="4">
            <div class="stat-item">
              <div class="stat-value">{{ statistics.studentCount }}</div>
              <div class="stat-label">参与人数</div>
            </div>
          </el-col>
          <el-col :xs="12" :sm="8" :md="4">
            <div class="stat-item">
              <div class="stat-value">{{ statistics.submittedCount }}</div>
              <div class="stat-label">已提交</div>
            </div>
          </el-col>
          <el-col :xs="12" :sm="8" :md="4">
            <div class="stat-item">
              <div class="stat-value">{{ statistics.maxScore }}</div>
              <div class="stat-label">最高分</div>
            </div>
          </el-col>
          <el-col :xs="12" :sm="8" :md="4">
            <div class="stat-item">
              <div class="stat-value">{{ statistics.minScore }}</div>
              <div class="stat-label">最低分</div>
            </div>
          </el-col>
          <el-col :xs="12" :sm="8" :md="4">
            <div class="stat-item">
              <div class="stat-value">{{ statistics.avgScore }}</div>
              <div class="stat-label">平均分</div>
            </div>
          </el-col>
          <el-col :xs="12" :sm="8" :md="4">
            <div class="stat-item">
              <div class="stat-value">{{ statistics.passRate }}%</div>
              <div class="stat-label">及格率</div>
            </div>
          </el-col>
        </el-row>

        <el-table v-loading="loading" :data="rows" border stripe>
          <el-table-column prop="username" label="学号" width="130" />
          <el-table-column prop="studentName" label="姓名" width="120" />
          <el-table-column prop="score" label="得分" width="90" align="center" />
          <el-table-column prop="totalScore" label="总分" width="90" align="center" />
          <el-table-column label="状态" width="100" align="center">
            <template slot-scope="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
                {{ row.status === 1 ? '已提交' : '未提交' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="submitTime" label="提交时间" width="170" />
          <el-table-column label="操作" width="120" align="center">
            <template slot-scope="{ row }">
              <el-button type="text" :disabled="row.status !== 1" @click="viewDetail(row)">查看答卷</el-button>
            </template>
          </el-table-column>
        </el-table>

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
      </template>
    </div>

    <el-dialog title="答卷详情" :visible.sync="detailVisible" width="860px">
      <template v-if="recordDetail">
        <el-descriptions :column="3" border size="small" style="margin-bottom: 16px">
          <el-descriptions-item label="学号">{{ recordDetail.username }}</el-descriptions-item>
          <el-descriptions-item label="姓名">{{ recordDetail.studentName }}</el-descriptions-item>
          <el-descriptions-item label="试卷">{{ recordDetail.paperName }}</el-descriptions-item>
          <el-descriptions-item label="得分">
            <b class="score-text">{{ recordDetail.score }}</b> / {{ recordDetail.totalScore }}
          </el-descriptions-item>
          <el-descriptions-item label="提交时间">{{ recordDetail.submitTime }}</el-descriptions-item>
          <el-descriptions-item label="课程">{{ recordDetail.courseName }}</el-descriptions-item>
        </el-descriptions>
        <el-table :data="answerRows" border size="small" max-height="420">
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column prop="title" label="题干" min-width="260" show-overflow-tooltip />
          <el-table-column label="题型" width="80" align="center">
            <template slot-scope="{ row }">{{ typeName(row.questionType) }}</template>
          </el-table-column>
          <el-table-column label="标准答案" width="100" align="center">
            <template slot-scope="{ row }">{{ displayAnswer(row.answer, row.questionType) }}</template>
          </el-table-column>
          <el-table-column label="学生答案" width="100" align="center">
            <template slot-scope="{ row }">{{ displayAnswer(row.studentAnswer, row.questionType) }}</template>
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
import { paperApi, recordApi } from '../../api'

export default {
  name: 'Record',
  data() {
    return {
      papers: [],
      paperId: null,
      statistics: null,
      rows: [],
      total: 0,
      loading: false,
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
    this.init()
  },
  methods: {
    async init() {
      const res = await paperApi.page({ pageNum: 1, pageSize: 100 })
      this.papers = res.data.list
      const queryPaperId = Number(this.$route.query.paperId)
      if (queryPaperId) {
        this.paperId = queryPaperId
      } else if (this.papers.length > 0) {
        this.paperId = this.papers[0].id
      }
      if (this.paperId) {
        this.refresh()
      }
    },
    onPaperChange() {
      this.query.pageNum = 1
      this.refresh()
    },
    refresh() {
      this.load()
      this.loadStatistics()
    },
    async load() {
      if (!this.paperId) return
      this.loading = true
      try {
        const res = await recordApi.page({
          paperId: this.paperId,
          pageNum: this.query.pageNum,
          pageSize: this.query.pageSize
        })
        this.rows = res.data.list
        this.total = res.data.total
      } finally {
        this.loading = false
      }
    },
    async loadStatistics() {
      if (!this.paperId) return
      const res = await recordApi.statistics({ paperId: this.paperId })
      this.statistics = res.data
    },
    handlePageChange(page) {
      this.query.pageNum = page
      this.load()
    },
    exportExcel() {
      window.open(`/api/records/export?paperId=${this.paperId}`)
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
.stat-row {
  margin-bottom: 16px;
}

.stat-item {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 14px 8px;
  text-align: center;
  margin-bottom: 10px;
}

.stat-value {
  font-size: 22px;
  font-weight: 600;
  color: #409eff;
}

.stat-label {
  margin-top: 4px;
  color: #909399;
  font-size: 12px;
}

.score-text {
  color: #f56c6c;
  font-size: 16px;
}
</style>
