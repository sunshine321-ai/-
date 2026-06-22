<script setup>
import { onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const datasets = new Set(['learning', 'kernel'])
const initialDataset = datasets.has(String(route.query.dataset)) ? String(route.query.dataset) : 'learning'
const frameSrc = `/pages/data-viz.html?dataset=${encodeURIComponent(initialDataset)}`

function handleDatasetChange(event) {
  if (event.origin !== window.location.origin) return
  if (event.data?.type !== 'data-viz-dataset-change' || !datasets.has(event.data.dataset)) return
  router.replace({ query: { ...route.query, dataset: event.data.dataset } })
}

onMounted(() => window.addEventListener('message', handleDatasetChange))
onUnmounted(() => window.removeEventListener('message', handleDatasetChange))
</script>

<template>
  <iframe
    :src="frameSrc"
    style="width:100%;height:100vh;border:none;display:block;"
    title="数据洞察大屏"
  />
</template>
