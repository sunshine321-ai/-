import { API_BASE } from '@/config/api'

export const getStudyNote = async () => {
  try {
    const response = await fetch(`${API_BASE}/notes`)
    const data = await response.json()

    if (response.ok && data.success) {
      return { success: true, data: data.data }
    }

    return { success: false, error: data.msg || '获取学习笔记失败' }
  } catch (error) {
    return { success: false, error: error.message }
  }
}

export const saveStudyNote = async (content) => {
  try {
    const response = await fetch(`${API_BASE}/notes`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ content })
    })
    const data = await response.json()

    if (response.ok && data.success) {
      return { success: true, data: data.data }
    }

    return { success: false, error: data.msg || '保存学习笔记失败' }
  } catch (error) {
    return { success: false, error: error.message }
  }
}
