<template>
  <el-tag :type="info.color || undefined" size="small" :effect="effect">{{ info.label }}</el-tag>
</template>

<script setup>
import { computed } from 'vue'
import {
  ITEM_STATUS_DICT,
  ORDER_STATUS_DICT,
  USER_STATUS_DICT,
  REPORT_STATUS_DICT,
  TRADE_TYPE_DICT
} from '@/constants'

const props = defineProps({
  // item | order | user | report | trade
  kind: { type: String, default: 'item' },
  value: { type: String, default: '' },
  effect: { type: String, default: 'light' }
})

const DICT_MAP = {
  item: ITEM_STATUS_DICT,
  order: ORDER_STATUS_DICT,
  user: USER_STATUS_DICT,
  report: REPORT_STATUS_DICT,
  trade: TRADE_TYPE_DICT
}

const info = computed(() => {
  const dict = DICT_MAP[props.kind] || ITEM_STATUS_DICT
  return dict[props.value] || { label: props.value || '-', color: '' }
})
</script>
