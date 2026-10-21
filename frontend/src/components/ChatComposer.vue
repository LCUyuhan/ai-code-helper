<template>
  <footer class="composer">
    <div class="composer__inner">
      <form class="composer__box" @submit.prevent="submit">
        <textarea
          class="composer__input"
          rows="2"
          :value="modelValue"
          placeholder="问一个具体问题，例如：Java 后端的学习路线怎么排？"
          @input="onInput"
          @keydown.enter.exact.prevent="submit"
          @keydown.enter.shift.stop
        ></textarea>
        <button class="send-button" type="submit" :disabled="busy || !modelValue.trim()">
          {{ busy ? '生成中' : '发送' }}
        </button>
      </form>
      <div class="composer__hint">
        <span>Enter 发送 · Shift + Enter 换行</span>
        <span>回答由 AI 生成，请自行核实</span>
      </div>
    </div>
  </footer>
</template>

<script setup>
const props = defineProps({
  modelValue: {
    type: String,
    default: '',
  },
  busy: {
    type: Boolean,
    default: false,
  },
})

const emit = defineEmits(['update:modelValue', 'send'])

function onInput(event) {
  emit('update:modelValue', event.target.value)
}

function submit() {
  if (props.busy || !props.modelValue.trim()) {
    return
  }
  emit('send')
}
</script>
