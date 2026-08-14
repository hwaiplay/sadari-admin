import { useEffect, useState } from 'react'
import { getInquiry, releaseInquirySuspension, setInquiryAnswer, startInquiryReview } from '../../api/inquiryApi'
import { INQUIRY_LIST_PATH } from '../../constants/routes'
import { useMenuPermission } from '../../contexts/useMenuPermission'
import type { Inquiry } from '../../types/inquiry'
import { formatDate } from '../../utils/code'

type Props = { inqrNumb: number; onMovePath: (path: string) => void; onError: (message: string | null) => void }

/** 고객문의 접수 내용과 답변 및 연결 이용정지 해제 기능을 제공한다 */
export function InquiryDetailPage({ inqrNumb, onMovePath, onError }: Props) {
  const permission = useMenuPermission()
  const [inquiry, setInquiry] = useState<Inquiry | null>(null)
  const [answer, setAnswer] = useState('')
  const [releaseMemo, setReleaseMemo] = useState('')
  const [saving, setSaving] = useState(false)

  const refresh = async () => setInquiry(await getInquiry(inqrNumb))
  useEffect(() => { void refresh().then(() => onError(null)).catch((error: unknown) => onError(error instanceof Error ? error.message : '고객문의 상세를 불러오지 못했습니다.')) }, [inqrNumb])

  const startReview = async () => {
    if (!inquiry || !window.confirm('이 고객문의의 검토를 시작하시겠습니까?')) return
    setSaving(true); try { setInquiry(await startInquiryReview(inqrNumb, inquiry.updtDate)); onError(null) } catch (error: unknown) { onError(error instanceof Error ? error.message : '검토를 시작하지 못했습니다.') } finally { setSaving(false) }
  }
  const saveAnswer = async () => {
    if (!inquiry || !answer.trim()) { onError('답변 내용을 입력해 주세요.'); return }
    if (!window.confirm('이 답변을 사용자에게 등록하시겠습니까?')) return
    setSaving(true); try { setInquiry(await setInquiryAnswer(inqrNumb, answer.trim(), inquiry.updtDate)); setAnswer(''); onError(null) } catch (error: unknown) { onError(error instanceof Error ? error.message : '답변을 등록하지 못했습니다.') } finally { setSaving(false) }
  }
  const release = async () => {
    if (!window.confirm('이 문의에 연결된 이용정지를 해제하시겠습니까?')) return
    setSaving(true); try { await releaseInquirySuspension(inqrNumb, releaseMemo.trim()); await refresh(); onError(null) } catch (error: unknown) { onError(error instanceof Error ? error.message : '이용정지를 해제하지 못했습니다.') } finally { setSaving(false) }
  }

  if (!inquiry) return <section className="empty">고객문의 상세를 불러오고 있습니다.</section>
  return <section className="complaint-page complaint-detail-page">
    <section className="content-header"><h2>고객문의 #{inquiry.inqrNumb}</h2><span className="complaint-status">{inquiry.inqrStatName}</span></section>
    <section className="detail-panel"><div className="detail-title"><h3>문의 정보</h3></div><section className="table-wrap menu-info-table complaint-info-table"><table><tbody>
      <tr><th>사용자</th><td>{inquiry.userNick ?? '-'} ({inquiry.userNumb ?? '-'})</td><th>회원상태</th><td>{inquiry.userStatName ?? inquiry.userStat ?? '-'}</td></tr>
      <tr><th>카테고리</th><td>{inquiry.inqrCatgName}</td><th>담당 관리자</th><td>{inquiry.asgnAdmnName ?? '-'}</td></tr>
      <tr><th>제목</th><td colSpan={3}>{inquiry.inqrTitl}</td></tr><tr><th>문의내용</th><td colSpan={3} className="complaint-content-cell">{inquiry.inqrCntn}</td></tr>
      <tr><th>접수일시</th><td>{formatDate(inquiry.regiDate)}</td><th>답변일시</th><td>{formatDate(inquiry.answDate) || '-'}</td></tr>
    </tbody></table></section></section>
    {inquiry.spndNumb && <section className="detail-panel"><div className="detail-title"><h3>연결 이용정지 #{inquiry.spndNumb}</h3></div><section className="table-wrap menu-info-table"><table><tbody><tr><th>정지 상태</th><td>{inquiry.spndStatName ?? inquiry.spndStat ?? '-'}</td><th>정지 사유</th><td>{inquiry.spndRsonName ?? '-'}</td></tr><tr><th>시작</th><td>{formatDate(inquiry.spndStrtDate)}</td><th>종료</th><td>{formatDate(inquiry.spndEndxDate) || '무기한'}</td></tr></tbody></table></section>{inquiry.spndStat === 'ACTIVE' && permission.writYsno === 'Y' && <div className="complaint-process-actions"><input value={releaseMemo} placeholder="해제 메모" onChange={event => setReleaseMemo(event.target.value)} /><button type="button" disabled={saving} onClick={() => void release()}>이용정지 해제</button></div>}</section>}
    <section className="detail-panel"><div className="detail-title"><h3>답변</h3></div>{inquiry.answers?.length ? inquiry.answers.map(item => <section key={item.answNumb} className="table-wrap menu-info-table complaint-info-table"><table><tbody>
      <tr><th>답변 번호</th><td>{item.answNumb}</td><th>등록 관리자</th><td>{item.regiAdmnName}</td></tr>
      <tr><th>등록일시</th><td>{formatDate(item.regiDate)}</td><th>사용자 확인</th><td>{item.readYsno === 'Y' ? '읽음' : '읽지 않음'}</td></tr>
      <tr><th>답변 내용</th><td colSpan={3} className="complaint-content-cell">{item.answCntn}</td></tr>
    </tbody></table></section>) : <section className="table-wrap menu-info-table complaint-info-table"><table><tbody><tr><td colSpan={4} className="empty small">등록된 답변이 없습니다.</td></tr></tbody></table></section>}
      {permission.writYsno === 'Y' && inquiry.inqrStat === 'INQR_RECEIVED' && <div className="complaint-process-actions"><button type="button" disabled={saving} onClick={() => void startReview()}>검토 시작</button></div>}
      {permission.writYsno === 'Y' && inquiry.inqrStat === 'INQR_REVIEWING' && <><label className="complaint-process-note">답변 내용<textarea value={answer} maxLength={4000} onChange={event => setAnswer(event.target.value)} /></label><div className="complaint-process-actions"><button type="button" disabled={saving} onClick={() => void saveAnswer()}>답변 등록</button></div></>}
    </section>
    <div className="detail-footer"><button type="button" className="subtle-button" onClick={() => onMovePath(INQUIRY_LIST_PATH)}>목록</button></div>
  </section>
}
