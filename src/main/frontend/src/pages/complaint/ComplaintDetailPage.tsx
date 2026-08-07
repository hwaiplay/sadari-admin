import { useEffect, useState } from 'react'
import {
  createComplaintTargetSuspension,
  getComplaint,
  getComplaintTargetSuspensions,
  releaseComplaintTargetSuspension,
  updateComplaint,
} from '../../api/complaintApi'
import { ImagePreviewButton } from '../../components/ImagePreviewButton'
import { UserSuspensionPanel } from '../../components/UserSuspensionPanel'
import { COMPLAINT_LIST_PATH } from '../../constants/routes'
import { useMenuPermission } from '../../contexts/useMenuPermission'
import type { PageData } from '../../types/common'
import type { Complaint, ComplaintDetail } from '../../types/complaint'
import type { CurrentUserSuspension, CurrentUserSuspensionRequest } from '../../types/currentUser'
import { formatDate } from '../../utils/code'

type ComplaintDetailPageProps = {
  cmplNumb: number
  adminNumb: number
  adminAuthCode: string
  onMovePath: (path: string) => void
  onError: (message: string | null) => void
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
    return getComplaintTargetSuspensions(cmplNumb, pageNumber)
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
    return createComplaintTargetSuspension(cmplNumb, request)
  }

  /**
   * 공통 이용정지 패널에서 사용자 신고 대상의 정지를 해제한다
   *
   * @author SeungHyeon.Kang
   * @param spndNumb 정지 이력 번호
   * @param rlesCntn 관리자 내부 해제 메모
   * @return 반환값이 없다
   */
  const uptTargetSuspensionReleased = (spndNumb: number, rlesCntn: string): Promise<void> => {
    // 회원번호 없이 신고번호와 정지 이력 번호를 신고 관리 API에 전달한다
    return releaseComplaintTargetSuspension(cmplNumb, spndNumb, rlesCntn)
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

  // 신고 접수 정보와 처리 영역 및 조건부 이용정지 기능을 반환한다
  return (
    <section className="complaint-page complaint-detail-page">
      {/* 신고 상세 화면 제목과 현재 처리 상태 */}
      <section className="content-header">
        <h1>신고 상세</h1>
        <span className={`complaint-status ${complaint.cmplStat.toLowerCase()}`}>
          {complaint.cmplStatName ?? complaint.cmplStat}
        </span>
      </section>

      {/* 신고 접수와 신고자 정보 */}
      <section className="detail-panel">
        <div className="detail-title">
          <div>
            <h2>신고 정보</h2>
            <p>신고자와 신고 대상은 내부 번호를 기준으로 확인합니다.</p>
          </div>
        </div>
        <section className="table-wrap menu-info-table complaint-info-table">
          <table>
            <tbody>
              <tr>
                <th>신고번호</th>
                <td>{complaint.cmplNumb}</td>
                <th>처리 상태</th>
                <td>{complaint.cmplStatName ?? complaint.cmplStat}</td>
                <th>접수일시</th>
                <td>{formatDate(complaint.regiDate)}</td>
              </tr>
              <tr>
                <th>신고자</th>
                <td>{complaint.userNumb ? `${complaint.reporterNick ?? '닉네임 없음'} (${complaint.userNumb})` : '탈퇴한 사용자'}</td>
                <th>신고 사유</th>
                <td>{complaint.cmplRsonName ?? complaint.cmplRson}</td>
                <th>담당 관리자</th>
                <td>{complaint.procAdmnName ?? '-'}</td>
              </tr>
              <tr>
                <th>신고자 사진</th>
                <td colSpan={5}>
                  {/* 신고자 프로필 사진과 배경화면 보기 버튼 영역 */}
                  <div className="image-preview-actions">
                    {/* "프로필 사진 보기" */}
                    <ImagePreviewButton
                      imagePath={complaint.reporterProfPath}
                      buttonLabel="프로필 사진 보기"
                      dialogTitle={`${complaint.reporterNick ?? '탈퇴한 사용자'} 프로필 사진`}
                      emptyLabel="프로필 미등록"
                    />
                    {/* "배경화면 보기" */}
                    <ImagePreviewButton
                      imagePath={complaint.reporterBgimPath}
                      buttonLabel="배경화면 보기"
                      dialogTitle={`${complaint.reporterNick ?? '탈퇴한 사용자'} 배경화면`}
                      emptyLabel="배경 미등록"
                    />
                  </div>
                </td>
              </tr>
              <tr>
                <th>대상 유형</th>
                <td>{complaint.tagtTypeName ?? complaint.tagtType}</td>
                <th>대상번호</th>
                <td>{complaint.tagtNumb}</td>
                <th>처리일시</th>
                <td>{formatDate(complaint.procDate) || '-'}</td>
              </tr>
              <tr>
                <th>신고 내용</th>
                <td colSpan={5} className="complaint-content-cell">{complaint.cmplCntn || '-'}</td>
              </tr>
            </tbody>
          </table>
        </section>
      </section>

      {/* 신고 대상 기본정보 */}
      <section className="detail-panel">
        <div className="detail-title">
          <div>
            <h2>신고 대상</h2>
            <p>다른 관리 화면으로 이동하지 않고 신고 대상 번호를 기준으로 처리합니다.</p>
          </div>
        </div>
        {detail.targetUser ? (
          <section className="table-wrap menu-info-table complaint-target-user-table">
            <table>
              <tbody>
                <tr>
                  <th>회원번호</th>
                  <td>{detail.targetUser.userNumb}</td>
                  <th>닉네임</th>
                  <td>{detail.targetUser.userNick}</td>
                  <th>회원 상태</th>
                  <td>{detail.targetUser.userStatName ?? detail.targetUser.userStat}</td>
                </tr>
                <tr>
                  <th>사용자 사진</th>
                  <td colSpan={5}>
                    {/* 신고 대상 프로필 사진과 배경화면 보기 버튼 영역 */}
                    <div className="image-preview-actions">
                      {/* "프로필 사진 보기" */}
                      <ImagePreviewButton
                        imagePath={detail.targetUser.profPath}
                        buttonLabel="프로필 사진 보기"
                        dialogTitle={`${detail.targetUser.userNick} 프로필 사진`}
                        emptyLabel="프로필 미등록"
                      />
                      {/* "배경화면 보기" */}
                      <ImagePreviewButton
                        imagePath={detail.targetUser.bgimPath}
                        buttonLabel="배경화면 보기"
                        dialogTitle={`${detail.targetUser.userNick} 배경화면`}
                        emptyLabel="배경 미등록"
                      />
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </section>
        ) : (
          <p className="empty small">
            {complaint.tagtType === 'CMPL_USER' ? '삭제된 사용자 대상입니다.' : `대상번호 ${complaint.tagtNumb}`}
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

      {complaint.tagtType === 'CMPL_USER' && detail.targetUser && (
        /* 사용자 신고 대상의 이용 정지 등록과 해제 및 이력 */
        <UserSuspensionPanel
          key={cmplNumb}
          contextKey={cmplNumb}
          targetUser={detail.targetUser}
          adminAuthCode={adminAuthCode}
          getSuspensions={getTargetSuspensions}
          createSuspension={setTargetSuspension}
          releaseSuspension={uptTargetSuspensionReleased}
          onRefreshTargetUser={refreshComplaint}
          onError={onError}
        />
      )}

      {/* 동일 대상의 최근 다른 신고 */}
      <section className="detail-panel">
        <div className="detail-title">
          <div>
            <h2>동일 대상 신고</h2>
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
                <tr className="empty-row"><td colSpan={5}>동일 대상의 다른 신고가 없습니다.</td></tr>
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
