<template>
  <div v-loading="loading" class="item-grid">
    <div v-if="items.length" class="item-grid__list">
      <ItemCard
        v-for="item in items"
        :key="item.id"
        :item="item"
        :show-status="showStatus"
        :show-seller="showSeller"
      />
    </div>
    <EmptyState v-else-if="!loading" :text="emptyText" :show-reset="showReset" @reset="emit('reset')" />
  </div>
</template>

<script setup>
import ItemCard from './ItemCard.vue'
import EmptyState from './EmptyState.vue'

defineProps({
  items: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
  showStatus: { type: Boolean, default: false },
  showSeller: { type: Boolean, default: true },
  showReset: { type: Boolean, default: false },
  emptyText: { type: String, default: '还没有符合条件的商品' }
})

const emit = defineEmits(['reset'])
</script>

<style scoped>
.item-grid__list {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}
</style>
