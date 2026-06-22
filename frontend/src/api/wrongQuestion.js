import { API_BASE } from '@/config/api'

export const getWrongQuestions = async () => {
  try {
    const response = await fetch(`${API_BASE}/wrong-questions`)
    const data = await response.json()
    return response.ok && data.success
      ? { success: true, data: data.data }
      : { success: false, error: data.msg || '读取错题失败' }
  } catch (error) {
    return { success: false, error: error.message }
  }
}

export const createWrongQuestion = async (question) => {
  try {
    const response = await fetch(`${API_BASE}/wrong-questions`, {
      method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(question)
    })
    const data = await response.json()
    return response.ok && data.success
      ? { success: true, data: data.data }
      : { success: false, error: data.msg || '保存错题失败' }
  } catch (error) {
    return { success: false, error: error.message }
  }
}

export const deleteWrongQuestion = async (id) => {
  const response = await fetch(`${API_BASE}/wrong-questions/${id}`, { method: 'DELETE' })
  return response.json()
}

export const clearWrongQuestions = async () => {
  const response = await fetch(`${API_BASE}/wrong-questions`, { method: 'DELETE' })
  return response.json()
}
