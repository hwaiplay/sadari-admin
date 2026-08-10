import { fetchJson, fetchResult } from './client'
import type { PageData } from '../types/common'
import type { ServiceInfo, ServiceInfoForm } from '../types/serviceInfo'

/** 카테고리별 대표 서비스 정보 버전 목록을 조회한다. */
export const getServiceInfoList = (page: number, keyword: string, cateCode: string) => {
  const params = new URLSearchParams({ page: String(page), keyword, cateCode })
  // 관리자 서비스 정보 목록 조회 요청을 반환한다.
  return fetchJson<PageData<ServiceInfo>>(`/api/service-info?${params.toString()}`)
}

/** 서비스 정보 카테고리의 지정 버전 상세를 조회한다. */
export const getServiceInfoDetail = (cateCode: string, versNumb: number) => (
  fetchJson<ServiceInfo>(`/api/service-info/${encodeURIComponent(cateCode)}/${versNumb}`)
)

/** 서비스 정보 카테고리의 전체 버전 이력을 조회한다. */
export const getServiceInfoVersions = (cateCode: string) => (
  fetchJson<ServiceInfo[]>(`/api/service-info/${encodeURIComponent(cateCode)}/versions`)
)

/** 글이 없는 서비스 정보 카테고리에 최초 버전을 등록한다. */
export const createServiceInfo = (form: ServiceInfoForm) => (
  fetchResult<ServiceInfo>('/api/service-info', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(form),
  })
)

/** 서비스 정보의 현재 배포 상태에 따라 초안을 수정하거나 다음 버전을 생성한다. */
export const updateServiceInfoVersion = (cateCode: string, versNumb: number, form: ServiceInfoForm) => (
  fetchResult<ServiceInfo>(`/api/service-info/${encodeURIComponent(cateCode)}/${versNumb}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(form),
  })
)

/** 서비스 정보 카테고리의 지정 버전을 사용자 배포본으로 전환한다. */
export const deployServiceInfo = (cateCode: string, versNumb: number) => (
  fetchResult<ServiceInfo>(`/api/service-info/${encodeURIComponent(cateCode)}/${versNumb}/deploy`, { method: 'POST' })
)

/** 서비스 정보 카테고리의 모든 버전을 삭제한다. */
export const deleteServiceInfo = (cateCode: string) => (
  fetchResult<null>(`/api/service-info/${encodeURIComponent(cateCode)}`, { method: 'DELETE' })
)

/** 서비스 정보 Summernote 이미지를 업로드한다. */
export const uploadServiceInfoImage = async (file: File): Promise<string> => {
  const body = new FormData()
  body.append('file', file)
  const uploaded = await fetchJson<{ url: string }>('/api/service-info/images', { method: 'POST', body })
  // 업로드가 완료된 이미지의 공개 경로를 반환한다.
  return uploaded.url
}
