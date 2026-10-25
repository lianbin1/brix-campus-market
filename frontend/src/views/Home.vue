<template>
  <div class="home">
    <section class="home__hero">
      <div class="home__hero-inner brix-container">
        <div class="home__hero-text">
          <h1>校园闲置，当面验货再付款</h1>
          <p>严格限定本校同学之间交易 · 面交确认码保障一手交钱一手交货 · AI 帮你智能定价</p>
          <div class="home__hero-actions">
            <el-button v-if="auth.role === 'USER'" type="primary" size="large" @click="router.push('/publish')">
              发布闲置
            </el-button>
            <el-button v-else-if="!auth.isLogin" type="primary" size="large" @click="router.push('/register')">
              免费注册
            </el-button>
            <el-button size="large" plain @click="scrollToGrid">逛逛好物</el-button>
          </div>
        </div>
        <div class="home__hero-stats">
          <div class="home__stat">
            <span class="home__stat-value">{{ inSaleTotal }}</span>
            <span class="home__stat-label">在售好物</span>
          </div>
          <div class="home__stat">
            <span class="home__stat-value">{{ categories.length }}</span>
            <span class="home__stat-label">商品分类</span>
          </div>
          <div class="home__stat">
            <span class="home__stat-value">3</span>
            <span class="home__stat-label">个校区</span>
          </div>
        </div>
      </div>
    </section>

    <section ref="gridRef" class="home__grid brix-container">
      <FilterBar v-model="filters" :categories="categories" @change="onFilterChange" />
      <ItemGrid :items="items" :loading="loading" show-reset @reset="resetFilters" />
      <el-pagination
        v-if="total > query.pageSize"
        background
        layout="prev, pager, next, total"
        :total="total"
        :page-size="query.pageSize"
        :current-page="query.currentPage"
        @current-change="onPageChange"
      />
    </section>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { fetchItems } from '@/api/item'
import { fetchCategories } from '@/api/category'
import { useAuthStore } from '@/stores/auth'
import { DEFAULT_PAGE } from '@/constants'
import FilterBar from '@/components/FilterBar.vue'
import ItemGrid from '@/components/ItemGrid.vue'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const categories = ref([])
const items = ref([])
const total = ref(0)
const inSaleTotal = ref(0)
const loading = ref(false)
const gridRef = ref(null)

// 分页参数（单独管理，条件变化时回到第 1 页）
const query = reactive({ ...DEFAULT_PAGE })

// 筛选条件（与 URL query 双向同步，可分享 / 后退复现）
const filters = reactive({
  keyword: '',
  categoryId: '',
  campus: '',
  minPrice: '',
  maxPrice: '',
  sort: 'latest'
})

/** 从 URL query 恢复筛选条件 */
function syncFromRoute() {
  const q = route.query
  filters.keyword = typeof q.keyword === 'string' ? q.keyword : ''
  filters.categoryId = typeof q.categoryId === 'string' ? q.categoryId : ''
  filters.campus = typeof q.campus === 'string' ? q.campus : ''
  filters.minPrice = typeof q.minPrice === 'string' ? q.minPrice : ''
  filters.maxPrice = typeof q.maxPrice === 'string' ? q.maxPrice : ''
  filters.sort = typeof q.sort === 'string' ? q.sort : 'latest'
  query.currentPage = Number(q.currentPage) > 0 ? Number(q.currentPage) : 1
}

/** 拉取商品列表 */
async function load() {
  loading.value = true
  try {
    const params = {
      currentPage: query.currentPage,
      pageSize: query.pageSize,
      sort: filters.sort || 'latest'
    }
    if (filters.keyword) params.keyword = filters.keyword
    if (filters.categoryId) params.categoryId = filters.categoryId
    if (filters.campus) params.campus = filters.campus
    if (filters.minPrice !== '' && filters.minPrice !== null) params.minPrice = filters.minPrice
    if (filters.maxPrice !== '' && filters.maxPrice !== null) params.maxPrice = filters.maxPrice

    const data = await fetchItems(params)
    items.value = data?.records || []
    total.value = Number(data?.total || 0)
    if (query.currentPage === 1) inSaleTotal.value = total.value
  } finally {
    loading.value = false
  }
}

/** 条件变化：回到第 1 页并把条件写入 URL */
function onFilterChange() {
  query.currentPage = 1
  writeQuery()
}

/** 翻页 */
function onPageChange(page) {
  query.currentPage = page
  writeQuery()
}

/** 重置筛选 */
function resetFilters() {
  filters.keyword = ''
  filters.categoryId = ''
  filters.campus = ''
  filters.minPrice = ''
  filters.maxPrice = ''
  filters.sort = 'latest'
  query.currentPage = 1
  writeQuery()
}

/** 把筛选条件写入 URL query（首页可分享、后退可复现） */
function writeQuery() {
  const q = {}
  if (filters.keyword) q.keyword = filters.keyword
  if (filters.categoryId) q.categoryId = filters.categoryId
  if (filters.campus) q.campus = filters.campus
  if (filters.minPrice !== '' && filters.minPrice !== null) q.minPrice = filters.minPrice
  if (filters.maxPrice !== '' && filters.maxPrice !== null) q.maxPrice = filters.maxPrice
  if (filters.sort && filters.sort !== 'latest') q.sort = filters.sort
  if (query.currentPage > 1) q.currentPage = String(query.currentPage)
  router.replace({ path: '/', query: q })
}

function scrollToGrid() {
  gridRef.value?.scrollIntoView({ behavior: 'smooth' })
}

watch(
  () => route.query,
  () => {
    syncFromRoute()
    load()
  }
)

onMounted(async () => {
  syncFromRoute()
  load()
  try {
    categories.value = (await fetchCategories()) || []
  } catch (error) {
    categories.value = []
  }
})
</script>

<style scoped>
.home__hero {
  background: linear-gradient(120deg, #0fa968 0%, #12bd75 60%, #2fd39a 100%);
  border-radius: 14px;
  overflow: hidden;
  color: #fff;
}

.home__hero-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 40px;
  padding: 48px 40px;
}

.home__hero-text h1 {
  font-size: 32px;
  line-height: 1.3;
  margin: 0;
}

.home__hero-text p {
  margin-top: 12px;
  font-size: 15px;
  opacity: 0.92;
}

.home__hero-actions {
  display: flex;
  gap: 12px;
  margin-top: 24px;
}

.home__hero-actions :deep(.el-button--primary) {
  background: #fff;
  border-color: #fff;
  color: var(--brix-primary);
}

.home__hero-actions :deep(.el-button--primary:hover) {
  background: #f2fbf7;
  border-color: #f2fbf7;
}

.home__hero-actions :deep(.el-button.is-plain) {
  background: transparent;
  border-color: rgba(255, 255, 255, 0.6);
  color: #fff;
}

.home__hero-stats {
  display: flex;
  gap: 28px;
  flex-shrink: 0;
}

.home__stat {
  display: flex;
  flex-direction: column;
  align-items: center;
  background: rgba(255, 255, 255, 0.16);
  border-radius: 12px;
  padding: 16px 22px;
  min-width: 96px;
}

.home__stat-value {
  font-size: 26px;
  font-weight: 700;
}

.home__stat-label {
  margin-top: 2px;
  font-size: 12px;
  opacity: 0.9;
}

.home__grid {
  padding-top: 24px;
}
</style>
