import { fetchJson, fetchResult } from './client'
import type { AdminAuth, AdminAuthManage } from '../types/adminAuth'

/** 관리자 권한 부여 화면 데이터 조회 */
export const getAdminAuthManage = (pageNumber = 1) =>
  fetchJson<AdminAuthManage>(`/api/admin-auths?page=${pageNumber}`, undefined, '관리자 권한 정보를 불러오지 못했습니다.')

/** 관리자 권한 일괄 수정 */
export const updateAdminAuths = (admins: AdminAuth[], pageNumber: number) =>
  fetchResult<AdminAuthManage>(
    `/api/admin-auths?page=${pageNumber}`,
    {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(admins),
    },
    '관리자 권한 수정에 실패했습니다.',
  )
