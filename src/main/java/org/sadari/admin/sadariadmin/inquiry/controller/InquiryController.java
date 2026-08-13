package org.sadari.admin.sadariadmin.inquiry.controller;

import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;
import org.sadari.admin.sadariadmin.common.constant.Constant;
import org.sadari.admin.sadariadmin.common.result.ResultData;
import org.sadari.admin.sadariadmin.inquiry.service.InquiryService;
import org.sadari.admin.sadariadmin.inquiry.vo.InquiryActionVO;
import org.sadari.admin.sadariadmin.inquiry.vo.InquirySearchVO;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * fileName       : InquiryController
 * author         : SeungHyeon.Kang
 * date           : 2026-08-13
 * description    : 관리자 고객문의 목록과 상세 및 답변 API를 제공한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-13        SeungHyeon.Kang    최초 생성
 */
@RestController
@RequestMapping(Constant.API_INQUIRIES_PREFIX)
public class InquiryController {

    // 관리자 고객문의 업무 서비스
    private final InquiryService inquiryService;

    /**
     * 관리자 고객문의 API를 생성한다
     *
     * @author SeungHyeon.Kang
     * @param inquiryService 관리자 고객문의 업무 서비스
     */
    public InquiryController(InquiryService inquiryService) {

        this.inquiryService = inquiryService;
    }

    @GetMapping
    public ResultData getInquiryList(@ModelAttribute InquirySearchVO search
                                    , @AuthenticationPrincipal AdminSessionVO admin) {

        // 검색 조건에 맞는 고객문의 목록 페이지를 반환한다
        return ResultData.success(inquiryService.getInquiryList(search, admin));
    }

    @GetMapping("/{inqrNumb}")
    public ResultData getInquiryDtl(@PathVariable Long inqrNumb
                                   , @AuthenticationPrincipal AdminSessionVO admin) {

        // 고객문의 본문과 관리자 답변을 반환한다
        return ResultData.success(inquiryService.getInquiryDtl(inqrNumb, admin));
    }

    @PatchMapping("/{inqrNumb}/review")
    public ResultData uptInquiryReviewing(@PathVariable Long inqrNumb, @RequestBody InquiryActionVO action
                                         , @AuthenticationPrincipal AdminSessionVO admin) {

        // 담당자와 검토 중 상태가 반영된 고객문의를 반환한다
        return ResultData.success(inquiryService.uptInquiryReviewing(inqrNumb, action, admin));
    }

    @PostMapping("/{inqrNumb}/answers")
    public ResultData setInquiryAnswer(@PathVariable Long inqrNumb, @RequestBody InquiryActionVO action
                                      , @AuthenticationPrincipal AdminSessionVO admin) {

        // 답변 완료 상태와 등록 답변이 반영된 고객문의를 반환한다
        return ResultData.success(inquiryService.setInquiryAnswer(inqrNumb, action, admin));
    }

    @PatchMapping("/{inqrNumb}/suspension/release")
    public ResultData uptInquirySuspensionReleased(@PathVariable Long inqrNumb
                                                   , @RequestBody(required = false) InquiryActionVO action
                                                   , @AuthenticationPrincipal AdminSessionVO admin) {

        inquiryService.uptInquirySuspensionReleased(inqrNumb, action, admin);
        // 고객문의에 연결된 이용정지 해제 완료 응답을 반환한다
        return ResultData.success();
    }
}
