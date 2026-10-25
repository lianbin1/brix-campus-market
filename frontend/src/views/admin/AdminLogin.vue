<template>
  <div class="admin-login">
    <div class="admin-login__card brix-card">
      <span class="admin-login__mark">B</span>
      <h1 class="admin-login__title">Brix 后台管理</h1>
      <p class="admin-login__sub">仅审核员（REVIEWER）与运营（OPERATOR）可登录</p>

      <el-form ref="formRef" :model="form" :rules="rules" size="large" @keyup.enter="submit">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="后台账号" :prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" :prefix-icon="Lock" show-password />
        </el-form-item>
        <el-button type="primary" class="admin-login__submit" :loading="loading" @click="submit">
          登录后台
        </el-button>
      </el-form>

      <router-link to="/login" class="admin-login__back">← 返回前台登录</router-link>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Lock, User } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()

const formRef = ref(null)
const loading = ref(false)
const form = reactive({ username: '', password: '' })

const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

/** 后台登录：USER 会被后端拒绝（无后台权限），成功后按角色落地 */
async function submit() {
  try {
    await formRef.value.validate()
  } catch (error) {
    return
  }
  loading.value = true
  try {
    await auth.adminLogin({ username: form.username.trim(), password: form.password })
    ElMessage.success('登录成功')
    router.replace(auth.role === 'OPERATOR' ? '/admin/dashboard' : '/admin/items')
  } catch (error) {
    // 「无后台权限」等提示由 request.js 弹出
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.admin-login {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #11181f;
}

.admin-login__card {
  width: 380px;
  padding: 36px;
  text-align: center;
}

.admin-login__mark {
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

.admin-login__title {
  margin: 12px 0 0;
  font-size: 20px;
}

.admin-login__sub {
  margin: 6px 0 20px;
  font-size: 13px;
  color: var(--brix-text-secondary);
}

.admin-login__submit {
  width: 100%;
}

.admin-login__back {
  display: inline-block;
  margin-top: 14px;
  font-size: 13px;
  color: var(--brix-text-secondary);
}

.admin-login__back:hover {
  color: var(--brix-primary);
}
</style>
