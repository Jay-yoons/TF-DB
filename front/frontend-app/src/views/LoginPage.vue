<template>
  <div class="p-8">
    <h1 class="text-2xl font-bold mb-4">로그인</h1>
    <div v-if="userStore.loading" class="text-blue-500">
      로그인 처리 중...
    </div>
    <div v-if="userStore.error" class="text-red-500">
      오류: {{ userStore.error }}
    </div>
    <div v-if="userStore.isAuthenticated" class="border p-4 rounded-lg bg-green-100">
      <p class="font-semibold">로그인 되었습니다!</p>
      <p>사용자 이름: {{ userStore.user?.username }}</p>
      <button @click="userStore.logout" class="mt-4 bg-red-500 text-white px-4 py-2 rounded">
        로그아웃
      </button>
    </div>
    <div v-else>
      <button @click="userStore.loginDummy" class="bg-blue-500 text-white px-4 py-2 rounded">
        더미 로그인 (개발용)
      </button>
      <a :href="loginUrl" class="ml-4 text-blue-500">
        Cognito로 로그인
      </a>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import { useUserStore } from '@/stores/userStore';
import axios from 'axios';

const userStore = useUserStore();
const loginUrl = ref('');

onMounted(async () => {
  // Cognito 로그인 URL 불러오기
  try {
    const response = await axios.get('/api/users/login/url');
    loginUrl.value = response.data.url;
  } catch (err) {
    console.error('로그인 URL을 불러오지 못했습니다.', err);
  }
});
</script>
