<template>
  <div class="booking-detail">
    <div v-if="booking">
      <h1>예약 상세 정보</h1>
      <div class="detail-card">
        <div><strong>예약 번호:</strong> {{ booking.bookingNum }}</div>
        <div><strong>매장 ID:</strong> {{ booking.storeId }}</div>
        <div><strong>예약 날짜:</strong> {{ booking.bookingDate }}</div>
        <div><strong>예약 상태:</strong> {{ booking.bookingState }}</div>
        <div><strong>좌석 수:</strong> {{ booking.count }}</div>
      </div>
      <div v-if="booking.bookingState === 'CONFIRMED'">
        <button @click="cancelBooking" class="cancel-button">예약 취소</button>
      </div>
      <div v-else-if="booking.bookingState === 'COMPLETED'">
        <router-link
          :to="{ name: 'ReviewCreate', params: { storeId: booking.storeId, bookingNum: booking.bookingNum } }"
          class="review-button"
        >
          리뷰 작성
        </router-link>
      </div>
      <p v-else>취소 또는 리뷰 작성할 수 없는 예약입니다.</p>
    </div>
    <div v-else>
      <p>예약 정보를 불러오는 중입니다...</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import axios from 'axios';

const booking = ref(null);
const route = useRoute();

// 예약 상세 정보를 가져오는 함수
const fetchBookingDetail = async (bookingNum) => {
  const accessToken = localStorage.getItem('accessToken');
  const headers = accessToken ? { Authorization: `Bearer ${accessToken}` } : {};

  try {
    const response = await axios.get(`/api/bookings/${bookingNum}`, { headers });
    booking.value = response.data;
  } catch (error) {
    console.error('예약 상세 정보를 가져오는 데 실패했습니다.', error);
  }
};

// 예약 취소 함수
const cancelBooking = async () => {
  const accessToken = localStorage.getItem('accessToken');
  if (!accessToken) {
    alert('예약을 취소하려면 로그인이 필요합니다.');
    return;
  }
  const headers = { Authorization: `Bearer ${accessToken}` };
  
  try {
    // 1. 예약 취소 API 호출
    const cancelBookingResponse = await axios.patch(
      `/api/bookings/${booking.value.bookingNum}`,
      null,
      { headers }
    );
    console.log('예약 취소 성공:', cancelBookingResponse.data);

    // 2. 좌석 수 감소 API 호출
    const decrementSeatResponse = await axios.post(
      `/api/stores/${booking.value.storeId}/seats/decrement?count=${booking.value.count}`,
      null,
      { headers }
    );
    console.log('좌석 감소 성공:', decrementSeatResponse.data);

    alert('예약이 성공적으로 취소되었습니다');

    // 3. 취소 후 예약 정보 다시 불러오기
    fetchBookingDetail(booking.value.bookingNum);
  } catch (error) {
    console.error('예약 취소 작업 실패:', error);
    alert(`예약 취소에 실패했습니다: ${error.message}`);
  }
};

// 컴포넌트가 마운트될 때 예약 정보 가져오기
onMounted(() => {
  fetchBookingDetail(route.params.bookingNum);
});
</script>

<style scoped>
.booking-detail {
  max-width: 600px;
  margin: 0 auto;
}

.detail-card {
  border: 1px solid #ccc;
  border-radius: 8px;
  padding: 20px;
  text-align: left;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.detail-card div {
  margin-bottom: 10px;
}

.cancel-button {
  margin-top: 20px;
  padding: 10px 20px;
  background-color: #f44336;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-size: 1em;
}

.review-button {
  margin-top: 20px;
  padding: 10px 20px;
  background-color: #4CAF50;
  color: white;
  border-radius: 5px;
  cursor: pointer;
  text-decoration: none;
  font-size: 1em;
  display: inline-block;
}
</style>