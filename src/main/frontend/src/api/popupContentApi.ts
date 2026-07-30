import { fetchJson, fetchResult } from './client'
import type { PageData } from '../types/common'
import type { PopupContent, PopupContentForm, PopupContentKey } from '../types/popupContent'
import { popupLinesToJson } from '../utils/popupContent'

/**
 * 팝업 콘텐츠 목록 페이지를 조회한다
 *
 * @author SeungHyeon.Kang
 * @param pageNumber 조회할 페이지 번호
 * @return 팝업 콘텐츠 목록과 페이지 정보
 * @throws Error API 조회 또는 공통 응답 검증에 실패할 때 발생한다
 */
export function getPopupContentList(pageNumber = 1): Promise<PageData<PopupContent>> {
  // 요청 페이지의 팝업 콘텐츠 목록을 반환한다
  return fetchJson<PageData<PopupContent>>(`/api/popup-contents?page=${pageNumber}`, undefined
                                        , '팝업 콘텐츠 목록 조회에 실패했습니다.')
}

/**
 * 복합키에 해당하는 팝업 콘텐츠 상세를 조회한다
 *
 * @author SeungHyeon.Kang
 * @param popuSitu 팝업 사용 화면 구분 코드
 * @param popuCode 팝업 식별 코드
 * @return 팝업 콘텐츠 상세
 * @throws Error API 조회 또는 공통 응답 검증에 실패할 때 발생한다
 */
export function getPopupContentDetail(popuSitu: string, popuCode: string): Promise<PopupContent> {
  // URL에서 복합키가 안전하게 구분되도록 각 코드값을 인코딩한다
  const detailPath = `${encodeURIComponent(popuSitu)}/${encodeURIComponent(popuCode)}`
  // 복합키에 해당하는 팝업 콘텐츠 상세를 반환한다
  return fetchJson<PopupContent>(`/api/popup-contents/${detailPath}`, undefined
                               , '팝업 콘텐츠 상세 조회에 실패했습니다.')
}

/**
 * 줄 단위 관리자 입력값을 JSON 목록으로 변환하여 팝업 콘텐츠를 저장한다
 *
 * @author SeungHyeon.Kang
 * @param form 팝업 콘텐츠 관리자 입력값
 * @param detail 기존 상세 수정 여부
 * @return 저장된 팝업 콘텐츠와 성공 메시지
 * @throws Error API 저장 또는 공통 응답 검증에 실패할 때 발생한다
 */
export function savePopupContentApi(form: PopupContentForm, detail: boolean) {
  // 사용자 화면에 필요한 네 개 목록 영역을 문자열 배열 JSON으로 변환한다
  const payload = {
    popuSitu: form.popuSitu.trim(),
    popuCode: form.popuCode.trim(),
    mngmTitl: form.mngmTitl.trim(),
    contFirs: popupLinesToJson(form.contFirs),
    contSeco: popupLinesToJson(form.contSeco),
    contThir: popupLinesToJson(form.contThir),
    contFour: popupLinesToJson(form.contFour),
  }
  // 상세에서는 기존 복합키 URL을 사용하고 신규 화면에서는 컬렉션 URL을 사용한다
  const requestPath = detail
    ? `/api/popup-contents/${encodeURIComponent(form.popuSitu)}/${encodeURIComponent(form.popuCode)}`
    : '/api/popup-contents'
  // 등록과 수정을 HTTP 메서드로 구분하여 저장 결과를 반환한다
  return fetchResult<PopupContent>(
    requestPath,
    {
      method: detail ? 'PUT' : 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    },
    '팝업 콘텐츠 저장에 실패했습니다.',
  )
}

/**
 * 관리자 경로에서 팝업 콘텐츠 복합키를 추출한다
 *
 * @author SeungHyeon.Kang
 * @param path 현재 관리자 화면 경로
 * @param detailPrefix 팝업 상세 경로 접두어
 * @return 상세 복합키 또는 상세 경로가 아닐 때 Null
 */
export function getPopupContentKey(path: string, detailPrefix: string): PopupContentKey | null {
  // 상세 경로가 아니면 복합키를 해석하지 않는다
  if (!path.startsWith(`${detailPrefix}/`)) {
    // 현재 경로가 팝업 상세가 아님을 나타내는 Null을 반환한다
    return null
  }

  // 상세 접두어 다음의 화면 구분과 팝업 코드를 분리한다
  const keyPath = path.slice(detailPrefix.length + 1)
  // URL 경로의 두 복합키 구간을 분리한다
  const [popuSitu, popuCode, ...restPath] = keyPath.split('/')
  // 복합키가 없거나 예상하지 않은 추가 경로가 있으면 상세로 처리하지 않는다
  if (!popuSitu || !popuCode || restPath.length > 0) {
    // 올바른 상세 복합키가 없음을 나타내는 Null을 반환한다
    return null
  }

  // URL 인코딩을 해제한 팝업 콘텐츠 복합키를 반환한다
  return {
    popuSitu: decodeURIComponent(popuSitu),
    popuCode: decodeURIComponent(popuCode),
  }
}
