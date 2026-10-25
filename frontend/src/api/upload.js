import request from '@/utils/request'

/**
 * 图片上传接口（N06、N07）。
 */

/**
 * 上传单张图片。
 * @param {File} file 图片文件
 * @param {'item'|'avatar'|'review'} bizType 业务类型
 */
export function uploadImage(file, bizType = 'item') {
  const form = new FormData()
  form.append('file', file)
  form.append('bizType', bizType)
  return request.post('/upload/image', form, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
