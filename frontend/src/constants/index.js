/**
 * 全局常量与枚举字典。
 *
 * 说明：所有 id 在后端都以字符串下发（Long → String），前端一律按 string 处理。
 */

/** 后端统一响应码 */
export const RESULT_CODE = {
  SUCCESS: 200,
  PARAM_ERROR: 400,
  UNAUTHORIZED: 401,
  FORBIDDEN: 403,
  NOT_FOUND: 404,
  METHOD_NOT_ALLOWED: 405,
  TOO_MANY_REQUESTS: 429,
  BIZ_ERROR: 500,
  SYSTEM_ERROR: 500
}

/** 分页默认值（与后端 currentPage / pageSize 对齐） */
export const DEFAULT_PAGE = {
  currentPage: 1,
  pageSize: 10
}

/** 私信分页默认值（后端默认 20） */
export const CHAT_PAGE = {
  currentPage: 1,
  pageSize: 20
}

/** 弹窗模式 */
export const DIALOG_MODE = {
  CREATE: 'create',
  EDIT: 'edit'
}

/** 金额格式（0.01 ~ 99999.99，最多两位小数） */
export const PRICE_PATTERN = /^(?!0+(\.0{1,2})?$)\d{1,5}(\.\d{1,2})?$/

/** 校区选项（与种子数据一致，不做移动端适配） */
export const CAMPUS_OPTIONS = ['东校区', '西校区', '南校区']

/** 角色字典 */
export const ROLE_DICT = {
  USER: { label: '普通用户', color: '' },
  REVIEWER: { label: '审核员', color: 'warning' },
  OPERATOR: { label: '运营', color: 'success' }
}

/** 用户状态字典 */
export const USER_STATUS_DICT = {
  NORMAL: { label: '正常', color: 'success' },
  DISABLED: { label: '已禁用', color: 'info' }
}

/** 商品状态字典（5 种） */
export const ITEM_STATUS_DICT = {
  PENDING: { label: '待审核', color: 'warning' },
  ON_SALE: { label: '在售', color: 'success' },
  OFF_SHELF: { label: '已下架', color: 'info' },
  SOLD: { label: '已售出', color: 'danger' },
  REJECTED: { label: '审核拒绝', color: 'danger' }
}

/** 成色字典 */
export const CONDITION_DICT = {
  BRAND_NEW: { label: '全新', color: 'success' },
  LIKE_NEW: { label: '几乎全新', color: 'success' },
  LIGHT_USE: { label: '轻微使用', color: 'warning' },
  HEAVY_USE: { label: '明显使用', color: 'info' }
}

/** 交易方式字典 */
export const TRADE_TYPE_DICT = {
  MEET: { label: '面交', color: 'success' },
  SHIP: { label: '邮寄', color: 'warning' },
  BOTH: { label: '面交或邮寄', color: '' }
}

/** 订单状态字典（6 种） */
export const ORDER_STATUS_DICT = {
  UNPAID: { label: '待付款', color: 'warning' },
  TO_SHIP: { label: '待发货', color: '' },
  TO_RECEIVE: { label: '待收货', color: '' },
  TO_MEET: { label: '待面交', color: 'success' },
  DONE: { label: '已完成', color: 'info' },
  CANCELED: { label: '已取消', color: 'info' }
}

/** 支付方式字典 */
export const PAY_METHOD_DICT = {
  WECHAT: { label: '微信支付', icon: 'ChatDotRound' },
  ALIPAY: { label: '支付宝', icon: 'Wallet' },
  BANK: { label: '银行卡', icon: 'CreditCard' }
}

/** 举报原因字典 */
export const REPORT_REASON_DICT = {
  FAKE_INFO: '虚假信息',
  CONTRABAND: '违禁物品',
  PRICE_FRAUD: '价格欺诈',
  STOLEN_IMAGE: '盗图',
  OTHER: '其他'
}

/** 举报状态字典 */
export const REPORT_STATUS_DICT = {
  PENDING: { label: '待处理', color: 'warning' },
  HANDLED: { label: '已处理', color: 'success' }
}

/** 举报处理动作字典 */
export const HANDLE_ACTION_DICT = {
  OFFLINE: '下架商品',
  REJECT: '驳回举报'
}

/** 订单关闭原因字典 */
export const CLOSE_REASON_DICT = {
  TIMEOUT: '超时未支付',
  BUYER_CANCEL: '买家取消',
  SELLER_CANCEL: '卖家取消',
  SYSTEM: '系统关闭'
}

/** 商品排序选项 */
export const SORT_OPTIONS = [
  { value: 'latest', label: '最新发布' },
  { value: 'price_asc', label: '价格从低到高' },
  { value: 'price_desc', label: '价格从高到低' },
  { value: 'hot', label: '最多浏览' }
]

/** 商品图片上限 */
export const MAX_ITEM_IMAGES = 9

/** 评价图片上限 */
export const MAX_REVIEW_IMAGES = 3

/** 待付款超时（分钟），与后端 brix.order.unpaid-timeout-minutes 一致 */
export const UNPAID_TIMEOUT_MINUTES = 30

/** 后台菜单（刷新后按 role 本地重建，与后端 LoginVO.menus 保持一致） */
export const ADMIN_MENUS = {
  OPERATOR: [
    { key: 'dashboard', title: '数据大屏', path: '/admin/dashboard', icon: 'DataLine' },
    { key: 'users', title: '用户管理', path: '/admin/users', icon: 'User' },
    { key: 'orders', title: '订单管理', path: '/admin/orders', icon: 'List' },
    { key: 'categories', title: '分类管理', path: '/admin/categories', icon: 'Menu' },
    { key: 'chats', title: '私信记录', path: '/admin/chats', icon: 'ChatDotRound' }
  ],
  REVIEWER: [
    { key: 'items', title: '商品审核', path: '/admin/items', icon: 'Goods' },
    { key: 'reports', title: '举报处理', path: '/admin/reports', icon: 'Warning' },
    { key: 'chats', title: '私信记录', path: '/admin/chats', icon: 'ChatDotRound' }
  ]
}
