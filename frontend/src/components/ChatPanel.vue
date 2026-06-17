<script setup lang="ts">
import { ref, nextTick, watch } from 'vue'
import { useChatStore } from '@/stores/chat'
import type { ChatMessage } from '@/stores/chat'

const props = defineProps<{
  context: 'home' | 'tutor' | 'study'
  title?: string
}>()

const store = useChatStore()
const input = ref('')
const chatContainer = ref<HTMLElement>()

const messages = (() => {
  switch (props.context) {
    case 'home': return store.homeChat
    case 'tutor': return store.tutorChat
    case 'study': return store.studyChat
  }
})()

async function send() {
  if (!input.value.trim() || store.isLoading) return
  const msg = input.value
  input.value = ''
  await store.sendMessage(msg, props.context)
  await nextTick()
  scrollBottom()
}

function scrollBottom() {
  if (chatContainer.value) {
    chatContainer.value.scrollTop = chatContainer.value.scrollHeight
  }
}

watch(() => messages.value.length, () => nextTick(() => scrollBottom()))
</script>

<template>
  <div class="chat-panel">
    <div class="chat-header" v-if="title">
      <i class="fas fa-robot" style="color:var(--c-yellow);margin-right:8px;"></i>
      {{ title }}
    </div>
    <div class="chat-messages" ref="chatContainer">
      <div v-if="messages.length === 0" class="chat-empty">
        <i class="fas fa-comments" style="font-size:2rem;color:var(--text-muted);opacity:0.4;margin-bottom:8px;"></i>
        <p>开始和 AI 助教对话吧</p>
      </div>
      <div
        v-for="(m, i) in messages"
        :key="i"
        class="chat-msg"
        :class="m.role"
      >
        <div class="msg-bubble">{{ m.content }}</div>
      </div>
      <div v-if="store.isLoading" class="chat-msg assistant">
        <div class="msg-bubble loading">思考中<span class="dots">...</span></div>
      </div>
    </div>
    <div class="chat-input-area">
      <input
        v-model="input"
        type="text"
        placeholder="输入你的问题..."
        @keydown.enter="send()"
        :disabled="store.isLoading"
      />
      <button @click="send()" :disabled="store.isLoading || !input.trim()">
        <i class="fas fa-paper-plane"></i>
      </button>
    </div>
  </div>
</template>

<style scoped>
.chat-panel {
  display: flex;
  flex-direction: column;
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: 12px;
  overflow: hidden;
  height: 100%;
  min-height: 300px;
}
.chat-header {
  padding: 14px 18px;
  font-weight: 700;
  color: var(--c-mint);
  border-bottom: 1px solid var(--border);
  display: flex;
  align-items: center;
}
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.chat-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: var(--text-muted);
  font-size: .9rem;
}
.chat-msg { display: flex; }
.chat-msg.user { justify-content: flex-end; }
.chat-msg.assistant { justify-content: flex-start; }
.msg-bubble {
  max-width: 80%;
  padding: 10px 16px;
  border-radius: 14px;
  font-size: .9rem;
  line-height: 1.55;
  white-space: pre-wrap;
  word-break: break-word;
}
.chat-msg.user .msg-bubble {
  background: linear-gradient(135deg, var(--c-coral), var(--c-yellow));
  color: white;
  border-bottom-right-radius: 4px;
}
.chat-msg.assistant .msg-bubble {
  background: white;
  border: 1px solid var(--border);
  color: var(--text-main);
  border-bottom-left-radius: 4px;
}
.msg-bubble.loading {
  color: var(--text-muted);
  font-style: italic;
}
.chat-input-area {
  display: flex;
  padding: 12px;
  border-top: 1px solid var(--border);
  gap: 8px;
}
.chat-input-area input {
  flex: 1;
  padding: 10px 14px;
  border: 1px solid var(--border);
  border-radius: 24px;
  font-size: .9rem;
  background: white;
  font-family: inherit;
  outline: none;
}
.chat-input-area input:focus {
  border-color: var(--c-yellow);
}
.chat-input-area button {
  width: 40px;
  height: 40px;
  border: none;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--c-coral), var(--c-yellow));
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.chat-input-area button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
