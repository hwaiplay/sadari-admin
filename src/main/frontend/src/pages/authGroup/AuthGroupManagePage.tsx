import { useEffect, useMemo, useState } from 'react'
import type { FormEvent } from 'react'
import { checkAuthGroupDuplicate, deleteAuthGroup, getAuthGroup, getAuthGroups, saveAuthGroup } from '../../api/authGroupApi'
import { getSidebarMenus } from '../../api/menuApi'
import { getCodeList } from '../../api/codeApi'
import { COMM_YSNO, DEFAULT_USEE_YSNO } from '../../constants/codes'
import { AUTH_GROUP_DETAIL_PREFIX, AUTH_GROUP_LIST_PATH, AUTH_GROUP_NEW_PATH } from '../../constants/routes'
import type { AuthGroup, AuthMenu } from '../../types/authGroup'
import type { Code } from '../../types/code'
import type { Menu } from '../../types/menu'
import { AuditInfoTable } from '../../components/AuditInfoTable'
import { formatDate, getUseeYsnoCodeName } from '../../utils/code'
import { useMenuPermission } from '../../contexts/MenuPermissionContext'
import { Pagination } from '../../components/Pagination'
import type { PageData } from '../../types/common'

type AuthGroupManagePageProps = {
  currentPath: string
  onMovePath: (path: string) => void
  onError: (message: string | null) => void
}

const emptyGroup = (): AuthGroup => ({
  authCode: '',
  authName: '',
  useeYsno: DEFAULT_USEE_YSNO,
  regiAdmn: null,
  regiDate: null,
  updtAdmn: null,
  updtDate: null,
  menus: [],
})

/** 관리자 메뉴를 권한 입력 행으로 변환 */
const toAuthMenu = (menu: Menu): AuthMenu => ({
  authCode: '',
  menuNumb: menu.menuNumb,
  subxNumb: menu.subxNumb,
  menuName: menu.menuName,
  menuUrlx: menu.menuUrlx,
  sortOrdr: menu.sortOrdr,
  readYsno: 'N',
  writYsno: 'N',
  deltYsno: 'N',
})

/** 권한그룹 관리 화면 */
export function AuthGroupManagePage({ currentPath, onMovePath, onError }: AuthGroupManagePageProps) {
  const permission = useMenuPermission()
  const [groups, setGroups] = useState<AuthGroup[]>([])
  const [pageData, setPageData] = useState<PageData<AuthGroup>>({ items: [], totalCount: 0, pageNumber: 1, pageSize: 20, totalPages: 0 })
  const [form, setForm] = useState<AuthGroup>(emptyGroup())
  const [useeCodes, setUseeCodes] = useState<Code[]>([])
  const [saving, setSaving] = useState(false)
  const [duplicateCheckedCode, setDuplicateCheckedCode] = useState('')
  const [duplicateAvailable, setDuplicateAvailable] = useState(false)

  const isList = currentPath === AUTH_GROUP_LIST_PATH
  const isNew = currentPath === AUTH_GROUP_NEW_PATH
  const detailCode = useMemo(() => {
    if (!currentPath.startsWith(`${AUTH_GROUP_DETAIL_PREFIX}/`)) return ''
    return decodeURIComponent(currentPath.slice(AUTH_GROUP_DETAIL_PREFIX.length + 1))
  }, [currentPath])
  const checkedCurrentCode = duplicateCheckedCode === form.authCode && duplicateCheckedCode !== ''

  /** 현재 경로에 필요한 데이터를 조회 */
  useEffect(() => {
    const load = async () => {
      onError(null)
      try {
        const codes = await getCodeList(COMM_YSNO)
        setUseeCodes(codes)
        if (isList) {
          const result = await getAuthGroups(1)
          setPageData(result)
          setGroups(result.items)
          return
        }
        if (isNew) {
          const sidebarMenus = await getSidebarMenus()
          setForm({ ...emptyGroup(), menus: sidebarMenus.map(toAuthMenu) })
          setDuplicateCheckedCode('')
          setDuplicateAvailable(false)
          return
        }
        if (detailCode) setForm(await getAuthGroup(detailCode))
      } catch (error) {
        onError(error instanceof Error ? error.message : '권한그룹 정보를 불러오지 못했습니다.')
      }
    }
    void load()
  }, [currentPath])

  /** 권한그룹 목록 페이지 조회 */
  const loadListPage = async (pageNumber: number) => {
    const result = await getAuthGroups(pageNumber)
    setPageData(result)
    setGroups(result.items)
  }

  /** 권한 코드 입력 */
  const changeAuthCode = (value: string) => {
    const authCode = value.toUpperCase().replace(/[^A-Z0-9_]/g, '')
    setForm({ ...form, authCode })
    setDuplicateCheckedCode('')
    setDuplicateAvailable(false)
  }

  /** 권한 코드 중복 확인 */
  const checkDuplicate = async () => {
    if (!form.authCode) {
      alert('권한 코드를 입력해 주세요.')
      return
    }
    try {
      const duplicated = await checkAuthGroupDuplicate(form.authCode)
      setDuplicateCheckedCode(form.authCode)
      setDuplicateAvailable(!duplicated)
      onError(null)
    } catch (error) {
      onError(error instanceof Error ? error.message : '중복 확인에 실패했습니다.')
    }
  }

  /** 메뉴 권한 값 변경 */
  const changeMenuPermission = (index: number, field: 'readYsno' | 'writYsno' | 'deltYsno', checked: boolean) => {
    const menus = form.menus.map((menu, menuIndex) => {
      if (menuIndex !== index) return menu
      const value = checked ? 'Y' : 'N'
      if (field === 'readYsno' && !checked) return { ...menu, readYsno: 'N', writYsno: 'N', deltYsno: 'N' }
      if (field !== 'readYsno' && checked) return { ...menu, readYsno: 'Y', [field]: value }
      return { ...menu, [field]: value }
    })
    setForm({ ...form, menus })
  }

  /** 권한그룹 저장 */
  const submit = async (event?: FormEvent<HTMLFormElement>) => {
    event?.preventDefault()
    if (!form.authCode.trim() || !form.authName.trim()) {
      alert('권한 코드와 권한명을 입력해 주세요.')
      return
    }
    if (isNew && (!duplicateAvailable || duplicateCheckedCode !== form.authCode)) {
      alert('권한 코드 중복검사를 완료해 주세요.')
      return
    }
    setSaving(true)
    onError(null)
    try {
      const result = await saveAuthGroup(form, Boolean(detailCode))
      alert(result.message)
      onMovePath(`${AUTH_GROUP_DETAIL_PREFIX}/${encodeURIComponent(result.data.authCode)}`)
    } catch (error) {
      onError(error instanceof Error ? error.message : '권한그룹 저장에 실패했습니다.')
    } finally {
      setSaving(false)
    }
  }

  /** 권한그룹 삭제 */
  const remove = async () => {
    if (!window.confirm('권한그룹을 삭제하시겠습니까.')) return
    try {
      const result = await deleteAuthGroup(form.authCode)
      alert(result.message)
      onMovePath(AUTH_GROUP_LIST_PATH)
    } catch (error) {
      onError(error instanceof Error ? error.message : '권한그룹 삭제에 실패했습니다.')
    }
  }

  if (isList) {
    return (
      <section className="menu-manage">
        <section className="content-header">
          <h1>권한그룹관리</h1>
          <div className="status">총 {pageData.totalCount}건</div>
        </section>
        <section className="table-wrap">
          <table>
            <thead><tr><th>권한 코드</th><th>권한명</th><th className="col-usee">사용여부</th><th>수정자</th><th>수정일</th></tr></thead>
            <tbody>
              {groups.length === 0
                ? <tr className="empty-row"><td colSpan={5}>등록된 권한그룹이 없습니다.</td></tr>
                : groups.map((group) => (
                  <tr key={group.authCode} onClick={() => onMovePath(`${AUTH_GROUP_DETAIL_PREFIX}/${encodeURIComponent(group.authCode)}`)}>
                    <td>{group.authCode}</td><td>{group.authName}</td>
                    <td className="col-usee">{getUseeYsnoCodeName(useeCodes, group.useeYsno, group.useeYsnoName)}</td>
                    <td>{group.updtAdmnName ?? group.updtAdmn}</td><td>{formatDate(group.updtDate)}</td>
                  </tr>
                ))}
            </tbody>
          </table>
        </section>
        <Pagination pageNumber={pageData.pageNumber} totalPages={pageData.totalPages} onPageChange={(pageNumber) => void loadListPage(pageNumber)} />
        {permission.writYsno === 'Y' && <button type="button" className="floating-button" onClick={() => onMovePath(AUTH_GROUP_NEW_PATH)}>등록</button>}
      </section>
    )
  }

  return (
    <section className="menu-detail-page">
      <section className="content-header">
        <h1>{isNew ? '권한그룹 등록' : '권한그룹관리 상세'}</h1>
      </section>
      <form noValidate onSubmit={submit}>
        <section className="detail-panel">
          <div className="detail-title"><div><h2>권한그룹 정보</h2><p>권한 코드와 권한명을 설정합니다.</p></div></div>
          <section className="table-wrap menu-info-table">
            <table><tbody><tr>
              <th>권한 코드</th>
              <td>
                <div className="inline-check">
                  <input value={form.authCode} maxLength={30} readOnly={!isNew} onChange={(event) => changeAuthCode(event.target.value)} required />
                  {isNew && <button type="button" onClick={() => void checkDuplicate()}>중복검사</button>}
                </div>
                {isNew && checkedCurrentCode && (
                  <p className={duplicateAvailable ? 'duplicate-message success' : 'duplicate-message fail'}>
                    {duplicateAvailable ? '사용 가능한 코드입니다.' : '이미 사용 중인 코드입니다.'}
                  </p>
                )}
              </td>
              <th>권한명</th><td><input value={form.authName} maxLength={100} onChange={(event) => setForm({ ...form, authName: event.target.value })} required /></td>
              <th>사용여부</th><td><select value={form.useeYsno} onChange={(event) => setForm({ ...form, useeYsno: event.target.value })}>{useeCodes.map((code) => <option key={code.comdCode} value={code.comdCode}>{code.opt1Name ?? code.comdName}</option>)}</select></td>
            </tr></tbody></table>
          </section>
        </section>
        <section className="detail-panel">
          <div className="detail-title"><div><h2>메뉴 권한</h2><p>조회, 쓰기, 삭제 권한을 메뉴별로 설정합니다.</p></div></div>
          <section className="table-wrap auth-menu-table">
            <table>
              <thead><tr><th>메뉴명</th><th>URL</th><th className="col-permission">조회</th><th className="col-permission">쓰기</th><th className="col-permission">삭제</th></tr></thead>
              <tbody>{form.menus.map((menu, index) => (
                <tr key={`${menu.menuNumb}-${menu.subxNumb}`} className={menu.subxNumb === '0' ? 'parent-menu-row' : ''}>
                  <td>{menu.subxNumb !== '0' && <span className="menu-depth-mark">└</span>}{menu.menuName}</td><td>{menu.menuUrlx}</td>
                  <td className="col-permission"><input type="checkbox" checked={menu.readYsno === 'Y'} onChange={(event) => changeMenuPermission(index, 'readYsno', event.target.checked)} /></td>
                  <td className="col-permission"><input type="checkbox" checked={menu.writYsno === 'Y'} onChange={(event) => changeMenuPermission(index, 'writYsno', event.target.checked)} /></td>
                  <td className="col-permission"><input type="checkbox" checked={menu.deltYsno === 'Y'} onChange={(event) => changeMenuPermission(index, 'deltYsno', event.target.checked)} /></td>
                </tr>
              ))}</tbody>
            </table>
          </section>
        </section>
      </form>
      {!isNew && <AuditInfoTable regiAdmn={form.regiAdmn} regiAdmnName={form.regiAdmnName} regiDate={form.regiDate} updtAdmn={form.updtAdmn} updtAdmnName={form.updtAdmnName} updtDate={form.updtDate} />}
      <div className="detail-footer">
        <div className="detail-footer-left">
          <button type="button" className="subtle-button" onClick={() => onMovePath(AUTH_GROUP_LIST_PATH)}>목록</button>
          {!isNew && permission.deltYsno === 'Y' && <button type="button" className="delete-button" onClick={() => void remove()}>삭제</button>}
        </div>
        <div className="detail-footer-right">
          {permission.writYsno === 'Y' && <button type="button" disabled={saving} onClick={() => void submit()}>{saving ? '저장 중' : isNew ? '저장' : '수정'}</button>}
        </div>
      </div>
    </section>
  )
}
