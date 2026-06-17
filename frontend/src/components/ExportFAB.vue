<script setup lang="ts">
import { useExportStore } from '@/stores/export'
import { useRoute } from 'vue-router'
import { computed } from 'vue'

const store = useExportStore()
const route = useRoute()

const pageId = computed(() => {
  const name = route.name as string
  if (name === 'home') return 'home'
  if (name === 'study') return 'study'
  if (name === 'ai-tutor') return 'tutor'
  return 'unknown'
})
</script>

<template>
  <div class="fab-wrapper">
    <button class="fab-btn" @click="store.open(pageId)">
      <i class="fas fa-file-export"></i> 选择导出
    </button>
  </div>
</template>

<style scoped>
.fab-wrapper {
  position: fixed;
  bottom: 28px;
  right: 28px;
  z-index: 9998;
}
.fab-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 13px 22px;
  background: linear-gradient(135deg, #C2410C, #D97706);
  border: none;
  border-radius: 50px;
  color: white;
  font-size: .95rem;
  font-weight: 700;
  cursor: pointer;
  box-shadow: 0 8px 24px rgba(194, 65, 12, 0.35);
  transition: .25s;
  font-family: inherit;
}
.fab-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 32px rgba(194, 65, 12, 0.5);
}
</style>
