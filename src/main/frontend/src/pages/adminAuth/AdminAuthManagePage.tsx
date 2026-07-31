import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { getAdminAuthManage, updateAdminAuths } from '../../api/adminAuthManageApi'
import { useMenuPermission } from '../../contexts/useMenuPermission'
import type { AdminAuth, AdminAuthGroup, AdminAuthSearch } from '../../types/adminAuth'
import type { PageData } from '../../types/common'
import { Pagination } from '../../components/Pagination'

type AdminAuthManagePageProps = {
  onError: (message: string | null) => void
}

const DEFAULT_SEARCH: AdminAuthSearch = {
  keyword: '',
  deptCode: '',
  authCode: '',
}

/** 관리자 권한 부여 화면 */
export function AdminAuthManagePage({ onError }: AdminAuthManagePageProps) {
  const permission = useMenuPermission()
  const [admins, setAdmins] = useState<AdminAuth[]>([])
  const [pageData, setPageData] = useState<PageData<AdminAuth>>({ items: [], totalCount: 0, pageNumber: 1, pageSize: 20, totalPages: 0 })
  const [authGroups, setAuthGroups] = useState<AdminAuthGroup[]>([])
  const [saving, setSaving] = useState(false)
  const [search, setSearch] = useState<AdminAuthSearch>(DEFAULT_SEARCH)
  const [appliedSearch, setAppliedSearch] = useState<AdminAuthSearch>(DEFAULT_SEARCH)

  /** 관리자와 권한그룹 목록 조회 */
  useEffect(() => {
    onError(null)
    getAdminAuthManage(1, DEFAULT_SEARCH)
      .then((result) => {
        setPageData(result.admins)
        setAdmins(result.admins.items)
        setAuthGroups(result.authGroups.filter((group) => group.useeYsno === 'Y'))
      })
      .catch((error: unknown) => onError(error instanceof Error ? error.message : '관리자 권한 정보를 불러오지 못했습니다.'))
  }, [onError])

  /**
   * 지정한 검색 조건과 페이지로 관리자 권한 목록을 조회한다
   *
   * @author SeungHyeon.Kang
   * @param pageNumber 조회할 페이지 번호
   * @param targetSearch 적용할 관리자 권한 검색 조건
   * @return 반환값이 없다
   */
  const loadListPage = async (pageNumber: number, targetSearch: AdminAuthSearch): Promise<void> => {
    const result = await getAdminAuthManage(pageNumber, targetSearch)
    setPageData(result.admins)
    setAdmins(result.admins.items)
    setAuthGroups(result.authGroups.filter((group) => group.useeYsno === 'Y'))
    setAppliedSearch(targetSearch)
  }

  /**
   * 입력한 관리자 권한 조건으로 첫 페이지를 검색한다
   *
   * @author SeungHyeon.Kang
   * @param event 관리자 권한 검색 폼 제출 이벤트
   * @return 반환값이 없다
   */
  const handleSearch = async (event: FormEvent<HTMLFormElement>): Promise<void> => {
    event.preventDefault()
    await loadListPage(1, search)
  }

  /**
   * 관리자 권한 검색 조건과 결과를 전체 목록으로 초기화한다
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없다
   */
  const handleSearchReset = async (): Promise<void> => {
    setSearch(DEFAULT_SEARCH)
    await loadListPage(1, DEFAULT_SEARCH)
  }

  /** 관리자 권한 코드 변경 */
  const changeAuthCode = (index: number, authCode: string) => {
    setAdmins(admins.map((admin, adminIndex) => adminIndex === index ? { ...admin, authCode } : admin))
  }

  /** 관리자 권한 일괄 수정 */
  const save = async () => {
    if (admins.some((admin) => !admin.authCode)) {
      alert('모든 관리자의 권한을 선택해 주세요.')
      return
    }
    setSaving(true)
    onError(null)
    try {
      const result = await updateAdminAuths(admins, pageData.pageNumber, appliedSearch)
      setPageData(result.data.admins)
      setAdmins(result.data.admins.items)
      setAuthGroups(result.data.authGroups.filter((group) => group.useeYsno === 'Y'))
      alert(result.message)
    } catch (error) {
      onError(error instanceof Error ? error.message : '관리자 권한 수정에 실패했습니다.')
    } finally {
      setSaving(false)
    }
  }

  return (
    <section className="menu-manage">
      <section className="content-header">
        <h1>관리자 권한 관리</h1>
        <div className="status">총 {pageData.totalCount}건</div>
      </section>
      <form className="list-search" onSubmit={(event) => void handleSearch(event)}>
        <label>
          <span>검색어</span>
          <input value={search.keyword} placeholder="관리자 번호, 아이디 또는 이름"
                 onChange={(event) => setSearch({ ...search, keyword: event.target.value })} />
        </label>
        <label>
          <span>부서코드</span>
          <input value={search.deptCode} placeholder="부서코드"
                 onChange={(event) => setSearch({ ...search, deptCode: event.target.value })} />
        </label>
        <label>
          <span>권한그룹</span>
          <select value={search.authCode}
                  onChange={(event) => setSearch({ ...search, authCode: event.target.value })}>
            <option value="">전체</option>
            {authGroups.map((group) => (
              <option key={group.authCode} value={group.authCode}>{group.authName} ({group.authCode})</option>
            ))}
          </select>
        </label>
        <div className="list-search-actions">
          <button type="button" className="subtle-button"
                  onClick={() => void handleSearchReset()}>초기화</button>
          <button type="submit">검색</button>
        </div>
      </form>
      <section className="table-wrap admin-auth-table">
        <table>
          <thead>
            <tr>
              <th className="col-number">관리자 번호</th>
              <th>관리자 아이디</th>
              <th>관리자명</th>
              <th>부서</th>
              <th>권한그룹</th>
            </tr>
          </thead>
          <tbody>
            {admins.length === 0 ? (
              <tr className="empty-row"><td colSpan={5}>등록된 관리자가 없습니다.</td></tr>
            ) : admins.map((admin, index) => (
              <tr key={admin.admnNumb}>
                <td className="col-number">{admin.admnNumb}</td>
                <td>{admin.admnIdxx}</td>
                <td>{admin.admnName}</td>
                <td>{admin.deptCode ?? ''}</td>
                <td>
                  <select value={admin.authCode} disabled={permission.writYsno !== 'Y'} onChange={(event) => changeAuthCode(index, event.target.value)}>
                    {authGroups.map((group) => <option key={group.authCode} value={group.authCode}>{group.authName} ({group.authCode})</option>)}
                  </select>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>
      <Pagination pageNumber={pageData.pageNumber} totalPages={pageData.totalPages}
                  onPageChange={(pageNumber) => void loadListPage(pageNumber, appliedSearch)} />
      {permission.writYsno === 'Y' && (
        <div className="detail-footer">
          <div className="detail-footer-left" />
          <div className="detail-footer-right">
            <button type="button" disabled={saving || admins.length === 0} onClick={() => void save()}>{saving ? '수정 중' : '수정'}</button>
          </div>
        </div>
      )}
    </section>
  )
}
