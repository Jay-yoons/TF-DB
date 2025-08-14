import { createApp } from 'vue';
import { createPinia } from 'pinia';
import App from './App.vue';
import router from './router';
import axios from 'axios';
import { useUserStore } from '@/stores/userStore';

// Pinia 인스턴스 생성
const pinia = createPinia();
const app = createApp(App);

// 1. 앱에 Pinia를 등록
app.use(pinia);

// 2. Pinia 스토어를 활성화된 Pinia 인스턴스에 접근하여 가져옴
const userStore = useUserStore();

// 3. Axios 전역 설정 (Pinia 스토어의 토큰을 사용하도록 수정)
//    이 시점에서 userStore는 이미 사용 가능합니다.
axios.interceptors.request.use(async config => {
  const token = userStore.accessToken;
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
}, error => {
  return Promise.reject(error);
});

// 4. 앱에 라우터를 등록
app.use(router);

// 5. 라우터 가드 설정
//    이 시점에서 userStore.isAuthenticated 상태는 초기화 상태(false)입니다.
router.beforeEach((to, from, next) => {
    // userStore를 가져옵니다.
    const userStore = useUserStore();

    // 1. 초기 로딩 시 localStorage에서 토큰을 불러와 상태를 업데이트
    if (!userStore.isAuthenticated && localStorage.getItem('accessToken')) {
        userStore.initializeStore();
    }

    // 2. 인증이 필요한 페이지에 접근할 때, 토큰 존재 여부를 확실히 확인
    if (to.meta.requiresAuth && !userStore.isAuthenticated) {
        console.log('인증되지 않아 로그인 페이지로 리다이렉트합니다.');
        next({ name: 'Login' });
    } else {
        next();
    }
});

// 6. 라우터가 준비된 후에 앱을 마운트
//    이렇게 하면 첫 페이지 로드 시 라우터 가드가 Pinia 상태가 로드된 후에 실행됩니다.
router.isReady().then(() => {
  app.mount('#app');
});