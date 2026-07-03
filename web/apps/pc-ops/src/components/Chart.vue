<template>
  <div ref="el" :style="{ height, width: '100%' }"></div>
</template>

<script setup lang="ts">
import { onMounted, onBeforeUnmount, ref, watch } from 'vue'
import * as echarts from 'echarts'

const props = defineProps<{ option: any; height?: string }>()
const el = ref<HTMLElement>()
let chart: echarts.ECharts | null = null

function render() { if (!el.value) return; if (!chart) chart = echarts.init(el.value); chart.setOption(props.option, true) }
function resize() { chart?.resize() }
onMounted(() => { render(); window.addEventListener('resize', resize) })
onBeforeUnmount(() => { window.removeEventListener('resize', resize); chart?.dispose() })
watch(() => props.option, render, { deep: true })
</script>
