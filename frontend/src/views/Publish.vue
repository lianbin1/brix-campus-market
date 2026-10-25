<template>
  <div class="publish">
    <div class="publish__head">
      <h1>{{ isEdit ? '编辑商品' : '发布闲置' }}</h1>
      <p class="brix-text-secondary">发布后进入待审核，审核员通过后才会出现在首页</p>
    </div>

    <div class="publish__body">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px" class="publish__form brix-card">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" maxlength="40" show-word-limit placeholder="一句话说清是什么东西（40 字内）" />
        </el-form-item>

        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="选择分类" style="width: 240px">
            <el-option v-for="category in categories" :key="category.id" :label="category.name" :value="category.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="价格" prop="price">
          <div class="publish__price-row">
            <el-input-number v-model="form.price" :min="0.01" :max="99999.99" :precision="2" :controls="false" placeholder="0.00" style="width: 160px" />
            <span class="publish__price-label">元</span>
            <el-input-number v-model="form.originalPrice" :min="0.01" :max="999999.99" :precision="2" :controls="false" placeholder="原价（选填）" style="width: 160px" />
            <span class="publish__price-label">原价（选填，不低于现价）</span>
          </div>
        </el-form-item>

        <el-form-item label="成色" prop="conditionLevel">
          <el-radio-group v-model="form.conditionLevel">
            <el-radio-button
              v-for="(info, value) in CONDITION_DICT"
              :key="value"
              :value="value"
            >
              {{ info.label }}
            </el-radio-button>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="校区" prop="campus">
          <el-radio-group v-model="form.campus">
            <el-radio-button v-for="campus in CAMPUS_OPTIONS" :key="campus" :value="campus">
              {{ campus }}
            </el-radio-button>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="交易方式" prop="tradeType">
          <el-radio-group v-model="form.tradeType">
            <el-radio-button
              v-for="(info, value) in TRADE_TYPE_DICT"
              :key="value"
              :value="value"
            >
              {{ info.label }}
            </el-radio-button>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="联系方式">
          <el-input v-model="form.contact" maxlength="50" placeholder="微信号 / QQ（选填，也可只留私信）" />
        </el-form-item>

        <el-form-item label="商品图片">
          <ImageUploader v-model="form.images" :max="MAX_ITEM_IMAGES" biz-type="item" />
        </el-form-item>

        <el-form-item label="描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="6"
            maxlength="2000"
            show-word-limit
            placeholder="成色细节、购买渠道、配件情况、可面交时间等"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" size="large" :loading="submitting" @click="submit">
            {{ isEdit ? '保存修改' : '提交审核' }}
          </el-button>
          <el-button size="large" @click="router.back()">取消</el-button>
        </el-form-item>
      </el-form>

      <aside class="publish__ai">
        <AiPricePanel
          :category-id="form.categoryId"
          :condition-level="form.conditionLevel"
          @apply-price="applyPrice"
          @apply-copy="applyCopy"
        />
      </aside>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createItem, fetchItemDetail, updateItem } from '@/api/item'
import { fetchCategories } from '@/api/category'
import {
  CAMPUS_OPTIONS,
  CONDITION_DICT,
  MAX_ITEM_IMAGES,
  TRADE_TYPE_DICT
} from '@/constants'
import ImageUploader from '@/components/ImageUploader.vue'
import AiPricePanel from '@/components/AiPricePanel.vue'

const route = useRoute()
const router = useRouter()

const editId = typeof route.query.id === 'string' && route.query.id ? route.query.id : ''
const isEdit = computed(() => !!editId)

const formRef = ref(null)
const submitting = ref(false)
const categories = ref([])

// 表单副本（编辑模式先取详情再回填）
const form = reactive({
  title: '',
  description: '',
  categoryId: '',
  price: null,
  originalPrice: null,
  conditionLevel: 'LIGHT_USE',
  campus: '',
  tradeType: 'MEET',
  contact: '',
  images: []
})

const rules = {
  title: [{ required: true, message: '标题不能为空', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  price: [{ required: true, message: '价格不能为空', trigger: 'blur' }],
  conditionLevel: [{ required: true, message: '请选择成色', trigger: 'change' }],
  campus: [{ required: true, message: '请选择校区', trigger: 'change' }],
  tradeType: [{ required: true, message: '请选择交易方式', trigger: 'change' }],
  description: [{ required: true, message: '描述不能为空', trigger: 'blur' }]
}

/** AI 建议价回填 */
function applyPrice(price) {
  form.price = Number(price)
  ElMessage.success('已使用 AI 建议价')
}

/** AI 文案回填（覆盖标题与描述，可再编辑） */
function applyCopy(copy) {
  if (copy?.title) form.title = copy.title
  if (copy?.description) form.description = copy.description
  ElMessage.success('已填入标题与描述')
}

/** 提交：新建走 POST，编辑走 PUT（后端一律回到待审核） */
async function submit() {
  try {
    await formRef.value.validate()
  } catch (error) {
    return
  }
  const payload = {
    title: form.title.trim(),
    description: form.description.trim(),
    categoryId: form.categoryId,
    price: form.price,
    originalPrice: form.originalPrice,
    conditionLevel: form.conditionLevel,
    campus: form.campus,
    tradeType: form.tradeType,
    contact: form.contact.trim(),
    images: form.images
  }
  if (isEdit.value && form.originalPrice !== null && Number(form.originalPrice) < Number(form.price)) {
    ElMessage.warning('原价不能低于现价')
    return
  }
  submitting.value = true
  try {
    if (isEdit.value) {
      await updateItem(editId, payload)
      ElMessage.success('修改成功，等待重新审核')
    } else {
      await createItem(payload)
      ElMessage.success('已提交审核')
    }
    router.push('/my/items')
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  try {
    categories.value = (await fetchCategories()) || []
  } catch (error) {
    categories.value = []
  }
  if (!applyDefaultCampus()) {
    form.campus = ''
  }

  // 编辑模式：先取详情再回填（禁止用列表行对象直接回填）
  if (isEdit.value) {
    try {
      const detail = await fetchItemDetail(editId)
      form.title = detail.title
      form.description = detail.description
      form.categoryId = detail.categoryId
      form.price = Number(detail.price)
      form.originalPrice = detail.originalPrice === null ? null : Number(detail.originalPrice)
      form.conditionLevel = detail.conditionLevel
      form.campus = detail.campus
      form.tradeType = detail.tradeType
      form.contact = detail.contact || ''
      form.images = (detail.images || []).map((image) => image.url)
    } catch (error) {
      ElMessage.error('商品加载失败')
      router.push('/my/items')
    }
  }
})

/** 取当前登录用户校区作为默认值 */
function applyDefaultCampus() {
  const campus = localStorage.getItem('brix_user')
  if (!campus) return false
  try {
    const user = JSON.parse(campus)
    if (user?.campus) {
      form.campus = user.campus
      return true
    }
  } catch (error) {
    // ignore
  }
  return false
}
</script>

<style scoped>
.publish__head {
  margin-bottom: 16px;
}

.publish__head h1 {
  font-size: 22px;
  margin: 0;
}

.publish__body {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

.publish__form {
  flex: 1;
  min-width: 0;
  padding: 24px 24px 8px;
}

.publish__ai {
  width: 520px;
  flex-shrink: 0;
}

.publish__price-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.publish__price-label {
  font-size: 12px;
  color: var(--brix-text-placeholder);
}
</style>
