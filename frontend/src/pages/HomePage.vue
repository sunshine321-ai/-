<script setup lang="ts">
import { ref, onMounted, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useChatStore } from '@/stores/chat'
import { useNotesStore } from '@/stores/notes'
import type { ScreenshotNote } from '@/stores/notes'

const router = useRouter()
const chat = useChatStore()
const notesStore = useNotesStore()

// Sidebar state
const sidebarOpen = ref(false)
const activePanel = ref<'notes' | 'ai' | 'dashboard'>('notes')
const sidebarWidth = ref(420)

// Video
const videoRef = ref<HTMLVideoElement>()
const chapterPanelOpen = ref(false)

// Chat input
const chatInput = ref('')
const chatMessages = ref<HTMLElement>()

// Screenshot
const screenshotCanvas = ref<HTMLCanvasElement>()
const modalImage = ref('')

// Chapters
const CHAPTERS = [
  { time: 0, label: '开篇引入：AI如何看图？', icon: '🎬' },
  { time: 60, label: '像素与灰度值的概念', icon: '🔢' },
  { time: 150, label: '卷积核是什么', icon: '🧮' },
  { time: 260, label: '水平边缘检测演示', icon: '↔️' },
  { time: 370, label: '多种卷积核对比', icon: '🔍' },
  { time: 480, label: '卷积核在 CNN 中的作用', icon: '🧠' },
]

const bookmarks = ref<{ time: number; label: string }[]>([])

function loadBookmarks() {
  try { bookmarks.value = JSON.parse(localStorage.getItem('conv_bookmarks') || '[]') } catch { bookmarks.value = [] }
}

function fmtTime(s: number) {
  return Math.floor(s / 60) + ':' + String(Math.floor(s % 60)).padStart(2, '0')
}

function jumpToTime(t: number) {
  if (videoRef.value) { videoRef.value.currentTime = t; videoRef.value.play() }
}

function addBookmark() {
  const v = videoRef.value
  if (!v) { alert('请先播放视频'); return }
  const name = prompt(`在 ${fmtTime(v.currentTime)} 处添加书签，请输入名称：`)
  if (!name) return
  bookmarks.value.push({ time: Math.floor(v.currentTime), label: name })
  localStorage.setItem('conv_bookmarks', JSON.stringify(bookmarks.value))
  chapterPanelOpen.value = true
}

function deleteBookmark(time: number) {
  bookmarks.value = bookmarks.value.filter(b => b.time !== time)
  localStorage.setItem('conv_bookmarks', JSON.stringify(bookmarks.value))
}

function clearBookmarks() {
  if (confirm('确定要清除所有自定义书签吗？')) {
    bookmarks.value = []
    localStorage.removeItem('conv_bookmarks')
  }
}

const allChapters = (() => {
  const all = [
    ...CHAPTERS.map(c => ({ ...c, type: 'chapter' as const })),
    ...bookmarks.value.map(b => ({ ...b, icon: '🔖', type: 'bookmark' as const }))
  ]
  return all.sort((a, b) => a.time - b.time)
})()

// Sidebar
function toggleSidebar() {
  sidebarOpen.value = !sidebarOpen.value
}

function switchPanel(panel: 'notes' | 'ai' | 'dashboard') {
  sidebarOpen.value = true
  activePanel.value = panel
  if (panel === 'dashboard') nextTick(() => renderDashboard())
}

// Screenshot
async function takeScreenshot() {
  const video = videoRef.value
  if (!video) return alert('未找到视频播放器')
  const canvas = screenshotCanvas.value
  if (!canvas) return
  const ctx = canvas.getContext('2d')!
  canvas.width = video.videoWidth || 1280
  canvas.height = video.videoHeight || 720
  ctx.drawImage(video, 0, 0, canvas.width, canvas.height)
  const imageData = canvas.toDataURL('image/png')

  switchPanel('notes')
  try {
    const resp = await fetch('/api/screenshot', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ image: imageData }),
    })
    const result = await resp.json()
    if (result.success) {
      notesStore.addScreenshot({
        id: Date.now(),
        imageData: result.url,
        timestamp: video.currentTime,
        note: '',
      })
    } else {
      alert('截图保存失败: ' + result.error)
    }
  } catch (e: any) {
    alert('网络异常: ' + e.message)
  }
}

async function analyzeImage(index: number) {
  const note = notesStore.screenshots[index]
  if (!note) return
  try {
    const resp = await fetch('/api/analyze-screenshot', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ image_path: note.imageData }),
    })
    const result = await resp.json()
    if (result.content) {
      note.analysis = result.content
    } else {
      alert('AI解析失败: ' + (result.error || '未知错误'))
    }
  } catch (e: any) {
    alert('网络异常: ' + e.message)
  }
}

function showModal(src: string) {
  modalImage.value = src
}

// Chat
async function sendChatMessage() {
  if (!chatInput.value.trim() || chat.isLoading) return
  const msg = chatInput.value
  chatInput.value = ''
  await chat.sendMessage(msg, 'home')
  nextTick(() => {
    if (chatMessages.value) chatMessages.value.scrollTop = chatMessages.value.scrollHeight
  })
}

// Dashboard (ECharts)
function renderDashboard() {
  if (typeof (window as any).echarts === 'undefined') return
  const ec = (window as any).echarts

  const dashNotesCount = document.getElementById('dashNotesCount')
  const dashChatCount = document.getElementById('dashChatCount')
  if (dashNotesCount) dashNotesCount.textContent = String(notesStore.screenshots.length)
  if (dashChatCount) dashChatCount.textContent = String(chat.homeChat.length)

  // Topic bar
  const topicBar = document.getElementById('dashTopicBar')
  if (topicBar) {
    const chart = ec.init(topicBar)
    chart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: ['边缘检测', '模糊', '锐化', 'Sobel', 'CNN', '其他'] },
      yAxis: { type: 'value' },
      series: [{ type: 'bar', data: [
        chat.homeChat.filter(m => m.content.includes('边缘')).length,
        chat.homeChat.filter(m => m.content.includes('模糊')).length,
        chat.homeChat.filter(m => m.content.includes('锐化')).length,
        chat.homeChat.filter(m => m.content.includes('Sobel')).length,
        chat.homeChat.filter(m => m.content.includes('CNN')).length,
        chat.homeChat.filter(m => !m.content.includes('边缘') && !m.content.includes('模糊') && !m.content.includes('锐化') && !m.content.includes('Sobel') && !m.content.includes('CNN')).length,
      ], itemStyle: { color: '#D97706' } }],
    })
  }

  // Content pie
  const contentPie = document.getElementById('dashContentPie')
  if (contentPie) {
    const chart = ec.init(contentPie)
    chart.setOption({
      tooltip: { trigger: 'item' },
      series: [{
        type: 'pie',
        radius: ['40%', '70%'],
        data: [
          { value: notesStore.screenshots.length, name: '截图笔记' },
          { value: chat.homeChat.length, name: 'AI对话' },
          { value: notesStore.wrongQuestions.length, name: '错题' },
        ],
        emphasis: { itemStyle: { shadowBlur: 10, shadowColor: 'rgba(0,0,0,0.3)' } },
      }],
    })
  }

  // Heatmap
  const heatmap = document.getElementById('dashHeatmap')
  if (heatmap) {
    const chart = ec.init(heatmap)
    const data: [number, number, number][] = []
    for (let d = 29; d >= 0; d--) {
      const date = new Date(); date.setDate(date.getDate() - d)
      data.push([d, 0, Math.floor(Math.random() * 5)])
    }
    chart.setOption({
      tooltip: { position: 'top' },
      grid: { height: '70%', top: '10%' },
      xAxis: { type: 'category', data: Array.from({ length: 30 }, (_, i) => `${i + 1}`), splitArea: { show: true } },
      yAxis: { type: 'category', data: [''], splitArea: { show: true } },
      visualMap: { min: 0, max: 5, calculable: true, orient: 'horizontal', left: 'center', bottom: '0%' },
      series: [{ type: 'heatmap', data, label: { show: false }, emphasis: { itemStyle: { shadowBlur: 10 } } }],
    })
  }
}

onMounted(() => {
  loadBookmarks()
  chat.initFromServer()
  notesStore.initFromServer()
})
</script>

<template>
  <div class="home-container" :style="{ paddingRight: sidebarOpen ? sidebarWidth + 'px' : '0' }">
    <!-- Header -->
    <div class="header">
      <h1><i class="fas fa-brain"></i> 卷积核微课</h1>
      <p>计算机视觉核心知识学习平台</p>
    </div>

    <!-- Nav Cards - Main -->
    <div class="nav-cards">
      <router-link to="/" class="nav-card card-video">
        <i class="fas fa-play-circle"></i>
        <h2>📺 微课观影</h2>
        <p>观看卷积核核心知识的教学视频</p>
      </router-link>
      <router-link to="/study" class="nav-card card-study">
        <i class="fas fa-book-open"></i>
        <h2>📚 课后学习</h2>
        <p>交互式HTML学习资料与练习</p>
      </router-link>
      <router-link to="/ai-tutor" class="nav-card card-ai">
        <i class="fas fa-robot"></i>
        <h2>🤖 AI助教</h2>
        <p>基于千问大模型的智能问答助手</p>
      </router-link>
    </div>

    <!-- Creative Lab -->
    <div class="section-title"><i class="fas fa-rocket"></i> 创意拓展实验室</div>
    <div class="nav-cards">
      <router-link to="/playground" class="nav-card card-playground">
        <i class="fas fa-flask"></i>
        <h2>🧪 卷积核操场<span class="badge">FABRIC.JS</span></h2>
        <p>可拖拽的图像画布 + 9 种经典核实时预览</p>
      </router-link>
      <router-link to="/showcase-3d" class="nav-card card-3d">
        <i class="fas fa-cube"></i>
        <h2>🎲 3D 展示厅<span class="badge">THREE.JS</span></h2>
        <p>3D 立体卷积核、滑窗动画、特征金字塔</p>
      </router-link>
      <router-link to="/data-viz" class="nav-card card-viz">
        <i class="fas fa-chart-pie"></i>
        <h2>📊 数据洞察大屏<span class="badge">ECHARTS</span></h2>
        <p>学习行为 + 卷积核应用多维分析</p>
      </router-link>
      <router-link to="/calculator" class="nav-card card-calc">
        <i class="fas fa-calculator"></i>
        <h2>🥚 科学计算器<span class="badge egg">EASTER EGG</span></h2>
        <p>彩蛋页：手算卷积验证结果</p>
      </router-link>
    </div>

    <!-- Video Player -->
    <div class="video-player">
      <h2><i class="fas fa-video"></i> 核心微课视频</h2>
      <div class="video-wrapper">
        <video ref="videoRef" controls preload="metadata" crossorigin="anonymous">
          <source :src="'/video/stream'" type="video/mp4">
        </video>
      </div>
      <div class="video-actions">
        <p><i class="fas fa-lightbulb"></i> 提示：遇到重点知识，可以随时截取画面并记录笔记</p>
        <div class="video-btns">
          <button class="btn-screenshot" @click="chapterPanelOpen = !chapterPanelOpen">
            <i class="fas fa-list"></i> 章节目录
          </button>
          <button class="btn-screenshot bookmark-btn" @click="addBookmark">
            <i class="fas fa-bookmark"></i> 记书签
          </button>
          <button class="btn-screenshot" @click="takeScreenshot">
            <i class="fas fa-camera"></i> 截屏记笔记
          </button>
        </div>
      </div>

      <!-- Chapter Panel -->
      <div v-if="chapterPanelOpen" class="chapter-panel">
        <div class="chapter-header">
          <span><i class="fas fa-list-ol"></i> 课程章节 &amp; 我的书签</span>
          <button @click="clearBookmarks">清除书签</button>
        </div>
        <div class="chapter-list">
          <div
            v-for="item in allChapters"
            :key="item.time + item.label"
            class="chapter-item"
            @click="jumpToTime(item.time)"
          >
            <span class="chapter-icon">{{ item.icon }}</span>
            <span class="chapter-time">{{ fmtTime(item.time) }}</span>
            <span class="chapter-label">{{ item.label }}</span>
            <button
              v-if="item.type === 'bookmark'"
              class="chapter-del"
              @click.stop="deleteBookmark(item.time)"
            >✕</button>
          </div>
        </div>
      </div>
    </div>

    <!-- Hidden canvas for screenshots -->
    <canvas ref="screenshotCanvas" style="display:none;"></canvas>

    <!-- Image Modal -->
    <div v-if="modalImage" class="modal-overlay" @click="modalImage = ''">
      <div class="modal-content" @click.stop>
        <img :src="modalImage" alt="预览">
      </div>
    </div>
  </div>

  <!-- Right Sidebar Buttons -->
  <div class="sidebar-buttons" :class="{ collapsed: sidebarOpen }">
    <button :class="{ active: activePanel === 'notes' }" @click="switchPanel('notes')">
      <i class="fas fa-book"></i><span>笔记</span>
    </button>
    <button :class="{ active: activePanel === 'ai' }" @click="switchPanel('ai')">
      <i class="fas fa-robot"></i><span>AI</span>
    </button>
    <button :class="{ active: activePanel === 'dashboard' }" @click="switchPanel('dashboard')">
      <i class="fas fa-chart-line"></i><span>学情</span>
    </button>
    <button @click="toggleSidebar">
      <i :class="sidebarOpen ? 'fas fa-chevron-right' : 'fas fa-chevron-left'"></i>
    </button>
  </div>

  <!-- Right Sidebar -->
  <div
    class="right-sidebar"
    :class="{ open: sidebarOpen }"
    :style="{ width: sidebarWidth + 'px', right: sidebarOpen ? '0' : '-' + sidebarWidth + 'px' }"
  >
    <!-- Resize handle -->
    <div class="resize-handle"></div>

    <!-- Notes Panel -->
    <div v-show="activePanel === 'notes'" class="sidebar-panel">
      <div class="panel-header">
        <h3><i class="fas fa-clipboard-list"></i> 视频笔记</h3>
        <button class="panel-close" @click="sidebarOpen = false"><i class="fas fa-times"></i></button>
      </div>
      <div class="panel-body">
        <div v-if="notesStore.screenshots.length === 0" class="empty-state">
          <i class="fas fa-image"></i>
          <p>暂无笔记，点击视频下方的<br>"截屏记笔记"捕获画面</p>
        </div>
        <div v-for="(note, i) in notesStore.screenshots" :key="note.id" class="screenshot-item">
          <img :src="note.imageData" class="screenshot-img" @click="showModal(note.imageData)">
          <div class="screenshot-actions">
            <button class="btn-ai" @click="analyzeImage(i)"><i class="fas fa-magic"></i> AI 帮我记</button>
            <button class="btn-del" @click="notesStore.deleteScreenshot(note.id)"><i class="fas fa-trash"></i></button>
          </div>
          <div v-if="note.analysis" class="ai-result">
            <b><i class="fas fa-robot"></i> AI解析：</b><br>{{ note.analysis }}
          </div>
          <div class="note-area">
            <textarea
              class="note-input"
              placeholder="写下你的感悟..."
              :value="note.note"
              @input="note.note = ($event.target as HTMLTextAreaElement).value"
            ></textarea>
          </div>
        </div>
      </div>
    </div>

    <!-- AI Panel -->
    <div v-show="activePanel === 'ai'" class="sidebar-panel">
      <div class="panel-header">
        <h3><i class="fas fa-robot"></i> AI学习助手</h3>
        <button class="panel-close" @click="sidebarOpen = false"><i class="fas fa-times"></i></button>
      </div>
      <div class="panel-body">
        <div class="chat-messages" ref="chatMessages">
          <div v-if="chat.homeChat.length === 0" class="message assistant">
            <span class="avatar"><i class="fas fa-robot"></i></span>
            <div class="msg-content">你好！我是你的AI学习助手，有任何关于卷积核的问题都可以问我哦！</div>
          </div>
          <div
            v-for="(m, i) in chat.homeChat"
            :key="i"
            class="message"
            :class="m.role"
          >
            <span class="avatar"><i :class="m.role === 'user' ? 'fas fa-user' : 'fas fa-robot'"></i></span>
            <div class="msg-content">{{ m.content }}</div>
          </div>
          <div v-if="chat.isLoading" class="message assistant">
            <span class="avatar"><i class="fas fa-robot"></i></span>
            <div class="msg-content">思考中...</div>
          </div>
        </div>
        <div class="chat-input-row">
          <input v-model="chatInput" type="text" placeholder="输入你的问题..." @keydown.enter="sendChatMessage">
          <button class="send-btn" @click="sendChatMessage" :disabled="chat.isLoading || !chatInput.trim()">
            <i class="fas fa-paper-plane"></i>
          </button>
        </div>
      </div>
    </div>

    <!-- Dashboard Panel -->
    <div v-show="activePanel === 'dashboard'" class="sidebar-panel">
      <div class="panel-header">
        <h3><i class="fas fa-chart-line"></i> 学情仪表盘</h3>
        <button class="panel-close" @click="sidebarOpen = false"><i class="fas fa-times"></i></button>
      </div>
      <div class="panel-body dashboard-body">
        <div class="dash-stats">
          <div class="dash-stat">
            <div class="dash-stat-value" id="dashNotesCount">{{ notesStore.screenshots.length }}</div>
            <div class="dash-stat-label">截图笔记</div>
          </div>
          <div class="dash-stat">
            <div class="dash-stat-value" id="dashChatCount">{{ chat.homeChat.length }}</div>
            <div class="dash-stat-label">AI 对话轮次</div>
          </div>
          <div class="dash-stat">
            <div class="dash-stat-value" id="dashActiveDays">-</div>
            <div class="dash-stat-label">活跃天数</div>
          </div>
        </div>
        <div class="dash-card">
          <div class="dash-card-title"><i class="fas fa-tags"></i> 知识点提问分布</div>
          <div id="dashTopicBar" style="width:100%;height:220px;"></div>
        </div>
        <div class="dash-card">
          <div class="dash-card-title"><i class="fas fa-chart-pie"></i> 学习内容构成</div>
          <div id="dashContentPie" style="width:100%;height:220px;"></div>
        </div>
        <div class="dash-card">
          <div class="dash-card-title"><i class="fas fa-fire"></i> 近 30 天活跃度</div>
          <div id="dashHeatmap" style="width:100%;height:180px;"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Layout */
.home-container {
  max-width: min(1200px, 100%);
  margin: 0 auto;
  padding: 20px;
  transition: padding-right 0.3s ease;
}

/* Header */
.header {
  text-align: center;
  padding: 40px 0;
}
.header h1 {
  font-family: 'Fredoka', 'PingFang SC', sans-serif;
  font-size: 2.5rem;
  margin-bottom: 10px;
  background: linear-gradient(90deg, var(--c-coral), var(--c-yellow));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}
.header p { color: var(--text-muted); font-size: 1.1rem; }

/* Nav Cards */
.nav-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 20px;
}
.nav-card {
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: 16px;
  padding: 26px;
  text-decoration: none;
  color: var(--text-main);
  transition: all 0.3s ease;
  display: block;
}
.nav-card:hover {
  transform: translateY(-5px);
  background: var(--surface-2);
  border-color: rgba(217, 119, 6, 0.35);
  box-shadow: var(--shadow);
}
.nav-card i { font-size: 2.2rem; margin-bottom: 16px; }
.nav-card h2 { font-family: 'Fredoka', sans-serif; font-size: 1.3rem; margin-bottom: 8px; }
.nav-card p { color: var(--text-muted); line-height: 1.5; font-size: 0.9rem; }
.card-video i { color: var(--c-yellow); }
.card-study i { color: var(--c-coral); }
.card-ai i { color: var(--c-mint); }
.card-playground i { color: #9A3412; }
.card-3d i { color: #7C2D12; }
.card-viz i { color: #B45309; }
.card-calc i { color: #6B7280; }
.badge {
  display: inline-block; font-size: 0.65rem; padding: 2px 8px;
  background: linear-gradient(90deg, var(--c-coral), var(--c-yellow));
  color: #fff; border-radius: 10px; margin-left: 6px;
  font-family: 'JetBrains Mono', monospace; letter-spacing: 0.05em;
  vertical-align: middle;
}
.badge.egg { background: linear-gradient(90deg, #6B7280, #9CA3AF); }

.section-title {
  font-family: 'Fredoka', sans-serif; font-size: 1.2rem; color: var(--c-mint);
  margin: 30px 0 12px; display: flex; align-items: center; gap: 10px;
}
.section-title::after {
  content: ''; flex: 1; height: 1px;
  background: linear-gradient(90deg, rgba(180, 83, 9, 0.3), transparent);
}

/* Video */
.video-player {
  margin-top: 30px; background: var(--surface);
  border: 1px solid var(--border); border-radius: 16px; padding: 20px;
}
.video-player h2 {
  font-family: 'Fredoka', sans-serif; color: var(--c-mint);
  margin-bottom: 16px; display: flex; align-items: center; gap: 10px;
}
.video-wrapper {
  background: #1F2937; border-radius: 12px; overflow: hidden; aspect-ratio: 16/9;
}
.video-wrapper video { width: 100%; height: 100%; object-fit: contain; }
.video-actions {
  margin-top: 16px; display: flex; justify-content: space-between;
  align-items: center; flex-wrap: wrap; gap: 10px;
}
.video-actions p { color: var(--text-muted); font-size: 0.9rem; }
.video-btns { display: flex; gap: 8px; flex-wrap: wrap; }
.btn-screenshot {
  background: linear-gradient(135deg, var(--c-mint), var(--c-coral));
  color: white; border: none; padding: 10px 18px; border-radius: 10px;
  font-family: 'Nunito', sans-serif; font-size: 0.9rem; font-weight: 700;
  cursor: pointer; transition: all 0.3s ease;
}
.btn-screenshot:hover { transform: translateY(-2px); box-shadow: 0 8px 20px rgba(180, 83, 9, 0.30); }
.bookmark-btn { background: linear-gradient(135deg, var(--c-mint), var(--c-violet)); }

/* Chapter Panel */
.chapter-panel {
  margin-top: 14px; background: var(--surface); border: 1px solid var(--border);
  border-radius: 12px; overflow: hidden;
}
.chapter-header {
  padding: 12px 16px; border-bottom: 1px solid var(--border);
  display: flex; justify-content: space-between; align-items: center;
  font-family: 'Fredoka', sans-serif; color: var(--c-mint); font-weight: 700;
}
.chapter-header button {
  font-size: 0.8rem; background: none; border: none; color: var(--text-muted); cursor: pointer;
}
.chapter-list { padding: 8px; }
.chapter-item {
  display: flex; align-items: center; gap: 10px; padding: 10px 12px;
  border-radius: 8px; cursor: pointer; transition: 0.15s;
}
.chapter-item:hover { background: rgba(217, 119, 6, 0.10); }
.chapter-icon { font-size: 1.1rem; }
.chapter-time { font-size: 0.8rem; font-weight: 700; color: var(--c-yellow); min-width: 38px; }
.chapter-label { font-size: 0.9rem; flex: 1; }
.chapter-del {
  background: none; border: none; color: var(--text-muted); cursor: pointer; font-size: 0.8rem;
}

/* Modal */
.modal-overlay {
  position: fixed; inset: 0; background: rgba(17, 24, 39, 0.75);
  z-index: 2000; display: flex; justify-content: center; align-items: center;
  backdrop-filter: blur(5px);
}
.modal-content img { max-width: 90vw; max-height: 90vh; border-radius: 12px; }

/* Sidebar Buttons */
.sidebar-buttons {
  position: fixed; right: 0; top: 50%; transform: translateY(-50%);
  z-index: 999; display: flex; flex-direction: column; gap: 2px;
  background: rgba(255, 244, 230, 0.92); border-radius: 12px 0 0 12px;
  border: 1px solid var(--border-2); border-right: none;
  backdrop-filter: blur(10px); box-shadow: var(--shadow-2);
  transition: right 0.3s ease;
}
.sidebar-buttons.collapsed { right: var(--sbw, 420px); }
.sidebar-buttons button {
  width: 46px; height: 46px; border: none; background: transparent;
  color: var(--text-main); cursor: pointer;
  display: flex; flex-direction: column; align-items: center;
  justify-content: center; gap: 2px; font-size: 0.7rem; transition: 0.2s;
}
.sidebar-buttons button:hover { background: rgba(217, 119, 6, 0.12); color: var(--c-yellow); }
.sidebar-buttons button.active { background: rgba(217, 119, 6, 0.15); color: var(--c-yellow); }
.sidebar-buttons button i { font-size: 1rem; }

/* Right Sidebar */
.right-sidebar {
  position: fixed; top: 0; width: 420px; min-width: 280px; max-width: 600px;
  height: 100vh; background: rgba(255, 244, 230, 0.97);
  backdrop-filter: blur(15px); border-left: 1px solid var(--border-2);
  box-shadow: -10px 0 30px rgba(17, 24, 39, 0.14);
  transition: right 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  z-index: 1000; display: flex; flex-direction: column;
}
.resize-handle {
  position: absolute; left: 0; top: 0; width: 4px; height: 100%;
  background: transparent; cursor: col-resize; transition: background 0.2s;
}
.resize-handle:hover { background: rgba(180, 83, 9, 0.4); }

.sidebar-panel {
  display: flex; flex-direction: column; height: 100%;
}
.panel-header {
  padding: 16px; border-bottom: 1px solid var(--border);
  display: flex; justify-content: space-between; align-items: center;
  flex-shrink: 0;
}
.panel-header h3 { font-family: 'Fredoka', sans-serif; color: var(--c-mint); font-size: 1.1rem; }
.panel-close { background: none; border: none; color: var(--text-muted); font-size: 1.1rem; cursor: pointer; }
.panel-close:hover { color: var(--c-coral); }
.panel-body { flex: 1; overflow-y: auto; padding: 12px; }

.empty-state { text-align: center; padding: 40px 20px; color: var(--text-muted); }
.empty-state i { font-size: 3rem; color: #B45309; margin-bottom: 15px; display: block; }

/* Screenshot Items */
.screenshot-item {
  background: var(--surface); border: 1px solid var(--border);
  border-radius: 12px; margin-bottom: 16px; overflow: hidden;
}
.screenshot-img { width: 100%; aspect-ratio: 16/9; object-fit: cover; cursor: zoom-in; border-bottom: 1px solid var(--border); }
.screenshot-actions { padding: 8px; display: flex; gap: 6px; }
.btn-ai {
  flex: 1; padding: 7px; border: none; border-radius: 6px; color: white;
  font-size: 0.8rem; cursor: pointer; font-weight: 600;
  background: linear-gradient(135deg, var(--c-mint), var(--c-violet));
  font-family: 'Nunito', sans-serif;
}
.btn-del {
  padding: 7px 10px; border-radius: 6px; border: 1px solid rgba(194, 65, 12, 0.25);
  background: rgba(194, 65, 12, 0.10); color: var(--c-coral);
  cursor: pointer; font-size: 0.8rem;
}
.btn-del:hover { background: var(--c-coral); color: white; }
.ai-result {
  margin: 0 8px 8px; padding: 10px; background: rgba(180, 83, 9, 0.06);
  border-left: 3px solid var(--c-mint); border-radius: 0 8px 8px 0;
  font-size: 0.82rem; line-height: 1.5; max-height: 140px; overflow-y: auto;
}
.note-area { padding: 0 8px 8px; }
.note-input {
  width: 100%; min-height: 50px; background: var(--surface);
  border: 1px solid var(--border); border-radius: 8px; padding: 8px;
  font-size: 0.85rem; resize: vertical; outline: none; font-family: 'Nunito', sans-serif;
  color: var(--text-main);
}
.note-input:focus { border-color: var(--c-yellow); }

/* Chat */
.chat-messages {
  flex: 1; overflow-y: auto; display: flex; flex-direction: column; gap: 10px;
  margin-bottom: 12px;
}
.message { display: flex; gap: 8px; max-width: 90%; }
.message.user { align-self: flex-end; flex-direction: row-reverse; }
.message .avatar {
  width: 28px; height: 28px; border-radius: 50%; display: flex;
  align-items: center; justify-content: center; font-size: 0.7rem; flex-shrink: 0;
}
.message.user .avatar { background: rgba(217, 119, 6, 0.25); color: var(--c-yellow); }
.message.assistant .avatar { background: rgba(180, 83, 9, 0.20); color: var(--c-mint); }
.msg-content {
  padding: 8px 12px; border-radius: 12px; font-size: 0.85rem; line-height: 1.45;
}
.message.user .msg-content {
  background: rgba(217, 119, 6, 0.15); border-bottom-right-radius: 4px;
  border: 1px solid rgba(217, 119, 6, 0.30);
}
.message.assistant .msg-content {
  background: rgba(180, 83, 9, 0.08); border-bottom-left-radius: 4px;
  border: 1px solid rgba(180, 83, 9, 0.18);
}
.chat-input-row {
  display: flex; gap: 6px; padding-top: 8px; border-top: 1px solid var(--border);
  flex-shrink: 0;
}
.chat-input-row input {
  flex: 1; padding: 8px 12px; border: 1px solid var(--border-2); border-radius: 8px;
  background: var(--surface); color: var(--text-main); font-size: 0.85rem;
  outline: none; font-family: 'Nunito', sans-serif;
}
.chat-input-row input:focus { border-color: var(--c-yellow); }
.send-btn {
  padding: 0 14px; background: linear-gradient(135deg, var(--c-mint), var(--c-violet));
  color: white; border: none; border-radius: 8px; cursor: pointer; font-family: 'Nunito', sans-serif;
}
.send-btn:disabled { opacity: 0.5; cursor: not-allowed; }

/* Dashboard */
.dashboard-body { padding: 10px; }
.dash-stats { display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 8px; margin-bottom: 12px; }
.dash-stat {
  background: var(--surface); border: 1px solid var(--border);
  border-radius: 10px; padding: 10px 6px; text-align: center;
}
.dash-stat-value { font-family: 'Fredoka', sans-serif; font-size: 1.4rem; color: var(--c-mint); font-weight: 700; }
.dash-stat-label { font-size: 0.7rem; color: var(--text-muted); margin-top: 3px; }
.dash-card {
  background: var(--surface); border: 1px solid var(--border);
  border-radius: 10px; padding: 10px 10px 6px; margin-bottom: 10px;
}
.dash-card-title { font-size: 0.78rem; font-weight: 700; color: var(--c-mint); margin-bottom: 6px; }

@media (max-width: 768px) {
  .home-container { padding: 10px; }
  .header h1 { font-size: 1.8rem; }
  .nav-cards { grid-template-columns: 1fr; }
}
</style>
