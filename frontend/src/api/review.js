import request from '@/utils/request'

/**
 * 评价接口（G01）。
 */

/** 买家评价卖家（订单必须已完成，每单一次） */
export function createReview(orderId, data) {
  return request.post(`/orders/${orderId}/review`, data)
}
