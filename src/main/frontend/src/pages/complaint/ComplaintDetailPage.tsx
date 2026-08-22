import { useEffect, useState } from 'react'
import {
  delComplaintBgimImage,
  delComplaintIntroduction,
  delComplaintProfImage,
  delComplaintTargetContent,
  setComplaintSuspension,
  getComplaint,
  getComplaintSuspList,
  uptComplaintSuspReleased,
  updateComplaint,
} from '../../api/complaintApi'
import { ImagePreviewButton } from '../../components/ImagePreviewButton'
import { UserSuspensionPanel } from '../../components/UserSuspensionPanel'
import { COMPLAINT_LIST_PATH } from '../../constants/routes'
import { useMenuPermission } from '../../contexts/useMenuPermission'
import type { PageData } from '../../types/common'
import type { Complaint, ComplaintAction, ComplaintDetail } from '../../types/complaint'
import type { CurrentUserSuspension, CurrentUserSuspensionRequest } from '../../types/currentUser'
import { formatDate } from '../../utils/code'

type ComplaintDetailPageProps = {
  cmplNumb: number
  adminNumb: number
  adminAuthCode: string
  onMovePath: (path: string) => void
  onError: (message: string | null) => void
}

// 신고 대상 유형별로 접수 당시 저장된 스냅샷의 실제 의미를 표시한다
const TARGET_CONTENT_LABELS: Record<string, string> = {
  CMPL_USER: '접수 당시 프로필 내용',
  CMPL_BOOK_REPORT: '접수 당시 독후감 원문',
  CMPL_REPLY: '접수 당시 댓글·답글 원문',
  CMPL_CLUB: '접수 당시 독서 모임 소개',
  CMPL_PROF_IMAGE: '접수 당시 프로필 사진',
  CMPL_INTRO: '접수 당시 한줄소개 원문',
}

// 신고 대상 유형별 현재 원본 삭제 버튼 문구를 표시한다
const TARGET_DELETE_LABELS: Record<string, string> = {
  CMPL_BOOK_REPORT: '독후감 완전 삭제',
  CMPL_REPLY: '댓글 삭제 처리',
  CMPL_CLUB: '모임 소개 삭제',
}

// 신고 대상 유형별 삭제 결과를 명확히 안내하는 확인 문구를 표시한다
const TARGET_DELETE_CONFIRM_MESSAGES: Record<string, string> = {
  CMPL_BOOK_REPORT: '신고 대상 독후감과 연결된 댓글 및 좋아요를 완전히 삭제하시겠습니까? 이 작업은 복구할 수 없습니다.',
  CMPL_REPLY: '신고 대상 댓글을 삭제 상태로 변경하시겠습니까?',
  CMPL_CLUB: '신고 대상 모임의 소개 내용을 삭제하시겠습니까?',
}

// 현재 원본과 처리 이력을 반영한 자동조치 진행 상태를 관리자용 문구로 표시한다
const AUTO_ACTION_PROGRESS_LABELS: Record<string, string> = {
  // "자동조치 누적 진행 중"
  PENDING: '자동조치 누적 진행 중',
  // "자동조치 완료"
  AUTO_ACTIONED: '자동조치 완료',
  // "관리자 수동조치 완료"
  MANUAL_ACTIONED: '관리자 수동조치 완료',
  // "신고 당시 대상 버전 비노출"
  VERSION_CHANGED: '신고 당시 대상 버전 비노출',
  // "신고 대상 원본 없음"
  TARGET_MISSING: '신고 대상 원본 없음',
}

/**
 * 신고 접수 정보와 처리 및 사용자 신고 대상 이용정지 기능을 제공한다
 *
 * @author SeungHyeon.Kang
 * @param cmplNumb 신고 번호
 * @param adminNumb 로그인 관리자 번호
 * @param adminAuthCode 로그인 관리자 권한 코드
 * @param onMovePath 화면 경로 이동 함수
 * @param onError 공통 오류 메시지 변경 함수
 * @return 관리자 신고 상세 화면
 */
export function ComplaintDetailPage({
  cmplNumb,
  adminNumb,
  adminAuthCode,
  onMovePath,
  onError,
}: ComplaintDetailPageProps) {
  // 현재 신고 메뉴의 쓰기 권한을 처리 버튼 노출 기준으로 조회한다
  const permission = useMenuPermission()
  const [detail, setDetail] = useState<ComplaintDetail | null>(null)
  const [processContent, setProcessContent] = useState('')
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [moderating, setModerating] = useState<string | null>(null)

  // 신고번호가 변경되면 해당 신고의 최신 상세를 조회한다
  useEffect(() => {
    let active = true
    // 신고 처리와 동일 대상 판단 정보를 함께 조회한다
    getComplaint(cmplNumb)
      .then((loadedDetail) => {
        // 화면이 유지되는 동안에만 신고 상세를 반영한다
        if (active) {
          // 조회된 신고 상세를 현재 화면에 설정한다
          setDetail(loadedDetail)
          // 기존 처리 메모가 있으면 최종 처리 입력에 표시한다
          setProcessContent(loadedDetail.complaint.procCntn ?? '')
          // 이전 신고 상세 조회 오류를 제거한다
          onError(null)
          // 신고 상세 로딩을 종료한다
          setLoading(false)
        }
      })
      .catch((error: unknown) => {
        // 화면이 유지되는 동안에만 신고 상세 조회 오류를 표시한다
        if (active) {
          // "신고 상세를 불러오지 못했습니다."
          onError(error instanceof Error ? error.message : '신고 상세를 불러오지 못했습니다.')
          // 오류 상태에서도 신고 상세 로딩을 종료한다
          setLoading(false)
        }
      })
    // 화면 해제 뒤 도착하는 응답이 상태를 변경하지 않도록 차단한다
    return () => {
      active = false
    }
  }, [cmplNumb, onError])

  /**
   * 처리 또는 이용정지 변경 뒤 신고 상세를 다시 조회한다
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없다
   */
  const refreshComplaint = async (): Promise<void> => {
    // 신고 대상 회원 상태와 처리 상태가 포함된 최신 상세를 조회한다
    const refreshedDetail = await getComplaint(cmplNumb)
    // 최신 신고 상세를 화면에 반영한다
    setDetail(refreshedDetail)
  }

  /**
   * 신고를 현재 로그인 관리자의 검토 중 상태로 변경한다
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없다
   */
  const handleStartReview = async (): Promise<void> => {
    // 처리 대상 신고가 없으면 상태 변경을 요청하지 않는다
    if (!detail) {
      // 상세가 없는 검토 시작 요청을 종료한다
      return
    }

    // "이 신고의 검토를 시작하시겠습니까?"
    if (!window.confirm('이 신고의 검토를 시작하시겠습니까?')) {
      // 관리자가 취소한 검토 시작 요청을 종료한다
      return
    }

    // 신고 처리 버튼의 중복 제출을 막는다
    setSaving(true)
    // 상태 변경 실패 시 기존 신고 상세을 유지하고 공통 오류를 표시한다
    try {
      // 화면이 조회한 수정일시와 검토 중 상태를 서버에 전달한다
      const updatedDetail = await updateComplaint(cmplNumb, {
        cmplStat: 'CMPL_REVIEWING',
        procCntn: '',
        updtDate: detail.complaint.updtDate,
      })
      // 담당자와 최신 수정일시가 반영된 신고 상세를 설정한다
      setDetail(updatedDetail)
      // 이전 신고 처리 오류를 제거한다
      onError(null)
    } catch (error: unknown) {
      // "신고 검토를 시작하지 못했습니다."
      onError(error instanceof Error ? error.message : '신고 검토를 시작하지 못했습니다.')
    } finally {
      // 성공 여부와 관계없이 신고 처리 진행 상태를 해제한다
      setSaving(false)
    }
  }

  /**
   * 검토 중인 신고를 조치 완료 또는 반려 상태로 최종 처리한다
   *
   * @author SeungHyeon.Kang
   * @param nextStatus 변경할 최종 처리 상태
   * @return 반환값이 없다
   */
  const handleFinishComplaint = async (nextStatus: 'CMPL_ACTIONED' | 'CMPL_REJECTED'): Promise<void> => {
    // 처리 대상 신고가 없으면 최종 상태 변경을 요청하지 않는다
    if (!detail) {
      // 상세가 없는 최종 처리 요청을 종료한다
      return
    }

    // 최종 처리에는 관리자 판단 근거가 반드시 남아야 한다
    if (!processContent.trim()) {
      // "처리 내용을 입력해 주세요."
      onError('처리 내용을 입력해 주세요.')
      // 빈 처리 내용의 최종 상태 변경을 종료한다
      return
    }

    // 상태에 맞는 최종 처리 확인 문구를 생성한다
    const confirmMessage = nextStatus === 'CMPL_ACTIONED'
      ? '이 신고를 조치 완료로 처리하시겠습니까?'
      : '이 신고를 반려 처리하시겠습니까?'
    // 조치 완료와 반려는 다시 열지 않으므로 관리자 확인을 받는다
    if (!window.confirm(confirmMessage)) {
      // 관리자가 취소한 최종 처리 요청을 종료한다
      return
    }

    // 신고 처리 버튼의 중복 제출을 막는다
    setSaving(true)
    // 상태 변경 실패 시 입력한 처리 메모를 유지하고 공통 오류를 표시한다
    try {
      // 화면이 조회한 수정일시와 최종 상태 및 처리 내용을 서버에 전달한다
      const updatedDetail = await updateComplaint(cmplNumb, {
        cmplStat: nextStatus,
        procCntn: processContent.trim(),
        updtDate: detail.complaint.updtDate,
      })
      // 최종 처리자와 처리일시가 반영된 신고 상세를 설정한다
      setDetail(updatedDetail)
      // 서버에 저장된 처리 내용으로 입력값을 동기화한다
      setProcessContent(updatedDetail.complaint.procCntn ?? '')
      // 이전 신고 처리 오류를 제거한다
      onError(null)
    } catch (error: unknown) {
      // "신고를 최종 처리하지 못했습니다."
      onError(error instanceof Error ? error.message : '신고를 최종 처리하지 못했습니다.')
    } finally {
      // 성공 여부와 관계없이 신고 처리 진행 상태를 해제한다
      setSaving(false)
    }
  }

  /**
   * 공통 이용정지 패널에 사용자 신고 대상의 이력 페이지를 제공한다
   *
   * @author SeungHyeon.Kang
   * @param pageNumber 조회할 페이지 번호
   * @return 사용자 신고 대상의 이용정지 이력 페이지
   */
  const getTargetSuspensions = (pageNumber: number): Promise<PageData<CurrentUserSuspension>> => {
    // 신고 관리 API에서 지정 페이지의 대상 회원 이용정지 이력을 반환한다
    return getComplaintSuspList(cmplNumb, pageNumber)
  }

  /**
   * 공통 이용정지 패널에서 사용자 신고 대상에게 정지를 적용한다
   *
   * @author SeungHyeon.Kang
   * @param request 정지 유형과 사유 및 기간
   * @return 등록된 이용정지 이력
   */
  const setTargetSuspension = (
    request: CurrentUserSuspensionRequest,
  ): Promise<CurrentUserSuspension> => {
    // 회원번호 없이 신고번호와 이용정지 등록값만 신고 관리 API에 전달한다
    return setComplaintSuspension(cmplNumb, request)
  }

  /**
   * 공통 이용정지 패널에서 사용자 신고 대상의 정지를 해제한다
   *
   * @author SeungHyeon.Kang
   * @param spndNumb 정지 이력 번호
   * @param rlesCntn 관리자 내부 해제 메모
   * @return 반환값이 없다
   */
  const uptTargetSuspReleased = (spndNumb: number, rlesCntn: string): Promise<void> => {
    // 회원번호 없이 신고번호와 정지 이력 번호를 신고 관리 API에 전달한다
    return uptComplaintSuspReleased(cmplNumb, spndNumb, rlesCntn)
  }

  /**
   * 피신고자 정보 또는 신고 대상 원본 삭제를 확인한 뒤 최신 상세를 반영한다
   *
   * @author SeungHyeon.Kang
   * @param actionKey 중복 요청을 막을 조치 식별값
   * @param confirmMessage 관리자 확인 문구
   * @param action 실행할 신고 조치 API
   * @param fallbackMessage 알 수 없는 오류의 기본 문구
   * @return 반환값이 없다
   */
  const handleModeration = async (
    actionKey: string,
    confirmMessage: string,
    action: () => Promise<ComplaintDetail>,
    fallbackMessage: string,
  ): Promise<void> => {
    // 삭제 조치는 복원되지 않으므로 실행 전에 관리자 확인을 받는다
    if (!window.confirm(confirmMessage)) {
      // 관리자가 취소한 삭제 조치는 실행하지 않고 종료한다
      return
    }

    // 다른 삭제 버튼의 중복 실행을 막도록 현재 조치 식별값을 설정한다
    setModerating(actionKey)
    // 서버 조치 결과를 현재 신고 상세에 안전하게 반영한다
    try {
      // 서버가 신고 대상과 피신고자를 다시 검증한 조치 결과를 조회한다
      const updatedDetail = await action()
      // 조치 가능 여부와 현재 사용자 정보가 갱신된 상세를 반영한다
      setDetail(updatedDetail)
      // 이전 신고 상세 오류 메시지를 초기화한다
      onError(null)

    }

    // 서버가 거절한 조치 사유 또는 기본 실패 메시지를 화면에 표시한다
    catch (error: unknown) {
      // 조치 실패 원인을 신고 관리 화면 공통 오류 영역에 설정한다
      onError(error instanceof Error ? error.message : fallbackMessage)

    }

    // 성공과 실패 모두에서 다른 관리자 조치를 다시 허용한다
    finally {
      // 현재 조치 식별값을 초기화한다
      setModerating(null)
    }
  }

  /**
   * 동일 대상의 다른 신고 행을 표시한다
   *
   * @author SeungHyeon.Kang
   * @param complaint 표시할 다른 신고
   * @return 동일 대상 신고 행
   */
  const renderRelatedComplaintRow = (complaint: Complaint) => (
    <tr key={complaint.cmplNumb}>
      <td>{complaint.cmplNumb}</td>
      <td>{complaint.cmplStatName ?? complaint.cmplStat}</td>
      <td>{complaint.cmplRsonName ?? complaint.cmplRson}</td>
      <td>{complaint.userNumb ? `${complaint.reporterNick ?? '닉네임 없음'} (${complaint.userNumb})` : '탈퇴한 사용자'}</td>
      <td className="col-date-time">{formatDate(complaint.regiDate)}</td>
    </tr>
  )

  /**
   * 동일 대상에 실제 실행된 자동 조치 결과 행을 표시한다
   *
   * @author SeungHyeon.Kang
   * @param action 표시할 자동 조치 실행 이력
   * @return 자동 조치 실행 이력 행
   */
  const renderAutoActionRow = (action: ComplaintAction) => (
    <tr key={action.actnNumb}>
      <td>{action.actnOrdr}차</td>
      <td>{action.actnTypeName ?? action.actnType}</td>
      <td>{action.rsltCodeName ?? action.rsltCode}</td>
      <td>{action.cmplCntt.toLocaleString()}건 / {action.thrsCntt.toLocaleString()}건</td>
      <td>{action.trigCmpl ?? '-'}</td>
      <td className="complaint-content-cell">{action.rsltCntn || '-'}</td>
      <td className="col-date-time">{formatDate(action.regiDate)}</td>
    </tr>
  )

  // 신고 상세가 도착하기 전에는 고정 높이 안내를 표시한다
  if (loading) {
    // 신고 상세 로딩 화면을 반환한다
    return <section className="empty">신고 상세를 불러오고 있습니다.</section>
  }

  // 상세 조회가 실패한 경우 신고 목록으로 돌아갈 수 있는 안내를 표시한다
  if (!detail) {
    // 신고 상세 조회 실패 화면을 반환한다
    return (
      <section className="detail-panel">
        <p className="empty small">신고 정보를 표시할 수 없습니다.</p>
        <div className="detail-footer">
          <button type="button" className="subtle-button" onClick={() => onMovePath(COMPLAINT_LIST_PATH)}>목록</button>
        </div>
      </section>
    )
  }

  const complaint = detail.complaint
  const isReceived = complaint.cmplStat === 'CMPL_RECEIVED'
  const isReviewing = complaint.cmplStat === 'CMPL_REVIEWING'
  const canFinish = isReviewing && (complaint.procAdmn === adminNumb || adminAuthCode === 'SUPER')
  // 유형별 명칭이 없는 향후 신고 대상은 범용 스냅샷 명칭으로 표시한다
  const targetContentLabel = TARGET_CONTENT_LABELS[complaint.tagtType] ?? '접수 당시 신고 대상 내용'
  // 자동 조치 진행과 실행 이력을 한 영역에서 표시할 상세 정보를 분리한다
  const autoAction = detail.autoAction
  // 원본이 신고 당시 버전으로 실제 노출 중인 경우에만 다음 임계치 정보를 표시한다
  const isAutoActionPending = autoAction.progressStatus === 'PENDING'
  // 서버가 판정한 현재 자동조치 진행 상태를 관리자에게 명확한 문구로 표시한다
  const autoActionProgressLabel = autoAction.progressStatus
    ? AUTO_ACTION_PROGRESS_LABELS[autoAction.progressStatus] ?? autoAction.progressStatus
    : '자동조치 상태 확인 불가'

  // 신고 접수 정보와 처리 영역 및 조건부 이용정지 기능을 반환한다
  return (
    <section className="complaint-page complaint-detail-page">
      {/* 신고 상세 화면 제목 */}
      <section className="content-header">
        <h1>신고 상세</h1>
      </section>

      {/* 신고 접수와 신고자 및 신고 대상 정보 */}
      <section className="detail-panel">
        <div className="detail-title">
          <div>
            <h2>신고 정보</h2>
            {/* "신고자와 피신고자 및 접수 시점의 신고 대상 내용을 함께 확인합니다." */}
            <p>신고자와 피신고자 및 접수 시점의 신고 대상 내용을 함께 확인합니다.</p>
          </div>
        </div>
        <section className="table-wrap menu-info-table complaint-info-table">
          <table>
            {/* 신고 대상과 피신고자 식별정보의 기준 열 너비 */}
            <colgroup>
              <col className="complaint-label-column" />
              <col className="complaint-value-column" />
              <col className="complaint-label-column" />
              <col className="complaint-number-column" />
              <col className="complaint-target-user-column" />
              <col className="complaint-wide-value-column" />
            </colgroup>
            <tbody>
              {/* 신고 접수 식별정보 */}
              <tr>
                <th>신고번호</th>
                <td colSpan={3}>{complaint.cmplNumb}</td>
                <th>접수일시</th>
                <td>{formatDate(complaint.regiDate)}</td>
              </tr>

              {/* 신고자 기본정보 */}
              <tr>
                <th>신고자</th>
                <td colSpan={5}>{complaint.userNumb ? `${complaint.reporterNick ?? '닉네임 없음'} (${complaint.userNumb})` : '탈퇴한 사용자'}</td>
              </tr>

              {/* 신고 사유와 신고자가 작성한 보충 설명 */}
              <tr>
                <th>신고 사유</th>
                <td colSpan={5}>{complaint.cmplRsonName ?? complaint.cmplRson}</td>
              </tr>
              <tr>
                <th>신고자가 작성한 상세 설명</th>
                <td colSpan={5} className="complaint-content-cell">{complaint.cmplCntn || '-'}</td>
              </tr>

              {/* 신고 대상 식별정보와 피신고자 현재정보 */}
              <tr>
                <th>신고 대상 유형</th>
                <td colSpan={5}>
                  {`${complaint.tagtTypeName ?? complaint.tagtType} (${complaint.tagtNumb})`}
                </td>
              </tr>
              <tr>
                <th>피신고자</th>
                <td colSpan={3}>
                  {detail.targetUser
                    ? `${detail.targetUser.userNick} (${detail.targetUser.userNumb})`
                    : complaint.tagtUser
                      ? (
                          <>
                            {/* "회원 정보 없음" */}
                            회원 정보 없음 ({complaint.tagtUser})
                          </>
                        )
                      : (
                          <>
                            {/* "연결 정보 없음" */}
                            연결 정보 없음
                          </>
                        )}
                </td>
                <th>피신고자 회원 상태</th>
                <td>{detail.targetUser?.userStatName ?? detail.targetUser?.userStat ?? '-'}</td>
              </tr>
              <tr>
                <th>피신고자 이미지</th>
                <td colSpan={5}>
                  {detail.targetUser ? (
                    /* 피신고자 프로필 사진과 배경화면 보기 버튼 영역 */
                    <div className="image-preview-actions">
                      {/* "프로필 사진 보기" */}
                      <ImagePreviewButton
                        imagePath={detail.targetUser.profPath}
                        buttonLabel="프로필 사진 보기"
                        dialogTitle={`${detail.targetUser.userNick} 프로필 사진`}
                        emptyLabel="프로필 미등록"
                      />
                      {permission.deltYsno === 'Y' && detail.targetUser.profPath && (
                        <button
                          type="button"
                          className="delete-button"
                          disabled={moderating !== null}
                          onClick={() => void handleModeration(
                            'profile-image',
                            '피신고자의 프로필 사진을 삭제하고 기본 프로필로 변경하시겠습니까?',
                            () => delComplaintProfImage(cmplNumb),
                            '피신고자 프로필 사진을 삭제하지 못했습니다.',
                          )}
                        >
                          {moderating === 'profile-image' ? '삭제 중' : '프로필 사진 삭제'}
                        </button>
                      )}
                      {/* "배경화면 보기" */}
                      <ImagePreviewButton
                        imagePath={detail.targetUser.bgimPath}
                        buttonLabel="배경화면 보기"
                        dialogTitle={`${detail.targetUser.userNick} 배경화면`}
                        emptyLabel="배경 미등록"
                      />
                      {permission.deltYsno === 'Y' && detail.targetUser.bgimPath && (
                        <button
                          type="button"
                          className="delete-button"
                          disabled={moderating !== null}
                          onClick={() => void handleModeration(
                            'background-image',
                            '피신고자의 배경사진을 삭제하고 기본 배경으로 변경하시겠습니까?',
                            () => delComplaintBgimImage(cmplNumb),
                            '피신고자 배경사진을 삭제하지 못했습니다.',
                          )}
                        >
                          {moderating === 'background-image' ? '삭제 중' : '배경사진 삭제'}
                        </button>
                      )}
                    </div>
                  ) : (
                    /* "회원 원본 또는 이미지 정보 없음" */
                    <span>회원 원본 또는 이미지 정보 없음</span>
                  )}
                </td>
              </tr>
              <tr>
                <th>피신고자 자기소개</th>
                <td colSpan={5}>
                  <div className="complaint-moderation-content">
                    <span className="complaint-content-cell">{detail.targetUser?.intrCntn || '등록된 자기소개 없음'}</span>
                    {permission.deltYsno === 'Y' && detail.targetUser?.intrCntn && (
                      <button
                        type="button"
                        className="delete-button"
                        disabled={moderating !== null}
                        onClick={() => void handleModeration(
                          'introduction',
                          '피신고자의 자기소개를 삭제하시겠습니까?',
                          () => delComplaintIntroduction(cmplNumb),
                          '피신고자 자기소개를 삭제하지 못했습니다.',
                        )}
                      >
                        {moderating === 'introduction' ? '삭제 중' : '자기소개 삭제'}
                      </button>
                    )}
                  </div>
                </td>
              </tr>
              <tr>
                <th>{targetContentLabel}</th>
                <td colSpan={5}>
                  <div className="complaint-moderation-content">
                    {/* "내용 없음" */}
                    <span className="complaint-content-cell">{complaint.tagtCntn || '내용 없음'}</span>
                    {/* 프로필 사진 신고는 현재 사진이 아닌 접수 시점의 실제 관리자 전용 증거를 표시한다 */}
                    {complaint.tagtType === 'CMPL_PROF_IMAGE' && (
                      <ImagePreviewButton
                        imagePath={complaint.evidenceAvailable ? `/api/complaints/${complaint.cmplNumb}/evidence` : null}
                        buttonLabel="접수 당시 프로필 사진 보기"
                        dialogTitle={`신고 #${complaint.cmplNumb} 접수 당시 프로필 사진`}
                        emptyLabel="증거 보존기간 만료 또는 증거 없음"
                      />
                    )}
                  </div>
                </td>
              </tr>
              {TARGET_DELETE_LABELS[complaint.tagtType] && (
                <tr>
                  <th>현재 신고 대상 원본</th>
                  <td colSpan={5}>
                    <div className="complaint-moderation-content">
                      <span>{detail.targetContentExists ? '원본이 현재 서비스에 남아 있습니다.' : '원본이 이미 삭제되었습니다.'}</span>
                      {permission.deltYsno === 'Y' && detail.targetContentExists && (
                        <button
                          type="button"
                          className="delete-button"
                          disabled={moderating !== null}
                          onClick={() => void handleModeration(
                            'target-content',
                            TARGET_DELETE_CONFIRM_MESSAGES[complaint.tagtType],
                            () => delComplaintTargetContent(cmplNumb),
                            '신고 대상 원본을 삭제하지 못했습니다.',
                          )}
                        >
                          {moderating === 'target-content' ? '처리 중' : TARGET_DELETE_LABELS[complaint.tagtType]}
                        </button>
                      )}
                    </div>
                  </td>
                </tr>
              )}

              {/* 신고 처리 상태와 담당자 및 완료일시 */}
              <tr>
                <th>처리 상태</th>
                <td>{complaint.cmplStatName ?? complaint.cmplStat}</td>
                <th>담당 관리자</th>
                <td>{complaint.procAdmnName ?? '-'}</td>
                <th>최종 처리일시</th>
                <td>{formatDate(complaint.procDate) || '-'}</td>
              </tr>
            </tbody>
          </table>
        </section>
      </section>

      {/* 동일 대상의 자동 조치 적용 기준과 현재 진행 및 실행 결과 */}
      <section className="detail-panel complaint-auto-action-panel">
        <div className="detail-title">
          <div>
            {/* "자동조치 현황" */}
            <h2>자동조치 현황</h2>
            {/* "반려를 제외한 동일 대상 버전 신고 누적을 기준으로 자동조치 진행 상태를 표시합니다." */}
            <p>반려를 제외한 동일 대상 버전 신고만 합산해 자동조치 진행 상태를 표시합니다.</p>
          </div>
          {/* 자동조치 대상 여부와 현재 원본 상태를 반영한 진행 결과 */}
          <div className="status">
            {autoAction.autoActionTarget && isAutoActionPending ? (
              <>
                {/* "다음 자동조치까지 N건" */}
                다음 자동조치까지 {autoAction.remainingCount.toLocaleString()}건
              </>
            ) : autoAction.autoActionTarget ? (
              <>{autoActionProgressLabel}</>
            ) : (
              <>
                {/* "자동조치 미적용" */}
                자동조치 미적용
              </>
            )}
          </div>
        </div>

        {autoAction.autoActionTarget ? (
          <>
            {/* 자동조치 기준과 누적 진행 요약 */}
            <div className="complaint-auto-action-summary">
              <div>
                {/* "대상 버전" */}
                <span>대상 버전</span>
                <strong>{complaint.tagtHash.slice(0, 12)}</strong>
              </div>
              <div>
                {/* "예정 자동조치" */}
                <span>예정 자동조치</span>
                <strong>{autoAction.actnTypeName ?? autoAction.actnType}</strong>
              </div>
              <div>
                {/* "유효 신고 누적" */}
                <span>유효 신고 누적</span>
                <strong>{autoAction.complaintCount.toLocaleString()}건</strong>
              </div>
              <div>
                {/* "실행 기준" */}
                <span>실행 기준</span>
                <strong>{autoAction.threshold.toLocaleString()}건마다</strong>
              </div>
              {isAutoActionPending ? (
                <div>
                  {/* "다음 실행 시점" */}
                  <span>다음 실행 시점</span>
                  <strong>누적 {autoAction.nextActionCount.toLocaleString()}건</strong>
                </div>
              ) : (
                <div>
                  {/* "현재 진행 상태" */}
                  <span>현재 진행 상태</span>
                  <strong>{autoActionProgressLabel}</strong>
                </div>
              )}
              <div>
                {/* "실행 이력" */}
                <span>실행 이력</span>
                <strong>{autoAction.actionHistories.length.toLocaleString()}회</strong>
              </div>
            </div>

            {/* 실제 자동조치 실행 결과 이력 */}
            <section className="table-wrap complaint-auto-action-table">
              <table>
                <thead>
                  <tr>
                    {/* "조치 순번" */}
                    <th>조치 순번</th>
                    {/* "자동조치" */}
                    <th>자동조치</th>
                    {/* "실행 결과" */}
                    <th>실행 결과</th>
                    {/* "당시 누적 / 기준" */}
                    <th>당시 누적 / 기준</th>
                    {/* "발생 신고번호" */}
                    <th>발생 신고번호</th>
                    {/* "결과 상세" */}
                    <th>결과 상세</th>
                    {/* "실행일시" */}
                    <th className="col-date-time">실행일시</th>
                  </tr>
                </thead>
                <tbody>
                  {autoAction.actionHistories.length === 0 ? (
                    /* "아직 실행된 자동조치가 없습니다." */
                    <tr className="empty-row"><td colSpan={7}>아직 실행된 자동조치가 없습니다.</td></tr>
                  ) : autoAction.actionHistories.map(renderAutoActionRow)}
                </tbody>
              </table>
            </section>
          </>
        ) : (
          /* "이 신고 대상 유형은 자동조치 대상이 아니며 관리자 검토와 수동 조치로 처리합니다." */
          <p className="complaint-auto-action-empty">
            이 신고 대상 유형은 자동조치 대상이 아니며 관리자 검토와 수동 조치로 처리합니다.
          </p>
        )}
      </section>

      {/* 신고 검토 시작과 최종 처리 */}
      <section className="detail-panel">
        <div className="detail-title">
          <div>
            <h2>처리 결과</h2>
            <p>조치 완료와 반려에는 관리자 처리 내용이 필수입니다.</p>
          </div>
        </div>
        <label className="complaint-process-note">
          {/* "관리자 처리 내용" */}
          관리자 처리 내용
          {/* "신고 판단 근거와 실제 조치 내용을 입력해 주세요." */}
          <textarea
            value={processContent}
            maxLength={1000}
            disabled={!isReviewing || !canFinish}
            placeholder="신고 판단 근거와 실제 조치 내용을 입력해 주세요."
            onChange={(event) => setProcessContent(event.target.value)}
          />
        </label>
        {/* 신고 처리 상태 변경 버튼 */}
        <div className="complaint-process-actions">
          {permission.writYsno === 'Y' && isReceived && (
            <button type="button" disabled={saving} onClick={() => void handleStartReview()}>
              {saving ? '처리 중' : '검토 시작'}
            </button>
          )}
          {permission.writYsno === 'Y' && canFinish && (
            <>
              <button type="button" className="subtle-button" disabled={saving} onClick={() => void handleFinishComplaint('CMPL_REJECTED')}>
                반려
              </button>
              <button type="button" disabled={saving} onClick={() => void handleFinishComplaint('CMPL_ACTIONED')}>
                조치 완료
              </button>
            </>
          )}
          {!isReceived && !canFinish && isReviewing && (
            <span className="status">담당 관리자 검토 중</span>
          )}
        </div>
      </section>

      {detail.targetUser && (
        /* 사용자 신고 대상의 이용 정지 등록과 해제 및 이력 */
        <UserSuspensionPanel
          key={cmplNumb}
          contextKey={cmplNumb}
          targetUser={detail.targetUser}
          adminAuthCode={adminAuthCode}
          getSuspensions={getTargetSuspensions}
          createSuspension={setTargetSuspension}
          releaseSuspension={uptTargetSuspReleased}
          onRefreshTargetUser={refreshComplaint}
          onError={onError}
        />
      )}

      {/* 동일 대상 버전의 최근 다른 신고 */}
      <section className="detail-panel">
        <div className="detail-title">
          <div>
            <h2>동일 대상 버전 신고</h2>
            <p>현재 신고를 제외한 최근 10건을 표시합니다.</p>
          </div>
          <div className="status">총 {detail.relatedComplaintCount.toLocaleString()}건</div>
        </div>
        <section className="table-wrap complaint-related-table">
          <table>
            <thead>
              <tr>
                <th>신고번호</th>
                <th>상태</th>
                <th>사유</th>
                <th>신고자</th>
                <th className="col-date-time">접수일시</th>
              </tr>
            </thead>
            <tbody>
              {detail.relatedComplaints.length === 0 ? (
                <tr className="empty-row"><td colSpan={5}>동일 대상 버전의 다른 신고가 없습니다.</td></tr>
              ) : detail.relatedComplaints.map(renderRelatedComplaintRow)}
            </tbody>
          </table>
        </section>
      </section>

      {/* 신고 목록 이동 버튼 */}
      <div className="detail-footer">
        <button type="button" className="subtle-button" onClick={() => onMovePath(COMPLAINT_LIST_PATH)}>목록</button>
      </div>
    </section>
  )
}
