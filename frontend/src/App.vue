<template>
  <div class="app">
    <AppSidebar :memory-id="memoryId" />

    <main class="main">
      <header class="main-header">
        <div>
          <p class="main-header__eyebrow">WORKSPACE</p>
          <h1 class="main-header__title">编程学习与求职问答</h1>
        </div>
        <div class="main-header__meta">
          <span class="status-dot" :class="{ 'status-dot--busy': isStreaming }"></span>
          <span>{{ isStreaming ? '正在生成' : '就绪' }}</span>
        </div>
      </header>

      <section ref="messageListRef" class="thread" aria-live="polite">
        <div class="thread__inner">
          <section v-if="messages.length === 0" class="intro">
            <p class="intro__kicker">AI 编程小助手</p>
            <h1>把编程学习和求职，拆成可以执行的下一步。</h1>
            <p class="intro__lede">
              学习路线、项目建议、简历优化、面试准备——直接问，我会结合知识库检索和联网搜索给出可落地的回答。
            </p>
            <ol class="capability-list">
              <li>
                <span class="capability-list__num">01</span>
                <div>
                  <h2>学习路线</h2>
                  <p>从零基础到进阶，拆出阶段目标和该学的东西。</p>
                </div>
              </li>
              <li>
                <span class="capability-list__num">02</span>
                <div>
                  <h2>项目建议</h2>
                  <p>怎么选项目、怎么改、面试时怎么讲。</p>
                </div>
              </li>
              <li>
                <span class="capability-list__num">03</span>
                <div>
                  <h2>求职准备</h2>
                  <p>简历优化、投递节奏、面试复盘。</p>
                </div>
              </li>
              <li>
                <span class="capability-list__num">04</span>
                <div>
                  <h2>面试题库</h2>
                  <p>按技术方向检索高频题，并给出答题思路。</p>
                </div>
              </li>
            </ol>
          </section>

          <ChatMessage
            v-for="item in messages"
            :key="item.id"
            :role="item.role"
            :content="item.content"
            :streaming="item.streaming"
          />
        </div>
      </section>

      <ChatComposer v-model="inputMessage" :busy="isSending" @send="sendMessage" />
    </main>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import AppSidebar from './components/AppSidebar.vue'
import ChatMessage from './components/ChatMessage.vue'
import ChatComposer from './components/ChatComposer.vue'
import { createChatStreamUrl } from './api/chat'

// 当前会话 ID，后端按它区分不同用户的对话记忆
const memoryId = ref(generateMemoryId())
const inputMessage = ref('')
const messages = ref([])
const isSending = ref(false)
const messageListRef = ref(null)

// 超过这个时间没有收到任何内容就判定为超时
const STREAM_TIMEOUT_MS = 120000

let eventSource = null
let streamTimeout = null

// 是否有助手消息还在流式输出
const isStreaming = computed(() =>
  messages.value.some((item) => item.role === 'assistant' && item.streaming),
)

// 生成或复用会话 ID：存在 sessionStorage 中，同一标签页刷新后仍是同一个会话
function generateMemoryId() {
  const storageKey = 'ai-code-helper-memory-id'

  if (typeof sessionStorage !== 'undefined') {
    const cachedId = sessionStorage.getItem(storageKey)
    if (cachedId && /^\d+$/.test(cachedId)) {
      return cachedId
    }
  }

  const generatedId = String(Math.floor(Date.now() % 2147483647) || Math.floor(Math.random() * 2147483647) + 1)

  if (typeof sessionStorage !== 'undefined') {
    sessionStorage.setItem(storageKey, generatedId)
  }

  return generatedId
}

function createMessage(role, content = '', streaming = false) {
  return reactive({
    id: `${role}-${Date.now()}-${Math.random().toString(16).slice(2)}`,
    role,
    content,
    streaming,
  })
}

async function scrollToBottom() {
  await nextTick()

  const list = messageListRef.value
  if (!list) {
    return
  }

  list.scrollTop = list.scrollHeight
}

function clearStreamTimeout() {
  if (streamTimeout) {
    clearTimeout(streamTimeout)
    streamTimeout = null
  }
}

function closeStream() {
  clearStreamTimeout()

  if (eventSource) {
    eventSource.close()
    eventSource = null
  }

  isSending.value = false
}

// 每收到一段内容就重新计时，长时间没有输出则结束本次生成
function armStreamTimeout(assistantMessage) {
  clearStreamTimeout()

  streamTimeout = setTimeout(async () => {
    assistantMessage.streaming = false
    if (!assistantMessage.content) {
      assistantMessage.content = '生成超时，请重试。'
    }

    closeStream()
    await scrollToBottom()
  }, STREAM_TIMEOUT_MS)
}

function sendMessage() {
  const content = inputMessage.value.trim()

  if (!content || isSending.value) {
    return
  }

  const userMessage = createMessage('user', content)
  const assistantMessage = createMessage('assistant', '', true)

  messages.value.push(userMessage, assistantMessage)
  inputMessage.value = ''
  isSending.value = true

  // 拼接后端 SSE 地址，用浏览器内置的 EventSource 建立流式连接
  const url = createChatStreamUrl(memoryId.value, content)
  eventSource = new EventSource(url)
  armStreamTimeout(assistantMessage)

  // 每收到一段文字就追加到当前 AI 气泡，形成逐字输出的效果
  eventSource.onmessage = async (event) => {
    const chunk = event.data || ''
    assistantMessage.content += chunk
    armStreamTimeout(assistantMessage)
    await scrollToBottom()
  }

  // 连接结束或异常时收尾：已收到内容就保留，完全没收到则提示重试
  eventSource.onerror = async () => {
    assistantMessage.streaming = false
    if (!assistantMessage.content) {
      assistantMessage.content = '连接已中断，请重试。'
    }

    closeStream()
    await scrollToBottom()
  }
}

onMounted(() => {
  scrollToBottom()
})

onBeforeUnmount(() => {
  clearStreamTimeout()

  if (eventSource) {
    eventSource.close()
    eventSource = null
  }
})
</script>
