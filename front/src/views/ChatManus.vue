<template>
  <div class="chat-page">
    <header class="chat-header chat-header-common"><span class="chat-header-avatar">🧠</span>Manus</header>
    <div class="chat-history chat-history-common" ref="historyEl">
      <div v-for="m in messages" :key="m.id" class="chat-msg" :class="m.role">
        <div class="chat-avatar" v-if="m.role === 'assistant'">🧠</div>
        <div class="chat-avatar" v-else>🧑</div>
        <div class="chat-bubble" v-html="m.html"></div>
      </div>
    </div>
    <form class="chat-input chat-input-common" @submit.prevent="onSend">
      <input v-model="input" placeholder="向智能体提问..." />
      <button type="submit">发送</button>
    </form>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, nextTick } from 'vue'
import { useSSE } from '@/utils/sse'
import { escapeToHtml } from '@/utils/text'

type ChatMessage = { id: string; role: 'user' | 'assistant'; html: string }

const messages = ref<ChatMessage[]>([])
const input = ref('')
const historyEl = ref<HTMLDivElement | null>(null)

watch(messages, async () => {
  await nextTick()
  if (historyEl.value) historyEl.value.scrollTop = historyEl.value.scrollHeight
}, { deep: true })

async function onSend() {
  const content = input.value.trim()
  if (!content) return
  messages.value.push({ id: crypto.randomUUID(), role: 'user', html: escapeToHtml(content) })
  input.value = ''

  const url = new URL('/api/hyper_project/manus/chat', window.location.origin)
  url.searchParams.set('message', content)

  const close = useSSE(url.toString(), (chunk: string) => {
    if (!chunk || chunk === '[DONE]' || chunk === 'DONE') return
    messages.value.push({ id: crypto.randomUUID(), role: 'assistant', html: escapeToHtml(chunk) })
  }, (_err: unknown) => {
    // Ignore generic EventSource errors to avoid noisy UI
  })

  // optional cleanup after 2 minutes
  setTimeout(close, 120000)
}
</script>

<style scoped>
.chat-page {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.chat-header {
  padding: 12px 16px;
  border-bottom: 1px solid #ffd6e9;
  background: #fff6fb;
  color: #6a1b9a;
  font-weight: 700;
}

.chat-history {
  flex: 1;
  overflow: auto;
  padding: 16px;
  background: linear-gradient(180deg, #fff, #f6f0ff);
}

.msg {
  display: flex;
  align-items: flex-end;
  margin-bottom: 12px;
  gap: 8px;
}

.msg.assistant {
  flex-direction: row;
}

.msg.user {
  flex-direction: row-reverse;
}

.avatar {
  width: 32px;
  height: 32px;
  display: grid;
  place-items: center;
  background: #fff;
  border: 1px solid #d8c8ff;
  border-radius: 50%;
}

.bubble {
  max-width: 70%;
  padding: 10px 12px;
  border-radius: 14px;
  border: 1px solid #d8c8ff;
  background: #fff;
  white-space: pre-wrap;
  word-break: break-word;
}

.msg.user .bubble {
  background: #eee6ff;
}

.chat-input {
  display: flex;
  gap: 8px;
  padding: 12px;
  border-top: 1px solid #d8c8ff;
}

.chat-input input {
  flex: 1;
  padding: 10px 12px;
  border: 1px solid #d8c8ff;
  border-radius: 10px;
}

.chat-input button {
  background: #6a1b9a;
  color: #fff;
  border: none;
  padding: 10px 14px;
  border-radius: 10px;
  cursor: pointer;
}

.err {
  color: #d00;
}
</style>
