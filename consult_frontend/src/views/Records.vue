<template>
  <div class="records">
    <h2>{{ isStudent ? '我的咨询记录' : '咨询记录管理' }}</h2>
    
    <el-row :gutter="20" class="toolbar">
      <el-col :span="8">
        <el-input
          v-model="searchText"
          placeholder="搜索学生或咨询师姓名"
          prefix-icon="el-icon-search"
          clearable
          @input="onSearchInput"
        />
      </el-col>
      <el-col :span="4">
        <el-select v-model="statusFilter" placeholder="筛选状态" clearable @change="onStatusChange">
          <el-option label="全部" :value="undefined" />
          <el-option label="待处理" value="PENDING" />
          <el-option label="进行中" value="IN_PROGRESS" />
          <el-option label="已完成" value="COMPLETED" />
          <el-option label="已取消" value="CANCELLED" />
        </el-select>
      </el-col>
      <el-col :span="4">
        <el-button v-if="isAdmin || isCounselor" type="primary" icon="el-icon-plus" @click="showCreateDialog">创建咨询记录</el-button>
      </el-col>
    </el-row>
    
    <el-table
      :data="records"
      v-loading="loading"
      style="width: 100%"
      border
    >
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column label="学生姓名">
        <template slot-scope="scope">
          {{ getStudentName(scope.row) }}
        </template>
      </el-table-column>
      <el-table-column label="咨询师姓名">
        <template slot-scope="scope">
          {{ getCounselorName(scope.row) }}
        </template>
      </el-table-column>
      <el-table-column label="预约时间" width="180">
        <template slot-scope="scope">
          {{ formatDate(scope.row.appointmentTime) }}
        </template>
      </el-table-column>
      <el-table-column label="状态" width="120">
        <template slot-scope="scope">
          <el-tag :type="getStatusType(scope.row.status)">
            {{ getStatusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="summary" label="咨询摘要" show-overflow-tooltip />
      <el-table-column label="操作" width="300">
        <template slot-scope="scope">
          <el-button size="mini" @click="handleView(scope.row)">查看</el-button>
          <el-button v-if="isAdmin || isCounselor" size="mini" type="primary" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button v-if="isAdmin || isCounselor" size="mini" type="danger" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <div class="table-footer">
      <el-pagination
        background
        layout="prev, pager, next, sizes, total"
        :current-page="pagination.page"
        :page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50]"
        @current-change="onPageChange"
        @size-change="onSizeChange"
      />
    </div>
    
    <el-dialog title="咨询记录详情" :visible.sync="detailDialogVisible" width="600px">
      <el-descriptions v-if="selectedRecord" :column="1" size="small" border>
        <el-descriptions-item label="ID">{{ selectedRecord.id }}</el-descriptions-item>
        <el-descriptions-item label="学生姓名">{{ getStudentName(selectedRecord) }}</el-descriptions-item>
        <el-descriptions-item label="咨询师姓名">{{ getCounselorName(selectedRecord) }}</el-descriptions-item>
        <el-descriptions-item label="预约时间">{{ formatDate(selectedRecord.appointmentTime) }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(selectedRecord.status)">
            {{ getStatusText(selectedRecord.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatDate(selectedRecord.createdAt) }}</el-descriptions-item>
        <el-descriptions-item label="咨询摘要">{{ selectedRecord.summary || '无' }}</el-descriptions-item>
        <el-descriptions-item label="详细内容">{{ selectedRecord.content || '无' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
    
    <el-dialog :title="isCreating ? '创建咨询记录' : '编辑咨询记录'" :visible.sync="editDialogVisible" width="600px">
      <el-form ref="editFormRef" :model="editForm" :rules="rules" label-width="100px">
        <el-form-item label="学生" prop="studentId">
          <el-select v-model="editForm.studentId" placeholder="请选择学生" style="width: 100%">
            <el-option
              v-for="student in students"
              :key="student.id"
              :label="student.realName || student.username"
              :value="student.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="咨询师" prop="counselorId">
          <el-select v-model="editForm.counselorId" placeholder="请选择咨询师" style="width: 100%">
            <el-option
              v-for="counselor in counselors"
              :key="counselor.id"
              :label="counselor.realName || counselor.username"
              :value="counselor.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="预约时间" prop="appointmentTime">
          <el-date-picker
            v-model="editForm.appointmentTime"
            type="datetime"
            placeholder="选择日期时间"
            style="width: 100%"
            format="yyyy-MM-dd HH:mm:ss"
            value-format="yyyy-MM-dd HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="editForm.status" placeholder="请选择状态" style="width: 100%">
            <el-option label="待处理" value="PENDING" />
            <el-option label="进行中" value="IN_PROGRESS" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </el-form-item>
        <el-form-item label="咨询摘要">
          <el-input v-model="editForm.summary" type="textarea" rows="2" placeholder="请输入咨询摘要" />
        </el-form-item>
        <el-form-item label="详细内容" prop="content">
          <el-input v-model="editForm.content" type="textarea" rows="4" placeholder="请输入详细内容" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { recordAPI, studentAPI } from '@/services/api'
import { mapGetters } from 'vuex'

export default {
  name: 'Records',
  data() {
    return {
      loading: false,
      records: [],
      searchText: '',
      statusFilter: undefined,
      debounceTimer: null,
      pagination: {
        page: 1,
        size: 10,
        total: 0
      },
      studentsList: [],
      detailDialogVisible: false,
      editDialogVisible: false,
      selectedRecord: null,
      isCreating: false,
      editForm: {
        id: null,
        studentId: null,
        counselorId: null,
        appointmentTime: '',
        status: 'PENDING',
        summary: '',
        content: ''
      },
      rules: {
        studentId: [{ required: true, message: '请选择学生', trigger: 'change' }],
        counselorId: [{ required: true, message: '请选择咨询师', trigger: 'change' }],
        appointmentTime: [{ required: true, message: '请选择预约时间', trigger: 'change' }],
        status: [{ required: true, message: '请选择状态', trigger: 'change' }],
        content: [{ required: true, message: '请输入详细内容', trigger: 'blur' }]
      }
    }
  },
  computed: {
    ...mapGetters(['getUsers', 'currentUser', 'isAdmin', 'isCounselor', 'isStudent']),
    users() {
      return this.getUsers || []
    },
    students() {
      return this.studentsList;
    },
    counselors() {
      return this.users.filter(user => user.role === 'COUNSELOR' || user.role === 'ADMIN')
    }
  },
  created() {
    // 加载学生数据（用于下拉选择）
    this.fetchStudents()
    this.fetchRecords()
  },
  methods: {
    async fetchRecords({ page = this.pagination.page, size = this.pagination.size } = {}) {
      this.loading = true
      try {
        const statusParam = this.statusFilter === undefined || this.statusFilter === null
          ? undefined
          : this.statusFilter
        
        console.log('开始加载咨询记录，参数:', {
          page: page - 1,
          size,
          status: statusParam,
          q: this.searchText || undefined
        })
        
        const response = await recordAPI.getAll({
          page: page - 1,
          size,
          status: statusParam,
          q: this.searchText || undefined
        })
        
        console.log('咨询记录API响应:', response)
        
        // 处理分页响应
        if (response.content !== undefined) {
          // 分页响应
          const { content = [], totalElements = 0, number = 0, size: pageSize = size } = response
          console.log('解析为分页响应，数据数量:', content.length, '总数:', totalElements)
          this.records = content
          this.pagination = {
            page: number + 1,
            size: pageSize,
            total: totalElements
          }
        } else if (Array.isArray(response)) {
          // 数组响应（向后兼容）
          console.log('解析为数组响应，数据数量:', response.length)
          this.records = response
          this.pagination = {
            page: 1,
            size: response.length,
            total: response.length
          }
        } else {
          console.warn('意外的响应格式:', response)
          this.records = []
        }
        
        console.log('处理后的咨询记录列表，数量:', this.records.length)
      } catch (error) {
        const errorMsg = error.response?.data?.error || error.response?.data?.message || error.message || '未知错误'
        this.$message.error('加载咨询记录失败: ' + errorMsg)
        console.error('Failed to fetch records:', error)
        console.error('错误详情:', {
          response: error.response,
          message: error.message,
          stack: error.stack
        })
        this.records = []
      } finally {
        this.loading = false
      }
    },
    onPageChange(page) {
      this.pagination.page = page
      this.fetchRecords({ page })
    },
    onSizeChange(size) {
      this.pagination.size = size
      this.fetchRecords({ page: 1, size })
    },
    onStatusChange() {
      this.pagination.page = 1
      this.fetchRecords({ page: 1 })
    },
    onSearchInput() {
      if (this.debounceTimer) {
        clearTimeout(this.debounceTimer)
      }
      this.debounceTimer = setTimeout(() => {
        this.pagination.page = 1
        this.fetchRecords({ page: 1 })
      }, 400)
    },
    async fetchStudents() {
      try {
        const response = await this.$store.dispatch('fetchStudents') || await studentAPI.getAll();
        // 确保响应是数组格式
        this.studentsList = Array.isArray(response) ? response : (response.content || []);
      } catch (error) {
        console.error('Failed to fetch students:', error);
        this.studentsList = [];
      }
    },
    getStudentName(record) {
      if (record.student && record.student.realName) {
        return record.student.realName
      }
      const student = this.users.find(u => u.id === record.studentId)
      return student ? (student.realName || student.username) : '未知学生'
    },
    getCounselorName(record) {
      if (record.counselor && record.counselor.realName) {
        return record.counselor.realName
      }
      const counselor = this.users.find(u => u.id === record.counselorId)
      return counselor ? (counselor.realName || counselor.username) : '未知咨询师'
    },
    showCreateDialog() {
      this.isCreating = true
      this.resetForm()
      this.editDialogVisible = true
    },
    handleEdit(row) {
      this.isCreating = false
      this.editForm = {
        id: row.id,
        studentId: row.studentId || (row.student ? row.student.id : null),
        counselorId: row.counselorId || (row.counselor ? row.counselor.id : null),
        appointmentTime: row.appointmentTime ? this.formatDateTimeForPicker(row.appointmentTime) : '',
        status: row.status || 'PENDING',
        summary: row.summary || '',
        content: row.content || ''
      }
      this.editDialogVisible = true
      this.$nextTick(() => {
        if (this.$refs.editFormRef) {
          this.$refs.editFormRef.clearValidate()
        }
      })
    },
    handleView(row) {
      this.selectedRecord = row
      this.detailDialogVisible = true
    },
    async handleDelete(row) {
      try {
        await this.$confirm('确定要删除这条咨询记录吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        await recordAPI.delete(row.id)
        this.$message.success('删除成功')
        this.fetchRecords()
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error('删除失败')
          console.error('Failed to delete record:', error)
        }
      }
    },
    resetForm() {
      this.editForm = {
        id: null,
        studentId: null,
        counselorId: null,
        appointmentTime: '',
        status: 'PENDING',
        summary: '',
        content: ''
      }
      if (this.$refs.editFormRef) {
        this.$refs.editFormRef.resetFields()
      }
    },
    formatDateTimeForPicker(dateTime) {
      if (!dateTime) return ''
      const date = new Date(dateTime)
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      const hours = String(date.getHours()).padStart(2, '0')
      const minutes = String(date.getMinutes()).padStart(2, '0')
      const seconds = String(date.getSeconds()).padStart(2, '0')
      return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
    },
    async submitForm() {
      try {
        await this.$refs.editFormRef.validate()
        
        // 打印表单数据以便调试
        console.log('提交表单数据:', {
          studentId: this.editForm.studentId,
          counselorId: this.editForm.counselorId,
          appointmentTime: this.editForm.appointmentTime,
          status: this.editForm.status,
          summary: this.editForm.summary,
          content: this.editForm.content
        });
        
        const payload = {
          studentId: this.editForm.studentId,
          counselorId: this.editForm.counselorId,
          appointmentTime: this.editForm.appointmentTime,
          status: this.editForm.status,
          summary: this.editForm.summary,
          content: this.editForm.content
        }
        
        // 确保日期格式正确
        if (payload.appointmentTime && typeof payload.appointmentTime === 'string') {
          // 验证日期格式
          const dateRegex = /^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}$/;
          if (!dateRegex.test(payload.appointmentTime)) {
            this.$message.error('日期时间格式错误，请使用正确的日期时间选择器');
            return;
          }
        }
        
        console.log('发送的请求数据:', JSON.stringify(payload, null, 2));
        
        if (this.isCreating) {
          await recordAPI.create(payload)
          this.$message.success('创建成功')
        } else {
          await recordAPI.update(this.editForm.id, payload)
          this.$message.success('更新成功')
        }
        this.editDialogVisible = false
        this.fetchRecords()
      } catch (error) {
        if (error !== 'cancel') {
          const errorMessage = error?.message || error?.error || '保存失败'
          this.$message.error(errorMessage)
          console.error('Failed to submit record form:', error)
        }
      }
    },
    getStatusType(status) {
      switch (status) {
        case 'PENDING': return 'info'
        case 'IN_PROGRESS': return 'primary'
        case 'COMPLETED': return 'success'
        case 'CANCELLED': return 'danger'
        default: return ''
      }
    },
    getStatusText(status) {
      switch (status) {
        case 'PENDING': return '待处理'
        case 'IN_PROGRESS': return '进行中'
        case 'COMPLETED': return '已完成'
        case 'CANCELLED': return '已取消'
        default: return status
      }
    },
    formatDate(dateString) {
      if (!dateString) return '未知'
      const date = new Date(dateString)
      return date.toLocaleString('zh-CN')
    }
  }
}
</script>

<style scoped>
.records {
  padding: 20px;
}

.toolbar {
  margin-bottom: 20px;
}

.table-footer {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
