import { useEffect, useState } from 'react'
import type { MouseEvent } from 'react'
import { getScheduleLogs } from '../../api/scheduleLogApi'
import { SCHEDULE_LOG_DETAIL_PREFIX } from '../../constants/routes'
import type { ScheduleLog } from '../../types/scheduleLog'
import { formatDate } from '../../utils/code'
import { formatExecutionTime, getScheduleStatusClassName } from '../../utils/scheduleLog'

type ScheduleLogListPageProps = {
  onMovePath: (path: string) => void
  onError: (message: string | null) => void
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
  const [loading, setLoading] = useState(true)

  /**
   * 실행번호 조회 버튼에서 상세 화면으로 이동한다
   *
   * @author SeungHyeon.Kang
   * @param event 실행번호 조회 버튼 클릭 이벤트
   * @return 반환값이 없다
   */
  const handleDetailMove = (event: MouseEvent<HTMLButtonElement>): void => {
    const runxNumb = Number(event.currentTarget.value)
    // 유효한 실행 번호인 경우에만 상세 경로를 생성한다
    if (Number.isInteger(runxNumb) && runxNumb > 0) {
      onMovePath(`${SCHEDULE_LOG_DETAIL_PREFIX}/${runxNumb}`)
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
    <tr key={scheduleLog.runxNumb}>
      <td className="col-run-number">
        <button type="button" className="table-link-button" value={scheduleLog.runxNumb} onClick={handleDetailMove}>
          {scheduleLog.runxNumb}
        </button>
      </td>
      <td>{scheduleLog.schdCode}</td>
      <td>{scheduleLog.methName}</td>
      <td className="col-schedule-status"><span className={getScheduleStatusClassName(scheduleLog.execStat)}>{scheduleLog.execStat}</span></td>
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

    // 최신 스케줄러 실행 결과를 서버에서 조회한다
    getScheduleLogs()
      .then((rows) => {
        // 화면이 유지되는 동안 도착한 응답만 상태에 반영한다
        if (active) {
          onError(null)
          setScheduleLogs(rows)
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

  return (
    <>
      {/* 스케줄러 실행 결과 목록 전체 영역 */}
      <section className="schedule-log-page">
        {/* 스케줄러 실행 결과 제목과 전체 건수 영역 */}
        <section className="content-header">
          <h1>스케줄러 로그 확인</h1>
          <div className="status">총 {scheduleLogs.length}건</div>
        </section>

        {/* 스케줄러 실행 결과 목록 영역 */}
        <section className="table-wrap schedule-log-table">
          <table>
            <thead>
              <tr>
                <th className="col-run-number">실행번호</th>
                <th>스케줄러 코드</th>
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
      </section>
    </>
  )
}
