import { fetchJson } from './client'
import type { PageData } from '../types/common'
import type { Inquiry, InquirySearch } from '../types/inquiry'

const setSearchParams = (pageNumber: number, search: InquirySearch): URLSearchParams => {
  const params = new URLSearchParams({ page: String(pageNumber) })
  Object.entries(search).forEach(([key, value]) => {
    if (value.trim()) params.set(key, value.trim())
  })
  return params
}

/** 관리자 고객문의 목록을 검색한다 */
export const getInquiries = (pageNumber: number, search: InquirySearch): Promise<PageData<Inquiry>> =>
  fetchJson<PageData<Inquiry>>(`/api/inquiries?${setSearchParams(pageNumber, search)}`, undefined, '고객문의 목록을 불러오지 못했습니다.')

/** 고객문의 상세와 답변을 조회한다 */
export const getInquiry = (inqrNumb: number): Promise<Inquiry> =>
  fetchJson<Inquiry>(`/api/inquiries/${inqrNumb}`, undefined, '고객문의 상세를 불러오지 못했습니다.')

/** 고객문의를 현재 관리자의 검토 중 상태로 변경한다 */
export const startInquiryReview = (inqrNumb: number, updtDate: string): Promise<Inquiry> =>
  fetchJson<Inquiry>(`/api/inquiries/${inqrNumb}/review`, {
    method: 'PATCH', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ updtDate }),
  }, '고객문의 검토를 시작하지 못했습니다.')

/** 검토 중인 고객문의에 답변을 등록한다 */
export const setInquiryAnswer = (inqrNumb: number, answCntn: string, updtDate: string): Promise<Inquiry> =>
  fetchJson<Inquiry>(`/api/inquiries/${inqrNumb}/answers`, {
    method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ answCntn, updtDate }),
  }, '고객문의 답변을 등록하지 못했습니다.')

/** 고객문의에 연결된 이용정지를 해제한다 */
export const releaseInquirySuspension = (inqrNumb: number, rlesCntn: string): Promise<void> =>
  fetchJson<void>(`/api/inquiries/${inqrNumb}/suspension/release`, {
    method: 'PATCH', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ rlesCntn }),
  }, '연결된 이용정지를 해제하지 못했습니다.')
