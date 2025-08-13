<template>
  <div class="bg-gray-100 min-h-screen">
    <nav class="bg-white shadow-md p-4 flex justify-between items-center">
      <div class="flex items-center">
        <router-link to="/" class="text-2xl font-bold text-gray-800">
          <span class="text-green-500">Restaurant</span> Reservation
        </router-link>
      </div>
      <div class="flex items-center space-x-4 text-sm text-gray-600">
        <router-link to="/" class="hover:text-blue-500 transition-colors">
          홈
        </router-link>
        <router-link to="/mypage" class="hover:text-blue-500 transition-colors">
          마이페이지
        </router-link>
        <button v-if="isLoggedIn" @click="logout"
          class="flex items-center space-x-1 hover:text-blue-500 transition-colors">
          <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none"
            stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" />
            <polyline points="16 17 21 12 16 7" />
            <line x1="21" y1="12" x2="9" y2="12" />
          </svg>
          <span>로그아웃</span>
        </button>
        <a v-else :href="loginUrl" class="flex items-center space-x-1 hover:text-blue-500 transition-colors">
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
    <main class="container mx-auto p-8">
      <h2 class="text-2xl font-bold text-gray-700 mb-6">주요 현황</h2>
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
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
import { ref, onMounted, onActivated } from 'vue';
import axios from 'axios';
import { useRouter } from 'vue-router';
// import { Auth } from 'aws-amplify';

export default {
  name: 'HomePage',
  setup() {
    const router = useRouter();
    const counts = ref({
      stores: 0,
      members: 0,
      bookings: 0
    });
    const isLoggedIn = ref(false);
    const loginUrl = ref('');

    const checkLoginStatus = async () => {
      // localStorage에 토큰이 있는지 직접 확인
      const accessToken = localStorage.getItem('accessToken');
      if (accessToken) {
        isLoggedIn.value = true;
      } else {
        isLoggedIn.value = false;
      }
    };
    
    const fetchCounts = async () => {
      try {
        const response = await axios.get('/api/dashboard/counts');
        counts.value = response.data;
      } catch (error) {
        console.error('대시보드 데이터를 불러오는 중 오류 발생:', error);
        counts.value = {
          stores: 3,
          members: 150,
          bookings: 23
        };
      }
    };

    const fetchLoginUrl = async () => {
      try {
        const response = await axios.get('/api/users/login/url');
        loginUrl.value = response.data.loginUrl;
      } catch (err) {
        console.error('로그인 URL을 불러오는 데 실패했습니다.', err);
      }
    };

    const logout = async () => {
      try {
        // localStorage의 토큰 삭제
        localStorage.removeItem('accessToken');
        localStorage.removeItem('idToken');
        localStorage.removeItem('refreshToken');
        
        isLoggedIn.value = false;
        alert('로그아웃되었습니다.');
        router.push('/');
      } catch (error) {
        console.error('로그아웃 중 오류 발생:', error);
        alert('로그아웃에 실패했습니다. 다시 시도해주세요.');
      }
    };

    onMounted(() => {
      checkLoginStatus();
      fetchCounts();
      fetchLoginUrl();
    });
    
    onActivated(() => {
      checkLoginStatus();
    });

    return {
      counts,
      isLoggedIn,
      loginUrl,
      logout,
    };
  },
};
</script>