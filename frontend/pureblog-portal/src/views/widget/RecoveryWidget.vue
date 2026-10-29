<template>
  <div class="recovery-widget">
    <!-- 顶部: 整体进度 + 新建仓位 -->
    <div class="card summary-card">
      <div class="summary-row">
        <div class="summary-item">
          <div class="summary-label">总成本</div>
          <div class="summary-value">¥ {{ formatMoney(dashboard?.totalCost) }}</div>
        </div>
        <div class="summary-item">
          <div class="summary-label">当前市值</div>
          <div class="summary-value">¥ {{ formatMoney(dashboard?.totalMarketValue) }}</div>
        </div>
        <div class="summary-item">
          <div class="summary-label">总盈亏</div>
          <div class="summary-value" :class="pnlClass(dashboard?.totalPnl)">
            {{ formatDelta(dashboard?.totalPnl) }}
          </div>
        </div>
        <div class="summary-item summary-item--progress">
          <div class="summary-label">整体回本进度</div>
          <el-progress
            :percentage="Number(dashboard?.totalRecoveryProgress || 0)"
            :stroke-width="14"
            :color="progressColors"
          />
        </div>
      </div>
      <div class="summary-actions">
        <el-button type="primary" size="small" @click="openNewPosition">+ 新建仓位</el-button>
      </div>
    </div>

    <!-- 仓位列表 -->
    <div v-if="loading" class="text-secondary empty-state">加载中...</div>
    <div v-else-if="!dashboard?.positions?.length" class="text-secondary empty-state">还没有仓位, 点右上角加一个</div>
    <div v-else class="position-list">
      <div v-for="p in dashboard.positions" :key="p.id" class="card position-card">
        <div class="position-header">
          <h3 class="position-name">{{ p.name }}</h3>
          <div class="position-actions">
            <el-button text type="primary" size="small" @click="openEditPosition(p)">编辑</el-button>
            <el-button text type="danger" size="small" @click="confirmDeletePosition(p.id)">删除</el-button>
          </div>
        </div>
        <div class="position-grid">
          <div class="kv">
            <span class="kv-label">成本</span>
            <span class="kv-value">¥ {{ formatMoney(p.cost) }}</span>
          </div>
          <div class="kv">
            <span class="kv-label">当前市值</span>
            <span class="kv-value">
              <input
                :value="p.latestMarketValue"
                @change="(e) => onMarketValueChange(p, (e.target as HTMLInputElement).value)"
                class="kv-input"
                inputmode="decimal"
                placeholder="填入并回车"
              />
            </span>
          </div>
          <div class="kv">
            <span class="kv-label">盈亏</span>
            <span class="kv-value" :class="pnlClass(p.pnl)">
              {{ formatDelta(p.pnl) }}
              <span v-if="p.pnlRate != null" class="kv-rate">
                ({{ formatDelta(p.pnlRate) }}%)
              </span>
            </span>
          </div>
          <div class="kv">
            <span class="kv-label">距回本</span>
            <span class="kv-value">
              <template v-if="p.breakEven">已回本 ✓</template>
              <template v-else>¥ {{ formatMoney(p.distanceToBreakEven) }}</template>
            </span>
          </div>
        </div>
        <div class="progress-wrap">
          <el-progress
            :percentage="Number(p.recoveryProgress || 0)"
            :stroke-width="10"
            :color="progressColors"
            :show-text="false"
          />
          <span class="progress-text">{{ formatMoney(p.recoveryProgress) }}%</span>
        </div>
        <div v-if="p.remark" class="position-remark text-secondary">{{ p.remark }}</div>
      </div>
    </div>

    <!-- 新建/编辑仓位 dialog -->
    <el-dialog
      v-model="positionDialogVisible"
      :title="positionForm.id ? '编辑仓位' : '新建仓位'"
      width="440px"
    >
      <el-form :model="positionForm" label-width="80px">
        <el-form-item label="名称">
          <el-input v-model="positionForm.name" maxlength="64" />
        </el-form-item>
        <el-form-item label="成本">
          <el-input v-model="positionForm.cost" type="number" step="0.01" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="positionForm.remark" type="textarea" :rows="2" maxlength="255" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="positionDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="savingPosition" @click="savePosition">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { recoveryApi, type RecoveryDashboardVO, type PositionVO } from '@/api/recovery'
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'

const userStore = useUserStore()
const router = useRouter()

const dashboard = ref<RecoveryDashboardVO | null>(null)
const loading = ref(false)
const positionDialogVisible = ref(false)
const savingPosition = ref(false)
const positionForm = ref({
  id: undefined as number | undefined,
  name: '',
  cost: '0',
  remark: ''
})

const progressColors = [
  { color: '#ef4444', percentage: 33 },
  { color: '#f59e0b', percentage: 66 },
  { color: '#10b981', percentage: 100 }
]

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
    const res = await recoveryApi.dashboard()
    dashboard.value = res.data
  } catch (err: any) {
    ElMessage.error(err?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

function openNewPosition() {
  positionForm.value = { id: undefined, name: '', cost: '0', remark: '' }
  positionDialogVisible.value = true
}

function openEditPosition(p: PositionVO) {
  positionForm.value = {
    id: p.id,
    name: p.name,
    cost: String(p.cost),
    remark: p.remark || ''
  }
  positionDialogVisible.value = true
}

async function savePosition() {
  if (!positionForm.value.name.trim()) {
    ElMessage.error('请输入名称')
    return
  }
  savingPosition.value = true
  try {
    await recoveryApi.savePosition({
      id: positionForm.value.id,
      name: positionForm.value.name.trim(),
      cost: parseFloat(positionForm.value.cost) || 0,
      remark: positionForm.value.remark.trim() || undefined
    })
    ElMessage.success('已保存')
    positionDialogVisible.value = false
    await load()
  } catch (err: any) {
    ElMessage.error(err?.message || '保存失败')
  } finally {
    savingPosition.value = false
  }
}

async function confirmDeletePosition(id: number) {
  try {
    await ElMessageBox.confirm('确定要删除这个仓位吗? (市值历史会一起删)', '确认', { type: 'warning' })
    await recoveryApi.deletePosition(id)
    ElMessage.success('已删除')
    await load()
  } catch (err: any) {
    if (err !== 'cancel' && err?.message) ElMessage.error(err.message)
  }
}

async function onMarketValueChange(p: PositionVO, value: string) {
  const mv = parseFloat(value)
  if (isNaN(mv) || mv < 0) {
    ElMessage.error('市值不合法')
    return
  }
  try {
    await recoveryApi.saveValue({
      positionId: p.id,
      marketValue: mv
    })
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
.recovery-widget {
  width: 100%;
  display: flex; flex-direction: column;
  gap: 16px;
}

.summary-card { padding-bottom: 16px; }
.summary-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr) 2fr;
  gap: 12px;
  align-items: center;
}
.summary-item { text-align: center; }
.summary-label { font-size: 12px; color: var(--text-secondary); margin-bottom: 4px; }
.summary-value {
  font-size: 18px; font-weight: 600; color: var(--primary);
  font-variant-numeric: tabular-nums;
}
.summary-value.pos { color: #10b981; }
.summary-value.neg { color: #ef4444; }
.summary-item--progress { text-align: left; padding: 0 12px; }

.summary-actions { margin-top: 12px; text-align: right; }

.empty-state {
  padding: 48px;
  text-align: center;
  background: white;
  border-radius: 8px;
  font-size: 14px;
}

.position-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(360px, 1fr));
  gap: 16px;
}
.position-card { display: flex; flex-direction: column; gap: 8px; }
.position-header {
  display: flex; align-items: baseline; justify-content: space-between;
}
.position-name { font-size: 15px; font-weight: 600; margin: 0; }
.position-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px 16px;
  font-size: 13px;
}
.kv { display: flex; justify-content: space-between; }
.kv-label { color: var(--text-secondary); }
.kv-value { font-variant-numeric: tabular-nums; }
.kv-value.pos { color: #10b981; }
.kv-value.neg { color: #ef4444; }
.kv-rate { font-size: 12px; color: var(--text-secondary); margin-left: 4px; }
.kv-input {
  width: 100px; text-align: right;
  padding: 2px 6px; border: 1px solid var(--border); border-radius: 4px;
  font-variant-numeric: tabular-nums;
}
.kv-input:focus { border-color: var(--primary); outline: none; }

.progress-wrap { display: flex; align-items: center; gap: 8px; }
.progress-wrap :deep(.el-progress) { flex: 1; }
.progress-text { font-size: 12px; color: var(--text-secondary); font-variant-numeric: tabular-nums; }

.position-remark { font-size: 12px; padding-top: 4px; border-top: 1px dashed var(--border); }

@media (max-width: 900px) {
  .summary-row { grid-template-columns: 1fr 1fr; }
  .summary-item--progress { grid-column: 1 / -1; }
}
</style>
