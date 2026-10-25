<template>
  <el-tag :type="info.color || undefined" size="small" effect="plain">{{ info.label }}</el-tag>
</template>

<script setup>
import { computed } from 'vue'
import { CONDITION_DICT } from '@/constants'

const props = defineProps({
  level: { type: String, default: '' }
})

const info = computed(() => CONDITION_DICT[props.level] || { label: props.level || '未知', color: '' })
</script>
