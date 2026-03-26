<template>
  <div class="user-detail" v-if="user">
    <el-page-header @back="goBack" :title="user.realName || user.username">
      <template slot="content">
        <el-tag :type="getRoleType(user.role)">
          {{ getRoleText(user.role) }}
        </el-tag>
      </template>
    </el-page-header>
    
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="8">
        <el-card class="user-card">
          <div slot="header">
            <span>基本信息</span>
            <el-button style="float: right; padding: 3px 0" type="text" @click="showEditDialog">编辑</el-button>
          </div>
          <div class="user-avatar">
            <el-avatar :size="100" icon="el-icon-user-solid"></el-avatar>
          </div>
          <el-descriptions :column="1" style="margin-top: 20px;">
            <el-descriptions-item label="ID">{{ user.id }}</el-descriptions-item>
            <el-descriptions-item label="用户名">{{ user.username }}</el-descriptions-item>
            <el-descriptions-item label="真实姓名">{{ user.realName }}</el-descriptions-item>
            <el-descriptions-item label="邮箱">{{ user.email }}</el-descriptions-item>
            <el-descriptions-item label="电话">{{ user.phone || '未填写' }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="user.active ? 'success' : 'danger'">
                {{ user.active ? '激活' : '禁用' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ formatDate(user.createdAt) }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
      
      <el-col :span="16">
        <el-card v-if="user.role === 'STUDENT'">
          <div slot="header">
            <span>咨询记录</span>
            <el-button style="float: right; padding: 3px 0" type="text" @click="createRecord">查看咨询记录</el-button>
          </div>
          <el-table
            :data="studentRecords"
            v-loading="recordsLoading"
            style="width: 100%"
            border
          >
            <el-table-column prop="id" label="ID" width="80"></el-table-column>
            <el-table-column prop="counselorName" label="咨询师"></el-table-column>
            <el-table-column prop="appointmentTime" label="预约时间">
              <template slot-scope="scope">
                {{ formatDate(scope.row.appointmentTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态">
              <template slot-scope="scope">
                <el-tag :type="getStatusType(scope.row.status)">
                  {{ getStatusText(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="summary" label="咨询摘要" show-overflow-tooltip></el-table-column>
            <el-table-column label="操作" width="100">
              <template slot-scope="scope">
                <el-button
                  size="mini"
                  @click="viewRecord(scope.row)">查看</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
        
        <el-card v-if="user.role === 'COUNSELOR'">
          <div slot="header">
            <span>咨询记录</span>
          </div>
          <el-table
            :data="counselorRecords"
            v-loading="recordsLoading"
            style="width: 100%"
            border
          >
            <el-table-column prop="id" label="ID" width="80"></el-table-column>
            <el-table-column prop="studentName" label="学生"></el-table-column>
            <el-table-column prop="appointmentTime" label="预约时间">
              <template slot-scope="scope">
                {{ formatDate(scope.row.appointmentTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态">
              <template slot-scope="scope">
                <el-tag :type="getStatusType(scope.row.status)">
                  {{ getStatusText(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="summary" label="咨询摘要" show-overflow-tooltip></el-table-column>
            <el-table-column label="操作" width="100">
              <template slot-scope="scope">
                <el-button
                  size="mini"
                  @click="viewRecord(scope.row)">查看</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 编辑用户对话框 -->
    <el-dialog title="编辑用户" :visible.sync="editDialogVisible" width="50%">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="editForm.username" disabled></el-input>
        </el-form-item>
        <el-form-item label="真实姓名">
          <el-input v-model="editForm.realName"></el-input>
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="editForm.email"></el-input>
        </el-form-item>
        <el-form-item label="电话">
          <el-input v-model="editForm.phone"></el-input>
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
    
    <!-- 查看咨询记录对话框 -->
    <el-dialog title="咨询记录详情" :visible.sync="recordDialogVisible" width="60%">
      <div v-if="selectedRecord">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="ID">{{ selectedRecord.id }}</el-descriptions-item>
          <el-descriptions-item label="学生姓名">{{ selectedRecord.studentName }}</el-descriptions-item>
          <el-descriptions-item label="咨询师姓名">{{ selectedRecord.counselorName }}</el-descriptions-item>
          <el-descriptions-item label="预约时间">{{ formatDate(selectedRecord.appointmentTime) }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(selectedRecord.status)">
              {{ getStatusText(selectedRecord.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDate(selectedRecord.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="咨询摘要" span="2">{{ selectedRecord.summary || '无' }}</el-descriptions-item>
          <el-descriptions-item label="详细内容" span="2">{{ selectedRecord.content || '无' }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { mapGetters } from 'vuex';

export default {
  name: 'UserDetail',
  data() {
    return {
      userId: null,
      editDialogVisible: false,
      recordDialogVisible: false,
      selectedRecord: null,
      editForm: {
        id: null,
        username: '',
        realName: '',
        email: '',
        phone: '',
        active: true
      }
    };
  },
  computed: {
    ...mapGetters({
      loading: 'isLoading',
      records: 'getRecords',
      users: 'getUsers'
    }),
    user() {
      return this.users.find(u => u.id === this.userId);
    },
    studentRecords() {
      if (!this.user || this.user.role !== 'STUDENT') return [];
      
      return this.records
        .filter(record => record.studentId === this.userId)
        .map(record => {
          const counselor = this.users.find(u => u.id === record.counselorId);
          return {
            ...record,
            counselorName: counselor ? counselor.realName : '未知咨询师'
          };
        });
    },
    counselorRecords() {
      if (!this.user || this.user.role !== 'COUNSELOR') return [];
      
      return this.records
        .filter(record => record.counselorId === this.userId)
        .map(record => {
          const student = this.users.find(u => u.id === record.studentId);
          return {
            ...record,
            studentName: student ? student.realName : '未知学生'
          };
        });
    },
    recordsLoading() {
      return this.loading('records');
    }
  },
  created() {
    this.userId = parseInt(this.$route.params.id);
    this.$store.dispatch('fetchUsers');
    this.$store.dispatch('fetchRecords');
  },
  methods: {
    goBack() {
      this.$router.push('/users');
    },
    showEditDialog() {
      this.editForm = { ...this.user };
      this.editDialogVisible = true;
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
    createRecord() {
      this.$router.push({
        path: '/records',
        query: { studentId: this.userId }
      });
    },
    viewRecord(record) {
      this.selectedRecord = record;
      this.recordDialogVisible = true;
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
    getStatusType(status) {
      switch (status) {
        case 'PENDING': return 'info';
        case 'IN_PROGRESS': return 'primary';
        case 'COMPLETED': return 'success';
        case 'CANCELLED': return 'danger';
        default: return '';
      }
    },
    getStatusText(status) {
      switch (status) {
        case 'PENDING': return '待处理';
        case 'IN_PROGRESS': return '进行中';
        case 'COMPLETED': return '已完成';
        case 'CANCELLED': return '已取消';
        default: return status;
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
.user-detail {
  padding: 20px;
}

.user-card {
  text-align: center;
}

.user-avatar {
  margin: 0 auto;
  width: 100px;
  height: 100px;
}
</style>