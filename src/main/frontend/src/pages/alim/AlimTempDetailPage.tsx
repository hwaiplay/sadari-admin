import type { FormEvent } from 'react'
import { ALIM_TEMP_LIST_PATH } from '../../constants/routes'
import type { AlimTemp, AlimTempForm } from '../../types/alim'
import type { Code } from '../../types/code'
import type { Permission } from '../../types/common'
import { formatDate } from '../../utils/code'

type AlimTempDetailPageProps = {
  isNewPage: boolean
  pageTitle: string
  saving: boolean
  alimTempForm: AlimTempForm
  alimTempDetail: AlimTemp | null
  alimSituCodes: Code[]
  useeYsnoCodes: Code[]
  permission: Permission
  onMovePath: (path: string) => void
  onChange: (field: keyof AlimTempForm, value: string) => void
  onSubmit: (event: FormEvent<HTMLFormElement>) => void
}

/**
 * 알림 템플릿 상세 화면
 * @Author SeungHyeon.Kang
 * @param isNewPage
 * @param pageTitle
 * @param saving
 * @param alimTempForm
 * @param alimTempDetail
 * @param alimSituCodes
 * @param useeYsnoCodes
 * @param permission
 * @param onMovePath
 * @param onChange
 * @param onSubmit
 * @return
 */
export function AlimTempDetailPage({ isNewPage, pageTitle, saving, alimTempForm, alimTempDetail, alimSituCodes, useeYsnoCodes, permission, onMovePath, onChange, onSubmit }: AlimTempDetailPageProps) {
  /**
   * 템플릿 코드 입력값 변경
   * @Author SeungHyeon.Kang
   * @param value
   * @return
   */
  const changeTempCode = (value: string) => {
    onChange('tempCode', value.toUpperCase().replace(/[^A-Z_]/g, ''))
  }

  return (
    <section className="alim-temp-detail-page">
      <section className="content-header">
        <h1>{pageTitle}</h1>
        <div className="header-actions">
          <button type="button" className="subtle-button" onClick={() => onMovePath(ALIM_TEMP_LIST_PATH)}>목록</button>
        </div>
      </section>

      <form className="detail-panel" onSubmit={onSubmit}>
        <div className="detail-title">
          <div>
            <h2>{isNewPage ? '알림 템플릿 등록' : '알림 템플릿 정보'}</h2>
            <p>알림 상황별 템플릿 제목과 내용을 관리합니다.</p>
          </div>
        </div>
        <section className="table-wrap alim-temp-info-table">
          <table>
            <tbody>
              <tr>
                <th>알림상황</th>
                <td>
                  <select value={alimTempForm.alimSitu} onChange={(event) => onChange('alimSitu', event.target.value)} required>
                    {alimSituCodes.map((code) => <option key={code.comdCode} value={code.comdCode}>{code.comdName}</option>)}
                  </select>
                </td>
                <th>템플릿코드</th>
                <td><input value={alimTempForm.tempCode} placeholder="영문 대문자와 _만 입력 가능" onChange={(event) => changeTempCode(event.target.value)} required /></td>
              </tr>
              <tr>
                <th>관리용 제목</th>
                <td><input value={alimTempForm.tempTitl} onChange={(event) => onChange('tempTitl', event.target.value)} required /></td>
                <th>알림 제목</th>
                <td><input value={alimTempForm.alimTitl} onChange={(event) => onChange('alimTitl', event.target.value)} /></td>
              </tr>
              <tr>
                <th>이동 URL</th>
                <td colSpan={3}><input value={alimTempForm.linkUrlx} onChange={(event) => onChange('linkUrlx', event.target.value)} required /></td>
              </tr>
              <tr>
                <th>사용여부</th>
                <td>
                  <select value={alimTempForm.useeYsno} onChange={(event) => onChange('useeYsno', event.target.value)}>
                    {useeYsnoCodes.map((code) => <option key={code.comdCode} value={code.comdCode}>{code.opt1Name ?? code.comdName}</option>)}
                  </select>
                </td>
                {!isNewPage && (
                  <>
                    <th>수정자</th>
                    <td className="readonly-cell">{alimTempDetail?.updtAdmnName ?? alimTempDetail?.updtAdmn ?? ''}</td>
                  </>
                )}
                {isNewPage && <td colSpan={2} />}
              </tr>
              {!isNewPage && (
                <tr>
                  <th>수정일</th>
                  <td className="readonly-cell" colSpan={3}>{formatDate(alimTempDetail?.updtDate ?? null)}</td>
                </tr>
              )}
              <tr>
                <th>템플릿 내용</th>
                <td colSpan={3}>
                  <textarea className="content-textarea" value={alimTempForm.tempCont} onChange={(event) => onChange('tempCont', event.target.value)} required />
                </td>
              </tr>
            </tbody>
          </table>
        </section>
        {permission.writYn && <div className="section-actions"><button type="submit" disabled={saving}>{saving ? '저장 중' : isNewPage ? '저장' : '수정'}</button></div>}
      </form>
    </section>
  )
}
