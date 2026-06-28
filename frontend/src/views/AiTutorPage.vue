<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { askAi } from '@/api/ai'
import { clearChatMessages, getChatMessages } from '@/api/chat'

const router = useRouter()
const messages = ref([])
const question = ref('')
const loading = ref(false)

const demoMessages = [
  {
    role: 'user',
    content: '卷积核为什么能检测图像边缘？',
  },
  {
    role: 'assistant',
    content:
      '卷积核会在图像上滑动，把周围像素按权重相乘再求和。边缘检测核通常让一侧像素为正、另一侧为负，当明暗变化明显时输出值会变大，因此能把边缘位置凸显出来。',
  },
  {
    role: 'user',
    content: '如果我把 Sobel 核换成均值核，会发生什么？',
  },
  {
    role: 'assistant',
    content:
      'Sobel 核强调梯度变化，适合提取边缘；均值核会把邻域像素平均，适合平滑和降噪。换成均值核后，边缘会变得不那么清晰，图像整体更柔和。',
  },
]

onMounted(async () => {
  const result = await getChatMessages('tutor')
  messages.value = result.success && result.data?.length ? result.data : demoMessages
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
  <div class="tutor-page">
    <header class="tutor-header">
      <button class="ghost-button" type="button" @click="router.push('/')">返回首页</button>
      <div>
        <p class="eyebrow">Qwen · Convolution Learning Assistant</p>
        <h1>AI 助教智能问答</h1>
      </div>
      <button class="ghost-button" type="button" @click="clearMessages">清空记录</button>
    </header>

    <main class="tutor-shell">
      <aside class="prompt-panel">
        <h2>常用提问</h2>
        <button type="button" @click="question = '请用生活化例子解释卷积核。'">生活化解释</button>
        <button type="button" @click="question = '帮我比较 Sobel 核、锐化核和均值核。'">核类型对比</button>
        <button type="button" @click="question = '我做错了卷积计算题，应该怎么复习？'">错题复盘</button>
      </aside>

      <section class="chat-panel">
        <div v-if="messages.length === 0" class="empty">
          可以向 AI 助教提问卷积核、CNN、图像处理和课后练习中的问题。
        </div>
        <div
          v-for="(message, index) in messages"
          :key="message.id || index"
          :class="['message', message.role]"
        >
          <strong>{{ message.role === 'user' ? '我' : 'AI 助教' }}</strong>
          <p>{{ message.content }}</p>
        </div>
        <div v-if="loading" class="message assistant">
          <strong>AI 助教</strong>
          <p>正在思考中...</p>
        </div>
      </section>
    </main>

    <form class="composer" @submit.prevent="sendMessage">
      <textarea
        v-model="question"
        placeholder="输入你的问题，例如：为什么边缘检测核的中间列全是 0？"
        @keydown.ctrl.enter="sendMessage"
      ></textarea>
      <button type="submit" :disabled="loading">发送</button>
    </form>
  </div>
</template>

<style scoped>
.tutor-page {
  min-height: 100vh;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 18px;
  background: var(--bg);
}

.tutor-header {
  max-width: 1180px;
  width: 100%;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
}

.eyebrow {
  color: var(--text-muted);
  font-size: 0.78rem;
  letter-spacing: 0;
  text-align: center;
}

h1 {
  color: var(--c-mint);
  font-size: 2rem;
  margin-top: 4px;
}

.ghost-button,
.composer button,
.prompt-panel button {
  border: 1px solid var(--border);
  border-radius: 8px;
  background: #fffaf3;
  color: var(--text-main);
  cursor: pointer;
}

.ghost-button {
  padding: 10px 16px;
}

.tutor-shell {
  max-width: 1180px;
  width: 100%;
  margin: 0 auto;
  flex: 1;
  min-height: 0;
  display: grid;
  grid-template-columns: 260px minmax(0, 1fr);
  gap: 18px;
}

.prompt-panel,
.chat-panel,
.composer {
  border: 1px solid var(--border);
  border-radius: 12px;
  background: rgba(255, 250, 243, 0.9);
  box-shadow: var(--shadow-2);
}

.prompt-panel {
  padding: 18px;
  height: fit-content;
}

.prompt-panel h2 {
  font-size: 1rem;
  color: var(--c-coral);
  margin-bottom: 12px;
}

.prompt-panel button {
  width: 100%;
  padding: 12px;
  margin-bottom: 10px;
  text-align: left;
}

.chat-panel {
  min-height: 560px;
  padding: 18px;
  overflow-y: auto;
}

.empty {
  color: var(--text-muted);
  text-align: center;
  margin-top: 180px;
}

.message {
  max-width: 78%;
  margin: 12px 0;
  padding: 14px 16px;
  border-radius: 10px;
  background: #fffaf3;
  border: 1px solid var(--border);
}

.message.user {
  margin-left: auto;
  background: rgba(217, 119, 6, 0.14);
}

.message.assistant {
  border-left: 4px solid var(--c-coral);
}

.message strong {
  color: var(--c-coral);
}

.message p {
  margin-top: 8px;
  white-space: pre-wrap;
  line-height: 1.65;
}

.composer {
  max-width: 1180px;
  width: 100%;
  margin: 0 auto;
  padding: 12px;
  display: flex;
  gap: 12px;
}

textarea {
  flex: 1;
  min-height: 72px;
  padding: 12px;
  border: 1px solid var(--border);
  border-radius: 10px;
  resize: vertical;
  font: inherit;
}

.composer button {
  min-width: 96px;
  background: var(--c-mint);
  color: #fff;
}

.composer button:disabled {
  opacity: 0.7;
  cursor: wait;
}

@media (max-width: 760px) {
  .tutor-page {
    padding: 14px;
  }

  .tutor-header {
    align-items: stretch;
    flex-direction: column;
  }

  .eyebrow,
  h1 {
    text-align: left;
  }

  .tutor-shell {
    grid-template-columns: 1fr;
  }

  .message {
    max-width: 100%;
  }

  .composer {
    flex-direction: column;
  }
}
</style>
