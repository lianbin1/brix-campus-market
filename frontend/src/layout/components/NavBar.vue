<template>
  <header class="navbar">
    <div class="brix-container navbar__inner">
      <router-link to="/" class="navbar__logo">
        <span class="navbar__logo-mark">B</span>
        <span class="navbar__logo-text">Brix Campus Market</span>
      </router-link>

      <el-input
        v-model="keyword"
        class="navbar__search"
        placeholder="搜索教材、数码、生活用品…"
        clearable
        @keyup.enter="doSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>

      <nav class="navbar__links">
        <router-link to="/" class="navbar__link">首页</router-link>
        <router-link v-if="auth.role === 'USER'" to="/publish" class="navbar__link">发布闲置</router-link>
        <router-link v-if="auth.isLogin" to="/messages" class="navbar__link navbar__link--badge">
          私信
          <el-badge v-if="auth.unreadCount > 0" :value="auth.unreadCount" :max="99" class="navbar__badge" />
        </router-link>
        <router-link v-if="auth.isLogin" to="/my/orders" class="navbar__link">我的订单</router-link>
      </nav>

      <div class="navbar__user">
        <template v-if="auth.isLogin">
          <el-dropdown trigger="click" @command="onCommand">
            <span class="navbar__user-trigger">
              <el-avatar :size="30" :src="resolveImage(auth.user?.avatar)">
                {{ nicknameInitial(auth.user?.nickname) }}
              </el-avatar>
              <span class="navbar__user-name">{{ auth.user?.nickname }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="myItems">我的商品</el-dropdown-item>
                <el-dropdown-item command="favorites">我的收藏</el-dropdown-item>
                <el-dropdown-item v-if="auth.isAdmin" command="admin" divided>后台管理</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <el-button text @click="router.push('/login')">登录</el-button>
          <el-button type="primary" @click="router.push('/register')">注册</el-button>
        </template>
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { fetchUnreadCount } from '@/api/chat'
import { resolveImage, nicknameInitial } from '@/utils/format'
import ws from '@/utils/ws'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()
const keyword = ref('')

// 首页 URL 带 keyword 时同步到搜索框
watch(
  () => route.query.keyword,
  (value) => {
    keyword.value = typeof value === 'string' ? value : ''
  },
  { immediate: true }
)

onMounted(async () => {
  if (!auth.isLogin) return
  // 私信未读数与 WebSocket 连接（仅登录态）
  try {
    const data = await fetchUnreadCount()
    auth.setUnread(data?.count ?? 0)
  } catch (error) {
    // 未读数失败不阻断页面
  }
  ws.connect()
})

/** 搜索：把关键词写进 URL query */
function doSearch() {
  router.push({ path: '/', query: keyword.value ? { keyword: keyword.value } : {} })
}

/** 下拉菜单命令 */
function onCommand(command) {
  if (command === 'profile') router.push('/my/profile')
  if (command === 'myItems') router.push('/my/items')
  if (command === 'favorites') router.push('/my/favorites')
  if (command === 'admin') router.push(auth.role === 'OPERATOR' ? '/admin/dashboard' : '/admin/items')
  if (command === 'logout') {
    ws.disconnect()
    auth.logout()
    ElMessage.success('已退出登录')
    router.push('/login')
  }
}
</script>

<style scoped>
.navbar {
  position: sticky;
  top: 0;
  z-index: 100;
  background: #fff;
  border-bottom: 0.5px solid var(--brix-border);
}

.navbar__inner {
  display: flex;
  align-items: center;
  height: 64px;
  gap: 24px;
}

.navbar__logo {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.navbar__logo-mark {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  border-radius: 8px;
  background: var(--brix-primary);
  color: #fff;
  font-weight: 700;
}

.navbar__logo-text {
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 0.2px;
}

.navbar__search {
  width: 320px;
}

.navbar__links {
  display: flex;
  align-items: center;
  gap: 22px;
  flex: 1;
}

.navbar__link {
  color: var(--brix-text-secondary);
  font-size: 14px;
  transition: color 0.15s;
}

.navbar__link:hover,
.navbar__link.router-link-exact-active {
  color: var(--brix-primary);
}

.navbar__link--badge {
  display: inline-flex;
  align-items: center;
}

.navbar__badge {
  margin-left: 4px;
  transform: translateY(-6px);
}

.navbar__user-trigger {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  outline: none;
}

.navbar__user-name {
  max-width: 96px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
