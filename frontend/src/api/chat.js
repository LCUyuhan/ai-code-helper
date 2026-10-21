export const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8081/api'

export function createChatStreamUrl(memoryId, message) {
  const params = new URLSearchParams({
    memoryId: String(memoryId),
    message,
  })

  return `${apiBaseUrl}/ai/chat?${params.toString()}`
}
