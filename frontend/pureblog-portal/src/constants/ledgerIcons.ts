import {
  ChatDotRound,
  Money,
  ShoppingCart,
  VideoCamera,
  Share,
  Sunny,
  Moon,
  Dish,
  Bowl,
  Goblet,
  Food,
  HotWater,
  House,
  Watermelon,
  Coin,
  TakeawayBox,
  Promotion,
  CreditCard,
  Cellphone,
  Lightning,
  Wallet,
  Present
} from '@element-plus/icons-vue'

/**
 * 平台: 按 dictId 写死的图标映射. 不依赖 DB, 改这里就能改前端显示.
 * Element Plus Icons 没有真正的品牌 logo, 用语义最贴近的图标凑合.
 */
export const PLATFORM_ICONS: Record<number, any> = {
  101: VideoCamera,  // 抖音
  102: Share,        // 小红书
  103: ShoppingCart, // 淘宝
  104: ChatDotRound, // 微信
  105: Money         // 支付宝
}

/** 项目可选图标库: 添加新项目时给用户选 */
export interface IconOption {
  key: string
  label: string
  comp: any
  color: string
}

export const ITEM_ICON_OPTIONS: IconOption[] = [
  { key: 'Dish',         label: '餐食', comp: Dish,         color: '#f97316' },
  { key: 'Bowl',         label: '正餐', comp: Bowl,         color: '#f59e0b' },
  { key: 'Food',         label: '食物', comp: Food,         color: '#ef4444' },
  { key: 'Moon',         label: '夜宵', comp: Moon,         color: '#8b5cf6' },
  { key: 'Sunny',        label: '白天', comp: Sunny,        color: '#eab308' },
  { key: 'Goblet',       label: '饮料', comp: Goblet,       color: '#06b6d4' },
  { key: 'HotWater',     label: '热水', comp: HotWater,     color: '#0ea5e9' },
  { key: 'House',        label: '居家', comp: House,        color: '#6366f1' },
  { key: 'Watermelon',   label: '水果', comp: Watermelon,   color: '#10b981' },
  { key: 'Coin',         label: '零钱', comp: Coin,         color: '#eab308' },
  { key: 'TakeawayBox',  label: '外卖', comp: TakeawayBox,  color: '#f97316' },
  { key: 'Promotion',    label: '活动', comp: Promotion,    color: '#ec4899' },
  { key: 'CreditCard',   label: '刷卡', comp: CreditCard,   color: '#1677ff' },
  { key: 'Cellphone',    label: '话费', comp: Cellphone,    color: '#3b82f6' },
  { key: 'Lightning',    label: '水电', comp: Lightning,    color: '#eab308' },
  { key: 'Wallet',       label: '钱包', comp: Wallet,       color: '#84cc16' },
  { key: 'Present',      label: '礼物', comp: Present,      color: '#f43f5e' }
]

export const ITEM_ICON_MAP: Record<string, IconOption> = Object.fromEntries(
  ITEM_ICON_OPTIONS.map(o => [o.key, o])
)
