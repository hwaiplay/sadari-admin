import { useEffect, useState } from 'react'
import type { FormEvent, KeyboardEvent, MouseEvent } from 'react'
import { getReadingClubs } from '../../api/readingClubApi'
import { AdminDatePicker } from '../../components/AdminDatePicker'
import { Pagination } from '../../components/Pagination'
import { READING_CLUB_DETAIL_PREFIX, READING_CLUB_LIST_PATH } from '../../constants/routes'
import type { PageData } from '../../types/common'
import type { ReadingClub, ReadingClubSearch } from '../../types/readingClub'
import { formatDate } from '../../utils/code'
import { getListPageSnapshot, setListPageSnapshot } from '../../utils/search'

type ReadingClubListPageProps = {
  onMovePath: (path: string) => void
  onError: (message: string | null) => void
}

const DEFAULT_SEARCH: ReadingClubSearch = {
  keyword: '',
  clubStat: '',
  clubVisb: '',
  joinType: '',
  rcrtYsno: '',
  regiDateFrom: '',
  regiDateTo: '',
}

const EMPTY_PAGE: PageData<ReadingClub> = {
  items: [],
  totalCount: 0,
  pageNumber: 1,
  pageSize: 20,
  totalPages: 0,
}

/**
 * 관리자 독서 모임 검색과 운영 현황 목록을 제공한다.
 *
 * @author HanWon.Jang
 * @param onMovePath 화면 경로 이동 함수
 * @param onError 공통 오류 메시지 변경 함수
 * @return 독서 모임 관리 목록 화면
 */
export function ReadingClubListPage({ onMovePath, onError }: ReadingClubListPageProps) {
  const [search, setSearch] = useState<ReadingClubSearch>({ ...DEFAULT_SEARCH })
  const [appliedSearch, setAppliedSearch] = useState<ReadingClubSearch>({ ...DEFAULT_SEARCH })
  const [pageData, setPageData] = useState<PageData<ReadingClub>>(EMPTY_PAGE)
  const [loading, setLoading] = useState(true)

  /** 지정한 검색 조건과 페이지로 독서 모임 목록을 조회한다. */
  const loadReadingClubs = async (
    pageNumber: number,
    targetSearch: ReadingClubSearch,
  ): Promise<void> => {
    // 목록 조회가 시작되었음을 화면에 표시한다.
    setLoading(true)
    // 조회 실패 시 현재 검색값을 유지하고 공통 오류를 표시한다.
    try {
      // 서버 검색 결과를 현재 페이지 상태에 반영한다.
      const result = await getReadingClubs(pageNumber, targetSearch)
      // 조회된 독서 모임 페이지를 화면에 설정한다.
      setPageData(result)
      // 상세 화면에서 돌아올 때 현재 검색 조건과 페이지를 복원하도록 저장한다.
      setListPageSnapshot(READING_CLUB_LIST_PATH, result.pageNumber, targetSearch)
      // 이전 목록 오류를 제거한다.
      onError(null)
    } catch (error: unknown) {
      // "독서 모임 목록을 불러오지 못했습니다."
      onError(error instanceof Error ? error.message : '독서 모임 목록을 불러오지 못했습니다.')
    } finally {
      // 성공 여부와 관계없이 목록 로딩을 종료한다.
      setLoading(false)
    }
  }

  // 첫 진입 시 저장된 검색 조건과 페이지의 독서 모임 목록을 조회한다.
  useEffect(() => {
    let active = true
    // 상세 이동 전에 사용한 독서 모임 목록 조회 상태를 확인한다.
    const snapshot = getListPageSnapshot(READING_CLUB_LIST_PATH, DEFAULT_SEARCH)
    // 복원된 검색 조건으로 목록을 조회한다.
    getReadingClubs(snapshot.pageNumber, snapshot.search)
      .then((clubs) => {
        // 화면이 유지되는 동안에만 조회 결과를 반영한다.
        if (active) {
          // 복원된 목록 페이지를 화면에 설정한다.
          setPageData(clubs)
          // 복원된 검색값을 입력 폼에 설정한다.
          setSearch(snapshot.search)
          // 페이지 이동에도 같은 검색값을 사용하도록 설정한다.
          setAppliedSearch(snapshot.search)
          // 이전 목록 조회 오류를 제거한다.
          onError(null)
          // 초기 목록 로딩을 종료한다.
          setLoading(false)
        }
      })
      .catch((error: unknown) => {
        // 화면이 유지되는 동안에만 초기 조회 오류를 표시한다.
        if (active) {
          // "독서 모임 목록을 불러오지 못했습니다."
          onError(error instanceof Error ? error.message : '독서 모임 목록을 불러오지 못했습니다.')
          // 오류 상태에서도 초기 목록 로딩을 종료한다.
          setLoading(false)
        }
      })
    // 화면 해제 뒤 도착하는 응답이 상태를 변경하지 않도록 차단한다.
    return () => {
      active = false
    }
  }, [onError])

  /** 입력된 검색 조건으로 첫 페이지부터 조회한다. */
  const handleSearch = (event: FormEvent<HTMLFormElement>): void => {
    // 브라우저 기본 폼 전송을 막는다.
    event.preventDefault()
    // 페이지 이동에도 동일 조건을 사용할 검색 스냅샷을 준비한다.
    const nextSearch = { ...search }
    // 적용된 검색 조건을 페이지 이동용 상태에 설정한다.
    setAppliedSearch(nextSearch)
    // 변경된 조건으로 독서 모임 첫 페이지를 조회한다.
    void loadReadingClubs(1, nextSearch)
  }

  /** 검색 조건과 결과를 전체 모임 상태로 초기화한다. */
  const handleReset = (): void => {
    // 검색 폼을 기본값으로 되돌린다.
    const nextSearch = { ...DEFAULT_SEARCH }
    // 기본 검색값을 입력 상태에 설정한다.
    setSearch(nextSearch)
    // 기본 검색값을 페이지 이동용 상태에 설정한다.
    setAppliedSearch(nextSearch)
    // 전체 조건의 독서 모임 첫 페이지를 조회한다.
    void loadReadingClubs(1, nextSearch)
  }

  /** 선택한 표 행의 독서 모임 상세로 이동한다. */
  const moveDetail = (row: HTMLTableRowElement): void => {
    // 표 행에 저장된 모임 번호를 숫자로 변환한다.
    const clubNumb = Number(row.dataset.clubNumber)
    // 정상 모임 번호만 상세 경로에 포함한다.
    if (Number.isInteger(clubNumb) && clubNumb > 0) {
      // 선택한 독서 모임 상세 화면으로 이동한다.
      onMovePath(`${READING_CLUB_DETAIL_PREFIX}/${clubNumb}`)
    }
  }

  /** 마우스로 선택한 독서 모임 상세로 이동한다. */
  const handleRowClick = (event: MouseEvent<HTMLTableRowElement>): void => {
    // 선택한 행의 독서 모임 상세 화면으로 이동한다.
    moveDetail(event.currentTarget)
  }

  /** 키보드로 선택한 독서 모임 상세로 이동한다. */
  const handleRowKeyDown = (event: KeyboardEvent<HTMLTableRowElement>): void => {
    // 링크 역할에 해당하는 키만 상세 이동에 사용한다.
    if (event.key === 'Enter' || event.key === ' ') {
      // Space 키가 표를 스크롤하지 않도록 기본 동작을 막는다.
      event.preventDefault()
      // 키보드로 선택한 독서 모임 상세 화면으로 이동한다.
      moveDetail(event.currentTarget)
    }
  }

  // 독서 모임 검색 폼과 운영 현황 목록을 반환한다.
  return (
    <section className="complaint-page">
      <section className="content-header">
        <h1>독서 모임 관리</h1>
        <div className="status">총 {pageData.totalCount.toLocaleString()}건</div>
      </section>

      <form className="complaint-search" onSubmit={handleSearch}>
        <label>
          <span>통합 검색</span>
          <input
            value={search.keyword}
            maxLength={100}
            placeholder="모임번호, 모임명 또는 모임장"
            onChange={(event) => setSearch({ ...search, keyword: event.target.value })}
          />
        </label>
        <label>
          <span>운영 상태</span>
          <select value={search.clubStat} onChange={(event) => setSearch({ ...search, clubStat: event.target.value })}>
            <option value="">전체</option>
            <option value="ACTIVE">운영 중</option>
            <option value="OWNER_ELECTION">모임장 선거</option>
            <option value="PAUSED">일시 중지</option>
            <option value="CLOSED">종료</option>
          </select>
        </label>
        <label>
          <span>공개 범위</span>
          <select value={search.clubVisb} onChange={(event) => setSearch({ ...search, clubVisb: event.target.value })}>
            <option value="">전체</option>
            <option value="PUBLIC">공개</option>
            <option value="PRIVATE">비공개</option>
          </select>
        </label>
        <label>
          <span>가입 방식</span>
          <select value={search.joinType} onChange={(event) => setSearch({ ...search, joinType: event.target.value })}>
            <option value="">전체</option>
            <option value="OPEN">즉시 가입</option>
            <option value="APPROVAL">승인 가입</option>
            <option value="INVITE">초대 가입</option>
          </select>
        </label>
        <label>
          <span>모집 상태</span>
          <select value={search.rcrtYsno} onChange={(event) => setSearch({ ...search, rcrtYsno: event.target.value })}>
            <option value="">전체</option>
            <option value="Y">모집 중</option>
            <option value="N">모집 중지</option>
          </select>
        </label>
        <label>
          <span>생성일 시작</span>
          <AdminDatePicker value={search.regiDateFrom} ariaLabel="생성일 시작 날짜 선택" onChange={(value) => setSearch({ ...search, regiDateFrom: value })} />
        </label>
        <label>
          <span>생성일 종료</span>
          <AdminDatePicker value={search.regiDateTo} ariaLabel="생성일 종료 날짜 선택" onChange={(value) => setSearch({ ...search, regiDateTo: value })} />
        </label>
        <div className="complaint-search-actions">
          <button type="button" className="subtle-button" onClick={handleReset}>초기화</button>
          <button type="submit">검색</button>
        </div>
      </form>

      <section className="table-wrap complaint-list-table">
        <table>
          <thead>
            <tr>
              <th>모임번호</th>
              <th>모임명</th>
              <th>모임장</th>
              <th>카테고리</th>
              <th>공개·가입</th>
              <th>운영 상태</th>
              <th>모집</th>
              <th>인원</th>
              <th>생성일시</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr className="empty-row"><td colSpan={9}>독서 모임 목록을 불러오고 있습니다.</td></tr>
            ) : pageData.items.length === 0 ? (
              <tr className="empty-row"><td colSpan={9}>검색 조건에 맞는 독서 모임이 없습니다.</td></tr>
            ) : pageData.items.map((club) => (
              <tr
                key={club.clubNumb}
                className="complaint-row"
                role="link"
                tabIndex={0}
                data-club-number={club.clubNumb}
                onClick={handleRowClick}
                onKeyDown={handleRowKeyDown}
              >
                <td><span className="table-link-button">{club.clubNumb}</span></td>
                <td>{club.clubName}</td>
                <td>{club.ownrNumb ? `${club.ownrNick ?? '닉네임 없음'} (${club.ownrNumb})` : '모임장 없음'}</td>
                <td>{club.categoryNames ?? '-'}</td>
                <td>{club.clubVisbName ?? club.clubVisb} · {club.joinTypeName ?? club.joinType}</td>
                <td>{club.clubStatName ?? club.clubStat}</td>
                <td>{club.rcrtYsno === 'Y' ? '모집 중' : '모집 중지'}</td>
                <td>{club.memberCnt}/{club.maxxMemb} (예약 {club.invitedCnt})</td>
                <td className="col-date-time">{formatDate(club.regiDate)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>
      <Pagination
        pageNumber={pageData.pageNumber}
        totalPages={pageData.totalPages}
        onPageChange={(pageNumber) => void loadReadingClubs(pageNumber, appliedSearch)}
      />
    </section>
  )
}
