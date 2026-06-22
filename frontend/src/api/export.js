const WORD_MIME_TYPE = 'application/msword;charset=utf-8'

const escapeHtml = (value) => String(value ?? '')
  .replaceAll('&', '&amp;')
  .replaceAll('<', '&lt;')
  .replaceAll('>', '&gt;')
  .replaceAll('"', '&quot;')
  .replaceAll("'", '&#39;')

const pickValue = (item, first, second) => item?.[first] ?? item?.[second] ?? ''

const timestamp = () => {
  const now = new Date()
  const pad = (value) => String(value).padStart(2, '0')
  return `${now.getFullYear()}${pad(now.getMonth() + 1)}${pad(now.getDate())}_${pad(now.getHours())}${pad(now.getMinutes())}${pad(now.getSeconds())}`
}

const createDocument = (title, body) => `<!doctype html>
<html lang="zh-CN">
<head>
  <meta charset="utf-8">
  <title>${escapeHtml(title)}</title>
  <style>
    body { font-family: "Microsoft YaHei", sans-serif; line-height: 1.7; max-width: 860px; margin: auto; padding: 40px; color: #1e293b; }
    h1 { text-align: center; }
    .card { padding: 16px; margin: 14px 0; border: 1px solid #cbd5e1; border-radius: 10px; }
  </style>
</head>
<body>
  <h1>${escapeHtml(title)}</h1>
  <p>导出时间：${new Date().toLocaleString('zh-CN', { hour12: false })}</p>
  ${body}
</body>
</html>`

const downloadWord = (prefix, documentContent) => {
  const blob = new Blob(['\ufeff', documentContent], { type: WORD_MIME_TYPE })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `${prefix}_${timestamp()}.doc`
  document.body.appendChild(link)
  link.click()
  link.remove()
  setTimeout(() => URL.revokeObjectURL(url), 0)
}

const printPdf = (documentContent) => {
  const printWindow = window.open('', '_blank')
  if (!printWindow) throw new Error('浏览器阻止了打印窗口，请允许弹出窗口后重试')

  printWindow.document.open()
  printWindow.document.write(documentContent)
  printWindow.document.close()
  setTimeout(() => {
    printWindow.focus()
    printWindow.print()
  }, 500)
}

const output = (prefix, format, documentContent) => {
  if (format === 'word') {
    downloadWord(prefix, documentContent)
  } else if (format === 'pdf') {
    printPdf(documentContent)
  } else {
    throw new Error('导出格式只能是 word 或 pdf')
  }
  return { success: true, format }
}

const runExport = (action) => {
  try {
    return Promise.resolve(action())
  } catch (error) {
    return Promise.resolve({ success: false, msg: error.message })
  }
}

export const exportNotes = (content, format = 'word') => runExport(() => {
  if (!content?.trim()) throw new Error('笔记内容为空')
  return output('StudyNotes', format, createDocument('卷积核学习笔记', content))
})

export const exportWrongQuestions = (wrongQuestions, format = 'word') => runExport(() => {
  if (!wrongQuestions?.length) throw new Error('错题内容为空')
  const body = wrongQuestions.map((item, index) => `
    <section class="card">
      <h2>${index + 1}. ${escapeHtml(pickValue(item, 'question', 'title'))}</h2>
      <p><b>你的答案：</b>${escapeHtml(pickValue(item, 'userAnswer', 'userChoice'))}</p>
      <p><b>正确答案：</b>${escapeHtml(pickValue(item, 'correctAnswer', 'answer'))}</p>
      <p><b>解析：</b>${escapeHtml(pickValue(item, 'note', 'aiExplanation'))}</p>
    </section>`).join('')
  return output('WrongQuestions', format, createDocument('卷积核错题本', body))
})

const renderItems = (items) => Array.isArray(items)
  ? items.map((item) => `<section class="card">${Object.entries(item ?? {})
    .map(([key, value]) => `<p><b>${escapeHtml(key)}：</b>${escapeHtml(value)}</p>`)
    .join('')}</section>`).join('')
  : ''

export const exportCombined = (sections, format = 'word') => runExport(() => {
  if (!sections?.length) throw new Error('没有可导出的内容')

  const body = sections.map((section) => {
    if (section.type === 'study_notes') {
      return `<h2>学习笔记</h2><section class="card">${section.content ?? ''}</section>`
    }
    if (section.type === 'ai_chat') {
      return `<h2>${escapeHtml(section.label || 'AI 对话')}</h2>${renderItems(section.messages)}`
    }
    if (section.type === 'wrong_questions' || section.type === 'screenshot_notes') {
      const title = section.type === 'wrong_questions' ? '错题本' : '截图笔记'
      return `<h2>${title}</h2>${renderItems(section.items)}`
    }
    return ''
  }).join('')

  if (!body) throw new Error('没有可导出的内容')
  return output('CombinedExport', format, createDocument('卷积核微课学习资料', body))
})
