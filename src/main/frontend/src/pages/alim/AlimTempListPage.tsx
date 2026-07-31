import { useState } from 'react'
import type { FormEvent } from 'react'
import { ALIM_TEMP_DETAIL_PREFIX, ALIM_TEMP_NEW_PATH } from '../../constants/routes'
import type { AlimTemp, AlimTempSearch } from '../../types/alim'
import type { Code } from '../../types/code'
import { formatDate, getUseeYsnoCodeName } from '../../utils/code'
import { useMenuPermission } from '../../contexts/useMenuPermission'
import { Pagination } from '../../components/Pagination'
import type { PageData } from '../../types/common'

type AlimTempListPageProps = {
  alimTemps: AlimTemp[]
  pageData: PageData<AlimTemp>
  alimSituCodes: Code[]
  useeYsnoCodes: Code[]
  onSearch: (pageNumber: number, search: AlimTempSearch) => void
  onMovePath: (path: string) => void
}

const DEFAULT_SEARCH: AlimTempSearch = {
  keyword: '',
  alimSitu: '',
  useeYsno: '',
}

/**
 * 알림 템플릿 목록 화면
 * @Author SeungHyeon.Kang
 * @param alimTemps
 * @param useeYsnoCodes
 * @param onMovePath
 * @return
 */
export function AlimTempListPage({
  alimTemps,
  pageData,
  alimSituCodes,
  useeYsnoCodes,
  onSearch,
  onMovePath,
}: AlimTempListPageProps) {
  const permission = useMenuPermission()
  const [search, setSearch] = useState<AlimTempSearch>({ ...DEFAULT_SEARCH })
  const [appliedSearch, setAppliedSearch] = useState<AlimTempSearch>({ ...DEFAULT_SEARCH })

  /**
   * 입력한 알림 템플릿 조건으로 첫 페이지를 검색한다
   *
   * @author SeungHyeon.Kang
   * @param event 알림 템플릿 검색 폼 제출 이벤트
   * @return 반환값이 없다
   */
  const handleSearch = (event: FormEvent<HTMLFormElement>): void => {
    // 브라우저 기본 폼 전송을 막는다
    event.preventDefault()
    // 페이지 이동에도 유지할 검색 조건 스냅샷을 저장한다
    const nextSearch = { ...search }
    // 적용 검색 조건을 갱신한다
    setAppliedSearch(nextSearch)
    // 변경된 조건으로 첫 페이지를 조회한다
    onSearch(1, nextSearch)
  }

  /**
   * 알림 템플릿 검색 조건과 결과를 전체 목록으로 초기화한다
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없다
   */
  const handleReset = (): void => {
    // 검색 입력과 적용 조건을 기본값으로 되돌린다
    const nextSearch = { ...DEFAULT_SEARCH }
    // 검색 입력 조건을 초기화한다
    setSearch(nextSearch)
    // 적용 검색 조건을 초기화한다
    setAppliedSearch(nextSearch)
    // 전체 알림 템플릿의 첫 페이지를 조회한다
    onSearch(1, nextSearch)
  }

  // 알림 템플릿 검색 조건과 결과 목록을 반환한다
  return (
    <section className="alim-temp-manage">
      {/* 알림 템플릿 목록 제목과 검색 결과 건수 영역 */}
      <section className="content-header">
        <h1>알림 템플릿 관리</h1>
        <div className="status">총 {pageData.totalCount}건</div>
      </section>
      {/* 알림 템플릿 검색 조건 영역 */}
      <form className="list-search" onSubmit={handleSearch}>
        <label>
          <span>코드·관리용 제목</span>
          <input
            value={search.keyword}
            maxLength={100}
            placeholder="템플릿코드 또는 관리용 제목"
            onChange={(event) => setSearch({ ...search, keyword: event.target.value })}
          />
        </label>
        <label>
          <span>알림상황</span>
          <select value={search.alimSitu} onChange={(event) => setSearch({ ...search, alimSitu: event.target.value })}>
            <option value="">전체</option>
            {alimSituCodes.map((code) => (
              <option key={code.comdCode} value={code.comdCode}>{code.comdName}</option>
            ))}
          </select>
        </label>
        <label>
          <span>사용여부</span>
          <select value={search.useeYsno} onChange={(event) => setSearch({ ...search, useeYsno: event.target.value })}>
            <option value="">전체</option>
            {useeYsnoCodes.map((code) => (
              <option key={code.comdCode} value={code.comdCode}>{code.comdName}</option>
            ))}
          </select>
        </label>
        {/* 알림 템플릿 검색 실행과 초기화 버튼 영역 */}
        <div className="list-search-actions">
          <button type="button" className="subtle-button" onClick={handleReset}>초기화</button>
          <button type="submit">검색</button>
        </div>
      </form>
      {/* 알림 템플릿 검색 결과 영역 */}
      <section className="table-wrap alim-temp-list-table">
        <table>
          <thead>
            <tr>
              <th>알림상황</th>
              <th>템플릿코드</th>
              <th>관리용 제목</th>
              <th className="col-usee">사용여부</th>
              <th>등록자</th>
              <th>등록일</th>
            </tr>
          </thead>
          <tbody>
            {alimTemps.length === 0 ? (
              <tr className="empty-row">
                <td colSpan={6}>알림 템플릿이 없습니다.</td>
              </tr>
            ) : (
              alimTemps.map((alimTemp) => (
                <tr key={`${alimTemp.alimSitu}-${alimTemp.tempCode}`} onClick={() => onMovePath(`${ALIM_TEMP_DETAIL_PREFIX}/${encodeURIComponent(alimTemp.alimSitu)}/${encodeURIComponent(alimTemp.tempCode)}`)}>
                  <td>{alimTemp.alimSituName ?? alimTemp.alimSitu}</td>
                  <td>{alimTemp.tempCode}</td>
                  <td>{alimTemp.tempTitl}</td>
                  <td className="col-usee">{getUseeYsnoCodeName(useeYsnoCodes, alimTemp.useeYsno, alimTemp.useeYsnoName)}</td>
                  <td>{alimTemp.regiAdmnName ?? alimTemp.regiAdmn}</td>
                  <td>{formatDate(alimTemp.regiDate)}</td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </section>
      {/* 알림 템플릿 검색 결과 페이지 이동 영역 */}
      <Pagination
        pageNumber={pageData.pageNumber}
        totalPages={pageData.totalPages}
        onPageChange={(pageNumber) => onSearch(pageNumber, appliedSearch)}
      />
      {permission.writYsno === 'Y' && <button type="button" className="floating-button" onClick={() => onMovePath(ALIM_TEMP_NEW_PATH)}>등록</button>}
    </section>
  )
}
