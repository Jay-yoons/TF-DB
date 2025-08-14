<script setup lang="ts">
// [Vue 버전 지도 컴포넌트] - React 훅 미사용, Vue 라이프사이클로 동작
import { onMounted, onBeforeUnmount, ref } from 'vue'
import { fetchMapsKey } from '@/storeApiService'

const props = defineProps<{ lat: number; lng: number }>()
const mapEl = ref<HTMLDivElement | null>(null)
const error = ref<string | null>(null)

async function loadGoogleScript(apiKey: string) {
  if ((window as any).google?.maps) return
  await new Promise<void>((resolve, reject) => {
    ;(window as any).initMap = () => resolve()
    const s = document.createElement('script')
    s.src = `https://maps.googleapis.com/maps/api/js?key=${apiKey}&libraries=places&callback=initMap`
    s.async = true
    s.defer = true
    s.onerror = () => reject(new Error('Google Maps script load failed'))
    document.head.appendChild(s)
  })
}

let mounted = false
onMounted(async () => {
  try {
    mounted = true
    const key = await fetchMapsKey()
    if (!key) throw new Error('Google Maps API key is empty')
    await loadGoogleScript(key)
    if (!mounted) return
    if (mapEl.value) {
      mapEl.value.style.height = '420px'
      mapEl.value.style.width = '100%'
      const map = new (window as any).google.maps.Map(mapEl.value, {
        center: { lat: props.lat, lng: props.lng },
        zoom: 15,
      })
      new (window as any).google.maps.Marker({ position: { lat: props.lat, lng: props.lng }, map })
    }
  } catch (e: any) {
    console.error(e)
    error.value = e?.message ?? 'map error'
  }
})
onBeforeUnmount(() => { mounted = false })
</script>

<template>
  <div>
    <div ref="mapEl" style="border-radius: 8px; background: #f5f6f7" />
    <div v-if="error" style="color:crimson; margin-top:8px;">지도를 불러오지 못했습니다: {{ error }}</div>
  </div>
</template>
