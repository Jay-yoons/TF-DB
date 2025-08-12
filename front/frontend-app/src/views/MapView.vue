<template>
  <!-- 지도가 표시될 컨테이너. 화면 전체를 차지하도록 스타일을 적용합니다. -->
  <div id="map" ref="mapRef" class="h-screen w-full"></div>
</template>

<script setup>
import { onMounted, onUnmounted, ref, nextTick } from 'vue';

const mapRef = ref(null);
let mapInstance = null;

// Google Maps 스크립트를 비동기적으로 로드하는 함수
const loadGoogleMapsApi = (apiKey) => {
  return new Promise((resolve, reject) => {
    // 스크립트가 이미 존재하면 추가 로드하지 않음
    if (document.querySelector(`script[src*="maps.googleapis.com"]`)) {
      if (typeof google !== 'undefined') {
        resolve();
      } else {
        window.addEventListener('load', resolve, { once: true });
      }
      return;
    }
    
    const script = document.createElement('script');
    script.src = `https://maps.googleapis.com/maps/api/js?key=${apiKey}`;
    script.async = true;
    script.defer = true;
    document.head.appendChild(script);

    script.onload = () => {
      console.log('Google Maps API script loaded successfully.');
      resolve();
    };

    script.onerror = (e) => {
      console.error("Google Maps API script loading failed:", e);
      reject(new Error("Google Maps API 스크립트 로드 실패"));
    };
  });
};

// 지도를 초기화하는 핵심 함수
const initMap = async () => {
  // nextTick을 사용하여 DOM이 완전히 렌더링된 후 지도를 초기화하도록 보장
  await nextTick();
  
  if (!mapRef.value) {
    console.error("Map container not found.");
    return;
  }
  
  try {
    const { Map } = await google.maps.importLibrary("maps");

    mapInstance = new Map(mapRef.value, {
      zoom: 12,
      center: { lat: 37.5665, lng: 126.9780 }, // 서울 시청
      mapId: "DEMO_MAP_ID", // AdvancedMarkerElement 사용 시 Map ID 필요
      disableDefaultUI: true,
      zoomControl: true,
    });

  } catch (e) {
    console.error("Failed to load map libraries:", e);
  }
};


onMounted(() => {
  // 실제 API 키를 여기에 넣어주세요
  const apiKey = 'AIzaSyB6yKm_vZw2nCWECG9Ju_Ytg2i19W3L96s'; 

  loadGoogleMapsApi(apiKey)
    .then(() => {
      initMap();
    })
    .catch(e => {
      console.error(e.message);
    });
});

// 컴포넌트 언마운트 시 리소스 정리
onUnmounted(() => {
  if (mapInstance) {
    mapInstance = null;
  }
});
</script>

<style scoped>
/* Tailwind CSS 클래스를 사용해 컨테이너 크기 지정 */
#map {
  height: 100vh; /* viewport 높이의 100% */
  width: 100vw;  /* viewport 너비의 100% */
}
</style>
