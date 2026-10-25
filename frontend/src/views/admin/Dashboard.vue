<template>
  <div v-loading="loading" class="dashboard">
    <!-- 指标卡 -->
    <div class="dashboard__metrics">
      <div class="metric brix-card">
        <el-icon class="metric__icon" color="#0FA968"><User /></el-icon>
        <div>
          <div class="metric__value">{{ overview.userCount }}</div>
          <div class="metric__label">注册用户</div>
        </div>
      </div>
      <div class="metric brix-card">
        <el-icon class="metric__icon" color="#F5A623"><Goods /></el-icon>
        <div>
          <div class="metric__value">{{ overview.onSaleCount }}</div>
          <div class="metric__label">在售商品</div>
        </div>
      </div>
      <div class="metric brix-card">
        <el-icon class="metric__icon" color="#4E8FF7"><List /></el-icon>
        <div>
          <div class="metric__value">{{ overview.todayOrderCount }}</div>
          <div class="metric__label">今日订单</div>
        </div>
      </div>
      <div class="metric brix-card">
        <el-icon class="metric__icon" color="#FF4D2E"><Coin /></el-icon>
        <div>
          <div class="metric__value">¥{{ formatPrice(overview.totalTradeAmount) }}</div>
          <div class="metric__label">累计成交额</div>
        </div>
      </div>
    </div>

    <!-- 图表 -->
    <div class="dashboard__charts">
      <div class="chart-card brix-card">
        <h3 class="chart-card__title">近 7 天订单趋势</h3>
        <LineTrend :data="trend" :height="260" />
      </div>
      <div class="chart-card brix-card">
        <h3 class="chart-card__title">在售商品分类分布</h3>
        <PieCategory :data="distribution" :height="260" />
      </div>
    </div>

    <!-- 待审核列表 -->
    <div class="dashboard__pending brix-card">
      <div class="brix-section-title">
        <span>待审核商品（{{ pendingItems.length }}）</span>
        <el-button text type="primary" @click="router.push('/admin/items')">去审核</el-button>
      </div>
      <el-table v-if="pendingItems.length" :data="pendingItems" size="small">
        <el-table-column label="商品" min-width="220">
          <template #default="{ row }">
            <div class="dashboard__pending-item">
              <el-image :src="resolveImage(row.coverImage)" fit="cover" class="dashboard__pending-image" />
              <span class="dashboard__pending-title">{{ row.title }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="sellerNickname" label="卖家" width="140" />
        <el-table-column label="价格" width="100">
          <template #default="{ row }">¥{{ formatPrice(row.price) }}</template>
        </el-table-column>
        <el-table-column prop="createdAt" label="提交时间" width="160">
          <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
        </el-table-column>
      </el-table>
      <EmptyState v-else text="没有待审核的商品" />
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { fetchCategoryDistribution, fetchDashboardOverview, fetchOrderTrend } from '@/api/admin'
import { formatPrice, formatTime, resolveImage } from '@/utils/format'
import LineTrend from '@/components/charts/LineTrend.vue'
import PieCategory from '@/components/charts/PieCategory.vue'
import EmptyState from '@/components/EmptyState.vue'

const router = useRouter()

const loading = ref(false)
const overview = reactive({
  userCount: 0,
  onSaleCount: 0,
  todayOrderCount: 0,
  totalTradeAmount: 0
})
const trend = ref([])
const distribution = ref([])
const pendingItems = ref([])

onMounted(async () => {
  loading.value = true
  try {
    const [overviewData, trendData, distributionData] = await Promise.all([
      fetchDashboardOverview(),
      fetchOrderTrend(7),
      fetchCategoryDistribution()
    ])
    Object.assign(overview, overviewData || {})
    trend.value = trendData || []
    distribution.value = distributionData?.categories || []
    pendingItems.value = distributionData?.pendingItems || []
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.dashboard__metrics {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.metric {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px 20px;
}

.metric__icon {
  font-size: 30px;
}

.metric__value {
  font-size: 24px;
  font-weight: 700;
  line-height: 1.2;
}

.metric__label {
  font-size: 12px;
  color: var(--brix-text-secondary);
}

.dashboard__charts {
  display: grid;
  grid-template-columns: 1.4fr 1fr;
  gap: 16px;
  margin-top: 16px;
}

.chart-card {
  padding: 16px;
}

.chart-card__title {
  margin: 0 0 8px;
  font-size: 15px;
}

.dashboard__pending {
  margin-top: 16px;
  padding: 16px;
}

.dashboard__pending-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.dashboard__pending-image {
  width: 44px;
  height: 44px;
  border-radius: 6px;
  overflow: hidden;
  background: #f2f4f6;
  flex-shrink: 0;
}

.dashboard__pending-title {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
