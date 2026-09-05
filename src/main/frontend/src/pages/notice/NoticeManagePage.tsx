import { useCallback, useEffect, useMemo, useState } from 'react'
import type { FormEvent } from 'react'
import { createNotice, deleteNotice, deployNotice, getNoticeDetail, getNoticeList, getNoticeVersions, updateNoticeVersion } from '../../api/noticeApi'
import { getCodeList } from '../../api/codeApi'
import { SummernoteEditor } from '../../components/SummernoteEditor'
import { Pagination } from '../../components/Pagination'
import { AuditInfoTable } from '../../components/AuditInfoTable'
import { NOTICE_DETAIL_PREFIX, NOTICE_LIST_PATH, NOTICE_NEW_PATH } from '../../constants/routes'
import { NOTI_CATE } from '../../constants/codes'
import type { Code } from '../../types/code'
import type { PageData } from '../../types/common'
import type { Notice, NoticeForm } from '../../types/notice'

type NoticeManagePageProps = {
  currentPath: string
  onMovePath: (path: string) => void
  onError: (message: string | null) => void
}

const EMPTY_PAGE: PageData<Notice> = {
  items: [], totalCount: 0, pageNumber: 1, pageSize: 20, totalPages: 0,
}

const EMPTY_FORM: NoticeForm = { cateCode: '', notiTitl: '', notiEntl: '', notiCntn: '', notiEnct: '', topxYsno: 'N' }

/** 상단 고정 공지에 사용하는 핀 아이콘을 표시한다. */
function PinIcon() {
  return (
    <svg width="19" height="19" viewBox="0 0 19 19" fill="none" xmlns="http://www.w3.org/2000/svg">
      <path d="M11 2L17 8L15 10L12.5 8.5L9.5 11.5L10 15L8 17L2 11L4 9L7.5 9.5L10.5 6.5L9 4L11 2Z" fill="#2F8F64"/>
      <path d="M4.39415 14.0967L2 16.4908" stroke="#2F8F64" stroke-linecap="square"/>
    </svg>
  )
}

/** 공지사항 목록과 불변 버전 작성 및 배포 화면을 관리한다. */
export function NoticeManagePage({ currentPath, onMovePath, onError }: NoticeManagePageProps) {
  const [pageData, setPageData] = useState<PageData<Notice>>(EMPTY_PAGE)
  const [keyword, setKeyword] = useState('')
  const [searchCategoryCode, setSearchCategoryCode] = useState('')
  const [detail, setDetail] = useState<Notice | null>(null)
  const [versions, setVersions] = useState<Notice[]>([])
  const [form, setForm] = useState<NoticeForm>(EMPTY_FORM)
  const [categoryCodes, setCategoryCodes] = useState<Code[]>([])
  const [loading, setLoading] = useState(false)
  const [saving, setSaving] = useState(false)
  const [deleting, setDeleting] = useState(false)

  const detailKey = useMemo(() => {
    if (!currentPath.startsWith(`${NOTICE_DETAIL_PREFIX}/`)) return null
    const [notiNumb, versNumb] = currentPath.slice(NOTICE_DETAIL_PREFIX.length + 1).split('/').map(Number)
    return Number.isInteger(notiNumb) && notiNumb > 0 && Number.isInteger(versNumb) && versNumb > 0
      ? { notiNumb, versNumb }
      : null
  }, [currentPath])
  const isListPage = currentPath === NOTICE_LIST_PATH
  const isNewPage = currentPath === NOTICE_NEW_PATH

  /** 현재 배포 버전을 우선하는 공지 목록을 검색 조건으로 조회한다. */
  const loadList = useCallback(async (page = 1, searchKeyword = keyword, searchCateCode = searchCategoryCode): Promise<void> => {
    setLoading(true)
    onError(null)
    try {
      setPageData(await getNoticeList(page, searchKeyword.trim(), searchCateCode))
    } catch (error: unknown) {
      onError(error instanceof Error ? error.message : '공지사항 목록을 조회하지 못했습니다.')
    } finally {
      setLoading(false)
    }
  }, [keyword, onError, searchCategoryCode])

  /** 복합키에 해당하는 공지 버전을 조회한다. */
  const loadDetail = useCallback(async (notiNumb: number, versNumb: number): Promise<void> => {
    setLoading(true)
    onError(null)
    try {
      const [found, foundVersions] = await Promise.all([
        getNoticeDetail(notiNumb, versNumb),
        getNoticeVersions(notiNumb),
      ])
      setDetail(found)
      setVersions(foundVersions)
      setForm({
        cateCode: found.cateCode,
        notiTitl: found.notiTitl,
        notiEntl: found.notiEntl,
        notiCntn: found.notiCntn,
        notiEnct: found.notiEnct,
        topxYsno: found.topxYsno,
      })
    } catch (error: unknown) {
      onError(error instanceof Error ? error.message : '공지사항 상세를 조회하지 못했습니다.')
    } finally {
      setLoading(false)
    }
  }, [onError])

  /** 공지사항 등록과 상세에 사용할 카테고리 공통코드를 조회한다. */
  const loadCategoryCodes = useCallback(async (): Promise<void> => {
    try {
      const codes = await getCodeList(NOTI_CATE)
      setCategoryCodes(codes)
      setForm((current) => current.cateCode ? current : { ...current, cateCode: codes[0]?.comdCode ?? '' })
    } catch (error: unknown) {
      onError(error instanceof Error ? error.message : '공지사항 카테고리를 조회하지 못했습니다.')
    }
  }, [onError])

  useEffect(() => {
    const timer = window.setTimeout(() => {
      void loadCategoryCodes()
    }, 0)
    return () => window.clearTimeout(timer)
  }, [loadCategoryCodes])

  useEffect(() => {
    const timer = window.setTimeout(() => {
      if (isListPage) {
        void loadList(1, '', '')
      } else if (isNewPage) {
        setDetail(null)
        setVersions([])
        setForm({ ...EMPTY_FORM, cateCode: categoryCodes[0]?.comdCode ?? '' })
      } else if (detailKey) {
        void loadDetail(detailKey.notiNumb, detailKey.versNumb)
      }
    }, 0)
    return () => window.clearTimeout(timer)
  }, [categoryCodes, detailKey, isListPage, isNewPage, loadDetail, loadList])

  /**
   * 미배포 버전은 같은 버전으로 수정하고 현재 배포 중인 버전은 다음 버전으로 저장한다
   *
   * @author SeungHyeon.Kang
   * @param event 공지사항 저장 폼 제출 이벤트
   * @return 저장 처리가 끝나면 완료되는 Promise
   * @throws 공지사항 저장 API 요청이 실패할 때 발생한다
   */
  const saveNotice = async (event: FormEvent<HTMLFormElement>): Promise<void> => {
    // 브라우저 기본 제출을 막아 현재 관리 화면에서 API 저장을 처리한다.
    event.preventDefault()
    // 필수 입력값이 없으면 서버 요청 없이 관리자에게 입력 항목을 안내한다.
    if (!form.cateCode || !form.notiTitl.trim() || !form.notiEntl.trim()
        || !form.notiCntn.trim() || !form.notiEnct.trim()) {
      // "공지사항 카테고리와 제목 및 내용을 입력해 주세요."
      onError('공지사항 카테고리와 한글·영문 제목 및 내용을 입력해 주세요.')
      // 유효하지 않은 입력값으로 저장 API가 호출되지 않도록 종료한다.
      return
    }

    // 중복 저장을 막기 위해 저장 진행 상태를 활성화한다.
    setSaving(true)
    // 이전 요청의 오류가 현재 저장 결과와 함께 표시되지 않도록 초기화한다.
    onError(null)
    // 저장 결과에 따라 상세를 갱신하고 실패 여부를 화면 상태에 반영한다.
    try {
      let result: Awaited<ReturnType<typeof createNotice>>
      // 기존 공지 상세에서는 서버가 현재 배포 상태를 판정하도록 기준 복합키를 전달한다.
      if (detail) {
        // 선택한 공지 버전의 수정 또는 다음 버전 생성을 요청한다.
        result = await updateNoticeVersion(detail.notiNumb, detail.versNumb, form)
      }

      // 신규 등록 화면에서는 최초 버전 생성을 요청한다.
      else {
        // 입력한 공지사항의 최초 버전 생성을 요청한다.
        result = await createNotice(form)
      }

      // "저장했습니다."
      window.alert(result.message)
      const resultPath = `${NOTICE_DETAIL_PREFIX}/${result.data.notiNumb}/${result.data.versNumb}`
      // 미배포 초안 저장은 경로가 유지되므로 상세와 버전 이력을 직접 다시 조회한다.
      if (currentPath === resultPath) {
        // 같은 경로의 수정 관리 정보와 버전 목록을 최신 저장 결과로 갱신한다.
        await loadDetail(result.data.notiNumb, result.data.versNumb)
      }

      // 배포 중인 버전에서 새 버전이 생성되면 생성된 버전 상세로 이동한다.
      else {
        // 새로 생성된 버전 번호가 포함된 관리자 상세 경로로 이동한다.
        onMovePath(resultPath)
      }

    }

    // 저장 실패 원인을 관리자 오류 영역에 표시한다.
    catch (error: unknown) {
      let errorMessage = '공지사항을 저장하지 못했습니다.'
      // 표준 오류 객체이면 서버 또는 API 계층이 제공한 원인을 사용한다.
      if (error instanceof Error) {
        errorMessage = error.message
      }

      // "공지사항을 저장하지 못했습니다."
      onError(errorMessage)
    }

    // 저장 성공 여부와 무관하게 다시 저장할 수 있도록 진행 상태를 해제한다.
    finally {
      // 공지사항 저장 버튼의 진행 상태를 해제한다.
      setSaving(false)
    }
  }

  /** 현재 상세 버전을 사용자 화면 배포본으로 전환한다. */
  const deployCurrentNotice = async (): Promise<void> => {
    if (!detail || !window.confirm(`공지 ${detail.notiNumb}의 ${detail.versNumb}버전을 배포할까요?`)) return
    setSaving(true)
    onError(null)
    try {
      const result = await deployNotice(detail.notiNumb, detail.versNumb)
      window.alert(result.message)
      await loadDetail(detail.notiNumb, detail.versNumb)
    } catch (error: unknown) {
      onError(error instanceof Error ? error.message : '공지사항을 배포하지 못했습니다.')
    } finally {
      setSaving(false)
    }
  }

  /** 선택한 공지사항의 모든 버전과 읽음 이력 및 실제 파일을 삭제한다. */
  const deleteCurrentNotice = async (): Promise<void> => {
    // "공지 {공지번호}의 모든 버전과 파일을 삭제하시겠습니까?"
    if (!detail || !window.confirm(`공지 ${detail.notiNumb}의 모든 버전과 파일을 삭제하시겠습니까?`)) return
    setDeleting(true)
    onError(null)
    try {
      const result = await deleteNotice(detail.notiNumb)
      window.alert(result.message)
      onMovePath(NOTICE_LIST_PATH)
    } catch (error: unknown) {
      onError(error instanceof Error ? error.message : '공지사항을 삭제하지 못했습니다.')
    } finally {
      setDeleting(false)
    }
  }

  if (isListPage) {
    return (
      <section className="notice-page">
        <div className="content-header">
          <div><h1>공지사항</h1><p>공지별 현재 배포본을 우선하고 배포 전 공지는 최신 초안으로 관리합니다.</p></div>
        </div>
        <form className="list-search" onSubmit={(event) => { event.preventDefault(); void loadList(1) }}>
          <label><span>제목 검색</span><input value={keyword} onChange={(event) => setKeyword(event.target.value)} /></label>
          <label>
            <span>카테고리</span>
            <select value={searchCategoryCode} onChange={(event) => setSearchCategoryCode(event.target.value)}>
              <option value="">전체</option>
              {categoryCodes.map((code) => <option key={code.comdCode} value={code.comdCode}>{code.comdName}</option>)}
            </select>
          </label>
          <div className="list-search-actions"><button type="submit">검색</button></div>
        </form>
        <div className="table-wrap notice-list-table">
          <table>
            <thead>
              <tr><th>고정</th><th>카테고리</th><th>제목</th><th>공지번호</th><th>배포</th><th>등록자</th><th>등록일시</th></tr>
            </thead>
            <tbody>
              {pageData.items.length === 0 ? (
                <tr><td colSpan={7}>{loading ? '조회 중입니다.' : '등록된 공지사항이 없습니다.'}</td></tr>
              ) : pageData.items.map((notice) => (
                <tr className="notice-row" key={notice.notiNumb} tabIndex={0}
                    onClick={() => onMovePath(`${NOTICE_DETAIL_PREFIX}/${notice.notiNumb}/${notice.versNumb}`)}>
                  <td>{notice.topxYsno === 'Y' && <PinIcon />}</td><td>{notice.cateName}</td><td><span className="table-link-button">{notice.notiTitl}</span></td>
                  <td>{notice.notiNumb}</td>
                  <td><span className={`notice-deploy ${notice.dplyYsno === 'Y' ? 'active' : ''}`}>{notice.dplyYsno === 'Y' ? '배포 중' : '미배포'}</span></td>
                  <td>{notice.regiAdmnName ?? notice.regiAdmn}</td><td>{notice.regiDate?.replace('T', ' ')}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        <Pagination pageNumber={pageData.pageNumber} totalPages={pageData.totalPages} onPageChange={(page) => void loadList(page)} />
        <button type="button" className="floating-button" onClick={() => onMovePath(NOTICE_NEW_PATH)}>등록</button>
      </section>
    )
  }

  if (!isNewPage && !detailKey) return null

  return (
    <section className="notice-page">
      <div className="content-header">
        <div><h1>{isNewPage ? '공지사항 등록' : '공지사항 상세'}</h1><p>저장한 내용은 새 버전이 되며 배포 전에는 사용자에게 보이지 않습니다.</p></div>
      </div>
      {loading ? <div className="detail-panel">조회 중입니다.</div> : (
        <form onSubmit={(event) => void saveNotice(event)}>
          {/* 공지사항 기본 정보와 본문 및 지난 버전 테두리 영역 */}
          <section className="detail-panel notice-form">
            {/* 공지사항 버전 기본 정보와 수정 입력 영역 */}
            <section className="table-wrap notice-detail-table">
              <table>
                <tbody>
                  {!isNewPage && detail && (
                    <tr>
                      <th>공지번호</th><td className="readonly-cell">{detail.notiNumb}</td>
                      <th>버전</th><td className="readonly-cell">{detail.versNumb}</td>
                      <th>상단 고정</th>
                      <td className="notice-top-fixed-cell">
                        <input className="notice-top-fixed-checkbox" type="checkbox" aria-label="상단 고정" checked={form.topxYsno === 'Y'} onChange={(event) => setForm({ ...form, topxYsno: event.target.checked ? 'Y' : 'N' })} />
                      </td>
                    </tr>
                  )}
                  {isNewPage ? (
                    <>
                      {/* 공지사항 등록 카테고리와 제목 및 상단 고정 입력 영역 */}
                      <tr>
                        <th>카테고리</th>
                        <td className="notice-category-cell">
                          <select value={form.cateCode} onChange={(event) => setForm({ ...form, cateCode: event.target.value })}>
                            {categoryCodes.map((code) => <option key={code.comdCode} value={code.comdCode}>{code.comdName}</option>)}
                          </select>
                        </td>
                        <th>제목</th>
                        <td><input maxLength={300} value={form.notiTitl} onChange={(event) => setForm({ ...form, notiTitl: event.target.value })} /></td>
                        <th>상단 고정</th>
                        <td className="notice-top-fixed-cell">
                          <input className="notice-top-fixed-checkbox" type="checkbox" aria-label="상단 고정" checked={form.topxYsno === 'Y'} onChange={(event) => setForm({ ...form, topxYsno: event.target.checked ? 'Y' : 'N' })} />
                        </td>
                      </tr>
                      <tr>
                        <th>영문 제목</th>
                        <td colSpan={5}><input maxLength={300} value={form.notiEntl} onChange={(event) => setForm({ ...form, notiEntl: event.target.value })} /></td>
                      </tr>
                    </>
                  ) : (
                    <>
                      {/* 공지사항 상세 카테고리와 제목 수정 영역 */}
                      <tr>
                        <th>카테고리</th>
                        <td colSpan={5}>
                          <select value={form.cateCode} onChange={(event) => setForm({ ...form, cateCode: event.target.value })}>
                            {categoryCodes.map((code) => <option key={code.comdCode} value={code.comdCode}>{code.comdName}</option>)}
                          </select>
                        </td>
                      </tr>
                      <tr>
                        <th>제목</th>
                        <td colSpan={5}><input maxLength={300} value={form.notiTitl} onChange={(event) => setForm({ ...form, notiTitl: event.target.value })} /></td>
                      </tr>
                      <tr>
                        <th>영문 제목</th>
                        <td colSpan={5}><input maxLength={300} value={form.notiEntl} onChange={(event) => setForm({ ...form, notiEntl: event.target.value })} /></td>
                      </tr>
                    </>
                  )}
                </tbody>
              </table>
            </section>
            <label><span>내용</span></label>
            <SummernoteEditor value={form.notiCntn} disabled={saving} onChange={(notiCntn) => setForm((current) => ({ ...current, notiCntn }))} onError={onError} />
            <label><span>영문 내용</span></label>
            <SummernoteEditor value={form.notiEnct} disabled={saving} onChange={(notiEnct) => setForm((current) => ({ ...current, notiEnct }))} onError={onError} />
            {!isNewPage && detail && (
              /* 선택 가능한 공지사항 지난 버전 목록 영역 */
              <section className="notice-version-section">
                <div className="detail-title"><div><h2>지난 버전</h2><p>버전을 선택하면 해당 내용을 확인하고 다시 배포할 수 있습니다.</p></div></div>
                <div className="table-wrap notice-version-table">
                  <table>
                    <thead><tr><th>버전</th><th>카테고리</th><th>제목</th><th>상단 고정</th><th>배포 상태</th><th>수정자</th><th>수정일</th></tr></thead>
                    <tbody>
                      {versions.length === 0 ? (
                        <tr className="empty-row"><td colSpan={7}>지난 버전이 없습니다.</td></tr>
                      ) : versions.map((version) => (
                        <tr className="notice-version-row" key={version.versNumb} tabIndex={0}
                            aria-current={version.versNumb === detail.versNumb}
                            onClick={() => onMovePath(`${NOTICE_DETAIL_PREFIX}/${version.notiNumb}/${version.versNumb}`)}>
                          <td><span className="table-link-button">{version.versNumb}</span></td><td>{version.cateName}</td><td>{version.notiTitl}</td>
                          <td>{version.topxYsno === 'Y' ? 'Y' : 'N'}</td><td>{version.dplyYsno === 'Y' ? '배포 중' : '미배포'}</td>
                          <td>{version.updtAdmnName ?? version.updtAdmn ?? version.regiAdmnName ?? version.regiAdmn}</td><td>{(version.updtDate ?? version.regiDate)?.replace('T', ' ')}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </section>
            )}
          </section>
          {!isNewPage && detail && (
            /* 선택한 공지사항 버전의 등록·수정·배포 관리 정보 영역 */
            <AuditInfoTable
              regiAdmn={detail.regiAdmn} regiAdmnName={detail.regiAdmnName} regiDate={detail.regiDate}
              updtAdmn={detail.updtAdmn} updtAdmnName={detail.updtAdmnName} updtDate={detail.updtDate}
              dplyAdmn={detail.dplyAdmn} dplyAdmnName={detail.dplyAdmnName} dplyDate={detail.dplyDate} showDeployInfo
            />
          )}
          <div className="detail-footer">
            <div className="detail-footer-left">
              <button type="button" className="subtle-button" onClick={() => onMovePath(NOTICE_LIST_PATH)}>목록</button>
              {!isNewPage && <button type="button" className="delete-button" disabled={saving || deleting} onClick={() => void deleteCurrentNotice()}>{deleting ? '삭제 중' : '삭제'}</button>}
            </div>
            <div className="detail-footer-right">
              {!isNewPage && <button type="button" disabled={saving || detail?.dplyYsno === 'Y'} onClick={() => void deployCurrentNotice()}>{detail?.dplyYsno === 'Y' ? '배포 중' : '배포'}</button>}
              <button type="submit" disabled={saving}>{saving ? '저장 중' : '저장'}</button>
            </div>
          </div>
        </form>
      )}
    </section>
  )
}
