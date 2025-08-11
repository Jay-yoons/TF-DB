<template>
  <div class="booking-detail">
    <div v-if="booking">
      <h1>예약 상세 정보</h1>
      <div class="detail-card">
        <div><strong>예약 번호:</strong> {{ booking.bookingNum }}</div>
        <div><strong>매장 ID:</strong> {{ booking.storeId }}</div>
        <div><strong>예약 날짜:</strong> {{ booking.bookingDate }}</div>
        <div><strong>예약 상태:</strong> {{ booking.bookingStateCode }}</div>
        <div><strong>좌석 수:</strong> {{ booking.count }}</div> </div>
      <div v-if="booking.bookingStateCode === 'CONFIRMED'">
        <button @click="cancelBooking" class="cancel-button">예약 취소</button>
      </div>
      <p v-else>취소할 수 없는 예약입니다.</p>
    </div>
    <div v-else>
      <p>예약 정보를 불러오는 중입니다...</p>
    </div>
  </div>
</template>

<script>
export default {
  name: 'BookingDetail',
  data() {
    return {
      booking: null,
    };
  },
  created() {
    this.fetchBookingDetail(this.$route.params.bookingNum);
  },
  methods: {
    fetchBookingDetail(bookingNum) {
      this.$axios.get(`/api/bookings/${bookingNum}`)
        .then(response => {
          this.booking = response.data;
        })
        .catch(error => {
          console.error('예약 상세 정보를 가져오는 데 실패했습니다.', error);
        });
    },
    cancelBooking() {
      this.$axios.patch(`/api/bookings/${this.booking.bookingNum}`)
        .then(response => {
          this.booking = response.data;
          alert('예약이 취소되었습니다.');
        })
        .catch(error => {
          console.error('예약 취소에 실패했습니다.', error);
        });
    },
  },
};
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
</style>