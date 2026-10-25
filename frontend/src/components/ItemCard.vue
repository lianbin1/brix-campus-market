<template>
  <router-link :to="`/items/${item.id}`" class="item-card">
    <div class="item-card__cover">
      <el-image :src="resolveImage(item.coverImage)" fit="cover" class="item-card__image" lazy>
        <template #error>
          <div class="item-card__placeholder">
            <el-icon><Picture /></el-icon>
          </div>
        </template>
      </el-image>
      <span v-if="showStatus" class="item-card__status">
        <StatusTag kind="item" :value="item.status" effect="dark" />
      </span>
      <span v-if="item.status === 'SOLD'" class="item-card__mask">已售出</span>
    </div>

    <div class="item-card__body">
      <h3 class="item-card__title" :title="item.title">{{ item.title }}</h3>
      <div class="item-card__price">
        <PriceTag :price="item.price" :original-price="item.originalPrice" size="small" />
      </div>
      <div class="item-card__meta">
        <ConditionTag :level="item.conditionLevel" />
        <span class="item-card__campus">
          <el-icon><Location /></el-icon>
          {{ item.campus }}
        </span>
      </div>
      <div v-if="showSeller" class="item-card__footer">
        <span class="item-card__seller">{{ item.sellerNickname || '匿名同学' }}</span>
        <span class="item-card__views">
          <el-icon><View /></el-icon>
          {{ item.viewCount ?? 0 }}
        </span>
      </div>
    </div>
  </router-link>
</template>

<script setup>
import PriceTag from './PriceTag.vue'
import ConditionTag from './ConditionTag.vue'
import StatusTag from './StatusTag.vue'
import { resolveImage } from '@/utils/format'

defineProps({
  item: { type: Object, required: true },
  showStatus: { type: Boolean, default: false },
  showSeller: { type: Boolean, default: true }
})
</script>

<style scoped>
.item-card {
  display: block;
  overflow: hidden;
  background: #fff;
  border: 0.5px solid var(--brix-border);
  border-radius: var(--brix-radius);
  transition: transform 0.18s ease, box-shadow 0.18s ease;
}

.item-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 10px 24px rgba(15, 169, 104, 0.12);
}

.item-card__cover {
  position: relative;
  height: 176px;
  background: #f2f4f6;
}

.item-card__image {
  width: 100%;
  height: 100%;
  display: block;
}

.item-card__placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 176px;
  color: #c9cfd7;
  font-size: 28px;
}

.item-card__status {
  position: absolute;
  top: 8px;
  left: 8px;
}

.item-card__mask {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.42);
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 2px;
}

.item-card__body {
  padding: 12px 12px 14px;
}

.item-card__title {
  font-size: 14px;
  font-weight: 500;
  line-height: 1.45;
  height: 40px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.item-card__price {
  margin-top: 8px;
}

.item-card__meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 8px;
  font-size: 12px;
  color: var(--brix-text-secondary);
}

.item-card__campus {
  display: inline-flex;
  align-items: center;
  gap: 2px;
}

.item-card__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 10px;
  padding-top: 10px;
  border-top: 0.5px dashed var(--brix-border);
  font-size: 12px;
  color: var(--brix-text-placeholder);
}

.item-card__seller {
  max-width: 110px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-card__views {
  display: inline-flex;
  align-items: center;
  gap: 2px;
}
</style>
