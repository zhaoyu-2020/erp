<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-left">
        <div class="welcome-text">
          <h1>欢迎登录</h1>
          <p>登录 MC ERP 系统以无缝管理您的资源。</p>
        </div>
      </div>
      <div class="login-right">
        <div class="login-form-wrapper">
          <div class="logo">
            <el-icon :size="40" color="#409EFF"><OfficeBuilding /></el-icon>
            <h2>MC ERP 系统</h2>
          </div>
          
          <el-form ref="loginFormRef" :model="loginForm" :rules="loginRules" class="login-form">
            <el-form-item prop="username">
              <el-input 
                v-model="loginForm.username" 
                placeholder="用户名" 
                :prefix-icon="User"
                size="large"
              />
            </el-form-item>
            
            <el-form-item prop="password">
              <el-input 
                v-model="loginForm.password" 
                type="password" 
                placeholder="密码" 
                :prefix-icon="Lock"
                show-password
                size="large"
                @keyup.enter="handleLogin"
              />
            </el-form-item>
            
            <el-button 
              :loading="loading" 
              type="primary" 
              class="login-btn" 
              size="large"
              @click="handleLogin"
            >
              登 录
            </el-button>
          </el-form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, OfficeBuilding } from '@element-plus/icons-vue'
import { login } from '@/api/auth'

const router = useRouter()
const loginFormRef = ref()
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const loginRules = {
  username: [{ required: true, trigger: 'blur', message: '请输入用户名' }],
  password: [{ required: true, trigger: 'blur', message: '请输入密码' }]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  await loginFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      loading.value = true
      try {
        const res = await login(loginForm)
        if (res.code === 200) {
          ElMessage.success('登录成功')
          localStorage.setItem('token', res.data.token)
          localStorage.setItem('userInfo', JSON.stringify(res.data))
          router.push('/')
        } else {
          ElMessage.error(res.message || '登录失败')
        }
      } catch (error) {
        // 错误已由全局拦截器处理，此处无需重复提示
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  padding: 20px;
}

.login-box {
  width: 900px;
  height: 500px;
  background: #ffffff;
  border-radius: 20px;
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.1);
  display: flex;
  overflow: hidden;
}

.login-left {
  flex: 1;
  background: linear-gradient(135deg, #409EFF 0%, #3a7bd5 100%);
  padding: 40px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  color: white;
}

.welcome-text h1 {
  font-size: 36px;
  margin-bottom: 20px;
  font-weight: 600;
}

.welcome-text p {
  font-size: 16px;
  line-height: 1.6;
  opacity: 0.9;
}

.login-right {
  flex: 1;
  padding: 40px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.login-form-wrapper {
  max-width: 350px;
  margin: 0 auto;
  width: 100%;
}

.logo {
  text-align: center;
  margin-bottom: 40px;
}

.logo h2 {
  color: #303133;
  font-size: 24px;
  margin-top: 15px;
  font-weight: 600;
}

.login-form {
  margin-top: 20px;
}

.login-btn {
  width: 100%;
  margin-top: 10px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  height: 44px;
}

:deep(.el-input__wrapper) {
  border-radius: 8px;
  box-shadow: 0 0 0 1px #dcdfe6 inset;
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #409EFF inset;
}
</style>
