<template>
  <div class="home">
    <img alt="Vue logo" src="../assets/logo.png">
    <h1>어서오세요!</h1>
    <p>
      이곳은 예약 목록을 관리하는 페이지입니다.
      상단 메뉴에서 '예약 목록'을 클릭하여 확인하세요.
    </p>
  </div>
</template>

<script>
export default {
  name: 'HomePage',
}
</script>

<style scoped>
.home {
  text-align: center;
  padding: 20px;
}
.home h1 {
  color: #42b983;
}
</style>