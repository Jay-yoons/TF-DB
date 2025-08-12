const { defineConfig } = require('@vue/cli-service');

module.exports = defineConfig({
  publicPath: '/',
  devServer: {
    proxy: {
      // 스토어 서비스 관련 API 요청을 localhost:8081로 프록시
      // 이 규칙이 더 구체적이므로 먼저 위치해야 합니다.
      '/api/stores': {
        target: 'http://localhost:8081',
        changeOrigin: true,
        // pathRewrite: {
        //   '^/api/stores': '/stores'
        // },
        // 요청 로그를 보려면 주석을 해제하세요
        loglevel: 'debug'
      },
      // 나머지 모든 /api 요청을 localhost:8080으로 프록시
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        pathRewrite: {
          '^/api': ''
        },
        loglevel: 'debug'
      }
    }
  }
});
