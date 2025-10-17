export function createChatId(): string {
  const dt = new Date()
  const y = dt.getFullYear()
  const m = String(dt.getMonth() + 1).padStart(2, '0')
  const d = String(dt.getDate()).padStart(2, '0')
  const t = String(dt.getTime()).slice(-6)
  const rnd = Math.random().toString(36).slice(2, 8)
  return `chat_${y}${m}${d}_${t}_${rnd}`
}

