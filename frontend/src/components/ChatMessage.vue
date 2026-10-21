<template>
  <article class="msg" :class="role === 'user' ? 'msg--user' : 'msg--assistant'">
    <div v-if="role === 'user'" class="msg__user-bubble">{{ content }}</div>

    <template v-else>
      <div class="msg__label">助手 · AI 编程小助手</div>
      <div v-if="streaming && !content" class="typing" aria-label="正在生成">
        <span></span><span></span><span></span>
      </div>
      <div v-else class="msg__body md" v-html="rendered"></div>
    </template>
  </article>
</template>

<script setup>
import { computed } from 'vue'
import { renderMarkdown } from '../utils/markdown'

const props = defineProps({
  role: {
    type: String,
    required: true,
  },
  content: {
    type: String,
    default: '',
  },
  streaming: {
    type: Boolean,
    default: false,
  },
})

const rendered = computed(() => renderMarkdown(props.content))
</script>
