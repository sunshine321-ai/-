# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 常用命令

```bash
# 后端（FastAPI，端口 8000）
pip install -r requirements.txt
python main.py

# 前端（Vue 3 + Vite，独立开发服务器）
cd frontend
npm install
npm run dev          # 默认端口 5173，通过 Vite proxy 转发 API 到 8000
npm run build        # 生产构建 → frontend/dist/
```

端口冲突时杀进程，**不换端口**：
```bash
netstat -ano | findstr :8000
taskkill /PID <PID> /F
```

健康检查：`GET http://localhost:8000/health` → 返回 video / html / api-key / db 四种状态。

无单测、无 lint、无构建步骤——后端就是 `python main.py` 一条命令跑全栈。

## 架构（必须先理解再改代码）

本项目有两套前端，**并行存在**：

### 1. 后端嵌入式 HTML（原始方案，仍在运行）

`main.py`（约 3400 行）把首页 HTML、AI 助教 HTML 都用 Python 长字符串内嵌返回。`/study` 读取外部 `课后学习资料_优化版.html`（6100 行）后在 `</body>` 前注入导出弹窗和劫持代码。

### 2. Vue 3 SPA 前端（工程化改造，`frontend/` 目录）

```
frontend/src/
├── main.ts                    # 入口，挂载 Pinia + Router + App
├── App.vue                    # 根组件：<router-view /> + ExportFAB + ExportModal
├── router/index.ts            # 7 条路由（/, /study, /ai-tutor, /playground, /showcase-3d, /data-viz, /calculator）
├── pages/                     # 页面组件（HomePage / StudyPage / AiTutorPage / PlaygroundPage / Showcase3DPage / DataVizPage / CalculatorPage）
├── components/                # 跨页面组件（ExportModal / ExportFAB / ChatPanel）
├── stores/export.ts           # 导出状态（Pinia），复刻了后端 get_shared_export_modal 的全部逻辑
├── stores/chat.ts / notes.ts  # 对话和笔记状态
└── composables/               # useIframePage 等组合式函数
```

Vue 前端通过 `<iframe>` 嵌入 `static/pages/*.html`（playground / showcase-3d / data-viz），通过 `localStorage` 共享数据。

### 三个主页面 + 四个子页面

| 路由 | 来源 | 主要内容 |
|---|---|---|
| `/` 首页/微课观影 | main.py 内嵌 (~273-1755) | 视频播放、截图笔记、AI 对话侧栏 |
| `/study` 课后学习 | main.py 读取外部 HTML + 注入 (~1781-1846) | 交互式课后资料、错题本、AI 问答 |
| `/ai-tutor` AI 助教 | main.py 内嵌 (~1848-2619) | 专职对话页面 |
| `/playground` | `static/pages/playground.html` | 卷积核交互实验 |
| `/showcase-3d` | `static/pages/showcase-3d.html` | 3D 卷积演示 |
| `/data-viz` | `static/pages/data-viz.html` | 数据可视化（ECharts） |
| `/calculator` | `Calculator.html` | 卷积运算计算器 |

### 数据库（2026.6 新增）

`database.py` 使用 `aiomysql` 连接池，启动时自动建库建表。两张表：

| 表 | 用途 |
|---|---|
| `user_data` | KV 存储，key 对应 6 个 localStorage key，user_id 固定 `default` |
| `chat_messages` | 按 context（home/study/tutor）分组的聊天记录 |

对应 API：
- `POST /api/data/sync` — 单 key 写入
- `GET /api/data/load` — 全量读取
- `POST /api/chat/sync` — 全量替换聊天记录
- `GET /api/chat/{context}` — 按上下文加载聊天

数据库是**可选的**——不配置 `MYSQL_PASSWORD` 时仅用 localStorage，不会报错。

### 跨页面统一导出（最容易踩坑的特性）

后端版：`get_shared_export_modal()`（38 行起）和 `get_shared_export_btn(page_id)` 生成同一套 HTML/CSS/JS，注入到三个老页面。

前端版：`frontend/src/stores/export.ts` 用 Pinia 复刻了全部逻辑（模块定义、勾选、分组、导出请求），`ExportModal.vue` + `ExportFAB.vue` 渲染 UI。

两套导出**共享同一个后端入口** `POST /api/export-combined`（~3137 行）。改导出时三处（后端弹窗函数 + 前端 Pinia store + 后端路由）必须一起改。

6 个可导出模块：

| 模块 | localStorage key | 类型 |
|---|---|---|
| 首页截图笔记 | `convolutionKernelNotes` | JSON 数组 |
| 首页 AI 对话 | `vlab_home_chat` | JSON 数组 |
| 课后学习笔记 | `convolution_notes` | HTML 字符串 |
| 课后错题本 | `convolution_wrong_questions` | JSON 数组 |
| 课后 AI 对话 | `vlab_study_chat` | JSON 数组 |
| AI 助教对话 | `vlab_tutor_chat` | JSON 数组 |

### 两套 AI 对话 API（**别合并**）

- `POST /ai-tutor/chat` → 返回 `{response}`，新版接口，首页和 AI 助教页用
- `POST /api/ask` → 返回 `{content}`，**老接口契约**，专门给 `课后学习资料_优化版.html` 用，改前先确认前端兼容

系统提示词位置：`/ai-tutor/chat` 约 2621-2692 行，`/api/ask` 约 2693-2750 行。

### 模型配置

- 文本模型：`.env` 的 `QWEN_MODEL`（默认 `qwen-plus`）
- 视觉模型：`qwen-vl-plus` **写死**在 `/api/analyze-screenshot`（~2828 行），换模型要改源码

## 改动指引

- **改首页视频/截图** → `main.py` ~273-1755（后端）+ `frontend/src/pages/HomePage.vue`（Vue 前端）
- **改课后学习页内容** → 改 `课后学习资料_优化版.html`，`main.py` 的注入逻辑尽量别碰
- **改 AI 助教页** → `main.py` ~1848-2619（后端）+ `frontend/src/pages/AiTutorPage.vue`
- **加新静态页** → 放 `static/pages/`，在 main.py 仿照 `/playground` 加 `_serve_static_page()` 路由，在 `frontend/src/router/index.ts` 加路由 + `frontend/src/pages/` 加页面组件
- **改 AI 提示词** → 上面两处 system_prompt
- **改导出** → 三处一起改：`get_shared_export_modal()` + `frontend/src/stores/export.ts` + `/api/export-combined`
- **改数据库表结构** → `database.py` 的 `CREATE_TABLES_SQL` + 对应 API 路由

## 历史遗留警示

- `requirements.txt` 中 streamlit / python-docx / reportlab / Pillow 当前主流程未使用，清理需谨慎
- `main.py` 已 3400+ 行，远超项目铁律的 400 行上限——这是历史遗留。**新功能优先抽到 `database.py` 那样的独立模块，或放 `static/pages/` / `frontend/src/`，不要继续往 `main.py` 堆**
- 老页面（main.py 内嵌 HTML）和 Vue 前端**并行运行**，两端都要维护。改动涉及页面 UI 时确认是否需要两端同步
- 评审材料 `*.docx` 由 `gen_doc1.js`/`gen_doc2.js` 生成，根目录的旧版本文件已删除
- `static/screenshots/` 和根目录 `screenshots/` 两个截图目录并存，前者是当前使用的
