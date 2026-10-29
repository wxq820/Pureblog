<template>
  <div class="typing-widget">
    <div class="card">
      <div class="header">
        <span class="progress">{{ typed.length }} / {{ text.length }}</span>
        <button class="btn btn-outline" @click="reset">换一段</button>
      </div>

      <div class="text-display" :class="{ done: isDone }">
        <span
          v-for="(ch, i) in chars"
          :key="i"
          :class="charClass(i)"
        >{{ ch }}</span>
      </div>

      <input
        ref="inputRef"
        v-model="typed"
        class="input typing-input"
        :placeholder="text.length ? '开始打字...' : '加载中...'"
        :disabled="isDone"
        @keydown="onKeyDown"
        @input="onInput"
      />

      <div v-if="isDone" class="result">
        <span class="badge">完成</span>
        <span class="text-secondary">耗时 {{ elapsedSec }} 秒, {{ wpm }} WPM</span>
        <button class="btn btn-primary" @click="reset">再来一次</button>
      </div>
    </div>

    <div class="card keyboard">
      <div v-for="(row, ri) in keyboardRows" :key="ri" class="kb-row">
        <div
          v-for="k in row"
          :key="k.label"
          :class="['kb-key', { active: activeKey === k.label.toLowerCase() }]"
        >{{ k.label }}</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick } from 'vue'

const SAMPLES = [
  'The quick brown fox jumps over the lazy dog.',
  'Pack my box with five dozen liquor jugs.',
  'How vexingly quick daft zebras jump.',
  'Sphinx of black quartz, judge my vow.',
  'Bright vixens jump, dozy fowl quack.',
  'The five boxing wizards jump quickly.',
  'Jackdaws love my big sphinx of quartz.',
  'Cwm fjord bank glyphs vext quiz.',
  'Programming is the art of telling another human what one wants the computer to do.',
  'Simplicity is the ultimate sophistication.',
  'First, solve the problem. Then, write the code.',
  'Make it work, make it right, make it fast.',
  'Code is read much more often than it is written.',
  'Any fool can write code that a computer can understand.',
  'Good programmers use their brains, not their fingers.',
  'The best way to predict the future is to invent it.',
  'Talk is cheap. Show me the code.',
  'Programs must be written for people to read.',
  'Always code as if the guy who ends up maintaining it is a violent psychopath.',
  'There are only two hard things in computer science: cache invalidation and naming things.',
  'Walking on water and developing software from a specification are easy if both are frozen.',
  'It works on my machine.',
  'There are 10 types of people in the world: those who understand binary and those who do not.',
  'A good programmer looks both ways before crossing a one-way street.',
  'I would have written a shorter program, but I did not have the time.',
  'The most disastrous thing you can ever learn is your first programming language.',
  'Weeks of programming can save you hours of planning.',
  'Documentation is a love letter that you write to your future self.',
  'Fix the cause, not the symptom.',
  'Deleted code is debugged code.'
]

const KEYBOARD = [
  [{ label: 'Q' }, { label: 'W' }, { label: 'E' }, { label: 'R' }, { label: 'T' }, { label: 'Y' }, { label: 'U' }, { label: 'I' }, { label: 'O' }, { label: 'P' }],
  [{ label: 'A' }, { label: 'S' }, { label: 'D' }, { label: 'F' }, { label: 'G' }, { label: 'H' }, { label: 'J' }, { label: 'K' }, { label: 'L' }],
  [{ label: 'Z' }, { label: 'X' }, { label: 'C' }, { label: 'V' }, { label: 'B' }, { label: 'N' }, { label: 'M' }, { label: ',' }, { label: '.' }, { label: '/' }],
  [{ label: 'Space' }]
]

const text = ref('')
const typed = ref('')
const startTime = ref<number | null>(null)
const endTime = ref<number | null>(null)
const inputRef = ref<HTMLInputElement | null>(null)
const activeKey = ref<string | null>(null)
const keyboardRows = KEYBOARD

const chars = computed(() => Array.from(text.value))

const isDone = computed(() => text.value.length > 0 && typed.value.length >= text.value.length)

const elapsedSec = computed(() => {
  if (!startTime.value || !endTime.value) return 0
  return Math.max(1, Math.round((endTime.value - startTime.value) / 1000))
})

const wpm = computed(() => {
  if (elapsedSec.value === 0) return 0
  // 5 字符 = 1 词 (WPM 通用算法)
  return Math.round((text.value.length / 5) / (elapsedSec.value / 60))
})

function charClass(i: number) {
  if (i < typed.value.length) {
    return typed.value[i] === text.value[i] ? 'ch correct' : 'ch wrong'
  }
  if (i === typed.value.length) return 'ch current'
  return 'ch pending'
}

function onInput() {
  if (isDone.value) return
  if (startTime.value === null) startTime.value = Date.now()
  if (isDone.value) endTime.value = Date.now()
  // 修正 typed 不能超过 text
  if (typed.value.length > text.value.length) {
    typed.value = typed.value.slice(0, text.value.length)
  }
}

function onKeyDown(e: KeyboardEvent) {
  const k = e.key.toLowerCase()
  activeKey.value = k
  setTimeout(() => { if (activeKey.value === k) activeKey.value = null }, 120)
}

function reset() {
  const idx = Math.floor(Math.random() * SAMPLES.length)
  text.value = SAMPLES[idx]
  typed.value = ''
  startTime.value = null
  endTime.value = null
  nextTick(() => inputRef.value?.focus())
}

onMounted(() => {
  reset()
})
</script>

<style scoped>
.typing-widget { display: flex; flex-direction: column; gap: 16px; }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.progress { font-size: 14px; color: var(--text-secondary); font-variant-numeric: tabular-nums; }

.text-display {
  font-family: 'Courier New', monospace;
  font-size: 20px;
  line-height: 1.8;
  padding: 20px;
  background: var(--bg-secondary);
  border-radius: 8px;
  letter-spacing: 1px;
  user-select: none;
  word-break: break-all;
}
.text-display.done { background: #f0fdf4; }
.ch { transition: color 0.05s; }
.ch.correct { color: #10b981; }
.ch.wrong { color: #ef4444; background: #fee2e2; border-radius: 2px; }
.ch.current { background: #dbeafe; border-radius: 2px; box-shadow: 0 0 0 1px var(--primary); }
.ch.pending { color: #9ca3af; }

.typing-input { width: 100%; margin-top: 12px; font-family: 'Courier New', monospace; font-size: 16px; }

.result { display: flex; align-items: center; gap: 12px; margin-top: 12px; padding: 12px; background: #f0fdf4; border-radius: 6px; }
.badge { background: #10b981; color: white; padding: 2px 8px; border-radius: 4px; font-size: 12px; }

.keyboard { padding: 16px; }
.kb-row { display: flex; justify-content: center; gap: 4px; margin-bottom: 4px; }
.kb-key {
  display: inline-flex; align-items: center; justify-content: center;
  min-width: 36px; height: 36px; padding: 0 8px;
  background: white; border: 1px solid var(--border); border-radius: 4px;
  font-family: monospace; font-size: 13px; color: var(--text-secondary);
  transition: all 0.05s;
}
.kb-key.active { background: var(--primary); color: white; border-color: var(--primary); transform: translateY(1px); }
.kb-row:last-child .kb-key { min-width: 200px; }
</style>
