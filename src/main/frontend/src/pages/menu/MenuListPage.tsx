import { useState } from 'react'
import type { FormEvent } from 'react'
import { MENU_DETAIL_PREFIX, MENU_LIST_PATH, MENU_NEW_PATH } from '../../constants/routes'
import type { Code } from '../../types/code'
import type { Menu, MenuSearch } from '../../types/menu'
import { formatDate, getUseeYsnoCodeName } from '../../utils/code'
import { useMenuPermission } from '../../contexts/useMenuPermission'
import { Pagination } from '../../components/Pagination'
import type { PageData } from '../../types/common'
import { getListPageSnapshot } from '../../utils/search'

type MenuListPageProps = {
  menuRows: Menu[]
  pageData: PageData<Menu>
  useeYsnoCodes: Code[]
  onSearch: (pageNumber: number, search: MenuSearch) => void
  onMovePath: (path: string) => void
  onDelete: (menu: Menu, search: MenuSearch) => void
}

const DEFAULT_SEARCH: MenuSearch = {
  keyword: '',
  useeYsno: '',
}

/**
 * 메뉴관리 목록 화면
 * @Author SeungHyeon.Kang
 * @param menuRows
 * @param useeYsnoCodes
 * @param onMovePath
 * @param onDelete
 * @return
 */
export function MenuListPage({
  menuRows,
  pageData,
  useeYsnoCodes,
  onSearch,
  onMovePath,
  onDelete,
}: MenuListPageProps) {
  const permission = useMenuPermission()
  // 상세 화면 이전에 사용한 메뉴 검색 조건을 목록 입력값으로 복원한다
  const initialSnapshot = getListPageSnapshot(MENU_LIST_PATH, DEFAULT_SEARCH)
  const [search, setSearch] = useState<MenuSearch>(initialSnapshot.search)
  const [appliedSearch, setAppliedSearch] = useState<MenuSearch>(initialSnapshot.search)

  /**
   * 입력한 메뉴 조건으로 첫 페이지를 검색한다
   *
   * @author SeungHyeon.Kang
   * @param event 메뉴 검색 폼 제출 이벤트
   * @return 반환값이 없다
   */
  const handleSearch = (event: FormEvent<HTMLFormElement>): void => {
    // 브라우저 기본 폼 전송을 막는다
    event.preventDefault()
    // 페이지 이동에도 유지할 검색 조건 스냅샷을 저장한다
    const nextSearch = { ...search }
    // 적용 검색 조건을 갱신한다
    setAppliedSearch(nextSearch)
    // 변경된 조건으로 첫 페이지를 조회한다
    onSearch(1, nextSearch)
  }

  /**
   * 메뉴 검색 조건과 결과를 전체 목록으로 초기화한다
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없다
   */
  const handleReset = (): void => {
    // 검색 입력과 적용 조건을 기본값으로 되돌린다
    const nextSearch = { ...DEFAULT_SEARCH }
    // 검색 입력 조건을 초기화한다
    setSearch(nextSearch)
    // 적용 검색 조건을 초기화한다
    setAppliedSearch(nextSearch)
    // 전체 메뉴의 첫 페이지를 조회한다
    onSearch(1, nextSearch)
  }

  // 메뉴 검색 조건과 결과 목록을 반환한다
  return (
    <section className="menu-manage">
      {/* 메뉴 목록 제목과 검색 결과 건수 영역 */}
      <section className="content-header">
        <h1>메뉴관리</h1>
        <div className="status">총 {pageData.totalCount}건</div>
      </section>
      {/* 관리자 메뉴 검색 조건 영역 */}
      <form className="list-search" onSubmit={handleSearch}>
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
          <span>사용여부</span>
          <select value={search.useeYsno} onChange={(event) => setSearch({ ...search, useeYsno: event.target.value })}>
            <option value="">전체</option>
            {useeYsnoCodes.map((code) => (
              <option key={code.comdCode} value={code.comdCode}>{code.comdName}</option>
            ))}
          </select>
        </label>
        {/* 메뉴 검색 실행과 초기화 버튼 영역 */}
        <div className="list-search-actions">
          <button type="button" className="subtle-button" onClick={handleReset}>초기화</button>
          <button type="submit">검색</button>
        </div>
      </form>
      {/* 관리자 메뉴 검색 결과 영역 */}
      <section className="table-wrap menu-list-table">
        <table>
          <thead>
            <tr>
              <th>메뉴명</th>
              <th>URL</th>
              <th className="col-usee">사용여부</th>
              <th className="col-sort">정렬</th>
              <th>수정자</th>
              <th>수정일</th>
              <th className="col-action">삭제</th>
            </tr>
          </thead>
          <tbody>
            {menuRows.map((menu) => (
              <tr key={`${menu.menuNumb}-${menu.subxNumb}`} onClick={() => onMovePath(`${MENU_DETAIL_PREFIX}/${menu.menuNumb}/${menu.subxNumb}`)}>
                <td>{menu.menuName}</td>
                <td>{menu.menuUrlx}</td>
                <td className="col-usee">{getUseeYsnoCodeName(useeYsnoCodes, menu.useeYsno, menu.useeYsnoName)}</td>
                <td className="col-sort">{menu.sortOrdr}</td>
                <td>{menu.updtAdmnName ?? menu.updtAdmn}</td>
                <td>{formatDate(menu.updtDate)}</td>
                <td className="col-action">
                  {permission.deltYsno === 'Y' && <button type="button" className="delete-button" onClick={(event) => { event.stopPropagation(); onDelete(menu, appliedSearch) }}>삭제</button>}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>
      {/* 메뉴 검색 결과 페이지 이동 영역 */}
      <Pagination
        pageNumber={pageData.pageNumber}
        totalPages={pageData.totalPages}
        onPageChange={(pageNumber) => onSearch(pageNumber, appliedSearch)}
      />
      {permission.writYsno === 'Y' && <button type="button" className="floating-button" onClick={() => onMovePath(MENU_NEW_PATH)}>등록</button>}
    </section>
  )
}
