import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { getDeletedSuspensions, uptDeletedSuspension } from '../../api/currentUserApi'
import { Pagination } from '../../components/Pagination'
import { CURRENT_USER_LIST_PATH } from '../../constants/routes'
import type { PageData } from '../../types/common'
import type { CurrentUserSuspension, DeletedSuspensionSearch } from '../../types/currentUser'
import { formatDate } from '../../utils/code'

type DeletedSuspensionPageProps = {
  onMovePath: (path: string) => void
  onError: (message: string | null) => void
}

const DEFAULT_SEARCH: DeletedSuspensionSearch = { userNumb: '' }

const EMPTY_PAGE: PageData<CurrentUserSuspension> = {
  items: [],
  totalCount: 0,
  pageNumber: 1,
  pageSize: 20,
  totalPages: 0,
}

/**
 * 물리 삭제된 회원에게 남아 있는 유효 제재 목록과 관리자 해제 기능을 제공한다
 *
 * @author SeungHyeon.Kang
 * @param onMovePath 화면 경로 이동 함수
 * @param onError 공통 오류 메시지 변경 함수
 * @return 삭제 회원 제재 관리 화면
 */
export function DeletedSuspensionPage({ onMovePath, onError }: DeletedSuspensionPageProps) {
  const [search, setSearch] = useState<DeletedSuspensionSearch>({ ...DEFAULT_SEARCH })
  const [appliedSearch, setAppliedSearch] = useState<DeletedSuspensionSearch>({ ...DEFAULT_SEARCH })
  const [pageData, setPageData] = useState<PageData<CurrentUserSuspension>>(EMPTY_PAGE)
  const [loading, setLoading] = useState(true)
  const [selected, setSelected] = useState<CurrentUserSuspension | null>(null)
  const [releaseMemo, setReleaseMemo] = useState('')
  const [saving, setSaving] = useState(false)

  /**
   * 지정한 조건과 페이지로 삭제 회원의 유효 제재를 조회한다
   *
   * @author SeungHyeon.Kang
   * @param pageNumber 조회할 페이지 번호
   * @param targetSearch 적용할 검색 조건
   * @return 반환값이 없다
   */
  const loadSuspensions = async (
    pageNumber: number,
    targetSearch: DeletedSuspensionSearch,
  ): Promise<void> => {
    // 목록 조회 중 중복 동작을 구분할 수 있도록 로딩 상태를 표시한다
    setLoading(true)
    // 기존 목록은 조회 실패 시 유지하고 공통 오류로 안내한다
    try {
      // 해시가 제외된 유효 제재 목록을 조회한다
      const result = await getDeletedSuspensions(pageNumber, targetSearch)
      // 조회된 페이지를 목록 상태에 반영한다
      setPageData(result)
      // 정상 조회 뒤 이전 오류를 제거한다
      onError(null)
    }

    catch (error: unknown) {
      // "삭제 회원 제재 목록을 불러오지 못했습니다."
      onError(error instanceof Error ? error.message : '삭제 회원 제재 목록을 불러오지 못했습니다.')
    }

    finally {
      // 성공 여부와 관계없이 목록 로딩 상태를 종료한다
      setLoading(false)
    }
  }

  // 첫 진입에는 과거 회원 번호 조건 없이 전체 유효 제재를 조회한다
  useEffect(() => {
    // 화면이 해제된 뒤 도착한 응답이 목록 상태를 바꾸지 않도록 활성 여부를 기록한다
    let active = true
    // 최초 페이지의 삭제 회원 제재 목록을 비동기로 조회한다
    getDeletedSuspensions(1, DEFAULT_SEARCH)
      .then((result) => {
        // 화면이 유지되는 동안에만 최초 조회 결과를 반영한다
        if (active) {
          // 조회된 유효 제재의 첫 페이지를 목록에 표시한다
          setPageData(result)
          // 정상 조회 뒤 이전 공통 오류를 제거한다
          onError(null)
          // 최초 목록 로딩 상태를 종료한다
          setLoading(false)
        }
      })
      .catch((error: unknown) => {
        // 화면이 유지되는 동안에만 조회 오류를 안내한다
        if (active) {
          // "삭제 회원 제재 목록을 불러오지 못했습니다."
          onError(error instanceof Error ? error.message : '삭제 회원 제재 목록을 불러오지 못했습니다.')
          // 실패한 최초 목록 조회의 로딩 상태를 종료한다
          setLoading(false)
        }
      })
    // 화면 해제 뒤 비동기 응답을 무시하도록 활성 상태를 제거한다
    return () => {
      // 이미 해제된 화면에는 조회 결과를 반영하지 않는다
      active = false
    }
  }, [onError])

  /**
   * 입력한 과거 회원 번호로 삭제 회원 제재를 검색한다
   *
   * @author SeungHyeon.Kang
   * @param event 검색 폼 제출 이벤트
   * @return 반환값이 없다
   */
  const handleSearch = (event: FormEvent<HTMLFormElement>): void => {
    // 브라우저 기본 폼 이동을 막고 현재 관리자 화면에서 조회한다
    event.preventDefault()
    // 페이지 이동에도 같은 조건을 유지하도록 적용 조건을 복사한다
    const nextSearch = { ...search }
    // 검색 버튼을 누른 시점의 조건을 페이지 상태에 저장한다
    setAppliedSearch(nextSearch)
    // 변경된 조건은 첫 페이지부터 조회한다
    void loadSuspensions(1, nextSearch)
  }

  /**
   * 삭제 회원 제재 검색조건을 전체로 초기화한다
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없다
   */
  const handleReset = (): void => {
    // 입력값과 페이지 이동 조건을 모두 전체 조회로 되돌린다
    const nextSearch = { ...DEFAULT_SEARCH }
    // 과거 회원 번호 입력값을 제거한다
    setSearch(nextSearch)
    // 적용된 검색조건도 전체로 변경한다
    setAppliedSearch(nextSearch)
    // 전체 유효 제재의 첫 페이지를 조회한다
    void loadSuspensions(1, nextSearch)
  }

  /**
   * 선택한 삭제 회원 제재의 해제 메모 입력창을 연다
   *
   * @author SeungHyeon.Kang
   * @param suspension 해제할 유효 제재
   * @return 반환값이 없다
   */
  const handleReleaseOpen = (suspension: CurrentUserSuspension): void => {
    // 해제 대상의 과거 회원 번호와 제재 번호를 모달 상태에 보관한다
    setSelected(suspension)
    // 다른 제재에서 입력했던 메모를 새 해제 요청에 재사용하지 않는다
    setReleaseMemo('')
  }

  /**
   * 삭제 회원 제재 해제 입력창을 닫는다
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없다
   */
  const handleReleaseClose = (): void => {
    // 저장 중에는 대상이 바뀌어 이력이 어긋나지 않도록 닫기를 제한한다
    if (saving) {
      // 진행 중인 해제 처리가 끝날 때까지 모달을 유지한다
      return
    }

    // 현재 선택한 제재를 제거해 모달을 닫는다
    setSelected(null)
    // 입력한 관리자 메모를 화면 상태에서 제거한다
    setReleaseMemo('')
  }

  /**
   * 필수 관리자 메모와 함께 삭제 회원의 유효 제재를 해제한다
   *
   * @author SeungHyeon.Kang
   * @param event 해제 폼 제출 이벤트
   * @return 반환값이 없다
   */
  const handleRelease = async (event: FormEvent<HTMLFormElement>): Promise<void> => {
    // 모달 폼의 기본 화면 이동을 막는다
    event.preventDefault()
    // 선택 대상과 해제 근거가 모두 있어야 서버 변경을 요청한다
    if (!selected || !releaseMemo.trim()) {
      // "해제 메모를 입력해주세요."
      onError('해제 메모를 입력해주세요.')
      // 필수값이 없는 해제 요청을 종료한다
      return
    }

    // 동일 제재의 중복 해제 제출을 막는다
    setSaving(true)
    // 해제 실패 시 선택 대상과 입력 메모를 유지한다
    try {
      // 과거 회원 번호와 제재 번호 및 판단 근거를 감사 이력에 저장한다
      await uptDeletedSuspension(selected.userNumb, selected.spndNumb, releaseMemo.trim())
      // 해제된 제재가 목록에서 제거되도록 현재 페이지를 다시 조회한다
      await loadSuspensions(pageData.pageNumber, appliedSearch)
      // 처리 완료된 제재 선택을 제거해 모달을 닫는다
      setSelected(null)
      // 다음 해제 요청에 이전 메모가 남지 않도록 제거한다
      setReleaseMemo('')
      // 정상 해제 뒤 이전 오류를 제거한다
      onError(null)
    }

    catch (error: unknown) {
      // "삭제 회원 제재를 해제하지 못했습니다."
      onError(error instanceof Error ? error.message : '삭제 회원 제재를 해제하지 못했습니다.')
    }

    finally {
      // 성공 여부와 관계없이 해제 저장 상태를 종료한다
      setSaving(false)
    }
  }

  /**
   * 삭제 회원에게 남아 있는 유효 제재 한 행을 표시한다
   *
   * @author SeungHyeon.Kang
   * @param suspension 표시할 유효 제재
   * @return 삭제 회원 제재 목록 행
   */
  const renderSuspensionRow = (suspension: CurrentUserSuspension) => (
    <tr key={suspension.spndNumb}>
      <td className="col-user-number">{suspension.userNumb}</td>
      <td className="col-history-number">{suspension.spndNumb}</td>
      <td>{suspension.spndTypeName ?? suspension.spndType}</td>
      <td>{suspension.spndRsonName ?? suspension.spndRson}</td>
      <td className="col-date-time">{formatDate(suspension.strtDate)}</td>
      <td className="col-date-time">{suspension.endxDate ? formatDate(suspension.endxDate) : '무기한'}</td>
      <td>{suspension.regiAdmnName ?? suspension.regiAdmn}</td>
      <td className="col-date-time">{formatDate(suspension.regiDate)}</td>
      <td>
        <button type="button" className="subtle-button" onClick={() => handleReleaseOpen(suspension)}>
          해제
        </button>
      </td>
    </tr>
  )

  // 삭제 회원 제재 검색과 목록 및 해제 모달을 반환한다
  return (
    <section className="current-user-page">
      {/* 삭제 회원 제재 화면 제목과 현재 조회 건수 */}
      <section className="content-header">
        <h1>삭제회원 제재</h1>
        {/* 목록 복귀와 조회 건수 영역 */}
        <div className="header-actions">
          <div className="status">총 {pageData.totalCount.toLocaleString()}건</div>
          <button type="button" className="subtle-button" onClick={() => onMovePath(CURRENT_USER_LIST_PATH)}>현 사용자 관리</button>
        </div>
      </section>

      {/* 과거 회원 번호 검색 영역 */}
      <form className="list-search" onSubmit={handleSearch}>
        <label>
          <span>과거 회원번호</span>
          <input
            value={search.userNumb}
            inputMode="numeric"
            maxLength={19}
            placeholder="회원번호"
            onChange={(event) => setSearch({ userNumb: event.target.value.replace(/\D/g, '') })}
          />
        </label>
        {/* 검색 실행과 전체 조건 초기화 영역 */}
        <div className="list-search-actions">
          <button type="button" className="subtle-button" onClick={handleReset}>초기화</button>
          <button type="submit">검색</button>
        </div>
      </form>

      {/* 물리 삭제 뒤에도 유효한 제재 목록 */}
      <section className="table-wrap">
        <table>
          <thead>
            <tr>
              <th>과거 회원번호</th>
              <th>제재번호</th>
              <th>유형</th>
              <th>사유</th>
              <th>시작일</th>
              <th>종료일</th>
              <th>등록 관리자</th>
              <th>등록일</th>
              <th>관리</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr className="empty-row"><td colSpan={9}>삭제 회원 제재 목록을 불러오고 있습니다.</td></tr>
            ) : pageData.items.length === 0 ? (
              <tr className="empty-row"><td colSpan={9}>유효한 삭제 회원 제재가 없습니다.</td></tr>
            ) : pageData.items.map(renderSuspensionRow)}
          </tbody>
        </table>
      </section>

      {/* 삭제 회원 제재 목록 페이지 이동 */}
      <Pagination
        pageNumber={pageData.pageNumber}
        totalPages={pageData.totalPages}
        onPageChange={(pageNumber) => void loadSuspensions(pageNumber, appliedSearch)}
      />

      {/* 선택한 삭제 회원 제재의 필수 해제 메모 모달 */}
      {selected && (
        <section className="modal-backdrop" role="presentation">
          {/* 해제 대상과 판단 근거 입력 영역 */}
          <form className="modal-panel" role="dialog" aria-modal="true" aria-labelledby="deleted-suspension-release-title" onSubmit={handleRelease}>
            <h2 id="deleted-suspension-release-title">삭제회원 제재 해제</h2>
            <p className="small">과거 회원번호 {selected.userNumb} · 제재번호 {selected.spndNumb}</p>
            <label>
              <span>해제 메모</span>
              <textarea
                value={releaseMemo}
                maxLength={1000}
                rows={6}
                placeholder="해제 판단 근거를 입력해주세요."
                onChange={(event) => setReleaseMemo(event.target.value)}
              />
            </label>
            {/* 해제 취소와 저장 영역 */}
            <div className="form-actions">
              <button type="button" className="subtle-button" disabled={saving} onClick={handleReleaseClose}>취소</button>
              <button type="submit" disabled={saving || !releaseMemo.trim()}>{saving ? '해제 중' : '제재 해제'}</button>
            </div>
          </form>
        </section>
      )}
    </section>
  )
}
