import { useEffect, useState } from 'react'
import type { FormEvent, MouseEvent } from 'react'
import {
  createCurrentUserSuspension,
  getCurrentUser,
  getCurrentUserLoginHistories,
  getCurrentUserSuspensions,
  getCurrentUserWithdrawalHistories,
  releaseCurrentUserSuspension,
} from '../../api/currentUserApi'
import { getCodeList } from '../../api/codeApi'
import { Pagination } from '../../components/Pagination'
import { SPND_RSON, SPND_TYPE } from '../../constants/codes'
import { CURRENT_USER_LIST_PATH } from '../../constants/routes'
import { useMenuPermission } from '../../contexts/useMenuPermission'
import type { Code } from '../../types/code'
import type { PageData } from '../../types/common'
import type {
  CurrentUser,
  CurrentUserLoginHistory,
  CurrentUserSuspension,
  CurrentUserSuspensionRequest,
  CurrentUserWithdrawalHistory,
} from '../../types/currentUser'
import { formatDate } from '../../utils/code'

type CurrentUserDetailPageProps = {
  userNumb: number
  adminAuthCode: string
  onMovePath: (path: string) => void
  onError: (message: string | null) => void
}

// 사용자 서버 반영 대기 상태일 때만 회원 상태 뒤에 붙일 안내 문구를 정의한다.
const USER_STATUS_SYNC_SUFFIX: Record<string, string> = {
  // " (반영 대기)"
  PENDING: ' (반영 대기)',
}

const emptyPage = <T,>(): PageData<T> => ({
  items: [],
  totalCount: 0,
  pageNumber: 1,
  pageSize: 20,
  totalPages: 0,
})

const createDefaultEndDate = (): string => {
  const endDate = new Date()
  endDate.setDate(endDate.getDate() + 7)
  endDate.setMinutes(0, 0, 0)
  const timezoneOffset = endDate.getTimezoneOffset() * 60_000
  return new Date(endDate.getTime() - timezoneOffset).toISOString().slice(0, 16)
}

/**
 * 현재 사용자 상세와 로그인·계정 처리 이력을 표시한다.
 *
 * @author SeungHyeon.Kang
 * @param userNumb 사용자 번호
 * @param adminAuthCode 로그인 관리자 권한 코드
 * @param onMovePath 화면 경로 이동 함수
 * @param onError 공통 오류 메시지 변경 함수
 * @return 현재 사용자 상세 화면
 */
export function CurrentUserDetailPage({
  userNumb,
  adminAuthCode,
  onMovePath,
  onError,
}: CurrentUserDetailPageProps) {
  const permission = useMenuPermission()
  const [currentUser, setCurrentUser] = useState<CurrentUser | null>(null)
  const [loginHistories, setLoginHistories] = useState<PageData<CurrentUserLoginHistory>>(emptyPage())
  const [withdrawalHistories, setWithdrawalHistories] = useState<PageData<CurrentUserWithdrawalHistory>>(emptyPage())
  const [suspensionHistories, setSuspensionHistories] = useState<PageData<CurrentUserSuspension>>(emptyPage())
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
  const [loading, setLoading] = useState(true)

  // 사용자 번호가 변경되면 상세와 두 이력의 첫 페이지를 함께 조회한다.
  useEffect(() => {
    let active = true
    // 상세 화면에 필요한 세 종류의 읽기 전용 데이터를 병렬로 조회한다.
    Promise.all([
      getCurrentUser(userNumb),
      getCurrentUserLoginHistories(userNumb, 1),
      getCurrentUserWithdrawalHistories(userNumb, 1),
      getCurrentUserSuspensions(userNumb, 1),
      getCodeList(SPND_TYPE),
      getCodeList(SPND_RSON),
    ])
      .then(([user, loginPage, withdrawalPage, suspensionPage, typeCodes, reasonCodes]) => {
        // 화면이 유지되는 동안에만 조회 결과를 반영한다.
        if (active) {
          setCurrentUser(user)
          setLoginHistories(loginPage)
          setWithdrawalHistories(withdrawalPage)
          setSuspensionHistories(suspensionPage)
          setSuspensionTypes(typeCodes.filter((code) => code.useeYsno !== 'N'))
          setSuspensionReasons(reasonCodes.filter((code) => code.useeYsno !== 'N'))
          setSuspensionForm((currentForm) => ({
            ...currentForm,
            spndRson: currentForm.spndRson || reasonCodes[0]?.comdCode || '',
          }))
          onError(null)
          setLoading(false)
        }
      })
      .catch((error: unknown) => {
        // 화면이 유지되는 동안에만 오류를 표시한다.
        if (active) {
          onError(error instanceof Error ? error.message : '현재 사용자 상세 정보를 불러오지 못했습니다.')
          setLoading(false)
        }
      })
    // 화면 해제 뒤 도착하는 응답이 상태를 변경하지 않도록 차단한다.
    return () => {
      active = false
    }
  }, [userNumb, onError])

  /**
   * 로그인 이력의 지정 페이지를 조회한다.
   *
   * @author SeungHyeon.Kang
   * @param pageNumber 조회할 페이지 번호
   * @return 반환값이 없다
   */
  const loadLoginHistoryPage = async (pageNumber: number): Promise<void> => {
    try {
      // 선택한 페이지의 마스킹 로그인 이력을 반영한다.
      setLoginHistories(await getCurrentUserLoginHistories(userNumb, pageNumber))
      onError(null)
    } catch (error: unknown) {
      // 이력 조회 실패를 공통 오류 영역에 표시한다.
      onError(error instanceof Error ? error.message : '로그인 이력을 불러오지 못했습니다.')
    }
  }

  /**
   * 계정 처리 이력의 지정 페이지를 조회한다.
   *
   * @author SeungHyeon.Kang
   * @param pageNumber 조회할 페이지 번호
   * @return 반환값이 없다
   */
  const loadWithdrawalHistoryPage = async (pageNumber: number): Promise<void> => {
    try {
      // 선택한 페이지의 비활성화·영구탈퇴 이력을 반영한다.
      setWithdrawalHistories(await getCurrentUserWithdrawalHistories(userNumb, pageNumber))
      onError(null)
    } catch (error: unknown) {
      // 이력 조회 실패를 공통 오류 영역에 표시한다.
      onError(error instanceof Error ? error.message : '계정 처리 이력을 불러오지 못했습니다.')
    }
  }

  /**
   * 이용 정지 이력의 지정 페이지를 조회한다.
   *
   * @author SeungHyeon.Kang
   * @param pageNumber 조회할 페이지 번호
   * @return 반환값이 없다
   */
  const loadSuspensionHistoryPage = async (pageNumber: number): Promise<void> => {
    try {
      setSuspensionHistories(await getCurrentUserSuspensions(userNumb, pageNumber))
      onError(null)
    } catch (error: unknown) {
      onError(error instanceof Error ? error.message : '이용 정지 이력을 불러오지 못했습니다.')
    }
  }

  /**
   * 회원 정지 등록 또는 해제 후 기본정보와 정지 이력을 다시 조회한다.
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없다
   */
  const refreshSuspensionState = async (): Promise<void> => {
    const [user, suspensionPage] = await Promise.all([
      getCurrentUser(userNumb),
      getCurrentUserSuspensions(userNumb, 1),
    ])
    setCurrentUser(user)
    setSuspensionHistories(suspensionPage)
  }

  /**
   * Outbox 처리 여부를 다시 조회하여 사용자 서버 상태 반영 결과를 갱신한다.
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없다
   */
  const handleRefreshUserStatusSync = async (): Promise<void> => {
    // 중복 조회를 막기 위해 반영 상태 확인 요청을 진행 상태로 전환한다.
    setRefreshingUserStatusSync(true)
    // 조회 실패를 공통 오류 영역으로 전달하고 화면의 기존 상태는 유지한다.
    try {
      // 사용자 상세와 정지 이력을 함께 다시 읽어 Outbox 처리 결과를 반영한다.
      await refreshSuspensionState()
      // 이전 조회 오류가 남지 않도록 공통 오류를 초기화한다.
      onError(null)
    } catch (error: unknown) {
      // "사용자 서버 반영 상태를 확인하지 못했습니다."
      onError(error instanceof Error ? error.message : '사용자 서버 반영 상태를 확인하지 못했습니다.')
    } finally {
      // 성공 여부와 관계없이 관리자가 다시 확인할 수 있도록 진행 상태를 해제한다.
      setRefreshingUserStatusSync(false)
    }
  }

  /**
   * 입력한 기간 또는 무기한 이용 정지를 현재 사용자에게 적용한다.
   *
   * @author SeungHyeon.Kang
   * @param event 정지 등록 폼 제출 이벤트
   * @return 반환값이 없다
   */
  const handleCreateSuspension = async (event: FormEvent<HTMLFormElement>): Promise<void> => {
    event.preventDefault()
    if (!suspensionForm.spndType || !suspensionForm.spndRson) {
      onError('정지 유형과 사유를 선택해주세요.')
      return
    }
    if (suspensionForm.spndType === 'PERIOD' && !suspensionForm.endxDate) {
      onError('기간 정지의 종료일시를 입력해주세요.')
      return
    }
    if (!window.confirm('이 사용자에게 이용 정지를 적용하시겠습니까?')) return

    setSavingSuspension(true)
    try {
      await createCurrentUserSuspension(userNumb, {
        ...suspensionForm,
        endxDate: suspensionForm.spndType === 'PERIOD' ? suspensionForm.endxDate : null,
      })
      await refreshSuspensionState()
      setSuspensionForm((form) => ({
        ...form,
        spndType: 'PERIOD',
        spndCntn: '',
        endxDate: createDefaultEndDate(),
      }))
      onError(null)
    } catch (error: unknown) {
      onError(error instanceof Error ? error.message : '이용 정지를 적용하지 못했습니다.')
    } finally {
      setSavingSuspension(false)
    }
  }

  /**
   * 적용 중인 회원 이용 정지를 관리자 해제한다.
   *
   * @author SeungHyeon.Kang
   * @param spndNumb 해제할 정지 이력 번호
   * @return 반환값이 없다
   */
  const handleReleaseSuspension = async (spndNumb: number): Promise<void> => {
    if (!window.confirm('이 사용자의 이용 정지를 즉시 해제하시겠습니까?')) return

    setSavingSuspension(true)
    try {
      await releaseCurrentUserSuspension(userNumb, spndNumb, releaseContent.trim())
      await refreshSuspensionState()
      setReleaseContent('')
      onError(null)
    } catch (error: unknown) {
      onError(error instanceof Error ? error.message : '이용 정지를 해제하지 못했습니다.')
    } finally {
      setSavingSuspension(false)
    }
  }

  /**
   * 선택한 이용 정지 이력의 정지 메모와 해제 메모 팝업을 연다.
   *
   * @author SeungHyeon.Kang
   * @param event 팝업 보기 버튼 클릭 이벤트
   * @return 반환값이 없다
   */
  const handleSuspensionMemoOpen = (event: MouseEvent<HTMLButtonElement>): void => {
    const suspensionNumber = Number(event.currentTarget.dataset.suspensionNumber)
    const selectedHistory = suspensionHistories.items.find((history) => history.spndNumb === suspensionNumber)

    // 현재 조회 중인 이력에서 선택한 항목을 찾은 경우에만 팝업을 연다.
    if (!selectedHistory) {
      return
    }

    // 관리자가 선택한 정지 이력의 메모를 팝업에 표시한다.
    setSelectedSuspensionMemo(selectedHistory)
  }

  /**
   * 이용 정지 이력 메모 팝업을 닫는다.
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없다
   */
  const handleSuspensionMemoClose = (): void => {
    // 선택한 이력을 초기화하여 메모 팝업을 닫는다.
    setSelectedSuspensionMemo(null)
  }

  /**
   * 로그인 이력 행을 표시한다.
   *
   * @author SeungHyeon.Kang
   * @param history 로그인 이력
   * @return 로그인 이력 행
   */
  const renderLoginHistoryRow = (history: CurrentUserLoginHistory) => (
    <tr key={history.lognNumb}>
      <td className="col-history-number">{history.lognNumb}</td>
      <td className="col-date-time">{formatDate(history.lognDate)}</td>
      <td>{history.lognIpxx ?? '-'}</td>
      <td>{history.provCodeName ?? history.provCode ?? '-'}</td>
      <td className="user-agent-cell" title={history.userAgnt ?? ''}>{history.userAgnt ?? '-'}</td>
    </tr>
  )

  /**
   * 계정 처리 이력 행을 표시한다.
   *
   * @author SeungHyeon.Kang
   * @param history 계정 처리 이력
   * @return 계정 처리 이력 행
   */
  const renderWithdrawalHistoryRow = (history: CurrentUserWithdrawalHistory) => (
    <tr key={history.wthdNumb}>
      <td className="col-history-number">{history.wthdNumb}</td>
      <td>{history.wthdTypeName ?? history.wthdType}</td>
      <td>{history.wthdRsonName ?? history.wthdRson ?? '-'}</td>
      <td>{history.wthdStatName ?? history.wthdStat}</td>
      <td className="col-date-time">{formatDate(history.requDate)}</td>
      <td className="col-date-time">{formatDate(history.deltDate)}</td>
      <td className="col-date-time">{formatDate(history.procDate ?? history.rcovDate)}</td>
    </tr>
  )

  /**
   * 이용 정지 이력 행을 표시한다.
   *
   * @author SeungHyeon.Kang
   * @param history 이용 정지 이력
   * @return 이용 정지 이력 행
   */
  const renderSuspensionHistoryRow = (history: CurrentUserSuspension) => (
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

  const activeSuspension = suspensionHistories.items.find((history) => history.spndStat === 'ACTIVE')

  // 상세 정보가 도착하기 전에는 고정 높이 안내를 표시한다.
  if (loading) {
    return <section className="empty">현재 사용자 상세 정보를 불러오고 있습니다.</section>
  }

  // 상세 조회가 실패해 데이터가 없으면 목록으로 돌아갈 수 있는 안내를 표시한다.
  if (!currentUser) {
    return (
      <section className="detail-panel">
        <p className="empty small">현재 사용자 정보를 표시할 수 없습니다.</p>
        <div className="detail-footer">
          <button type="button" className="subtle-button" onClick={() => onMovePath(CURRENT_USER_LIST_PATH)}>목록</button>
        </div>
      </section>
    )
  }

  const userStatusName = currentUser.userStatName ?? currentUser.userStat
  const userStatusSyncSuffix = USER_STATUS_SYNC_SUFFIX[currentUser.userStatusSyncStat] ?? ''
  const userStatusWithSyncName = `${userStatusName}${userStatusSyncSuffix}`
  const hasSuspensionHistory = suspensionHistories.items.length > 0
  const isUserStatusSyncPending = currentUser.userStatusSyncStat === 'PENDING'

  // 현재 사용자 기본정보와 활동 요약, 두 종류의 이력을 표시한다.
  return (
    <section className="current-user-page">
      {/* 상세 화면 제목 */}
      <section className="content-header">
        <h1>현 사용자 상세</h1>
      </section>

      {/* 사용자 계정과 프로필 기본정보 */}
      <section className="detail-panel">
        <div className="detail-title">
          <div>
            <h2>기본 정보</h2>
            <p>암호화된 외부 사용자 식별값은 관리자 화면과 API에 제공하지 않습니다.</p>
          </div>
        </div>
        <section className="table-wrap menu-info-table current-user-info-table">
          <table>
            <tbody>
              <tr>
                <th>회원번호</th>
                <td>{currentUser.userNumb}</td>
                <th>닉네임</th>
                <td>{currentUser.userNick}</td>
                <th>회원 상태</th>
                <td>{userStatusWithSyncName}</td>
              </tr>
              <tr>
                <th>가입 제공자</th>
                <td>{currentUser.userProvName ?? currentUser.userProv ?? '-'}</td>
                <th>권한</th>
                <td>{currentUser.userRole ?? '-'}</td>
                <th>온보딩</th>
                <td>{currentUser.onbdYsnoName ?? currentUser.onbdYsno}</td>
              </tr>
              <tr>
                <th>가입일</th>
                <td>{formatDate(currentUser.joinDate)}</td>
                <th>최근 로그인</th>
                <td>{formatDate(currentUser.lastLognDate) || '-'}</td>
                <th>비활성화 요청일</th>
                <td>{formatDate(currentUser.wthdDate) || '-'}</td>
              </tr>
              <tr>
                <th>삭제 예정일</th>
                <td>{formatDate(currentUser.deltDate) || '-'}</td>
                <th>프로필 이미지</th>
                <td>{currentUser.profPath ? '등록' : '미등록'}</td>
                <th>배경 이미지</th>
                <td>{currentUser.bgimPath ? '등록' : '미등록'}</td>
              </tr>
              <tr>
                <th>한줄 소개</th>
                <td colSpan={5}>{currentUser.intrCntn ?? '-'}</td>
              </tr>
            </tbody>
          </table>
        </section>
      </section>

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
                  onClick={handleRefreshUserStatusSync}
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
              ) : suspensionHistories.items.map(renderSuspensionHistoryRow)}
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

      {/* 서비스 활동 건수 요약 */}
      <section className="detail-panel">
        <div className="detail-title">
          <div>
            <h2>활동 요약</h2>
            <p>현재 보관 중인 사용자 활동 데이터의 건수입니다.</p>
          </div>
        </div>
        <div className="current-user-activity-grid">
          <div><span>독후감</span><strong>{currentUser.reportCntt.toLocaleString()}</strong></div>
          <div><span>댓글</span><strong>{currentUser.replyCntt.toLocaleString()}</strong></div>
          <div><span>좋아요</span><strong>{currentUser.likeCntt.toLocaleString()}</strong></div>
          <div><span>팔로잉</span><strong>{currentUser.followingCntt.toLocaleString()}</strong></div>
          <div><span>팔로워</span><strong>{currentUser.followerCntt.toLocaleString()}</strong></div>
          <div><span>목표</span><strong>{currentUser.goalCntt.toLocaleString()}</strong></div>
          <div><span>푸시 구독</span><strong>{currentUser.pushCntt.toLocaleString()}</strong></div>
        </div>
      </section>

      {/* 마스킹된 로그인 이력 */}
      <section className="detail-panel">
        <div className="detail-title">
          <div>
            <h2>로그인 이력</h2>
            <p>접속 IP는 서버에서 일부 마스킹된 값만 표시합니다.</p>
          </div>
          <div className="status">총 {loginHistories.totalCount.toLocaleString()}건</div>
        </div>
        <section className="table-wrap current-user-history-table">
          <table>
            <thead>
              <tr>
                <th className="col-history-number">번호</th>
                <th className="col-date-time">로그인 일시</th>
                <th>접속 IP</th>
                <th>제공자</th>
                <th>브라우저 정보</th>
              </tr>
            </thead>
            <tbody>
              {loginHistories.items.length === 0 ? (
                <tr className="empty-row"><td colSpan={5}>로그인 이력이 없습니다.</td></tr>
              ) : loginHistories.items.map(renderLoginHistoryRow)}
            </tbody>
          </table>
        </section>
        <Pagination
          pageNumber={loginHistories.pageNumber}
          totalPages={loginHistories.totalPages}
          onPageChange={(pageNumber) => void loadLoginHistoryPage(pageNumber)}
        />
      </section>

      {/* 비활성화와 영구탈퇴 계정 처리 이력 */}
      <section className="detail-panel">
        <div className="detail-title">
          <div>
            <h2>계정 처리 이력</h2>
            <p>자유 입력 사유와 내부 오류문구는 개인정보 보호를 위해 표시하지 않습니다.</p>
          </div>
          <div className="status">총 {withdrawalHistories.totalCount.toLocaleString()}건</div>
        </div>
        <section className="table-wrap current-user-history-table">
          <table>
            <thead>
              <tr>
                <th className="col-history-number">번호</th>
                <th>처리 방식</th>
                <th>사유</th>
                <th>상태</th>
                <th className="col-date-time">요청일</th>
                <th className="col-date-time">삭제 예정일</th>
                <th className="col-date-time">처리·복구일</th>
              </tr>
            </thead>
            <tbody>
              {withdrawalHistories.items.length === 0 ? (
                <tr className="empty-row"><td colSpan={7}>계정 처리 이력이 없습니다.</td></tr>
              ) : withdrawalHistories.items.map(renderWithdrawalHistoryRow)}
            </tbody>
          </table>
        </section>
        <Pagination
          pageNumber={withdrawalHistories.pageNumber}
          totalPages={withdrawalHistories.totalPages}
          onPageChange={(pageNumber) => void loadWithdrawalHistoryPage(pageNumber)}
        />
      </section>

      {/* 현재 사용자 목록 복귀 */}
      <div className="detail-footer">
        <div className="detail-footer-left">
          <button type="button" className="subtle-button" onClick={() => onMovePath(CURRENT_USER_LIST_PATH)}>목록</button>
        </div>
      </div>
    </section>
  )
}
