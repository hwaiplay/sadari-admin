import { fetchJson, fetchResult } from './client'
import type { PageData } from '../types/common'
import type { Notice, NoticeForm } from '../types/notice'

export const getNoticeList = (page: number, keyword: string) => {
  const params = new URLSearchParams({ page: String(page), keyword })
  return fetchJson<PageData<Notice>>(`/api/notices?${params.toString()}`)
}

export const getNoticeDetail = (notiNumb: number, versNumb: number) => (
  fetchJson<Notice>(`/api/notices/${notiNumb}/${versNumb}`)
)

export const getNoticeVersions = (notiNumb: number) => (
  fetchJson<Notice[]>(`/api/notices/${notiNumb}/versions`)
)

export const createNotice = (form: NoticeForm) => (
  fetchResult<Notice>('/api/notices', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(form),
  })
)

/**
 * 현재 배포 상태에 따라 공지 버전을 수정하거나 다음 버전으로 저장한다
 *
 * @author SeungHyeon.Kang
 * @param notiNumb 수정할 공지사항 번호
 * @param versNumb 수정 기준 버전 번호
 * @param form 저장할 공지사항 입력값
 * @return 서버가 결정한 버전의 저장 결과 Promise
 * @throws 공지사항 저장 API 요청이 실패할 때 발생한다
 */
export const updateNoticeVersion = (notiNumb: number, versNumb: number, form: NoticeForm) => (
  fetchResult<Notice>(`/api/notices/${notiNumb}/${versNumb}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(form),
  })
)

export const deployNotice = (notiNumb: number, versNumb: number) => (
  fetchResult<Notice>(`/api/notices/${notiNumb}/${versNumb}/deploy`, { method: 'POST' })
)

export const deleteNotice = (notiNumb: number) => (
  fetchResult<null>(`/api/notices/${notiNumb}`, { method: 'DELETE' })
)

export const uploadNoticeImage = async (file: File): Promise<string> => {
  const body = new FormData()
  body.append('file', file)
  const uploaded = await fetchJson<{ url: string }>('/api/notices/images', { method: 'POST', body })
  return uploaded.url
}
