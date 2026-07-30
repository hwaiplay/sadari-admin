import type { KeyboardEvent, MouseEvent } from 'react'
import { Pagination } from '../../components/Pagination'
import { useMenuPermission } from '../../contexts/useMenuPermission'
import { POPUP_CONTENT_DETAIL_PREFIX, POPUP_CONTENT_NEW_PATH } from '../../constants/routes'
import type { PageData } from '../../types/common'
import type { PopupContent } from '../../types/popupContent'
import { formatDate } from '../../utils/code'
import { getPopupContentAreaCount } from '../../utils/popupContent'

type PopupContentListPageProps = {
  popupContents: PopupContent[]
  pageData: PageData<PopupContent>
  onPageChange: (pageNumber: number) => void
  onMovePath: (path: string) => void
}

/**
 * 팝업 콘텐츠 목록과 등록 진입 버튼을 표시한다
 *
 * @author SeungHyeon.Kang
 * @param popupContents 현재 페이지 팝업 콘텐츠 목록
 * @param pageData 팝업 콘텐츠 페이지 정보
 * @param onPageChange 페이지 이동 처리 함수
 * @param onMovePath 관리자 화면 경로 이동 함수
 * @return 팝업 콘텐츠 목록 화면
 */
export function PopupContentListPage({
  popupContents,
  pageData,
  onPageChange,
  onMovePath,
}: PopupContentListPageProps) {
  // 현재 메뉴의 등록과 수정 가능 여부를 확인한다
  const permission = useMenuPermission()

  /**
   * 목록 행에 저장된 복합키로 팝업 상세 화면을 연다
   *
   * @author SeungHyeon.Kang
   * @param row 클릭하거나 키보드로 선택한 목록 행
   * @return 반환값이 없다
   */
  const movePopupContentDetail = (row: HTMLTableRowElement): void => {
    // 목록 행에서 사용자 화면 구분과 팝업 코드를 읽는다
    const { popuSitu, popuCode } = row.dataset
    // 복합키가 없는 빈 목록 행에서는 상세 이동을 실행하지 않는다
    if (!popuSitu || !popuCode) {
      // 식별할 팝업 콘텐츠가 없어 상세 이동을 종료한다
      return
    }

    // 복합키를 안전하게 인코딩하여 팝업 상세 화면으로 이동한다
    onMovePath(`${POPUP_CONTENT_DETAIL_PREFIX}/${encodeURIComponent(popuSitu)}/${encodeURIComponent(popuCode)}`)
  }

  /**
   * 마우스로 선택한 팝업 목록 행의 상세 화면을 연다
   *
   * @author SeungHyeon.Kang
   * @param event 팝업 목록 행 마우스 이벤트
   * @return 반환값이 없다
   */
  const handlePopupRowClick = (event: MouseEvent<HTMLTableRowElement>): void => {
    // 클릭된 목록 행의 복합키로 상세 화면을 연다
    movePopupContentDetail(event.currentTarget)
  }

  /**
   * Enter 또는 Space로 선택한 팝업 목록 행의 상세 화면을 연다
   *
   * @author SeungHyeon.Kang
   * @param event 팝업 목록 행 키보드 이벤트
   * @return 반환값이 없다
   */
  const handlePopupRowKeyDown = (event: KeyboardEvent<HTMLTableRowElement>): void => {
    // 표준 링크 조작 키 이외의 입력은 목록 이동에 사용하지 않는다
    if (event.key !== 'Enter' && event.key !== ' ') {
      // 상세 이동 대상 키가 아니므로 키보드 처리를 종료한다
      return
    }

    // Space 입력이 페이지 스크롤로 함께 처리되지 않도록 기본 동작을 막는다
    event.preventDefault()
    // 키보드로 선택된 목록 행의 복합키로 상세 화면을 연다
    movePopupContentDetail(event.currentTarget)
  }

  /**
   * 신규 팝업 콘텐츠 등록 화면으로 이동한다
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없다
   */
  const handlePopupRegister = (): void => {
    // 빈 팝업 콘텐츠를 입력할 수 있는 등록 전용 경로로 이동한다
    onMovePath(POPUP_CONTENT_NEW_PATH)
  }

  /**
   * 팝업 콘텐츠 한 건을 클릭 가능한 목록 행으로 렌더링한다
   *
   * @author SeungHyeon.Kang
   * @param popupContent 목록에 표시할 팝업 콘텐츠
   * @return 팝업 콘텐츠 목록 행
   */
  const renderPopupContentRow = (popupContent: PopupContent) => {
    // 관리 제목과 마지막 수정 이력을 확인할 수 있는 팝업 목록 행을 반환한다
    return (
      /* 팝업 콘텐츠 개별 항목 영역 */
      <tr
        key={`${popupContent.popuSitu}-${popupContent.popuCode}`}
        className="popup-content-row"
        role="link"
        tabIndex={0}
        data-popu-situ={popupContent.popuSitu}
        data-popu-code={popupContent.popuCode}
        onClick={handlePopupRowClick}
        onKeyDown={handlePopupRowKeyDown}
      >
        <td><span className="table-link-button">{popupContent.popuSituName ?? popupContent.popuSitu}</span></td>
        <td>{popupContent.popuCode}</td>
        <td>{popupContent.mngmTitl}</td>
        <td className="col-content-area">{getPopupContentAreaCount(popupContent)}개</td>
        <td>{popupContent.updtAdmnName ?? popupContent.updtAdmn ?? popupContent.regiAdmnName ?? popupContent.regiAdmn}</td>
        <td>{formatDate(popupContent.updtDate ?? popupContent.regiDate)}</td>
      </tr>
    )
  }

  // 팝업 콘텐츠 목록과 페이지 이동 및 등록 진입 화면을 반환한다
  return (
    /* 팝업 콘텐츠 목록 전체 영역 */
    <section className="popup-content-manage">
      {/* 팝업 콘텐츠 목록 제목과 전체 건수 영역 */}
      <section className="content-header">
        {/* "팝업 관리" */}
        <h1>팝업 관리</h1>
        <div className="status">총 {pageData.totalCount}건</div>
      </section>

      {/* 팝업 콘텐츠 목록 표 영역 */}
      <section className="table-wrap popup-content-list-table">
        <table>
          <thead>
            <tr>
              {/* "사용 화면" */}
              <th>사용 화면</th>
              {/* "팝업 코드" */}
              <th>팝업 코드</th>
              {/* "관리용 제목" */}
              <th>관리용 제목</th>
              {/* "콘텐츠 영역" */}
              <th className="col-content-area">콘텐츠 영역</th>
              {/* "최종 수정자" */}
              <th>최종 수정자</th>
              {/* "최종 수정일" */}
              <th>최종 수정일</th>
            </tr>
          </thead>
          {/* 팝업 콘텐츠 목록 데이터 영역 */}
          <tbody>
            {popupContents.length === 0 ? (
              <tr className="empty-row">
                {/* "관리할 팝업 콘텐츠가 없습니다." */}
                <td colSpan={6}>관리할 팝업 콘텐츠가 없습니다.</td>
              </tr>
            ) : popupContents.map(renderPopupContentRow)}
          </tbody>
        </table>
      </section>

      {/* 팝업 콘텐츠 목록 페이지 이동 영역 */}
      <Pagination pageNumber={pageData.pageNumber} totalPages={pageData.totalPages} onPageChange={onPageChange} />

      {/* 팝업 콘텐츠 신규 등록 진입 영역 */}
      {permission.writYsno === 'Y' && (
        <>
          {/* "등록" */}
          <button type="button" className="floating-button" onClick={handlePopupRegister}>등록</button>
        </>
      )}
    </section>
  )
}
