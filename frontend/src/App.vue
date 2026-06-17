<script setup lang="ts">
import ExportFAB from '@/components/ExportFAB.vue'
import ExportModal from '@/components/ExportModal.vue'
</script>

<template>
  <router-view />
  <ExportFAB />
  <ExportModal />
</template>
