import { useCallback, useEffect, useMemo, useState } from 'react'
import type { FormEvent } from 'react'
import { getCodeList } from '../../api/codeApi'
import {
  createServiceInfo,
  deleteServiceInfo,
  deployServiceInfo,
  getServiceInfoDetail,
  getServiceInfoList,
  getServiceInfoVersions,
  updateServiceInfoVersion,
  uploadServiceInfoImage,
} from '../../api/serviceInfoApi'
import { AuditInfoTable } from '../../components/AuditInfoTable'
import { Pagination } from '../../components/Pagination'
import { SummernoteEditor } from '../../components/SummernoteEditor'
import { SVIF_CATE } from '../../constants/codes'
import {
  SERVICE_INFO_DETAIL_PREFIX,
  SERVICE_INFO_LIST_PATH,
  SERVICE_INFO_NEW_PATH,
} from '../../constants/routes'
import type { Code } from '../../types/code'
import type { PageData } from '../../types/common'
import type { ServiceInfo, ServiceInfoForm } from '../../types/serviceInfo'
import { formatDate } from '../../utils/code'

type ServiceInfoManagePageProps = {
  currentPath: string
  onMovePath: (path: string) => void
  onError: (message: string | null) => void
}

const EMPTY_PAGE: PageData<ServiceInfo> = {
  items: [], totalCount: 0, pageNumber: 1, pageSize: 20, totalPages: 0,
}

const EMPTY_FORM: ServiceInfoForm = { cateCode: '', svciTitl: '', svciCntn: '' }

/** 카테고리별 단일 서비스 정보의 목록과 버전 및 배포 화면을 관리한다. */
export function ServiceInfoManagePage({ currentPath, onMovePath, onError }: ServiceInfoManagePageProps) {
  const [pageData, setPageData] = useState<PageData<ServiceInfo>>(EMPTY_PAGE)
  const [keyword, setKeyword] = useState('')
  const [searchCategoryCode, setSearchCategoryCode] = useState('')
  const [appliedKeyword, setAppliedKeyword] = useState('')
  const [appliedSearchCategoryCode, setAppliedSearchCategoryCode] = useState('')
  const [detail, setDetail] = useState<ServiceInfo | null>(null)
  const [versions, setVersions] = useState<ServiceInfo[]>([])
  const [form, setForm] = useState<ServiceInfoForm>(EMPTY_FORM)
  const [categoryCodes, setCategoryCodes] = useState<Code[]>([])
  const [loading, setLoading] = useState(false)
  const [saving, setSaving] = useState(false)
  const [deleting, setDeleting] = useState(false)

  const detailKey = useMemo(() => {
    // 서비스 정보 상세 경로가 아니면 복합키를 반환하지 않는다.
    if (!currentPath.startsWith(`${SERVICE_INFO_DETAIL_PREFIX}/`)) {
      return null
    }

    const [cateCode, versNumbText] = currentPath.slice(SERVICE_INFO_DETAIL_PREFIX.length + 1).split('/')
    const versNumb = Number(versNumbText)
    // 카테고리와 양의 버전이 모두 있을 때만 상세키를 반환한다.
    return cateCode && Number.isInteger(versNumb) && versNumb > 0
      ? { cateCode: decodeURIComponent(cateCode), versNumb }
      : null
  }, [currentPath])
  const isListPage = currentPath === SERVICE_INFO_LIST_PATH
  const isNewPage = currentPath === SERVICE_INFO_NEW_PATH

  /** 서비스 정보 목록과 카테고리 공통코드를 조회한다. */
  const loadList = useCallback(async (
    page: number,
    searchKeyword: string,
    categoryCode: string,
  ): Promise<void> => {
    setLoading(true)
    onError(null)
    // 검색 조건과 카테고리를 함께 갱신한다.
    try {
      const [result, codes] = await Promise.all([
        getServiceInfoList(page, searchKeyword, categoryCode),
        getCodeList(SVIF_CATE),
      ])
      setPageData(result)
      setCategoryCodes(codes)
    } catch (error: unknown) {
      onError(error instanceof Error ? error.message : '서비스 정보 목록 조회에 실패했습니다.')
    } finally {
      setLoading(false)
    }
  }, [onError])

  /** 현재 경로의 서비스 정보 목록 또는 상세를 조회한다. */
  useEffect(() => {
    const timer = window.setTimeout(() => {
      // 목록 경로에서는 초기 조건으로 목록을 조회한다.
      if (isListPage) {
        void loadList(1, '', '')
        return
      }

      // 등록 화면에서는 카테고리와 빈 입력값을 준비한다.
      if (isNewPage) {
        setDetail(null)
        setVersions([])
        setForm(EMPTY_FORM)
        void getCodeList(SVIF_CATE).then(setCategoryCodes).catch((error: unknown) => {
          onError(error instanceof Error ? error.message : '서비스 정보 카테고리 조회에 실패했습니다.')
        })
        return
      }

      // 상세키가 있으면 선택 버전과 전체 버전 이력을 함께 조회한다.
      if (detailKey) {
        setLoading(true)
        onError(null)
        void Promise.all([
          getServiceInfoDetail(detailKey.cateCode, detailKey.versNumb),
          getServiceInfoVersions(detailKey.cateCode),
          getCodeList(SVIF_CATE),
        ]).then(([result, versionList, codes]) => {
          setDetail(result)
          setVersions(versionList)
          setCategoryCodes(codes)
          setForm({ cateCode: result.cateCode, svciTitl: result.svciTitl, svciCntn: result.svciCntn })
        }).catch((error: unknown) => {
          onError(error instanceof Error ? error.message : '서비스 정보 상세 조회에 실패했습니다.')
        }).finally(() => setLoading(false))
      }
    }, 0)
    return () => window.clearTimeout(timer)
  }, [currentPath, detailKey, isListPage, isNewPage, loadList, onError])

  /** 서비스 정보 검색 폼을 제출한다. */
  const handleSearch = (event: FormEvent<HTMLFormElement>): void => {
    event.preventDefault()
    setAppliedKeyword(keyword)
    setAppliedSearchCategoryCode(searchCategoryCode)
    void loadList(1, keyword, searchCategoryCode)
  }

  /** 서비스 정보 검색 조건과 결과를 전체 목록으로 초기화한다. */
  const handleSearchReset = (): void => {
    setKeyword('')
    setSearchCategoryCode('')
    setAppliedKeyword('')
    setAppliedSearchCategoryCode('')
    void loadList(1, '', '')
  }

  /** 서비스 정보 초안을 저장하고 서버가 결정한 버전 상세로 이동한다. */
  const save = async (): Promise<void> => {
    // 필수 입력값이 없으면 API 요청을 실행하지 않는다.
    if (!form.cateCode || !form.svciTitl.trim() || !form.svciCntn.trim()) {
      alert('카테고리와 제목 및 내용을 모두 입력해 주세요.')
      return
    }

    setSaving(true)
    onError(null)
    // 신규 등록과 버전 저장을 현재 경로에 따라 구분한다.
    try {
      const result = detailKey
        ? await updateServiceInfoVersion(detailKey.cateCode, detailKey.versNumb, form)
        : await createServiceInfo(form)
      alert(result.message)
      onMovePath(`${SERVICE_INFO_DETAIL_PREFIX}/${encodeURIComponent(result.data.cateCode)}/${result.data.versNumb}`)
    } catch (error: unknown) {
      onError(error instanceof Error ? error.message : '서비스 정보 저장에 실패했습니다.')
    } finally {
      setSaving(false)
    }
  }

  /** 선택한 서비스 정보 버전을 사용자 배포본으로 전환한다. */
  const deploy = async (): Promise<void> => {
    // 상세 화면에서만 배포를 실행한다.
    if (!detailKey) {
      return
    }

    setSaving(true)
    // 선택 버전을 배포한 뒤 같은 상세를 다시 표시한다.
    try {
      const result = await deployServiceInfo(detailKey.cateCode, detailKey.versNumb)
      alert(result.message)
      onMovePath(`${SERVICE_INFO_DETAIL_PREFIX}/${encodeURIComponent(result.data.cateCode)}/${result.data.versNumb}`)
    } catch (error: unknown) {
      onError(error instanceof Error ? error.message : '서비스 정보 배포에 실패했습니다.')
    } finally {
      setSaving(false)
    }
  }

  /** 현재 카테고리의 모든 서비스 정보 버전을 삭제한다. */
  const remove = async (): Promise<void> => {
    // 상세 카테고리가 없으면 삭제를 실행하지 않는다.
    if (!detailKey || !confirm('이 카테고리의 모든 버전을 삭제하시겠습니까?')) {
      return
    }

    setDeleting(true)
    // 전체 버전 삭제 후 목록으로 이동한다.
    try {
      const result = await deleteServiceInfo(detailKey.cateCode)
      alert(result.message)
      onMovePath(SERVICE_INFO_LIST_PATH)
    } catch (error: unknown) {
      onError(error instanceof Error ? error.message : '서비스 정보 삭제에 실패했습니다.')
    } finally {
      setDeleting(false)
    }
  }

  // 서비스 정보 목록 경로에서는 카테고리별 한 행만 표시한다.
  if (isListPage) {
    return (
      /* 서비스 정보 검색과 목록 전체 영역 */
      <section className="notice-page">
        {/* 서비스 정보 목록 제목과 전체 건수 영역 */}
        <section className="content-header">
          {/* "서비스 정보 관리" */}
          <h1>서비스 정보 관리</h1>
          <div className="status">총 {pageData.totalCount}건</div>
        </section>

        {/* 서비스 정보 제목과 카테고리 검색 조건 영역 */}
        <form className="list-search" onSubmit={handleSearch}>
          <label>
            <span>검색어</span>
            <input
              value={keyword}
              placeholder="서비스 정보 제목"
              onChange={(event) => setKeyword(event.target.value)}
            />
          </label>
          <label>
            <span>카테고리</span>
            <select
              value={searchCategoryCode}
              onChange={(event) => setSearchCategoryCode(event.target.value)}
            >
              <option value="">전체</option>
              {categoryCodes.map((code) => (
                <option key={code.comdCode} value={code.comdCode}>{code.comdName}</option>
              ))}
            </select>
          </label>
          {/* 서비스 정보 검색 실행과 초기화 버튼 영역 */}
          <div className="list-search-actions">
            <button type="button" className="subtle-button" onClick={handleSearchReset}>초기화</button>
            <button type="submit">검색</button>
          </div>
        </form>
        <section className="table-wrap notice-list-table">
          <table><thead><tr><th>카테고리</th><th>제목</th><th>버전</th><th>배포</th><th>수정자</th><th>수정일</th></tr></thead>
            <tbody>{pageData.items.length === 0 ? <tr className="empty-row"><td colSpan={6}>{loading ? '조회 중입니다.' : '등록된 서비스 정보가 없습니다.'}</td></tr> : pageData.items.map((item) => (
              <tr key={item.cateCode} onClick={() => onMovePath(`${SERVICE_INFO_DETAIL_PREFIX}/${encodeURIComponent(item.cateCode)}/${item.versNumb}`)}><td>{item.cateName}</td><td>{item.svciTitl}</td><td>{item.versNumb}</td><td><span className={`notice-deploy ${item.dplyYsno === 'Y' ? 'active' : ''}`}>{item.dplyYsno === 'Y' ? '배포 중' : '미배포'}</span></td><td>{item.updtAdmnName ?? item.regiAdmnName ?? '-'}</td><td>{formatDate(item.updtDate ?? item.regiDate)}</td></tr>
            ))}</tbody></table>
        </section>
        <Pagination
          pageNumber={pageData.pageNumber}
          totalPages={pageData.totalPages}
          onPageChange={(page) => void loadList(page, appliedKeyword, appliedSearchCategoryCode)}
        />
        <button type="button" className="floating-button" onClick={() => onMovePath(SERVICE_INFO_NEW_PATH)}>등록</button>
      </section>
    )
  }

  // 서비스 정보 등록 또는 버전 상세 편집 화면을 반환한다.
  return (
    /* 서비스 정보 상세 편집 전체 영역 */
    <section className="notice-page">
      <section className="content-header"><h1>{isNewPage ? '서비스 정보 등록' : '서비스 정보 상세'}</h1></section>
      <section className="detail-panel">
        <section className="table-wrap notice-detail-table"><table><tbody><tr><th>카테고리</th><td><select value={form.cateCode} disabled={!isNewPage} onChange={(event) => setForm({ ...form, cateCode: event.target.value })}><option value="">선택</option>{categoryCodes.map((code) => <option key={code.comdCode} value={code.comdCode}>{code.comdName}</option>)}</select></td><th>버전</th><td>{detail ? detail.versNumb : '신규'}</td><th>배포 상태</th><td>{detail?.dplyYsno === 'Y' ? '배포 중' : '미배포'}</td></tr><tr><th>제목</th><td colSpan={5}><input value={form.svciTitl} onChange={(event) => setForm({ ...form, svciTitl: event.target.value })} /></td></tr></tbody></table></section>
        <div className="notice-form"><label><span>내용</span><SummernoteEditor value={form.svciCntn} disabled={saving} placeholder="서비스 정보 내용을 입력해 주세요." uploadImage={uploadServiceInfoImage} uploadErrorMessage="서비스 정보 이미지 업로드에 실패했습니다." onChange={(svciCntn) => setForm((current) => ({ ...current, svciCntn }))} onError={onError} /></label></div>
        {!isNewPage && versions.length > 0 && <section className="notice-version-section"><div className="detail-title"><div><h2>버전 이력</h2><p>카테고리에는 하나의 글만 존재하며 수정 이력은 버전으로 관리됩니다.</p></div></div><section className="table-wrap"><table><thead><tr><th>버전</th><th>제목</th><th>배포</th><th>수정일</th></tr></thead><tbody>{versions.map((version) => <tr key={version.versNumb} className="notice-version-row" onClick={() => onMovePath(`${SERVICE_INFO_DETAIL_PREFIX}/${encodeURIComponent(version.cateCode)}/${version.versNumb}`)}><td>{version.versNumb}</td><td>{version.svciTitl}</td><td>{version.dplyYsno === 'Y' ? '배포 중' : '미배포'}</td><td>{formatDate(version.updtDate ?? version.regiDate)}</td></tr>)}</tbody></table></section></section>}
      </section>
      {!isNewPage && detail && <AuditInfoTable regiAdmn={detail.regiAdmn} regiAdmnName={detail.regiAdmnName} regiDate={detail.regiDate} updtAdmn={detail.updtAdmn} updtAdmnName={detail.updtAdmnName} updtDate={detail.updtDate} dplyAdmn={detail.dplyAdmn} dplyAdmnName={detail.dplyAdmnName} dplyDate={detail.dplyDate} showDeployInfo />}
      <div className="detail-footer"><div className="detail-footer-left"><button type="button" className="subtle-button" onClick={() => onMovePath(SERVICE_INFO_LIST_PATH)}>목록</button>{!isNewPage && <button type="button" className="delete-button" disabled={deleting} onClick={() => void remove()}>{deleting ? '삭제 중' : '삭제'}</button>}</div><div className="detail-footer-right">{!isNewPage && detail?.dplyYsno !== 'Y' && <button type="button" className="subtle-button" disabled={saving} onClick={() => void deploy()}>배포</button>}<button type="button" disabled={saving} onClick={() => void save()}>{saving ? '저장 중' : '저장'}</button></div></div>
    </section>
  )
}
