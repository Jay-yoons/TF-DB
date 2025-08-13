<template>
  <div class="container mx-auto p-4 max-w-2xl">
    <div v-if="loading" class="text-center text-blue-500">
      리뷰를 불러오는 중...
    </div>
    <div v-else-if="error" class="text-center text-red-500">
      {{ error }}
    </div>
    <div v-else-if="reviews.length > 0">
      <h1 class="text-3xl font-bold mb-6">리뷰 목록</h1>
      <router-link
        v-for="review in reviews"
        :key="review.reviewId"
        :to="{ name: 'ReviewDetail', params: { id: review.reviewId } }"
      >
        <div class="bg-white shadow-lg rounded-xl p-6 mb-4 cursor-pointer hover:bg-gray-100 transition duration-300">
          <div class="flex items-center mb-2">
            <p class="font-semibold text-lg">{{ review.userId }}</p>
            <div class="ml-auto text-yellow-400">
              <span v-for="i in review.score" :key="'star-' + review.reviewId + '-' + i">★</span>
              <span v-for="i in (5 - review.score)" :key="'empty-star-' + review.reviewId + '-' + i" class="text-gray-300">☆</span>
            </div>
          </div>
          <p class="text-gray-700">{{ review.comment }}</p>
        </div>
      </router-link>
    </div>
    <div v-else class="text-center text-gray-500">
      아직 작성된 리뷰가 없습니다.
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import axios from 'axios';

export default {
  name: 'ReviewList',
  setup() {
    const route = useRoute();
    const reviews = ref([]);
    const loading = ref(true);
    const error = ref(null);

    const fetchStoreReviews = async () => {
      try {
        const storeId = route.params.storeId;
        const response = await axios.get(`/api/reviews/stores/${storeId}`);
        reviews.value = response.data;
      } catch (e) {
        error.value = `리뷰를 불러오는 데 실패했습니다: ${e.message}`;
      } finally {
        loading.value = false;
      }
    };

    onMounted(() => {
      fetchStoreReviews();
    });

    return {
      reviews,
      loading,
      error,
    };
  },
};
</script>

<style scoped>
/* Tailwind CSS 사용 */
</style>