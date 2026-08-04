import { onBeforeUnmount, ref } from 'vue'

export function usePlayTimer() {
  const elapsedSeconds = ref(0)
  let interval = null

  function start() {
    if (interval) return
    interval = setInterval(() => {
      elapsedSeconds.value += 1
    }, 1000)
  }

  function stop() {
    if (interval) {
      clearInterval(interval)
      interval = null
    }
  }

  onBeforeUnmount(stop)

  return { elapsedSeconds, start, stop }
}
