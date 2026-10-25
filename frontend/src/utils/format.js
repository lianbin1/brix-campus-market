import dayjs from 'dayjs'
import { UNPAID_TIMEOUT_MINUTES } from '@/constants'

/**
 * 格式化工具：金额、时间、倒计时、图片地址、字典文案。
 */

/**
 * 金额格式化：保留两位小数并加千分位。
 * @param {number|string|null|undefined} value 金额
 * @returns {string} 形如 1,280.00
 */
export function formatPrice(value) {
  const num = Number(value)
  if (value === null || value === undefined || Number.isNaN(num)) return '0.00'
  return num.toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',')
}

/**
 * 时间格式化。
 * @param {string|number|Date|null} value 时间
 * @param {string} pattern 格式
 * @returns {string}
 */
export function formatTime(value, pattern = 'YYYY-MM-DD HH:mm') {
  if (!value) return ''
  return dayjs(value).format(pattern)
}

/**
 * 相对时间（刚刚 / n 分钟前 / n 小时前 / n 天前，超过 7 天显示日期）。
 * @param {string|number|Date} value 时间
 * @returns {string}
 */
export function formatRelative(value) {
  if (!value) return ''
  const target = dayjs(value)
  const diffMinute = dayjs().diff(target, 'minute')
  if (diffMinute < 1) return '刚刚'
  if (diffMinute < 60) return `${diffMinute} 分钟前`
  const diffHour = Math.floor(diffMinute / 60)
  if (diffHour < 24) return `${diffHour} 小时前`
  const diffDay = Math.floor(diffHour / 24)
  if (diffDay <= 7) return `${diffDay} 天前`
  return target.format('YYYY-MM-DD')
}

/**
 * 解析图片地址：已是 http(s) 直接返回，否则拼后端地址（dev 下也可由 /uploads 代理）。
 * @param {string} url 相对或绝对地址
 * @returns {string}
 */
export function resolveImage(url) {
  if (!url) return ''
  if (/^https?:\/\//i.test(url)) return url
  const backend = import.meta.env.VITE_BACKEND || ''
  return `${backend}${url.startsWith('/') ? '' : '/'}${url}`
}

/**
 * 计算待付款剩余秒数（下单时间 + 30 分钟 - 现在）。
 * @param {string} createdAt 下单时间
 * @returns {number} 剩余秒数，最小 0
 */
export function payRemainSeconds(createdAt) {
  if (!createdAt) return 0
  const deadline = dayjs(createdAt).add(UNPAID_TIMEOUT_MINUTES, 'minute')
  const remain = deadline.diff(dayjs(), 'second')
  return remain > 0 ? remain : 0
}

/**
 * 秒数转 mm:ss。
 * @param {number} seconds 秒
 * @returns {string}
 */
export function formatCountdown(seconds) {
  const total = Math.max(0, Number(seconds) || 0)
  const mm = String(Math.floor(total / 60)).padStart(2, '0')
  const ss = String(total % 60).padStart(2, '0')
  return `${mm}:${ss}`
}

/**
 * 从字典取文案。
 * @param {Record<string, {label: string}>} dict 字典
 * @param {string} key 键
 * @returns {string}
 */
export function dictLabel(dict, key) {
  if (!dict || !key) return ''
  return dict[key]?.label ?? key
}

/**
 * 从字典取 el-tag 的 type。
 * @param {Record<string, {color: string}>} dict 字典
 * @param {string} key 键
 * @returns {string}
 */
export function dictColor(dict, key) {
  if (!dict || !key) return ''
  return dict[key]?.color ?? ''
}

/**
 * 昵称首字（无头像时的占位）。
 * @param {string} nickname 昵称
 * @returns {string}
 */
export function nicknameInitial(nickname) {
  if (!nickname) return '用'
  return nickname.trim().charAt(0)
}
