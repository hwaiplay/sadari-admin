import { useEffect, useState } from 'react'
import { getScheduleFailures, getScheduleLog } from '../../api/scheduleLogApi'
import { SCHEDULE_LOG_LIST_PATH } from '../../constants/routes'
import type { ScheduleFail, ScheduleLog } from '../../types/scheduleLog'
import { formatDate } from '../../utils/code'
import { formatExecutionTime, getScheduleStatusClass } from '../../utils/scheduleLog'

type ScheduleLogDetailPageProps = {
  runxNumb: number
  pageTitle: string
  onMovePath: (path: string) => void
  onError: (message: string | null) => void
}

/**
 * 스케줄러 실행 결과와 실패 상세 조회 화면을 제공한다
 *
 * @author SeungHyeon.Kang
 * @param runxNumb 스케줄러 실행 번호
 * @param pageTitle 상세 화면 제목
 * @param onMovePath 화면 경로 이동 함수
 * @param onError 공통 오류 메시지 변경 함수
 * @return 스케줄러 로그 상세 화면
 */
export function ScheduleLogDetailPage({ runxNumb, pageTitle, onMovePath, onError }: ScheduleLogDetailPageProps) {
  const [scheduleLog, setScheduleLog] = useState<ScheduleLog | null>(null)
  const [scheduleFailures, setScheduleFailures] = useState<ScheduleFail[]>([])
  const [loading, setLoading] = useState(true)

  /**
   * 스케줄러 실패 상세 한 행을 표시한다
   *
   * @author SeungHyeon.Kang
   * @param failure 스케줄러 실패 상세
   * @return 스케줄러 실패 상세 표 행
   */
  const renderScheduleFailureRow = (failure: ScheduleFail) => (
    <tr key={`${failure.runxNumb}-${failure.failNumb}`}>
      <td>{failure.failType}</td>
      <td className="col-result-code">{failure.rsltCode ?? ''}</td>
      <td>{failure.rsltMesg ?? ''}</td>
      <td>{failure.erroType ?? ''}</td>
      <td className="error-content-cell"><pre>{failure.erroCntn ?? ''}</pre></td>
      <td className="col-date-time">{formatDate(failure.failDate)}</td>
    </tr>
  )

  // 실행번호가 변경되면 부모 실행 로그와 자식 실패 로그를 함께 다시 조회한다
  useEffect(() => {
    let active = true

    // 상세 화면 구성에 필요한 부모와 자식 로그를 병렬로 조회한다
    Promise.all([getScheduleLog(runxNumb), getScheduleFailures(runxNumb)])
      .then(([detail, failures]) => {
        // 화면이 유지되는 동안 도착한 응답만 상태에 반영한다
        if (active) {
          onError(null)
          setScheduleLog(detail)
          setScheduleFailures(failures)
          setLoading(false)
        }
      })
      .catch((error: unknown) => {
        // 화면이 유지되는 동안 발생한 API 오류만 공통 오류 영역에 표시한다
        if (active) {
          onError(error instanceof Error ? error.message : '스케줄러 로그 상세를 불러오지 못했습니다.')
          setLoading(false)
        }
      })

    // 화면이 해제된 뒤 도착한 API 응답이 상태를 변경하지 않도록 정리한다
    return () => {
      active = false
    }
  }, [runxNumb, onError])

  return (
    <>
      {/* 스케줄러 실행 결과와 실패 로그 상세 전체 영역 */}
      <section className="schedule-log-page">
        {/* 스케줄러 로그 상세 화면 제목 영역 */}
        <section className="content-header">
          <h1>{pageTitle}</h1>
        </section>

        {/* 선택한 스케줄러 실행 결과 요약 영역 */}
        <section className="detail-panel schedule-log-summary">
          <div className="detail-title">
            <div>
              <h2>실행 정보</h2>
              <p>스케줄러 실행 결과와 처리 건수를 확인합니다.</p>
            </div>
          </div>
          <section className="table-wrap menu-info-table">
            <table>
              <tbody>
                <tr>
                  <th>실행번호</th>
                  <td>{scheduleLog?.runxNumb ?? ''}</td>
                  <th>스케줄러명</th>
                  <td>{scheduleLog?.schdCodeName ?? scheduleLog?.schdCode ?? ''}</td>
                  <th>상태</th>
                  <td>{scheduleLog && <span className={getScheduleStatusClass(scheduleLog.execStat)}>{scheduleLog.execStat}</span>}</td>
                </tr>
                <tr>
                  <th>메서드명</th>
                  <td colSpan={5}>{scheduleLog?.methName ?? ''}</td>
                </tr>
                <tr>
                  <th>시작일시</th>
                  <td>{formatDate(scheduleLog?.strtDate ?? null)}</td>
                  <th>종료일시</th>
                  <td>{formatDate(scheduleLog?.fnshDate ?? null)}</td>
                  <th>소요시간</th>
                  <td>{formatExecutionTime(scheduleLog?.execMsec ?? null)}</td>
                </tr>
                <tr>
                  <th>대상 건수</th>
                  <td>{scheduleLog?.trgtCntt.toLocaleString() ?? ''}</td>
                  <th>성공 건수</th>
                  <td className="success-count">{scheduleLog?.succCntt.toLocaleString() ?? ''}</td>
                  <th>실패 건수</th>
                  <td className="fail-count">{scheduleLog?.failCntt.toLocaleString() ?? ''}</td>
                </tr>
              </tbody>
            </table>
          </section>
        </section>

        {/* 선택한 실행의 실패 상세 목록 영역 */}
        <section className="schedule-failure-section">
          <div className="detail-title">
            <div>
              <h2>실패 상세</h2>
              <p>실행번호 {runxNumb}에서 발생한 실패 로그입니다.</p>
            </div>
            <div className="status">총 {scheduleFailures.length}건</div>
          </div>
          <section className="table-wrap schedule-failure-table">
            <table>
              <thead>
                <tr>
                  <th>실패유형</th>
                  <th className="col-result-code">결과코드</th>
                  <th>결과메시지</th>
                  <th>예외유형</th>
                  <th>오류내용</th>
                  <th className="col-date-time">실패일시</th>
                </tr>
              </thead>
              <tbody>
                {loading ? (
                  <tr className="empty-row"><td colSpan={6}>스케줄러 로그 상세를 불러오고 있습니다.</td></tr>
                ) : scheduleFailures.length === 0 ? (
                  <tr className="empty-row"><td colSpan={6}>실패 로그가 없습니다.</td></tr>
                ) : scheduleFailures.map(renderScheduleFailureRow)}
              </tbody>
            </table>
          </section>
        </section>

        {/* 스케줄러 로그 목록 이동 영역 */}
        <div className="detail-footer">
          <div className="detail-footer-left">
            <button type="button" className="subtle-button" onClick={() => onMovePath(SCHEDULE_LOG_LIST_PATH)}>목록</button>
          </div>
        </div>
      </section>
    </>
  )
}
