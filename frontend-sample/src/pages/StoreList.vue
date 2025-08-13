<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { listStores, getStoreLocation, fetchMapsKey, getStoreAvgScore, isOpenNowKST, openStatusKST } from '../../storeApiService'

type StoreSummary = {
  storeId: string
  storeName: string
  storeLocation: string
  seatNum: number
  serviceTime?: string
  imageUrl?: string | null
  openNow?: boolean | null
  openStatus?: string | null
}

const stores = ref<StoreSummary[]>([])
const loading = ref(true)
const error = ref<string | null>(null)
const categoryStr = ref<string>('')

const avgScores = ref<Record<string, number | null>>({})

const mapEl = ref<HTMLDivElement | null>(null)
let map: google.maps.Map | null = null
let markers: google.maps.Marker[] = []

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
function clearMarkers() { markers.forEach(m => m.setMap(null)); markers = [] }

function getOpenLabel(item: StoreSummary): { label: string; open: boolean } {
  const open = typeof item.openNow === 'boolean' ? item.openNow : isOpenNowKST(item.serviceTime ?? '')
  const label = item.openStatus ?? openStatusKST(item.serviceTime ?? '')
  return { label, open }
}

async function renderMarkers() {
  if (!mapEl.value) return
  const key = await fetchMapsKey()
  await loadGoogleScript(key)
  mapEl.value.style.height = '420px'
  mapEl.value.style.width = '100%'
  if (!map) {
    // @ts-ignore
    map = new google.maps.Map(mapEl.value, { center: { lat: 37.49795, lng: 127.0276368 }, zoom: 13 })
  }
  clearMarkers()
  const locations = await Promise.all(stores.value.map(async s => {
    try {
      const loc = await getStoreLocation(s.storeId)
      const lat = parseFloat(loc.latitude), lng = parseFloat(loc.longitude)
      if (Number.isFinite(lat) && Number.isFinite(lng)) return { s, lat, lng }
    } catch {}
    return null
  }))
  const valid = locations.filter(Boolean) as { s: StoreSummary; lat: number; lng: number }[]
  if (!valid.length) return
  // @ts-ignore
  const bounds = new google.maps.LatLngBounds()
  valid.forEach(({ s, lat, lng }) => {
    // @ts-ignore
    const marker = new google.maps.Marker({ position: { lat, lng }, map })
    const infoHtml = `<div style="min-width:200px">
      <b>${s.storeName}</b><br/>
      ${s.storeLocation}<br/>
      좌석 ${s.seatNum}<br/>
      ${s.serviceTime ? `영업시간 ${s.serviceTime}<br/>` : ''}
      <a href="/stores/${s.storeId}">상세 보기</a>
    </div>`
    // @ts-ignore
    const info = new google.maps.InfoWindow({ content: infoHtml })
    marker.addListener('click', () => info.open({ map, anchor: marker }))
    markers.push(marker)
    // @ts-ignore
    bounds.extend({ lat, lng })
  })
  // @ts-ignore
  if (valid.length > 1) map!.fitBounds(bounds); else { map!.setCenter({ lat: valid[0].lat, lng: valid[0].lng }); map!.setZoom(15) }
}

async function loadAvgScores() {
  const entries = await Promise.all(stores.value.map(async s => {
    try { return [s.storeId, await getStoreAvgScore(s.storeId)] as const } catch { return [s.storeId, null] as const }
  }))
  avgScores.value = Object.fromEntries(entries)
}

async function reload() {
  loading.value = true; error.value = null
  try {
    const code = categoryStr.value ? Number(categoryStr.value) : undefined
    stores.value = await listStores(code)
    await Promise.all([renderMarkers(), loadAvgScores()])
  } catch (e: any) {
    error.value = e?.message ?? 'load error'
  } finally {
    loading.value = false
  }
}
onMounted(reload)
</script>

<template>
  <div class="page">
    <h2>가게 목록</h2>

    <!-- 지도(가게 위치) -->
    <div class="map-card"><div ref="mapEl" class="map-box"></div></div>

    <!-- 필터 -->
    <div class="toolbar">
      <select v-model="categoryStr" @change="reload">
        <option value="">전체</option>
        <option value="1">한식</option>
        <option value="2">일식</option>
        <option value="3">양식</option>
        <option value="4">중식</option>
        <option value="5">카페</option>
      </select>
      <button @click="reload">새로고침</button>
    </div>

    <div v-if="loading">로딩 중...</div>
    <div v-if="error" class="err">{{ error }}</div>

    <!-- 카드 목록 -->
    <ul class="grid">
      <li v-for="s in stores" :key="s.storeId" class="card">
        <div class="name">
          {{ s.storeName }}
          <span v-if="getOpenLabel(s).open" class="badge open">{{ getOpenLabel(s).label }}</span>
          <span v-else class="badge closed">{{ getOpenLabel(s).label }}</span>
        </div>
        <div class="sub">{{ s.storeLocation }} · 좌석 {{ s.seatNum }}</div>
        <div class="hours" v-if="s.serviceTime">영업시간 {{ s.serviceTime }}</div>
        <img v-if="s.imageUrl" :src="s.imageUrl" alt="thumb" class="thumb" />
        <router-link :to="`/stores/${s.storeId}`" class="link">상세 보기</router-link>
        <div class="rating-row">
          <span v-if="avgScores[s.storeId] !== null" class="rating">★ {{ avgScores[s.storeId] }}</span>
          <span v-else class="no-rating">평점 없음</span>
        </div>
      </li>
    </ul>
  </div>
</template>

<style scoped>
.page { padding: 16px; }
.map-card { border: 1px solid #eee; border-radius: 8px; padding: 8px; margin-bottom: 12px; }
.map-box { width: 100%; height: 420px; border-radius: 8px; background: #f5f6f7; }
.toolbar { margin: 8px 0; display: flex; gap: 8px; align-items: center; }
.grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(260px, 1fr)); gap: 12px; padding: 0; list-style: none; }
.card { border: 1px solid #eee; border-radius: 8px; padding: 12px; }
.name { font-weight: 700; display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.badge { padding: 2px 6px; border-radius: 6px; font-size: 12px; }
.badge.open { background: #e8f5e9; color: #2e7d32; }
.badge.closed { background: #ffebee; color: #c62828; }
.sub { color: #666; font-size: 14px; margin-top: 2px; }
.hours { color: #444; font-size: 13px; margin-top: 2px; }
.thumb { width: 100%; height: 140px; object-fit: cover; margin-top: 8px; border-radius: 6px; }
.link { display: inline-block; margin-top: 8px; }
.rating-row { margin-top: 6px; }
.rating { color: #ff9800; font-weight: 600; }
.no-rating { color: #999; font-size: 12px; }
.err { color: crimson; }
</style>
