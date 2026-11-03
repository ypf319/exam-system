<template>
  <div class="exam-page">
    <div class="exam-header">
      <div class="exam-title">
        <el-button icon="el-icon-back" type="text" @click="goBack">返回</el-button>
        <span>{{ exam ? exam.paperName : '在线考试' }}</span>
      </div>
      <div class="exam-info">
        <span class="info-item">总分：{{ exam ? exam.totalScore : '-' }}</span>
        <span class="info-item">题目数：{{ exam ? exam.questions.length : '-' }}</span>
        <span class="countdown" :class="{ warning: remainSeconds <= 300 }">
          <i class="el-icon-timer"></i> {{ countdownText }}
        </span>
        <el-button type="primary" :loading="submitting" :disabled="!exam" @click="confirmSubmit">交卷</el-button>
      </div>
    </div>

    <div v-loading="loading" class="exam-body">
      <template v-if="exam">
        <div class="progress-bar">
          <div class="progress-text">
            已作答 <b>{{ answeredCount }}</b> / {{ exam.questions.length }} 题
          </div>
          <el-progress
            :percentage="progressPercent"
            :show-text="false"
            :stroke-width="8"
            color="#409eff"
          />
        </div>

        <el-card v-for="(question, index) in exam.questions" :key="question.questionId" shadow="never" class="question-card">
          <div class="question-head">
            <span class="question-index">{{ index + 1 }}</span>
            <el-tag size="small" :type="typeTag(question.questionType)">
              {{ typeName(question.questionType) }}
            </el-tag>
            <span class="question-score">{{ question.score }} 分</span>
          </div>
          <div class="question-title">{{ question.title }}</div>

          <div v-if="question.questionType === 1" class="options">
            <el-radio
              v-for="option in optionsOf(question)"
              :key="option.key"
              v-model="answers[question.questionId]"
              :label="option.key"
              class="option-item"
            >
              <b>{{ option.key }}.</b> {{ option.text }}
            </el-radio>
          </div>

          <div v-else-if="question.questionType === 2" class="options">
            <el-checkbox-group v-model="answers[question.questionId]">
              <el-checkbox
                v-for="option in optionsOf(question)"
                :key="option.key"
                :label="option.key"
                class="option-item"
              >
                <b>{{ option.key }}.</b> {{ option.text }}
              </el-checkbox>
            </el-checkbox-group>
          </div>

          <div v-else class="options">
            <el-radio v-model="answers[question.questionId]" label="true" class="option-item">正确</el-radio>
            <el-radio v-model="answers[question.questionId]" label="false" class="option-item">错误</el-radio>
          </div>
        </el-card>

        <div class="bottom-actions">
          <el-button type="primary" size="medium" :loading="submitting" @click="confirmSubmit">提交试卷</el-button>
        </div>
      </template>

      <el-empty v-else-if="!loading" description="试卷加载失败">
        <el-button type="primary" @click="$router.push('/student/papers')">返回考试列表</el-button>
      </el-empty>
    </div>

    <el-dialog title="考试结果" :visible.sync="resultVisible" width="420px" :close-on-click-modal="false" :show-close="false">
      <div class="result-box">
        <i class="el-icon-success result-icon"></i>
        <div class="result-title">交卷成功</div>
        <div class="result-score">{{ resultScore }} <span>/ {{ exam ? exam.totalScore : '-' }} 分</span></div>
        <p class="text-muted">客观题已自动判分，可前往「我的成绩」查看答卷详情。</p>
      </div>
      <span slot="footer">
        <el-button @click="finish">返回考试列表</el-button>
        <el-button type="primary" @click="$router.push('/student/records')">查看成绩</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { examApi } from '../../api'

export default {
  name: 'ExamPage',
  data() {
    return {
      loading: false,
      submitting: false,
      submitted: false,
      exam: null,
      answers: {},
      remainSeconds: 0,
      timer: null,
      resultVisible: false,
      resultScore: 0
    }
  },
  computed: {
    countdownText() {
      const total = Math.max(this.remainSeconds, 0)
      const hour = Math.floor(total / 3600)
      const minute = Math.floor((total % 3600) / 60)
      const second = total % 60
      const pad = value => String(value).padStart(2, '0')
      return hour > 0 ? `${pad(hour)}:${pad(minute)}:${pad(second)}` : `${pad(minute)}:${pad(second)}`
    },
    answeredCount() {
      if (!this.exam) return 0
      return this.exam.questions.filter(question => {
        const value = this.answers[question.questionId]
        return Array.isArray(value) ? value.length > 0 : !!value
      }).length
    },
    progressPercent() {
      if (!this.exam || this.exam.questions.length === 0) return 0
      return Math.round((this.answeredCount / this.exam.questions.length) * 100)
    }
  },
  created() {
    this.loadExam()
  },
  beforeDestroy() {
    this.clearTimer()
    window.removeEventListener('beforeunload', this.handleBeforeUnload)
  },
  beforeRouteLeave(to, from, next) {
    if (!this.submitted && this.exam) {
      this.$confirm('考试尚未提交，确定离开吗？离开后答题进度将不会保存。', '提示', { type: 'warning' })
        .then(() => next())
        .catch(() => next(false))
      return
    }
    next()
  },
  methods: {
    async loadExam() {
      this.loading = true
      try {
        const paperId = Number(this.$route.params.paperId)
        const res = await examApi.start(paperId)
        this.exam = res.data
        this.remainSeconds = res.data.remainSeconds || 0
        this.initAnswers(res.data)
        this.startTimer()
        window.addEventListener('beforeunload', this.handleBeforeUnload)
      } catch (e) {
        setTimeout(() => this.$router.push('/student/papers'), 800)
      } finally {
        this.loading = false
      }
    },
    initAnswers(data) {
      let saved = {}
      try {
        saved = JSON.parse(data.savedAnswer || '{}')
      } catch (e) {
        saved = {}
      }
      const answers = {}
      data.questions.forEach(question => {
        const value = saved[question.questionId]
        if (question.questionType === 2) {
          answers[question.questionId] = value ? String(value).split('') : []
        } else {
          answers[question.questionId] = value || ''
        }
      })
      this.answers = answers
    },
    startTimer() {
      this.clearTimer()
      this.timer = setInterval(() => {
        if (this.remainSeconds <= 0) {
          this.autoSubmit()
          return
        }
        this.remainSeconds--
        if (this.remainSeconds <= 0) {
          this.autoSubmit()
        }
      }, 1000)
    },
    clearTimer() {
      if (this.timer) {
        clearInterval(this.timer)
        this.timer = null
      }
    },
    handleBeforeUnload(event) {
      if (!this.submitted && this.exam) {
        event.preventDefault()
        event.returnValue = ''
      }
    },
    optionsOf(question) {
      const list = []
      if (question.optionA) list.push({ key: 'A', text: question.optionA })
      if (question.optionB) list.push({ key: 'B', text: question.optionB })
      if (question.optionC) list.push({ key: 'C', text: question.optionC })
      if (question.optionD) list.push({ key: 'D', text: question.optionD })
      return list
    },
    typeName(type) {
      return { 1: '单选题', 2: '多选题', 3: '判断题' }[type]
    },
    typeTag(type) {
      return { 1: '', 2: 'success', 3: 'warning' }[type]
    },
    confirmSubmit() {
      if (!this.exam || this.submitting) return
      const unanswered = this.exam.questions.length - this.answeredCount
      const message = unanswered > 0
        ? `还有 ${unanswered} 题未作答，确定交卷吗？`
        : '确定提交试卷吗？提交后不可修改。'
      this.$confirm(message, '确认交卷', { type: 'warning' })
        .then(() => this.doSubmit())
        .catch(() => {})
    },
    autoSubmit() {
      if (this.submitting || this.submitted) return
      this.clearTimer()
      this.$message.warning('考试时间到，系统正在自动交卷')
      this.doSubmit()
    },
    async doSubmit() {
      if (!this.exam || this.submitting || this.submitted) return
      this.submitting = true
      try {
        const payload = {}
        this.exam.questions.forEach(question => {
          let value = this.answers[question.questionId]
          if (question.questionType === 2) {
            value = Array.isArray(value) ? [...value].sort().join('') : ''
          }
          payload[question.questionId] = value || ''
        })
        const res = await examApi.submit(this.exam.paperId, {
          recordId: this.exam.recordId,
          answers: payload
        })
        this.submitted = true
        this.clearTimer()
        this.resultScore = res.data
        this.resultVisible = true
        window.removeEventListener('beforeunload', this.handleBeforeUnload)
      } catch (e) {
        this.clearTimer()
      } finally {
        this.submitting = false
      }
    },
    finish() {
      this.resultVisible = false
      this.$router.push('/student/papers')
    },
    goBack() {
      this.$router.push('/student/papers')
    }
  }
}
</script>

<style scoped>
.exam-page {
  min-height: 100%;
  background: #f0f2f5;
}

.exam-header {
  position: sticky;
  top: 0;
  z-index: 10;
  height: 60px;
  padding: 0 24px;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 1px 6px rgba(0, 21, 41, 0.1);
}

.exam-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 17px;
  font-weight: 600;
}

.exam-info {
  display: flex;
  align-items: center;
  gap: 18px;
}

.info-item {
  color: #606266;
  font-size: 13px;
}

.countdown {
  font-size: 18px;
  font-weight: 700;
  color: #409eff;
  font-variant-numeric: tabular-nums;
}

.countdown.warning {
  color: #f56c6c;
}

.exam-body {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px 16px 40px;
}

.progress-bar {
  background: #fff;
  border-radius: 8px;
  padding: 14px 20px;
  margin-bottom: 16px;
}

.progress-text {
  margin-bottom: 8px;
  color: #606266;
  font-size: 13px;
}

.question-card {
  margin-bottom: 16px;
  border-radius: 8px;
}

.question-head {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}

.question-index {
  width: 26px;
  height: 26px;
  line-height: 26px;
  text-align: center;
  border-radius: 50%;
  background: #409eff;
  color: #fff;
  font-size: 13px;
}

.question-score {
  color: #909399;
  font-size: 13px;
}

.question-title {
  font-size: 15px;
  line-height: 1.7;
  margin-bottom: 12px;
  word-break: break-all;
}

.options {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.option-item {
  display: flex;
  align-items: flex-start;
  white-space: normal;
  height: auto;
  margin-right: 0;
  padding: 8px 10px;
  border-radius: 6px;
  transition: background 0.2s;
}

.option-item:hover {
  background: #f5f7fa;
}

.bottom-actions {
  text-align: center;
  margin-top: 24px;
}

.result-box {
  text-align: center;
  padding: 10px 0;
}

.result-icon {
  font-size: 54px;
  color: #67c23a;
}

.result-title {
  margin-top: 8px;
  font-size: 18px;
  font-weight: 600;
}

.result-score {
  margin-top: 12px;
  font-size: 36px;
  font-weight: 700;
  color: #409eff;
}

.result-score span {
  font-size: 16px;
  color: #909399;
  font-weight: 400;
}
</style>
