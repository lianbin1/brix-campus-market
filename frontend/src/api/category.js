import request from '@/utils/request'

/**
 * 分类接口（B09 + 后台 K12）。
 */

/** 前台分类列表（按 sort 升序，无需登录） */
export function fetchCategories() {
  return request.get('/categories')
}

/** 后台分类列表（含商品数） */
export function fetchAdminCategories() {
  return request.get('/admin/categories')
}

/** 新增分类 */
export function createCategory(data) {
  return request.post('/admin/categories', data)
}

/** 编辑分类 */
export function updateCategory(id, data) {
  return request.put(`/admin/categories/${id}`, data)
}

/** 删除分类（有商品时后端拒绝） */
export function deleteCategory(id) {
  return request.delete(`/admin/categories/${id}`)
}
