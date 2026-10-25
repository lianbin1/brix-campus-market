import request from '@/utils/request'

/**
 * 后台接口（K02-K12、H02）。
 */

/** 4 项核心指标 */
export function fetchDashboardOverview() {
  return request.get('/admin/dashboard/overview')
}

/** 近 N 天订单趋势（缺日补 0） */
export function fetchOrderTrend(days = 7) {
  return request.get('/admin/dashboard/order-trend', { params: { days } })
}

/** 分类分布 + 待审核 Top10 */
export function fetchCategoryDistribution() {
  return request.get('/admin/dashboard/category-distribution')
}

/** 用户列表 */
export function fetchAdminUsers(params) {
  return request.get('/admin/users', { params })
}

/** 禁用 / 启用用户 */
export function updateUserStatus(id, status) {
  return request.patch(`/admin/users/${id}/status`, { status })
}

/** 商品审核列表（默认 PENDING） */
export function fetchAdminItems(params) {
  return request.get('/admin/items', { params })
}

/** 批量（或单条）审核通过 */
export function auditPass(ids) {
  return request.post('/admin/items/audit/pass', { ids })
}

/** 审核拒绝 */
export function auditReject(id, reason) {
  return request.post(`/admin/items/${id}/audit/reject`, { reason })
}

/** 强制下架 */
export function forceOffline(id, reason) {
  return request.post(`/admin/items/${id}/offline`, { reason })
}

/** 全平台订单查询 */
export function fetchAdminOrders(params) {
  return request.get('/admin/orders', { params })
}

/** 重置面交核销失败次数（仅 TO_MEET 且已锁定可执行） */
export function resetTradeCode(id) {
  return request.post(`/admin/orders/${id}/reset-trade-code`)
}

/** 强制取消订单（仅 TO_MEET / TO_SHIP 可执行） */
export function forceCancelOrder(id) {
  return request.post(`/admin/orders/${id}/force-cancel`)
}

/** 某个订单买卖双方的私信记录（只读，纠纷取证） */
export function fetchOrderChat(orderId) {
  return request.get(`/admin/orders/${orderId}/chat`)
}

/** 全平台私信检索（只读，分页） */
export function pageChats(params) {
  return request.get('/admin/chats', { params })
}

/** 举报列表 */
export function fetchAdminReports(params) {
  return request.get('/admin/reports', { params })
}

/** 处理举报（OFFLINE / REJECT） */
export function handleReport(id, action) {
  return request.post(`/admin/reports/${id}/handle`, { action })
}
