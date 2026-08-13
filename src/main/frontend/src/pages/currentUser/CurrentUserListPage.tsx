import { useEffect, useState } from 'react'
import type { FormEvent, KeyboardEvent, MouseEvent } from 'react'
import { getCodeList } from '../../api/codeApi'
import { getCurrentUsers } from '../../api/currentUserApi'
import { Pagination } from '../../components/Pagination'
import { AdminDatePicker } from '../../components/AdminDatePicker'
import { USER_PROV, USER_STAT } from '../../constants/codes'
import { CURRENT_USER_DETAIL_PREFIX, CURRENT_USER_LIST_PATH, DELETED_SUSPENSION_PATH } from '../../constants/routes'
import type { Code } from '../../types/code'
import type { PageData } from '../../types/common'
import type { CurrentUser, CurrentUserSearch } from '../../types/currentUser'
import { formatDate } from '../../utils/code'
import { getListPageSnapshot, setListPageSnapshot } from '../../utils/search'

type CurrentUserListPageProps = {
  onMovePath: (path: string) => void
  onError: (message: string | null) => void
}

const DEFAULT_SEARCH: CurrentUserSearch = {
  keyword: '',
  userStat: '',
  userProv: '',
  onbdYsno: '',
  joinDateFrom: '',
  joinDateTo: '',
}

const EMPTY_PAGE: PageData<CurrentUser> = {
  items: [],
  totalCount: 0,
  pageNumber: 1,
  pageSize: 20,
  totalPages: 0,
}

/**
 * 현재 사용자 조회와 검색 화면을 제공한다.
 *
 * @author SeungHyeon.Kang
 * @param onMovePath 화면 경로 이동 함수
 * @param onError 공통 오류 메시지 변경 함수
 * @return 현재 사용자 목록 화면
 */
export function CurrentUserListPage({ onMovePath, onError }: CurrentUserListPageProps) {
  const [search, setSearch] = useState<CurrentUserSearch>({ ...DEFAULT_SEARCH })
  const [appliedSearch, setAppliedSearch] = useState<CurrentUserSearch>({ ...DEFAULT_SEARCH })
  const [statusCodes, setStatusCodes] = useState<Code[]>([])
  const [providerCodes, setProviderCodes] = useState<Code[]>([])
  const [pageData, setPageData] = useState<PageData<CurrentUser>>(EMPTY_PAGE)
  const [loading, setLoading] = useState(true)

  /**
   * 지정한 조건과 페이지로 현재 사용자 목록을 조회한다.
   *
   * @author SeungHyeon.Kang
   * @param pageNumber 조회할 페이지 번호
   * @param targetSearch 적용할 검색 조건
   * @return 반환값이 없다
   */
  const loadCurrentUsers = async (
    pageNumber: number,
    targetSearch: CurrentUserSearch,
  ): Promise<void> => {
    // 새 조회가 시작되었음을 화면에 표시한다.
    setLoading(true)
    try {
      // 서버 검색 결과를 현재 페이지 상태에 반영한다.
      const result = await getCurrentUsers(pageNumber, targetSearch)
      setPageData(result)
      // 상세 화면에서 돌아올 때 현재 사용자 조회 상태를 복원하도록 저장한다.
      setListPageSnapshot(CURRENT_USER_LIST_PATH, result.pageNumber, targetSearch)
      // 이전 오류 메시지를 초기화한다.
      onError(null)
    } catch (error: unknown) {
      // API 오류를 관리자 공통 오류 영역에 표시한다.
      onError(error instanceof Error ? error.message : '현재 사용자 목록을 불러오지 못했습니다.')
    } finally {
      // 성공 여부와 관계없이 로딩 표시를 종료한다.
      setLoading(false)
    }
  }

  // 첫 진입 시 전체 사용자 목록과 상태 공통코드를 함께 조회한다.
  useEffect(() => {
    let active = true
    // 상세 이동 전에 사용한 현재 사용자 목록 조회 상태를 확인한다.
    const snapshot = getListPageSnapshot(CURRENT_USER_LIST_PATH, DEFAULT_SEARCH)
    // 화면에서 사용할 회원 상태명과 기본 목록을 병렬로 조회한다.
    Promise.all([getCodeList(USER_STAT), getCodeList(USER_PROV), getCurrentUsers(snapshot.pageNumber, snapshot.search)])
      .then(([codes, providers, users]) => {
        // 화면이 유지되는 동안에만 조회 결과를 반영한다.
        if (active) {
          setStatusCodes(codes)
          setProviderCodes(providers)
          setPageData(users)
          setSearch(snapshot.search)
          setAppliedSearch(snapshot.search)
          onError(null)
          setLoading(false)
        }
      })
      .catch((error: unknown) => {
        // 화면이 유지되는 동안에만 오류를 표시한다.
        if (active) {
          onError(error instanceof Error ? error.message : '현재 사용자 목록을 불러오지 못했습니다.')
          setLoading(false)
        }
      })
    // 화면 해제 뒤 도착하는 응답이 상태를 변경하지 않도록 차단한다.
    return () => {
      active = false
    }
  }, [onError])

  /**
   * 입력된 조건으로 첫 페이지부터 다시 검색한다.
   *
   * @author SeungHyeon.Kang
   * @param event 검색 폼 제출 이벤트
   * @return 반환값이 없다
   */
  const handleSearch = (event: FormEvent<HTMLFormElement>): void => {
    // 브라우저 기본 폼 전송을 막는다.
    event.preventDefault()
    // 페이지 이동에도 동일 조건을 유지할 검색 스냅샷을 저장한다.
    const nextSearch = { ...search }
    setAppliedSearch(nextSearch)
    // 변경된 검색 조건으로 첫 페이지를 조회한다.
    void loadCurrentUsers(1, nextSearch)
  }

  /**
   * 검색 조건과 결과를 전체 사용자 상태로 초기화한다.
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없다
   */
  const handleReset = (): void => {
    // 입력 조건과 적용 조건을 모두 기본값으로 되돌린다.
    const nextSearch = { ...DEFAULT_SEARCH }
    setSearch(nextSearch)
    setAppliedSearch(nextSearch)
    // 기본 조건의 첫 페이지를 다시 조회한다.
    void loadCurrentUsers(1, nextSearch)
  }

  /**
   * 선택한 사용자 상세 화면으로 이동한다.
   *
   * @author SeungHyeon.Kang
   * @param row 선택된 표 행
   * @return 반환값이 없다
   */
  const moveDetail = (row: HTMLTableRowElement): void => {
    // 행에 저장된 사용자 번호를 숫자로 변환한다.
    const userNumb = Number(row.dataset.userNumb)
    // 정상 사용자 번호만 상세 경로에 포함한다.
    if (Number.isInteger(userNumb) && userNumb > 0) {
      onMovePath(`${CURRENT_USER_DETAIL_PREFIX}/${userNumb}`)
    }
  }

  /**
   * 마우스로 선택한 사용자의 상세 화면으로 이동한다.
   *
   * @author SeungHyeon.Kang
   * @param event 사용자 행 클릭 이벤트
   * @return 반환값이 없다
   */
  const handleRowClick = (event: MouseEvent<HTMLTableRowElement>): void => {
    // 선택한 행의 사용자 상세 화면으로 이동한다.
    moveDetail(event.currentTarget)
  }

  /**
   * 키보드로 선택한 사용자의 상세 화면으로 이동한다.
   *
   * @author SeungHyeon.Kang
   * @param event 사용자 행 키보드 이벤트
   * @return 반환값이 없다
   */
  const handleRowKeyDown = (event: KeyboardEvent<HTMLTableRowElement>): void => {
    // 링크 역할에 해당하는 키만 상세 이동에 사용한다.
    if (event.key === 'Enter' || event.key === ' ') {
      event.preventDefault()
      moveDetail(event.currentTarget)
    }
  }

  /**
   * 현재 사용자 목록 행을 표시한다.
   *
   * @author SeungHyeon.Kang
   * @param user 표시할 현재 사용자
   * @return 현재 사용자 목록 행
   */
  const renderCurrentUserRow = (user: CurrentUser) => (
    <tr
      key={user.userNumb}
      className="current-user-row"
      role="link"
      tabIndex={0}
      data-user-numb={user.userNumb}
      onClick={handleRowClick}
      onKeyDown={handleRowKeyDown}
    >
      <td className="col-user-number"><span className="table-link-button">{user.userNumb}</span></td>
      <td>{user.userNick}</td>
      <td>{user.userStatName ?? user.userStat}</td>
      <td>{user.userProvName ?? user.userProv ?? '-'}</td>
      <td>{user.onbdYsnoName ?? user.onbdYsno}</td>
      <td className="col-count">{user.reportCntt.toLocaleString()}</td>
      <td className="col-date-time">{formatDate(user.lastLognDate)}</td>
      <td className="col-date-time">{formatDate(user.joinDate)}</td>
    </tr>
  )

  // 검색 폼과 조회 결과 목록을 표시한다.
  return (
    <section className="current-user-page">
      {/* 현재 사용자 화면 제목과 검색 결과 건수 */}
      <section className="content-header">
        <h1>현 사용자 관리</h1>
        {/* 현재 사용자 건수와 삭제 회원 제재 화면 이동 영역 */}
        <div className="header-actions">
          <div className="status">총 {pageData.totalCount.toLocaleString()}건</div>
          <button type="button" className="subtle-button" onClick={() => onMovePath(DELETED_SUSPENSION_PATH)}>
            {/* "삭제회원 제재" */}
            삭제회원 제재
          </button>
        </div>
      </section>

      {/* 사용자 검색 조건 */}
      <form className="current-user-search" onSubmit={handleSearch}>
        <label>
          <span>회원번호·닉네임</span>
          <input
            value={search.keyword}
            maxLength={100}
            placeholder="회원번호 또는 닉네임"
            onChange={(event) => setSearch({ ...search, keyword: event.target.value })}
          />
        </label>
        <label>
          <span>회원 상태</span>
          <select value={search.userStat} onChange={(event) => setSearch({ ...search, userStat: event.target.value })}>
            <option value="">전체</option>
            {statusCodes.map((code) => <option key={code.comdCode} value={code.comdCode}>{code.comdName}</option>)}
          </select>
        </label>
        <label>
          <span>가입 제공자</span>
          <select value={search.userProv} onChange={(event) => setSearch({ ...search, userProv: event.target.value })}>
            <option value="">전체</option>
            {providerCodes.map((code) => (
              <option key={code.comdCode} value={code.comdCode}>{code.comdName}</option>
            ))}
          </select>
        </label>
        <label>
          <span>온보딩</span>
          <select value={search.onbdYsno} onChange={(event) => setSearch({ ...search, onbdYsno: event.target.value })}>
            <option value="">전체</option>
            <option value="Y">완료</option>
            <option value="N">미완료</option>
          </select>
        </label>
        <label>
          <span>가입일 시작</span>
          <AdminDatePicker value={search.joinDateFrom} ariaLabel="가입일 시작 날짜 선택" onChange={(value) => setSearch({ ...search, joinDateFrom: value })} />
        </label>
        <label>
          <span>가입일 종료</span>
          <AdminDatePicker value={search.joinDateTo} ariaLabel="가입일 종료 날짜 선택" onChange={(value) => setSearch({ ...search, joinDateTo: value })} />
        </label>
        {/* 검색 실행과 초기화 버튼 */}
        <div className="current-user-search-actions">
          <button type="button" className="subtle-button" onClick={handleReset}>초기화</button>
          <button type="submit">검색</button>
        </div>
      </form>

      {/* 현재 사용자 검색 결과 */}
      <section className="table-wrap current-user-list-table">
        <table>
          <thead>
            <tr>
              <th className="col-user-number">회원번호</th>
              <th>닉네임</th>
              <th>회원 상태</th>
              <th>가입 제공자</th>
              <th>온보딩</th>
              <th className="col-count">독후감</th>
              <th className="col-date-time">최근 로그인</th>
              <th className="col-date-time">가입일</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr className="empty-row"><td colSpan={8}>현재 사용자 목록을 불러오고 있습니다.</td></tr>
            ) : pageData.items.length === 0 ? (
              <tr className="empty-row"><td colSpan={8}>검색 조건에 맞는 사용자가 없습니다.</td></tr>
            ) : pageData.items.map(renderCurrentUserRow)}
          </tbody>
        </table>
      </section>
      {/* 검색 결과 페이지 이동 */}
      <Pagination
        pageNumber={pageData.pageNumber}
        totalPages={pageData.totalPages}
        onPageChange={(pageNumber) => void loadCurrentUsers(pageNumber, appliedSearch)}
      />
    </section>
  )
}
