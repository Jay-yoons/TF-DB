<template>
  <div class="p-8">
    <h1 class="text-2xl font-bold mb-4">마이페이지</h1>
    
    <div v-if="userStore.loading" class="text-blue-500">
      데이터 불러오는 중...
    </div>
    <div v-if="userStore.error" class="text-red-500">
      오류: {{ userStore.error }}
    </div>

    <div v-if="userStore.isAuthenticated">
      <!-- 내 정보 섹션 -->
      <section class="mb-8">
        <h2 class="text-xl font-semibold mb-2">내 정보</h2>
        <div v-if="userStore.user" class="border p-4 rounded-lg bg-white shadow">
          <p>이름: {{ userStore.user.name }}</p>
          <p>이메일: {{ userStore.user.email }}</p>
          <p>전화번호: {{ userStore.user.phone }}</p>
        </div>
        <div v-else class="text-gray-500">
          내 정보를 불러오는 중입니다.
        </div>
      </section>

      <!-- 즐겨찾기 섹션 -->
      <section>
        <h2 class="text-xl font-semibold mb-2">즐겨찾기 가게 목록 ({{ userStore.favoriteCount }})</h2>
        <div v-if="userStore.favorites.length" class="space-y-4">
          <div v-for="favorite in userStore.favorites" :key="favorite.id" class="flex justify-between items-center p-4 border rounded-lg bg-white shadow">
            <p>{{ favorite.storeName }}</p>
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
  // 컴포넌트가 마운트될 때 데이터 불러오기
  await userStore.fetchMyInfo();
  await userStore.fetchFavorites();
  await userStore.fetchFavoriteCount();
});
</script>
