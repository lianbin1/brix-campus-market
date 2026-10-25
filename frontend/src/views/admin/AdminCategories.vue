<template>
  <div class="admin-categories">
    <div class="brix-card admin-categories__table">
      <div class="admin-categories__toolbar">
        <h3 class="admin-categories__title">分类管理</h3>
        <el-button type="primary" @click="openDialog(null)">新增分类</el-button>
      </div>

      <el-table v-loading="loading" :data="categories">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="图标" width="80">
          <template #default="{ row }">
            <el-icon size="20" color="#0FA968">
              <component :is="row.icon || 'Goods'" />
            </el-icon>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="分类名称" min-width="160" />
        <el-table-column prop="sort" label="排序" width="90" />
        <el-table-column prop="itemCount" label="商品数" width="100" />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button text type="primary" size="small" @click="openDialog(row)">编辑</el-button>
            <el-button text type="danger" size="small" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 新增 / 编辑弹窗（本地副本，打开时刷新并清校验） -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑分类' : '新增分类'" width="420px" @open="syncForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" maxlength="30" placeholder="如：数码电子" />
        </el-form-item>
        <el-form-item label="图标" prop="icon">
          <el-select v-model="form.icon" style="width: 100%" placeholder="选择图标（Element Plus 图标名）">
            <el-option v-for="icon in ICON_OPTIONS" :key="icon" :label="icon" :value="icon" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" :max="99" controls-position="right" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createCategory, deleteCategory, fetchAdminCategories, updateCategory } from '@/api/category'
import { DIALOG_MODE } from '@/constants'

const ICON_OPTIONS = ['Cellphone', 'Monitor', 'Notebook', 'House', 'Basketball', 'MagicStick', 'Goods', 'Star']

const categories = ref([])
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const formRef = ref(null)

// 弹窗表单副本：绝不直接改 props / 行对象
const form = reactive({ id: '', name: '', icon: '', sort: 0 })
const dialogMode = ref(DIALOG_MODE.CREATE)

const rules = {
  name: [{ required: true, message: '分类名称不能为空', trigger: 'blur' }]
}

/** 加载分类（含商品数） */
async function load() {
  loading.value = true
  try {
    categories.value = (await fetchAdminCategories()) || []
  } finally {
    loading.value = false
  }
}

/** 打开弹窗（新增传 null，编辑传行对象） */
function openDialog(row) {
  dialogMode.value = row ? DIALOG_MODE.EDIT : DIALOG_MODE.CREATE
  form.id = row?.id ?? ''
  dialogVisible.value = true
}

/** 打开时把行数据拷贝进表单副本 */
function syncForm() {
  if (dialogMode.value === DIALOG_MODE.EDIT) {
    const row = categories.value.find((item) => String(item.id) === String(form.id)) || {}
    form.name = row.name || ''
    form.icon = row.icon || ''
    form.sort = row.sort ?? 0
  } else {
    form.name = ''
    form.icon = ''
    form.sort = categories.value.length + 1
  }
  formRef.value?.clearValidate()
}

/** 保存 */
async function save() {
  try {
    await formRef.value.validate()
  } catch (error) {
    return
  }
  saving.value = true
  try {
    const payload = { name: form.name.trim(), icon: form.icon, sort: form.sort }
    if (dialogMode.value === DIALOG_MODE.EDIT) {
      await updateCategory(form.id, payload)
      ElMessage.success('保存成功')
    } else {
      await createCategory(payload)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    load()
  } finally {
    saving.value = false
  }
}

/** 删除（分类下仍有商品时后端拒绝） */
async function remove(row) {
  try {
    await ElMessageBox.confirm(`确定删除分类「${row.name}」吗？`, '删除分类', { type: 'warning' })
  } catch (error) {
    return
  }
  await deleteCategory(row.id)
  ElMessage.success('已删除')
  load()
}

onMounted(load)
</script>

<style scoped>
.admin-categories__table {
  padding: 8px 16px 16px;
}

.admin-categories__toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 0 14px;
}

.admin-categories__title {
  margin: 0;
  font-size: 16px;
}
</style>
