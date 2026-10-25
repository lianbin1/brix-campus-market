<template>
  <section class="filter-bar brix-card">
    <div class="filter-bar__row">
      <el-input
        v-model="form.keyword"
        class="filter-bar__keyword"
        placeholder="搜索商品标题或描述"
        clearable
        @keyup.enter="emitChange"
        @clear="emitChange"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>

      <el-select v-model="form.categoryId" class="filter-bar__select" placeholder="全部分类" clearable @change="emitChange">
        <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
      </el-select>

      <el-select v-model="form.campus" class="filter-bar__select" placeholder="全部校区" clearable @change="emitChange">
        <el-option v-for="c in campusOptions" :key="c" :label="c" :value="c" />
      </el-select>

      <div class="filter-bar__price">
        <el-input-number v-model="form.minPrice" :min="0" :controls="false" placeholder="最低价" class="filter-bar__price-input" />
        <span class="filter-bar__dash">—</span>
        <el-input-number v-model="form.maxPrice" :min="0" :controls="false" placeholder="最高价" class="filter-bar__price-input" />
        <el-button plain @click="emitChange">确定</el-button>
      </div>

      <el-select v-model="form.sort" class="filter-bar__select filter-bar__select--sort" @change="emitChange">
        <el-option v-for="s in sortOptions" :key="s.value" :label="s.label" :value="s.value" />
      </el-select>

      <el-button text type="primary" @click="reset">重置</el-button>
    </div>
  </section>
</template>

<script setup>
import { reactive, watch } from 'vue'
import { CAMPUS_OPTIONS, SORT_OPTIONS } from '@/constants'

const props = defineProps({
  modelValue: { type: Object, default: () => ({}) },
  categories: { type: Array, default: () => [] }
})

const emit = defineEmits(['update:modelValue', 'change'])

const campusOptions = CAMPUS_OPTIONS
const sortOptions = SORT_OPTIONS

// 本地副本：子组件绝不直接改 props
const form = reactive({
  keyword: '',
  categoryId: '',
  campus: '',
  minPrice: null,
  maxPrice: null,
  sort: 'latest'
})

// 父级（URL query）变化时同步到本地表单
watch(
  () => props.modelValue,
  (value) => {
    if (!value) return
    form.keyword = value.keyword || ''
    form.categoryId = value.categoryId || ''
    form.campus = value.campus || ''
    form.minPrice = value.minPrice === '' || value.minPrice === undefined || value.minPrice === null
      ? null
      : Number(value.minPrice)
    form.maxPrice = value.maxPrice === '' || value.maxPrice === undefined || value.maxPrice === null
      ? null
      : Number(value.maxPrice)
    form.sort = value.sort || 'latest'
  },
  { immediate: true, deep: true }
)

/** 条件变化：向上抛出（父级负责回到第 1 页并写 URL） */
function emitChange() {
  const payload = {
    keyword: form.keyword || '',
    categoryId: form.categoryId || '',
    campus: form.campus || '',
    minPrice: form.minPrice ?? '',
    maxPrice: form.maxPrice ?? '',
    sort: form.sort || 'latest'
  }
  emit('update:modelValue', payload)
  emit('change', payload)
}

/** 重置筛选 */
function reset() {
  form.keyword = ''
  form.categoryId = ''
  form.campus = ''
  form.minPrice = null
  form.maxPrice = null
  form.sort = 'latest'
  emitChange()
}
</script>

<style scoped>
.filter-bar {
  padding: 16px;
  margin-bottom: 20px;
}

.filter-bar__row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.filter-bar__keyword {
  width: 260px;
}

.filter-bar__select {
  width: 140px;
}

.filter-bar__select--sort {
  width: 160px;
  margin-left: auto;
}

.filter-bar__price {
  display: flex;
  align-items: center;
  gap: 6px;
}

.filter-bar__price-input {
  width: 96px;
}

.filter-bar__dash {
  color: var(--brix-text-placeholder);
}
</style>
