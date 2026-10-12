import { fetchJson, fetchResult } from './client'
import type { Code, CodeMaster, CodeMasterSearch, DetailCodePayload } from '../types/code'
import type { PageData } from '../types/common'
import { createSearchParams } from '../utils/search'

/**
 * 세부코드 목록 조회
 * @Author SeungHyeon.Kang
 * @param commCode
 * @return
 */
export const getCodeList = (commCode: string) => fetchJson<Code[]>(`/api/codes/${commCode}`, undefined, '코드 조회에 실패했습니다.')

/**
 * 공통코드 목록 조회
 * @Author SeungHyeon.Kang
 * @return
 */
export const getCodeMasters = (pageNumber: number, search: CodeMasterSearch) =>
  fetchJson<PageData<CodeMaster>>(
    `/api/code-manage/masters?${createSearchParams(pageNumber, search).toString()}`,
    undefined,
    '공통코드 목록 조회에 실패했습니다.',
  )

/**
 * 공통코드 상세 조회
 * @Author SeungHyeon.Kang
 * @param commCode
 * @return
 */
export const getCodeMaster = (commCode: string) => fetchJson<CodeMaster>(`/api/code-manage/masters/${commCode}`, undefined, '공통코드 상세 조회에 실패했습니다.')

/**
 * 공통코드별 세부코드 목록 조회
 * @Author SeungHyeon.Kang
 * @param commCode
 * @return
 */
export const getDetailCodes = (commCode: string) => fetchJson<Code[]>(`/api/code-manage/masters/${commCode}/details`, undefined, '세부코드 목록 조회에 실패했습니다.')

/**
 * 공통코드 중복 확인
 * @Author SeungHyeon.Kang
 * @param commCode
 * @return
 */
export const checkMasterDuplicate = (commCode: string) => fetchJson<boolean>(`/api/code-manage/masters/${commCode}/duplicate`, undefined, '공통코드 중복검사에 실패했습니다.')

/**
 * 공통코드 등록
 * @Author SeungHyeon.Kang
 * @param master
 * @return
 */
export const createCodeMaster = (master: CodeMaster) =>
  fetchResult<CodeMaster>(
    '/api/code-manage/masters',
    { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(master) },
    '공통코드 등록에 실패했습니다.',
  )

/**
 * 공통코드와 기존 및 신규 세부코드를 한 요청으로 저장한다
 *
 * @author SeungHyeon.Kang
 * @param commCode 저장할 공통코드
 * @param master 수정할 공통코드 데이터
 * @param detailCodes 수정할 기존 세부코드
 * @param newCodes 등록할 신규 세부코드
 * @return 공통코드 상세 전체 저장 결과
 * @throws 공통코드 상세 저장 요청이 실패할 때 발생한다
 */
export const saveCodeBatch = (
  commCode: string,
  master: CodeMaster,
  detailCodes: DetailCodePayload[],
  newCodes: DetailCodePayload[],
) =>
  fetchResult<CodeMaster>(
    `/api/code-manage/masters/${commCode}/batch`,
    {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ master, detailCodes, newCodes }),
    },
    '공통코드 상세 저장에 실패했습니다.',
  )
