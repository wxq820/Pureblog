export interface WidgetVO {
  id: number | string
  code: string
  name: string
  description?: string
  icon?: string
  iconColor?: string
  path: string
  sortOrder: number
  status: number
  clickCount: number
  createdAt?: string
  updatedAt?: string
}

export interface LedgerDictVO {
  id: number | string
  /** 1=平台 2=项目 */
  type: number
  name: string
  color?: string
  icon?: string
  sortOrder: number
  status: number
  createdAt?: string
}

export interface LedgerRecordVO {
  id: number | string
  /** YYYY-MM-DD */
  recordDate: string
  platformDictId: number | string
  platformName: string
  platformColor?: string
  platformIcon?: string
  /** 字典项的 name, 或自定义文本 itemText 的原值 */
  itemName: string
  itemColor?: string
  /** Element Plus Icon 的 key 字符串, 如 "Dish" / "Bowl"; null 时前端 fallback 圆点 */
  itemIcon?: string
  amount: number
  createdAt?: string
}
