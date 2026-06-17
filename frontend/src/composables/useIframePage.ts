import { ref, onMounted } from 'vue'

export function useIframePage(url: string) {
  const loading = ref(true)

  function onLoad() {
    loading.value = false
  }

  return { url, loading, onLoad }
}
