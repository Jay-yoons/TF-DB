<script setup lang="ts">
import { onMounted, onBeforeUnmount, ref, watch } from 'vue'
// 이 파일에서 키를 호출한다면 두 단계 올라갑니다.
import { fetchMapsKey } from '../../storeApiService'

const props = defineProps<{ lat: number; lng: number }>()
const mapEl = ref<HTMLDivElement | null>(null)
const error = ref<string | null>(null)
let mounted = true

async function loadGoogleScript(apiKey: string) {
  // @ts-ignore
  if (window.google?.maps) return
  await new Promise<void>((resolve, reject) => {
    // @ts-ignore
    window.initMap = () => resolve()
    const s = document.createElement('script')
    s.src = `https://maps.googleapis.com/maps/api/js?key=${apiKey}&libraries=places&callback=initMap`
    s.async = true
    s.defer = true
    s.onerror = () => reject(new Error('Google Maps script load failed'))
    document.head.appendChild(s)
  })
}

async function renderMap() {
  try {
    error.value = null
    const key = await fetchMapsKey()
    if (!key) throw new Error('Google Maps API key is empty')
    await loadGoogleScript(key)
    if (!mounted || !mapEl.value) return

    mapEl.value.style.height = '420px'
    mapEl.value.style.width = '100%'

    // @ts-ignore
    const map = new google.maps.Map(mapEl.value, {
      center: { lat: props.lat, lng: props.lng },
      zoom: 15,
    })
    // @ts-ignore
    new google.maps.Marker({ position: { lat: props.lat, lng: props.lng }, map })
  } catch (e: any) {
    console.error(e)
    error.value = e?.message ?? 'map error'
  }
}

onMounted(renderMap)
watch(() => [props.lat, props.lng], renderMap)
onBeforeUnmount(() => { mounted = false })
</script>

<template>
  <div>
    <div ref="mapEl" class="map-box"></div>
    <div v-if="error" class="err">지도를 불러오지 못했습니다: {{ error }}</div>
  </div>
</template>

<style scoped>
.map-box { border-radius: 8px; background: #f5f6f7; }
.err { color: crimson; margin-top: 8px; }
</style>
