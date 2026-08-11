<template>
  <div class="skill-tree-container">
    <div class="tree-header">
      <h3>{{ tree.name }}</h3>
      <div class="tree-hint">拖拽旋转 · 点击节点跳转</div>
    </div>
    <div class="canvas-wrapper" ref="wrapperRef">
      <canvas ref="canvasRef" @mousedown="onMouseDown" @mousemove="onMouseMove" @mouseup="onMouseUp" @mouseleave="onMouseUp" @click="onClick"></canvas>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import type { SkillTree, SkillNode } from '@/types/skillTree'

interface LayoutNode {
  node: SkillNode
  x: number
  y: number
  radius: number
}

const props = defineProps<{
  tree: SkillTree
}>()

const router = useRouter()
const canvasRef = ref<HTMLCanvasElement>()
const wrapperRef = ref<HTMLDivElement>()
let ctx: CanvasRenderingContext2D | null = null
let rotation = 0
let isDragging = false
let lastX = 0
let layoutNodes: LayoutNode[] = []
let animationFrame: number | null = null

const NODE_RADIUS = [28, 18, 14, 12, 10, 8, 7]
const LEVEL_SPACING = 65
const BRANCH_SPREAD = [0, 0.8, 0.6, 0.5, 0.4, 0.35, 0.3]

function getNodeRadius(level: number): number {
  return NODE_RADIUS[Math.min(level, NODE_RADIUS.length - 1)]
}

function calculateLayout() {
  layoutNodes = []
  const root = props.tree.root
  const cx = 0
  const cy = 0

  function traverse(node: SkillNode, level: number, startAngle: number, endAngle: number) {
    const angle = (startAngle + endAngle) / 2
    const radius = level * LEVEL_SPACING
    const x = cx + radius * Math.cos(angle)
    const y = cy + radius * Math.sin(angle)

    layoutNodes.push({ node, x, y, radius: getNodeRadius(level) })

    if (node.children && level < 6) {
      const spread = BRANCH_SPREAD[Math.min(level + 1, BRANCH_SPREAD.length - 1)]
      const totalAngle = endAngle - startAngle
      const childCount = node.children.length
      const childSpread = totalAngle * spread / childCount

      let currentAngle = startAngle + (totalAngle - childSpread * (childCount - 1)) / 2
      node.children.forEach(child => {
        const childEndAngle = currentAngle + childSpread
        traverse(child, level + 1, currentAngle, childEndAngle)
        currentAngle = childEndAngle
      })
    }
  }

  traverse(root, 0, 0, Math.PI * 2)
}

function draw() {
  if (!ctx || !canvasRef.value) return

  const canvas = canvasRef.value
  const width = canvas.width
  const height = canvas.height
  const cx = width / 2
  const cy = height / 2

  ctx.clearRect(0, 0, width, height)

  ctx.save()
  ctx.translate(cx, cy)
  ctx.rotate(rotation)
  ctx.translate(-cx, -cy)

  // Draw connections first
  layoutNodes.forEach(layoutNode => {
    const parentNode = findParent(layoutNode.node)
    if (parentNode) {
      const parentLayout = layoutNodes.find(ln => ln.node === parentNode)
      if (parentLayout) {
        ctx!.beginPath()
        ctx!.moveTo(parentLayout.x, parentLayout.y)
        ctx!.lineTo(layoutNode.x, layoutNode.y)
        ctx!.strokeStyle = layoutNode.node.color || '#d1d5db'
        ctx!.lineWidth = Math.max(1, 3 - layoutNode.node.level * 0.4)
        ctx!.stroke()
      }
    }
  })

  // Draw nodes
  layoutNodes.forEach(layoutNode => {
    const { node, x, y, radius } = layoutNode

    ctx!.beginPath()
    ctx!.arc(x, y, radius, 0, Math.PI * 2)
    ctx!.fillStyle = node.color || '#2563eb'
    ctx!.fill()
    ctx!.strokeStyle = 'white'
    ctx!.lineWidth = 2
    ctx!.stroke()

    // Draw text
    ctx!.fillStyle = 'white'
    ctx!.textAlign = 'center'
    ctx!.textBaseline = 'middle'
    const fontSize = Math.max(8, Math.min(12 - node.level, 10))
    ctx!.font = `bold ${fontSize}px -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif`
    ctx!.fillText(node.name, x, y, radius * 1.8)
  })

  ctx.restore()
}

function findParent(node: SkillNode): SkillNode | null {
  function search(parent: SkillNode): boolean {
    if (parent.children?.includes(node)) return true
    return parent.children?.some(child => search(child)) || false
  }

  function find(parent: SkillNode): SkillNode | null {
    if (parent.children?.includes(node)) return parent
    for (const child of parent.children || []) {
      const found = find(child)
      if (found) return found
    }
    return null
  }

  return find(props.tree.root)
}

function getNodeAtPosition(clientX: number, clientY: number): LayoutNode | null {
  if (!canvasRef.value) return null
  const rect = canvasRef.value.getBoundingClientRect()
  const x = clientX - rect.left
  const y = clientY - rect.top
  const width = canvasRef.value.width
  const height = canvasRef.value.height
  const cx = width / 2
  const cy = height / 2

  // Transform to rotated coordinates
  const dx = x - cx
  const dy = y - cy
  const angle = -rotation
  const rx = dx * Math.cos(angle) - dy * Math.sin(angle) + cx
  const ry = dx * Math.sin(angle) + dy * Math.cos(angle) + cy

  for (const layoutNode of layoutNodes) {
    const dist = Math.sqrt((rx - layoutNode.x) ** 2 + (ry - layoutNode.y) ** 2)
    if (dist <= layoutNode.radius + 4) {
      return layoutNode
    }
  }
  return null
}

function onMouseDown(e: MouseEvent) {
  isDragging = true
  lastX = e.clientX
}

function onMouseMove(e: MouseEvent) {
  if (!isDragging) return
  const deltaX = e.clientX - lastX
  rotation += deltaX * 0.005
  lastX = e.clientX
  draw()
}

function onMouseUp() {
  isDragging = false
}

function onClick(e: MouseEvent) {
  if (isDragging) return
  const layoutNode = getNodeAtPosition(e.clientX, e.clientY)
  if (layoutNode && layoutNode.node.link) {
    router.push(layoutNode.node.link)
  }
}

function resize() {
  if (!canvasRef.value || !wrapperRef.value) return
  const { width, height } = wrapperRef.value.getBoundingClientRect()
  const size = Math.min(width, height, 500)
  canvasRef.value.width = size
  canvasRef.value.height = size
  calculateLayout()
  draw()
}

onMounted(() => {
  if (canvasRef.value) {
    ctx = canvasRef.value.getContext('2d')
    resize()
    window.addEventListener('resize', resize)
  }
})

onUnmounted(() => {
  window.removeEventListener('resize', resize)
  if (animationFrame) cancelAnimationFrame(animationFrame)
})

watch(() => props.tree, () => {
  rotation = 0
  calculateLayout()
  draw()
}, { deep: true })
</script>

<style scoped>
.skill-tree-container {
  background: white;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
  overflow: hidden;
}

.tree-header {
  padding: 12px 16px;
  border-bottom: 1px solid var(--border);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.tree-header h3 {
  font-size: 14px;
  font-weight: 600;
  margin: 0;
}

.tree-hint {
  font-size: 11px;
  color: var(--text-secondary);
}

.canvas-wrapper {
  padding: 16px;
  display: flex;
  justify-content: center;
  align-items: center;
}

canvas {
  cursor: grab;
  border-radius: 8px;
}

canvas:active {
  cursor: grabbing;
}
</style>
