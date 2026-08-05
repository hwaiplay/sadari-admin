import { fetchJson } from './client'
import type { PageData } from '../types/common'
import type { Complaint, ComplaintDetail, ComplaintSearch, ComplaintUpdate } from '../types/complaint'
import type { CurrentUserSuspension, CurrentUserSuspensionRequest } from '../types/currentUser'

/**
 * 신고 검색 조건을 API 쿼리 문자열로 변환한다
 *
 * @author SeungHyeon.Kang
 * @param pageNumber 조회할 페이지 번호
 * @param search 신고 검색 조건
 * @return 신고 검색 쿼리 문자열
 */
const createComplaintSearchParams = (pageNumber: number, search: ComplaintSearch): URLSearchParams => {
  // 모든 신고 목록 요청에 현재 페이지 번호를 포함한다
  const params = new URLSearchParams({ page: String(pageNumber) })
  // 비어 있지 않은 검색 조건만 서버에 전달해 동적 SQL 조건을 단순하게 유지한다
  Object.entries(search).forEach(([key, value]) => {
    // 공백뿐인 검색값은 서버 요청에서 제외한다
    if (value.trim()) {
      // 정리된 검색값을 API 쿼리 조건에 설정한다
      params.set(key, value.trim())
    }
  })
  // 완성된 신고 검색 조건을 반환한다
  return params
}

/**
 * 관리자 검색 조건에 맞는 신고 목록을 조회한다
 *
 * @author SeungHyeon.Kang
 * @param pageNumber 조회할 페이지 번호
 * @param search 신고 검색 조건
 * @return 신고 목록 페이지
 */
export const getComplaints = (pageNumber: number, search: ComplaintSearch): Promise<PageData<Complaint>> => {
  // 검색 조건을 URL에 포함해 신고 목록 API를 호출한다
  return fetchJson<PageData<Complaint>>(
    `/api/complaints?${createComplaintSearchParams(pageNumber, search).toString()}`,
    undefined,
    '신고 목록을 불러오지 못했습니다.',
  )
}

/**
 * 신고번호에 해당하는 관리자 신고 상세를 조회한다
 *
 * @author SeungHyeon.Kang
 * @param cmplNumb 신고 번호
 * @return 신고 상세
 */
export const getComplaint = (cmplNumb: number): Promise<ComplaintDetail> => {
  // 신고번호로 처리 정보와 동일 대상 신고를 조회한다
  return fetchJson<ComplaintDetail>(
    `/api/complaints/${cmplNumb}`,
    undefined,
    '신고 상세를 불러오지 못했습니다.',
  )
}

/**
 * 신고의 검토 시작 또는 최종 처리 상태를 저장한다
 *
 * @author SeungHyeon.Kang
 * @param cmplNumb 신고 번호
 * @param update 변경할 신고 처리 정보
 * @return 변경된 신고 상세
 */
export const updateComplaint = (cmplNumb: number, update: ComplaintUpdate): Promise<ComplaintDetail> => {
  // 화면이 조회한 수정일시와 처리 상태를 서버의 동시성 검증에 전달한다
  return fetchJson<ComplaintDetail>(
    `/api/complaints/${cmplNumb}`,
    {
      method: 'PATCH',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(update),
    },
    '신고를 처리하지 못했습니다.',
  )
}

/**
 * 사용자 신고 대상의 이용정지 이력을 조회한다
 *
 * @author SeungHyeon.Kang
 * @param cmplNumb 신고 번호
 * @param pageNumber 조회할 페이지 번호
 * @return 이용정지 이력 페이지
 */
export const getComplaintTargetSuspensions = (
  cmplNumb: number,
  pageNumber: number,
): Promise<PageData<CurrentUserSuspension>> => {
  // 서버가 신고 대상 번호를 회원번호로 검증한 이용정지 이력을 조회한다
  return fetchJson<PageData<CurrentUserSuspension>>(
    `/api/complaints/${cmplNumb}/suspensions?page=${pageNumber}`,
    undefined,
    '이용 정지 이력을 불러오지 못했습니다.',
  )
}

/**
 * 사용자 신고 대상에게 이용정지를 적용한다
 *
 * @author SeungHyeon.Kang
 * @param cmplNumb 신고 번호
 * @param request 정지 유형과 사유 및 기간
 * @return 등록된 이용정지 이력
 */
export const createComplaintTargetSuspension = (
  cmplNumb: number,
  request: CurrentUserSuspensionRequest,
): Promise<CurrentUserSuspension> => {
  // 회원번호 없이 신고번호와 정지값만 신고 관리 API에 전달한다
  return fetchJson<CurrentUserSuspension>(
    `/api/complaints/${cmplNumb}/suspensions`,
    {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(request),
    },
    '이용 정지를 적용하지 못했습니다.',
  )
}

/**
 * 사용자 신고 대상의 적용 중인 이용정지를 해제한다
 *
 * @author SeungHyeon.Kang
 * @param cmplNumb 신고 번호
 * @param spndNumb 정지 이력 번호
 * @param rlesCntn 관리자 내부 해제 메모
 * @return 반환값이 없다
 */
export const releaseComplaintTargetSuspension = (
  cmplNumb: number,
  spndNumb: number,
  rlesCntn: string,
): Promise<void> => {
  // 신고번호로 서버가 확정한 사용자 신고 대상의 이용정지를 해제한다
  return fetchJson<void>(
    `/api/complaints/${cmplNumb}/suspensions/${spndNumb}`,
    {
      method: 'PATCH',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ rlesCntn }),
    },
    '이용 정지를 해제하지 못했습니다.',
  )
}
