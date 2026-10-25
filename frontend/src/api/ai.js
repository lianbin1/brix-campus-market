import request from '@/utils/request'

/**
 * AI 接口（J01-J04）。两个接口共用每天 20 次的计数池。
 */

/** 智能估价（无 Key 时后端自动降级，返回同结构结果） */
export function estimatePrice(data) {
  return request.post('/ai/estimate', data, { timeout: 20000 })
}

/** 一键生成标题与描述 */
export function generateCopywriting(data) {
  return request.post('/ai/copywriting', data, { timeout: 20000 })
}
