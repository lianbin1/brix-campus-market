<template>
  <el-dialog :model-value="modelValue" title="举报商品" width="460px" @update:model-value="close" @open="reset">
    <el-form label-width="70px">
      <el-form-item label="原因" required>
        <el-select v-model="form.reason" placeholder="请选择举报原因" style="width: 100%">
          <el-option v-for="(label, value) in REPORT_REASON_DICT" :key="value" :label="label" :value="value" />
        </el-select>
      </el-form-item>
      <el-form-item label="说明">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="3"
          maxlength="200"
          show-word-limit
          placeholder="补充说明（可选），便于审核员判断"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="close(false)">取消</el-button>
      <el-button type="danger" :loading="submitting" @click="submit">提交举报</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { createReport } from '@/api/report'
import { REPORT_REASON_DICT } from '@/constants'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  itemId: { type: [String, Number], default: '' }
})

const emit = defineEmits(['update:modelValue', 'success'])

const submitting = ref(false)

// 本地副本，避免直接改 props
const form = reactive({
  reason: '',
  description: ''
})

/** 重置 */
function reset() {
  form.reason = ''
  form.description = ''
}

/** 关闭 */
function close(value = false) {
  emit('update:modelValue', value)
}

/** 提交举报 */
async function submit() {
  if (!form.reason) {
    ElMessage.warning('请选择举报原因')
    return
  }
  submitting.value = true
  try {
    await createReport(props.itemId, { reason: form.reason, description: form.description.trim() })
    ElMessage.success('举报已提交，平台会尽快处理')
    close(false)
    emit('success')
  } finally {
    submitting.value = false
  }
}
</script>
