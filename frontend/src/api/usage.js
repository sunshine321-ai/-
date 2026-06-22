import { API_BASE } from '@/config/api'

export const recordUsageEvent = (moduleKey, eventType, durationSeconds = 0) =>
  fetch(`${API_BASE}/usage/events`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ moduleKey, eventType, durationSeconds }),
    keepalive: true,
  }).catch((error) => console.error('使用行为记录失败', error))

export const getUsageSummary = async () => {
  const response = await fetch(`${API_BASE}/usage/summary`)
  const result = await response.json()
  return response.ok && result.success
    ? { success: true, data: result.data }
    : { success: false, error: result.msg || '使用统计读取失败' }
}
