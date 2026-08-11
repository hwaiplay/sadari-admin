import { useEffect, useEffectEvent, useState } from 'react'
import type { FormEvent, MouseEvent } from 'react'
import { getCodeList } from '../api/codeApi'
import { SPND_RSON, SPND_TYPE } from '../constants/codes'
import { useMenuPermission } from '../contexts/useMenuPermission'
import type { Code } from '../types/code'
import type { PageData } from '../types/common'
import type { CurrentUser, CurrentUserSuspension, CurrentUserSuspensionRequest } from '../types/currentUser'
import { formatDate } from '../utils/code'
import { Pagination } from './Pagination'

type UserSuspensionPanelProps = {
  contextKey: number
  targetUser: CurrentUser
  adminAuthCode: string
  getSuspensions: (pageNumber: number) => Promise<PageData<CurrentUserSuspension>>
  createSuspension: (request: CurrentUserSuspensionRequest) => Promise<CurrentUserSuspension>
  releaseSuspension: (spndNumb: number, rlesCntn: string) => Promise<void>
  onRefreshTargetUser: () => Promise<void>
  onError: (message: string | null) => void
}

const EMPTY_SUSPENSION_PAGE: PageData<CurrentUserSuspension> = {
  items: [],
  totalCount: 0,
  pageNumber: 1,
  pageSize: 20,
  totalPages: 0,
}

/**
 * 기간 정지 입력에 사용할 기본 종료 일시를 생성한다
 *
 * @author SeungHyeon.Kang
 * @return 현재 시각으로부터 7일 뒤의 로컬 일시
 */
const createDefaultEndDate = (): string => {
  // 기본 기간 정지 종료일 계산에 사용할 현재 날짜를 생성한다
  const endDate = new Date()
  // 관리자가 바로 제출할 수 있도록 기본 정지 기간을 7일로 설정한다
  endDate.setDate(endDate.getDate() + 7)
  // 초 단위 차이로 입력값이 복잡해지지 않도록 분 단위로 정리한다
  endDate.setMinutes(0, 0, 0)
  // 브라우저 시간대를 datetime-local 형식에 반영할 보정값을 계산한다
  const timezoneOffset = endDate.getTimezoneOffset() * 60_000
  // 로컬 시간대가 반영된 분 단위 종료 일시를 반환한다
  return new Date(endDate.getTime() - timezoneOffset).toISOString().slice(0, 16)
}

/**
 * 현재 사용자 상세와 신고 상세에서 동일한 회원 이용정지 관리 기능을 제공한다
 *
 * @author SeungHyeon.Kang
 * @param contextKey 이용정지 API 문맥을 구분하는 사용자 또는 신고 번호
 * @param targetUser 이용정지 대상 사용자
 * @param adminAuthCode 로그인 관리자 권한 코드
 * @param getSuspensions 이용정지 이력 조회 함수
 * @param createSuspension 이용정지 적용 함수
 * @param releaseSuspension 이용정지 해제 함수
 * @param onRefreshTargetUser 대상 사용자 상태 갱신 함수
 * @param onError 공통 오류 메시지 변경 함수
 * @return 이용정지 등록과 해제 및 이력 화면
 */
export function UserSuspensionPanel({
  contextKey,
  targetUser,
  adminAuthCode,
  getSuspensions,
  createSuspension,
  releaseSuspension,
  onRefreshTargetUser,
  onError,
}: UserSuspensionPanelProps) {
  // 현재 메뉴의 쓰기 권한을 이용정지 버튼 노출 기준으로 조회한다
  const permission = useMenuPermission()
  const [suspensionHistories, setSuspensionHistories] = useState<PageData<CurrentUserSuspension>>(
    EMPTY_SUSPENSION_PAGE,
  )
  const [suspensionTypes, setSuspensionTypes] = useState<Code[]>([])
  const [suspensionReasons, setSuspensionReasons] = useState<Code[]>([])
  const [suspensionForm, setSuspensionForm] = useState<CurrentUserSuspensionRequest>({
    spndType: 'PERIOD',
    spndRson: '',
    spndCntn: '',
    endxDate: createDefaultEndDate(),
  })
  const [releaseContent, setReleaseContent] = useState('')
  const [savingSuspension, setSavingSuspension] = useState(false)
  const [refreshingUserStatusSync, setRefreshingUserStatusSync] = useState(false)
  const [selectedSuspensionMemo, setSelectedSuspensionMemo] = useState<CurrentUserSuspension | null>(null)
  const [loadingSuspensions, setLoadingSuspensions] = useState(true)

  // 최신 API 함수와 오류 처리 함수를 의존성 증가 없이 초기 조회에서 사용한다
  const loadInitialSuspensionData = useEffectEvent(async (): Promise<void> => {
    // 이용정지 이력과 입력 공통코드를 동시에 조회한다
    const [suspensionPage, typeCodes, reasonCodes] = await Promise.all([
      getSuspensions(1),
      getCodeList(SPND_TYPE),
      getCodeList(SPND_RSON),
    ])
    // 이용정지 이력 첫 페이지를 화면에 반영한다
    setSuspensionHistories(suspensionPage)
    // 미사용 코드를 제외한 정지 유형을 입력 선택지로 반영한다
    setSuspensionTypes(typeCodes.filter((code) => code.useeYsno !== 'N'))
    // 미사용 코드를 제외한 정지 사유를 입력 선택지로 반영한다
    setSuspensionReasons(reasonCodes.filter((code) => code.useeYsno !== 'N'))
    // 첫 번째 활성 정지 사유를 초기 입력값으로 설정한다
    setSuspensionForm((currentForm) => ({
      ...currentForm,
      spndRson: currentForm.spndRson || reasonCodes[0]?.comdCode || '',
    }))
  })

  // 사용자 또는 신고 문맥이 바뀌면 해당 대상의 이용정지 데이터를 다시 조회한다
  useEffect(() => {
    let active = true
    // 렌더 완료 뒤 현재 문맥의 이용정지 이력과 공통코드를 조회한다
    const loadTimer = window.setTimeout(() => {
      // 현재 문맥의 이용정지 이력과 입력 코드를 비동기로 조회한다
      loadInitialSuspensionData()
        .then(() => {
          // 화면이 유지되는 동안에만 이전 오류와 로딩 상태를 초기화한다
          if (active) {
            // 이전 이용정지 조회 오류를 제거한다
            onError(null)
            // 이용정지 영역 로딩을 종료한다
            setLoadingSuspensions(false)
          }
        })
        .catch((error: unknown) => {
          // 화면이 유지되는 동안에만 이용정지 조회 오류를 표시한다
          if (active) {
            // "이용 정지 이력을 불러오지 못했습니다."
            onError(error instanceof Error ? error.message : '이용 정지 이력을 불러오지 못했습니다.')
            // 오류 상태에서도 이용정지 영역 로딩을 종료한다
            setLoadingSuspensions(false)
          }
        })
    }, 0)
    // 화면 해제 뒤 도착하는 응답이 상태를 변경하지 않도록 차단한다
    return () => {
      active = false
      // 아직 실행되지 않은 초기 조회 타이머를 해제한다
      window.clearTimeout(loadTimer)
    }
  }, [contextKey, onError])

  /**
   * 이용정지 이력의 지정 페이지를 조회한다
   *
   * @author SeungHyeon.Kang
   * @param pageNumber 조회할 페이지 번호
   * @return 반환값이 없다
   */
  const loadSuspensionHistoryPage = async (pageNumber: number): Promise<void> => {
    // 페이지 이동 중 기존 이력을 유지하고 오류만 공통 영역에 표시한다
    try {
      // 선택한 페이지의 이용정지 이력을 화면에 반영한다
      setSuspensionHistories(await getSuspensions(pageNumber))
      // 이전 이용정지 조회 오류를 제거한다
      onError(null)
    } catch (error: unknown) {
      // "이용 정지 이력을 불러오지 못했습니다."
      onError(error instanceof Error ? error.message : '이용 정지 이력을 불러오지 못했습니다.')
    }
  }

  /**
   * 이용정지 변경 후 대상 사용자와 정지 이력을 다시 조회한다
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없다
   */
  const refreshSuspensionState = async (): Promise<void> => {
    // 회원 상태와 이용정지 이력이 같은 변경 결과를 표시하도록 함께 갱신한다
    const [, suspensionPage] = await Promise.all([onRefreshTargetUser(), getSuspensions(1)])
    // 갱신된 이용정지 이력 첫 페이지를 화면에 반영한다
    setSuspensionHistories(suspensionPage)
  }

  /**
   * 사용자 서버의 회원 상태 반영 결과를 다시 조회한다
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없다
   */
  const handleUserStatusSync = async (): Promise<void> => {
    // 반영 상태 확인 요청의 중복 실행을 막는다
    setRefreshingUserStatusSync(true)
    // 조회 실패 시 기존 화면 상태를 유지하고 공통 오류만 표시한다
    try {
      // 사용자 상태와 이용정지 이력을 최신 데이터로 갱신한다
      await refreshSuspensionState()
      // 이전 반영 상태 조회 오류를 제거한다
      onError(null)
    } catch (error: unknown) {
      // "사용자 서버 반영 상태를 확인하지 못했습니다."
      onError(error instanceof Error ? error.message : '사용자 서버 반영 상태를 확인하지 못했습니다.')
    } finally {
      // 성공 여부와 관계없이 다시 확인할 수 있도록 진행 상태를 해제한다
      setRefreshingUserStatusSync(false)
    }
  }

  /**
   * 입력한 기간 또는 무기한 이용정지를 대상 사용자에게 적용한다
   *
   * @author SeungHyeon.Kang
   * @param event 이용정지 등록 폼 제출 이벤트
   * @return 반환값이 없다
   */
  const handleCreateSuspension = async (event: FormEvent<HTMLFormElement>): Promise<void> => {
    // 브라우저 기본 폼 제출을 막고 API 처리로 전환한다
    event.preventDefault()
    // 정지 유형과 사유가 없으면 서버 요청 전에 입력 오류를 표시한다
    if (!suspensionForm.spndType || !suspensionForm.spndRson) {
      // "정지 유형과 사유를 선택해 주세요."
      onError('정지 유형과 사유를 선택해 주세요.')
      // 필수 선택값이 없는 이용정지 요청을 종료한다
      return
    }

    // 기간 정지는 서버가 만료 시각을 판단할 종료 일시가 필수이다
    if (suspensionForm.spndType === 'PERIOD' && !suspensionForm.endxDate) {
      // "기간 정지의 종료 일시를 입력해 주세요."
      onError('기간 정지의 종료 일시를 입력해 주세요.')
      // 종료 일시가 없는 기간 정지 요청을 종료한다
      return
    }

    // "이 사용자에게 이용 정지를 적용하시겠습니까?"
    if (!window.confirm('이 사용자에게 이용 정지를 적용하시겠습니까?')) {
      // 관리자가 취소한 이용정지 요청을 종료한다
      return
    }

    // 이용정지 등록 버튼의 중복 제출을 막는다
    setSavingSuspension(true)
    // 등록 실패 시 기존 대상 상태를 유지하고 공통 오류를 표시한다
    try {
      // 기간 정지에만 종료 일시를 포함하여 이용정지를 적용한다
      await createSuspension({
        ...suspensionForm,
        endxDate: suspensionForm.spndType === 'PERIOD' ? suspensionForm.endxDate : null,
      })
      // 등록된 정지 상태와 이력을 함께 다시 조회한다
      await refreshSuspensionState()
      // 다음 정지 등록을 위한 기본 입력값으로 폼을 초기화한다
      setSuspensionForm((form) => ({
        ...form,
        spndType: 'PERIOD',
        spndCntn: '',
        endxDate: createDefaultEndDate(),
      }))
      // 이전 이용정지 처리 오류를 제거한다
      onError(null)
    } catch (error: unknown) {
      // "이용 정지를 적용하지 못했습니다."
      onError(error instanceof Error ? error.message : '이용 정지를 적용하지 못했습니다.')
    } finally {
      // 성공 여부와 관계없이 이용정지 등록 진행 상태를 해제한다
      setSavingSuspension(false)
    }
  }

  /**
   * 적용 중인 대상 사용자의 이용정지를 관리자 해제한다
   *
   * @author SeungHyeon.Kang
   * @param spndNumb 해제할 정지 이력 번호
   * @return 반환값이 없다
   */
  const handleReleaseSuspension = async (spndNumb: number): Promise<void> => {
    // "이 사용자의 이용 정지를 즉시 해제하시겠습니까?"
    if (!window.confirm('이 사용자의 이용 정지를 즉시 해제하시겠습니까?')) {
      // 관리자가 취소한 이용정지 해제 요청을 종료한다
      return
    }

    // 이용정지 해제 버튼의 중복 제출을 막는다
    setSavingSuspension(true)
    // 해제 실패 시 기존 정지 상태를 유지하고 공통 오류를 표시한다
    try {
      // 관리자 내부 해제 메모와 함께 적용 중인 정지를 해제한다
      await releaseSuspension(spndNumb, releaseContent.trim())
      // 해제된 회원 상태와 이용정지 이력을 함께 다시 조회한다
      await refreshSuspensionState()
      // 다음 해제 처리에 이전 메모가 남지 않도록 초기화한다
      setReleaseContent('')
      // 이전 이용정지 처리 오류를 제거한다
      onError(null)
    } catch (error: unknown) {
      // "이용 정지를 해제하지 못했습니다."
      onError(error instanceof Error ? error.message : '이용 정지를 해제하지 못했습니다.')
    } finally {
      // 성공 여부와 관계없이 이용정지 해제 진행 상태를 해제한다
      setSavingSuspension(false)
    }
  }

  /**
   * 선택한 이용정지 이력의 정지 메모와 해제 메모 팝업을 연다
   *
   * @author SeungHyeon.Kang
   * @param event 팝업 보기 버튼 클릭 이벤트
   * @return 반환값이 없다
   */
  const handleSuspensionMemoOpen = (event: MouseEvent<HTMLButtonElement>): void => {
    // 버튼에 저장된 이용정지 이력 번호를 숫자로 변환한다
    const suspensionNumber = Number(event.currentTarget.dataset.suspensionNumber)
    // 현재 페이지에서 관리자가 선택한 이용정지 이력을 찾는다
    const selectedHistory = suspensionHistories.items.find((history) => history.spndNumb === suspensionNumber)
    // 현재 페이지에 없는 이력 번호로는 메모 팝업을 열지 않는다
    if (!selectedHistory) {
      // 잘못된 선택값의 팝업 열기를 종료한다
      return
    }

    // 선택한 이용정지 이력의 메모를 팝업 상태에 설정한다
    setSelectedSuspensionMemo(selectedHistory)
  }

  /**
   * 이용정지 이력 메모 팝업을 닫는다
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없다
   */
  const handleSuspensionMemoClose = (): void => {
    // 선택한 이력을 초기화하여 메모 팝업을 닫는다
    setSelectedSuspensionMemo(null)
  }

  /**
   * 이용정지 이력 행을 표시한다
   *
   * @author SeungHyeon.Kang
   * @param history 표시할 이용정지 이력
   * @return 이용정지 이력 행
   */
  const renderSuspensionRow = (history: CurrentUserSuspension) => (
    <tr key={history.spndNumb}>
      <td className="col-history-number">{history.spndNumb}</td>
      <td>{history.spndTypeName ?? history.spndType}</td>
      <td>{history.spndRsonName ?? history.spndRson}</td>
      <td>{history.spndStatName ?? history.spndStat}</td>
      <td className="col-date-time">{formatDate(history.strtDate)}</td>
      <td className="col-date-time">{history.endxDate ? formatDate(history.endxDate) : '무기한'}</td>
      <td className="col-date-time">{formatDate(history.rlesDate) || '-'}</td>
      <td>{history.regiAdmnName ?? history.regiAdmn}</td>
      <td className="current-user-suspension-memo-action">
        <button
          type="button"
          className="subtle-button current-user-suspension-memo-button"
          data-suspension-number={history.spndNumb}
          aria-haspopup="dialog"
          onClick={handleSuspensionMemoOpen}
        >
          {/* "팝업 보기" */}
          팝업 보기
        </button>
      </td>
    </tr>
  )

  // 첫 페이지에서 적용 중인 최신 이용정지 이력을 찾는다
  const activeSuspension = suspensionHistories.items.find((history) => history.spndStat === 'ACTIVE')
  const hasSuspensionHistory = suspensionHistories.items.length > 0
  const isUserStatusSyncPending = targetUser.userStatusSyncStat === 'PENDING'

  // 이용정지 이력과 입력 코드가 도착하기 전에는 고정 안내를 표시한다
  if (loadingSuspensions) {
    // 이용정지 영역의 로딩 상태를 반환한다
    return <section className="detail-panel"><p className="empty small">이용 정지 정보를 불러오고 있습니다.</p></section>
  }

  // 이용정지 등록과 해제 및 전체 이력 영역을 반환한다
  return (
    <>
      {/* 관리자 이용 정지 등록과 해제 */}
      <section className="detail-panel">
        <div className="detail-title">
          <div>
            <h2>이용 정지 관리</h2>
            <p>
              같은 Kakao 계정의 재로그인을 차단하며, 반영 완료는 사용자 서버가 이벤트를 처리했다는 의미입니다.
              로그인 세션이 없으면 다음 로그인부터 DB 상태가 적용됩니다.
            </p>
          </div>
          {/* 이용 정지 상태와 사용자 서버 Outbox 처리 결과 영역 */}
          <div className="current-user-suspension-status-group">
            <span className={`status ${activeSuspension ? 'current-user-suspended-status' : ''}`}>
              {activeSuspension ? '정지 적용 중' : '적용 중인 정지 없음'}
            </span>
            {hasSuspensionHistory && (
              <>
                <span
                  className={`status ${
                    isUserStatusSyncPending ? 'current-user-sync-pending' : 'current-user-sync-completed'
                  }`}
                >
                  {isUserStatusSyncPending ? (
                    <>
                      {/* "사용자 서버 반영 대기" */}
                      사용자 서버 반영 대기
                    </>
                  ) : (
                    <>
                      {/* "사용자 서버 반영 완료" */}
                      사용자 서버 반영 완료
                    </>
                  )}
                </span>
                <button
                  type="button"
                  className="subtle-button current-user-sync-refresh"
                  disabled={refreshingUserStatusSync}
                  onClick={() => void handleUserStatusSync()}
                >
                  {refreshingUserStatusSync ? '확인 중' : '반영 상태 확인'}
                </button>
              </>
            )}
          </div>
        </div>

        {permission.writYsno === 'Y' && activeSuspension && (
          <section className="current-user-release-form">
            <div>
              <strong>{activeSuspension.spndTypeName ?? activeSuspension.spndType}</strong>
              <span>
                {activeSuspension.spndRsonName ?? activeSuspension.spndRson}
                {' · '}
                {activeSuspension.endxDate ? `${formatDate(activeSuspension.endxDate)}까지` : '무기한'}
              </span>
            </div>
            <label>
              {/* "해제 메모" */}
              해제 메모
              {/* "사용자에게 공개되지 않는 해제 메모" */}
              <textarea
                value={releaseContent}
                maxLength={1000}
                placeholder="사용자에게 공개되지 않는 해제 메모"
                onChange={(event) => setReleaseContent(event.target.value)}
              />
            </label>
            <button
              type="button"
              disabled={savingSuspension}
              onClick={() => void handleReleaseSuspension(activeSuspension.spndNumb)}
            >
              {savingSuspension ? '처리 중' : '정지 해제'}
            </button>
          </section>
        )}

        {permission.writYsno === 'Y' && !activeSuspension && (
          <form className="current-user-suspension-form" onSubmit={(event) => void handleCreateSuspension(event)}>
            {/* 정지 유형과 사유 구분 및 종료 일시 선택 영역 */}
            <div className="current-user-suspension-fields">
              <label>
                {/* "정지 유형" */}
                정지 유형
                <select
                  value={suspensionForm.spndType}
                  onChange={(event) => setSuspensionForm({
                    ...suspensionForm,
                    spndType: event.target.value,
                    endxDate: event.target.value === 'PERIOD'
                      ? suspensionForm.endxDate ?? createDefaultEndDate()
                      : null,
                  })}
                >
                  {suspensionTypes
                    .filter((code) => code.comdCode !== 'INDEFINITE' || adminAuthCode === 'SUPER')
                    .map((code) => (
                      <option key={code.comdCode} value={code.comdCode}>{code.comdName}</option>
                    ))}
                </select>
              </label>
              <label>
                {/* "정지 사유 구분" */}
                정지 사유 구분
                <select
                  value={suspensionForm.spndRson}
                  onChange={(event) => setSuspensionForm({ ...suspensionForm, spndRson: event.target.value })}
                >
                  {suspensionReasons.map((code) => (
                    <option key={code.comdCode} value={code.comdCode}>{code.comdName}</option>
                  ))}
                </select>
              </label>
              {suspensionForm.spndType === 'PERIOD' && (
                <label>
                  {/* "종료 일시" */}
                  종료 일시
                  <input
                    type="datetime-local"
                    value={suspensionForm.endxDate ?? ''}
                    onChange={(event) => setSuspensionForm({ ...suspensionForm, endxDate: event.target.value })}
                    required
                  />
                </label>
              )}
            </div>

            {/* 관리자에게만 공개되는 정지 메모 입력 영역 */}
            <label className="current-user-suspension-note">
              {/* "정지 메모" */}
              정지 메모
              {/* "사용자에게 공개되지 않는 정지 메모를 입력해 주세요." */}
              <textarea
                value={suspensionForm.spndCntn}
                maxLength={1000}
                placeholder="사용자에게 공개되지 않는 정지 메모를 입력해 주세요."
                onChange={(event) => setSuspensionForm({ ...suspensionForm, spndCntn: event.target.value })}
              />
            </label>

            {/* 이용 정지 등록 버튼 영역 */}
            <div className="current-user-suspension-actions">
              <button type="submit" disabled={savingSuspension}>
                {savingSuspension ? '적용 중' : '이용 정지 적용'}
              </button>
            </div>
          </form>
        )}
      </section>

      {/* 관리자 이용 정지 이력 */}
      <section className="detail-panel">
        <div className="detail-title">
          <div>
            <h2>이용 정지 이력</h2>
            <p>정지 메모와 해제 메모는 관리자에게만 표시되며 사용자 API에는 제공하지 않습니다.</p>
          </div>
          <div className="status">총 {suspensionHistories.totalCount.toLocaleString()}건</div>
        </div>
        <section className="table-wrap current-user-history-table current-user-suspension-table">
          <table>
            <thead>
              <tr>
                <th className="col-history-number">번호</th>
                <th>유형</th>
                <th>사유</th>
                <th>상태</th>
                <th className="col-date-time">시작일</th>
                <th className="col-date-time">종료 예정일</th>
                <th className="col-date-time">해제·만료일</th>
                <th>등록자</th>
                <th>
                  {/* "메모" */}
                  메모
                </th>
              </tr>
            </thead>
            <tbody>
              {suspensionHistories.items.length === 0 ? (
                <tr className="empty-row"><td colSpan={9}>이용 정지 이력이 없습니다.</td></tr>
              ) : suspensionHistories.items.map(renderSuspensionRow)}
            </tbody>
          </table>
        </section>
        <Pagination
          pageNumber={suspensionHistories.pageNumber}
          totalPages={suspensionHistories.totalPages}
          onPageChange={(pageNumber) => void loadSuspensionHistoryPage(pageNumber)}
        />
      </section>

      {selectedSuspensionMemo && (
        /* 이용 정지 이력의 정지 메모와 해제 메모 팝업 영역 */
        <section className="modal-backdrop">
          {/* 메모 팝업 본문 영역 */}
          <section
            className="modal-panel current-user-suspension-memo-modal"
            role="dialog"
            aria-modal="true"
            aria-labelledby="current-user-suspension-memo-title"
          >
            {/* 메모 팝업 제목과 이력 식별 정보 영역 */}
            <div className="current-user-suspension-memo-header">
              <div>
                <h2 id="current-user-suspension-memo-title">
                  {/* "이용 정지 메모" */}
                  이용 정지 메모
                </h2>
                <p>
                  {selectedSuspensionMemo.spndTypeName ?? selectedSuspensionMemo.spndType}
                  {' · '}
                  {selectedSuspensionMemo.spndRsonName ?? selectedSuspensionMemo.spndRson}
                </p>
              </div>
            </div>

            {/* 정지 메모와 해제 메모 내용 영역 */}
            <div className="current-user-suspension-memo-content">
              <section>
                <h3>
                  {/* "정지 메모" */}
                  정지 메모
                </h3>
                <p>{selectedSuspensionMemo.spndCntn || '-'}</p>
              </section>
              <section>
                <h3>
                  {/* "해제 메모" */}
                  해제 메모
                </h3>
                <p>{selectedSuspensionMemo.rlesCntn || '-'}</p>
              </section>
            </div>

            {/* 메모 팝업 닫기 버튼 영역 */}
            <div className="form-actions">
              <button type="button" className="subtle-button" onClick={handleSuspensionMemoClose}>
                {/* "닫기" */}
                닫기
              </button>
            </div>
          </section>
        </section>
      )}
    </>
  )
}
