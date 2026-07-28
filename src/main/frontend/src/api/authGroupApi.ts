import { fetchJson, fetchResult } from './client'
import type { AuthGroup } from '../types/authGroup'
import type { PageData } from '../types/common'

/** 권한그룹 목록 조회 */
export const getAuthGroups = (pageNumber = 1) =>
  fetchJson<PageData<AuthGroup>>(`/api/auth-groups?page=${pageNumber}`, undefined, '권한그룹 목록 조회에 실패했습니다.')

/** 권한그룹 상세 조회 */
export const getAuthGroup = (authCode: string) =>
  fetchJson<AuthGroup>(`/api/auth-groups/${encodeURIComponent(authCode)}`, undefined, '권한그룹 상세 조회에 실패했습니다.')

/** 권한 코드 중복 확인 */
export const checkAuthGroupDuplicate = (authCode: string) =>
  fetchJson<boolean>(`/api/auth-groups/${encodeURIComponent(authCode)}/duplicate`, undefined, '권한 코드 중복 확인에 실패했습니다.')

/** 권한그룹 저장 */
export const saveAuthGroup = (authGroup: AuthGroup, detail: boolean) =>
  fetchResult<AuthGroup>(
    detail ? `/api/auth-groups/${encodeURIComponent(authGroup.authCode)}` : '/api/auth-groups',
    {
      method: detail ? 'PUT' : 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(authGroup),
    },
    '권한그룹 저장에 실패했습니다.',
  )

/** 권한그룹 삭제 */
export const deleteAuthGroup = (authCode: string) =>
  fetchResult<void>(
    `/api/auth-groups/${encodeURIComponent(authCode)}`,
    { method: 'DELETE' },
    '권한그룹 삭제에 실패했습니다.',
  )
