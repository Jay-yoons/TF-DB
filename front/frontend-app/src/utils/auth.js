/**
 * JWT 토큰에서 사용자 ID를 추출하는 함수
 * @returns {string | null} 사용자 ID 또는 토큰이 없거나 유효하지 않으면 null
 */
export const getCurrentUserId = () => {
  try {
    const idToken = localStorage.getItem('idToken');
    if (!idToken || typeof idToken !== 'string') {
      console.error('로컬 스토리지에 유효한 idToken이 없습니다.');
      return null;
    }

    // 토큰의 페이로드 부분(두 번째 부분)을 가져옴
    const payloadBase64 = idToken.split('.')[1];

    // URL-safe Base64 디코딩을 위한 전처리
    // '-'를 '+'로, '_'를 '/'로 치환
    let base64 = payloadBase64.replace(/-/g, '+').replace(/_/g, '/');

    // Base64 문자열의 길이를 4의 배수로 맞추기 위해 '=' 패딩 추가
    while (base64.length % 4) {
      base64 += '=';
    }

    const decodedPayload = atob(base64);
    const payload = JSON.parse(decodedPayload);

    // 'cognito:username' 필드에서 사용자 ID 추출
    return payload['cognito:username'] || null;
  } catch (error) {
    console.error('JWT 토큰 디코딩 실패:', error);
    return null;
  }
};