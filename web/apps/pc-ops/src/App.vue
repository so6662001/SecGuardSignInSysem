<template><router-view /></template>
<script setup lang="ts"></script>
<style>
* { box-sizing: border-box; }
html, body, #app { height: 100%; margin: 0; }
body { font-family: -apple-system, "PingFang SC", "Microsoft YaHei", sans-serif; background: #f4f6fb; }
.stat-card { border-radius: 14px; padding: 18px 20px; background: #fff; border: 1px solid #eceff5; }
.stat-card .num { font-size: 28px; font-weight: 800; }
.stat-card .lbl { color: #5a6478; font-size: 13px; margin-top: 4px; }
</style>
