<template>
  <div class="skill-tree-container">
    <div class="tree-header">
      <h3>{{ tree.name }}</h3>
    </div>
    <div class="tree-body">
      <div class="canvas-wrapper" ref="wrapperRef">
        <canvas
          ref="canvasRef"
          @click="onClick"
        ></canvas>
        <div v-if="selectedDepth3Id" class="selection-banner">
          <span>{{ selectedNodeName }}</span>
          <button class="clear-btn" @click="clearSelection">清除选择</button>
        </div>
        <!-- 深度轴：刻度与画布层高一一对应，拖动切面手柄改变可见深度 -->
        <div class="depth-axis">
          <div
            v-if="selectionBandStyle"
            class="axis-selection-band"
            :style="selectionBandStyle"
          ></div>
          <div
            v-for="lvl in depthLevels"
            :key="lvl"
            :class="['axis-tick', { faded: lvl > visibleMaxDepth, active: lvl === visibleMaxDepth }]"
            :style="tickStyle(lvl)"
            :title="`显示到第 ${lvl} 层（该层共 ${depthCounts[lvl] ?? 0} 个节点）`"
            @click.stop="setMaxDepth(lvl)"
          >
            <span class="axis-tick-line"></span>
            <span class="axis-tick-label">L{{ lvl }}</span>
            <span class="axis-tick-count">{{ depthCounts[lvl] ?? 0 }}</span>
          </div>
          <div
            v-for="lvl in rotLevels"
            :key="`rot-${lvl}`"
            :class="['layer-rot', { faded: lvl > visibleMaxDepth }]"
            :style="layerRotStyle(lvl)"
          >
            <div
              class="rot-track"
              :style="{ height: rotTrackHeight(lvl) + 'px' }"
              title="拖动旋转该层（相对上一层），双击归零"
              @pointerdown.stop="startLayerDrag(lvl, $event)"
              @dblclick.stop="resetLayerOffset(lvl)"
            >
              <div class="rot-knob" :style="rotKnobStyle(lvl)"></div>
            </div>
            <span class="rot-val">{{ formatDeg(layerOffsets[lvl] || 0) }}</span>
          </div>
          <div
            class="axis-handle"
            :style="handleStyle"
            title="拖动调整可见深度"
            @pointerdown.stop="onHandlePointerDown"
          >
            <span class="handle-grip"></span>
          </div>
        </div>
      </div>
      <div class="rotation-bar">
        <span class="label">整体旋转</span>
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

const NODE_RADIUS = [36, 22, 18, 15, 13, 11, 10]

// 深度轴占用的画布右侧水槽宽度（px），画布内容区宽度 = 画布宽 - 该值
// 左 68px 给刻度文字，右侧留给各层迷你旋转控件
const RAIL_WIDTH = 150
const RAIL_TICK_WIDTH = 68

// 深度轴纵向布局与 resolveWorld 的 Y 公式保持一致（比例）
const AXIS_TOP_MARGIN = 0.06
const AXIS_VERTICAL_USABLE = 0.85

// 旋转（绕竖直 Y 轴）
let rotation = 0
// 进度条值 0..1 → rotation 0..π
const rotationProgress = ref(0)

// 各层相对上一层的旋转偏移（度），键为层深（>=2），范围 [-180, 180]
// 级联叠加：L2 相对 L1 偏移 off2，L3 相对 L2 再偏移 off3，以此类推
const layerOffsets = ref<Record<number, number>>({})

// 迷你旋转控件支持的层深（L1 由底部整体旋转条控制）
const rotLevels = computed(() => {
  const out: number[] = []
  for (let d = 2; d <= maxDepth.value; d++) out.push(d)
  return out
})

// 真实层深信息：由 layoutNodes 统计，不写死
const maxDepth = ref(0)
const depthCounts = ref<Record<number, number>>({})

// 全局切面：显示到第几层。默认展示全部（= 真实 maxDepth）
const visibleMaxDepth = ref(0)

const expandedNodeIds = new Set<string>()

// 选中节点（仅 depth>=2 可选）→ 该子树穿透切面完整显示
const selectedDepth3Id = ref<string | null>(null)
// 选中子树可见到的深度上限
const selectionRevealDepth = ref(0)

// ---------- 聚焦（飞入正前方） ----------
// 每个节点的聚焦混合值：0=在圆环原位, 1=完全飞入正前方（可略微 >1 形成回弹冲过）
interface FocusAnim {
  value: number
  from: number
  to: number
  startTs: number
  duration: number
  ease: (t: number) => number
  pulse: number
  done: boolean
}
const focusAnims = new Map<string, FocusAnim>()
// 当前聚焦的节点 id（落位后画呼吸光晕）
const focusTargetId = ref<string | null>(null)
let focusAnimRaf = 0
// 呼吸光晕节流：上次重绘时间戳
let lastPulseDrawTs = 0

const FOCUS_DURATION = 520      // 单程动画时长(ms)
const FOCUS_FRONT_SCALE = 1.15  // 飞入正前方后的额外放大（近大远小）
const FOCUS_PULSE = 0.25        // 飞行途中的缩放脉冲幅度
// 聚焦节点在相机空间的虚拟半径：用于深度排序（保证画在最前）与缩放基准
const FOCUS_VIRTUAL_R = 400

// 缓动：飞入用 easeOutBack（冲过中心再弹回 = duang），退回用 easeInOutQuad
function easeOutBack(t: number): number {
  const c1 = 1.70158
  const c3 = c1 + 1
  const p = t - 1
  return 1 + c3 * p * p * p + c1 * p * p
}
function easeInOutQuad(t: number): number {
  return t < 0.5 ? 2 * t * t : 1 - Math.pow(-2 * t + 2, 2) / 2
}

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

// 是否可见：全局切面深度 + 单点局部展开 + 选中子树穿透切面
function isVisible(ln: LayoutNode): boolean {
  if (ln.depth <= visibleMaxDepth.value) return true
  // 单点局部展开：祖先被点过「+」则整条子树可见
  let cur: LayoutNode | null = ln
  while (cur) {
    if (expandedNodeIds.has(cur.node.id)) return true
    cur = layoutNodes.find(l => l.node === cur!.parent) || null
  }
  // 选中子树穿透切面：仅该子树内可见到 selectionRevealDepth
  if (selectedDepth3Id.value) {
    const sel = findLayoutByNodeId(selectedDepth3Id.value)
    if (sel && isDescendantOf(ln, sel)) {
      return ln.depth <= selectionRevealDepth.value
    }
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
  refreshDepthStats()
}

// 统计真实层深：最大深度 + 每层节点数，供深度轴使用
function refreshDepthStats() {
  const counts: Record<number, number> = {}
  let deepest = 0
  for (const ln of layoutNodes) {
    counts[ln.depth] = (counts[ln.depth] || 0) + 1
    if (ln.depth > deepest) deepest = ln.depth
  }
  depthCounts.value = counts
  maxDepth.value = deepest
}

function resolveWorld(canvasWidth: number, canvasHeight: number) {
  if (!layoutNodes.length) return
  // 按 depth 分组
  const byDepth = new Map<number, LayoutNode[]>()
  for (const ln of layoutNodes) {
    if (!byDepth.has(ln.depth)) byDepth.set(ln.depth, [])
    byDepth.get(ln.depth)!.push(ln)
  }
  // 层深跨度：与深度轴共用同一来源（真实最大深度）
  const depthSpan = Math.max(maxDepth.value, 1)
  // 垂直：根在底部、叶子在顶部，按画布高度 0.85 比例铺
  const verticalUsable = canvasHeight * AXIS_VERTICAL_USABLE
  const topMargin = canvasHeight * AXIS_TOP_MARGIN
  const bottomY = topMargin + verticalUsable
  // 水平：内容区宽度扣掉右侧深度轴水槽，圆环最大半径取宽高较小者，保证不超出画布
  const contentW = Math.max(canvasWidth - RAIL_WIDTH, 1)
  const maxR = Math.min(contentW / 2, canvasHeight / 2) * 0.78
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
    if (depth === 1) {
      const r = maxR * (1 - (depth - 1) / depthSpan)
      const y = topMargin + verticalUsable * (1 - depth / depthSpan)
      // depth=1：从根节点（中心）直接展开，保持全局均匀铺满整圆
      nodes.forEach((ln, i) => {
        ln.worldAngle = (i / Math.max(nodes.length, 1)) * Math.PI * 2
        ln.worldR = r
        ln.worldY = y
      })
    } else {
      const r = maxR * (1 - (depth - 1) / depthSpan)
      const y = topMargin + verticalUsable * (1 - depth / depthSpan)
      // depth>=2：子节点从父节点方向继承并展开，不全局铺开
      const parents = byDepth.get(depth - 1) ?? []
      // 按父节点分组子节点
      const childrenByParent = new Map<string, LayoutNode[]>()
      for (const ln of nodes) {
        const pid = ln.parent?.id ?? ''
        if (!childrenByParent.has(pid)) childrenByParent.set(pid, [])
        childrenByParent.get(pid)!.push(ln)
      }
      // 每个父节点占父层整圆的一个扇区，子节点在该扇区内均匀分布
      const parentArc = Math.PI * 2 / Math.max(parents.length, 1)
      for (const parentLn of parents) {
        const children = childrenByParent.get(parentLn.node.id) ?? []
        if (children.length === 0) continue
        const seg = parentArc / children.length
        const parentAngle = parentLn.worldAngle
        children.forEach((ln, i) => {
          // 从父节点角度开始，在扇区内均匀排开；单子节点则与父同向
          ln.worldAngle = parentAngle - parentArc / 2 + seg * (i + 0.5)
          ln.worldR = r
          ln.worldY = y
        })
      }
    }
  }
}

// 节点所在层级的绝对旋转角（度）：整体旋转 + 各层相对偏移的级联累加
// cumulativeOffsets[depth] = off2 + off3 + ... + off(depth)
const cumulativeOffsets = computed(() => {
  const out: number[] = [0, 0]
  let acc = 0
  for (let d = 2; d <= maxDepth.value; d++) {
    acc += layerOffsets.value[d] || 0
    out.push(acc)
  }
  return out
})

function absoluteDegFor(depth: number): number {
  return (rotation * 180) / Math.PI + (cumulativeOffsets.value[depth] || 0)
}

// 3D 投影：节点呈圆环分布在水平面上，绕竖直 Y 轴旋转
// 拖动进度条时，节点 screenX 真的会左右移动；背面 viewZ<0 节点 α 低、正面高
function projectNode(ln: LayoutNode, height: number, canvasW: number) {
  // 内容区中心：扣掉右侧深度轴水槽，避免圆环被轴压住
  const cx = Math.max(canvasW - RAIL_WIDTH, 1) / 2
  // 垂直中心：聚焦节点飞入的目标高度
  const cy = height / 2

  // ---------- 聚焦混合 ----------
  // f = 0 → 完全在圆环原位；f = 1 → 完全脱离圆环、正对相机（正前方）
  const anim = focusAnims.get(ln.node.id)
  const f = anim ? anim.value : 0
  const isFocused = f > 0.0001

  // 三维坐标（绕 Y 轴）
  const x3 = Math.sin(ln.worldAngle) * ln.worldR
  const z3 = Math.cos(ln.worldAngle) * ln.worldR   // 初始指向相机为正
  // 绕 Y 轴旋转：整体旋转 + 该层级联叠加的相对偏移
  const rot = (absoluteDegFor(ln.depth) * Math.PI) / 180
  const cosR = Math.cos(rot)
  const sinR = Math.sin(rot)
  let viewX = x3 * cosR + z3 * sinR
  let viewZ = -x3 * sinR + z3 * cosR
  let screenX = cx + viewX
  let screenY = ln.worldY
  let worldR = ln.worldR

  if (isFocused) {
    // 目标位姿：脱离圆环、正对相机轴
    // 在「相机空间」插值到 (viewX=0, viewZ=focusR)：
    //   viewX=0 → screenX 恒为 cx，转动旋转条也不会歪（这才是真正的"正前方"）
    //   viewZ 很大 → 深度排序时永远画在最前，不会被其他球盖住
    const tViewX = 0
    const tViewZ = FOCUS_VIRTUAL_R
    viewX = viewX * (1 - f) + tViewX * f
    viewZ = viewZ * (1 - f) + tViewZ * f
    screenX = cx + viewX
    // 垂直方向：飞向画面正中
    screenY = ln.worldY * (1 - f) + cy * f
    worldR = ln.worldR * (1 - f)
  }

  // 聚焦节点恒为不透明
  if (isFocused) {
    // settled：动画已结束且停在正前方（回弹过冲途中不算，否则光晕会闪）
    const settled = !!anim && anim.done && anim.to === 1
    return { screenX, screenY, alpha: 1, viewZ, worldR, focusF: f, settled }
  }

  // 背面(viewZ<0)α 低、正面(viewZ>0)α 高；r=0 节点（根）恒为 1
  const alpha = worldR <= 0
    ? 1
    : Math.max(0.08, Math.min(1, (viewZ + worldR) / (2 * worldR)))
  return { screenX, screenY, alpha, viewZ, worldR, focusF: f, settled: false }
}

function draw() {
  if (!ctx || !canvasRef.value) return
  const width = canvasRef.value.width
  const height = canvasRef.value.height
  ctx.clearRect(0, 0, width, height)
  ctx.fillStyle = '#fafafa'
  ctx.fillRect(0, 0, width, height)

  const cx = Math.max(width - RAIL_WIDTH, 1) / 2

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
  type Proj = { ln: LayoutNode, x: number, y: number, z: number, alpha: number, r: number, focusF: number, settled: boolean }
  const projs: Proj[] = visible.map(ln => {
    const p = getRenderPos(ln, height, width)
    return {
      ln,
      x: p.x, y: p.y, z: p.viewZ,
      alpha: nodeAlpha(ln, p.alpha),
      r: p.r,
      focusF: p.focusF,
      settled: p.settled,
    }
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
    let lineA = Math.max(aA, bA) * 0.85
    // 聚焦节点脱离圆环，连线会跨越画面 → 淡化处理，避免突兀
    if (a.settled || b.settled) lineA *= 0.45
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
    // 已落位在正前方的聚焦节点：呼吸光晕
    const isFocused = p.settled

    // 聚焦节点的光晕（外扩半透明圆，缓慢脉动）
    if (isFocused) {
      const pulse = 0.5 + 0.5 * Math.sin(performance.now() / 420)
      const glowR = r * (1.18 + pulse * 0.22)
      ctx!.beginPath()
      ctx!.arc(p.x, p.y, glowR, 0, Math.PI * 2)
      ctx!.fillStyle = hexWithAlpha('#fbbf24', 0.16 + pulse * 0.14)
      ctx!.fill()
    }

    // 选中高亮边框
    const isSel = selectedDepth3Id.value && p.ln.node.id === selectedDepth3Id.value
    ctx!.beginPath()
    ctx!.arc(p.x, p.y, r, 0, Math.PI * 2)
    ctx!.fillStyle = hexWithAlpha(nodeFill, p.alpha)
    ctx!.fill()

    if (isSel || isFocused) {
      ctx!.strokeStyle = '#fbbf24'
      ctx!.lineWidth = isFocused ? 3.5 : 3
    } else {
      ctx!.strokeStyle = hexWithAlpha('white', p.alpha)
      ctx!.lineWidth = 2
    }
    ctx!.stroke()

    if (r >= 9) {
      ctx!.fillStyle = hexWithAlpha('white', p.alpha)
      ctx!.textAlign = 'center'
      ctx!.textBaseline = 'middle'
      const fontSize = Math.max(8, r * 0.42)
      ctx!.font = `bold ${fontSize}px 'Microsoft YaHei', 'PingFang SC', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif`
      ctx!.fillText(p.ln.node.name, p.x, p.y, r * 1.6)
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
    visibleMaxDepth: visibleMaxDepth.value,
    maxDepth: maxDepth.value,
    selectedDepth3Id: selectedDepth3Id.value,
    selectionRevealDepth: selectionRevealDepth.value,
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

// 统一渲染坐标与拾取坐标：聚焦节点的缩放（正前方放大 + 飞行脉冲）必须两者一致
function getRenderPos(ln: LayoutNode, height: number, width: number) {
  const p = projectNode(ln, height, width)
  const anim = focusAnims.get(ln.node.id)
  const focusF = p.focusF
  const scale = focusF > 0
    ? 1 + (FOCUS_FRONT_SCALE - 1) * focusF + (anim ? anim.pulse : 0)
    : 1
  return { x: p.screenX, y: p.screenY, r: ln.radius * scale, viewZ: p.viewZ, alpha: p.alpha, focusF, settled: p.settled }
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
  const candidates: Array<{ ln: LayoutNode, p: ReturnType<typeof getRenderPos> }> = []
  for (const ln of visible) {
    candidates.push({ ln, p: getRenderPos(ln, height, width) })
  }
  candidates.sort((a, b) => b.p.viewZ - a.p.viewZ)
  for (const c of candidates) {
    if (hasHiddenChildren(c.ln) && !c.p.settled) {
      const px = c.p.x + c.p.r * 0.8
      const py = c.p.y - c.p.r * 0.8
      const pr = Math.max(6, c.p.r * 0.35)
      if (Math.hypot(x - px, y - py) <= pr + 4) {
        return { ln: c.ln, isPlusBtn: true }
      }
    }
    if (Math.hypot(x - c.p.x, y - c.p.y) <= c.p.r + 4) {
      return { ln: c.ln, isPlusBtn: false }
    }
  }
  return null
}

function onProgressInput(e: Event) {
  const v = parseFloat((e.target as HTMLInputElement).value)
  rotationProgress.value = isNaN(v) ? 0 : v
  rotation = rotationProgress.value * Math.PI * 2
  draw()
}

/* ---------- 聚焦动画 ---------- */

// 启动飞入：节点脱离圆环、正对相机，带 duang 回弹
function focusNode(id: string) {
  const ln = findLayoutByNodeId(id)
  if (!ln) return
  const prev = focusAnims.get(id)
  focusAnims.set(id, {
    value: prev ? prev.value : 0,
    from: prev ? prev.value : 0,
    to: 1,
    startTs: performance.now(),
    duration: FOCUS_DURATION,
    ease: easeOutBack,
    pulse: 0,
    done: false,
  })
  focusTargetId.value = id
  startFocusAnim()
}

// 退回圆环原位
function unfocusNode(id: string) {
  const prev = focusAnims.get(id)
  if (!prev) return
  focusAnims.set(id, {
    value: prev.value,
    from: prev.value,
    to: 0,
    startTs: performance.now(),
    duration: FOCUS_DURATION,
    ease: easeInOutQuad,
    pulse: 0,
    done: false,
  })
  if (focusTargetId.value === id) focusTargetId.value = null
  startFocusAnim()
}

// 动画循环：动画期间逐帧重绘；落位后转为低频脉动重绘（呼吸光晕）
function startFocusAnim() {
  if (focusAnimRaf) return
  const step = () => {
    const now = performance.now()
    let running = false
    focusAnims.forEach((anim, id) => {
      if (anim.done) return
      const t = Math.min(1, (now - anim.startTs) / anim.duration)
      const eased = anim.ease(t)
      anim.value = anim.from + (anim.to - anim.from) * eased
      // 飞行途中的缩放脉冲：sin 曲线，起止时为 0
      anim.pulse = Math.sin(t * Math.PI) * FOCUS_PULSE
      if (t >= 1) {
        anim.value = anim.to
        anim.pulse = 0
        anim.done = true
        if (anim.to === 0) focusAnims.delete(id)
      } else {
        running = true
      }
    })
    if (running) {
      // 动画进行中：逐帧重绘
      draw()
      focusAnimRaf = requestAnimationFrame(step)
    } else if (focusTargetId.value) {
      // 已落位：呼吸光晕，约 15fps 足够，避免满帧空耗
      if (now - lastPulseDrawTs > 66) {
        lastPulseDrawTs = now
        draw()
      }
      focusAnimRaf = requestAnimationFrame(() => {
        focusAnimRaf = 0
        startFocusAnim()
      })
    } else {
      focusAnimRaf = 0
    }
  }
  focusAnimRaf = requestAnimationFrame(step)
}

// 清除所有聚焦状态（切换树时用）
function clearAllFocus() {
  focusAnims.clear()
  focusTargetId.value = null
  if (focusAnimRaf) {
    cancelAnimationFrame(focusAnimRaf)
    focusAnimRaf = 0
  }
}

const selectedNodeName = computed(() => {
  if (!selectedDepth3Id.value) return ''
  const n = findNode(selectedDepth3Id.value)
  return n?.name || ''
})

const rotationDeg = computed(() => (rotationProgress.value * 360))

/* ---------- 深度轴 ---------- */

// 轴上的刻度列表：0..maxDepth
const depthLevels = computed(() => {
  const out: number[] = []
  for (let d = 0; d <= maxDepth.value; d++) out.push(d)
  return out
})

// 层 d 在轴上的纵向位置（百分比），与 resolveWorld 的 Y 公式同源
function levelRatio(depth: number): number {
  const span = Math.max(maxDepth.value, 1)
  return AXIS_TOP_MARGIN + AXIS_VERTICAL_USABLE * (1 - depth / span)
}

function tickStyle(depth: number) {
  return { top: `${levelRatio(depth) * 100}%` }
}

const handleStyle = computed(() => ({
  top: `${levelRatio(visibleMaxDepth.value) * 100}%`,
}))

// 选中子树的节点色窄带：从切面一直延伸到该子树触及的最深层
const selectionBandStyle = computed(() => {
  if (!selectedDepth3Id.value) return null
  const sel = findLayoutByNodeId(selectedDepth3Id.value)
  if (!sel) return null
  // 选中子树实际触及的最深深度
  let deepest = sel.depth
  for (const ln of layoutNodes) {
    if (isDescendantOf(ln, sel) && ln.depth > deepest) deepest = ln.depth
  }
  const top = levelRatio(deepest)
  const bottom = levelRatio(visibleMaxDepth.value)
  if (bottom <= top) return null
  return {
    top: `${top * 100}%`,
    height: `${(bottom - top) * 100}%`,
    background: sel.node.color || '#2563eb',
  }
})

function setMaxDepth(lvl: number) {
  const clamped = Math.max(0, Math.min(lvl, maxDepth.value))
  if (visibleMaxDepth.value === clamped) return
  visibleMaxDepth.value = clamped
  draw()
}

/* ---------- 各层迷你旋转控件 ---------- */

// 画布高度（px），用于换算轨道像素尺寸，随 resize 更新
const canvasH = ref(600)

// 层 d 在轴上的像素纵坐标
function levelY(depth: number): number {
  return levelRatio(depth) * canvasH.value
}

// 控件垂直位置：与该层刻度对齐
function layerRotStyle(lvl: number) {
  return { top: `${levelRatio(lvl) * 100}%` }
}

// 轨道高度：取相邻层间距的一部分，限制在 [28, 70]px，避免层数多时控件重叠
function rotTrackHeight(lvl: number): number {
  const span = Math.max(maxDepth.value, 1)
  const y = levelY(lvl)
  const above = lvl < maxDepth.value ? y - levelY(lvl + 1) : Infinity
  const below = lvl > 0 ? levelY(lvl - 1) - y : Infinity
  const gap = Math.min(above, below)
  const usable = Number.isFinite(gap) ? gap * 0.7 : 80
  return Math.max(28, Math.min(70, usable))
}

// 旋钮位置：偏移 -180°(底) ~ +180°(顶)
function rotKnobStyle(lvl: number) {
  const off = layerOffsets.value[lvl] || 0
  const pct = (1 - (off + 180) / 360) * 100
  return { top: `${Math.max(0, Math.min(100, pct))}%` }
}

function formatDeg(deg: number): string {
  return `${Math.round(deg)}°`
}

function setLayerOffset(lvl: number, deg: number) {
  const clamped = Math.max(-180, Math.min(180, deg))
  layerOffsets.value = { ...layerOffsets.value, [lvl]: Math.round(clamped) }
  draw()
}

function resetLayerOffset(lvl: number) {
  layerOffsets.value = { ...layerOffsets.value, [lvl]: 0 }
  draw()
}

// 轨道拖拽：向上为正角度；轨道全长对应 360°
function startLayerDrag(lvl: number, e: PointerEvent) {
  e.preventDefault()
  const track = e.currentTarget as HTMLElement
  const rect = track.getBoundingClientRect()
  const startY = e.clientY
  const startOffset = layerOffsets.value[lvl] || 0
  const move = (ev: PointerEvent) => {
    const dy = startY - ev.clientY
    const delta = (dy / Math.max(rect.height, 1)) * 360
    setLayerOffset(lvl, startOffset + delta)
  }
  const up = () => {
    window.removeEventListener('pointermove', move)
    window.removeEventListener('pointerup', up)
  }
  window.addEventListener('pointermove', move)
  window.addEventListener('pointerup', up)
}

// 切面手柄拖拽：反算指针位置对应的层深
function onHandlePointerDown(e: PointerEvent) {
  e.preventDefault()
  const move = (ev: PointerEvent) => {
    const rect = wrapperRef.value?.getBoundingClientRect()
    if (!rect) return
    const rel = (ev.clientY - rect.top) / Math.max(rect.height, 1)
    const span = Math.max(maxDepth.value, 1)
    const raw = Math.round(span * (1 - (rel - AXIS_TOP_MARGIN) / AXIS_VERTICAL_USABLE))
    const clamped = Math.max(0, Math.min(raw, maxDepth.value))
    if (visibleMaxDepth.value !== clamped) {
      visibleMaxDepth.value = clamped
      draw()
    }
  }
  const up = () => {
    window.removeEventListener('pointermove', move)
    window.removeEventListener('pointerup', up)
  }
  window.addEventListener('pointermove', move)
  window.addEventListener('pointerup', up)
  move(e)
}

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
  // 第 3 层及更深（depth>=2）节点：飞入正前方 + 写 URL 筛选文章
  if (hit.ln.depth >= 2) {
    const nodeId = hit.ln.node.id
    // 再次点击同一个节点 → 退回圆环原位 + 取消筛选
    if (selectedDepth3Id.value === nodeId) {
      clearSelection()
      return
    }
    // 切换聚焦：旧节点退回，新节点飞入
    if (focusTargetId.value && focusTargetId.value !== nodeId) {
      unfocusNode(focusTargetId.value)
    }
    focusNode(nodeId)
    selectedDepth3Id.value = nodeId
    // 选中子树穿透切面：完整显示到真实最大深度
    selectionRevealDepth.value = maxDepth.value
    draw()
    router.push({ query: { nodeId, treeId: hit.ln.node.treeId } })
    return
  }
  // 选中状态下点同一节点：清除选择
  if (selectedDepth3Id.value && hit.ln.node.id === selectedDepth3Id.value) {
    clearSelection()
    return
  }
  // 节点本体：跳转（如果有显式 link，比如首屏占位）
  if (hit.ln.node.link) {
    router.push(hit.ln.node.link)
  }
}

function clearSelection() {
  // 聚焦节点退回圆环原位
  if (focusTargetId.value) {
    unfocusNode(focusTargetId.value)
  }
  selectedDepth3Id.value = null
  selectionRevealDepth.value = 0
  draw()
  // 清除 URL 上的筛选条件
  if (router.currentRoute.value.query.nodeId) {
    const q = { ...router.currentRoute.value.query }
    delete q.nodeId
    delete q.treeId
    router.push({ query: q })
  }
}

function resize() {
  if (!canvasRef.value || !wrapperRef.value) return
  const rect = wrapperRef.value.getBoundingClientRect()
  canvasRef.value.width = Math.floor(rect.width)
  canvasRef.value.height = Math.floor(rect.height)
  canvasH.value = canvasRef.value.height
  resolveWorld(canvasRef.value.width, canvasRef.value.height)
  draw()
}

onMounted(() => {
  if (canvasRef.value) {
    ctx = canvasRef.value.getContext('2d')
    buildLayout()
    // 初始展示全部层：切面 = 真实最大深度
    visibleMaxDepth.value = maxDepth.value
    resize()
    window.addEventListener('resize', resize)
  }
})

onUnmounted(() => {
  window.removeEventListener('resize', resize)
  // 停止聚焦动画循环，避免内存泄漏
  if (focusAnimRaf) {
    cancelAnimationFrame(focusAnimRaf)
    focusAnimRaf = 0
  }
})

watch(() => props.tree, () => {
  expandedNodeIds.clear()
  selectedDepth3Id.value = null
  selectionRevealDepth.value = 0
  // 切换树 → 清除所有聚焦状态
  clearAllFocus()
  rotation = 0
  rotationProgress.value = 0
  layerOffsets.value = {}
  buildLayout()
  // 树变了 → 切面重置为新的真实最大深度（展示全部）
  visibleMaxDepth.value = maxDepth.value
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
  flex: 1;
  display: flex;
  flex-direction: column;
}

.tree-header {
  padding: 10px 16px;
  border-bottom: 1px solid var(--border);
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-shrink: 0;
}
.tree-header h3 { font-size: 14px; font-weight: 600; margin: 0; }

.tree-body {
  flex: 1 1 auto;
  display: flex;
  flex-direction: column;
  min-height: 0;
  position: relative;
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
  display: block;
}

.rotation-bar {
  flex-shrink: 0;
  margin: 0 16px 8px;
  height: 28px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 12px;
  background: rgba(255, 255, 255, 0.92);
  border: 1px solid #e5e7eb;
  border-radius: 14px;
  box-shadow: 0 2px 6px rgba(0,0,0,0.08);
  z-index: 999;
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

/* ---------- 深度轴 ---------- */
.depth-axis {
  position: absolute;
  right: 0; top: 0; bottom: 0;
  width: 150px;
  border-left: 1px solid var(--border);
  background: rgba(250, 250, 250, 0.92);
  user-select: none;
  cursor: default;
  overflow: hidden;
}

/* 刻度：绝对定位，top 由 resolveWorld 同源公式换算而来 */
.axis-tick {
  position: absolute;
  left: 0; right: 82px;
  transform: translateY(-50%);
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 2px 6px 2px 18px;
  cursor: pointer;
  transition: opacity 0.15s ease, background 0.15s ease;
}
.axis-tick:hover { background: rgba(37, 99, 235, 0.07); }
.axis-tick.faded { opacity: 0.38; }

.axis-tick-line {
  width: 10px;
  height: 1px;
  background: var(--border);
  flex-shrink: 0;
  margin-left: -12px;
}
.axis-tick.active .axis-tick-line { background: var(--primary); width: 14px; height: 2px; }

.axis-tick-label {
  font-size: 11px;
  font-weight: 600;
  color: var(--text-secondary);
  font-variant-numeric: tabular-nums;
}
.axis-tick.active .axis-tick-label { color: var(--primary); }

.axis-tick-count {
  font-size: 10px;
  color: #94a3b8;
  font-variant-numeric: tabular-nums;
}
.axis-tick.active .axis-tick-count { color: var(--primary); }

/* ---------- 各层迷你旋转控件 ---------- */
/* 定位在轴右侧窄栏，与该层刻度纵向对齐 */
.layer-rot {
  position: absolute;
  left: 82px;
  width: 64px;
  transform: translateY(-50%);
  display: flex;
  align-items: center;
  gap: 6px;
  transition: opacity 0.15s ease;
}
.layer-rot.faded { opacity: 0.38; }

.rot-track {
  position: relative;
  width: 14px;
  min-height: 28px;
  background: #e2e8f0;
  border-radius: 7px;
  cursor: grab;
  touch-action: none;
}
.rot-track:active { cursor: grabbing; }
.rot-track:hover { background: #cbd5e1; }

.rot-knob {
  position: absolute;
  left: 50%;
  width: 12px;
  height: 12px;
  margin-left: -6px;
  margin-top: -6px;
  background: var(--primary);
  border: 2px solid white;
  border-radius: 50%;
  box-shadow: 0 1px 3px rgba(37, 99, 235, 0.4);
  pointer-events: none;
}

.rot-val {
  font-size: 10px;
  font-weight: 600;
  color: var(--text-secondary);
  font-variant-numeric: tabular-nums;
  min-width: 30px;
}

/* 选中子树穿透区间的节点色窄带 */
.axis-selection-band {
  position: absolute;
  left: 0;
  width: 3px;
  border-radius: 0 2px 2px 0;
  opacity: 0.75;
  pointer-events: none;
}

/* 切面手柄：只占轴左侧窄槽，不遮挡刻度文字 */
.axis-handle {
  position: absolute;
  left: 3px;
  width: 13px;
  height: 20px;
  transform: translateY(-50%);
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--primary);
  border-radius: 4px;
  box-shadow: 0 1px 4px rgba(37, 99, 235, 0.4);
  cursor: grab;
  touch-action: none;
  z-index: 2;
}
.axis-handle:active { cursor: grabbing; }

.handle-grip {
  width: 5px;
  height: 10px;
  border-left: 1px solid rgba(255, 255, 255, 0.75);
  border-right: 1px solid rgba(255, 255, 255, 0.75);
  flex-shrink: 0;
}
</style>
