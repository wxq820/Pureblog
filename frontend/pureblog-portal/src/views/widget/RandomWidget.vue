<template>
  <div class="random-widget">
    <div class="card">
      <div class="random-body">
        <div class="input-panel">
          <div class="form">
            <div class="form-row">
              <label>最小值</label>
              <input v-model.number="minVal" type="number" class="input" />
            </div>
            <div class="form-row">
              <label>最大值</label>
              <input v-model.number="maxVal" type="number" class="input" />
            </div>
            <div class="form-row">
              <label>数量</label>
              <input v-model.number="count" type="number" class="input" min="1" max="100" />
            </div>
            <div class="form-row">
              <label>
                <input v-model="noRepeat" type="checkbox" :disabled="!canNoRepeat" />
                不重复
              </label>
              <small v-if="!canNoRepeat" class="text-secondary">范围太小时自动取消</small>
            </div>
          </div>

          <div class="actions">
            <button class="btn btn-outline" @click="clearResults">清空</button>
            <button class="btn btn-outline" @click="copyAll" :disabled="!results.length">复制全部</button>
            <button class="btn btn-primary" @click="generate">生成</button>
          </div>
        </div>

        <div class="results-panel">
          <div v-if="results.length" class="results">
            <div v-for="(n, i) in results" :key="i" class="chip" @click="copyOne(n)">{{ n }}</div>
          </div>
          <div v-else class="empty text-secondary">点击生成按钮</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'

const minVal = ref<number>(1)
const maxVal = ref<number>(100)
const count = ref<number>(10)
const noRepeat = ref<boolean>(false)
const results = ref<number[]>([])

const canNoRepeat = computed(() => {
  const span = Math.floor(maxVal.value) - Math.floor(minVal.value) + 1
  return span >= count.value
})

function generate() {
  const lo = Math.floor(minVal.value)
  const hi = Math.floor(maxVal.value)
  if (isNaN(lo) || isNaN(hi) || lo > hi) {
    ElMessage.error('范围不合法')
    return
  }
  if (count.value < 1 || count.value > 100) {
    ElMessage.error('数量需在 1-100 之间')
    return
  }
  if (noRepeat.value && !canNoRepeat.value) {
    ElMessage.warning('范围太小, 已自动允许重复')
    noRepeat.value = false
  }
  const span = hi - lo + 1
  if (noRepeat.value) {
    // Fisher-Yates 部分洗牌
    const pool: number[] = []
    for (let i = lo; i <= hi; i++) pool.push(i)
    for (let i = pool.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1))
      ;[pool[i], pool[j]] = [pool[j], pool[i]]
    }
    results.value = pool.slice(0, count.value)
  } else {
    results.value = Array.from({ length: count.value }, () => lo + Math.floor(Math.random() * span))
  }
}

function clearResults() {
  results.value = []
}

async function copyOne(n: number) {
  try {
    await navigator.clipboard.writeText(String(n))
    ElMessage.success(`已复制 ${n}`)
  } catch {
    ElMessage.error('复制失败')
  }
}

async function copyAll() {
  try {
    await navigator.clipboard.writeText(results.value.join(', '))
    ElMessage.success('已复制全部')
  } catch {
    ElMessage.error('复制失败')
  }
}
</script>

<style scoped>
.random-widget { width: 100%; }
.random-body {
  display: grid;
  grid-template-columns: minmax(280px, 360px) 1fr;
  gap: 32px;
  align-items: start;
}
.input-panel { display: flex; flex-direction: column; min-width: 0; }
.results-panel { min-width: 0; }

.form { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
.form-row { display: flex; flex-direction: column; gap: 4px; }
.form-row label { font-size: 14px; color: var(--text-secondary); }
.form-row label input[type="checkbox"] { margin-right: 6px; }
.form-row small { font-size: 12px; }

.actions { display: flex; gap: 8px; margin-top: 20px; }
.results {
  display: flex; flex-wrap: wrap; gap: 10px;
  min-height: 200px;
  padding: 16px;
  background: var(--bg-secondary); border-radius: 8px;
}
.chip {
  display: inline-flex; align-items: center; justify-content: center;
  min-width: 56px; height: 40px; padding: 0 16px;
  background: white; border: 1px solid var(--border); border-radius: 20px;
  font-variant-numeric: tabular-nums; font-size: 15px; cursor: pointer;
  transition: all 0.15s;
}
.chip:hover { background: var(--primary); color: white; border-color: var(--primary); transform: translateY(-1px); }
.empty {
  min-height: 200px;
  display: flex; align-items: center; justify-content: center;
  background: var(--bg-secondary); border-radius: 8px;
  font-size: 14px;
}

@media (max-width: 900px) {
  .random-body { grid-template-columns: 1fr; gap: 20px; }
}
</style>
