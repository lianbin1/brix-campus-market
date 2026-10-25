<template>
  <div class="admin-orders">
    <div class="admin-orders__toolbar brix-card">
      <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 150px" @change="search">
        <el-option v-for="(info, value) in ORDER_STATUS_DICT" :key="value" :label="info.label" :value="value" />
      </el-select>
      <el-input
        v-model="query.keyword"
        placeholder="订单号 / 商品标题 / 买卖家用户名"
        clearable
        style="width: 280px"
        @keyup.enter="search"
        @clear="search"
      />
      <el-button type="primary" plain @click="search">查询</el-button>
    </div>

    <div class="brix-card admin-orders__table">
      <el-table v-loading="loading" :data="orders">
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column label="商品" min-width="200">
          <template #default="{ row }">
            <div class="admin-orders__item">
              <el-image :src="resolveImage(row.itemCover)" fit="cover" class="admin-orders__image" />
              <span class="admin-orders__title">{{ row.itemTitle }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="买家" width="130">
          <template #default="{ row }">{{ row.buyerNickname }}（{{ row.buyerUsername }}）</template>
        </el-table-column>
        <el-table-column label="卖家" width="130">
          <template #default="{ row }">{{ row.sellerNickname }}（{{ row.sellerUsername }}）</template>
        </el-table-column>
        <el-table-column label="金额" width="100">
          <template #default="{ row }">¥{{ formatPrice(row.amount) }}</template>
        </el-table-column>
        <el-table-column label="支付方式" width="100">
          <template #default="{ row }">{{ row.payMethodDesc || '-' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <StatusTag kind="order" :value="row.status" />
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="下单时间" width="160">
          <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" size="small" @click="openDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

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

    <!-- 详情抽屉：完整时间线 + 运营兜底操作 + 私信取证 -->
    <el-drawer v-model="drawerVisible" title="订单详情" size="460px">
      <template v-if="detail">
        <div class="admin-orders__drawer-head">
          <StatusTag kind="order" :value="detail.status" />
          <span class="admin-orders__drawer-no">{{ detail.orderNo }}</span>
        </div>
        <div class="admin-orders__drawer-amount">
          ¥{{ formatPrice(detail.amount) }}
          <span class="admin-orders__drawer-method">{{ detail.payMethodDesc || '未支付' }}</span>
        </div>

        <h4>商品信息</h4>
        <div class="admin-orders__drawer-item">
          <el-image :src="resolveImage(detail.itemCover)" fit="cover" class="admin-orders__image" />
          <div>
            <div>{{ detail.itemTitle }}</div>
            <div class="admin-orders__drawer-meta">
              单价 ¥{{ formatPrice(detail.itemPrice) }} ·
              {{ detail.tradeType === 'MEET' ? '面交' : '邮寄' }}
            </div>
            <div v-if="detail.meetPlace" class="admin-orders__drawer-meta">面交地点：{{ detail.meetPlace }}</div>
            <div v-if="detail.buyerNote" class="admin-orders__drawer-meta">买家备注：{{ detail.buyerNote }}</div>
          </div>
        </div>

        <h4>交易双方</h4>
        <p class="admin-orders__drawer-meta">
          买家：{{ detail.buyerNickname }}（{{ detail.buyerUsername }}）<br />
          卖家：{{ detail.sellerNickname }}（{{ detail.sellerUsername }}）
        </p>

        <!-- 面交确认码状态与运营兜底入口（BUG-C） -->
        <template v-if="detail.status === 'TO_MEET'">
          <h4>确认码状态</h4>
          <div class="admin-orders__code">
            <el-tag size="small" effect="plain" :type="detail.codeLocked ? 'danger' : 'success'">
              {{ detail.codeLocked ? '已锁定' : `剩余 ${detail.remainingAttempts ?? 0} 次` }}
            </el-tag>
            <span class="admin-orders__drawer-meta">
              核销失败次数：{{ detail.codeFailCount ?? 0 }}
            </span>
          </div>
          <div v-if="detail.codeLocked" class="admin-orders__ops">
            <el-alert
              type="warning"
              :closable="false"
              show-icon
              title="卖家核销次数已用尽，订单已锁定，需运营兜底处理"
              class="admin-orders__ops-alert"
            />
            <div class="admin-orders__ops-buttons">
              <el-button type="warning" size="small" :loading="resettingCode" @click="handleResetTradeCode">
                重置核销次数
              </el-button>
              <el-button type="danger" size="small" :loading="forcingCancel" @click="handleForceCancel">
                强制取消订单
              </el-button>
            </div>
          </div>
        </template>

        <h4>时间线</h4>
        <el-timeline>
          <el-timeline-item
            v-for="node in drawerTimeline"
            :key="node.label"
            :type="node.time ? 'success' : 'info'"
            :hollow="!node.time"
            :timestamp="node.time || '待完成'"
          >
            {{ node.label }}
          </el-timeline-item>
        </el-timeline>
        <el-alert
          v-if="detail.closeReasonDesc"
          type="info"
          :closable="false"
          :title="`关闭原因：${detail.closeReasonDesc}`"
        />

        <h4>私信取证</h4>
        <div class="admin-orders__chat-entry">
          <el-button size="small" plain @click="openChat">查看聊天记录</el-button>
          <span class="admin-orders__drawer-meta">私信不可撤回、不可删除</span>
        </div>
      </template>
    </el-drawer>

    <!-- 买卖双方私信记录（只读） -->
    <el-dialog v-model="chatVisible" title="买卖双方私信记录" width="620px">
      <div v-loading="chatLoading" class="admin-chat">
        <p class="admin-chat__meta">
          订单号：{{ chatData?.orderNo || '-' }} · 商品：{{ chatData?.itemTitle || '-' }}
        </p>
        <p class="admin-chat__meta">
          买家：{{ chatData?.buyer?.nickname || '-' }} ／ 卖家：{{ chatData?.seller?.nickname || '-' }}
          · 共 {{ chatData?.total || 0 }} 条
        </p>
        <div class="admin-chat__body">
          <div
            v-for="message in chatMessages"
            :key="message.id"
            :class="['admin-chat__row', isBuyerMessage(message) ? 'is-left' : 'is-right']"
          >
            <div class="admin-chat__bubble">
              <div class="admin-chat__sender">{{ message.fromNickname || '-' }}</div>
              <p class="admin-chat__content">{{ message.content }}</p>
              <span class="admin-chat__time">{{ formatTime(message.createdAt, 'YYYY-MM-DD HH:mm:ss') }}</span>
            </div>
          </div>
          <div v-if="!chatLoading && !chatMessages.length" class="admin-chat__empty">双方暂无聊天记录</div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  fetchAdminOrders,
  fetchOrderChat,
  forceCancelOrder,
  resetTradeCode
} from '@/api/admin'
import { fetchOrderDetail } from '@/api/order'
import { DEFAULT_PAGE, ORDER_STATUS_DICT } from '@/constants'
import { formatPrice, formatTime, resolveImage } from '@/utils/format'
import StatusTag from '@/components/StatusTag.vue'

const orders = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ ...DEFAULT_PAGE, status: '', keyword: '' })

const drawerVisible = ref(false)
const detail = ref(null)
const resettingCode = ref(false)
const forcingCancel = ref(false)

const chatVisible = ref(false)
const chatLoading = ref(false)
const chatData = ref(null)
/** 聊天记录消息列表（空对话返回空数组，不报错） */
const chatMessages = computed(() => chatData.value?.messages || [])

/** 抽屉时间线 */
const drawerTimeline = computed(() => {
  const target = detail.value
  if (!target) return []
  return [
    { label: '创建订单', time: formatTime(target.createdAt) || '' },
    { label: '买家付款', time: formatTime(target.payTime) || '' },
    { label: '卖家发货', time: formatTime(target.shipTime) || '' },
    { label: '交易完成', time: formatTime(target.finishTime) || '' },
    { label: '订单取消', time: formatTime(target.cancelTime) || '' }
  ]
})

/** 加载订单 */
async function load() {
  loading.value = true
  try {
    const params = { ...query }
    if (!params.status) delete params.status
    if (!params.keyword) delete params.keyword
    const data = await fetchAdminOrders(params)
    orders.value = data?.records || []
    total.value = Number(data?.total || 0)
  } finally {
    loading.value = false
  }
}

function search() {
  query.currentPage = 1
  load()
}

function onPageChange(page) {
  query.currentPage = page
  load()
}

/** 打开抽屉（复用订单详情接口：后台角色可见） */
async function openDetail(row) {
  drawerVisible.value = true
  detail.value = null
  try {
    detail.value = await fetchOrderDetail(row.id)
  } catch (error) {
    drawerVisible.value = false
  }
}

/**
 * 用运营操作返回的最新订单同步列表行（抽屉内容随后从详情接口重新拉取，避免本地推算状态）。
 * @param {object} latest 后端返回的最新订单
 */
function syncLatest(latest) {
  if (!latest) return
  const index = orders.value.findIndex((item) => String(item.id) === String(latest.id))
  if (index >= 0) {
    orders.value[index] = { ...orders.value[index], ...latest }
  }
}

/** 运营重置核销次数（二次确认） */
async function handleResetTradeCode() {
  const target = detail.value
  if (!target) return
  try {
    await ElMessageBox.confirm(
      `确认重置订单 ${target.orderNo} 的核销失败次数？重置后卖家可继续核销（剩余 5 次）。`,
      '重置核销次数',
      { type: 'warning', confirmButtonText: '确认重置', cancelButtonText: '取消' }
    )
  } catch (error) {
    return
  }
  resettingCode.value = true
  try {
    const latest = await resetTradeCode(target.id)
    syncLatest(latest)
    ElMessage.success('已重置核销次数')
    await load()
    detail.value = await fetchOrderDetail(target.id)
  } finally {
    resettingCode.value = false
  }
}

/** 运营强制取消订单（二次确认） */
async function handleForceCancel() {
  const target = detail.value
  if (!target) return
  try {
    await ElMessageBox.confirm(
      `确认强制取消订单 ${target.orderNo}？订单将置为「已取消」，商品将回到在售状态。该操作不可撤销。`,
      '强制取消订单',
      { type: 'warning', confirmButtonText: '确认取消', cancelButtonText: '返回' }
    )
  } catch (error) {
    return
  }
  forcingCancel.value = true
  try {
    const latest = await forceCancelOrder(target.id)
    syncLatest(latest)
    ElMessage.success('订单已强制取消')
    await load()
    detail.value = await fetchOrderDetail(target.id)
  } finally {
    forcingCancel.value = false
  }
}

/** 打开私信记录对话框 */
async function openChat() {
  const target = detail.value
  if (!target) return
  chatVisible.value = true
  chatLoading.value = true
  chatData.value = null
  try {
    chatData.value = await fetchOrderChat(target.id)
  } finally {
    chatLoading.value = false
  }
}

/**
 * 判断消息是否为买家发出（买家气泡在左，卖家在右）。
 * @param {{fromUserId: string|number}} message 消息
 * @returns {boolean}
 */
function isBuyerMessage(message) {
  return String(message.fromUserId) === String(chatData.value?.buyer?.id)
}

onMounted(load)
</script>

<style scoped>
.admin-orders__toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  margin-bottom: 16px;
}

.admin-orders__table {
  padding: 8px 16px 16px;
}

.admin-orders__item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.admin-orders__image {
  width: 42px;
  height: 42px;
  border-radius: 6px;
  overflow: hidden;
  background: #f2f4f6;
  flex-shrink: 0;
}

.admin-orders__title {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.admin-orders__drawer-head {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.admin-orders__drawer-no {
  font-size: 13px;
  color: var(--brix-text-secondary);
}

.admin-orders__drawer-amount {
  font-size: 24px;
  font-weight: 700;
  color: var(--brix-price);
  margin-bottom: 16px;
}

.admin-orders__drawer-amount .admin-orders__drawer-method {
  margin-left: 8px;
  font-size: 12px;
  font-weight: 400;
  color: var(--brix-text-secondary);
}

.admin-orders__drawer-item {
  display: flex;
  gap: 10px;
  margin-bottom: 8px;
}

.admin-orders__drawer-meta {
  margin-top: 4px;
  font-size: 12px;
  color: var(--brix-text-secondary);
  line-height: 1.7;
}

.admin-orders__code {
  display: flex;
  align-items: center;
  gap: 10px;
}

.admin-orders__ops {
  margin-top: 10px;
}

.admin-orders__ops-alert {
  margin-bottom: 10px;
}

.admin-orders__ops-buttons {
  display: flex;
  gap: 10px;
}

.admin-orders__chat-entry {
  display: flex;
  align-items: center;
  gap: 10px;
}

.admin-orders :deep(h4) {
  margin: 18px 0 8px;
  font-size: 14px;
}

.admin-chat__meta {
  margin: 0 0 6px;
  font-size: 12px;
  color: var(--brix-text-secondary);
}

.admin-chat__body {
  max-height: 420px;
  overflow-y: auto;
  padding: 12px;
  border-radius: 8px;
  background: #f7f8fa;
}

.admin-chat__row {
  display: flex;
  margin-bottom: 12px;
}

.admin-chat__row.is-left {
  justify-content: flex-start;
}

.admin-chat__row.is-right {
  justify-content: flex-end;
}

.admin-chat__bubble {
  max-width: 70%;
  padding: 8px 12px;
  border-radius: 10px;
  background: #fff;
  border: 0.5px solid var(--brix-border);
}

.admin-chat__row.is-right .admin-chat__bubble {
  background: var(--brix-primary-light);
  border-color: transparent;
}

.admin-chat__sender {
  font-size: 11px;
  color: var(--brix-text-placeholder);
  margin-bottom: 2px;
}

.admin-chat__content {
  margin: 0;
  font-size: 13px;
  white-space: pre-wrap;
  word-break: break-word;
}

.admin-chat__time {
  display: block;
  margin-top: 4px;
  font-size: 11px;
  color: var(--brix-text-placeholder);
  text-align: right;
}

.admin-chat__empty {
  padding: 60px 0;
  text-align: center;
  font-size: 13px;
  color: var(--brix-text-placeholder);
}
</style>
