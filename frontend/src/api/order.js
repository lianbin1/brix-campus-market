import request from '@/utils/request'

/**
 * 订单与面交码接口（E01-E09、F02-F04）。
 */

/** 下单（返回 UNPAID 订单，前端跳收银台） */
export function createOrder(data) {
  return request.post('/orders', data)
}

/** 收银台支付（模拟） */
export function payOrder(id, payMethod) {
  return request.post(`/orders/${id}/pay`, { payMethod })
}

/** 卖家发货 */
export function shipOrder(id) {
  return request.post(`/orders/${id}/ship`)
}

/** 买家确认收货 */
export function confirmOrder(id) {
  return request.post(`/orders/${id}/confirm`)
}

/** 取消订单 */
export function cancelOrder(id, reason) {
  return request.post(`/orders/${id}/cancel`, { reason })
}

/** 我买到的订单 */
export function fetchBoughtOrders(params) {
  return request.get('/orders/bought', { params })
}

/** 我卖出的订单 */
export function fetchSoldOrders(params) {
  return request.get('/orders/sold', { params })
}

/** 订单详情 */
export function fetchOrderDetail(id) {
  return request.get(`/orders/${id}`)
}

/** 查看面交确认码（仅买卖双方） */
export function fetchTradeCode(id) {
  return request.get(`/orders/${id}/trade-code`)
}

/** 卖家核销确认码（skipErrorTip：就地展示剩余次数） */
export function verifyTradeCode(id, code) {
  return request.post(`/orders/${id}/trade-code/verify`, { code }, { skipErrorTip: true })
}
