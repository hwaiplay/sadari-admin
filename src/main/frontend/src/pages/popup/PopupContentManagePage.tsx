import { useCallback, useEffect, useMemo, useState } from 'react'
import type { FormEvent } from 'react'
import { getCodeList } from '../../api/codeApi'
import {
  getPopupContentDetail,
  getPopupContentKey,
  getPopupContentList,
  savePopupContentApi,
} from '../../api/popupContentApi'
import { POPU_SITU } from '../../constants/codes'
import {
  POPUP_CONTENT_DETAIL_PREFIX,
  POPUP_CONTENT_LIST_PATH,
  POPUP_CONTENT_NEW_PATH,
} from '../../constants/routes'
import type { Code } from '../../types/code'
import type { PageData } from '../../types/common'
import type { PopupContent, PopupContentForm } from '../../types/popupContent'
import { emptyPopupContentForm, popupLinesToJson, toPopupContentForm } from '../../utils/popupContent'
import { PopupContentDetailPage } from './PopupContentDetailPage'
import { PopupContentListPage } from './PopupContentListPage'

type PopupContentManagePageProps = {
  currentPath: string
  onMovePath: (path: string) => void
  onError: (message: string | null) => void
}

const EMPTY_PAGE_DATA: PageData<PopupContent> = {
  items: [],
  totalCount: 0,
  pageNumber: 1,
  pageSize: 20,
  totalPages: 0,
}

/**
 * 팝업 콘텐츠 목록과 신규 등록 및 상세 수정 흐름을 관리한다
 *
 * @author SeungHyeon.Kang
 * @param currentPath 현재 관리자 화면 경로
 * @param onMovePath 관리자 화면 경로 이동 함수
 * @param onError 공통 오류 표시 함수
 * @return 현재 경로에 맞는 팝업 콘텐츠 관리 화면
 */
export function PopupContentManagePage({
  currentPath,
  onMovePath,
  onError,
}: PopupContentManagePageProps) {
  const [popupPageData, setPopupPageData] = useState<PageData<PopupContent>>(EMPTY_PAGE_DATA)
  const [popupContentDetail, setPopupContentDetail] = useState<PopupContent | null>(null)
  const [popupContentForm, setPopupContentForm] = useState<PopupContentForm>(emptyPopupContentForm())
  const [popupSituCodes, setPopupSituCodes] = useState<Code[]>([])
  const [loading, setLoading] = useState(false)
  const [saving, setSaving] = useState(false)

  // 현재 경로가 가리키는 팝업 콘텐츠 복합키를 한 번만 해석한다
  const detailKey = useMemo(
    () => getPopupContentKey(currentPath, POPUP_CONTENT_DETAIL_PREFIX),
    [currentPath],
  )
  const isListPage = currentPath === POPUP_CONTENT_LIST_PATH
  const isNewPage = currentPath === POPUP_CONTENT_NEW_PATH
  const isDetailPage = detailKey !== null

  /**
   * 팝업 콘텐츠 목록의 요청 페이지를 조회한다
   *
   * @author SeungHyeon.Kang
   * @param pageNumber 조회할 페이지 번호
   * @return 조회 완료 Promise
   */
  const loadPopupContentList = useCallback(async (pageNumber = 1): Promise<void> => {
    // 새로운 목록 요청 전에 이전 화면 오류를 지운다
    onError(null)
    // 목록 조회 중 빈 화면으로 오인하지 않도록 로딩 상태를 시작한다
    setLoading(true)
    // API 실패를 공통 오류 영역에 격리하여 현재 메뉴 구조를 유지한다
    try {
      // 요청 페이지의 팝업 콘텐츠 목록과 페이지 정보를 조회한다
      const pageData = await getPopupContentList(pageNumber)
      // 검증된 목록과 페이지 정보를 화면 상태에 반영한다
      setPopupPageData(pageData)
      // 목록 화면에서는 이전에 조회한 상세 감사정보를 제거한다
      setPopupContentDetail(null)
    } catch (error: unknown) {
      // 원시 응답 대신 API 모듈에서 정리한 사용자 오류를 표시한다
      onError(error instanceof Error ? error.message : '팝업 콘텐츠 목록 조회 중 오류가 발생했습니다.')
    } finally {
      // 성공과 실패에 관계없이 목록 로딩 상태를 종료한다
      setLoading(false)
    }
  }, [onError])

  /**
   * 신규 팝업 등록에 필요한 화면 구분 코드와 빈 입력값을 준비한다
   *
   * @author SeungHyeon.Kang
   * @return 신규 등록 화면 준비 완료 Promise
   */
  const loadPopupContentNew = useCallback(async (): Promise<void> => {
    // 새로운 등록 화면을 열기 전에 이전 오류를 지운다
    onError(null)
    // 공통코드 조회 중 입력을 허용하지 않도록 로딩 상태를 시작한다
    setLoading(true)
    // 공통코드 실패를 입력 화면 밖의 공통 오류 영역에 표시한다
    try {
      // 사용 가능한 팝업 화면 구분 공통코드를 조회한다
      const situCodes = await getCodeList(POPU_SITU)
      // 신규 등록 화면의 선택 항목을 공통코드로 구성한다
      setPopupSituCodes(situCodes)
      // 상세 감사정보가 신규 화면에 남지 않도록 제거한다
      setPopupContentDetail(null)
      // 첫 번째 화면 구분 코드를 기본 선택한 빈 등록 폼을 생성한다
      setPopupContentForm(emptyPopupContentForm(situCodes[0]?.comdCode ?? ''))
    } catch (error: unknown) {
      // 원시 응답 대신 API 모듈에서 정리한 사용자 오류를 표시한다
      onError(error instanceof Error ? error.message : '팝업 등록 화면을 준비하는 중 오류가 발생했습니다.')
    } finally {
      // 성공과 실패에 관계없이 등록 화면 로딩 상태를 종료한다
      setLoading(false)
    }
  }, [onError])

  /**
   * 복합키에 해당하는 팝업 콘텐츠 상세와 화면 구분 코드를 조회한다
   *
   * @author SeungHyeon.Kang
   * @param popuSitu 팝업 사용 화면 구분 코드
   * @param popuCode 팝업 식별 코드
   * @return 상세 화면 준비 완료 Promise
   */
  const loadPopupContentDetail = useCallback(async (popuSitu: string, popuCode: string): Promise<void> => {
    // 새로운 상세 요청 전에 이전 화면 오류를 지운다
    onError(null)
    // 상세 데이터와 공통코드가 준비되기 전 입력을 막기 위해 로딩 상태를 시작한다
    setLoading(true)
    // 상세와 공통코드 조회 중 하나라도 실패하면 같은 오류 경로로 처리한다
    try {
      // 상세 데이터와 화면 구분 공통코드를 동시에 조회한다
      const [detail, situCodes] = await Promise.all([
        getPopupContentDetail(popuSitu, popuCode),
        getCodeList(POPU_SITU),
      ])
      // 화면 구분 선택 항목을 공통코드로 구성한다
      setPopupSituCodes(situCodes)
      // 조회된 감사정보를 상세 화면에 반영한다
      setPopupContentDetail(detail)
      // JSON 목록을 줄 단위 관리자 입력값으로 변환하여 상세 폼에 반영한다
      setPopupContentForm(toPopupContentForm(detail))
    } catch (error: unknown) {
      // 손상된 DB 콘텐츠와 API 실패를 모두 관리자 공통 오류 영역에 표시한다
      onError(error instanceof Error ? error.message : '팝업 콘텐츠 상세 조회 중 오류가 발생했습니다.')
    } finally {
      // 성공과 실패에 관계없이 상세 화면 로딩 상태를 종료한다
      setLoading(false)
    }
  }, [onError])

  useEffect(() => {
    // Effect 본문에서 동기 상태 변경이 연쇄되지 않도록 현재 경로 데이터 조회를 다음 작업으로 예약한다
    const loadTimer = window.setTimeout(() => {
      // 현재 경로에 맞는 팝업 관리 데이터를 중복 없이 한 번만 조회한다
      if (isListPage) {
        // 목록 경로의 첫 페이지 데이터를 조회한다
        void loadPopupContentList()
        // 다른 팝업 화면 조회가 이어지지 않도록 예약 작업을 종료한다
        return
      }

      // 신규 등록 경로에서는 화면 구분 코드와 빈 폼을 준비한다
      if (isNewPage) {
        // 신규 팝업 등록 화면 데이터를 준비한다
        void loadPopupContentNew()
        // 상세 조회가 이어지지 않도록 예약 작업을 종료한다
        return
      }

      // 상세 복합키가 정상적으로 해석된 경로에서만 상세 API를 호출한다
      if (detailKey) {
        // 복합키에 해당하는 팝업 콘텐츠 상세를 조회한다
        void loadPopupContentDetail(detailKey.popuSitu, detailKey.popuCode)
      }
    }, 0)

    // 경로가 바뀌거나 화면이 해제되면 이전 경로의 예약 조회를 취소한다
    return () => window.clearTimeout(loadTimer)
  }, [
    detailKey,
    isListPage,
    isNewPage,
    loadPopupContentDetail,
    loadPopupContentList,
    loadPopupContentNew,
  ])

  /**
   * 팝업 콘텐츠 입력값 한 항목을 불변 상태로 변경한다
   *
   * @author SeungHyeon.Kang
   * @param field 변경할 팝업 콘텐츠 필드
   * @param value 변경할 입력값
   * @return 반환값이 없다
   */
  const handlePopupContentChange = (field: keyof PopupContentForm, value: string): void => {
    // 다른 필드 값을 유지한 새 객체로 팝업 콘텐츠 입력 상태를 갱신한다
    setPopupContentForm({ ...popupContentForm, [field]: value })
  }

  /**
   * 필수 입력과 첫 번째 목록 문구가 저장 가능한지 확인한다
   *
   * @author SeungHyeon.Kang
   * @return 팝업 콘텐츠 저장 가능 여부
   */
  const validatePopupContentForm = (): boolean => {
    // 복합키와 관리 제목 및 첫 번째 목록 영역은 사용자 팝업 연결에 필수이다
    if (!popupContentForm.popuSitu.trim() || !popupContentForm.popuCode.trim()
        || !popupContentForm.mngmTitl.trim() || !popupLinesToJson(popupContentForm.contFirs)) {
      // "사용 화면, 팝업 코드, 관리용 제목, 내용 1을 입력해 주세요."
      onError('사용 화면, 팝업 코드, 관리용 제목, 내용 1을 입력해 주세요.')
      // 필수값이 누락되어 저장할 수 없음을 반환한다
      return false
    }

    // 사용자 API와 공통코드에서 사용하는 영문 대문자 기반 코드 형식만 허용한다
    if (!/^[A-Z][A-Z0-9_]*$/u.test(popupContentForm.popuCode)) {
      // "팝업 코드는 영문 대문자로 시작하고 숫자와 _만 함께 사용할 수 있습니다."
      onError('팝업 코드는 영문 대문자로 시작하고 숫자와 _만 함께 사용할 수 있습니다.')
      // 팝업 코드 형식이 올바르지 않아 저장할 수 없음을 반환한다
      return false
    }

    // 필수값과 팝업 코드 형식이 저장 가능함을 반환한다
    return true
  }

  /**
   * 신규 또는 기존 팝업 콘텐츠를 저장한다
   *
   * @author SeungHyeon.Kang
   * @param event 팝업 콘텐츠 저장 폼 제출 이벤트
   * @return 저장 완료 Promise
   */
  const handlePopupContentSubmit = async (event: FormEvent<HTMLFormElement>): Promise<void> => {
    // 브라우저 기본 폼 이동 대신 현재 관리자 경로에서 API 저장을 실행한다
    event.preventDefault()
    // 필수값과 팝업 코드가 유효하지 않으면 API 요청을 만들지 않는다
    if (!validatePopupContentForm()) {
      // 화면에 표시된 검증 오류를 유지하고 저장을 종료한다
      return
    }

    // 중복 제출을 막기 위해 저장 진행 상태를 시작한다
    setSaving(true)
    // 저장 요청 전에 이전 오류를 지운다
    onError(null)
    // 등록과 수정 실패를 현재 상세 화면의 공통 오류 영역에 표시한다
    try {
      // 현재 경로가 상세이면 수정하고 신규이면 새 팝업 콘텐츠를 등록한다
      const result = await savePopupContentApi(popupContentForm, isDetailPage)
      // 서버가 반환한 "저장했습니다." 또는 "수정했습니다." 메시지를 표시한다
      window.alert(result.message)
      // 저장된 복합키를 사용하여 최신 감사정보가 있는 상세 화면으로 이동한다
      onMovePath(
        `${POPUP_CONTENT_DETAIL_PREFIX}/${encodeURIComponent(result.data.popuSitu)}`
        + `/${encodeURIComponent(result.data.popuCode)}`,
      )
      // 같은 상세 경로에서 수정한 경우 경로 변경 없이 최신 DB 데이터를 다시 조회한다
      if (isDetailPage) {
        // 수정 결과와 감사정보를 즉시 반영하도록 상세 데이터를 다시 조회한다
        await loadPopupContentDetail(result.data.popuSitu, result.data.popuCode)
      }
    } catch (error: unknown) {
      // 원시 응답 대신 API 모듈에서 정리한 사용자 오류를 표시한다
      onError(error instanceof Error ? error.message : '팝업 콘텐츠 저장 중 오류가 발생했습니다.')
    } finally {
      // 성공과 실패에 관계없이 저장 진행 상태를 종료한다
      setSaving(false)
    }
  }

  // 목록 경로에서는 팝업 콘텐츠 목록과 페이지 이동 화면을 반환한다
  if (isListPage) {
    // 현재 페이지의 팝업 콘텐츠 목록 화면을 반환한다
    return (
      <PopupContentListPage
        popupContents={popupPageData.items}
        pageData={popupPageData}
        onPageChange={loadPopupContentList}
        onMovePath={onMovePath}
      />
    )
  }

  // 신규 또는 정상적인 복합키 상세 경로에서는 팝업 콘텐츠 편집 화면을 반환한다
  if (isNewPage || isDetailPage) {
    // 현재 경로에 맞는 팝업 콘텐츠 등록 또는 상세 수정 화면을 반환한다
    return (
      <PopupContentDetailPage
        isNewPage={isNewPage}
        loading={loading}
        saving={saving}
        popupContentForm={popupContentForm}
        popupContentDetail={popupContentDetail}
        popupSituCodes={popupSituCodes}
        onMovePath={onMovePath}
        onChange={handlePopupContentChange}
        onSubmit={handlePopupContentSubmit}
      />
    )
  }

  // 지원하지 않는 팝업 하위 경로에서는 잘못된 화면이 보이지 않도록 빈 영역을 반환한다
  return null
}
