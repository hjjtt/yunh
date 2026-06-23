<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <el-icon :size="40" color="#409EFF"><Reading /></el-icon>
        <h2>云课堂管理后台</h2>
        <p>在线教育平台管理系统</p>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="0" size="large">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" :prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            :prefix-icon="Lock"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-form-item prop="captchaCode">
          <div class="captcha-row">
            <el-input
              v-model="form.captchaCode"
              placeholder="请输入验证码"
              :prefix-icon="Key"
              @keyup.enter="handleLogin"
            />
            <div class="captcha-display" @click="refreshCaptcha">
              <span v-if="captchaLoading" class="captcha-loading">加载中...</span>
              <img v-else-if="captchaImage" :src="'data:image/png;base64,' + captchaImage" class="captcha-img" alt="验证码" />
              <span v-else class="captcha-placeholder">点击获取</span>
            </div>
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" class="login-btn" @click="handleLogin">
            登 录
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Key, Reading } from '@element-plus/icons-vue'
import { login, getCaptcha } from '../api/user.js'
import { useUserStore } from '../store/index.js'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)

const captchaKey = ref('')
const captchaImage = ref('')
const captchaLoading = ref(false)

const form = reactive({
  username: '',
  password: '',
  captchaCode: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  captchaCode: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
}

const refreshCaptcha = async () => {
  captchaLoading.value = true
  try {
    const res = await getCaptcha()
    captchaKey.value = res.data.captchaKey
    captchaImage.value = res.data.captchaImage
  } catch (e) {
    ElMessage.error('获取验证码失败')
  } finally {
    captchaLoading.value = false
  }
}

const handleLogin = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  if (!captchaKey.value) {
    ElMessage.warning('请先获取验证码')
    return
  }

  loading.value = true
  try {
    const res = await login({
      username: form.username,
      password: form.password,
      captchaKey: captchaKey.value,
      captchaCode: form.captchaCode
    })
    const data = res.data
    userStore.setToken(data.accessToken)
    userStore.setRefreshToken(data.refreshToken)
    userStore.setUser({
      userId: data.userId,
      username: data.username,
      nickname: data.nickname,
      role: data.role
    })
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } catch (e) {
    refreshCaptcha()
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  refreshCaptcha()
})
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 400px;
  padding: 40px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.login-header h2 {
  margin: 12px 0 8px;
  color: #303133;
  font-size: 24px;
}

.login-header p {
  color: #909399;
  font-size: 14px;
}

.captcha-row {
  display: flex;
  gap: 12px;
  width: 100%;
}

.captcha-row .el-input {
  flex: 1;
}

.captcha-display {
  width: 120px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  cursor: pointer;
  user-select: none;
  flex-shrink: 0;
}

.captcha-display:hover {
  border-color: #409EFF;
}

.captcha-loading {
  font-size: 12px;
  color: #909399;
}

.captcha-img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.captcha-placeholder {
  font-size: 12px;
  letter-spacing: 0;
  font-weight: 400;
  font-style: normal;
  color: #909399;
}

.login-btn {
  width: 100%;
}
</style>
