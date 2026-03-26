<template>
  <div class="sessions">
    <h2>咨询会话管理</h2>
    <div class="filter-bar">
      <el-row :gutter="20" style="margin-bottom: 20px;">
        <el-col :span="8">
          <el-input
            v-model="searchText"
            placeholder="搜索咨询记录标题、学生姓名或咨询师姓名"
            prefix-icon="el-icon-search"
            clearable
          ></el-input>
        </el-col>
        <el-col :span="6">
          <el-select v-model="selectedRecordId" placeholder="选择咨询记录"
            @change="loadSessionsByRecord">
            <el-option label="全部咨询记录" value=""></el-option>
            <el-option
              v-for="record in counselingRecords"
              :key="record.id"
              :label="`${record.title} (${record.studentName} - ${record.counselorName})`"
              :value="record.id">
            </el-option>
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-button v-if="isAdmin || isCounselor" type="primary" @click="showCreateDialog">添加会话记录</el-button>
        </el-col>
      </el-row>
    </div>
    <el-table
      :data="filteredSessions"
      v-loading="loading"
      style="width: 100%"
      border
    >
      <el-table-column prop="id" label="ID" width="80"></el-table-column>
      <el-table-column prop="recordTitle" label="咨询记录"></el-table-column>
      <el-table-column prop="sessionNumber" label="会话序号"></el-table-column>
      <el-table-column prop="sessionDate" label="会话时间">
        <template slot-scope="scope">
          {{ formatDateTime(scope.row.sessionDate) }}
        </template>
      </el-table-column>
      <el-table-column prop="duration" label="会话时长（分钟）"></el-table-column>
      <el-table-column prop="sessionContent" label="会话内容摘要" show-overflow-tooltip min-width="200">
        <template slot-scope="scope">
          {{ scope.row.sessionContent ? scope.row.sessionContent.substring(0, 100) + '...' : '无' }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="300">
        <template slot-scope="scope">
          <el-button
            size="mini"
            @click="handleView(scope.row)">查看</el-button>
          <el-button
            v-if="isAdmin || isCounselor"
            size="mini"
            type="primary"
            @click="handleEdit(scope.row)">编辑</el-button>
          <el-button
            v-if="isAdmin || isCounselor"
            size="mini"
            type="danger"
            @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <!-- 会话详情对话框 -->
    <el-dialog title="会话详情" :visible.sync="detailVisible" width="70%">
      <div v-if="selectedSession">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="咨询记录">{{ selectedSession.recordTitle }}</el-descriptions-item>
          <el-descriptions-item label="会话序号">第 {{ selectedSession.sessionNumber }} 次会话</el-descriptions-item>
          <el-descriptions-item label="会话时间">{{ formatDateTime(selectedSession.sessionDate) }}</el-descriptions-item>
          <el-descriptions-item label="会话时长">{{ selectedSession.duration }} 分钟</el-descriptions-item>
          <el-descriptions-item label="会话内容" :span="2">
            <div class="content-box">{{ selectedSession.sessionContent || '无' }}</div>
          </el-descriptions-item>
          <el-descriptions-item label="下一步计划" :span="2">
            <div class="content-box">{{ selectedSession.nextPlan || '无' }}</div>
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
    <!-- 创建/编辑会话对话框 -->
    <el-dialog :title="isCreating ? '添加会话记录' : '编辑会话记录'" :visible.sync="editVisible" width="70%">
      <el-form :model="form" :rules="rules" ref="form" label-width="120px">
        <el-form-item label="咨询记录" prop="recordId">
          <el-select v-model="form.recordId" placeholder="请选择咨询记录">
            <el-option
              v-for="record in counselingRecords"
              :key="record.id"
              :label="`${record.title} (${record.studentName} - ${record.counselorName})`"
              :value="record.id">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="会话序号" prop="sessionNumber">
          <el-input-number v-model="form.sessionNumber" :min="1" :max="100" placeholder="请输入会话序号"></el-input-number>
        </el-form-item>
        <el-form-item label="会话时间" prop="sessionDate">
          <el-date-picker
            v-model="form.sessionDate"
            type="datetime"
            placeholder="选择会话时间"
            style="width: 100%"
            format="yyyy-MM-dd HH:mm:ss"
            value-format="yyyy-MM-dd HH:mm:ss"
          ></el-date-picker>
        </el-form-item>
        <el-form-item label="会话时长（分钟）" prop="duration">
          <el-input-number v-model="form.duration" :min="1" :max="300" placeholder="请输入会话时长"></el-input-number>
        </el-form-item>
        <el-form-item label="会话内容" prop="sessionContent">
          <el-input type="textarea" v-model="form.sessionContent" placeholder="请输入会话内容" rows="8"></el-input>
        </el-form-item>
        <el-form-item label="下一步计划">
          <el-input type="textarea" v-model="form.nextPlan" placeholder="请输入下一步计划" rows="4"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { recordAPI, sessionAPI } from '@/services/api'
import { mapGetters } from 'vuex'

export default {
  name: 'Sessions',
  data() {
    return {
      loading: false,
      counselingRecords: [],
      sessions: [],
      searchText: '',
      selectedRecordId: '',
      detailVisible: false,
      editVisible: false,
      isCreating: false,
      selectedSession: null,
      form: {
        id: null,
        recordId: null,
        sessionNumber: 1,
        sessionDate: new Date(),
        duration: 60,
        sessionContent: '',
        nextPlan: ''
      },
      rules: {
        recordId: [{ required: true, message: '请选择咨询记录', trigger: 'change' }],
        sessionNumber: [{ required: true, message: '请输入会话序号', trigger: 'blur' }],
        sessionDate: [{ required: true, message: '请选择会话时间', trigger: 'change' }],
        duration: [{ required: true, message: '请输入会话时长', trigger: 'blur' }],
        sessionContent: [{ required: true, message: '请输入会话内容', trigger: 'blur' }]
      }
    }
  },
  computed: {
    ...mapGetters(['currentUser', 'isAdmin', 'isCounselor', 'isStudent']),
    filteredSessions() {
      console.log('计算filteredSessions，当前sessions:', this.sessions)
      console.log('筛选条件 - searchText:', this.searchText, 'selectedRecordId:', this.selectedRecordId)
      
      const filtered = this.sessions.filter(session => {
        const matchesSearch = !this.searchText || 
          (session.recordTitle && session.recordTitle.toLowerCase().includes(this.searchText.toLowerCase())) ||
          (session.studentName && session.studentName.toLowerCase().includes(this.searchText.toLowerCase())) ||
          (session.counselorName && session.counselorName.toLowerCase().includes(this.searchText.toLowerCase()))
        const matchesRecord = !this.selectedRecordId || session.recordId == this.selectedRecordId
        
        const matches = matchesSearch && matchesRecord
        if (!matches) {
          console.log('会话被过滤:', session, {
            matchesSearch,
            matchesRecord
          })
        }
        return matches
      })
      
      console.log('过滤后的会话数量:', filtered.length)
      return filtered
    }
  },
  mounted() {
    this.loadCounselingRecords()
    this.loadAllSessions()
  },
  methods: {
    async loadCounselingRecords() {
      try {
        const res = await recordAPI.getAll()
        let recordsData = []
        if (res.content !== undefined) {
          recordsData = res.content
        } else if (Array.isArray(res)) {
          recordsData = res
        } else if (res.data && Array.isArray(res.data)) {
          recordsData = res.data
        }
        this.counselingRecords = recordsData.map(record => ({
          ...record,
          title: record.summary || record.title || `咨询记录 #${record.id}`,
          studentName: record.student?.realName || record.studentName || '',
          counselorName: record.counselor?.realName || record.counselorName || ''
        }))
      } catch (error) {
        this.$message.error('加载咨询记录失败')
        console.error('Failed to load counseling records:', error)
      }
    },
    async loadAllSessions() {
      this.loading = true
      try {
        console.log('开始加载会话记录...')
        const res = await sessionAPI.getAll()
        console.log('会话记录API响应:', res)
        
        let sessionsData = []
        if (res.content !== undefined) {
          // 分页响应
          sessionsData = res.content
          console.log('解析为分页响应，数据数量:', sessionsData.length)
        } else if (Array.isArray(res)) {
          // 数组响应
          sessionsData = res
          console.log('解析为数组响应，数据数量:', sessionsData.length)
        } else if (res.data && Array.isArray(res.data)) {
          // 包装的数组响应
          sessionsData = res.data
          console.log('解析为包装数组响应，数据数量:', sessionsData.length)
        } else {
          console.warn('意外的响应格式:', res)
        }
        
        console.log('解析后的会话数据:', sessionsData)
        
        this.sessions = sessionsData.map(session => {
          const record = this.counselingRecords.find(r => r.id === session.recordId)
          const mappedSession = {
            ...session,
            recordTitle: record ? (record.summary || record.title || '未知') : '未知',
            studentName: record ? (record.student?.realName || record.studentName || '') : '',
            counselorName: record ? (record.counselor?.realName || record.counselorName || '') : ''
          }
          console.log('映射后的会话:', mappedSession)
          return mappedSession
        })
        
        console.log('处理后的会话列表，数量:', this.sessions.length)
        console.log('filteredSessions计算属性值:', this.filteredSessions)
      } catch (error) {
        const errorMsg = error.response?.data?.error || error.response?.data?.message || error.message || '未知错误'
        this.$message.error('加载会话记录失败: ' + errorMsg)
        console.error('Failed to load sessions:', error)
        console.error('错误详情:', {
          response: error.response,
          message: error.message,
          stack: error.stack
        })
      } finally {
        this.loading = false
      }
    },
    async loadSessionsByRecord() {
      if (!this.selectedRecordId) {
        this.loadAllSessions()
        return
      }
      
      this.loading = true
      try {
        const res = await sessionAPI.getByRecordId(this.selectedRecordId)
        const record = this.counselingRecords.find(r => r.id === this.selectedRecordId)
        const sessionsData = Array.isArray(res) ? res : (res.data || [])
        this.sessions = sessionsData.map(session => ({
          ...session,
          recordTitle: record ? (record.summary || record.title || '未知') : '未知',
          studentName: record ? (record.student?.realName || record.studentName || '') : '',
          counselorName: record ? (record.counselor?.realName || record.counselorName || '') : ''
        }))
      } catch (error) {
        this.$message.error('加载会话记录失败')
        console.error('Failed to load sessions by record:', error)
      } finally {
        this.loading = false
      }
    },
    formatDateTime(dateTime) {
      if (!dateTime) return ''
      const date = new Date(dateTime)
      return date.toLocaleString('zh-CN')
    },
    showCreateDialog() {
      this.isCreating = true
      this.resetForm()
      if (this.selectedRecordId) {
        this.form.recordId = this.selectedRecordId
      }
      this.editVisible = true
    },
    handleView(session) {
      this.selectedSession = session
      this.detailVisible = true
    },
    handleEdit(session) {
      this.isCreating = false
      this.form = {
        id: session.id,
        recordId: session.recordId,
        sessionNumber: session.sessionNumber,
        sessionDate: new Date(session.sessionDate),
        duration: session.duration,
        sessionContent: session.sessionContent || '',
        nextPlan: session.nextPlan || ''
      }
      this.editVisible = true
    },
    async handleDelete(session) {
      try {
        await this.$confirm('确定要删除该会话记录吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        await sessionAPI.delete(session.id)
        this.$message.success('删除成功')
        this.loadAllSessions()
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error('删除失败')
          console.error('Failed to delete session:', error)
        }
      }
    },
    resetForm() {
      this.form = {
        id: null,
        recordId: null,
        sessionNumber: 1,
        sessionDate: new Date(),
        duration: 60,
        sessionContent: '',
        nextPlan: ''
      }
      if (this.$refs.form) {
        this.$refs.form.resetFields()
      }
    },
    async submitForm() {
      try {
        await this.$refs.form.validate()
        
        // 格式化日期时间为后端期望的格式
        let sessionDate = this.form.sessionDate
        if (sessionDate && typeof sessionDate === 'object') {
          // 将Date对象格式化为字符串
          const year = sessionDate.getFullYear()
          const month = String(sessionDate.getMonth() + 1).padStart(2, '0')
          const day = String(sessionDate.getDate()).padStart(2, '0')
          const hours = String(sessionDate.getHours()).padStart(2, '0')
          const minutes = String(sessionDate.getMinutes()).padStart(2, '0')
          const seconds = String(sessionDate.getSeconds()).padStart(2, '0')
          sessionDate = `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
        }
        
        const formData = {
          ...this.form,
          sessionDate
        }
        
        console.log('提交的表单数据:', JSON.stringify(formData, null, 2))
        
        if (this.isCreating) {
          await sessionAPI.create(formData)
          this.$message.success('添加成功')
        } else {
          await sessionAPI.update(this.form.id, formData)
          this.$message.success('更新成功')
        }
        
        this.editVisible = false
        this.loadAllSessions()
      } catch (error) {
        if (error === 'cancel') return
        
        const errorMessage = error?.message || error?.error || '操作失败'
        this.$message.error(errorMessage)
        console.error('Failed to submit form:', error)
      }
    }
  }
}
</script>

<style scoped>
.sessions {
  padding: 20px;
}
.filter-bar {
  margin-bottom: 20px;
}
.content-box {
  white-space: pre-wrap;
  word-break: break-all;
}
</style>