/**
 * 스케줄러 실행 상태에 대응하는 표시 클래스를 결정한다
 *
 * @author SeungHyeon.Kang
 * @param execStat 스케줄러 실행 상태 코드
 * @return 실행 상태 표시 클래스
 */
export const getScheduleStatusClass = (execStat: string): string => {
  // 정상 종료 상태는 성공 색상으로 구분한다
  if (execStat === 'SUCCESS') {
    return 'schedule-status success'
  }

  // 실패가 포함된 상태는 오류 색상으로 구분한다
  if (execStat === 'FAIL' || execStat === 'FAILED' || execStat === 'ERROR') {
    return 'schedule-status fail'
  }

  // 실행 중 상태는 진행 색상으로 구분한다
  if (execStat === 'RUNNING') {
    return 'schedule-status running'
  }

  // 처리 대상 없음과 정의되지 않은 상태는 중립 색상으로 표시한다
  return 'schedule-status neutral'
}

/**
 * 밀리초 실행 시간을 화면 표시값으로 변환한다
 *
 * @author SeungHyeon.Kang
 * @param execMsec 실행 소요 시간 밀리초
 * @return 단위가 포함된 실행 소요 시간
 */
export const formatExecutionTime = (execMsec: number | null): string => {
  // 종료되지 않은 실행은 소요 시간이 없으므로 빈 값으로 표시한다
  if (execMsec === null) {
    return ''
  }

  return `${execMsec.toLocaleString()} ms`
}
