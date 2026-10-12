import { fetchJson, fetchResult } from './client'
import type { Menu, MenuForm, MenuSearch } from '../types/menu'
import type { PageData } from '../types/common'
import { createSearchParams } from '../utils/search'

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
export const getMenuMngList = (pageNumber: number, search: MenuSearch) =>
  fetchJson<PageData<Menu>>(
    `/api/menus?${createSearchParams(pageNumber, search).toString()}`,
    undefined,
    '메뉴관리 목록 조회에 실패했습니다.',
  )

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
 * 메뉴 입력값을 서버 저장 형식으로 변환한다
 *
 * @author SeungHyeon.Kang
 * @param form 변환할 메뉴 입력값
 * @return 서버에 전달할 메뉴 데이터
 */
const toMenuPayload = (form: MenuForm) => {
  // 공백과 숫자 입력값을 서버 계약에 맞게 정규화하여 반환한다
  return {
    menuNumb: form.menuNumb || null,
    subxNumb: form.subxNumb || null,
    menuName: form.menuName.trim(),
    menuUrlx: form.menuUrlx.trim(),
    sortOrdr: Number(form.sortOrdr),
    useeYsno: form.useeYsno,
  }
}

/**
 * 기준 메뉴와 기존 및 신규 하위 메뉴를 한 요청으로 저장한다
 *
 * @author SeungHyeon.Kang
 * @param menu 저장할 기준 메뉴
 * @param subMenus 수정할 기존 하위 메뉴
 * @param childMenus 등록할 신규 하위 메뉴
 * @return 메뉴 상세 전체 저장 결과
 * @throws 메뉴 상세 저장 요청이 실패할 때 발생한다
 */
export const saveMenuBatch = (menu: MenuForm, subMenus: Menu[], childMenus: MenuForm[]) => {
  // 한 트랜잭션으로 처리할 메뉴 상세 전체 요청을 반환한다
  return fetchResult<Menu>(
    '/api/menus/batch',
    {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        menu: toMenuPayload(menu),
        subMenus: subMenus.map((subMenu) => toMenuPayload({
          ...subMenu,
          sortOrdr: String(subMenu.sortOrdr ?? 1),
          useeYsno: subMenu.useeYsno ?? 'Y',
        })),
        childMenus: childMenus.map(toMenuPayload),
      }),
    },
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
