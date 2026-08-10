import { useEffect, useMemo, useState } from 'react'
import type { FormEvent, MouseEvent } from 'react'
import { getCodeList } from '../../api/codeApi'
import {
  deleteUserMenuApi,
  getUserMenuChildren,
  getUserMenuDetail,
  getUserMenuParents,
  getUserMenus,
  saveUserMenuApi,
} from '../../api/userMenuApi'
import { AuditInfoTable } from '../../components/AuditInfoTable'
import { Pagination } from '../../components/Pagination'
import { COMM_YSNO, DEFAULT_USEE_YSNO } from '../../constants/codes'
import { USER_MENU_DETAIL_PREFIX, USER_MENU_LIST_PATH, USER_MENU_NEW_PATH } from '../../constants/routes'
import { useMenuPermission } from '../../contexts/useMenuPermission'
import type { Code } from '../../types/code'
import type { PageData } from '../../types/common'
import type { UserMenu, UserMenuForm, UserMenuSearch } from '../../types/userMenu'
import { formatDate, getUseeYsnoCodeName } from '../../utils/code'
import { getListPageSnapshot, setListPageSnapshot } from '../../utils/search'

type UserMenuManagePageProps = {
  currentPath: string
  onMovePath: (path: string) => void
  onError: (message: string | null) => void
}

type VisibleUserMenuRow = {
  menu: UserMenu
  hasChildren: boolean
  expanded: boolean
}

const DEFAULT_SEARCH: UserMenuSearch = {
  keyword: '',
  showYsno: '',
  useeYsno: '',
  menuLevl: '',
}

/** 빈 사용자 메뉴 입력 폼을 생성한다. */
const emptyUserMenuForm = (): UserMenuForm => ({
  menuNumb: '',
  parnNumb: '',
  menuName: '',
  menuUrlx: '/',
  sortOrdr: '1',
  showYsno: DEFAULT_USEE_YSNO,
  useeYsno: DEFAULT_USEE_YSNO,
})

/** 조회한 사용자 메뉴를 입력 폼으로 변환한다. */
const toUserMenuForm = (menu: UserMenu): UserMenuForm => ({
  menuNumb: String(menu.menuNumb),
  parnNumb: menu.parnNumb == null ? '' : String(menu.parnNumb),
  menuName: menu.menuName,
  menuUrlx: menu.menuUrlx,
  sortOrdr: menu.showYsno === DEFAULT_USEE_YSNO ? String(menu.sortOrdr ?? 1) : '',
  showYsno: menu.showYsno ?? DEFAULT_USEE_YSNO,
  useeYsno: menu.useeYsno ?? DEFAULT_USEE_YSNO,
})

/** 현재 메뉴와 모든 하위 메뉴 번호를 상위 메뉴 선택 제외 목록으로 계산한다. */
const getInvalidParentNumbs = (menuNumb: number | null, menuList: UserMenu[]): Set<number> => {
  const invalidMenuNumbs = new Set<number>()
  // 신규 메뉴는 자기 자신이나 하위 메뉴가 없으므로 빈 제외 목록을 반환한다.
  if (menuNumb == null) {
    return invalidMenuNumbs
  }

  // 현재 메뉴 자체를 상위 메뉴 선택 제외 목록에 추가한다.
  invalidMenuNumbs.add(menuNumb)
  const parentMenuNumbs = [menuNumb]

  // 모든 하위 메뉴를 따라가며 상위 메뉴 선택 제외 목록을 완성한다.
  while (parentMenuNumbs.length > 0) {
    const parentMenuNumb = parentMenuNumbs.shift()
    // 확인할 부모 번호가 없으면 다음 반복으로 이동한다.
    if (parentMenuNumb == null) {
      continue
    }

    // 현재 부모의 직접 하위 메뉴를 찾아 제외 목록과 다음 탐색 대상에 추가한다.
    menuList.forEach((menu) => {
      // 현재 부모와 연결되지 않은 메뉴는 건너뛴다.
      if (menu.parnNumb !== parentMenuNumb) {
        return
      }

      // 하위 메뉴를 상위 메뉴 선택 제외 목록과 다음 탐색 대상에 추가한다.
      invalidMenuNumbs.add(menu.menuNumb)
      parentMenuNumbs.push(menu.menuNumb)
    })
  }

  // 자기 자신과 모든 하위 메뉴 번호를 반환한다.
  return invalidMenuNumbs
}

/** 현재 페이지 메뉴 중 펼쳐진 계층에 포함되는 행만 구성한다. */
const getVisibleUserMenuRows = (
  menuList: UserMenu[],
  expandedMenuNumbs: Set<number>,
): VisibleUserMenuRow[] => {
  const menuByNumb = new Map(menuList.map((menu) => [menu.menuNumb, menu]))
  const parentNumbs = new Set(
    menuList
      .map((menu) => menu.parnNumb)
      .filter((menuNumb): menuNumb is number => menuNumb != null),
  )

  // 현재 페이지 안에서 부모 계층이 모두 펼쳐진 메뉴만 화면 행으로 반환한다.
  return menuList
    .filter((menu) => {
      let parentNumb = menu.parnNumb
      // 현재 페이지에 함께 조회된 부모 계층의 펼침 상태를 순서대로 확인한다.
      while (parentNumb != null && menuByNumb.has(parentNumb)) {
        // 부모가 접혀 있으면 현재 메뉴와 그 하위 분기를 숨긴다.
        if (!expandedMenuNumbs.has(parentNumb)) {
          return false
        }

        // 한 단계 위의 부모를 이어서 확인한다.
        parentNumb = menuByNumb.get(parentNumb)?.parnNumb ?? null
      }

      // 현재 페이지 경계 밖의 부모는 검색 결과 행을 숨기지 않는다.
      return true
    })
    .map((menu) => ({
      menu,
      hasChildren: parentNumbs.has(menu.menuNumb),
      expanded: expandedMenuNumbs.has(menu.menuNumb),
    }))
}

/** 사용자 메뉴 목록·상세·등록 화면을 제공한다. */
export function UserMenuManagePage({ currentPath, onMovePath, onError }: UserMenuManagePageProps) {
  const permission = useMenuPermission()
  const [rows, setRows] = useState<UserMenu[]>([])
  const [pageData, setPageData] = useState<PageData<UserMenu>>({
    items: [], totalCount: 0, pageNumber: 1, pageSize: 20, totalPages: 0,
  })
  const [detail, setDetail] = useState<UserMenu | null>(null)
  const [childMenus, setChildMenus] = useState<UserMenu[]>([])
  const [childEditForms, setChildEditForms] = useState<UserMenuForm[]>([])
  const [childForms, setChildForms] = useState<UserMenuForm[]>([])
  const [parentMenus, setParentMenus] = useState<UserMenu[]>([])
  const [form, setForm] = useState<UserMenuForm>(emptyUserMenuForm())
  const [ysnoCodes, setYsnoCodes] = useState<Code[]>([])
  const [saving, setSaving] = useState(false)
  const [search, setSearch] = useState<UserMenuSearch>(DEFAULT_SEARCH)
  const [appliedSearch, setAppliedSearch] = useState<UserMenuSearch>(DEFAULT_SEARCH)
  const [expandedMenuNumbs, setExpandedMenuNumbs] = useState<Set<number>>(new Set())

  const detailMenuNumb = useMemo(() => {
    // 사용자 메뉴 상세 경로가 아니면 상세 메뉴 번호를 반환하지 않는다.
    if (!currentPath.startsWith(`${USER_MENU_DETAIL_PREFIX}/`)) {
      return null
    }

    // 상세 경로 마지막 값을 숫자 메뉴 번호로 변환한다.
    const menuNumb = Number(currentPath.split('/').at(-1))
    // 숫자가 아닌 상세 경로는 조회 대상으로 사용하지 않는다.
    return Number.isFinite(menuNumb) ? menuNumb : null
  }, [currentPath])
  const isList = currentPath === USER_MENU_LIST_PATH
  const isNew = currentPath === USER_MENU_NEW_PATH
  const invalidParentNumbs = useMemo(
    () => getInvalidParentNumbs(detailMenuNumb, parentMenus),
    [detailMenuNumb, parentMenus],
  )
  const selectableParentMenus = parentMenus.filter((menu) => !invalidParentNumbs.has(menu.menuNumb))
  const selectedParent = parentMenus.find((menu) => String(menu.menuNumb) === form.parnNumb)
  const expectedMenuLevl = selectedParent ? selectedParent.menuLevl + 1 : 1
  const visibleRows = useMemo(
    () => getVisibleUserMenuRows(rows, expandedMenuNumbs),
    [expandedMenuNumbs, rows],
  )

  /** 현재 경로에 필요한 사용자 메뉴 데이터를 조회한다. */
  useEffect(() => {
    /** 목록 또는 상세 경로에 필요한 초기 데이터를 조회한다. */
    const load = async (): Promise<void> => {
      onError(null)
      try {
        // 사용자 메뉴의 여부 셀렉트박스에 사용할 공통코드를 조회한다.
        const codeResult = await getCodeList(COMM_YSNO)
        // 조회한 여부 공통코드를 화면 상태에 설정한다.
        setYsnoCodes(codeResult)

        // 목록 경로에서는 저장한 검색 상태로 사용자 메뉴 목록을 복원한다.
        if (isList) {
          const snapshot = getListPageSnapshot(USER_MENU_LIST_PATH, DEFAULT_SEARCH)
          const result = await getUserMenus(snapshot.pageNumber, snapshot.search)
          setPageData(result)
          setRows(result.items)
          setSearch(snapshot.search)
          setAppliedSearch(snapshot.search)
          setExpandedMenuNumbs(new Set())
          setDetail(null)
          setChildMenus([])
          setChildEditForms([])
          setChildForms([])
          return
        }

        // 등록과 상세 화면에서 사용할 상위 메뉴 후보를 조회한다.
        const parents = await getUserMenuParents()
        // 상위 메뉴 후보를 화면 상태에 설정한다.
        setParentMenus(parents)

        // 신규 등록 경로는 빈 사용자 메뉴 입력 폼을 표시한다.
        if (isNew) {
          setDetail(null)
          setChildMenus([])
          setChildEditForms([])
          setChildForms([])
          setForm(emptyUserMenuForm())
          return
        }

        // 상세 메뉴 번호가 있으면 상세와 직계 하위 메뉴를 함께 조회한다.
        if (detailMenuNumb != null) {
          const [menu, children] = await Promise.all([
            getUserMenuDetail(detailMenuNumb),
            getUserMenuChildren(detailMenuNumb),
          ])
          setDetail(menu)
          setChildMenus(children)
          setChildEditForms(children.map(toUserMenuForm))
          setChildForms([])
          setForm(toUserMenuForm(menu))
        }
      } catch (error: unknown) {
        // "사용자 메뉴 조회 중 오류가 발생했습니다."
        onError(error instanceof Error ? error.message : '사용자 메뉴 조회 중 오류가 발생했습니다.')
      }
    }

    // 현재 경로에 필요한 사용자 메뉴 조회를 실행한다.
    void load()
  }, [currentPath, detailMenuNumb, isList, isNew, onError])

  /** 지정한 검색 조건과 페이지로 사용자 메뉴 목록을 조회한다. */
  const loadListPage = async (pageNumber: number, targetSearch: UserMenuSearch): Promise<void> => {
    // 사용자 메뉴 목록의 지정 페이지를 조회한다.
    const result = await getUserMenus(pageNumber, targetSearch)
    // 페이지 메타데이터와 목록 행을 화면 상태에 설정한다.
    setPageData(result)
    setRows(result.items)
    setAppliedSearch(targetSearch)
    setExpandedMenuNumbs(new Set())
    // 상세 화면에서 목록으로 돌아올 때 현재 조회 상태를 복원하도록 저장한다.
    setListPageSnapshot(USER_MENU_LIST_PATH, result.pageNumber, targetSearch)
  }

  /** 입력한 사용자 메뉴 조건으로 첫 페이지를 검색한다. */
  const handleSearch = async (event: FormEvent<HTMLFormElement>): Promise<void> => {
    // 검색 폼의 브라우저 기본 제출을 막는다.
    event.preventDefault()
    // 입력한 검색 조건으로 첫 페이지를 조회한다.
    await loadListPage(1, search)
  }

  /** 사용자 메뉴 검색 조건과 결과를 전체 목록으로 초기화한다. */
  const handleSearchReset = async (): Promise<void> => {
    // 사용자 메뉴 검색 입력값을 기본 조건으로 초기화한다.
    setSearch(DEFAULT_SEARCH)
    // 기본 검색 조건으로 첫 페이지를 조회한다.
    await loadListPage(1, DEFAULT_SEARCH)
  }

  /** 사용자 메뉴 계층의 펼침 상태를 전환한다. */
  const handleToggleMenu = (event: MouseEvent<HTMLButtonElement>, menuNumb: number): void => {
    // 펼침 버튼이 사용자 메뉴 상세 이동까지 실행하지 않도록 이벤트 전파를 막는다.
    event.stopPropagation()
    setExpandedMenuNumbs((currentMenuNumbs) => {
      const nextMenuNumbs = new Set(currentMenuNumbs)
      // 현재 펼침 상태에 따라 선택한 메뉴 분기를 접거나 펼친다.
      if (nextMenuNumbs.has(menuNumb)) {
        nextMenuNumbs.delete(menuNumb)
      } else {
        nextMenuNumbs.add(menuNumb)
      }

      // 변경된 사용자 메뉴 펼침 상태를 반환한다.
      return nextMenuNumbs
    })
  }

  /** 사용자 메뉴 입력값을 변경한다. */
  const changeForm = (field: keyof UserMenuForm, value: string): void => {
    // 햄버거 메뉴 노출 여부에 따라 정렬 입력값을 사용하거나 제거한다.
    if (field === 'showYsno') {
      setForm({ ...form, showYsno: value, sortOrdr: value === DEFAULT_USEE_YSNO ? form.sortOrdr || '1' : '' })
      return
    }

    // 변경한 단일 입력값을 기존 사용자 메뉴 폼에 반영한다.
    setForm({ ...form, [field]: value })
  }

  /** 현재 사용자 메뉴의 신규 하위 메뉴 입력 행을 추가한다. */
  const addChildForm = (): void => {
    // 상세 메뉴가 없거나 최대 메뉴 단계이면 하위 입력 행을 추가하지 않는다.
    if (!detail || expectedMenuLevl >= 3) {
      return
    }

    // 현재 상세 메뉴를 상위 메뉴로 지정한 빈 입력 행을 추가한다.
    setChildForms((currentForms) => [
      ...currentForms,
      { ...emptyUserMenuForm(), parnNumb: String(detail.menuNumb), menuUrlx: '' },
    ])
  }

  /** 신규 하위 메뉴 입력 행의 단일 값을 변경한다. */
  const changeChildForm = (index: number, field: keyof UserMenuForm, value: string): void => {
    setChildForms((currentForms) => currentForms.map((childForm, childIndex) => {
      // 선택하지 않은 입력 행은 기존 값을 유지한다.
      if (childIndex !== index) {
        return childForm
      }

      // 햄버거 메뉴 노출 여부에 따라 정렬 입력값을 사용하거나 제거한다.
      if (field === 'showYsno') {
        return {
          ...childForm,
          showYsno: value,
          sortOrdr: value === DEFAULT_USEE_YSNO ? childForm.sortOrdr || '1' : '',
        }
      }

      // 변경한 단일 입력값을 선택한 하위 메뉴 폼에 반영한다.
      return { ...childForm, [field]: value }
    }))
  }

  /** 기존 하위 메뉴 입력 행의 단일 값을 변경한다. */
  const changeChildEditForm = (index: number, field: keyof UserMenuForm, value: string): void => {
    setChildEditForms((currentForms) => currentForms.map((childForm, childIndex) => {
      // 선택하지 않은 입력 행은 기존 값을 유지한다.
      if (childIndex !== index) {
        return childForm
      }

      // 햄버거 메뉴 노출 여부에 따라 정렬 입력값을 사용하거나 제거한다.
      if (field === 'showYsno') {
        return {
          ...childForm,
          showYsno: value,
          sortOrdr: value === DEFAULT_USEE_YSNO ? childForm.sortOrdr || '1' : '',
        }
      }

      // 변경한 단일 입력값을 선택한 기존 하위 메뉴 폼에 반영한다.
      return { ...childForm, [field]: value }
    }))
  }

  /** 저장 전 신규 하위 메뉴 입력 행을 삭제한다. */
  const removeChildForm = (index: number): void => {
    // 선택한 입력 행을 제외한 하위 메뉴 폼만 유지한다.
    setChildForms((currentForms) => currentForms.filter((_, childIndex) => childIndex !== index))
  }

  /** 사용자 메뉴를 등록하거나 수정한다. */
  const saveMenu = async (event?: FormEvent<HTMLFormElement>): Promise<void> => {
    // 버튼과 폼 제출 모두 같은 저장 함수를 사용하도록 기본 제출을 막는다.
    event?.preventDefault()
    // 메뉴명이 없으면 저장 요청을 실행하지 않는다.
    if (!form.menuName.trim()) {
      // "메뉴명을 입력해 주세요."
      alert('메뉴명을 입력해 주세요.')
      return
    }

    // 계산한 메뉴 단계가 3을 넘으면 저장 요청을 실행하지 않는다.
    if (expectedMenuLevl > 3) {
      // "사용자 메뉴는 최대 3뎁스까지 등록할 수 있습니다."
      alert('사용자 메뉴는 최대 3뎁스까지 등록할 수 있습니다.')
      return
    }

    // 현재 메뉴가 3뎁스가 되면 하위 메뉴를 함께 등록할 수 없다.
    if (childForms.length > 0 && expectedMenuLevl >= 3) {
      // "3뎁스 메뉴에는 하위 메뉴를 등록할 수 없습니다."
      alert('3뎁스 메뉴에는 하위 메뉴를 등록할 수 없습니다.')
      return
    }

    // 추가할 하위 메뉴 중 메뉴명이 비어 있으면 저장 요청을 실행하지 않는다.
    if (childForms.some((childForm) => !childForm.menuName.trim())) {
      // "추가할 하위 메뉴의 메뉴명을 입력해 주세요."
      alert('추가할 하위 메뉴의 메뉴명을 입력해 주세요.')
      return
    }

    // 수정할 기존 하위 메뉴 중 메뉴명이 비어 있으면 저장 요청을 실행하지 않는다.
    if (childEditForms.some((childForm) => !childForm.menuName.trim())) {
      alert('하위 메뉴의 메뉴명을 입력해 주세요.')
      return
    }

    // 중복 저장 요청을 막기 위해 저장 중 상태를 설정한다.
    setSaving(true)
    onError(null)
    try {
      // 상세 여부에 따라 사용자 메뉴 등록 또는 수정 API를 호출한다.
      const result = await saveUserMenuApi(form, detailMenuNumb != null)
      // 신규 메뉴 등록 화면은 저장된 사용자 메뉴 상세 경로로 이동한다.
      if (detailMenuNumb == null) {
        alert(result.message)
        onMovePath(`${USER_MENU_DETAIL_PREFIX}/${result.data.menuNumb}`)
        return
      }

      // 기존 하위 메뉴 수정과 신규 하위 메뉴 등록 요청을 구성한다.
      const childEditRequests = childEditForms.map((childForm) => saveUserMenuApi(childForm, true))
      const childSaveRequests = childForms.map((childForm) => saveUserMenuApi(childForm, false))
      // 모든 직계 하위 메뉴를 현재 상세 화면에서 함께 저장한다.
      await Promise.all([...childEditRequests, ...childSaveRequests])
      // 저장 결과가 반영된 직계 하위 메뉴와 상위 메뉴 후보를 다시 조회한다.
      const [children, parents] = await Promise.all([
        getUserMenuChildren(result.data.menuNumb),
        getUserMenuParents(),
      ])
      // 현재 상세 화면과 하위 메뉴 목록을 최신 저장 결과로 갱신한다.
      setDetail(result.data)
      setForm(toUserMenuForm(result.data))
      setChildMenus(children)
      setChildEditForms(children.map(toUserMenuForm))
      setParentMenus(parents)
      setChildForms([])
      // 저장한 범위에 맞는 성공 메시지를 현재 상세 화면에서 표시한다.
      alert(childEditForms.length > 0 || childForms.length > 0
        ? '사용자 메뉴와 하위 메뉴가 저장되었습니다.'
        : result.message)
    } catch (error: unknown) {
      // "사용자 메뉴 저장 중 오류가 발생했습니다."
      onError(error instanceof Error ? error.message : '사용자 메뉴 저장 중 오류가 발생했습니다.')
    } finally {
      // 저장 성공 여부와 관계없이 다시 저장할 수 있도록 상태를 해제한다.
      setSaving(false)
    }
  }

  /** 사용자 메뉴를 삭제한다. */
  const deleteMenu = async (menuNumb: number): Promise<void> => {
    try {
      // 하위 메뉴가 없는 사용자 메뉴 삭제 API를 호출한다.
      const result = await deleteUserMenuApi(menuNumb)
      // 서버가 반환한 삭제 성공 메시지를 표시한다.
      alert(result.message)
      // 삭제한 사용자 메뉴 상세에서 목록으로 이동한다.
      onMovePath(USER_MENU_LIST_PATH)
    } catch (error: unknown) {
      // "사용자 메뉴 삭제 중 오류가 발생했습니다."
      onError(error instanceof Error ? error.message : '사용자 메뉴 삭제 중 오류가 발생했습니다.')
    }
  }

  // 사용자 메뉴 목록 경로에서는 공통코드관리형 계층 목록을 반환한다.
  if (isList) {
    return (
      /* 사용자 메뉴 목록 전체 영역 */
      <section className="menu-manage">
        {/* 사용자 메뉴 목록 제목과 전체 건수 영역 */}
        <section className="content-header">
          <h1>사용자 메뉴관리</h1>
          <div className="status">총 {pageData.totalCount}건</div>
        </section>
        {/* 사용자 메뉴 목록 검색 영역 */}
        <form className="list-search" onSubmit={(event) => void handleSearch(event)}>
          <label>
            <span>메뉴명·URL</span>
            <input
              value={search.keyword}
              maxLength={100}
              placeholder="메뉴명 또는 URL"
              onChange={(event) => setSearch({ ...search, keyword: event.target.value })}
            />
          </label>
          <label>
            <span>메뉴 단계</span>
            <select value={search.menuLevl} onChange={(event) => setSearch({ ...search, menuLevl: event.target.value })}>
              <option value="">전체</option>
              <option value="1">1뎁스</option>
              <option value="2">2뎁스</option>
              <option value="3">3뎁스</option>
            </select>
          </label>
          <label>
            <span>햄버거 메뉴 노출</span>
            <select value={search.showYsno} onChange={(event) => setSearch({ ...search, showYsno: event.target.value })}>
              <option value="">전체</option>
              {ysnoCodes.map((code) => <option key={code.comdCode} value={code.comdCode}>{code.opt1Name ?? code.comdName}</option>)}
            </select>
          </label>
          <label>
            <span>사용여부</span>
            <select value={search.useeYsno} onChange={(event) => setSearch({ ...search, useeYsno: event.target.value })}>
              <option value="">전체</option>
              {ysnoCodes.map((code) => <option key={code.comdCode} value={code.comdCode}>{code.opt1Name ?? code.comdName}</option>)}
            </select>
          </label>
          {/* 사용자 메뉴 검색 실행과 초기화 버튼 영역 */}
          <div className="list-search-actions">
            <button type="button" className="subtle-button" onClick={() => void handleSearchReset()}>초기화</button>
            <button type="submit">검색</button>
          </div>
        </form>
        {/* 최대 3단계 사용자 메뉴 계층 검색 결과 영역 */}
        <section className="table-wrap code-list-table code-tree-table user-menu-tree-table">
          <table>
            <thead>
              <tr>
                <th className="col-tree-toggle">계층</th>
                <th>메뉴명</th>
                <th>URL</th>
                <th>단계</th>
                <th>상위 메뉴</th>
                <th className="col-usee">햄버거 노출</th>
                <th className="col-usee">사용여부</th>
                <th className="col-sort">정렬</th>
                <th>수정자</th>
                <th>수정일</th>
              </tr>
            </thead>
            <tbody>
              {rows.length === 0 ? (
                <tr className="empty-row"><td colSpan={10}>등록된 사용자 메뉴가 없습니다.</td></tr>
              ) : visibleRows.map(({ menu, hasChildren, expanded }) => (
                /* 사용자 메뉴 계층 개별 항목 영역 */
                <tr
                  className={menu.menuLevl === 1 ? 'code-master-row' : 'code-detail-tree-row'}
                  key={menu.menuNumb}
                  onClick={() => onMovePath(`${USER_MENU_DETAIL_PREFIX}/${menu.menuNumb}`)}
                >
                  <td className="col-tree-toggle">
                    {hasChildren ? (
                      <button
                        type="button"
                        className="icon-toggle-button"
                        aria-label={expanded ? '하위 메뉴 접기' : '하위 메뉴 펼치기'}
                        title={expanded ? '접기' : '펼치기'}
                        onClick={(event) => handleToggleMenu(event, menu.menuNumb)}
                      >
                        <TreeToggleIcon expanded={expanded} />
                      </button>
                    ) : <span className="tree-toggle-placeholder" />}
                  </td>
                  <td
                    className="tree-code-cell"
                    style={{ paddingLeft: `${16 + Math.max(menu.menuLevl - 1, 0) * 24}px` }}
                  >
                    {menu.menuName}
                  </td>
                  <td>{menu.menuUrlx || '-'}</td>
                  <td>{menu.menuLevl}뎁스</td>
                  <td>{menu.parnName ?? '-'}</td>
                  <td className="col-usee">{getUseeYsnoCodeName(ysnoCodes, menu.showYsno, menu.showYsnoName)}</td>
                  <td className="col-usee">{getUseeYsnoCodeName(ysnoCodes, menu.useeYsno, menu.useeYsnoName)}</td>
                  <td className="col-sort">{menu.showYsno === DEFAULT_USEE_YSNO ? menu.sortOrdr : ''}</td>
                  <td>{menu.updtAdmnName ?? menu.updtAdmn}</td>
                  <td>{formatDate(menu.updtDate)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </section>
        {/* 사용자 메뉴 목록 페이지 이동 영역 */}
        <Pagination
          pageNumber={pageData.pageNumber}
          totalPages={pageData.totalPages}
          onPageChange={(pageNumber) => void loadListPage(pageNumber, appliedSearch)}
        />
        {permission.writYsno === 'Y' && (
          <button type="button" className="floating-button" onClick={() => onMovePath(USER_MENU_NEW_PATH)}>등록</button>
        )}
      </section>
    )
  }

  // 사용자 메뉴 등록 또는 공통코드관리형 상세 편집 화면을 반환한다.
  return (
    /* 사용자 메뉴 상세 전체 영역 */
    <section className="code-detail user-menu-detail">
      {/* 사용자 메뉴 상세 제목 영역 */}
      <section className="content-header">
        <h1>{isNew ? '사용자 메뉴 등록' : '사용자 메뉴관리 상세'}</h1>
      </section>

      {/* 현재 사용자 메뉴 기본정보 편집 영역 */}
      <form className="detail-panel" onSubmit={saveMenu}>
        <div className="detail-title">
          <div>
            <h2>{isNew ? '사용자 메뉴 등록' : '사용자 메뉴 정보'}</h2>
            <p>상위 메뉴를 선택해 최대 3뎁스까지 구성합니다.</p>
          </div>
        </div>
        <UserMenuFormTable
          form={form}
          expectedMenuLevl={expectedMenuLevl}
          parentMenus={selectableParentMenus}
          ysnoCodes={ysnoCodes}
          onChange={changeForm}
        />
      </form>

      {/* 현재 사용자 메뉴의 직계 하위 메뉴 목록 영역 */}
      {!isNew && detail && detail.menuLevl < 3 && (
        <section className="detail-panel">
          <div className="detail-title">
            <div>
              <h2>하위 메뉴</h2>
              <p>현재 메뉴 바로 아래의 사용자 메뉴를 이 화면에서 함께 수정합니다.</p>
            </div>
            <div className="status">총 {childMenus.length}건</div>
          </div>
          <section className="table-wrap code-edit-table user-menu-child-table">
            <table>
              <thead>
                <tr>
                  <th>메뉴명</th>
                  <th>URL</th>
                  <th>단계</th>
                  <th className="col-usee">햄버거 노출</th>
                  <th className="col-usee">사용여부</th>
                  <th className="col-sort">정렬</th>
                  <th>수정자</th>
                  <th className="col-datetime">수정일</th>
                  <th className="col-action">상세</th>
                </tr>
              </thead>
              <tbody>
                {childMenus.length === 0 ? (
                  <tr className="empty-row"><td colSpan={9}>하위 메뉴가 없습니다.</td></tr>
                ) : childEditForms.map((childForm, index) => {
                  const menu = childMenus[index]
                  return (
                  /* 직계 하위 사용자 메뉴 개별 항목 영역 */
                  <tr className="editable-row" key={childForm.menuNumb}>
                    <td>
                      <input
                        value={childForm.menuName}
                        onChange={(event) => changeChildEditForm(index, 'menuName', event.target.value)}
                        required
                      />
                    </td>
                    <td>
                      <input
                        value={childForm.menuUrlx}
                        placeholder="그룹 메뉴는 비워둘 수 있습니다."
                        onChange={(event) => changeChildEditForm(index, 'menuUrlx', event.target.value)}
                      />
                    </td>
                    <td>{menu.menuLevl}뎁스</td>
                    <td className="col-usee">
                      <YsnoSelect
                        value={childForm.showYsno}
                        codes={ysnoCodes}
                        onChange={(value) => changeChildEditForm(index, 'showYsno', value)}
                      />
                    </td>
                    <td className="col-usee">
                      <YsnoSelect
                        value={childForm.useeYsno}
                        codes={ysnoCodes}
                        onChange={(value) => changeChildEditForm(index, 'useeYsno', value)}
                      />
                    </td>
                    <td className="col-sort">
                      {childForm.showYsno === DEFAULT_USEE_YSNO ? (
                        <input
                          type="number"
                          min="1"
                          value={childForm.sortOrdr}
                          onChange={(event) => changeChildEditForm(index, 'sortOrdr', event.target.value)}
                          required
                        />
                      ) : '-'}
                    </td>
                    <td>{menu.updtAdmnName ?? menu.updtAdmn}</td>
                    <td className="col-datetime">{formatDate(menu.updtDate)}</td>
                    <td className="col-action">
                      <button
                        type="button"
                        className="subtle-button"
                        onClick={() => onMovePath(`${USER_MENU_DETAIL_PREFIX}/${menu.menuNumb}`)}
                      >
                        상세
                      </button>
                    </td>
                  </tr>
                  )
                })}
              </tbody>
            </table>
          </section>
        </section>
      )}

      {/* 현재 사용자 메뉴 아래의 신규 하위 메뉴 인라인 입력 영역 */}
      {!isNew && detail && detail.menuLevl < 3 && (
        <section className="detail-panel">
          <div className="detail-title">
            <div>
              <h2>하위 메뉴 추가</h2>
              <p>추가할 하위 메뉴를 여러 개 입력한 뒤 현재 화면에서 한 번에 저장합니다.</p>
            </div>
            {permission.writYsno === 'Y' && (
              <button type="button" className="subtle-button" onClick={addChildForm}>하위 메뉴 추가</button>
            )}
          </div>
          {childForms.length > 0 ? (
            <section className="table-wrap code-edit-table new-code-table user-menu-child-table">
              <table>
                <thead>
                  <tr>
                    <th>메뉴명</th>
                    <th>URL</th>
                    <th>단계</th>
                    <th className="col-usee">햄버거 노출</th>
                    <th className="col-usee">사용여부</th>
                    <th className="col-sort">정렬</th>
                    <th className="col-action">삭제</th>
                  </tr>
                </thead>
                <tbody>
                  {childForms.map((childForm, index) => (
                    /* 신규 하위 사용자 메뉴 개별 입력 행 */
                    <tr className="editable-row" key={index}>
                      <td>
                        <input
                          value={childForm.menuName}
                          onChange={(event) => changeChildForm(index, 'menuName', event.target.value)}
                          required
                        />
                      </td>
                      <td>
                        <input
                          value={childForm.menuUrlx}
                          placeholder="그룹 메뉴는 비워둘 수 있습니다."
                          onChange={(event) => changeChildForm(index, 'menuUrlx', event.target.value)}
                        />
                      </td>
                      <td>{expectedMenuLevl + 1}뎁스</td>
                      <td className="col-usee">
                        <YsnoSelect
                          value={childForm.showYsno}
                          codes={ysnoCodes}
                          onChange={(value) => changeChildForm(index, 'showYsno', value)}
                        />
                      </td>
                      <td className="col-usee">
                        <YsnoSelect
                          value={childForm.useeYsno}
                          codes={ysnoCodes}
                          onChange={(value) => changeChildForm(index, 'useeYsno', value)}
                        />
                      </td>
                      <td className="col-sort">
                        {childForm.showYsno === DEFAULT_USEE_YSNO ? (
                          <input
                            type="number"
                            min="1"
                            value={childForm.sortOrdr}
                            onChange={(event) => changeChildForm(index, 'sortOrdr', event.target.value)}
                            required
                          />
                        ) : '-'}
                      </td>
                      <td className="col-action">
                        <button type="button" className="delete-button" onClick={() => removeChildForm(index)}>삭제</button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </section>
          ) : (
            <div className="empty small">추가할 하위 메뉴가 없습니다.</div>
          )}
        </section>
      )}

      {/* 사용자 메뉴 등록·수정 감사정보 영역 */}
      {!isNew && detail && (
        <AuditInfoTable
          regiAdmn={detail.regiAdmn}
          regiAdmnName={detail.regiAdmnName}
          regiDate={detail.regiDate}
          updtAdmn={detail.updtAdmn}
          updtAdmnName={detail.updtAdmnName}
          updtDate={detail.updtDate}
        />
      )}
      {/* 사용자 메뉴 목록·삭제·저장 명령 영역 */}
      <div className="detail-footer">
        <div className="detail-footer-left">
          <button type="button" className="subtle-button" onClick={() => onMovePath(USER_MENU_LIST_PATH)}>목록</button>
          {!isNew && detail && permission.deltYsno === 'Y' && (
            <button type="button" className="delete-button" onClick={() => void deleteMenu(detail.menuNumb)}>삭제</button>
          )}
        </div>
        <div className="detail-footer-right">
          {permission.writYsno === 'Y' && (
            <button type="button" disabled={saving} onClick={() => void saveMenu()}>
              {saving ? '저장 중' : isNew ? '저장' : '수정'}
            </button>
          )}
        </div>
      </div>
    </section>
  )
}

type UserMenuFormTableProps = {
  form: UserMenuForm
  expectedMenuLevl: number
  parentMenus: UserMenu[]
  ysnoCodes: Code[]
  onChange: (field: keyof UserMenuForm, value: string) => void
}

/** 공통코드관리형 표 구조로 사용자 메뉴 기본정보 입력 행을 표시한다. */
function UserMenuFormTable({
  form,
  expectedMenuLevl,
  parentMenus,
  ysnoCodes,
  onChange,
}: UserMenuFormTableProps) {
  // 사용자 메뉴의 현재 계층과 기본정보 편집 표를 반환한다.
  return (
    <section className="table-wrap code-edit-table user-menu-edit-table">
      <table>
        <thead>
          <tr>
            <th>상위 메뉴</th>
            <th>단계</th>
            <th>메뉴명</th>
            <th>URL</th>
            <th className="col-usee">햄버거 노출</th>
            <th className="col-usee">사용여부</th>
            <th className="col-sort">정렬</th>
          </tr>
        </thead>
        <tbody>
          <tr className="editable-row">
            <td>
              <select value={form.parnNumb} onChange={(event) => onChange('parnNumb', event.target.value)}>
                <option value="">없음 (1뎁스)</option>
                {parentMenus.map((menu) => (
                  <option key={menu.menuNumb} value={menu.menuNumb}>{menu.menuLevl}뎁스 / {menu.menuName}</option>
                ))}
              </select>
            </td>
            <td>{expectedMenuLevl}뎁스</td>
            <td><input value={form.menuName} onChange={(event) => onChange('menuName', event.target.value)} required /></td>
            <td>
              <input
                value={form.menuUrlx}
                placeholder="그룹 메뉴는 비워둘 수 있습니다."
                onChange={(event) => onChange('menuUrlx', event.target.value)}
              />
            </td>
            <td className="col-usee">
              <YsnoSelect value={form.showYsno} codes={ysnoCodes} onChange={(value) => onChange('showYsno', value)} />
            </td>
            <td className="col-usee">
              <YsnoSelect value={form.useeYsno} codes={ysnoCodes} onChange={(value) => onChange('useeYsno', value)} />
            </td>
            <td className="col-sort">
              {form.showYsno === DEFAULT_USEE_YSNO ? (
                <input
                  type="number"
                  min="1"
                  value={form.sortOrdr}
                  onChange={(event) => onChange('sortOrdr', event.target.value)}
                  required
                />
              ) : '-'}
            </td>
          </tr>
        </tbody>
      </table>
    </section>
  )
}

/** 사용자 메뉴 여부 공통코드 셀렉트박스를 표시한다. */
function YsnoSelect({ value, codes, onChange }: { value: string; codes: Code[]; onChange: (value: string) => void }) {
  // 여부 공통코드 선택 목록을 반환한다.
  return (
    <select value={value} onChange={(event) => onChange(event.target.value)}>
      {codes.map((code) => (
        <option key={code.comdCode} value={code.comdCode}>{code.opt1Name ?? code.comdName}</option>
      ))}
    </select>
  )
}

/** 사용자 메뉴 계층의 펼침 또는 접힘 상태 아이콘을 표시한다. */
function TreeToggleIcon({ expanded }: { expanded: boolean }) {
  // 현재 펼침 상태에 맞는 방향의 화살표 아이콘을 반환한다.
  return (
    <svg viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path
        d={expanded
          ? 'M19.92 15.05L13.4 8.53C12.63 7.76 11.37 7.76 10.6 8.53L4.08 15.05'
          : 'M19.92 8.95L13.4 15.47C12.63 16.24 11.37 16.24 10.6 15.47L4.08 8.95'}
        stroke="currentColor"
        strokeWidth="1.7"
        strokeMiterlimit="10"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </svg>
  )
}
