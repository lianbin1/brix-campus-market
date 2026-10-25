<template>
  <div class="admin-layout">
    <AdminSidebar />
    <div class="admin-layout__body">
      <header class="admin-layout__header">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item>后台管理</el-breadcrumb-item>
          <el-breadcrumb-item>{{ currentTitle }}</el-breadcrumb-item>
        </el-breadcrumb>

        <div class="admin-layout__user">
          <el-avatar :size="28" :src="resolveImage(auth.user?.avatar)">
            {{ nicknameInitial(auth.user?.nickname) }}
          </el-avatar>
          <span>{{ auth.user?.nickname }}</span>
          <el-tag size="small" effect="plain">{{ roleLabel }}</el-tag>
          <el-button text type="danger" @click="logout">退出</el-button>
        </div>
      </header>

      <main class="admin-layout__main">
        <router-view v-slot="{ Component }">
          <transition name="brix-fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import AdminSidebar from './components/AdminSidebar.vue'
import { useAuthStore } from '@/stores/auth'
import { resolveImage, nicknameInitial } from '@/utils/format'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const currentTitle = computed(() => route.meta.title || '')
const roleLabel = computed(() => (auth.role === 'OPERATOR' ? '运营' : '审核员'))

/** 退出后台登录态 */
function logout() {
  auth.logout()
  ElMessage.success('已退出登录')
  router.push('/admin/login')
}
</script>

<style scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
  background: var(--brix-bg);
}

.admin-layout__body {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.admin-layout__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 56px;
  padding: 0 20px;
  background: #fff;
  border-bottom: 0.5px solid var(--brix-border);
}

.admin-layout__user {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 13px;
}

.admin-layout__main {
  flex: 1;
  padding: 20px;
}
</style>
