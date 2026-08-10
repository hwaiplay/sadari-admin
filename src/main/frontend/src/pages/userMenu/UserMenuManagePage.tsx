import { useEffect, useMemo, useState } from 'react'
import type { FormEvent } from 'react'
import { getCodeList } from '../../api/codeApi'
import {
  deleteUserMenuApi,
  getUserMenuDetail,
  getUserMenuParents,
  getUserMenus,
  saveUserMenuApi,
} from '../../api/userMenuApi'
import { COMM_YSNO, DEFAULT_USEE_YSNO } from '../../constants/codes'
import { USER_MENU_DETAIL_PREFIX, USER_MENU_LIST_PATH, USER_MENU_NEW_PATH } from '../../constants/routes'
import type { Code } from '../../types/code'
import type { UserMenu, UserMenuForm, UserMenuSearch } from '../../types/userMenu'
import { formatDate, getUseeYsnoCodeName } from '../../utils/code'
import { AuditInfoTable } from '../../components/AuditInfoTable'
import { useMenuPermission } from '../../contexts/useMenuPermission'
import { Pagination } from '../../components/Pagination'
import type { PageData } from '../../types/common'
import { getListPageSnapshot, setListPageSnapshot } from '../../utils/search'

type UserMenuManagePageProps = {
  currentPath: string
  onMovePath: (path: string) => void
  onError: (message: string | null) => void
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

/** 메뉴 단계에 맞는 들여쓰기 표시를 반환한다. */
const getMenuDepthMark = (menuLevl: number): string => {
  // 최상위 메뉴는 별도 들여쓰기 표시 없이 이름만 보여준다.
  if (menuLevl <= 1) {
    return ''
  }
  // 2단계와 3단계 메뉴는 단계 수만큼 들여쓰기 표시를 반환한다.
  return `${'　'.repeat(menuLevl - 2)}└ `
}

/** 현재 메뉴와 모든 하위 메뉴 번호를 상위 메뉴 선택 제외 목록으로 계산한다. */
const getInvalidParentNumbs = (menuNumb: number | null, menuList: UserMenu[]): Set<number> => {
  const invalidMenuNumbs = new Set<number>()
  // 신규 메뉴는 자기 자신이나 하위 메뉴가 없으므로 빈 제외 목록을 반환한다.
  if (menuNumb == null) {
    return invalidMenuNumbs
  }

  // 현재 메뉴 자체를 상위 메뉴 선택 제외 목록에 추가한다.
  invalidMenuNumbs.add(menuNumb)
  // 아직 확인하지 않은 하위 메뉴의 부모 번호를 담는다.
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
      // 하위 메뉴를 상위 메뉴 선택 제외 목록에 추가한다.
      invalidMenuNumbs.add(menu.menuNumb)
      // 하위 메뉴의 다음 단계도 확인하도록 탐색 대상에 추가한다.
      parentMenuNumbs.push(menu.menuNumb)
    })
  }
  // 자기 자신과 모든 하위 메뉴 번호를 반환한다.
  return invalidMenuNumbs
}

/** 사용자 메뉴 목록·상세·등록 화면을 제공한다. */
export function UserMenuManagePage({ currentPath, onMovePath, onError }: UserMenuManagePageProps) {
  const permission = useMenuPermission()
  const [rows, setRows] = useState<UserMenu[]>([])
  const [pageData, setPageData] = useState<PageData<UserMenu>>({
    items: [], totalCount: 0, pageNumber: 1, pageSize: 20, totalPages: 0,
  })
  const [detail, setDetail] = useState<UserMenu | null>(null)
  const [parentMenus, setParentMenus] = useState<UserMenu[]>([])
  const [form, setForm] = useState<UserMenuForm>(emptyUserMenuForm())
  const [ysnoCodes, setYsnoCodes] = useState<Code[]>([])
  const [saving, setSaving] = useState(false)
  const [search, setSearch] = useState<UserMenuSearch>(DEFAULT_SEARCH)
  const [appliedSearch, setAppliedSearch] = useState<UserMenuSearch>(DEFAULT_SEARCH)

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
          setDetail(null)
          return
        }

        // 등록과 상세 화면에서 사용할 상위 메뉴 후보를 조회한다.
        const parents = await getUserMenuParents()
        // 상위 메뉴 후보를 화면 상태에 설정한다.
        setParentMenus(parents)

        // 신규 등록 경로는 빈 사용자 메뉴 폼을 표시한다.
        if (isNew) {
          setDetail(null)
          setForm(emptyUserMenuForm())
          return
        }

        // 상세 메뉴 번호가 있으면 사용자 메뉴 상세를 조회한다.
        if (detailMenuNumb != null) {
          const menu = await getUserMenuDetail(detailMenuNumb)
          setDetail(menu)
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

    // 중복 저장 요청을 막기 위해 저장 중 상태를 설정한다.
    setSaving(true)
    onError(null)
    try {
      // 상세 여부에 따라 사용자 메뉴 등록 또는 수정 API를 호출한다.
      const result = await saveUserMenuApi(form, detailMenuNumb != null)
      // 서버가 반환한 저장 또는 수정 성공 메시지를 표시한다.
      alert(result.message)
      // 저장된 사용자 메뉴 상세 경로로 이동한다.
      onMovePath(`${USER_MENU_DETAIL_PREFIX}/${result.data.menuNumb}`)
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
      // 상세 화면에서 삭제했으면 사용자 메뉴 목록으로 이동한다.
      if (detailMenuNumb === menuNumb) {
        onMovePath(USER_MENU_LIST_PATH)
        return
      }
      // 목록 화면에서 삭제했으면 현재 검색 페이지를 다시 조회한다.
      await loadListPage(pageData.pageNumber, appliedSearch)
    } catch (error: unknown) {
      // "사용자 메뉴 삭제 중 오류가 발생했습니다."
      onError(error instanceof Error ? error.message : '사용자 메뉴 삭제 중 오류가 발생했습니다.')
    }
  }

  // 사용자 메뉴 목록 경로에서는 검색과 전체 단계 메뉴 표를 반환한다.
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
          <label><span>검색어</span><input value={search.keyword} placeholder="메뉴명 또는 URL"
            onChange={(event) => setSearch({ ...search, keyword: event.target.value })}/></label>
          <label><span>메뉴 단계</span><select value={search.menuLevl}
            onChange={(event) => setSearch({ ...search, menuLevl: event.target.value })}>
            <option value="">전체</option><option value="1">1뎁스</option><option value="2">2뎁스</option><option value="3">3뎁스</option>
          </select></label>
          <label><span>햄버거 메뉴 노출</span><select value={search.showYsno}
            onChange={(event) => setSearch({ ...search, showYsno: event.target.value })}>
            <option value="">전체</option>{ysnoCodes.map((code) => <option key={code.comdCode} value={code.comdCode}>{code.opt1Name ?? code.comdName}</option>)}
          </select></label>
          <label><span>사용여부</span><select value={search.useeYsno}
            onChange={(event) => setSearch({ ...search, useeYsno: event.target.value })}>
            <option value="">전체</option>{ysnoCodes.map((code) => <option key={code.comdCode} value={code.comdCode}>{code.opt1Name ?? code.comdName}</option>)}
          </select></label>
          <div className="list-search-actions"><button type="button" className="subtle-button" onClick={() => void handleSearchReset()}>초기화</button><button type="submit">검색</button></div>
        </form>
        {/* 최대 3단계 사용자 메뉴 목록 표 영역 */}
        <section className="table-wrap menu-list-table">
          <table><thead><tr><th>메뉴명</th><th>단계</th><th>상위 메뉴</th><th>URL</th><th className="col-usee">햄버거 메뉴 노출</th><th className="col-usee">사용여부</th><th className="col-sort">정렬</th><th>수정자</th><th>수정일</th><th className="col-action">삭제</th></tr></thead>
            <tbody>{rows.length === 0 ? <tr className="empty-row"><td colSpan={10}>등록된 사용자 메뉴가 없습니다.</td></tr> : rows.map((menu) => (
              /* 사용자 메뉴 목록 개별 행 영역 */
              <tr key={menu.menuNumb} onClick={() => onMovePath(`${USER_MENU_DETAIL_PREFIX}/${menu.menuNumb}`)}>
                <td>{getMenuDepthMark(menu.menuLevl)}{menu.menuName}</td><td>{menu.menuLevl}뎁스</td><td>{menu.parnName ?? '-'}</td><td>{menu.menuUrlx}</td>
                <td className="col-usee">{getUseeYsnoCodeName(ysnoCodes, menu.showYsno, menu.showYsnoName)}</td>
                <td className="col-usee">{getUseeYsnoCodeName(ysnoCodes, menu.useeYsno, menu.useeYsnoName)}</td>
                <td className="col-sort">{menu.showYsno === DEFAULT_USEE_YSNO ? menu.sortOrdr : ''}</td><td>{menu.updtAdmnName ?? menu.updtAdmn}</td><td>{formatDate(menu.updtDate)}</td>
                <td className="col-action">{permission.deltYsno === 'Y' && <button type="button" className="delete-button" onClick={(event) => { event.stopPropagation(); void deleteMenu(menu.menuNumb) }}>삭제</button>}</td>
              </tr>
            ))}</tbody></table>
        </section>
        {/* 사용자 메뉴 목록 페이지 이동 영역 */}
        <Pagination pageNumber={pageData.pageNumber} totalPages={pageData.totalPages} onPageChange={(pageNumber) => void loadListPage(pageNumber, appliedSearch)}/>
        {permission.writYsno === 'Y' && <button type="button" className="floating-button" onClick={() => onMovePath(USER_MENU_NEW_PATH)}>등록</button>}
      </section>
    )
  }

  // 사용자 메뉴 등록 또는 상세 편집 화면을 반환한다.
  return (
    /* 사용자 메뉴 상세 전체 영역 */
    <section className="menu-detail-page">
      {/* 사용자 메뉴 상세 제목 영역 */}
      <section className="content-header"><h1>{isNew ? '사용자 메뉴 등록' : '사용자 메뉴관리 상세'}</h1></section>
      {/* 사용자 메뉴 기본정보 입력 영역 */}
      <form className="detail-panel" onSubmit={saveMenu}>
        <div className="detail-title"><div><h2>{isNew ? '사용자 메뉴 등록' : '사용자 메뉴 정보'}</h2><p>상위 메뉴를 선택해 최대 3뎁스까지 구성합니다.</p></div></div>
        <section className="table-wrap menu-info-table"><table><tbody>
          <tr><th>상위 메뉴</th><td><select value={form.parnNumb} onChange={(event) => changeForm('parnNumb', event.target.value)}>
            <option value="">없음 (1뎁스)</option>{selectableParentMenus.map((menu) => <option key={menu.menuNumb} value={menu.menuNumb}>{menu.menuLevl}뎁스 / {menu.menuName}</option>)}
          </select></td><th>메뉴 단계</th><td>{expectedMenuLevl}뎁스</td><th>메뉴명</th><td><input value={form.menuName} onChange={(event) => changeForm('menuName', event.target.value)} required/></td></tr>
          <tr><th>URL</th><td><input value={form.menuUrlx} placeholder="그룹 메뉴는 비워둘 수 있습니다." onChange={(event) => changeForm('menuUrlx', event.target.value)}/></td>
            <th>사용여부</th><td><YsnoSelect value={form.useeYsno} codes={ysnoCodes} onChange={(value) => changeForm('useeYsno', value)}/></td>
            <th>햄버거 메뉴 노출</th><td><YsnoSelect value={form.showYsno} codes={ysnoCodes} onChange={(value) => changeForm('showYsno', value)}/></td></tr>
          <tr><th>정렬</th><td>{form.showYsno === DEFAULT_USEE_YSNO ? <input type="number" min="1" value={form.sortOrdr} onChange={(event) => changeForm('sortOrdr', event.target.value)} required/> : '-'}</td><td colSpan={4}/></tr>
        </tbody></table></section>
      </form>
      {/* 사용자 메뉴 등록·수정 감사정보 영역 */}
      {!isNew && detail && <AuditInfoTable regiAdmn={detail.regiAdmn} regiAdmnName={detail.regiAdmnName} regiDate={detail.regiDate} updtAdmn={detail.updtAdmn} updtAdmnName={detail.updtAdmnName} updtDate={detail.updtDate}/>}
      {/* 사용자 메뉴 목록·삭제·저장 명령 영역 */}
      <div className="detail-footer"><div className="detail-footer-left"><button type="button" className="subtle-button" onClick={() => onMovePath(USER_MENU_LIST_PATH)}>목록</button>
        {!isNew && detail && permission.deltYsno === 'Y' && <button type="button" className="delete-button" onClick={() => void deleteMenu(detail.menuNumb)}>삭제</button>}</div>
        <div className="detail-footer-right">{permission.writYsno === 'Y' && <button type="button" disabled={saving} onClick={() => void saveMenu()}>{saving ? '저장 중' : isNew ? '저장' : '수정'}</button>}</div></div>
    </section>
  )
}

/** 여부 공통코드 셀렉트박스를 표시한다. */
function YsnoSelect({ value, codes, onChange }: { value: string; codes: Code[]; onChange: (value: string) => void }) {
  // 여부 공통코드 선택 목록을 반환한다.
  return <select value={value} onChange={(event) => onChange(event.target.value)}>{codes.map((code) => <option key={code.comdCode} value={code.comdCode}>{code.opt1Name ?? code.comdName}</option>)}</select>
}
