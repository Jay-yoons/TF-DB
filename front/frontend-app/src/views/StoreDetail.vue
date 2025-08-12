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

      <router-link
        :to="{ name: 'BookingPage', params: { storeId: store.storeId } }"
        class="inline-block w-full text-center bg-blue-600 text-white font-bold py-3 px-6 rounded-lg hover:bg-blue-700 transition duration-300"
      >
        예약하기
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
import axios from 'axios'; // axios를 직접 import

export default {
  name: 'StoreDetail',
  setup() {
    const route = useRoute();
    const store = ref(null);
    const loading = ref(true);
    const error = ref(null);

    const fetchStoreDetail = async () => {
      try {
        const storeId = route.params.storeId;
        // this.$axios 대신 import한 axios 사용
        const response = await axios.get(`/api/stores/${storeId}`);
        store.value = response.data;
      } catch (e) {
        error.value = `가게 정보를 불러오는 데 실패했습니다: ${e.message}`;
      } finally {
        loading.value = false;
      }
    };

    onMounted(() => {
      fetchStoreDetail();
    });

    return {
      store,
      loading,
      error,
    };
  },
};
</script>