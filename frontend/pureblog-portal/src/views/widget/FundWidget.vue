<template>
  <div class="fund-widget">
    <div class="actions-row">
      <el-button type="primary" size="small" @click="openNewFund">+ 新建基金</el-button>
    </div>

    <div v-if="loading" class="text-secondary empty-state">加载中...</div>
    <div v-else-if="!funds.length" class="text-secondary empty-state">还没有基金, 点右上角加一个</div>
    <div v-else class="fund-list">
      <div v-for="f in funds" :key="f.id" class="card fund-card">
        <div class="fund-header">
          <div class="fund-title">
            <h3 class="fund-name">{{ f.name }}</h3>
            <span v-if="f.code" class="fund-code text-secondary">{{ f.code }}</span>
            <span v-if="f.type" class="fund-type">{{ f.type }}</span>
          </div>
          <div class="fund-actions">
            <el-button text type="primary" size="small" @click="openEditFund(f)">编辑</el-button>
            <el-button text type="danger" size="small" @click="confirmDeleteFund(f.id)">删除</el-button>
          </div>
        </div>

        <div class="fund-grid">
          <div class="kv">
            <span class="kv-label">最新净值</span>
            <span class="kv-value">
              <input
                v-model="navInputs[f.id]"
                @keyup.enter="onNavChange(f)"
                @blur="onNavChange(f)"
                class="kv-input"
                inputmode="decimal"
                placeholder="填入回车"
              />
            </span>
          </div>
          <div class="kv">
            <span class="kv-label">累计份额</span>
            <span class="kv-value">{{ formatMoney(f.totalShares) }}</span>
          </div>
          <div class="kv">
            <span class="kv-label">累计成本</span>
            <span class="kv-value">¥ {{ formatMoney(f.totalCost) }}</span>
          </div>
          <div class="kv">
            <span class="kv-label">当前市值</span>
            <span class="kv-value">¥ {{ formatMoney(f.marketValue) }}</span>
          </div>
          <div class="kv kv--span2">
            <span class="kv-label">盈亏</span>
            <span class="kv-value" :class="pnlClass(f.pnl)">
              {{ formatDelta(f.pnl) }}
              <span v-if="f.pnlRate != null" class="kv-rate">
                ({{ formatDelta(f.pnlRate) }}%)
              </span>
            </span>
          </div>
        </div>

        <div v-if="f.remark" class="fund-remark text-secondary">{{ f.remark }}</div>

        <!-- 买入明细 -->
        <div class="buys-section">
          <div class="buys-header">
            <span>买入明细 ({{ f.buys.length }})</span>
            <el-button text type="primary" size="small" @click="openNewBuy(f.id)">+ 加一行</el-button>
          </div>
          <div v-if="!f.buys.length" class="text-secondary buys-empty">还没有买入记录</div>
          <div v-else class="buys-table-wrap">
            <table class="buys-table">
              <thead>
                <tr>
                  <th>日期</th>
                  <th class="num">单价</th>
                  <th class="num">份额</th>
                  <th class="num">金额</th>
                  <th>备注</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="b in f.buys" :key="b.id">
                  <td>{{ b.buyDate }}</td>
                  <td class="num">{{ formatMoney(b.price) }}</td>
                  <td class="num">{{ formatMoney(b.shares) }}</td>
                  <td class="num strong">¥ {{ formatMoney(b.amount) }}</td>
                  <td class="text-secondary text-small">{{ b.remark || '—' }}</td>
                  <td>
                    <el-button text type="danger" size="small" @click="confirmDeleteBuy(b.id)">删</el-button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>

    <!-- 基金 dialog -->
    <el-dialog
      v-model="fundDialogVisible"
      :title="fundForm.id ? '编辑基金' : '新建基金'"
      width="440px"
    >
      <el-form :model="fundForm" label-width="80px">
        <el-form-item label="名称">
          <el-input v-model="fundForm.name" maxlength="64" />
        </el-form-item>
        <el-form-item label="代码">
          <el-input v-model="fundForm.code" maxlength="32" placeholder="可选" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="fundForm.type" placeholder="可选" clearable>
            <el-option label="基金" value="基金" />
            <el-option label="股票" value="股票" />
            <el-option label="ETF" value="ETF" />
            <el-option label="债券" value="债券" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="最新净值">
          <el-input
            v-model="fundForm.latestNav"
            type="number"
            step="0.0001"
            placeholder="可选, 填了会在保存时多记一条净值"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="fundForm.remark" type="textarea" :rows="2" maxlength="255" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="fundDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="savingFund" @click="saveFund">保存</el-button>
      </template>
    </el-dialog>

    <!-- 买入 dialog -->
    <el-dialog v-model="buyDialogVisible" title="新增买入" width="440px">
      <el-form :model="buyForm" label-width="80px">
        <el-form-item label="买入日期">
          <el-date-picker
            v-model="buyForm.buyDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="选择日期"
            class="date-picker"
          />
        </el-form-item>
        <el-form-item label="单价">
          <el-input v-model="buyForm.price" type="number" step="0.0001" />
        </el-form-item>
        <el-form-item label="份额">
          <el-input v-model="buyForm.shares" type="number" step="0.01" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="buyForm.remark" maxlength="255" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="buyDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="savingBuy" @click="saveBuy">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { fundApi, type FundVO } from '@/api/fund'
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'

const userStore = useUserStore()
const router = useRouter()

const funds = ref<FundVO[]>([])
const loading = ref(false)
const navInputs = ref<Record<string, string>>({})

const fundDialogVisible = ref(false)
const savingFund = ref(false)
const fundForm = ref({
  id: undefined as number | undefined,
  name: '',
  code: '',
  type: '',
  remark: '',
  latestNav: ''
})

const buyDialogVisible = ref(false)
const savingBuy = ref(false)
const buyForm = ref({
  fundId: 0,
  buyDate: new Date().toISOString().slice(0, 10),
  price: '0',
  shares: '0',
  remark: ''
})

const totalCostAll = computed(() => funds.value.reduce((s, f) => s + (Number(f.totalCost) || 0), 0))
const totalValueAll = computed(() => funds.value.reduce((s, f) => s + (Number(f.marketValue) || 0), 0))
const totalPnlAll = computed(() => totalValueAll.value - totalCostAll.value)

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

function pnlClass(n: number | string | null | undefined): string {
  if (n == null) return ''
  const v = Number(n)
  if (v > 0) return 'pos'
  if (v < 0) return 'neg'
  return ''
}

async function load() {
  loading.value = true
  try {
    const res = await fundApi.dashboard()
    funds.value = res.data?.funds || []
    syncNavInputs()
  } catch (err: any) {
    ElMessage.error(err?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

function syncNavInputs() {
  for (const f of funds.value) {
    navInputs.value[f.id] = f.latestNav != null ? String(f.latestNav) : ''
  }
}

function openNewFund() {
  fundForm.value = { id: undefined, name: '', code: '', type: '', remark: '', latestNav: '' }
  fundDialogVisible.value = true
}

function openEditFund(f: FundVO) {
  fundForm.value = {
    id: f.id,
    name: f.name,
    code: f.code || '',
    type: f.type || '',
    remark: f.remark || '',
    latestNav: f.latestNav != null ? String(f.latestNav) : ''
  }
  fundDialogVisible.value = true
}

async function saveFund() {
  if (!fundForm.value.name.trim()) {
    ElMessage.error('请输入名称')
    return
  }
  savingFund.value = true
  try {
    const res = await fundApi.saveFund({
      id: fundForm.value.id,
      name: fundForm.value.name.trim(),
      code: fundForm.value.code.trim() || undefined,
      type: fundForm.value.type || undefined,
      remark: fundForm.value.remark.trim() || undefined
    })
    // 用户在 dialog 里填了最新净值 → 顺手多记一条净值
    const navStr = (fundForm.value.latestNav || '').trim()
    if (navStr) {
      const nav = parseFloat(navStr)
      if (isNaN(nav) || nav <= 0) {
        ElMessage.warning('基金已保存, 但净值不合法, 没有记净值')
      } else {
        const fundId = res.data?.id ?? fundForm.value.id
        if (fundId != null) {
          await fundApi.saveNav({ fundId, nav })
        }
      }
    }
    ElMessage.success('已保存')
    fundDialogVisible.value = false
    await load()
  } catch (err: any) {
    ElMessage.error(err?.message || '保存失败')
  } finally {
    savingFund.value = false
  }
}

async function confirmDeleteFund(id: number | string) {
  try {
    await ElMessageBox.confirm('确定要删除这个基金吗? (买入和净值历史会一起删)', '确认', { type: 'warning' })
    await fundApi.deleteFund(id)
    ElMessage.success('已删除')
    await load()
  } catch (err: any) {
    if (err !== 'cancel' && err?.message) ElMessage.error(err.message)
  }
}

function openNewBuy(fundId: number) {
  buyForm.value = {
    fundId,
    buyDate: new Date().toISOString().slice(0, 10),
    price: '0',
    shares: '0',
    remark: ''
  }
  buyDialogVisible.value = true
}

async function saveBuy() {
  if (!buyForm.value.buyDate) {
    ElMessage.error('请选择日期')
    return
  }
  const price = parseFloat(buyForm.value.price)
  const shares = parseFloat(buyForm.value.shares)
  if (isNaN(price) || price <= 0) {
    ElMessage.error('单价必须大于 0')
    return
  }
  if (isNaN(shares) || shares <= 0) {
    ElMessage.error('份额必须大于 0')
    return
  }
  savingBuy.value = true
  try {
    await fundApi.saveBuy({
      fundId: buyForm.value.fundId,
      buyDate: buyForm.value.buyDate,
      price,
      shares,
      remark: buyForm.value.remark.trim() || undefined
    })
    ElMessage.success('已保存')
    buyDialogVisible.value = false
    await load()
  } catch (err: any) {
    ElMessage.error(err?.message || '保存失败')
  } finally {
    savingBuy.value = false
  }
}

async function confirmDeleteBuy(id: number | string) {
  try {
    await ElMessageBox.confirm('确定要删除这条买入记录吗?', '确认', { type: 'warning' })
    await fundApi.deleteBuy(id)
    ElMessage.success('已删除')
    await load()
  } catch (err: any) {
    if (err !== 'cancel' && err?.message) ElMessage.error(err.message)
  }
}

async function onNavChange(f: FundVO) {
  const raw = navInputs.value[f.id] ?? ''
  const nav = parseFloat(raw)
  if (isNaN(nav) || nav <= 0) {
    ElMessage.error('净值必须大于 0')
    return
  }
  try {
    await fundApi.saveNav({ fundId: f.id, nav })
    ElMessage.success('已记录')
    await load()
  } catch (err: any) {
    ElMessage.error(err?.message || '保存失败')
  }
}

onMounted(() => {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  load()
})
</script>

<style scoped>
.fund-widget { width: 100%; display: flex; flex-direction: column; gap: 16px; }
.actions-row { text-align: right; }

.empty-state {
  padding: 48px;
  text-align: center;
  background: white;
  border-radius: 8px;
  font-size: 14px;
}

.fund-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(420px, 1fr));
  gap: 16px;
}
.fund-card { display: flex; flex-direction: column; gap: 10px; }

.fund-header {
  display: flex; align-items: baseline; justify-content: space-between;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--border);
}
.fund-title { display: flex; align-items: baseline; gap: 8px; }
.fund-name { font-size: 15px; font-weight: 600; margin: 0; }
.fund-code { font-size: 12px; font-family: monospace; }
.fund-type {
  font-size: 11px; padding: 1px 6px;
  background: var(--bg-secondary);
  border-radius: 6px;
  color: var(--text-secondary);
}

.fund-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 6px 16px;
  font-size: 13px;
}
.kv { display: flex; justify-content: space-between; align-items: center; }
.kv--span2 { grid-column: 1 / -1; }
.kv-label { color: var(--text-secondary); }
.kv-value { font-variant-numeric: tabular-nums; }
.kv-value.pos { color: #10b981; font-weight: 600; }
.kv-value.neg { color: #ef4444; font-weight: 600; }
.kv-rate { font-size: 12px; color: var(--text-secondary); margin-left: 4px; }
.kv-input {
  width: 100px; text-align: right;
  padding: 2px 6px; border: 1px solid var(--border); border-radius: 4px;
  font-variant-numeric: tabular-nums;
}
.kv-input:focus { border-color: var(--primary); outline: none; }

.fund-remark { font-size: 12px; padding-top: 4px; border-top: 1px dashed var(--border); }

.buys-section { margin-top: 4px; }
.buys-header {
  display: flex; align-items: center; justify-content: space-between;
  font-size: 13px; font-weight: 600;
  padding: 4px 0;
}
.buys-empty { font-size: 12px; padding: 8px 0; }
.buys-table-wrap { overflow-x: auto; }
.buys-table {
  width: 100%; border-collapse: collapse;
  font-size: 12px;
}
.buys-table th, .buys-table td {
  padding: 6px 4px; text-align: left;
  border-bottom: 1px solid var(--border);
}
.buys-table th { background: var(--bg-secondary); font-weight: 600; }
.buys-table td.num, .buys-table th.num { text-align: right; font-variant-numeric: tabular-nums; }
.buys-table td.strong { font-weight: 600; color: var(--primary); }
.text-small { font-size: 11px; }

.date-picker { width: 100%; }

@media (max-width: 900px) {
  .fund-grid { grid-template-columns: 1fr; }
  .kv--span2 { grid-column: 1; }
}
</style>
