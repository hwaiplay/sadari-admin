package org.sadari.admin.sadariadmin.complaint;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;
import org.sadari.admin.sadariadmin.common.code.mapper.CodeMapper;
import org.sadari.admin.sadariadmin.common.constant.Constant;
import org.sadari.admin.sadariadmin.common.exception.BusinessException;
import org.sadari.admin.sadariadmin.common.result.ResultEnum;
import org.sadari.admin.sadariadmin.complaint.mapper.ComplaintMapper;
import org.sadari.admin.sadariadmin.complaint.service.ComplaintService;
import org.sadari.admin.sadariadmin.complaint.service.impl.ComplaintServiceImpl;
import org.sadari.admin.sadariadmin.complaint.vo.ComplaintDetailVO;
import org.sadari.admin.sadariadmin.complaint.vo.ComplaintUpdateVO;
import org.sadari.admin.sadariadmin.complaint.vo.ComplaintVO;
import org.sadari.admin.sadariadmin.currentuser.service.CurrentUserService;
import org.sadari.admin.sadariadmin.currentuser.vo.CurrentUserSuspensionVO;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
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
 */
@ExtendWith(MockitoExtension.class)
class ComplaintServiceTests {

    // 신고 조회와 처리 Mapper 대역
    @Mock
    private ComplaintMapper complaintMapper;

    // 신고 공통코드 검증 Mapper 대역
    @Mock
    private CodeMapper codeMapper;

    // 기존 회원 이용정지 서비스 대역
    @Mock
    private CurrentUserService currentUserService;

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
        complaintService = new ComplaintServiceImpl(complaintMapper, codeMapper, currentUserService);
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
        when(complaintMapper.getRelatedComplaintList(Constant.CMPL_TARGET_USER, 10L, 1L))
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
    void suspendComplaintTargetUser() {
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
     * 콘텐츠 신고 대상 번호를 회원번호로 이용정지하지 못하도록 차단하는지 확인한다
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void rejectNonUserTargetSuspension() {
        // 독후감 대상 번호 10을 가진 콘텐츠 신고를 생성한다
        ComplaintVO complaint = createComplaint("CMPL_BOOK_REPORT", Constant.CMPL_STATUS_REVIEWING
                                                , LocalDateTime.now());
        // 신고번호로 조회되는 독후감 신고 대상을 설정한다
        when(complaintMapper.getComplaintDtl(1L)).thenReturn(complaint);

        // 독후감 번호를 회원번호로 오인하는 이용정지 요청의 예외를 확인한다
        BusinessException exception = assertThrows(BusinessException.class, () ->
            complaintService.setTargetUserSuspension(1L, new CurrentUserSuspensionVO(), createAdminSession())
        );
        // 사용자 신고 대상 아님 결과가 반환되는지 확인한다
        assertEquals(ResultEnum.COMPLAINT_TARGET_NOT_USER, exception.getResultEnum());
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
