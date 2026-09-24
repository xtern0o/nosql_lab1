const TOKEN_KEY = 'kage_tokens'
const USER_KEY = 'kage_user'

export function getTokens() {
  try {
    return JSON.parse(localStorage.getItem(TOKEN_KEY))
  } catch {
    return null
  }
}

export function saveTokens(tokens) {
  localStorage.setItem(TOKEN_KEY, JSON.stringify(tokens))
}

export function clearSession() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
}

export function getStoredUser() {
  try {
    return JSON.parse(localStorage.getItem(USER_KEY))
  } catch {
    return null
  }
}

export function saveUser(user) {
  localStorage.setItem(USER_KEY, JSON.stringify(user))
}

export function decodeToken(token) {
  if (!token) return null
  try {
    const payload = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')
    return JSON.parse(decodeURIComponent(escape(atob(payload))))
  } catch {
    return null
  }
}

async function parseResponse(response) {
  if (response.status === 204) return null
  const type = response.headers.get('content-type') || ''
  const body = type.includes('json') ? await response.json() : await response.text()
  if (!response.ok) {
    const message = body?.detail || body?.message || body?.error || body || `Ошибка ${response.status}`
    throw new Error(typeof message === 'string' ? message : `Ошибка ${response.status}`)
  }
  return body
}

export async function refreshTokens() {
  const current = getTokens()
  if (!current?.refresh_token) throw new Error('Сессия завершена')
  const response = await fetch('/auth/refresh', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ refresh_token: current.refresh_token }),
  })
  const next = await parseResponse(response)
  saveTokens(next)
  return next
}

export async function api(path, options = {}, retry = true) {
  const tokens = getTokens()
  const headers = { ...options.headers }
  if (options.body && !(options.body instanceof FormData)) headers['Content-Type'] = 'application/json'
  if (tokens?.access_token) headers.Authorization = `Bearer ${tokens.access_token}`

  const response = await fetch(path, { ...options, headers })
  if (response.status === 401 && retry && tokens?.refresh_token) {
    try {
      await refreshTokens()
      return api(path, options, false)
    } catch {
      clearSession()
    }
  }
  return parseResponse(response)
}

async function beginAuth(endpoint) {
  const config = await api('/auth/config')
  const realm = import.meta.env.VITE_KEYCLOAK_REALM || 'nosql-lab1'
  const redirectUri = `${window.location.origin}/callback`
  const url = new URL(`${config.keycloak_base_url}/realms/${realm}/protocol/openid-connect/${endpoint}`)
  url.searchParams.set('client_id', config.client_id)
  url.searchParams.set('redirect_uri', redirectUri)
  url.searchParams.set('response_type', 'code')
  url.searchParams.set('scope', 'openid profile email')
  if (endpoint === 'registrations') {
    url.searchParams.set('prompt', 'login')
    url.searchParams.set('max_age', '0')
  }
  window.location.assign(url)
}

export async function beginLogin() {
  return beginAuth('auth')
}

export async function beginRegistration() {
  return beginAuth('registrations')
}

export async function beginLogout() {
  const config = await api('/auth/config')
  const tokens = getTokens()
  const realm = import.meta.env.VITE_KEYCLOAK_REALM || 'nosql-lab1'
  const url = new URL(`${config.keycloak_base_url}/realms/${realm}/protocol/openid-connect/logout`)
  url.searchParams.set('client_id', config.client_id)
  url.searchParams.set('post_logout_redirect_uri', window.location.origin)
  if (tokens?.id_token) url.searchParams.set('id_token_hint', tokens.id_token)
  clearSession()
  window.location.assign(url)
}

export async function finishLogin(code) {
  const tokens = await api('/auth/callback', {
    method: 'POST',
    body: JSON.stringify({ code, redirect_uri: `${window.location.origin}/callback` }),
  })
  saveTokens(tokens)
  return tokens
}
