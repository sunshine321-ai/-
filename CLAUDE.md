# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 常用命令

```bash
pip install -r requirements.txt
python main.py                          # 固定端口 9000
```

端口冲突时杀进程，**不换端口**：
```bash
netstat -ano | findstr :9000
taskkill /PID <PID> /F
```

健康检查：`GET http://localhost:9000/health` → 返回 video / html / api-key 状态。

无单测、无 lint、无构建步骤——本项目就是 `python main.py` 一条命令跑全栈。

## 架构（必须先理解再改代码）

这是一个**单文件 FastAPI 后端 + 嵌入式 HTML** 的奇特结构：

- `main.py`（约 3300 行）把首页 HTML、AI 助教 HTML 都用 Python 长字符串内嵌返回。`/study` 是唯一例外——读取外部的 `课后学习资料_优化版.html`（6100 行）后**在 `</body>` 前注入**导出弹窗和劫持代码。
- `static/pages/*.html`（playground / showcase-3d / data-viz）是独立子页面，由 `_serve_static_page()` 直接吐出。
- 三个主页面通过 `localStorage` 共享数据，**没有数据库**。服务端只把截图和导出文件写到 `static/`。

### 三个主页面与对应的 main.py 区域

| 路由 | main.py 行范围 | 主要内容 |
|---|---|---|
| `/` 首页/微课观影 | ~272–1747 | 视频播放、截图笔记、AI 对话侧栏 |
| `/study` 课后学习 | ~1780–1846 | 读外部 HTML + 注入逻辑（注入逻辑别动） |
| `/ai-tutor` AI 助教 | ~1847–2619 | 专职对话页面 |

### 跨页面统一导出（最容易踩坑的特性）

`get_shared_export_modal()`（37 行起）和 `get_shared_export_btn(page_id)`（235 行起）生成同一套 HTML/CSS/JS，被注入到三个页面。任意页面点「选择导出」都能勾选下面 6 个模块的任意组合：

| 模块 | localStorage key | 类型 |
|---|---|---|
| 首页截图笔记 | `convolutionKernelNotes` | JSON 数组 |
| 首页 AI 对话 | `vlab_home_chat` | JSON 数组 |
| 课后学习笔记 | `convolution_notes` | HTML 字符串 |
| 课后错题本 | `convolution_wrong_questions` | JSON 数组 |
| 课后 AI 对话 | `vlab_study_chat` | JSON 数组 |
| AI 助教对话 | `vlab_tutor_chat` | JSON 数组 |

后端入口是 `POST /api/export-combined`（~3136 行）。改导出格式时这两处函数和该路由必须一起改，否则三个页面表现会不一致。

### 两套 AI 对话 API（**别合并**）

- `POST /ai-tutor/chat` → 返回 `{response}`，新版接口，首页和 AI 助教页用
- `POST /api/ask` → 返回 `{content}`，**老接口契约**，专门给 `课后学习资料_优化版.html` 用，改前先确认前端兼容

系统提示词位置：`/ai-tutor/chat` 在 ~2227 行，`/api/ask` 在 ~2295 行。

### 模型配置

- 文本模型：`.env` 的 `QWEN_MODEL`（默认 `qwen-plus`）
- 视觉模型：`qwen-vl-plus` **写死**在 `/api/analyze-screenshot`（~2449 行），换模型要改源码

## 改动指引

- **改首页视频/截图** → `main.py` ~272–1370
- **改课后学习页内容** → 改 `课后学习资料_优化版.html`，`main.py` 的注入逻辑尽量别碰
- **加新静态页** → 放 `static/pages/`，在 main.py 仿照 `/playground` 加一个 `_serve_static_page()` 路由
- **改 AI 提示词** → 上面两处 system_prompt
- **改导出** → `get_shared_export_modal()` + `get_shared_export_btn()` + `/api/export-combined` 三处一起改

## 历史遗留警示

- `requirements.txt` 中 streamlit / python-docx / reportlab / Pillow 当前主流程未使用（`generate_reports.py` 用了 python-docx，是一次性脚本），清理需谨慎确认无人引用
- `main.py` 已 3300 行，远超项目铁律的 400 行上限——这是历史遗留。**新功能优先抽到独立模块或 `static/pages/`，不要继续往 `main.py` 堆**
- 评审材料 `*.docx` 由 `gen_doc1.js`/`gen_doc2.js` 生成，根目录的 `D` 状态文件是已删除的旧版本
