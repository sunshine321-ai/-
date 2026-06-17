"""
卷积核微课 - 虚拟实验室
基于FastAPI的计算机视觉微课学习平台
"""

import os
import json
import time
import base64
from pathlib import Path
from typing import Optional

import uvicorn
from fastapi import FastAPI, Request, HTTPException
from fastapi.responses import HTMLResponse, StreamingResponse, JSONResponse
from fastapi.staticfiles import StaticFiles
from fastapi.templating import Jinja2Templates
from dotenv import load_dotenv
import requests

# 加载环境变量
load_dotenv()

# ============ 配置 ============
BASE_DIR = Path(__file__).parent
VIDEO_FILE = BASE_DIR / "DougongGrowth.mp4"
HTML_FILE = BASE_DIR / "课后学习资料_优化版.html"
INDEX_FILE = BASE_DIR / "index.html"
AI_TUTOR_FILE = BASE_DIR / "ai_tutor.html"

# 千问API配置
DASHSCOPE_API_KEY = os.getenv("DASHSCOPE_API_KEY", "")
DASHSCOPE_BASE_URL = os.getenv("DASHSCOPE_BASE_URL", "https://dashscope.aliyuncs.com/api/v1")
QWEN_MODEL = os.getenv("QWEN_MODEL", "qwen-plus")
# ============ 跨页面统一导出弹窗（共享HTML+JS）============
def get_shared_export_modal() -> str:
    """
    返回可注入任意页面的「选择导出」弹窗 HTML+CSS+JS。
    读取 localStorage 中所有模块的数据，支持任意组合导出。
    调用方式：在页面 JS 中调用 openVlabModal('home'|'study'|'tutor')
    """
    return r"""
<!-- ===== 跨页面统一导出弹窗 ===== -->
<div id="vlabModal" style="display:none;position:fixed;inset:0;z-index:99999;background:rgba(0,0,0,0.65);backdrop-filter:blur(6px);">
  <div style="position:absolute;top:50%;left:50%;transform:translate(-50%,-50%);background:#141e33;border:1px solid rgba(255,255,255,0.1);border-radius:20px;width:min(580px,94vw);max-height:90vh;overflow-y:auto;box-shadow:0 32px 80px rgba(0,0,0,0.65);color:white;font-family:'Inter',-apple-system,'PingFang SC',sans-serif;">

    <!-- 头部 sticky -->
    <div style="padding:22px 28px 16px;border-bottom:1px solid rgba(255,255,255,0.07);display:flex;align-items:center;justify-content:space-between;position:sticky;top:0;background:#141e33;border-radius:20px 20px 0 0;z-index:1;">
      <div style="display:flex;align-items:center;gap:12px;">
        <div style="width:38px;height:38px;background:linear-gradient(135deg,#00d9ff,#0073ff);border-radius:10px;display:flex;align-items:center;justify-content:center;flex-shrink:0;">
          <i class="fas fa-file-export"></i>
        </div>
        <div>
          <div style="font-size:1.1rem;font-weight:700;letter-spacing:-0.3px;">选择导出内容</div>
          <div style="font-size:0.77rem;color:#64748b;margin-top:1px;">可跨页面自由组合任意模块</div>
        </div>
      </div>
      <button onclick="vlabCloseModal()" style="background:rgba(255,255,255,0.06);border:none;color:#94a3b8;cursor:pointer;width:32px;height:32px;border-radius:8px;font-size:1rem;">
        <i class="fas fa-times"></i>
      </button>
    </div>

    <!-- 模块列表 -->
    <div id="vlabModuleList" style="padding:16px 28px 4px;"></div>

    <!-- 格式选择 -->
    <div style="padding:0 28px 16px;">
      <div style="background:rgba(255,255,255,0.04);border-radius:14px;padding:16px;">
        <p style="color:#64748b;font-size:0.78rem;font-weight:600;text-transform:uppercase;letter-spacing:0.6px;margin-bottom:12px;">📄 导出格式</p>
        <div style="display:flex;gap:12px;">
          <div id="vlabFmtWord" onclick="vlabSelectFmt('word')" style="flex:1;border:2px solid #00d9ff;background:rgba(0,217,255,0.07);border-radius:12px;padding:14px;text-align:center;cursor:pointer;transition:0.25s;">
            <i class="fas fa-file-word" style="color:#2b7cd3;font-size:1.6rem;display:block;margin-bottom:7px;"></i>
            <div style="font-weight:600;font-size:0.88rem;">Word 文档</div>
            <div style="color:#64748b;font-size:0.73rem;margin-top:3px;">.doc，可直接编辑</div>
          </div>
          <div id="vlabFmtPDF" onclick="vlabSelectFmt('pdf')" style="flex:1;border:2px solid rgba(255,255,255,0.1);background:rgba(255,255,255,0.03);border-radius:12px;padding:14px;text-align:center;cursor:pointer;transition:0.25s;">
            <i class="fas fa-file-pdf" style="color:#c9302c;font-size:1.6rem;display:block;margin-bottom:7px;"></i>
            <div style="font-weight:600;font-size:0.88rem;">PDF 文件</div>
            <div style="color:#64748b;font-size:0.73rem;margin-top:3px;">浏览器打印另存</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 操作按钮 -->
    <div style="padding:0 28px 20px;display:flex;gap:12px;">
      <button onclick="vlabCloseModal()" style="flex:1;padding:13px;border:1px solid rgba(255,255,255,0.1);background:none;border-radius:12px;color:#94a3b8;cursor:pointer;font-size:0.95rem;">取消</button>
      <button onclick="vlabDoExport()" id="vlabExportBtn" style="flex:2.5;padding:13px;border:none;background:linear-gradient(135deg,#00d9ff,#0073ff);border-radius:12px;color:white;cursor:pointer;font-size:0.95rem;font-weight:600;">
        <i class="fas fa-download"></i> 开始导出
      </button>
    </div>
    <p id="vlabExportMsg" style="text-align:center;font-size:0.85rem;padding:0 28px 20px;min-height:20px;color:#64748b;"></p>
  </div>
</div>

<style>
.vlab-section-label{font-size:.73rem;color:#475569;text-transform:uppercase;letter-spacing:.6px;font-weight:600;margin:14px 0 8px;display:flex;align-items:center;gap:6px;}
.vlab-mod-card{background:rgba(255,255,255,0.04);border:1.5px solid rgba(255,255,255,0.07);border-radius:12px;padding:12px 14px;margin-bottom:8px;cursor:pointer;transition:.2s;user-select:none;}
.vlab-mod-card.has-data:hover{background:rgba(255,255,255,0.07);}
.vlab-mod-card.selected{border-color:rgba(0,217,255,.4);background:rgba(0,217,255,.06);}
.vlab-mod-card.no-data{opacity:.4;cursor:default;}
.vlab-mod-row{display:flex;align-items:center;gap:10px;}
.vlab-checkbox{width:18px;height:18px;border-radius:5px;border:2px solid rgba(255,255,255,.2);display:flex;align-items:center;justify-content:center;flex-shrink:0;transition:.15s;}
.vlab-checkbox.checked{background:#00d9ff;border-color:#00d9ff;color:#000;font-size:.72rem;font-weight:800;}
.vlab-mod-icon{width:32px;height:32px;border-radius:8px;display:flex;align-items:center;justify-content:center;font-size:.88rem;flex-shrink:0;}
</style>

<script>
(function(){
/* ======== 跨页面统一导出弹窗核心逻辑 ======== */
window._vlabCurrentPage='unknown';
window._vlabExportFmt='word';
window._vlabState={};

const VLAB_MODS=[
  {id:'screenshot_notes', label:'首页截图笔记',        icon:'fa-camera',         bg:'#0e3f6e',color:'#00d9ff',page:'首页',     key:'convolutionKernelNotes',         kind:'json_array', secType:'screenshot_notes'},
  {id:'home_chat',        label:'首页 AI 学习助手对话', icon:'fa-robot',          bg:'#3b1a6e',color:'#a855f7',page:'首页',     key:'vlab_home_chat',                  kind:'json_array', secType:'ai_chat'},
  {id:'study_notes',      label:'课后学习笔记',          icon:'fa-pen-nib',        bg:'#063e28',color:'#22c55e',page:'课后习题',key:'convolution_notes',               kind:'html_string',secType:'study_notes'},
  {id:'wrong_questions',  label:'课后错题本',            icon:'fa-book-open',      bg:'#4a1010',color:'#ef4444',page:'课后习题',key:'convolution_wrong_questions',     kind:'json_array', secType:'wrong_questions'},
  {id:'study_chat',       label:'课后 AI 对话',          icon:'fa-comments',       bg:'#3d2a00',color:'#f59e0b',page:'课后习题',key:'vlab_study_chat',                 kind:'json_array', secType:'ai_chat'},
  {id:'tutor_chat',       label:'AI 助教对话',           icon:'fa-graduation-cap', bg:'#1a1a4a',color:'#6366f1',page:'AI助教', key:'vlab_tutor_chat',                 kind:'json_array', secType:'ai_chat'},
];

function _getData(mod){
  const raw=localStorage.getItem(mod.key);
  if(!raw)return null;
  if(mod.kind==='json_array'){try{const d=JSON.parse(raw);return d&&d.length>0?d:null;}catch(e){return null;}}
  return raw.trim()?raw:null;
}
function _count(mod){
  const d=_getData(mod);
  if(!d)return 0;
  return Array.isArray(d)?d.length:1;
}

window.vlabSelectFmt=function(fmt){
  window._vlabExportFmt=fmt;
  const w=document.getElementById('vlabFmtWord'),p=document.getElementById('vlabFmtPDF');
  if(fmt==='word'){
    w.style.border='2px solid #00d9ff';w.style.background='rgba(0,217,255,.07)';
    p.style.border='2px solid rgba(255,255,255,.1)';p.style.background='rgba(255,255,255,.03)';
  }else{
    p.style.border='2px solid #c9302c';p.style.background='rgba(201,48,44,.07)';
    w.style.border='2px solid rgba(255,255,255,.1)';w.style.background='rgba(255,255,255,.03)';
  }
};

window._vlabToggle=function(id){
  const s=window._vlabState[id];
  if(!s||s.disabled)return;
  s.checked=!s.checked;
  const card=document.getElementById('vlabCard_'+id);
  const chk=document.getElementById('vlabChk_'+id);
  if(s.checked){card.classList.add('selected');chk.classList.add('checked');chk.textContent='✓';}
  else{card.classList.remove('selected');chk.classList.remove('checked');chk.textContent='';}
};

function _buildModuleList(){
  window._vlabState={};
  const groups={};
  VLAB_MODS.forEach(m=>{if(!groups[m.page])groups[m.page]=[];groups[m.page].push(m);});
  let html='';
  Object.keys(groups).forEach(page=>{
    html+=`<div class="vlab-section-label"><i class="fas fa-layer-group"></i>${page}</div>`;
    groups[page].forEach(mod=>{
      const cnt=_count(mod),has=cnt>0;
      const badge=Array.isArray(_getData(mod))?`${cnt} 条`:(has?'有内容':'暂无');
      window._vlabState[mod.id]={checked:has,disabled:!has};
      html+=`<div class="vlab-mod-card ${has?'has-data selected':'no-data'}" id="vlabCard_${mod.id}" onclick="_vlabToggle('${mod.id}')">
        <div class="vlab-mod-row">
          <div class="vlab-checkbox ${has?'checked':''}" id="vlabChk_${mod.id}">${has?'✓':''}</div>
          <div class="vlab-mod-icon" style="background:${mod.bg};color:${mod.color};"><i class="fas ${mod.icon}"></i></div>
          <div style="flex:1;">
            <div style="font-size:.88rem;font-weight:600;color:#e2e8f0;">${mod.label}</div>
            <div style="font-size:.75rem;color:${has?mod.color:'#475569'};margin-top:2px;">${has?badge:'暂无数据'}</div>
          </div>
          ${!has?'<div style="font-size:.7rem;color:#475569;background:rgba(255,255,255,.05);padding:3px 8px;border-radius:20px;">未记录</div>':''}
        </div>
      </div>`;
    });
  });
  document.getElementById('vlabModuleList').innerHTML=html;
}

window.openVlabModal=function(pageId){
  window._vlabCurrentPage=pageId;
  if(typeof window._vlabSavePage==='function')window._vlabSavePage(pageId);
  _buildModuleList();
  document.getElementById('vlabExportMsg').textContent='';
  document.getElementById('vlabModal').style.display='block';
  vlabSelectFmt('word');
};

window.vlabCloseModal=function(){
  document.getElementById('vlabModal').style.display='none';
};

window.vlabDoExport=async function(){
  const btn=document.getElementById('vlabExportBtn');
  const msg=document.getElementById('vlabExportMsg');
  const sections=[];
  VLAB_MODS.forEach(mod=>{
    if(!(window._vlabState[mod.id]&&window._vlabState[mod.id].checked))return;
    const data=_getData(mod);
    if(!data)return;
    if(mod.secType==='screenshot_notes')sections.push({type:'screenshot_notes',items:data});
    else if(mod.secType==='ai_chat')sections.push({type:'ai_chat',messages:data,label:mod.label});
    else if(mod.secType==='wrong_questions')sections.push({type:'wrong_questions',items:data});
    else if(mod.secType==='study_notes')sections.push({type:'study_notes',content:data});
  });
  if(!sections.length){
    msg.textContent='⚠️ 请至少勾选一个有内容的模块';
    msg.style.color='#f87171';return;
  }
  btn.disabled=true;btn.innerHTML='<i class="fas fa-spinner fa-spin"></i> 生成中...';msg.textContent='';
  try{
    const r=await fetch('/api/export-combined',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({format:window._vlabExportFmt,sections})});
    const d=await r.json();
    if(d.success){
      window.open(d.url,'_blank');
      msg.textContent=window._vlabExportFmt==='pdf'?'✅ 已打开，请按 Ctrl+P → 另存为 PDF':'✅ 文档已生成，请在新标签页另存';
      msg.style.color='#4ade80';
    }else{msg.textContent='❌ 导出失败：'+(d.error||'未知错误');msg.style.color='#f87171';}
  }catch(e){msg.textContent='❌ 网络错误：'+e.message;msg.style.color='#f87171';}
  finally{btn.disabled=false;btn.innerHTML='<i class="fas fa-download"></i> 开始导出';}
};

document.getElementById('vlabModal').addEventListener('click',function(e){if(e.target===this)vlabCloseModal();});
})();
</script>
"""


def get_shared_export_btn(page_id: str, extra_style: str = "") -> str:
    """返回悬浮的「选择导出」按钮 HTML，注入任意页面。"""
    return f"""
<div style="position:fixed;bottom:28px;right:28px;z-index:9998;{extra_style}">
  <button onclick="openVlabModal('{page_id}')"
          style="display:flex;align-items:center;gap:8px;padding:13px 22px;background:linear-gradient(135deg,#00d9ff,#0073ff);border:none;border-radius:50px;color:white;font-size:.95rem;font-weight:700;cursor:pointer;box-shadow:0 8px 24px rgba(0,115,255,.4);transition:.25s;"
          onmouseover="this.style.transform='translateY(-2px)';this.style.boxShadow='0 12px 32px rgba(0,115,255,.5)'"
          onmouseout="this.style.transform='';this.style.boxShadow='0 8px 24px rgba(0,115,255,.4)'">
    <i class="fas fa-file-export"></i> 选择导出
  </button>
</div>
"""


# ============ FastAPI应用 ============
app = FastAPI(
    title="卷积核微课 - 虚拟实验室",
    description="计算机视觉微课学习平台",
    version="1.0.0"
)

# 静态文件及生成文件目录配置
static_dir = BASE_DIR / "static"
screenshots_dir = static_dir / "screenshots"
exports_dir = static_dir / "exports"

static_dir.mkdir(exist_ok=True)
screenshots_dir.mkdir(exist_ok=True)
exports_dir.mkdir(exist_ok=True)

app.mount("/static", StaticFiles(directory=str(static_dir)), name="static")

# Jinja2模板
templates = Jinja2Templates(directory=str(BASE_DIR))


# ============ ??/???? ============
@app.get("/", response_class=HTMLResponse)
async def index():
    """??????"""
    if not INDEX_FILE.exists():
        raise HTTPException(status_code=404, detail="???????")
    with open(INDEX_FILE, 'r', encoding='utf-8') as f:
        return f.read()


# ============ 课后学习 ============
@app.get("/study", response_class=HTMLResponse)
async def study():
    """课后学习页面（注入跨页面导出弹窗）"""
    if not HTML_FILE.exists():
        raise HTTPException(status_code=404, detail="学习资料文件不存在")

    with open(HTML_FILE, 'r', encoding='utf-8') as f:
        html_content = f.read()

    # 注入：课后AI对话保存到 localStorage + _vlabSavePage 钩子
    study_inject_script = r"""
<script>
(function(){
  // 课后习题页：保存 aiChatHistory 到 vlab_study_chat
  // 等页面加载完成后再劫持 addChatMessage
  function _hookStudyChat() {
    if (typeof addChatMessage !== 'function') {
      setTimeout(_hookStudyChat, 200);
      return;
    }
    const _orig = addChatMessage;
    window.addChatMessage = function(content, role) {
      const r = _orig.call(this, content, role);
      setTimeout(function() {
        try {
          var hist = window.aiChatHistory || [];
          var exportable = hist.filter(function(m) {
            return !(m.content||'').includes('loading-dots');
          });
          localStorage.setItem('vlab_study_chat', JSON.stringify(exportable));
        } catch(e) {}
      }, 80);
      return r;
    };
  }
  _hookStudyChat();

  // 供共享弹窗打开前调用
  window._vlabSavePage = function(pageId) {
    if (pageId === 'study') {
      try {
        var hist = window.aiChatHistory || [];
        var exportable = hist.filter(function(m) {
          return !(m.content||'').includes('loading-dots');
        });
        localStorage.setItem('vlab_study_chat', JSON.stringify(exportable));
      } catch(e) {}
    }
  };
})();
</script>
"""
    # 将注入脚本和共享弹窗插入到 </body> 之前
    inject_block = (
        study_inject_script
        + get_shared_export_modal()
        + get_shared_export_btn('study')
    )
    if '</body>' in html_content:
        html_content = html_content.replace('</body>', inject_block + '\n</body>', 1)
    else:
        html_content += inject_block

    return html_content



# ============ AI?? ============
@app.get("/ai-tutor", response_class=HTMLResponse)
async def ai_tutor():
    """AI????"""
    if not AI_TUTOR_FILE.exists():
        raise HTTPException(status_code=404, detail="AI?????????")
    with open(AI_TUTOR_FILE, 'r', encoding='utf-8') as f:
        return f.read()


# ============ AI聊天API ============
@app.post("/ai-tutor/chat")
async def chat(request: Request):
    """AI聊天接口"""
    if not DASHSCOPE_API_KEY:
        return JSONResponse({"error": "API密钥未配置"}, status_code=400)

    try:
        body = await request.json()
        user_message = body.get("message", "")

        if not user_message:
            return JSONResponse({"error": "消息不能为空"}, status_code=400)

        # 系统提示词
        system_prompt = """你是一位专业的计算机视觉助教，专门面向初中生讲解卷积核（Convolution Kernel）的相关知识。

### 回答要求：
1. **排版清晰**：必须分段，用数字列表或项目符号展示要点，大段内容要拆分
2. **语言简单**：避免专业术语，用初中生能理解的通俗语言解释
3. **重点突出**：重要概念用**加粗**标记，关键步骤单独成行
4. **结构友好**：先讲结论，再讲细节，例子单独放在代码块或引用块里
5. **避免密集**：每部分内容不要超过3行，多空行分隔

你可以帮助学生理解以下内容：
- 卷积核的定义和原理
- 常见卷积核类型（边缘检测、模糊、锐化、Sobel等）
- 卷积操作的过程和计算
- 卷积神经网络中卷积层的作用
- 实际应用场景和例子

如果学生问的问题与卷积核或计算机视觉无关，请礼貌地引导他们回到主题上来。"""

        # 调用千问API
        url = f"{DASHSCOPE_BASE_URL}/services/aigc/text-generation/generation"
        headers = {
            "Authorization": f"Bearer {DASHSCOPE_API_KEY}",
            "Content-Type": "application/json"
        }
        payload = {
            "model": QWEN_MODEL,
            "input": {
                "messages": [
                    {"role": "system", "content": system_prompt},
                    {"role": "user", "content": user_message}
                ]
            },
            "parameters": {
                "result_format": "message",
                "temperature": 0.7
            }
        }

        response = requests.post(url, headers=headers, json=payload, timeout=30)
        result = response.json()

        if "output" in result and "choices" in result["output"]:
            ai_response = result["output"]["choices"][0]["message"]["content"]
        elif "usage" in result:
            # 处理其他响应格式
            ai_response = str(result)
        else:
            return JSONResponse({"error": f"API响应异常: {result}"}, status_code=500)

        return {"response": ai_response}

    except requests.Timeout:
        return JSONResponse({"error": "请求超时，请稍后重试"}, status_code=504)
    except Exception as e:
        return JSONResponse({"error": str(e)}, status_code=500)


# ============ 兼容旧版课后学习的API ============
@app.post("/api/ask")
async def ask_compatibility(request: Request):
    """专门为了兼容 课后学习资料.html 中写死的旧版请求"""
    if not DASHSCOPE_API_KEY:
        return JSONResponse({"error": "API密钥未配置"}, status_code=400)

    try:
        body = await request.json()
        # 课后学习资料.html 传的参数名叫 question
        question = body.get("question", "")
        system_prompt = body.get("system_prompt", """你是一个专业、耐心的深度学习助教，面向初中生讲解卷积核知识。
### 回答要求：
1. 排版清晰：必须分段，用数字列表或项目符号展示要点，大段内容要拆分
2. 语言简单：避免专业术语，用初中生能理解的通俗语言解释
3. 重点突出：重要概念用**加粗**标记，关键步骤单独成行
4. 结构友好：先讲结论，再讲细节，例子单独展示
5. 避免密集：每部分内容不要超过3行，多空行分隔
""")

        if not question:
            return JSONResponse({"error": "消息不能为空"}, status_code=400)

        url = f"{DASHSCOPE_BASE_URL}/services/aigc/text-generation/generation"
        headers = {
            "Authorization": f"Bearer {DASHSCOPE_API_KEY}",
            "Content-Type": "application/json"
        }
        payload = {
            "model": QWEN_MODEL,
            "input": {
                "messages": [
                    {"role": "system", "content": system_prompt},
                    {"role": "user", "content": question}
                ]
            },
            "parameters": {
                "result_format": "message",
                "temperature": 0.7
            }
        }

        response = requests.post(url, headers=headers, json=payload, timeout=30)
        result = response.json()

        if "output" in result and "choices" in result["output"]:
            ai_response = result["output"]["choices"][0]["message"]["content"]
            # 关键：老版本 HTML 期望的返回值格式是 {"content": "..."}
            return {"content": ai_response}
        else:
            return JSONResponse({"error": "API响应异常"}, status_code=500)

    except requests.Timeout:
        return JSONResponse({"error": "请求超时"}, status_code=504)
    except Exception as e:
        return JSONResponse({"error": str(e)}, status_code=500)


# ============ 视频流 ============
@app.get("/video/stream")
async def stream_video(request: Request):
    """视频流式传输 (支持拖动进度条/Range断点续传)"""
    if not VIDEO_FILE.exists():
        raise HTTPException(status_code=404, detail="视频文件不存在")

    file_size = VIDEO_FILE.stat().st_size
    range_header = request.headers.get("Range")

    if range_header:
        # 解析 Range 头，例如 "bytes=5000000-10000000"
        range_str = range_header.replace("bytes=", "")
        start_str, end_str = range_str.split("-")
        start = int(start_str) if start_str else 0
        end = int(end_str) if end_str else file_size - 1
        status_code = 206  # 206 Partial Content 表示部分内容
    else:
        start = 0
        end = file_size - 1
        status_code = 200

    # 确保 range 合法
    if start >= file_size or end >= file_size:
        return StreamingResponse(status_code=416, headers={"Content-Range": f"bytes */{file_size}"})

    chunk_size = end - start + 1

    def iter_file_range(start_byte, length):
        with open(VIDEO_FILE, "rb") as f:
            f.seek(start_byte)  # 直接跳到浏览器请求的时间点
            bytes_read = 0
            while bytes_read < length:
                read_size = min(1024 * 1024, length - bytes_read)  # 每次读1MB
                chunk = f.read(read_size)
                if not chunk:
                    break
                bytes_read += len(chunk)
                yield chunk

    headers = {
        "Content-Range": f"bytes {start}-{end}/{file_size}",
        "Accept-Ranges": "bytes",
        "Content-Length": str(chunk_size),
        "Content-Type": "video/mp4",
    }

    return StreamingResponse(
        iter_file_range(start, chunk_size),
        status_code=status_code,
        headers=headers
    )


# ============ 新增：截图笔记与导出API ============
@app.post("/api/screenshot")
async def save_screenshot(request: Request):
    """保存前端传来的视频截图"""
    try:
        data = await request.json()
        image_data = data.get("image", "")
        if not image_data:
            return {"success": False, "error": "没有收到图片数据"}

        # 解析 base64 (格式: data:image/png;base64,iVBORw0KGgo...)
        header, encoded = image_data.split(",", 1)
        img_bytes = base64.b64decode(encoded)
        filename = f"screenshot_{int(time.time())}.png"
        filepath = screenshots_dir / filename
        
        with open(filepath, "wb") as f:
            f.write(img_bytes)

        return {"success": True, "url": f"/static/screenshots/{filename}"}
    except Exception as e:
        return {"success": False, "error": str(e)}


@app.post("/api/analyze-screenshot")
async def analyze_screenshot(request: Request):
    """调用千问大模型进行图像视觉分析 (多模态)"""
    if not DASHSCOPE_API_KEY:
        return JSONResponse({"error": "未配置 DashScope API Key"}, status_code=400)

    try:
        data = await request.json()
        image_url = data.get("image_path")
        if not image_url:
            return JSONResponse({"error": "缺少图片参数"}, status_code=400)

        # 提取真实文件路径
        filename = image_url.split("/")[-1]
        filepath = screenshots_dir / filename
        if not filepath.exists():
            return JSONResponse({"error": "图片文件不存在"}, status_code=404)

        # 转换为 base64 以保证 API 可以读取到本地图片
        with open(filepath, "rb") as f:
            base64_img = base64.b64encode(f.read()).decode('utf-8')

        # 使用兼容 OpenAI 格式的多模态请求 (调用 qwen-vl-plus)
        url = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions"
        headers = {
            "Authorization": f"Bearer {DASHSCOPE_API_KEY}",
            "Content-Type": "application/json"
        }
        payload = {
            "model": "qwen-vl-plus",  # 视觉模型
            "messages": [
                {
                    "role": "user",
                    "content": [
                        {
                            "type": "image_url",
                            "image_url": {"url": f"data:image/png;base64,{base64_img}"}
                        },
                        {
                            "type": "text",
                            "text": "你是一个专业的计算机视觉助教。请仔细观察这张视频教学截图，提取其中关于卷积核或图像处理的核心知识点，用通俗易懂的语言总结画面内容。要求简明扼要，有条理。"
                        }
                    ]
                }
            ]
        }
        
        resp = requests.post(url, headers=headers, json=payload, timeout=40)
        result = resp.json()

        if "choices" in result and len(result["choices"]) > 0:
            ai_text = result["choices"][0]["message"]["content"]
            # 简单处理 Markdown 换行，使其在前端可以正常显示
            formatted_text = ai_text.replace('\n', '<br>')
            return {"content": formatted_text}
        else:
            return {"error": f"大模型无法解析图片，请稍后重试。"}
    except requests.Timeout:
        return {"error": "AI 解析超时，可能图片较大或网络拥堵"}
    except Exception as e:
        return {"error": f"服务器内部错误: {str(e)}"}


@app.post("/api/export-word")
async def export_word(request: Request):
    """导出为 Word 兼容格式 (无需安装 python-docx 依赖)"""
    try:
        data = await request.json()
        notes = data.get("notes", [])
        
        # 组装纯净的 HTML，Word 能够原生无损解析这套结构
        html_content = "<html><head><meta charset='utf-8'><title>学习笔记</title></head><body>"
        html_content += "<h1 style='text-align:center;'>计算机视觉微课 - 学习笔记</h1><hr/>"
        
        for i, note in enumerate(notes):
            html_content += f"<h2>截图 {i+1}</h2>"
            # 植入 Base64 图片，确保断网也能看
            img_path = BASE_DIR / note['image_url'].lstrip('/')
            if img_path.exists():
                with open(img_path, "rb") as f:
                    b64 = base64.b64encode(f.read()).decode('utf-8')
                html_content += f"<img src='data:image/png;base64,{b64}' width='500'/><br><br>"
                
            if note.get('user_note'):
                html_content += f"<h3>📝 我的感悟：</h3><p style='background:#f5f5f5;padding:10px;'>{note['user_note']}</p>"
            if note.get('ai_analysis'):
                html_content += f"<h3>🤖 AI 分析：</h3><p style='background:#eef8ff;padding:10px;'>{note['ai_analysis']}</p>"
            html_content += "<br><hr/>"
            
        html_content += "</body></html>"
        
        filename = f"StudyNotes_{int(time.time())}.doc"
        filepath = exports_dir / filename
        with open(filepath, "w", encoding="utf-8") as f:
            f.write(html_content)
            
        return {"success": True, "url": f"/static/exports/{filename}"}
    except Exception as e:
        return {"success": False, "error": str(e)}


@app.post("/api/export-pdf")
async def export_pdf(request: Request):
    """生成排版精美的独立网页，便于用户通过浏览器打印成高清 PDF (免依赖)"""
    try:
        data = await request.json()
        notes = data.get("notes", [])
        
        html_content = f"""
        <html><head><meta charset='utf-8'><title>学习笔记 (PDF打印版)</title>
        <style>
            body {{ font-family: 'PingFang SC', sans-serif; line-height: 1.6; max-width: 800px; margin: 0 auto; padding: 40px; color: #333; }}
            h1 {{ text-align: center; color: #1e293b; border-bottom: 2px solid #e2e8f0; padding-bottom: 20px; }}
            .note-card {{ border: 1px solid #cbd5e1; padding: 20px; margin-bottom: 30px; border-radius: 12px; page-break-inside: avoid; background: white; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1); }}
            img {{ max-width: 100%; border-radius: 8px; border: 1px solid #e2e8f0; margin: 15px 0; }}
            .section {{ padding: 15px; border-radius: 8px; margin-top: 15px; }}
            .user-note {{ background: #f8fafc; border-left: 4px solid #3b82f6; }}
            .ai-note {{ background: #faf5ff; border-left: 4px solid #a855f7; }}
            @media print {{ body {{ padding: 0; }} .note-card {{ box-shadow: none; border: 1px solid #000; }} }}
        </style>
        </head><body onload="setTimeout(()=>window.print(), 500)">
        <h1>计算机视觉微课 - 学习笔记</h1>
        """
        
        for i, note in enumerate(notes):
            html_content += f"<div class='note-card'><h2>关键画面 {i+1}</h2>"
            img_path = BASE_DIR / note['image_url'].lstrip('/')
            if img_path.exists():
                with open(img_path, "rb") as f:
                    b64 = base64.b64encode(f.read()).decode('utf-8')
                html_content += f"<img src='data:image/png;base64,{b64}'/>"
            
            if note.get('user_note'):
                html_content += f"<div class='section user-note'><b>📝 我的感悟：</b><br>{note['user_note']}</div>"
            if note.get('ai_analysis'):
                html_content += f"<div class='section ai-note'><b>🤖 AI 分析：</b><br>{note['ai_analysis']}</div>"
            html_content += "</div>"
            
        html_content += "</body></html>"
        
        filename = f"StudyNotes_Print_{int(time.time())}.html"
        filepath = exports_dir / filename
        with open(filepath, "w", encoding="utf-8") as f:
            f.write(html_content)
            
        return {"success": True, "url": f"/static/exports/{filename}"}
    except Exception as e:
        return {"success": False, "error": str(e)}


# ============ 错题本导出接口 ============
@app.post("/api/export-wrong-word")
async def export_wrong_word(request: Request):
    """导出错题本为Word格式"""
    try:
        data = await request.json()
        wrongQuestions = data.get("wrongQuestions", [])

        html_content = "<html><head><meta charset='utf-8'><title>卷积核错题本</title></head><body>"
        html_content += "<h1 style='text-align:center;'>卷积核课后练习 - 错题本</h1><hr/>"

        for i, q in enumerate(wrongQuestions):
            html_content += f"<h2>{i+1}. {q['title']}</h2>"
            html_content += f"<p><strong>题目：</strong>{q['question']}</p>"
            html_content += "<h3>选项：</h3><ul>"
            for opt in q['options']:
                if opt == q['correctAnswer']:
                    html_content += f"<li style='color: green;'>{opt} ✅ (正确答案)</li>"
                elif opt.startswith(q['userChoice']):
                    html_content += f"<li style='color: red;'>{opt} ❌ (你的选择)</li>"
                else:
                    html_content += f"<li>{opt}</li>"
            html_content += "</ul>"
            html_content += f"<p><strong>AI解析：</strong></p><p style='background:#f5f3ff;padding:10px;'>{q['aiExplanation']}</p>"
            html_content += f"<p style='color:#666;'>添加时间：{q['addTime']}</p>"
            html_content += "<br><hr/>"

        html_content += "</body></html>"

        filename = f"WrongQuestions_{int(time.time())}.doc"
        filepath = exports_dir / filename
        with open(filepath, "w", encoding="utf-8") as f:
            f.write(html_content)

        return {"success": True, "url": f"/static/exports/{filename}"}
    except Exception as e:
        return {"success": False, "error": str(e)}


@app.post("/api/export-wrong-pdf")
async def export_wrong_pdf(request: Request):
    """导出错题本为PDF格式"""
    try:
        data = await request.json()
        wrongQuestions = data.get("wrongQuestions", [])

        html_content = f"""
        <html><head><meta charset='utf-8'><title>错题本 (PDF打印版)</title>
        <style>
            body {{ font-family: 'PingFang SC', sans-serif; line-height: 1.6; max-width: 800px; margin: 0 auto; padding: 40px; color: #333; }}
            h1 {{ text-align: center; color: #991B1B; border-bottom: 2px solid #FECDD3; padding-bottom: 20px; }}
            .wrong-card {{ border: 1px solid #FECDD3; padding: 20px; margin-bottom: 30px; border-radius: 12px; page-break-inside: avoid; background: #FFF1F2; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1); }}
            .option-item {{ margin: 5px 0; }}
            .correct {{ color: #065F46; font-weight: bold; }}
            .wrong {{ color: #991B1B; text-decoration: line-through; }}
            .ai-explanation {{ background: #F5F3FF; padding: 15px; border-radius: 8px; border-left: 4px solid #8B5CF6; margin: 10px 0; }}
            .time {{ color: #666; font-size: 0.9rem; }}
            @media print {{ body {{ padding: 0; }} .wrong-card {{ box-shadow: none; border: 1px solid #991B1B; }} }}
        </style>
        </head><body onload="setTimeout(()=>window.print(), 500)">
        <h1>📕 卷积核课后练习 - 错题本</h1>
        """

        for i, q in enumerate(wrongQuestions):
            html_content += f"<div class='wrong-card'><h2>{i+1}. {q['title']}</h2>"
            html_content += f"<p><strong>题目：</strong>{q['question']}</p>"
            html_content += "<h4>选项：</h4>"
            for opt in q['options']:
                if opt == q['correctAnswer']:
                    html_content += f"<p class='option-item correct'>{opt} ✅ (正确答案)</p>"
                elif opt.startswith(q['userChoice']):
                    html_content += f"<p class='option-item wrong'>{opt} ❌ (你的选择)</p>"
                else:
                    html_content += f"<p class='option-item'>{opt}</p>"
            html_content += f"<div class='ai-explanation'><strong>🤖 AI解析：</strong><br>{q['aiExplanation']}</div>"
            html_content += f"<p class='time'>添加时间：{q['addTime']}</p>"
            html_content += "</div>"

        html_content += "</body></html>"

        filename = f"WrongQuestions_Print_{int(time.time())}.html"
        filepath = exports_dir / filename
        with open(filepath, "w", encoding="utf-8") as f:
            f.write(html_content)

        return {"success": True, "url": f"/static/exports/{filename}"}
    except Exception as e:
        return {"success": False, "error": str(e)}


# ============ 学习笔记导出接口 ============
@app.post("/api/export-notes-word")
async def export_notes_word(request: Request):
    """导出学习笔记为Word格式"""
    try:
        data = await request.json()
        content = data.get("content", "")

        if not content or content.strip() == '':
            return {"success": False, "error": "笔记内容为空"}

        html_content = "<html><head><meta charset='utf-8'><title>卷积核学习笔记</title></head><body>"
        html_content += "<h1 style='text-align:center;'>卷积核学习笔记</h1><hr/>"
        html_content += content
        html_content += f"<br><br><p style='text-align:right; color:#666;'>导出时间：{time.strftime('%Y-%m-%d %H:%M:%S')}</p>"
        html_content += "</body></html>"

        filename = f"StudyNotes_{int(time.time())}.doc"
        filepath = exports_dir / filename
        with open(filepath, "w", encoding="utf-8") as f:
            f.write(html_content)

        return {"success": True, "url": f"/static/exports/{filename}"}
    except Exception as e:
        return {"success": False, "error": str(e)}


@app.post("/api/export-notes-pdf")
async def export_notes_pdf(request: Request):
    """导出学习笔记为PDF格式"""
    try:
        data = await request.json()
        content = data.get("content", "")

        if not content or content.strip() == '':
            return {"success": False, "error": "笔记内容为空"}

        html_content = f"""
        <html><head><meta charset='utf-8'><title>学习笔记 (PDF打印版)</title>
        <style>
            body {{ font-family: 'PingFang SC', sans-serif; line-height: 1.6; max-width: 800px; margin: 0 auto; padding: 40px; color: #333; }}
            h1 {{ text-align: center; color: #3B82F6; border-bottom: 2px solid #DBEAFE; padding-bottom: 20px; }}
            .note-content {{ line-height: 1.8; }}
            img {{ max-width: 100%; border-radius: 8px; margin: 10px 0; }}
            .footer {{ text-align: right; color: #666; margin-top: 40px; padding-top: 20px; border-top: 1px solid #E2E8F0; }}
            @media print {{ body {{ padding: 0; }} }}
        </style>
        </head><body onload="setTimeout(()=>window.print(), 500)">
        <h1>📝 卷积核学习笔记</h1>
        <div class="note-content">
        {content}
        </div>
        <div class="footer">
            导出时间：{time.strftime('%Y-%m-%d %H:%M:%S')}
        </div>
        </body></html>
        """

        filename = f"StudyNotes_Print_{int(time.time())}.html"
        filepath = exports_dir / filename
        with open(filepath, "w", encoding="utf-8") as f:
            f.write(html_content)

        return {"success": True, "url": f"/static/exports/{filename}"}
    except Exception as e:
        return {"success": False, "error": str(e)}


# ============ 综合选择导出接口 ============
@app.post("/api/export-combined")
async def export_combined(request: Request):
    """综合导出：支持截图笔记 + AI对话的任意组合，输出 Word 或 PDF"""
    try:
        data = await request.json()
        fmt = data.get("format", "word")          # "word" | "pdf"
        sections = data.get("sections", [])       # 有序 section 列表

        # ---- 构造内容 HTML ----
        body_parts = []
        for sec in sections:
            sec_type = sec.get("type", "")

            if sec_type == "screenshot_notes":
                notes = sec.get("items", [])
                if not notes:
                    continue
                body_parts.append("<h2 style='color:#1e40af;border-bottom:2px solid #93c5fd;padding-bottom:8px;'>📷 视频截图笔记</h2>")
                for i, note in enumerate(notes):
                    body_parts.append(f"<div style='margin-bottom:28px;padding:16px;border:1px solid #cbd5e1;border-radius:10px;'>")
                    body_parts.append(f"<h3 style='margin:0 0 10px 0;color:#334155;'>截图 {i+1}</h3>")
                    img_path = BASE_DIR / note['image_url'].lstrip('/')
                    if img_path.exists():
                        with open(img_path, "rb") as f:
                            b64 = base64.b64encode(f.read()).decode('utf-8')
                        body_parts.append(f"<img src='data:image/png;base64,{b64}' style='max-width:100%;border-radius:6px;margin-bottom:10px;'/>")
                    if note.get('user_note'):
                        body_parts.append(f"<div style='background:#f0f9ff;padding:10px;border-left:4px solid #3b82f6;margin-top:8px;border-radius:4px;'><b>📝 我的感悟：</b><br>{note['user_note']}</div>")
                    if note.get('ai_analysis'):
                        body_parts.append(f"<div style='background:#faf5ff;padding:10px;border-left:4px solid #a855f7;margin-top:8px;border-radius:4px;'><b>🤖 AI 分析：</b><br>{note['ai_analysis']}</div>")
                    body_parts.append("</div>")

            elif sec_type == "study_notes":
                content = sec.get("content", "")
                if not content or not content.strip():
                    continue
                body_parts.append("<h2 style='color:#15803d;border-bottom:2px solid #86efac;padding-bottom:8px;'>📝 课后学习笔记</h2>")
                body_parts.append(f"<div style='padding:16px;border:1px solid #bbf7d0;border-radius:10px;background:#f0fdf4;line-height:1.8;color:#1e293b;'>{content}</div>")

            elif sec_type == "ai_chat":
                messages = sec.get("messages", [])
                if not messages:
                    continue
                sec_label = sec.get("label", "AI 对话记录")
                body_parts.append(f"<h2 style='color:#6d28d9;border-bottom:2px solid #c4b5fd;padding-bottom:8px;'>🤖 {sec_label}</h2>")
                for msg in messages:
                    role = msg.get("role", "user")
                    content = msg.get("content", "")
                    if role == "user":
                        body_parts.append(f"<div style='margin:10px 0;padding:12px 16px;background:#eff6ff;border-radius:10px;border-left:4px solid #3b82f6;'><b>你：</b><br>{content}</div>")
                    else:
                        body_parts.append(f"<div style='margin:10px 0;padding:12px 16px;background:#faf5ff;border-radius:10px;border-left:4px solid #a855f7;'><b>AI助手：</b><br>{content}</div>")

            elif sec_type == "wrong_questions":
                questions = sec.get("items", [])
                if not questions:
                    continue
                body_parts.append("<h2 style='color:#991b1b;border-bottom:2px solid #fecdd3;padding-bottom:8px;'>📕 错题本</h2>")
                for i, q in enumerate(questions):
                    body_parts.append(f"<div style='margin-bottom:24px;padding:16px;background:#fff1f2;border:1px solid #fecdd3;border-radius:10px;'>")
                    body_parts.append(f"<h3>{i+1}. {q.get('title','')}</h3>")
                    body_parts.append(f"<p><b>题目：</b>{q.get('question','')}</p>")
                    for opt in q.get('options', []):
                        if opt == q.get('correctAnswer'):
                            body_parts.append(f"<p style='color:green;'>✅ {opt}（正确答案）</p>")
                        elif opt.startswith(q.get('userChoice', '__')):
                            body_parts.append(f"<p style='color:red;text-decoration:line-through;'>❌ {opt}（你的选择）</p>")
                        else:
                            body_parts.append(f"<p>{opt}</p>")
                    body_parts.append(f"<div style='background:#f5f3ff;padding:10px;border-radius:6px;margin-top:8px;'><b>🤖 AI解析：</b><br>{q.get('aiExplanation','')}</div>")
                    body_parts.append("</div>")

        if not body_parts:
            return {"success": False, "error": "没有可导出的内容"}

        body_html = "\n".join(body_parts)
        export_time = time.strftime('%Y-%m-%d %H:%M:%S')

        if fmt == "word":
            full_html = f"""<html xmlns:o='urn:schemas-microsoft-com:office:office'
                  xmlns:w='urn:schemas-microsoft-com:office:word'
                  xmlns='http://www.w3.org/TR/REC-html40'>
<head><meta charset='utf-8'>
<title>卷积核微课 - 导出文档</title>
<style>
  body {{ font-family: 'PingFang SC', '微软雅黑', sans-serif; line-height: 1.7; margin: 40px; color: #1e293b; }}
  h1 {{ text-align: center; color: #0f172a; }}
  img {{ max-width: 500px; }}
</style>
</head>
<body>
<h1>卷积核微课 - 学习资料导出</h1>
<p style='text-align:center;color:#64748b;'>导出时间：{export_time}</p>
<hr/>
{body_html}
</body></html>"""
            filename = f"Combined_Export_{int(time.time())}.doc"
        else:
            full_html = f"""<!DOCTYPE html>
<html><head><meta charset='utf-8'><title>卷积核微课 - PDF打印版</title>
<style>
  body {{ font-family: 'PingFang SC', sans-serif; line-height: 1.7; max-width: 820px; margin: 0 auto; padding: 40px; color: #1e293b; }}
  h1 {{ text-align: center; color: #0f172a; border-bottom: 2px solid #e2e8f0; padding-bottom: 16px; }}
  img {{ max-width: 100%; border-radius: 8px; }}
  @media print {{ body {{ padding: 0; }} * {{ -webkit-print-color-adjust: exact; }} }}
</style>
</head>
<body onload="setTimeout(()=>window.print(), 600)">
<h1>📚 卷积核微课 - 学习资料导出</h1>
<p style='text-align:center;color:#64748b;'>导出时间：{export_time}</p>
<hr/>
{body_html}
</body></html>"""
            filename = f"Combined_Export_{int(time.time())}.html"

        filepath = exports_dir / filename
        with open(filepath, "w", encoding="utf-8") as f:
            f.write(full_html)

        return {"success": True, "url": f"/static/exports/{filename}", "format": fmt}
    except Exception as e:
        return {"success": False, "error": str(e)}


# ============ 健康检查 ============
@app.get("/health")
async def health():
    """健康检查"""
    return {
        "status": "ok",
        "video_exists": VIDEO_FILE.exists(),
        "html_exists": HTML_FILE.exists(),
        "api_configured": bool(DASHSCOPE_API_KEY)
    }


# ============ Fabric.js编辑器 ============
@app.get("/fabric-editor", response_class=HTMLResponse)
async def fabric_editor():
    """Fabric.js独立编辑器页面"""
    FABRIC_FILE = Path(__file__).parent / "fabric_editor.html"
    if not FABRIC_FILE.exists():
        raise HTTPException(status_code=404, detail="Fabric编辑器文件不存在")
    
    with open(FABRIC_FILE, 'r', encoding='utf-8') as f:
        html_content = f.read()
    
    return html_content


# ============ 3D学习空间 ============
@app.get("/3d-space", response_class=HTMLResponse)
async def three_d_space():
    """3D学习空间页面"""
    THREE_FILE = Path(__file__).parent / "three-showcase.html"
    if not THREE_FILE.exists():
        raise HTTPException(status_code=404, detail="3D学习空间文件不存在")
    
    with open(THREE_FILE, 'r', encoding='utf-8') as f:
        html_content = f.read()
    
    return html_content


# ============ 数据看板 ============
@app.get("/three-showcase", response_class=HTMLResponse)
async def three_showcase_alias():
    """three-showcase.html compatible route"""
    return await three_d_space()


@app.get("/data-board", response_class=HTMLResponse)
async def data_board():
    """数据可视化看板页面"""
    ECHARTS_FILE = Path(__file__).parent / "echarts_analysis.html"
    if not ECHARTS_FILE.exists():
        raise HTTPException(status_code=404, detail="数据看板文件不存在")
    
    with open(ECHARTS_FILE, 'r', encoding='utf-8') as f:
        html_content = f.read()
    
    return html_content


# ============ 启动 ============
@app.get("/echarts-analysis", response_class=HTMLResponse)
async def echarts_analysis_alias():
    """echarts_analysis.html compatible route"""
    return await data_board()


if __name__ == "__main__":
    print("""
╔══════════════════════════════════════════════════════════════╗
║           卷积核微课 - 虚拟实验室 启动中...                     ║
╠══════════════════════════════════════════════════════════════╣
║  访问地址: http://localhost:9000                               ║
║                                                               ║
║  功能页面:                                                    ║
║    微课观影: http://localhost:9000/                        ║
║    课后学习: http://localhost:9000/study                    ║
║    AI助教:   http://localhost:9000/ai-tutor                ║
║    3D学习空间: http://localhost:9000/3d-space              ║
║    数据看板: http://localhost:9000/data-board               ║
║    Fabric编辑器: http://localhost:9000/fabric-editor        ║
╚══════════════════════════════════════════════════════════════╝
    """)

    uvicorn.run(app, host="0.0.0.0", port=9000)
