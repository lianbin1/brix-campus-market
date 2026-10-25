import request from '@/utils/request'

/**
 * 收藏接口（C01、C02）。
 */

/** 收藏 / 取消收藏（切换式，返回 { favorited, favoriteCount }） */
export function toggleFavorite(id) {
  return request.post(`/items/${id}/favorite`)
}

/** 我的收藏分页 */
export function fetchFavorites(params) {
  return request.get('/favorites', { params })
}
