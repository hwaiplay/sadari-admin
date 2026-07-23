import { useEffect, useMemo, useState } from 'react'
import type { FormEvent } from 'react'
import { getAdminSession, loginAdmin, logoutAdmin } from './api/adminApi'
import { checkMasterDuplicate, createCodeMaster, createDetailCode, getCodeList, getCodeMaster, getCodeMasters, getDetailCodes, updateCodeMaster, updateDetailCode } from './api/codeApi'
import { deleteMenuApi, getMenuDetail, getMenuMngList, getPermission, getSidebarMenus, getSubMenus, saveMenuApi } from './api/menuApi'
import './App.css'
import { DEFAULT_AUTH_CODE, DEFAULT_USEE_YSNO, AUTH_CODE, COMM_YSNO } from './constants/codes'
import { CODE_DETAIL_PREFIX, CODE_LIST_PATH, HOME_PATH, LOGIN_PATH, MENU_DETAIL_PREFIX, MENU_LIST_PATH, MENU_NEW_PATH } from './constants/routes'
import { AdminLayout } from './components/AdminLayout'
import { LoginPage } from './pages/LoginPage'
import { CodeDetailPage } from './pages/code/CodeDetailPage'
import { CodeListPage } from './pages/code/CodeListPage'
import { CodeMasterModal } from './pages/code/CodeMasterModal'
import { MenuDetailPage } from './pages/menu/MenuDetailPage'
import { MenuListPage } from './pages/menu/MenuListPage'
import type { AdminSession } from './types/admin'
import type { Code, CodeMaster, DetailCodeForm, DetailCodePayload } from './types/code'
import { emptyPermission } from './types/common'
import type { Permission } from './types/common'
import type { Menu, MenuForm } from './types/menu'
import { emptyDetailForm, emptyMenuForm, toDetailCodeForm, toMenuForm } from './utils/forms'

/**
 * 관리자 프론트 루트 컴포넌트
 * @Author SeungHyeon.Kang
 * @return
 */
function App() {
  const [admin, setAdmin] = useState<AdminSession | null>(null)
  const [menus, setMenus] = useState<Menu[]>([])
  const [menuRows, setMenuRows] = useState<Menu[]>([])
  const [menuDetail, setMenuDetail] = useState<Menu | null>(null)
  const [subMenus, setSubMenus] = useState<Menu[]>([])
  const [authCodes, setAuthCodes] = useState<Code[]>([])
  const [useeYsnoCodes, setUseeYsnoCodes] = useState<Code[]>([])
  const [menuPermission, setMenuPermission] = useState<Permission>(emptyPermission)
  const [codePermission, setCodePermission] = useState<Permission>(emptyPermission)
  const [menuForm, setMenuForm] = useState<MenuForm>(emptyMenuForm())
  const [childForms, setChildForms] = useState<MenuForm[]>([])
  const [codeMasters, setCodeMasters] = useState<CodeMaster[]>([])
  const [selectedMaster, setSelectedMaster] = useState<CodeMaster | null>(null)
  const [masterEditForm, setMasterEditForm] = useState<CodeMaster | null>(null)
  const [detailCodes, setDetailCodes] = useState<Code[]>([])
  const [detailEditForms, setDetailEditForms] = useState<DetailCodeForm[]>([])
  const [showMasterForm, setShowMasterForm] = useState(false)
  const [masterForm, setMasterForm] = useState<CodeMaster>({ commCode: '', codeName: '', codeExpl: '', useeYsno: DEFAULT_USEE_YSNO, regiAdmn: null, regiAdmnName: null, regiDate: null, updtAdmn: null, updtAdmnName: null, updtDate: null })
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

  const activeMenuPath = useMemo(() => {
    if (currentPath === MENU_NEW_PATH || currentPath.startsWith(`${MENU_NEW_PATH}/`) || currentPath.startsWith(MENU_DETAIL_PREFIX)) return MENU_LIST_PATH
    if (currentPath.startsWith(CODE_DETAIL_PREFIX)) return CODE_LIST_PATH
    return currentPath
  }, [currentPath])

  const activeMenuName = useMemo(() => menus.find((menu) => menu.menuUrlx === activeMenuPath)?.menuName ?? '', [menus, activeMenuPath])

  /**
   * 화면 경로 이동
   * @Author SeungHyeon.Kang
   * @param path
   * @return
   */
  const movePath = (path: string) => {
    if (window.location.pathname !== path) window.history.pushState(null, '', path)
    setCurrentPath(path)
  }

  /**
   * 사이드바 메뉴 목록 로드
   * @Author SeungHyeon.Kang
   * @return
   */
  const loadSidebarMenuList = async () => {
    setMenus(await getSidebarMenus())
  }

  /**
   * 권한 코드 목록 로드
   * @Author SeungHyeon.Kang
   * @return
   */
  const loadAuthCodeList = async () => {
    const codes = await getCodeList(AUTH_CODE)
    setAuthCodes(codes)
    return codes
  }

  /**
   * 사용여부 코드 목록 로드
   * @Author SeungHyeon.Kang
   * @return
   */
  const loadUseeYsnoCodeList = async () => {
    const codes = await getCodeList(COMM_YSNO)
    setUseeYsnoCodes(codes)
    return codes
  }

  /**
   * 공통코드 등록 폼 초기화
   * @Author SeungHyeon.Kang
   * @return
   */
  const resetMasterForm = () => {
    setMasterForm({ commCode: '', codeName: '', codeExpl: '', useeYsno: DEFAULT_USEE_YSNO, regiAdmn: null, regiAdmnName: null, regiDate: null, updtAdmn: null, updtAdmnName: null, updtDate: null })
    setDuplicateCheckedCode('')
    setDuplicateAvailable(false)
  }

  useEffect(() => {
    /**
     * 브라우저 뒤로가기 경로 동기화
     * @Author SeungHyeon.Kang
     * @return
     */
    const handlePopState = () => setCurrentPath(window.location.pathname)
    window.addEventListener('popstate', handlePopState)
    return () => window.removeEventListener('popstate', handlePopState)
  }, [])

  useEffect(() => {
    getAdminSession()
      .then(async (session) => {
        setAdmin(session)
        if (!session) {
          movePath(LOGIN_PATH)
          return
        }
        await loadSidebarMenuList()
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

  /**
   * 메뉴관리 목록 화면 열기
   * @Author SeungHyeon.Kang
   * @return
   */
  const openMenuListPage = async () => {
    setError(null)
    setChildForms([])
    setMenuDetail(null)
    const [permission, rows] = await Promise.all([getPermission(MENU_LIST_PATH), getMenuMngList(), loadUseeYsnoCodeList()])
    setMenuPermission(permission)
    setMenuRows(rows)
  }

  /**
   * 메뉴 등록 화면 열기
   * @Author SeungHyeon.Kang
   * @param parentNumb
   * @return
   */
  const openMenuNewPage = async (parentNumb: string) => {
    setError(null)
    const [codes, permission] = await Promise.all([loadAuthCodeList(), getPermission(MENU_LIST_PATH), loadUseeYsnoCodeList()])
    setMenuPermission(permission)
    setMenuDetail(null)
    setSubMenus([])
    setChildForms([])
    setMenuForm(emptyMenuForm(codes[0]?.comdCode ?? DEFAULT_AUTH_CODE, parentNumb))
  }

  /**
   * 메뉴 상세 화면 열기
   * @Author SeungHyeon.Kang
   * @param menuNumb
   * @param subxNumb
   * @return
   */
  const openMenuDetailPage = async (menuNumb: string, subxNumb: string) => {
    setError(null)
    const [, permission, detail, children] = await Promise.all([loadAuthCodeList(), getPermission(MENU_LIST_PATH), getMenuDetail(menuNumb, subxNumb), getSubMenus(menuNumb), loadUseeYsnoCodeList()])
    setMenuPermission(permission)
    setMenuDetail(detail)
    setMenuForm(toMenuForm(detail))
    setSubMenus(children)
    setChildForms([])
  }

  /**
   * 코드관리 목록 화면 열기
   * @Author SeungHyeon.Kang
   * @return
   */
  const openCodeListPage = async () => {
    setError(null)
    const [permission, masters] = await Promise.all([getPermission(CODE_LIST_PATH), getCodeMasters(), loadUseeYsnoCodeList()])
    setCodePermission(permission)
    setCodeMasters(masters)
    setSelectedMaster(null)
    setMasterEditForm(null)
    setDetailCodes([])
    setDetailEditForms([])
    setDetailForms([])
  }

  /**
   * 코드관리 상세 화면 열기
   * @Author SeungHyeon.Kang
   * @param commCode
   * @return
   */
  const openCodeDetailPage = async (commCode: string) => {
    setError(null)
    const [permission, master, details] = await Promise.all([getPermission(CODE_LIST_PATH), getCodeMaster(commCode), getDetailCodes(commCode), loadUseeYsnoCodeList()])
    setCodePermission(permission)
    setSelectedMaster(master)
    setMasterEditForm(master)
    setDetailCodes(details)
    setDetailEditForms(details.map(toDetailCodeForm))
    setDetailForms([])
  }

  /**
   * 로그인 처리
   * @Author SeungHyeon.Kang
   * @param event
   * @return
   */
  const handleLogin = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setSubmitting(true)
    setError(null)
    try {
      const session = await loginAdmin(admnIdxx, passWord)
      setAdmin(session)
      setPassWord('')
      await loadSidebarMenuList()
      movePath(HOME_PATH)
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : '로그인 중 오류가 발생했습니다.')
    } finally {
      setSubmitting(false)
    }
  }

  /**
   * 로그아웃 처리
   * @Author SeungHyeon.Kang
   * @return
   */
  const handleLogout = async () => {
    await logoutAdmin()
    setAdmin(null)
    setMenus([])
    setError(null)
    movePath(LOGIN_PATH)
  }

  /**
   * 메뉴 저장 처리
   * @Author SeungHyeon.Kang
   * @param event
   * @return
   */
  const saveMenu = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setSaving(true)
    setError(null)
    try {
      const savedMenu = await saveMenuApi(menuForm, isMenuDetailPage)
      alert(savedMenu.message)
      await loadSidebarMenuList()
      movePath(`${MENU_DETAIL_PREFIX}/${savedMenu.data.menuNumb}/${savedMenu.data.subxNumb}`)
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : '메뉴 저장 중 오류가 발생했습니다.')
    } finally {
      setSaving(false)
    }
  }

  /**
   * 메뉴 삭제 처리
   * @Author SeungHyeon.Kang
   * @param menu
   * @return
   */
  const deleteMenu = async (menu: Pick<Menu, 'menuNumb' | 'subxNumb'>) => {
    await deleteMenuApi(menu)
    await loadSidebarMenuList()
    if (isMenuDetailPage) movePath(MENU_LIST_PATH)
    else setMenuRows(await getMenuMngList())
  }

  /**
   * 하위메뉴 입력 폼 추가
   * @Author SeungHyeon.Kang
   * @return
   */
  const addChildForm = () => {
    setChildForms([...childForms, emptyMenuForm(authCodes[0]?.comdCode ?? DEFAULT_AUTH_CODE, menuForm.menuNumb)])
  }

  /**
   * 하위메뉴 입력값 변경
   * @Author SeungHyeon.Kang
   * @param index
   * @param field
   * @param value
   * @return
   */
  const changeChildForm = (index: number, field: keyof MenuForm, value: string) => {
    setChildForms(childForms.map((form, formIndex) => (formIndex === index ? { ...form, [field]: value } : form)))
  }

  /**
   * 하위메뉴 입력값 검증
   * @Author SeungHyeon.Kang
   * @return
   */
  const validateChildForms = () => {
    const hasEmptyRequired = childForms.some((form) => !form.menuName.trim() || !form.menuUrlx.trim())
    if (hasEmptyRequired) {
      setError('하위메뉴명과 URL을 입력해 주세요.')
      return false
    }
    return true
  }

  /**
   * 하위메뉴 일괄 저장
   * @Author SeungHyeon.Kang
   * @return
   */
  const saveAllChildMenus = async () => {
    if (childForms.length === 0 || !validateChildForms()) return
    setSaving(true)
    setError(null)
    try {
      const results = await Promise.all(childForms.map((form) => saveMenuApi(form, false)))
      alert(results[results.length - 1]?.message ?? '저장했습니다.')
      await openMenuDetailPage(menuForm.menuNumb, menuForm.subxNumb)
      await loadSidebarMenuList()
      setChildForms([])
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : '하위메뉴 저장 중 오류가 발생했습니다.')
    } finally {
      setSaving(false)
    }
  }

  /**
   * 공통코드 선택
   * @Author SeungHyeon.Kang
   * @param master
   * @return
   */
  const selectCodeMaster = (master: CodeMaster) => {
    movePath(`${CODE_DETAIL_PREFIX}/${master.commCode}`)
  }

  /**
   * 선택된 공통코드의 세부코드 로드
   * @Author SeungHyeon.Kang
   * @param master
   * @return
   */
  const loadSelectedCodeDetails = async (master: CodeMaster) => {
    setSelectedMaster(master)
    setMasterEditForm(master)
    setDetailForms([])
    const details = await getDetailCodes(master.commCode)
    setDetailCodes(details)
    setDetailEditForms(details.map(toDetailCodeForm))
  }

  /**
   * 공통코드 중복 검사
   * @Author SeungHyeon.Kang
   * @return
   */
  const checkCommCodeDuplicate = async () => {
    const commCode = masterForm.commCode.trim()
    if (!commCode) {
      setDuplicateCheckedCode('')
      setDuplicateAvailable(false)
      return
    }
    const duplicated = await checkMasterDuplicate(commCode)
    setDuplicateCheckedCode(commCode)
    setDuplicateAvailable(!duplicated)
    setError(null)
  }

  /**
   * 공통코드 저장 처리
   * @Author SeungHyeon.Kang
   * @param event
   * @return
   */
  const saveCommCode = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    if (!duplicateAvailable || duplicateCheckedCode !== masterForm.commCode.trim()) {
      setError('공통코드 중복검사를 먼저 완료해 주세요.')
      return
    }
    setSaving(true)
    try {
      const result = await createCodeMaster({ ...masterForm, commCode: masterForm.commCode.trim() })
      alert(result.message)
      const saved = result.data
      setCodeMasters(await getCodeMasters())
      setSelectedMaster(saved)
      setMasterEditForm(saved)
      setDetailCodes([])
      setDetailEditForms([])
      setShowMasterForm(false)
      resetMasterForm()
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : '공통코드 등록 중 오류가 발생했습니다.')
    } finally {
      setSaving(false)
    }
  }

  /**
   * 세부코드 입력 폼 추가
   * @Author SeungHyeon.Kang
   * @return
   */
  const addDetailInput = () => setDetailForms([...detailForms, emptyDetailForm()])

  /**
   * 신규 세부코드 입력값 변경
   * @Author SeungHyeon.Kang
   * @param index
   * @param field
   * @param value
   * @return
   */
  const changeDetailForm = (index: number, field: keyof DetailCodeForm, value: string) => {
    setDetailForms(detailForms.map((form, formIndex) => (formIndex === index ? { ...form, [field]: value } : form)))
  }

  /**
   * 공통코드 수정 저장
   * @Author SeungHyeon.Kang
   * @return
   */
  const saveMasterEditForm = async () => {
    if (!masterEditForm) return
    setSaving(true)
    setError(null)
    try {
      const result = await updateCodeMaster(masterEditForm.commCode, masterEditForm)
      alert(result.message)
      setSelectedMaster(result.data)
      setMasterEditForm(result.data)
      setCodeMasters(await getCodeMasters())
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : '공통코드 수정 중 오류가 발생했습니다.')
    } finally {
      setSaving(false)
    }
  }

  /**
   * 기존 세부코드 입력값 변경
   * @Author SeungHyeon.Kang
   * @param index
   * @param field
   * @param value
   * @return
   */
  const changeDetailEditForm = (index: number, field: keyof DetailCodeForm, value: string) => {
    setDetailEditForms(detailEditForms.map((form, formIndex) => (formIndex === index ? { ...form, [field]: value } : form)))
  }

  /**
   * 세부코드 저장 요청값 변환
   * @Author SeungHyeon.Kang
   * @param form
   * @return
   */
  const toDetailPayload = (form: DetailCodeForm): DetailCodePayload => ({
    comdCode: form.comdCode.trim(),
    comdName: form.comdName.trim(),
    codeExpl: form.codeExpl.trim(),
    opt1Code: form.opt1Code.trim(),
    opt1Name: form.opt1Name.trim(),
    opt2Code: form.opt2Code.trim(),
    opt2Name: form.opt2Name.trim(),
    opt3Code: form.opt3Code.trim(),
    opt3Name: form.opt3Name.trim(),
    opt4Code: form.opt4Code.trim(),
    opt4Name: form.opt4Name.trim(),
    sortOrdr: Number(form.sortOrdr),
    useeYsno: form.useeYsno,
  })

  /**
   * 신규 세부코드 입력값 검증
   * @Author SeungHyeon.Kang
   * @return
   */
  const validateNewDetailForms = () => {
    const existingCodes = detailCodes.map((detail) => detail.comdCode)
    const screenCodes = detailForms.map((detail) => detail.comdCode.trim()).filter(Boolean)
    const hasEmptyRequired = detailForms.some((detail) => !detail.comdCode.trim() || !detail.comdName.trim())
    const hasDuplicatedCode = screenCodes.some((code, index) => screenCodes.indexOf(code) !== index || existingCodes.includes(code))
    if (hasEmptyRequired) {
      setError('세부코드와 세부코드명을 입력해 주세요.')
      return false
    }
    if (hasDuplicatedCode) {
      setError('같은 공통코드 안에서는 세부코드를 중복할 수 없습니다.')
      return false
    }
    return true
  }

  /**
   * 신규 세부코드 일괄 저장
   * @Author SeungHyeon.Kang
   * @return
   */
  const saveAllDetailCodes = async () => {
    if (!selectedMaster || detailForms.length === 0 || !validateNewDetailForms()) return
    setSaving(true)
    setError(null)
    try {
      const results = await Promise.all(detailForms.map((form) => createDetailCode(selectedMaster.commCode, toDetailPayload({ ...form, comdCode: form.comdCode.trim() }))))
      alert(results[results.length - 1]?.message ?? '저장했습니다.')
      await loadSelectedCodeDetails(selectedMaster)
      setDetailForms([])
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : '세부코드 등록 중 오류가 발생했습니다.')
    } finally {
      setSaving(false)
    }
  }

  /**
   * 기존 세부코드 일괄 수정
   * @Author SeungHyeon.Kang
   * @return
   */
  const saveAllDetailEditCodes = async () => {
    if (!selectedMaster) return
    if (detailEditForms.some((form) => !form.comdName.trim())) {
      setError('세부코드명을 입력해 주세요.')
      return
    }
    setSaving(true)
    setError(null)
    try {
      const results = await Promise.all(detailEditForms.map((form) => updateDetailCode(selectedMaster.commCode, form.comdCode, toDetailPayload(form))))
      alert(results[results.length - 1]?.message ?? '수정했습니다.')
      await loadSelectedCodeDetails(selectedMaster)
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : '세부코드 수정 중 오류가 발생했습니다.')
    } finally {
      setSaving(false)
    }
  }

  /**
   * 공통코드 등록 입력값 변경
   * @Author SeungHyeon.Kang
   * @param form
   * @return
   */
  const changeMasterForm = (form: CodeMaster) => {
    setMasterForm(form)
    setDuplicateCheckedCode('')
    setDuplicateAvailable(false)
  }

  if (checkingSession) return <main className="login-page"><div className="panel">세션을 확인하고 있습니다.</div></main>

  if (!admin || currentPath === LOGIN_PATH) {
    return (
      <LoginPage
        admnIdxx={admnIdxx}
        passWord={passWord}
        submitting={submitting}
        error={error}
        onChangeAdmnIdxx={setAdmnIdxx}
        onChangePassWord={setPassWord}
        onSubmit={handleLogin}
      />
    )
  }

  return (
    <AdminLayout admin={admin} menus={menus} currentPath={currentPath} activePath={activeMenuPath} error={error} onMovePath={movePath} onLogout={handleLogout}>
      {isMenuListPage && <MenuListPage menuRows={menuRows} permission={menuPermission} useeYsnoCodes={useeYsnoCodes} onMovePath={movePath} onDelete={(menu) => void deleteMenu(menu)} />}
      {(isMenuDetailPage || isMenuNewPage) && (
        <MenuDetailPage
          isNewPage={isMenuNewPage}
          pageTitle={isMenuNewPage ? '메뉴 등록' : `${activeMenuName || '메뉴관리'} 상세`}
          saving={saving}
          menuForm={menuForm}
          menuDetail={menuDetail}
          childForms={childForms}
          subMenus={subMenus}
          authCodes={authCodes}
          useeYsnoCodes={useeYsnoCodes}
          permission={menuPermission}
          onMovePath={movePath}
          onChangeMenuForm={(field, value) => setMenuForm({ ...menuForm, [field]: value })}
          onChangeChildForm={changeChildForm}
          onSubmit={saveMenu}
          onAddChildForm={addChildForm}
          onSaveAllChildMenus={() => void saveAllChildMenus()}
          onDelete={(menu) => void deleteMenu(menu)}
        />
      )}
      {isCodeListPage && <CodeListPage codeMasters={codeMasters} permission={codePermission} useeYsnoCodes={useeYsnoCodes} onSelect={selectCodeMaster} onOpenRegister={() => setShowMasterForm(true)} />}
      {isCodeDetailPage && (
        <CodeDetailPage
          selectedMaster={selectedMaster}
          pageTitle={`${activeMenuName || '코드관리'} 상세`}
          masterEditForm={masterEditForm}
          detailCodes={detailCodes}
          detailEditForms={detailEditForms}
          detailForms={detailForms}
          permission={codePermission}
          useeYsnoCodes={useeYsnoCodes}
          saving={saving}
          onMovePath={movePath}
          onChangeMasterForm={setMasterEditForm}
          onSaveMasterForm={() => void saveMasterEditForm()}
          onAddDetailInput={addDetailInput}
          onChangeDetailEditForm={changeDetailEditForm}
          onChangeDetailForm={changeDetailForm}
          onSaveAllDetailEditCodes={() => void saveAllDetailEditCodes()}
          onSaveAllDetailCodes={() => void saveAllDetailCodes()}
        />
      )}
      {showMasterForm && (
        <CodeMasterModal
          masterForm={masterForm}
          useeYsnoCodes={useeYsnoCodes}
          saving={saving}
          duplicateAvailable={duplicateAvailable}
          duplicateCheckedCode={duplicateCheckedCode}
          onChange={changeMasterForm}
          onClose={() => { setShowMasterForm(false); resetMasterForm() }}
          onCheckDuplicate={() => void checkCommCodeDuplicate()}
          onSubmit={saveCommCode}
        />
      )}
    </AdminLayout>
  )
}

export default App
