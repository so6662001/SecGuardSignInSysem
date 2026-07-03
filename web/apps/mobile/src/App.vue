<template><router-view /></template>
<script setup lang="ts"></script>
<style>
html, body, #app { margin: 0; height: 100%; background: #f4f6fb; }
body { font-family: -apple-system, "PingFang SC", sans-serif; }
.page { min-height: 100vh; }
.appbar { background: linear-gradient(135deg,#2f6bed,#4f82ff); color: #fff; padding: 16px; }
.appbar .t { font-size: 18px; font-weight: 800; }
.appbar .s { font-size: 12px; opacity: .85; margin-top: 2px; }
.pad { padding: 12px; }
</style>
