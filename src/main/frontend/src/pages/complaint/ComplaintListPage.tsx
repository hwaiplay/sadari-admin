import { useEffect, useState } from 'react'
import type { FormEvent, KeyboardEvent, MouseEvent } from 'react'
import { getCodeList } from '../../api/codeApi'
import { getComplaints } from '../../api/complaintApi'
import { Pagination } from '../../components/Pagination'
import { CMPL_RSON, CMPL_STAT, CMPL_TAGT } from '../../constants/codes'
import { COMPLAINT_DETAIL_PREFIX, COMPLAINT_LIST_PATH } from '../../constants/routes'
import type { Code } from '../../types/code'
import type { PageData } from '../../types/common'
import type { Complaint, ComplaintSearch } from '../../types/complaint'
import { formatDate } from '../../utils/code'
import { getListPageSnapshot, setListPageSnapshot } from '../../utils/search'

type ComplaintListPageProps = {
  onMovePath: (path: string) => void
  onError: (message: string | null) => void
}

const DEFAULT_SEARCH: ComplaintSearch = {
  cmplNumb: '',
  cmplStat: 'CMPL_RECEIVED',
  tagtType: '',
  tagtNumb: '',
  cmplRson: '',
  reporterKeyword: '',
  regiDateFrom: '',
  regiDateTo: '',
}

const EMPTY_PAGE: PageData<Complaint> = {
  items: [],
  totalCount: 0,
  pageNumber: 1,
  pageSize: 20,
  totalPages: 0,
}

/**
 * 관리자 신고 검색과 처리 현황 목록을 제공한다
 *
 * @author SeungHyeon.Kang
 * @param onMovePath 화면 경로 이동 함수
 * @param onError 공통 오류 메시지 변경 함수
 * @return 신고 관리 목록 화면
 */
export function ComplaintListPage({ onMovePath, onError }: ComplaintListPageProps) {
  const [search, setSearch] = useState<ComplaintSearch>({ ...DEFAULT_SEARCH })
  const [appliedSearch, setAppliedSearch] = useState<ComplaintSearch>({ ...DEFAULT_SEARCH })
  const [targetCodes, setTargetCodes] = useState<Code[]>([])
  const [reasonCodes, setReasonCodes] = useState<Code[]>([])
  const [statusCodes, setStatusCodes] = useState<Code[]>([])
  const [pageData, setPageData] = useState<PageData<Complaint>>(EMPTY_PAGE)
  const [loading, setLoading] = useState(true)

  /**
   * 지정한 조건과 페이지로 신고 목록을 조회한다
   *
   * @author SeungHyeon.Kang
   * @param pageNumber 조회할 페이지 번호
   * @param targetSearch 적용할 신고 검색 조건
   * @return 반환값이 없다
   */
  const loadComplaints = async (pageNumber: number, targetSearch: ComplaintSearch): Promise<void> => {
    // 신고 목록의 새 조회가 시작되었음을 표시한다
    setLoading(true)
    // 조회 실패 시 기존 검색값을 유지하고 공통 오류를 표시한다
    try {
      // 서버 신고 목록 페이지를 현재 화면에 반영한다
      const result = await getComplaints(pageNumber, targetSearch)
      setPageData(result)
      // 상세 화면에서 돌아올 때 현재 신고 조회 상태를 복원하도록 저장한다
      setListPageSnapshot(COMPLAINT_LIST_PATH, result.pageNumber, targetSearch)
      // 이전 신고 목록 조회 오류를 제거한다
      onError(null)
    } catch (error: unknown) {
      // "신고 목록을 불러오지 못했습니다."
      onError(error instanceof Error ? error.message : '신고 목록을 불러오지 못했습니다.')
    } finally {
      // 성공 여부와 관계없이 신고 목록 로딩을 종료한다
      setLoading(false)
    }
  }

  // 첫 진입 시 기본 접수 신고 목록과 세 종류의 신고 공통코드를 조회한다
  useEffect(() => {
    let active = true
    // 상세 이동 전에 사용한 신고 목록 조회 상태를 확인한다
    const snapshot = getListPageSnapshot(COMPLAINT_LIST_PATH, DEFAULT_SEARCH)
    // 신고 검색 셀렉트와 기본 접수 목록 데이터를 병렬로 조회한다
    Promise.all([
      getCodeList(CMPL_TAGT),
      getCodeList(CMPL_RSON),
      getCodeList(CMPL_STAT),
      getComplaints(snapshot.pageNumber, snapshot.search),
    ])
      .then(([targets, reasons, statuses, complaints]) => {
        // 화면이 유지되는 동안에만 신고 목록과 검색 코드를 반영한다
        if (active) {
          // 활성 신고 대상 유형을 검색 선택지에 설정한다
          setTargetCodes(targets.filter((code) => code.useeYsno !== 'N'))
          // 활성 신고 사유를 검색 선택지에 설정한다
          setReasonCodes(reasons.filter((code) => code.useeYsno !== 'N'))
          // 활성 신고 처리 상태를 검색 선택지에 설정한다
          setStatusCodes(statuses.filter((code) => code.useeYsno !== 'N'))
          // 기본 접수 신고 목록을 화면에 설정한다
          setPageData(complaints)
          // 복원된 신고 검색 조건을 입력과 페이지 이동 조건에 함께 설정한다
          setSearch(snapshot.search)
          setAppliedSearch(snapshot.search)
          // 이전 신고 목록 오류를 제거한다
          onError(null)
          // 신고 목록 로딩을 종료한다
          setLoading(false)
        }
      })
      .catch((error: unknown) => {
        // 화면이 유지되는 동안에만 신고 목록 초기 조회 오류를 표시한다
        if (active) {
          // "신고 목록을 불러오지 못했습니다."
          onError(error instanceof Error ? error.message : '신고 목록을 불러오지 못했습니다.')
          // 오류 상태에서도 신고 목록 로딩을 종료한다
          setLoading(false)
        }
      })
    // 화면 해제 뒤 도착하는 응답이 상태를 변경하지 않도록 차단한다
    return () => {
      active = false
    }
  }, [onError])

  /**
   * 입력된 신고 검색 조건으로 첫 페이지부터 다시 조회한다
   *
   * @author SeungHyeon.Kang
   * @param event 검색 폼 제출 이벤트
   * @return 반환값이 없다
   */
  const handleSearch = (event: FormEvent<HTMLFormElement>): void => {
    // 브라우저 기본 폼 전송을 막는다
    event.preventDefault()
    // 페이지 이동에도 동일 조건을 사용할 검색 스냅샷을 저장한다
    const nextSearch = { ...search }
    // 현재 검색 조건을 페이지 이동용 적용 조건으로 설정한다
    setAppliedSearch(nextSearch)
    // 변경된 검색 조건으로 신고 첫 페이지를 조회한다
    void loadComplaints(1, nextSearch)
  }

  /**
   * 신고 검색 조건과 결과를 기본 접수 상태로 초기화한다
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없다
   */
  const handleReset = (): void => {
    // 입력 조건과 적용 조건을 기본 접수 상태로 되돌린다
    const nextSearch = { ...DEFAULT_SEARCH }
    // 검색 폼을 기본값으로 설정한다
    setSearch(nextSearch)
    // 페이지 이동용 적용 검색값을 기본값으로 설정한다
    setAppliedSearch(nextSearch)
    // 기본 접수 조건의 신고 첫 페이지를 조회한다
    void loadComplaints(1, nextSearch)
  }

  /**
   * 선택한 신고 상세 화면으로 이동한다
   *
   * @author SeungHyeon.Kang
   * @param row 선택된 신고 표 행
   * @return 반환값이 없다
   */
  const moveDetail = (row: HTMLTableRowElement): void => {
    // 표 행에 저장된 신고번호를 숫자로 변환한다
    const cmplNumb = Number(row.dataset.complaintNumber)
    // 정상 신고번호만 상세 경로에 포함한다
    if (Number.isInteger(cmplNumb) && cmplNumb > 0) {
      // 선택한 신고 상세 화면으로 이동한다
      onMovePath(`${COMPLAINT_DETAIL_PREFIX}/${cmplNumb}`)
    }
  }

  /**
   * 마우스로 선택한 신고 상세 화면으로 이동한다
   *
   * @author SeungHyeon.Kang
   * @param event 신고 행 클릭 이벤트
   * @return 반환값이 없다
   */
  const handleRowClick = (event: MouseEvent<HTMLTableRowElement>): void => {
    // 선택한 신고 행의 상세 화면으로 이동한다
    moveDetail(event.currentTarget)
  }

  /**
   * 키보드로 선택한 신고 상세 화면으로 이동한다
   *
   * @author SeungHyeon.Kang
   * @param event 신고 행 키보드 이벤트
   * @return 반환값이 없다
   */
  const handleRowKeyDown = (event: KeyboardEvent<HTMLTableRowElement>): void => {
    // 링크 역할에 해당하는 키만 신고 상세 이동에 사용한다
    if (event.key === 'Enter' || event.key === ' ') {
      // Space 키가 표를 스크롤하지 않도록 기본 동작을 막는다
      event.preventDefault()
      // 키보드로 선택한 신고 상세 화면으로 이동한다
      moveDetail(event.currentTarget)
    }
  }

  /**
   * 신고 목록 행을 표시한다
   *
   * @author SeungHyeon.Kang
   * @param complaint 표시할 신고
   * @return 신고 목록 행
   */
  const renderComplaintRow = (complaint: Complaint) => (
    <tr
      key={complaint.cmplNumb}
      className="complaint-row"
      role="link"
      tabIndex={0}
      data-complaint-number={complaint.cmplNumb}
      onClick={handleRowClick}
      onKeyDown={handleRowKeyDown}
    >
      <td className="col-history-number"><span className="table-link-button">{complaint.cmplNumb}</span></td>
      <td><span className={`complaint-status ${complaint.cmplStat.toLowerCase()}`}>{complaint.cmplStatName ?? complaint.cmplStat}</span></td>
      <td>{complaint.tagtTypeName ?? complaint.tagtType}</td>
      <td className="col-target-number">{complaint.tagtNumb}</td>
      <td>{complaint.cmplRsonName ?? complaint.cmplRson}</td>
      <td>{complaint.userNumb ? `${complaint.reporterNick ?? '닉네임 없음'} (${complaint.userNumb})` : '탈퇴한 사용자'}</td>
      <td>{complaint.procAdmnName ?? '-'}</td>
      <td className="col-date-time">{formatDate(complaint.regiDate)}</td>
      <td className="col-date-time">{formatDate(complaint.procDate) || '-'}</td>
    </tr>
  )

  // 신고 검색 폼과 처리 현황 목록을 반환한다
  return (
    <section className="complaint-page">
      {/* 신고 관리 화면 제목과 검색 결과 건수 */}
      <section className="content-header">
        <h1>신고 관리</h1>
        <div className="status">총 {pageData.totalCount.toLocaleString()}건</div>
      </section>

      {/* 신고 검색 조건 */}
      <form className="complaint-search" onSubmit={handleSearch}>
        <label>
          <span>신고번호</span>
          <input
            type="number"
            min="1"
            value={search.cmplNumb}
            placeholder="신고번호"
            onChange={(event) => setSearch({ ...search, cmplNumb: event.target.value })}
          />
        </label>
        <label>
          <span>처리 상태</span>
          <select value={search.cmplStat} onChange={(event) => setSearch({ ...search, cmplStat: event.target.value })}>
            <option value="">전체</option>
            {statusCodes.map((code) => <option key={code.comdCode} value={code.comdCode}>{code.comdName}</option>)}
          </select>
        </label>
        <label>
          <span>대상 유형</span>
          <select value={search.tagtType} onChange={(event) => setSearch({ ...search, tagtType: event.target.value })}>
            <option value="">전체</option>
            {targetCodes.map((code) => <option key={code.comdCode} value={code.comdCode}>{code.comdName}</option>)}
          </select>
        </label>
        <label>
          <span>대상번호</span>
          <input
            type="number"
            min="1"
            value={search.tagtNumb}
            placeholder="대상번호"
            onChange={(event) => setSearch({ ...search, tagtNumb: event.target.value })}
          />
        </label>
        <label>
          <span>신고 사유</span>
          <select value={search.cmplRson} onChange={(event) => setSearch({ ...search, cmplRson: event.target.value })}>
            <option value="">전체</option>
            {reasonCodes.map((code) => <option key={code.comdCode} value={code.comdCode}>{code.comdName}</option>)}
          </select>
        </label>
        <label>
          <span>신고자</span>
          <input
            value={search.reporterKeyword}
            maxLength={100}
            placeholder="회원번호 또는 닉네임"
            onChange={(event) => setSearch({ ...search, reporterKeyword: event.target.value })}
          />
        </label>
        <label>
          <span>접수일 시작</span>
          <input type="date" value={search.regiDateFrom} onChange={(event) => setSearch({ ...search, regiDateFrom: event.target.value })} />
        </label>
        <label>
          <span>접수일 종료</span>
          <input type="date" value={search.regiDateTo} onChange={(event) => setSearch({ ...search, regiDateTo: event.target.value })} />
        </label>
        {/* 신고 검색 실행과 초기화 버튼 */}
        <div className="complaint-search-actions">
          <button type="button" className="subtle-button" onClick={handleReset}>초기화</button>
          <button type="submit">검색</button>
        </div>
      </form>

      {/* 신고 검색 결과 */}
      <section className="table-wrap complaint-list-table">
        <table>
          <thead>
            <tr>
              <th className="col-history-number">신고번호</th>
              <th>상태</th>
              <th>대상 유형</th>
              <th className="col-target-number">대상번호</th>
              <th>신고 사유</th>
              <th>신고자</th>
              <th>담당자</th>
              <th className="col-date-time">접수일시</th>
              <th className="col-date-time">처리일시</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr className="empty-row"><td colSpan={9}>신고 목록을 불러오고 있습니다.</td></tr>
            ) : pageData.items.length === 0 ? (
              <tr className="empty-row"><td colSpan={9}>검색 조건에 맞는 신고가 없습니다.</td></tr>
            ) : pageData.items.map(renderComplaintRow)}
          </tbody>
        </table>
      </section>
      {/* 신고 검색 결과 페이지 이동 */}
      <Pagination
        pageNumber={pageData.pageNumber}
        totalPages={pageData.totalPages}
        onPageChange={(pageNumber) => void loadComplaints(pageNumber, appliedSearch)}
      />
    </section>
  )
}
