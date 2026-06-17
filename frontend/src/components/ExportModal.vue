<script setup lang="ts">
import { useExportStore } from '@/stores/export'

const store = useExportStore()
</script>

<template>
  <div
    v-if="store.isOpen"
    class="modal-overlay"
    @click.self="store.close()"
  >
    <div class="modal-panel">
      <!-- Header -->
      <div class="modal-header">
        <div class="header-left">
          <div class="header-icon">
            <i class="fas fa-file-export"></i>
          </div>
          <div>
            <div class="header-title">选择导出内容</div>
            <div class="header-sub">可跨页面自由组合任意模块</div>
          </div>
        </div>
        <button class="close-btn" @click="store.close()">
          <i class="fas fa-times"></i>
        </button>
      </div>

      <!-- Module List -->
      <div class="module-list">
        <template v-for="(mods, page) in store.groupedModules" :key="page">
          <div class="section-label">
            <i class="fas fa-layer-group"></i>{{ page }}
          </div>
          <div
            v-for="mod in mods"
            :key="mod.id"
            class="mod-card"
            :class="{ selected: store.checkedIds.has(mod.id), 'no-data': !mod.hasData }"
            @click="store.toggle(mod.id)"
          >
            <div class="mod-row">
              <div class="checkbox" :class="{ checked: store.checkedIds.has(mod.id) }">
                {{ store.checkedIds.has(mod.id) ? '✓' : '' }}
              </div>
              <div class="mod-icon" :style="{ background: mod.bg, color: mod.color }">
                <i :class="'fas ' + mod.icon"></i>
              </div>
              <div class="mod-info">
                <div class="mod-label">{{ mod.label }}</div>
                <div class="mod-count" :style="{ color: mod.hasData ? mod.color : '#92400E' }">
                  {{ mod.hasData ? (Array.isArray(mod.count) ? mod.count + ' 条' : '有内容') : '暂无数据' }}
                </div>
              </div>
              <div v-if="!mod.hasData" class="no-data-badge">未记录</div>
            </div>
          </div>
        </template>
      </div>

      <!-- Format Selection -->
      <div class="format-section">
        <p class="format-title">📄 导出格式</p>
        <div class="format-row">
          <div
            class="format-card"
            :class="{ active: store.format === 'word' }"
            @click="store.selectFormat('word')"
          >
            <i class="fas fa-file-word" style="color:#2b7cd3;font-size:1.6rem;display:block;margin-bottom:7px;"></i>
            <div style="font-weight:600;font-size:0.88rem;">Word 文档</div>
            <div style="color:#92400E;font-size:0.73rem;margin-top:3px;">.doc，可直接编辑</div>
          </div>
          <div
            class="format-card"
            :class="{ active: store.format === 'pdf' }"
            @click="store.selectFormat('pdf')"
          >
            <i class="fas fa-file-pdf" style="color:#c9302c;font-size:1.6rem;display:block;margin-bottom:7px;"></i>
            <div style="font-weight:600;font-size:0.88rem;">PDF 文件</div>
            <div style="color:#92400E;font-size:0.73rem;margin-top:3px;">浏览器打印另存</div>
          </div>
        </div>
      </div>

      <!-- Actions -->
      <div class="modal-actions">
        <button class="btn-cancel" @click="store.close()">取消</button>
        <button
          class="btn-export"
          :disabled="store.isExporting"
          @click="store.doExport()"
        >
          <template v-if="store.isExporting">
            <i class="fas fa-spinner fa-spin"></i> 生成中...
          </template>
          <template v-else>
            <i class="fas fa-download"></i> 开始导出
          </template>
        </button>
      </div>
      <p class="msg" :style="{ color: store.message.includes('✅') ? '#4ade80' : store.message.includes('❌') ? '#f87171' : '#92400E' }">
        {{ store.message }}
      </p>
    </div>
  </div>
</template>

<style scoped>
.modal-overlay {
  position: fixed;
  inset: 0;
  z-index: 99999;
  background: rgba(31, 24, 16, 0.55);
  backdrop-filter: blur(6px);
}
.modal-panel {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: #FFF8EF;
  border: 1.5px solid rgba(196, 130, 60, 0.22);
  border-radius: 20px;
  width: min(580px, 94vw);
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 32px 80px rgba(120, 60, 10, 0.18);
  color: #1F2937;
  font-family: 'Nunito', 'PingFang SC', sans-serif;
}
.modal-header {
  padding: 22px 28px 16px;
  border-bottom: 1.5px solid rgba(196, 130, 60, 0.15);
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: sticky;
  top: 0;
  background: #FFF8EF;
  border-radius: 20px 20px 0 0;
  z-index: 1;
}
.header-left { display: flex; align-items: center; gap: 12px; }
.header-icon {
  width: 38px; height: 38px;
  background: linear-gradient(135deg, #C2410C, #D97706);
  border-radius: 10px;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0; color: white;
}
.header-title { font-size: 1.1rem; font-weight: 700; }
.header-sub { font-size: 0.77rem; color: #92400E; margin-top: 1px; }
.close-btn {
  background: rgba(194, 65, 12, 0.08);
  border: none; color: #92400E; cursor: pointer;
  width: 32px; height: 32px; border-radius: 8px; font-size: 1rem;
}
.module-list { padding: 16px 28px 4px; }
.section-label {
  font-size: .73rem; color: #92400E; text-transform: uppercase;
  letter-spacing: .6px; font-weight: 600; margin: 14px 0 8px;
  display: flex; align-items: center; gap: 6px;
}
.mod-card {
  background: rgba(194, 65, 12, 0.04);
  border: 1.5px solid rgba(196, 130, 60, 0.18);
  border-radius: 12px; padding: 12px 14px; margin-bottom: 8px;
  cursor: pointer; transition: .2s; user-select: none;
}
.mod-card.no-data { opacity: .4; cursor: default; }
.mod-card.has-data:hover { background: rgba(194, 65, 12, 0.09); }
.mod-card.selected {
  border-color: rgba(194, 65, 12, 0.5);
  background: rgba(194, 65, 12, 0.07);
}
.mod-row { display: flex; align-items: center; gap: 10px; }
.checkbox {
  width: 18px; height: 18px; border-radius: 5px;
  border: 2px solid rgba(196, 130, 60, 0.35);
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0; transition: .15s; font-size: .72rem;
}
.checkbox.checked { background: #C2410C; border-color: #C2410C; color: #fff; font-weight: 800; }
.mod-icon {
  width: 32px; height: 32px; border-radius: 8px;
  display: flex; align-items: center; justify-content: center;
  font-size: .88rem; flex-shrink: 0;
}
.mod-info { flex: 1; }
.mod-label { font-size: .88rem; font-weight: 600; }
.mod-count { font-size: .75rem; margin-top: 2px; }
.no-data-badge {
  font-size: .7rem; color: #92400E;
  background: rgba(196, 130, 60, .1);
  padding: 3px 8px; border-radius: 20px;
}
.format-section { padding: 0 28px 16px; }
.format-title {
  color: #92400E; font-size: .78rem; font-weight: 600;
  text-transform: uppercase; letter-spacing: .6px; margin-bottom: 12px;
}
.format-row { display: flex; gap: 12px; }
.format-card {
  flex: 1; border: 2px solid rgba(196, 130, 60, 0.25);
  background: rgba(196, 130, 60, 0.04); border-radius: 12px;
  padding: 14px; text-align: center; cursor: pointer; transition: .25s;
}
.format-card.active { border-color: #C2410C; background: rgba(194, 65, 12, 0.07); }
.modal-actions { padding: 0 28px 20px; display: flex; gap: 12px; }
.btn-cancel {
  flex: 1; padding: 13px; border: 1.5px solid rgba(196, 130, 60, 0.3);
  background: rgba(194, 65, 12, 0.04); border-radius: 12px;
  color: #92400E; cursor: pointer; font-size: .95rem;
}
.btn-export {
  flex: 2.5; padding: 13px; border: none;
  background: linear-gradient(135deg, #C2410C, #D97706);
  border-radius: 12px; color: white; cursor: pointer;
  font-size: .95rem; font-weight: 600;
  box-shadow: 0 4px 14px rgba(194, 65, 12, 0.3);
}
.btn-export:disabled { opacity: 0.6; cursor: not-allowed; }
.msg {
  text-align: center; font-size: .85rem;
  padding: 0 28px 20px; min-height: 20px;
}
</style>
