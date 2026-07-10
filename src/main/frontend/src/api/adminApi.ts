import { fetchJson, getResultData } from './client'
import type { AdminSession } from '../types/admin'

/**
 * 관리자 세션 조회
 * @Author SeungHyeon.Kang
 * @return
 */
export const getAdminSession = async () => {
  const response = await fetch('/api/auth/me')
  if (response.status === 401) return null
  if (!response.ok) throw new Error(`세션 확인에 실패했습니다. (${response.status})`)
  return getResultData<AdminSession>(response)
}

/**
 * 관리자 로그인
 * @Author SeungHyeon.Kang
 * @param admnIdxx
 * @param passWord
 * @return
 */
export const loginAdmin = (admnIdxx: string, passWord: string) =>
  fetchJson<AdminSession>(
    '/api/auth/login',
    { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ admnIdxx, passWord }) },
    '아이디 또는 비밀번호가 올바르지 않습니다.',
  )

/**
 * 관리자 로그아웃
 * @Author SeungHyeon.Kang
 * @return
 */
export const logoutAdmin = async () => {
  await fetch('/api/auth/logout', { method: 'POST' })
}
