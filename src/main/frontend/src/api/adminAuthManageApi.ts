import { fetchJson, fetchResult } from './client'
import type { AdminAuth, AdminAuthManage } from '../types/adminAuth'

/** 관리자 권한 부여 화면 데이터 조회 */
export const getAdminAuthManage = () =>
  fetchJson<AdminAuthManage>('/api/admin-auths', undefined, '관리자 권한 정보를 불러오지 못했습니다.')

/** 관리자 권한 일괄 수정 */
export const updateAdminAuths = (admins: AdminAuth[]) =>
  fetchResult<AdminAuthManage>(
    '/api/admin-auths',
    {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(admins),
    },
    '관리자 권한 수정에 실패했습니다.',
  )
