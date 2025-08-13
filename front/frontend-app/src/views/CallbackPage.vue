<template>
    <div class="flex items-center justify-center min-h-screen bg-gray-100">
        <div class="p-8 text-center text-blue-500">
            로그인 처리 중... 잠시만 기다려 주세요.
        </div>
    </div>
</template>

<script>
import { onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import axios from 'axios';

export default {
    name: 'CallbackPage',
    setup() {
        const route = useRoute();
        const router = useRouter();

        const handleCallback = async () => {
            try {
                const code = route.query.code;
                const state = route.query.state;

                if (!code || !state) {
                    throw new Error('인증 코드 또는 상태 값이 없습니다.');
                }

                // 백엔드의 콜백 API 호출
                const response = await axios.post('/api/users/login/callback', {
                    code: code,
                    state: state,
                });

                const { accessToken, idToken, refreshToken } = response.data;

                if (accessToken && idToken) {
                    localStorage.setItem('accessToken', accessToken);
                    localStorage.setItem('idToken', idToken);
                    if (refreshToken) {
                        localStorage.setItem('refreshToken', refreshToken);
                    }

                    alert('로그인이 완료되었습니다!');
                    router.push('/');
                } else {
                    throw new Error('토큰을 받지 못했습니다.');
                }

            } catch (error) {
                console.error('로그인 콜백 처리 실패:', error);
                alert(`로그인 실패: ${error.message}`);
                router.push('/login'); // 오류 발생 시 로그인 페이지로 이동
            }
        };

        onMounted(() => {
            handleCallback();
        });

        return {};
    },
};
</script>