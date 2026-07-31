/**
 * 목록 검색 조건을 페이지 번호가 포함된 API 쿼리 문자열로 변환한다
 *
 * @author SeungHyeon.Kang
 * @param pageNumber 조회할 페이지 번호
 * @param search 목록별 검색 조건
 * @return 값이 있는 검색 조건만 포함한 쿼리 문자열
 */
export const createSearchParams = (pageNumber: number, search: object): URLSearchParams => {
  // 페이지 번호는 모든 목록 조회 요청에 포함한다
  const params = new URLSearchParams({ page: String(pageNumber) })
  // 목록별 검색 조건을 API 쿼리 파라미터로 변환한다
  Object.entries(search).forEach(([key, value]) => {
    // 공백뿐인 입력은 검색 조건에서 제외한다
    if (typeof value === 'string' && value.trim()) {
      // 서버 검색 객체의 필드명과 동일한 쿼리 파라미터를 설정한다
      params.set(key, value.trim())
    }
  })
  // 완성된 검색 쿼리 문자열을 반환한다
  return params
}
