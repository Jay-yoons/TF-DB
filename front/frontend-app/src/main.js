import { createApp } from 'vue';
import App from './App.vue';
import router from './router';
import axios from 'axios';
import { Amplify, Auth } from 'aws-amplify'; // Amplify와 Auth를 함께 import합니다.
import awsmobile from '../aws-exports'; // aws-exports.js 파일 경로 확인

// Axios 전역 설정
const app = createApp(App);

// AWS Amplify 설정
Amplify.configure(awsmobile);

// Axios를 전역 속성으로 추가
app.config.globalProperties.$axios = axios;

// Axios 요청 인터셉터 설정 (JWT 토큰 자동 추가)
// 이 설정은 모든 API 요청에 로그인된 사용자의 토큰을 자동으로 포함시킵니다.
axios.interceptors.request.use(async config => {
  try {
    const session = await Auth.currentSession();
    const token = session.getIdToken().getJwtToken();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
  } catch (error) {
    console.error('No current session, skipping token attachment.');
  }
  return config;
});

app.use(router);
app.mount('#app');