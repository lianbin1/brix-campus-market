<template>
  <el-dialog :model-value="modelValue" title="评价卖家" width="480px" @update:model-value="close" @open="reset">
    <el-form label-width="70px">
      <el-form-item label="评分">
        <el-rate v-model="form.rating" show-text :texts="RATE_TEXTS" />
      </el-form-item>
      <el-form-item label="评价">
        <el-input
          v-model="form.content"
          type="textarea"
          :rows="4"
          maxlength="500"
          show-word-limit
          placeholder="说说成色是否与描述一致、卖家是否好沟通（可留空）"
        />
      </el-form-item>
      <el-form-item label="图片">
        <ImageUploader v-model="form.images" :max="MAX_REVIEW_IMAGES" biz-type="review" />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="close(false)">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="submit">提交评价</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import ImageUploader from './ImageUploader.vue'
import { createReview } from '@/api/review'
import { MAX_REVIEW_IMAGES } from '@/constants'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  orderId: { type: [String, Number], default: '' }
})

const emit = defineEmits(['update:modelValue', 'success'])

const RATE_TEXTS = ['很差', '较差', '一般', '满意', '非常满意']
const submitting = ref(false)

// 本地副本：每次打开弹窗刷新并清校验
const form = reactive({
  rating: 5,
  content: '',
  images: []
})

/** 重置表单 */
function reset() {
  form.rating = 5
  form.content = ''
  form.images = []
}

/** 关闭弹窗 */
function close(value = false) {
  emit('update:modelValue', value)
}

/** 提交评价 */
async function submit() {
  if (!form.rating) {
    ElMessage.warning('请选择评分')
    return
  }
  submitting.value = true
  try {
    await createReview(props.orderId, {
      rating: form.rating,
      content: form.content.trim(),
      images: form.images
    })
    ElMessage.success('评价成功，感谢你的反馈')
    close(false)
    emit('success')
  } finally {
    submitting.value = false
  }
}
</script>
