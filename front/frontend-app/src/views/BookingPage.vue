<template>
  <div class="container mx-auto p-4 max-w-2xl">
    <div v-if="loading" class="text-center text-blue-500">
      예약 처리 중...
    </div>
    <div v-else-if="error" class="text-center text-red-500">
      {{ error }}
    </div>
    <div v-else-if="store" class="bg-white shadow-lg rounded-xl p-8">
      <h1 class="text-3xl font-bold mb-4">{{ store.storeName }} 예약</h1>
      <p class="text-gray-600 mb-4"><strong>잔여 좌석:</strong> {{ store.availableSeats }}석</p>

      <div class="mb-4">
        <label for="reservationDate" class="block text-sm font-medium text-gray-700">예약 날짜</label>
        <input
          id="reservationDate"
          type="date"
          v-model="reservationDate"
          class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-300 focus:ring focus:ring-blue-200 focus:ring-opacity-50"
          :min="today"
        />
      </div>

      <div class="mb-4">
        <label for="reservationTime" class="block text-sm font-medium text-gray-700">예약 시간</label>
        <input
          id="reservationTime"
          type="time"
          v-model="reservationTime"
          class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-300 focus:ring focus:ring-blue-200 focus:ring-opacity-50"
        />
      </div>

      <div class="mb-4">
        <label for="seatCount" class="block text-sm font-medium text-gray-700">예약 좌석 수</label>
        <input
          id="seatCount"
          type="number"
          v-model.number="count"
          min="1"
          :max="store.availableSeats"
          class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-300 focus:ring focus:ring-blue-200 focus:ring-opacity-50"
        />
      </div>

      <button
        @click="createBooking"
        class="w-full text-center bg-blue-600 text-white font-bold py-3 px-6 rounded-lg hover:bg-blue-700 transition duration-300"
        :disabled="!reservationDate || !reservationTime || count < 1 || count > store.availableSeats"
      >
        예약 확정
      </button>
    </div>
    <div v-else class="text-center text-gray-500">
      가게 정보를 찾을 수 없습니다.
    </div>
  </div>
</template>

<script>
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import axios from 'axios';
import { getCurrentUserId } from '@/utils/auth';

export default {
  name: 'BookingPage',
  setup() {
    const route = useRoute();
    const router = useRouter();
    const store = ref(null);
    const storeId = ref(route.params.storeId);
    const count = ref(1);
    const loading = ref(true);
    const error = ref(null);
    const reservationDate = ref('');
    const reservationTime = ref('');

    const today = computed(() => {
      const now = new Date();
      const year = now.getFullYear();
      const month = String(now.getMonth() + 1).padStart(2, '0');
      const day = String(now.getDate()).padStart(2, '0');
      return `${year}-${month}-${day}`;
    });

    const fetchStoreDetail = async () => {
      try {
        const response = await axios.get(`/api/stores/${storeId.value}`);
        store.value = response.data;
      } catch (e) {
        error.value = `가게 정보를 불러오는 데 실패했습니다: ${e.message}`;
      } finally {
        loading.value = false;
      }
    };

    const createBooking = async () => {
      loading.value = true;
      error.value = null;

      try {
        const userId = getCurrentUserId();
        const accessToken = localStorage.getItem('accessToken');

        if (!userId || !accessToken) {
          alert('예약을 위해 로그인이 필요합니다.');
          loading.value = false;
          return;
        }

        if (!reservationDate.value || !reservationTime.value) {
          alert('예약 날짜와 시간을 모두 선택해주세요.');
          loading.value = false;
          return;
        }

        const headers = {
          Authorization: `Bearer ${accessToken}`,
        };

        const bookingDate = `${reservationDate.value}T${reservationTime.value}`;
        
        console.log('전송될 예약 날짜/시간:', bookingDate);
        console.log('전송될 Authorization 헤더:', headers.Authorization);

        const bookingRequest = {
          storeId: storeId.value,
          userId: userId,
          count: count.value,
          bookingDate: bookingDate,
          state: 'CONFIRMED',
        };

        // 1. 예약 생성 API 호출
        const bookingResponse = await axios.post('/api/bookings/new', bookingRequest, { headers });
        console.log('예약 성공:', bookingResponse.data);

        // 2. 예약 성공 후 좌석 증가 API 호출
        const incrementSeatResponse = await axios.post(`/api/stores/${storeId.value}/seats/increment?count=${count.value}`, null, { headers });
        console.log('좌석 증가 성공:', incrementSeatResponse.data);

        const bookingNum = bookingResponse.data.bookingNum;

        if (bookingNum) {
          router.push({ name: 'BookingDetail', params: { bookingNum } });
        } else {
          throw new Error('API 응답에 예약 번호가 없습니다.');
        }
      } catch (e) {
        console.error('예약 또는 좌석 증가 실패:', e);
        error.value = `작업 실패: ${e.response?.data?.message || e.message}`;
      } finally {
        loading.value = false;
      }
    };

    onMounted(() => {
      fetchStoreDetail();
    });

    return {
      store,
      storeId,
      count,
      loading,
      error,
      reservationDate,
      reservationTime,
      today,
      createBooking,
    };
  },
};
</script>