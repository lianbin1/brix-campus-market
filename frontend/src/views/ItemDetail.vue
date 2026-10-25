<template>
  <div v-loading="loading" class="item-detail">
    <template v-if="item">
      <div class="item-detail__main">
        <!-- 左：图片轮播 -->
        <div class="item-detail__gallery brix-card">
          <el-carousel
            v-if="images.length"
            :autoplay="false"
            height="420px"
            indicator-position="outside"
            arrow="always"
          >
            <el-carousel-item v-for="image in images" :key="image">
              <el-image :src="resolveImage(image)" fit="cover" class="item-detail__image" :preview-src-list="previewList" />
            </el-carousel-item>
          </el-carousel>
          <div v-else class="item-detail__no-image">
            <el-icon><Picture /></el-icon>
            <p>卖家没有上传图片</p>
          </div>
        </div>

        <!-- 右：信息区 -->
        <div class="item-detail__info">
          <h1 class="item-detail__title">{{ item.title }}</h1>

          <div class="item-detail__price brix-card">
            <PriceTag :price="item.price" :original-price="item.originalPrice" size="large" />
            <span class="item-detail__views">
              <el-icon><View /></el-icon>
              {{ item.viewCount }} 次浏览 · {{ item.favoriteCount }} 人收藏
            </span>
          </div>

          <dl class="item-detail__specs">
            <div class="item-detail__spec">
              <dt>成色</dt>
              <dd><ConditionTag :level="item.conditionLevel" /></dd>
            </div>
            <div class="item-detail__spec">
              <dt>交易方式</dt>
              <dd><StatusTag kind="trade" :value="item.tradeType" effect="plain" /></dd>
            </div>
            <div class="item-detail__spec">
              <dt>校区</dt>
              <dd>{{ item.campus }}</dd>
            </div>
            <div class="item-detail__spec">
              <dt>分类</dt>
              <dd>{{ item.categoryName || '-' }}</dd>
            </div>
            <div class="item-detail__spec">
              <dt>联系方式</dt>
              <dd>{{ item.contact || '通过站内私信联系' }}</dd>
            </div>
          </dl>

          <p class="item-detail__desc">{{ item.description }}</p>

          <div class="item-detail__actions">
            <el-button
              type="primary"
              size="large"
              :disabled="sold"
              @click="wantIt"
            >
              {{ sold ? '已售出' : '我想要' }}
            </el-button>
            <el-button size="large" :disabled="sold" @click="goChat">私信卖家</el-button>
            <el-button size="large" :type="item.isFavorite ? 'warning' : 'default'" plain @click="toggleFavorite">
              {{ item.isFavorite ? '已收藏' : '收藏' }}
            </el-button>
            <el-button size="large" text type="danger" @click="reportVisible = true">举报</el-button>
          </div>
          <p v-if="sold" class="item-detail__sold-tip">这件宝贝已经找到新主人啦，看看同类好物吧～</p>
        </div>
      </div>

      <div class="item-detail__aside-row">
        <div class="item-detail__comments">
          <CommentList :item-id="itemId" />
        </div>
        <div class="item-detail__seller">
          <SellerCard :seller="item.seller" />
        </div>
      </div>
    </template>

    <EmptyState v-else-if="!loading" text="商品不存在或已下架" show-reset @reset="router.push('/')" />

    <ReportDialog v-model="reportVisible" :item-id="itemId" />
    <el-dialog v-model="tradeDialog" title="选择交易方式" width="380px">
      <el-radio-group v-model="tradeType" class="item-detail__trade">
        <el-radio value="MEET">面交（推荐，可当面验货）</el-radio>
        <el-radio v-if="item?.tradeType === 'BOTH'" value="SHIP">邮寄</el-radio>
      </el-radio-group>
      <template #footer>
        <el-button @click="tradeDialog = false">取消</el-button>
        <el-button type="primary" :loading="ordering" @click="createOrderNow">确定下单</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { fetchItemDetail } from '@/api/item'
import { toggleFavorite as toggleFavoriteApi } from '@/api/favorite'
import { createOrder } from '@/api/order'
import { useAuthStore } from '@/stores/auth'
import { resolveImage } from '@/utils/format'
import PriceTag from '@/components/PriceTag.vue'
import ConditionTag from '@/components/ConditionTag.vue'
import StatusTag from '@/components/StatusTag.vue'
import SellerCard from '@/components/SellerCard.vue'
import CommentList from '@/components/CommentList.vue'
import EmptyState from '@/components/EmptyState.vue'
import ReportDialog from '@/components/ReportDialog.vue'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const itemId = String(route.params.id || '')
const item = ref(null)
const loading = ref(false)
const reportVisible = ref(false)
const tradeDialog = ref(false)
const tradeType = ref('MEET')
const ordering = ref(false)

const sold = computed(() => item.value?.status === 'SOLD')
const images = computed(() => (item.value?.images || []).map((image) => image.url))
const previewList = computed(() => images.value.map((url) => resolveImage(url)))

/** 加载详情（后端顺带浏览量 +1） */
async function load() {
  loading.value = true
  try {
    item.value = await fetchItemDetail(itemId)
    if (item.value?.tradeType === 'SHIP') tradeType.value = 'SHIP'
  } finally {
    loading.value = false
  }
}

/** 我想要：未登录先登录（带回跳），买家选择交易方式后下单 */
function wantIt() {
  if (!auth.isLogin) {
    router.push({ path: '/login', query: { redirect: encodeURIComponent(route.fullPath) } })
    return
  }
  if (item.value?.isOwner) {
    ElMessage.info('这是你自己发布的商品')
    return
  }
  tradeType.value = item.value?.tradeType === 'SHIP' ? 'SHIP' : 'MEET'
  tradeDialog.value = true
}

/** 下单并跳收银台 */
async function createOrderNow() {
  ordering.value = true
  try {
    const data = await createOrder({
      itemId,
      tradeType: tradeType.value,
      meetPlace: tradeType.value === 'MEET' ? item.value?.campus : null
    })
    ElMessage.success('下单成功，请尽快支付')
    tradeDialog.value = false
    router.push(`/checkout/${data.id}`)
  } finally {
    ordering.value = false
  }
}

/** 跳私信并带上商品来源 */
function goChat() {
  if (!auth.isLogin) {
    router.push({ path: '/login', query: { redirect: encodeURIComponent(route.fullPath) } })
    return
  }
  router.push({ path: '/messages', query: { peerId: String(item.value?.seller?.id || ''), itemId } })
}

/** 收藏切换（未登录先登录） */
async function toggleFavorite() {
  if (!auth.isLogin) {
    router.push({ path: '/login', query: { redirect: encodeURIComponent(route.fullPath) } })
    return
  }
  try {
    const data = await toggleFavoriteApi(itemId)
    item.value.isFavorite = !!data?.favorited
    item.value.favoriteCount = data?.favoriteCount ?? item.value.favoriteCount
    ElMessage.success(item.value.isFavorite ? '已加入收藏' : '已取消收藏')
  } catch (error) {
    // request.js 已提示
  }
}

onMounted(load)
</script>

<style scoped>
.item-detail__main {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

.item-detail__gallery {
  flex: 1;
  min-width: 0;
  overflow: hidden;
}

.item-detail__image {
  width: 100%;
  height: 420px;
  display: block;
}

.item-detail__no-image {
  height: 420px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: var(--brix-text-placeholder);
  background: #f7f8fa;
}

.item-detail__no-image .el-icon {
  font-size: 44px;
}

.item-detail__info {
  width: 420px;
  flex-shrink: 0;
}

.item-detail__title {
  font-size: 22px;
  line-height: 1.4;
  margin: 0 0 14px;
}

.item-detail__price {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  background: #fff8f6;
  border-color: rgba(255, 77, 46, 0.12);
}

.item-detail__views {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--brix-text-secondary);
}

.item-detail__specs {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px 20px;
  margin: 16px 0;
  padding: 14px 16px;
  background: #fff;
  border: 0.5px solid var(--brix-border);
  border-radius: var(--brix-radius);
}

.item-detail__spec {
  display: flex;
  gap: 10px;
  font-size: 13px;
}

.item-detail__spec dt {
  color: var(--brix-text-placeholder);
  flex-shrink: 0;
}

.item-detail__spec dd {
  margin: 0;
  color: var(--brix-text);
}

.item-detail__desc {
  white-space: pre-wrap;
  line-height: 1.8;
  color: var(--brix-text);
  background: #fff;
  border: 0.5px solid var(--brix-border);
  border-radius: var(--brix-radius);
  padding: 14px 16px;
  font-size: 14px;
}

.item-detail__actions {
  display: flex;
  gap: 8px;
  margin-top: 16px;
  flex-wrap: wrap;
}

.item-detail__sold-tip {
  margin-top: 8px;
  font-size: 12px;
  color: var(--brix-price);
}

.item-detail__aside-row {
  display: flex;
  gap: 20px;
  margin-top: 20px;
  align-items: flex-start;
}

.item-detail__comments {
  flex: 1;
  min-width: 0;
}

.item-detail__seller {
  width: 340px;
  flex-shrink: 0;
}

.item-detail__trade {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
</style>
