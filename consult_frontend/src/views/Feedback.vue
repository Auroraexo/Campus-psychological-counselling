<template>
  <div class="feedback">
    <h2>咨询反馈管理</h2>
    <div class="filter-bar">
      <el-row :gutter="20" style="margin-bottom: 20px;">
        <el-col :span="8">
          <el-input
            v-model="searchText"
            placeholder="搜索学生姓名、咨询师姓名或反馈内容"
            prefix-icon="el-icon-search"
            clearable
          ></el-input>
        </el-col>
        <el-col :span="6">
          <el-select v-model="ratingFilter" placeholder="筛选评分" clearable>
            <el-option label="全部" value=""></el-option>
            <el-option label="5星" :value="5"></el-option>
            <el-option label="4星" :value="4"></el-option>
            <el-option label="3星" :value="3"></el-option>
            <el-option label="2星" :value="2"></el-option>
            <el-option label="1星" :value="1"></el-option>
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-select v-model="anonymousFilter" placeholder="是否匿名" clearable>
            <el-option label="全部" value=""></el-option>
            <el-option label="匿名" :value="true"></el-option>
            <el-option label="非匿名" :value="false"></el-option>
          </el-select>
        </el-col>
      </el-row>
    </div>
    <el-table
      :data="filteredFeedback"
      v-loading="loading"
      style="width: 100%"
      border
    >
      <el-table-column prop="id" label="ID" width="80"></el-table-column>
      <el-table-column prop="recordTitle" label="咨询记录" min-width="150"></el-table-column>
      <el-table-column prop="studentName" label="学生姓名">
        <template slot-scope="scope">
          {{ scope.row.anonymous ? '匿名' : (scope.row.studentName || '未知') }}
        </template>
      </el-table-column>
      <el-table-column prop="counselorName" label="咨询师姓名"></el-table-column>
      <el-table-column prop="rating" label="评分">
        <template slot-scope="scope">
          <el-rate v-model="scope.row.rating" disabled show-score :max="5"></el-rate>
        </template>
      </el-table-column>
      <el-table-column prop="feedback" label="反馈内容" show-overflow-tooltip min-width="200">
        <template slot-scope="scope">
          {{ scope.row.feedback || '无' }}
        </template>
      </el-table-column>
      <el-table-column prop="counselorResponse" label="咨询师回复" show-overflow-tooltip min-width="200">
        <template slot-scope="scope">
          {{ scope.row.counselorResponse || '无' }}
        </template>
      </el-table-column>
      <el-table-column prop="anonymous" label="是否匿名">
        <template slot-scope="scope">
          <el-tag :type="scope.row.anonymous ? 'info' : 'success'">
            {{ scope.row.anonymous ? '是' : '否' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间">
        <template slot-scope="scope">
          {{ formatDateTime(scope.row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template slot-scope="scope">
          <el-button
            size="mini"
            @click="handleView(scope.row)">查看</el-button>
          <el-button
            size="mini"
            type="primary"
            @click="handleReply(scope.row)">回复</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <!-- 反馈详情对话框 -->
    <el-dialog title="反馈详情" :visible.sync="detailVisible" width="70%">
      <div v-if="selectedFeedback">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="咨询记录">{{ selectedFeedback.recordTitle }}</el-descriptions-item>
          <el-descriptions-item label="学生姓名">{{ selectedFeedback.anonymous ? '匿名' : (selectedFeedback.studentName || '未知') }}</el-descriptions-item>
          <el-descriptions-item label="咨询师姓名">{{ selectedFeedback.counselorName }}</el-descriptions-item>
          <el-descriptions-item label="评分">
            <el-rate v-model="selectedFeedback.rating" disabled show-score :max="5"></el-rate>
          </el-descriptions-item>
          <el-descriptions-item label="反馈内容" :span="2">
            <div class="content-box">{{ selectedFeedback.feedback || '无' }}</div>
          </el-descriptions-item>
          <el-descriptions-item label="是否匿名">
            <el-tag :type="selectedFeedback.anonymous ? 'info' : 'success'">
              {{ selectedFeedback.anonymous ? '是' : '否' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="咨询师回复" :span="2">
            <div class="content-box">{{ selectedFeedback.counselorResponse || '无' }}</div>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDateTime(selectedFeedback.createdAt) }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
    <!-- 咨询师回复对话框 -->
    <el-dialog title="咨询师回复" :visible.sync="replyVisible" width="60%">
      <el-form :model="replyForm" :rules="replyRules" ref="replyForm" label-width="120px">
        <el-form-item label="咨询记录">
          <el-input v-model="replyForm.recordTitle" disabled></el-input>
        </el-form-item>
        <el-form-item label="学生">
          <el-input v-model="replyForm.studentName" disabled></el-input>
        </el-form-item>
        <el-form-item label="反馈内容">
          <el-input type="textarea" v-model="replyForm.feedback" disabled rows="4"></el-input>
        </el-form-item>
        <el-form-item label="咨询师回复" prop="counselorResponse">
          <el-input type="textarea" v-model="replyForm.counselorResponse" placeholder="请输入回复内容" rows="4"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="replyVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReply">提交回复</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { feedbackAPI } from '@/services/api'

export default {
  name: 'Feedback',
  data() {
    return {
      loading: false,
      feedbackList: [],
      searchText: '',
      ratingFilter: '',
      anonymousFilter: '',
      detailVisible: false,
      replyVisible: false,
      selectedFeedback: null,
      replyForm: {
        id: null,
        recordTitle: '',
        studentName: '',
        feedback: '',
        counselorResponse: ''
      },
      replyRules: {
        counselorResponse: [{ required: true, message: '请输入回复内容', trigger: 'blur' }]
      }
    }
  },
  computed: {
    filteredFeedback() {
      console.log('计算filteredFeedback，当前feedbackList:', this.feedbackList)
      console.log('筛选条件 - searchText:', this.searchText, 'ratingFilter:', this.ratingFilter, 'anonymousFilter:', this.anonymousFilter)
      
      const filtered = this.feedbackList.filter(feedback => {
        const matchesSearch = !this.searchText || 
          (feedback.studentName && feedback.studentName.toLowerCase().includes(this.searchText.toLowerCase())) ||
          (feedback.counselorName && feedback.counselorName.toLowerCase().includes(this.searchText.toLowerCase())) ||
          (feedback.feedback && feedback.feedback.toLowerCase().includes(this.searchText.toLowerCase())) ||
          (feedback.recordTitle && feedback.recordTitle.toLowerCase().includes(this.searchText.toLowerCase()))
        const matchesRating = !this.ratingFilter || feedback.rating === parseInt(this.ratingFilter)
        const matchesAnonymous = this.anonymousFilter === '' || feedback.anonymous === (this.anonymousFilter === 'true' || this.anonymousFilter === true)
        
        const matches = matchesSearch && matchesRating && matchesAnonymous
        if (!matches) {
          console.log('反馈被过滤:', feedback, {
            matchesSearch,
            matchesRating,
            matchesAnonymous
          })
        }
        return matches
      })
      
      console.log('过滤后的反馈数量:', filtered.length)
      return filtered
    }
  },
  mounted() {
    this.loadFeedback()
  },
  methods: {
    async loadFeedback() {
      this.loading = true
      try {
        console.log('开始加载反馈数据...')
        const res = await feedbackAPI.getAll()
        console.log('反馈API响应:', res)
        
        let feedbackData = []
        if (res.content !== undefined) {
          // 分页响应
          feedbackData = res.content
          console.log('解析为分页响应，数据数量:', feedbackData.length)
        } else if (Array.isArray(res)) {
          // 数组响应
          feedbackData = res
          console.log('解析为数组响应，数据数量:', feedbackData.length)
        } else if (res.data && Array.isArray(res.data)) {
          // 包装的数组响应
          feedbackData = res.data
          console.log('解析为包装数组响应，数据数量:', feedbackData.length)
        } else {
          console.warn('意外的响应格式:', res)
        }
        
        console.log('解析后的反馈数据:', feedbackData)
        
        this.feedbackList = feedbackData.map(feedback => {
          const record = feedback.record || {}
          const mappedFeedback = {
            ...feedback,
            rating: parseInt(feedback.rating) || 0, // 确保评分是数字类型
            recordTitle: record.summary || record.title || `咨询记录 #${record.id || feedback.recordId}`,
            studentName: record.student?.realName || record.studentName || '',
            counselorName: record.counselor?.realName || record.counselorName || ''
          }
          console.log('映射后的反馈:', mappedFeedback)
          return mappedFeedback
        })
        
        console.log('处理后的反馈列表，数量:', this.feedbackList.length)
        console.log('filteredFeedback计算属性值:', this.filteredFeedback)
      } catch (error) {
        const errorMsg = error.response?.data?.error || error.response?.data?.message || error.message || '未知错误'
        this.$message.error('加载反馈数据失败: ' + errorMsg)
        console.error('Failed to load feedback:', error)
        console.error('错误详情:', {
          response: error.response,
          message: error.message,
          stack: error.stack
        })
      } finally {
        this.loading = false
      }
    },
    formatDateTime(dateTime) {
      if (!dateTime) return ''
      const date = new Date(dateTime)
      return date.toLocaleString('zh-CN')
    },
    handleView(feedback) {
      this.selectedFeedback = feedback
      this.detailVisible = true
    },
    handleReply(feedback) {
      this.replyForm = {
        id: feedback.id,
        recordTitle: feedback.recordTitle || '',
        studentName: feedback.anonymous ? '匿名' : (feedback.studentName || ''),
        feedback: feedback.feedback || '',
        counselorResponse: feedback.counselorResponse || ''
      }
      this.replyVisible = true
    },
    async submitReply() {
      try {
        await this.$refs.replyForm.validate()
        
        await feedbackAPI.updateResponse(this.replyForm.id, {
          counselorResponse: this.replyForm.counselorResponse
        })
        
        this.$message.success('回复成功')
        this.replyVisible = false
        this.loadFeedback()
      } catch (error) {
        if (error === 'cancel') return
        this.$message.error('回复失败')
        console.error('Failed to submit reply:', error)
      }
    }
  }
}
</script>

<style scoped>
.feedback {
  padding: 20px;
}
.filter-bar {
  margin-bottom: 20px;
}
.content-box {
  white-space: pre-wrap;
  word-break: break-all;
  min-height: 60px;
}
.el-rate {
  display: inline-block;
}
</style>