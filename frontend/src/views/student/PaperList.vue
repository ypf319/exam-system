<template>
  <div>
    <div class="page-card">
      <div class="toolbar">
        <span class="card-title" style="margin: 0">可参加的考试</span>
        <div class="spacer"></div>
        <el-button icon="el-icon-refresh" @click="load">刷新</el-button>
      </div>
      <el-row v-loading="loading" :gutter="16">
        <el-col v-for="paper in rows" :key="paper.id" :xs="24" :sm="12" :lg="8">
          <el-card shadow="hover" class="paper-card">
            <div class="paper-head">
              <h3>{{ paper.paperName }}</h3>
              <el-tag :type="statusTag(paper)" size="small">{{ statusText(paper) }}</el-tag>
            </div>
            <p class="course">{{ paper.courseName }}</p>
            <div class="meta">
              <span><i class="el-icon-medal"></i> 总分 {{ paper.totalScore }} 分</span>
              <span><i class="el-icon-time"></i> {{ paper.duration }} 分钟</span>
            </div>
            <div class="meta"><i class="el-icon-date"></i> 开始：{{ paper.startTime || '不限' }}</div>
            <div class="meta"><i class="el-icon-date"></i> 截止：{{ paper.endTime || '不限' }}</div>
            <div class="paper-footer">
              <template v-if="paper.examStatus === 1">
                <span class="score">得分：{{ paper.score }} / {{ paper.totalScore }}</span>
                <el-button type="text" @click="$router.push('/student/records')">查看成绩</el-button>
              </template>
              <el-button v-else type="primary" @click="startExam(paper)">
                {{ paper.examStatus === 0 ? '继续考试' : '开始考试' }}
              </el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>
      <el-empty v-if="!loading && rows.length === 0" description="暂无可参加的考试" />
    </div>
  </div>
</template>

<script>
import { examApi } from '../../api'

export default {
  name: 'StudentPaperList',
  data() {
    return {
      loading: false,
      rows: []
    }
  },
  created() {
    this.load()
  },
  methods: {
    async load() {
      this.loading = true
      try {
        const res = await examApi.available()
        this.rows = res.data
      } finally {
        this.loading = false
      }
    },
    statusText(paper) {
      if (paper.examStatus === 1) return '已提交'
      if (paper.examStatus === 0) return '进行中'
      return '未参加'
    },
    statusTag(paper) {
      if (paper.examStatus === 1) return 'success'
      if (paper.examStatus === 0) return 'warning'
      return 'info'
    },
    startExam(paper) {
      this.$router.push(`/exam/${paper.id}`)
    }
  }
}
</script>

<style scoped>
.paper-card {
  margin-bottom: 16px;
}

.paper-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 8px;
}

.paper-head h3 {
  margin: 0;
  font-size: 16px;
  line-height: 1.4;
}

.course {
  margin: 8px 0 12px;
  color: #409eff;
  font-size: 13px;
}

.meta {
  display: flex;
  gap: 12px;
  color: #606266;
  font-size: 13px;
  line-height: 2;
}

.paper-footer {
  margin-top: 14px;
  padding-top: 12px;
  border-top: 1px solid #ebeef5;
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 32px;
}

.score {
  color: #67c23a;
  font-weight: 600;
}
</style>
