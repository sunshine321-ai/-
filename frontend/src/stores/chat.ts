import { defineStore } from 'pinia'
import { ref, watch } from 'vue'

export interface ChatMessage {
  role: 'user' | 'assistant'
  content: string
}

async function syncChatToServer(context: string, messages: ChatMessage[]) {
  try {
    await fetch('/api/chat/sync', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ context, messages }),
    })
  } catch { /* 静默失败，localStorage 兜底 */ }
}

async function loadChatFromServer(context: string): Promise<ChatMessage[] | null> {
  try {
    const resp = await fetch(`/api/chat/${context}`)
    const data = await resp.json()
    if (data.success && data.messages?.length > 0) return data.messages
  } catch { /* 网络不通时回退到 localStorage */ }
  return null
}

export const useChatStore = defineStore('chat', () => {
  const homeChat = ref<ChatMessage[]>(loadFromStorage('vlab_home_chat'))
  const tutorChat = ref<ChatMessage[]>(loadFromStorage('vlab_tutor_chat'))
  const studyChat = ref<ChatMessage[]>(loadFromStorage('vlab_study_chat'))
  const isLoading = ref(false)
  const serverLoaded = ref(false)

  function loadFromStorage(key: string): ChatMessage[] {
    try {
      const raw = localStorage.getItem(key)
      if (!raw) return []
      const parsed = JSON.parse(raw)
      return Array.isArray(parsed) ? parsed.filter((m: ChatMessage) => !(m.content || '').includes('loading-dots')) : []
    } catch { return [] }
  }

  function persist(key: string, data: ChatMessage[]) {
    localStorage.setItem(key, JSON.stringify(data))
  }

  watch(homeChat, (v) => { persist('vlab_home_chat', v); syncChatToServer('home', v) }, { deep: true })
  watch(tutorChat, (v) => { persist('vlab_tutor_chat', v); syncChatToServer('tutor', v) }, { deep: true })
  watch(studyChat, (v) => { persist('vlab_study_chat', v); syncChatToServer('study', v) }, { deep: true })

  async function initFromServer() {
    if (serverLoaded.value) return
    serverLoaded.value = true
    const [home, tutor, study] = await Promise.all([
      loadChatFromServer('home'),
      loadChatFromServer('tutor'),
      loadChatFromServer('study'),
    ])
    if (home && home.length > homeChat.value.length) homeChat.value = home
    if (tutor && tutor.length > tutorChat.value.length) tutorChat.value = tutor
    if (study && study.length > studyChat.value.length) studyChat.value = study
  }

  async function sendMessage(message: string, context: 'home' | 'tutor' | 'study') {
    if (!message.trim() || isLoading.value) return
    isLoading.value = true

    const chatRef = context === 'home' ? homeChat : context === 'tutor' ? tutorChat : studyChat
    chatRef.value.push({ role: 'user', content: message })

    try {
      const endpoint = context === 'study' ? '/api/ask' : '/ai-tutor/chat'
      const resp = await fetch(endpoint, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ message }),
      })
      const data = await resp.json()
      const reply = data.response || data.content || '抱歉，我暂时无法回复。'
      chatRef.value.push({ role: 'assistant', content: reply })
    } catch {
      chatRef.value.push({ role: 'assistant', content: '网络错误，请稍后重试。' })
    } finally {
      isLoading.value = false
    }
  }

  return { homeChat, tutorChat, studyChat, isLoading, initFromServer, serverLoaded, sendMessage }
})
