import { marked } from 'marked'
import DOMPurify from 'dompurify'

marked.setOptions({
  gfm: true,
  breaks: true,
})

// 模型有时输出 "##标题"（符号后没有空格），Markdown 不会识别为标题，这里补一个空格
function normalizeHeadings(text) {
  return text.replace(/^(#{1,6})([^\s#])/gm, '$1 $2')
}

export function renderMarkdown(text) {
  if (!text) {
    return ''
  }
  return DOMPurify.sanitize(marked.parse(normalizeHeadings(text)))
}
