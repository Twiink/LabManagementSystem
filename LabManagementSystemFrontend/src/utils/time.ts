export const DISPLAY_TIME_ZONE = 'Asia/Shanghai'

export function parseDateTime(value: string): Date | null {
  if (!value) return null
  const normalized = value.includes(' ') && !value.includes('T')
    ? value.replace(' ', 'T')
    : value
  const date = new Date(normalized)
  return Number.isNaN(date.getTime()) ? null : date
}

export function formatDateTime(value: string, options: Intl.DateTimeFormatOptions = {}) {
  const date = parseDateTime(value)
  if (!date) return ''
  return date.toLocaleString('zh-CN', {
    timeZone: DISPLAY_TIME_ZONE,
    hour12: false,
    ...options
  })
}

export function formatTime(value: string, options: Intl.DateTimeFormatOptions = {}) {
  const date = parseDateTime(value)
  if (!date) return ''
  return date.toLocaleTimeString('zh-CN', {
    timeZone: DISPLAY_TIME_ZONE,
    hour12: false,
    hour: '2-digit',
    minute: '2-digit',
    ...options
  })
}

export function formatDateKey(date: Date) {
  const parts = new Intl.DateTimeFormat('zh-CN', {
    timeZone: DISPLAY_TIME_ZONE,
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  }).formatToParts(date)
  const year = parts.find(p => p.type === 'year')?.value || ''
  const month = parts.find(p => p.type === 'month')?.value || ''
  const day = parts.find(p => p.type === 'day')?.value || ''
  return `${year}-${month}-${day}`
}
