import type { ChangeEvent, FormEvent } from 'react'
import { AuditInfoTable } from '../../components/AuditInfoTable'
import { POPUP_CONTENT_LIST_PATH } from '../../constants/routes'
import { useMenuPermission } from '../../contexts/useMenuPermission'
import type { Code } from '../../types/code'
import type { PopupContent, PopupContentForm } from '../../types/popupContent'

type PopupContentDetailPageProps = {
  isNewPage: boolean
  loading: boolean
  saving: boolean
  popupContentForm: PopupContentForm
  popupContentDetail: PopupContent | null
  popupSituCodes: Code[]
  onMovePath: (path: string) => void
  onChange: (field: keyof PopupContentForm, value: string) => void
  onSubmit: (event: FormEvent<HTMLFormElement>) => void
}

/**
 * 팝업 복합키와 네 개 목록 콘텐츠 영역을 등록하거나 수정하는 화면을 표시한다
 *
 * @author SeungHyeon.Kang
 * @param isNewPage 신규 등록 화면 여부
 * @param loading 상세 조회 진행 여부
 * @param saving 저장 진행 여부
 * @param popupContentForm 팝업 콘텐츠 입력값
 * @param popupContentDetail 조회된 팝업 콘텐츠 상세
 * @param popupSituCodes 팝업 사용 화면 구분 공통코드
 * @param onMovePath 관리자 화면 경로 이동 함수
 * @param onChange 팝업 콘텐츠 입력값 변경 함수
 * @param onSubmit 팝업 콘텐츠 저장 함수
 * @return 팝업 콘텐츠 등록 또는 상세 수정 화면
 */
export function PopupContentDetailPage({
  isNewPage,
  loading,
  saving,
  popupContentForm,
  popupContentDetail,
  popupSituCodes,
  onMovePath,
  onChange,
  onSubmit,
}: PopupContentDetailPageProps) {
  // 현재 메뉴의 등록과 수정 가능 여부를 확인한다
  const permission = useMenuPermission()

  /**
   * 이름이 지정된 입력 요소의 팝업 콘텐츠 값을 변경한다
   *
   * @author SeungHyeon.Kang
   * @param event 팝업 콘텐츠 입력 변경 이벤트
   * @return 반환값이 없다
   */
  const handleInputChange = (event: ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>): void => {
    // 입력 요소 이름을 팝업 콘텐츠 폼 필드로 사용한다
    const field = event.currentTarget.name as keyof PopupContentForm
    // 현재 입력 요소의 값을 상위 관리 상태에 반영한다
    onChange(field, event.currentTarget.value)
  }

  /**
   * 팝업 코드를 영문 대문자와 숫자 및 밑줄 형식으로 변경한다
   *
   * @author SeungHyeon.Kang
   * @param event 팝업 코드 입력 변경 이벤트
   * @return 반환값이 없다
   */
  const handlePopupCodeChange = (event: ChangeEvent<HTMLInputElement>): void => {
    // 사용자 화면 API 식별 규칙에 맞게 소문자와 허용하지 않는 문자를 제거한다
    const popupCode = event.currentTarget.value.toUpperCase().replace(/[^A-Z0-9_]/gu, '')
    // 정규화한 팝업 코드를 상위 관리 상태에 반영한다
    onChange('popuCode', popupCode)
  }

  /**
   * 팝업 사용 화면 공통코드 한 건을 선택 항목으로 렌더링한다
   *
   * @author SeungHyeon.Kang
   * @param code 팝업 사용 화면 구분 공통코드
   * @return 팝업 사용 화면 선택 항목
   */
  const renderPopupSituOption = (code: Code) => {
    // 관리자가 확인할 화면 코드명과 실제 코드값을 연결한 선택 항목을 반환한다
    return <option key={code.comdCode} value={code.comdCode}>{code.comdName}</option>
  }

  /**
   * 팝업 콘텐츠 목록 화면으로 이동한다
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없다
   */
  const handleListMove = (): void => {
    // 현재 편집 화면에서 팝업 콘텐츠 목록으로 이동한다
    onMovePath(POPUP_CONTENT_LIST_PATH)
  }

  // 팝업 콘텐츠 식별값과 네 개 목록 문구 및 감사정보를 관리하는 화면을 반환한다
  return (
    /* 팝업 콘텐츠 상세 관리 전체 영역 */
    <section className="popup-content-detail-page">
      {/* 팝업 콘텐츠 상세 제목 영역 */}
      <section className="content-header">
        {/* "팝업 등록" 또는 "팝업 관리 상세" */}
        <h1>{isNewPage ? '팝업 등록' : '팝업 관리 상세'}</h1>
      </section>

      {/* 팝업 콘텐츠 입력과 저장 영역 */}
      <form onSubmit={onSubmit}>
        {/* 팝업 콘텐츠 기본 정보와 목록 문구 입력 영역 */}
        <section className="detail-panel">
          {/* 팝업 콘텐츠 편집 안내 영역 */}
          <div className="detail-title">
            <div>
              {/* "팝업 콘텐츠 등록" 또는 "팝업 콘텐츠 정보" */}
              <h2>{isNewPage ? '팝업 콘텐츠 등록' : '팝업 콘텐츠 정보'}</h2>
              {/* "각 내용 영역은 한 줄당 하나의 목록 문구로 저장됩니다." */}
              <p>각 내용 영역은 한 줄당 하나의 목록 문구로 저장됩니다.</p>
            </div>
          </div>

          {/* 팝업 콘텐츠 복합키와 관리용 제목 영역 */}
          <section className="table-wrap popup-content-info-table">
            <table>
              <tbody>
                <tr>
                  {/* "사용 화면" */}
                  <th>사용 화면</th>
                  <td>
                    <select
                      name="popuSitu"
                      value={popupContentForm.popuSitu}
                      onChange={handleInputChange}
                      disabled={!isNewPage || loading}
                      required
                    >
                      {popupSituCodes.map(renderPopupSituOption)}
                    </select>
                  </td>
                  {/* "팝업 코드" */}
                  <th>팝업 코드</th>
                  <td>
                    <input
                      name="popuCode"
                      value={popupContentForm.popuCode}
                      onChange={handlePopupCodeChange}
                      readOnly={!isNewPage || loading}
                      maxLength={100}
                      required
                    />
                  </td>
                </tr>
                <tr>
                  {/* "관리용 제목" */}
                  <th>관리용 제목</th>
                  <td colSpan={3}>
                    <input
                      name="mngmTitl"
                      value={popupContentForm.mngmTitl}
                      onChange={handleInputChange}
                      maxLength={200}
                      disabled={loading}
                      required
                    />
                  </td>
                </tr>
              </tbody>
            </table>
          </section>

          {/* 사용자 화면에 표시되는 네 개 목록 콘텐츠 영역 */}
          <section className="popup-content-fields">
            {/* 첫 번째 필수 목록 콘텐츠 영역 */}
            <label className="popup-content-field">
              {/* "내용 1" */}
              <span>내용 1 <strong>필수</strong></span>
              <textarea
                name="contFirs"
                value={popupContentForm.contFirs}
                onChange={handleInputChange}
                className="popup-content-textarea"
                rows={5}
                disabled={loading}
                required
              />
            </label>

            <label className="popup-content-field">
              <span>영문 내용 1 <strong>필수</strong></span>
              <textarea name="englFirs" value={popupContentForm.englFirs} onChange={handleInputChange}
                className="popup-content-textarea" rows={5} disabled={loading} required />
            </label>

            {/* 두 번째 선택 목록 콘텐츠 영역 */}
            <label className="popup-content-field">
              {/* "내용 2" */}
              <span>내용 2 <em>선택</em></span>
              <textarea
                name="contSeco"
                value={popupContentForm.contSeco}
                onChange={handleInputChange}
                className="popup-content-textarea"
                rows={5}
                disabled={loading}
              />
            </label>

            <label className="popup-content-field">
              <span>영문 내용 2 <em>선택</em></span>
              <textarea name="englSeco" value={popupContentForm.englSeco} onChange={handleInputChange}
                className="popup-content-textarea" rows={5} disabled={loading} />
            </label>

            {/* 세 번째 선택 목록 콘텐츠 영역 */}
            <label className="popup-content-field">
              {/* "내용 3" */}
              <span>내용 3 <em>선택</em></span>
              <textarea
                name="contThir"
                value={popupContentForm.contThir}
                onChange={handleInputChange}
                className="popup-content-textarea"
                rows={5}
                disabled={loading}
              />
            </label>

            <label className="popup-content-field">
              <span>영문 내용 3 <em>선택</em></span>
              <textarea name="englThir" value={popupContentForm.englThir} onChange={handleInputChange}
                className="popup-content-textarea" rows={5} disabled={loading} />
            </label>

            {/* 네 번째 선택 목록 콘텐츠 영역 */}
            <label className="popup-content-field">
              {/* "내용 4" */}
              <span>내용 4 <em>선택</em></span>
              <textarea
                name="contFour"
                value={popupContentForm.contFour}
                onChange={handleInputChange}
                className="popup-content-textarea"
                rows={5}
                disabled={loading}
              />
            </label>

            <label className="popup-content-field">
              <span>영문 내용 4 <em>선택</em></span>
              <textarea name="englFour" value={popupContentForm.englFour} onChange={handleInputChange}
                className="popup-content-textarea" rows={5} disabled={loading} />
            </label>
          </section>
        </section>

        {/* 기존 팝업 콘텐츠 등록과 수정 이력 영역 */}
        {!isNewPage && popupContentDetail && (
          <AuditInfoTable
            regiAdmn={popupContentDetail.regiAdmn}
            regiAdmnName={popupContentDetail.regiAdmnName}
            regiDate={popupContentDetail.regiDate}
            updtAdmn={popupContentDetail.updtAdmn}
            updtAdmnName={popupContentDetail.updtAdmnName}
            updtDate={popupContentDetail.updtDate}
          />
        )}

        {/* 팝업 콘텐츠 목록 이동과 저장 버튼 영역 */}
        <div className="detail-footer">
          <div className="detail-footer-left">
            {/* "목록" */}
            <button type="button" className="subtle-button" onClick={handleListMove}>목록</button>
          </div>
          <div className="detail-footer-right">
            {permission.writYsno === 'Y' && (
              <>
                {/* "저장", "수정" 또는 "저장 중" */}
                <button type="submit" disabled={saving || loading}>
                  {saving ? '저장 중' : isNewPage ? '저장' : '수정'}
                </button>
              </>
            )}
          </div>
        </div>
      </form>
    </section>
  )
}
