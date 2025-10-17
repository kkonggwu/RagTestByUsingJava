export function useSSE(url: string, onMessage: (data: string) => void, onError?: (err: unknown) => void) {
  const es = new EventSource(url)
  es.onmessage = (e) => {
    try {
      onMessage(e.data)
    } catch (err) {
      onError?.(err)
    }
  }
  es.onerror = (e) => {
    onError?.(e)
    es.close()
  }
  return () => es.close()
}

export async function sseStreamFromFetch(response: Response, onMessage: (data: string) => void) {
  const reader = response.body?.getReader()
  if (!reader) return
  const decoder = new TextDecoder()
  let buffer = ''
  while (true) {
    const { done, value } = await reader.read()
    if (done) break
    buffer += decoder.decode(value, { stream: true })
    const parts = buffer.split('\n\n')
    buffer = parts.pop() || ''
    for (const part of parts) {
      for (const line of part.split('\n')) {
        if (line.startsWith('data:')) {
          onMessage(line.slice(5).trim())
        }
      }
    }
  }
  if (buffer.startsWith('data:')) onMessage(buffer.slice(5).trim())
}

