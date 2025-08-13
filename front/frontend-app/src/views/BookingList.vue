<template>
  <div class="booking-list">
    <h1>예약 목록</h1>
    <ul v-if="bookings.length > 0">
      <li v-for="booking in bookings" :key="booking.bookingNum">
        <div class="booking-card">
          <div class="card-header">
            <strong>예약 번호:</strong> 
            <router-link :to="{ name: 'BookingDetail', params: { bookingNum: booking.bookingNum } }">
              {{ booking.bookingNum }}
            </router-link>
          </div>
          <div class="card-body">
            <div><strong>매장 ID:</strong> {{ booking.storeId }}</div>
            <div><strong>예약 날짜:</strong> {{ booking.bookingDate }}</div>
            <div><strong>예약 상태:</strong> {{ booking.bookingState }}</div>
          </div>
        </div>
      </li>
    </ul>
    <p v-else-if="loading">예약 목록을 불러오는 중입니다...</p>
    <p v-else-if="error">{{ error }}</p>
    <p v-else>예약 목록이 없습니다.</p>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import { getCurrentUserId } from '@/utils/auth';

export default {
  name: 'BookingList',
  setup() {
    const bookings = ref([]);
    const loading = ref(true);
    const error = ref(null);

    const fetchBookings = async () => {
      try {
        const userId = getCurrentUserId();
        const accessToken = localStorage.getItem('accessToken');
        
        if (!userId || !accessToken) {
          error.value = '로그인이 필요합니다.';
          loading.value = false;
          return;
        }

        const response = await axios.get(`/api/bookings/users/${userId}`, {
          headers: {
            Authorization: `Bearer ${accessToken}`,
          },
        });
        bookings.value = response.data;
      } catch (e) {
        error.value = `예약 목록을 불러오는 데 실패했습니다: ${e.message}`;
      } finally {
        loading.value = false;
      }
    };

    onMounted(() => {
      fetchBookings();
    });

    return {
      bookings,
      loading,
      error,
    };
  },
};
</script>

<style scoped>
.booking-list {
  max-width: 600px;
  margin: 0 auto;
}
.booking-card {
  border: 1px solid #ccc;
  border-radius: 8px;
  margin-bottom: 15px;
  padding: 15px;
  text-align: left;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}
.card-header {
  font-size: 1.2em;
  border-bottom: 1px solid #eee;
  padding-bottom: 10px;
  margin-bottom: 10px;
}
.card-body div {
  margin-bottom: 5px;
}
</style>