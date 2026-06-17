import { defineStore } from 'pinia'
import { ref, watch } from 'vue'

export interface ScreenshotNote {
  id: number
  imageData: string
  timestamp: number
  note: string
  analysis?: string
}

export interface WrongQuestion {
  id: number
  question: string
  answer: string
  userAnswer: string
  isCorrect: boolean
  note?: string
}

async function syncToServer(key: string, value: any) {
  try {
    await fetch('/api/data/sync', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ key, value }),
    })
  } catch { /* 静默失败 */ }
}

async function loadFromServer(): Promise<Record<string, any> | null> {
  try {
    const resp = await fetch('/api/data/load')
    const data = await resp.json()
    if (data.success) return data.data
  } catch { /* 网络不通 */ }
  return null
}

export const useNotesStore = defineStore('notes', () => {
  const screenshots = ref<ScreenshotNote[]>(
    loadJSON('convolutionKernelNotes') || []
  )
  const wrongQuestions = ref<WrongQuestion[]>(
    loadJSON('convolution_wrong_questions') || []
  )
  const studyNotes = ref<string>(
    localStorage.getItem('convolution_notes') || ''
  )
  const serverLoaded = ref(false)

  function loadJSON(key: string) {
    try { return JSON.parse(localStorage.getItem(key) || 'null') } catch { return null }
  }

  watch(screenshots, (v) => {
    localStorage.setItem('convolutionKernelNotes', JSON.stringify(v))
    syncToServer('convolutionKernelNotes', v)
  }, { deep: true })
  watch(wrongQuestions, (v) => {
    localStorage.setItem('convolution_wrong_questions', JSON.stringify(v))
    syncToServer('convolution_wrong_questions', v)
  }, { deep: true })
  watch(studyNotes, (v) => {
    localStorage.setItem('convolution_notes', v)
    syncToServer('convolution_notes', v)
  })

  async function initFromServer() {
    if (serverLoaded.value) return
    serverLoaded.value = true
    const data = await loadFromServer()
    if (!data) return
    if (data.convolutionKernelNotes && Array.isArray(data.convolutionKernelNotes) && data.convolutionKernelNotes.length > screenshots.value.length)
      screenshots.value = data.convolutionKernelNotes
    if (data.convolution_wrong_questions && Array.isArray(data.convolution_wrong_questions) && data.convolution_wrong_questions.length > wrongQuestions.value.length)
      wrongQuestions.value = data.convolution_wrong_questions
    if (data.convolution_notes && typeof data.convolution_notes === 'string' && data.convolution_notes.length > studyNotes.value.length)
      studyNotes.value = data.convolution_notes
  }

  function addScreenshot(note: ScreenshotNote) {
    screenshots.value.push(note)
  }

  function deleteScreenshot(id: number) {
    screenshots.value = screenshots.value.filter((s) => s.id !== id)
  }

  function addWrongQuestion(q: WrongQuestion) {
    wrongQuestions.value.push(q)
  }

  function setStudyNotes(html: string) {
    studyNotes.value = html
  }

  return { screenshots, wrongQuestions, studyNotes, serverLoaded, initFromServer, addScreenshot, deleteScreenshot, addWrongQuestion, setStudyNotes }
})
