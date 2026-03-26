<template>
  <div class="users">
    <h2>用户管理</h2>
    
    <el-row :gutter="20" style="margin-bottom: 20px;">
      <el-col :span="8">
        <el-input
          v-model="searchText"
          placeholder="搜索用户名或真实姓名"
          prefix-icon="el-icon-search"
          clearable
          @input="onSearchInput"
        ></el-input>
      </el-col>
      <el-col :span="4">
        <el-select v-model="roleFilter" placeholder="筛选角色" clearable @change="onRoleChange">
          <el-option label="全部" value=""></el-option>
          <el-option label="学生" value="STUDENT"></el-option>
          <el-option label="咨询师" value="COUNSELOR"></el-option>
          <el-option label="管理员" value="ADMIN"></el-option>
        </el-select>
      </el-col>
    </el-row>
    
    <el-table
      :data="users"
      v-loading="loading"
      style="width: 100%"
      border
    >
      <el-table-column prop="id" label="ID" width="80"></el-table-column>
      <el-table-column prop="username" label="用户名"></el-table-column>
      <el-table-column prop="realName" label="真实姓名"></el-table-column>
      <el-table-column prop="email" label="邮箱"></el-table-column>
      <el-table-column prop="phone" label="电话"></el-table-column>
      <el-table-column prop="role" label="角色">
        <template slot-scope="scope">
          <el-tag :type="getRoleType(scope.row.role)">
            {{ getRoleText(scope.row.role) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="active" label="状态">
        <template slot-scope="scope">
          <el-tag :type="scope.row.active ? 'success' : 'danger'">
            {{ scope.row.active ? '激活' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="300px">
        <template slot-scope="scope">
          <el-button
            size="mini"
            @click="handleView(scope.row)">查看</el-button>
          <el-button
            size="mini"
            type="primary"
            @click="handleEdit(scope.row)">编辑</el-button>
          <el-button
            size="mini"
            type="danger"
            @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <!-- 用户详情对话框 -->
    <el-dialog title="用户详情" :visible.sync="dialogVisible" width="50%">
      <div v-if="selectedUser">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="ID">{{ selectedUser.id }}</el-descriptions-item>
          <el-descriptions-item label="用户名">{{ selectedUser.username }}</el-descriptions-item>
          <el-descriptions-item label="真实姓名">{{ selectedUser.real_name }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ selectedUser.email }}</el-descriptions-item>
          <el-descriptions-item label="电话">{{ selectedUser.phone || '未填写' }}</el-descriptions-item>
          <el-descriptions-item label="角色">
            <el-tag :type="getRoleType(selectedUser.role)">
              {{ getRoleText(selectedUser.role) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="selectedUser.active ? 'success' : 'danger'">
              {{ selectedUser.active ? '激活' : '禁用' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDate(selectedUser.created_at) }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ formatDate(selectedUser.updated_at) }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
    
    <!-- 编辑用户对话框 -->
    <el-dialog title="编辑用户" :visible.sync="editDialogVisible" width="50%">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="editForm.username" disabled></el-input>
        </el-form-item>
        <el-form-item label="真实姓名">
          <el-input v-model="editForm.real_name"></el-input>
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="editForm.email"></el-input>
        </el-form-item>
        <el-form-item label="电话">
          <el-input v-model="editForm.phone"></el-input>
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="editForm.role" placeholder="请选择角色">
            <el-option label="学生" value="STUDENT"></el-option>
            <el-option label="咨询师" value="COUNSELOR"></el-option>
            <el-option label="管理员" value="ADMIN"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="editForm.active"></el-switch>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="updateUser">确定</el-button>
      </div>
    </el-dialog>
    <div style="margin-top: 16px; text-align: right;">
      <el-pagination
        background
        layout="prev, pager, next, sizes, total"
        :total="usersTotal"
        :page-size="usersSize"
        :current-page="usersPage + 1"
        @current-change="onPageChange"
        @size-change="onSizeChange"
        :page-sizes="[10,20,50]"
      />
    </div>
  </div>
</template>

<script>
import { mapGetters } from 'vuex';

export default {
  name: 'Users',
  data() {
    return {
      searchText: '',
      roleFilter: '',
      dialogVisible: false,
      editDialogVisible: false,
      selectedUser: null,
      editForm: {
        id: null,
        username: '',
        realName: '',
        email: '',
        phone: '',
        role: '',
        active: true
      },
      searchTimer: null
    };
  },
  computed: {
    ...mapGetters({
      loadingGetter: 'isLoading',
      users: 'getUsers',
      usersTotal: 'getUsersTotal',
      usersPage: 'getUsersPage',
      usersSize: 'getUsersSize'
    }),
    loading() {
      return this.loadingGetter('users')
    }
  },
  created() {
    this.fetchUsers()
  },
  methods: {
    fetchUsers({ page, size } = {}) {
      this.$store.dispatch('fetchUsers', {
        page,
        size,
        role: this.roleFilter,
        q: this.searchText
      })
    },
    onPageChange(page) {
      this.fetchUsers({ page: page - 1, size: this.usersSize })
    },
    onSizeChange(size) {
      this.fetchUsers({ page: 0, size })
    },
    onRoleChange() {
      this.fetchUsers({ page: 0, size: this.usersSize })
    },
    onSearchInput() {
      if (this.searchTimer) clearTimeout(this.searchTimer)
      this.searchTimer = setTimeout(() => {
        this.fetchUsers({ page: 0, size: this.usersSize })
      }, 400)
    },
    handleView(user) {
      this.selectedUser = user;
      this.dialogVisible = true;
    },
    handleEdit(user) {
      this.editForm = { ...user };
      this.editDialogVisible = true;
    },
    handleDelete(user) {
      this.$confirm(`确定要删除用户 ${user.username} 吗?`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$store.dispatch('deleteUser', user.id)
          .then(() => {
            this.$message({
              type: 'success',
              message: '删除成功!'
            });
          })
          .catch(error => {
            this.$message({
              type: 'error',
              message: `删除失败: ${error.message}`
            });
          });
      }).catch(() => {
        this.$message({
          type: 'info',
          message: '已取消删除'
        });          
      });
    },
    updateUser() {
      this.$store.dispatch('updateUser', {
        id: this.editForm.id,
        user: this.editForm
      })
      .then(() => {
        this.editDialogVisible = false;
        this.$message({
          type: 'success',
          message: '更新成功!'
        });
      })
      .catch(error => {
        this.$message({
          type: 'error',
          message: `更新失败: ${error.message}`
        });
      });
    },
    getRoleType(role) {
      switch (role) {
        case 'STUDENT': return '';
        case 'COUNSELOR': return 'success';
        case 'ADMIN': return 'warning';
        default: return 'info';
      }
    },
    getRoleText(role) {
      switch (role) {
        case 'STUDENT': return '学生';
        case 'COUNSELOR': return '咨询师';
        case 'ADMIN': return '管理员';
        default: return role;
      }
    },
    formatDate(dateString) {
      if (!dateString) return '未知';
      const date = new Date(dateString);
      return date.toLocaleString();
    }
  }
};
</script>

<style scoped>
.users {
  margin-top: 20px;
}

.el-table {
  margin-top: 20px;
}
</style>