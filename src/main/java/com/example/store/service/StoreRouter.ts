import { createRouter, createWebHistory } from 'vue-router'
import StoreList from '../views/StoreList.vue'
import StoreDetail from '../views/StoreDetail.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'stores', component: StoreList },
    { path: '/stores/:storeId', name: 'storeDetail', component: StoreDetail, props: true },
  ],
})

export default router
