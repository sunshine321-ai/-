import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

interface ExportModule {
  id: string
  label: string
  icon: string
  bg: string
  color: string
  page: string
  key: string
  kind: 'json_array' | 'html_string'
  secType: string
}

const MODULES: ExportModule[] = [
  { id: 'screenshot_notes', label: '首页截图笔记', icon: 'fa-camera', bg: '#fef3c7', color: '#B45309', page: '首页', key: 'convolutionKernelNotes', kind: 'json_array', secType: 'screenshot_notes' },
  { id: 'home_chat', label: '首页 AI 学习助手对话', icon: 'fa-robot', bg: '#fee2e2', color: '#9A3412', page: '首页', key: 'vlab_home_chat', kind: 'json_array', secType: 'ai_chat' },
  { id: 'study_notes', label: '课后学习笔记', icon: 'fa-pen-nib', bg: '#fff7ed', color: '#C2410C', page: '课后习题', key: 'convolution_notes', kind: 'html_string', secType: 'study_notes' },
  { id: 'wrong_questions', label: '课后错题本', icon: 'fa-book-open', bg: '#fef2f2', color: '#b91c1c', page: '课后习题', key: 'convolution_wrong_questions', kind: 'json_array', secType: 'wrong_questions' },
  { id: 'study_chat', label: '课后 AI 对话', icon: 'fa-comments', bg: '#fefce8', color: '#ca8a04', page: '课后习题', key: 'vlab_study_chat', kind: 'json_array', secType: 'ai_chat' },
  { id: 'tutor_chat', label: 'AI 助教对话', icon: 'fa-graduation-cap', bg: '#fdf4ff', color: '#9A3412', page: 'AI助教', key: 'vlab_tutor_chat', kind: 'json_array', secType: 'ai_chat' },
]

export const useExportStore = defineStore('export', () => {
  const isOpen = ref(false)
  const currentPage = ref<string>('unknown')
  const format = ref<'word' | 'pdf'>('word')
  const checkedIds = ref<Set<string>>(new Set())
  const isExporting = ref(false)
  const message = ref('')

  const modulesWithState = computed(() =>
    MODULES.map((mod) => {
      const data = getRawData(mod.key)
      const count = Array.isArray(data) ? data.length : (data ? 1 : 0)
      return { ...mod, count, hasData: count > 0 }
    })
  )

  const groupedModules = computed(() => {
    const groups: Record<string, typeof modulesWithState.value> = {}
    modulesWithState.value.forEach((mod) => {
      if (!groups[mod.page]) groups[mod.page] = []
      groups[mod.page].push(mod)
    })
    return groups
  })

  const selectedSections = computed(() => {
    return modulesWithState.value
      .filter((m) => checkedIds.value.has(m.id) && m.hasData)
      .map((m) => {
        const data = getRawData(m.key)
        if (m.secType === 'screenshot_notes') return { type: 'screenshot_notes', items: data }
        if (m.secType === 'ai_chat') return { type: 'ai_chat', messages: data, label: m.label }
        if (m.secType === 'wrong_questions') return { type: 'wrong_questions', items: data }
        if (m.secType === 'study_notes') return { type: 'study_notes', content: data }
        return null
      })
      .filter(Boolean)
  })

  function getRawData(key: string) {
    const raw = localStorage.getItem(key)
    if (!raw) return null
    const mod = MODULES.find((m) => m.key === key)
    if (mod?.kind === 'json_array') {
      try { const d = JSON.parse(raw); return d?.length > 0 ? d : null } catch { return null }
    }
    return raw.trim() || null
  }

  function open(pageId: string) {
    currentPage.value = pageId
    // Pre-check modules that have data
    checkedIds.value = new Set(modulesWithState.value.filter((m) => m.hasData).map((m) => m.id))
    format.value = 'word'
    message.value = ''
    isOpen.value = true
  }

  function close() {
    isOpen.value = false
  }

  function toggle(id: string) {
    const mod = modulesWithState.value.find((m) => m.id === id)
    if (!mod?.hasData) return
    const next = new Set(checkedIds.value)
    if (next.has(id)) next.delete(id)
    else next.add(id)
    checkedIds.value = next
  }

  function selectFormat(fmt: 'word' | 'pdf') {
    format.value = fmt
  }

  async function doExport() {
    if (selectedSections.value.length === 0) {
      message.value = '⚠️ 请至少勾选一个有内容的模块'
      return
    }
    isExporting.value = true
    message.value = ''
    try {
      const resp = await fetch('/api/export-combined', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ format: format.value, sections: selectedSections.value }),
      })
      const data = await resp.json()
      if (data.success) {
        window.open(data.url, '_blank')
        message.value = format.value === 'pdf' ? '✅ 已打开，请按 Ctrl+P → 另存为 PDF' : '✅ 文档已生成，请在新标签页另存'
      } else {
        message.value = '❌ 导出失败：' + (data.error || '未知错误')
      }
    } catch (e: any) {
      message.value = '❌ 网络错误：' + e.message
    } finally {
      isExporting.value = false
    }
  }

  return { isOpen, format, checkedIds, isExporting, message, modulesWithState, groupedModules, selectedSections, open, close, toggle, selectFormat, doExport }
})
