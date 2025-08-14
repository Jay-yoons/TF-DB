import { createApp } from 'vue'
import App from './App.vue'
import router from './router'

// [추가] 전역 디자인 시스템/유틸 CSS
import './styles/app.css'
document.body.classList.add('theme-light')


createApp(App).use(router).mount('#app')
