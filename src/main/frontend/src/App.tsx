import { useEffect, useEffectEvent, useMemo, useState } from 'react'
import type { FormEvent } from 'react'
import { getAdminSession, loginAdmin, logoutAdmin } from './api/adminApi'
import { checkAlimTempDuplicate, getAlimTempDetail, getAlimTempList, saveAlimTempApi } from './api/alimTempApi'
import { checkMasterDuplicate, createCodeMaster, createDetailCode, getCodeList, getCodeMaster, getCodeMasters, getDetailCodes, updateCodeMaster, updateDetailCode } from './api/codeApi'
import { deleteMenuApi, getMenuDetail, getMenuMngList, getSidebarMenus, getSubMenus, saveMenuApi } from './api/menuApi'
import './App.css'
import { DEFAULT_USEE_YSNO, ALIM_SITU, COMM_YSNO } from './constants/codes'
import { ADMIN_AUTH_MANAGE_PATH, ALIM_TEMP_DETAIL_PREFIX, ALIM_TEMP_LIST_PATH, ALIM_TEMP_NEW_PATH, AUTH_GROUP_DETAIL_PREFIX, AUTH_GROUP_LIST_PATH, AUTH_GROUP_NEW_PATH, CODE_DETAIL_PREFIX, CODE_LIST_PATH, CURRENT_USER_DETAIL_PREFIX, CURRENT_USER_LIST_PATH, HOME_PATH, LOGIN_PATH, MENU_DETAIL_PREFIX, MENU_LIST_PATH, MENU_NEW_PATH, POPUP_CONTENT_DETAIL_PREFIX, POPUP_CONTENT_LIST_PATH, POPUP_CONTENT_NEW_PATH, SCHEDULE_LOG_DETAIL_PREFIX, SCHEDULE_LOG_LIST_PATH, USER_MENU_DETAIL_PREFIX, USER_MENU_LIST_PATH, USER_MENU_NEW_PATH } from './constants/routes'
import { AdminLayout } from './components/AdminLayout'
import { LoginPage } from './pages/LoginPage'
import { AlimTempDetailPage } from './pages/alim/AlimTempDetailPage'
import { AlimTempListPage } from './pages/alim/AlimTempListPage'
import { CodeDetailPage } from './pages/code/CodeDetailPage'
import { CodeListPage } from './pages/code/CodeListPage'
import { CodeMasterModal } from './pages/code/CodeMasterModal'
import { MenuDetailPage } from './pages/menu/MenuDetailPage'
import { MenuListPage } from './pages/menu/MenuListPage'
import { UserMenuManagePage } from './pages/userMenu/UserMenuManagePage'
import { AuthGroupManagePage } from './pages/authGroup/AuthGroupManagePage'
import { AdminAuthManagePage } from './pages/adminAuth/AdminAuthManagePage'
import { ScheduleLogListPage } from './pages/scheduleLog/ScheduleLogListPage'
import { ScheduleLogDetailPage } from './pages/scheduleLog/ScheduleLogDetailPage'
import { PopupContentManagePage } from './pages/popup/PopupContentManagePage'
import { CurrentUserListPage } from './pages/currentUser/CurrentUserListPage'
import { CurrentUserDetailPage } from './pages/currentUser/CurrentUserDetailPage'
import type { AdminSession } from './types/admin'
import type { AlimTemp, AlimTempForm } from './types/alim'
import type { Code, CodeMaster, DetailCodeForm, DetailCodePayload } from './types/code'
import type { Menu, MenuForm } from './types/menu'
import type { PageData } from './types/common'
import { emptyDetailForm, emptyMenuForm, toDetailCodeForm, toMenuForm } from './utils/forms'

const emptyPageData = <T,>(): PageData<T> => ({ items: [], totalCount: 0, pageNumber: 1, pageSize: 20, totalPages: 0 })

/**
 * 관리자 프론트 루트 컴포넌트
 * @Author SeungHyeon.Kang
 * @return
 */
function App() {
  const [admin, setAdmin] = useState<AdminSession | null>(null)
  const [menus, setMenus] = useState<Menu[]>([])
  const [menuRows, setMenuRows] = useState<Menu[]>([])
  const [menuPageData, setMenuPageData] = useState<PageData<Menu>>(emptyPageData())
  const [menuDetail, setMenuDetail] = useState<Menu | null>(null)
  const [subMenus, setSubMenus] = useState<Menu[]>([])
  const [alimSituCodes, setAlimSituCodes] = useState<Code[]>([])
  const [useeYsnoCodes, setUseeYsnoCodes] = useState<Code[]>([])
  const [menuForm, setMenuForm] = useState<MenuForm>(emptyMenuForm())
  const [childForms, setChildForms] = useState<MenuForm[]>([])
  const [codeMasters, setCodeMasters] = useState<CodeMaster[]>([])
  const [codePageData, setCodePageData] = useState<PageData<CodeMaster>>(emptyPageData())
  const [selectedMaster, setSelectedMaster] = useState<CodeMaster | null>(null)
  const [masterEditForm, setMasterEditForm] = useState<CodeMaster | null>(null)
  const [detailCodes, setDetailCodes] = useState<Code[]>([])
  const [detailEditForms, setDetailEditForms] = useState<DetailCodeForm[]>([])
  const [showMasterForm, setShowMasterForm] = useState(false)
  const [masterForm, setMasterForm] = useState<CodeMaster>({ commCode: '', codeName: '', codeExpl: '', useeYsno: DEFAULT_USEE_YSNO, regiAdmn: null, regiAdmnName: null, regiDate: null, updtAdmn: null, updtAdmnName: null, updtDate: null })
  const [duplicateCheckedCode, setDuplicateCheckedCode] = useState('')
  const [duplicateAvailable, setDuplicateAvailable] = useState(false)
  const [detailForms, setDetailForms] = useState<DetailCodeForm[]>([])
  const [alimTemps, setAlimTemps] = useState<AlimTemp[]>([])
  const [alimPageData, setAlimPageData] = useState<PageData<AlimTemp>>(emptyPageData())
  const [alimTempDetail, setAlimTempDetail] = useState<AlimTemp | null>(null)
  const [alimTempForm, setAlimTempForm] = useState<AlimTempForm>({ alimSitu: '', tempCode: '', tempTitl: '', alimTitl: '', tempCont: '', linkUrlx: '', useeYsno: DEFAULT_USEE_YSNO })
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

  const alimTempDetailKey = useMemo(() => {
    if (!currentPath.startsWith(ALIM_TEMP_DETAIL_PREFIX)) return null
    const [, , , , , alimSitu, tempCode] = currentPath.split('/')
    return alimSitu && tempCode ? { alimSitu: decodeURIComponent(alimSitu), tempCode: decodeURIComponent(tempCode) } : null
  }, [currentPath])

  const scheduleLogDetailKey = useMemo(() => {
    if (!currentPath.startsWith(`${SCHEDULE_LOG_DETAIL_PREFIX}/`)) return null
    const runxNumb = Number(currentPath.slice(SCHEDULE_LOG_DETAIL_PREFIX.length + 1))
    return Number.isInteger(runxNumb) && runxNumb > 0 ? runxNumb : null
  }, [currentPath])

  const currentUserDetailKey = useMemo(() => {
    if (!currentPath.startsWith(`${CURRENT_USER_DETAIL_PREFIX}/`)) return null
    const userNumb = Number(currentPath.slice(CURRENT_USER_DETAIL_PREFIX.length + 1))
    return Number.isInteger(userNumb) && userNumb > 0 ? userNumb : null
  }, [currentPath])

  const isMenuListPage = currentPath === MENU_LIST_PATH
  const isMenuNewPage = currentPath === MENU_NEW_PATH || currentPath.startsWith(`${MENU_NEW_PATH}/`)
  const isMenuDetailPage = detailKey !== null
  const isCodeListPage = currentPath === CODE_LIST_PATH
  const isCodeDetailPage = Boolean(codeDetailKey)
  const isAlimTempListPage = currentPath === ALIM_TEMP_LIST_PATH
  const isAlimTempNewPage = currentPath === ALIM_TEMP_NEW_PATH
  const isAlimTempDetailPage = alimTempDetailKey !== null
  const isUserMenuPage = currentPath === USER_MENU_LIST_PATH || currentPath === USER_MENU_NEW_PATH || currentPath.startsWith(USER_MENU_DETAIL_PREFIX)
  const isAuthGroupPage = currentPath === AUTH_GROUP_LIST_PATH || currentPath === AUTH_GROUP_NEW_PATH || currentPath.startsWith(AUTH_GROUP_DETAIL_PREFIX)
  const isAdminAuthManagePage = currentPath === ADMIN_AUTH_MANAGE_PATH
  const isScheduleLogListPage = currentPath === SCHEDULE_LOG_LIST_PATH
  const isScheduleLogDetailPage = scheduleLogDetailKey !== null
  const isCurrentUserListPage = currentPath === CURRENT_USER_LIST_PATH
  const isCurrentUserDetailPage = currentUserDetailKey !== null
  const isPopupContentPage = currentPath === POPUP_CONTENT_LIST_PATH
    || currentPath === POPUP_CONTENT_NEW_PATH
    || currentPath.startsWith(`${POPUP_CONTENT_DETAIL_PREFIX}/`)

  const activeMenuPath = useMemo(() => {
    if (currentPath === MENU_NEW_PATH || currentPath.startsWith(`${MENU_NEW_PATH}/`) || currentPath.startsWith(MENU_DETAIL_PREFIX)) return MENU_LIST_PATH
    if (currentPath === USER_MENU_NEW_PATH || currentPath.startsWith(USER_MENU_DETAIL_PREFIX)) return USER_MENU_LIST_PATH
    if (currentPath.startsWith(CODE_DETAIL_PREFIX)) return CODE_LIST_PATH
    if (currentPath === ALIM_TEMP_NEW_PATH || currentPath.startsWith(ALIM_TEMP_DETAIL_PREFIX)) return ALIM_TEMP_LIST_PATH
    if (currentPath === POPUP_CONTENT_NEW_PATH || currentPath.startsWith(POPUP_CONTENT_DETAIL_PREFIX)) return POPUP_CONTENT_LIST_PATH
    if (currentPath === AUTH_GROUP_NEW_PATH || currentPath.startsWith(AUTH_GROUP_DETAIL_PREFIX)) return AUTH_GROUP_LIST_PATH
    if (currentPath.startsWith(SCHEDULE_LOG_DETAIL_PREFIX)) return SCHEDULE_LOG_LIST_PATH
    if (currentPath.startsWith(CURRENT_USER_DETAIL_PREFIX)) return CURRENT_USER_LIST_PATH
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
  /**
   * 알림상황 코드 목록 로드
   * @Author SeungHyeon.Kang
   * @return
   */
  const loadAlimSituCodeList = async () => {
    const codes = await getCodeList(ALIM_SITU)
    setAlimSituCodes(codes)
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
    const initialPath = window.location.pathname
    getAdminSession()
      .then(async (session) => {
        setAdmin(session)
        if (!session) {
          movePath(LOGIN_PATH)
          return
        }
        await loadSidebarMenuList()
        movePath(initialPath.startsWith('/sadari/adm') && initialPath !== LOGIN_PATH ? initialPath : HOME_PATH)
      })
      .catch((err: unknown) => {
        setError(err instanceof Error ? err.message : '세션 확인 중 오류가 발생했습니다.')
        movePath(LOGIN_PATH)
      })
      .finally(() => setCheckingSession(false))
  }, [])

  /**
   * 메뉴관리 목록 화면 열기
   * @Author SeungHyeon.Kang
   * @return
   */
  const openMenuListPage = async (pageNumber = 1) => {
    setError(null)
    setChildForms([])
    setMenuDetail(null)
    const [pageData] = await Promise.all([getMenuMngList(pageNumber), loadUseeYsnoCodeList()])
    setMenuPageData(pageData)
    setMenuRows(pageData.items)
  }

  /**
   * 메뉴 등록 화면 열기
   * @Author SeungHyeon.Kang
   * @param parentNumb
   * @return
   */
  const openMenuNewPage = async (parentNumb: string) => {
    setError(null)
    await loadUseeYsnoCodeList()
    setMenuDetail(null)
    setSubMenus([])
    setChildForms([])
    setMenuForm(emptyMenuForm(parentNumb))
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
    const [detail, children] = await Promise.all([getMenuDetail(menuNumb, subxNumb), getSubMenus(menuNumb), loadUseeYsnoCodeList()])
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
  const openCodeListPage = async (pageNumber = 1) => {
    setError(null)
    const [pageData] = await Promise.all([getCodeMasters(pageNumber), loadUseeYsnoCodeList()])
    setCodePageData(pageData)
    setCodeMasters(pageData.items)
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
    const [master, details] = await Promise.all([getCodeMaster(commCode), getDetailCodes(commCode), loadUseeYsnoCodeList()])
    setSelectedMaster(master)
    setMasterEditForm(master)
    setDetailCodes(details)
    setDetailEditForms(details.map(toDetailCodeForm))
    setDetailForms([])
  }

  /**
   * 알림 템플릿 목록 화면 열기
   * @Author SeungHyeon.Kang
   * @return
   */
  const openAlimTempListPage = async (pageNumber = 1) => {
    setError(null)
    const [pageData] = await Promise.all([getAlimTempList(pageNumber), loadUseeYsnoCodeList()])
    setAlimPageData(pageData)
    setAlimTemps(pageData.items)
    setAlimTempDetail(null)
  }

  /**
   * 알림 템플릿 등록 화면 열기
   * @Author SeungHyeon.Kang
   * @return
   */
  const openAlimTempNewPage = async () => {
    setError(null)
    const [situCodes] = await Promise.all([loadAlimSituCodeList(), loadUseeYsnoCodeList()])
    setAlimTempDetail(null)
    setAlimTempForm({ alimSitu: situCodes[0]?.comdCode ?? '', tempCode: '', tempTitl: '', alimTitl: '', tempCont: '', linkUrlx: '', useeYsno: DEFAULT_USEE_YSNO })
  }

  /**
   * 알림 템플릿 상세 화면 열기
   * @Author SeungHyeon.Kang
   * @param alimSitu
   * @param tempCode
   * @return
   */
  const openAlimTempDetailPage = async (alimSitu: string, tempCode: string) => {
    setError(null)
    const [detail] = await Promise.all([getAlimTempDetail(alimSitu, tempCode), loadAlimSituCodeList(), loadUseeYsnoCodeList()])
    setAlimTempDetail(detail)
    setAlimTempForm({ alimSitu: detail.alimSitu, tempCode: detail.tempCode, tempTitl: detail.tempTitl, alimTitl: detail.alimTitl ?? '', tempCont: detail.tempCont, linkUrlx: detail.linkUrlx, useeYsno: detail.useeYsno ?? DEFAULT_USEE_YSNO })
  }

  /**
   * 현재 경로에 해당하는 기존 관리자 관리 화면 데이터를 조회한다
   * @Author SeungHyeon.Kang
   * @return
   */
  const loadCurrentAdminPage = useEffectEvent(() => {
    if (isMenuListPage) void openMenuListPage()
    else if (isMenuNewPage) void openMenuNewPage(parentMenuNumb)
    else if (detailKey) void openMenuDetailPage(detailKey.menuNumb, detailKey.subxNumb)
    else if (isCodeListPage) void openCodeListPage()
    else if (isCodeDetailPage) void openCodeDetailPage(codeDetailKey)
    else if (isAlimTempListPage) void openAlimTempListPage()
    else if (isAlimTempNewPage) void openAlimTempNewPage()
    else if (alimTempDetailKey) void openAlimTempDetailPage(alimTempDetailKey.alimSitu, alimTempDetailKey.tempCode)
  })

  useEffect(() => {
    if (!admin) return
    const loadTimer = window.setTimeout(loadCurrentAdminPage, 0)
    return () => window.clearTimeout(loadTimer)
  }, [admin, currentPath])

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
   * 메뉴 삭제 처리
   * @Author SeungHyeon.Kang
   * @param menu
   * @return
   */
  const deleteMenu = async (menu: Pick<Menu, 'menuNumb' | 'subxNumb'>) => {
    await deleteMenuApi(menu)
    await loadSidebarMenuList()
    if (isMenuDetailPage && menu.menuNumb === menuForm.menuNumb && menu.subxNumb === menuForm.subxNumb) movePath(MENU_LIST_PATH)
    else if (isMenuDetailPage) await openMenuDetailPage(menuForm.menuNumb, menuForm.subxNumb)
    else await openMenuListPage(menuPageData.pageNumber)
  }

  /**
   * 기존 하위메뉴 입력값 변경
   * @Author SeungHyeon.Kang
   * @param index
   * @param field
   * @param value
   * @return
   */
  const changeSubMenu = (index: number, field: 'menuName' | 'menuUrlx' | 'useeYsno' | 'sortOrdr', value: string) => {
    setSubMenus(subMenus.map((menu, menuIndex) => (
      menuIndex === index
        ? { ...menu, [field]: field === 'sortOrdr' ? Number(value) : value }
        : menu
    )))
  }

  /**
   * 기존 하위메뉴 일괄 수정
   * @Author SeungHyeon.Kang
   * @return
   */
  const saveAllSubMenus = async () => {
    if (subMenus.some((menu) => !menu.menuName.trim() || !menu.menuUrlx.trim())) {
      alert('하위메뉴명과 URL을 입력해 주세요.')
      return
    }
    setSaving(true)
    setError(null)
    try {
      const results = await Promise.all(subMenus.map((menu) => saveMenuApi(toMenuForm(menu), true)))
      alert(results.at(-1)?.message ?? '수정했습니다.')
      await openMenuDetailPage(menuForm.menuNumb, menuForm.subxNumb)
      await loadSidebarMenuList()
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : '하위메뉴 수정 중 오류가 발생했습니다.')
    } finally {
      setSaving(false)
    }
  }

  /**
   * 메뉴 상세 전체 정보 저장
   * @Author SeungHyeon.Kang
   * @return
   */
  const saveAllMenuDetail = async () => {
    if (!menuForm.menuName.trim() || !menuForm.menuUrlx.trim()) {
      alert('메뉴명과 URL을 입력해 주세요.')
      return
    }
    if (subMenus.some((menu) => !menu.menuName.trim() || !menu.menuUrlx.trim())
      || childForms.some((menu) => !menu.menuName.trim() || !menu.menuUrlx.trim())) {
      alert('하위메뉴명과 URL을 입력해 주세요.')
      return
    }
    setSaving(true)
    setError(null)
    try {
      const result = await saveMenuApi(menuForm, isMenuDetailPage)
      if (isMenuDetailPage) {
        await Promise.all(subMenus.map((menu) => saveMenuApi(toMenuForm(menu), true)))
        await Promise.all(childForms.map((menu) => saveMenuApi(menu, false)))
        await openMenuDetailPage(menuForm.menuNumb, menuForm.subxNumb)
        await loadSidebarMenuList()
      } else {
        movePath(`${MENU_DETAIL_PREFIX}/${result.data.menuNumb}/${result.data.subxNumb}`)
      }
      alert(result.message)
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : '메뉴 저장 중 오류가 발생했습니다.')
    } finally {
      setSaving(false)
    }
  }

  /**
   * 코드 상세 전체 정보 저장
   * @Author SeungHyeon.Kang
   * @return
   */
  const saveAllCodeDetail = async () => {
    if (!selectedMaster || !masterEditForm) return
    if (!masterEditForm.codeName.trim() || detailEditForms.some((form) => !form.comdName.trim())
      || detailForms.some((form) => !form.comdCode.trim() || !form.comdName.trim())) {
      alert('필수값을 입력해 주세요.')
      return
    }
    if (!validateNewDetailForms()) return
    setSaving(true)
    setError(null)
    try {
      const masterResult = await updateCodeMaster(masterEditForm.commCode, masterEditForm)
      await Promise.all(detailEditForms.map((form) => updateDetailCode(selectedMaster.commCode, form.comdCode, toDetailPayload(form))))
      await Promise.all(detailForms.map((form) => createDetailCode(selectedMaster.commCode, toDetailPayload(form))))
      alert(masterResult.message)
      await openCodeDetailPage(selectedMaster.commCode)
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : '코드 상세 저장 중 오류가 발생했습니다.')
    } finally {
      setSaving(false)
    }
  }

  /**
   * 하위메뉴 입력 폼 추가
   * @Author SeungHyeon.Kang
   * @return
   */
  const addChildForm = () => {
    setChildForms([...childForms, emptyMenuForm(menuForm.menuNumb)])
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
      const pageData = await getCodeMasters(codePageData.pageNumber)
      setCodePageData(pageData)
      setCodeMasters(pageData.items)
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
   * 저장 전 세부코드 입력 폼 삭제
   * @Author SeungHyeon.Kang
   * @param index
   * @return
   */
  const removeDetailInput = (index: number) => {
    setDetailForms(detailForms.filter((_, formIndex) => formIndex !== index))
  }

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
      const pageData = await getCodeMasters(codePageData.pageNumber)
      setCodePageData(pageData)
      setCodeMasters(pageData.items)
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

  /**
   * 알림 템플릿 저장값 검증
   * @Author SeungHyeon.Kang
   * @return
   */
  const validateAlimTempForm = () => {
    const hasRequired = alimTempForm.alimSitu.trim() && alimTempForm.tempCode.trim() && alimTempForm.tempTitl.trim() && alimTempForm.tempCont.trim() && alimTempForm.linkUrlx.trim()
    // 필수값이 하나라도 없으면 저장 요청을 보내지 않는다
    if (!hasRequired) {
      setError('알림상황, 템플릿코드, 관리용 제목, 템플릿 내용, 이동 URL을 입력해 주세요.')
      return false
    }
    // 템플릿 코드는 영문 대문자와 밑줄만 허용한다
    if (!/^[A-Z_]+$/.test(alimTempForm.tempCode.trim())) {
      setError('템플릿코드는 영문 대문자와 _만 입력할 수 있습니다.')
      return false
    }
    return true
  }

  /**
   * 알림 템플릿 저장 처리
   * @Author SeungHyeon.Kang
   * @param event
   * @return
   */
  const saveAlimTemp = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    if (!validateAlimTempForm()) return
    setSaving(true)
    setError(null)
    try {
      const oldAlimSitu = alimTempDetail?.alimSitu
      const oldTempCode = alimTempDetail?.tempCode
      const changedKey = isAlimTempNewPage || oldAlimSitu !== alimTempForm.alimSitu || oldTempCode !== alimTempForm.tempCode.trim()
      // 신규 등록이거나 복합키가 변경된 경우에는 같은 알림 상황 안의 템플릿 코드 중복 여부를 먼저 확인한다
      if (changedKey && await checkAlimTempDuplicate(alimTempForm.alimSitu, alimTempForm.tempCode.trim())) {
        setError('같은 알림상황 안에서 이미 사용 중인 템플릿코드입니다.')
        return
      }
      const result = await saveAlimTempApi(alimTempForm, isAlimTempDetailPage, oldAlimSitu, oldTempCode)
      alert(result.message)
      const saved = result.data
      movePath(`${ALIM_TEMP_DETAIL_PREFIX}/${encodeURIComponent(saved.alimSitu)}/${encodeURIComponent(saved.tempCode)}`)
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : '알림 템플릿 저장 중 오류가 발생했습니다.')
    } finally {
      setSaving(false)
    }
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
      {isMenuListPage && <MenuListPage menuRows={menuRows} pageData={menuPageData} useeYsnoCodes={useeYsnoCodes} onPageChange={(pageNumber) => void openMenuListPage(pageNumber)} onMovePath={movePath} onDelete={(menu) => void deleteMenu(menu)} />}
      {(isMenuDetailPage || isMenuNewPage) && (
        <MenuDetailPage
          isNewPage={isMenuNewPage}
          pageTitle={isMenuNewPage ? '메뉴 등록' : `${activeMenuName || '메뉴관리'} 상세`}
          saving={saving}
          menuForm={menuForm}
          menuDetail={menuDetail}
          childForms={childForms}
          subMenus={subMenus}
          useeYsnoCodes={useeYsnoCodes}
          onMovePath={movePath}
          onChangeMenuForm={(field, value) => setMenuForm({ ...menuForm, [field]: value })}
          onChangeChildForm={changeChildForm}
          onChangeSubMenu={changeSubMenu}
          onSubmit={(event) => { event.preventDefault(); void saveAllMenuDetail() }}
          onAddChildForm={addChildForm}
          onSaveAllChildMenus={() => void saveAllChildMenus()}
          onSaveAllSubMenus={() => void saveAllSubMenus()}
          onSaveAll={() => void saveAllMenuDetail()}
          onDelete={(menu) => void deleteMenu(menu)}
        />
      )}
      {isUserMenuPage && <UserMenuManagePage currentPath={currentPath} onMovePath={movePath} onError={setError} />}
      {isAuthGroupPage && <AuthGroupManagePage currentPath={currentPath} onMovePath={movePath} onError={setError} />}
      {isAdminAuthManagePage && <AdminAuthManagePage onError={setError} />}
      {isScheduleLogListPage && <ScheduleLogListPage onMovePath={movePath} onError={setError} />}
      {isScheduleLogDetailPage && (
        <ScheduleLogDetailPage
          runxNumb={scheduleLogDetailKey}
          pageTitle={`${activeMenuName || '스케줄러 로그 확인'} 상세`}
          onMovePath={movePath}
          onError={setError}
        />
      )}
      {isCurrentUserListPage && <CurrentUserListPage onMovePath={movePath} onError={setError} />}
      {isCurrentUserDetailPage && (
        <CurrentUserDetailPage
          userNumb={currentUserDetailKey}
          adminAuthCode={admin?.authCode ?? ''}
          onMovePath={movePath}
          onError={setError}
        />
      )}
      {isPopupContentPage && <PopupContentManagePage currentPath={currentPath} onMovePath={movePath} onError={setError} />}
      {isCodeListPage && <CodeListPage codeMasters={codeMasters} pageData={codePageData} useeYsnoCodes={useeYsnoCodes} onPageChange={(pageNumber) => void openCodeListPage(pageNumber)} onSelect={selectCodeMaster} onOpenRegister={() => setShowMasterForm(true)} />}
      {isCodeDetailPage && (
        <CodeDetailPage
          selectedMaster={selectedMaster}
          pageTitle={`${activeMenuName || '코드관리'} 상세`}
          masterEditForm={masterEditForm}
          detailCodes={detailCodes}
          detailEditForms={detailEditForms}
          detailForms={detailForms}
          useeYsnoCodes={useeYsnoCodes}
          saving={saving}
          onMovePath={movePath}
          onChangeMasterForm={setMasterEditForm}
          onSaveMasterForm={() => void saveMasterEditForm()}
          onAddDetailInput={addDetailInput}
          onRemoveDetailInput={removeDetailInput}
          onChangeDetailEditForm={changeDetailEditForm}
          onChangeDetailForm={changeDetailForm}
          onSaveAllDetailEditCodes={() => void saveAllDetailEditCodes()}
          onSaveAllDetailCodes={() => void saveAllDetailCodes()}
          onSaveAll={() => void saveAllCodeDetail()}
        />
      )}
      {isAlimTempListPage && <AlimTempListPage alimTemps={alimTemps} pageData={alimPageData} useeYsnoCodes={useeYsnoCodes} onPageChange={(pageNumber) => void openAlimTempListPage(pageNumber)} onMovePath={movePath} />}
      {(isAlimTempDetailPage || isAlimTempNewPage) && (
        <AlimTempDetailPage
          isNewPage={isAlimTempNewPage}
          pageTitle={isAlimTempNewPage ? '알림 템플릿 등록' : `${activeMenuName || '알림 템플릿 관리'} 상세`}
          saving={saving}
          alimTempForm={alimTempForm}
          alimTempDetail={alimTempDetail}
          alimSituCodes={alimSituCodes}
          useeYsnoCodes={useeYsnoCodes}
          onMovePath={movePath}
          onChange={(field, value) => setAlimTempForm({ ...alimTempForm, [field]: value })}
          onSubmit={saveAlimTemp}
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
