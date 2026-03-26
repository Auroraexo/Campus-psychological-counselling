<template>
  <div id="app">
    <el-container>
      <!-- 登录和注册页面不显示布局 -->
      <template v-if="!isLoginPage && !isRegisterPage">
        <!-- 左侧菜单栏 - 固定定位，确保始终在左侧显示 -->
        <el-aside width="220px" class="aside-container fixed-aside">
          <div class="logo-container">
            <h2 class="logo-text">心理<span class="logo-highlight">咨询</span></h2>
          </div>
          <el-menu
            :default-active="activeIndex"
            class="el-menu-vertical-demo"
            mode="vertical"
            @select="handleSelect"
            router
            background-color="#304156"
            text-color="#fff"
            active-text-color="#fff"
          >
            <el-menu-item index="/home">
              <i class="el-icon-s-home"></i>
              <span slot="title">首页</span>
            </el-menu-item>
            <el-menu-item v-if="isAdmin" index="/users">
              <i class="el-icon-user"></i>
              <span slot="title">用户管理</span>
            </el-menu-item>
            <el-menu-item v-if="isAdmin" index="/counselors">
              <i class="el-icon-user-solid"></i>
              <span slot="title">咨询师管理</span>
            </el-menu-item>
            <el-menu-item v-if="isAdmin || isCounselor || (isStudent && currentUser && currentUser.id)" index="/students">
              <i class="el-icon-school"></i>
              <span slot="title">{{ isStudent ? '个人信息' : '学生管理' }}</span>
            </el-menu-item>
            <el-menu-item v-if="isAdmin || isCounselor" index="/appointments">
              <i class="el-icon-date"></i>
              <span slot="title">预约管理</span>
            </el-menu-item>
            <el-menu-item v-if="isAdmin || isCounselor || (isStudent && currentUser && currentUser.id)" index="/sessions">
              <i class="el-icon-chat-dot-round"></i>
              <span slot="title">{{ isStudent ? '我的咨询会话' : '咨询会话管理' }}</span>
            </el-menu-item>
            <el-menu-item v-if="isAdmin || isCounselor || (isStudent && currentUser && currentUser.id)" index="/records">
              <i class="el-icon-document"></i>
              <span slot="title">{{ isStudent ? '我的咨询记录' : '咨询记录管理' }}</span>
            </el-menu-item>
            <el-menu-item v-if="isAdmin || isCounselor" index="/feedback">
              <i class="el-icon-star-on"></i>
              <span slot="title">咨询反馈管理</span>
            </el-menu-item>
          </el-menu>
        </el-aside>
        
        <!-- 主内容区域 -->
        <el-container style="margin-left: 220px;">
          <!-- 顶部导航栏 -->
          <el-header>
            <div class="header-content">
              <h1 class="system-title">校园心理咨询系统</h1>
              <div class="user-center">
                <el-dropdown @command="handleCommand" trigger="click">
                  <span class="el-dropdown-link">
                    <i class="el-icon-user-circle"></i>
                    <span class="user-name">{{ currentUser ? currentUser.realName : '用户' }}</span>
                    <i class="el-icon-arrow-down el-icon--right"></i>
                  </span>
                  <el-dropdown-menu slot="dropdown" class="user-dropdown-menu">
                    <el-dropdown-item command="profile">
                      <i class="el-icon-user"></i>
                      <span>个人中心</span>
                    </el-dropdown-item>
                    <el-dropdown-item command="logout" divided>
                      <i class="el-icon-switch-button"></i>
                      <span>退出登录</span>
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </el-dropdown>
              </div>
            </div>
          </el-header>
          
          <!-- 内容区域 -->
          <el-main>
            <router-view/>
          </el-main>
        </el-container>
      </template>
      
      <!-- 登录和注册页面使用全屏布局 -->
      <template v-else>
        <el-main class="full-main">
          <router-view/>
        </el-main>
      </template>
    </el-container>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'

export default {
  name: 'App',
  data() {
    return {
      activeIndex: '/home'
    };
  },
  computed: {
    ...mapGetters(['isAuthenticated', 'currentUser', 'isAdmin']),
    isLoginPage() {
      return this.$route.name === 'Login';
    },
    isRegisterPage() {
      return this.$route.name === 'Register';
    }
  },
  created() {
    // 初始化认证状态
    this.$store.dispatch('initAuth');
    // 设置当前激活的菜单项
    this.activeIndex = this.$route.path;
  },
  watch: {
    '$route'(to) {
      this.activeIndex = to.path;
    }
  },
  methods: {
    handleSelect(key) {
      this.activeIndex = key;
    },
    handleCommand(command) {
      switch (command) {
        case 'profile':
          this.$router.push(`/users/${this.currentUser.id}`);
          break;
        case 'logout':
          this.$confirm('确认退出登录吗?', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }).then(() => {
            this.$store.dispatch('logout');
            this.$router.push('/login');
            this.$message({
              type: 'success',
              message: '已退出登录'
            });
          }).catch(() => {});
          break;
      }
    }
  }
}
</script>

<style>
* {
  box-sizing: border-box;
}

#app {
  font-family: 'Avenir', Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #2c3e50;
  height: 100vh;
  overflow: hidden;
  background-color: #f0f2f5;
}

.el-container {
  height: 100%;
  overflow: hidden;
}

/* 左侧菜单栏样式 */
.aside-container {
  background-color: #304156;
  height: 100vh;
  overflow-y: auto;
  box-shadow: 2px 0 6px rgba(0, 21, 41, 0.35);
}

.fixed-aside {
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  z-index: 20;
}

.logo-container {
  padding: 20px 0;
  text-align: center;
  background-color: #263445;
  border-bottom: 1px solid #3a4b5c;
}

.logo-text {
  color: #ffffff;
  font-size: 18px;
  font-weight: bold;
  margin: 0;
}

.logo-highlight {
  color: #409EFF;
}

.el-aside {
  overflow: hidden;
  transition: width 0.3s;
}

.el-menu {
  border-right: none;
  height: calc(100vh - 68px);
  background-color: #304156;
}

/* 左侧菜单项样式增强 */
.el-menu-vertical-demo {
  background-color: #304156;
}

.el-menu-vertical-demo .el-menu-item {
  height: 50px;
  line-height: 50px;
  padding: 0 25px;
  font-size: 14px;
  color: #a6b7c9;
  transition: all 0.3s ease;
}

.el-menu-vertical-demo .el-menu-item:hover {
  background-color: #409EFF;
  color: #fff;
}

.el-menu-vertical-demo .el-menu-item.is-active {
  background-color: #409EFF;
  color: #fff;
  border-right: 3px solid #fff;
}

.el-menu-vertical-demo i {
  font-size: 16px;
  margin-right: 12px;
}

/* 顶部导航栏样式 */
.el-header {
  background-color: #fff;
  color: #333;
  line-height: 60px;
  padding: 0 30px;
  height: 60px !important;
  min-height: 60px !important;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  z-index: 10;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 100%;
}

.system-title {
  margin: 0;
  font-size: 18px;
  font-weight: bold;
  color: #304156;
  white-space: nowrap;
}

/* 用户中心样式 */
.user-center {
  display: flex;
  align-items: center;
}

.el-dropdown-link {
  color: #333;
  cursor: pointer;
  display: flex;
  align-items: center;
  padding: 8px 15px;
  border-radius: 4px;
  transition: all 0.3s ease;
  background-color: transparent;
}

.el-dropdown-link:hover {
  color: #409EFF;
  background-color: #f5f7fa;
}

.el-dropdown-link i.el-icon-user-circle {
  font-size: 20px;
  margin-right: 8px;
  color: #409EFF;
}

.user-name {
  margin-right: 5px;
  font-weight: 500;
}

.user-dropdown-menu {
  width: 160px;
}

.user-dropdown-menu .el-dropdown-item {
  display: flex;
  align-items: center;
  height: 40px;
  padding: 0 20px;
}

.user-dropdown-menu .el-dropdown-item i {
  margin-right: 10px;
  font-size: 16px;
}

/* 主内容区域样式 */
.el-main {
  padding: 25px;
  background-color: #f0f2f5;
  overflow-y: auto;
  height: calc(100vh - 60px);
}

/* 全屏主内容（登录/注册页面） */
.full-main {
  padding: 0;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f0f2f5;
  height: 100vh;
}

/* 嵌套容器样式 */
.el-container.is-vertical {
  height: calc(100vh - 60px);
}

/* 滚动条样式 */
.aside-container::-webkit-scrollbar,
.el-main::-webkit-scrollbar {
  width: 6px;
}

.aside-container::-webkit-scrollbar-track,
.el-main::-webkit-scrollbar-track {
  background: #f1f1f1;
}

.aside-container::-webkit-scrollbar-thumb,
.el-main::-webkit-scrollbar-thumb {
  background: #888;
  border-radius: 3px;
}

.aside-container::-webkit-scrollbar-thumb:hover,
.el-main::-webkit-scrollbar-thumb:hover {
  background: #555;
}

/* 响应式设计 */
@media screen and (max-width: 768px) {
  .fixed-aside {
    width: 180px !important;
  }
  
  .el-container {
    margin-left: 180px !important;
  }
}
</style>
