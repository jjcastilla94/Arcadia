export function formatDuration(seconds) {
  const total = Math.max(0, Math.floor(seconds || 0))
  const hours = Math.floor(total / 3600)
  const minutes = Math.floor((total % 3600) / 60)
  if (hours > 0) return `${hours}h ${minutes}m`
  if (minutes > 0) return `${minutes}m ${total % 60}s`
  return `${total}s`
}

export function formatClock(seconds) {
  const total = Math.max(0, Math.floor(seconds || 0))
  const minutes = String(Math.floor(total / 60)).padStart(2, '0')
  const secs = String(total % 60).padStart(2, '0')
  return `${minutes}:${secs}`
}
