<template>
  <div class="auth-page">
    <div class="auth-card brix-card">
      <div class="auth-card__head">
        <span class="auth-card__mark">B</span>
        <h1 class="auth-card__title">登录 Brix Campus Market</h1>
        <p class="auth-card__sub">校园闲置，当面验货更放心</p>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" size="large" @keyup.enter="submit">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" :prefix-icon="User" autocomplete="username" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="密码"
            :prefix-icon="Lock"
            show-password
            autocomplete="current-password"
          />
        </el-form-item>
        <el-button type="primary" class="auth-card__submit" :loading="loading" @click="submit">登 录</el-button>
      </el-form>

      <div class="auth-card__footer">
        <span>还没有账号？</span>
        <router-link to="/register" class="auth-card__link">去注册</router-link>
        <router-link to="/admin/login" class="auth-card__link auth-card__link--right">后台入口</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Lock, User } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import ws from '@/utils/ws'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const formRef = ref(null)
const loading = ref(false)
const form = reactive({ username: '', password: '' })

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

/** 登录成功后按角色跳转（带 redirect 时优先回原页） */
async function submit() {
  try {
    await formRef.value.validate()
  } catch (error) {
    return
  }
  loading.value = true
  try {
    await auth.login({ username: form.username.trim(), password: form.password })
    ElMessage.success('登录成功')
    ws.connect()
    const redirect = typeof route.query.redirect === 'string' ? decodeURIComponent(route.query.redirect) : ''
    if (redirect) {
      router.replace(redirect)
    } else {
      router.replace(auth.isAdmin ? (auth.role === 'OPERATOR' ? '/admin/dashboard' : '/admin/items') : '/')
    }
  } catch (error) {
    // 失败提示由 request.js 弹出（skipErrorTip 未开启时）
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(160deg, #0fa968 0%, #0b7f50 55%, #0a5c3c 100%);
}

.auth-card {
  width: 400px;
  padding: 36px 36px 28px;
}

.auth-card__head {
  text-align: center;
  margin-bottom: 24px;
}

.auth-card__mark {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  border-radius: 12px;
  background: var(--brix-primary);
  color: #fff;
  font-size: 22px;
  font-weight: 700;
}

.auth-card__title {
  margin-top: 12px;
  font-size: 20px;
}

.auth-card__sub {
  margin-top: 4px;
  font-size: 13px;
  color: var(--brix-text-secondary);
}

.auth-card__submit {
  width: 100%;
  margin-top: 4px;
}

.auth-card__footer {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 16px;
  font-size: 13px;
  color: var(--brix-text-secondary);
}

.auth-card__link {
  color: var(--brix-primary);
}

.auth-card__link--right {
  margin-left: auto;
}
</style>
