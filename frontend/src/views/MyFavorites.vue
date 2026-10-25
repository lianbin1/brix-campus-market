<template>
  <div class="my-favorites">
    <div class="brix-section-title">
      <span>我的收藏</span>
      <span class="brix-section-title__sub">已售出 / 已下架的商品仍会展示并带状态角标</span>
    </div>

    <div v-loading="loading">
      <div v-if="items.length" class="my-favorites__grid">
        <div v-for="favorite in items" :key="favorite.id" class="fav-card brix-card">
          <router-link :to="`/items/${favorite.itemId}`" class="fav-card__cover">
            <el-image :src="resolveImage(favorite.itemCover)" fit="cover" class="fav-card__image">
              <template #error>
                <div class="fav-card__placeholder"><el-icon><Picture /></el-icon></div>
              </template>
            </el-image>
            <span class="fav-card__status">
              <StatusTag kind="item" :value="favorite.itemStatus" effect="dark" />
            </span>
          </router-link>

          <div class="fav-card__body">
            <router-link :to="`/items/${favorite.itemId}`" class="fav-card__title" :title="favorite.itemTitle">
              {{ favorite.itemTitle }}
            </router-link>
            <div class="fav-card__price">
              <PriceTag :price="favorite.itemPrice" :show-original="false" size="small" />
            </div>
            <div class="fav-card__meta">
              <span>{{ favorite.sellerNickname || '匿名同学' }}</span>
              <span>收藏于 {{ formatTime(favorite.createdAt, 'MM-DD HH:mm') }}</span>
            </div>
            <div class="fav-card__actions">
              <el-button size="small" text type="danger" @click="unfavorite(favorite)">取消收藏</el-button>
            </div>
          </div>
        </div>
      </div>
      <EmptyState v-else-if="!loading" text="还没有收藏任何商品" tip="在商品详情页点「收藏」即可加入这里" />
    </div>

    <el-pagination
      v-if="total > query.pageSize"
      background
      layout="prev, pager, next, total"
      :total="total"
      :page-size="query.pageSize"
      :current-page="query.currentPage"
      @current-change="onPageChange"
    />
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchFavorites, toggleFavorite } from '@/api/favorite'
import { DEFAULT_PAGE } from '@/constants'
import { formatTime, resolveImage } from '@/utils/format'
import PriceTag from '@/components/PriceTag.vue'
import StatusTag from '@/components/StatusTag.vue'
import EmptyState from '@/components/EmptyState.vue'

const items = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ ...DEFAULT_PAGE })

/** 加载收藏列表 */
async function load() {
  loading.value = true
  try {
    const data = await fetchFavorites({ ...query })
    items.value = data?.records || []
    total.value = Number(data?.total || 0)
  } finally {
    loading.value = false
  }
}

/** 翻页 */
function onPageChange(page) {
  query.currentPage = page
  load()
}

/** 取消收藏（切换式接口），成功后从列表移除 */
async function unfavorite(favorite) {
  const data = await toggleFavorite(favorite.itemId)
  ElMessage.success(data?.favorited ? '已重新收藏' : '已取消收藏')
  if (items.value.length === 1 && query.currentPage > 1) {
    query.currentPage -= 1
  }
  load()
}

onMounted(load)
</script>

<style scoped>
.my-favorites__grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.fav-card {
  overflow: hidden;
  transition: transform 0.18s ease, box-shadow 0.18s ease;
}

.fav-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 10px 24px rgba(15, 169, 104, 0.12);
}

.fav-card__cover {
  position: relative;
  display: block;
  height: 160px;
  background: #f2f4f6;
}

.fav-card__image {
  width: 100%;
  height: 100%;
  display: block;
}

.fav-card__placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 160px;
  color: #c9cfd7;
  font-size: 26px;
}

.fav-card__status {
  position: absolute;
  top: 8px;
  left: 8px;
}

.fav-card__body {
  padding: 12px;
}

.fav-card__title {
  display: block;
  font-size: 14px;
  font-weight: 500;
  height: 40px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.fav-card__title:hover {
  color: var(--brix-primary);
}

.fav-card__price {
  margin-top: 6px;
}

.fav-card__meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 8px;
  font-size: 12px;
  color: var(--brix-text-placeholder);
}

.fav-card__actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 4px;
}
</style>
