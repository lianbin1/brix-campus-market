<template>
  <span class="price-tag" :class="`price-tag--${size}`">
    <span class="price-tag__symbol">¥</span>
    <span class="price-tag__value">{{ formatPrice(price) }}</span>
    <span v-if="showOriginal && originalPrice" class="brix-price-origin">¥{{ formatPrice(originalPrice) }}</span>
  </span>
</template>

<script setup>
import { formatPrice } from '@/utils/format'

defineProps({
  price: { type: [Number, String], default: 0 },
  originalPrice: { type: [Number, String], default: null },
  showOriginal: { type: Boolean, default: true },
  size: { type: String, default: 'normal' }
})
</script>

<style scoped>
.price-tag {
  color: var(--brix-price);
  font-weight: 600;
  display: inline-flex;
  align-items: baseline;
}

.price-tag__symbol {
  font-size: 12px;
  margin-right: 1px;
}

.price-tag--small .price-tag__value {
  font-size: 16px;
}

.price-tag--normal .price-tag__value {
  font-size: 20px;
}

.price-tag--large .price-tag__value {
  font-size: 30px;
}
</style>
