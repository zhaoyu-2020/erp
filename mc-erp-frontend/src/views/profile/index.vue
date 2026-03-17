<template>
  <div class="profile-container">
    <el-row :gutter="20">
      <!-- 左侧：头像 + 基本信息 -->
      <el-col :span="8">
        <el-card>
          <div class="avatar-section">
            <el-avatar :size="80" :icon="UserFilled" class="avatar" />
            <div class="user-name">{{ userInfo.username || '—' }}</div>
            <div class="user-role">{{ userInfo.roles?.join('、') || '普通用户' }}</div>
          </div>
          <el-divider />
          <ul class="info-list">
            <li>
              <el-icon><User /></el-icon>
              <span>用户名：{{ userInfo.username || '—' }}</span>
            </li>
            <li>
              <el-icon><Message /></el-icon>
              <span>邮箱：{{ userInfo.email || '—' }}</span>
            </li>
            <li>
              <el-icon><Phone /></el-icon>
              <span>手机：{{ userInfo.phone || '—' }}</span>
            </li>
          </ul>
        </el-card>
      </el-col>

      <!-- 右侧：修改密码 -->
      <el-col :span="16">
        <el-card header="修改密码">
          <el-form
            ref="pwdFormRef"
            :model="pwdForm"
            :rules="pwdRules"
            label-width="100px"
            style="max-width: 480px"
          >
            <el-form-item label="旧密码" prop="oldPassword">
              <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入旧密码" />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="请输入新密码（至少6位）" />
            </el-form-item>
            <el-form-item label="确认新密码" prop="confirmPassword">
              <el-input v-model="pwdForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="loading" @click="handleChangePwd">保存修改</el-button>
              <el-button @click="resetForm">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { User, Message, Phone, UserFilled } from '@element-plus/icons-vue'
import { changePassword } from '@/api/system'

interface UserInfo {
  username?: string
  email?: string
  phone?: string
  roles?: string[]
}

const router = useRouter()
const userInfo = reactive<UserInfo>({})
const pwdFormRef = ref()
const loading = ref(false)

const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirm = (_rule: any, value: string, callback: any) => {
  if (value !== pwdForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' }
  ]
}

onMounted(() => {
  const stored = localStorage.getItem('userInfo')
  if (stored) {
    try {
      Object.assign(userInfo, JSON.parse(stored))
    } catch {}
  }
})

const handleChangePwd = async () => {
  if (!pwdFormRef.value) return
  await pwdFormRef.value.validate(async (valid: boolean) => {
    if (!valid) return
    loading.value = true
    try {
      await changePassword({ oldPassword: pwdForm.oldPassword, newPassword: pwdForm.newPassword })
      ElMessage.success('密码修改成功，请重新登录')
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      router.push('/login')
    } catch {
      ElMessage.error('密码修改失败')
    } finally {
      loading.value = false
    }
  })
}

const resetForm = () => {
  pwdFormRef.value?.resetFields()
}
</script>

<style scoped>
.profile-container {
  padding: 0;
}
.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16px 0;
}
.avatar {
  background-color: #409eff;
}
.user-name {
  margin-top: 12px;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}
.user-role {
  margin-top: 4px;
  font-size: 13px;
  color: #909399;
}
.info-list {
  list-style: none;
  padding: 0;
  margin: 0;
}
.info-list li {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 0;
  font-size: 14px;
  color: #606266;
  border-bottom: 1px solid #f0f0f0;
}
.info-list li:last-child {
  border-bottom: none;
}
</style>
