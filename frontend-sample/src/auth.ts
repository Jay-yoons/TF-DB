// Simple auth header helper for attaching Cognito access token to API calls.

let tokenProvider: (() => string | null | Promise<string | null>) | null = null

/**
 * 외부(로그인 모듈)에서 액세스 토큰을 제공하는 콜백을 주입합니다.
 * 예) setAccessTokenProvider(() => auth.currentSession.getAccessToken())
 */
export function setAccessTokenProvider(provider: () => string | null | Promise<string | null>) {
  tokenProvider = provider
}

/**
 * 간단히 정적 토큰을 설정하려면 이 함수를 사용하세요.
 */
export function setStaticAccessToken(token: string | null) {
  tokenProvider = () => token
}

/**
 * Authorization 헤더를 포함한 headers 객체를 생성합니다.
 */
export async function authHeaders(extra?: Record<string, string>): Promise<Record<string, string>> {
  let token: string | null = null
  if (tokenProvider) {
    const v = tokenProvider()
    token = v instanceof Promise ? await v : v
  }
  const headers: Record<string, string> = { ...(extra || {}) }
  if (token) headers['Authorization'] = `Bearer ${token}`
  return headers
}
