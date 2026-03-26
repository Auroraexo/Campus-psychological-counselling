<template>
  <div class="register-container">
    <div class="register-box">
      <div class="register-header">
        <h1 class="logo">心理<span class="logo-highlight">咨询</span></h1>
        <p class="subtitle">创建新账户，加入我们</p>
      </div>
      
      <el-form
        :model="registerForm"
        :rules="registerRules"
        ref="registerForm"
        class="register-form"
      >
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item prop="username">
              <el-input
                v-model="registerForm.username"
                placeholder="用户名"
                clearable
                class="modern-input"
              ></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item prop="realName">
              <el-input
                v-model="registerForm.realName"
                placeholder="真实姓名"
                clearable
                class="modern-input"
              ></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item prop="password">
              <el-input
                v-model="registerForm.password"
                type="password"
                placeholder="密码"
                show-password
                clearable
                class="modern-input"
              ></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item prop="confirmPassword">
              <el-input
                v-model="registerForm.confirmPassword"
                type="password"
                placeholder="确认密码"
                show-password
                clearable
                class="modern-input"
              ></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item prop="email">
              <el-input
                v-model="registerForm.email"
                placeholder="邮箱"
                clearable
                class="modern-input"
              ></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item prop="phone">
              <el-input
                v-model="registerForm.phone"
                placeholder="电话"
                clearable
                class="modern-input"
              ></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item prop="role">
          <el-select v-model="registerForm.role" placeholder="请选择角色" class="modern-select">
            <el-option label="学生" value="STUDENT"></el-option>
            <el-option label="咨询师" value="COUNSELOR"></el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item class="button-container">
          <el-button
            type="primary"
            :loading="loading"
            class="register-button"
            @click="handleRegister"
          >注册</el-button>
          <router-link to="/login" class="back-link">返回登录</router-link>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Register',
  data() {
    // 自定义验证规则：确认密码必须与密码一致
    const validateConfirmPassword = (rule, value, callback) => {
      if (value !== this.registerForm.password) {
        callback(new Error('两次输入的密码不一致'));
      } else {
        callback();
      }
    };
    
    return {
      registerForm: {
        username: '',
        password: '',
        confirmPassword: '',
        realName: '',
        email: '',
        phone: '',
        role: 'STUDENT'
      },
      registerRules: {
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' },
          { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, message: '请再次输入密码', trigger: 'blur' },
          { validator: validateConfirmPassword, trigger: 'blur' }
        ],
        realName: [
          { required: true, message: '请输入真实姓名', trigger: 'blur' },
          { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
        ],
        email: [
          { required: true, message: '请输入邮箱', trigger: 'blur' },
          { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
        ],
        phone: [
          { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
        ],
        role: [
          { required: true, message: '请选择角色', trigger: 'change' }
        ]
      },
      loading: false
    };
  },
  methods: {
    handleRegister() {
      this.$refs.registerForm.validate(valid => {
        if (valid) {
          this.loading = true;
          // 创建用户对象，不包含确认密码
          const userData = {
            username: this.registerForm.username,
            password: this.registerForm.password,
            realName: this.registerForm.realName,
            email: this.registerForm.email,
            phone: this.registerForm.phone,
            role: this.registerForm.role
          };
          
          this.$store.dispatch('register', userData)
            .then(() => {
              this.$message.success('注册成功，请登录');
              this.$router.push('/login');
            })
            .catch(error => {
              this.$message.error(`注册失败: ${error.message}`);
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
.register-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e7eb 100%);
  font-family: 'Helvetica Neue', Arial, sans-serif;
  padding: 20px 0;
}

.register-box {
  width: 600px;
  padding: 40px;
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

.register-box:hover {
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.1);
  transform: translateY(-5px);
}

.register-header {
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

.register-form {
  margin-bottom: 20px;
}

.modern-input >>> .el-input__inner {
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  padding: 15px;
  font-size: 15px;
  transition: all 0.3s ease;
  box-shadow: none;
  width: 100%;
}

.modern-input >>> .el-input__inner:focus {
  border-color: #409EFF;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

.modern-select {
  width: 100%;
}

.modern-select >>> .el-input__inner {
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  padding: 15px;
  font-size: 15px;
  transition: all 0.3s ease;
  box-shadow: none;
}

.modern-select >>> .el-input__inner:focus {
  border-color: #409EFF;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

.button-container {
  text-align: center;
  margin-top: 30px;
}

.register-button {
  width: 100%;
  height: 50px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  background: linear-gradient(90deg, #409EFF 0%, #5dade2 100%);
  border: none;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
  margin-bottom: 20px;
}

.register-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(64, 158, 255, 0.4);
}

.register-button:active {
  transform: translateY(0);
}

.back-link {
  display: inline-block;
  color: #909399;
  text-decoration: none;
  font-size: 14px;
  transition: color 0.3s ease;
}

.back-link:hover {
  color: #409EFF;
  text-decoration: underline;
}

/* 表单项间距调整 */
.register-form .el-form-item {
  margin-bottom: 20px;
}

/* 响应式设计 */
@media screen and (max-width: 768px) {
  .register-box {
    width: 90%;
    padding: 30px 20px;
  }
  
  .el-col {
    margin-bottom: 10px;
  }
}
</style>