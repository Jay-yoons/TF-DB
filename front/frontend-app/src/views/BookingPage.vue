<template>
  <div class="flex items-center justify-center min-h-screen bg-gray-100 p-6">
    <div class="bg-white p-8 rounded-xl shadow-lg w-full max-w-md">
      <h1 class="text-2xl font-bold text-gray-800 mb-6 text-center">예약하기</h1>

      <!-- 로딩 및 에러 메시지 -->
      <div v-if="loading" class="text-center text-blue-500 mb-4">예약을 진행 중입니다...</div>
      <div v-if="error" class="text-red-500 text-sm p-4 bg-red-50 rounded-lg mb-4">{{ error }}</div>

      <form @submit.prevent="makeBooking">
        <div class="mb-4">
          <label for="bookingDate" class="block text-gray-700 font-semibold mb-2">예약 날짜</label>
          <input
            type="datetime-local"
            id="bookingDate"
            v-model="bookingDate"
            required
            class="w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>

        <div class="mb-4">
          <label for="count" class="block text-gray-700 font-semibold mb-2">예약 인원</label>
          <input
            type="number"
            id="count"
            v-model.number="count"
            required
            min="1"
            class="w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>

        <div class="text-gray-500 text-sm mb-6">
          <p>가게 ID: {{ storeId }}</p>
          <p>사용자 ID: {{ userId }}</p>
        </div>

        <button
          type="submit"
          :disabled="loading"
          class="w-full bg-blue-600 text-white font-bold py-3 px-4 rounded-lg hover:bg-blue-700 transition duration-300 disabled:bg-blue-300 disabled:cursor-not-allowed"
        >
          예약 완료
        </button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import axios from 'axios';

const router = useRouter();
const route = useRoute();

const bookingDate = ref('');
const count = ref(1);
const loading = ref(false);
const error = ref(null);

const storeId = ref(route.params.storeId);
const userId = ref('user02');

const makeBooking = async () => {
  if (loading.value) return;

  // 클라이언트 측에서 유효성 검사 추가
  if (!bookingDate.value || !count.value || isNaN(count.value) || count.value < 1) {
    error.value = "예약 날짜와 유효한 예약 인원을 입력해주세요.";
    return;
  }

  loading.value = true;
  error.value = null;

  try {
    const bookingRequest = {
      bookingDate: bookingDate.value,
      userId: userId.value,
      storeId: storeId.value,
      count: count.value,
    };

    console.log('예약 요청 데이터:', bookingRequest);

    // 1. 예약 생성 API 호출
    const bookingResponse = await axios.post('/api/bookings/new', bookingRequest);
    console.log('예약 성공:', bookingResponse.data);

    // 2. 예약 성공 후 좌석 증가 API 호출
    // URL에 storeId와 count를 동적으로 포함시킵니다.
    const incrementSeatResponse = await axios.post(`/api/stores/${storeId.value}/seats/increment?count=${count.value}`);
    console.log('좌석 증가 성공:', incrementSeatResponse.data);

    const bookingNum = bookingResponse.data.bookingNum;

    if (bookingNum) {
      router.push({ name: 'BookingDetail', params: { bookingNum } });
    } else {
      throw new Error('API 응답에 예약 번호가 없습니다.');
    }
  } catch (e) {
    console.error('예약 또는 좌석 증가 실패:', e);
    // 두 API 호출 중 어느 하나라도 실패하면 사용자에게 오류 메시지를 표시합니다.
    error.value = `작업 실패: ${e.response?.data?.message || e.message}`;
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  // 초기화 로직
});
</script>

<style scoped>
/* Tailwind CSS */
</style>
