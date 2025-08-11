import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import axios from 'axios' // axios 전역 설정

// axios를 전역 속성으로 추가
const app = createApp(App)
app.config.globalProperties.$axios = axios
app.use(router)
app.mount('#app')