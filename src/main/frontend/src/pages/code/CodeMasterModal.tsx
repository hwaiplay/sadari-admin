import type { FormEvent } from 'react'
import type { Code, CodeMaster } from '../../types/code'

type CodeMasterModalProps = {
  masterForm: CodeMaster
  useeYsnoCodes: Code[]
  saving: boolean
  duplicateAvailable: boolean
  duplicateCheckedCode: string
  onChange: (form: CodeMaster) => void
  onClose: () => void
  onCheckDuplicate: () => void
  onSubmit: (event: FormEvent<HTMLFormElement>) => void
}

/**
 * 공통코드 등록 모달
 * @Author SeungHyeon.Kang
 * @param masterForm
 * @param useeYsnoCodes
 * @param saving
 * @param duplicateAvailable
 * @param duplicateCheckedCode
 * @param onChange
 * @param onClose
 * @param onCheckDuplicate
 * @param onSubmit
 * @return
 */
export function CodeMasterModal({ masterForm, useeYsnoCodes, saving, duplicateAvailable, duplicateCheckedCode, onChange, onClose, onCheckDuplicate, onSubmit }: CodeMasterModalProps) {
  const checkedCurrentCode = duplicateCheckedCode === masterForm.commCode.trim() && duplicateCheckedCode !== ''

  return (
    <section className="modal-backdrop">
      <form className="modal-panel" onSubmit={onSubmit}>
        <h2>공통코드 등록</h2>
        <label>
          <span>공통코드</span>
          <div className="inline-check">
            <input value={masterForm.commCode} onChange={(event) => onChange({ ...masterForm, commCode: event.target.value })} required />
            <button type="button" onClick={onCheckDuplicate}>중복검사</button>
          </div>
          {checkedCurrentCode && <p className={duplicateAvailable ? 'duplicate-message success' : 'duplicate-message fail'}>{duplicateAvailable ? '사용가능한 코드입니다.' : '이미 사용중인 코드입니다.'}</p>}
        </label>
        <label><span>공통코드명</span><input value={masterForm.codeName} onChange={(event) => onChange({ ...masterForm, codeName: event.target.value })} required /></label>
        <label><span>설명</span><input value={masterForm.codeExpl ?? ''} onChange={(event) => onChange({ ...masterForm, codeExpl: event.target.value })} /></label>
        <label>
          <span>사용여부</span>
          <select value={masterForm.useeYsno ?? 'Y'} onChange={(event) => onChange({ ...masterForm, useeYsno: event.target.value })}>
            {useeYsnoCodes.map((code) => <option key={code.comdCode} value={code.comdCode}>{code.opt1Name ?? code.comdName}</option>)}
          </select>
        </label>
        <div className="form-actions">
          <button type="button" className="subtle-button" onClick={onClose}>취소</button>
          <button type="submit" disabled={saving || !duplicateAvailable}>등록</button>
        </div>
      </form>
    </section>
  )
}
