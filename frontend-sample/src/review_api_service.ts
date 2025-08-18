// TypeScript
// 기존 export들(listStores, getStoreDetail, getStoreLocation, incrementSeat, decrementSeat, getAvailableSeats, fetchMapsKey 등) 유지
import { authHeaders } from './auth'

export type ReviewDto = {
  reviewId: number
  storeId: string
  userId: string
  comment: string
  score: number
}

export type ReviewRequest = {
  storeId: string
  comment: string
  score: number
}

const API = '' // Vite 프록시 사용 중이면 빈 문자열 유지

export async function getStoreReviews(storeId: string): Promise<ReviewDto[]> {
  const res = await fetch(`${API}/api/reviews/stores/${storeId}`, {
    headers: await authHeaders(),
  })
  if (!res.ok) throw new Error('getStoreReviews failed: ' + res.status)
  return res.json()
}

export async function createReview(req: ReviewRequest): Promise<ReviewDto> {
  const res = await fetch(`${API}/api/reviews`, {
    method: 'POST',
    headers: await authHeaders({ 'Content-Type': 'application/json' }),
    body: JSON.stringify(req),
  })
  if (!res.ok) {
    const msg = await res.text().catch(() => '')
    throw new Error('createReview failed: ' + res.status + (msg ? ' ' + msg : ''))
  }
  return res.json()
}
