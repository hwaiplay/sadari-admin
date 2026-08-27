import { useEffect, useEffectEvent, useMemo, useState } from 'react'
import type { FormEvent } from 'react'
import { getAdminSession, loginAdmin, logoutAdmin } from './api/adminApi'
import { checkAlimTempDuplicate, getAlimTempDetail, getAlimTempList, saveAlimTempApi } from './api/alimTempApi'
import { getAlimIconDetail, getAlimIconList, saveAlimIconApi } from './api/alimIconApi'
import { checkMasterDuplicate, createCodeMaster, createDetailCode, getCodeList, getCodeMaster, getCodeMasters, getDetailCodes, updateCodeMaster, updateDetailCode } from './api/codeApi'
import { deleteMenuApi, getMenuDetail, getMenuMngList, getSidebarMenus, getSubMenus, saveMenuApi } from './api/menuApi'
import './App.css'
import { DEFAULT_USEE_YSNO, ALIM_SITU, COMM_YSNO } from './constants/codes'
import { ADMIN_AUTH_MANAGE_PATH, ALIM_ICON_DETAIL_PREFIX, ALIM_ICON_LIST_PATH, ALIM_TEMP_DETAIL_PREFIX, ALIM_TEMP_LIST_PATH, ALIM_TEMP_NEW_PATH, AUTH_GROUP_DETAIL_PREFIX, AUTH_GROUP_LIST_PATH, AUTH_GROUP_NEW_PATH, CODE_DETAIL_PREFIX, CODE_LIST_PATH, COMPLAINT_DETAIL_PREFIX, COMPLAINT_LIST_PATH, CURRENT_USER_DETAIL_PREFIX, CURRENT_USER_LIST_PATH, HOME_PATH, LOGIN_PATH, MENU_DETAIL_PREFIX, MENU_LIST_PATH, MENU_NEW_PATH, NOTICE_DETAIL_PREFIX, NOTICE_LIST_PATH, NOTICE_NEW_PATH, POPUP_CONTENT_DETAIL_PREFIX, POPUP_CONTENT_LIST_PATH, POPUP_CONTENT_NEW_PATH, SCHEDULE_LOG_DETAIL_PREFIX, SCHEDULE_LOG_LIST_PATH, SERVICE_INFO_DETAIL_PREFIX, SERVICE_INFO_LIST_PATH, SERVICE_INFO_NEW_PATH, USER_MENU_DETAIL_PREFIX, USER_MENU_LIST_PATH, USER_MENU_NEW_PATH, USER_STATISTICS_PATH } from './constants/routes'
import { AdminLayout } from './components/AdminLayout'
import { LoginPage } from './pages/LoginPage'
import { AlimTempDetailPage } from './pages/alim/AlimTempDetailPage'
import { AlimTempListPage } from './pages/alim/AlimTempListPage'
import { AlimIconDetailPage } from './pages/alim/AlimIconDetailPage'
import { AlimIconListPage } from './pages/alim/AlimIconListPage'
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
import { DeletedSuspensionPage } from './pages/currentUser/DeletedSuspensionPage'
import { ComplaintListPage } from './pages/complaint/ComplaintListPage'
import { ComplaintDetailPage } from './pages/complaint/ComplaintDetailPage'
import { InquiryListPage } from './pages/inquiry/InquiryListPage'
import { InquiryDetailPage } from './pages/inquiry/InquiryDetailPage'
import { NoticeManagePage } from './pages/notice/NoticeManagePage'
import { ServiceInfoManagePage } from './pages/serviceInfo/ServiceInfoManagePage'
import { UserStatisticsPage } from './pages/statistics/UserStatisticsPage'
import type { AdminSession } from './types/admin'
import type { AlimIcon, AlimIconSearch, AlimTemp, AlimTempForm, AlimTempSearch } from './types/alim'
import type { Code, CodeMaster, CodeMasterSearch, DetailCodeForm, DetailCodePayload } from './types/code'
import type { Menu, MenuForm, MenuSearch } from './types/menu'
import type { PageData } from './types/common'
import { emptyDetailForm, emptyMenuForm, getNextSortOrdr, toDetailCodeForm, toMenuForm } from './utils/forms'
import { getListPageSnapshot, setListPageSnapshot } from './utils/search'
import { DELETED_SUSPENSION_PATH, INQUIRY_DETAIL_PREFIX, INQUIRY_LIST_PATH } from './constants/routes'

const emptyPageData = <T,>(): PageData<T> => ({ items: [], totalCount: 0, pageNumber: 1, pageSize: 20, totalPages: 0 })

const DEFAULT_MENU_SEARCH: MenuSearch = { keyword: '', useeYsno: '' }
const DEFAULT_CODE_SEARCH: CodeMasterSearch = { keyword: '', useeYsno: '' }
const DEFAULT_ALIM_SEARCH: AlimTempSearch = { keyword: '', alimSitu: '', useeYsno: '' }
const DEFAULT_ALIM_ICON_SEARCH: AlimIconSearch = { keyword: '', useeYsno: '' }

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
  const [codeAppliedSearch, setCodeAppliedSearch] = useState<CodeMasterSearch>(DEFAULT_CODE_SEARCH)
  const [selectedMaster, setSelectedMaster] = useState<CodeMaster | null>(null)
  const [masterEditForm, setMasterEditForm] = useState<CodeMaster | null>(null)
  const [detailCodes, setDetailCodes] = useState<Code[]>([])
  const [detailEditForms, setDetailEditForms] = useState<DetailCodeForm[]>([])
  const [selectedDetailCode, setSelectedDetailCode] = useState('')
  const [showMasterForm, setShowMasterForm] = useState(false)
  const [masterForm, setMasterForm] = useState<CodeMaster>({ commCode: '', codeName: '', codeExpl: '', useeYsno: DEFAULT_USEE_YSNO, regiAdmn: null, regiAdmnName: null, regiDate: null, updtAdmn: null, updtAdmnName: null, updtDate: null })
  const [duplicateCheckedCode, setDuplicateCheckedCode] = useState('')
  const [duplicateAvailable, setDuplicateAvailable] = useState(false)
  const [detailForms, setDetailForms] = useState<DetailCodeForm[]>([])
  const [alimTemps, setAlimTemps] = useState<AlimTemp[]>([])
  const [alimPageData, setAlimPageData] = useState<PageData<AlimTemp>>(emptyPageData())
  const [alimTempDetail, setAlimTempDetail] = useState<AlimTemp | null>(null)
  const [alimTempForm, setAlimTempForm] = useState<AlimTempForm>({ alimSitu: '', tempCode: '', tempTitl: '', alimTitl: '', tempCont: '', useeYsno: DEFAULT_USEE_YSNO })
  const [alimIcons, setAlimIcons] = useState<AlimIcon[]>([])
  const [alimIconPageData, setAlimIconPageData] = useState<PageData<AlimIcon>>(emptyPageData())
  const [alimIconDetail, setAlimIconDetail] = useState<AlimIcon | null>(null)
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
    // 코드 상세 경로가 아니면 조회 키를 만들지 않는다
    if (!currentPath.startsWith(`${CODE_DETAIL_PREFIX}/`)) return null
    // 공통코드와 선택한 세부코드를 안전하게 분리한다
    const pathSegments = currentPath.slice(CODE_DETAIL_PREFIX.length + 1).split('/')
    const commCode = pathSegments[0] ? decodeURIComponent(pathSegments[0]) : ''
    const comdCode = pathSegments[1] ? decodeURIComponent(pathSegments[1]) : ''
    // 공통코드가 있을 때만 상세 조회 키를 반환한다
    return commCode ? { commCode, comdCode } : null
  }, [currentPath])

  const alimTempDetailKey = useMemo(() => {
    if (!currentPath.startsWith(ALIM_TEMP_DETAIL_PREFIX)) return null
    const [, , , , , alimSitu, tempCode] = currentPath.split('/')
    return alimSitu && tempCode ? { alimSitu: decodeURIComponent(alimSitu), tempCode: decodeURIComponent(tempCode) } : null
  }, [currentPath])

  const alimIconDetailKey = useMemo(() => {
    if (!currentPath.startsWith(`${ALIM_ICON_DETAIL_PREFIX}/`)) return null
    const alimSitu = currentPath.slice(ALIM_ICON_DETAIL_PREFIX.length + 1)
    return alimSitu ? decodeURIComponent(alimSitu) : null
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

  const complaintDetailKey = useMemo(() => {
    if (!currentPath.startsWith(`${COMPLAINT_DETAIL_PREFIX}/`)) return null
    const cmplNumb = Number(currentPath.slice(COMPLAINT_DETAIL_PREFIX.length + 1))
    return Number.isInteger(cmplNumb) && cmplNumb > 0 ? cmplNumb : null
  }, [currentPath])

  const inquiryDetailKey = useMemo(() => {
    if (!currentPath.startsWith(`${INQUIRY_DETAIL_PREFIX}/`)) return null
    const inqrNumb = Number(currentPath.slice(INQUIRY_DETAIL_PREFIX.length + 1))
    return Number.isInteger(inqrNumb) && inqrNumb > 0 ? inqrNumb : null
  }, [currentPath])

  const isMenuListPage = currentPath === MENU_LIST_PATH
  const isMenuNewPage = currentPath === MENU_NEW_PATH || currentPath.startsWith(`${MENU_NEW_PATH}/`)
  const isMenuDetailPage = detailKey !== null
  const isCodeListPage = currentPath === CODE_LIST_PATH
  const isCodeDetailPage = codeDetailKey !== null
  const isAlimTempListPage = currentPath === ALIM_TEMP_LIST_PATH
  const isAlimTempNewPage = currentPath === ALIM_TEMP_NEW_PATH
  const isAlimTempDetailPage = alimTempDetailKey !== null
  const isAlimIconListPage = currentPath === ALIM_ICON_LIST_PATH
  const isAlimIconDetailPage = alimIconDetailKey !== null
  const isUserMenuPage = currentPath === USER_MENU_LIST_PATH || currentPath === USER_MENU_NEW_PATH || currentPath.startsWith(USER_MENU_DETAIL_PREFIX)
  const isAuthGroupPage = currentPath === AUTH_GROUP_LIST_PATH || currentPath === AUTH_GROUP_NEW_PATH || currentPath.startsWith(AUTH_GROUP_DETAIL_PREFIX)
  const isAdminAuthManagePage = currentPath === ADMIN_AUTH_MANAGE_PATH
  const isScheduleLogListPage = currentPath === SCHEDULE_LOG_LIST_PATH
  const isScheduleLogDetailPage = scheduleLogDetailKey !== null
  const isCurrentUserListPage = currentPath === CURRENT_USER_LIST_PATH
  const isCurrentUserDetailPage = currentUserDetailKey !== null
  const isDeletedSuspensionPage = currentPath === DELETED_SUSPENSION_PATH
  const isUserStatisticsPage = currentPath === USER_STATISTICS_PATH
  const isComplaintListPage = currentPath === COMPLAINT_LIST_PATH
  const isComplaintDetailPage = complaintDetailKey !== null
  const isInquiryListPage = currentPath === INQUIRY_LIST_PATH
  const isInquiryDetailPage = inquiryDetailKey !== null
  const isPopupContentPage = currentPath === POPUP_CONTENT_LIST_PATH
    || currentPath === POPUP_CONTENT_NEW_PATH
    || currentPath.startsWith(`${POPUP_CONTENT_DETAIL_PREFIX}/`)
  const isNoticePage = currentPath === NOTICE_LIST_PATH
    || currentPath === NOTICE_NEW_PATH
    || currentPath.startsWith(`${NOTICE_DETAIL_PREFIX}/`)
  const isServiceInfoPage = currentPath === SERVICE_INFO_LIST_PATH
    || currentPath === SERVICE_INFO_NEW_PATH
    || currentPath.startsWith(`${SERVICE_INFO_DETAIL_PREFIX}/`)

  const activeMenuPath = useMemo(() => {
    if (currentPath === MENU_NEW_PATH || currentPath.startsWith(`${MENU_NEW_PATH}/`) || currentPath.startsWith(MENU_DETAIL_PREFIX)) return MENU_LIST_PATH
    if (currentPath === USER_MENU_NEW_PATH || currentPath.startsWith(USER_MENU_DETAIL_PREFIX)) return USER_MENU_LIST_PATH
    if (currentPath.startsWith(CODE_DETAIL_PREFIX)) return CODE_LIST_PATH
    if (currentPath === ALIM_TEMP_NEW_PATH || currentPath.startsWith(ALIM_TEMP_DETAIL_PREFIX)) return ALIM_TEMP_LIST_PATH
    if (currentPath.startsWith(ALIM_ICON_DETAIL_PREFIX)) return ALIM_ICON_LIST_PATH
    if (currentPath === POPUP_CONTENT_NEW_PATH || currentPath.startsWith(POPUP_CONTENT_DETAIL_PREFIX)) return POPUP_CONTENT_LIST_PATH
    if (currentPath === AUTH_GROUP_NEW_PATH || currentPath.startsWith(AUTH_GROUP_DETAIL_PREFIX)) return AUTH_GROUP_LIST_PATH
    if (currentPath.startsWith(SCHEDULE_LOG_DETAIL_PREFIX)) return SCHEDULE_LOG_LIST_PATH
    if (currentPath.startsWith(CURRENT_USER_DETAIL_PREFIX)) return CURRENT_USER_LIST_PATH
    if (currentPath === DELETED_SUSPENSION_PATH) return CURRENT_USER_LIST_PATH
    if (currentPath.startsWith(COMPLAINT_DETAIL_PREFIX)) return COMPLAINT_LIST_PATH
    if (currentPath.startsWith(INQUIRY_DETAIL_PREFIX)) return INQUIRY_LIST_PATH
    if (currentPath === NOTICE_NEW_PATH || currentPath.startsWith(NOTICE_DETAIL_PREFIX)) return NOTICE_LIST_PATH
    if (currentPath === SERVICE_INFO_NEW_PATH || currentPath.startsWith(SERVICE_INFO_DETAIL_PREFIX)) return SERVICE_INFO_LIST_PATH
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
   *
   * @author SeungHyeon.Kang
   * @param pageNumber 조회할 페이지 번호
   * @param search 적용할 메뉴 검색 조건
   * @return 반환값이 없다
   */
  const openMenuListPage = async (pageNumber = 1, search = DEFAULT_MENU_SEARCH) => {
    setError(null)
    setChildForms([])
    setMenuDetail(null)
    const [pageData] = await Promise.all([getMenuMngList(pageNumber, search), loadUseeYsnoCodeList()])
    setMenuPageData(pageData)
    setMenuRows(pageData.items)
    // 상세 화면에서 돌아올 때 현재 메뉴 검색 결과를 복원하도록 조회 상태를 저장한다
    setListPageSnapshot(MENU_LIST_PATH, pageData.pageNumber, search)
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
   *
   * @author SeungHyeon.Kang
   * @param pageNumber 조회할 페이지 번호
   * @param search 적용할 공통코드 검색 조건
   * @return 반환값이 없다
   */
  const openCodeListPage = async (pageNumber = 1, search = DEFAULT_CODE_SEARCH) => {
    setError(null)
    const [pageData] = await Promise.all([getCodeMasters(pageNumber, search), loadUseeYsnoCodeList()])
    setCodePageData(pageData)
    setCodeMasters(pageData.items)
    setCodeAppliedSearch(search)
    // 상세 화면에서 돌아올 때 현재 코드 검색 결과를 복원하도록 조회 상태를 저장한다
    setListPageSnapshot(CODE_LIST_PATH, pageData.pageNumber, search)
    setSelectedMaster(null)
    setMasterEditForm(null)
    setDetailCodes([])
    setDetailEditForms([])
    setSelectedDetailCode('')
    setDetailForms([])
  }

  /**
   * 공통코드와 선택한 세부코드 기준의 상세 화면 데이터를 조회한다
   *
   * @author SeungHyeon.Kang
   * @param commCode 조회할 공통코드
   * @param comdCode 상세 기준으로 사용할 세부코드
   * @return 상세 데이터 조회 완료 Promise
   */
  const openCodeDetailPage = async (commCode: string, comdCode = '') => {
    setError(null)
    const [master, details] = await Promise.all([getCodeMaster(commCode), getDetailCodes(commCode), loadUseeYsnoCodeList()])
    setSelectedMaster(master)
    setMasterEditForm(master)
    setDetailCodes(details)
    setDetailEditForms(details.map(toDetailCodeForm))
    setSelectedDetailCode(comdCode)
    setDetailForms([])
  }

  /**
   * 알림 템플릿 목록 화면 열기
   *
   * @author SeungHyeon.Kang
   * @param pageNumber 조회할 페이지 번호
   * @param search 적용할 알림 템플릿 검색 조건
   * @return 반환값이 없다
   */
  const openAlimTempListPage = async (pageNumber = 1, search = DEFAULT_ALIM_SEARCH) => {
    setError(null)
    const [pageData] = await Promise.all([
      getAlimTempList(pageNumber, search),
      loadUseeYsnoCodeList(),
      loadAlimSituCodeList(),
    ])
    setAlimPageData(pageData)
    setAlimTemps(pageData.items)
    // 상세 화면에서 돌아올 때 현재 알림 템플릿 검색 결과를 복원하도록 조회 상태를 저장한다
    setListPageSnapshot(ALIM_TEMP_LIST_PATH, pageData.pageNumber, search)
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
    setAlimTempForm({ alimSitu: situCodes[0]?.comdCode ?? '', tempCode: '', tempTitl: '', alimTitl: '', tempCont: '', useeYsno: DEFAULT_USEE_YSNO })
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
    setAlimTempForm({ alimSitu: detail.alimSitu, tempCode: detail.tempCode, tempTitl: detail.tempTitl, alimTitl: detail.alimTitl ?? '', tempCont: detail.tempCont, useeYsno: detail.useeYsno ?? DEFAULT_USEE_YSNO })
  }

  /** 알림 아이콘 목록 화면을 조회한다. */
  const openAlimIconListPage = async (pageNumber = 1, search = DEFAULT_ALIM_ICON_SEARCH) => {
    setError(null)
    const [pageData] = await Promise.all([getAlimIconList(pageNumber, search), loadUseeYsnoCodeList()])
    setAlimIconPageData(pageData)
    setAlimIcons(pageData.items)
    setListPageSnapshot(ALIM_ICON_LIST_PATH, pageData.pageNumber, search)
    setAlimIconDetail(null)
  }

  /** 선택한 알림 아이콘 상세 화면을 조회한다. */
  const openAlimIconDetailPage = async (alimSitu: string) => {
    setError(null)
    const detail = await getAlimIconDetail(alimSitu)
    setAlimIconDetail(detail)
  }

  /**
   * 현재 경로에 해당하는 기존 관리자 관리 화면 데이터를 조회한다
   * @Author SeungHyeon.Kang
   * @return
   */
  const loadCurrentAdminPage = useEffectEvent(() => {
    // 메뉴 목록은 마지막으로 조회한 페이지와 검색 조건으로 복원한다
    if (isMenuListPage) {
      // 저장된 메뉴 목록 조회 상태를 확인한다
      const snapshot = getListPageSnapshot(MENU_LIST_PATH, DEFAULT_MENU_SEARCH)
      // 저장된 메뉴 페이지를 같은 검색 조건으로 다시 조회한다
      void openMenuListPage(snapshot.pageNumber, snapshot.search)
    }

    // 메뉴 등록 경로에서는 신규 입력값을 준비한다
    else if (isMenuNewPage) void openMenuNewPage(parentMenuNumb)
    // 메뉴 상세 경로에서는 선택한 메뉴 정보를 조회한다
    else if (detailKey) void openMenuDetailPage(detailKey.menuNumb, detailKey.subxNumb)
    // 코드 목록은 마지막으로 조회한 페이지와 검색 조건으로 복원한다
    else if (isCodeListPage) {
      // 저장된 코드 목록 조회 상태를 확인한다
      const snapshot = getListPageSnapshot(CODE_LIST_PATH, DEFAULT_CODE_SEARCH)
      // 저장된 코드 페이지를 같은 검색 조건으로 다시 조회한다
      void openCodeListPage(snapshot.pageNumber, snapshot.search)
    }

    // 코드 상세 경로에서는 선택한 공통코드와 세부코드 정보를 조회한다
    else if (codeDetailKey) void openCodeDetailPage(codeDetailKey.commCode, codeDetailKey.comdCode)
    // 알림 템플릿 목록은 마지막으로 조회한 페이지와 검색 조건으로 복원한다
    else if (isAlimTempListPage) {
      // 저장된 알림 템플릿 목록 조회 상태를 확인한다
      const snapshot = getListPageSnapshot(ALIM_TEMP_LIST_PATH, DEFAULT_ALIM_SEARCH)
      // 저장된 알림 템플릿 페이지를 같은 검색 조건으로 다시 조회한다
      void openAlimTempListPage(snapshot.pageNumber, snapshot.search)
    }

    // 알림 템플릿 등록 경로에서는 신규 입력값을 준비한다
    else if (isAlimTempNewPage) void openAlimTempNewPage()
    // 알림 템플릿 상세 경로에서는 선택한 템플릿 정보를 조회한다
    else if (alimTempDetailKey) void openAlimTempDetailPage(alimTempDetailKey.alimSitu, alimTempDetailKey.tempCode)
    // 알림 아이콘 목록은 마지막 검색 조건과 페이지로 복원한다
    else if (isAlimIconListPage) {
      const snapshot = getListPageSnapshot(ALIM_ICON_LIST_PATH, DEFAULT_ALIM_ICON_SEARCH)
      void openAlimIconListPage(snapshot.pageNumber, snapshot.search)
    }
    // 선택한 알림 아이콘 상세를 조회한다
    else if (alimIconDetailKey) void openAlimIconDetailPage(alimIconDetailKey)
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
   *
   * @author SeungHyeon.Kang
   * @param menu 삭제할 메뉴 식별정보
   * @param search 삭제 후 유지할 메뉴 검색 조건
   * @return 반환값이 없다
   */
  const deleteMenu = async (
    menu: Pick<Menu, 'menuNumb' | 'subxNumb'>,
    search = DEFAULT_MENU_SEARCH,
  ) => {
    await deleteMenuApi(menu)
    await loadSidebarMenuList()
    if (isMenuDetailPage && menu.menuNumb === menuForm.menuNumb && menu.subxNumb === menuForm.subxNumb) movePath(MENU_LIST_PATH)
    else if (isMenuDetailPage) await openMenuDetailPage(menuForm.menuNumb, menuForm.subxNumb)
    else await openMenuListPage(menuPageData.pageNumber, search)
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
      await openCodeDetailPage(selectedMaster.commCode, selectedDetailCode)
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
    // 기존 하위메뉴와 아직 저장하지 않은 입력 행을 합쳐 다음 정렬값을 계산한다
    const nextSortOrdr = getNextSortOrdr([...subMenus, ...childForms])
    // 현재 메뉴 아래에 다음 정렬값이 입력된 신규 하위메뉴 행을 추가한다
    setChildForms([
      ...childForms,
      { ...emptyMenuForm(menuForm.menuNumb), sortOrdr: nextSortOrdr },
    ])
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
   * 선택한 공통코드의 최상위 상세 경로로 이동한다
   *
   * @author SeungHyeon.Kang
   * @param master 상세를 표시할 공통코드
   * @return 반환값이 없다
   */
  const selectCodeMaster = (master: CodeMaster) => {
    movePath(`${CODE_DETAIL_PREFIX}/${encodeURIComponent(master.commCode)}`)
  }

  /**
   * 세부코드를 자식 목록의 상세 기준으로 선택한다
   *
   * @author SeungHyeon.Kang
   * @param commCode 세부코드가 속한 공통코드
   * @param comdCode 선택한 세부코드
   * @return 반환값이 없다
   */
  const selectDetailCode = (commCode: string, comdCode: string): void => {
    // 선택한 세부코드가 새 상세 경로의 부모가 되도록 이동한다
    movePath(`${CODE_DETAIL_PREFIX}/${encodeURIComponent(commCode)}/${encodeURIComponent(comdCode)}`)
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
      const pageData = await getCodeMasters(codePageData.pageNumber, codeAppliedSearch)
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
   * 현재 선택한 세부코드를 부모로 지정한 신규 입력 폼을 추가한다
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없다
   */
  const addDetailInput = () => {
    const siblingItems: Array<{ sortOrdr: number | string | null }> = []

    // 현재 선택한 부모에 속한 기존 세부코드만 다음 정렬값 계산에 포함한다
    for (const detailCode of detailCodes) {
      // 다른 부모에 속한 세부코드는 현재 입력 행의 형제 정렬값에서 제외한다
      if ((detailCode.upprCode ?? '') !== selectedDetailCode) {
        continue
      }

      // 같은 부모에 속한 기존 세부코드의 정렬값을 계산 목록에 추가한다
      siblingItems.push(detailCode)
    }

    // 현재 선택한 부모에 추가한 미저장 입력 행도 다음 정렬값 계산에 포함한다
    for (const detailForm of detailForms) {
      // 다른 부모에 추가한 입력 행은 현재 입력 행의 형제 정렬값에서 제외한다
      if (detailForm.upprCode !== selectedDetailCode) {
        continue
      }

      // 같은 부모에 추가한 미저장 입력 행의 정렬값을 계산 목록에 추가한다
      siblingItems.push(detailForm)
    }

    // 현재 형제 데이터의 마지막 정렬값 다음 번호를 신규 입력 행에 설정한다
    const nextSortOrdr = getNextSortOrdr(siblingItems)
    // 선택한 세부코드 아래에 계산된 정렬값을 가진 신규 입력 행을 추가한다
    setDetailForms([
      ...detailForms,
      { ...emptyDetailForm(selectedDetailCode), sortOrdr: nextSortOrdr },
    ])
  }

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
   * 세부코드 입력 폼을 공백 정리와 상위코드 정규화가 끝난 API 요청값으로 변환한다
   *
   * @author SeungHyeon.Kang
   * @param form 변환할 세부코드 입력 폼
   * @return 세부코드 저장 API 요청값
   */
  const toDetailPayload = (form: DetailCodeForm): DetailCodePayload => ({
    comdCode: form.comdCode.trim(),
    comdName: form.comdName.trim(),
    codeExpl: form.codeExpl.trim(),
    upprCode: form.upprCode.trim() || null,
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
    const hasRequired = alimTempForm.alimSitu.trim() && alimTempForm.tempCode.trim() && alimTempForm.tempTitl.trim() && alimTempForm.tempCont.trim()
    // 필수값이 하나라도 없으면 저장 요청을 보내지 않는다
    if (!hasRequired) {
      setError('알림상황, 템플릿코드, 관리용 제목과 템플릿 내용을 입력해 주세요.')
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

  /** 알림 상황별 아이콘을 등록하거나 현재 원본을 교체한다. */
  const saveAlimIcon = async (alimSitu: string, file: File) => {
    setSaving(true)
    setError(null)
    try {
      const result = await saveAlimIconApi(alimSitu, file)
      alert(result.message)
      movePath(`${ALIM_ICON_DETAIL_PREFIX}/${encodeURIComponent(result.data.alimSitu)}`)
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : '알림 아이콘 저장 중 오류가 발생했습니다.')
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
      {isMenuListPage && <MenuListPage menuRows={menuRows} pageData={menuPageData} useeYsnoCodes={useeYsnoCodes} onSearch={(pageNumber, search) => void openMenuListPage(pageNumber, search)} onMovePath={movePath} onDelete={(menu, search) => void deleteMenu(menu, search)} />}
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
      {isAlimIconListPage && <AlimIconListPage icons={alimIcons} pageData={alimIconPageData} useeYsnoCodes={useeYsnoCodes} onSearch={(pageNumber, search) => void openAlimIconListPage(pageNumber, search)} onMovePath={movePath} />}
      {isAlimIconDetailPage && alimIconDetail && (
        <AlimIconDetailPage
          key={alimIconDetail.alimSitu}
          saving={saving}
          detail={alimIconDetail}
          onMovePath={movePath}
          onSave={(alimSitu, file) => void saveAlimIcon(alimSitu, file)}
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
      {isDeletedSuspensionPage && <DeletedSuspensionPage onMovePath={movePath} onError={setError} />}
      {isUserStatisticsPage && <UserStatisticsPage onError={setError} />}
      {isCurrentUserDetailPage && (
        <CurrentUserDetailPage
          userNumb={currentUserDetailKey}
          adminAuthCode={admin?.authCode ?? ''}
          onMovePath={movePath}
          onError={setError}
        />
      )}
      {isComplaintListPage && <ComplaintListPage onMovePath={movePath} onError={setError} />}
      {isComplaintDetailPage && (
        <ComplaintDetailPage
          key={complaintDetailKey}
          cmplNumb={complaintDetailKey}
          adminNumb={admin.admnNumb}
          adminAuthCode={admin.authCode}
          onMovePath={movePath}
          onError={setError}
        />
      )}
      {isInquiryListPage && <InquiryListPage onMovePath={movePath} onError={setError} />}
      {isInquiryDetailPage && (
        <InquiryDetailPage
          key={inquiryDetailKey}
          inqrNumb={inquiryDetailKey}
          onMovePath={movePath}
          onError={setError}
        />
      )}
      {isPopupContentPage && <PopupContentManagePage currentPath={currentPath} onMovePath={movePath} onError={setError} />}
      {isNoticePage && <NoticeManagePage currentPath={currentPath} onMovePath={movePath} onError={setError} />}
      {isServiceInfoPage && <ServiceInfoManagePage currentPath={currentPath} onMovePath={movePath} onError={setError} />}
      {isCodeListPage && (
        <CodeListPage
          codeMasters={codeMasters}
          pageData={codePageData}
          useeYsnoCodes={useeYsnoCodes}
          onSearch={(pageNumber, search) => void openCodeListPage(pageNumber, search)}
          onSelect={selectCodeMaster}
          onSelectDetail={(detail) => selectDetailCode(detail.commCode, detail.comdCode)}
          onLoadDetails={getDetailCodes}
          onOpenRegister={() => setShowMasterForm(true)}
          onError={setError}
        />
      )}
      {isCodeDetailPage && (
        <CodeDetailPage
          selectedMaster={selectedMaster}
          pageTitle={`${activeMenuName || '코드관리'} 상세`}
          masterEditForm={masterEditForm}
          selectedDetailCode={selectedDetailCode}
          detailCodes={detailCodes}
          detailEditForms={detailEditForms}
          detailForms={detailForms}
          useeYsnoCodes={useeYsnoCodes}
          saving={saving}
          onMovePath={movePath}
          onChangeMasterForm={setMasterEditForm}
          onAddDetailInput={addDetailInput}
          onRemoveDetailInput={removeDetailInput}
          onChangeDetailEditForm={changeDetailEditForm}
          onChangeDetailForm={changeDetailForm}
          onSelectDetail={(detail) => selectDetailCode(detail.commCode, detail.comdCode)}
          onSaveAll={() => void saveAllCodeDetail()}
        />
      )}
      {isAlimTempListPage && <AlimTempListPage alimTemps={alimTemps} pageData={alimPageData} alimSituCodes={alimSituCodes} useeYsnoCodes={useeYsnoCodes} onSearch={(pageNumber, search) => void openAlimTempListPage(pageNumber, search)} onMovePath={movePath} />}
      {(isAlimTempDetailPage || isAlimTempNewPage) && (
        <AlimTempDetailPage
          key={isAlimTempNewPage ? 'new' : `${alimTempDetail?.alimSitu ?? ''}-${alimTempDetail?.tempCode ?? ''}`}
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
