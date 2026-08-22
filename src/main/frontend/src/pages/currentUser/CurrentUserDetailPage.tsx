import { useEffect, useState } from 'react'
import {
  delUserBgimImage,
  delUserIntroduction,
  delUserProfImage,
  setCurrentUserSuspension,
  getCurrentUser,
  getUserComplaintList,
  getUserLoginHistoryList,
  getUserWithdrawalList,
  getCurrentUserSuspensions,
  uptCurrentUserSuspRelease,
} from '../../api/currentUserApi'
import { Pagination } from '../../components/Pagination'
import { ImagePreviewButton } from '../../components/ImagePreviewButton'
import { UserSuspensionPanel } from '../../components/UserSuspensionPanel'
import { CURRENT_USER_LIST_PATH } from '../../constants/routes'
import { useMenuPermission } from '../../contexts/useMenuPermission'
import type { PageData } from '../../types/common'
import type {
  CurrentUser,
  CurrentUserComplaint,
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
  // 현재 사용자 메뉴의 삭제 권한을 프로필 정보 조치 버튼 노출 기준으로 조회한다.
  const permission = useMenuPermission()
  const [currentUser, setCurrentUser] = useState<CurrentUser | null>(null)
  const [loginHistories, setLoginHistories] = useState<PageData<CurrentUserLoginHistory>>(emptyPage())
  const [withdrawalHistories, setWithdrawalHistories] = useState<PageData<CurrentUserWithdrawalHistory>>(emptyPage())
  const [complaintHistories, setComplaintHistories] = useState<PageData<CurrentUserComplaint>>(emptyPage())
  const [loading, setLoading] = useState(true)
  const [moderating, setModerating] = useState<string | null>(null)

  // 사용자 번호가 변경되면 상세와 세 이력의 첫 페이지를 함께 조회한다.
  useEffect(() => {
    let active = true
    // 상세 화면에 필요한 사용자 정보와 로그인 및 계정 처리 및 받은 신고 이력을 병렬로 조회한다.
    Promise.all([
      getCurrentUser(userNumb),
      getUserLoginHistoryList(userNumb, 1),
      getUserWithdrawalList(userNumb, 1),
      getUserComplaintList(userNumb, 1),
    ])
      .then(([user, loginPage, withdrawalPage, complaintPage]) => {
        // 화면이 유지되는 동안에만 조회 결과를 반영한다.
        if (active) {
          setCurrentUser(user)
          setLoginHistories(loginPage)
          setWithdrawalHistories(withdrawalPage)
          setComplaintHistories(complaintPage)
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
      setLoginHistories(await getUserLoginHistoryList(userNumb, pageNumber))
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
      setWithdrawalHistories(await getUserWithdrawalList(userNumb, pageNumber))
      onError(null)
    } catch (error: unknown) {
      // 이력 조회 실패를 공통 오류 영역에 표시한다.
      onError(error instanceof Error ? error.message : '계정 처리 이력을 불러오지 못했습니다.')
    }
  }

  /**
   * 받은 신고 이력의 지정 페이지를 조회한다.
   *
   * @author SeungHyeon.Kang
   * @param pageNumber 조회할 페이지 번호
   * @return 반환값이 없다
   */
  const loadComplaintHistoryPage = async (pageNumber: number): Promise<void> => {
    // 신고 대상 소유자 기준으로 연결된 지정 페이지 이력을 조회한다.
    try {
      // 선택한 페이지의 받은 신고 이력을 반영한다.
      setComplaintHistories(await getUserComplaintList(userNumb, pageNumber))
      // 이전 조회 오류를 제거한다.
      onError(null)
    }

    // 받은 신고 이력 조회 실패를 현재 상세 화면에 표시한다.
    catch (error: unknown) {
      // 서버가 제공한 안전한 오류 문구를 공통 오류 영역에 표시한다.
      onError(error instanceof Error ? error.message : '신고 이력을 불러오지 못했습니다.')
    }
  }

  /**
   * 공통 이용정지 패널에 현재 사용자의 이력 페이지를 제공한다
   *
   * @author SeungHyeon.Kang
   * @param pageNumber 조회할 페이지 번호
   * @return 현재 사용자의 이용정지 이력 페이지
   */
  const getUserSuspensions = (pageNumber: number): Promise<PageData<CurrentUserSuspension>> => {
    // 현재 사용자 관리 API에서 지정 페이지의 이용정지 이력을 반환한다
    return getCurrentUserSuspensions(userNumb, pageNumber)
  }

  /**
   * 공통 이용정지 패널에서 현재 사용자에게 이용정지를 적용한다
   *
   * @author SeungHyeon.Kang
   * @param request 정지 유형과 사유 및 기간
   * @return 등록된 이용정지 이력
   */
  const setUserSuspension = (
    request: CurrentUserSuspensionRequest,
  ): Promise<CurrentUserSuspension> => {
    // 현재 사용자 관리 API에 검증된 이용정지 등록값을 전달한다
    return setCurrentUserSuspension(userNumb, request)
  }

  /**
   * 공통 이용정지 패널에서 현재 사용자의 적용 중인 정지를 해제한다
   *
   * @author SeungHyeon.Kang
   * @param spndNumb 정지 이력 번호
   * @param rlesCntn 관리자 내부 해제 메모
   * @return 반환값이 없다
   */
  const uptUserSuspensionReleased = (spndNumb: number, rlesCntn: string): Promise<void> => {
    // 현재 사용자 관리 API에 해제할 이력 번호와 내부 메모를 전달한다
    return uptCurrentUserSuspRelease(userNumb, spndNumb, rlesCntn)
  }

  /**
   * 이용정지 변경 뒤 현재 사용자 상태를 다시 조회한다
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없다
   */
  const refreshCurrentUser = async (): Promise<void> => {
    // 이용정지 상태와 사용자 서버 반영 결과가 포함된 현재 사용자 상세를 갱신한다
    setCurrentUser(await getCurrentUser(userNumb))
  }

  /**
   * 현재 사용자의 프로필 정보 삭제 조치를 확인하고 최신 상세를 반영한다.
   *
   * @author SeungHyeon.Kang
   * @param actionKey 실행할 조치 식별값
   * @param confirmMessage 관리자 확인 문구
   * @param action 실행할 삭제 API
   * @param fallbackMessage 기본 실패 문구
   * @return 반환값이 없다
   */
  const handleModeration = async (
    actionKey: string,
    confirmMessage: string,
    action: () => Promise<CurrentUser>,
    fallbackMessage: string,
  ): Promise<void> => {
    // 프로필 정보 삭제는 자동 복원되지 않으므로 실행 전에 관리자 확인을 받는다.
    if (!window.confirm(confirmMessage)) {
      // 관리자가 취소한 삭제 조치는 실행하지 않고 종료한다.
      return
    }

    // 다른 삭제 버튼의 중복 실행을 막도록 현재 조치 식별값을 설정한다.
    setModerating(actionKey)
    // 서버 조치 결과를 현재 사용자 상세에 안전하게 반영한다.
    try {
      // 서버가 현재 사용자와 파일 참조를 다시 검증한 조치 결과를 조회한다.
      const updatedUser = await action()
      // 프로필 정보가 갱신된 최신 사용자 상세를 반영한다.
      setCurrentUser(updatedUser)
      // 이전 현재 사용자 상세 오류 메시지를 초기화한다.
      onError(null)
    }

    // 서버가 거절한 조치 사유 또는 기본 실패 메시지를 화면에 표시한다.
    catch (error: unknown) {
      // 조치 실패 원인을 현재 사용자 관리 화면 공통 오류 영역에 설정한다.
      onError(error instanceof Error ? error.message : fallbackMessage)
    }

    // 성공과 실패 모두에서 다른 관리자 조치를 다시 허용한다.
    finally {
      // 현재 조치 식별값을 초기화한다.
      setModerating(null)
    }
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
  const renderWithdrawalRow = (history: CurrentUserWithdrawalHistory) => (
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
   * 현재 사용자와 사용자 작성 대상이 받은 신고 이력 행을 표시한다.
   *
   * @author SeungHyeon.Kang
   * @param complaint 표시할 받은 신고 이력
   * @return 받은 신고 이력 행
   */
  const renderComplaintRow = (complaint: CurrentUserComplaint) => (
    <tr key={complaint.cmplNumb}>
      <td className="col-history-number">{complaint.cmplNumb}</td>
      <td>{complaint.tagtTypeName ?? complaint.tagtType}</td>
      <td className="complaint-content-cell current-user-complaint-content">{complaint.tagtCntn || '내용 없음'}</td>
      <td>{complaint.reporterUserNumb ? `${complaint.reporterNick ?? '닉네임 없음'} (${complaint.reporterUserNumb})` : '탈퇴한 사용자'}</td>
      <td>{complaint.cmplRsonName ?? complaint.cmplRson}</td>
      <td className="complaint-content-cell current-user-complaint-detail">{complaint.cmplCntn || '-'}</td>
      <td><span className={`complaint-status ${complaint.cmplStat.toLowerCase()}`}>{complaint.cmplStatName ?? complaint.cmplStat}</span></td>
      <td className="col-date-time">{formatDate(complaint.regiDate)}</td>
      <td className="col-date-time">{formatDate(complaint.procDate) || '-'}</td>
    </tr>
  )

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
                <td>
                  {/* 현재 사용자의 프로필 사진 보기와 삭제 조치 영역 */}
                  <div className="image-preview-actions">
                    {/* "프로필 사진 보기" */}
                    <ImagePreviewButton
                      imagePath={currentUser.profPath}
                      buttonLabel="프로필 사진 보기"
                      dialogTitle={`${currentUser.userNick} 프로필 사진`}
                      emptyLabel="미등록"
                    />
                    {permission.deltYsno === 'Y' && currentUser.profPath && (
                      <button
                        type="button"
                        className="delete-button"
                        disabled={moderating !== null}
                        onClick={() => void handleModeration(
                          'profile-image',
                          '현재 사용자의 프로필 사진을 삭제하고 기본 프로필로 변경하시겠습니까?',
                          () => delUserProfImage(userNumb),
                          '프로필 사진을 삭제하지 못했습니다.',
                        )}
                      >
                        {moderating === 'profile-image' ? '삭제 중' : '프로필 사진 삭제'}
                      </button>
                    )}
                  </div>
                </td>
                <th>배경 이미지</th>
                <td>
                  {/* 현재 사용자의 배경화면 보기와 삭제 조치 영역 */}
                  <div className="image-preview-actions">
                    {/* "배경화면 보기" */}
                    <ImagePreviewButton
                      imagePath={currentUser.bgimPath}
                      buttonLabel="배경화면 보기"
                      dialogTitle={`${currentUser.userNick} 배경화면`}
                      emptyLabel="미등록"
                    />
                    {permission.deltYsno === 'Y' && currentUser.bgimPath && (
                      <button
                        type="button"
                        className="delete-button"
                        disabled={moderating !== null}
                        onClick={() => void handleModeration(
                          'background-image',
                          '현재 사용자의 배경화면을 삭제하고 기본 배경으로 변경하시겠습니까?',
                          () => delUserBgimImage(userNumb),
                          '배경화면을 삭제하지 못했습니다.',
                        )}
                      >
                        {moderating === 'background-image' ? '삭제 중' : '배경화면 삭제'}
                      </button>
                    )}
                  </div>
                </td>
              </tr>
              <tr>
                <th>한줄 소개</th>
                <td colSpan={5}>
                  {/* 현재 사용자의 한줄 소개와 삭제 조치 영역 */}
                  <div className="complaint-moderation-content">
                    <span className="complaint-content-cell">{currentUser.intrCntn ?? '-'}</span>
                    {permission.deltYsno === 'Y' && currentUser.intrCntn && (
                      <button
                        type="button"
                        className="delete-button"
                        disabled={moderating !== null}
                        onClick={() => void handleModeration(
                          'introduction',
                          '현재 사용자의 한줄 소개를 삭제하시겠습니까?',
                          () => delUserIntroduction(userNumb),
                          '한줄 소개를 삭제하지 못했습니다.',
                        )}
                      >
                        {moderating === 'introduction' ? '삭제 중' : '한줄 소개 삭제'}
                      </button>
                    )}
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </section>
      </section>

      {/* 현재 사용자 이용 정지 등록과 해제 및 이력 */}
      <UserSuspensionPanel
        key={userNumb}
        contextKey={userNumb}
        targetUser={currentUser}
        adminAuthCode={adminAuthCode}
        getSuspensions={getUserSuspensions}
        createSuspension={setUserSuspension}
        releaseSuspension={uptUserSuspensionReleased}
        onRefreshTargetUser={refreshCurrentUser}
        onError={onError}
      />
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
          <div><span>누적 신고</span><strong>{complaintHistories.totalCount.toLocaleString()}</strong></div>
        </div>
      </section>

      {/* 현재 사용자와 사용자 작성 대상이 받은 신고 누적 횟수와 이력 */}
      <section className="detail-panel">
        <div className="detail-title">
          <div>
            <h2>신고 이력</h2>
            <p>사용자 본인과 사용자가 작성한 독후감·댓글이 받은 신고를 접수 당시 내용으로 표시합니다.</p>
          </div>
          <div className="status">누적 {complaintHistories.totalCount.toLocaleString()}건</div>
        </div>
        {/* 받은 신고 이력 목록 */}
        <section className="table-wrap current-user-history-table current-user-complaint-table">
          <table>
            <thead>
              <tr>
                <th className="col-history-number">신고번호</th>
                <th>대상 유형</th>
                <th>신고 대상 내용</th>
                <th>신고자</th>
                <th>신고 사유</th>
                <th>신고 내용</th>
                <th>처리 상태</th>
                <th className="col-date-time">접수일</th>
                <th className="col-date-time">처리일</th>
              </tr>
            </thead>
            <tbody>
              {complaintHistories.items.length === 0 ? (
                <tr className="empty-row"><td colSpan={9}>신고 이력이 없습니다.</td></tr>
              ) : complaintHistories.items.map(renderComplaintRow)}
            </tbody>
          </table>
        </section>
        <Pagination
          pageNumber={complaintHistories.pageNumber}
          totalPages={complaintHistories.totalPages}
          onPageChange={(pageNumber) => void loadComplaintHistoryPage(pageNumber)}
        />
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
              ) : withdrawalHistories.items.map(renderWithdrawalRow)}
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
