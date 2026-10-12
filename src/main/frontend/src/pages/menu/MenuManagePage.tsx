import { useEffect, useEffectEvent, useMemo, useState } from 'react'
import type { FormEvent } from 'react'
import { getCodeList } from '../../api/codeApi'
import {
  deleteMenuApi,
  getMenuDetail,
  getMenuMngList,
  getSubMenus,
  saveMenuBatch,
} from '../../api/menuApi'
import { COMM_YSNO } from '../../constants/codes'
import { MENU_DETAIL_PREFIX, MENU_LIST_PATH, MENU_NEW_PATH } from '../../constants/routes'
import type { Code } from '../../types/code'
import type { PageData } from '../../types/common'
import type { Menu, MenuForm, MenuSearch } from '../../types/menu'
import { emptyMenuForm, getNextSortOrdr, toMenuForm } from '../../utils/forms'
import { getListPageSnapshot, setListPageSnapshot } from '../../utils/search'
import { MenuDetailPage } from './MenuDetailPage'
import { MenuListPage } from './MenuListPage'

type MenuManagePageProps = {
  currentPath: string
  activeMenuName: string
  onMovePath: (path: string) => void
  onError: (message: string | null) => void
  onSidebarRefresh: () => Promise<unknown>
}

const EMPTY_PAGE: PageData<Menu> = {
  items: [], totalCount: 0, pageNumber: 1, pageSize: 20, totalPages: 0,
}

const DEFAULT_SEARCH: MenuSearch = { keyword: '', useeYsno: '' }

/**
 * 메뉴 목록과 상세 및 하위 메뉴 변경 상태를 관리한다
 *
 * @author SeungHyeon.Kang
 * @param props 현재 경로와 메뉴 이동 및 공통 오류 처리 동작
 * @return 현재 경로에 해당하는 메뉴 관리 화면
 */
export function MenuManagePage({
  currentPath,
  activeMenuName,
  onMovePath,
  onError,
  onSidebarRefresh,
}: MenuManagePageProps) {
  const [menuRows, setMenuRows] = useState<Menu[]>([])
  const [pageData, setPageData] = useState<PageData<Menu>>(EMPTY_PAGE)
  const [menuDetail, setMenuDetail] = useState<Menu | null>(null)
  const [subMenus, setSubMenus] = useState<Menu[]>([])
  const [menuForm, setMenuForm] = useState<MenuForm>(emptyMenuForm())
  const [childForms, setChildForms] = useState<MenuForm[]>([])
  const [useeYsnoCodes, setUseeYsnoCodes] = useState<Code[]>([])
  const [saving, setSaving] = useState(false)

  const detailKey = useMemo(() => {
    // 메뉴 상세 경로가 아니면 복합키를 반환하지 않는다
    if (!currentPath.startsWith(MENU_DETAIL_PREFIX)) {
      // 목록과 등록 화면에서는 상세 조회 키가 없음을 반환한다
      return null
    }

    const [, , , , , menuNumb, subxNumb] = currentPath.split('/')
    // 상위와 하위 메뉴 번호가 모두 있을 때만 상세 조회 키를 반환한다
    return menuNumb && subxNumb ? { menuNumb, subxNumb } : null
  }, [currentPath])

  const parentMenuNumb = useMemo(() => {
    // 메뉴 등록 경로가 아니면 상위 메뉴 번호를 반환하지 않는다
    if (!currentPath.startsWith(MENU_NEW_PATH)) {
      // 최상위 메뉴 등록으로 사용할 빈 번호를 반환한다
      return ''
    }

    const [, , , , , menuNumb] = currentPath.split('/')
    // 하위 메뉴 등록 경로에 포함된 상위 메뉴 번호를 반환한다
    return menuNumb ?? ''
  }, [currentPath])

  const isListPage = currentPath === MENU_LIST_PATH
  const isNewPage = currentPath === MENU_NEW_PATH || currentPath.startsWith(`${MENU_NEW_PATH}/`)
  const isDetailPage = detailKey !== null

  /**
   * 사용여부 공통코드를 조회한다
   *
   * @author SeungHyeon.Kang
   * @return 조회된 사용여부 공통코드
   */
  const loadUseeYsnoCodes = async (): Promise<Code[]> => {
    // 메뉴 입력과 목록에 사용할 사용여부 공통코드를 조회한다
    const codes = await getCodeList(COMM_YSNO)
    // 조회한 사용여부 코드를 현재 메뉴 화면에 반영한다
    setUseeYsnoCodes(codes)
    // 조회된 사용여부 공통코드를 반환한다
    return codes
  }

  /**
   * 메뉴 목록을 마지막 적용 검색 조건으로 조회한다
   *
   * @author SeungHyeon.Kang
   * @param pageNumber 조회할 페이지 번호
   * @param search 적용할 메뉴 검색 조건
   * @return 목록 조회가 끝나면 완료되는 Promise
   */
  const openList = async (pageNumber = 1, search = DEFAULT_SEARCH): Promise<void> => {
    // 이전 메뉴 화면 오류가 새 조회 결과와 함께 표시되지 않도록 초기화한다
    onError(null)
    // 목록 이동 시 상세 전용 신규 입력 행을 비운다
    setChildForms([])
    // 목록 이동 시 이전 상세 데이터를 비운다
    setMenuDetail(null)
    // 메뉴 목록과 사용여부 공통코드를 함께 조회한다
    const [result] = await Promise.all([getMenuMngList(pageNumber, search), loadUseeYsnoCodes()])
    // 조회한 페이지 정보를 목록 화면에 반영한다
    setPageData(result)
    // 조회한 메뉴 행을 목록 화면에 반영한다
    setMenuRows(result.items)
    // 상세 화면에서 돌아올 때 같은 페이지와 검색 조건을 복원하도록 저장한다
    setListPageSnapshot(MENU_LIST_PATH, result.pageNumber, search)
  }

  /**
   * 메뉴 등록 화면의 빈 입력값과 공통코드를 준비한다
   *
   * @author SeungHyeon.Kang
   * @param parentNumb 하위 메뉴 등록 시 사용할 상위 메뉴 번호
   * @return 등록 화면 준비가 끝나면 완료되는 Promise
   */
  const openNew = async (parentNumb: string): Promise<void> => {
    // 이전 메뉴 화면 오류가 등록 화면에 남지 않도록 초기화한다
    onError(null)
    // 등록 화면의 사용여부 선택값을 조회한다
    await loadUseeYsnoCodes()
    // 등록 화면에서 이전 상세 데이터를 비운다
    setMenuDetail(null)
    // 등록 화면에서 기존 하위 메뉴 목록을 비운다
    setSubMenus([])
    // 등록 화면에서 신규 하위 메뉴 입력 행을 비운다
    setChildForms([])
    // 상위 메뉴 관계가 반영된 빈 입력값을 설정한다
    setMenuForm(emptyMenuForm(parentNumb))
  }

  /**
   * 메뉴 상세와 하위 메뉴 및 공통코드를 조회한다
   *
   * @author SeungHyeon.Kang
   * @param menuNumb 조회할 상위 메뉴 번호
   * @param subxNumb 조회할 하위 메뉴 번호
   * @return 상세 조회가 끝나면 완료되는 Promise
   */
  const openDetail = async (menuNumb: string, subxNumb: string): Promise<void> => {
    // 이전 메뉴 화면 오류가 새 상세 결과와 함께 표시되지 않도록 초기화한다
    onError(null)
    // 기준 메뉴와 하위 메뉴 및 사용여부 공통코드를 함께 조회한다
    const [detail, children] = await Promise.all([
      getMenuDetail(menuNumb, subxNumb),
      getSubMenus(menuNumb),
      loadUseeYsnoCodes(),
    ])
    // 조회한 기준 메뉴를 감사정보와 편집 화면에 반영한다
    setMenuDetail(detail)
    // 조회한 기준 메뉴를 편집 입력값으로 변환하여 반영한다
    setMenuForm(toMenuForm(detail))
    // 조회한 기존 하위 메뉴를 편집 목록에 반영한다
    setSubMenus(children)
    // 새 상세 조회에서는 이전 신규 입력 행을 비운다
    setChildForms([])
  }

  const loadCurrentMenuPage = useEffectEvent(() => {
    // 목록 경로에서는 마지막으로 성공한 페이지와 검색 조건을 복원한다
    if (isListPage) {
      const snapshot = getListPageSnapshot(MENU_LIST_PATH, DEFAULT_SEARCH)
      // 복원한 메뉴 목록 요청의 실패를 공통 오류 영역에 표시한다
      void openList(snapshot.pageNumber, snapshot.search).catch((error: unknown) => {
        onError(error instanceof Error ? error.message : '메뉴 목록을 조회하지 못했습니다.')
      })
      // 목록 화면 조회 분기를 마친다
      return
    }

    // 등록 경로에서는 상위 메뉴 관계가 반영된 빈 입력값을 준비한다
    if (isNewPage) {
      // 메뉴 등록 화면 준비 실패를 공통 오류 영역에 표시한다
      void openNew(parentMenuNumb).catch((error: unknown) => {
        onError(error instanceof Error ? error.message : '메뉴 등록 화면을 준비하지 못했습니다.')
      })
      // 등록 화면 준비 분기를 마친다
      return
    }

    // 상세 복합키가 있으면 선택한 메뉴와 하위 메뉴를 조회한다
    if (detailKey) {
      // 메뉴 상세 조회 실패를 공통 오류 영역에 표시한다
      void openDetail(detailKey.menuNumb, detailKey.subxNumb).catch((error: unknown) => {
        onError(error instanceof Error ? error.message : '메뉴 상세를 조회하지 못했습니다.')
      })
    }
  })

  useEffect(() => {
    const timer = window.setTimeout(loadCurrentMenuPage, 0)
    // 메뉴 경로가 바뀌면 이전 화면 조회 예약을 취소한다
    return () => window.clearTimeout(timer)
  }, [currentPath])

  /**
   * 메뉴 목록 조회 요청의 실패를 공통 오류 영역에 표시한다
   *
   * @author SeungHyeon.Kang
   * @param pageNumber 조회할 페이지 번호
   * @param search 적용할 메뉴 검색 조건
   * @return 반환값이 없다
   */
  const handleSearch = (pageNumber: number, search: MenuSearch): void => {
    // 목록 화면 검색과 페이지 이동의 실패를 공통 오류 영역에 격리한다
    void openList(pageNumber, search).catch((error: unknown) => {
      onError(error instanceof Error ? error.message : '메뉴 목록을 조회하지 못했습니다.')
    })
  }

  /**
   * 메뉴를 삭제하고 현재 목록 또는 상세 화면을 갱신한다
   *
   * @author SeungHyeon.Kang
   * @param menu 삭제할 메뉴 식별정보
   * @param search 삭제 후 유지할 메뉴 검색 조건
   * @return 삭제 처리가 끝나면 완료되는 Promise
   */
  const deleteMenu = async (
    menu: Pick<Menu, 'menuNumb' | 'subxNumb'>,
    search = DEFAULT_SEARCH,
  ): Promise<void> => {
    // 선택한 메뉴와 권한 연결을 삭제한다
    await deleteMenuApi(menu)
    // 삭제 결과를 사이드바 메뉴에 반영한다
    await onSidebarRefresh()
    // 현재 보고 있는 기준 메뉴가 삭제됐으면 목록으로 이동한다
    if (isDetailPage && menu.menuNumb === menuForm.menuNumb && menu.subxNumb === menuForm.subxNumb) {
      // 삭제된 상세 대신 메뉴 목록으로 이동한다
      onMovePath(MENU_LIST_PATH)
    }

    // 상세의 하위 메뉴가 삭제됐으면 현재 기준 메뉴를 다시 조회한다
    else if (isDetailPage) {
      // 남은 하위 메뉴와 기준 메뉴 정보를 갱신한다
      await openDetail(menuForm.menuNumb, menuForm.subxNumb)
    }

    // 목록에서 삭제했으면 같은 페이지와 검색 조건을 유지한다
    else {
      // 삭제 후 목록을 현재 조회 상태로 갱신한다
      await openList(pageData.pageNumber, search)
    }
  }

  /**
   * 기준 메뉴와 기존 및 신규 하위 메뉴를 한 요청으로 저장한다
   *
   * @author SeungHyeon.Kang
   * @return 저장 처리가 끝나면 완료되는 Promise
   */
  const saveAll = async (): Promise<void> => {
    // 기준 메뉴 필수값이 없으면 서버 요청을 실행하지 않는다
    if (!menuForm.menuName.trim() || !menuForm.menuUrlx.trim()) {
      // "메뉴명과 URL을 입력해 주세요."
      window.alert('메뉴명과 URL을 입력해 주세요.')
      // 유효하지 않은 기준 메뉴 저장을 종료한다
      return
    }

    // 기존 또는 신규 하위 메뉴 필수값이 없으면 전체 저장을 시작하지 않는다
    if (subMenus.some((menu) => !menu.menuName.trim() || !menu.menuUrlx.trim())
        || childForms.some((menu) => !menu.menuName.trim() || !menu.menuUrlx.trim())) {
      // "하위메뉴명과 URL을 입력해 주세요."
      window.alert('하위메뉴명과 URL을 입력해 주세요.')
      // 유효하지 않은 하위 메뉴가 포함된 전체 저장을 종료한다
      return
    }

    // 중복 저장을 막기 위해 메뉴 저장 진행 상태를 활성화한다
    setSaving(true)
    // 이전 메뉴 화면 오류가 저장 결과와 함께 표시되지 않도록 초기화한다
    onError(null)
    // 전체 저장 성공과 실패를 현재 메뉴 화면에 반영한다
    try {
      // 기준 메뉴와 기존 및 신규 하위 메뉴를 단일 트랜잭션으로 저장한다
      const result = await saveMenuBatch(menuForm, subMenus, childForms)
      // 기존 상세 저장이면 현재 복합키의 최신 데이터를 다시 조회한다
      if (isDetailPage) {
        // 저장된 기준 메뉴와 하위 메뉴를 상세 화면에 갱신한다
        await openDetail(menuForm.menuNumb, menuForm.subxNumb)
        // 메뉴명과 사용 상태 변경을 사이드바에 반영한다
        await onSidebarRefresh()
      }

      // 신규 메뉴 저장이면 서버가 생성한 복합키의 상세로 이동한다
      else {
        // 저장된 메뉴의 상세 경로로 이동한다
        onMovePath(`${MENU_DETAIL_PREFIX}/${result.data.menuNumb}/${result.data.subxNumb}`)
      }

      // 서버가 반환한 저장 완료 문구를 표시한다
      window.alert(result.message)
    }

    // 메뉴 상세 저장 실패를 공통 오류 영역에 표시한다
    catch (error: unknown) {
      // 서버 업무 메시지가 있으면 기본 오류 문구보다 우선 표시한다
      onError(error instanceof Error ? error.message : '메뉴 저장 중 오류가 발생했습니다.')
    }

    // 저장 성공 여부와 무관하게 다시 저장할 수 있도록 진행 상태를 해제한다
    finally {
      // 메뉴 저장 버튼의 진행 상태를 해제한다
      setSaving(false)
    }
  }

  /** 메뉴 상세 폼 제출로 전체 저장을 실행한다. */
  const handleSubmit = (event: FormEvent<HTMLFormElement>): void => {
    // 브라우저 기본 폼 제출을 막는다
    event.preventDefault()
    // 현재 메뉴 상세 전체 저장을 실행한다
    void saveAll()
  }

  /** 신규 하위 메뉴 입력 행을 다음 정렬값으로 추가한다. */
  const addChildForm = (): void => {
    // 기존과 신규 하위 메뉴의 마지막 정렬값 다음 번호를 계산한다
    const nextSortOrdr = getNextSortOrdr([...subMenus, ...childForms])
    // 현재 기준 메뉴 아래에 신규 하위 메뉴 입력 행을 추가한다
    setChildForms([
      ...childForms,
      { ...emptyMenuForm(menuForm.menuNumb), sortOrdr: nextSortOrdr },
    ])
  }

  /** 기존 하위 메뉴 입력값을 불변 배열로 갱신한다. */
  const changeSubMenu = (
    index: number,
    field: 'menuName' | 'menuUrlx' | 'useeYsno' | 'sortOrdr',
    value: string,
  ): void => {
    // 선택한 기존 하위 메뉴 행만 변경한 새 배열을 설정한다
    setSubMenus(subMenus.map((menu, menuIndex) => (
      menuIndex === index
        ? { ...menu, [field]: field === 'sortOrdr' ? Number(value) : value }
        : menu
    )))
  }

  /** 신규 하위 메뉴 입력값을 불변 배열로 갱신한다. */
  const changeChildForm = (index: number, field: keyof MenuForm, value: string): void => {
    // 선택한 신규 하위 메뉴 입력 행만 변경한 새 배열을 설정한다
    setChildForms(childForms.map((form, formIndex) => (
      formIndex === index ? { ...form, [field]: value } : form
    )))
  }

  // 메뉴 목록 경로에서는 검색과 페이지 이동이 가능한 목록을 반환한다
  if (isListPage) {
    return (
      <MenuListPage
        menuRows={menuRows}
        pageData={pageData}
        useeYsnoCodes={useeYsnoCodes}
        onSearch={handleSearch}
        onMovePath={onMovePath}
        onDelete={(menu, search) => void deleteMenu(menu, search)}
      />
    )
  }

  // 메뉴 등록 또는 상세 경로에서는 기준 메뉴와 하위 메뉴 편집 화면을 반환한다
  return (
    <MenuDetailPage
      isNewPage={isNewPage}
      pageTitle={isNewPage ? '메뉴 등록' : `${activeMenuName || '메뉴관리'} 상세`}
      saving={saving}
      menuForm={menuForm}
      menuDetail={menuDetail}
      childForms={childForms}
      subMenus={subMenus}
      useeYsnoCodes={useeYsnoCodes}
      onMovePath={onMovePath}
      onChangeMenuForm={(field, value) => setMenuForm({ ...menuForm, [field]: value })}
      onChangeChildForm={changeChildForm}
      onChangeSubMenu={changeSubMenu}
      onSubmit={handleSubmit}
      onAddChildForm={addChildForm}
      onSaveAll={() => void saveAll()}
      onDelete={(menu) => void deleteMenu(menu)}
    />
  )
}
