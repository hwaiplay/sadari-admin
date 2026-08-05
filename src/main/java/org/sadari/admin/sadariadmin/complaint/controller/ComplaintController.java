package org.sadari.admin.sadariadmin.complaint.controller;

import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;
import org.sadari.admin.sadariadmin.common.constant.Constant;
import org.sadari.admin.sadariadmin.common.result.ResultData;
import org.sadari.admin.sadariadmin.complaint.service.ComplaintService;
import org.sadari.admin.sadariadmin.complaint.vo.ComplaintSearchVO;
import org.sadari.admin.sadariadmin.complaint.vo.ComplaintUpdateVO;
import org.sadari.admin.sadariadmin.currentuser.vo.CurrentUserSuspensionVO;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * fileName       : ComplaintController
 * author         : SeungHyeon.Kang
 * date           : 2026-08-05
 * description    : 관리자 신고 조회와 처리 및 사용자 신고 대상 이용정지 API를 제공한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-05        SeungHyeon.Kang    최초 생성
 */
@RestController
@RequestMapping(Constant.API_COMPLAINTS_PREFIX)
public class ComplaintController {

    // 신고 조회와 처리 서비스
    private final ComplaintService complaintService;

    /**
     * 관리자 신고 API를 생성한다
     *
     * @author SeungHyeon.Kang
     * @param complaintService 신고 조회와 처리 서비스
     */
    public ComplaintController(ComplaintService complaintService) {

        this.complaintService = complaintService;
    }

    /**
     * 관리자 검색 조건에 맞는 신고 목록을 조회한다
     *
     * @author SeungHyeon.Kang
     * @param search 신고 검색 조건
     * @param admin 로그인 관리자
     * @return 신고 목록 페이지
     */
    @GetMapping
    public ResultData getComplaintList(@ModelAttribute ComplaintSearchVO search
                                      , @AuthenticationPrincipal AdminSessionVO admin) {
        // 검색 조건에 맞는 신고 목록 페이지를 반환한다
        return ResultData.success(complaintService.getComplaintList(search, admin));
    }

    /**
     * 신고번호에 해당하는 관리자 신고 상세를 조회한다
     *
     * @author SeungHyeon.Kang
     * @param cmplNumb 신고 번호
     * @param admin 로그인 관리자
     * @return 신고 상세
     */
    @GetMapping("/{cmplNumb}")
    public ResultData getComplaintDtl(@PathVariable Long cmplNumb
                                    , @AuthenticationPrincipal AdminSessionVO admin) {
        // 신고 처리와 동일 대상 판단 정보를 포함한 상세를 반환한다
        return ResultData.success(complaintService.getComplaintDtl(cmplNumb, admin));
    }

    /**
     * 신고의 검토 시작 또는 최종 처리 상태를 저장한다
     *
     * @author SeungHyeon.Kang
     * @param cmplNumb 신고 번호
     * @param update 변경할 신고 처리 정보
     * @param admin 처리 관리자
     * @return 변경된 신고 상세
     */
    @PatchMapping("/{cmplNumb}")
    public ResultData uptComplaint(@PathVariable Long cmplNumb
                                  , @RequestBody ComplaintUpdateVO update
                                  , @AuthenticationPrincipal AdminSessionVO admin) {
        // 담당자와 처리 상태가 반영된 신고 상세를 반환한다
        return ResultData.success(complaintService.uptComplaint(cmplNumb, update, admin));
    }

    /**
     * 사용자 신고 대상의 관리자 이용정지 이력을 조회한다
     *
     * @author SeungHyeon.Kang
     * @param cmplNumb 신고 번호
     * @param pageNumber 페이지 번호
     * @param admin 로그인 관리자
     * @return 이용정지 이력 페이지
     */
    @GetMapping("/{cmplNumb}/suspensions")
    public ResultData getTargetUserSuspensionList(@PathVariable Long cmplNumb
                                                 , @RequestParam(name = "page", defaultValue = "1") int pageNumber
                                                 , @AuthenticationPrincipal AdminSessionVO admin) {
        // 신고 대상 회원번호를 서버에서 확인한 이용정지 이력 페이지를 반환한다
        return ResultData.success(complaintService.getTargetUserSuspensionList(cmplNumb, pageNumber, admin));
    }

    /**
     * 사용자 신고 대상에게 기간 또는 무기한 이용정지를 적용한다
     *
     * @author SeungHyeon.Kang
     * @param cmplNumb 신고 번호
     * @param suspension 정지 등록값
     * @param admin 처리 관리자
     * @return 등록된 이용정지 이력
     */
    @PostMapping("/{cmplNumb}/suspensions")
    public ResultData setTargetUserSuspension(@PathVariable Long cmplNumb
                                             , @RequestBody CurrentUserSuspensionVO suspension
                                             , @AuthenticationPrincipal AdminSessionVO admin) {
        // 사용자 신고 대상에게 적용된 이용정지 이력을 반환한다
        return ResultData.success(complaintService.setTargetUserSuspension(cmplNumb, suspension, admin));
    }

    /**
     * 사용자 신고 대상의 적용 중인 이용정지를 관리자 해제한다
     *
     * @author SeungHyeon.Kang
     * @param cmplNumb 신고 번호
     * @param spndNumb 정지 이력 번호
     * @param request 해제 메모
     * @param admin 처리 관리자
     * @return 처리 결과
     */
    @PatchMapping("/{cmplNumb}/suspensions/{spndNumb}")
    public ResultData uptTargetUserSuspensionReleased(@PathVariable Long cmplNumb
                                                     , @PathVariable Long spndNumb
                                                     , @RequestBody(required = false) CurrentUserSuspensionVO request
                                                     , @AuthenticationPrincipal AdminSessionVO admin) {
        // 신고 대상 회원번호를 서버에서 확인한 뒤 적용 중인 이용정지를 해제한다
        complaintService.uptTargetUserSuspensionReleased(cmplNumb, spndNumb, request, admin);
        // 이용정지 해제 완료 응답을 반환한다
        return ResultData.success();
    }
}
