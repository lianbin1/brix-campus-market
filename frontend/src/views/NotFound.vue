<template>
  <div class="not-found">
    <h1>404</h1>
    <p>页面不存在或已被移除</p>
    <el-button type="primary" @click="router.push('/')">回到首页</el-button>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<style scoped>
.not-found {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: var(--brix-text-secondary);
}

.not-found h1 {
  font-size: 72px;
  color: var(--brix-primary);
  margin: 0;
}
</style>
