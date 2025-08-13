<script setup lang="ts">
import { onMounted, ref, watch, computed } from 'vue'
import { useRoute } from 'vue-router'
import StoreMap from './StoreMap.vue'
import {
  decrementSeat,
  getAvailableSeats,
  getStoreDetail,
  getStoreLocation,
  incrementSeat,
  getStoreReviews,
  createReview,
  computeAverageScore,
  isOpenNowKST,
  openStatusKST,
  type ReviewDto,
} from '../../storeApiService'

interface StoreDetailType {
  storeId: string
  storeName: string
  storeLocation: string
  seatNum: number
  availableSeats: number
  imageUrl?: string | null
  serviceTime?: string
  openNow?: boolean | null
  openStatus?: string | null
}

const route = useRoute()
const storeId = ref<string>(route.params.storeId as string)

const detail = ref<StoreDetailType | null>(null)
const lat = ref<number | null>(null)
const lng = ref<number | null>(null)
const count = ref<number>(1)
const loading = ref<boolean>(true)
const error = ref<string | null>(null)
const busy = ref<boolean>(false)

// 리뷰/평균
const reviews = ref<ReviewDto[]>([])
const avgScore = ref<number | null>(null)
const reviewUserId = ref<string>('demo-user') // 샘플 계정
const reviewScore = ref<number>(5)
const reviewComment = ref<string>('')

// KST 기준 영업 상태(백엔드 값 우선, 없으면 계산)
const openInfo = computed(() => {
  if (!detail.value) return { label: '영업종료', open: false }
  const d = detail.value
  const open = typeof d.openNow === 'boolean' ? d.openNow : isOpenNowKST(d.serviceTime ?? '')
  const label = d.openStatus ?? openStatusKST(d.serviceTime ?? '')
  return { open, label }
})

async function loadAll() {
  if (!storeId.value) return
  loading.value = true; error.value = null
  try {
    const [d, loc, rv] = await Promise.all([
      getStoreDetail(storeId.value),
      getStoreLocation(storeId.value),
      getStoreReviews(storeId.value),
    ])
    detail.value = d
    reviews.value = rv
    avgScore.value = computeAverageScore(rv)

    const plat = parseFloat(loc.latitude)
    const plng = parseFloat(loc.longitude)
    lat.value = Number.isFinite(plat) ? plat : null
    lng.value = Number.isFinite(plng) ? plng : null
  } catch (e: any) {
    error.value = e?.message ?? 'load error'
  } finally {
    loading.value = false
  }
}

async function updateAvailable() {
  if (!storeId.value) return
  const val = await getAvailableSeats(storeId.value)
  if (detail.value) detail.value = { ...detail.value, availableSeats: val }
}

async function onReserve(kind: 'inc' | 'dec') {
  if (!storeId.value) return
  busy.value = true
  try {
    if (kind === 'inc') await incrementSeat(storeId.value, count.value)
    else await decrementSeat(storeId.value, count.value)
    await updateAvailable()
  } catch (e: any) {
    alert(e?.message ?? '예약 처리 중 오류')
  } finally {
    busy.value = false
  }
}

async function submitReview() {
  if (!storeId.value) return
  try {
    if (!reviewComment.value.trim()) {
      alert('리뷰 내용을 입력해 주세요.')
      return
    }
    await createReview({
      storeId: storeId.value,
      userId: reviewUserId.value,
      comment: reviewComment.value.trim(),
      score: reviewScore.value,
    })
    // 목록/평균 갱신
    reviews.value = await getStoreReviews(storeId.value)
    avgScore.value = computeAverageScore(reviews.value)
    reviewComment.value = ''
    reviewScore.value = 5
    alert('리뷰가 등록되었습니다.')
  } catch (e: any) {
    alert(e?.message ?? '리뷰 등록 중 오류')
  }
}

onMounted(loadAll)
watch(() => route.params.storeId, (v) => {
  storeId.value = v as string
  loadAll()
})
</script>

<template>
  <div class="wrap">
    <div>
      <div class="back"><router-link to="/">← 돌아가기</router-link></div>
      <h2 class="title">
        {{ detail?.storeName || '상세' }}
        <span v-if="openInfo.open" class="badge open">{{ openInfo.label }}</span>
        <span v-else class="badge closed">{{ openInfo.label }}</span>
        <span class="rating" v-if="avgScore !== null">★ {{ avgScore }}</span>
        <span class="no-rating" v-else>평점 없음</span>
      </h2>
      <div class="sub">좌석 {{ detail?.seatNum }} · {{ detail?.storeLocation }}</div>
      <div class="hours" v-if="detail?.serviceTime">영업시간 {{ detail?.serviceTime }}</div>

      <!-- 지도 (가게 위치) -->
      <div class="map-card">
        <StoreMap v-if="lat !== null && lng !== null" :lat="lat!" :lng="lng!" />
        <div v-else class="map-fallback">위치 정보를 불러오지 못했습니다.</div>
      </div>

      <!-- 리뷰 목록 -->
      <div class="panel" style="margin-top:16px">
        <div class="panel-title">리뷰</div>
        <div v-if="!reviews.length" style="color:#666">아직 리뷰가 없습니다.</div>
        <ul v-else class="review-list">
          <li v-for="r in reviews" :key="r.reviewId" class="review-item">
            <div class="review-head">
              <b>{{ r.userId }}</b>
              <span class="score">★ {{ r.score }}</span>
            </div>
            <div class="review-body">{{ r.comment }}</div>
          </li>
        </ul>
      </div>

      <!-- 리뷰 작성(샘플) -->
      <div class="panel" style="margin-top:12px">
        <div class="panel-title">리뷰 작성(샘플)</div>
        <div class="row">
          <input v-model="reviewUserId" placeholder="userId" class="txt" />
          <select v-model.number="reviewScore" class="txt">
            <option :value="5">5</option>
            <option :value="4">4</option>
            <option :value="3">3</option>
            <option :value="2">2</option>
            <option :value="1">1</option>
          </select>
        </div>
        <div class="row">
          <textarea v-model="reviewComment" placeholder="한 줄 후기" rows="3" class="ta"></textarea>
        </div>
        <div class="row">
          <button @click="submitReview">등록</button>
        </div>
      </div>
    </div>

    <!-- 예약(좌석 증감) 사이드 패널 -->
    <aside class="aside">
      <img v-if="detail?.imageUrl" :src="detail.imageUrl" alt="thumb" class="aside-thumb" />
      <div class="panel">
        <div class="panel-title">좌석</div>
        <div>여유 좌석: <b>{{ detail?.availableSeats }}</b></div>
        <div class="row">
          <input type="number" min="1" v-model.number="count" class="num" />
          <button :disabled="busy" @click="onReserve('inc')">예약 확정</button>
          <button :disabled="busy" @click="onReserve('dec')">예약 취소</button>
        </div>
        <div class="row">
          <button @click="updateAvailable">여유 좌석 새로고침</button>
        </div>
      </div>
    </aside>
  </div>

  <div v-if="loading" class="loading">로딩 중...</div>
  <div v-if="error" class="err">에러: {{ error }}</div>
</template>

<style scoped>
.wrap { padding: 16px; display: grid; grid-template-columns: 1fr 360px; gap: 24px; }
.back { margin-bottom: 8px; }
.title { margin: 0; display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.badge { padding: 2px 6px; border-radius: 6px; font-size: 12px; }
.badge.open { background: #e8f5e9; color: #2e7d32; }
.badge.closed { background: #ffebee; color: #c62828; }
.rating { color: #ff9800; font-weight: 700; font-size: 18px; }
.no-rating { color: #999; font-size: 12px; }
.sub { color: #666; margin-bottom: 6px; }
.hours { color: #444; font-size: 13px; margin-bottom: 12px; }
.map-card { border: 1px solid #eee; border-radius: 8px; padding: 8px; min-height: 420px; }
.map-fallback { padding: 12px; }
.aside-thumb { width: 100%; height: 180px; object-fit: cover; border-radius: 8px; margin-bottom: 12px; }
.panel { border: 1px solid #eee; border-radius: 8px; padding: 12px; }
.panel-title { font-weight: 600; margin-bottom: 8px; }
.row { display: flex; align-items: center; gap: 8px; margin-top: 8px; }
.num { width: 80px; }
.review-list { list-style: none; padding: 0; margin: 0; display: flex; flex-direction: column; gap: 10px; }
.review-item { border: 1px solid #f0f0f0; border-radius: 8px; padding: 10px; }
.review-head { display: flex; justify-content: space-between; color: #333; }
.score { color: #ff9800; }
.err { color: crimson; padding: 16px; }
.loading { padding: 16px; }
</style>
