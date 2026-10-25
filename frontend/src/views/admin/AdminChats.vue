<template>
  <div class="admin-chats">
    <div class="admin-chats__toolbar brix-card">
      <el-input
        v-model="query.keyword"
        placeholder="消息内容关键词"
        clearable
        style="width: 220px"
        @keyup.enter="search"
        @clear="search"
      />
      <el-input
        v-model="query.username"
        placeholder="发送方 / 接收方 用户名或昵称"
        clearable
        style="width: 260px"
        @keyup.enter="search"
        @clear="search"
      />
      <el-button type="primary" plain @click="search">查询</el-button>
      <el-button @click="reset">重置</el-button>
      <span class="admin-chats__tip">私信不可撤回、不可删除，此处仅供纠纷取证查看</span>
    </div>

    <div class="brix-card admin-chats__table">
      <el-table v-loading="loading" :data="chats">
        <el-table-column label="发送时间" width="160">
          <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="发送方" width="150">
          <template #default="{ row }">
            {{ row.fromUser?.nickname || '-' }}
            <span class="admin-chats__account">（{{ row.fromUser?.username || '-' }}）</span>
          </template>
        </el-table-column>
        <el-table-column label="接收方" width="150">
          <template #default="{ row }">
            {{ row.toUser?.nickname || '-' }}
            <span class="admin-chats__account">（{{ row.toUser?.username || '-' }}）</span>
          </template>
        </el-table-column>
        <el-table-column label="关联商品" min-width="160">
          <template #default="{ row }">
            <span v-if="row.itemTitle" :title="row.itemTitle" class="admin-chats__ellipsis">
              {{ row.itemTitle }}
            </span>
            <span v-else class="admin-chats__account">-</span>
          </template>
        </el-table-column>
        <el-table-column label="内容" min-width="240">
          <template #default="{ row }">
            <el-tooltip :content="row.content" placement="top" :show-after="300">
              <span class="admin-chats__ellipsis">{{ row.content }}</span>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column label="是否已读" width="100">
          <template #default="{ row }">
            <el-tag size="small" effect="plain" :type="row.isRead === 1 ? 'success' : 'info'">
              {{ row.isRead === 1 ? '已读' : '未读' }}
            </el-tag>
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
import { pageChats } from '@/api/admin'
import { DEFAULT_PAGE } from '@/constants'
import { formatTime } from '@/utils/format'

const chats = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ ...DEFAULT_PAGE, keyword: '', username: '' })

/** 加载私信记录（只读，默认时间倒序） */
async function load() {
  loading.value = true
  try {
    const params = {
      currentPage: query.currentPage,
      pageSize: query.pageSize,
      keyword: query.keyword.trim(),
      username: query.username.trim()
    }
    if (!params.keyword) delete params.keyword
    if (!params.username) delete params.username
    const data = await pageChats(params)
    chats.value = data?.records || []
    total.value = Number(data?.total || 0)
  } finally {
    loading.value = false
  }
}

/** 查询：回到第一页 */
function search() {
  query.currentPage = 1
  load()
}

/** 重置检索条件 */
function reset() {
  query.keyword = ''
  query.username = ''
  query.currentPage = 1
  load()
}

/** 翻页 */
function onPageChange(page) {
  query.currentPage = page
  load()
}

onMounted(load)
</script>

<style scoped>
.admin-chats__toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.admin-chats__tip {
  margin-left: auto;
  font-size: 12px;
  color: var(--brix-text-placeholder);
}

.admin-chats__table {
  padding: 8px 16px 16px;
}

.admin-chats__account {
  font-size: 12px;
  color: var(--brix-text-secondary);
}

.admin-chats__ellipsis {
  display: inline-block;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  vertical-align: bottom;
}
</style>
