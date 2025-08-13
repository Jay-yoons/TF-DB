import { createRouter, createWebHistory } from 'vue-router'
import StoreList from '../pages/StoreList.vue'
import StoreDetail from '../pages/StoreDetail.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'stores', component: StoreList },
    { path: '/stores/:storeId', name: 'storeDetail', component: StoreDetail, props: true },
  ],
})

export default router
