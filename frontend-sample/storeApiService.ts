// TypeScript
// 프록시 사용 중이면 빈 문자열 유지
const API = ''

// ---------- Stores ----------
export type StoreSummary = {
  storeId: string
  storeName: string
  storeLocation: string
  seatNum: number
  imageUrl?: string | null
}
export type StoreDetail = StoreSummary & {
  availableSeats: number
  imageUrls?: string[]
}
export type StoreLocation = { latitude: string; longitude: string }

export async function listStores(categoryCode?: number): Promise<StoreSummary[]> {
  const q = categoryCode != null ? `?categoryCode=${categoryCode}` : ''
  const res = await fetch(`${API}/api/stores${q}`)
  if (!res.ok) throw new Error('listStores failed: ' + res.status)
  return res.json()
}

export async function getStoreDetail(storeId: string): Promise<StoreDetail> {
  const res = await fetch(`${API}/api/stores/${storeId}`)
  if (!res.ok) throw new Error('getStoreDetail failed: ' + res.status)
  return res.json()
}

export async function getStoreLocation(storeId: string): Promise<StoreLocation> {
  const res = await fetch(`${API}/api/stores/${storeId}/location`)
  if (!res.ok) throw new Error('getStoreLocation failed: ' + res.status)
  return res.json()
}

// ---------- Seats (Booking) ----------
export async function incrementSeat(storeId: string, count = 1): Promise<number> {
  const res = await fetch(`${API}/api/stores/${storeId}/seats/increment?count=${count}`, { method: 'POST' })
  if (!res.ok) {
    const msg = await res.text().catch(() => '')
    throw new Error(`incrementSeat failed: ${res.status} ${msg}`)
  }
  return res.json()
}

export async function decrementSeat(storeId: string, count = 1): Promise<number> {
  const res = await fetch(`${API}/api/stores/${storeId}/seats/decrement?count=${count}`, { method: 'POST' })
  if (!res.ok) {
    const msg = await res.text().catch(() => '')
    throw new Error(`decrementSeat failed: ${res.status} ${msg}`)
  }
  return res.json()
}

export async function getAvailableSeats(storeId: string): Promise<number> {
  const res = await fetch(`${API}/api/stores/${storeId}/available-seats`)
  if (!res.ok) throw new Error('getAvailableSeats failed: ' + res.status)
  return res.json()
}

// ---------- Config (Maps key) ----------
export async function fetchMapsKey(): Promise<string> {
  const res = await fetch(`${API}/api/config/maps-key`)
  if (!res.ok) throw new Error('maps-key fetch failed: ' + res.status)
  const data = await res.json()
  return data.googleMapsApiKey || ''
}

// ---------- Reviews ----------
export type ReviewDto = {
  reviewId: number
  storeId: string
  userId: string
  comment: string
  score: number
}

export type ReviewRequest = {
  storeId: string
  userId: string
  comment: string
  score: number
}

export async function getStoreReviews(storeId: string): Promise<ReviewDto[]> {
  const res = await fetch(`${API}/api/reviews/stores/${storeId}`)
  if (!res.ok) throw new Error('getStoreReviews failed: ' + res.status)
  return res.json()
}

export async function createReview(req: ReviewRequest): Promise<ReviewDto> {
  const res = await fetch(`${API}/api/reviews`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(req),
  })
  if (!res.ok) {
    const msg = await res.text().catch(() => '')
    throw new Error(`createReview failed: ${res.status} ${msg}`)
  }
  return res.json()
}

// ... 기존 코드(Stores/Seats/Config/Reviews) 유지

export function computeAverageScore(reviews: ReviewDto[]): number | null {
  if (!reviews || reviews.length === 0) return null
  const sum = reviews.reduce((acc, r) => acc + (r.score ?? 0), 0)
  return Number((sum / reviews.length).toFixed(1)) // 소수점 1자리
}

export async function getStoreAvgScore(storeId: string): Promise<number | null> {
  const reviews = await getStoreReviews(storeId)
  return computeAverageScore(reviews)
}
