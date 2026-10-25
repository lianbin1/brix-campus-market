<template>
  <div class="empty-state">
    <el-icon class="empty-state__icon"><Box /></el-icon>
    <p class="empty-state__text">{{ text }}</p>
    <p v-if="tip" class="empty-state__tip">{{ tip }}</p>
    <div v-if="showReset" class="empty-state__actions">
      <el-button type="primary" plain @click="emit('reset')">重置筛选</el-button>
    </div>
    <slot />
  </div>
</template>

<script setup>
defineProps({
  text: { type: String, default: '暂无数据' },
  tip: { type: String, default: '' },
  showReset: { type: Boolean, default: false }
})

const emit = defineEmits(['reset'])
</script>

<style scoped>
.empty-state {
  padding: 64px 0;
  text-align: center;
  color: var(--brix-text-secondary);
}

.empty-state__icon {
  font-size: 46px;
  color: #d9dde3;
  margin-bottom: 12px;
}

.empty-state__text {
  font-size: 15px;
  color: var(--brix-text);
}

.empty-state__tip {
  margin-top: 6px;
  font-size: 13px;
  color: var(--brix-text-placeholder);
}

.empty-state__actions {
  margin-top: 16px;
}
</style>
