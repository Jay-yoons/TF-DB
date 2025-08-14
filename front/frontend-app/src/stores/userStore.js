import { defineStore } from 'pinia';
import axios from 'axios';
import router from '@/router'; // 라우터를 import하여 로그아웃 후 로그인 페이지로 이동시키기 위함

export const useUserStore = defineStore('user', {
  state: () => ({
    user: null,
    favorites: [],
    favoriteCount: 0,
    isAuthenticated: false,
    loading: false,
    error: null,
    // 토큰 정보를 상태에 추가
    accessToken: null,
    refreshToken: null,
    idToken: null,
  }),
  actions: {
    // 초기화 시 로컬 스토리지에서 토큰을 불러오는 액션
    initializeStore() {
        this.accessToken = localStorage.getItem('accessToken');
        this.idToken = localStorage.getItem('idToken');
        this.refreshToken = localStorage.getItem('refreshToken');
        
        if (this.accessToken) {
            this.isAuthenticated = true;
            // 토큰이 유효할 경우 사용자 정보를 가져옴 (선택적)
            // this.fetchMyInfo(); 
        }
    },

    // 내 정보 가져오기 (GET /api/users/me)
    async fetchMyInfo() {
      // 1. 토큰이 없으면 API 호출을 하지 않음
      if (!this.accessToken) {
        this.isAuthenticated = false;
        return;
      }
      
      this.loading = true;
      this.error = null;
      try {
        const response = await axios.get('/api/users/me');
        this.user = response.data;
        this.isAuthenticated = true;
      } catch (e) {
        // 2. HTTP 401(Unauthorized) 에러인 경우에만 로그아웃 처리
        if (e.response && e.response.status === 401) {
            console.log("인증 오류(401). 로그아웃 처리합니다.");
            this.logout();
        } else {
            this.error = '내 정보를 불러오는 데 실패했습니다.';
            console.error(e);
        }
      } finally {
        this.loading = false;
      }
    },
    
    // Cognito 로그인 URL 가져오기 (GET /api/users/login/url)
    async getLoginUrl() {
      try {
        const response = await axios.get('/api/users/login/url');
        return response.data.loginUrl;
      } catch (e) {
        this.error = '로그인 URL을 가져오는 데 실패했습니다.';
        return null;
      }
    },

    // Cognito 콜백 처리 및 토큰 저장 (POST /api/users/login/callback)
    async handleCognitoCallback(code, state) {
      this.loading = true;
      this.error = null;
      try {
        const response = await axios.post('/api/users/login/callback', { code, state });
        this.accessToken = response.data.accessToken;
        this.idToken = response.data.idToken;
        this.refreshToken = response.data.refreshToken;
        this.isAuthenticated = true;

        localStorage.setItem('accessToken', this.accessToken);
        localStorage.setItem('idToken', this.idToken);
        localStorage.setItem('refreshToken', this.refreshToken);
        
        await this.fetchMyInfo();

      } catch (e) {
        this.error = '로그인 처리 중 오류가 발생했습니다.';
        this.logout();
      } finally {
        this.loading = false;
      }
    },

    // 사용자 로그아웃 (POST /api/users/logout)
    async logout() {
      this.loading = true;
      try {
        // 토큰이 있을 때만 로그아웃 API 호출
        if (this.accessToken) {
            await axios.post('/api/users/logout', null);
        }
      } catch (e) {
        this.error = '로그아웃 처리 중 문제가 발생했지만, 클라이언트에서 로그아웃되었습니다.';
      } finally {
        this.user = null;
        this.isAuthenticated = false;
        this.favorites = [];
        this.favoriteCount = 0;
        this.accessToken = null;
        this.refreshToken = null;
        this.idToken = null;
        
        localStorage.removeItem('accessToken');
        localStorage.removeItem('idToken');
        localStorage.removeItem('refreshToken');
        this.loading = false;

        // 로그아웃 후 로그인 페이지로 이동
        router.push({ name: 'Login' });
      }
    },

    // =============================================================================
    // 즐겨찾기 가게 관련 API
    // =============================================================================

    // 내 즐겨찾기 가게 목록 조회 (GET /api/users/me/favorites)
    async fetchFavorites() {
      if (!this.accessToken) return;
      
      this.loading = true;
      this.error = null;
      try {
        const response = await axios.get('/api/users/me/favorites');
        this.favorites = response.data;
      } catch (e) {
        this.error = '즐겨찾기 목록을 불러오는 데 실패했습니다.';
      } finally {
        this.loading = false;
      }
    },
    
    // 내 즐겨찾기 가게 개수 조회 (GET /api/users/me/favorites/count)
    async fetchFavoriteCount() {
      if (!this.accessToken) return;
      
      this.loading = true;
      this.error = null;
      try {
        const response = await axios.get('/api/users/me/favorites/count');
        this.favoriteCount = response.data.count;
      } catch (e) {
        this.error = '즐겨찾기 개수를 불러오는 데 실패했습니다.';
      } finally {
        this.loading = false;
      }
    },
    
    // 즐겨찾기 가게 추가 (POST /api/users/me/favorites)
    async addFavorite(storeId) {
      if (!this.accessToken) return;

      try {
        await axios.post('/api/users/me/favorites', { storeId });
        await this.fetchFavorites();
        await this.fetchFavoriteCount();
      } catch (e) {
        this.error = '즐겨찾기 추가에 실패했습니다.';
      }
    },
    
    // 즐겨찾기 가게 삭제 (DELETE /api/users/me/favorites/{storeId})
    async removeFavorite(storeId) {
      if (!this.accessToken) return;
      
      try {
        await axios.delete(`/api/users/me/favorites/${storeId}`);
        await this.fetchFavorites();
        await this.fetchFavoriteCount();
      } catch (e) {
        this.error = '즐겨찾기 삭제에 실패했습니다.';
      }
    },
    
    // 특정 가게의 즐겨찾기 상태 확인 (GET /api/users/me/favorites/{storeId}/check)
    async isFavoriteStore(storeId) {
      if (!this.accessToken) return false;
      
      try {
        const response = await axios.get(`/api/users/me/favorites/${storeId}/check`);
        return response.data.isFavorite;
      } catch (e) {
        this.error = '즐겨찾기 상태 확인에 실패했습니다.';
        return false;
      }
    }
  },
});