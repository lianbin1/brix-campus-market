<template>
  <div class="admin-users">
    <div class="admin-users__toolbar brix-card">
      <el-input
        v-model="query.keyword"
        placeholder="用户名 / 昵称"
        clearable
        style="width: 220px"
        @keyup.enter="search"
        @clear="search"
      />
      <el-select v-model="query.role" placeholder="全部角色" clearable style="width: 140px" @change="search">
        <el-option label="普通用户" value="USER" />
        <el-option label="审核员" value="REVIEWER" />
        <el-option label="运营" value="OPERATOR" />
      </el-select>
      <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 140px" @change="search">
        <el-option label="正常" value="NORMAL" />
        <el-option label="已禁用" value="DISABLED" />
      </el-select>
      <el-button type="primary" plain @click="search">查询</el-button>
    </div>

    <div class="brix-card admin-users__table">
      <el-table v-loading="loading" :data="users">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="nickname" label="昵称" min-width="130" />
        <el-table-column prop="school" label="学校" min-width="120" />
        <el-table-column prop="campus" label="校区" width="100" />
        <el-table-column label="角色" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="row.role === 'USER' ? undefined : 'success'" effect="plain">
              {{ row.roleDesc }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="creditScore" label="信用分" width="80" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <StatusTag kind="user" :value="row.status" />
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="注册时间" width="160">
          <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="110" fixed="right">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status === 'NORMAL'"
              :disabled="String(row.id) === String(auth.user?.id)"
              inline-prompt
              active-text="启用"
              inactive-text="禁用"
              @change="toggleStatus(row)"
            />
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
import { fetchAdminUsers, updateUserStatus } from '@/api/admin'
import { useAuthStore } from '@/stores/auth'
import { DEFAULT_PAGE } from '@/constants'
import { formatTime } from '@/utils/format'
import StatusTag from '@/components/StatusTag.vue'

const auth = useAuthStore()

const users = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ ...DEFAULT_PAGE, keyword: '', role: '', status: '' })

/** 加载用户列表 */
async function load() {
  loading.value = true
  try {
    const params = { ...query }
    if (!params.keyword) delete params.keyword
    if (!params.role) delete params.role
    if (!params.status) delete params.status
    const data = await fetchAdminUsers(params)
    users.value = data?.records || []
    total.value = Number(data?.total || 0)
  } finally {
    loading.value = false
  }
}

/** 条件查询：回第 1 页 */
function search() {
  query.currentPage = 1
  load()
}

function onPageChange(page) {
  query.currentPage = page
  load()
}

/** 禁用 / 启用（禁用需二次确认，禁自己按钮置灰由后端再兜底） */
async function toggleStatus(row) {
  const disable = row.status === 'NORMAL'
  if (disable) {
    try {
      await ElMessageBox.confirm(
        `禁用后「${row.nickname}」将立即无法登录，已登录的请求会返回 403。确定禁用吗？`,
        '禁用账号',
        { type: 'warning' }
      )
    } catch (error) {
      return
    }
  }
  try {
    await updateUserStatus(row.id, disable ? 'DISABLED' : 'NORMAL')
    ElMessage.success(disable ? '已禁用' : '已启用')
  } finally {
    load()
  }
}

onMounted(load)
</script>

<style scoped>
.admin-users__toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  margin-bottom: 16px;
}

.admin-users__table {
  padding: 8px 16px 16px;
}
</style>
