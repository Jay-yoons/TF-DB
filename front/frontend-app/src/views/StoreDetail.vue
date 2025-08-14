<template>
  <div class="container mx-auto p-4 max-w-2xl">
    <div v-if="loading" class="text-center text-blue-500">
      가게 정보를 불러오는 중...
    </div>
    <div v-else-if="error" class="text-center text-red-500">
      {{ error }}
    </div>
    <div v-else-if="store" class="bg-white shadow-lg rounded-xl p-8">
      <h1 class="text-3xl font-bold mb-4">{{ store.storeName }}</h1>
      <p class="text-gray-600 mb-2"><strong>위치:</strong> {{ store.storeLocation }}</p>
      <p class="text-gray-600 mb-2"><strong>영업시간:</strong> {{ store.serviceTime }}</p>
      <p class="text-gray-600 mb-4"><strong>전체 좌석:</strong> {{ store.seatNum }}석</p>
      <p class="text-gray-600 mb-4"><strong>잔여 좌석:</strong> {{ store.availableSeats }}석</p>
      
      <button
        @click="toggleFavorite"
        :class="[
          'inline-block w-full text-center font-bold py-3 px-6 rounded-lg transition duration-300 mb-4',
          isFavorite ? 'bg-yellow-500 text-white hover:bg-yellow-600' : 'bg-gray-200 text-gray-800 hover:bg-gray-300'
        ]"
      >
        <span v-if="isFavorite">⭐ 즐겨찾기 취소</span>
        <span v-else>🤍 즐겨찾기 추가</span>
      </button>

      <router-link
        :to="{ name: 'BookingPage', params: { storeId: store.storeId } }"
        class="inline-block w-full text-center bg-blue-600 text-white font-bold py-3 px-6 rounded-lg hover:bg-blue-700 transition duration-300 mb-4"
      >
        예약하기
      </router-link>

      <router-link
        :to="{ name: 'ReviewList', params: { storeId: store.storeId } }"
        class="inline-block w-full text-center bg-gray-600 text-white font-bold py-3 px-6 rounded-lg hover:bg-gray-700 transition duration-300"
      >
        리뷰 보기
      </router-link>

    </div>
    <div v-else class="text-center text-gray-500">
      가게 정보를 찾을 수 없습니다.
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import axios from 'axios';
import { useUserStore } from '@/stores/userStore';

export default {
  name: 'StoreDetail',
  setup() {
    const route = useRoute();
    const userStore = useUserStore();
    const store = ref(null);
    const loading = ref(true);
    const error = ref(null);
    const isFavorite = ref(false); // 즐겨찾기 상태를 저장할 반응형 변수 추가

    const fetchStoreDetail = async () => {
      try {
        const storeId = route.params.storeId;
        const response = await axios.get(`/api/stores/${storeId}`);
        store.value = response.data;
      } catch (e) {
        error.value = `가게 정보를 불러오는 데 실패했습니다: ${e.message}`;
      } finally {
        loading.value = false;
      }
    };
    
    // 즐겨찾기 상태를 확인하는 함수
    const checkFavoriteStatus = async () => {
      if (!userStore.isAuthenticated) {
        isFavorite.value = false;
        return;
      }
      try {
        const storeId = route.params.storeId;
        // /api/users/me/favorites/{storeId}/check 엔드포인트 사용
        const response = await axios.get(`/api/users/me/favorites/${storeId}/check`);
        isFavorite.value = response.data.isFavorite;
      } catch (e) {
        console.error("즐겨찾기 상태 확인 실패:", e);
      }
    };

    // 즐겨찾기 추가/삭제 토글 함수
    const toggleFavorite = async () => {
      if (!userStore.isAuthenticated) {
        alert("즐겨찾기 기능을 이용하려면 로그인이 필요합니다.");
        return;
      }
      try {
        const storeId = route.params.storeId;
        if (isFavorite.value) {
          // 즐겨찾기 취소 (DELETE)
          await axios.delete(`/api/users/me/favorites/${storeId}`);
          alert('즐겨찾기에서 삭제되었습니다.');
        } else {
          // 즐겨찾기 추가 (POST)
          await axios.post(`/api/users/me/favorites`, { storeId });
          alert('즐겨찾기에 추가되었습니다.');
        }
        // 상태를 업데이트
        isFavorite.value = !isFavorite.value;
      } catch (e) {
        console.error("즐겨찾기 토글 실패:", e);
        alert('즐겨찾기 처리 중 오류가 발생했습니다.');
      }
    };

    onMounted(async () => {
      await fetchStoreDetail();
      await checkFavoriteStatus();
    });

    return {
      store,
      loading,
      error,
      isFavorite,
      toggleFavorite,
    };
  },
};
</script>