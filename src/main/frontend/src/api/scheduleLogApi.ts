import { fetchJson } from './client'
import type { ScheduleFail, ScheduleLog } from '../types/scheduleLog'
import type { PageData } from '../types/common'

/**
 * 스케줄러 실행 결과 목록을 조회한다
 *
 * @author SeungHyeon.Kang
 * @return 스케줄러 실행 결과 목록
 * @throws 스케줄러 실행 결과 API가 실패하면 발생한다
 */
export const getScheduleLogs = (pageNumber = 1): Promise<PageData<ScheduleLog>> =>
  fetchJson<PageData<ScheduleLog>>(`/api/schedule-logs?page=${pageNumber}`, undefined, '스케줄러 로그 목록 조회에 실패했습니다.')

/**
 * 선택한 스케줄러 실행 결과를 조회한다
 *
 * @author SeungHyeon.Kang
 * @param runxNumb 스케줄러 실행 번호
 * @return 스케줄러 실행 결과
 * @throws 스케줄러 실행 상세 API가 실패하면 발생한다
 */
export const getScheduleLog = (runxNumb: number): Promise<ScheduleLog> =>
  fetchJson<ScheduleLog>(`/api/schedule-logs/${runxNumb}`, undefined, '스케줄러 로그 상세 조회에 실패했습니다.')

/**
 * 선택한 스케줄러 실행의 실패 상세 목록을 조회한다
 *
 * @author SeungHyeon.Kang
 * @param runxNumb 스케줄러 실행 번호
 * @return 스케줄러 실패 상세 목록
 * @throws 스케줄러 실패 상세 API가 실패하면 발생한다
 */
export const getScheduleFailures = (runxNumb: number): Promise<ScheduleFail[]> =>
  fetchJson<ScheduleFail[]>(`/api/schedule-logs/${runxNumb}/failures`, undefined, '스케줄러 실패 로그 조회에 실패했습니다.')
