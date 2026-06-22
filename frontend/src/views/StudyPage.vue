<script setup>
import { onMounted, onUnmounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getStudyNote, saveStudyNote } from '@/api/studyNote'

// 课后学习页 - 6100行复杂页面，暂时通过iframe加载
// TODO: 未来拆解为Vue组件（笔记、错题本、AI对话、互动练习）
const studyFrame = ref()
const route = useRoute()
const router = useRouter()
const studySections = new Set(['intro', 'visualizer', 'exercises', 'challenge', 'discussion', 'summary', 'lab'])
const initialSection = studySections.has(String(route.query.section)) ? String(route.query.section) : 'intro'
const studyFrameSrc = `/legacy/study.html?section=${encodeURIComponent(initialSection)}`
let saveTimer

function handleLegacyMessage(event) {
  if (event.origin !== window.location.origin) return
  if (event.data?.type === 'study-section-change' && studySections.has(event.data.sectionId)) {
    router.replace({ query: { ...route.query, section: event.data.sectionId } })
    return
  }
  if (event.data?.type !== 'study-note-change') return

  clearTimeout(saveTimer)
  saveTimer = setTimeout(() => {
    saveStudyNote(event.data.content)
  }, 500)
}

async function loadStudyNote() {
  try {
    const result = await getStudyNote()
    if (typeof result.data?.content === 'string') {
      studyFrame.value?.contentWindow?.postMessage({
        type: 'study-note-load',
        content: result.data.content,
      }, window.location.origin)
    }
  } catch {
    // Java 后端不可用时不加载数据。
  }
}

onMounted(() => window.addEventListener('message', handleLegacyMessage))
onUnmounted(() => {
  window.removeEventListener('message', handleLegacyMessage)
  clearTimeout(saveTimer)
})
</script>

<template>
  <iframe
    ref="studyFrame"
    :src="studyFrameSrc"
    @load="loadStudyNote"
    style="width:100%;height:100vh;border:none;display:block;"
    title="课后学习"
  />
</template>
