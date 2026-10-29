/**
 * 匿名设备 ID: 用 localStorage 持久化, 所有 widget 共享.
 * 浏览器首次访问时生成, 永不过期 (除非用户清缓存).
 */
const KEY = 'pureblog_device_id'

export function getDeviceId(): string {
  let id = localStorage.getItem(KEY)
  if (!id) {
    id = (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function')
      ? crypto.randomUUID()
      : 'dev-' + Math.random().toString(36).slice(2) + '-' + Date.now().toString(36)
    localStorage.setItem(KEY, id)
  }
  return id
}
