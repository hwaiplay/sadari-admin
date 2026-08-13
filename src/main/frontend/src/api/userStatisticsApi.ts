import { fetchJson } from './client'
import type { UserStatistics } from '../types/userStatistics'

/**
 * 선택 기간의 사용자 통계 대시보드 데이터를 조회한다.
 *
 * @author SeungHyeon.Kang
 * @param days 오늘을 포함한 조회 일수
 * @return 사용자 통계 대시보드 데이터
 * @throws 관리자 인증 또는 실시간 통계 조회가 실패하면 오류가 발생한다
 */
export const getUserStatistics = (days: 30 | 90): Promise<UserStatistics> => {
  // 허용된 조회 일수를 쿼리 문자열에 포함해 실시간 통계 API를 호출한다.
  return fetchJson<UserStatistics>(
    `/api/user-statistics?days=${days}`,
    undefined,
    '사용자 통계를 불러오지 못했습니다.',
  )
}
