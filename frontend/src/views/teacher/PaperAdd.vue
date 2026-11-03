<template>
  <div class="page-card">
    <el-form ref="baseForm" :model="form" :rules="baseRules" label-width="100px" class="base-form">
      <el-form-item label="试卷名称" prop="paperName">
        <el-input v-model="form.paperName" placeholder="请输入试卷名称" style="max-width: 420px" />
      </el-form-item>
      <el-form-item label="所属课程" prop="courseId">
        <el-select v-model="form.courseId" placeholder="请选择课程" style="width: 260px" @change="onCourseChange">
          <el-option v-for="item in courses" :key="item.id" :label="item.courseName" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="考试时长" prop="duration">
        <el-input-number v-model="form.duration" :min="1" :max="600" />
        <span class="unit">分钟</span>
      </el-form-item>
      <el-form-item label="考试时间" prop="timeRange">
        <el-date-picker
          v-model="form.timeRange"
          type="datetimerange"
          range-separator="至"
          start-placeholder="开始时间"
          end-placeholder="截止时间"
          value-format="yyyy-MM-dd HH:mm:ss"
          style="width: 420px"
        />
      </el-form-item>
    </el-form>

    <el-tabs v-model="mode">
      <el-tab-pane label="自动随机组卷" name="auto">
        <el-form :model="autoForm" label-width="110px" class="auto-form">
          <el-form-item label="单选题数量">
            <el-input-number v-model="autoForm.singleCount" :min="0" :max="100" />
          </el-form-item>
          <el-form-item label="多选题数量">
            <el-input-number v-model="autoForm.multiCount" :min="0" :max="100" />
          </el-form-item>
          <el-form-item label="判断题数量">
            <el-input-number v-model="autoForm.judgeCount" :min="0" :max="100" />
          </el-form-item>
          <el-form-item label="难度">
            <el-select v-model="autoForm.difficulty" placeholder="不限" clearable style="width: 200px">
              <el-option label="简单" :value="1" />
              <el-option label="中等" :value="2" />
              <el-option label="困难" :value="3" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="saving" @click="submitAuto">随机生成试卷</el-button>
            <span class="text-muted tip">系统将从所选课程题库中随机抽取题目，题目不足时会提示。</span>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <el-tab-pane label="手动选择组卷" name="manual">
        <div class="toolbar">
          <el-select v-model="questionQuery.questionType" placeholder="全部题型" clearable style="width: 130px" @change="loadQuestions">
            <el-option label="单选题" :value="1" />
            <el-option label="多选题" :value="2" />
            <el-option label="判断题" :value="3" />
          </el-select>
          <el-select v-model="questionQuery.difficulty" placeholder="全部难度" clearable style="width: 120px" @change="loadQuestions">
            <el-option label="简单" :value="1" />
            <el-option label="中等" :value="2" />
            <el-option label="困难" :value="3" />
          </el-select>
          <el-input
            v-model="questionQuery.keyword"
            placeholder="题干关键词"
            clearable
            style="width: 200px"
            @keyup.enter.native="loadQuestions"
          />
          <el-button type="primary" icon="el-icon-search" @click="loadQuestions">查询</el-button>
          <div class="spacer"></div>
          <span class="text-muted">已选 {{ selectedQuestions.length }} 道题，合计 {{ selectedScore }} 分</span>
        </div>

        <el-table
          v-loading="questionLoading"
          :data="questionRows"
          border
          stripe
          @selection-change="onSelectionChange"
        >
          <el-table-column type="selection" width="50" align="center" />
          <el-table-column prop="title" label="题干" min-width="300" show-overflow-tooltip />
          <el-table-column label="题型" width="90" align="center">
            <template slot-scope="{ row }">{{ typeName(row.questionType) }}</template>
          </el-table-column>
          <el-table-column label="答案" width="90" align="center">
            <template slot-scope="{ row }">
              <span v-if="row.questionType === 3">{{ row.answer === 'true' ? '正确' : '错误' }}</span>
              <span v-else>{{ row.answer }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="score" label="分值" width="70" align="center" />
          <el-table-column label="难度" width="90" align="center">
            <template slot-scope="{ row }">{{ difficultyName(row.difficulty) }}</template>
          </el-table-column>
        </el-table>

        <div class="pagination">
          <el-pagination
            background
            :current-page="questionQuery.pageNum"
            :page-size="questionQuery.pageSize"
            :total="questionTotal"
            layout="total, prev, pager, next"
            @current-change="handlePageChange"
          />
        </div>

        <div class="submit-bar">
          <el-button @click="$router.push('/teacher/papers')">返回列表</el-button>
          <el-button type="primary" :loading="saving" @click="submitManual">生成试卷</el-button>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
import { courseApi, paperApi, questionApi } from '../../api'

export default {
  name: 'PaperAdd',
  data() {
    return {
      courses: [],
      mode: 'auto',
      saving: false,
      form: {
        paperName: '',
        courseId: null,
        duration: 60,
        timeRange: []
      },
      baseRules: {
        paperName: [{ required: true, message: '请输入试卷名称', trigger: 'blur' }],
        courseId: [{ required: true, message: '请选择课程', trigger: 'change' }],
        duration: [{ required: true, message: '请填写考试时长', trigger: 'change' }],
        timeRange: [{ required: true, message: '请选择考试时间', trigger: 'change' }]
      },
      autoForm: {
        singleCount: 5,
        multiCount: 3,
        judgeCount: 2,
        difficulty: null
      },
      questionRows: [],
      questionTotal: 0,
      questionLoading: false,
      questionQuery: {
        courseId: null,
        questionType: null,
        difficulty: null,
        keyword: '',
        pageNum: 1,
        pageSize: 10
      },
      selectedQuestions: []
    }
  },
  computed: {
    selectedScore() {
      return this.selectedQuestions.reduce((sum, item) => sum + (item.score || 0), 0)
    }
  },
  created() {
    this.loadCourses()
  },
  methods: {
    async loadCourses() {
      const res = await courseApi.list()
      this.courses = res.data
    },
    typeName(type) {
      return { 1: '单选', 2: '多选', 3: '判断' }[type]
    },
    difficultyName(value) {
      return { 1: '简单', 2: '中等', 3: '困难' }[value]
    },
    onCourseChange(courseId) {
      this.questionQuery.courseId = courseId
      this.questionQuery.pageNum = 1
      this.selectedQuestions = []
      this.loadQuestions()
    },
    async loadQuestions() {
      if (!this.form.courseId) {
        this.questionRows = []
        this.questionTotal = 0
        return
      }
      this.questionQuery.courseId = this.form.courseId
      this.questionLoading = true
      try {
        const res = await questionApi.page(this.questionQuery)
        this.questionRows = res.data.list
        this.questionTotal = res.data.total
      } finally {
        this.questionLoading = false
      }
    },
    handlePageChange(page) {
      this.questionQuery.pageNum = page
      this.loadQuestions()
    },
    onSelectionChange(selection) {
      this.selectedQuestions = selection
    },
    buildBasePayload() {
      return {
        paperName: this.form.paperName,
        courseId: this.form.courseId,
        duration: this.form.duration,
        startTime: this.form.timeRange && this.form.timeRange[0],
        endTime: this.form.timeRange && this.form.timeRange[1]
      }
    },
    validateBase() {
      if (!this.form.paperName || !this.form.courseId || !this.form.duration) {
        this.$message.warning('请填写完整的试卷基础信息')
        return false
      }
      if (!this.form.timeRange || this.form.timeRange.length !== 2) {
        this.$message.warning('请选择考试开始与截止时间')
        return false
      }
      return true
    },
    async submitAuto() {
      if (!this.validateBase()) return
      const total = (this.autoForm.singleCount || 0) + (this.autoForm.multiCount || 0) + (this.autoForm.judgeCount || 0)
      if (total <= 0) {
        this.$message.warning('请至少设置一种题型的抽题数量')
        return
      }
      this.saving = true
      try {
        const payload = {
          ...this.buildBasePayload(),
          singleCount: this.autoForm.singleCount || 0,
          multiCount: this.autoForm.multiCount || 0,
          judgeCount: this.autoForm.judgeCount || 0,
          difficulty: this.autoForm.difficulty
        }
        await paperApi.createAuto(payload)
        this.$message.success('随机组卷成功')
        this.$router.push('/teacher/papers')
      } finally {
        this.saving = false
      }
    },
    async submitManual() {
      if (!this.validateBase()) return
      if (this.selectedQuestions.length === 0) {
        this.$message.warning('请至少选择一道题目')
        return
      }
      this.saving = true
      try {
        const payload = {
          ...this.buildBasePayload(),
          questionIds: this.selectedQuestions.map(item => item.id)
        }
        await paperApi.createManual(payload)
        this.$message.success('手动组卷成功')
        this.$router.push('/teacher/papers')
      } finally {
        this.saving = false
      }
    }
  }
}
</script>

<style scoped>
.base-form {
  max-width: 720px;
}

.unit {
  margin-left: 8px;
  color: #909399;
}

.auto-form {
  max-width: 560px;
  margin-top: 10px;
}

.tip {
  margin-left: 12px;
}

.submit-bar {
  margin-top: 20px;
  text-align: right;
}
</style>
