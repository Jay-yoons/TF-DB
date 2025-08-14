<script setup lang="ts">
/* =======================
   StoreDetail (정상 구조)
   - 상세/지도/좌석/리뷰/즐겨찾기
   - 임포트 경로는 @ 별칭을 권장(@ = src)
   ======================= */

// API (src/storeApiService.ts)
import {
  addFavorite,
  removeFavorite,
  getMyFavorites,
  type FavoriteItem,
  getStoreDetail,
  getStoreLocation,
  getAvailableSeats,
  incrementSeat,
  decrementSeat,
  getStoreReviews,
  createReview,
  computeAverageScore,
  isOpenNowKST,
  openStatusKST,
  type ReviewDto,
} from "@/storeApiService"

// [수정] 지도 컴포넌트는 src/components/StoreMap.vue만 사용
import StoreMap from "../components/StoreMap.vue"

import { onMounted, ref, watch, computed } from "vue"
// [추가] 목록으로 이동을 위해 useRouter 임포트
import { useRoute, useRouter } from "vue-router"

/* === 타입 === */
interface StoreDetailType {
  storeId: string
  storeName: string
  storeLocation: string
  seatNum: number
  availableSeats: number
  imageUrl?: string | null
  imageUrls?: string[] | null
  serviceTime?: string
  openNow?: boolean | null
  openStatus?: string | null
}

/* === 테스트용 사용자 === */
const TEST_USER_ID_KEY = "demoUserId"
function getUserIdForTest(): string {
  const v = localStorage.getItem(TEST_USER_ID_KEY)
  if (v) return v
  localStorage.setItem(TEST_USER_ID_KEY, "U1")
  return "U1"
}

/* === 라우트/상태 === */
const route = useRoute()
// [추가] 목록으로 이동을 위한 라우터 인스턴스
const router = useRouter()
// [추가] 목록으로 이동 함수
function goStoreList() {
  router.push({ name: 'stores' })
}
const storeId = ref<string>(String(route.params.storeId ?? ""))

// 상세/지도
const detail = ref<StoreDetailType | null>(null)
const lat = ref<number | null>(null)
const lng = ref<number | null>(null)
const loading = ref<boolean>(true)
const error = ref<string | null>(null)

// 좌석
const count = ref<number>(1)
const busy = ref<boolean>(false)

// 리뷰
const reviews = ref<ReviewDto[]>([])
const avgScore = ref<number | null>(null)
const reviewUserId = ref<string>("demo-user")
const reviewScore = ref<number>(5)
const reviewComment = ref<string>("")

// 즐겨찾기
const isFavorite = ref<boolean>(false)
const loadingFavorite = ref<boolean>(false)
const favoriteError = ref<string>("")

// 영업 상태(백엔드 값 우선)
const openInfo = computed(() => {
  if (!detail.value) return { open: false, label: "영업종료" }
  const d = detail.value
  const open = typeof d.openNow === "boolean" ? d.openNow : isOpenNowKST(d.serviceTime ?? "")
  const label = d.openStatus ?? openStatusKST(d.serviceTime ?? "")
  return { open, label }
})

/* === 로딩 === */
async function loadAll() {
  if (!storeId.value) return
  loading.value = true
  error.value = null
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
    error.value = e?.message ?? "상세 정보를 불러오지 못했습니다."
  } finally {
    loading.value = false
  }
}

async function loadFavorite() {
  try {
    loadingFavorite.value = true
    favoriteError.value = ""
    const userId = getUserIdForTest()
    const list: FavoriteItem[] = await getMyFavorites(userId)
    isFavorite.value = list.some(f => f.storeId === storeId.value)
  } catch (e: any) {
    favoriteError.value = e?.message ?? "즐겨찾기 상태를 불러오지 못했습니다."
  } finally {
    loadingFavorite.value = false
  }
}

/* === 좌석 증감 === */
async function updateAvailable() {
  if (!storeId.value) return
  const val = await getAvailableSeats(storeId.value)
  if (detail.value) detail.value = { ...detail.value, availableSeats: val }
}

async function onReserve(kind: "inc" | "dec") {
  if (!storeId.value) return
  busy.value = true
  try {
    if (kind === "inc") await incrementSeat(storeId.value, count.value)
    else await decrementSeat(storeId.value, count.value)
    await updateAvailable()
  } catch (e: any) {
    alert(e?.message ?? "좌석 처리 중 오류")
  } finally {
    busy.value = false
  }
}

/* === 리뷰 등록 === */
async function submitReview() {
  if (!storeId.value) return
  try {
    if (!reviewComment.value.trim()) {
      alert("리뷰 내용을 입력해 주세요.")
      return
    }
    await createReview({
      storeId: storeId.value,
      userId: reviewUserId.value,
      comment: reviewComment.value.trim(),
      score: reviewScore.value,
    })
    reviews.value = await getStoreReviews(storeId.value)
    avgScore.value = computeAverageScore(reviews.value)
    reviewComment.value = ""
    reviewScore.value = 5
    alert("리뷰가 등록되었습니다.")
  } catch (e: any) {
    alert(e?.message ?? "리뷰 등록 중 오류")
  }
}

/* === 즐겨찾기 토글 === */
async function toggleFavorite() {
  try {
    loadingFavorite.value = true
    favoriteError.value = ""
    const userId = getUserIdForTest()
    if (isFavorite.value) {
      await removeFavorite(userId, storeId.value)
      isFavorite.value = false
    } else {
      await addFavorite(userId, storeId.value)
      isFavorite.value = true
    }
  } catch (e: any) {
    favoriteError.value = e?.message ?? "즐겨찾기 처리 중 오류"
  } finally {
    loadingFavorite.value = false
  }
}

/* === 라이프사이클/라우팅 === */
onMounted(async () => {
  await loadAll()
  await loadFavorite()
})

watch(() => route.params.storeId, async (nv) => {
  storeId.value = String(nv ?? "")
  await loadAll()
  await loadFavorite()
})
</script>

<template>
  <!-- 상단 액션 카드 -->
  <section class="container" style="margin-top: 12px;">
    <div class="card p-24" style="margin-bottom:16px;">
      <div style="display:flex; align-items:center; justify-content:space-between; gap:16px;">
        <div>
          <h2 style="margin:0; font-size:24px; font-weight:800;">매장 상세</h2>
          <div class="sub">운영 정보 · 리뷰 · 즐겨찾기</div>
        </div>
        <!-- [추가] 목록으로 버튼 -->
        <button class="btn btn-ghost" @click="goStoreList" aria-label="목록으로">← 목록으로</button>
        <button
          class="btn"
          :class="isFavorite ? 'fav-on' : 'fav-off'"
          :disabled="loadingFavorite || !storeId"
          @click="toggleFavorite"
          aria-label="즐겨찾기 토글"
          title="즐겨찾기 토글"
        >
          <span style="width:18px;height:18px;display:inline-block;" aria-hidden="true">
            <svg v-if="isFavorite" viewBox="0 0 24 24" fill="currentColor">
              <path d="M12 17.27 18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z"/>
            </svg>
            <svg v-else viewBox="0 0 24 24" fill="currentColor">
              <path d="M22 9.24l-7.19-.62L12 2 9.19 8.62 2 9.24l5.46 4.73L5.82 21 12 17.27 18.18 21l-1.63-7.03L22 9.24zm-10 6.11l-3.76 2.27 1-4.28L5.5 10.5l4.38-.38L12 6.3l2.12 3.82 4.38.38-3.74 3.84 1 4.28L12 15.35z"/>
            </svg>
          </span>
          <span>{{ isFavorite ? '즐겨찾기 해제' : '즐겨찾기 추가' }}</span>
        </button>
      </div>

      <div style="display:flex; align-items:center; gap:12px; margin-top:10px;">
        <span v-if="loadingFavorite" class="badge">처리중…</span>
        <span v-if="favoriteError" class="badge" style="background:rgba(42,21,21,.5); color:#ffc7c7; border-color:rgba(255,199,199,.18);">
          {{ favoriteError }}
        </span>
      </div>
    </div>

    <!-- 2열 레이아웃: 좌측(정보/지도/좌석), 우측(리뷰) -->
    <div class="grid grid-2">
      <!-- 좌측 -->
      <div style="display:flex; flex-direction: column; gap:16px; width:100%;">
        <!-- 정보 -->
        <div class="card p-24">
          <h3 style="font-size:18px; font-weight:800; margin:0 0 6px 0;">매장 정보</h3>
          <div class="sub">영업시간, 좌석, 위치 등 상세 정보</div>

          <div v-if="loading" class="text-muted" style="margin-top:12px;">불러오는 중…</div>
          <div v-else-if="error" style="margin-top:12px; color:#ffb4b4;">{{ error }}</div>
          <template v-else>
            <div style="display:grid; grid-template-columns: 120px 1fr; gap:16px; margin-top:12px;">
              <img
                v-if="detail?.imageUrl"
                :src="detail?.imageUrl || ''"
                alt="store"
                style="width:120px; height:120px; object-fit:cover; border-radius:12px; border:1px solid rgba(255,255,255,.08);"
              />
              <div>
                <div style="font-weight:700; font-size:18px;">{{ detail?.storeName }}</div>
                <div class="text-muted" style="margin-top:4px;">{{ detail?.storeLocation }}</div>
                <div style="margin-top:8px;">
                  <span class="badge" :style="{ background: openInfo.open ? 'rgba(18,36,24,.5)' : 'rgba(36,18,18,.5)' }">
                    {{ openInfo.label }}
                  </span>
                  <span class="badge" style="margin-left:8px;">좌석: {{ detail?.availableSeats }} / {{ detail?.seatNum }}</span>
                  <span class="badge" style="margin-left:8px;">영업시간: {{ detail?.serviceTime || '정보 없음' }}</span>
                </div>
              </div>
            </div>
          </template>
        </div>

        <!-- 지도 -->
        <div class="card p-24">
          <h3 style="font-size:18px; font-weight:800; margin:0 0 6px 0;">위치</h3>
          <div class="sub">Google Maps</div>
          <div style="margin-top:12px;">
            <StoreMap v-if="lat != null && lng != null" :lat="lat!" :lng="lng!" />
            <div v-else class="text-muted">좌표 정보를 불러오지 못했습니다.</div>
          </div>
        </div>

        <!-- 좌석 -->
        <div class="card p-24">
          <h3 style="font-size:18px; font-weight:800; margin:0 0 6px 0;">좌석 예약/해제</h3>
          <div class="sub">현재 좌석 수를 조정합니다</div>
          <div style="display:flex; align-items:center; gap:12px; margin-top:12px;">
            <input class="input" type="number" min="1" v-model.number="count" style="max-width:120px;" aria-label="좌석 변경 수" />
            <button class="btn btn-primary" :disabled="busy || loading" @click="onReserve('inc')">예약(+)</button>
            <button class="btn btn-danger" :disabled="busy || loading" @click="onReserve('dec')">해제(-)</button>
            <span class="text-muted" v-if="busy">처리중…</span>
          </div>
          <div class="text-muted" style="margin-top:12px;">
            현재 좌석: {{ detail?.availableSeats ?? "-" }} / {{ detail?.seatNum ?? "-" }}
          </div>
        </div>
      </div>

      <!-- 우측: 리뷰 -->
      <div class="card p-24">
        <h3 style="font-size:18px; font-weight:800; margin:0 0 6px 0;">리뷰</h3>
        <div class="sub">평균 평점과 사용자 리뷰</div>

        <div style="margin-top:12px;">
          <span class="badge">평균 평점: {{ avgScore ?? "-" }}</span>
        </div>

        <!-- 리뷰 목록 -->
        <div style="margin-top:16px;" v-if="reviews.length > 0">
          <div v-for="r in reviews" :key="r.reviewId" class="card p-16" style="margin-bottom:10px;">
            <div style="display:flex; align-items:center; justify-content:space-between;">
              <div style="font-weight:600;">{{ r.userId }}</div>
              <div class="badge">★ {{ r.score }}</div>
            </div>
            <div style="margin-top:6px;">{{ r.comment }}</div>
          </div>
        </div>
        <div class="text-muted" style="margin-top:16px;" v-else>아직 리뷰가 없습니다.</div>

        <!-- 리뷰 작성 -->
        <div style="margin-top:16px;">
          <div style="display:flex; align-items:center; gap:12px; margin-bottom:8px;">
            <select class="input" v-model.number="reviewScore" style="max-width:120px;">
              <option v-for="n in 5" :key="n" :value="n">평점 {{ n }}</option>
            </select>
            <input class="input" v-model="reviewUserId" style="max-width:220px;" placeholder="작성자(테스트용)" />
          </div>
          <textarea class="textarea" v-model="reviewComment" rows="3" placeholder="리뷰 내용을 입력하세요"></textarea>
          <div style="display:flex; align-items:center; gap:12px; margin-top:12px;">
            <button class="btn btn-primary" @click="submitReview">리뷰 등록</button>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
/* 페이지 전용 보조 스타일은 최소화합니다(전역은 app.css 사용). */
</style>
