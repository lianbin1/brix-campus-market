<template>
  <div class="admin-reports">
    <div class="admin-reports__toolbar brix-card">
      <el-radio-group v-model="query.status" @change="search">
        <el-radio-button value="PENDING">待处理</el-radio-button>
        <el-radio-button value="HANDLED">已处理</el-radio-button>
      </el-radio-group>
    </div>

    <div class="brix-card admin-reports__table">
      <el-table v-loading="loading" :data="reports">
        <el-table-column label="商品" min-width="220">
          <template #default="{ row }">
            <a
              :href="`/#/items/${row.itemId}`"
              target="_blank"
              class="admin-reports__item-link"
            >{{ row.itemTitle || `商品 #${row.itemId}` }}</a>
          </template>
        </el-table-column>
        <el-table-column prop="reporterNickname" label="举报人" width="120" />
        <el-table-column label="原因" width="110">
          <template #default="{ row }">
            <el-tag size="small" type="warning" effect="plain">{{ row.reasonDesc }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="说明" min-width="200" show-overflow-tooltip />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <StatusTag kind="report" :value="row.status" />
          </template>
        </el-table-column>
        <el-table-column label="处理结果" min-width="140">
          <template #default="{ row }">
            <template v-if="row.status === 'HANDLED'">
              {{ row.handleActionDesc }} · {{ row.handlerNickname || '-' }}
            </template>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="举报时间" width="160">
          <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="170" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 'PENDING'">
              <el-button type="danger" size="small" @click="handle(row, 'OFFLINE')">下架商品</el-button>
              <el-button size="small" @click="handle(row, 'REJECT')">驳回举报</el-button>
            </template>
            <span v-else class="admin-reports__done-time">{{ formatTime(row.handledAt) }}</span>
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
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { fetchAdminReports, handleReport } from '@/api/admin'
import { DEFAULT_PAGE, HANDLE_ACTION_DICT } from '@/constants'
import { formatTime } from '@/utils/format'
import StatusTag from '@/components/StatusTag.vue'

const reports = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ ...DEFAULT_PAGE, status: 'PENDING' })

/** 加载举报列表 */
async function load() {
  loading.value = true
  try {
    const data = await fetchAdminReports({ ...query })
    reports.value = data?.records || []
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

/** 处理举报：下架商品 / 驳回举报 */
async function handle(row, action) {
  const label = HANDLE_ACTION_DICT[action]
  const tip =
    action === 'OFFLINE'
      ? `下架后商品立即从首页消失，并通知卖家「${row.itemTitle}」。确定处理吗？`
      : `驳回后该举报记为已处理，商品保持不变。确定驳回吗？`
  try {
    await ElMessageBox.confirm(tip, label, { type: action === 'OFFLINE' ? 'warning' : 'info' })
  } catch (error) {
    return
  }
  await handleReport(row.id, action)
  ElMessage.success(`${label}完成`)
  load()
}

onMounted(load)
</script>

<style scoped>
.admin-reports__toolbar {
  display: flex;
  align-items: center;
  padding: 14px 16px;
  margin-bottom: 16px;
}

.admin-reports__table {
  padding: 8px 16px 16px;
}

.admin-reports__item-link {
  color: var(--brix-primary);
}

.admin-reports__done-time {
  font-size: 12px;
  color: var(--brix-text-placeholder);
}
</style>
