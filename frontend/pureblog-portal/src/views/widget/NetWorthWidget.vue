<template>
  <div class="networth-widget">
    <!-- 左栏: 录入表单 -->
    <div class="card form-panel">
      <div class="panel-header">
        <h3 class="panel-title">新增快照</h3>
        <span class="text-secondary panel-hint">{{ today }}</span>
      </div>

      <div class="section">
        <h3 class="section-title">快照日期</h3>
        <div class="meta-row">
          <el-date-picker
            v-model="snapshotAtDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="选择日期"
            class="date-picker"
          />
          <el-select
            v-model="eventTag"
            class="event-tag-select"
            placeholder="事件标签"
            allow-create
            filterable
            clearable
            maxlength="32"
          >
            <el-option v-for="t in EVENT_TAG_PRESETS" :key="t" :label="t" :value="t" />
          </el-select>
          <input v-model="remark" class="input" placeholder="备注 (可选)" maxlength="255" />
        </div>
      </div>

      <div class="section">
        <h3 class="section-title">12 个账户</h3>
        <div v-for="cat in categories" :key="cat.id" class="cat-group" :class="{ 'cat-group--zero': cat.isCountedInTotal === 0 }">
          <div class="cat-group-title">
            <span>{{ cat.name }}</span>
            <span class="cat-group-sum">¥ {{ catSum(cat.id) }}</span>
          </div>
          <div v-for="acc in accountsByCategory[cat.id] || []" :key="acc.id" class="acc-row">
            <span class="acc-name" :class="{ 'acc-name--zero': cat.isCountedInTotal === 0 }">{{ acc.name }}</span>
            <el-select
              v-model="formEntries[acc.id].categoryId"
              size="small"
              class="acc-cat-select"
            >
              <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
            </el-select>
            <input
              v-model="formEntries[acc.id].balance"
              class="input acc-balance"
              placeholder="0.00"
              inputmode="decimal"
            />
          </div>
        </div>
      </div>

      <div class="preview-row">
        <div class="preview-block">
          <div class="preview-label">目前完整身家</div>
          <div class="preview-value">¥ {{ grandTotal }}</div>
        </div>
        <div class="preview-block preview-block--sub">
          <div class="preview-label">0 余额 (不计)</div>
          <div class="preview-value preview-value--sub">¥ {{ zeroTotal }}</div>
        </div>
      </div>

      <button
        type="button"
        class="btn btn-primary submit-btn"
        :disabled="submitting"
        :aria-busy="submitting"
        @click="submit"
      >
        {{ submitting ? '保存中...' : '保存快照' }}
      </button>
    </div>

    <!-- 右栏: 时间线 -->
    <div class="card list-panel">
      <div class="panel-header">
        <h3 class="panel-title">时间线</h3>
        <span class="text-secondary">共 {{ snapshots.length }} 条</span>
      </div>
      <div v-if="loading" class="text-secondary empty-state">加载中...</div>
      <div v-else-if="!snapshots.length" class="text-secondary empty-state">还没有快照, 去左边加一条吧</div>
      <div v-else class="table-wrap">
        <table class="snap-table">
          <thead>
            <tr>
              <th>日期</th>
              <th>事件</th>
              <th class="num">活钱</th>
              <th class="num">定期</th>
              <th class="num dim">0 余额</th>
              <th class="num strong">目前完整身家</th>
              <th class="num">相较上次</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="s in snapshots" :key="s.id">
              <td>{{ formatDate(s.snapshotAt) }}</td>
              <td>
                <span v-if="s.eventTag" class="tag">{{ s.eventTag }}</span>
                <span v-else class="text-secondary">—</span>
              </td>
              <td class="num">{{ perCat(s, catCountedId, 0) }}</td>
              <td class="num">{{ perCat(s, catCountedId, 1) }}</td>
              <td class="num dim">{{ perCat(s, catZeroId, null) }}</td>
              <td class="num strong">{{ formatMoney(s.total) }}</td>
              <td class="num" :class="deltaClass(s.deltaToPrev)">
                {{ s.deltaToPrev == null ? '—' : formatDelta(s.deltaToPrev) }}
              </td>
              <td>
                <el-button text type="danger" size="small" @click="confirmDelete(s.id)">删</el-button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { netWorthApi, type NetWorthSnapshotVO, type AccountCategoryVO, type AccountVO } from '@/api/networth'
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'

const userStore = useUserStore()
const router = useRouter()

// 事件标签预设: 用户也可以在 el-select 里直接键入自定义值 (allow-create)
const EVENT_TAG_PRESETS = [
  '发薪',
  '20号',
  '月底',
  '季末',
  '年初',
  '年底',
  '理财赎回',
  '基金加仓',
  '大额支出',
  '奖金'
]

const today = new Date().toISOString().slice(0, 10)
const snapshotAtDate = ref(today)
const eventTag = ref('')
const remark = ref('')

const categories = ref<AccountCategoryVO[]>([])
const accounts = ref<AccountVO[]>([])
const snapshots = ref<NetWorthSnapshotVO[]>([])

interface FormEntry { categoryId: number | string; balance: string }
const formEntries = reactive<Record<string, FormEntry>>({})

const submitting = ref(false)
const loading = ref(false)

const accountsByCategory = computed(() => {
  // 默认按 category 字段展示, 但因为 account 表无 category 字段, 这里按 init seed 顺序展示
  // 实际我们用 formEntries 默认 category 推断展示分组
  const map: Record<string, AccountVO[]> = {}
  for (const acc of accounts.value) {
    const catId = formEntries[acc.id]?.categoryId ?? categories.value[0]?.id ?? 1
    if (!map[catId]) map[catId] = []
    map[catId].push(acc)
  }
  // 按 cat sort_order 排序 accounts
  for (const k of Object.keys(map)) {
    map[+k].sort((a, b) => a.sortOrder - b.sortOrder)
  }
  return map
})

const catCountedId = computed(() => categories.value.filter(c => c.isCountedInTotal === 1).map(c => c.id))
const catZeroId = computed(() => categories.value.filter(c => c.isCountedInTotal === 0).map(c => c.id))

function catSum(catId: number): string {
  let s = 0
  for (const acc of accounts.value) {
    if (formEntries[acc.id]?.categoryId === catId) {
      s += parseFloat(formEntries[acc.id].balance || '0') || 0
    }
  }
  return s.toFixed(2)
}

const grandTotal = computed(() => {
  let s = 0
  for (const acc of accounts.value) {
    const e = formEntries[acc.id]
    if (!e) continue
    const cat = categories.value.find(c => c.id === e.categoryId)
    if (cat?.isCountedInTotal === 1) s += parseFloat(e.balance || '0') || 0
  }
  return s.toFixed(2)
})

const zeroTotal = computed(() => {
  let s = 0
  for (const acc of accounts.value) {
    const e = formEntries[acc.id]
    if (!e) continue
    const cat = categories.value.find(c => c.id === e.categoryId)
    if (cat?.isCountedInTotal === 0) s += parseFloat(e.balance || '0') || 0
  }
  return s.toFixed(2)
})

function perCat(s: NetWorthSnapshotVO, ids: number[], idx: number | null): string {
  // 兼容 catCountedId (数组) 和 catZeroId (数组)
  if (idx == null) {
    let sum = 0
    for (const id of ids) {
      const ct = s.perCategoryTotals.find(p => p.categoryId === id)
      if (ct) sum += parseFloat(String(ct.total)) || 0
    }
    return sum.toFixed(2)
  } else {
    const id = ids[idx]
    const ct = s.perCategoryTotals.find(p => p.categoryId === id)
    return ct ? formatMoney(ct.total) : '0.00'
  }
}

function formatDate(s: string): string {
  return s ? s.slice(0, 10) : ''
}

function formatMoney(n: number | string | null | undefined): string {
  if (n == null) return '0.00'
  return Number(n).toFixed(2)
}

function formatDelta(n: number | string | null | undefined): string {
  if (n == null) return '—'
  const v = Number(n)
  if (v > 0) return '+' + v.toFixed(2)
  return v.toFixed(2)
}

function deltaClass(n: number | string | null | undefined): string {
  if (n == null) return ''
  const v = Number(n)
  if (v > 0) return 'pos'
  if (v < 0) return 'neg'
  return ''
}

async function loadAll() {
  loading.value = true
  try {
    const [cats, accs, snaps] = await Promise.all([
      netWorthApi.listCategories(),
      netWorthApi.listAccounts(),
      netWorthApi.listSnapshots()
    ])
    categories.value = cats.data || []
    accounts.value = (accs.data || []).filter(a => a.status === 1)
    snapshots.value = snaps.data || []
    // 初始化 formEntries, 默认 category = 上次 snapshot 的 category, 无则第一个 counted
    const lastByAccount: Record<string, number | string> = {}
    if (snapshots.value.length > 0) {
      // 这里简化: 如果有 snapshots, 我们用最新一条的 entries 拉一次
      try {
        const latest = snapshots.value[0]
        const entries = await netWorthApi.listEntries(latest.id)
        for (const e of entries.data || []) {
          lastByAccount[e.accountId] = e.categoryId
        }
      } catch { /* 忽略 */ }
    }
    const defaultCat = (categories.value.find(c => c.isCountedInTotal === 1) || categories.value[0])?.id || 1
    for (const acc of accounts.value) {
      formEntries[acc.id] = {
        categoryId: lastByAccount[acc.id] ?? defaultCat,
        balance: ''
      }
    }
  } catch (err: any) {
    ElMessage.error(err?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

async function submit() {
  console.log('[networth] submit clicked', {
    snapshotAt: snapshotAtDate.value,
    accounts: accounts.value.length,
    entries: accounts.value.length
  })
  if (!snapshotAtDate.value) {
    ElMessage.error('请选择日期')
    return
  }
  if (accounts.value.length === 0) {
    ElMessage.error('账户列表还没加载, 请稍后再试')
    return
  }
  const entries = accounts.value
    .map(acc => ({
      accountId: acc.id,
      categoryId: formEntries[acc.id].categoryId,
      balance: formEntries[acc.id].balance || '0'
    }))
    .filter(e => parseFloat(e.balance) !== 0 || true) // 保留 0 余额, 用户可能想记
  submitting.value = true
  try {
    await netWorthApi.saveSnapshot({
      snapshotAt: snapshotAtDate.value + 'T12:00:00',
      eventTag: eventTag.value.trim() || undefined,
      remark: remark.value.trim() || undefined,
      entries
    })
    ElMessage.success('已保存')
    eventTag.value = ''
    remark.value = ''
    for (const acc of accounts.value) formEntries[acc.id].balance = ''
    await loadAll()
  } catch (err: any) {
    console.error('[networth] save failed', err)
    ElMessage.error(err?.message || err?.msg || '保存失败, 请看控制台')
  } finally {
    submitting.value = false
  }
}

async function confirmDelete(id: number | string) {
  try {
    await ElMessageBox.confirm('确定要删除这条快照吗? (会级联删除明细)', '确认', { type: 'warning' })
    await netWorthApi.deleteSnapshot(id)
    ElMessage.success('已删除')
    await loadAll()
  } catch (err: any) {
    if (err !== 'cancel' && err?.message) ElMessage.error(err.message)
  }
}

onMounted(() => {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  loadAll()
})
</script>

<style scoped>
.networth-widget {
  width: 100%;
  display: grid;
  grid-template-columns: minmax(420px, 480px) 1fr;
  gap: 20px;
  align-items: stretch;
}

.form-panel, .list-panel { display: flex; flex-direction: column; min-width: 0; }

.panel-header {
  display: flex; align-items: baseline; justify-content: space-between;
  margin-bottom: 16px; padding-bottom: 12px;
  border-bottom: 2px solid var(--primary);
}
.panel-title { font-size: 16px; font-weight: 600; }
.panel-hint { font-size: 12px; }

.section { margin-bottom: 18px; }
.section-title {
  font-size: 13px; font-weight: 600; margin-bottom: 10px;
  display: flex; align-items: center; gap: 8px;
}

/* 日期 + 事件标签 + 备注 三个并排 */
.meta-row {
  display: grid;
  grid-template-columns: 1.1fr 1fr 1.4fr;
  gap: 8px;
  align-items: center;
}
.meta-row .input { margin-top: 0; }
.meta-row :deep(.el-select__wrapper),
.meta-row :deep(.el-date-editor) {
  width: 100%;
}
.date-picker { width: 100%; }
.input { width: 100%; margin-top: 6px; }

.cat-group { margin-bottom: 12px; }
.cat-group--zero { opacity: 0.55; }
.cat-group-title {
  display: flex; justify-content: space-between; align-items: center;
  padding: 6px 10px;
  background: var(--bg-secondary);
  border-radius: 6px;
  font-size: 12px; font-weight: 600;
  margin-bottom: 4px;
}
.cat-group--zero .cat-group-title { font-style: italic; }
.cat-group-sum { font-variant-numeric: tabular-nums; color: var(--text-secondary); }
.cat-group--zero .cat-group-sum { text-decoration: line-through; }

.acc-row {
  display: grid;
  grid-template-columns: 1fr 100px 90px;
  gap: 6px; align-items: center;
  padding: 4px 6px;
  border-radius: 4px;
  font-size: 13px;
}
.acc-row:hover { background: var(--bg-secondary); }
.acc-name { color: var(--text-primary); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.acc-name--zero { color: var(--text-secondary); }
.acc-cat-select { width: 100%; }
.acc-balance {
  text-align: right; font-variant-numeric: tabular-nums;
  padding: 4px 8px; border: 1px solid var(--border); border-radius: 4px;
  font-size: 13px;
}
.acc-balance:focus { border-color: var(--primary); outline: none; }

.preview-row {
  display: grid; grid-template-columns: 2fr 1fr; gap: 8px;
  margin: 16px 0 12px;
}
.preview-block {
  background: var(--bg-secondary);
  padding: 12px;
  border-radius: 8px;
  text-align: center;
}
.preview-label { font-size: 12px; color: var(--text-secondary); margin-bottom: 4px; }
.preview-value {
  font-size: 22px; font-weight: 700; color: var(--primary);
  font-variant-numeric: tabular-nums;
}
.preview-value--sub { font-size: 14px; color: var(--text-secondary); }

.submit-btn { width: 100%; height: 42px; font-size: 15px; }
.submit-btn:disabled { opacity: 0.5; cursor: not-allowed; }

/* 右栏 */
.list-panel { padding-bottom: 16px; }
.empty-state {
  flex: 1; min-height: 200px;
  display: flex; align-items: center; justify-content: center;
  font-size: 14px;
}
.table-wrap {
  flex: 1; overflow: auto;
  margin: 0 -24px -16px; padding: 0 24px 16px;
  max-height: 70vh;
}
.snap-table {
  width: 100%; border-collapse: collapse;
  font-size: 13px;
}
.snap-table th, .snap-table td {
  padding: 8px 6px; text-align: left;
  border-bottom: 1px solid var(--border);
}
.snap-table th { background: var(--bg-secondary); font-weight: 600; position: sticky; top: 0; }
.snap-table td.num, .snap-table th.num { text-align: right; font-variant-numeric: tabular-nums; }
.snap-table td.strong { font-weight: 600; color: var(--primary); }
.snap-table td.dim { color: var(--text-secondary); }
.snap-table td.pos { color: #10b981; }
.snap-table td.neg { color: #ef4444; }

.tag {
  display: inline-block;
  padding: 1px 6px;
  font-size: 11px;
  background: var(--primary);
  color: white;
  border-radius: 8px;
}

@media (max-width: 900px) {
  .networth-widget { grid-template-columns: 1fr; }
  .acc-row { grid-template-columns: 1fr 90px 80px; }
}
</style>
