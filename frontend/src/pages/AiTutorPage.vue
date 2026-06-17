<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useChatStore } from '@/stores/chat'
import ChatPanel from '@/components/ChatPanel.vue'

const router = useRouter()
const chat = useChatStore()

onMounted(() => {
  chat.initFromServer()
})

function clearChat() {
  if (confirm('确定要清空所有聊天记录吗？')) {
    chat.tutorChat.length = 0
  }
}

async function copyLastAnswer() {
  const last = chat.tutorChat.filter(m => m.role === 'assistant').pop()
  if (last) {
    try {
      await navigator.clipboard.writeText(last.content)
    } catch {
      alert('复制失败')
    }
  } else {
    alert('没有可复制的AI回复')
  }
}
</script>

<template>
  <div class="tutor-container">
    <div class="header-bar">
      <a class="back-link" @click.prevent="router.push('/')">
        <i class="fas fa-arrow-left"></i> 返回首页
      </a>
      <h1>AI助手</h1>
    </div>

    <div class="chat-wrapper">
      <ChatPanel context="tutor" title="AI 学习助教" />
    </div>

    <div class="chat-actions">
      <button class="action-btn" @click="clearChat">
        <i class="fas fa-trash"></i> 清空对话
      </button>
      <button class="action-btn" @click="copyLastAnswer">
        <i class="fas fa-copy"></i> 复制最后回复
      </button>
    </div>
  </div>
</template>

<style scoped>
.tutor-container {
  height: 100vh;
  max-width: 900px;
  margin: 0 auto;
  padding: 0 20px 20px;
  display: flex;
  flex-direction: column;
}
.header-bar {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 12px 0;
  border-bottom: 1px solid var(--border);
  flex-shrink: 0;
  position: relative;
}
.header-bar h1 {
  font-family: 'Fredoka', 'PingFang SC', sans-serif;
  font-size: 1.3rem;
  font-weight: 600;
  color: var(--c-mint);
}
.back-link {
  position: absolute;
  left: 0;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: var(--text-muted);
  cursor: pointer;
  transition: 0.2s;
}
.back-link:hover { color: var(--c-yellow); }
.chat-wrapper {
  flex: 1;
  min-height: 0;
  margin: 16px 0;
}
.chat-actions {
  display: flex;
  gap: 8px;
  padding-bottom: 10px;
  justify-content: flex-end;
}
.action-btn {
  padding: 6px 12px;
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: 6px;
  color: var(--text-muted);
  font-size: 0.85rem;
  cursor: pointer;
  transition: 0.2s;
  font-family: 'Nunito', sans-serif;
}
.action-btn:hover {
  background: rgba(180, 83, 9, 0.10);
  color: var(--c-mint);
  border-color: rgba(180, 83, 9, 0.30);
}
</style>
