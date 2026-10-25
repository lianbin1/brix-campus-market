<template>
  <div class="trade-code brix-card">
    <div class="brix-section-title">
      <span>面交确认码</span>
      <span class="brix-section-title__sub">验货后由卖家核销，订单立即完成</span>
    </div>

    <div v-if="locked" class="trade-code__locked">
      <el-alert
        type="error"
        :closable="false"
        show-icon
        title="该订单核销已锁定"
        description="累计失败 5 次，请联系运营处理"
      />
    </div>

    <div v-else class="trade-code__view">
      <div class="trade-code__digits">
        <span v-for="(digit, index) in digits" :key="index" class="trade-code__digit">{{ digit }}</span>
      </div>
      <div class="trade-code__hint">
        <span>剩余可试 {{ remainingAttempts }} 次</span>
        <el-button text type="primary" size="small" @click="copyCode">复制确认码</el-button>
      </div>
    </div>

    <div v-if="isSeller && orderStatus === 'TO_MEET'" class="trade-code__verify">
      <el-input
        v-model="code"
        maxlength="6"
        placeholder="请输入买家口述的 6 位确认码"
        class="trade-code__input"
        :disabled="locked"
        @keyup.enter="doVerify"
      />
      <el-button type="primary" :loading="verifying" :disabled="locked || code.length !== 6" @click="doVerify">
        核销
      </el-button>
    </div>

    <p v-if="errorMessage" class="trade-code__error">{{ errorMessage }}</p>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchTradeCode, verifyTradeCode } from '@/api/order'

const props = defineProps({
  orderId: { type: [String, Number], required: true },
  isSeller: { type: Boolean, default: false },
  orderStatus: { type: String, default: 'TO_MEET' }
})

const emit = defineEmits(['verified'])

const tradeCode = ref('')
const failCount = ref(0)
const remainingAttempts = ref(5)
const locked = ref(false)
const code = ref('')
const verifying = ref(false)
const errorMessage = ref('')

const digits = computed(() => (tradeCode.value || '------').split(''))

/** 加载确认码 */
async function load() {
  try {
    const data = await fetchTradeCode(props.orderId)
    tradeCode.value = data?.tradeCode || ''
    failCount.value = Number(data?.failCount || 0)
    remainingAttempts.value = Number(data?.remainingAttempts ?? 5)
    locked.value = !!data?.locked
  } catch (error) {
    errorMessage.value = error?.message || '确认码加载失败'
  }
}

/** 复制确认码 */
async function copyCode() {
  try {
    await navigator.clipboard.writeText(tradeCode.value)
    ElMessage.success('确认码已复制')
  } catch (error) {
    ElMessage.warning(`复制失败，请手动记下：${tradeCode.value}`)
  }
}

/** 核销：失败时就地提示剩余次数（skipErrorTip 已在 api 层开启） */
async function doVerify() {
  if (code.value.length !== 6) {
    ElMessage.warning('请输入 6 位确认码')
    return
  }
  verifying.value = true
  errorMessage.value = ''
  try {
    const data = await verifyTradeCode(props.orderId, code.value)
    ElMessage.success('核销成功，订单已完成')
    emit('verified', data)
    await load()
  } catch (error) {
    errorMessage.value = error?.message || '核销失败'
    await load()
  } finally {
    verifying.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.trade-code {
  padding: 16px;
}

.trade-code__digits {
  display: flex;
  gap: 8px;
  justify-content: center;
  padding: 8px 0;
}

.trade-code__digit {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 46px;
  height: 58px;
  border-radius: 8px;
  background: var(--brix-primary-light);
  color: var(--brix-primary);
  font-size: 30px;
  font-weight: 700;
  letter-spacing: 1px;
}

.trade-code__hint {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  font-size: 12px;
  color: var(--brix-text-secondary);
}

.trade-code__verify {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 14px;
  padding-top: 14px;
  border-top: 0.5px dashed var(--brix-border);
}

.trade-code__input {
  width: 240px;
}

.trade-code__error {
  margin-top: 8px;
  color: #f56c6c;
  font-size: 13px;
}

.trade-code__locked {
  margin: 8px 0;
}
</style>
