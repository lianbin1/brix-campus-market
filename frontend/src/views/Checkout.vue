<template>
  <div v-loading="loading" class="checkout">
    <template v-if="order">
      <div class="checkout__card brix-card">
        <h1 class="checkout__title">收银台</h1>

        <div class="checkout__countdown" :class="{ 'is-danger': remainSeconds <= 60 }">
          <el-icon><AlarmClock /></el-icon>
          <span v-if="order.status === 'UNPAID'">请在 {{ formatCountdown(remainSeconds) }} 内完成支付</span>
          <span v-else>订单已关闭或已支付</span>
        </div>

        <div class="checkout__order">
          <el-image :src="resolveImage(order.itemCover)" fit="cover" class="checkout__image">
            <template #error>
              <div class="checkout__placeholder"><el-icon><Picture /></el-icon></div>
            </template>
          </el-image>
          <div class="checkout__order-info">
            <div class="checkout__order-title">{{ order.itemTitle }}</div>
            <div class="checkout__order-meta">
              订单号 {{ order.orderNo }} ·
              {{ order.tradeType === 'MEET' ? '面交' : '邮寄' }}
              <template v-if="order.meetPlace"> · {{ order.meetPlace }}</template>
            </div>
          </div>
          <div class="checkout__amount">
            <span class="checkout__amount-label">应付金额</span>
            <PriceTag :price="order.amount" :show-original="false" size="large" />
          </div>
        </div>

        <el-divider />

        <div class="checkout__methods">
          <div class="checkout__methods-title">选择支付方式（模拟支付，不产生真实扣款）</div>
          <el-radio-group v-model="payMethod" class="checkout__method-group">
            <el-radio-button
              v-for="(info, value) in PAY_METHOD_DICT"
              :key="value"
              :value="value"
              :disabled="order.status !== 'UNPAID'"
            >
              {{ info.label }}
            </el-radio-button>
          </el-radio-group>
        </div>

        <el-button
          type="primary"
          size="large"
          class="checkout__submit"
          :loading="paying"
          :disabled="order.status !== 'UNPAID' || remainSeconds <= 0"
          @click="pay"
        >
          确认支付 ¥{{ formatPrice(order.amount) }}
        </el-button>

        <p class="checkout__tip">
          支付成功后面交单将生成 6 位确认码，请在面交验货后把确认码告知卖家完成核销
        </p>
      </div>
    </template>

    <EmptyState v-else-if="!loading" text="订单不存在或无权查看" />
  </div>
</template>

<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { fetchOrderDetail, payOrder } from '@/api/order'
import { useAuthStore } from '@/stores/auth'
import { formatCountdown, formatPrice, payRemainSeconds, resolveImage } from '@/utils/format'
import PriceTag from '@/components/PriceTag.vue'
import EmptyState from '@/components/EmptyState.vue'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const orderId = String(route.params.orderId || '')
const order = ref(null)
const loading = ref(false)
const paying = ref(false)
const payMethod = ref('WECHAT')
const remainSeconds = ref(0)
let timer = null

const UNPAID = 'UNPAID'

/** 启动每秒倒计时 */
function startCountdown() {
  stopCountdown()
  const tick = () => {
    remainSeconds.value = payRemainSeconds(order.value?.createdAt)
    if (remainSeconds.value <= 0 && order.value?.status === UNPAID) {
      stopCountdown()
      ElMessage.warning('订单已超时关闭')
      router.replace('/my/orders')
    }
  }
  tick()
  timer = setInterval(tick, 1000)
}

function stopCountdown() {
  if (timer) {
    clearInterval(timer)
    timer = null
  }
}

/** 模拟支付：按钮 loading 约 1 秒后成功跳转 */
async function pay() {
  paying.value = true
  const startedAt = Date.now()
  try {
    const paid = await payOrder(orderId, payMethod.value)
    // 保持约 1 秒的 loading，模拟支付回调
    const elapsed = Date.now() - startedAt
    if (elapsed < 1000) {
      await new Promise((resolve) => setTimeout(resolve, 1000 - elapsed))
    }
    ElMessage.success('支付成功')
    router.replace(`/orders/${paid.id}`)
  } finally {
    paying.value = false
  }
}

async function load() {
  loading.value = true
  try {
    order.value = await fetchOrderDetail(orderId)
    if (order.value?.status === UNPAID) {
      startCountdown()
    }
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  if (!auth.isLogin) {
    router.push({ path: '/login', query: { redirect: encodeURIComponent(route.fullPath) } })
    return
  }
  load()
})

onBeforeUnmount(stopCountdown)
</script>

<style scoped>
.checkout__card {
  max-width: 720px;
  margin: 0 auto;
  padding: 28px 32px;
}

.checkout__title {
  font-size: 22px;
  margin: 0 0 12px;
}

.checkout__countdown {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border-radius: 20px;
  background: #fff7e8;
  color: #b25e00;
  font-size: 13px;
}

.checkout__countdown.is-danger {
  background: #fdeaea;
  color: #d03050;
}

.checkout__order {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-top: 20px;
}

.checkout__image {
  width: 88px;
  height: 88px;
  border-radius: 10px;
  overflow: hidden;
  background: #f2f4f6;
  flex-shrink: 0;
}

.checkout__placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 88px;
  height: 88px;
  color: #c9cfd7;
}

.checkout__order-info {
  flex: 1;
  min-width: 0;
}

.checkout__order-title {
  font-weight: 600;
}

.checkout__order-meta {
  margin-top: 6px;
  font-size: 12px;
  color: var(--brix-text-secondary);
}

.checkout__amount {
  text-align: right;
  flex-shrink: 0;
}

.checkout__amount-label {
  display: block;
  font-size: 12px;
  color: var(--brix-text-placeholder);
  margin-bottom: 4px;
}

.checkout__methods {
  margin-top: 4px;
}

.checkout__methods-title {
  font-size: 13px;
  color: var(--brix-text-secondary);
  margin-bottom: 10px;
}

.checkout__method-group {
  display: flex;
}

.checkout__method-group :deep(.el-radio-button) {
  flex: 1;
}

.checkout__submit {
  width: 100%;
  margin-top: 20px;
  height: 46px;
  font-size: 16px;
}

.checkout__tip {
  margin-top: 12px;
  font-size: 12px;
  color: var(--brix-text-placeholder);
  text-align: center;
}
</style>
