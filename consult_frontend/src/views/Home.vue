<template>
  <div class="home-container">
    <!-- 简约标题区域 -->
    <div class="page-header">
      <h1>{{ currentUser ? currentUser.realName : '用户' }}</h1>
      <p class="subtitle">校园心理咨询管理系统</p>
    </div>
    
    <!-- 统计数据区域 - 简约风格 -->
    <div class="stats-grid">
      <div class="stat-item">
        <div class="stat-icon">
          <i class="el-icon-user"></i>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ isLoading('users') ? '...' : totalUsers }}</div>
          <div class="stat-label">用户总数</div>
        </div>
      </div>
      <div class="stat-item">
        <div class="stat-icon">
          <i class="el-icon-school"></i>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ isLoading('users') ? '...' : totalStudents }}</div>
          <div class="stat-label">学生数量</div>
        </div>
      </div>
      <div class="stat-item">
        <div class="stat-icon">
          <i class="el-icon-service"></i>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ isLoading('users') ? '...' : totalCounselors }}</div>
          <div class="stat-label">咨询师数量</div>
        </div>
      </div>
      <div class="stat-item">
        <div class="stat-icon">
          <i class="el-icon-document"></i>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ isLoading('records') ? '...' : totalRecords }}</div>
          <div class="stat-label">咨询记录</div>
        </div>
      </div>
    </div>

    <!-- 内容区域 - 简约卡片 -->
    <div class="content-area">
      <!-- 最新记录 -->
      <div class="content-card">
        <div class="card-title">最新记录</div>
        <div class="records-list">
          <div v-if="isLoading('records')" class="loading-placeholder">
            <el-skeleton :rows="3" animated />
          </div>
          <div v-else-if="latestRecords.length > 0">
            <div 
              v-for="record in latestRecords" 
              :key="record.id" 
              class="record-item"
              @click="viewRecordDetail(record.id)"
            >
              <div class="record-info">
                <div class="record-title">{{ record.title }}</div>
                <div class="record-meta">
                  <span class="record-status" :class="getStatusClass(record.status)">{{ getStatusText(record.status) }}</span>
                  <span class="record-date">{{ formatDate(record.appointmentTime || record.createdAt) }}</span>
                </div>
              </div>
            </div>
          </div>
          <div v-else class="empty-state">
            <p>暂无咨询记录</p>
            <el-button type="primary" size="small" @click="$router.push('/records')">创建记录</el-button>
          </div>
        </div>
        <div class="card-footer">
          <el-button type="text" @click="$router.push('/records')">查看全部</el-button>
        </div>
      </div>

      <!-- 快速操作 -->
      <div class="content-card">
        <div class="card-title">快速操作</div>
        <div class="actions-grid">
          <div class="action-item" @click="$router.push('/users')">
            <i class="el-icon-user-solid"></i>
            <span>用户管理</span>
          </div>
          <div class="action-item" @click="$router.push('/records')">
            <i class="el-icon-edit-outline"></i>
            <span>咨询记录</span>
          </div>
          <div class="action-item" @click="$router.push('/sessions')">
            <i class="el-icon-date"></i>
            <span>咨询会话</span>
          </div>
          <div class="action-item" @click="$router.push('/appointments')">
            <i class="el-icon-time"></i>
            <span>预约管理</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Home',
  computed: {
    // 获取当前用户信息
    currentUser() {
      return this.$store.getters.currentUser;
    },
    // 获取用户总数
    totalUsers() {
      return this.$store.getters.getUsersTotal || this.$store.getters.getUsers.length || 0;
    },
    // 获取学生数量
    totalStudents() {
      return this.$store.getters.getStudents.length;
    },
    // 获取咨询师数量
    totalCounselors() {
      return this.$store.getters.getCounselors.length;
    },
    // 获取咨询记录数量
    totalRecords() {
      return this.$store.getters.getRecords.length;
    },
    // 获取最新的3条咨询记录
    latestRecords() {
      const records = this.$store.getters.getRecords;
      // 如果有真实数据，按创建时间降序排序并取前3条
      if (records && records.length > 0) {
        return [...records]
          .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
          .slice(0, 3);
      }
      // 如果没有数据，返回空数组
      return [];
    }
  },
  methods: {
    // 检查是否正在加载
    isLoading(type) {
      return this.$store.getters.isLoading(type);
    },
    // 格式化日期
    formatDate(dateString) {
      if (!dateString) return '';
      const date = new Date(dateString);
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
    },
    // 获取状态样式类
    getStatusClass(status) {
      if (status === 'IN_PROGRESS') return 'status-in-progress';
      if (status === 'COMPLETED') return 'status-completed';
      return 'status-pending';
    },
    // 获取状态文本
    getStatusText(status) {
      if (status === 'IN_PROGRESS') return '进行中';
      if (status === 'COMPLETED') return '已完成';
      if (status === 'PENDING') return '待处理';
      if (status === 'SCHEDULED') return '已安排';
      return status;
    },
    // 查看记录详情
    viewRecordDetail(recordId) {
      this.$router.push(`/records/${recordId}`);
    }
  },
  created() {
    // 页面加载时获取数据
    this.$store.dispatch('fetchUsers');
    this.$store.dispatch('fetchRecords');
  }
};
</script>

<style scoped>
/* 简约风格首页 */
.home-container {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
  background-color: #f8f9fa;
  min-height: 100vh;
}

/* 标题区域 */
.page-header {
  margin-bottom: 40px;
  text-align: center;
}

.page-header h1 {
  font-size: 32px;
  font-weight: 300;
  color: #2c3e50;
  margin: 0 0 8px;
  letter-spacing: 1px;
}

.subtitle {
  font-size: 16px;
  color: #7f8c8d;
  margin: 0;
  font-weight: 300;
}

/* 统计区域 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
  margin-bottom: 40px;
}

.stat-item {
  background: white;
  border-radius: 8px;
  padding: 24px;
  display: flex;
  align-items: center;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.stat-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.08);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background-color: #f1f2f6;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
}

.stat-icon i {
  font-size: 24px;
  color: #2c3e50;
}

.stat-content {
  flex: 1;
}

.stat-number {
  font-size: 28px;
  font-weight: 600;
  color: #2c3e50;
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  color: #7f8c8d;
  margin-top: 4px;
}

/* 内容区域 */
.content-area {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 24px;
}

.content-card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.card-title {
  font-size: 18px;
  font-weight: 500;
  color: #2c3e50;
  padding: 20px 20px 0;
  margin: 0 0 16px;
}

/* 记录列表 */
.records-list {
  padding: 0 20px;
}

.record-item {
  padding: 16px 0;
  border-bottom: 1px solid #f1f2f6;
  cursor: pointer;
  transition: all 0.2s ease;
}

.record-item:last-child {
  border-bottom: none;
}

.record-item:hover {
  background-color: #f8f9fa;
  margin: 0 -20px;
  padding-left: 20px;
  padding-right: 20px;
}

.record-info {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.record-title {
  font-size: 16px;
  font-weight: 500;
  color: #2c3e50;
  margin: 0;
  max-width: 60%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.record-meta {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
}

.record-status {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
  white-space: nowrap;
}

.status-pending {
  background-color: #f1f2f6;
  color: #7f8c8d;
}

.status-in-progress {
  background-color: #ebf5ff;
  color: #409eff;
}

.status-completed {
  background-color: #f0f9ff;
  color: #67c23a;
}

.record-date {
  font-size: 12px;
  color: #95a5a6;
}

.empty-state {
  padding: 40px 20px;
  text-align: center;
}

.empty-state p {
  color: #7f8c8d;
  margin: 0 0 16px;
}

.card-footer {
  padding: 12px 20px;
  text-align: center;
  border-top: 1px solid #f1f2f6;
}

/* 快速操作区域 */
.actions-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  padding: 0 20px 20px;
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px;
  border-radius: 8px;
  background-color: #f8f9fa;
  cursor: pointer;
  transition: all 0.2s ease;
}

.action-item:hover {
  background-color: #e1e8ed;
  transform: translateY(-3px);
}

.action-item i {
  font-size: 24px;
  color: #3498db;
  margin-bottom: 8px;
}

.action-item span {
  font-size: 14px;
  color: #2c3e50;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .content-area {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .home-container {
    padding: 16px;
  }
  
  .page-header h1 {
    font-size: 28px;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .record-title {
    max-width: 70%;
  }
  
  .actions-grid {
    grid-template-columns: 1fr;
  }
}
</style>