const { defineConfig } = require('@vue/cli-service');

module.exports = defineConfig({
  devServer: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        pathRewrite: {
          '^/api': '' // API 주소에서 '/api'를 제거합니다.
        },
        loglevel: 'debug'
      }
    }
  }
});