import { createRouter, createWebHistory } from 'vue-router';
import HomePage from '../views/HomePage.vue';
import BookingList from '../views/BookingList.vue';
import BookingDetail from '../views/BookingDetail.vue';
import MapView from '../views/MapView.vue';
import StoreList from '../views/StoreList.vue';
import StoreDetail from '../views/StoreDetail.vue';
import BookingPage from '../views/BookingPage.vue';
import ReviewList from '../views/ReviewList.vue';
import ReviewDetail from '../views/ReviewDetail.vue';
import Login from '../views/LoginPage.vue';
import SignUp from '../views/SignUp.vue';
import ReviewCreate from '../views/ReviewCreate.vue';
import CallbackPage from '../views/CallbackPage.vue'

const routes = [
  {
    path: '/',
    name: 'HomePage',
    component: HomePage
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
  {
    path: '/map',
    name: 'mapView',
    component: MapView,
  },
  {
    path: '/stores',
    name: 'StoreList',
    component: StoreList,
  },
  {
    path: '/stores/:storeId', // 가게 상세 정보 페이지 라우트 추가
    name: 'StoreDetail',
    component: StoreDetail
  },
  {
    path: '/stores/:storeId/book',
    name: 'BookingPage',
    component: BookingPage
  },
  {
    path: '/stores/:storeId/reviews',
    name: 'ReviewList',
    component: ReviewList,
  },
  {
    path: '/reviews/:id',
    name: 'ReviewDetail',
    component: ReviewDetail,
  },
  { 
    path: '/login', 
    name: 'Login', 
    component: Login 
  },
  { 
    path: '/signup', 
    name: 'SignUp', 
    component: SignUp 
  },
  {
    path: '/reviews/new/:storeId/:bookingNum', // 리뷰 작성 페이지 경로
    name: 'ReviewCreate',
    component: ReviewCreate,
    props: true,
  },
  { 
    path: '/callback', 
    name: 'Callback', 
    component: CallbackPage 
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

export default router;