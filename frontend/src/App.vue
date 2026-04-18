<template>
  <div class="app-container">
    <ChatBox />
  </div>
</template>

<script setup>
import ChatBox from './Views/ChatBox.vue'
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}
body {
  background-color: #f5f7fa;
}
.app-container {
  width: 100vw;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
}
</style>