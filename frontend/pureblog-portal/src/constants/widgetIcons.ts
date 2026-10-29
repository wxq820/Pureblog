import {
  Aim,
  Bell,
  Box,
  Calendar,
  ChatDotRound,
  Coin,
  Compass,
  Connection,
  CreditCard,
  DataAnalysis,
  DataBoard,
  DataLine,
  Discount,
  Document,
  EditPen,
  Film,
  Files,
  Folder,
  Goods,
  Grid,
  Histogram,
  House,
  List,
  Location,
  MagicStick,
  Management,
  Memo,
  Money,
  Monitor,
  Notebook,
  Operation,
  Orange,
  Paperclip,
  PieChart,
  Position,
  Present,
  PriceTag,
  Reading,
  ReadingLamp,
  Stamp,
  Star,
  Stopwatch,
  Tickets,
  Timer,
  Tools,
  TrendCharts,
  Trophy,
  Wallet,
  WalletFilled,
  Watermelon
} from '@element-plus/icons-vue'

/**
 * 小工具图标: Element Plus Icons (单色 SVG), 不再用彩色 emoji.
 * DB 里 pb_widget.icon 存的是这里定义的 key, 前端按 key 查组件渲染.
 */
export interface WidgetIconOption {
  key: string
  label: string
  comp: any
}

/** 后台「图标」下拉的可选项 */
export const WIDGET_ICON_OPTIONS: WidgetIconOption[] = [
  { key: 'EditPen', label: '编辑', comp: EditPen },
  { key: 'Operation', label: '运算', comp: Operation },
  { key: 'Wallet', label: '钱包', comp: Wallet },
  { key: 'WalletFilled', label: '钱包(实心)', comp: WalletFilled },
  { key: 'Money', label: '金额', comp: Money },
  { key: 'Coin', label: '硬币', comp: Coin },
  { key: 'CreditCard', label: '银行卡', comp: CreditCard },
  { key: 'PriceTag', label: '价签', comp: PriceTag },
  { key: 'Discount', label: '折扣', comp: Discount },
  { key: 'Tickets', label: '票券', comp: Tickets },
  { key: 'TrendCharts', label: '趋势', comp: TrendCharts },
  { key: 'DataLine', label: '数据线', comp: DataLine },
  { key: 'DataBoard', label: '看板', comp: DataBoard },
  { key: 'DataAnalysis', label: '分析', comp: DataAnalysis },
  { key: 'Histogram', label: '柱状图', comp: Histogram },
  { key: 'PieChart', label: '饼图', comp: PieChart },
  { key: 'Stopwatch', label: '秒表', comp: Stopwatch },
  { key: 'Timer', label: '计时器', comp: Timer },
  { key: 'Calendar', label: '日历', comp: Calendar },
  { key: 'MagicStick', label: '魔法棒', comp: MagicStick },
  { key: 'Star', label: '星星', comp: Star },
  { key: 'Trophy', label: '奖杯', comp: Trophy },
  { key: 'Bell', label: '铃铛', comp: Bell },
  { key: 'Aim', label: '靶心', comp: Aim },
  { key: 'Compass', label: '指南针', comp: Compass },
  { key: 'Location', label: '位置', comp: Location },
  { key: 'Position', label: '方位', comp: Position },
  { key: 'Document', label: '文档', comp: Document },
  { key: 'Files', label: '文件', comp: Files },
  { key: 'Folder', label: '文件夹', comp: Folder },
  { key: 'Memo', label: '备忘录', comp: Memo },
  { key: 'Notebook', label: '笔记本', comp: Notebook },
  { key: 'Reading', label: '阅读', comp: Reading },
  { key: 'ReadingLamp', label: '台灯', comp: ReadingLamp },
  { key: 'Stamp', label: '印章', comp: Stamp },
  { key: 'Paperclip', label: '回形针', comp: Paperclip },
  { key: 'Grid', label: '网格', comp: Grid },
  { key: 'List', label: '列表', comp: List },
  { key: 'Management', label: '管理', comp: Management },
  { key: 'Monitor', label: '显示器', comp: Monitor },
  { key: 'Tools', label: '工具', comp: Tools },
  { key: 'Box', label: '盒子', comp: Box },
  { key: 'Goods', label: '商品', comp: Goods },
  { key: 'Present', label: '礼物', comp: Present },
  { key: 'House', label: '房屋', comp: House },
  { key: 'Connection', label: '连接', comp: Connection },
  { key: 'ChatDotRound', label: '对话', comp: ChatDotRound },
  { key: 'Film', label: '影片', comp: Film },
  { key: 'Orange', label: '橙子', comp: Orange },
  { key: 'Watermelon', label: '西瓜', comp: Watermelon }
]

/** key → 组件 */
export const WIDGET_ICON_MAP: Record<string, any> = Object.fromEntries(
  WIDGET_ICON_OPTIONS.map(o => [o.key, o.comp])
)

/**
 * 按 widget code 的兜底映射: DB 里 icon 为空/不认识时按 code 取,
 * 避免首页出现空白方块.
 */
export const WIDGET_ICON_BY_CODE: Record<string, string> = {
  typing: 'EditPen',
  random: 'Operation',
  ledger: 'Wallet',
  networth: 'CreditCard',
  recovery: 'TrendCharts',
  fund: 'DataLine'
}

export const DEFAULT_WIDGET_ICON = 'Grid'

/**
 * 取图标组件: 优先 DB 的 icon 名 → 兜底 code 映射 → 兜底默认图标.
 */
export function resolveWidgetIcon(icon?: string | null, code?: string | null): any {
  if (icon && WIDGET_ICON_MAP[icon]) return WIDGET_ICON_MAP[icon]
  const fallbackKey = code ? WIDGET_ICON_BY_CODE[code] : undefined
  if (fallbackKey && WIDGET_ICON_MAP[fallbackKey]) return WIDGET_ICON_MAP[fallbackKey]
  return WIDGET_ICON_MAP[DEFAULT_WIDGET_ICON]
}
