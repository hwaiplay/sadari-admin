import { useCallback, useEffect, useMemo, useState } from 'react'
import type { ChangeEvent, FormEvent, KeyboardEvent } from 'react'
import {
  createWelcomePage,
  deleteWelcomePage,
  deployWelcomePage,
  getWelcomePageDetail,
  getWelcomePageList,
  getWelcomePageVersions,
  updateWelcomePageVersion,
  uploadWelcomePageImage,
} from '../../api/welcomePageApi'
import { AuditInfoTable } from '../../components/AuditInfoTable'
import {
  WELCOME_PAGE_DETAIL_PREFIX,
  WELCOME_PAGE_LIST_PATH,
  WELCOME_PAGE_NEW_PATH,
} from '../../constants/routes'
import type { WelcomePage, WelcomePageForm } from '../../types/welcomePage'
import './WelcomePageManagePage.css'

type WelcomePageManagePageProps = {
  currentPath: string
  onMovePath: (path: string) => void
  onError: (message: string | null) => void
}

type PreviewPage = Pick<WelcomePageForm, 'subxTitl' | 'mainTitl' | 'pageDesc' | 'imgeUrlx' | 'sortOrdr'> & {
  previewKey: string
}

const EMPTY_FORM: WelcomePageForm = {
  subxTitl: '', subxEntl: '', mainTitl: '', mainEntl: '', pageDesc: '', pageEnct: '',
  imgeUrlx: null, imgeEnur: null, sortOrdr: 1,
}

/** 관리자 웰컴페이지 문구와 이미지 및 사용자 화면 미리보기를 관리한다. */
export const WelcomePageManagePage = ({ currentPath, onMovePath, onError }: WelcomePageManagePageProps) => {
  const [pages, setPages] = useState<WelcomePage[]>([])
  const [detail, setDetail] = useState<WelcomePage | null>(null)
  const [versions, setVersions] = useState<WelcomePage[]>([])
  const [form, setForm] = useState<WelcomePageForm>(EMPTY_FORM)
  const [loading, setLoading] = useState(false)
  const [saving, setSaving] = useState(false)
  const [uploading, setUploading] = useState(false)
  const [previewOpen, setPreviewOpen] = useState(false)
  const [previewIndex, setPreviewIndex] = useState(0)

  const detailKey = useMemo(() => {
    if (!currentPath.startsWith(`${WELCOME_PAGE_DETAIL_PREFIX}/`)) return null
    const [wlcmNumb, versNumb] = currentPath.slice(WELCOME_PAGE_DETAIL_PREFIX.length + 1).split('/').map(Number)
    return Number.isInteger(wlcmNumb) && wlcmNumb > 0 && Number.isInteger(versNumb) && versNumb > 0
      ? { wlcmNumb, versNumb }
      : null
  }, [currentPath])
  const isListPage = currentPath === WELCOME_PAGE_LIST_PATH
  const isNewPage = currentPath === WELCOME_PAGE_NEW_PATH

  /** 배포본 우선 웰컴페이지 목록을 조회한다. */
  const loadList = useCallback(async (): Promise<void> => {
    setLoading(true)
    onError(null)
    try {
      setPages(await getWelcomePageList())
    } catch (error: unknown) {
      onError(error instanceof Error ? error.message : '웰컴페이지 목록을 조회하지 못했습니다.')
    } finally {
      setLoading(false)
    }
  }, [onError])

  /** 신규 페이지 입력 순서와 미리보기용 기존 페이지를 준비한다. */
  const loadNewPage = useCallback(async (): Promise<void> => {
    setLoading(true)
    onError(null)
    try {
      const foundPages = await getWelcomePageList()
      setPages(foundPages)
      setDetail(null)
      setVersions([])
      const nextOrder = Math.max(0, ...foundPages.map((page) => page.sortOrdr)) + 1
      setForm({ ...EMPTY_FORM, sortOrdr: nextOrder })
    } catch (error: unknown) {
      onError(error instanceof Error ? error.message : '웰컴페이지 정보를 조회하지 못했습니다.')
    } finally {
      setLoading(false)
    }
  }, [onError])

  /** 선택한 상세와 버전 및 미리보기용 페이지를 함께 조회한다. */
  const loadDetail = useCallback(async (wlcmNumb: number, versNumb: number): Promise<void> => {
    setLoading(true)
    onError(null)
    try {
      const [found, foundVersions, foundPages] = await Promise.all([
        getWelcomePageDetail(wlcmNumb, versNumb),
        getWelcomePageVersions(wlcmNumb),
        getWelcomePageList(),
      ])
      setDetail(found)
      setVersions(foundVersions)
      setPages(foundPages)
      setForm({
        subxTitl: found.subxTitl,
        subxEntl: found.subxEntl,
        mainTitl: found.mainTitl,
        mainEntl: found.mainEntl,
        pageDesc: found.pageDesc,
        pageEnct: found.pageEnct,
        imgeUrlx: found.imgeUrlx,
        imgeEnur: found.imgeEnur,
        sortOrdr: found.sortOrdr,
      })
    } catch (error: unknown) {
      onError(error instanceof Error ? error.message : '웰컴페이지 상세를 조회하지 못했습니다.')
    } finally {
      setLoading(false)
    }
  }, [onError])

  useEffect(() => {
    const timer = window.setTimeout(() => {
      if (isListPage) void loadList()
      else if (isNewPage) void loadNewPage()
      else if (detailKey) void loadDetail(detailKey.wlcmNumb, detailKey.versNumb)
    }, 0)
    return () => window.clearTimeout(timer)
  }, [detailKey, isListPage, isNewPage, loadDetail, loadList, loadNewPage])

  const previewPages = useMemo<PreviewPage[]>(() => {
    const currentKey = detail ? String(detail.wlcmNumb) : 'new'
    const existing = pages
      .filter((page) => !detail || page.wlcmNumb !== detail.wlcmNumb)
      .map((page) => ({
        previewKey: String(page.wlcmNumb),
        subxTitl: page.subxTitl,
        mainTitl: page.mainTitl,
        pageDesc: page.pageDesc,
        imgeUrlx: page.imgeUrlx,
        sortOrdr: page.sortOrdr,
      }))
    return [...existing, { previewKey: currentKey, ...form }]
      .sort((left, right) => left.sortOrdr - right.sortOrdr || left.previewKey.localeCompare(right.previewKey))
  }, [detail, form, pages])

  const activePreview = previewPages[previewIndex]

  /** 현재 편집 페이지 위치에서 사용자 화면 미리보기를 연다. */
  const openPreview = (): void => {
    const currentKey = detail ? String(detail.wlcmNumb) : 'new'
    setPreviewIndex(Math.max(0, previewPages.findIndex((page) => page.previewKey === currentKey)))
    setPreviewOpen(true)
  }

  /** 선택한 이미지 파일을 검증 저장하고 폼과 미리보기에 반영한다. */
  const uploadImage = async (event: ChangeEvent<HTMLInputElement>): Promise<void> => {
    const file = event.target.files?.[0]
    event.target.value = ''
    if (!file) return
    setUploading(true)
    onError(null)
    try {
      const imageUrl = await uploadWelcomePageImage(file)
      setForm((current) => ({ ...current, imgeUrlx: imageUrl, imgeEnur: current.imgeEnur ?? imageUrl }))
    } catch (error: unknown) {
      onError(error instanceof Error ? error.message : '웰컴페이지 이미지를 업로드하지 못했습니다.')
    } finally {
      setUploading(false)
    }
  }

  /** 신규 또는 기존 웰컴페이지 버전을 저장한다. */
  const savePage = async (event: FormEvent<HTMLFormElement>): Promise<void> => {
    event.preventDefault()
    if (!form.subxTitl.trim() || !form.subxEntl.trim() || !form.mainTitl.trim() || !form.mainEntl.trim()
        || !form.pageDesc.trim() || !form.pageEnct.trim() || !form.imgeUrlx || !form.imgeEnur || form.sortOrdr < 1) {
      onError('한글·영문 소제목, 제목, 설명, 이미지와 1 이상의 노출 순서를 입력해 주세요.')
      return
    }
    setSaving(true)
    onError(null)
    try {
      const result = detail
        ? await updateWelcomePageVersion(detail.wlcmNumb, detail.versNumb, form)
        : await createWelcomePage(form)
      window.alert(result.message)
      const resultPath = `${WELCOME_PAGE_DETAIL_PREFIX}/${result.data.wlcmNumb}/${result.data.versNumb}`
      if (currentPath === resultPath) await loadDetail(result.data.wlcmNumb, result.data.versNumb)
      else onMovePath(resultPath)
    } catch (error: unknown) {
      onError(error instanceof Error ? error.message : '웰컴페이지를 저장하지 못했습니다.')
    } finally {
      setSaving(false)
    }
  }

  /** 현재 버전을 사용자 웰컴 화면에 배포한다. */
  const deployCurrentPage = async (): Promise<void> => {
    if (!detail || !window.confirm(`${detail.mainTitl} ${detail.versNumb}버전을 배포할까요?`)) return
    setSaving(true)
    onError(null)
    try {
      const result = await deployWelcomePage(detail.wlcmNumb, detail.versNumb)
      window.alert(result.message)
      await loadDetail(detail.wlcmNumb, detail.versNumb)
    } catch (error: unknown) {
      onError(error instanceof Error ? error.message : '웰컴페이지를 배포하지 못했습니다.')
    } finally {
      setSaving(false)
    }
  }

  /** 현재 웰컴페이지의 모든 버전을 삭제한다. */
  const deleteCurrentPage = async (): Promise<void> => {
    if (!detail || !window.confirm(`${detail.mainTitl}의 모든 버전을 삭제할까요?`)) return
    setSaving(true)
    onError(null)
    try {
      const result = await deleteWelcomePage(detail.wlcmNumb)
      window.alert(result.message)
      onMovePath(WELCOME_PAGE_LIST_PATH)
    } catch (error: unknown) {
      onError(error instanceof Error ? error.message : '웰컴페이지를 삭제하지 못했습니다.')
    } finally {
      setSaving(false)
    }
  }

  const moveFromRow = (event: KeyboardEvent<HTMLTableRowElement>, path: string): void => {
    if (event.key !== 'Enter' && event.key !== ' ') return
    event.preventDefault()
    onMovePath(path)
  }

  if (isListPage) {
    return (
      <section className="notice-page">
        <div className="content-header">
          <div><h1>웰컴페이지 관리</h1><p>등록 페이지 뒤에는 닉네임과 관심분야 설정 페이지가 항상 고정됩니다.</p></div>
        </div>
        <div className="table-wrap notice-list-table">
          <table>
            <thead><tr><th>순서</th><th>제목</th><th>페이지번호</th><th>버전</th><th>배포</th><th>등록자</th><th>등록일시</th></tr></thead>
            <tbody>
              {pages.length === 0 ? (
                <tr><td colSpan={7}>{loading ? '조회 중입니다.' : '등록된 웰컴페이지가 없습니다.'}</td></tr>
              ) : pages.map((page) => {
                const path = `${WELCOME_PAGE_DETAIL_PREFIX}/${page.wlcmNumb}/${page.versNumb}`
                return (
                  <tr className="notice-row" key={page.wlcmNumb} tabIndex={0}
                      onClick={() => onMovePath(path)} onKeyDown={(event) => moveFromRow(event, path)}>
                    <td>{page.sortOrdr}</td><td><span className="table-link-button">{page.mainTitl}</span></td>
                    <td>{page.wlcmNumb}</td><td>{page.versNumb}</td>
                    <td><span className={`notice-deploy ${page.dplyYsno === 'Y' ? 'active' : ''}`}>{page.dplyYsno === 'Y' ? '배포 중' : '미배포'}</span></td>
                    <td>{page.regiAdmnName ?? page.regiAdmn}</td><td>{page.regiDate?.replace('T', ' ')}</td>
                  </tr>
                )
              })}
            </tbody>
          </table>
        </div>
        <button type="button" className="floating-button" onClick={() => onMovePath(WELCOME_PAGE_NEW_PATH)}>등록</button>
      </section>
    )
  }

  if (!isNewPage && !detailKey) return null

  return (
    <section className="notice-page">
      {/* 웰컴페이지 상세 제목과 고정 화면 정책 안내 영역 */}
      <div className="content-header">
        <div><h1>{isNewPage ? '웰컴페이지 추가' : '웰컴페이지 상세'}</h1><p>로고, 페이지 번호, 이전·다음 버튼은 사용자 화면에서 고정됩니다.</p></div>
      </div>
      {loading ? <div className="detail-panel">조회 중입니다.</div> : (
        <form onSubmit={(event) => void savePage(event)}>
          {/* 웰컴페이지 기본 정보와 콘텐츠 및 지난 버전 테두리 영역 */}
          <section className="detail-panel notice-form">
            {/* 웰컴페이지 식별정보와 노출 콘텐츠 입력 영역 */}
            <section className="table-wrap notice-detail-table welcome-detail-table">
              <table>
                <tbody>
                  {!isNewPage && detail && (
                    <tr>
                      <th>페이지번호</th><td className="readonly-cell">{detail.wlcmNumb}</td>
                      <th>버전</th><td className="readonly-cell">{detail.versNumb}</td>
                      <th>배포 상태</th><td className="readonly-cell">{detail.dplyYsno === 'Y' ? '배포 중' : '미배포'}</td>
                    </tr>
                  )}
                  <tr>
                    <th>노출 순서</th>
                    <td colSpan={5}><input type="number" min={1} max={9999} value={form.sortOrdr} onChange={(event) => setForm({ ...form, sortOrdr: Number(event.target.value) })} /></td>
                  </tr>
                  <tr>
                    <th>소제목</th>
                    <td colSpan={5}><input maxLength={200} value={form.subxTitl} placeholder="예: 표지에서 시작하는 책장" onChange={(event) => setForm({ ...form, subxTitl: event.target.value })} /></td>
                  </tr>
                  <tr>
                    <th>영문 소제목</th>
                    <td colSpan={5}><input maxLength={200} value={form.subxEntl} onChange={(event) => setForm({ ...form, subxEntl: event.target.value })} /></td>
                  </tr>
                  <tr>
                    <th>제목</th>
                    <td colSpan={5}><textarea maxLength={300} rows={3} value={form.mainTitl} placeholder={'예: 책 표지의 분위기를\n내 책장 색으로'} onChange={(event) => setForm({ ...form, mainTitl: event.target.value })} /></td>
                  </tr>
                  <tr>
                    <th>영문 제목</th>
                    <td colSpan={5}><textarea maxLength={300} rows={3} value={form.mainEntl} onChange={(event) => setForm({ ...form, mainEntl: event.target.value })} /></td>
                  </tr>
                  <tr>
                    <th>설명</th>
                    <td colSpan={5}><textarea maxLength={1000} rows={4} value={form.pageDesc} placeholder="페이지를 설명하는 문구를 입력해 주세요." onChange={(event) => setForm({ ...form, pageDesc: event.target.value })} /></td>
                  </tr>
                  <tr>
                    <th>영문 설명</th>
                    <td colSpan={5}><textarea maxLength={1000} rows={4} value={form.pageEnct} onChange={(event) => setForm({ ...form, pageEnct: event.target.value })} /></td>
                  </tr>
                  <tr>
                    <th>이미지</th>
                    <td colSpan={5}>
                      {/* 웰컴페이지 이미지 업로드와 등록 상태 영역 */}
                      <div className="welcome-image-upload">
                        <input type="file" accept="image/jpeg,image/png" disabled={uploading || saving} onChange={(event) => void uploadImage(event)} />
                        <span>{uploading ? '업로드 중' : form.imgeUrlx ? '등록됨' : '미등록'}</span>
                      </div>
                      {form.imgeUrlx && (
                        /* 현재 등록한 웰컴페이지 이미지 확인과 제거 영역 */
                        <div className="welcome-image-current">
                          <img src={form.imgeUrlx} alt="현재 등록한 웰컴페이지 미리보기" />
                          <button type="button" className="subtle-button" disabled={uploading || saving} onClick={() => setForm({ ...form, imgeUrlx: null })}>이미지 제거</button>
                        </div>
                      )}
                    </td>
                  </tr>
                  <tr>
                    <th>영문 이미지 URL</th>
                    <td colSpan={5}><input maxLength={500} value={form.imgeEnur ?? ''} placeholder="같은 이미지를 사용하면 위 URL을 입력합니다." onChange={(event) => setForm({ ...form, imgeEnur: event.target.value || null })} /></td>
                  </tr>
                </tbody>
              </table>
            </section>
            {!isNewPage && detail && (
              /* 선택 가능한 웰컴페이지 지난 버전 목록 영역 */
              <section className="notice-version-section">
                <div className="detail-title"><div><h2>지난 버전</h2><p>선택한 버전을 확인하거나 다시 배포할 수 있습니다.</p></div></div>
                <div className="table-wrap notice-version-table"><table>
                  <thead><tr><th>버전</th><th>제목</th><th>순서</th><th>배포 상태</th><th>수정자</th><th>수정일</th></tr></thead>
                  <tbody>{versions.map((version) => {
                    const path = `${WELCOME_PAGE_DETAIL_PREFIX}/${version.wlcmNumb}/${version.versNumb}`
                    return (
                      <tr className="notice-version-row" key={version.versNumb} tabIndex={0} aria-current={version.versNumb === detail.versNumb}
                          onClick={() => onMovePath(path)} onKeyDown={(event) => moveFromRow(event, path)}>
                        <td>{version.versNumb}</td><td>{version.mainTitl}</td><td>{version.sortOrdr}</td><td>{version.dplyYsno === 'Y' ? '배포 중' : '미배포'}</td>
                        <td>{version.updtAdmnName ?? version.updtAdmn ?? version.regiAdmnName ?? version.regiAdmn}</td><td>{(version.updtDate ?? version.regiDate)?.replace('T', ' ')}</td>
                      </tr>
                    )
                  })}</tbody>
                </table></div>
              </section>
            )}
          </section>
          {/* 선택한 웰컴페이지 버전의 등록·수정·배포 관리 정보 영역 */}
          {!isNewPage && detail && <AuditInfoTable regiAdmn={detail.regiAdmn} regiAdmnName={detail.regiAdmnName} regiDate={detail.regiDate}
            updtAdmn={detail.updtAdmn} updtAdmnName={detail.updtAdmnName} updtDate={detail.updtDate}
            dplyAdmn={detail.dplyAdmn} dplyAdmnName={detail.dplyAdmnName} dplyDate={detail.dplyDate} showDeployInfo />}
          {/* 웰컴페이지 목록 이동과 삭제 및 미리보기·저장·배포 버튼 영역 */}
          <div className="detail-footer">
            <div className="detail-footer-left">
              <button type="button" className="subtle-button" onClick={() => onMovePath(WELCOME_PAGE_LIST_PATH)}>목록</button>
              {!isNewPage && detail && <button type="button" className="delete-button" disabled={saving} onClick={() => void deleteCurrentPage()}>삭제</button>}
            </div>
            <div className="detail-footer-right">
              {!isNewPage && detail && <button type="button" disabled={saving || detail.dplyYsno === 'Y'} onClick={() => void deployCurrentPage()}>{detail.dplyYsno === 'Y' ? '배포 중' : '배포'}</button>}
              <button type="button" className="subtle-button" disabled={loading} onClick={openPreview}>미리보기</button>
              <button type="submit" disabled={saving || uploading}>{saving ? '저장 중' : '저장'}</button>
            </div>
          </div>
        </form>
      )}

      {previewOpen && activePreview && (
        <div className="welcome-preview-overlay" role="dialog" aria-modal="true" aria-label="사용자 웰컴페이지 미리보기">
          <button type="button" className="welcome-preview-close" onClick={() => setPreviewOpen(false)}>미리보기 닫기</button>
          <main className="welcome-preview-screen">
            <header className="welcome-preview-header">
              <img src="/img/common/logo-upper.svg" alt="사다리 로고" />
              <p aria-live="polite">{previewIndex + 1} / {previewPages.length}</p>
            </header>
            <div className="welcome-preview-viewport">
              <section className="welcome-preview-slide">
                <div className="welcome-preview-copy">
                  <p className="welcome-preview-eyebrow">{activePreview.subxTitl || '소제목을 입력해 주세요.'}</p>
                  <h1>{activePreview.mainTitl || '제목을 입력해 주세요.'}</h1>
                  <p className="welcome-preview-description">{activePreview.pageDesc || '설명을 입력해 주세요.'}</p>
                </div>
                <div className="welcome-preview-image-wrap">
                  {activePreview.imgeUrlx && <img src={activePreview.imgeUrlx} alt="" />}
                </div>
              </section>
            </div>
            <footer className="welcome-preview-footer">
              <div className="welcome-preview-dots" role="group" aria-label="웰컴 화면 이동">
                {previewPages.map((page, index) => (
                  <button type="button" className={index === previewIndex ? 'active' : ''} aria-label={`${index + 1}번째 페이지`} aria-current={index === previewIndex ? 'step' : undefined} onClick={() => setPreviewIndex(index)} key={page.previewKey} />
                ))}
              </div>
              <div className="welcome-preview-navigation">
                <button type="button" disabled={previewIndex === 0} onClick={() => setPreviewIndex((index) => index - 1)}>이전</button>
                <button type="button" disabled={previewIndex === previewPages.length - 1} onClick={() => setPreviewIndex((index) => index + 1)}>다음</button>
              </div>
            </footer>
          </main>
        </div>
      )}
    </section>
  )
}
