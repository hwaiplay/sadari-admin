import { useEffect, useRef, useState } from 'react'
import type { FormEvent } from 'react'
import { AuditInfoTable } from '../../components/AuditInfoTable'
import { useMenuPermission } from '../../contexts/useMenuPermission'
import { ALIM_ICON_LIST_PATH } from '../../constants/routes'
import type { AlimIcon } from '../../types/alim'

type Props = {
  saving: boolean
  detail: AlimIcon
  onMovePath: (path: string) => void
  onSave: (alimSitu: string, file: File) => void
}

/** 알림 상황 공통코드의 SVG 또는 PNG 등록과 원본 교체를 제공한다. */
export function AlimIconDetailPage({ saving, detail, onMovePath, onSave }: Props) {
  const permission = useMenuPermission()
  const [file, setFile] = useState<File | null>(null)
  const currentImageUrl = detail.iconRegiYsno === 'Y' ? `/api/alim-icons/${encodeURIComponent(detail.alimSitu)}/image` : ''
  const [previewUrl, setPreviewUrl] = useState(currentImageUrl)
  const objectUrlRef = useRef('')

  // 컴포넌트가 해제될 때 마지막 아이콘 미리보기용 브라우저 URL을 해제한다
  useEffect(() => {
    // Effect 정리 함수에서 현재 임시 URL만 해제한다
    return () => {
      // 브라우저가 생성한 임시 URL이 있을 때만 메모리를 반환한다
      if (objectUrlRef.current) URL.revokeObjectURL(objectUrlRef.current)
    }
  }, [])

  /** 선택한 SVG 또는 PNG 파일의 입력 상태와 로컬 미리보기를 함께 갱신한다. */
  const handleFileChange = (nextFile: File | null) => {
    // 이전 파일의 임시 URL을 먼저 해제해 반복 선택 시 브라우저 메모리가 누적되지 않게 한다
    if (objectUrlRef.current) URL.revokeObjectURL(objectUrlRef.current)
    objectUrlRef.current = nextFile ? URL.createObjectURL(nextFile) : ''
    setFile(nextFile)
    setPreviewUrl(objectUrlRef.current || currentImageUrl)
  }

  /** 아이콘 파일이 선택된 경우 알림 상황별 저장 처리로 전달한다. */
  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    if (!file) return
    onSave(detail.alimSitu, file)
  }

  return (
    <section className="alim-icon-detail-page">
      <section className="content-header"><h1>알림 아이콘 상세</h1></section>
      <form className="detail-panel" onSubmit={handleSubmit}>
        <div className="detail-title"><div><h2>{detail.alimSituName}</h2><p>알림상황 코드에 아이콘을 등록하며, 미등록 상황은 사용자 화면에서 DEFAULT 아이콘으로 표시됩니다.</p></div></div>
        <section className="table-wrap"><table className="alim-icon-detail-table"><tbody>
          <tr><th>알림상황 코드</th><td>{detail.alimSitu}</td><th>공통코드명</th><td>{detail.alimSituName}</td></tr>
          <tr><th>공통코드 사용여부</th><td>{detail.useeYsno}</td><th>아이콘 등록여부</th><td>{detail.iconRegiYsno === 'Y' ? '등록' : '미등록'}</td></tr>
          <tr><th>아이콘 이미지</th><td colSpan={3}><div className="alim-icon-upload"><div className="alim-icon-upload-control"><input type="file" accept=".svg,.png,image/svg+xml,image/png" required onChange={(event) => handleFileChange(event.target.files?.[0] ?? null)} /><p>200KB 이하, 정사각형, 한 변 16~256px SVG 또는 PNG만 등록할 수 있습니다.</p></div>{previewUrl && <img className="alim-icon-preview-large" src={previewUrl} alt="아이콘 미리보기" />}</div></td></tr>
        </tbody></table></section>
      </form>
      {detail.iconRegiYsno === 'Y' && <AuditInfoTable regiAdmn={detail.regiAdmn} regiAdmnName={detail.regiAdmnName} regiDate={detail.regiDate} updtAdmn={detail.updtAdmn} updtAdmnName={detail.updtAdmnName} updtDate={detail.updtDate} />}
      <div className="detail-footer"><div className="detail-footer-left"><button type="button" className="subtle-button" onClick={() => onMovePath(ALIM_ICON_LIST_PATH)}>목록</button></div><div className="detail-footer-right">{permission.writYsno === 'Y' && <button type="button" disabled={saving || !file} onClick={() => document.querySelector<HTMLFormElement>('.alim-icon-detail-page form')?.requestSubmit()}>{saving ? '저장 중' : detail.iconRegiYsno === 'Y' ? '교체' : '등록'}</button>}</div></div>
    </section>
  )
}
