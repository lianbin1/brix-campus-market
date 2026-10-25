<template>
  <div class="seller-card brix-card">
    <div class="seller-card__head">
      <el-avatar :size="46" :src="resolveImage(seller?.avatar)">
        {{ nicknameInitial(seller?.nickname) }}
      </el-avatar>
      <div class="seller-card__info">
        <div class="seller-card__name">{{ seller?.nickname || '匿名同学' }}</div>
        <div class="seller-card__campus">
          <el-icon><Location /></el-icon>
          {{ seller?.campus || '校区未填写' }}
        </div>
      </div>
      <div class="seller-card__credit">
        <span class="seller-card__credit-value">{{ seller?.creditScore ?? '-' }}</span>
        <span class="seller-card__credit-label">信用分</span>
      </div>
    </div>

    <div class="seller-card__summary">
      <el-rate :model-value="avgRating" disabled allow-half size="small" />
      <span class="seller-card__avg">{{ avgRating ? avgRating.toFixed(1) : '暂无评分' }}</span>
      <span class="seller-card__count">共 {{ total }} 条评价</span>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { fetchUserReviews } from '@/api/auth'
import { resolveImage, nicknameInitial } from '@/utils/format'

const props = defineProps({
  seller: { type: Object, default: () => ({}) }
})

const avgRating = ref(0)
const total = ref(0)

onMounted(async () => {
  if (!props.seller?.id) return
  try {
    const data = await fetchUserReviews(props.seller.id, { currentPage: 1, pageSize: 1 })
    avgRating.value = Number(data?.summary?.avgRating || 0)
    total.value = Number(data?.summary?.total || 0)
  } catch (error) {
    // 无评价时静默
  }
})
</script>

<style scoped>
.seller-card {
  padding: 16px;
}

.seller-card__head {
  display: flex;
  align-items: center;
  gap: 12px;
}

.seller-card__info {
  flex: 1;
  min-width: 0;
}

.seller-card__name {
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.seller-card__campus {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  margin-top: 2px;
  font-size: 12px;
  color: var(--brix-text-secondary);
}

.seller-card__credit {
  text-align: right;
}

.seller-card__credit-value {
  display: block;
  color: var(--brix-primary);
  font-size: 18px;
  font-weight: 600;
  line-height: 1.1;
}

.seller-card__credit-label {
  font-size: 11px;
  color: var(--brix-text-placeholder);
}

.seller-card__summary {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 0.5px solid var(--brix-border);
  font-size: 12px;
  color: var(--brix-text-secondary);
}

.seller-card__avg {
  color: var(--brix-price);
  font-weight: 600;
}
</style>
