import { DEFAULT_USEE_YSNO } from '../constants/codes'
import type { DetailCodeForm } from '../types/code'
import type { Menu, MenuForm } from '../types/menu'

type SortOrderItem = {
  sortOrdr: number | string | null | undefined
}

/**
 * 기존 형제 데이터와 미저장 입력 행을 기준으로 다음 정렬값을 계산한다
 *
 * @author SeungHyeon.Kang
 * @param items 정렬값을 가진 현재 형제 데이터와 미저장 입력 행
 * @return 현재 최댓값보다 1 큰 정렬 입력값
 */
export const getNextSortOrdr = (items: SortOrderItem[]): string => {
  let maxSortOrdr = 0

  // 비어 있거나 잘못된 정렬값은 제외하고 현재 형제 데이터의 최댓값을 찾는다
  for (const item of items) {
    const sortOrdr = Number(item.sortOrdr)
    // 1보다 작은 값과 숫자가 아닌 값은 다음 정렬값 계산에서 제외한다
    if (!Number.isFinite(sortOrdr) || sortOrdr < 1) {
      continue
    }

    // 현재까지 확인한 정렬값 중 가장 큰 값을 유지한다
    maxSortOrdr = Math.max(maxSortOrdr, sortOrdr)
  }

  // 기존 형제 데이터가 없으면 1을 반환하고 있으면 최댓값의 다음 번호를 반환한다
  return String(maxSortOrdr + 1)
}

/**
 * 빈 메뉴 입력 폼 생성
 * @Author SeungHyeon.Kang
 * @param parentMenuNumb
 * @return
 */
export const emptyMenuForm = (parentMenuNumb = ''): MenuForm => ({
  menuNumb: parentMenuNumb,
  subxNumb: '',
  menuName: '',
  menuUrlx: '/sadari/adm/',
  sortOrdr: '1',
  useeYsno: DEFAULT_USEE_YSNO,
})

/**
 * 빈 세부코드 입력 폼 생성
 * @Author SeungHyeon.Kang
 * @param upprCode 새 세부코드가 속할 상위 세부코드
 * @return 세부코드 등록 입력 폼
 */
export const emptyDetailForm = (upprCode = ''): DetailCodeForm => ({
  comdCode: '',
  comdName: '',
  codeExpl: '',
  upprCode,
  opt1Code: '',
  opt1Name: '',
  opt2Code: '',
  opt2Name: '',
  opt3Code: '',
  opt3Name: '',
  opt4Code: '',
  opt4Name: '',
  sortOrdr: '1',
  useeYsno: DEFAULT_USEE_YSNO,
})

/**
 * 세부코드 조회값을 입력 폼으로 변환
 * @Author SeungHyeon.Kang
 * @param code
 * @return
 */
export const toDetailCodeForm = (code: {
  comdCode: string
  comdName: string
  codeExpl: string | null
  upprCode: string | null
  opt1Code?: string | null
  opt1Name?: string | null
  opt2Code?: string | null
  opt2Name?: string | null
  opt3Code?: string | null
  opt3Name?: string | null
  opt4Code?: string | null
  opt4Name?: string | null
  sortOrdr: number | null
  useeYsno: string | null
}): DetailCodeForm => ({
  comdCode: code.comdCode,
  comdName: code.comdName,
  codeExpl: code.codeExpl ?? '',
  upprCode: code.upprCode ?? '',
  opt1Code: code.opt1Code ?? '',
  opt1Name: code.opt1Name ?? '',
  opt2Code: code.opt2Code ?? '',
  opt2Name: code.opt2Name ?? '',
  opt3Code: code.opt3Code ?? '',
  opt3Name: code.opt3Name ?? '',
  opt4Code: code.opt4Code ?? '',
  opt4Name: code.opt4Name ?? '',
  sortOrdr: String(code.sortOrdr ?? 1),
  useeYsno: code.useeYsno ?? DEFAULT_USEE_YSNO,
})

/**
 * 메뉴 조회값을 입력 폼으로 변환
 * @Author SeungHyeon.Kang
 * @param menu
 * @return
 */
export const toMenuForm = (menu: Menu): MenuForm => ({
  menuNumb: menu.menuNumb,
  subxNumb: menu.subxNumb,
  menuName: menu.menuName,
  menuUrlx: menu.menuUrlx,
  sortOrdr: String(menu.sortOrdr ?? 1),
  useeYsno: menu.useeYsno ?? DEFAULT_USEE_YSNO,
})
