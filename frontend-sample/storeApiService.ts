// frontend-sample/src/api.ts
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

const API = '' // Vite dev 프록시 사용 시 빈 문자열 유지

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

export async function incrementSeat(storeId: string, count = 1): Promise<number> {
  const res = await fetch(`${API}/api/stores/${storeId}/seats/increment?count=${count}`, { method: 'POST' })
  if (!res.ok) throw new Error('incrementSeat failed: ' + (await res.text()).slice(0, 200))
  return res.json() // 여유 좌석 수(int)
}

export async function decrementSeat(storeId: string, count = 1): Promise<number> {
  const res = await fetch(`${API}/api/stores/${storeId}/seats/decrement?count=${count}`, { method: 'POST' })
  if (!res.ok) throw new Error('decrementSeat failed: ' + (await res.text()).slice(0, 200))
  return res.json() // 여유 좌석 수(int)
}

export async function getAvailableSeats(storeId: string): Promise<number> {
  const res = await fetch(`${API}/api/stores/${storeId}/available-seats`)
  if (!res.ok) throw new Error('getAvailableSeats failed: ' + res.status)
  return res.json()
}

export async function fetchMapsKey(): Promise<string> {
  const res = await fetch(`${API}/api/config/maps-key`)
  if (!res.ok) throw new Error('maps-key fetch failed: ' + res.status)
  const data = await res.json()
  return data.googleMapsApiKey || ''
}
