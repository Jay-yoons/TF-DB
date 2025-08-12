<template>
  <div class="bg-gray-100 min-h-screen p-8">
    <h1 class="text-3xl font-bold text-gray-800 mb-6">가게 목록</h1>

    <!-- 로딩 상태 -->
    <div v-if="loading" class="flex justify-center items-center h-64">
      <p class="text-xl text-gray-500">가게 목록을 불러오는 중입니다...</p>
    </div>

    <!-- 에러 상태 -->
    <div v-else-if="error" class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded relative" role="alert">
      <strong class="font-bold">오류 발생:</strong>
      <span class="block sm:inline">{{ error }}</span>
    </div>

    <!-- 가게 목록 표시 -->
    <div v-else class="max-w-4xl mx-auto space-y-4">
      <div v-for="store in stores" :key="store.storeId" class="bg-white rounded-xl shadow-md p-6 hover:bg-gray-50 transition-colors cursor-pointer">
        <router-link :to="`/stores/${store.storeId}`" class="block">
          <h2 class="text-2xl font-bold text-blue-600 hover:underline">{{ store.storeName }}</h2>
          <p class="text-gray-600 mt-1">{{ store.storeLocation }}</p>
          <div class="mt-4 text-sm text-gray-500">
            <span class="mr-4">전체 좌석: {{ store.seatNum }}석</span>
            <span>영업 시간: {{ store.serviceTime }}</span>
          </div>
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';

const stores = ref([]);
const loading = ref(true);
const error = ref(null);

const fetchStores = async () => {
  try {
    const response = await axios.get('/api/stores');
    stores.value = response.data;
  } catch (err) {
    console.error('가게 목록을 불러오는 데 실패했습니다:', err);
    error.value = '가게 목록을 불러오는 데 문제가 발생했습니다.';
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  fetchStores();
});
</script>

