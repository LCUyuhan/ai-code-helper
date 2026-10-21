import { apiBaseUrl } from './http'

export function createChatStreamUrl(memoryId, message) {
  const params = new URLSearchParams({
    memoryId: String(memoryId),
    message,
  })

  return `${apiBaseUrl}/ai/chat?${params.toString()}`
}