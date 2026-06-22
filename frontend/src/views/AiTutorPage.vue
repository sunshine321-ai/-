<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { askAi } from '@/api/ai'
import { clearChatMessages, getChatMessages } from '@/api/chat'

const router = useRouter()
const messages = ref([])
const question = ref('')
const loading = ref(false)

onMounted(async () => {
  const result = await getChatMessages('tutor')
  if (result.success) messages.value = result.data || []
})

async function sendMessage() {
  const content = question.value.trim()
  if (!content || loading.value) return
  messages.value.push({ role: 'user', content })
  question.value = ''
  loading.value = true
  const result = await askAi(content, 'tutor')
  loading.value = false
  if (result.success) messages.value.push({ role: 'assistant', content: result.data })
  else messages.value.push({ role: 'assistant', content: `请求失败：${result.error}` })
}

async function clearMessages() {
  const result = await clearChatMessages('tutor')
  if (result.success) messages.value = []
}
</script>

<template>
  <div class="tutor-container">
    <header>
      <button @click="router.push('/')">← 返回首页</button>
      <h1>AI 学习助手</h1>
      <button @click="clearMessages">清空记录</button>
    </header>

    <main>
      <div v-if="messages.length === 0" class="empty">可以问我任何卷积核相关问题。</div>
      <div v-for="(message, index) in messages" :key="message.id || index" :class="['message', message.role]">
        <strong>{{ message.role === 'user' ? '我' : 'AI' }}</strong>
        <p>{{ message.content }}</p>
      </div>
      <div v-if="loading" class="message assistant"><p>AI 正在思考……</p></div>
    </main>

    <form @submit.prevent="sendMessage">
      <textarea v-model="question" placeholder="请输入问题" @keydown.ctrl.enter="sendMessage"></textarea>
      <button type="submit" :disabled="loading">发送</button>
    </form>
  </div>
</template>

<style scoped>
.tutor-container { height: 100vh; max-width: 900px; margin: auto; padding: 16px; display: flex; flex-direction: column; gap: 14px; }
header { display: flex; align-items: center; justify-content: space-between; border-bottom: 1px solid var(--border); padding-bottom: 12px; }
header h1 { font-size: 1.3rem; color: var(--c-mint); }
button { padding: 8px 14px; border: 1px solid var(--border); border-radius: 8px; background: var(--surface); color: var(--text); cursor: pointer; }
main { flex: 1; overflow-y: auto; padding: 10px; border: 1px solid var(--border); border-radius: 12px; }
.empty { color: var(--text-muted); text-align: center; margin-top: 30%; }
.message { max-width: 78%; margin: 12px 0; padding: 12px 16px; border-radius: 12px; background: var(--surface); }
.message.user { margin-left: auto; background: rgba(180, 83, 9, 0.15); }
.message p { margin: 6px 0 0; white-space: pre-wrap; line-height: 1.6; }
form { display: flex; gap: 10px; }
textarea { flex: 1; min-height: 70px; padding: 12px; border: 1px solid var(--border); border-radius: 10px; resize: vertical; }
form button { min-width: 80px; background: var(--c-mint); color: white; }
</style>
