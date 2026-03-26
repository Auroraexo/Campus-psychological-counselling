<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <h1 class="logo">心理<span class="logo-highlight">咨询</span></h1>
        <p class="subtitle">欢迎回来，请登录您的账户</p>
      </div>
      
      <el-form
        :model="loginForm"
        :rules="loginRules"
        ref="loginForm"
        class="login-form"
        @keyup.enter.native="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            prefix-icon="el-icon-user"
            placeholder="用户名"
            clearable
            class="modern-input"
          ></el-input>
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            prefix-icon="el-icon-lock"
            type="password"
            placeholder="密码"
            show-password
            clearable
            class="modern-input"
          ></el-input>
        </el-form-item>
        
        <el-form-item class="button-container">
          <el-button
            type="primary"
            :loading="loading"
            class="login-button"
            @click="handleLogin"
          >登录</el-button>
        </el-form-item>
      </el-form>
      
      <div class="login-footer">
        <span>还没有账户？</span>
        <router-link to="/register" class="register-link">立即注册</router-link>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Login',
  data() {
    return {
      loginForm: {
        username: '',
        password: ''
      },
      loginRules: {
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' },
          { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
        ]
      },
      loading: false
    };
  },
  methods: {
    handleLogin() {
      this.$refs.loginForm.validate(valid => {
        if (valid) {
          this.loading = true;
          this.$store.dispatch('login', this.loginForm)
            .then(() => {
              this.$message.success('登录成功');
              // 所有用户登录后都跳转到首页
              this.$router.push('/');
            })
            .catch(error => {
                // 处理不同类型的错误响应
                const errorMessage = error.response?.data?.message || 
                                    error.message || 
                                    error.error || 
                                    '用户名或密码错误';
                // 直接显示错误消息，不再添加前缀
                this.$message.error(errorMessage);
              })
            .finally(() => {
              this.loading = false;
            });
        }
      });
    }
  }
};
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e7eb 100%);
  font-family: 'Helvetica Neue', Arial, sans-serif;
}

.login-box {
  width: 420px;
  padding: 40px;
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

.login-box:hover {
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.1);
  transform: translateY(-5px);
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.logo {
  font-size: 28px;
  font-weight: 600;
  margin: 0 0 15px 0;
  color: #303133;
  letter-spacing: 1px;
}

.logo-highlight {
  color: #409EFF;
}

.subtitle {
  margin: 0;
  color: #909399;
  font-size: 16px;
  font-weight: 300;
}

.login-form {
  margin-bottom: 30px;
}

.modern-input >>> .el-input__inner {
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  padding: 15px;
  font-size: 15px;
  transition: all 0.3s ease;
  box-shadow: none;
}

.modern-input >>> .el-input__inner:focus {
  border-color: #409EFF;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

.modern-input >>> .el-input__prefix {
  left: 15px;
}

.modern-input >>> .el-input__inner {
  padding-left: 40px;
}

.button-container {
  margin-top: 25px;
}

.login-button {
  width: 100%;
  height: 50px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  background: linear-gradient(90deg, #409EFF 0%, #5dade2 100%);
  border: none;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.login-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(64, 158, 255, 0.4);
}

.login-button:active {
  transform: translateY(0);
}

.login-footer {
  text-align: center;
  font-size: 14px;
  color: #909399;
}

.register-link {
  color: #409EFF;
  text-decoration: none;
  font-weight: 500;
  margin-left: 5px;
  transition: color 0.3s ease;
}

.register-link:hover {
  color: #66b1ff;
  text-decoration: underline;
}

/* 表单项间距调整 */
.login-form .el-form-item {
  margin-bottom: 20px;
}

/* 输入框图标样式 */
.login-form .el-input__prefix .el-input__icon {
  color: #909399;
  font-size: 16px;
}
</style>