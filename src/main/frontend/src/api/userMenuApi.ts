import { fetchJson, fetchResult } from './client'
import { DEFAULT_USEE_YSNO } from '../constants/codes'
import type { UserMenu, UserMenuForm, UserMenuSearch } from '../types/userMenu'
import type { PageData } from '../types/common'
import { createSearchParams } from '../utils/search'

/** 사용자 메뉴 목록을 조회한다. */
export const getUserMenus = (pageNumber: number, search: UserMenuSearch) =>
  fetchJson<PageData<UserMenu>>(
    `/api/user-menus?${createSearchParams(pageNumber, search).toString()}`,
    undefined,
    '사용자 메뉴 목록 조회에 실패했습니다.',
  )

/** 사용자 메뉴 상세를 조회한다. */
export const getUserMenuDetail = (menuNumb: number) =>
  fetchJson<UserMenu>(`/api/user-menus/${menuNumb}`, undefined, '사용자 메뉴 상세 조회에 실패했습니다.')

/** 사용자 메뉴의 직계 하위 메뉴 목록을 조회한다. */
export const getUserMenuChildren = (menuNumb: number) =>
  fetchJson<UserMenu[]>(
    `/api/user-menus/${menuNumb}/children`,
    undefined,
    '하위 사용자 메뉴 조회에 실패했습니다.',
  )

/** 사용자 메뉴의 상위 메뉴 후보 목록을 조회한다. */
export const getUserMenuParents = () =>
  fetchJson<UserMenu[]>('/api/user-menus/parents', undefined, '상위 사용자 메뉴 조회에 실패했습니다.')

/** 사용자 메뉴를 저장한다. */
export const saveUserMenuApi = (form: UserMenuForm, detail: boolean) => {
  const payload = {
    parnNumb: form.parnNumb ? Number(form.parnNumb) : null,
    menuName: form.menuName.trim(),
    menuUrlx: form.menuUrlx.trim(),
    sortOrdr: form.showYsno === DEFAULT_USEE_YSNO && form.sortOrdr ? Number(form.sortOrdr) : null,
    showYsno: form.showYsno,
    useeYsno: form.useeYsno,
  }

  // 상세 화면에서는 단일 메뉴 번호로 수정하고 등록 화면에서는 신규 메뉴를 생성한다.
  return fetchResult<UserMenu>(
    detail ? `/api/user-menus/${form.menuNumb}` : '/api/user-menus',
    { method: detail ? 'PUT' : 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload) },
    '사용자 메뉴 저장에 실패했습니다.',
  )
}

/** 사용자 메뉴를 삭제한다. */
export const deleteUserMenuApi = (menuNumb: number) =>
  fetchResult<void>(`/api/user-menus/${menuNumb}`, { method: 'DELETE' }, '사용자 메뉴 삭제에 실패했습니다.')
