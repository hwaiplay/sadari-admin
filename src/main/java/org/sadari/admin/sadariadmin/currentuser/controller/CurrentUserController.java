package org.sadari.admin.sadariadmin.currentuser.controller;

import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;
import org.sadari.admin.sadariadmin.common.constant.Constant;
import org.sadari.admin.sadariadmin.common.result.ResultData;
import org.sadari.admin.sadariadmin.currentuser.service.CurrentUserService;
import org.sadari.admin.sadariadmin.currentuser.vo.CurrentUserSearchVO;
import org.sadari.admin.sadariadmin.currentuser.vo.CurrentUserSuspensionVO;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * fileName       : CurrentUserController
 * author         : SeungHyeon.Kang
 * date           : 2026-07-30
 * description    : 현재 사용자 조회와 로그인·계정 처리 이력 확인 API를 제공한다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-30        SeungHyeon.Kang    최초 생성
 * 2026-08-13        SeungHyeon.Kang    삭제 회원의 유효 제재 목록과 해제 API 추가
 */
@RestController
@RequestMapping(Constant.API_CURRENT_USERS_PREFIX)
public class CurrentUserController {

    // 현재 사용자 조회 서비스
    private final CurrentUserService currentUserService;

    /**
     * 현재 사용자 조회 API를 생성한다.
     *
     * @author SeungHyeon.Kang
     * @param currentUserService 현재 사용자 조회 서비스
     */
    public CurrentUserController(CurrentUserService currentUserService) {
        this.currentUserService = currentUserService;
    }

    /**
     * 현재 사용자 목록을 검색한다.
     *
     * @author SeungHyeon.Kang
     * @param search 검색 조건
     * @param admin 로그인한 관리자
     * @return 현재 사용자 목록
     */
    @GetMapping
    public ResultData getCurrentUserList(
        @ModelAttribute CurrentUserSearchVO search
        , @AuthenticationPrincipal AdminSessionVO admin
    ) {
        // 검색 조건에 맞는 현재 사용자 목록을 반환한다.
        return ResultData.success(currentUserService.getCurrentUserList(search, admin));
    }

    /**
     * 현재 사용자 상세 정보를 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 사용자 번호
     * @param admin 로그인한 관리자
     * @return 현재 사용자 상세 정보
     */
    @GetMapping("/{userNumb}")
    public ResultData getCurrentUserDtl(
        @PathVariable Long userNumb
        , @AuthenticationPrincipal AdminSessionVO admin
    ) {
        // 사용자 번호에 해당하는 상세 정보를 반환한다.
        return ResultData.success(currentUserService.getCurrentUserDtl(userNumb, admin));
    }

    /**
     * 현재 사용자의 로그인 이력을 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 사용자 번호
     * @param pageNumber 페이지 번호
     * @param admin 로그인한 관리자
     * @return 로그인 이력
     */
    @GetMapping("/{userNumb}/login-histories")
    public ResultData getLoginHistoryList(
        @PathVariable Long userNumb
        , @RequestParam(name = "page", defaultValue = "1") int pageNumber
        , @AuthenticationPrincipal AdminSessionVO admin
    ) {
        // 마스킹된 로그인 이력을 반환한다.
        return ResultData.success(currentUserService.getLoginHistoryList(userNumb, pageNumber, admin));
    }

    /**
     * 현재 사용자의 계정 처리 이력을 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 사용자 번호
     * @param pageNumber 페이지 번호
     * @param admin 로그인한 관리자
     * @return 계정 처리 이력
     */
    @GetMapping("/{userNumb}/withdrawal-histories")
    public ResultData getWithdrawalHistoryList(
        @PathVariable Long userNumb
        , @RequestParam(name = "page", defaultValue = "1") int pageNumber
        , @AuthenticationPrincipal AdminSessionVO admin
    ) {
        // 비활성화와 영구탈퇴 계정 처리 이력을 반환한다.
        return ResultData.success(currentUserService.getWithdrawalHistoryList(userNumb, pageNumber, admin));
    }

    /**
     * 현재 사용자의 관리자 이용 정지 이력을 조회한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 회원 번호
     * @param pageNumber 페이지 번호
     * @param admin 로그인 관리자
     * @return 이용 정지 이력
     */
    @GetMapping("/{userNumb}/suspensions")
    public ResultData getSuspensionHistoryList(
        @PathVariable Long userNumb
        , @RequestParam(name = "page", defaultValue = "1") int pageNumber
        , @AuthenticationPrincipal AdminSessionVO admin
    ) {
        // 관리자에게 회원 이용 정지 이력 페이지를 반환한다
        return ResultData.success(currentUserService.getSuspensionHistoryList(userNumb, pageNumber, admin));
    }

    /**
     * 현재 사용자에게 기간 또는 무기한 이용 정지를 적용한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 회원 번호
     * @param suspension 정지 등록값
     * @param admin 처리 관리자
     * @return 등록된 정지 이력
     */
    @PostMapping("/{userNumb}/suspensions")
    public ResultData setUserSuspension(
        @PathVariable Long userNumb
        , @RequestBody CurrentUserSuspensionVO suspension
        , @AuthenticationPrincipal AdminSessionVO admin
    ) {
        // 검증과 권한 확인이 끝난 회원 이용 정지 등록 결과를 반환한다
        return ResultData.success(currentUserService.setUserSuspension(userNumb, suspension, admin));
    }

    /**
     * 현재 사용자의 적용 중인 이용 정지를 관리자 해제한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 회원 번호
     * @param spndNumb 정지 이력 번호
     * @param request 해제 메모
     * @param admin 처리 관리자
     * @return 처리 결과
     */
    @PatchMapping("/{userNumb}/suspensions/{spndNumb}")
    public ResultData uptUserSuspensionReleased(
        @PathVariable Long userNumb
        , @PathVariable Long spndNumb
        , @RequestBody(required = false) CurrentUserSuspensionVO request
        , @AuthenticationPrincipal AdminSessionVO admin
    ) {
        // 관리자 요청의 회원 이용 정지를 해제한다
        currentUserService.uptUserSuspensionReleased(userNumb, spndNumb, request, admin);
        // 정지 해제 완료 응답을 반환한다
        return ResultData.success();
    }

    /**
     * 물리 삭제된 회원에게 남아 있는 유효 제재 목록을 조회한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 검색할 과거 회원 번호
     * @param pageNumber 페이지 번호
     * @param admin 로그인 관리자
     * @return 삭제 회원의 유효 제재 목록
     */
    @GetMapping("/deleted-suspensions")
    public ResultData getDeletedSuspensionList(
        @RequestParam(required = false) Long userNumb
        , @RequestParam(name = "page", defaultValue = "1") int pageNumber
        , @AuthenticationPrincipal AdminSessionVO admin
    ) {
        // 과거 회원 번호만 노출하고 OAuth 식별값은 제외한 제재 목록을 반환한다
        return ResultData.success(currentUserService.getDeletedSuspensionList(userNumb, pageNumber, admin));
    }

    /**
     * 물리 삭제된 회원에게 남아 있는 유효 제재를 해제한다
     *
     * @author SeungHyeon.Kang
     * @param spndNumb 제재 이력 번호
     * @param request 과거 회원 번호와 필수 해제 메모
     * @param admin 처리 관리자
     * @return 처리 결과
     */
    @PatchMapping("/deleted-suspensions/{spndNumb}")
    public ResultData uptDeletedSuspReleased(
        @PathVariable Long spndNumb
        , @RequestBody CurrentUserSuspensionVO request
        , @AuthenticationPrincipal AdminSessionVO admin
    ) {
        // 삭제 회원 제재 이력에 관리자와 일시 및 해제 근거를 기록한다
        currentUserService.uptDeletedSuspReleased(request.getUserNumb(), spndNumb, request, admin);
        // 삭제 회원 제재 해제 완료 응답을 반환한다
        return ResultData.success();
    }
}
