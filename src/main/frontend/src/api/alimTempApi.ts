import { fetchJson, fetchResult } from './client'
import type { AlimTemp, AlimTempForm, AlimTempSearch } from '../types/alim'
import type { PageData } from '../types/common'
import { createSearchParams } from '../utils/search'

/**
 * 알림 템플릿 목록 조회
 * @Author SeungHyeon.Kang
 * @return
 */
export const getAlimTempList = (pageNumber: number, search: AlimTempSearch) =>
  fetchJson<PageData<AlimTemp>>(
    `/api/alim-temps?${createSearchParams(pageNumber, search).toString()}`,
    undefined,
    '알림 템플릿 목록 조회에 실패했습니다.',
  )

/**
 * 알림 템플릿 상세 조회
 * @Author SeungHyeon.Kang
 * @param alimSitu
 * @param tempCode
 * @return
 */
export const getAlimTempDetail = (alimSitu: string, tempCode: string) =>
  fetchJson<AlimTemp>(`/api/alim-temps/${encodeURIComponent(alimSitu)}/${encodeURIComponent(tempCode)}`, undefined, '알림 템플릿 상세 조회에 실패했습니다.')

/**
 * 알림 템플릿 중복 조회
 * @Author SeungHyeon.Kang
 * @param alimSitu
 * @param tempCode
 * @return
 */
export const checkAlimTempDuplicate = (alimSitu: string, tempCode: string) =>
  fetchJson<boolean>(`/api/alim-temps/${encodeURIComponent(alimSitu)}/${encodeURIComponent(tempCode)}/duplicate`, undefined, '알림 템플릿 중복 검사에 실패했습니다.')

/**
 * 알림 템플릿 저장
 * @Author SeungHyeon.Kang
 * @param form
 * @param detail
 * @param oldAlimSitu
 * @param oldTempCode
 * @return
 */
export const saveAlimTempApi = (form: AlimTempForm, detail: boolean, oldAlimSitu?: string, oldTempCode?: string) => {
  const payload = {
    alimSitu: form.alimSitu,
    tempCode: form.tempCode.trim(),
    tempTitl: form.tempTitl.trim(),
    alimTitl: form.alimTitl.trim(),
    tempCont: form.tempCont.trim(),
    useeYsno: form.useeYsno,
  }

  return fetchResult<AlimTemp>(
    detail && oldAlimSitu && oldTempCode
      ? `/api/alim-temps/${encodeURIComponent(oldAlimSitu)}/${encodeURIComponent(oldTempCode)}`
      : '/api/alim-temps',
    { method: detail ? 'PUT' : 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload) },
    '알림 템플릿 저장에 실패했습니다.',
  )
}
