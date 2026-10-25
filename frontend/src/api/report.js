import request from '@/utils/request'

/**
 * 举报接口（H01）。
 */

/** 提交举报（reason 为枚举，当前用户不能举报自己的商品） */
export function createReport(itemId, data) {
  return request.post(`/items/${itemId}/reports`, data)
}
