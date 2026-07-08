import { useEffect, useMemo, useState } from 'react'
import type { FormEvent } from 'react'
import './App.css'

const LOGIN_PATH = '/sadari/adm/login'
const HOME_PATH = '/sadari/adm'
const MENU_LIST_PATH = '/sadari/adm/menu/list'
const MENU_NEW_PATH = '/sadari/adm/menu/new'
const MENU_DETAIL_PREFIX = '/sadari/adm/menu/detail'
const CODE_LIST_PATH = '/sadari/adm/code/list'
const CODE_DETAIL_PREFIX = '/sadari/adm/code/detail'
const AUTH_CODE = 'AUTH_CODE'

type AdminSession = { admnNumb: number; admnIdxx: string; admnName: string; authCode: string; authLevel: number; deptCode: string | null }
type Menu = { menuNumb: string; subxNumb: string; menuName: string; menuUrlx: string; readAuth: string | null; writAuth: string | null; deltAuth: string | null; sortOrdr: number | null; useeYsno: string | null }
type Code = { commCode: string; comdCode: string; comdName: string; codeExpl: string | null; useeYsno: string | null; sortOrder: number | null }
type CodeMaster = { commCode: string; codeName: string; codeExpl: string | null; useeYsno: string | null; regeAdmn: string | null; regiDate: string | null }
type ApiResult<T> = { code: number; message: string; data: T }
type Permission = { readYn: boolean; writYn: boolean; deltYn: boolean }
type MenuForm = { menuNumb: string; subxNumb: string; menuName: string; menuUrlx: string; readAuth: string; writAuth: string; deltAuth: string; sortOrdr: string; useeYsno: string }
type DetailCodeForm = { comdCode: string; comdName: string; codeExpl: string; sortOrder: string; useeYsno: string }

const emptyPermission: Permission = { readYn: false, writYn: false, deltYn: false }
const emptyMenuForm = (authCode = 'SUPER', parentMenuNumb = ''): MenuForm => ({
  menuNumb: parentMenuNumb,
  subxNumb: '',
  menuName: '',
  menuUrlx: '/sadari/adm/',
  readAuth: authCode,
  writAuth: authCode,
  deltAuth: authCode,
  sortOrdr: '10',
  useeYsno: 'Y',
})
const emptyDetailForm = (): DetailCodeForm => ({ comdCode: '', comdName: '', codeExpl: '', sortOrder: '10', useeYsno: 'Y' })
const toMenuForm = (menu: Menu): MenuForm => ({
  menuNumb: menu.menuNumb,
  subxNumb: menu.subxNumb,
  menuName: menu.menuName,
  menuUrlx: menu.menuUrlx,
  readAuth: menu.readAuth ?? 'SUPER',
  writAuth: menu.writAuth ?? menu.readAuth ?? 'SUPER',
  deltAuth: menu.deltAuth ?? menu.readAuth ?? 'SUPER',
  sortOrdr: String(menu.sortOrdr ?? 10),
  useeYsno: menu.useeYsno ?? 'Y',
})
const getResultData = async <T,>(response: Response): Promise<T> => {
  const result = (await response.json()) as ApiResult<T>
  if (result.code !== 200) throw new Error(result.message || '요청 처리에 실패했습니다.')
  return result.data
}

function App() {
  const [admin, setAdmin] = useState<AdminSession | null>(null)
  const [menus, setMenus] = useState<Menu[]>([])
  const [menuRows, setMenuRows] = useState<Menu[]>([])
  const [subMenus, setSubMenus] = useState<Menu[]>([])
  const [authCodes, setAuthCodes] = useState<Code[]>([])
  const [menuPermission, setMenuPermission] = useState<Permission>(emptyPermission)
  const [codePermission, setCodePermission] = useState<Permission>(emptyPermission)
  const [menuForm, setMenuForm] = useState<MenuForm>(emptyMenuForm())
  const [childForms, setChildForms] = useState<MenuForm[]>([])

  const [codeMasters, setCodeMasters] = useState<CodeMaster[]>([])
  const [selectedMaster, setSelectedMaster] = useState<CodeMaster | null>(null)
  const [detailCodes, setDetailCodes] = useState<Code[]>([])
  const [showMasterForm, setShowMasterForm] = useState(false)
  const [masterForm, setMasterForm] = useState<CodeMaster>({ commCode: '', codeName: '', codeExpl: '', useeYsno: 'Y', regeAdmn: null, regiDate: null })
  const [duplicateCheckedCode, setDuplicateCheckedCode] = useState('')
  const [duplicateAvailable, setDuplicateAvailable] = useState(false)
  const [detailForms, setDetailForms] = useState<DetailCodeForm[]>([])

  const [admnIdxx, setAdmnIdxx] = useState('admin')
  const [passWord, setPassWord] = useState('')
  const [checkingSession, setCheckingSession] = useState(true)
  const [submitting, setSubmitting] = useState(false)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [currentPath, setCurrentPath] = useState(window.location.pathname)

  const detailKey = useMemo(() => {
    if (!currentPath.startsWith(MENU_DETAIL_PREFIX)) return null
    const [, , , , , menuNumb, subxNumb] = currentPath.split('/')
    return menuNumb && subxNumb ? { menuNumb, subxNumb } : null
  }, [currentPath])
  const parentMenuNumb = useMemo(() => {
    if (!currentPath.startsWith(MENU_NEW_PATH)) return ''
    const [, , , , , menuNumb] = currentPath.split('/')
    return menuNumb ?? ''
  }, [currentPath])
  const codeDetailKey = useMemo(() => {
    if (!currentPath.startsWith(CODE_DETAIL_PREFIX)) return ''
    const [, , , , , commCode] = currentPath.split('/')
    return commCode ?? ''
  }, [currentPath])

  const isMenuListPage = currentPath === MENU_LIST_PATH
  const isMenuNewPage = currentPath === MENU_NEW_PATH || currentPath.startsWith(`${MENU_NEW_PATH}/`)
  const isMenuDetailPage = detailKey !== null
  const isCodeListPage = currentPath === CODE_LIST_PATH
  const isCodeDetailPage = Boolean(codeDetailKey)

  useEffect(() => {
    const handlePopState = () => setCurrentPath(window.location.pathname)
    window.addEventListener('popstate', handlePopState)
    return () => window.removeEventListener('popstate', handlePopState)
  }, [])

  useEffect(() => {
    fetch('/api/auth/me')
      .then(async (response) => {
        if (response.status === 401) return null
        if (!response.ok) throw new Error(`세션 확인에 실패했습니다. (${response.status})`)
        return getResultData<AdminSession>(response)
      })
      .then(async (session) => {
        setAdmin(session)
        if (!session) {
          movePath(LOGIN_PATH)
          return
        }
        await getSidebarMenuList()
        movePath(currentPath.startsWith('/sadari/adm') && currentPath !== LOGIN_PATH ? currentPath : HOME_PATH)
      })
      .catch((err: unknown) => {
        setError(err instanceof Error ? err.message : '세션 확인 중 오류가 발생했습니다.')
        movePath(LOGIN_PATH)
      })
      .finally(() => setCheckingSession(false))
  }, [])

  useEffect(() => {
    if (!admin) return
    if (isMenuListPage) void openMenuListPage()
    else if (isMenuNewPage) void openMenuNewPage(parentMenuNumb)
    else if (detailKey) void openMenuDetailPage(detailKey.menuNumb, detailKey.subxNumb)
    else if (isCodeListPage) void openCodeListPage()
    else if (isCodeDetailPage) void openCodeDetailPage(codeDetailKey)
  }, [admin, currentPath])

  const movePath = (path: string) => {
    if (window.location.pathname !== path) window.history.pushState(null, '', path)
    setCurrentPath(path)
  }
  const getSidebarMenuList = async () => {
    const response = await fetch('/api/menus/sidebar')
    if (!response.ok) throw new Error(`메뉴 목록 조회에 실패했습니다. (${response.status})`)
    setMenus(await getResultData<Menu[]>(response))
  }
  const getPermission = async (menuUrlx: string) => {
    const response = await fetch(`/api/menu-permissions?menuUrlx=${encodeURIComponent(menuUrlx)}`)
    if (!response.ok) throw new Error(`권한 조회에 실패했습니다. (${response.status})`)
    return getResultData<Permission>(response)
  }
  const getAuthCodeList = async () => {
    const response = await fetch(`/api/codes/${AUTH_CODE}`)
    if (!response.ok) throw new Error(`권한 코드 조회에 실패했습니다. (${response.status})`)
    const codes = await getResultData<Code[]>(response)
    setAuthCodes(codes)
    return codes
  }
  const getMenuMngList = async () => {
    const response = await fetch('/api/menus')
    if (!response.ok) throw new Error(`메뉴관리 목록 조회에 실패했습니다. (${response.status})`)
    setMenuRows(await getResultData<Menu[]>(response))
  }
  const openMenuListPage = async () => {
    setError(null)
    setChildForms([])
    const [permission] = await Promise.all([getPermission(MENU_LIST_PATH), getMenuMngList()])
    setMenuPermission(permission)
  }
  const openMenuNewPage = async (parentNumb: string) => {
    setError(null)
    const [codes, permission] = await Promise.all([getAuthCodeList(), getPermission(MENU_LIST_PATH)])
    setMenuPermission(permission)
    setSubMenus([])
    setChildForms([])
    setMenuForm(emptyMenuForm(codes[0]?.comdCode ?? 'SUPER', parentNumb))
  }
  const openMenuDetailPage = async (menuNumb: string, subxNumb: string) => {
    setError(null)
    const [, permission] = await Promise.all([getAuthCodeList(), getPermission(MENU_LIST_PATH)])
    setMenuPermission(permission)
    const detailResponse = await fetch(`/api/menus/${menuNumb}/${subxNumb}`)
    if (!detailResponse.ok) throw new Error(`메뉴 상세 조회에 실패했습니다. (${detailResponse.status})`)
    setMenuForm(toMenuForm(await getResultData<Menu>(detailResponse)))
    const childrenResponse = await fetch(`/api/menus/${menuNumb}/children`)
    if (!childrenResponse.ok) throw new Error(`하위 메뉴 조회에 실패했습니다. (${childrenResponse.status})`)
    setSubMenus(await getResultData<Menu[]>(childrenResponse))
    setChildForms([])
  }
  const openCodeListPage = async () => {
    setError(null)
    const [permission, masters] = await Promise.all([getPermission(CODE_LIST_PATH), getCodeMasters()])
    setCodePermission(permission)
    setCodeMasters(masters)
    setSelectedMaster(null)
    setDetailCodes([])
    setDetailForms([])
  }
  const getCodeMasters = async () => {
    const response = await fetch('/api/code-manage/masters')
    if (!response.ok) throw new Error(`공통코드 목록 조회에 실패했습니다. (${response.status})`)
    return getResultData<CodeMaster[]>(response)
  }

  const setAdminLogin = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setSubmitting(true)
    setError(null)
    try {
      const response = await fetch('/api/auth/login', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ admnIdxx, passWord }) })
      if (!response.ok) throw new Error('아이디 또는 비밀번호가 올바르지 않습니다.')
      const session = await getResultData<AdminSession>(response)
      setAdmin(session)
      setPassWord('')
      await getSidebarMenuList()
      movePath(HOME_PATH)
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : '로그인 중 오류가 발생했습니다.')
    } finally {
      setSubmitting(false)
    }
  }
  const delAdminSession = async () => {
    await fetch('/api/auth/logout', { method: 'POST' })
    setAdmin(null)
    setMenus([])
    setError(null)
    movePath(LOGIN_PATH)
  }

  const saveMenuPayload = async (form: MenuForm, detail: boolean) => {
    const payload = {
      menuNumb: form.menuNumb || null,
      subxNumb: form.subxNumb || null,
      menuName: form.menuName.trim(),
      menuUrlx: form.menuUrlx.trim(),
      readAuth: form.readAuth,
      writAuth: form.writAuth,
      deltAuth: form.deltAuth,
      sortOrdr: Number(form.sortOrdr),
      useeYsno: form.useeYsno,
    }
    const response = await fetch(detail ? `/api/menus/${form.menuNumb}/${form.subxNumb}` : '/api/menus', {
      method: detail ? 'PUT' : 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    })
    if (!response.ok) throw new Error(`메뉴 저장에 실패했습니다. (${response.status})`)
    return getResultData<Menu>(response)
  }
  const saveMenu = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setSaving(true)
    setError(null)
    try {
      const savedMenu = await saveMenuPayload(menuForm, isMenuDetailPage)
      await getSidebarMenuList()
      movePath(`${MENU_DETAIL_PREFIX}/${savedMenu.menuNumb}/${savedMenu.subxNumb}`)
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : '메뉴 저장 중 오류가 발생했습니다.')
    } finally {
      setSaving(false)
    }
  }
  const deleteMenu = async (menu: Menu) => {
    const response = await fetch(`/api/menus/${menu.menuNumb}/${menu.subxNumb}`, { method: 'DELETE' })
    if (!response.ok) throw new Error(`메뉴 삭제에 실패했습니다. (${response.status})`)
    await getSidebarMenuList()
    if (isMenuDetailPage) movePath(MENU_LIST_PATH)
    else await getMenuMngList()
  }
  const addChildForm = () => {
    setChildForms([...childForms, emptyMenuForm(authCodes[0]?.comdCode ?? 'SUPER', menuForm.menuNumb)])
  }
  const changeChildForm = (index: number, field: keyof MenuForm, value: string) => {
    setChildForms(childForms.map((form, formIndex) => (formIndex === index ? { ...form, [field]: value } : form)))
  }
  const saveChildMenu = async (index: number) => {
    setSaving(true)
    setError(null)
    try {
      await saveMenuPayload(childForms[index], false)
      await openMenuDetailPage(menuForm.menuNumb, menuForm.subxNumb)
      await getSidebarMenuList()
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : '하위메뉴 저장 중 오류가 발생했습니다.')
    } finally {
      setSaving(false)
    }
  }
  const changeMenuForm = (field: keyof MenuForm, value: string) => setMenuForm({ ...menuForm, [field]: value })
  const renderAuthOptions = () => authCodes.map((code) => <option key={code.comdCode} value={code.comdCode}>{code.comdName}</option>)

  const selectCodeMaster = async (master: CodeMaster) => {
    movePath(`${CODE_DETAIL_PREFIX}/${master.commCode}`)
  }

  const openCodeDetailPage = async (commCode: string) => {
    setError(null)
    const [permission, masterResponse, detailResponse] = await Promise.all([
      getPermission(CODE_LIST_PATH),
      fetch(`/api/code-manage/masters/${commCode}`),
      fetch(`/api/code-manage/masters/${commCode}/details`),
    ])
    if (!masterResponse.ok) throw new Error(`공통코드 상세 조회에 실패했습니다. (${masterResponse.status})`)
    if (!detailResponse.ok) throw new Error(`세부코드 목록 조회에 실패했습니다. (${detailResponse.status})`)
    setCodePermission(permission)
    setSelectedMaster(await getResultData<CodeMaster>(masterResponse))
    setDetailCodes(await getResultData<Code[]>(detailResponse))
    setDetailForms([])
  }

  const loadSelectedCodeDetails = async (master: CodeMaster) => {
    setSelectedMaster(master)
    setDetailForms([])
    const response = await fetch(`/api/code-manage/masters/${master.commCode}/details`)
    if (!response.ok) throw new Error(`세부코드 목록 조회에 실패했습니다. (${response.status})`)
    setDetailCodes(await getResultData<Code[]>(response))
  }
  const checkCommCodeDuplicate = async () => {
    if (!masterForm.commCode.trim()) {
      setError('공통코드를 입력해 주세요.')
      return
    }
    const response = await fetch(`/api/code-manage/masters/${masterForm.commCode.trim()}/duplicate`)
    if (!response.ok) throw new Error(`공통코드 중복검사에 실패했습니다. (${response.status})`)
    const duplicated = await getResultData<boolean>(response)
    setDuplicateCheckedCode(masterForm.commCode.trim())
    setDuplicateAvailable(!duplicated)
    setError(duplicated ? '이미 등록된 공통코드입니다.' : null)
  }
  const saveCommCode = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    if (!duplicateAvailable || duplicateCheckedCode !== masterForm.commCode.trim()) {
      setError('공통코드 중복검사를 먼저 완료해 주세요.')
      return
    }
    setSaving(true)
    try {
      const response = await fetch('/api/code-manage/masters', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ ...masterForm, commCode: masterForm.commCode.trim() }) })
      if (!response.ok) throw new Error(`공통코드 등록에 실패했습니다. (${response.status})`)
      const saved = await getResultData<CodeMaster>(response)
      setCodeMasters(await getCodeMasters())
      setSelectedMaster(saved)
      setDetailCodes([])
      setShowMasterForm(false)
      setMasterForm({ commCode: '', codeName: '', codeExpl: '', useeYsno: 'Y', regeAdmn: null, regiDate: null })
      setDuplicateCheckedCode('')
      setDuplicateAvailable(false)
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : '공통코드 등록 중 오류가 발생했습니다.')
    } finally {
      setSaving(false)
    }
  }
  const addDetailInput = () => setDetailForms([...detailForms, emptyDetailForm()])
  const changeDetailForm = (index: number, field: keyof DetailCodeForm, value: string) => setDetailForms(detailForms.map((form, formIndex) => (formIndex === index ? { ...form, [field]: value } : form)))
  const saveDetailCode = async (index: number) => {
    if (!selectedMaster) return
    const form = detailForms[index]
    const code = form.comdCode.trim()
    const existingCodes = detailCodes.map((detail) => detail.comdCode)
    const screenCodes = detailForms.map((detail, formIndex) => (formIndex === index ? '' : detail.comdCode.trim())).filter(Boolean)
    if (!code || existingCodes.includes(code) || screenCodes.includes(code)) {
      setError('같은 공통코드 내에서 세부코드는 중복될 수 없습니다.')
      return
    }
    setSaving(true)
    try {
      const response = await fetch(`/api/code-manage/masters/${selectedMaster.commCode}/details`, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ comdCode: code, comdName: form.comdName.trim(), codeExpl: form.codeExpl.trim(), sortOrder: Number(form.sortOrder), useeYsno: form.useeYsno }) })
      if (!response.ok) throw new Error(`세부코드 등록에 실패했습니다. (${response.status})`)
      await loadSelectedCodeDetails(selectedMaster)
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : '세부코드 등록 중 오류가 발생했습니다.')
    } finally {
      setSaving(false)
    }
  }
  const deleteDetailCode = async (detail: Code) => {
    if (!selectedMaster) return
    const response = await fetch(`/api/code-manage/masters/${selectedMaster.commCode}/details/${detail.comdCode}`, { method: 'DELETE' })
    if (!response.ok) throw new Error(`세부코드 삭제에 실패했습니다. (${response.status})`)
    await loadSelectedCodeDetails(selectedMaster)
  }

  if (checkingSession) return <main className="login-page"><div className="panel">세션을 확인하고 있습니다.</div></main>
  if (!admin || currentPath === LOGIN_PATH) {
    return (
      <main className="login-page">
        <form className="login-panel" onSubmit={setAdminLogin}>
          <div><p className="eyebrow">사다리 관리자</p><h1>관리자 로그인</h1></div>
          {error && <div className="error">{error}</div>}
          <label><span>관리자 아이디</span><input value={admnIdxx} onChange={(event) => setAdmnIdxx(event.target.value)} required /></label>
          <label><span>비밀번호</span><input type="password" value={passWord} onChange={(event) => setPassWord(event.target.value)} required /></label>
          <button type="submit" disabled={submitting}>{submitting ? '로그인 중' : '로그인'}</button>
        </form>
      </main>
    )
  }

  return (
    <div className="admin-layout">
      <aside className="sidebar">
        <div className="sidebar-title">사다리 관리자</div>
        <nav className="menu">
          {menus.map((menu) => (
            <button key={`${menu.menuNumb}-${menu.subxNumb}`} type="button" className={currentPath === menu.menuUrlx ? 'menu-item active' : 'menu-item'} onClick={() => movePath(menu.menuUrlx)}>
              <span className={menu.subxNumb === '0' ? 'menu-label' : 'menu-label child'}>{menu.menuName}</span>
            </button>
          ))}
        </nav>
      </aside>
      <div className="content-shell">
        <header className="top-header"><div className="welcome">{admin.admnName}님 환영합니다.</div><button type="button" className="logout-button" onClick={delAdminSession}>로그아웃</button></header>
        <main className="content">
          {error && <div className="error">{error}</div>}
          {isMenuListPage && renderMenuList()}
          {(isMenuDetailPage || isMenuNewPage) && renderMenuForm()}
          {isCodeListPage && renderCodeManage()}
          {isCodeDetailPage && renderCodeDetail()}
        </main>
      </div>
    </div>
  )

  function renderMenuList() {
    return (
      <section className="menu-manage">
        <section className="content-header"><h1>메뉴관리</h1><div className="status">총 {menuRows.length}건</div></section>
        <section className="table-wrap"><table><thead><tr><th>메뉴명</th><th>URL</th><th>사용여부</th>{menuPermission.deltYn && <th>삭제</th>}</tr></thead><tbody>
          {menuRows.map((menu) => (
            <tr key={`${menu.menuNumb}-${menu.subxNumb}`} onClick={() => movePath(`${MENU_DETAIL_PREFIX}/${menu.menuNumb}/${menu.subxNumb}`)}>
              <td>{menu.menuName}</td><td>{menu.menuUrlx}</td><td>{menu.useeYsno}</td>
              {menuPermission.deltYn && <td><button type="button" className="subtle-button" onClick={(event) => { event.stopPropagation(); void deleteMenu(menu) }}>삭제</button></td>}
            </tr>
          ))}
        </tbody></table></section>
        {menuPermission.writYn && <button type="button" className="floating-button" onClick={() => movePath(MENU_NEW_PATH)}>등록</button>}
      </section>
    )
  }

  function renderMenuForm() {
    return (
      <section className="menu-detail-page">
        <section className="content-header">
          <h1>{isMenuNewPage ? '메뉴 등록' : '메뉴 상세'}</h1>
          <div className="header-actions">
            {!isMenuNewPage && menuPermission.deltYn && <button type="button" className="subtle-button" onClick={() => void deleteMenu({ ...menuForm, sortOrdr: Number(menuForm.sortOrdr) })}>삭제</button>}
            <button type="button" className="subtle-button" onClick={() => movePath(MENU_LIST_PATH)}>목록</button>
          </div>
        </section>
        <form className="menu-detail-form" onSubmit={saveMenu}>
          {!isMenuNewPage && <label><span>메뉴번호</span><input value={`${menuForm.menuNumb}-${menuForm.subxNumb}`} readOnly /></label>}
          {renderMenuInputs(menuForm, changeMenuForm)}
          <div className="form-actions">
            {!isMenuNewPage && menuForm.subxNumb === '0' && menuPermission.writYn && <button type="button" className="subtle-button" onClick={addChildForm}>하위메뉴 등록</button>}
            {menuPermission.writYn && <button type="submit" disabled={saving}>{saving ? '저장 중' : '저장'}</button>}
          </div>
        </form>
        {!isMenuNewPage && menuForm.subxNumb === '0' && (
          <section className="sub-menu-list">
            <h2>하위 메뉴</h2>
            <section className="table-wrap"><table><thead><tr><th>메뉴명</th><th>URL</th><th>사용여부</th>{menuPermission.deltYn && <th>삭제</th>}</tr></thead><tbody>
              {subMenus.map((menu) => (
                <tr key={`${menu.menuNumb}-${menu.subxNumb}`} onClick={() => movePath(`${MENU_DETAIL_PREFIX}/${menu.menuNumb}/${menu.subxNumb}`)}>
                  <td>{menu.menuName}</td><td>{menu.menuUrlx}</td><td>{menu.useeYsno}</td>
                  {menuPermission.deltYn && <td><button type="button" className="subtle-button" onClick={(event) => { event.stopPropagation(); void deleteMenu(menu) }}>삭제</button></td>}
                </tr>
              ))}
            </tbody></table></section>
            {childForms.map((form, index) => (
              <section className="child-menu-form" key={index}>
                {renderMenuInputs(form, (field, value) => changeChildForm(index, field, value))}
                <button type="button" disabled={saving} onClick={() => void saveChildMenu(index)}>하위메뉴 저장</button>
              </section>
            ))}
          </section>
        )}
      </section>
    )
  }

  function renderMenuInputs(form: MenuForm, onChange: (field: keyof MenuForm, value: string) => void) {
    return (
      <>
        <label><span>메뉴명</span><input value={form.menuName} onChange={(event) => onChange('menuName', event.target.value)} required /></label>
        <label><span>메뉴 URL</span><input value={form.menuUrlx} onChange={(event) => onChange('menuUrlx', event.target.value)} required /></label>
        <label><span>사용여부</span><select value={form.useeYsno} onChange={(event) => onChange('useeYsno', event.target.value)}><option value="Y">사용</option><option value="N">미사용</option></select></label>
        <label><span>조회권한</span><select value={form.readAuth} onChange={(event) => onChange('readAuth', event.target.value)}>{renderAuthOptions()}</select></label>
        <label><span>쓰기권한</span><select value={form.writAuth} onChange={(event) => onChange('writAuth', event.target.value)}>{renderAuthOptions()}</select></label>
        <label><span>삭제권한</span><select value={form.deltAuth} onChange={(event) => onChange('deltAuth', event.target.value)}>{renderAuthOptions()}</select></label>
        <label><span>정렬순서</span><input type="number" value={form.sortOrdr} onChange={(event) => onChange('sortOrdr', event.target.value)} required /></label>
      </>
    )
  }

  function renderCodeManage() {
    return (
      <section className="code-manage">
        <section className="content-header"><h1>코드관리</h1><div className="status">총 {codeMasters.length}건</div></section>
        <section className="table-wrap"><table><thead><tr><th>공통코드</th><th>공통코드명</th><th>사용여부</th><th>등록자</th><th>등록일</th></tr></thead><tbody>
            {codeMasters.map((master) => (
              <tr key={master.commCode} onClick={() => selectCodeMaster(master)}>
                <td>{master.commCode}</td>
                <td>{master.codeName}</td>
                <td>{master.useeYsno}</td>
                <td>{master.regeAdmn}</td>
                <td>{formatDate(master.regiDate)}</td>
              </tr>
            ))}
          </tbody></table></section>
        {codePermission.writYn && <button type="button" className="floating-button" onClick={() => setShowMasterForm(true)}>등록</button>}
        {showMasterForm && renderMasterModal()}
      </section>
    )
  }

  function renderCodeDetail() {
    if (!selectedMaster) return null
    return (
      <section className="code-detail">
        <section className="content-header">
          <div>
            <h1>코드 상세</h1>
            <p className="eyebrow">{selectedMaster.commCode}</p>
          </div>
          <div className="header-actions">
            {codePermission.writYn && <button type="button" onClick={addDetailInput}>세부코드 추가</button>}
            <button type="button" className="subtle-button" onClick={() => movePath(CODE_LIST_PATH)}>목록</button>
          </div>
        </section>
        <section className="detail-panel">
          <div className="detail-title">
            <div>
              <h2>{selectedMaster.codeName}</h2>
              <p>{selectedMaster.codeExpl}</p>
            </div>
            <span>{selectedMaster.useeYsno}</span>
          </div>
          <section className="table-wrap"><table><thead><tr><th>세부코드</th><th>세부코드명</th><th>정렬</th><th>사용여부</th>{codePermission.deltYn && <th>삭제</th>}</tr></thead><tbody>
            {detailCodes.map((detail) => <tr key={detail.comdCode}><td>{detail.comdCode}</td><td>{detail.comdName}</td><td>{detail.sortOrder}</td><td>{detail.useeYsno}</td>{codePermission.deltYn && <td><button type="button" className="subtle-button" onClick={() => void deleteDetailCode(detail)}>삭제</button></td>}</tr>)}
          </tbody></table></section>
          {detailForms.map((form, index) => <section className="inline-form" key={index}><input placeholder="세부코드" value={form.comdCode} onChange={(event) => changeDetailForm(index, 'comdCode', event.target.value)} /><input placeholder="세부코드명" value={form.comdName} onChange={(event) => changeDetailForm(index, 'comdName', event.target.value)} /><input placeholder="설명" value={form.codeExpl} onChange={(event) => changeDetailForm(index, 'codeExpl', event.target.value)} /><input type="number" value={form.sortOrder} onChange={(event) => changeDetailForm(index, 'sortOrder', event.target.value)} /><select value={form.useeYsno} onChange={(event) => changeDetailForm(index, 'useeYsno', event.target.value)}><option value="Y">사용</option><option value="N">미사용</option></select><button type="button" disabled={saving} onClick={() => void saveDetailCode(index)}>저장</button></section>)}
        </section>
      </section>
    )
  }

  function formatDate(value: string | null) {
    if (!value) return ''
    return value.replace('T', ' ').slice(0, 19)
  }

  function renderMasterModal() {
    return (
      <section className="modal-backdrop">
        <form className="modal-panel" onSubmit={saveCommCode}>
          <h2>공통코드 등록</h2>
          <label><span>공통코드</span><div className="inline-check"><input value={masterForm.commCode} onChange={(event) => { setMasterForm({ ...masterForm, commCode: event.target.value }); setDuplicateCheckedCode(''); setDuplicateAvailable(false) }} required /><button type="button" onClick={() => void checkCommCodeDuplicate()}>중복검사</button></div></label>
          <label><span>공통코드명</span><input value={masterForm.codeName} onChange={(event) => setMasterForm({ ...masterForm, codeName: event.target.value })} required /></label>
          <label><span>설명</span><input value={masterForm.codeExpl ?? ''} onChange={(event) => setMasterForm({ ...masterForm, codeExpl: event.target.value })} /></label>
          <label><span>사용여부</span><select value={masterForm.useeYsno ?? 'Y'} onChange={(event) => setMasterForm({ ...masterForm, useeYsno: event.target.value })}><option value="Y">사용</option><option value="N">미사용</option></select></label>
          <div className="form-actions"><button type="button" className="subtle-button" onClick={() => setShowMasterForm(false)}>취소</button><button type="submit" disabled={saving || !duplicateAvailable}>등록</button></div>
        </form>
      </section>
    )
  }
}

export default App
