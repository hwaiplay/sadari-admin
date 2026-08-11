import { useEffect, useState } from 'react'
import type { FormEvent, KeyboardEvent, MouseEvent } from 'react'
import { getCodeList } from '../../api/codeApi'
import { getScheduleLogs } from '../../api/scheduleLogApi'
import { SCHD_CODE } from '../../constants/codes'
import { SCHEDULE_LOG_DETAIL_PREFIX, SCHEDULE_LOG_LIST_PATH } from '../../constants/routes'
import type { Code } from '../../types/code'
import type { ScheduleLog, ScheduleLogSearch } from '../../types/scheduleLog'
import { formatDate } from '../../utils/code'
import { formatExecutionTime, getScheduleStatusClass } from '../../utils/scheduleLog'
import { Pagination } from '../../components/Pagination'
import type { PageData } from '../../types/common'
import { getListPageSnapshot, setListPageSnapshot } from '../../utils/search'

type ScheduleLogListPageProps = {
  onMovePath: (path: string) => void
  onError: (message: string | null) => void
}

const DEFAULT_SEARCH: ScheduleLogSearch = {
  keyword: '',
  schdCode: '',
  execStat: '',
  strtDateFrom: '',
  strtDateTo: '',
}

/**
 * 스케줄러 실행 결과 목록 화면을 제공한다
 *
 * @author SeungHyeon.Kang
 * @param onMovePath 화면 경로 이동 함수
 * @param onError 공통 오류 메시지 변경 함수
 * @return 스케줄러 로그 목록 화면
 */
export function ScheduleLogListPage({ onMovePath, onError }: ScheduleLogListPageProps) {
  const [scheduleLogs, setScheduleLogs] = useState<ScheduleLog[]>([])
  const [pageData, setPageData] = useState<PageData<ScheduleLog>>({ items: [], totalCount: 0, pageNumber: 1, pageSize: 20, totalPages: 0 })
  const [loading, setLoading] = useState(true)
  const [scheduleCodes, setScheduleCodes] = useState<Code[]>([])
  const [search, setSearch] = useState<ScheduleLogSearch>(DEFAULT_SEARCH)
  const [appliedSearch, setAppliedSearch] = useState<ScheduleLogSearch>(DEFAULT_SEARCH)

  /**
   * 선택한 스케줄러 로그 행의 상세 화면으로 이동한다
   *
   * @author SeungHyeon.Kang
   * @param event 스케줄러 로그 행 클릭 이벤트
   * @return 반환값이 없다
   */
  const handleDetailMove = (event: MouseEvent<HTMLTableRowElement>): void => {
    const runxNumb = Number(event.currentTarget.dataset.runxNumb)
    // 유효한 실행 번호인 경우에만 상세 경로를 생성한다
    if (Number.isInteger(runxNumb) && runxNumb > 0) {
      onMovePath(`${SCHEDULE_LOG_DETAIL_PREFIX}/${runxNumb}`)
    }
  }

  /**
   * 키보드로 스케줄러 로그 행을 선택하면 상세 화면으로 이동한다
   *
   * @author SeungHyeon.Kang
   * @param event 스케줄러 로그 행 키보드 이벤트
   * @return 반환값이 없다
   */
  const handleDetailKeyDown = (event: KeyboardEvent<HTMLTableRowElement>): void => {
    // 링크 역할의 행은 Enter 또는 Space 입력에만 상세 이동을 수행한다
    if (event.key === 'Enter' || event.key === ' ') {
      event.preventDefault()
      const runxNumb = Number(event.currentTarget.dataset.runxNumb)
      // 유효한 실행 번호인 경우에만 상세 경로를 생성한다
      if (Number.isInteger(runxNumb) && runxNumb > 0) {
        onMovePath(`${SCHEDULE_LOG_DETAIL_PREFIX}/${runxNumb}`)
      }
    }
  }

  /**
   * 스케줄러 실행 결과 한 행을 표시한다
   *
   * @author SeungHyeon.Kang
   * @param scheduleLog 스케줄러 실행 결과
   * @return 스케줄러 실행 결과 표 행
   */
  const renderScheduleLogRow = (scheduleLog: ScheduleLog) => (
    <tr key={scheduleLog.runxNumb} className="schedule-detail-row" role="link" tabIndex={0}
        data-runx-numb={scheduleLog.runxNumb} onClick={handleDetailMove} onKeyDown={handleDetailKeyDown}>
      <td className="col-run-number">
        <span className="table-link-button">{scheduleLog.runxNumb}</span>
      </td>
      <td>{scheduleLog.schdCodeName ?? scheduleLog.schdCode}</td>
      <td>{scheduleLog.methName}</td>
      <td className="col-schedule-status"><span className={getScheduleStatusClass(scheduleLog.execStat)}>{scheduleLog.execStat}</span></td>
      <td className="col-date-time">{formatDate(scheduleLog.strtDate)}</td>
      <td className="col-date-time">{formatDate(scheduleLog.fnshDate)}</td>
      <td className="col-count">{scheduleLog.trgtCntt.toLocaleString()}</td>
      <td className="col-count success-count">{scheduleLog.succCntt.toLocaleString()}</td>
      <td className="col-count fail-count">{scheduleLog.failCntt.toLocaleString()}</td>
      <td className="col-execution-time">{formatExecutionTime(scheduleLog.execMsec)}</td>
    </tr>
  )

  // 화면 진입 시 API 완료 결과만 상태에 반영하여 Effect의 동기 상태 변경을 방지한다
  useEffect(() => {
    let active = true
    // 상세 이동 전에 사용한 스케줄러 로그 목록 조회 상태를 확인한다
    const snapshot = getListPageSnapshot(SCHEDULE_LOG_LIST_PATH, DEFAULT_SEARCH)

    // 최신 스케줄러 실행 결과를 서버에서 조회한다
    Promise.all([getScheduleLogs(snapshot.pageNumber, snapshot.search), getCodeList(SCHD_CODE)])
      .then(([result, codes]) => {
        // 화면이 유지되는 동안 도착한 응답만 상태에 반영한다
        if (active) {
          onError(null)
          setPageData(result)
          setScheduleLogs(result.items)
          setScheduleCodes(codes)
          setSearch(snapshot.search)
          setAppliedSearch(snapshot.search)
          setLoading(false)
        }
      })
      .catch((error: unknown) => {
        // 화면이 유지되는 동안 발생한 API 오류만 공통 오류 영역에 표시한다
        if (active) {
          onError(error instanceof Error ? error.message : '스케줄러 로그 목록을 불러오지 못했습니다.')
          setLoading(false)
        }
      })

    // 화면이 해제된 뒤 도착한 API 응답이 상태를 변경하지 않도록 정리한다
    return () => {
      active = false
    }
  }, [onError])

  /**
   * 스케줄러 실행 결과 목록 페이지를 조회한다
   *
   * @author SeungHyeon.Kang
   * @param pageNumber 조회할 페이지 번호
   * @param targetSearch 적용할 검색 조건
   * @return 반환값이 없다
   */
  const loadListPage = async (pageNumber: number, targetSearch: ScheduleLogSearch): Promise<void> => {
    try {
      const result = await getScheduleLogs(pageNumber, targetSearch)
      setPageData(result)
      setScheduleLogs(result.items)
      setAppliedSearch(targetSearch)
      // 상세 화면에서 돌아올 때 현재 스케줄러 로그 조회 상태를 복원하도록 저장한다
      setListPageSnapshot(SCHEDULE_LOG_LIST_PATH, result.pageNumber, targetSearch)
      onError(null)
    } catch (error: unknown) {
      onError(error instanceof Error ? error.message : '스케줄러 로그 목록을 불러오지 못했습니다.')
    }
  }

  /**
   * 스케줄러 로그 검색 조건을 첫 페이지부터 적용한다
   *
   * @author SeungHyeon.Kang
   * @param event 검색 폼 제출 이벤트
   * @return 반환값이 없다
   */
  const handleSearch = async (event: FormEvent<HTMLFormElement>): Promise<void> => {
    event.preventDefault()
    await loadListPage(1, search)
  }

  /**
   * 스케줄러 로그 검색 조건을 초기화한다
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없다
   */
  const handleSearchReset = async (): Promise<void> => {
    setSearch(DEFAULT_SEARCH)
    await loadListPage(1, DEFAULT_SEARCH)
  }

  return (
    <>
      {/* 스케줄러 실행 결과 목록 전체 영역 */}
      <section className="schedule-log-page">
        {/* 스케줄러 실행 결과 제목과 전체 건수 영역 */}
        <section className="content-header">
          <h1>스케줄러 로그 확인</h1>
          <div className="status">총 {pageData.totalCount}건</div>
        </section>

        {/* 스케줄러와 실행 결과 특성에 맞는 검색 조건 영역 */}
        <form className="list-search" onSubmit={(event) => void handleSearch(event)}>
          <label>
            <span>검색어</span>
            <input value={search.keyword} placeholder="실행번호 또는 메서드명"
                   onChange={(event) => setSearch({ ...search, keyword: event.target.value })} />
          </label>
          <label>
            <span>스케줄러</span>
            <select value={search.schdCode}
                    onChange={(event) => setSearch({ ...search, schdCode: event.target.value })}>
              <option value="">전체</option>
              {scheduleCodes.map((code) => (
                <option key={code.comdCode} value={code.comdCode}>{code.opt1Name ?? code.comdName}</option>
              ))}
            </select>
          </label>
          <label>
            <span>실행상태</span>
            <select value={search.execStat}
                    onChange={(event) => setSearch({ ...search, execStat: event.target.value })}>
              <option value="">전체</option>
              <option value="SUCCESS">성공</option>
              <option value="PARTIAL">부분 성공</option>
              <option value="FAILURE">실패</option>
              <option value="NO_DATA">대상 없음</option>
              <option value="RUNNING">실행 중</option>
            </select>
          </label>
          <label>
            <span>시작일(From)</span>
            <input type="date" value={search.strtDateFrom}
                   onChange={(event) => setSearch({ ...search, strtDateFrom: event.target.value })} />
          </label>
          <label>
            <span>시작일(To)</span>
            <input type="date" value={search.strtDateTo}
                   onChange={(event) => setSearch({ ...search, strtDateTo: event.target.value })} />
          </label>
          <div className="list-search-actions">
            <button type="button" className="subtle-button"
                    onClick={() => void handleSearchReset()}>초기화</button>
            <button type="submit">검색</button>
          </div>
        </form>

        {/* 스케줄러 실행 결과 목록 영역 */}
        <section className="table-wrap schedule-log-table">
          <table>
            <thead>
              <tr>
                <th className="col-run-number">실행번호</th>
                <th>스케줄러명</th>
                <th>메서드명</th>
                <th className="col-schedule-status">상태</th>
                <th className="col-date-time">시작일시</th>
                <th className="col-date-time">종료일시</th>
                <th className="col-count">대상</th>
                <th className="col-count">성공</th>
                <th className="col-count">실패</th>
                <th className="col-execution-time">소요시간</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr className="empty-row"><td colSpan={10}>스케줄러 로그를 불러오고 있습니다.</td></tr>
              ) : scheduleLogs.length === 0 ? (
                <tr className="empty-row"><td colSpan={10}>등록된 스케줄러 로그가 없습니다.</td></tr>
              ) : scheduleLogs.map(renderScheduleLogRow)}
            </tbody>
          </table>
        </section>
        <Pagination pageNumber={pageData.pageNumber} totalPages={pageData.totalPages}
                    onPageChange={(pageNumber) => void loadListPage(pageNumber, appliedSearch)} />
      </section>
    </>
  )
}
