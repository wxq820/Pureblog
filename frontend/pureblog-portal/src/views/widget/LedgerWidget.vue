<template>
  <div class="ledger-widget">
    <!-- 左栏: 加一笔表单 -->
    <div class="card form-panel">
      <div class="panel-header">
        <h3 class="panel-title">记一笔</h3>
        <span class="panel-hint text-secondary"></span>
      </div>

      <!-- 1. 时间 -->
      <div class="section">
        <h3 class="section-title">时间</h3>
        <el-date-picker
          v-model="recordDate"
          type="date"
          value-format="YYYY-MM-DD"
          placeholder="选择日期"
          class="date-picker"
        />
      </div>

      <!-- 2. 平台 -->
      <div class="section">
        <h3 class="section-title">平台</h3>
        <div class="chip-grid">
          <div
            v-for="p in platforms"
            :key="p.id"
            :class="['chip', { active: platformDictId === p.id }]"
            :style="chipStyle(p.color, platformDictId === p.id)"
            @click="platformDictId = p.id"
          >
            <el-icon class="chip-icon"><component :is="platformIconComp(p.id)" /></el-icon>
            <span>{{ p.name }}</span>
          </div>
        </div>
      </div>

      <!-- 3. 项目 -->
      <div class="section">
        <h3 class="section-title">
          项目
          <button class="add-btn" @click="openAddDialog" title="添加新项目">+</button>
        </h3>
        <div class="chip-grid">
          <div
            v-for="i in items"
            :key="i.id"
            :class="['chip', { active: itemDictId === i.id && !itemText }]"
            :style="chipStyle(i.color, itemDictId === i.id && !itemText)"
            @click="selectItem(i)"
          >
            <el-icon v-if="i.icon" class="chip-icon"><component :is="itemIconComp(i.icon)" /></el-icon>
            <span>{{ i.name }}</span>
          </div>
        </div>
        <input
          v-model="itemText"
          class="input custom-item"
          placeholder="或手动输入项目, 如: 5包方便面"
          @input="onItemTextInput"
        />
      </div>

      <!-- 4. 金额 -->
      <div class="section">
        <h3 class="section-title">金额</h3>
        <div class="amount-display">{{ amount || '0.00' }}</div>
        <div class="keypad">
          <button v-for="k in ['7','8','9']" :key="k" class="key" @click="press(k)">{{ k }}</button>
          <button v-for="k in ['4','5','6']" :key="k" class="key" @click="press(k)">{{ k }}</button>
          <button v-for="k in ['1','2','3']" :key="k" class="key" @click="press(k)">{{ k }}</button>
          <button class="key" @click="press('.')">.</button>
          <button class="key" @click="press('0')">0</button>
          <button class="key key-del" @click="backspace">⌫</button>
        </div>
      </div>

      <!-- 5. 提交 -->
      <button
        class="btn btn-primary submit-btn"
        :disabled="!canSubmit"
        @click="submit"
      >记一笔</button>
    </div>

    <!-- 右栏: 历史记录 -->
    <div class="card records-panel">
      <div class="panel-header">
        <h3 class="panel-title">我的记账</h3>
        <span class="text-secondary records-summary">
          共 {{ filteredRecords.length }} 笔
        </span>
      </div>

      <!-- 筛选行: 一行, 单日 + 平台/项目下拉 -->
      <div class="filter-row">
        <el-date-picker
          v-model="filterDate"
          type="date"
          value-format="YYYY-MM-DD"
          placeholder="选择日期"
          class="filter-date"
        />
        <el-select
          v-model="filterPlatformIds"
          multiple
          collapse-tags
          collapse-tags-tooltip
          placeholder="平台"
          class="filter-select"
        >
          <el-option v-for="p in platforms" :key="p.id" :label="p.name" :value="p.id" />
        </el-select>
        <el-select
          v-model="filterItemIds"
          multiple
          collapse-tags
          collapse-tags-tooltip
          placeholder="项目"
          class="filter-select"
        >
          <el-option v-for="i in items" :key="i.id" :label="i.name" :value="i.id" />
        </el-select>
        <button class="btn btn-outline filter-reset" @click="filterReset">重置</button>
      </div>

      <!-- 3 张统计卡片 -->
      <div class="stats-row">
        <div class="stat-card">
          <div class="stat-label">本日合计</div>
          <div class="stat-value">¥{{ todayTotal.toFixed(2) }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">本月合计</div>
          <div class="stat-value">¥{{ monthTotal.toFixed(2) }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">本年合计</div>
          <div class="stat-value">¥{{ yearTotal.toFixed(2) }}</div>
        </div>
      </div>

      <!-- 列表 -->
      <div v-if="recordsLoading && !records.length" class="records-loading text-secondary">
        加载中...
      </div>
      <div v-else-if="!filteredRecords.length" class="records-empty text-secondary">
        暂无记录, 去左边加一笔吧
      </div>
      <div v-else class="records-list">
        <template v-for="y in recordTree" :key="y.year">
          <div
            v-if="y.months.length || true"
            class="group-header year-header"
            :class="{ expanded: isYearOpen(y.year) }"
            @click="toggleYear(y.year)"
          >
            <el-icon class="arrow">
              <component :is="isYearOpen(y.year) ? ArrowDown : ArrowRight" />
            </el-icon>
            <span class="group-label">{{ y.year }} 年</span>
            <span class="text-secondary group-meta">
              {{ y.recordCount }} 笔, 合计
              <span class="amount-strong">¥{{ y.total.toFixed(2) }}</span>
            </span>
          </div>

          <template v-if="isYearOpen(y.year)">
            <template v-for="m in y.months" :key="y.year + '-' + m.month">
              <div
                class="group-header month-header"
                :class="{ expanded: isMonthOpen(y.year, m.month) }"
                @click="toggleMonth(y.year, m.month)"
              >
                <el-icon class="arrow">
                  <component :is="isMonthOpen(y.year, m.month) ? ArrowDown : ArrowRight" />
                </el-icon>
                <span class="group-label">{{ m.month }} 月</span>
                <span class="text-secondary group-meta">
                  {{ m.recordCount }} 笔, 合计
                  <span class="amount-strong">¥{{ m.total.toFixed(2) }}</span>
                </span>
              </div>

              <template v-if="isMonthOpen(y.year, m.month)">
                <template v-for="d in m.days" :key="d.date">
                  <div
                    class="group-header day-header"
                    :class="{ expanded: isDayOpen(d.date) }"
                    @click="toggleDay(d.date)"
                  >
                    <el-icon class="arrow">
                      <component :is="isDayOpen(d.date) ? ArrowDown : ArrowRight" />
                    </el-icon>
                    <span class="group-label">{{ formatDateShort(d.date) }} {{ weekdayCN(d.date) }}</span>
                    <span class="text-secondary group-meta">
                      {{ d.items.length }} 笔, 合计
                      <span class="amount-strong">¥{{ d.total.toFixed(2) }}</span>
                    </span>
                  </div>

                  <template v-if="isDayOpen(d.date)">
                    <div
                      v-for="r in d.items"
                      :key="r.id"
                      class="record-item"
                    >
                      <span
                        class="record-platform"
                        :style="r.platformColor
                          ? { color: r.platformColor, borderColor: r.platformColor }
                          : { color: '#6b7280', borderColor: '#6b7280' }"
                      >
                        <el-icon style="vertical-align: -2px; margin-right: 2px;">
                          <component :is="platformIconComp(r.platformDictId)" />
                        </el-icon>
                        {{ r.platformName }}
                      </span>
                      <span class="record-divider">·</span>
                      <span
                        class="record-item-name"
                        :style="r.itemColor ? { color: r.itemColor } : null"
                      >
                        <el-icon
                          v-if="itemIconComp(r.itemIcon)"
                          style="vertical-align: -2px; margin-right: 2px;"
                        >
                          <component :is="itemIconComp(r.itemIcon)" />
                        </el-icon>
                        {{ r.itemName }}
                      </span>
                      <span class="record-amount">¥{{ Number(r.amount).toFixed(2) }}</span>
                    </div>
                  </template>
                </template>
              </template>
            </template>
          </template>
        </template>
      </div>
    </div>

    <!-- 添加项目 dialog -->
    <el-dialog v-model="addDialogVisible" title="添加新项目" width="440px">
      <el-form :model="newItem" label-width="60px">
        <el-form-item label="名称">
          <el-input v-model="newItem.name" placeholder="如: 牛奶" maxlength="32" />
        </el-form-item>
        <el-form-item label="图标">
          <div class="icon-grid">
            <div
              v-for="opt in ITEM_ICON_OPTIONS"
              :key="opt.key"
              :class="['icon-cell', { active: newItem.icon === opt.key }]"
              :style="{
                background: newItem.icon === opt.key ? opt.color : '#fff',
                borderColor: newItem.icon === opt.key ? opt.color : 'var(--border)',
                color: newItem.icon === opt.key ? '#fff' : opt.color
              }"
              @click="newItem.icon = opt.key; newItem.color = opt.color"
              :title="opt.label"
            >
              <el-icon><component :is="opt.comp" /></el-icon>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="颜色">
          <el-color-picker v-model="newItem.color" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="adding" @click="confirmAdd">添加</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { ArrowDown, ArrowRight } from '@element-plus/icons-vue'
import { ledgerApi } from '@/api/ledger'
import { getDeviceId } from '@/utils/device'
import type { LedgerDictVO, LedgerRecordVO } from '@/types'
import {
  PLATFORM_ICONS,
  ITEM_ICON_OPTIONS,
  ITEM_ICON_MAP
} from '@/constants/ledgerIcons'

const today = new Date()
const recordDate = ref<string>(formatDate(today))

function formatDate(d: Date): string {
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const dd = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${dd}`
}

function formatDateShort(s: string): string {
  // s = 'YYYY-MM-DD' -> 'MM-DD'
  return s.slice(5)
}

function weekdayCN(s: string): string {
  const [y, m, d] = s.split('-').map(Number)
  const dt = new Date(y, m - 1, d)
  const days = ['日', '一', '二', '三', '四', '五', '六']
  return `周${days[dt.getDay()]}`
}

const platforms = ref<LedgerDictVO[]>([])
const items = ref<LedgerDictVO[]>([])

const platformDictId = ref<number | null>(null)
const itemDictId = ref<number | null>(null)
const itemText = ref<string>('')
const amount = ref<string>('')

const addDialogVisible = ref(false)
const adding = ref(false)
const newItem = ref({ name: '', color: '#6b7280', icon: 'Dish' })

function openAddDialog() {
  newItem.value = { name: '', color: '#6b7280', icon: 'Dish' }
  addDialogVisible.value = true
}

// 右栏数据
const records = ref<LedgerRecordVO[]>([])
const recordsLoading = ref(false)

// 筛选: 单行, 单日 + 平台/项目下拉
const filterDate = ref<string | null>(null)
const filterPlatformIds = ref<number[]>([])
const filterItemIds = ref<number[]>([])

function filterReset() {
  filterDate.value = null
  filterPlatformIds.value = []
  filterItemIds.value = []
}

const filteredRecords = computed<LedgerRecordVO[]>(() => {
  return records.value.filter(r => {
    if (filterDate.value && r.recordDate !== filterDate.value) return false
    if (filterPlatformIds.value.length && !filterPlatformIds.value.includes(r.platformDictId)) return false
    if (filterItemIds.value.length) {
      // record 里没有 itemDictId, 用 itemName 匹配用户选中的字典项
      const names = items.value.filter(i => filterItemIds.value.includes(i.id)).map(i => i.name)
      if (!names.includes(r.itemName)) return false
    }
    return true
  })
})

// 统计
const todayStr = formatDate(today)
const monthStr = todayStr.slice(0, 7)
const yearStr = todayStr.slice(0, 4)

const todayTotal = computed(() =>
  filteredRecords.value
    .filter(r => r.recordDate === todayStr)
    .reduce((s, r) => s + (Number(r.amount) || 0), 0)
)
const monthTotal = computed(() =>
  filteredRecords.value
    .filter(r => r.recordDate.startsWith(monthStr))
    .reduce((s, r) => s + (Number(r.amount) || 0), 0)
)
const yearTotal = computed(() =>
  filteredRecords.value
    .filter(r => r.recordDate.startsWith(yearStr))
    .reduce((s, r) => s + (Number(r.amount) || 0), 0)
)

// 三级 group
interface DayGroup { date: string; items: LedgerRecordVO[]; total: number }
interface MonthGroup { month: string; days: DayGroup[]; total: number; recordCount: number }
interface YearGroup { year: string; months: MonthGroup[]; total: number; recordCount: number }

const recordTree = computed<YearGroup[]>(() => {
  const years = new Map<string, Map<string, Map<string, LedgerRecordVO[]>>>()
  for (const r of filteredRecords.value) {
    const [y, m] = [r.recordDate.slice(0, 4), r.recordDate.slice(5, 7)]
    if (!years.has(y)) years.set(y, new Map())
    const ym = years.get(y)!
    if (!ym.has(m)) ym.set(m, new Map())
    const md = ym.get(m)!
    if (!md.has(r.recordDate)) md.set(r.recordDate, [])
    md.get(r.recordDate)!.push(r)
  }
  const tree: YearGroup[] = []
  for (const [y, ym] of years) {
    const months: MonthGroup[] = []
    let yTotal = 0, yCount = 0
    for (const [m, md] of ym) {
      const days: DayGroup[] = []
      let mTotal = 0
      for (const [d, items] of md) {
        const t = items.reduce((s, r) => s + (Number(r.amount) || 0), 0)
        days.push({ date: d, items, total: t })
        mTotal += t
      }
      days.sort((a, b) => b.date.localeCompare(a.date))
      months.push({ month: m, days, total: mTotal, recordCount: days.reduce((s, d) => s + d.items.length, 0) })
      yTotal += mTotal
      yCount += months[months.length - 1].recordCount
    }
    months.sort((a, b) => b.month.localeCompare(a.month))
    tree.push({ year: y, months, total: yTotal, recordCount: yCount })
  }
  tree.sort((a, b) => b.year.localeCompare(a.year))
  return tree
})

// 折叠状态
const expanded = ref<Set<string>>(new Set([
  `year:${todayStr.slice(0, 4)}`,
  `month:${todayStr.slice(0, 7)}`,
  `day:${todayStr}`
]))

function isYearOpen(y: string) { return expanded.value.has(`year:${y}`) }
function isMonthOpen(y: string, m: string) { return expanded.value.has(`month:${y}-${m}`) }
function isDayOpen(d: string) { return expanded.value.has(`day:${d}`) }

function toggleYear(y: string) { toggle(`year:${y}`) }
function toggleMonth(y: string, m: string) { toggle(`month:${y}-${m}`) }
function toggleDay(d: string) { toggle(`day:${d}`) }

function toggle(key: string) {
  const s = new Set(expanded.value)
  s.has(key) ? s.delete(key) : s.add(key)
  expanded.value = s
}

const canSubmit = computed(() => {
  if (!platformDictId.value) return false
  if (!itemDictId.value && !itemText.value.trim()) return false
  const n = parseFloat(amount.value)
  return !isNaN(n) && n > 0
})

// 图标解析
function platformIconComp(id: number) {
  return PLATFORM_ICONS[id] || null
}
function itemIconComp(key?: string | null) {
  if (!key) return null
  const opt = ITEM_ICON_MAP[key]
  return opt ? opt.comp : null
}

function chipStyle(color: string | undefined, active: boolean) {
  const c = color || '#6b7280'
  if (active) {
    return { backgroundColor: c, color: 'white', borderColor: c }
  }
  return { borderColor: c, color: c }
}

function selectItem(i: LedgerDictVO) {
  itemDictId.value = i.id
  itemText.value = ''
}

function onItemTextInput() {
  if (itemText.value) itemDictId.value = null
}

function press(k: string) {
  if (k === '.' && amount.value.includes('.')) return
  if (amount.value === '0' && k !== '.') amount.value = ''
  if (amount.value.includes('.')) {
    const decimals = amount.value.split('.')[1]
    if (decimals && decimals.length >= 2) return
  }
  if (amount.value.length >= 10) return
  amount.value += k
}

function backspace() {
  amount.value = amount.value.slice(0, -1)
}

async function loadDicts() {
  const [p, i] = await Promise.all([
    ledgerApi.getDict(1),
    ledgerApi.getDict(2)
  ])
  platforms.value = p.data
  items.value = i.data
}

async function loadRecords() {
  recordsLoading.value = true
  try {
    const res = await ledgerApi.listRecords(getDeviceId(), 200)
    records.value = res.data || []
  } catch {
    records.value = []
  } finally {
    recordsLoading.value = false
  }
}

async function confirmAdd() {
  if (!newItem.value.name.trim()) {
    ElMessage.error('请输入名称')
    return
  }
  adding.value = true
  try {
    const res = await ledgerApi.addDict({
      type: 2,
      name: newItem.value.name.trim(),
      color: newItem.value.color,
      icon: newItem.value.icon
    })
    items.value.push(res.data)
    itemDictId.value = res.data.id
    itemText.value = ''
    addDialogVisible.value = false
    ElMessage.success('已添加')
  } catch (err: any) {
    ElMessage.error(err?.message || '添加失败')
  } finally {
    adding.value = false
  }
}

function buildOptimisticRecord(): LedgerRecordVO | null {
  if (!platformDictId.value) return null
  const platform = platforms.value.find(p => p.id === platformDictId.value)
  if (!platform) return null
  const n = parseFloat(amount.value)
  if (isNaN(n) || n <= 0) return null
  if (!itemDictId.value && !itemText.value.trim()) return null

  const item = itemDictId.value
    ? items.value.find(i => i.id === itemDictId.value)
    : null
  return {
    id: -Date.now(),
    recordDate: recordDate.value,
    platformDictId: platform.id,
    platformName: platform.name,
    platformColor: platform.color,
    platformIcon: platform.icon,
    itemName: item?.name || itemText.value.trim(),
    itemColor: item?.color,
    itemIcon: item?.icon,
    amount: n,
    createdAt: new Date().toISOString()
  }
}

async function submit() {
  if (!canSubmit.value) return
  const optimistic = buildOptimisticRecord()
  try {
    await ledgerApi.addRecord({
      recordDate: recordDate.value,
      platformDictId: platformDictId.value!,
      itemDictId: itemText.value.trim() ? null : itemDictId.value,
      itemText: itemText.value.trim() || undefined,
      amount: parseFloat(amount.value),
      deviceId: getDeviceId()
    })
    if (optimistic) {
      records.value.unshift(optimistic)
      // 强制展开今天所在路径
      expanded.value = new Set([
        ...expanded.value,
        `year:${optimistic.recordDate.slice(0, 4)}`,
        `month:${optimistic.recordDate.slice(0, 7)}`,
        `day:${optimistic.recordDate}`
      ])
    }
    ElMessage.success('已记一笔')
    amount.value = ''
    itemText.value = ''
    loadRecords()
  } catch (err: any) {
    ElMessage.error(err?.message || '记账失败')
  }
}

onMounted(() => {
  loadDicts()
  loadRecords()
})
</script>

<style scoped>
.ledger-widget {
  width: 100%;
  display: grid;
  grid-template-columns: minmax(380px, 440px) 1fr;
  gap: 20px;
  align-items: stretch;
}

.form-panel, .records-panel {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.panel-header {
  display: flex; align-items: baseline; justify-content: space-between;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 2px solid var(--primary);
}
.panel-title {
  font-size: 16px; font-weight: 600; color: var(--text-primary);
}
.panel-hint { font-size: 12px; }

.section { margin-bottom: 18px; }
.section-title {
  font-size: 13px; font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 10px;
  display: flex; align-items: center; gap: 8px;
}
.add-btn {
  width: 22px; height: 22px; border-radius: 11px; border: 1px dashed var(--border);
  background: transparent; cursor: pointer; font-size: 14px; color: var(--text-secondary);
  display: flex; align-items: center; justify-content: center;
}
.add-btn:hover { color: var(--primary); border-color: var(--primary); }

.date-picker { width: 100%; }

.chip-grid { display: flex; flex-wrap: wrap; gap: 6px; }
.chip {
  display: inline-flex; align-items: center; gap: 4px;
  padding: 6px 10px; border-radius: 16px;
  border: 1.5px solid; cursor: pointer; font-size: 13px;
  transition: all 0.15s; background: white;
}
.chip:hover { transform: translateY(-1px); }
.chip-icon { font-size: 14px; }
.chip-sm { padding: 4px 8px; font-size: 12px; }
.chip-sm .chip-icon { font-size: 12px; }

.custom-item { width: 100%; margin-top: 6px; font-size: 13px; }

.amount-display {
  font-size: 26px; font-weight: 600; text-align: right;
  padding: 8px 14px; background: var(--bg-secondary);
  border-radius: 6px; font-variant-numeric: tabular-nums;
  letter-spacing: 1px; color: var(--primary);
  min-height: 44px;
}

.keypad {
  display: grid; grid-template-columns: repeat(3, 1fr);
  gap: 6px; margin-top: 8px;
}
.key {
  height: 48px; font-size: 20px; font-weight: 500;
  border: 1px solid var(--border); background: white; border-radius: 6px;
  cursor: pointer; transition: all 0.1s;
}
.key:hover { background: var(--bg-secondary); }
.key:active { transform: scale(0.96); }
.key-del { color: #ef4444; }

.submit-btn { width: 100%; height: 42px; font-size: 15px; margin-top: 4px; }
.submit-btn:disabled { opacity: 0.5; cursor: not-allowed; }

/* ----- 右栏 ----- */
.records-panel { padding-bottom: 16px; }
.records-summary { font-size: 13px; }
.amount-strong { color: var(--primary); font-weight: 600; font-variant-numeric: tabular-nums; }

.filter-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
  margin-bottom: 12px;
}
.filter-date { width: 150px; flex-shrink: 0; }
.filter-select { width: 150px; flex-shrink: 0; }
.filter-reset { font-size: 12px; padding: 4px 10px; margin-left: auto; }

.stats-row {
  display: grid; grid-template-columns: repeat(3, 1fr);
  gap: 10px; margin-bottom: 14px;
}
.stat-card {
  padding: 10px 12px;
  background: var(--bg-secondary);
  border-radius: 6px;
  text-align: center;
}
.stat-label { font-size: 12px; color: var(--text-secondary); margin-bottom: 4px; }
.stat-value {
  font-size: 16px; font-weight: 600; color: var(--primary);
  font-variant-numeric: tabular-nums;
}

.records-list {
  flex: 1;
  overflow-y: auto;
  margin: 0 -24px -16px;
  padding: 0 24px 16px;
  max-height: 60vh;
  min-height: 200px;
}

.group-header {
  display: flex; align-items: center; gap: 8px;
  padding: 8px 4px;
  cursor: pointer;
  user-select: none;
  border-radius: 4px;
  transition: background 0.1s;
}
.group-header:hover { background: var(--bg-secondary); }
.arrow { font-size: 14px; color: var(--text-secondary); flex-shrink: 0; }
.group-label { font-weight: 600; color: var(--text-primary); }
.group-meta { font-size: 12px; margin-left: auto; }
.group-meta .amount-strong { font-size: 12px; }

.year-header { font-size: 14px; padding-top: 12px; border-top: 1px solid var(--border); }
.year-header:first-child { border-top: none; }
.year-header .group-label { font-size: 15px; }
.month-header { font-size: 13px; padding-left: 18px; }
.month-header .group-label { font-size: 14px; }
.day-header { font-size: 12px; padding-left: 36px; color: var(--text-secondary); }
.day-header .group-label { font-size: 13px; }

.record-item {
  display: flex; align-items: center; gap: 8px;
  padding: 8px 8px 8px 52px;
  border-radius: 4px;
  font-size: 13px;
  transition: background 0.1s;
}
.record-item:hover { background: var(--bg-secondary); }
.record-platform {
  font-size: 12px;
  padding: 1px 8px;
  border: 1px solid;
  border-radius: 10px;
  flex-shrink: 0;
  white-space: nowrap;
}
.record-divider { color: var(--text-secondary); }
.record-item-name {
  flex: 1; min-width: 0;
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}
.record-amount {
  font-size: 14px; font-weight: 600; color: var(--primary);
  font-variant-numeric: tabular-nums;
  flex-shrink: 0;
  text-align: right;
  min-width: 80px;
}

.records-loading, .records-empty {
  flex: 1;
  display: flex; align-items: center; justify-content: center;
  font-size: 14px;
  min-height: 200px;
}

/* 图标选择网格 */
.icon-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(38px, 1fr));
  gap: 8px;
  width: 100%;
  max-height: 200px;
  overflow-y: auto;
  padding: 4px;
  border: 1px solid var(--border);
  border-radius: 6px;
}
.icon-cell {
  width: 38px; height: 38px;
  border: 1.5px solid;
  border-radius: 8px;
  display: flex; align-items: center; justify-content: center;
  cursor: pointer;
  font-size: 18px;
  transition: all 0.1s;
}
.icon-cell:hover { transform: scale(1.05); }
.icon-cell.active { font-weight: 600; }

@media (max-width: 900px) {
  .ledger-widget { grid-template-columns: 1fr; }
  .filter-date { width: 100%; }
  .records-list { max-height: none; }
}
</style>
