<template>
  <div class="my-page">
    <div v-if="userStore.loading" class="text-blue-500">
      데이터 불러오는 중...
    </div>
    <div v-if="userStore.error" class="text-red-500">
      오류: {{ userStore.error }}
    </div>

    <div v-if="userStore.isAuthenticated">
      <section class="mb-8">
        <h2 class="text-xl font-semibold mb-2">내 정보</h2>
        <div v-if="userStore.user" class="border p-4 rounded-lg bg-white shadow">
          <p>이름: {{ userStore.user.userName }}</p>
          <p>전화번호: {{ userStore.user.phoneNumber }}</p>
          <p>위치: {{ userStore.user.userLocation }}</p>
        </div>
        <div v-else class="text-gray-500">
          내 정보를 불러오는 중입니다.
        </div>
      </section>

      <section>
        <h2 class="text-xl font-semibold mb-2">즐겨찾기 가게 목록 ({{ userStore.favoriteCount }})</h2>
        <div v-if="userStore.favorites.length" class="space-y-4">
          <div v-for="favorite in userStore.favorites" :key="favorite.favStoreId" class="flex justify-between items-center p-4 border rounded-lg bg-white shadow">
            <router-link :to="{ name: 'StoreDetail', params: { storeId: favorite.storeId } }" class="text-blue-500 hover:underline">
              {{ favorite.storeName }}
            </router-link>
            <button @click="userStore.removeFavorite(favorite.storeId)" class="text-red-500 hover:text-red-700">
              삭제
            </button>
          </div>
        </div>
        <div v-else class="text-gray-500">
          즐겨찾기한 가게가 없습니다.
        </div>
      </section>
    </div>
    <div v-else>
      <p class="text-red-500">로그인이 필요합니다.</p>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue';
import { useUserStore } from '@/stores/userStore';

const userStore = useUserStore();

onMounted(async () => {
  await userStore.fetchMyInfo();
  await userStore.fetchFavorites();
  await userStore.fetchFavoriteCount();
});
</script>

<style scoped>
/* 필요한 스타일 추가 */
.text-blue-500 {
  color: #3b82f6;
}
.hover\:underline:hover {
  text-decoration: underline;
}
</style>