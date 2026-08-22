package org.sadari.admin.sadariadmin.complaint.controller;

import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;
import org.sadari.admin.sadariadmin.common.constant.Constant;
import org.sadari.admin.sadariadmin.common.result.ResultData;
import org.sadari.admin.sadariadmin.complaint.service.ComplaintService;
import org.sadari.admin.sadariadmin.complaint.vo.ComplaintEvidenceVO;
import org.sadari.admin.sadariadmin.complaint.vo.ComplaintSearchVO;
import org.sadari.admin.sadariadmin.complaint.vo.ComplaintUpdateVO;
import org.sadari.admin.sadariadmin.currentuser.vo.CurrentUserSuspensionVO;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
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

    // MIME 스니핑을 차단하는 표준 보안 응답 헤더명
    private static final String HEADER_X_CONTENT_TYPE_OPTIONS = "X-Content-Type-Options";

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
     * 신고번호에 연결된 프로필 사진 신고 증거 원본을 관리자에게 제공한다
     *
     * @author SeungHyeon.Kang
     * @param cmplNumb 신고 번호
     * @param admin 로그인 관리자
     * @return 캐시를 금지한 이미지 증거 원본 응답
     */
    @GetMapping("/{cmplNumb}/evidence")
    public ResponseEntity<byte[]> getComplaintEvidence(@PathVariable Long cmplNumb
                                                      , @AuthenticationPrincipal AdminSessionVO admin) {
        // 관리자 권한으로 신고번호에 연결된 만료 전 이미지 증거를 조회한다
        ComplaintEvidenceVO evidence = complaintService.getComplaintEvidence(cmplNumb, admin);
        // 비정상 MIME 값은 브라우저 실행 가능 형식으로 해석되지 않도록 일반 바이너리로 제한한다
        MediaType mediaType;
        try {
            // 저장 당시 검증한 MIME 유형을 응답 Content-Type으로 변환한다
            mediaType = MediaType.parseMediaType(evidence.getMimeType());
            // 사용자 업로드 정책에서 허용한 JPEG와 PNG만 브라우저 표시 이미지로 응답한다
            if (!MediaType.IMAGE_JPEG.isCompatibleWith(mediaType)
                    && !MediaType.IMAGE_PNG.isCompatibleWith(mediaType)) {
                // 그 외 MIME 유형은 실행 또는 스니핑되지 않는 일반 바이너리로 제한한다
                mediaType = MediaType.APPLICATION_OCTET_STREAM;
            }
        } catch (IllegalArgumentException e) {
            // 이전 데이터의 잘못된 MIME 값은 다운로드 가능한 일반 바이너리로 응답한다
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }
        // 관리자 브라우저와 중간 캐시에 민감한 신고 증거가 남지 않도록 no-store를 적용한다
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .header(HEADER_X_CONTENT_TYPE_OPTIONS, "nosniff")
                .contentType(mediaType)
                .body(evidence.getEvdcData());
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
     * 피신고자의 프로필 이미지를 기본 이미지 상태로 변경한다
     *
     * @author SeungHyeon.Kang
     * @param cmplNumb 신고 번호
     * @param admin 처리 관리자
     * @return 변경된 신고 상세
     */
    @DeleteMapping("/{cmplNumb}/target-user/profile-image")
    public ResultData delTargetProfImage(@PathVariable Long cmplNumb
                                         , @AuthenticationPrincipal AdminSessionVO admin) {
        // 프로필 이미지 조치 뒤 현재 피신고자 정보가 갱신된 신고 상세를 반환한다
        return ResultData.success(complaintService.delTargetProfImage(cmplNumb, admin));
    }

    /**
     * 피신고자의 배경 이미지를 기본 이미지 상태로 변경한다
     *
     * @author SeungHyeon.Kang
     * @param cmplNumb 신고 번호
     * @param admin 처리 관리자
     * @return 변경된 신고 상세
     */
    @DeleteMapping("/{cmplNumb}/target-user/background-image")
    public ResultData delTargetBgimImage(@PathVariable Long cmplNumb
                                         , @AuthenticationPrincipal AdminSessionVO admin) {
        // 배경 이미지 조치 뒤 현재 피신고자 정보가 갱신된 신고 상세를 반환한다
        return ResultData.success(complaintService.delTargetBgimImage(cmplNumb, admin));
    }

    /**
     * 피신고자의 자기소개를 삭제한다
     *
     * @author SeungHyeon.Kang
     * @param cmplNumb 신고 번호
     * @param admin 처리 관리자
     * @return 변경된 신고 상세
     */
    @DeleteMapping("/{cmplNumb}/target-user/introduction")
    public ResultData delTargetIntroduction(@PathVariable Long cmplNumb
                                            , @AuthenticationPrincipal AdminSessionVO admin) {
        // 자기소개 조치 뒤 현재 피신고자 정보가 갱신된 신고 상세를 반환한다
        return ResultData.success(complaintService.delTargetIntroduction(cmplNumb, admin));
    }

    /**
     * 신고 유형에 맞는 독후감, 댓글 또는 모임 소개를 삭제한다
     *
     * @author SeungHyeon.Kang
     * @param cmplNumb 신고 번호
     * @param admin 처리 관리자
     * @return 변경된 신고 상세
     */
    @DeleteMapping("/{cmplNumb}/target-content")
    public ResultData delTargetContent(@PathVariable Long cmplNumb
                                       , @AuthenticationPrincipal AdminSessionVO admin) {
        // 유형별 원본 조치 가능 여부가 갱신된 신고 상세를 반환한다
        return ResultData.success(complaintService.delTargetContent(cmplNumb, admin));
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
    public ResultData getTargetUserSuspList(@PathVariable Long cmplNumb
                                                 , @RequestParam(name = "page", defaultValue = "1") int pageNumber
                                                 , @AuthenticationPrincipal AdminSessionVO admin) {
        // 신고 대상 회원번호를 서버에서 확인한 이용정지 이력 페이지를 반환한다
        return ResultData.success(complaintService.getTargetUserSuspList(cmplNumb, pageNumber, admin));
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
    public ResultData uptTargetSuspReleased(@PathVariable Long cmplNumb
                                                     , @PathVariable Long spndNumb
                                                     , @RequestBody(required = false) CurrentUserSuspensionVO request
                                                     , @AuthenticationPrincipal AdminSessionVO admin) {
        // 신고 대상 회원번호를 서버에서 확인한 뒤 적용 중인 이용정지를 해제한다
        complaintService.uptTargetSuspReleased(cmplNumb, spndNumb, request, admin);
        // 이용정지 해제 완료 응답을 반환한다
        return ResultData.success();
    }
}
