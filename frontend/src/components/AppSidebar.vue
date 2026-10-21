<template>
  <aside class="rail">
    <div class="brand">
      <div class="brand__mark" aria-hidden="true">AI</div>
      <div>
        <div class="brand__name">AI 编程小助手</div>
        <div class="brand__sub">code · career · interview</div>
      </div>
    </div>

    <section>
      <p class="rail__label">当前会话</p>
      <div class="session">
        <div class="session__key">SESSION ID</div>
        <div class="session__id">{{ memoryId }}</div>
      </div>
    </section>

    <section>
      <p class="rail__label">可以问我</p>
      <ul class="rail__list">
        <li><span>01</span>规划编程学习路线</li>
        <li><span>02</span>项目学习与改进建议</li>
        <li><span>03</span>简历优化与投递策略</li>
        <li><span>04</span>高频面试题与答题思路</li>
      </ul>
    </section>

    <p class="rail__footer">
      回答由 AI 生成，重要信息请自行核实。<br />
      知识库检索 · 联网搜索 · 工具调用
    </p>
  </aside>
</template>

<script setup>
defineProps({
  memoryId: {
    type: String,
    required: true,
  },
})
</script>
