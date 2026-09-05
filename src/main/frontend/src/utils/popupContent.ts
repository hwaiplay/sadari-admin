import type { PopupContent, PopupContentForm } from '../types/popupContent'

/**
 * 팝업 콘텐츠 등록 화면의 빈 입력값을 생성한다
 *
 * @author SeungHyeon.Kang
 * @param popuSitu 기본 선택할 팝업 화면 구분 코드
 * @return 빈 팝업 콘텐츠 입력값
 */
export function emptyPopupContentForm(popuSitu = ''): PopupContentForm {
  // 신규 팝업 등록에 필요한 빈 입력값을 반환한다
  return {
    popuSitu,
    popuCode: '',
    mngmTitl: '',
    contFirs: '',
    englFirs: '',
    contSeco: '',
    englSeco: '',
    contThir: '',
    englThir: '',
    contFour: '',
    englFour: '',
  }
}

/**
 * DB의 JSON 문자열 배열을 관리자 줄 단위 입력값으로 변환한다
 *
 * @author SeungHyeon.Kang
 * @param content DB에서 조회한 JSON 문자열 배열
 * @return 한 줄당 하나의 목록 문구로 구성된 입력값
 * @throws Error 문자열 배열이 아닌 콘텐츠가 저장되어 있을 때 발생한다
 */
export function popupJsonToLines(content: string | null): string {
  // 선택 영역이 저장되지 않았으면 빈 입력값으로 표시한다
  if (!content) {
    // 내용이 없는 선택 영역의 빈 입력값을 반환한다
    return ''
  }

  // DB 원문이 관리자 편집 화면에서 안전한 문자열 배열인지 확인한다
  try {
    // JSON 원문을 런타임 구조 검증이 가능한 값으로 변환한다
    const parsedContent: unknown = JSON.parse(content)
    // 문자열 배열이 아니거나 공백 문구가 있으면 손상된 운영 콘텐츠로 판단한다
    if (!Array.isArray(parsedContent)
        || parsedContent.some((item) => typeof item !== 'string' || item.trim().length === 0)) {
      throw new Error('저장된 팝업 내용 형식이 올바르지 않습니다.')
    }

    // 각 목록 문구를 관리자 입력창의 한 줄로 변환한다
    return parsedContent.map((item) => item.trim()).join('\n')
  } catch (error: unknown) {
    // 이미 사용자에게 전달할 수 있는 형식 오류는 그대로 유지한다
    if (error instanceof Error && error.message === '저장된 팝업 내용 형식이 올바르지 않습니다.') {
      throw error
    }

    throw new Error('저장된 팝업 내용 형식이 올바르지 않습니다.', { cause: error })
  }
}

/**
 * 조회된 팝업 콘텐츠를 줄 단위 관리자 입력값으로 변환한다
 *
 * @author SeungHyeon.Kang
 * @param popupContent 조회된 팝업 콘텐츠
 * @return 팝업 콘텐츠 상세 입력값
 */
export function toPopupContentForm(popupContent: PopupContent): PopupContentForm {
  // 각 JSON 영역을 줄 단위 입력값으로 변환한 팝업 상세 폼을 반환한다
  return {
    popuSitu: popupContent.popuSitu,
    popuCode: popupContent.popuCode,
    mngmTitl: popupContent.mngmTitl,
    contFirs: popupJsonToLines(popupContent.contFirs),
    englFirs: popupJsonToLines(popupContent.englFirs),
    contSeco: popupJsonToLines(popupContent.contSeco),
    englSeco: popupJsonToLines(popupContent.englSeco),
    contThir: popupJsonToLines(popupContent.contThir),
    englThir: popupJsonToLines(popupContent.englThir),
    contFour: popupJsonToLines(popupContent.contFour),
    englFour: popupJsonToLines(popupContent.englFour),
  }
}

/**
 * 관리자 줄 단위 입력값을 중복 없는 JSON 문자열 배열로 변환한다
 *
 * @author SeungHyeon.Kang
 * @param content 한 줄당 하나의 목록 문구로 입력된 값
 * @return JSON 문자열 배열 또는 내용이 없는 선택 영역의 Null
 */
export function popupLinesToJson(content: string): string | null {
  // 줄바꿈과 앞뒤 공백을 정리하고 빈 문구를 제외한다
  const contentLines = content.split(/\r?\n/u).map((line) => line.trim()).filter((line) => line.length > 0)
  // 입력 순서를 유지하면서 같은 목록 문구가 중복 저장되지 않도록 정리한다
  const uniqueContentLines = [...new Set(contentLines)]
  // 선택 영역에 문구가 없으면 DB Null로 저장할 수 있도록 구분한다
  if (uniqueContentLines.length === 0) {
    // 콘텐츠 영역이 없음을 나타내는 Null을 반환한다
    return null
  }

  // 사용자 화면에서 파싱할 문자열 배열 JSON을 반환한다
  return JSON.stringify(uniqueContentLines)
}

/**
 * 팝업 콘텐츠에 실제로 저장된 목록 영역 개수를 계산한다
 *
 * @author SeungHyeon.Kang
 * @param popupContent 목록에 표시할 팝업 콘텐츠
 * @return 내용이 있는 콘텐츠 영역 개수
 */
export function getPopupContentAreaCount(popupContent: PopupContent): number {
  // 네 개 콘텐츠 영역 중 저장된 값이 있는 영역 수를 반환한다
  return [popupContent.contFirs, popupContent.contSeco, popupContent.contThir, popupContent.contFour]
    .filter((content) => Boolean(content)).length
}
