import { Fragment, useState } from 'react'
import type { FormEvent, MouseEvent } from 'react'
import type { Code, CodeMaster, CodeMasterSearch } from '../../types/code'
import { formatDate, getUseeYsnoCodeName } from '../../utils/code'
import { useMenuPermission } from '../../contexts/useMenuPermission'
import { Pagination } from '../../components/Pagination'
import type { PageData } from '../../types/common'
import { CODE_LIST_PATH } from '../../constants/routes'
import { getListPageSnapshot } from '../../utils/search'

type CodeListPageProps = {
  codeMasters: CodeMaster[]
  pageData: PageData<CodeMaster>
  useeYsnoCodes: Code[]
  onSearch: (pageNumber: number, search: CodeMasterSearch) => void
  onSelect: (master: CodeMaster) => void
  onSelectDetail: (detail: Code) => void
  onLoadDetails: (commCode: string) => Promise<Code[]>
  onOpenRegister: () => void
  onError: (message: string) => void
}

type VisibleDetailRow = {
  detail: Code
  depth: number
  hasChildren: boolean
  expanded: boolean
}

const DEFAULT_SEARCH: CodeMasterSearch = {
  keyword: '',
  useeYsno: '',
}

/**
 * 공통코드와 세부코드를 함께 탐색하는 계층형 목록 화면을 구성한다
 *
 * @author SeungHyeon.Kang
 * @param props 공통코드 목록과 계층 조회 동작
 * @return 공통코드 계층 목록 화면
 */
export function CodeListPage({
  codeMasters,
  pageData,
  useeYsnoCodes,
  onSearch,
  onSelect,
  onSelectDetail,
  onLoadDetails,
  onOpenRegister,
  onError,
}: CodeListPageProps) {
  // 현재 관리 메뉴의 쓰기 권한을 조회한다
  const permission = useMenuPermission()
  // 상세 화면 이전에 사용한 코드 검색 조건을 목록 입력값으로 복원한다
  const initialSnapshot = getListPageSnapshot(CODE_LIST_PATH, DEFAULT_SEARCH)
  const [search, setSearch] = useState<CodeMasterSearch>(initialSnapshot.search)
  const [appliedSearch, setAppliedSearch] = useState<CodeMasterSearch>(initialSnapshot.search)
  const [expandedMasters, setExpandedMasters] = useState<Set<string>>(new Set())
  const [expandedDetails, setExpandedDetails] = useState<Set<string>>(new Set())
  const [detailsByMaster, setDetailsByMaster] = useState<Record<string, Code[]>>({})
  const [loadingMasters, setLoadingMasters] = useState<Set<string>>(new Set())

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
    const nextSearch = { ...search }
    // 페이지 이동에도 유지할 검색 조건을 저장한다
    setAppliedSearch(nextSearch)
    // 변경된 조건으로 첫 페이지를 조회한다
    onSearch(1, nextSearch)
  }

  /**
   * 공통코드 검색 조건과 계층 펼침 상태를 전체 목록으로 초기화한다
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없다
   */
  const handleReset = (): void => {
    const nextSearch = { ...DEFAULT_SEARCH }
    // 검색 입력 조건을 초기화한다
    setSearch(nextSearch)
    // 적용 검색 조건을 초기화한다
    setAppliedSearch(nextSearch)
    // 공통코드 펼침 상태를 초기화한다
    setExpandedMasters(new Set())
    // 세부코드 펼침 상태를 초기화한다
    setExpandedDetails(new Set())
    // 전체 공통코드의 첫 페이지를 조회한다
    onSearch(1, nextSearch)
  }

  /**
   * 공통코드의 최상위 세부코드를 펼치거나 접는다
   *
   * @author SeungHyeon.Kang
   * @param event 공통코드 행의 펼침 버튼 클릭 이벤트
   * @param master 펼침 상태를 변경할 공통코드
   * @return 비동기 처리 완료 Promise
   */
  const handleToggleMaster = async (event: MouseEvent<HTMLButtonElement>, master: CodeMaster): Promise<void> => {
    // 펼침 버튼이 공통코드 상세 이동까지 실행하지 않도록 이벤트 전파를 막는다
    event.stopPropagation()
    const nextExpandedMasters = new Set(expandedMasters)
    // 이미 펼친 공통코드는 자식 행을 접는다
    if (nextExpandedMasters.has(master.commCode)) {
      // 선택한 공통코드의 펼침 상태를 제거한다
      nextExpandedMasters.delete(master.commCode)
      // 변경된 공통코드 펼침 상태를 반영한다
      setExpandedMasters(nextExpandedMasters)
      return
    }

    // 세부코드 조회와 관계없이 먼저 펼침 상태를 표시한다
    nextExpandedMasters.add(master.commCode)
    // 변경된 공통코드 펼침 상태를 반영한다
    setExpandedMasters(nextExpandedMasters)

    // 이미 조회한 세부코드는 같은 목록 화면에서 캐시를 재사용한다
    if (detailsByMaster[master.commCode]) {
      return
    }

    const nextLoadingMasters = new Set(loadingMasters)
    // 중복 펼침 요청을 막기 위해 공통코드 로딩 상태를 시작한다
    nextLoadingMasters.add(master.commCode)
    // 변경된 로딩 상태를 반영한다
    setLoadingMasters(nextLoadingMasters)

    // 세부코드 조회 실패를 공통 오류 영역에 전달한다
    try {
      // 선택한 공통코드의 전체 세부코드를 한 번에 조회한다
      const details = await onLoadDetails(master.commCode)
      // 재귀 펼침에서 재사용할 세부코드 목록을 공통코드별로 저장한다
      setDetailsByMaster((currentDetails) => ({ ...currentDetails, [master.commCode]: details }))
    }

    catch (error: unknown) {
      // "세부코드 목록 조회에 실패했습니다."
      onError(error instanceof Error ? error.message : '세부코드 목록 조회에 실패했습니다.')
      // 조회하지 못한 공통코드는 빈 펼침 영역이 남지 않도록 다시 접는다
      setExpandedMasters((currentMasters) => {
        const failedMasters = new Set(currentMasters)
        // 실패한 공통코드의 펼침 상태를 제거한다
        failedMasters.delete(master.commCode)
        // 실패가 반영된 펼침 상태를 반환한다
        return failedMasters
      })
    }

    finally {
      // 완료된 공통코드는 다시 펼칠 수 있도록 로딩 상태에서 제거한다
      setLoadingMasters((currentMasters) => {
        const completedMasters = new Set(currentMasters)
        // 완료된 공통코드의 로딩 상태를 제거한다
        completedMasters.delete(master.commCode)
        // 완료 상태가 반영된 로딩 집합을 반환한다
        return completedMasters
      })
    }
  }

  /**
   * 세부코드의 직계 자식 행을 펼치거나 접는다
   *
   * @author SeungHyeon.Kang
   * @param event 세부코드 행의 펼침 버튼 클릭 이벤트
   * @param detail 펼침 상태를 변경할 세부코드
   * @return 반환값이 없다
   */
  const handleToggleDetail = (event: MouseEvent<HTMLButtonElement>, detail: Code): void => {
    // 펼침 버튼이 세부코드 상세 이동까지 실행하지 않도록 이벤트 전파를 막는다
    event.stopPropagation()
    const detailKey = getDetailKey(detail)
    const nextExpandedDetails = new Set(expandedDetails)
    // 현재 상태에 따라 선택한 세부코드의 자식 표시 여부를 전환한다
    if (nextExpandedDetails.has(detailKey)) {
      // 펼쳐진 세부코드를 접는다
      nextExpandedDetails.delete(detailKey)
    }

    else {
      // 접혀 있던 세부코드를 펼친다
      nextExpandedDetails.add(detailKey)
    }

    // 변경된 세부코드 펼침 상태를 반영한다
    setExpandedDetails(nextExpandedDetails)
  }

  // 공통코드와 재귀 세부코드 목록 화면을 반환한다
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
          {/* "코드·코드명·세부코드" */}
          <span>코드·코드명·세부코드</span>
          {/* "공통코드, 코드명, 세부코드 또는 세부코드명" */}
          <input
            value={search.keyword}
            maxLength={100}
            placeholder="공통코드, 코드명, 세부코드 또는 세부코드명"
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
      {/* 공통코드와 세부코드 계층 검색 결과 영역 */}
      <section className="table-wrap code-list-table code-tree-table">
        <table>
          <thead>
            <tr>
              <th className="col-tree-toggle">계층</th>
              <th>코드</th>
              <th>코드명</th>
              <th className="col-usee">사용여부</th>
              <th>등록자</th>
              <th>등록일</th>
              <th>수정자</th>
              <th>수정일</th>
            </tr>
          </thead>
          <tbody>
            {codeMasters.map((master) => {
              const masterExpanded = expandedMasters.has(master.commCode)
              const masterDetails = detailsByMaster[master.commCode] ?? []
              // 펼쳐진 분기만 부모 다음에 이어지도록 세부코드 계층을 평탄화한다
              const visibleDetailRows = getVisibleDetailRows(masterDetails, expandedDetails)
              // 공통코드 행과 현재 펼친 세부코드 행을 함께 반환한다
              return (
                <Fragment key={master.commCode}>
                  {/* 공통코드 개별 항목 영역 */}
                  <tr className="code-master-row" onClick={() => onSelect(master)}>
                    <td className="col-tree-toggle">
                      <button
                        type="button"
                        className="icon-toggle-button"
                        aria-label={masterExpanded ? '세부코드 접기' : '세부코드 펼치기'}
                        title={masterExpanded ? '접기' : '펼치기'}
                        disabled={loadingMasters.has(master.commCode)}
                        onClick={(event) => void handleToggleMaster(event, master)}
                      >
                        <TreeToggleIcon expanded={masterExpanded} />
                      </button>
                    </td>
                    <td className="tree-code-cell tree-depth-0">{master.commCode}</td>
                    <td>{master.codeName}</td>
                    <td className="col-usee">{getUseeYsnoCodeName(useeYsnoCodes, master.useeYsno, master.useeYsnoName)}</td>
                    <td>{master.regiAdmnName ?? master.regiAdmn}</td>
                    <td>{formatDate(master.regiDate)}</td>
                    <td>{master.updtAdmnName ?? master.updtAdmn}</td>
                    <td>{formatDate(master.updtDate)}</td>
                  </tr>
                  {masterExpanded && visibleDetailRows.map((row) => (
                    <tr className="code-detail-tree-row" key={getDetailKey(row.detail)} onClick={() => onSelectDetail(row.detail)}>
                      <td className="col-tree-toggle">
                        {row.hasChildren ? (
                          <button
                            type="button"
                            className="icon-toggle-button"
                            aria-label={row.expanded ? '하위 세부코드 접기' : '하위 세부코드 펼치기'}
                            title={row.expanded ? '접기' : '펼치기'}
                            onClick={(event) => handleToggleDetail(event, row.detail)}
                          >
                            <TreeToggleIcon expanded={row.expanded} />
                          </button>
                        ) : <span className="tree-toggle-placeholder" />}
                      </td>
                      <td className="tree-code-cell" style={{ paddingLeft: `${16 + row.depth * 24}px` }}>{row.detail.comdCode}</td>
                      <td>{row.detail.comdName}</td>
                      <td className="col-usee">{getUseeYsnoCodeName(useeYsnoCodes, row.detail.useeYsno, row.detail.useeYsnoName)}</td>
                      <td>{row.detail.regiAdmnName ?? row.detail.regiAdmn}</td>
                      <td>{formatDate(row.detail.regiDate)}</td>
                      <td>{row.detail.updtAdmnName ?? row.detail.updtAdmn}</td>
                      <td>{formatDate(row.detail.updtDate)}</td>
                    </tr>
                  ))}
                </Fragment>
              )
            })}
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

/**
 * 세부코드의 공통코드와 코드값을 펼침 상태 식별자로 결합한다
 *
 * @author SeungHyeon.Kang
 * @param detail 식별할 세부코드
 * @return 공통코드 안에서 유일한 세부코드 식별자
 */
function getDetailKey(detail: Code): string {
  // 서로 다른 공통코드에 같은 세부코드가 있어도 충돌하지 않는 키를 반환한다
  return `${detail.commCode}:${detail.comdCode}`
}

/**
 * 세부코드 목록에서 현재 펼쳐진 재귀 분기만 깊이와 함께 평탄화한다
 *
 * @author SeungHyeon.Kang
 * @param details 공통코드의 전체 세부코드
 * @param expandedDetails 펼쳐진 세부코드 식별자 집합
 * @return 화면에 표시할 깊이순 세부코드 행
 */
function getVisibleDetailRows(details: Code[], expandedDetails: Set<string>): VisibleDetailRow[] {
  const detailsByParent = new Map<string, Code[]>()
  const detailCodes = new Set(details.map((detail) => detail.comdCode))
  // 부모가 없는 코드와 고아 코드를 최상위로 묶고 나머지는 부모별로 묶는다
  details.forEach((detail) => {
    const parentCode = detail.upprCode && detailCodes.has(detail.upprCode) ? detail.upprCode : ''
    const siblings = detailsByParent.get(parentCode) ?? []
    // 같은 부모의 자식 목록에 현재 세부코드를 추가한다
    detailsByParent.set(parentCode, [...siblings, detail])
  })
  // 같은 부모 안에서는 정렬 순서와 코드값으로 안정적인 순서를 만든다
  detailsByParent.forEach((siblings, parentCode) => {
    // 정렬된 형제 목록을 부모 코드에 다시 연결한다
    detailsByParent.set(parentCode, [...siblings].sort(compareDetailCodes))
  })

  const visibleRows: VisibleDetailRow[] = []
  const visitedCodes = new Set<string>()

  /**
   * 한 부모의 자식들을 깊이 우선으로 방문해 펼쳐진 행을 추가한다
   *
   * @author SeungHyeon.Kang
   * @param parentCode 자식을 조회할 부모 세부코드
   * @param depth 화면에서 적용할 계층 깊이
   * @return 반환값이 없다
   */
  const visitChildren = (parentCode: string, depth: number): void => {
    const children = detailsByParent.get(parentCode) ?? []
    // 현재 부모의 모든 자식을 정렬된 순서대로 화면 행에 추가한다
    children.forEach((detail) => {
      // 비정상 순환 데이터가 있어도 같은 코드를 두 번 방문하지 않는다
      if (visitedCodes.has(detail.comdCode)) {
        return
      }

      // 재귀 탐색에서 현재 세부코드의 중복 방문을 차단한다
      visitedCodes.add(detail.comdCode)
      const detailKey = getDetailKey(detail)
      const hasChildren = (detailsByParent.get(detail.comdCode)?.length ?? 0) > 0
      const expanded = expandedDetails.has(detailKey)
      // 깊이와 자식 존재 여부를 포함한 화면 행을 추가한다
      visibleRows.push({ detail, depth, hasChildren, expanded })
      // 사용자가 펼친 세부코드만 다음 깊이의 자식까지 이어서 표시한다
      if (hasChildren && expanded) {
        // 한 단계 깊어진 자식 분기를 방문한다
        visitChildren(detail.comdCode, depth + 1)
      }
    })
  }

  // 최상위 세부코드부터 현재 펼친 모든 분기를 방문한다
  visitChildren('', 1)
  // 화면에 표시할 세부코드 계층 행을 반환한다
  return visibleRows
}

/**
 * 같은 부모에 속한 세부코드를 정렬 순서와 코드값으로 비교한다
 *
 * @author SeungHyeon.Kang
 * @param left 왼쪽 세부코드
 * @param right 오른쪽 세부코드
 * @return 정렬 비교 결과
 */
function compareDetailCodes(left: Code, right: Code): number {
  const leftOrder = left.sortOrdr ?? Number.MAX_SAFE_INTEGER
  const rightOrder = right.sortOrdr ?? Number.MAX_SAFE_INTEGER
  // 정렬 순서가 다르면 작은 순서를 먼저 배치한다
  if (leftOrder !== rightOrder) {
    // 정렬 순서 차이를 비교 결과로 반환한다
    return leftOrder - rightOrder
  }

  // 정렬 순서가 같으면 세부코드값으로 안정적인 순서를 반환한다
  return left.comdCode.localeCompare(right.comdCode)
}

/**
 * 코드 계층의 펼침 상태를 화살표 아이콘으로 표시한다
 *
 * @author SeungHyeon.Kang
 * @param expanded 현재 계층이 펼쳐졌는지 여부
 * @return 펼침 또는 접힘 화살표 아이콘
 */
function TreeToggleIcon({ expanded }: { expanded: boolean }) {
  // 현재 펼침 상태에 맞는 방향의 화살표 아이콘을 반환한다
  return (
    <svg viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path
        d={expanded
          ? 'M19.92 15.05L13.4 8.53C12.63 7.76 11.37 7.76 10.6 8.53L4.08 15.05'
          : 'M19.92 8.95L13.4 15.47C12.63 16.24 11.37 16.24 10.6 15.47L4.08 8.95'}
        stroke="currentColor"
        strokeWidth="1.7"
        strokeMiterlimit="10"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </svg>
  )
}
