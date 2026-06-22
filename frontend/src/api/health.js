import { API_BASE } from '@/config/api'

export const getHealth = async () => {
  try {
    const response = await fetch(`${API_BASE}/health`)
    const data = await response.json()

    if (response.ok) {
      return { success: true, data }
    }

    return { success: false, error: data.msg || '健康检查失败' }
  } catch (error) {
    return { success: false, error: error.message }
  }
}
