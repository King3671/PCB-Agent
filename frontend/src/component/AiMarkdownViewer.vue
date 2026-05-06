<template>
  <div class="ai-markdown-viewer">
    <div class="markdown-body" v-html="renderedContent"></div>
  </div>
</template>

<script setup>
import { computed, watch } from 'vue'
import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js'
import 'highlight.js/styles/github.css'

// 接收父组件传来的【流式实时更新】的 AI 输出文本
const props = defineProps({
  content: {
    type: String,
    default: '',
    required: true
  }
})

// 初始化 markdown-it（宽容解析，适配大模型不规范格式）
const md = new MarkdownIt({
  html: true,
  linkify: true,
  breaks: true,        // 关键：自动处理大模型换行
  typographer: true,
  highlight(str, lang) {
    if (lang && hljs.getLanguage(lang)) {
      try {
        return hljs.highlight(str, { language: lang }).value
      } catch (__) {}
    }
    return ''
  }
})

// 计算属性：每次 content 变化（流式更新），自动重新渲染
const renderedContent = computed(() => {
  let html = md.render(props.content)
  html = html.replace(/<img /g, '<img style="max-width:60%; border-radius:8px;" ')
  return html
})


// 每次渲染完，自动高亮代码（流式也能正常高亮）
watch(renderedContent, () => {
  setTimeout(() => hljs.highlightAll(), 0)
})
</script>

<style scoped>
/* ========== 重点美化优化：列表右移缩进 + 排版间距 ========== */
.markdown-body {
  line-height: 1.8;
  font-size: 15px;
  /* 气泡整体左右内边距，正文不再贴边框 */
  padding: 16px 20px;
  color: #1f2937;
}

/* 段落正文间距 */
.markdown-body p {
  margin: 0.8em 0;
}

/* 一级二级标题样式优化 */
.markdown-body h1 {
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 1.2em 0;
  color: #111827;
}
.markdown-body h2 {
  font-size: 22px;
  font-weight: 600;
  margin: 1.4em 0 0.8em 0;
  color: #1f2937;
}

/* ========== 核心优化：所有圆点列表整体右移缩进 ========== */
.markdown-body ul {
  /* 列表整体左缩进，实现右移效果 */
  padding-left: 28px;
  margin: 0.6em 0 0.6em 12px;
}
.markdown-body li {
  margin: 0.5em 0;
  line-height: 1.8;
}

/* 二级子列表（你截图里空心圆圈项）再额外右移，层级分明 */
.markdown-body ul > li > ul {
  padding-left: 24px;
  margin-left: 8px;
  margin-top: 4px;
  margin-bottom: 4px;
}

.markdown-body strong {
  font-weight: 600;
}
.markdown-body pre {
  background: #f6f8fa;
  padding: 12px;
  border-radius: 6px;
  overflow-x: auto;
  margin: 0.8em 0;
}
.markdown-body code {
  background: #f6f8fa;
  padding: 2px 4px;
  border-radius: 3px;
  font-size: 0.9em;
}
</style>