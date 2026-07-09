import { fetchJson, fetchResult } from './client'
import type { Code, CodeMaster, DetailCodePayload } from '../types/code'

export const getCodeList = (commCode: string) => fetchJson<Code[]>(`/api/codes/${commCode}`, undefined, '코드 조회에 실패했습니다.')

export const getCodeMasters = () => fetchJson<CodeMaster[]>('/api/code-manage/masters', undefined, '공통코드 목록 조회에 실패했습니다.')

export const getCodeMaster = (commCode: string) => fetchJson<CodeMaster>(`/api/code-manage/masters/${commCode}`, undefined, '공통코드 상세 조회에 실패했습니다.')

export const getDetailCodes = (commCode: string) => fetchJson<Code[]>(`/api/code-manage/masters/${commCode}/details`, undefined, '세부코드 목록 조회에 실패했습니다.')

export const checkMasterDuplicate = (commCode: string) => fetchJson<boolean>(`/api/code-manage/masters/${commCode}/duplicate`, undefined, '공통코드 중복검사에 실패했습니다.')

export const createCodeMaster = (master: CodeMaster) =>
  fetchResult<CodeMaster>(
    '/api/code-manage/masters',
    { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(master) },
    '공통코드 등록에 실패했습니다.',
  )

export const updateCodeMaster = (commCode: string, master: CodeMaster) =>
  fetchResult<CodeMaster>(
    `/api/code-manage/masters/${commCode}`,
    { method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(master) },
    '공통코드 수정에 실패했습니다.',
  )

export const createDetailCode = (commCode: string, payload: DetailCodePayload) =>
  fetchResult<void>(
    `/api/code-manage/masters/${commCode}/details`,
    { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload) },
    '세부코드 등록에 실패했습니다.',
  )

export const updateDetailCode = (commCode: string, comdCode: string, payload: DetailCodePayload) =>
  fetchResult<Code>(
    `/api/code-manage/masters/${commCode}/details/${comdCode}`,
    { method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload) },
    '세부코드 수정에 실패했습니다.',
  )
