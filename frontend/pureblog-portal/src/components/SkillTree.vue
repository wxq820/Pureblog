<template>
  <div class="skill-tree-container">
    <div class="tree-header">
      <h3>{{ tree.name }}</h3>
      <div class="tree-hint">拖动下方进度条旋转树（0° ↔ 360°）· 点击节点展开 · 右侧选层级</div>
    </div>
    <div class="tree-body">
      <div class="canvas-wrapper" ref="wrapperRef">
        <canvas
          ref="canvasRef"
          @click="onClick"
        ></canvas>
        <div class="rotation-bar">
          <span class="label">旋转</span>
          <input
            type="range"
            min="0"
            max="1"
            step="0.001"
            :value="rotationProgress"
            @input="onProgressInput"
          />
          <span class="deg">{{ Math.round(rotationDeg) }}°</span>
        </div>
        <div v-if="selectedDepth3Id" class="selection-banner">
          <span>{{ selectedNodeName }}</span>
          <button class="clear-btn" @click="clearSelection">清除选择</button>
        </div>
      </div>
      <div class="level-rail">
        <button
          v-for="lvl in LEVEL_OPTIONS_DISPLAY"
          :key="lvl"
          :class="['level-btn', { active: effectiveMaxDepth === lvl, disabled: !selectedDepth3Id }]"
          :disabled="!selectedDepth3Id"
          @click="setMaxDepth(lvl)"
        >{{ lvl }}</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, computed } from 'vue'
import { useRouter } from 'vue-router'
import type { SkillTree, SkillNode } from '@/types/skillTree'

interface LayoutNode {
  node: SkillNode
  depth: number
  parent: SkillNode | null
  radius: number
  hasChildren: boolean
  worldAngle: number
  worldR: number
  worldY: number
}

const props = defineProps<{ tree: SkillTree }>()

const router = useRouter()
const canvasRef = ref<HTMLCanvasElement>()
const wrapperRef = ref<HTMLDivElement>()
let ctx: CanvasRenderingContext2D | null = null
let layoutNodes: LayoutNode[] = []

const NODE_RADIUS = [26, 16, 13, 11, 9, 8, 7]
const LEVEL_HEIGHT = 110          // 第 1 层和第 2 层间距加大

// 视觉上自上而下：7 在最上面、3 在最下面
const LEVEL_OPTIONS_DISPLAY = [7, 6, 5, 4, 3]
const DEFAULT_MAX_DEPTH = 3

let visibleMaxDepth = DEFAULT_MAX_DEPTH
const expandedNodeIds = new Set<string>()

// 选中第 3 层节点（仅 depth=3 可选）→ 在该子树内局部展开层级
const selectedDepth3Id = ref<string | null>(null)
let selectedExpandedDepth = DEFAULT_MAX_DEPTH

// 旋转（绕竖直 Y 轴）
let rotation = 0
// 进度条值 0..1 → rotation 0..π
const rotationProgress = ref(0)
const ROTATION_SENSITIVITY = 0.008
const DRAG_THRESHOLD = 4

function getNodeRadius(level: number): number {
  return NODE_RADIUS[Math.min(level, NODE_RADIUS.length - 1)]
}

function findNode(id: string): SkillNode | null {
  function walk(n: SkillNode): SkillNode | null {
    if (n.id === id) return n
    for (const c of n.children || []) { const r = walk(c); if (r) return r }
    return null
  }
  return walk(props.tree.root)
}

function findLayoutByNodeId(id: string): LayoutNode | null {
  return layoutNodes.find(l => l.node.id === id) || null
}

// 是否可见：默认深度限制 + 局部展开 + 选中子树展开
function isVisible(ln: LayoutNode): boolean {
  if (ln.depth <= visibleMaxDepth) return true
  // 全局局部展开
  let cur: LayoutNode | null = ln
  while (cur) {
    if (expandedNodeIds.has(cur.node.id)) return true
    cur = layoutNodes.find(l => l.node === cur!.parent) || null
  }
  // 选中第 3 层节点后：仅该子树内的层级受 selectedExpandedDepth 控制
  if (selectedDepth3Id.value) {
    const sel = findLayoutByNodeId(selectedDepth3Id.value)
    if (sel && isDescendantOf(ln, sel)) {
      return ln.depth <= selectedExpandedDepth
    }
    return false
  }
  return false
}

function isDescendantOf(ln: LayoutNode, ancestor: LayoutNode): boolean {
  let cur: LayoutNode | null = ln
  while (cur) {
    if (cur === ancestor) return true
    const parentNode: SkillNode | null = cur.parent
    if (!parentNode) { cur = null; break }
    cur = layoutNodes.find(l => l.node === parentNode) || null
  }
  return false
}

// 节点是否还藏有可见之外的子节点
function hasHiddenChildren(ln: LayoutNode): boolean {
  if (!ln.hasChildren) return false
  function deeperHidden(n: LayoutNode): boolean {
    for (const child of n.node.children || []) {
      const cln = layoutNodes.find(l => l.node === child)
      if (!cln) continue
      if (!isVisible(cln)) return true
      if (deeperHidden(cln)) return true
    }
    return false
  }
  return deeperHidden(ln)
}

function buildLayout() {
  layoutNodes = []
  const root = props.tree.root
  function traverse(node: SkillNode, depth: number, parent: SkillNode | null) {
    const ln: LayoutNode = {
      node, depth, parent,
      radius: getNodeRadius(depth),
      hasChildren: !!(node.children && node.children.length > 0),
      worldAngle: 0, worldR: 0, worldY: 0,
    }
    layoutNodes.push(ln)
    ;(node.children || []).forEach(c => traverse(c, depth + 1, node))
  }
  traverse(root, 0, null)
}

function resolveWorld(canvasWidth: number, canvasHeight: number) {
  if (!layoutNodes.length) return
  // 按 depth 分组
  const byDepth = new Map<number, LayoutNode[]>()
  for (const ln of layoutNodes) {
    if (!byDepth.has(ln.depth)) byDepth.set(ln.depth, [])
    byDepth.get(ln.depth)!.push(ln)
  }
  const maxDepth = Math.max(...byDepth.keys(), 1)
  // 垂直：根在底部、叶子在顶部，按画布高度 0.85 比例铺
  const verticalUsable = canvasHeight * 0.85
  const topMargin = canvasHeight * 0.06
  const bottomY = topMargin + verticalUsable
  // 水平：圆环最大半径 = min(画布宽/2, 画布高/2) * 0.85，保证不超出画布
  const maxR = Math.min(canvasWidth / 2, canvasHeight / 2) * 0.85
  for (const [depth, nodes] of byDepth) {
    if (depth === 0) {
      // 根节点：单点，贴底
      nodes.forEach((ln) => {
        ln.worldAngle = 0
        ln.worldR = 0
        ln.worldY = bottomY
      })
      continue
    }
    // depth>=1 呈圆环：depth 越大半径越小、y 越靠上
    const r = maxR * (1 - (depth - 1) / Math.max(maxDepth, 1))
    const y = topMargin + verticalUsable * (1 - depth / Math.max(maxDepth, 1))
    nodes.forEach((ln, i) => {
      ln.worldAngle = (i / Math.max(nodes.length, 1)) * Math.PI * 2
      ln.worldR = r
      ln.worldY = y
    })
  }
}

// 3D 投影：节点呈圆环分布在水平面上，绕竖直 Y 轴旋转
// 拖动进度条时，节点 screenX 真的会左右移动；背面 viewZ<0 节点 α 低、正面高
function projectNode(ln: LayoutNode, height: number, canvasW: number) {
  const cx = canvasW / 2
  // 三维坐标（绕 Y 轴）
  const x3 = Math.sin(ln.worldAngle) * ln.worldR
  const z3 = Math.cos(ln.worldAngle) * ln.worldR   // 初始指向相机为正
  // 绕 Y 轴旋转 rotation：相机视角下的 X、Z
  const cosR = Math.cos(rotation)
  const sinR = Math.sin(rotation)
  const viewX = x3 * cosR + z3 * sinR
  const viewZ = -x3 * sinR + z3 * cosR
  const screenX = cx + viewX
  const screenY = ln.worldY
  // 背面(viewZ<0)α 低、正面(viewZ>0)α 高；r=0 节点（根）恒为 1
  const alpha = ln.worldR <= 0
    ? 1
    : Math.max(0.08, Math.min(1, (viewZ + ln.worldR) / (2 * ln.worldR)))
  return { screenX, screenY, alpha, viewZ, worldR: ln.worldR }
}

function draw() {
  if (!ctx || !canvasRef.value) return
  const width = canvasRef.value.width
  const height = canvasRef.value.height
  ctx.clearRect(0, 0, width, height)
  ctx.fillStyle = '#fafafa'
  ctx.fillRect(0, 0, width, height)

  const cx = width / 2

  // 可见节点
  const visible = layoutNodes.filter(isVisible)

  // 模糊化：选中第 3 层后，非选中子树的节点 alpha 降到 0.3
  function nodeAlpha(ln: LayoutNode, baseScale: number): number {
    let a = baseScale
    if (selectedDepth3Id.value) {
      const sel = findLayoutByNodeId(selectedDepth3Id.value)
      const inSelectedSubtree = sel ? isDescendantOf(ln, sel) : false
      // 第 1、2 层与选中节点所在的祖先链仍可见但不模糊；选中节点的祖先链（path）也算相关
      const isAncestorOfSel = sel ? isAncestorOrSelf(ln, sel) : false
      const isSiblingOrOther = !inSelectedSubtree && !isAncestorOfSel
      if (isSiblingOrOther) a = Math.min(a, 0.3)
    }
    return a
  }

  // 计算投影 + 排序（远→近）
  type Proj = { ln: LayoutNode, x: number, y: number, z: number, alpha: number, r: number }
  const projs: Proj[] = visible.map(ln => {
    const p = projectNode(ln, height, width)
    const baseA = p.alpha
    const r = ln.radius
    return { ln, x: p.screenX, y: p.screenY, z: p.viewZ, alpha: nodeAlpha(ln, baseA), r }
  })

  // 边：先按 viewZ 排序远的先画（让近的覆盖）
  type Edge = { from: Proj, to: Proj, z: number }
  const edges: Edge[] = []
  projs.forEach(p => {
    if (!p.ln.parent) return
    const parentProj = projs.find(q => q.ln.node === p.ln.parent)
    if (!parentProj) return
    edges.push({ from: parentProj, to: p, z: (parentProj.z + p.z) / 2 })
  })
  edges.sort((a, b) => a.z - b.z)

  // 画边
  edges.forEach(e => {
    const a = e.from, b = e.to
    const aA = a.alpha
    const bA = b.alpha
    if (Math.max(aA, bA) < 0.05) return
    const lineA = Math.max(aA, bA) * 0.85
    const parentLn = e.from.ln
    const stroke = parentLn.node.color || '#cbd5e1'
    ctx!.strokeStyle = hexWithAlpha(stroke, lineA)
    ctx!.lineWidth = Math.max(1, (2.5 - parentLn.depth * 0.25) * ((a.r + b.r) / 30))
    ctx!.beginPath()
    ctx!.moveTo(a.x, a.y)
    const mx = (a.x + b.x) / 2
    const my = (a.y + b.y) / 2
    ctx!.quadraticCurveTo(mx + (cx - mx) * 0.05, my, b.x, b.y)
    ctx!.stroke()
  })

  // 画节点（按 viewZ 排序：远的先画，近的覆盖）
  projs.sort((a, b) => a.z - b.z)
  projs.forEach(p => {
    const r = p.r
    if (p.alpha < 0.05) return
    const baseColor = p.ln.node.color || '#3b82f6'
    const nodeFill = baseColor

    // 选中高亮边框
    const isSel = selectedDepth3Id.value && p.ln.node.id === selectedDepth3Id.value
    ctx!.beginPath()
    ctx!.arc(p.x, p.y, r, 0, Math.PI * 2)
    ctx!.fillStyle = hexWithAlpha(nodeFill, p.alpha)
    ctx!.fill()

    if (isSel) {
      ctx!.strokeStyle = '#fbbf24'
      ctx!.lineWidth = 3
    } else {
      ctx!.strokeStyle = hexWithAlpha('white', p.alpha)
      ctx!.lineWidth = 2
    }
    ctx!.stroke()

    if (r >= 9) {
      ctx!.fillStyle = hexWithAlpha('white', p.alpha)
      ctx!.textAlign = 'center'
      ctx!.textBaseline = 'middle'
      const fontSize = Math.max(8, r * 0.5)
      ctx!.font = `bold ${fontSize}px -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif`
      ctx!.fillText(p.ln.node.name, p.x, p.y, r * 1.7)
    }

    if (hasHiddenChildren(p.ln) && p.alpha > 0.5) {
      const px = p.x + r * 0.8
      const py = p.y - r * 0.8
      const pr = Math.max(6, r * 0.35)
      ctx!.beginPath()
      ctx!.arc(px, py, pr, 0, Math.PI * 2)
      ctx!.fillStyle = hexWithAlpha('#10b981', p.alpha)
      ctx!.fill()
      ctx!.strokeStyle = hexWithAlpha('white', p.alpha)
      ctx!.lineWidth = 1.5
      ctx!.stroke()
      ctx!.strokeStyle = hexWithAlpha('white', p.alpha)
      ctx!.lineWidth = 2
      ctx!.beginPath()
      ctx!.moveTo(px - pr * 0.5, py)
      ctx!.lineTo(px + pr * 0.5, py)
      ctx!.moveTo(px, py - pr * 0.5)
      ctx!.lineTo(px, py + pr * 0.5)
      ctx!.stroke()
    }
  })

  // #region agent log (hypothesisId=N4)
  const payload = {
    width, height,
    rotationDeg: Math.round((rotation * 180) / Math.PI),
    visibleMaxDepth,
    selectedDepth3Id: selectedDepth3Id.value,
    selectedExpandedDepth,
    visibleNodeCount: visible.length,
    totalNodes: layoutNodes.length,
    hypothesisId: 'N4',
    ts: Date.now()
  }
}

function isAncestorOrSelf(ln: LayoutNode, candidate: LayoutNode): boolean {
  if (ln === candidate) return true
  return isAncestorOrSelfNode(ln.node, candidate.node)
}
function isAncestorOrSelfNode(selfNode: SkillNode, candidate: SkillNode): boolean {
  let cur: LayoutNode | null = findLayoutByNodeId(candidate.id)
  while (cur) {
    if (cur.node === selfNode) return true
    const parentNode = cur.parent
    if (!parentNode) { cur = null; break }
    cur = findLayoutByNodeId(parentNode.id)
  }
  return false
}

function hexWithAlpha(hex: string, alpha: number): string {
  const a = Math.max(0, Math.min(1, alpha))
  if (hex.startsWith('#') && (hex.length === 7 || hex.length === 4)) {
    let r = 0, g = 0, b = 0
    if (hex.length === 7) {
      r = parseInt(hex.slice(1, 3), 16)
      g = parseInt(hex.slice(3, 5), 16)
      b = parseInt(hex.slice(5, 7), 16)
    } else {
      r = parseInt(hex[1] + hex[1], 16)
      g = parseInt(hex[2] + hex[2], 16)
      b = parseInt(hex[3] + hex[3], 16)
    }
    return `rgba(${r}, ${g}, ${b}, ${a})`
  }
  return hex
}

function findLayoutAt(clientX: number, clientY: number): { ln: LayoutNode, isPlusBtn: boolean } | null {
  if (!canvasRef.value) return null
  const rect = canvasRef.value.getBoundingClientRect()
  const x = clientX - rect.left
  const y = clientY - rect.top
  const width = canvasRef.value.width
  const height = canvasRef.value.height
  const visible = layoutNodes.filter(isVisible)
  // 命中近的优先（z 大的）
  const candidates: Array<{ ln: LayoutNode, p: ReturnType<typeof projectNode>, r: number }> = []
  for (const ln of visible) {
    const p = projectNode(ln, height, width)
    const r = ln.radius
    candidates.push({ ln, p, r })
  }
  candidates.sort((a, b) => b.p.viewZ - a.p.viewZ)
  for (const c of candidates) {
    if (hasHiddenChildren(c.ln) && c.p.alpha > 0.5) {
      const px = c.p.screenX + c.r * 0.8
      const py = c.p.screenY - c.r * 0.8
      const pr = Math.max(6, c.r * 0.35)
      if (Math.hypot(x - px, y - py) <= pr + 4) {
        return { ln: c.ln, isPlusBtn: true }
      }
    }
    if (Math.hypot(x - c.p.screenX, y - c.p.screenY) <= c.r + 4) {
      return { ln: c.ln, isPlusBtn: false }
    }
  }
  return null
}

function onMouseDown(e: MouseEvent) {
  // 拖拽已禁用；进度条控制 rotation
  e.preventDefault()
}

function onProgressInput(e: Event) {
  const v = parseFloat((e.target as HTMLInputElement).value)
  rotationProgress.value = isNaN(v) ? 0 : v
  rotation = rotationProgress.value * Math.PI * 2
  draw()
}

const effectiveMaxDepth = computed(() => {
  if (selectedDepth3Id.value) return selectedExpandedDepth
  return visibleMaxDepth
})

const selectedNodeName = computed(() => {
  if (!selectedDepth3Id.value) return ''
  const n = findNode(selectedDepth3Id.value)
  return n?.name || ''
})

const rotationDeg = computed(() => (rotationProgress.value * 360))

function onClick(e: MouseEvent) {
  // 拖拽已禁用；纯点击
  const hit = findLayoutAt(e.clientX, e.clientY)

  if (!hit) {
    clearSelection()
    return
  }
  if (hit.isPlusBtn) {
    expandedNodeIds.add(hit.ln.node.id)
    draw()
    return
  }
  // 第 3 层节点：选中并进入该子树展开模式
  if (hit.ln.depth === 2) {
    selectedDepth3Id.value = hit.ln.node.id
    selectedExpandedDepth = 3
    draw()
    return
  }
  // 选中状态下点同一节点：清除选择
  if (selectedDepth3Id.value && hit.ln.node.id === selectedDepth3Id.value) {
    clearSelection()
    return
  }
  // 节点本体：跳转
  if (hit.ln.node.link) {
    router.push(hit.ln.node.link)
  }
}

function clearSelection() {
  selectedDepth3Id.value = null
  selectedExpandedDepth = DEFAULT_MAX_DEPTH
  draw()
}

function setMaxDepth(lvl: number) {
  if (!selectedDepth3Id.value) return
  selectedExpandedDepth = lvl
  draw()
}

function resize() {
  if (!canvasRef.value || !wrapperRef.value) return
  const rect = wrapperRef.value.getBoundingClientRect()
  const railWidth = 60
  canvasRef.value.width = Math.max(320, Math.floor(rect.width - railWidth))
  canvasRef.value.height = Math.max(500, Math.floor(rect.height))
  resolveWorld(canvasRef.value.width, canvasRef.value.height)
  draw()
}

onMounted(() => {
  if (canvasRef.value) {
    ctx = canvasRef.value.getContext('2d')
    buildLayout()
    resize()
    window.addEventListener('resize', resize)
  }
})

onUnmounted(() => {
  window.removeEventListener('resize', resize)
})

watch(() => props.tree, () => {
  visibleMaxDepth = DEFAULT_MAX_DEPTH
  selectedExpandedDepth = DEFAULT_MAX_DEPTH
  expandedNodeIds.clear()
  selectedDepth3Id.value = null
  rotation = 0
  buildLayout()
  resolveWorld(canvasRef.value?.width || 600, canvasRef.value?.height || 600)
  draw()
}, { deep: true })
</script>

<style scoped>
.skill-tree-container {
  background: white;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
  overflow: hidden;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.tree-header {
  padding: 12px 16px;
  border-bottom: 1px solid var(--border);
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-shrink: 0;
}
.tree-header h3 { font-size: 14px; font-weight: 600; margin: 0; }
.tree-hint { font-size: 11px; color: var(--text-secondary); }

.tree-body {
  flex: 1 1 auto;
  display: flex;
  flex-direction: row;
  min-height: 0;
}

.canvas-wrapper {
  flex: 1 1 auto;
  display: flex;
  justify-content: stretch;
  align-items: stretch;
  padding: 0;
  min-width: 0;
  min-height: 0;
  position: relative;
}

canvas {
  cursor: default;
  border-radius: 0;
  width: 100%;
  height: 100%;
  display: block;
}

.rotation-bar {
  position: absolute;
  left: 16px;
  right: 16px;
  bottom: 12px;
  height: 28px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 12px;
  background: rgba(255, 255, 255, 0.92);
  border: 1px solid #e5e7eb;
  border-radius: 14px;
  box-shadow: 0 2px 6px rgba(0,0,0,0.08);
  z-index: 5;
  user-select: none;
}
.rotation-bar .label {
  font-size: 12px;
  font-weight: 600;
  color: #475569;
  white-space: nowrap;
}
.rotation-bar input[type=range] {
  flex: 1 1 auto;
  -webkit-appearance: none;
  appearance: none;
  height: 6px;
  background: linear-gradient(to right, #1e3a8a 0%, #93c5fd 50%, #1e3a8a 100%);
  border-radius: 3px;
  outline: none;
  cursor: pointer;
}
.rotation-bar input[type=range]::-webkit-slider-thumb {
  -webkit-appearance: none;
  appearance: none;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: #2563eb;
  border: 2px solid white;
  box-shadow: 0 1px 3px rgba(0,0,0,0.3);
  cursor: grab;
}
.rotation-bar input[type=range]::-webkit-slider-thumb:active { cursor: grabbing; }
.rotation-bar input[type=range]::-moz-range-thumb {
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: #2563eb;
  border: 2px solid white;
  box-shadow: 0 1px 3px rgba(0,0,0,0.3);
  cursor: grab;
}
.rotation-bar .deg {
  font-size: 12px;
  color: #64748b;
  min-width: 44px;
  text-align: right;
  font-variant-numeric: tabular-nums;
}

.selection-banner {
  position: absolute;
  top: 8px;
  left: 8px;
  background: rgba(251, 191, 36, 0.95);
  color: #1f2937;
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.15);
}
.clear-btn {
  background: white;
  border: 1px solid #d1d5db;
  border-radius: 3px;
  padding: 2px 6px;
  font-size: 11px;
  cursor: pointer;
}
.clear-btn:hover { background: #f3f4f6; }

.level-rail {
  width: 56px;
  flex-shrink: 0;
  border-left: 1px solid var(--border);
  display: flex;
  flex-direction: column;
  align-items: stretch;
  padding: 8px 0;
  gap: 6px;
  background: #fafafa;
}

.level-btn {
  margin: 0 8px;
  height: 32px;
  border: 1px solid var(--border);
  border-radius: 6px;
  background: white;
  cursor: pointer;
  font-size: 12px;
  font-weight: 600;
  color: var(--text-secondary);
  transition: all 0.15s ease;
}
.level-btn:hover:not(:disabled) { border-color: #2563eb; color: #2563eb; }
.level-btn.active { background: #2563eb; color: white; border-color: #2563eb; }
.level-btn:disabled { opacity: 0.4; cursor: not-allowed; }
</style>
