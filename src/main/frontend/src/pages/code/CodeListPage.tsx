import { useState } from 'react'
import type { FormEvent } from 'react'
import type { Code, CodeMaster, CodeMasterSearch } from '../../types/code'
import { formatDate, getUseeYsnoCodeName } from '../../utils/code'
import { useMenuPermission } from '../../contexts/useMenuPermission'
import { Pagination } from '../../components/Pagination'
import type { PageData } from '../../types/common'

type CodeListPageProps = {
  codeMasters: CodeMaster[]
  pageData: PageData<CodeMaster>
  useeYsnoCodes: Code[]
  onSearch: (pageNumber: number, search: CodeMasterSearch) => void
  onSelect: (master: CodeMaster) => void
  onOpenRegister: () => void
}

const DEFAULT_SEARCH: CodeMasterSearch = {
  keyword: '',
  useeYsno: '',
}

/**
 * 코드관리 목록 화면
 * @Author SeungHyeon.Kang
 * @param codeMasters
 * @param useeYsnoCodes
 * @param onSelect
 * @param onOpenRegister
 * @return
 */
export function CodeListPage({
  codeMasters,
  pageData,
  useeYsnoCodes,
  onSearch,
  onSelect,
  onOpenRegister,
}: CodeListPageProps) {
  const permission = useMenuPermission()
  const [search, setSearch] = useState<CodeMasterSearch>({ ...DEFAULT_SEARCH })
  const [appliedSearch, setAppliedSearch] = useState<CodeMasterSearch>({ ...DEFAULT_SEARCH })

  /**
   * 입력한 공통코드 조건으로 첫 페이지를 검색한다
   *
   * @author SeungHyeon.Kang
   * @param event 공통코드 검색 폼 제출 이벤트
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
   * 공통코드 검색 조건과 결과를 전체 목록으로 초기화한다
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
    // 전체 공통코드의 첫 페이지를 조회한다
    onSearch(1, nextSearch)
  }

  // 공통코드 검색 조건과 결과 목록을 반환한다
  return (
    <section className="code-manage">
      {/* 공통코드 목록 제목과 검색 결과 건수 영역 */}
      <section className="content-header">
        <h1>코드관리</h1>
        <div className="status">총 {pageData.totalCount}건</div>
      </section>
      {/* 공통코드 검색 조건 영역 */}
      <form className="list-search" onSubmit={handleSearch}>
        <label>
          <span>코드·코드명</span>
          <input
            value={search.keyword}
            maxLength={100}
            placeholder="공통코드 또는 공통코드명"
            onChange={(event) => setSearch({ ...search, keyword: event.target.value })}
          />
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
        {/* 공통코드 검색 실행과 초기화 버튼 영역 */}
        <div className="list-search-actions">
          <button type="button" className="subtle-button" onClick={handleReset}>초기화</button>
          <button type="submit">검색</button>
        </div>
      </form>
      {/* 공통코드 검색 결과 영역 */}
      <section className="table-wrap code-list-table">
        <table>
          <thead>
            <tr>
              <th>공통코드</th>
              <th>공통코드명</th>
              <th className="col-usee">사용여부</th>
              <th>등록자</th>
              <th>등록일</th>
              <th>수정자</th>
              <th>수정일</th>
            </tr>
          </thead>
          <tbody>
            {codeMasters.map((master) => (
              <tr key={master.commCode} onClick={() => onSelect(master)}>
                <td>{master.commCode}</td>
                <td>{master.codeName}</td>
                <td className="col-usee">{getUseeYsnoCodeName(useeYsnoCodes, master.useeYsno, master.useeYsnoName)}</td>
                <td>{master.regiAdmnName ?? master.regiAdmn}</td>
                <td>{formatDate(master.regiDate)}</td>
                <td>{master.updtAdmnName ?? master.updtAdmn}</td>
                <td>{formatDate(master.updtDate)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>
      {/* 공통코드 검색 결과 페이지 이동 영역 */}
      <Pagination
        pageNumber={pageData.pageNumber}
        totalPages={pageData.totalPages}
        onPageChange={(pageNumber) => onSearch(pageNumber, appliedSearch)}
      />
      {permission.writYsno === 'Y' && <button type="button" className="floating-button" onClick={onOpenRegister}>등록</button>}
    </section>
  )
}
