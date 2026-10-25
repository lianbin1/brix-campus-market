<template>
  <div class="admin-items">
    <div class="admin-items__toolbar brix-card">
      <el-radio-group v-model="query.status" @change="search">
        <el-radio-button value="PENDING">待审核</el-radio-button>
        <el-radio-button value="ON_SALE">在售</el-radio-button>
        <el-radio-button value="REJECTED">已拒绝</el-radio-button>
        <el-radio-button value="OFF_SHELF">已下架</el-radio-button>
        <el-radio-button value="SOLD">已售出</el-radio-button>
      </el-radio-group>
      <el-input
        v-model="query.keyword"
        placeholder="按标题搜索"
        clearable
        style="width: 220px"
        @keyup.enter="search"
        @clear="search"
      />
      <el-button
        type="primary"
        :disabled="!selectedIds.length || query.status !== 'PENDING'"
        @click="batchPass"
      >
        批量通过（{{ selectedIds.length }}）
      </el-button>
    </div>

    <div class="brix-card admin-items__table">
      <el-table v-loading="loading" :data="items" @selection-change="onSelectionChange">
        <el-table-column v-if="query.status === 'PENDING'" type="selection" width="46" />
        <el-table-column label="商品" min-width="260">
          <template #default="{ row }">
            <div class="admin-items__item">
              <el-image
                :src="resolveImage(row.coverImage)"
                fit="cover"
                class="admin-items__image"
                :preview-src-list="[resolveImage(row.coverImage)]"
                preview-teleported
              />
              <div class="admin-items__item-info">
                <span class="admin-items__title">{{ row.title }}</span>
                <span v-if="row.rejectReason" class="admin-items__reject">{{ row.rejectReason }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="categoryName" label="分类" width="100" />
        <el-table-column prop="sellerNickname" label="卖家" width="130" />
        <el-table-column label="价格" width="100">
          <template #default="{ row }">¥{{ formatPrice(row.price) }}</template>
        </el-table-column>
        <el-table-column prop="campus" label="校区" width="90" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <StatusTag kind="item" :value="row.status" />
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="提交时间" width="160">
          <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 'PENDING'">
              <el-button type="primary" size="small" @click="pass([row.id])">通过</el-button>
              <el-button type="danger" plain size="small" @click="openReject(row)">拒绝</el-button>
            </template>
            <el-button v-else-if="row.status === 'ON_SALE'" type="warning" plain size="small" @click="offline(row)">
              强制下架
            </el-button>
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

    <!-- 拒绝原因弹窗：原因为空时禁用提交 -->
    <el-dialog v-model="rejectVisible" title="拒绝原因" width="420px">
      <el-input
        v-model="rejectReason"
        type="textarea"
        :rows="4"
        maxlength="200"
        show-word-limit
        placeholder="必填（1-200 字），卖家可在「我的商品」看到原因并修改后重提"
      />
      <template #footer>
        <el-button @click="rejectVisible = false">取消</el-button>
        <el-button type="danger" :disabled="!rejectReason.trim()" :loading="rejecting" @click="confirmReject">
          确认拒绝
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { auditPass, auditReject, fetchAdminItems, forceOffline } from '@/api/admin'
import { DEFAULT_PAGE } from '@/constants'
import { formatPrice, formatTime, resolveImage } from '@/utils/format'
import StatusTag from '@/components/StatusTag.vue'

const items = ref([])
const total = ref(0)
const loading = ref(false)
const selectedIds = ref([])

const query = reactive({ ...DEFAULT_PAGE, status: 'PENDING', keyword: '' })

const rejectVisible = ref(false)
const rejectReason = ref('')
const rejecting = ref(false)
const rejectTarget = ref(null)

/** 加载审核列表（默认待审核，按提交时间升序） */
async function load() {
  loading.value = true
  try {
    const params = { ...query }
    if (!params.keyword) delete params.keyword
    const data = await fetchAdminItems(params)
    items.value = data?.records || []
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

function onSelectionChange(rows) {
  selectedIds.value = rows.map((row) => row.id)
}

/** 通过（单条或批量） */
async function pass(ids) {
  const data = await auditPass(ids)
  ElMessage.success(`已通过 ${data?.passed ?? ids.length} 件商品`)
  if (items.value.length === ids.length && query.currentPage > 1) {
    query.currentPage -= 1
  }
  load()
}

/** 批量通过 */
async function batchPass() {
  try {
    await ElMessageBox.confirm(`确定批量通过选中的 ${selectedIds.value.length} 件商品吗？`, '批量通过', {
      type: 'info'
    })
  } catch (error) {
    return
  }
  pass(selectedIds.value)
}

/** 打开拒绝弹窗 */
function openReject(row) {
  rejectTarget.value = row
  rejectReason.value = ''
  rejectVisible.value = true
}

/** 确认拒绝 */
async function confirmReject() {
  if (!rejectReason.value.trim()) return
  rejecting.value = true
  try {
    await auditReject(rejectTarget.value.id, rejectReason.value.trim())
    ElMessage.success('已拒绝')
    rejectVisible.value = false
    load()
  } finally {
    rejecting.value = false
  }
}

/** 强制下架（不受商品归属限制） */
async function offline(row) {
  try {
    await ElMessageBox.prompt('请输入下架原因（选填，将展示给卖家）', `强制下架「${row.title}」`, {
      inputPlaceholder: '例如：标题与实物不符',
      inputValidator: (value) => !value || value.length <= 200 || '原因不能超过 200 字'
    })
      .then(({ value }) => forceOffline(row.id, value || ''))
      .catch(() => null)
  } catch (error) {
    return
  }
  ElMessage.success('已强制下架')
  load()
}

onMounted(load)
</script>

<style scoped>
.admin-items__toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  margin-bottom: 16px;
}

.admin-items__toolbar .el-input {
  margin-left: auto;
}

.admin-items__table {
  padding: 8px 16px 16px;
}

.admin-items__item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.admin-items__image {
  width: 48px;
  height: 48px;
  border-radius: 6px;
  overflow: hidden;
  background: #f2f4f6;
  flex-shrink: 0;
}

.admin-items__item-info {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.admin-items__title {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.admin-items__reject {
  font-size: 12px;
  color: #f56c6c;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
