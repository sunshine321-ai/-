<script setup lang="ts">
// 课后学习页 - 6100行复杂页面，暂时通过iframe加载
// 后端 /study 已处理导出弹窗注入 + localStorage劫持
// TODO: 未来拆解为Vue组件（笔记、错题本、AI对话、互动练习）
</script>

<template>
  <iframe
    src="/study-raw"
    style="width:100%;height:100vh;border:none;display:block;"
    title="课后学习"
  />
</template>
