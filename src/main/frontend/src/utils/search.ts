type StoredListPageSnapshot = {
  pageNumber: number
  search: object
}

export type ListPageSnapshot<TSearch extends object> = {
  pageNumber: number
  search: TSearch
}

// 현재 관리자 SPA 실행 중 목록 경로별 마지막 조회 상태
const listPageSnapshots = new Map<string, StoredListPageSnapshot>()

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

/**
 * 관리 목록의 마지막 페이지와 적용 검색 조건을 저장한다
 *
 * @author OpenAI.Codex
 * @param listPath 상태를 구분할 목록 경로
 * @param pageNumber 마지막으로 조회한 페이지 번호
 * @param search 마지막으로 적용한 검색 조건
 * @return 반환값이 없다
 */
export const setListPageSnapshot = <TSearch extends object>(
  listPath: string,
  pageNumber: number,
  search: TSearch,
): void => {
  // 상세 화면에서 목록으로 돌아올 때 같은 조회 상태를 복원하도록 불변 스냅샷을 저장한다
  listPageSnapshots.set(listPath, { pageNumber, search: { ...search } })
}

/**
 * 관리 목록의 마지막 페이지와 적용 검색 조건을 조회한다
 *
 * @author OpenAI.Codex
 * @param listPath 상태를 구분할 목록 경로
 * @param defaultSearch 저장된 상태가 없을 때 적용할 기본 검색 조건
 * @return 복원할 페이지 번호와 검색 조건
 */
export const getListPageSnapshot = <TSearch extends object>(
  listPath: string,
  defaultSearch: TSearch,
): ListPageSnapshot<TSearch> => {
  // 현재 브라우저 실행 중 저장된 목록 상태를 조회한다
  const snapshot = listPageSnapshots.get(listPath)
  // 처음 방문한 목록은 첫 페이지와 기본 검색 조건으로 시작한다
  if (!snapshot) {
    // 기본 목록 조회 상태를 반환한다
    return { pageNumber: 1, search: { ...defaultSearch } }
  }

  // 저장 당시 객체가 이후 입력 변경의 영향을 받지 않도록 새 검색 조건으로 반환한다
  return {
    pageNumber: snapshot.pageNumber,
    search: Object.assign({}, defaultSearch, snapshot.search),
  }
}
