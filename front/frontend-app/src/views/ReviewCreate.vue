<template>
  <div class="review-create">
    <h1>리뷰 작성</h1>
    <div class="review-form">
      <form @submit.prevent="submitReview">
        <div class="form-group">
          <label>별점 (1-5)</label>
          <div class="rating">
            <span v-for="n in 5" :key="n" @click="score = n" :class="{ 'filled-star': n <= score }">★</span>
          </div>
        </div>
        <div class="form-group">
          <label for="comment">리뷰 내용</label>
          <textarea id="comment" v-model="comment" rows="5" required></textarea>
        </div>
        <button type="submit" class="submit-button">리뷰 제출</button>
      </form>
    </div>
  </div>
</template>

<script>
import { ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import axios from 'axios';
import { getCurrentUserId } from '@/utils/auth';

export default {
  name: 'ReviewCreate',
  setup() {
    const route = useRoute();
    const router = useRouter();
    const score = ref(0);
    const comment = ref('');

    const submitReview = async () => {
      const accessToken = localStorage.getItem('accessToken');
      const userId = getCurrentUserId();
      
      if (!userId || !accessToken) {
        alert('리뷰를 작성하려면 로그인이 필요합니다.');
        return;
      }
      if (score.value === 0) {
        alert('별점을 선택해주세요.');
        return;
      }
      
      const reviewRequestDto = {
        storeId: route.params.storeId,
        userId: userId,
        comment: comment.value,
        score: score.value,
      };

      try {
        const headers = { Authorization: `Bearer ${accessToken}` };
        const response = await axios.post('/api/reviews', reviewRequestDto, { headers });
        
        alert('리뷰가 성공적으로 작성되었습니다.');
        
        // 리뷰 작성 완료 후, 리뷰 상세 페이지로 이동
        router.push({ name: 'ReviewDetail', params: { id: response.data.reviewId } });

      } catch (error) {
        console.error('리뷰 작성 실패:', error);
        alert(`리뷰 작성에 실패했습니다: ${error.message}`);
      }
    };

    return {
      score,
      comment,
      submitReview,
    };
  },
};
</script>

<style scoped>
.review-create {
  max-width: 600px;
  margin: 0 auto;
  text-align: center;
}
.review-form {
  margin-top: 20px;
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}
.form-group {
  margin-bottom: 20px;
  text-align: left;
}
.form-group label {
  display: block;
  font-weight: bold;
  margin-bottom: 5px;
}
.rating {
  font-size: 2em;
  cursor: pointer;
  color: #ccc;
}
.rating .filled-star {
  color: #FFD700;
}
textarea {
  width: 100%;
  padding: 10px;
  border: 1px solid #ccc;
  border-radius: 4px;
}
.submit-button {
  padding: 10px 20px;
  background-color: #4CAF50;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-size: 1em;
}
</style>