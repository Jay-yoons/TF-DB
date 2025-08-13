// frontend-sample/src/components/StoreMap.tsx
import { useEffect, useRef, useState } from 'react'
import { fetchMapsKey } from '../api'

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

export default function StoreMap({ lat, lng }: { lat: number; lng: number }) {
  const ref = useRef<HTMLDivElement>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let mounted = true
    ;(async () => {
      try {
        const key = await fetchMapsKey()
        if (!key) throw new Error('Google Maps API key is empty')
        await loadGoogleScript(key)
        if (!mounted) return
        if (ref.current) {
          ref.current.style.height = '420px'
          ref.current.style.width = '100%'
          const map = new (window as any).google.maps.Map(ref.current, {
            center: { lat, lng },
            zoom: 15,
          })
          new (window as any).google.maps.Marker({ position: { lat, lng }, map })
        }
      } catch (e: any) {
        console.error(e)
        setError(e?.message ?? 'map error')
      }
    })()
    return () => {
      mounted = false
    }
  }, [lat, lng])

  return (
    <div>
      <div ref={ref} style={{ borderRadius: 8, background: '#f5f6f7' }} />
      {error && <div style={{ color: 'crimson', marginTop: 8 }}>지도를 불러오지 못했습니다: {error}</div>}
    </div>
  )
}
