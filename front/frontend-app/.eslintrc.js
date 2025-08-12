module.exports = {
  // eslint-plugin-vue 플러그인을 활성화합니다.
  extends: [
    'eslint:recommended',
    'plugin:vue/vue3-recommended',
    'prettier'
  ],
  parserOptions: {
    // Vue 템플릿과 스크립트 블록을 올바르게 파싱하도록 설정합니다.
    parser: '@babel/eslint-parser',
    sourceType: 'module',
    requireConfigFile: false,
  },
  // 전역 변수를 추가하여 ESLint가 google 객체를 인식하도록 합니다.
  globals: {
    google: "readonly",
  },
  rules: {
    // 기타 ESLint 규칙
  },
};
