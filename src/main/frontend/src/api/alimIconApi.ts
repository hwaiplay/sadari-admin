import { fetchJson, fetchResult } from './client'
import type { AlimIcon, AlimIconSearch } from '../types/alim'
import type { PageData } from '../types/common'
import { createSearchParams } from '../utils/search'

/** ALIM_SITU 공통코드와 아이콘 등록 상태 목록을 조회한다. */
export const getAlimIconList = (pageNumber: number, search: AlimIconSearch) =>
  fetchJson<PageData<AlimIcon>>(
    `/api/alim-icons?${createSearchParams(pageNumber, search).toString()}`,
    undefined,
    '알림 아이콘 목록 조회에 실패했습니다.',
  )

/** 알림 상황별 아이콘 상세를 조회한다. */
export const getAlimIconDetail = (alimSitu: string) =>
  fetchJson<AlimIcon>(`/api/alim-icons/${encodeURIComponent(alimSitu)}`, undefined, '알림 아이콘 상세 조회에 실패했습니다.')

/** 알림 상황별 아이콘을 신규 등록하거나 현재 원본을 교체한다. */
export const saveAlimIconApi = (alimSitu: string, file: File) => {
  const body = new FormData()
  body.append('file', file)

  return fetchResult<AlimIcon>(
    `/api/alim-icons/${encodeURIComponent(alimSitu)}`,
    { method: 'PUT', body },
    '알림 아이콘 저장에 실패했습니다.',
  )
}
