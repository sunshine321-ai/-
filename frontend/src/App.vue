<script setup>
import { onUnmounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { recordUsageEvent } from '@/api/usage'

const route = useRoute()
const routeModules = {
  study: 'study',
  'ai-tutor': 'ai_tutor',
  playground: 'playground',
  'showcase-3d': 'showcase_3d',
  'data-viz': 'data_viz',
  calculator: 'calculator',
}
let activeModule = null
let activeSince = Date.now()

function flushRouteDuration() {
  if (!activeModule || document.visibilityState === 'hidden') return
  const seconds = Math.floor((Date.now() - activeSince) / 1000)
  if (seconds > 0) recordUsageEvent(activeModule, 'duration', seconds)
  activeSince = Date.now()
}

function handleVisibilityChange() {
  if (document.visibilityState === 'hidden') {
    const seconds = Math.floor((Date.now() - activeSince) / 1000)
    if (activeModule && seconds > 0) recordUsageEvent(activeModule, 'duration', seconds)
  } else {
    activeSince = Date.now()
  }
}

watch(() => route.name, (name) => {
  flushRouteDuration()
  activeModule = routeModules[name] || null
  activeSince = Date.now()
  if (activeModule) recordUsageEvent(activeModule, 'page_view')
}, { immediate: true })

document.addEventListener('visibilitychange', handleVisibilityChange)
onUnmounted(() => {
  flushRouteDuration()
  document.removeEventListener('visibilitychange', handleVisibilityChange)
})
</script>

<template>
  <router-view />
</template>
