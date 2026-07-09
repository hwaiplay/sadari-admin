import { fetchJson, fetchResult } from './client'
import type { Permission } from '../types/common'
import type { Menu, MenuForm } from '../types/menu'

export const getSidebarMenus = () => fetchJson<Menu[]>('/api/menus/sidebar', undefined, '메뉴 목록 조회에 실패했습니다.')

export const getPermission = (menuUrlx: string) => fetchJson<Permission>(`/api/menu-permissions?menuUrlx=${encodeURIComponent(menuUrlx)}`, undefined, '권한 조회에 실패했습니다.')

export const getMenuMngList = () => fetchJson<Menu[]>('/api/menus', undefined, '메뉴관리 목록 조회에 실패했습니다.')

export const getMenuDetail = (menuNumb: string, subxNumb: string) => fetchJson<Menu>(`/api/menus/${menuNumb}/${subxNumb}`, undefined, '메뉴 상세 조회에 실패했습니다.')

export const getSubMenus = (menuNumb: string) => fetchJson<Menu[]>(`/api/menus/${menuNumb}/children`, undefined, '하위 메뉴 조회에 실패했습니다.')

export const saveMenuApi = (form: MenuForm, detail: boolean) => {
  const payload = {
    menuNumb: form.menuNumb || null,
    subxNumb: form.subxNumb || null,
    menuName: form.menuName.trim(),
    menuUrlx: form.menuUrlx.trim(),
    readAuth: form.readAuth,
    writAuth: form.writAuth,
    deltAuth: form.deltAuth,
    sortOrdr: Number(form.sortOrdr),
    useeYsno: form.useeYsno,
  }

  return fetchResult<Menu>(
    detail ? `/api/menus/${form.menuNumb}/${form.subxNumb}` : '/api/menus',
    { method: detail ? 'PUT' : 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload) },
    '메뉴 저장에 실패했습니다.',
  )
}

export const deleteMenuApi = (menu: Pick<Menu, 'menuNumb' | 'subxNumb'>) => fetchJson<void>(`/api/menus/${menu.menuNumb}/${menu.subxNumb}`, { method: 'DELETE' }, '메뉴 삭제에 실패했습니다.')

