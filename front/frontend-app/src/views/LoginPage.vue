<template>
    <div class="flex items-center justify-center min-h-screen bg-gray-100">
        <div class="w-full max-w-md p-8 space-y-8 bg-white rounded-xl shadow-lg">
            <h2 class="text-3xl font-bold text-center">로그인</h2>
            <form @submit.prevent="login" class="space-y-6">
                <div>
                    <label for="userId" class="block text-sm font-medium text-gray-700">아이디</label>
                    <input id="userId" v-model="userId" type="text" required
                        class="w-full px-4 py-2 mt-1 border border-gray-300 rounded-md focus:ring-blue-500 focus:border-blue-500">
                </div>
                <div>
                    <label for="password" class="block text-sm font-medium text-gray-700">비밀번호</label>
                    <input id="password" v-model="password" type="password" required
                        class="w-full px-4 py-2 mt-1 border border-gray-300 rounded-md focus:ring-blue-500 focus:border-blue-500">
                </div>
                <div>
                    <button type="submit"
                        class="w-full py-2 px-4 text-white bg-blue-600 rounded-md hover:bg-blue-700 transition-colors">
                        로그인
                    </button>
                </div>
            </form>
            <div class="text-center text-sm">
                <p>계정이 없으신가요?
                    <router-link to="/signup" class="font-medium text-blue-600 hover:text-blue-500">
                        회원가입
                    </router-link>
                </p>
            </div>
        </div>
    </div>
</template>

<script>
import { Auth } from 'aws-amplify';

export default {
  methods: {
    async login() {
      try {
        const user = await Auth.signIn(this.userId, this.password);
        console.log(user);
        // 로그인 성공 후 홈 페이지로 이동
      } catch (error) {
        console.error('로그인 실패:', error);
      }
    }
  }
}
</script>