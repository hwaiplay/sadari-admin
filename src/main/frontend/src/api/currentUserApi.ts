import { fetchJson } from './client'
import type { PageData } from '../types/common'
import type {
  CurrentUser,
  CurrentUserLoginHistory,
  CurrentUserSearch,
  CurrentUserSuspension,
  CurrentUserSuspensionRequest,
  CurrentUserWithdrawalHistory,
} from '../types/currentUser'

/**
 * 현재 사용자 검색 조건을 API 쿼리 문자열로 변환한다.
 *
 * @author SeungHyeon.Kang
 * @param pageNumber 조회할 페이지 번호
 * @param search 현재 사용자 검색 조건
 * @return 검색 쿼리 문자열
 */
const createSearchParams = (pageNumber: number, search: CurrentUserSearch): URLSearchParams => {
  // 페이지 번호는 모든 목록 조회 요청에 포함한다.
  const params = new URLSearchParams({ page: String(pageNumber) })
  // 값이 있는 검색 조건만 전송해 동적 SQL 조건을 단순하게 유지한다.
  Object.entries(search).forEach(([key, value]) => {
    // 공백뿐인 입력은 검색 조건에서 제외한다.
    if (value.trim()) {
      params.set(key, value.trim())
    }
  })
  // 완성된 검색 조건을 반환한다.
  return params
}

/**
 * 현재 사용자 목록을 검색한다.
 *
 * @author SeungHyeon.Kang
 * @param pageNumber 조회할 페이지 번호
 * @param search 현재 사용자 검색 조건
 * @return 검색된 현재 사용자 페이지
 */
export const getCurrentUsers = (
  pageNumber: number,
  search: CurrentUserSearch,
): Promise<PageData<CurrentUser>> => {
  // 검색 조건을 URL에 포함해 읽기 전용 목록 API를 호출한다.
  return fetchJson<PageData<CurrentUser>>(
    `/api/current-users?${createSearchParams(pageNumber, search).toString()}`,
    undefined,
    '현재 사용자 목록을 불러오지 못했습니다.',
  )
}

/**
 * 현재 사용자 상세 정보를 조회한다.
 *
 * @author SeungHyeon.Kang
 * @param userNumb 사용자 번호
 * @return 현재 사용자 상세 정보
 */
export const getCurrentUser = (userNumb: number): Promise<CurrentUser> => {
  // 사용자 번호에 해당하는 상세 정보를 조회한다.
  return fetchJson<CurrentUser>(
    `/api/current-users/${userNumb}`,
    undefined,
    '현재 사용자 상세 정보를 불러오지 못했습니다.',
  )
}

/**
 * 현재 사용자의 마스킹된 로그인 이력을 조회한다.
 *
 * @author SeungHyeon.Kang
 * @param userNumb 사용자 번호
 * @param pageNumber 조회할 페이지 번호
 * @return 로그인 이력 페이지
 */
export const getCurrentUserLoginHistories = (
  userNumb: number,
  pageNumber: number,
): Promise<PageData<CurrentUserLoginHistory>> => {
  // IP가 서버에서 마스킹된 로그인 이력만 조회한다.
  return fetchJson<PageData<CurrentUserLoginHistory>>(
    `/api/current-users/${userNumb}/login-histories?page=${pageNumber}`,
    undefined,
    '로그인 이력을 불러오지 못했습니다.',
  )
}

/**
 * 현재 사용자의 계정 처리 이력을 조회한다.
 *
 * @author SeungHyeon.Kang
 * @param userNumb 사용자 번호
 * @param pageNumber 조회할 페이지 번호
 * @return 계정 처리 이력 페이지
 */
export const getCurrentUserWithdrawalHistories = (
  userNumb: number,
  pageNumber: number,
): Promise<PageData<CurrentUserWithdrawalHistory>> => {
  // 자유 입력 사유를 제외한 비활성화·영구탈퇴 처리 이력을 조회한다.
  return fetchJson<PageData<CurrentUserWithdrawalHistory>>(
    `/api/current-users/${userNumb}/withdrawal-histories?page=${pageNumber}`,
    undefined,
    '계정 처리 이력을 불러오지 못했습니다.',
  )
}

/**
 * 현재 사용자의 관리자 이용 정지 이력을 조회한다.
 *
 * @author SeungHyeon.Kang
 * @param userNumb 사용자 번호
 * @param pageNumber 조회할 페이지 번호
 * @return 이용 정지 이력 페이지
 */
export const getCurrentUserSuspensions = (
  userNumb: number,
  pageNumber: number,
): Promise<PageData<CurrentUserSuspension>> =>
  fetchJson<PageData<CurrentUserSuspension>>(
    `/api/current-users/${userNumb}/suspensions?page=${pageNumber}`,
    undefined,
    '이용 정지 이력을 불러오지 못했습니다.',
  )

/**
 * 현재 사용자에게 기간 또는 무기한 이용 정지를 적용한다.
 *
 * @author SeungHyeon.Kang
 * @param userNumb 사용자 번호
 * @param request 정지 유형과 사유 및 기간
 * @return 등록된 정지 이력
 */
export const createCurrentUserSuspension = (
  userNumb: number,
  request: CurrentUserSuspensionRequest,
): Promise<CurrentUserSuspension> =>
  fetchJson<CurrentUserSuspension>(
    `/api/current-users/${userNumb}/suspensions`,
    {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(request),
    },
    '이용 정지를 적용하지 못했습니다.',
  )

/**
 * 현재 사용자의 적용 중인 이용 정지를 해제한다.
 *
 * @author SeungHyeon.Kang
 * @param userNumb 사용자 번호
 * @param spndNumb 정지 이력 번호
 * @param rlesCntn 관리자 내부 해제 메모
 * @return 반환값 없음
 */
export const releaseCurrentUserSuspension = (
  userNumb: number,
  spndNumb: number,
  rlesCntn: string,
): Promise<void> =>
  fetchJson<void>(
    `/api/current-users/${userNumb}/suspensions/${spndNumb}`,
    {
      method: 'PATCH',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ rlesCntn }),
    },
    '이용 정지를 해제하지 못했습니다.',
  )
