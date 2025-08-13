<template>
  <div class="container mx-auto p-4 max-w-2xl">
    <div v-if="loading" class="text-center text-blue-500">
      리뷰 정보를 불러오는 중...
    </div>
    <div v-else-if="error" class="text-center text-red-500">
      {{ error }}
    </div>

    <div v-else-if="review" class="bg-white shadow-lg rounded-xl p-8">
      <h1 class="text-3xl font-bold mb-4">리뷰 상세 정보</h1>
      
      <div class="flex items-center mb-2">
        <p class="font-semibold text-lg">{{ review.userId }}</p>
        <div class="ml-auto text-yellow-400">
          <span v-for="i in review.score" :key="'star-' + i">★</span>
          <span v-for="i in (5 - review.score)" :key="'empty-star-' + i" class="text-gray-300">☆</span>
        </div>
      </div>
      <p class="text-gray-600 mb-4"><strong>가게 ID:</strong> {{ review.storeId }}</p>
      <p class="text-gray-700 leading-relaxed">{{ review.comment }}</p>

      <div v-if="isAuthor" class="flex mt-6 space-x-4">
        <button
          @click="showEditModal = true"
          class="flex-1 text-center bg-green-600 text-white font-bold py-2 px-4 rounded-lg hover:bg-green-700 transition duration-300"
        >
          수정
        </button>
        <button
          @click="deleteReview"
          class="flex-1 text-center bg-red-600 text-white font-bold py-2 px-4 rounded-lg hover:bg-red-700 transition duration-300"
        >
          삭제
        </button>
      </div>
    </div>
    
    <div v-else class="text-center text-gray-500">
      리뷰 정보를 찾을 수 없습니다.
    </div>

    <div v-if="showEditModal" class="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full flex justify-center items-center">
      <div class="bg-white p-8 rounded-lg shadow-xl max-w-lg w-full m-4">
        <h2 class="text-2xl font-bold mb-4">리뷰 수정</h2>
        <div class="mb-4">
          <label class="block text-gray-700 font-bold mb-2">내용:</label>
          <textarea v-model="editedComment" class="w-full px-3 py-2 text-gray-700 border rounded-lg focus:outline-none focus:shadow-outline" rows="4"></textarea>
        </div>
        <div class="mb-4">
          <label class="block text-gray-700 font-bold mb-2">점수 (1-5):</label>
          <input type="number" v-model.number="editedScore" min="1" max="5" class="w-full px-3 py-2 text-gray-700 border rounded-lg focus:outline-none focus:shadow-outline">
        </div>
        <div class="flex justify-end space-x-4">
          <button @click="showEditModal = false" class="bg-gray-400 text-white font-bold py-2 px-4 rounded-lg hover:bg-gray-500 transition duration-300">
            취소
          </button>
          <button @click="updateReview" class="bg-green-600 text-white font-bold py-2 px-4 rounded-lg hover:bg-green-700 transition duration-300">
            수정 완료
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import axios from 'axios';
import { getCurrentUserId } from '@/utils/auth'; // JWT 토큰 파싱 유틸리티 함수 임포트

export default {
  name: 'ReviewDetail',
  setup() {
    const route = useRoute();
    const router = useRouter();
    const review = ref(null);
    const loading = ref(true);
    const error = ref(null);
    const showEditModal = ref(false);
    const editedComment = ref('');
    const editedScore = ref(0);

    const currentUserId = getCurrentUserId(); // 현재 로그인한 사용자의 ID를 가져옴

    // 현재 사용자가 리뷰 작성자인지 확인
    const isAuthor = computed(() => {
      return review.value && review.value.userId === currentUserId;
    });

    const fetchReviewDetail = async () => {
      try {
        const reviewId = route.params.id;
        const response = await axios.get(`/api/reviews/${reviewId}`);
        review.value = response.data;
        editedComment.value = review.value.comment;
        editedScore.value = review.value.score;
      } catch (e) {
        error.value = `리뷰 상세 정보를 불러오는 데 실패했습니다: ${e.message}`;
      } finally {
        loading.value = false;
      }
    };

    const updateReview = async () => {
      try {
        const reviewId = review.value.reviewId;
        const updatedReviewData = {
          comment: editedComment.value,
          score: editedScore.value,
          userId: review.value.userId,
          storeId: review.value.storeId,
        };
        
        await axios.put(`/api/reviews/${reviewId}`, updatedReviewData);
        alert('리뷰가 성공적으로 수정되었습니다.');
        
        showEditModal.value = false;
        await fetchReviewDetail(); 
      } catch (e) {
        console.error('리뷰 수정 실패:', e);
        alert(`리뷰 수정에 실패했습니다: ${e.response?.data?.message || e.message}`);
      }
    };

    const deleteReview = async () => {
      if (confirm('정말로 이 리뷰를 삭제하시겠습니까?')) {
        try {
          const reviewId = review.value.reviewId;
          // 백엔드 API에 맞게 userId를 쿼리 파라미터로 전달
          await axios.delete(`/api/reviews/${reviewId}?userId=${currentUserId}`);
          
          alert('리뷰가 성공적으로 삭제되었습니다.');
          
          router.push({ name: 'ReviewList', params: { storeId: review.value.storeId } });
        } catch (e) {
          console.error('리뷰 삭제 실패:', e);
          alert(`리뷰 삭제에 실패했습니다: ${e.message}`);
        }
      }
    };

    onMounted(() => {
      fetchReviewDetail();
    });

    return {
      review,
      loading,
      error,
      showEditModal,
      editedComment,
      editedScore,
      isAuthor,
      updateReview,
      deleteReview,
    };
  },
};
</script>