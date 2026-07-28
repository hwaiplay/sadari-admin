import { useMemo } from 'react'
import type { MouseEvent } from 'react'

type PaginationProps = {
  pageNumber: number
  totalPages: number
  onPageChange: (pageNumber: number) => void
}

/**
 * 현재 페이지를 중심으로 표시할 페이지 번호를 계산한다
 *
 * @author SeungHyeon.Kang
 * @param pageNumber 현재 페이지 번호
 * @param totalPages 전체 페이지 수
 * @return 화면에 표시할 페이지 번호 목록
 */
const getPageNumbers = (pageNumber: number, totalPages: number): number[] => {
  const startPage = Math.max(1, Math.min(pageNumber - 2, totalPages - 4))
  const endPage = Math.min(totalPages, startPage + 4)
  return Array.from({ length: Math.max(0, endPage - startPage + 1) }, (_, index) => startPage + index)
}

/**
 * 목록 페이지 이동 컨트롤을 제공한다
 *
 * @author SeungHyeon.Kang
 * @param pageNumber 현재 페이지 번호
 * @param totalPages 전체 페이지 수
 * @param onPageChange 페이지 변경 함수
 * @return 목록 페이지 이동 컨트롤
 */
export function Pagination({ pageNumber, totalPages, onPageChange }: PaginationProps) {
  const pageNumbers = useMemo(() => getPageNumbers(pageNumber, totalPages), [pageNumber, totalPages])

  /**
   * 버튼 값에 해당하는 페이지로 이동한다
   *
   * @author SeungHyeon.Kang
   * @param event 페이지 버튼 클릭 이벤트
   * @return 반환값이 없다
   */
  const handlePageChange = (event: MouseEvent<HTMLButtonElement>): void => {
    onPageChange(Number(event.currentTarget.value))
  }

  /**
   * 페이지 번호 버튼을 표시한다
   *
   * @author SeungHyeon.Kang
   * @param targetPage 표시할 페이지 번호
   * @return 페이지 번호 버튼
   */
  const renderPageButton = (targetPage: number) => (
    <button key={targetPage} type="button" value={targetPage} className={targetPage === pageNumber ? 'active' : ''} aria-current={targetPage === pageNumber ? 'page' : undefined} onClick={handlePageChange}>
      {targetPage}
    </button>
  )

  // 전체 페이지가 한 페이지 이하면 이동 컨트롤을 표시하지 않는다
  if (totalPages <= 1) {
    return null
  }

  return (
    <>
      {/* 목록 페이지 이동 전체 영역 */}
      <nav className="pagination" aria-label="목록 페이지 이동">
        <button type="button" value={pageNumber - 1} disabled={pageNumber <= 1} onClick={handlePageChange}>이전</button>
        {/* 현재 페이지 주변의 페이지 번호 영역 */}
        {pageNumbers.map(renderPageButton)}
        <button type="button" value={pageNumber + 1} disabled={pageNumber >= totalPages} onClick={handlePageChange}>다음</button>
      </nav>
    </>
  )
}
