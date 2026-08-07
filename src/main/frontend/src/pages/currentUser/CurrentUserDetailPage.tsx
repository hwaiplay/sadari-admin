import { useEffect, useState } from 'react'
import {
  createCurrentUserSuspension,
  getCurrentUser,
  getCurrentUserLoginHistories,
  getCurrentUserWithdrawalHistories,
  getCurrentUserSuspensions,
  releaseCurrentUserSuspension,
} from '../../api/currentUserApi'
import { Pagination } from '../../components/Pagination'
import { ImagePreviewButton } from '../../components/ImagePreviewButton'
import { UserSuspensionPanel } from '../../components/UserSuspensionPanel'
import { CURRENT_USER_LIST_PATH } from '../../constants/routes'
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
  const [currentUser, setCurrentUser] = useState<CurrentUser | null>(null)
  const [loginHistories, setLoginHistories] = useState<PageData<CurrentUserLoginHistory>>(emptyPage())
  const [withdrawalHistories, setWithdrawalHistories] = useState<PageData<CurrentUserWithdrawalHistory>>(emptyPage())
  const [loading, setLoading] = useState(true)

  // 사용자 번호가 변경되면 상세와 두 이력의 첫 페이지를 함께 조회한다.
  useEffect(() => {
    let active = true
    // 상세 화면에 필요한 사용자 정보와 두 종류의 이력을 병렬로 조회한다.
    Promise.all([
      getCurrentUser(userNumb),
      getCurrentUserLoginHistories(userNumb, 1),
      getCurrentUserWithdrawalHistories(userNumb, 1),
    ])
      .then(([user, loginPage, withdrawalPage]) => {
        // 화면이 유지되는 동안에만 조회 결과를 반영한다.
        if (active) {
          setCurrentUser(user)
          setLoginHistories(loginPage)
          setWithdrawalHistories(withdrawalPage)
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
    return createCurrentUserSuspension(userNumb, request)
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
    return releaseCurrentUserSuspension(userNumb, spndNumb, rlesCntn)
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
                  {/* "프로필 사진 보기" */}
                  <ImagePreviewButton
                    imagePath={currentUser.profPath}
                    buttonLabel="프로필 사진 보기"
                    dialogTitle={`${currentUser.userNick} 프로필 사진`}
                    emptyLabel="미등록"
                  />
                </td>
                <th>배경 이미지</th>
                <td>
                  {/* "배경화면 보기" */}
                  <ImagePreviewButton
                    imagePath={currentUser.bgimPath}
                    buttonLabel="배경화면 보기"
                    dialogTitle={`${currentUser.userNick} 배경화면`}
                    emptyLabel="미등록"
                  />
                </td>
              </tr>
              <tr>
                <th>한줄 소개</th>
                <td colSpan={5}>{currentUser.intrCntn ?? '-'}</td>
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
