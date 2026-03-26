<template>
  <div class="appointments">
    <h2>预约管理</h2>
    <div class="filter-bar">
      <el-row :gutter="20" style="margin-bottom: 20px;">
        <el-col :span="6" v-if="isAdmin">
          <el-select v-model="selectedCounselor" placeholder="选择咨询师">
            <el-option label="全部咨询师" value=""></el-option>
            <el-option
              v-for="counselor in counselors"
              :key="counselor.id"
              :label="counselor.realName"
              :value="counselor.id">
            </el-option>
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-date-picker
            v-model="selectedDate"
            type="date"
            placeholder="选择日期（留空显示全部）"
            clearable
            @change="loadSlots"
            style="width: 100%"
          ></el-date-picker>
        </el-col>
        <el-col :span="4">
          <el-select v-model="statusFilter" placeholder="筛选状态" clearable>
            <el-option label="全部" value=""></el-option>
            <el-option label="可预约" value="AVAILABLE"></el-option>
            <el-option label="已预约" value="BOOKED"></el-option>
            <el-option label="已取消" value="CANCELLED"></el-option>
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-button type="primary" @click="showCreateDialog">添加预约时间段</el-button>
        </el-col>
      </el-row>
    </div>
    
    <el-table
      :data="filteredSlots"
      v-loading="loading"
      style="width: 100%"
      border
    >
      <el-table-column prop="id" label="ID" width="80"></el-table-column>
      <el-table-column prop="counselorName" label="咨询师"></el-table-column>
      <el-table-column prop="slotDate" label="日期">
        <template slot-scope="scope">
          {{ formatDate(scope.row.slotDate) }}
        </template>
      </el-table-column>
      <el-table-column prop="timeRange" label="时间段">
        <template slot-scope="scope">
          {{ scope.row.startTime }} - {{ scope.row.endTime }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态">
        <template slot-scope="scope">
          <el-tag :type="getStatusType(scope.row.status)">
            {{ getStatusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="studentName" label="预约学生">
        <template slot-scope="scope">
          {{ scope.row.studentName || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180">
        <template slot-scope="scope">
          <el-button
            size="mini"
            v-if="scope.row.status === 'AVAILABLE'"
            type="primary"
            @click="handleEdit(scope.row)">编辑</el-button>
          <el-button
            size="mini"
            type="danger"
            @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <!-- 创建/编辑预约时间段对话框 -->
    <el-dialog :title="isCreating ? '添加预约时间段' : '编辑预约时间段'" :visible.sync="dialogVisible" width="50%">
      <el-form :model="form" :rules="formRules" ref="form" label-width="120px">
        <el-form-item label="咨询师" prop="counselorId" v-if="isAdmin">
          <el-select v-model="form.counselorId" placeholder="请选择咨询师">
            <el-option
              v-for="counselor in counselors"
              :key="counselor.id"
              :label="counselor.realName"
              :value="counselor.id">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="咨询师" v-else>
          <el-input :value="currentUser ? (currentUser.realName || currentUser.username) : ''" disabled></el-input>
        </el-form-item>
        <el-form-item label="日期" prop="slotDate">
          <el-date-picker
            v-model="form.slotDate"
            type="date"
            placeholder="选择日期"
            style="width: 100%"
          ></el-date-picker>
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-time-picker
            v-model="form.startTime"
            format="HH:mm"
            placeholder="选择开始时间"
            style="width: 100%"
          ></el-time-picker>
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-time-picker
            v-model="form.endTime"
            format="HH:mm"
            placeholder="选择结束时间"
            style="width: 100%"
          ></el-time-picker>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { appointmentAPI, counselorAPI } from '@/services/api'
import { mapGetters } from 'vuex'

export default {
  name: 'Appointments',
  data() {
    return {
      loading: false,
      counselors: [],
      slots: [],
      selectedCounselor: '',
      selectedDate: null, // 初始不选择日期，显示所有数据
      statusFilter: '',
      dialogVisible: false,
      isCreating: false,
      form: {
        id: null,
        counselorId: null,
        slotDate: null,
        startTime: null,
        endTime: null
      },
      rules: {
        counselorId: [{ required: true, message: '请选择咨询师', trigger: 'change' }],
        slotDate: [{ required: true, message: '请选择日期', trigger: 'change' }],
        startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
        endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }]
      }
    }
  },
  computed: {
    ...mapGetters(['isAdmin', 'isCounselor', 'currentUser']),
    // 动态验证规则
    formRules() {
      const rules = {
        slotDate: [{ required: true, message: '请选择日期', trigger: 'change' }],
        startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
        endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }]
      }
      // 只有管理员需要验证咨询师选择
      if (this.isAdmin) {
        rules.counselorId = [{ required: true, message: '请选择咨询师', trigger: 'change' }]
      }
      return rules
    },
    filteredSlots() {
      console.log('计算filteredSlots，当前slots:', this.slots)
      console.log('筛选条件 - selectedCounselor:', this.selectedCounselor, 'selectedDate:', this.selectedDate, 'statusFilter:', this.statusFilter)
      
      const filtered = this.slots.filter(slot => {
        const matchesCounselor = !this.selectedCounselor || slot.counselorId == this.selectedCounselor
        const matchesDate = !this.selectedDate || 
          this.formatDate(slot.slotDate) === this.formatDate(this.selectedDate)
        const matchesStatus = !this.statusFilter || slot.status === this.statusFilter
        
        const matches = matchesCounselor && matchesDate && matchesStatus
        if (!matches) {
          console.log('时间段被过滤:', slot, {
            matchesCounselor,
            matchesDate,
            matchesStatus
          })
        }
        return matches
      })
      
      console.log('过滤后的时间段数量:', filtered.length)
      return filtered
    }
  },
  async mounted() {
    // 先加载咨询师数据，再加载预约时间段
    // 即使咨询师数据加载失败，也继续加载预约时间段
    try {
      await this.loadData()
    } catch (error) {
      console.error('加载咨询师数据失败，继续加载预约时间段:', error)
    }
    this.loadSlots()
  },
  methods: {
    async loadData() {
      try {
        // 获取所有咨询师，不传分页参数时使用默认值
        const counselorsRes = await counselorAPI.getAll({ page: 0, size: 1000 })
        let counselorsData = []
        // 后端总是返回分页响应
        if (counselorsRes && counselorsRes.content !== undefined) {
          counselorsData = counselorsRes.content || []
        } else if (Array.isArray(counselorsRes)) {
          counselorsData = counselorsRes
        } else if (counselorsRes && counselorsRes.data && Array.isArray(counselorsRes.data)) {
          counselorsData = counselorsRes.data
        }
        this.counselors = counselorsData
      } catch (error) {
        this.$message.error('加载咨询师数据失败')
        console.error('Failed to load counselors:', error)
        this.counselors = []
      }
    },
    async loadSlots() {
      this.loading = true
      try {
        // 构建请求参数，移除空值
        const params = {}
        if (this.selectedDate) {
          params.date = this.formatDate(this.selectedDate)
        }
        // 只有管理员才需要传递counselorId参数
        if (this.isAdmin && this.selectedCounselor) {
          params.counselorId = this.selectedCounselor
        }
        
        console.log('加载预约时间段，参数:', params)
        const res = await appointmentAPI.getAll(params)
        console.log('预约时间段API响应:', res)
        
        // 确保res是数组或有data属性
        let slotsData = []
        if (Array.isArray(res)) {
          slotsData = res
        } else if (res && res.data && Array.isArray(res.data)) {
          slotsData = res.data
        } else if (res && res.content && Array.isArray(res.content)) {
          slotsData = res.content
        } else if (res && typeof res === 'object') {
          // 如果返回的是对象但不是数组，尝试查找数据
          console.warn('意外的响应格式:', res)
        }
        
        console.log('解析后的预约时间段数据:', slotsData)
        console.log('数据数量:', slotsData.length)
        
        this.slots = slotsData.map(slot => {
          const mappedSlot = {
            ...slot,
            counselorName: this.getCounselorName(slot.counselorId),
            // 为了兼容前端表格显示，添加timeRange属性
            timeRange: `${slot.startTime || ''} - ${slot.endTime || ''}`
          }
          console.log('映射后的单个时间段:', mappedSlot)
          return mappedSlot
        })
        
        console.log('处理后的预约时间段列表:', this.slots)
        console.log('slots数组长度:', this.slots.length)
        console.log('filteredSlots计算属性值:', this.filteredSlots)
      } catch (error) {
        const errorMsg = error.response?.data?.error || error.response?.data?.message || error.message || '未知错误'
        this.$message.error('加载预约时间段失败: ' + errorMsg)
        console.error('Failed to load slots:', error)
        console.error('错误详情:', {
          response: error.response,
          data: error.response?.data,
          status: error.response?.status
        })
        this.slots = []
      } finally {
        this.loading = false
      }
    },
    getCounselorName(counselorId) {
      if (!counselorId) return '未知'
      // 确保ID类型匹配（可能是字符串或数字）
      const counselor = this.counselors.find(c => 
        c.id == counselorId || String(c.id) === String(counselorId)
      )
      return counselor ? (counselor.realName || counselor.username || '未知') : '未知'
    },
    formatDate(date) {
      if (!date) return ''
      const d = date instanceof Date ? date : new Date(date)
      if (isNaN(d.getTime())) {
        // 如果已经是字符串格式 yyyy-MM-dd，直接返回
        if (typeof date === 'string' && /^\d{4}-\d{2}-\d{2}$/.test(date)) {
          return date
        }
        return ''
      }
      const year = d.getFullYear()
      const month = String(d.getMonth() + 1).padStart(2, '0')
      const day = String(d.getDate()).padStart(2, '0')
      return `${year}-${month}-${day}`
    },
    formatTime(time) {
      if (!time) return ''
      // 如果是字符串格式 HH:mm，直接返回
      if (typeof time === 'string' && /^\d{2}:\d{2}$/.test(time)) {
        return time
      }
      // 如果是Date对象，格式化为HH:mm
      if (time instanceof Date) {
        const hours = String(time.getHours()).padStart(2, '0')
        const minutes = String(time.getMinutes()).padStart(2, '0')
        return `${hours}:${minutes}`
      }
      // 如果是字符串，尝试解析
      if (typeof time === 'string') {
        const match = time.match(/(\d{2}):(\d{2})/)
        if (match) {
          return `${match[1]}:${match[2]}`
        }
      }
      return ''
    },
    getStatusType(status) {
      const typeMap = {
        'AVAILABLE': 'success',
        'BOOKED': 'warning',
        'CANCELLED': 'danger'
      }
      return typeMap[status] || 'default'
    },
    getStatusText(status) {
      const textMap = {
        'AVAILABLE': '可预约',
        'BOOKED': '已预约',
        'CANCELLED': '已取消'
      }
      return textMap[status] || status
    },
    showCreateDialog() {
      this.isCreating = true
      this.resetForm()
      this.form.slotDate = this.selectedDate
      // 如果是咨询师，自动设置counselorId为当前用户ID
      if (this.isCounselor && this.currentUser) {
        this.form.counselorId = this.currentUser.id
      }
      this.dialogVisible = true
    },
    resetForm() {
      this.form = {
        id: null,
        counselorId: null,
        slotDate: null,
        startTime: null,
        endTime: null,
        status: 'AVAILABLE'
      }
      if (this.$refs.form) {
        this.$refs.form.resetFields()
      }
    },
    async submitForm() {
      this.$refs.form.validate(async (valid) => {
        if (valid) {
          try {
            // 如果是咨询师，确保counselorId已设置
            if (this.isCounselor && this.currentUser && !this.form.counselorId) {
              this.form.counselorId = this.currentUser.id
            }
            
            // 验证必填字段
            if (!this.form.counselorId) {
              this.$message.error('咨询师信息缺失，请刷新页面重试')
              return
            }
            
            // 验证时间范围
            const startTimeStr = this.formatTime(this.form.startTime)
            const endTimeStr = this.formatTime(this.form.endTime)
            
            if (!startTimeStr || !endTimeStr) {
              this.$message.error('请选择开始时间和结束时间')
              return
            }
            
            // 比较时间字符串
            if (startTimeStr >= endTimeStr) {
              this.$message.error('结束时间必须晚于开始时间')
              return
            }
            
            // 格式化数据为后端期望的格式
            const formData = {
              counselorId: this.form.counselorId,
              slotDate: this.form.slotDate ? this.formatDate(this.form.slotDate) : null,
              startTime: startTimeStr,
              endTime: endTimeStr,
              status: 'AVAILABLE'
            }
            
            if (this.isCreating) {
              await appointmentAPI.create(formData)
              this.$message.success('添加成功')
            } else {
              formData.id = this.form.id
              await appointmentAPI.update(this.form.id, formData)
              this.$message.success('更新成功')
            }
            
            this.dialogVisible = false
            this.loadSlots()
          } catch (error) {
            const errorMsg = error.response?.data?.message || error.response?.data || error.message || '操作失败'
            this.$message.error(errorMsg)
            console.error('Failed to submit form:', error)
          }
        }
      })
    },
    handleEdit(slot) {
      this.isCreating = false
      // 解析日期和时间
      const slotDate = slot.slotDate ? new Date(slot.slotDate + 'T00:00:00') : null
      const startTime = slot.startTime ? this.parseTime(slot.startTime) : null
      const endTime = slot.endTime ? this.parseTime(slot.endTime) : null
      
      this.form = {
        id: slot.id,
        counselorId: slot.counselorId,
        slotDate: slotDate,
        startTime: startTime,
        endTime: endTime,
        status: slot.status
      }
      this.dialogVisible = true
    },
    parseTime(timeStr) {
      if (!timeStr) return null
      // 如果是 HH:mm 格式，转换为Date对象
      if (typeof timeStr === 'string' && /^\d{2}:\d{2}/.test(timeStr)) {
        const [hours, minutes] = timeStr.split(':')
        const date = new Date()
        date.setHours(parseInt(hours), parseInt(minutes), 0, 0)
        return date
      }
      return timeStr
    },
    async handleDelete(slot) {
      try {
        await this.$confirm('确定要删除该预约时间段吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        await appointmentAPI.delete(slot.id)
        this.$message.success('删除成功')
        this.loadSlots()
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error('删除失败')
          console.error('Failed to delete slot:', error)
        }
      }
    }
  },
  watch: {
    selectedCounselor() {
      this.loadSlots()
    },
    statusFilter() {
      // 状态筛选只过滤当前加载的数据
    }
  }
}
</script>

<style scoped>
.appointments {
  padding: 20px;
}
.filter-bar {
  margin-bottom: 20px;
}
</style>