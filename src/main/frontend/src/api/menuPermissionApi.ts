import { fetchJson } from './client'
import type { MenuPermission } from '../types/permission'

/** 로그인 관리자의 메뉴 권한 조회 */
export const getMenuPermission = (menuUrlx: string) =>
  fetchJson<MenuPermission>(
    `/api/menu-permissions?menuUrlx=${encodeURIComponent(menuUrlx)}`,
    undefined,
    '메뉴 권한 조회에 실패했습니다.',
  )
