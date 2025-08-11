import { createRouter, createWebHistory } from 'vue-router';
import Home from '../views/Home.vue';
import BookingList from '../views/BookingList.vue';
import BookingDetail from '../views/BookingDetail.vue'; // 새로운 컴포넌트 추가

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home
  },
  {
    path: '/bookings',
    name: 'BookingList',
    component: BookingList
  },
  {
    path: '/bookings/:bookingNum', // 예약 번호를 파라미터로 받는 동적 라우트
    name: 'BookingDetail',
    component: BookingDetail
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

export default router;