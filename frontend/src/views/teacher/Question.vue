<template>
  <div class="page-card">
    <div class="toolbar">
      <el-select v-model="query.courseId" placeholder="全部课程" clearable style="width: 180px" @change="search">
        <el-option v-for="item in courses" :key="item.id" :label="item.courseName" :value="item.id" />
      </el-select>
      <el-select v-model="query.questionType" placeholder="全部题型" clearable style="width: 130px" @change="search">
        <el-option label="单选题" :value="1" />
        <el-option label="多选题" :value="2" />
        <el-option label="判断题" :value="3" />
      </el-select>
      <el-select v-model="query.difficulty" placeholder="全部难度" clearable style="width: 120px" @change="search">
        <el-option label="简单" :value="1" />
        <el-option label="中等" :value="2" />
        <el-option label="困难" :value="3" />
      </el-select>
      <el-input
        v-model="query.keyword"
        placeholder="题干关键词"
        clearable
        style="width: 200px"
        @keyup.enter.native="search"
      />
      <el-button type="primary" icon="el-icon-search" @click="search">查询</el-button>
      <el-button icon="el-icon-refresh" @click="reset">重置</el-button>
      <div class="spacer"></div>
      <el-button type="success" icon="el-icon-plus" @click="openAdd">新增题目</el-button>
      <el-button type="warning" icon="el-icon-upload2" @click="openImport">批量导入</el-button>
    </div>

    <el-table v-loading="loading" :data="rows" border stripe>
      <el-table-column prop="id" label="ID" width="70" align="center" />
      <el-table-column prop="title" label="题干" min-width="300" show-overflow-tooltip />
      <el-table-column prop="courseName" label="课程" width="150" />
      <el-table-column label="题型" width="90" align="center">
        <template slot-scope="{ row }">
          <el-tag :type="typeTag(row.questionType)" size="small">{{ typeName(row.questionType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="答案" width="100" align="center">
        <template slot-scope="{ row }">
          <span v-if="row.questionType === 3">{{ row.answer === 'true' ? '正确' : '错误' }}</span>
          <span v-else>{{ row.answer }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="score" label="分值" width="70" align="center" />
      <el-table-column label="难度" width="90" align="center">
        <template slot-scope="{ row }">
          <el-tag :type="difficultyTag(row.difficulty)" size="small">{{ difficultyName(row.difficulty) }}</el-tag>
        </template>
      </el-table-column>
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

    <el-dialog :title="editMode ? '编辑题目' : '新增题目'" :visible.sync="dialogVisible" width="640px">
      <el-form ref="form" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="所属课程" prop="courseId">
          <el-select v-model="form.courseId" placeholder="请选择课程" style="width: 100%">
            <el-option v-for="item in courses" :key="item.id" :label="item.courseName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="题型" prop="questionType">
          <el-radio-group v-model="form.questionType" @change="onTypeChange">
            <el-radio :label="1">单选题</el-radio>
            <el-radio :label="2">多选题</el-radio>
            <el-radio :label="3">判断题</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="题干" prop="title">
          <el-input v-model="form.title" type="textarea" :rows="3" placeholder="请输入题干" />
        </el-form-item>
        <template v-if="form.questionType !== 3">
          <el-form-item label="选项 A" prop="optionA">
            <el-input v-model="form.optionA" placeholder="选项 A 内容" />
          </el-form-item>
          <el-form-item label="选项 B" prop="optionB">
            <el-input v-model="form.optionB" placeholder="选项 B 内容" />
          </el-form-item>
          <el-form-item label="选项 C">
            <el-input v-model="form.optionC" placeholder="选项 C 内容（可空）" />
          </el-form-item>
          <el-form-item label="选项 D">
            <el-input v-model="form.optionD" placeholder="选项 D 内容（可空）" />
          </el-form-item>
        </template>
        <el-form-item label="标准答案" prop="answer">
          <el-select v-if="form.questionType === 1" v-model="form.answer" placeholder="请选择答案" style="width: 200px">
            <el-option v-for="opt in optionsOf(form)" :key="opt" :label="opt" :value="opt" />
          </el-select>
          <el-checkbox-group v-else-if="form.questionType === 2" v-model="form.multiAnswers">
            <el-checkbox v-for="opt in optionsOf(form)" :key="opt" :label="opt" />
          </el-checkbox-group>
          <el-radio-group v-else v-model="form.answer">
            <el-radio label="true">正确</el-radio>
            <el-radio label="false">错误</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="分值" prop="score">
          <el-input-number v-model="form.score" :min="1" :max="100" />
        </el-form-item>
        <el-form-item label="难度" prop="difficulty">
          <el-select v-model="form.difficulty" style="width: 200px">
            <el-option label="简单" :value="1" />
            <el-option label="中等" :value="2" />
            <el-option label="困难" :value="3" />
          </el-select>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
      </span>
    </el-dialog>

    <el-dialog title="批量导入题目" :visible.sync="importVisible" width="520px">
      <el-form label-width="90px">
        <el-form-item label="导入课程" required>
          <el-select v-model="importCourseId" placeholder="请选择课程" style="width: 100%">
            <el-option v-for="item in courses" :key="item.id" :label="item.courseName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="Excel 文件" required>
          <el-upload
            ref="upload"
            drag
            :auto-upload="false"
            :limit="1"
            :file-list="fileList"
            accept=".xlsx,.xls"
            :on-change="onFileChange"
            :on-remove="onFileRemove"
            style="width: 100%"
          >
            <i class="el-icon-upload"></i>
            <div class="el-upload__text">将 Excel 拖到此处，或<em>点击选择</em></div>
            <div slot="tip" class="el-upload__tip">
              请按模板格式填写，<el-button type="text" @click="downloadTemplate">下载导入模板</el-button>
            </div>
          </el-upload>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="importVisible = false">取消</el-button>
        <el-button type="primary" :loading="importing" @click="doImport">开始导入</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { courseApi, questionApi } from '../../api'

export default {
  name: 'Question',
  data() {
    return {
      courses: [],
      rows: [],
      total: 0,
      loading: false,
      saving: false,
      query: {
        courseId: null,
        questionType: null,
        difficulty: null,
        keyword: '',
        pageNum: 1,
        pageSize: 10
      },
      dialogVisible: false,
      editMode: false,
      form: this.emptyForm(),
      rules: {
        courseId: [{ required: true, message: '请选择课程', trigger: 'change' }],
        questionType: [{ required: true, message: '请选择题型', trigger: 'change' }],
        title: [{ required: true, message: '请输入题干', trigger: 'blur' }],
        optionA: [{ required: true, message: '请输入选项 A', trigger: 'blur' }],
        optionB: [{ required: true, message: '请输入选项 B', trigger: 'blur' }],
        score: [{ required: true, message: '请填写分值', trigger: 'change' }],
        difficulty: [{ required: true, message: '请选择难度', trigger: 'change' }]
      },
      importVisible: false,
      importing: false,
      importCourseId: null,
      fileList: [],
      selectedFile: null
    }
  },
  created() {
    this.loadCourses()
    this.load()
  },
  methods: {
    emptyForm() {
      return {
        id: null,
        courseId: null,
        questionType: 1,
        title: '',
        optionA: '',
        optionB: '',
        optionC: '',
        optionD: '',
        answer: '',
        multiAnswers: [],
        score: 5,
        difficulty: 1
      }
    },
    async loadCourses() {
      const res = await courseApi.list()
      this.courses = res.data
    },
    async load() {
      this.loading = true
      try {
        const res = await questionApi.page(this.query)
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
      this.query = {
        courseId: null,
        questionType: null,
        difficulty: null,
        keyword: '',
        pageNum: 1,
        pageSize: 10
      }
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
    typeName(type) {
      return { 1: '单选', 2: '多选', 3: '判断' }[type]
    },
    typeTag(type) {
      return { 1: '', 2: 'success', 3: 'warning' }[type]
    },
    difficultyName(value) {
      return { 1: '简单', 2: '中等', 3: '困难' }[value]
    },
    difficultyTag(value) {
      return { 1: 'success', 2: '', 3: 'danger' }[value]
    },
    optionsOf(form) {
      const options = []
      if (form.optionA) options.push('A')
      if (form.optionB) options.push('B')
      if (form.optionC) options.push('C')
      if (form.optionD) options.push('D')
      return options
    },
    onTypeChange(type) {
      if (type === 1) {
        this.form.answer = ''
        this.form.multiAnswers = []
      } else if (type === 2) {
        this.form.answer = ''
        this.form.multiAnswers = []
      } else {
        this.form.answer = 'true'
        this.form.multiAnswers = []
      }
    },
    openAdd() {
      this.editMode = false
      this.form = this.emptyForm()
      this.form.courseId = this.query.courseId || (this.courses[0] && this.courses[0].id) || null
      this.dialogVisible = true
      this.$nextTick(() => this.$refs.form && this.$refs.form.clearValidate())
    },
    openEdit(row) {
      this.editMode = true
      this.form = {
        id: row.id,
        courseId: row.courseId,
        questionType: row.questionType,
        title: row.title,
        optionA: row.optionA || '',
        optionB: row.optionB || '',
        optionC: row.optionC || '',
        optionD: row.optionD || '',
        answer: row.questionType === 3 ? row.answer : row.questionType === 1 ? row.answer : '',
        multiAnswers: row.questionType === 2 ? row.answer.split('') : [],
        score: row.score,
        difficulty: row.difficulty
      }
      this.dialogVisible = true
      this.$nextTick(() => this.$refs.form && this.$refs.form.clearValidate())
    },
    async submit() {
      this.$refs.form.validate(async valid => {
        if (!valid) return
        if (this.form.questionType === 1 && !this.form.answer) {
          this.$message.warning('请选择单选题答案')
          return
        }
        if (this.form.questionType === 2 && this.form.multiAnswers.length < 2) {
          this.$message.warning('多选题答案至少选择两项')
          return
        }
        if (this.form.questionType === 3 && !this.form.answer) {
          this.$message.warning('请选择判断题答案')
          return
        }
        let answer = this.form.answer
        if (this.form.questionType === 2) {
          answer = [...this.form.multiAnswers].sort().join('')
        }
        if (!answer) {
          this.$message.warning('请设置标准答案')
          return
        }
        const payload = {
          courseId: this.form.courseId,
          questionType: this.form.questionType,
          title: this.form.title,
          optionA: this.form.questionType === 3 ? null : this.form.optionA,
          optionB: this.form.questionType === 3 ? null : this.form.optionB,
          optionC: this.form.questionType === 3 ? null : this.form.optionC,
          optionD: this.form.questionType === 3 ? null : this.form.optionD,
          answer,
          score: this.form.score,
          difficulty: this.form.difficulty
        }
        this.saving = true
        try {
          if (this.editMode) {
            await questionApi.update(this.form.id, payload)
          } else {
            await questionApi.add(payload)
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
      this.$confirm(`确定删除题目「${row.title}」吗？`, '提示', { type: 'warning' })
        .then(async () => {
          await questionApi.remove(row.id)
          this.$message.success('删除成功')
          this.load()
        })
        .catch(() => {})
    },
    openImport() {
      this.importCourseId = this.query.courseId || (this.courses[0] && this.courses[0].id) || null
      this.fileList = []
      this.selectedFile = null
      this.importVisible = true
    },
    onFileChange(file, fileList) {
      this.selectedFile = file.raw
      this.fileList = fileList.slice(-1)
    },
    onFileRemove() {
      this.selectedFile = null
      this.fileList = []
    },
    downloadTemplate() {
      window.open('/api/questions/template')
    },
    async doImport() {
      if (!this.importCourseId) {
        this.$message.warning('请选择导入课程')
        return
      }
      if (!this.selectedFile) {
        this.$message.warning('请选择 Excel 文件')
        return
      }
      this.importing = true
      try {
        const res = await questionApi.importExcel(this.importCourseId, this.selectedFile)
        this.$message.success(res.msg || '导入成功')
        this.importVisible = false
        this.load()
      } finally {
        this.importing = false
      }
    }
  }
}
</script>

<style scoped>
.danger-text {
  color: #f56c6c;
}
</style>
