package org.sadari.admin.sadariadmin.complaint;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.InOrder;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;
import org.sadari.admin.sadariadmin.common.code.mapper.CodeMapper;
import org.sadari.admin.sadariadmin.common.constant.Constant;
import org.sadari.admin.sadariadmin.complaint.config.ComplaintAutoActionProperties;
import org.sadari.admin.sadariadmin.complaint.mapper.ComplaintMapper;
import org.sadari.admin.sadariadmin.complaint.service.ComplaintService;
import org.sadari.admin.sadariadmin.complaint.service.impl.ComplaintServiceImpl;
import org.sadari.admin.sadariadmin.complaint.vo.ComplaintDetailVO;
import org.sadari.admin.sadariadmin.complaint.vo.ComplaintEvidenceVO;
import org.sadari.admin.sadariadmin.complaint.vo.ComplaintActionVO;
import org.sadari.admin.sadariadmin.complaint.vo.ComplaintUpdateVO;
import org.sadari.admin.sadariadmin.complaint.vo.ComplaintVO;
import org.sadari.admin.sadariadmin.complaint.vo.ComplaintTargetContentVO;
import org.sadari.admin.sadariadmin.complaint.vo.ComplaintTargetFileVO;
import org.sadari.admin.sadariadmin.currentuser.service.CurrentUserService;
import org.sadari.admin.sadariadmin.currentuser.vo.CurrentUserSuspensionVO;
import org.sadari.admin.sadariadmin.currentuser.vo.CurrentUserVO;
import org.sadari.admin.sadariadmin.file.storage.FileStorage;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

/**
 * fileName       : ComplaintServiceTests
 * author         : SeungHyeon.Kang
 * date           : 2026-08-05
 * description    : 신고 상태 처리와 사용자 신고 대상 이용정지 위임 정책을 검증한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-05        SeungHyeon.Kang    최초 생성
 * 2026-08-22        SeungHyeon.Kang    자동·수동 조치와 이미지 증거 검증
 */
@ExtendWith(MockitoExtension.class)
class ComplaintServiceTests {

    // 동일 대상 버전 조회에 사용할 테스트 SHA-256 해시
    private static final String TEST_TARGET_HASH = "a".repeat(64);

    // 신고 조회와 처리 Mapper 대역
    @Mock
    private ComplaintMapper complaintMapper;

    // 신고 공통코드 검증 Mapper 대역
    @Mock
    private CodeMapper codeMapper;

    // 기존 회원 이용정지 서비스 대역
    @Mock
    private CurrentUserService currentUserService;

    // 피신고자 이미지 저장소 대역
    @Mock
    private FileStorage fileStorage;

    // 테스트할 신고 관리 서비스
    private ComplaintService complaintService;

    /**
     * 각 테스트에서 사용할 신고 관리 서비스를 생성한다
     *
     * @author SeungHyeon.Kang
     */
    @BeforeEach
    void setUp() {
        // Mapper와 이용정지 서비스 대역으로 신고 관리 서비스를 생성한다
        complaintService = new ComplaintServiceImpl(complaintMapper, codeMapper, currentUserService, fileStorage
                                                     , new ComplaintAutoActionProperties());
    }

    /**
     * 관리자 로그인 상태에서 신고번호에 연결된 실제 프로필 이미지 증거를 조회하는지 확인한다
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void getComplaintEvidenceReturnsStoredOriginal() {
        // 관리자 전용으로 저장된 이미지 원본과 MIME 유형을 생성한다
        ComplaintEvidenceVO evidence = new ComplaintEvidenceVO();
        evidence.setOrigName("reported-profile.jpg");
        evidence.setMimeType("image/jpeg");
        evidence.setEvdcData(new byte[]{1, 2, 3});
        // 신고번호로 이미지 증거가 조회되도록 설정한다
        when(complaintMapper.getComplaintEvidence(1L)).thenReturn(evidence);

        // 관리자 세션으로 신고 이미지 증거를 조회한다
        ComplaintEvidenceVO result = complaintService.getComplaintEvidence(1L, createAdminSession());

        // 저장된 MIME 유형과 실제 원본 바이트가 변경 없이 반환되는지 확인한다
        assertEquals("image/jpeg", result.getMimeType());
        assertArrayEquals(new byte[]{1, 2, 3}, result.getEvdcData());
    }

    /**
     * 자동 조치 대상의 유효 신고 누적과 다음 기준 및 실행 이력이 상세에 포함되는지 확인한다
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void getComplaintAutoAction() {
        // 5건마다 비공개 전환하는 독후감 신고를 생성한다
        ComplaintVO complaint = createComplaint(Constant.CMPL_TARGET_BOOK_REPORT
                                                , Constant.CMPL_STATUS_RECEIVED, LocalDateTime.now());
        // 현재 노출 중인 독후감과 같은 신고 대상 버전 해시를 설정한다
        complaint.setTagtHash(getTargetHash(Constant.CMPL_TARGET_BOOK_REPORT, "현재 독후감"));
        // 신고 당시 버전과 비교할 현재 독후감 원문을 생성한다
        ComplaintTargetContentVO currentTarget = new ComplaintTargetContentVO();
        // 현재 서비스에 노출 중인 독후감 원문을 설정한다
        currentTarget.setTagtCntn("현재 독후감");
        // 신고 상세 조회 결과를 설정한다
        when(complaintMapper.getComplaintDtl(1L)).thenReturn(complaint);
        // 신고 당시 버전과 동일한 현재 독후감 원문이 조회되도록 설정한다
        when(complaintMapper.getAutoActionTargetDtl(
                Constant.CMPL_TARGET_BOOK_REPORT, 10L, 10L)).thenReturn(currentTarget);
        // 반려를 제외한 동일 대상 유효 신고가 현재 3건임을 설정한다
        when(complaintMapper.getAutoActionCmplCnt(
                Constant.CMPL_TARGET_BOOK_REPORT, 10L, complaint.getTagtHash())).thenReturn(3);
        // 예정 자동 조치의 공통코드 명칭을 설정한다
        when(codeMapper.getCodeName(Constant.CMPL_ACTN, Constant.CMPL_ACTION_HIDE_REPORT))
                .thenReturn("독후감 비공개 전환");
        // 아직 실행되지 않은 자동 조치 이력을 설정한다
        when(complaintMapper.getAutoActionList(
                Constant.CMPL_TARGET_BOOK_REPORT, 10L, complaint.getTagtHash()))
                .thenReturn(List.of());
        // 동일 대상의 다른 신고가 없는 상태를 설정한다
        when(complaintMapper.getRelatedComplaintList(
                Constant.CMPL_TARGET_BOOK_REPORT, 10L, complaint.getTagtHash(), 1L))
                .thenReturn(List.of());

        // 관리자 신고 상세를 조회한다
        ComplaintDetailVO detail = complaintService.getComplaintDtl(1L, createAdminSession());

        // 독후감 신고가 자동 조치 대상으로 표시되는지 확인한다
        assertTrue(detail.getAutoAction().isAutoActionTarget());
        // YML 기본 임계치 5건이 표시되는지 확인한다
        assertEquals(5, detail.getAutoAction().getThreshold());
        // 반려 제외 누적 3건이 표시되는지 확인한다
        assertEquals(3, detail.getAutoAction().getComplaintCount());
        // 다음 자동 조치가 누적 5건에 실행되는지 확인한다
        assertEquals(5, detail.getAutoAction().getNextActionCount());
        // 다음 자동 조치까지 2건이 남았는지 확인한다
        assertEquals(2, detail.getAutoAction().getRemainingCount());
        // 아직 실행된 자동 조치 이력이 없는지 확인한다
        assertTrue(detail.getAutoAction().getActionHistories().isEmpty());
        // 신고 당시 버전 원본이 노출 중인 자동조치 진행 상태인지 확인한다
        assertEquals(Constant.CMPL_PROGRESS_PENDING, detail.getAutoAction().getProgressStatus());
    }

    /**
     * 자동조치로 원본이 비노출된 대상에는 다음 임계치를 표시하지 않는지 확인한다
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void getComplaintAutoActionCompleted() {
        // 자동 비공개 전환이 완료된 독후감 신고를 생성한다
        ComplaintVO complaint = createComplaint(Constant.CMPL_TARGET_BOOK_REPORT
                                                , Constant.CMPL_STATUS_ACTIONED, LocalDateTime.now());
        // 신고 상세 조회 결과를 설정한다
        when(complaintMapper.getComplaintDtl(1L)).thenReturn(complaint);
        // 자동조치 실행 당시 유효 신고 누적 건수를 설정한다
        when(complaintMapper.getAutoActionCmplCnt(
                Constant.CMPL_TARGET_BOOK_REPORT, 10L, TEST_TARGET_HASH)).thenReturn(5);
        // 첫 번째 자동 조치 실행 이력을 생성한다
        ComplaintActionVO action = new ComplaintActionVO();
        // 실행 이력의 자동 조치 순번을 설정한다
        action.setActnOrdr(1);
        // 원본이 비공개된 대상의 자동 조치 실행 이력을 설정한다
        when(complaintMapper.getAutoActionList(
                Constant.CMPL_TARGET_BOOK_REPORT, 10L, TEST_TARGET_HASH)).thenReturn(List.of(action));
        // 동일 대상의 다른 신고가 없는 상태를 설정한다
        when(complaintMapper.getRelatedComplaintList(
                Constant.CMPL_TARGET_BOOK_REPORT, 10L, TEST_TARGET_HASH, 1L)).thenReturn(List.of());

        // 자동조치가 완료된 신고 상세를 조회한다
        ComplaintDetailVO detail = complaintService.getComplaintDtl(1L, createAdminSession());

        // 원본이 사라진 원인을 자동조치 이력으로 판정하는지 확인한다
        assertEquals(Constant.CMPL_PROGRESS_AUTO_ACTIONED, detail.getAutoAction().getProgressStatus());
        // 완료된 대상에는 다음 자동조치 누적 건수를 표시하지 않는지 확인한다
        assertEquals(0, detail.getAutoAction().getNextActionCount());
        // 완료된 대상에는 남은 신고 건수를 표시하지 않는지 확인한다
        assertEquals(0, detail.getAutoAction().getRemainingCount());
    }

    /**
     * 접수 신고가 검토 중 상태와 로그인 관리자로 변경되는지 확인한다
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void startComplaintReview() {
        // 화면이 조회한 신고 수정 시각을 준비한다
        LocalDateTime updtDate = LocalDateTime.of(2026, 8, 5, 10, 0);
        // 접수 상태의 사용자 신고를 생성한다
        ComplaintVO complaint = createComplaint(Constant.CMPL_TARGET_USER, Constant.CMPL_STATUS_RECEIVED, updtDate);
        // 신고 처리 잠금 조회 결과를 설정한다
        when(complaintMapper.getComplaintForUpdate(1L)).thenReturn(complaint);
        // 신고 상태 수정 성공 건수를 설정한다
        when(complaintMapper.uptComplaint(eq(1L), any(ComplaintUpdateVO.class), eq(1L))).thenReturn(1);
        // 수정 뒤 상세 조회 결과를 설정한다
        when(complaintMapper.getComplaintDtl(1L)).thenReturn(complaint);
        // 동일 대상의 다른 신고가 없는 상태를 설정한다
        when(complaintMapper.getRelatedComplaintList(
                Constant.CMPL_TARGET_USER, 10L, TEST_TARGET_HASH, 1L))
            .thenReturn(List.of());

        // 검토 시작 상태와 화면 조회 버전을 요청값에 설정한다
        ComplaintUpdateVO update = new ComplaintUpdateVO();
        // 변경할 신고 상태를 검토 중으로 설정한다
        update.setCmplStat(Constant.CMPL_STATUS_REVIEWING);
        // 화면이 조회한 수정 시각을 동시성 검증값으로 설정한다
        update.setUpdtDate(updtDate);
        // 로그인 관리자로 신고 검토를 시작한다
        ComplaintDetailVO detail = complaintService.uptComplaint(1L, update, createAdminSession());

        // 변경된 신고 상세가 같은 신고번호를 유지하는지 확인한다
        assertEquals(1L, detail.getComplaint().getCmplNumb());
        // 로그인 관리자 번호가 신고 처리 Mapper에 전달되는지 확인한다
        verify(complaintMapper).uptComplaint(1L, update, 1L);
    }

    /**
     * 사용자 신고 대상 번호가 기존 이용정지 서비스의 회원번호로 전달되는지 확인한다
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void setComplaintTargetSusp() {
        // 이용정지 대상 번호가 10번 회원인 사용자 신고를 생성한다
        ComplaintVO complaint = createComplaint(Constant.CMPL_TARGET_USER, Constant.CMPL_STATUS_REVIEWING
                                                , LocalDateTime.now());
        // 신고번호로 조회되는 사용자 신고 대상을 설정한다
        when(complaintMapper.getComplaintDtl(1L)).thenReturn(complaint);
        // 기존 이용정지 서비스가 반환할 등록 이력을 생성한다
        CurrentUserSuspensionVO created = new CurrentUserSuspensionVO();
        // 등록된 정지 이력 번호를 설정한다
        created.setSpndNumb(3L);
        // 기존 이용정지 서비스의 처리 결과를 설정한다
        when(currentUserService.setUserSuspension(eq(10L), any(CurrentUserSuspensionVO.class)
                                                 , any(AdminSessionVO.class))).thenReturn(created);

        // 기간 정지 등록 요청값을 생성한다
        CurrentUserSuspensionVO request = new CurrentUserSuspensionVO();
        // 기존 이용정지 검증에서 사용할 기간 유형을 설정한다
        request.setSpndType(Constant.SPND_TYPE_PERIOD);
        // 기존 이용정지 검증에서 사용할 사유를 설정한다
        request.setSpndRson("POLICY_VIOLATION");
        // 신고번호만 사용하여 사용자 신고 대상에게 이용정지를 적용한다
        CurrentUserSuspensionVO result = complaintService.setTargetUserSuspension(
            1L, request, createAdminSession()
        );

        // 기존 이용정지 서비스의 등록 결과가 그대로 반환되는지 확인한다
        assertEquals(3L, result.getSpndNumb());
        // 신고 대상 번호 10이 회원번호로 전달되는지 확인한다
        verify(currentUserService).setUserSuspension(eq(10L), eq(request), any(AdminSessionVO.class));
    }

    /**
     * 콘텐츠 신고도 저장된 대상 소유 사용자 번호로 이용정지하는지 확인한다
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void setContentComplaintTargetSusp() {
        // 독후감 대상 번호 10을 가진 콘텐츠 신고를 생성한다
        ComplaintVO complaint = createComplaint("CMPL_BOOK_REPORT", Constant.CMPL_STATUS_REVIEWING
                                                , LocalDateTime.now());
        // 독후감 작성자 회원번호를 신고의 피신고자 연결값으로 설정한다
        complaint.setTagtUser(77L);
        // 신고번호로 조회되는 독후감 신고 대상을 설정한다
        when(complaintMapper.getComplaintDtl(1L)).thenReturn(complaint);

        // 독후감 신고에서 저장된 피신고자에게 이용정지를 적용한다
        complaintService.setTargetUserSuspension(1L, new CurrentUserSuspensionVO(), createAdminSession());

        // 독후감 번호가 아닌 저장된 작성자 회원번호가 정지 서비스에 전달되는지 확인한다
        verify(currentUserService).setUserSuspension(eq(77L), any(CurrentUserSuspensionVO.class)
                                                     , any(AdminSessionVO.class));
    }

    /**
     * 콘텐츠 신고도 저장된 대상 소유 사용자 번호로 피신고자 정보를 조회하는지 확인한다
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void getContentComplaintTargetUser() {
        // 독후감 번호와 작성자 회원번호가 다른 콘텐츠 신고를 생성한다
        ComplaintVO complaint = createComplaint("CMPL_BOOK_REPORT", Constant.CMPL_STATUS_RECEIVED
                                                , LocalDateTime.now());
        // 신고 대상 소유 사용자 번호를 설정한다
        complaint.setTagtUser(77L);
        // 신고 상세 조회 결과를 설정한다
        when(complaintMapper.getComplaintDtl(1L)).thenReturn(complaint);
        // 동일 대상의 다른 신고가 없는 상태를 설정한다
        when(complaintMapper.getRelatedComplaintList(
                "CMPL_BOOK_REPORT", 10L, TEST_TARGET_HASH, 1L)).thenReturn(List.of());
        // 관리자 화면에 표시할 피신고자 현재 정보를 생성한다
        CurrentUserVO targetUser = new CurrentUserVO();
        // 조회된 피신고자 회원번호를 설정한다
        targetUser.setUserNumb(77L);
        // 피신고자 정보 조회에 사용할 관리자 세션을 생성한다
        AdminSessionVO admin = createAdminSession();
        // 저장된 대상 소유 사용자 번호의 현재 사용자 조회 결과를 설정한다
        when(currentUserService.getCurrentUserDtl(77L, admin)).thenReturn(targetUser);

        // 독후감 신고 상세를 조회한다
        ComplaintDetailVO detail = complaintService.getComplaintDtl(1L, admin);

        // 독후감 번호가 아닌 저장된 대상 소유 사용자 번호의 회원 정보가 반환되는지 확인한다
        assertEquals(77L, detail.getTargetUser().getUserNumb());
        // 현재 사용자 서비스에 저장된 대상 소유 사용자 번호가 전달되는지 확인한다
        verify(currentUserService).getCurrentUserDtl(77L, admin);
    }

    /**
     * 신고 대상 독후감이 사용자 삭제와 같은 종속 데이터 순서로 완전 삭제되는지 확인한다
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void delTargetReportCompletely() {
        ComplaintVO complaint = createComplaint(
                Constant.CMPL_TARGET_BOOK_REPORT, Constant.CMPL_STATUS_REVIEWING, LocalDateTime.now());
        complaint.setTagtUser(77L);
        when(complaintMapper.getComplaintForUpdate(1L)).thenReturn(complaint);
        when(complaintMapper.delTargetReport(10L, 77L)).thenReturn(1);
        when(complaintMapper.getComplaintDtl(1L)).thenReturn(complaint);
        when(complaintMapper.getRelatedComplaintList(
                Constant.CMPL_TARGET_BOOK_REPORT, 10L, TEST_TARGET_HASH, 1L))
                .thenReturn(List.of());

        ComplaintDetailVO detail = complaintService.delTargetContent(1L, createAdminSession());

        InOrder deletionOrder = inOrder(complaintMapper);
        deletionOrder.verify(complaintMapper).delTargetReportReplyLikes(10L, 77L);
        deletionOrder.verify(complaintMapper).delTagtReportChildReply(10L, 77L);
        deletionOrder.verify(complaintMapper).delTargetReportReplies(10L, 77L);
        deletionOrder.verify(complaintMapper).delTargetReportLikes(10L, 77L);
        deletionOrder.verify(complaintMapper).delTargetReport(10L, 77L);
        deletionOrder.verify(complaintMapper).uptManualComplaints(
                Constant.CMPL_TARGET_BOOK_REPORT, 10L,
                "관리자 원본 수동 조치: 신고 대상 독후감을 완전 삭제. 관련 미처리 신고를 일괄 종결함. 기준 신고번호: 1", 1L);
        assertFalse(detail.isTargetContentExists());
    }

    /**
     * 신고 대상 댓글이 원본 행 삭제가 아닌 삭제 상태 변경으로 처리되는지 확인한다
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void delTargetReplyLogically() {
        ComplaintVO complaint = createComplaint(
                Constant.CMPL_TARGET_REPLY, Constant.CMPL_STATUS_REVIEWING, LocalDateTime.now());
        complaint.setTagtUser(77L);
        when(complaintMapper.getComplaintForUpdate(1L)).thenReturn(complaint);
        when(complaintMapper.delTargetReply(10L, 77L)).thenReturn(1);
        when(complaintMapper.getComplaintDtl(1L)).thenReturn(complaint);
        when(complaintMapper.getRelatedComplaintList(
                Constant.CMPL_TARGET_REPLY, 10L, TEST_TARGET_HASH, 1L))
                .thenReturn(List.of());

        complaintService.delTargetContent(1L, createAdminSession());

        verify(complaintMapper).delTargetReply(10L, 77L);
        // 논리 삭제된 같은 댓글 번호의 모든 미처리 신고가 함께 종결되는지 확인한다
        verify(complaintMapper).uptManualComplaints(
                Constant.CMPL_TARGET_REPLY, 10L,
                "관리자 원본 수동 조치: 신고 대상 댓글을 삭제 상태로 변경. 관련 미처리 신고를 일괄 종결함. 기준 신고번호: 1", 1L);
    }

    /**
     * 프로필 참조와 파일 메타정보를 제거한 뒤 검증된 내부 저장소 파일을 삭제하는지 확인한다
     *
     * @author SeungHyeon.Kang
     * @throws Exception 파일 저장소 검증 실패
     */
    @Test
    void delTargetProfileImage() throws Exception {
        ComplaintVO complaint = createComplaint(
                Constant.CMPL_TARGET_USER, Constant.CMPL_STATUS_REVIEWING, LocalDateTime.now());
        ComplaintTargetFileVO targetFile = new ComplaintTargetFileVO();
        targetFile.setFileNumb(30L);
        targetFile.setStorName("profile.jpg");
        targetFile.setFilePath("/uploads/profile/260822/profile.jpg");
        when(complaintMapper.getComplaintForUpdate(1L)).thenReturn(complaint);
        when(complaintMapper.getTagtProfFileForUpdate(10L)).thenReturn(targetFile);
        when(complaintMapper.delTargetProfileImage(10L, 30L)).thenReturn(1);
        when(complaintMapper.delTagtFileIfUnref(30L)).thenReturn(1);
        when(complaintMapper.getComplaintDtl(1L)).thenReturn(complaint);
        when(complaintMapper.getRelatedComplaintList(
                Constant.CMPL_TARGET_USER, 10L, TEST_TARGET_HASH, 1L))
                .thenReturn(List.of());

        complaintService.delTargetProfImage(1L, createAdminSession());

        verify(fileStorage).delFile("profile/260822/profile.jpg");
        // 현재 프로필 초기화로 해결된 해당 사용자의 프로필 사진 신고를 함께 종결하는지 확인한다
        verify(complaintMapper).uptManualComplaints(
                Constant.CMPL_TARGET_PROFILE_IMAGE, 10L,
                "관리자 원본 수동 조치: 피신고자의 프로필 사진을 초기화. 관련 미처리 신고를 일괄 종결함. 기준 신고번호: 1", 1L);
    }

    /**
     * 신고 접수와 같은 유형 구분자 및 원문으로 테스트 대상 버전 해시를 생성한다
     *
     * @author SeungHyeon.Kang
     * @param tagtType 신고 대상 유형
     * @param tagtCntn 신고 대상 원문
     * @return 소문자 64자리 SHA-256 해시
     */
    private String getTargetHash(String tagtType, String tagtCntn) {
        // 운영과 같은 SHA-256 알고리즘으로 대상 유형과 원문을 해시한다
        try {
            // 대상 버전 계산에 사용할 SHA-256 객체를 생성한다
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            // 서로 다른 대상 유형의 같은 원문을 분리하도록 유형 구분자를 반영한다
            digest.update((tagtType + "\u0000").getBytes(StandardCharsets.UTF_8));
            // 신고 당시 원문 전체를 대상 버전 계산에 반영한다
            digest.update(tagtCntn.getBytes(StandardCharsets.UTF_8));
            // 신고 테이블 저장 형식과 같은 소문자 64자리 해시를 반환한다
            return HexFormat.of().formatHex(digest.digest());
        }

        // 필수 JDK 알고리즘이 없으면 테스트 환경 오류로 즉시 실패시킨다
        catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available.", exception);
        }
    }

    /**
     * 테스트에 사용할 신고를 생성한다
     *
     * @author SeungHyeon.Kang
     * @param tagtType 신고 대상 유형
     * @param cmplStat 신고 처리 상태
     * @param updtDate 신고 수정 시각
     * @return 테스트 신고
     */
    private ComplaintVO createComplaint(String tagtType, String cmplStat, LocalDateTime updtDate) {
        // 신고 상태 처리에 필요한 최소 필드를 담을 객체를 생성한다
        ComplaintVO complaint = new ComplaintVO();
        // 테스트 신고번호를 설정한다
        complaint.setCmplNumb(1L);
        // 대상 유형별 이용정지 분기에 사용할 코드를 설정한다
        complaint.setTagtType(tagtType);
        // 대상 사용자 또는 콘텐츠 번호를 설정한다
        complaint.setTagtNumb(10L);
        // 동일 대상 번호의 버전별 조회를 검증할 해시를 설정한다
        complaint.setTagtHash(TEST_TARGET_HASH);
        // 사용자 신고 기본값과 콘텐츠 신고의 명시적 작성자 연결에 사용할 피신고자 번호를 설정한다
        complaint.setTagtUser(10L);
        // 현재 신고 처리 상태를 설정한다
        complaint.setCmplStat(cmplStat);
        // 화면 조회 버전과 비교할 수정 시각을 설정한다
        complaint.setUpdtDate(updtDate);
        // 상태 처리와 대상 검증에 사용할 신고를 반환한다
        return complaint;
    }

    /**
     * 테스트 요청에 사용할 관리자 세션을 생성한다
     *
     * @author SeungHyeon.Kang
     * @return 신고 처리 권한 확인을 통과할 관리자 세션
     */
    private AdminSessionVO createAdminSession() {
        // 신고 처리자 식별값을 담을 관리자 세션을 생성한다
        AdminSessionVO admin = new AdminSessionVO();
        // 신고 담당자로 저장할 관리자 번호를 설정한다
        admin.setAdmnNumb(1L);
        // 일반 관리자 신고 처리 권한 코드를 설정한다
        admin.setAuthCode("ADMIN");
        // 완성된 테스트 관리자 세션을 반환한다
        return admin;
    }
}
