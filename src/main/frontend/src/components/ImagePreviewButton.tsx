import { useId, useState } from 'react'

type ImagePreviewButtonProps = {
  imagePath: string | null
  buttonLabel: string
  dialogTitle: string
  emptyLabel: string
}

/**
 * 등록된 사용자 이미지를 기존 정보 그리드 밖의 모달에서 확인하게 한다
 *
 * @author SeungHyeon.Kang
 * @param imagePath 관리자 저장소 이미지 접근 경로
 * @param buttonLabel 이미지 보기 버튼 문구
 * @param dialogTitle 이미지 모달 제목
 * @param emptyLabel 이미지 미등록 문구
 * @return 이미지 보기 버튼 또는 미등록 문구와 이미지 모달
 */
export function ImagePreviewButton({
  imagePath,
  buttonLabel,
  dialogTitle,
  emptyLabel,
}: ImagePreviewButtonProps) {
  // 사용자 이미지 모달의 현재 노출 상태를 관리한다
  const [isOpen, setOpen] = useState(false)
  // 저장소 이미지 조회 실패 안내 상태를 관리한다
  const [hasImageError, setImageError] = useState(false)
  // 같은 상세 화면에 여러 이미지 모달이 있어도 제목 연결값이 겹치지 않게 생성한다
  const titleId = useId()
  // 비어 있거나 문자열로 직렬화된 무효 경로는 이미지 미등록 상태로 정규화한다
  const normalizedImagePath = imagePath?.trim()
  // 실제 저장소 접근 경로가 있을 때만 이미지 보기 버튼을 활성화한다
  const hasImagePath = Boolean(
    normalizedImagePath
      && normalizedImagePath !== 'null'
      && normalizedImagePath !== 'undefined',
  )

  /**
   * 선택한 사용자 이미지 모달을 연다
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없다
   */
  const handleOpen = (): void => {
    // 이전 열기에서 발생한 이미지 오류를 새 조회 전에 초기화한다
    setImageError(false)
    // 기존 정보 그리드를 변경하지 않는 별도 이미지 모달을 표시한다
    setOpen(true)
  }

  /**
   * 사용자 이미지 모달을 닫는다
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없다
   */
  const handleClose = (): void => {
    // 이미지 확인이 끝난 모달을 화면에서 제거한다
    setOpen(false)
  }

  /**
   * 저장소 이미지 조회 실패를 모달 안내 상태로 전환한다
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없다
   */
  const handleImageError = (): void => {
    // 삭제되었거나 읽을 수 없는 이미지의 실패 안내를 표시한다
    setImageError(true)
  }

  // 이미지 경로가 없으면 저장소 요청과 보기 버튼을 만들지 않는다
  if (!hasImagePath || !normalizedImagePath) {
    // 호출부가 지정한 이미지 미등록 문구를 반환한다
    return <span className="image-preview-empty">{emptyLabel}</span>
  }

  // 기존 표 그리드를 유지하는 보기 버튼과 독립된 이미지 모달을 반환한다
  return (
    <>
      <button type="button" className="image-preview-button" onClick={handleOpen}>
        {buttonLabel}
      </button>

      {/* 사용자 이미지 원본 확인 모달 전체 영역 */}
      {isOpen && (
        <div className="modal-backdrop image-preview-backdrop">
          {/* 사용자 이미지 모달 본문 영역 */}
          <section
            className="modal-panel image-preview-modal"
            role="dialog"
            aria-modal="true"
            aria-labelledby={titleId}
          >
            {/* 사용자 이미지 모달 제목과 닫기 버튼 영역 */}
            <header className="image-preview-header">
              <h2 id={titleId}>{dialogTitle}</h2>
              {/* "닫기" */}
              <button type="button" className="subtle-button" onClick={handleClose}>닫기</button>
            </header>

            {/* 저장소 사용자 이미지 표시 영역 */}
            <div className="image-preview-content">
              {/* 저장소 조회에 성공한 사용자 이미지 영역 */}
              {!hasImageError && (
                <img src={normalizedImagePath} alt={dialogTitle} onError={handleImageError} />
              )}
              {/* "이미지를 불러올 수 없습니다." */}
              {hasImageError && (
                <p className="empty small">
                  이미지를 불러올 수 없습니다.
                </p>
              )}
            </div>
          </section>
        </div>
      )}
    </>
  )
}
