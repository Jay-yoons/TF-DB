<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { listStores, getStoreLocation, fetchMapsKey, getStoreAvgScore } from '../../storeApiService'

type StoreSummary = {
  storeId: string
  storeName: string
  storeLocation: string
  seatNum: number
  imageUrl?: string | null
}

const stores = ref<StoreSummary[]>([])
const loading = ref(true)
const error = ref<string | null>(null)
const categoryStr = ref<string>('')

// 평균 점수 상태
const avgScores = ref<Record<string, number | null>>({})

// 지도 관련
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

function clearMarkers() {
  markers.forEach(m => m.setMap(null))
  markers = []
}

async function renderMarkers() {
  if (!mapEl.value) return
  try {
    const key = await fetchMapsKey()
    if (!key) throw new Error('Google Maps API key is empty')
    await loadGoogleScript(key)

    mapEl.value.style.height = '420px'
    mapEl.value.style.width = '100%'

    if (!map) {
      const center = { lat: 37.49795, lng: 127.0276368 }
      // @ts-ignore
      map = new google.maps.Map(mapEl.value, { center, zoom: 13 })
    }

    clearMarkers()

    const locations = await Promise.all(
      stores.value.map(async s => {
        try {
          const loc = await getStoreLocation(s.storeId)
          const lat = parseFloat(loc.latitude)
          const lng = parseFloat(loc.longitude)
          if (Number.isFinite(lat) && Number.isFinite(lng)) {
            return { s, lat, lng }
          }
        } catch {}
        return null
      })
    )

    const valid = locations.filter(Boolean) as { s: StoreSummary; lat: number; lng: number }[]
    if (!valid.length) return

    // @ts-ignore
    const bounds = new google.maps.LatLngBounds()
    valid.forEach(({ s, lat, lng }) => {
      // @ts-ignore
      const marker = new google.maps.Marker({
        position: { lat, lng },
        map: map!,
        title: `${s.storeName} · 좌석 ${s.seatNum}`,
      })
      // @ts-ignore
      const info = new google.maps.InfoWindow({
        content: `<div style="min-width:180px">
          <b>${s.storeName}</b><br/>
          ${s.storeLocation}<br/>
          좌석 ${s.seatNum}<br/>
          <a href="/stores/${s.storeId}">상세 보기</a>
        </div>`,
      })
      marker.addListener('click', () => info.open({ map: map!, anchor: marker }))
      markers.push(marker)
      // @ts-ignore
      bounds.extend({ lat, lng })
    })
    // @ts-ignore
    if (valid.length > 1) map!.fitBounds(bounds)
    else if (valid.length === 1) {
      // @ts-ignore
      map!.setCenter({ lat: valid[0].lat, lng: valid[0].lng })
      // @ts-ignore
      map!.setZoom(15)
    }
  } catch (e: any) {
    console.error(e)
  }
}

async function loadAvgScores() {
  const entries = await Promise.all(
    stores.value.map(async s => {
      try {
        const avg = await getStoreAvgScore(s.storeId)
        return [s.storeId, avg] as const
      } catch {
        return [s.storeId, null] as const
      }
    })
  )
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

    <div class="map-card">
      <div ref="mapEl" class="map-box"></div>
    </div>

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

    <ul class="grid">
      <li v-for="s in stores" :key="s.storeId" class="card">
        <div class="name">
          {{ s.storeName }}
          <span class="rating" v-if="avgScores[s.storeId] !== null">★ {{ avgScores[s.storeId] }}</span>
          <span class="no-rating" v-else>평점 없음</span>
        </div>
        <div class="sub">{{ s.storeLocation }} · 좌석 {{ s.seatNum }}</div>
        <img v-if="s.imageUrl" :src="s.imageUrl" alt="thumb" class="thumb" />
        <router-link :to="`/stores/${s.storeId}`" class="link">상세 보기</router-link>
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
.name { font-weight: 700; display: flex; align-items: center; gap: 8px; }
.sub { color: #666; font-size: 14px; margin-top: 2px; }
.thumb { width: 100%; height: 140px; object-fit: cover; margin-top: 8px; border-radius: 6px; }
.link { display: inline-block; margin-top: 8px; }
.rating { color: #ff9800; font-weight: 600; }
.no-rating { color: #999; font-size: 12px; }
.err { color: crimson; }
</style>
