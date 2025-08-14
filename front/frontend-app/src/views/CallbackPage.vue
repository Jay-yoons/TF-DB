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
import { useUserStore } from '@/stores/userStore'; // userStore import

export default {
    name: 'CallbackPage',
    setup() {
        const route = useRoute();
        const router = useRouter();
        const userStore = useUserStore(); // userStore 사용

        const handleCallback = async () => {
            const code = route.query.code;
            const state = route.query.state;

            if (!code || !state) {
                console.error('인증 코드 또는 상태 값이 없습니다.');
                alert('로그인 실패: 인증 코드 또는 상태 값이 누락되었습니다.');
                router.push('/');
                return;
            }

            try {
                // userStore의 handleCognitoCallback 액션 호출
                await userStore.handleCognitoCallback(code, state);

                if (userStore.isAuthenticated) {
                    alert('로그인이 완료되었습니다!');
                    router.push('/');
                } else {
                    throw new Error('로그인에 실패했습니다.');
                }
            } catch (error) {
                console.error('로그인 콜백 처리 실패:', error);
                alert(`로그인 실패: ${error.message}`);
                router.push('/');
            }
        };

        onMounted(() => {
            handleCallback();
        });

        return {};
    },
};
</script>