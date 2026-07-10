import type { ApiResult } from '../types/common'

/**
 * 응답 데이터 추출
 * @Author SeungHyeon.Kang
 * @param response
 * @return
 */
export const getResultData = async <T,>(response: Response): Promise<T> => {
  const result = await getResult<T>(response, '요청 처리에 실패했습니다.')
  return result.data
}

/**
 * JSON 데이터 요청
 * @Author SeungHyeon.Kang
 * @param input
 * @param init
 * @param errorMessage
 * @return
 */
export const fetchJson = async <T,>(input: RequestInfo | URL, init?: RequestInit, errorMessage = '요청에 실패했습니다.') => {
  const response = await fetch(input, init)
  const result = await getResult<T>(response, errorMessage)
  return result.data
}

/**
 * ResultData 전체 요청
 * @Author SeungHyeon.Kang
 * @param input
 * @param init
 * @param errorMessage
 * @return
 */
export const fetchResult = async <T,>(input: RequestInfo | URL, init?: RequestInit, errorMessage = '요청에 실패했습니다.') => {
  const response = await fetch(input, init)
  return getResult<T>(response, errorMessage)
}

/**
 * ResultData 응답 검증
 * @Author SeungHyeon.Kang
 * @param response
 * @param errorMessage
 * @return
 */
const getResult = async <T,>(response: Response, errorMessage: string): Promise<ApiResult<T>> => {
  let result: ApiResult<T> | null = null
  try {
    result = (await response.json()) as ApiResult<T>
  } catch {
    throw new Error(`${errorMessage} (${response.status})`)
  }
  if (!response.ok || result.code !== 200) throw new Error(result.message || `${errorMessage} (${response.status})`)
  return result
}
