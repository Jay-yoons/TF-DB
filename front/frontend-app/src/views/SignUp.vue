<template>
  <div class="flex items-center justify-center min-h-screen bg-gray-100">
    <div class="w-full max-w-md p-8 space-y-8 bg-white rounded-xl shadow-lg">
      <h2 class="text-3xl font-bold text-center">회원가입</h2>
      <form @submit.prevent="signup" class="space-y-6">
        <div>
          <label for="userId" class="block text-sm font-medium text-gray-700">아이디</label>
          <input
            id="userId"
            v-model="userId"
            type="text"
            required
            class="w-full px-4 py-2 mt-1 border border-gray-300 rounded-md focus:ring-blue-500 focus:border-blue-500"
          >
        </div>
        <div>
          <label for="userName" class="block text-sm font-medium text-gray-700">이름</label>
          <input
            id="userName"
            v-model="userName"
            type="text"
            required
            class="w-full px-4 py-2 mt-1 border border-gray-300 rounded-md focus:ring-blue-500 focus:border-blue-500"
          >
        </div>
        <div>
          <label for="phoneNumber" class="block text-sm font-medium text-gray-700">전화번호</label>
          <input
            id="phoneNumber"
            v-model="phoneNumber"
            type="tel"
            required
            class="w-full px-4 py-2 mt-1 border border-gray-300 rounded-md focus:ring-blue-500 focus:border-blue-500"
          >
        </div>
        <div>
          <label for="password" class="block text-sm font-medium text-gray-700">비밀번호</label>
          <input
            id="password"
            v-model="password"
            type="password"
            required
            class="w-full px-4 py-2 mt-1 border border-gray-300 rounded-md focus:ring-blue-500 focus:border-blue-500"
          >
        </div>
        <div>
          <button
            type="submit"
            class="w-full py-2 px-4 text-white bg-blue-600 rounded-md hover:bg-blue-700 transition-colors"
          >
            회원가입
          </button>
        </div>
      </form>
      <div class="text-center text-sm">
        <p>이미 계정이 있으신가요?
          <router-link to="/login" class="font-medium text-blue-600 hover:text-blue-500">
            로그인
          </router-link>
        </p>
      </div>
    </div>
  </div>
</template>

<script>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { Auth } from 'aws-amplify';
import axios from 'axios';

export default {
  setup() {
    const router = useRouter();
    const userId = ref('');
    const userName = ref('');
    const phoneNumber = ref('');
    const password = ref('');

    const signup = async () => {
      try {
        // 1. Cognito에 사용자 계정 생성
        await Auth.signUp({
          username: userId.value,
          password: password.value,
          attributes: {
            name: userName.value,
            phone_number: phoneNumber.value
          }
        });
        
        // 2. 백엔드 DB에 사용자 정보 저장
        const signupRequest = {
            userId: userId.value,
            userName: userName.value,
            phoneNumber: phoneNumber.value,
            userLocation: '' // 주소지 정보는 여기서 임시로 빈값
        };
        await axios.post('/api/users/signup', signupRequest);

        alert('회원가입이 완료되었습니다! 로그인 페이지로 이동합니다.');
        router.push('/login');
      } catch (error) {
        console.error('회원가입 실패:', error);
        alert(`회원가입 실패: ${error.message}`);
      }
    };
    
    return {
      userId,
      userName,
      phoneNumber,
      password,
      signup,
    };
  },
};
</script>