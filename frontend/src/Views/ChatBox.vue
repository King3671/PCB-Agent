<template>
  <div class="doubao-layout">
    <aside class="side-bar">
      <div class="user-info">
        <div class="avatar-small">PCB</div>
        <span class="user-name">原理图分析助手</span>
      </div>

      <button class="new-chat-btn" @click="createNewChat">
        <span class="plus-icon">+</span> 新对话
      </button>

      <div class="history-label">历史对话</div>
      <div class="session-list">
        <div v-for="s in sessionList" :key="s.id"
             :class="['session-item', { active: currentId === s.id }]"
             @click="loadChat(s.id)">
          <span class="chat-icon">💬</span>
          <span class="chat-title">{{ s.title || '未命名会话' }}</span>
        </div>
      </div>
    </aside>

    <main class="main-container">
      <header class="top-nav">
        <div class="nav-left">{{ currentTitle }}</div>
      </header>

      <div class="chat-scroller" ref="scrollRef">
        <div class="chat-inner-width">
          <div v-for="(msg, i) in msgList" :key="i" :class="['msg-wrapper', msg.type === 'user' ? 'user' : 'assistant']">
            <div class="avatar" v-if="msg.type === 'assistant'">AI</div>

            <div class="message-bubble">
              <template v-if="msg.media_data && msg.media_data.length > 0">
                <img v-for="(img, idx) in msg.media_data"
                     :key="idx"
                     :src="img"
                     class="chat-img-content" />
              </template>

              <!-- 👇 这里是关键修改：普通文本 → 流式 Markdown 渲染 -->
              <div v-if="msg.content" class="content-text">
                <AiMarkdownViewer v-if="msg.type === 'assistant'" :content="msg.content" />
                <span v-else>{{ msg.content }}</span>
              </div>
            </div>

            <div class="avatar" v-if="msg.type === 'user'">U</div>
          </div>

          <div v-if="msgList.length === 0" class="welcome-guide">
            <h1>我是PCB原理图分析助手，请上传图片或发送消息。</h1>
          </div>
        </div>
      </div>

      <footer class="input-section">
        <div class="input-area-wrapper">
          <div v-if="previewImage" class="preview-container">
            <div class="preview-card">
              <img :src="previewImage" alt="preview" />
              <div class="remove-btn" @click="clearPreview">×</div>
              <div class="preview-tag">{{ fileName }}</div>
            </div>
          </div>

          <div class="input-pill-container" :class="{ 'has-preview': previewImage }">
            <div class="tool-icons">
              <el-upload
                  action="#"
                  :auto-upload="false"
                  :on-change="onFileChange"
                  :show-file-list="false"
                  accept="image/*"
              >
                <span class="paperclip">🧷</span>
              </el-upload>
            </div>
            <input
                v-model="inputText"
                placeholder="发送消息或分析PCB图..."
                @keyup.enter="handleSend"
            />
            <button class="send-btn" :disabled="!inputText.trim() && !previewImage" @click="handleSend">
              <el-icon><Top /></el-icon>
            </button>
          </div>
        </div>
      </footer>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { Top } from '@element-plus/icons-vue'
import { getSessions, getHistory, chatStream, uploadFile } from '../api/chat'

// 👇 导入流式 Markdown 组件
import AiMarkdownViewer from '../component/AiMarkdownViewer.vue'

// --- 状态定义 ---
const sessionList = ref([])
const currentId = ref('')
const currentTitle = ref('新对话')
const msgList = ref([])
const inputText = ref('')
const scrollRef = ref(null)

const previewImage = ref(null)
const selectedFile = ref(null)
const fileName = ref('')

// --- 生命周期 ---
onMounted(async () => {
  await fetchList()
  if (sessionList.value.length > 0) loadChat(sessionList.value[0].id)
})

// --- 业务逻辑 ---
const fetchList = async () => {
  const res = await getSessions()
  sessionList.value = res.data
}

const loadChat = async (id) => {
  currentId.value = id
  const target = sessionList.value.find(s => s.id === id)
  currentTitle.value = target ? (target.title || '对话记录') : '对话记录'

  const res = await getHistory(id)
  msgList.value = res.data.map(m => ({
    type: m.type === 'ai' ? 'assistant' : m.type,
    content: m.content,
    media_data: m.media_data || []
  }))
  nextTick(scrollToBottom)
}

const onFileChange = (file) => {
  if (!file.raw) return
  selectedFile.value = file.raw
  fileName.value = file.name
  previewImage.value = URL.createObjectURL(file.raw)
}

const clearPreview = () => {
  previewImage.value = null
  selectedFile.value = null
  fileName.value = ''
}

const createNewChat = () => {
  currentId.value = 'sid_' + Date.now()
  currentTitle.value = '新对话'
  msgList.value = []
}

const handleSend = async () => {
  if (!inputText.value.trim() && !selectedFile.value) return;

  const question = inputText.value;
  const localImgUrl = previewImage.value;
  const fileToUpload = selectedFile.value;
  const hasImage = !!fileToUpload;

  msgList.value.push({
    type: 'user',
    content: question,
    media_data: localImgUrl ? [localImgUrl] : []
  });
  const userMsgIdx = msgList.value.length - 1;

  inputText.value = '';
  clearPreview();

  msgList.value.push({ type: 'assistant', content: '', media_data: [] });
  const aiLastIdx = msgList.value.length - 1;
  await nextTick(scrollToBottom);

  try {
    if (hasImage) {
      const formData = new FormData();
      formData.append('file', fileToUpload);
      formData.append('sessionId', currentId.value);
      const uploadRes = await uploadFile(formData);
      if (uploadRes.data && uploadRes.data.url) {
        msgList.value[userMsgIdx].media_data = [uploadRes.data.url];
      }
    }

    const response = await chatStream(question, currentId.value, hasImage);
    const reader = response.body.getReader();
    const decoder = new TextDecoder();
    let leftover = '';

    while (true) {
      const { done, value } = await reader.read();
      if (done) break;
      const chunk = leftover + decoder.decode(value, { stream: true });
      const lines = chunk.split('\n');
      leftover = lines.pop();
      for (const line of lines) {
        const trimmed = line.trim();
        if (trimmed.startsWith('data:')) {
          let content = trimmed.slice(5).trim();
          if (content === '[DONE]') continue;
          msgList.value[aiLastIdx].content += content;
        }
      }
      await nextTick(scrollToBottom);
    }
  } catch (e) {
    console.error(e);
    msgList.value[aiLastIdx].content = "⚠️ 服务异常，请重试。";
  } finally {
    fetchList();
  }
}

const scrollToBottom = () => {
  if (scrollRef.value) {
    scrollRef.value.scrollTop = scrollRef.value.scrollHeight
  }
}
</script>

<style scoped>
.doubao-layout { display: flex; width: 100vw; height: 100vh; background: #fff; color: #1f2937; }
.side-bar { width: 260px; background: #f9fafb; border-right: 1px solid #e5e7eb; display: flex; flex-direction: column; padding: 16px; }

.user-info { display: flex; align-items: center; gap: 12px; margin-bottom: 24px; flex-shrink: 0; white-space: nowrap; }
.avatar-small { width: 32px; height: 32px; background: #3b82f6; border-radius: 50%; color: white; display: flex; justify-content: center; align-items: center; font-weight: bold; flex-shrink: 0; }
.user-name { font-weight: 600; font-size: 16px; overflow: hidden; text-overflow: ellipsis; }

.new-chat-btn { background: #fff; border: 1px solid #e5e7eb; border-radius: 12px; padding: 10px; cursor: pointer; display: flex; align-items: center; justify-content: center; gap: 8px; margin-bottom: 20px; }
.session-item { padding: 10px; border-radius: 8px; cursor: pointer; font-size: 14px; margin-bottom: 4px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.session-item.active { background: #eff6ff; color: #3b82f6; }

.main-container { flex: 1; display: flex; flex-direction: column; overflow: hidden; }
.top-nav { height: 56px; border-bottom: 1px solid #f3f4f6; display: flex; align-items: center; padding: 0 24px; font-weight: 600; }

.chat-scroller { flex: 1; overflow-y: auto; }
.chat-inner-width { max-width: 800px; margin: 0 auto; padding: 20px; }

.msg-wrapper { display: flex; gap: 12px; margin-bottom: 24px; }
.msg-wrapper.user { justify-content: flex-end; }
.avatar { width: 32px; height: 32px; border-radius: 50%; display: flex; justify-content: center; align-items: center; flex-shrink: 0; }
.user .avatar { background: #3b82f6; color: white; order: 2; }
.assistant .avatar { background: #e5e7eb; color: #4b5563; font-size: 12px; }

.message-bubble { max-width: 80%; padding: 12px 16px; line-height: 1.6; }
.assistant .message-bubble { background: #f3f4f6; border-radius: 4px 16px 16px 16px; color: #1f2937; }
.user .message-bubble { background: #3b82f6; color: white; border-radius: 16px 4px 16px 16px; order: 1; }
.chat-img-content { max-width: 300px; border-radius: 8px; margin-bottom: 8px; display: block; border: 1px solid #eee; }

.input-section { padding: 10px 20px 40px; }
.input-area-wrapper { max-width: 800px; margin: 0 auto; }

.preview-container { background: #fff; border: 1px solid #e5e7eb; border-bottom: none; border-radius: 16px 16px 0 0; padding: 12px; }
.preview-card { position: relative; width: 60px; height: 60px; }
.preview-card img { width: 100%; height: 100%; object-fit: cover; border-radius: 8px; }
.remove-btn { position: absolute; top: -8px; right: -8px; width: 20px; height: 20px; background: rgba(0,0,0,0.5); color: white; border-radius: 50%; display: flex; justify-content: center; align-items: center; cursor: pointer; }
.preview-tag { position: absolute; left: 70px; top: 20px; font-size: 12px; color: #666; background: #f3f4f6; padding: 4px 10px; border-radius: 10px; white-space: nowrap; max-width: 200px; overflow: hidden; text-overflow: ellipsis; }

.input-pill-container { background: #fff; border: 1px solid #e5e7eb; border-radius: 24px; padding: 8px 16px; display: flex; align-items: center; gap: 12px; }
.input-pill-container.has-preview { border-top-left-radius: 0; border-top-right-radius: 0; border-top: 1px dashed #eee; }
.input-pill-container input { flex: 1; border: none; outline: none; font-size: 16px; }
.send-btn { width: 32px; height: 32px; border-radius: 50%; background: #3b82f6; color: white; border: none; cursor: pointer; display: flex; align-items: center; justify-content: center; }
.paperclip { cursor: pointer; color: #6b7280; font-size: 20px; }
</style>