<template>
  <aside class="admin-sidebar">
    <div class="admin-sidebar__brand">
      <span class="admin-sidebar__mark">B</span>
      <div class="admin-sidebar__titles">
        <strong>Brix 后台</strong>
        <small>{{ roleLabel }}</small>
      </div>
    </div>

    <el-menu :default-active="route.path" class="admin-sidebar__menu" router>
      <el-menu-item v-for="menu in menus" :key="menu.key" :index="menu.path">
        <el-icon><component :is="menu.icon" /></el-icon>
        <span>{{ menu.title }}</span>
      </el-menu-item>
    </el-menu>

    <div class="admin-sidebar__footer">
      <el-button text @click="router.push('/')">
        <el-icon><Back /></el-icon>
        <span>返回前台</span>
      </el-button>
    </div>
  </aside>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

// 菜单按角色渲染（REVIEWER 无用户/订单/分类/大屏；OPERATOR 无审核/举报）
const menus = computed(() => auth.menus || [])
const roleLabel = computed(() => (auth.role === 'OPERATOR' ? '运营控制台' : '内容审核台'))
</script>

<style scoped>
.admin-sidebar {
  display: flex;
  flex-direction: column;
  width: 208px;
  flex-shrink: 0;
  background: #11181f;
  color: #cfd8e3;
  min-height: 100vh;
}

.admin-sidebar__brand {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 18px 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.admin-sidebar__mark {
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

.admin-sidebar__titles {
  display: flex;
  flex-direction: column;
  line-height: 1.3;
}

.admin-sidebar__titles small {
  color: #8b97a5;
  font-size: 11px;
}

.admin-sidebar__menu {
  flex: 1;
  border-right: none;
  background: transparent;
  padding: 8px;
}

.admin-sidebar__menu :deep(.el-menu-item) {
  color: #cfd8e3;
  border-radius: 8px;
  margin-bottom: 4px;
  height: 44px;
}

.admin-sidebar__menu :deep(.el-menu-item:hover) {
  background: rgba(255, 255, 255, 0.08);
  color: #fff;
}

.admin-sidebar__menu :deep(.el-menu-item.is-active) {
  background: var(--brix-primary);
  color: #fff;
}

.admin-sidebar__footer {
  padding: 12px;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
}

.admin-sidebar__footer :deep(.el-button) {
  color: #8b97a5;
}

.admin-sidebar__footer :deep(.el-button:hover) {
  color: #fff;
}
</style>
