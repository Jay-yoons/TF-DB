import { createRouter, createWebHistory } from 'vue-router';
import HelloWorld from '../components/HelloWorld.vue'; // HelloWorld 컴포넌트 추가
import BookingList from '../views/BookingList.vue';

const routes = [
  {
    path: '/', // 루트 경로에 HelloWorld 컴포넌트 연결
    name: 'Home',
    component: HelloWorld
  },
  {
    path: '/bookings',
    name: 'BookingList',
    component: BookingList
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

export default router;