import { fetchJson } from './client'
import type { PageData } from '../types/common'
import type {
  CurrentUser,
  CurrentUserComplaint,
  CurrentUserLoginHistory,
  CurrentUserSearch,
  CurrentUserSuspension,
  CurrentUserSuspensionRequest,
  CurrentUserWithdrawalHistory,
  DeletedSuspensionSearch,
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
 * 현재 사용자의 프로필 사진을 삭제하고 기본 이미지 상태로 변경한다.
 *
 * @author SeungHyeon.Kang
 * @param userNumb 사용자 번호
 * @return 변경된 현재 사용자 상세 정보
 * @throws 프로필 사진 삭제 API가 실패하면 발생한다
 */
export const delUserProfImage = (userNumb: number): Promise<CurrentUser> => {
  // 현재 프로필 이미지 참조와 저장 파일을 함께 정리한다.
  return fetchJson<CurrentUser>(
    `/api/current-users/${userNumb}/profile-image`,
    { method: 'DELETE' },
    '프로필 사진을 삭제하지 못했습니다.',
  )
}

/**
 * 현재 사용자의 배경화면을 삭제하고 기본 이미지 상태로 변경한다.
 *
 * @author SeungHyeon.Kang
 * @param userNumb 사용자 번호
 * @return 변경된 현재 사용자 상세 정보
 * @throws 배경화면 삭제 API가 실패하면 발생한다
 */
export const delUserBgimImage = (userNumb: number): Promise<CurrentUser> => {
  // 현재 배경 이미지 참조와 저장 파일을 함께 정리한다.
  return fetchJson<CurrentUser>(
    `/api/current-users/${userNumb}/background-image`,
    { method: 'DELETE' },
    '배경화면을 삭제하지 못했습니다.',
  )
}

/**
 * 현재 사용자의 한줄 소개를 삭제한다.
 *
 * @author SeungHyeon.Kang
 * @param userNumb 사용자 번호
 * @return 변경된 현재 사용자 상세 정보
 * @throws 한줄 소개 삭제 API가 실패하면 발생한다
 */
export const delUserIntroduction = (userNumb: number): Promise<CurrentUser> => {
  // 현재 사용자의 한줄 소개를 NULL 처리한다.
  return fetchJson<CurrentUser>(
    `/api/current-users/${userNumb}/introduction`,
    { method: 'DELETE' },
    '한줄 소개를 삭제하지 못했습니다.',
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
export const getUserLoginHistoryList = (
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
export const getUserWithdrawalList = (
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
 * 현재 사용자와 사용자 작성 대상이 받은 신고 누적 건수와 이력을 조회한다.
 *
 * @author SeungHyeon.Kang
 * @param userNumb 사용자 번호
 * @param pageNumber 조회할 페이지 번호
 * @return 받은 신고 이력 페이지
 */
export const getUserComplaintList = (
  userNumb: number,
  pageNumber: number,
): Promise<PageData<CurrentUserComplaint>> => {
  // 대상 소유자 스냅샷으로 연결된 받은 신고 이력을 조회한다.
  return fetchJson<PageData<CurrentUserComplaint>>(
    `/api/current-users/${userNumb}/complaints?page=${pageNumber}`,
    undefined,
    '신고 이력을 불러오지 못했습니다.',
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
export const setCurrentUserSuspension = (
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
export const uptCurrentUserSuspRelease = (
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

/**
 * 물리 삭제된 회원에게 남아 있는 유효 제재를 조회한다
 *
 * @author SeungHyeon.Kang
 * @param pageNumber 조회할 페이지 번호
 * @param search 과거 회원 번호 검색 조건
 * @return 삭제 회원의 유효 제재 페이지
 */
export const getDeletedSuspensions = (
  pageNumber: number,
  search: DeletedSuspensionSearch,
): Promise<PageData<CurrentUserSuspension>> => {
  // 과거 회원 번호가 있을 때만 검색 조건에 포함한다
  const params = new URLSearchParams({ page: String(pageNumber) })
  // 공백을 제거한 회원 번호 검색값만 서버에 전달한다
  if (search.userNumb.trim()) {
    // 과거 회원 번호로 정확히 일치하는 제재를 조회한다
    params.set('userNumb', search.userNumb.trim())
  }

  // OAuth 식별값을 노출하지 않는 삭제 회원 제재 목록 API를 호출한다
  return fetchJson<PageData<CurrentUserSuspension>>(
    `/api/current-users/deleted-suspensions?${params.toString()}`,
    undefined,
    '삭제 회원 제재 목록을 불러오지 못했습니다.',
  )
}

/**
 * 물리 삭제된 회원에게 남아 있는 유효 제재를 관리자 메모와 함께 해제한다
 *
 * @author SeungHyeon.Kang
 * @param userNumb 과거 회원 번호
 * @param spndNumb 제재 이력 번호
 * @param rlesCntn 필수 관리자 해제 메모
 * @return 반환값 없음
 */
export const uptDeletedSuspension = (
  userNumb: number,
  spndNumb: number,
  rlesCntn: string,
): Promise<void> => {
  // 과거 회원 번호와 해제 근거를 감사 이력 API에 전달한다
  return fetchJson<void>(
    `/api/current-users/deleted-suspensions/${spndNumb}`,
    {
      method: 'PATCH',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ userNumb, rlesCntn }),
    },
    '삭제 회원 제재를 해제하지 못했습니다.',
  )
}
