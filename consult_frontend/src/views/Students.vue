<template>
  <div class="students">
    <h2>{{ isStudent ? '个人信息' : '学生管理' }}</h2>
    <el-row :gutter="20" class="toolbar">
      <el-col :span="8">
        <el-input
          v-model="searchText"
          placeholder="搜索用户名、姓名或手机号"
          prefix-icon="el-icon-search"
          clearable
          @input="onSearchInput"
        />
      </el-col>
      <el-col :span="4">
        <el-select v-model="statusFilter" placeholder="筛选状态" clearable @change="onStatusChange">
          <el-option label="全部" :value="undefined" />
          <el-option label="启用" :value="true" />
          <el-option label="禁用" :value="false" />
        </el-select>
      </el-col>
        <el-col :span="4" v-if="isAdmin || isCounselor">
          <el-button type="primary" icon="el-icon-plus" @click="showCreateDialog">添加学生</el-button>
        </el-col>
    </el-row>

    <el-table
      :data="students"
      v-loading="loading"
      style="width: 100%"
      border
    >
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="realName" label="姓名" />
      <el-table-column prop="phone" label="手机号" />
      <el-table-column prop="email" label="邮箱" />
      <el-table-column label="状态" width="120">
        <template slot-scope="scope">
          <el-tag :type="scope.row.active ? 'success' : 'danger'">
            {{ scope.row.active ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="300px">
        <template slot-scope="scope">
          <el-button size="mini" @click="handleView(scope.row)">查看</el-button>
          <el-button v-if="isAdmin || isCounselor || (isStudent && scope.row.id === currentUser.id)" size="mini" type="primary" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button v-if="isAdmin || isCounselor" size="mini" type="danger" @click="toggleActive(scope.row)">
            {{ scope.row.active ? '禁用' : '启用' }}
          </el-button>
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

    <el-dialog title="学生详情" :visible.sync="detailDialogVisible" width="480px">
      <el-descriptions v-if="selectedStudent" :column="1" size="small" border>
        <el-descriptions-item label="用户名">{{ selectedStudent.username }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ selectedStudent.realName || '未填写' }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ selectedStudent.email }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ selectedStudent.phone || '未填写' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="selectedStudent.active ? 'success' : 'danger'">
            {{ selectedStudent.active ? '启用' : '禁用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="角色">{{ selectedStudent.role }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <el-dialog :title="isCreating ? '添加学生' : '编辑学生'" :visible.sync="editDialogVisible" width="480px">
      <el-form ref="editFormRef" :model="editForm" :rules="rules" label-width="100px">
        <el-form-item label="用户名" prop="username" v-if="isCreating">
          <el-input v-model="editForm.username" placeholder="请输入用户名" autocomplete="off" />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="editForm.realName" placeholder="请输入姓名" autocomplete="off" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="editForm.email" placeholder="请输入邮箱" autocomplete="off" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="editForm.phone" placeholder="请输入手机号" autocomplete="off" />
        </el-form-item>
        <el-form-item label="初始密码" prop="password" v-if="isCreating">
          <el-input v-model="editForm.password" type="password" placeholder="请输入初始密码" autocomplete="new-password" />
        </el-form-item>
        <el-form-item label="状态" v-else>
          <el-switch v-model="editForm.active" active-text="启用" inactive-text="禁用" />
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
import { studentAPI } from '@/services/api'

export default {
  name: 'Students',
  data() {
    return {
      loading: false,
      students: [],
      searchText: '',
      statusFilter: undefined,
      debounceTimer: null,
      pagination: {
        page: 1,
        size: 10,
        total: 0
      },
      detailDialogVisible: false,
      editDialogVisible: false,
      selectedStudent: null,
      isCreating: false,
      editForm: {
        id: null,
        username: '',
        realName: '',
        email: '',
        phone: '',
        password: '',
        active: true
      },
      rules: {
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' },
          { min: 3, max: 20, message: '长度需要在3到20字符之间', trigger: 'blur' }
        ],
        realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
        email: [
          { required: true, message: '请输入邮箱', trigger: 'blur' },
          { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
        ],
        password: [{ required: true, message: '请输入初始密码', trigger: 'blur' }]
      }
    }
  },
  computed: {
    currentUser() {
      return this.$store.getters.currentUser;
    },
    isAdmin() {
      return this.$store.getters.isAdmin;
    },
    isCounselor() {
      return this.$store.getters.isCounselor;
    },
    isStudent() {
      return this.$store.getters.isStudent;
    }
  },
  created() {
    this.fetchStudents()
  },
  methods: {
    async fetchStudents({ page = this.pagination.page, size = this.pagination.size } = {}) {
      this.loading = true
      try {
        const activeParam = this.statusFilter === undefined || this.statusFilter === null
          ? undefined
          : this.statusFilter
        const response = await studentAPI.getAll({
          page: page - 1,
          size,
          q: this.searchText || undefined,
          active: activeParam
        })
        
        // 处理分页响应
        if (response && typeof response === 'object') {
          const content = response.content || []
          const totalElements = response.totalElements || 0
          const number = response.number !== undefined ? response.number : page - 1
          const pageSize = response.size || size
          
          this.students = content
          this.pagination = {
            page: number + 1,
            size: pageSize,
            total: totalElements
          }
        } else {
          // 如果不是分页响应，当作数组处理
          this.students = Array.isArray(response) ? response : []
          this.pagination = {
            page: 1,
            size: size,
            total: Array.isArray(response) ? response.length : 0
          }
        }
      } catch (error) {
        this.$message.error('加载学生失败: ' + (error.message || '未知错误'))
        console.error('Failed to fetch students:', error)
        this.students = []
        this.pagination = {
          page: 1,
          size: this.pagination.size,
          total: 0
        }
      } finally {
        this.loading = false
      }
    },
    onPageChange(page) {
      this.pagination.page = page
      this.fetchStudents({ page })
    },
    onSizeChange(size) {
      this.pagination.size = size
      this.fetchStudents({ page: 1, size })
    },
    onStatusChange() {
      this.pagination.page = 1
      this.fetchStudents({ page: 1 })
    },
    onSearchInput() {
      if (this.debounceTimer) {
        clearTimeout(this.debounceTimer)
      }
      this.debounceTimer = setTimeout(() => {
        this.pagination.page = 1
        this.fetchStudents({ page: 1 })
      }, 400)
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
        username: row.username,
        realName: row.realName,
        email: row.email,
        phone: row.phone,
        password: '',
        active: row.active
      }
      this.editDialogVisible = true
      this.$nextTick(() => {
        if (this.$refs.editFormRef) {
          this.$refs.editFormRef.clearValidate()
        }
      })
    },
    handleView(row) {
      this.selectedStudent = row
      this.detailDialogVisible = true
    },
    async toggleActive(row) {
      try {
        await this.$confirm(`确定要${row.active ? '禁用' : '启用'}该学生吗？`, '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        const payload = {
          ...row,
          active: !row.active,
          password: ''
        }
        await studentAPI.updateStatus(row.id, payload)
        this.$message.success('操作成功')
        this.fetchStudents()
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error('操作失败')
          console.error('Failed to toggle student status:', error)
        }
      }
    },
    resetForm() {
      this.editForm = {
        id: null,
        username: '',
        realName: '',
        email: '',
        phone: '',
        password: '',
        active: true
      }
      if (this.$refs.editFormRef) {
        this.$refs.editFormRef.resetFields()
      }
    },
    async submitForm() {
      try {
        await this.$refs.editFormRef.validate()
        const payload = {
          username: this.editForm.username,
          realName: this.editForm.realName,
          email: this.editForm.email,
          phone: this.editForm.phone,
          active: this.editForm.active
        }
        if (this.isCreating) {
          payload.password = this.editForm.password
          await studentAPI.create(payload)
          this.$message.success('添加成功')
        } else {
          if (this.editForm.password) {
            payload.password = this.editForm.password
          }
          await studentAPI.update(this.editForm.id, payload)
          this.$message.success('更新成功')
        }
        this.editDialogVisible = false
        this.fetchStudents()
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error('保存失败')
          console.error('Failed to submit student form:', error)
        }
      }
    }
  }
}
</script>

<style scoped>
.students {
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