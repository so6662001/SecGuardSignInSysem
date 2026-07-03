<template>
  <div class="fv-stat">
    <div v-if="trend" class="trend" :class="trendType">{{ trend }}</div>
    <div class="ico" :class="color"><el-icon><component :is="icon" /></el-icon></div>
    <div class="num" :style="{ color: numColor }">{{ value }}</div>
    <div class="lbl">{{ label }}</div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  value: string | number
  label: string
  icon: any
  color?: 'blue' | 'green' | 'orange' | 'purple'
  numColor?: string
  trend?: string
  trendType?: 'up' | 'down'
}>()
</script>
