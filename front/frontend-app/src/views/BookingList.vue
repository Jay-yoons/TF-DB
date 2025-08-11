<template>
  <div>
    <h1>예약 목록</h1>
    <ul v-if="bookings.length > 0">
      <li v-for="booking in bookings" :key="booking.bookingNum">
        예약 번호: {{ booking.bookingNum }} <br>
        예약 날짜: {{ booking.bookingDate }} <br>
        매장 ID: {{ booking.storeId }} <br>
        예약 상태: {{ booking.bookingStateCode }}
      </li>
    </ul>
    <p v-else>예약 목록이 없습니다.</p>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  data() {
    return {
      bookings: [],
      userId: 'user_a', // 예시로 사용할 userId
    };
  },
  mounted() {
    this.fetchBookings();
  },
  methods: {
    fetchBookings() {
      // 프록시 설정이 되어있다고 가정하고, 상대 경로로 호출
      axios.get(`/api/bookings`, {
        params: {
          userId: this.userId,
        },
      })
      .then(response => {
        // API 응답 데이터로 bookings 배열을 업데이트합니다
        this.bookings = response.data;
      })
      .catch(error => {
        console.error('예약 목록을 가져오는 데 실패했습니다.', error);
      });
    },
  },
};
</script>