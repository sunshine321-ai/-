import { API_BASE } from '@/config/api'

export const getChatMessages = async (context) => {
  const response = await fetch(`${API_BASE}/chats/${context}`)
  return response.json()
}

export const saveChatMessages = async (context, messages) => {
  const response = await fetch(`${API_BASE}/chats`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ context, messages })
  })
  return response.json()
}

export const clearChatMessages = async (context) => {
  const response = await fetch(`${API_BASE}/chats/${context}`, { method: 'DELETE' })
  return response.json()
}
