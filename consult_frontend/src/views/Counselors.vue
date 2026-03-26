<template>
  <div class="counselors">
    <h2>咨询师管理</h2>
    
    <el-row :gutter="20" class="toolbar">
      <el-col :span="8">
        <el-input
          v-model="searchText"
          placeholder="搜索用户名或真实姓名"
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
      <el-col :span="4">
        <el-button type="primary" icon="el-icon-plus" @click="showCreateDialog">添加咨询师</el-button>
      </el-col>
    </el-row>
    
    <el-table
      :data="counselors"
      v-loading="loading"
      border
      style="width: 100%"
    >
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="realName" label="姓名" />
      <el-table-column prop="email" label="邮箱" />
      <el-table-column prop="phone" label="电话" />
      <el-table-column label="状态" width="120">
        <template slot-scope="scope">
          <el-tag :type="scope.row.active ? 'success' : 'info'">
            {{ scope.row.active ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220">
        <template slot-scope="scope">
          <el-button size="mini" @click="handleView(scope.row)">查看</el-button>
          <el-button size="mini" type="primary" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button
            size="mini"
            type="danger"
            @click="toggleActive(scope.row)"
          >
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
    
    <el-dialog title="咨询师详情" :visible.sync="detailDialogVisible" width="480px">
      <el-descriptions v-if="selectedCounselor" :column="1" size="small" border>
        <el-descriptions-item label="用户名">{{ selectedCounselor.username }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ selectedCounselor.realName || '未填写' }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ selectedCounselor.email }}</el-descriptions-item>
        <el-descriptions-item label="电话">{{ selectedCounselor.phone || '未填写' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="selectedCounselor.active ? 'success' : 'info'">
            {{ selectedCounselor.active ? '启用' : '禁用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="角色">{{ selectedCounselor.role }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
    
    <el-dialog :title="isCreating ? '添加咨询师' : '编辑咨询师'" :visible.sync="editDialogVisible" width="480px">
      <el-form ref="editFormRef" :model="editForm" :rules="rules" label-width="100px">
        <el-form-item label="用户名" prop="username" v-if="isCreating">
          <el-input v-model="editForm.username" autocomplete="off" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="editForm.realName" autocomplete="off" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="editForm.email" autocomplete="off" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="电话">
          <el-input v-model="editForm.phone" autocomplete="off" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="初始密码" prop="password" v-if="isCreating">
          <el-input v-model="editForm.password" type="password" autocomplete="new-password" placeholder="请输入初始密码" />
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
import { counselorAPI } from '@/services/api'

export default {
  name: 'Counselors',
  data() {
    return {
      loading: false,
      counselors: [],
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
      selectedCounselor: null,
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
          { min: 3, max: 20, message: '长度需在3-20字符', trigger: 'blur' }
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
  created() {
    this.fetchCounselors()
  },
  methods: {
    async fetchCounselors({ page = this.pagination.page, size = this.pagination.size } = {}) {
      this.loading = true
      try {
        const activeParam = this.statusFilter === undefined || this.statusFilter === null
          ? undefined
          : this.statusFilter
        const response = await counselorAPI.getAll({
          page: page - 1,
          size,
          q: this.searchText || undefined,
          active: activeParam
        })
        const { content = [], totalElements = 0, number = 0, size: pageSize = size } = response
        this.counselors = content
        this.pagination = {
          page: number + 1,
          size: pageSize,
          total: totalElements
        }
      } catch (error) {
        console.error('Failed to fetch counselors:', error)
        this.$message.error('加载咨询师失败')
      } finally {
        this.loading = false
      }
    },
    onPageChange(page) {
      this.pagination.page = page
      this.fetchCounselors({ page })
    },
    onSizeChange(size) {
      this.pagination.size = size
      this.fetchCounselors({ page: 1, size })
    },
    onStatusChange() {
      this.pagination.page = 1
      this.fetchCounselors({ page: 1 })
    },
    onSearchInput() {
      if (this.debounceTimer) {
        clearTimeout(this.debounceTimer)
      }
      this.debounceTimer = setTimeout(() => {
        this.pagination.page = 1
        this.fetchCounselors({ page: 1 })
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
      this.selectedCounselor = row
      this.detailDialogVisible = true
    },
    async toggleActive(row) {
      try {
        await this.$confirm(`确定要${row.active ? '禁用' : '启用'}该咨询师吗？`, '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        const payload = {
          ...row,
          active: !row.active,
          password: ''
        }
        await counselorAPI.updateStatus(row.id, payload)
        this.$message.success('操作成功')
        this.fetchCounselors()
      } catch (error) {
        if (error !== 'cancel') {
          console.error('Failed to toggle counselor status:', error)
          this.$message.error('操作失败')
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
          await counselorAPI.create(payload)
          this.$message.success('添加成功')
        } else {
          if (this.editForm.password) {
            payload.password = this.editForm.password
          }
          await counselorAPI.update(this.editForm.id, payload)
          this.$message.success('更新成功')
        }
        this.editDialogVisible = false
        this.fetchCounselors()
      } catch (error) {
        if (error !== 'cancel') {
          console.error('Failed to submit counselor form:', error)
          this.$message.error('保存失败')
        }
      }
    }
  }
}
</script>

<style scoped>
.counselors {
  padding: 20px;
}

.toolbar {
  margin-bottom: 16px;
}

.table-footer {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>