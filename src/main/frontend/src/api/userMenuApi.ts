import { fetchJson, fetchResult } from './client'
import { DEFAULT_USEE_YSNO } from '../constants/codes'
import type { UserMenu, UserMenuForm } from '../types/userMenu'

/** 사용자 상위 메뉴 목록 조회 */
export const getUserMenus = () => fetchJson<UserMenu[]>('/api/user-menus', undefined, '사용자 메뉴 목록 조회에 실패했습니다.')

/** 사용자 메뉴 상세 조회 */
export const getUserMenuDetail = (menuNumb: string, subxNumb: string) =>
  fetchJson<UserMenu>(`/api/user-menus/${menuNumb}/${subxNumb}`, undefined, '사용자 메뉴 상세 조회에 실패했습니다.')

/** 사용자 하위 메뉴 목록 조회 */
export const getUserSubMenus = (menuNumb: string) =>
  fetchJson<UserMenu[]>(`/api/user-menus/${menuNumb}/children`, undefined, '사용자 하위 메뉴 조회에 실패했습니다.')

/** 사용자 메뉴 저장 */
export const saveUserMenuApi = (form: UserMenuForm, detail: boolean) => {
  const payload = {
    menuNumb: form.menuNumb || null,
    subxNumb: form.subxNumb || null,
    menuName: form.menuName.trim(),
    menuUrlx: form.menuUrlx.trim(),
    sortOrdr: form.showYsno === DEFAULT_USEE_YSNO && form.sortOrdr ? Number(form.sortOrdr) : null,
    showYsno: form.showYsno,
    useeYsno: form.useeYsno,
  }

  return fetchResult<UserMenu>(
    detail ? `/api/user-menus/${form.menuNumb}/${form.subxNumb}` : '/api/user-menus',
    { method: detail ? 'PUT' : 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload) },
    '사용자 메뉴 저장에 실패했습니다.',
  )
}

/** 사용자 메뉴 삭제 */
export const deleteUserMenuApi = (menu: Pick<UserMenu, 'menuNumb' | 'subxNumb'>) =>
  fetchResult<void>(`/api/user-menus/${menu.menuNumb}/${menu.subxNumb}`, { method: 'DELETE' }, '사용자 메뉴 삭제에 실패했습니다.')
