import { fetchJson, fetchResult } from './client'
import type { WelcomePage, WelcomePageForm } from '../types/welcomePage'

/** 배포본 우선 웰컴페이지 목록을 조회한다. */
export const getWelcomePageList = () => fetchJson<WelcomePage[]>('/api/welcome-pages')

/** 웰컴페이지 버전 상세를 조회한다. */
export const getWelcomePageDetail = (wlcmNumb: number, versNumb: number) => (
  fetchJson<WelcomePage>(`/api/welcome-pages/${wlcmNumb}/${versNumb}`)
)

/** 웰컴페이지의 모든 버전을 조회한다. */
export const getWelcomePageVersions = (wlcmNumb: number) => (
  fetchJson<WelcomePage[]>(`/api/welcome-pages/${wlcmNumb}/versions`)
)

/** 신규 웰컴페이지 최초 버전을 등록한다. */
export const createWelcomePage = (form: WelcomePageForm) => (
  fetchResult<WelcomePage>('/api/welcome-pages', {
    method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(form),
  })
)

/** 웰컴페이지 버전을 저장하거나 다음 초안을 생성한다. */
export const updateWelcomePageVersion = (wlcmNumb: number, versNumb: number, form: WelcomePageForm) => (
  fetchResult<WelcomePage>(`/api/welcome-pages/${wlcmNumb}/${versNumb}`, {
    method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(form),
  })
)

/** 선택한 웰컴페이지 버전을 배포한다. */
export const deployWelcomePage = (wlcmNumb: number, versNumb: number) => (
  fetchResult<WelcomePage>(`/api/welcome-pages/${wlcmNumb}/${versNumb}/deploy`, { method: 'POST' })
)

/** 웰컴페이지의 모든 버전을 삭제한다. */
export const deleteWelcomePage = (wlcmNumb: number) => (
  fetchResult<null>(`/api/welcome-pages/${wlcmNumb}`, { method: 'DELETE' })
)

/** 웰컴페이지 이미지를 검증해 콘텐츠 저장소에 업로드한다. */
export const uploadWelcomePageImage = async (file: File): Promise<string> => {
  const body = new FormData()
  body.append('file', file)
  const uploaded = await fetchJson<{ url: string }>('/api/welcome-pages/images', { method: 'POST', body })
  return uploaded.url
}
