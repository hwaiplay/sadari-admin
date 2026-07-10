import type { Code } from '../types/code'

/**
 * 사용여부 코드명 조회
 * @Author SeungHyeon.Kang
 * @param codes
 * @param useeYsno
 * @param useeYsnoName
 * @return
 */
export const getUseeYsnoCodeName = (codes: Code[], useeYsno: string | null | undefined, useeYsnoName?: string | null) =>
  useeYsnoName ?? codes.find((code) => code.comdCode === useeYsno)?.opt1Name ?? codes.find((code) => code.comdCode === useeYsno)?.comdName ?? useeYsno ?? ''

/**
 * 일자 표시값 변환
 * @Author SeungHyeon.Kang
 * @param value
 * @return
 */
export const formatDate = (value: string | null) => {
  if (!value) return ''
  return value.replace('T', ' ').slice(0, 19)
}
