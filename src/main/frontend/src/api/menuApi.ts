import { fetchJson, fetchResult } from './client'
import type { Menu, MenuForm } from '../types/menu'

/**
 * 사이드바 메뉴 목록 조회
 * @Author SeungHyeon.Kang
 * @return
 */
export const getSidebarMenus = () => fetchJson<Menu[]>('/api/menus/sidebar', undefined, '메뉴 목록 조회에 실패했습니다.')

/**
 * 메뉴관리 목록 조회
 * @Author SeungHyeon.Kang
 * @return
 */
export const getMenuMngList = () => fetchJson<Menu[]>('/api/menus', undefined, '메뉴관리 목록 조회에 실패했습니다.')

/**
 * 메뉴 상세 조회
 * @Author SeungHyeon.Kang
 * @param menuNumb
 * @param subxNumb
 * @return
 */
export const getMenuDetail = (menuNumb: string, subxNumb: string) => fetchJson<Menu>(`/api/menus/${menuNumb}/${subxNumb}`, undefined, '메뉴 상세 조회에 실패했습니다.')

/**
 * 하위메뉴 목록 조회
 * @Author SeungHyeon.Kang
 * @param menuNumb
 * @return
 */
export const getSubMenus = (menuNumb: string) => fetchJson<Menu[]>(`/api/menus/${menuNumb}/children`, undefined, '하위 메뉴 조회에 실패했습니다.')

/**
 * 메뉴 저장
 * @Author SeungHyeon.Kang
 * @param form
 * @param detail
 * @return
 */
export const saveMenuApi = (form: MenuForm, detail: boolean) => {
  const payload = {
    menuNumb: form.menuNumb || null,
    subxNumb: form.subxNumb || null,
    menuName: form.menuName.trim(),
    menuUrlx: form.menuUrlx.trim(),
    sortOrdr: Number(form.sortOrdr),
    useeYsno: form.useeYsno,
  }

  return fetchResult<Menu>(
    detail ? `/api/menus/${form.menuNumb}/${form.subxNumb}` : '/api/menus',
    { method: detail ? 'PUT' : 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload) },
    '메뉴 저장에 실패했습니다.',
  )
}

/**
 * 메뉴 삭제
 * @Author SeungHyeon.Kang
 * @param menu
 * @return
 */
export const deleteMenuApi = (menu: Pick<Menu, 'menuNumb' | 'subxNumb'>) => fetchJson<void>(`/api/menus/${menu.menuNumb}/${menu.subxNumb}`, { method: 'DELETE' }, '메뉴 삭제에 실패했습니다.')
