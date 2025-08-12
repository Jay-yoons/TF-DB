<template>
  <div class="bg-gray-100 min-h-screen">
    <!-- 상단 네비게이션 바 -->
    <nav class="bg-white shadow-md p-4 flex justify-between items-center">
      <div class="flex items-center space-x-2">
        <!-- 아이콘과 서비스명 -->
        <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none"
          stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="text-blue-500">
          <path d="M12 2a3 3 0 0 0-3 3v7a3 3 0 0 0 6 0V5a3 3 0 0 0-3-3z" />
          <path d="M19 10v2a7 7 0 0 1-14 0v-2" />
          <line x1="12" x2="12" y1="19" y2="22" />
        </svg>
        <span class="text-xl font-bold text-gray-800">전국 레스토랑 예약</span>
      </div>
      <!-- 홈, 마이페이지, 로그인 링크 -->
      <div class="flex items-center space-x-4 text-sm text-gray-600">
        <!-- 홈 링크 -->
        <a href="#" class="flex items-center space-x-1 hover:text-blue-500 transition-colors">
          <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none"
            stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z" />
            <polyline points="9 22 9 12 15 12 15 22" />
          </svg>
          <span>홈</span>
        </a>
        <!-- 마이페이지 링크 -->
        <a href="#" class="flex items-center space-x-1 hover:text-blue-500 transition-colors">
          <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none"
            stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M19 21v-2a4 4 0 0 0-4-4H9a4 4 0 0 0-4 4v2" />
            <circle cx="12" cy="7" r="4" />
          </svg>
          <span>마이페이지</span>
        </a>
        <!-- 로그인 링크 -->
        <a href="#" class="flex items-center space-x-1 hover:text-blue-500 transition-colors">
          <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none"
            stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4" />
            <polyline points="10 17 15 12 10 7" />
            <line x1="15" x2="3" y1="12" y2="12" />
          </svg>
          <span>로그인</span>
        </a>
      </div>
    </nav>

    <!-- 메인 콘텐츠 영역 -->
    <main class="container mx-auto p-8">
      <h2 class="text-2xl font-bold text-gray-700 mb-6">주요 현황</h2>
      <!-- 통계 카드 섹션 -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">

        <!-- 카드 1: 등록된 레스토랑 수 -->
        <router-link to="/stores" class="block">
          <div
            class="bg-white rounded-xl p-8 shadow-md text-left transition-transform transform hover:scale-105 hover:shadow-lg">
            <div class="text-purple-500 mb-4">
              <svg xmlns="http://www.w3.org/2000/svg" width="36" height="36" viewBox="0 0 24 24" fill="none"
                stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" />
                <circle cx="9" cy="7" r="4" />
                <path d="M22 21v-2a4 4 0 0 0-3-3.87" />
                <path d="M16 3.13a4 4 0 0 1 0 7.75" />
              </svg>
            </div>
            <p class="text-4xl font-bold mb-2">{{ counts.stores }}</p>
            <p class="text-gray-500">등록된 레스토랑</p>
          </div>
        </router-link>
      </div>
      <router-link to="/map" class="block">
        <div
          class="bg-white rounded-xl p-8 shadow-md text-left transition-transform transform hover:scale-105 hover:shadow-lg">
          <div class="text-green-500 mb-4">
            <svg xmlns="http://www.w3.org/2000/svg" width="36" height="36" viewBox="0 0 24 24" fill="none"
              stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z" />
              <circle cx="12" cy="10" r="3" />
            </svg>
          </div>
          <p class="text-4xl font-bold mb-2">지도</p>
          <p class="text-gray-500">지도로 레스토랑 찾기</p>
        </div>
      </router-link>
    </main>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue';
import axios from 'axios';

export default {
  name: 'HomePage',
  setup() {
    const counts = ref({
      stores: 0,
      members: 0,
      bookings: 0
    });

    const fetchCounts = async () => {
      try {
        // 실제 API 엔드포인트에 맞게 수정이 필요합니다.
        // 현재는 예시 데이터를 사용합니다.
        const response = await axios.get('/api/dashboard/counts');
        counts.value = response.data;
      } catch (error) {
        console.error('대시보드 데이터를 불러오는 중 오류 발생:', error);
        // 오류 발생 시 임시 데이터 설정
        counts.value = {
          stores: 3,
          members: 150,
          bookings: 23
        };
      }
    };

    onMounted(() => {
      fetchCounts();
    });

    return {
      counts,
    };
  },
};
</script>
